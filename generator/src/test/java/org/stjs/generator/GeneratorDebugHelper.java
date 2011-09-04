/**
 *  Copyright 2011 Alexandru Craciun, Eyal Kaspi
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.stjs.generator;

import japa.parser.ParseException;

import java.io.File;
import java.io.IOException;

import org.junit.Test;
import org.stjs.generator.executor.ExecutionResult;
import org.stjs.generator.executor.RhinoExecutor;

import test.innerclasses.ClassUsingInnerClass;

public class GeneratorDebugHelper {

	@Test
	public void testGenerator() throws ParseException, IOException {
		generate("src/test/java/test/ParentDeclaration1.java", ClassUsingInnerClass.class);
	}

	private void generate(String sourceFile, Class<?> clazz) throws ParseException, IOException {

		Generator generator = new Generator();
		File outputFile = new File("target/x.js");
		generator.generateJavascript(
				Thread.currentThread().getContextClassLoader(),
				new File(sourceFile),
				outputFile,
				new GeneratorConfigurationBuilder().allowedPackage(clazz.getPackage().getName())
						.allowedPackage("org.stjs.javascript").allowedPackage("org.w3c.dom.html").build());
		// NodeJSExecutor executor = new NodeJSExecutor();
		RhinoExecutor executor = new RhinoExecutor();
		ExecutionResult execution = executor.run(outputFile);
		System.out.println(execution.toString());
	}

}
