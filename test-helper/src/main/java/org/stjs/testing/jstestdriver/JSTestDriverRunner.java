package org.stjs.testing.jstestdriver;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.logging.LogManager;

import org.junit.runners.BlockJUnit4ClassRunner;
import org.junit.runners.model.FrameworkMethod;
import org.junit.runners.model.InitializationError;
import org.junit.runners.model.Statement;
import org.kohsuke.args4j.CmdLineException;
import org.stjs.testing.GeneratorWrapper;
import org.stjs.testing.HTMLFixture;
import org.stjs.testing.SourceFiles;

import com.google.common.collect.Lists;
import com.google.inject.Binder;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Module;
import com.google.inject.multibindings.Multibinder;
import com.google.jstestdriver.ActionRunner;
import com.google.jstestdriver.Args4jFlagsParser;
import com.google.jstestdriver.FailureException;
import com.google.jstestdriver.FileInfo;
import com.google.jstestdriver.Plugin;
import com.google.jstestdriver.PluginLoader;
import com.google.jstestdriver.config.CmdFlags;
import com.google.jstestdriver.config.CmdLineFlagsFactory;
import com.google.jstestdriver.config.Configuration;
import com.google.jstestdriver.config.InitializeModule;
import com.google.jstestdriver.config.Initializer;
import com.google.jstestdriver.config.UnreadableFilesException;
import com.google.jstestdriver.config.YamlParser;
import com.google.jstestdriver.guice.TestResultPrintingModule;
import com.google.jstestdriver.hooks.PluginInitializer;

public class JSTestDriverRunner extends BlockJUnit4ClassRunner {

	public JSTestDriverRunner(Class<?> klass) throws InitializationError {
		super(klass);
	}

	@Override
	protected Statement methodBlock(final FrameworkMethod method) {
		return new Statement() {

			@Override
			public void evaluate() throws Throwable {
				try {

					CmdFlags cmdLineFlags = new CmdLineFlagsFactory()
							.create(new String[] { "--tests", "all", "--reset" });
					List<Plugin> cmdLinePlugins = cmdLineFlags.getPlugins();

					LogManager.getLogManager().readConfiguration(cmdLineFlags.getRunnerMode().getLogConfig());

					PluginLoader pluginLoader = new PluginLoader();

					List<Module> pluginModules = pluginLoader.load(cmdLinePlugins);
					List<Module> initializeModules = Lists.newLinkedList(pluginModules);

					final Configuration userConfig = cmdLineFlags.getConfigurationSource().parse(
							cmdLineFlags.getBasePath(), new YamlParser());

					Configuration configuration = new DelegatingConfiguration(userConfig) {
						@Override
						public Set<FileInfo> getFilesList() {
							try {
								Set<FileInfo> filesList = userConfig.getFilesList();
								File srcFile = new GeneratorWrapper().generateCode(getTestClass(), method);

								filesList.add(new FileInfo(srcFile.getAbsolutePath(), System.currentTimeMillis(), -1L,
										false, false, null, srcFile.getName()));
								return filesList;
							} catch (IOException e) {
								throw new RuntimeException(e);
							} catch (AssertionError e) {
								throw new RuntimeException(e);
							}
						}

						@Override
						public java.util.List<FileInfo> getTests() {
							// TODO : Create the setup once per test (or test suite), not once per method!
							// TODO : Modify the generator :
							/*
							 * now: -- Generated MyClass = function() {} MyClass.protoype.method = function () {
							 * Assert.assertEquals..... }
							 * 
							 * -- Added by this runner MyClassAdapter = TestCase('MyClass'); MyClassAdatper.testmethod =
							 * function(){new MyClass().method();}; }
							 * 
							 * desired (required hook into generator to 1. Change class declaration, use TestCase 2.
							 * Change method declaration, prefix with 'test'
							 * 
							 * -- Generated MyClass = TestCase('MyClass'); MyClass.prototype.testMethod = function() {
							 * assertEquals(); }
							 */
							final HTMLFixture htmlFixture = getTestClass().getJavaClass().getAnnotation(
									HTMLFixture.class);
							try {
								File outputFile = File.createTempFile(getTestClass().getJavaClass().getName()
										+ "_js_test_driver_adapter", ".js");
								FileWriter writer = new FileWriter(outputFile);

								// Adapter between generated assert (not global) and JS-test-driver assert (which is a
								// set of global methods)
								writer.append("Assert=window;");

								final String testedClassName = getTestClass().getJavaClass().getSimpleName();
								final String jsObjectName = testedClassName + "Adapter";
								final String methodName = "test" + method.getName();
								writer.append(jsObjectName + " = TestCase('" + jsObjectName + "');\n" + jsObjectName
										+ ".prototype." + methodName + "= function() {");
								if (htmlFixture != null) {
									writer.append("\n/*:DOC +=" + htmlFixture.value() + "*/\n");
								}
								writer.append("new " + testedClassName + "()." + method.getName() + "();};");
								writer.flush();
								writer.close();
								// TODO : WTF is the FileIno used for?
								return Collections.singletonList(new FileInfo(outputFile.getAbsolutePath(), System
										.currentTimeMillis(), -1L, false, false, null, outputFile.getName()));
							} catch (IOException e) {
								throw new RuntimeException(e);
							} catch (AssertionError e) {
								throw new RuntimeException(e);
							}

						}
					};

					File basePath = configuration.getBasePath().getCanonicalFile();
					initializeModules.add(new InitializeModule(pluginLoader, basePath, new Args4jFlagsParser(
							cmdLineFlags), cmdLineFlags.getRunnerMode()));

					initializeModules.add(new Module() {
						@Override
						public void configure(Binder binder) {
							Multibinder.newSetBinder(binder, PluginInitializer.class).addBinding()
									.to(TestResultPrintingModule.TestResultPrintingInitializer.class);
						}
					});
					Injector initializeInjector = Guice.createInjector(initializeModules);

					List<Module> actionRunnerModules = (initializeInjector.getInstance(Initializer.class)).initialize(
							pluginModules, configuration, cmdLineFlags.getRunnerMode(),
							cmdLineFlags.getUnusedFlagsAsArgs());

					Injector injector = Guice.createInjector(actionRunnerModules);
					(injector.getInstance(ActionRunner.class)).runActions();
				} catch (CmdLineException e) {
					System.out.println(e.getMessage());
					throw e;
				} catch (UnreadableFilesException e) {
					System.out.println("Configuration Error: \n" + e.getMessage());
					throw e;
				} catch (FailureException e) {
					System.out.println("Tests failed: " + e.getMessage());
					throw e;
				} catch (Exception e) {
					e.printStackTrace();
					System.out.println("Unexpected Runner Condition: " + e.getMessage()
							+ "\n Use --runnerMode DEBUG for more information.");
					throw e;
				}
			}

		};
	}

	private List<File> getSourceFiles(SourceFiles sourceFileAnnote) {
		if (sourceFileAnnote != null) {
			List<File> files = new ArrayList<File>();
			for (String sourceFileName : sourceFileAnnote.files()) {
				files.add(new File(sourceFileName));
			}
			return files;
		} else {
			return Collections.singletonList(new File("src/test/java/"
					+ getTestClass().getJavaClass().getName().replaceAll("\\.", "/") + ".java"));
		}
	}
}
