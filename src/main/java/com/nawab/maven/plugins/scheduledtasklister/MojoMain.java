package com.nawab.maven.plugins.scheduledtasklister;

/*
 * Copyright 2001-2005 The Apache Software Foundation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;
import org.reflections.Configuration;
import org.reflections.Reflections;
import org.reflections.scanners.MethodAnnotationsScanner;
import org.reflections.util.ClasspathHelper;
import org.reflections.util.ConfigurationBuilder;
import org.springframework.scheduling.annotation.Scheduled;

import java.io.File;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Set;

import static org.apache.maven.plugins.annotations.LifecyclePhase.PROCESS_CLASSES;


@Mojo(name = "list-scheduled-tasks", defaultPhase = PROCESS_CLASSES)
public class MojoMain extends AbstractMojo {

    @Parameter(defaultValue = "${project}", readonly = true, required = true)
    private MavenProject project;

    public void execute() {
        try {
            // get the class path of compiled classes
            String classesDir = project.getBuild().getOutputDirectory();

            // Create a new URLClassLoader
            URLClassLoader classLoader = new URLClassLoader(
                    new URL[]{new File(classesDir).toURI().toURL()},
                    Thread.currentThread().getContextClassLoader()
            );

            // Configure Reflections to use the right classloader and the MethodAnnotationsScanner
            Configuration configuration = new ConfigurationBuilder()
                    .setUrls(ClasspathHelper.forClassLoader(classLoader))
                    .addClassLoader(classLoader)
                    .setScanners(new MethodAnnotationsScanner());

            Reflections reflections = new Reflections(configuration);

            // Look for methods annotated with @Scheduled
            Set<Method> scheduledMethods = reflections.getMethodsAnnotatedWith(Scheduled.class);

            for (Method method : scheduledMethods) {
//                getLog().info("Scheduled method: " + method.toString());
                Scheduled scheduled = method.getAnnotation(Scheduled.class);
                if (scheduled != null) {
                    if (!scheduled.cron().isEmpty() || scheduled.fixedRate() > 0) {
                        System.out.printf("%s,%s,%s,%s,%s%n",
                                method.getDeclaringClass().getName(),
                                method.getName(),
                                scheduled.fixedRateString(),
                                scheduled.fixedRate(),
                                scheduled.cron());
                    }
                }
                // You can now add the method to your CSV or whatever you need
            }
        } catch (MalformedURLException e) {
            getLog().error("Error scanning for @Scheduled annotations: ", e);
        }
    }

}
