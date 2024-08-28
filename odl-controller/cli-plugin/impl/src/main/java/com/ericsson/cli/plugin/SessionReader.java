/*
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package com.ericsson.cli.plugin;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Predicate;

public class SessionReader {
	static final Logger logger = LoggerFactory.getLogger(SessionReader.class);
	private Predicate<String> promptChecker = null;
	private static final int DEFAULT_MAX_FAIL_ALLOW = 5;
	private ExecutorService executorService;
	private int maxFailAllow = DEFAULT_MAX_FAIL_ALLOW;

	public ExecutorService getExecutorService() {
		return executorService;
	}

	public Predicate<String> getPromptChecker() {
		return promptChecker;
	}

	public void setPromptChecker(Predicate<String> promptChecker) {
		this.promptChecker = promptChecker;
	}

	public SessionReader promptChecker(Predicate<String> promptChecker) {
		this.promptChecker = promptChecker;
		return this;
	}

	public void setExecutorService(ExecutorService executorService) {
		this.executorService = executorService;
	}

	public SessionReader executorService(ExecutorService executorService) {
		this.executorService = executorService;
		return this;
	}

	public int getMaxFailAllow() {
		return maxFailAllow;
	}

	public void setMaxFailAllow(int maxFailAllow) {
		this.maxFailAllow = maxFailAllow;
	}

	public SessionReader maxFailAllow(int maxFailAllow) {
		this.maxFailAllow = maxFailAllow;
		return this;
	}

	public String read(final InputStream is) throws IOException {
		if ( executorService == null ) {
			logger.warn("SessionReader wrong configuration: executorService is missing");
			return null;
		}
		int availableByes = -1;
		int failtCounter = 0;
		try {
			while (true) {
				Thread.sleep(1000);
				availableByes = is.available();
				if (availableByes > 0) {
					break;
				}
				if (failtCounter > maxFailAllow) {
					break;
				}
				failtCounter++;
			}

			logger.debug("availableByes[" + availableByes + "]"
					+ " failtCounter[" + failtCounter
					+ "], start to read output...");
		} catch (InterruptedException e) {
			logger.error("", e);
		}

		StringBuilder sb = new StringBuilder();
		final byte[] buffer = new byte[1024];
		int readBytesNum = 0;

		while (true) {
			Callable<Integer> readTask = new Callable<Integer>() {
				@Override
				public Integer call() throws Exception {
					return is.read(buffer);
				}
			};
			Future<Integer> future = executorService.submit(readTask);
			try {
				readBytesNum = future.get(10000, TimeUnit.MILLISECONDS);
			} catch (Exception e) {
				logger.error("", e.getMessage());
				break;
			}

			if (readBytesNum == -1) {
				break;
			}
			logger.debug(String.format("readBytesNum = %d", readBytesNum));
			sb.append(new String(buffer, 0, readBytesNum));

			if ( promptChecker != null ) {
				if ( !promptChecker.apply(sb.toString()) ) {
					// Non yet arrived prompt, let's continue
					continue;
				}
			}
			if (readBytesNum < 1024) {
				break;
			}
		}
		logger.debug(sb.toString());
		return sb.toString();
	}

	public String readMore(final InputStream is, final OutputStream os) throws IOException {
		if ( executorService == null ) {
			logger.warn("SessionReader wrong configuration: executorService is missing");
			return null;
		}
		int availableByes = -1;
		int failtCounter = 0;
		try {
			while (true) {
				Thread.sleep(1000);
				availableByes = is.available();
				if (availableByes > 0) {
					break;
				}
				if (failtCounter > maxFailAllow) {
					break;
				}
				failtCounter++;
			}

			logger.debug("availableByes[" + availableByes + "]"
					+ " failtCounter[" + failtCounter
					+ "], start to read output...");
		} catch (InterruptedException e) {
			logger.error("", e);
		}

		StringBuilder sb = new StringBuilder();
		final byte[] buffer = new byte[1024];
		int readBytesNum = 0;

		while (true) {
			Callable<Integer> readTask = new Callable<Integer>() {
				@Override
				public Integer call() throws Exception {
					return is.read(buffer);
				}
			};
			Future<Integer> future = executorService.submit(readTask);
			try {
				readBytesNum = future.get(10000, TimeUnit.MILLISECONDS);
			} catch (Exception e) {
				logger.error("", e.getMessage());
				break;
			}

			if (readBytesNum == -1) {
				break;
			}
			logger.debug(String.format("readBytesNum = %d", readBytesNum));
			String content = new String(buffer, 0, readBytesNum);
			sb.append(content);

			if ( content.contains("--More--") ) {
				os.write(" ".getBytes());
				os.flush();
				sb.append("\n");
			}
			else {
				if ( promptChecker != null ) {
					if ( !promptChecker.apply(sb.toString()) ) {
						// Non yet arrived prompt, let's continue
						continue;
					}
				}

				if (readBytesNum < 1024) {
					break;
				}
			}
		}
		logger.debug(sb.toString());
		return sb.toString();
	}
}
