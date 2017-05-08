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

import de.esoco.entity.Entity;

import de.esoco.lib.logging.Log;
import de.esoco.lib.thread.ThreadManager;

import de.esoco.process.Process;
import de.esoco.process.ProcessDefinition;
import de.esoco.process.ProcessRunner;
import de.esoco.process.ProcessScheduler;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

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
	implements ServletContextListener, HttpSessionListener, ProcessScheduler
{
	//~ Static fields/initializers ---------------------------------------------

	private static ServiceContext rServiceContextInstance = null;

	//~ Instance fields --------------------------------------------------------

	private AuthenticatedServiceImpl<?> rService;

	private ExecutorService aThreadPool;
	private ServletContext  rServletContext;

	private Lock aScheduleProcessNotificationLock = new ReentrantLock();

	private Map<String, ProcessRunner> rScheduleProcessMap =
		new HashMap<String, ProcessRunner>();

	private boolean bIsEnabled = false;

	//~ Constructors -----------------------------------------------------------

	/***************************************
	 * Creates a new instance.
	 */
	public ServiceContext()
	{
		String sSchedulerEnabled = System.getProperty("process_scheduler");

		if ("yes".equalsIgnoreCase(sSchedulerEnabled))
		{
			bIsEnabled = true;
		}

		aThreadPool = Executors.newCachedThreadPool();
	}

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
	 * Adds a process to the management of the scheduler.
	 *
	 * @param rProcessDescription Entity that defines a schedule process.
	 */
	@Override
	public void addScheduleProcess(Entity rProcessDescription)
	{
		ProcessRunner aProcessRunner = getProcessRunner(rProcessDescription);

		if (aProcessRunner != null)
		{
			rScheduleProcessMap.put(rProcessDescription.toString(),
									aProcessRunner);
			aThreadPool.execute(aProcessRunner);
		}
	}

	/***************************************
	 * {@inheritDoc}
	 */
	@Override
	public void contextDestroyed(ServletContextEvent rEvent)
	{
		rServiceContextInstance = null;

		if (rScheduleProcessMap != null)
		{
			for (ProcessRunner rProcessRunner : rScheduleProcessMap.values())
			{
				rProcessRunner.stop();
			}
		}

		ThreadManager.shutdownAndAwaitTermination(aThreadPool, 60, false);

		destroy(rServletContext);

		aThreadPool						 = null;
		rServletContext					 = null;
		rScheduleProcessMap				 = null;
		aScheduleProcessNotificationLock = null;

		Log.info("Service context shutdown complete");
	}

	/***************************************
	 * {@inheritDoc}
	 */
	@Override
	public void contextInitialized(ServletContextEvent rEvent)
	{
		rServletContext = rEvent.getServletContext();

		init(rServletContext);

		startProcessSchedulingIfEnabled();

		if (rServiceContextInstance != null)
		{
			throw new IllegalStateException("Multiple service contexts");
		}

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
	 * @see ProcessScheduler#isEnabled()
	 */
	@Override
	public boolean isEnabled()
	{
		return bIsEnabled;
	}

	/***************************************
	 * Let every {@link ProcessRunner} resume from pause to check whether an
	 * execution is due now after changes have been made.
	 *
	 * @see ProcessScheduler#notifyScheduleProcessChanged()
	 */
	@Override
	public void notifyScheduleProcessChanged()
	{
		Collection<ProcessRunner> rProcessRunners =
			rScheduleProcessMap.values();

		for (ProcessRunner rProcessRunner : rProcessRunners)
		{
			rProcessRunner.resumeFromPause();
		}
	}

	/***************************************
	 * Invokes the method {@link #scheduleProcessFinished(Process)} in a
	 * thread-safe way.
	 *
	 * @param rProcess The schedule process that has finished
	 */

	@Override
	public void notifyScheduleProcessFinished(Process rProcess)
	{
		aScheduleProcessNotificationLock.lock();

		try
		{
			scheduleProcessFinished(rProcess);
		}
		finally
		{
			aScheduleProcessNotificationLock.unlock();
		}
	}

	/***************************************
	 * Invokes the method {@link #scheduleProcessStarting(Process)} in a
	 * thread-safe way.
	 *
	 * @param rProcess The schedule process that is starting
	 */

	@Override
	public void notifyScheduleProcessStarting(Process rProcess)
	{
		aScheduleProcessNotificationLock.lock();

		try
		{
			scheduleProcessStarting(rProcess);
		}
		finally
		{
			aScheduleProcessNotificationLock.unlock();
		}
	}

	/***************************************
	 * @see ProcessScheduler#removeScheduleProcess(String)
	 */
	@Override
	public void removeScheduleProcess(String sEntityId)
	{
		ProcessRunner rProcessRunner = rScheduleProcessMap.get(sEntityId);

		if (rProcessRunner != null)
		{
			rProcessRunner.stop();
			rScheduleProcessMap.remove(sEntityId);
		}
	}

	/***************************************
	 * @see ProcessScheduler#resumeProcess(Class)
	 */
	@Override
	public void resumeProcess(
		Class<? extends ProcessDefinition> rProcessDefinitionClass)
	{
		ProcessRunner rScheduleProcess =
			findProcessRunnerForProcessDefinition(rProcessDefinitionClass);

		if (rScheduleProcess != null)
		{
			rScheduleProcess.executeProcessNow();
		}
	}

	/***************************************
	 * {@inheritDoc}
	 */
	@Override
	public void resumeScheduleProcess(String sEntityId)
	{
		ProcessRunner rProcessRunner = rScheduleProcessMap.get(sEntityId);

		if (rProcessRunner != null && !rProcessRunner.isRunning())
		{
			rProcessRunner.resume();
			aThreadPool.execute(rProcessRunner);
		}
	}

	/***************************************
	 * {@inheritDoc}
	 */
	@Override
	public void runScheduleProcessNow(String sEntityId)
	{
		ProcessRunner rProcessRunner = rScheduleProcessMap.get(sEntityId);

		if (rProcessRunner != null)
		{
			rProcessRunner.executeProcessNow();
		}
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
	 * @see ProcessScheduler#suspendScheduleProcess(String)
	 */
	@Override
	public void suspendScheduleProcess(String sEntityId)
	{
		ProcessRunner rProcessRunner = rScheduleProcessMap.get(sEntityId);

		if (rProcessRunner != null)
		{
			rProcessRunner.stop();
		}
	}

	/***************************************
	 * Must be implemented by subclasses to return the application name.
	 *
	 * @return The application name
	 */
	protected abstract String getApplicationName();

	/***************************************
	 * Returns the process runner for the given process description or NULL if
	 * no process runner could be created.
	 *
	 * @param  rProcessDescription {@link Entity} that holds information about
	 *                             the process to run.
	 *
	 * @return The process runner or NULL if no process runner could be created.
	 */
	protected abstract ProcessRunner getProcessRunner(
		Entity rProcessDescription);

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
	 * Can be implemented by subclasses to return a collection of server
	 * processes that shall be executed in the background. This default
	 * implementation returns a new empty list that can be manipulated freely.
	 * Subclasses should always combine their own process list with that of the
	 * superclasses.
	 *
	 * @return The schedule processes
	 */
	protected Collection<Entity> getScheduleProcesses()
	{
		return new ArrayList<Entity>();
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

	/***************************************
	 * Will be invoked after the execution of a schedule process has finished.
	 * The default implementation does nothing.
	 *
	 * @param rProcess The finished process
	 */
	protected void scheduleProcessFinished(Process rProcess)
	{
	}

	/***************************************
	 * Will be invoked before the execution of a schedule process starts. The
	 * default implementation does nothing.
	 *
	 * @param rProcess The starting process
	 */
	protected void scheduleProcessStarting(Process rProcess)
	{
	}

	/***************************************
	 * Creates a {@link ProcessRunner} for each Entity that defines a Process.
	 *
	 * @param  rProcessesToAdd an Entity that defines a process.
	 *
	 * @return The created map
	 */
	private Map<String, ProcessRunner> createScheduleProcessMap(
		Collection<Entity> rProcessesToAdd)
	{
		Map<String, ProcessRunner> aScheduleProcessMap =
			new ConcurrentHashMap<String, ProcessRunner>(rProcessesToAdd
														 .size());

		for (Entity rProcessToAdd : rProcessesToAdd)
		{
			ProcessRunner rProcessRunner = getProcessRunner(rProcessToAdd);

			if (rProcessRunner != null)
			{
				aScheduleProcessMap.put(rProcessToAdd.toString(),
										rProcessRunner);
			}
		}

		return aScheduleProcessMap;
	}

	/***************************************
	 * Loops through all ProcessRunners and returns the first that runs an
	 * instance of the {@link ProcessDefinition} class given as the parameter.
	 *
	 * @param  rProcessDefinitionClass
	 *
	 * @return A {@link ProcessRunner} that runs an instance of the class given
	 *         as the parameter or NULL if no ProcessRunner is found.
	 */
	private ProcessRunner findProcessRunnerForProcessDefinition(
		Class<? extends ProcessDefinition> rProcessDefinitionClass)
	{
		ProcessRunner rScheduleProcess = null;

		if (rScheduleProcessMap != null)
		{
			Collection<ProcessRunner> rProcessRunners =
				rScheduleProcessMap.values();

			for (ProcessRunner rProcessRunner : rProcessRunners)
			{
				if (rProcessRunner.getProcessDefinition().getClass() ==
					rProcessDefinitionClass)
				{
					rScheduleProcess = rProcessRunner;

					break;
				}
			}
		}

		return rScheduleProcess;
	}

	/***************************************
	 * Adds each ProcessRunner to the {@link ExecutorService}
	 *
	 * @param rProcessRunnersToSchedule
	 */
	private void scheduleProcesses(
		Collection<ProcessRunner> rProcessRunnersToSchedule)
	{
		for (ProcessRunner rProcessRunner : rProcessRunnersToSchedule)
		{
			aThreadPool.execute(rProcessRunner);
		}
	}

	/***************************************
	 * Sets up the process scheduling and adds all schedule processes to the
	 * {@link ExecutorService}
	 */
	private void startProcessSchedulingIfEnabled()
	{
		if (bIsEnabled)
		{
			Log.info("Starting process scheduler");

			Collection<Entity> rScheduleProcesses = getScheduleProcesses();

			rScheduleProcessMap = createScheduleProcessMap(rScheduleProcesses);

			scheduleProcesses(rScheduleProcessMap.values());
		}
		else
		{
			Log.info("Process scheduler not running. " +
					 "Set property process_scheduler=yes to enable.");
		}
	}
}
