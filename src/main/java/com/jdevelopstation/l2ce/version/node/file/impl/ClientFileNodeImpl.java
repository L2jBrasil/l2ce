package com.jdevelopstation.l2ce.version.node.file.impl;

import java.io.File;
import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import com.jdevelopstation.l2ce.gui.etc.FileLoadInfo;
import com.jdevelopstation.l2ce.version.node.ClientNodeAttribute;
import com.jdevelopstation.l2ce.version.node.ClientNodeContainer;
import com.jdevelopstation.l2ce.version.node.data.ClientData;
import com.jdevelopstation.l2ce.version.node.data.ClientDataNode;
import com.jdevelopstation.l2ce.version.node.data.impl.ClientDataBlockNodeImpl;
import com.jdevelopstation.l2ce.version.node.data.impl.ClientDataForNodeImpl;
import com.jdevelopstation.l2ce.version.node.data.impl.ClientDataNodeImpl;
import com.jdevelopstation.l2ce.version.node.file.ClientFileNode;
import com.jdevelopstation.l2ce.version.node.file.ClientFileNodeContext;
import com.jdevelopstation.l2ce.version.node.file.ClientFileNodeModifier;
import com.jdevelopstation.l2ce.version.node.file.reader.ReadWriteType;

/**
 * @author VISTALL
 * @date 8:07/18.05.2011
 */
public class ClientFileNodeImpl implements ClientFileNode
{
	private ReadWriteType<?> _partType;
	private String _name;
	private String _value;
	private ClientFileNodeModifier _modifier;
	private boolean hex;
	private final List<ClientNodeAttribute> myAttributes;

	public ClientFileNodeImpl(String name, String value, String reader, ClientFileNodeModifier modifier, boolean hex, List<ClientNodeAttribute> attributes)
	{
		_name = name;
		_value = value;
		_modifier = modifier;
		this.hex = hex;
		myAttributes = attributes;
		try
		{
			Class<?> clazz = Class.forName("com.jdevelopstation.l2ce.version.node.file.reader." + reader);
			_partType = (ReadWriteType) clazz.newInstance();
		}
		catch(Exception e)
		{
			throw new Error(e);
		}
	}

	public List<ClientNodeAttribute> getAttributes()
	{
		return myAttributes;
	}

	private Map<String, String> buildArguments(ClientFileNodeContext context)
	{
		Map<String, String> map = new HashMap<>();
		for(ClientDataNode dataNode : context.getFileNode())
		{
			if(dataNode instanceof ClientDataForNodeImpl)
			{
				for(ClientDataNode clientDataNode : (ClientDataForNodeImpl) dataNode)
				{
					if(clientDataNode instanceof ClientDataBlockNodeImpl)
					{
						ClientDataNode indexNode = ((ClientDataBlockNodeImpl) clientDataNode).getNodeByName("index");
						ClientDataNode nameNode = ((ClientDataBlockNodeImpl) clientDataNode).getNodeByName("name");

						if(indexNode != null && nameNode != null)
						{
							map.put(String.valueOf(indexNode.getValue()), String.valueOf(nameNode.getValue()));
						}
					}
				}
			}
		}

		return map;
	}

	@Override
	public void parse(ClientNodeContainer<ClientDataNode> parent, ByteBuffer buff, long index, Set<FileLoadInfo> fileLoadInfos, ClientFileNodeContext context)
	{
		ClientDataNodeImpl node = new ClientDataNodeImpl(this);
		node.setValue(_value != null ? _value : _partType.read(buff));

		parent.add(node);

		if(!myAttributes.isEmpty())
		{
			for(ClientNodeAttribute attribute : myAttributes)
			{
				String expression = attribute.getExpression();
				if("$i".equals(expression))
				{
					node.setAttribute(attribute.getName(), String.valueOf(index));
				}
				else if("$l2skillargument".equals(expression))
				{
					Object value = node.getValue();
					if(!(value instanceof Number))
					{
						continue;
					}

					Map<String, String> arguments = context.get("arguments");
					if(arguments == null)
					{
						context.put("arguments", arguments = buildArguments(context));
					}

					String arg = arguments.get(String.valueOf(value));
					if(arg != null)
					{
						node.setAttribute(attribute.getName(), arg);
					}
				}
				else if("$l2gamedataname".equals(expression))
				{
					Object value = node.getValue();
					if(!(value instanceof Number))
					{
						continue;
					}

					int intValue = ((Number) value).intValue();

					ClientData loadedClientData = null;
					for(FileLoadInfo info : fileLoadInfos)
					{
						File file = info.getFile();
						if(StringUtils.containsIgnoreCase(file.getName(), "l2gamedataname"))
						{
							ClientData clientData = info.getClientData();
							if(clientData != null)
							{
								loadedClientData = clientData;
								break;
							}
						}
					}

					if(loadedClientData == null)
					{
						continue;
					}

					for(ClientDataNode dataNode : loadedClientData.getNodes())
					{
						if(dataNode instanceof ClientDataForNodeImpl)
						{
							List<ClientDataNode> nodes = ((ClientDataForNodeImpl) dataNode).getNodes();

							if(intValue < nodes.size() && intValue >= 0)
							{
								ClientDataNode l2gameValue = nodes.get(intValue);

								if(!(l2gameValue instanceof ClientDataBlockNodeImpl))
								{
									break;
								}

								List<ClientDataNode> children = ((ClientDataBlockNodeImpl) l2gameValue).getNodes();
								if(children.isEmpty())
								{
									break;
								}

								ClientDataNode child = children.get(0);
								node.setAttribute(attribute.getName(), String.valueOf(child.getValue()));
							}
						}
					}
				}
				else
				{
					throw new UnsupportedOperationException();
				}
			}
		}
	}

	@Override
	public String getName()
	{
		return _name;
	}

	@Override
	public ReadWriteType getReaderType()
	{
		return _partType;
	}

	@Override
	public String getValue()
	{
		return _value;
	}

	@Override
	public boolean isHex()
	{
		return hex;
	}

	public ClientFileNodeModifier getModifier()
	{
		return _modifier;
	}
}
