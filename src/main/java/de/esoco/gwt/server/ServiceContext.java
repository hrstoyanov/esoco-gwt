//++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
// This file is a part of the 'esoco-gwt' project.
// Copyright 2017 Elmar Sonnenschein, esoco GmbH, Flensburg, Germany
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//	  http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.
//++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
package de.esoco.gwt.server;

import de.esoco.lib.logging.Log;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.http.HttpSessionEvent;
import javax.servlet.http.HttpSessionListener;

import org.obrel.core.RelatedObject;


/********************************************************************
 * A base class that provides a context for GWT web applications. It implements
 * the ServletContextListener interface to initialize and shutdown global data
 * structures and executes schedule processes if setup by subclasses. It also
 * listens via HttpSessionListener for sessions and reports changes to an {@link
 * AuthenticatedServiceImpl} instance if one has been registered with.
 *
 * @author eso
 */
public abstract class ServiceContext extends RelatedObject
	implements ServletContextListener, HttpSessionListener
{
	//~ Static fields/initializers ---------------------------------------------

	private static ServiceContext rServiceContextInstance = null;

	//~ Instance fields --------------------------------------------------------

	private AuthenticatedServiceImpl<?> rService;
	private ServletContext			    rServletContext;

	//~ Static methods ---------------------------------------------------------

	/***************************************
	 * Returns the instance.
	 *
	 * @return The instance
	 */
	public static ServiceContext getInstance()
	{
		return rServiceContextInstance;
	}

	//~ Methods ----------------------------------------------------------------

	/***************************************
	 * {@inheritDoc}
	 */
	@Override
	public void contextDestroyed(ServletContextEvent rEvent)
	{
		destroy(rServletContext);

		rServletContext		    = null;
		rServiceContextInstance = null;

		Log.info("Service context shutdown complete");
	}

	/***************************************
	 * {@inheritDoc}
	 */
	@Override
	public void contextInitialized(ServletContextEvent rEvent)
	{
		if (rServiceContextInstance != null)
		{
			throw new IllegalStateException("Multiple service contexts");
		}

		rServletContext = rEvent.getServletContext();

		init(rServletContext);

		rServiceContextInstance = this;
	}

	/***************************************
	 * Returns the service of this instance.
	 *
	 * @return The service or NULL if not set
	 */
	public final AuthenticatedServiceImpl<?> getService()
	{
		return rService;
	}

	/***************************************
	 * Returns the servlet context of this instance.
	 *
	 * @return The servlet context
	 */
	public final ServletContext getServletContext()
	{
		return rServletContext;
	}

	/***************************************
	 * {@inheritDoc}
	 */
	@Override
	public void sessionCreated(HttpSessionEvent rEvent)
	{
	}

	/***************************************
	 * {@inheritDoc}
	 */
	@Override
	public void sessionDestroyed(HttpSessionEvent rEvent)
	{
		if (rService != null)
		{
			rService.removeSession(rEvent.getSession());
		}
	}

	/***************************************
	 * Sets the service of this context.
	 *
	 * @param rService The service
	 */
	public final void setService(AuthenticatedServiceImpl<?> rService)
	{
		this.rService = rService;
	}

	/***************************************
	 * Must be implemented by subclasses to return the application name.
	 *
	 * @return The application name
	 */
	protected abstract String getApplicationName();

	/***************************************
	 * Must be implemented by subclasses to notify the web application clients
	 * of data changes.
	 */
	protected abstract void notifyClients();

	/***************************************
	 * This method can be overridden by subclasses to cleanup internal data
	 * structures. The default implementation does nothing.
	 *
	 * @param rServletContext The servlet context
	 */
	protected void destroy(ServletContext rServletContext)
	{
	}

	/***************************************
	 * This method can be overridden by subclasses to initialize internal data
	 * structures. The default implementation does nothing.
	 *
	 * @param rServletContext The servlet context
	 */
	protected void init(ServletContext rServletContext)
	{
	}
}
