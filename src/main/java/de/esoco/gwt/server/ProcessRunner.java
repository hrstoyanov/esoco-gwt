//++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
// This file is a part of the 'esoco-gwt' project.
// Copyright 2015 Elmar Sonnenschein, esoco GmbH, Flensburg, Germany
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

import de.esoco.entity.ConcurrentEntityModificationException;

import de.esoco.lib.logging.Log;
import de.esoco.lib.logging.LogLevel;

import de.esoco.process.Process;
import de.esoco.process.ProcessDefinition;
import de.esoco.process.ProcessException;
import de.esoco.process.ProcessManager;
import de.esoco.process.ProcessRelationTypes;
import de.esoco.process.ProcessScheduler;

import java.util.Date;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import static de.esoco.process.ProcessRelationTypes.PROCESS_USER;


/********************************************************************
 * An abstract implementation of the {@link ProcessRunner} interface.
 *
 * @author "u.eggers"
 */
public abstract class ProcessRunner implements Runnable
{
	//~ Static fields/initializers ---------------------------------------------

	private static final long ENTITY_MODIFICATION_SLEEP_TIME	  = 1000L;
	private static final int  MAX_ENTITY_MODIFICATION_SLEEP_TRIES = 30;

	//~ Instance fields --------------------------------------------------------

	private final ProcessScheduler rProcessScheduler;

	private final Lock	    aLock  = new ReentrantLock();
	private final Condition aPause = aLock.newCondition();

	private LogLevel eLogLevel		  = LogLevel.ERROR;
	private boolean  bLogOnError	  = true;
	private boolean  bContinueOnError = false;
	private boolean  bRun			  = true;
	private boolean  bSingleRun		  = false;
	private boolean  bRunning		  = false;

	private Class<? extends ProcessDefinition> rProcessDefinitionClass;

	private Process rProcess = null;

	//~ Constructors -----------------------------------------------------------

	/***************************************
	 * Creates a new instance.
	 *
	 * @param rProcessScheduler the service context
	 */
	protected ProcessRunner(ProcessScheduler rProcessScheduler)
	{
		this.rProcessScheduler = rProcessScheduler;
	}

	//~ Methods ----------------------------------------------------------------

	/***************************************
	 * Run the process now even if it is currently paused or not scheduled to
	 * run in the future.
	 */
	public void executeProcessNow()
	{
		bSingleRun = true;

		if (bRunning)
		{
			resumeFromPause();
		}
		else
		{
			run();
		}
	}

	/***************************************
	 * TRUE if the ProcessRunner is running.
	 *
	 * @return TRUE if running.
	 */
	public boolean isRunning()
	{
		return bRunning;
	}

	/***************************************
	 * Resumes the execution of this runner if it is currently waiting.
	 */
	public void resume()
	{
		bRun = true;
		resumeFromPause();
	}

	/***************************************
	 * Resumes the execution of this runner if it is currently pausing.
	 */

	public void resumeFromPause()
	{
		if (aLock.tryLock())
		{
			try
			{
				aPause.signalAll();
			}
			finally
			{
				aLock.unlock();
			}
		}
	}

	/***************************************
	 * Executes the process this runner is associated with.
	 */
	@Override
	public void run()
	{
		bRunning = true;

		try
		{
			while (bRun || bSingleRun)
			{
				aLock.lock();

				try
				{
					long nSleepTime = 0;

					if (!bSingleRun)
					{
						nSleepTime =
							getCurrentScheduleDate().getTime() -
							System.currentTimeMillis();
					}

					if (nSleepTime > 0)
					{
						aPause.await(nSleepTime, TimeUnit.MILLISECONDS);
					}

					if ((bRun && isExecutionDue()) || bSingleRun)
					{
						bSingleRun = false;
						executeProcess();
					}
				}
				catch (InterruptedException e)
				{
					//aPause.await threw this. (most like caused by an exception in the background process)
					//the process is not currently running, but will
					//be executed the next round if it's execution is due.
				}
				finally
				{
					aLock.unlock();
				}
			}
		}
		catch (InterruptedException e)
		{
			// just terminate if process thread is interrupted; this
			// happens typically on shutdown
		}
		catch (Exception e)
		{
			handleProcessExecutionException(e);
		}
		finally
		{
			bRunning = false;
		}
	}

	/***************************************
	 * Stops this runner and the associated process.
	 */

	public void stop()
	{
		bRun = false;

		if (rProcess != null)
		{
			rProcess.setParameter(ProcessRelationTypes.STOP_PROCESS_EXECUTION,
								  Boolean.TRUE);
		}

		resumeFromPause();
	}

	/***************************************
	 * Returns the current schedule date that might be determined from the
	 * context. A subclass can throw any kind of Exception if an error occurs
	 * during the determination of the current schedule date.
	 *
	 * @return The schedule date
	 *
	 * @throws Exception If determining the schedule date fails.
	 */
	protected abstract Date getCurrentScheduleDate() throws Exception;

	/***************************************
	 * This is called when the execution of a process fails and throws an
	 * Exception. Implementing subclasses should take appropriate action.
	 *
	 * @param rE The Exception that occurred.
	 */
	protected abstract void handleProcessExecutionException(Exception rE);

	/***************************************
	 * Internal method that is called right after the process execution has
	 * finished The default implementation does nothing.
	 *
	 * @param  rProcess the schedule process that was executed.
	 *
	 * @throws Exception subclasses may throw any kind of exception
	 */
	protected void afterExecution(Process rProcess) throws Exception
	{
	}

	/***************************************
	 * Internal method that is called right before the process execution starts.
	 * The default implementation does nothing.
	 *
	 * @param  rProcess the schedule process that will be executed.
	 *
	 * @throws Exception subclasses may throw any kind of exception
	 */
	protected void beforeExecution(Process rProcess) throws Exception
	{
	}

