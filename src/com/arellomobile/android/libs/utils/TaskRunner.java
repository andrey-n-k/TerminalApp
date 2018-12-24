/*
 * Arello Mobile
 * Mobile Framework
 * Except where otherwise noted, this work is licensed under a Creative Commons Attribution 3.0 Unported License
 * http://creativecommons.org/licenses/by/3.0
 */

package com.arellomobile.android.libs.utils;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

/**
 *
 * Class for queue execution of runnable objects.<br/>
 * By default include 6 parallel threads (workers).<br/>
 * For customize this option use "com.arellomobile.android.TaskRunner.THREADS_COUNT" system property<br/>
 * <i>
 * System.setProperty("com.arellomobile.android.TaskRunner.THREADS_COUNT", "6");
 * </i>
 *
 * @author Swift
 */
public class TaskRunner implements Runnable{
	private static TaskRunner instance = null;
	private final Logger log = Logger.getLogger(getClass().getName());

	public static TaskRunner getInstance() {
		if (instance == null) instance = new TaskRunner();
		return instance;
	}

	private List<Runnable> tasks = new ArrayList<Runnable>();
	private final Object tasksMutex = new Object();

	protected Thread[] threads;
	private boolean run = true;

	TaskRunner() {

		int threadsCount = 6;
		String property = System.getProperty("com.arellomobile.android.TaskRunner.THREADS_COUNT", "6");
		try {
			threadsCount = Integer.parseInt(property);
		} catch(NumberFormatException e){
			log.warning("NumberFormatException while parse property \"com.arellomobile.android.TaskRunner.THREADS_COUNT\"");
		}
		threads = new Thread[threadsCount];
		for (int i = 0; i < threads.length; i++) {
			threads[i] = new Thread(this);
			threads[i].setPriority(Thread.MIN_PRIORITY);
			threads[i].start();
		}
	}

	public void run() {
		while(run) {
			Runnable runnable = null;
			synchronized (tasksMutex) {
				if (!tasks.isEmpty()) {
					runnable = tasks.get(0);
					tasks.remove(0);
				} else {
					try{tasksMutex.wait();} catch(InterruptedException e) {/**/}
				}
			}
			if (runnable != null){
				runnable.run();
			}
		}
	}

	/**
	 * stops all workers and drop instance
	 */
	public void cancel() {
		instance = null;
		run = false;
	}

	/**
	 * add new runnable object in queue
	 * @param r new task for execution
	 */
	public void queueTask(Runnable r) {
		synchronized (tasksMutex) {
			tasks.add(r);
			tasksMutex.notifyAll();
		}
	}
}
