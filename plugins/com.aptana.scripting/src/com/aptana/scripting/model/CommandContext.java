package com.aptana.scripting.model;

import java.io.InputStream;
import java.io.OutputStream;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtension;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.IExtensionRegistry;
import org.eclipse.core.runtime.Platform;
import org.jruby.runtime.builtin.IRubyObject;

import com.aptana.scripting.Activator;
import com.aptana.scripting.ScriptingEngine;

public class CommandContext
{
	// Key names for well known keys in the map
	public static final String WORKING_DIRECTORY = "working_directory"; //$NON-NLS-1$
	public static final String INPUT_TYPE = "input_type"; //$NON-NLS-1$
	public static final String INVOKED_VIA = "invoked_via"; //$NON-NLS-1$

	private static final String CONTEXT_CONTRIBUTOR_ID = "contextContributors"; //$NON-NLS-1$
	private static final String TAG_CONTRIBUTOR = "contributor"; //$NON-NLS-1$
	private static final String ATTR_CLASS = "class"; //$NON-NLS-1$
	
	private static ContextContributor[] contextContributors;
	
	private Map<String,Object> _map;
	private InputStream _inputStream;
	private OutputStream _outputStream;
	private OutputStream _errorStream;
	private OutputStream _consoleStream;
	private OutputType _outputType;
	private boolean _forcedExit;

	/**
	 * getContextContributors
	 * 
	 * @return
	 */
	public static ContextContributor[] getContextContributors()
	{
		if (contextContributors == null)
		{
			IExtensionRegistry registry = Platform.getExtensionRegistry();
			List<ContextContributor> contributors = new ArrayList<ContextContributor>();

			if (registry != null)
			{
				IExtensionPoint extensionPoint = registry.getExtensionPoint(Activator.PLUGIN_ID, CONTEXT_CONTRIBUTOR_ID);

				if (extensionPoint != null)
				{
					IExtension[] extensions = extensionPoint.getExtensions();

					for (IExtension extension : extensions)
					{
						IConfigurationElement[] elements = extension.getConfigurationElements();

						for (IConfigurationElement element : elements)
						{
							if (element.getName().equals(TAG_CONTRIBUTOR))
							{
								try
								{
									ContextContributor contributor = (ContextContributor) element.createExecutableExtension(ATTR_CLASS);
									
									contributors.add(contributor);
								}
								catch (CoreException e)
								{
									String message = MessageFormat.format(
										Messages.CommandElement_Error_Creating_Contributor,
										new Object[] { e.getMessage() }
									);
									
									Activator.logError(message, e);
								}
							}
						}
					}
				}
			}

			contextContributors = contributors.toArray(new ContextContributor[contributors.size()]);
		}
		
		return contextContributors;
	}
	
	/**
	 * CommandContext
	 */
	CommandContext(CommandElement command)
	{
		this._map = new HashMap<String,Object>();
		
		for (ContextContributor contributor : getContextContributors())
		{
			contributor.modifyContext(command, this);
		}
	}
	
	/**
	 * get
	 * 
	 * @param key
	 * @return
	 */
	public Object get(String key)
	{
		return this._map.get(key);
	}
	
	/**
	 * getConsoleStream
	 * 
	 * @return
	 */
	public OutputStream getConsoleStream()
	{
		return this._consoleStream;
	}
	
	/**
	 * getErrorStream
	 * 
	 * @return
	 */
	public OutputStream getErrorStream()
	{
		return this._errorStream;
	}
	
	/**
	 * getInputStream
	 * 
	 * @return
	 */
	public InputStream getInputStream()
	{
		return this._inputStream;
	}
	
	/**
	 * getOutputStream
	 * 
	 * @return
	 */
	public OutputStream getOutputStream()
	{
		return this._outputStream;
	}

	/**
	 * getMap
	 * 
	 * @return
	 */
	public Map<String,Object> getMap()
	{
		return this._map;
	}

	/**
	 * getOutputType
	 * 
	 * @return
	 */
	public OutputType getOutputType()
	{
		return this._outputType;
	}
	
	/**
	 * in
	 * 
	 * @return
	 */
	public IRubyObject in()
	{
		return ScriptingEngine.getInstance().getScriptingContainer().getRuntime().getObject().getConstant("STDIN"); //$NON-NLS-1$
	}
	
	/**
	 * isForcedExit
	 * 
	 * @return
	 */
	public boolean isForcedExit()
	{
		return this._forcedExit;
	}
	
	/**
	 * put
	 * 
	 * @param key
	 * @param value
	 */
	public void put(String key, Object value)
	{
		this._map.put(key, value);
	}
	
	/**
	 * putAll
	 * 
	 * @param entries
	 */
	public void putAll(Map<String,Object> entries)
	{
		this._map.putAll(entries);
	}

	/**
	 * setConsoleStream
	 * 
	 * @param stream
	 */
	public void setConsoleStream(OutputStream stream)
	{
		this._consoleStream = stream;
	}
	
	/**
	 * setErrorStream
	 * 
	 * @param stream
	 */
	public void setErrorStream(OutputStream stream)
	{
		this._errorStream = stream;
	}
	
	/**
	 * setForceExit
	 * 
	 * @param value
	 */
	public void setForcedExit(boolean value)
	{
		this._forcedExit = value;
	}
	
	/**
	 * @param inputStream
	 */
	public void setInputStream(InputStream inputStream)
	{
		this._inputStream = inputStream;
	}
	
	/**
	 * setOutputStream
	 * 
	 * @param stream
	 */
	public void setOutputStream(OutputStream stream)
	{
		this._outputStream = stream;
	}
	
	/**
	 * setOutputType
	 * 
	 * @param type
	 */
	public void setOutputType(OutputType type)
	{
		this._outputType = type;
	}

	/**
	 * setOutput
	 * 
	 * @param output
	 */
	public void setOutputType(String output)
	{
		this._outputType = OutputType.get(output);
	}
}
