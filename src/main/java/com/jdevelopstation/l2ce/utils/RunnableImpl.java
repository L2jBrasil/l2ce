package com.jdevelopstation.l2ce.utils;

import org.apache.log4j.Logger;

/**
 * Author: VISTALL
 * Date:  23:29/20.12.2010
 */
public abstract class RunnableImpl implements Runnable
{
	protected static final Logger _log = Logger.getLogger(RunnableImpl.class);

	protected abstract void runImpl() throws Throwable;

	@Override
	public final void run()
	{
		try
		{
			runImpl();
		}
		catch(Throwable t)
		{
			_log.info("Throwable: " + t, t);
		}
	}
}
