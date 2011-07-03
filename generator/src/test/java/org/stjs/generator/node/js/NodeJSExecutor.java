package org.stjs.generator.node.js;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

public class NodeJSExecutor {
	private static final String nodeJS = "node";

	public static class ExecutionResult {
		private final String stdOut;
		private final String stdErr;
		private final int exitValue;
		
		public ExecutionResult(String stdOut, String stdErr, int exitValue) {
			this.stdOut = stdOut;
			this.stdErr = stdErr;
			this.exitValue = exitValue;
		}

		public String getStdOut() {
			return stdOut;
		}

		public String getStdErr() {
			return stdErr;
		}

		public int getExitValue() {
			return exitValue;
		}
		
		@Override
		public String toString() {
			if (stdOut.isEmpty() && stdErr.isEmpty()) {
				return "Execution was sucessful";
			}
			return String.format("exitValue : %s\nstdout : %s\nstderr :%s", exitValue, stdOut, stdErr);
		}
	}
	
	public ExecutionResult run(File srcFile) {
		try {
			Process p = Runtime.getRuntime().exec(
					new String[] { nodeJS, srcFile.getAbsolutePath() });
			return new ExecutionResult(
					readStream(p.getInputStream()),
					readStream(p.getErrorStream()),
					p.exitValue());
		} catch (IOException e) {
			// TODO : this is not really going to be working on all OS!
			if (e.getMessage().contains("Cannot run program")) {
				String errMsg = "Please install node.js to use this feature https://github.com/joyent/node/wiki/Installation";
				System.err.println(errMsg);
				throw new RuntimeException(errMsg);
			}
			throw new RuntimeException(e);
		}
	}
	
	private String readStream(InputStream errStream) throws IOException {
		StringBuilder builder = new StringBuilder();
		BufferedReader in = new BufferedReader(new InputStreamReader(errStream));
		String line;
		while ((line = in.readLine()) != null) {
			builder.append(line);
			builder.append("\n");
		}
		return builder.toString();
	}
}