	/***************************************
	 * Returns the {@link ProcessScheduler} that serves as a context for the
	 * process execution.
	 *
	 * @return The {@link ProcessScheduler} used by this runner.
	 */
	protected ProcessScheduler getProcessScheduler()
	{
		return rProcessScheduler;
	}

	/***************************************
	 * Sets the continueOnError.
	 *
	 * @param bContinueOnError The continueOnError value
	 */
	protected void setContinueOnError(boolean bContinueOnError)
	{
		this.bContinueOnError = bContinueOnError;
	}

	/***************************************
	 * Sets the logLevel.
	 *
	 * @param eLogLevel The logLevel value
	 */
	protected void setLogLevel(LogLevel eLogLevel)
	{
		this.eLogLevel = eLogLevel;
	}

	/***************************************
	 * Sets the logOnError.
	 *
	 * @param bLogOnError The logOnError value
	 */
	protected void setLogOnError(boolean bLogOnError)
	{
		this.bLogOnError = bLogOnError;
	}

	/***************************************
	 * Sets the processDefinition class that is used to instantiate schedule
	 * processes.
	 *
	 * @param rProcessDefinitionClass The processDefinition class
	 */
	protected void setProcessDefinition(
		Class<? extends ProcessDefinition> rProcessDefinitionClass)
	{
		this.rProcessDefinitionClass = rProcessDefinitionClass;
	}

	/***************************************
	 * Checks whether this {@link ProcessRunner} runs an instance of the {@link
	 * ProcessDefinition} given as the parameter.
	 *
	 * @param  rProcessDefinitionClass the class to check against
	 *
	 * @return TRUE it the given parameter is the class this {@link
	 *         ProcessRunner} is running an instance of, FALSE otherwise.
	 */
	boolean runsProcessDefinitionClass(
		Class<? extends ProcessDefinition> rProcessDefinitionClass)
	{
		return rProcessDefinitionClass == this.rProcessDefinitionClass;
	}

	/***************************************
	 * Invokes the execution support methods {@link #beforeExecution(Process)}
	 * and {@link #afterExecution(Process)} and handles framework exceptions
	 * like {@link ConcurrentEntityModificationException}.
	 *
	 * @param  bBefore
	 *
	 * @throws Exception
	 */
	private void beforeAfterExecution(boolean bBefore) throws Exception
	{
		int nTries = 0;

		while (nTries++ < MAX_ENTITY_MODIFICATION_SLEEP_TRIES)
		{
			try
			{
				if (bBefore)
				{
					beforeExecution(rProcess);
				}
				else
				{
					afterExecution(rProcess);
				}

				break;
			}
			catch (ConcurrentEntityModificationException e)
			{
				Thread.sleep(ENTITY_MODIFICATION_SLEEP_TIME);

				if (nTries >= MAX_ENTITY_MODIFICATION_SLEEP_TRIES)
				{
					throw e;
				}
			}
		}

		Log.info((bBefore ? "Start " : "Finish ") + rProcess.getFullName());
	}

	/***************************************
	 * Creates a new process using the {@link ProcessDefinition} assigned to
	 * this {@link ProcessRunner}.
	 *
	 * @return The created {@link Process}
	 *
	 * @throws ProcessException if process creation fails
	 */
	private Process createProcess() throws ProcessException
	{
		Process rProcess = ProcessManager.getProcess(rProcessDefinitionClass);

		rProcess.set(PROCESS_USER, rProcessScheduler.getScheduleProcessUser());

		return rProcess;
	}

	/***************************************
	 * Checks whether {@link #bLogOnError} is set to TRUE. If it is, logging is
	 * done using the {@link LogLevel} {@link #eLogLevel}
	 *
	 * @param rLogMessage The log message
	 * @param e           The ProcessException
	 */
	private void doLoggingIfEnabled(String rLogMessage, Exception e)
	{
		if (bLogOnError)
		{
			Log.log(eLogLevel, rLogMessage, e);
		}
	}

	/***************************************
	 * Internal method that executes the given process
	 *
	 * @throws Exception If the process execution fails
	 */

	private void executeProcess() throws Exception
	{
		try
		{
			rProcess = createProcess();

			rProcessScheduler.notifyScheduleProcessStarting(rProcess);
			beforeAfterExecution(true);
			rProcess.execute();
		}
		catch (Exception e)
		{
			if (!bContinueOnError)
			{
				doLoggingIfEnabled("Schedule process error [" +
								   rProcess.getName() + "], stopping",
								   e);
				throw e;
			}
			else
			{
				doLoggingIfEnabled("Schedule process error [" +
								   rProcess.getName() + "], continuing",
								   e);
			}
		}
		finally
		{
			try
			{
				beforeAfterExecution(false);
			}
			catch (Exception e)
			{
				if (!bContinueOnError)
				{
					doLoggingIfEnabled("Error during execution of 'execution support method -> after execution', stopping ",
									   e);
					throw e;
				}
				else
				{
					doLoggingIfEnabled("Error during execution of 'execution support method -> after execution', continuing ",
									   e);
				}
			}

			rProcessScheduler.notifyScheduleProcessFinished(rProcess);
		}
	}

	/***************************************
	 * Finds out whether process execution is due. Process execution is due if
	 * now is after the schedule date.
	 *
	 * @return TRUE if execution is due
	 *
	 * @throws Exception forwarded from {@link #getCurrentScheduleDate()}
	 */
	private boolean isExecutionDue() throws Exception
	{
		Date rScheduleDate = getCurrentScheduleDate();
		Date aNow		   = new Date();

		return aNow.after(rScheduleDate);
	}
}
