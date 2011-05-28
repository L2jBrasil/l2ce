package com.jdevelopstation.l2ce.version.node.file.impl;

import java.nio.ByteBuffer;

import com.jdevelopstation.l2ce.version.node.data.ClientDataNode;
import com.jdevelopstation.l2ce.version.node.file.ClientFileNode;
import com.jdevelopstation.l2ce.version.node.ClientNodeContainer;
import com.jdevelopstation.l2ce.version.node.data.impl.ClientDataNodeImpl;
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

	public ClientFileNodeImpl(String name, String value, String reader)
	{
		_name = name;
		_value = value;
		try
		{
			Class<?> clazz = Class.forName("com.jdevelopstation.l2ce.version.node.file.reader." + reader);
			_partType = (ReadWriteType)clazz.newInstance();
		}
		catch(Exception e)
		{
			throw new Error(e);
		}
	}

	@Override
	public void parse(ClientNodeContainer<ClientDataNode> parent, ByteBuffer buff)
	{
		if(_value != null)
			return;

		ClientDataNodeImpl node = new ClientDataNodeImpl(this);
		node.setValue(_partType.read(buff));

		parent.add(node);
	}

	@Override
	public String getName()
	{
		return _name;
	}

	public ReadWriteType getReaderType()
	{
		return _partType;
	}

	public String getValue()
	{
		return _value;
	}
}
