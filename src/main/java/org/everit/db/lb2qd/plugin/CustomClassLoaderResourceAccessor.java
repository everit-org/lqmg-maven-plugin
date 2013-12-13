package org.everit.db.lb2qd.plugin;

/*
 * Copyright (c) 2011, Everit Kft.
 *
 * All rights reserved.
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston,
 * MA 02110-1301  USA
 */

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import liquibase.resource.ResourceAccessor;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.model.Resource;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.project.MavenProject;
import org.everit.db.lqmg.LQMG;

/**
 * An implementation of {@link ResourceAccessor}.
 */
public class CustomClassLoaderResourceAccessor implements ResourceAccessor {

    /**
     * The {@link Logger} instance for logging.
     */
    private static final Logger LOGGER = Logger.getLogger(LQMG.class.getName());

    /**
     * The {@link MavenProject} which using the CustomClassLoaderResourceAccessor. Required to find resources.
     */
    private MavenProject project;

    /**
     * The {@link ClassLoader} instance.
     */
    private ClassLoader classLoader;

    /**
     * The simple constructor.
     * 
     * @param project
     *            the MavenProject which call the CustomClassLoaderResourceAccessor.
     * @param classLoader
     *            the {@link ClassLoader}.
     * @throws MojoExecutionException
     *             if URL creation is failed.
     */
    public CustomClassLoaderResourceAccessor(final MavenProject project, final Map<String, Artifact> pluginArtifactMap)
            throws MojoExecutionException {
        LOGGER.log(Level.INFO, "Start create " + CustomClassLoaderResourceAccessor.class.getSimpleName()
                + " (constructor).");
        this.project = project;
        LOGGER.log(Level.INFO, "Start create classLoader.");
        Set<URL> urls = new HashSet<URL>();
        @SuppressWarnings("unchecked")
        List<Artifact> artifacts = new ArrayList<Artifact>(project.getArtifacts());
        artifacts.addAll(pluginArtifactMap.values());

        for (Artifact artifact : artifacts) {
            try {
                urls.add(artifact.getFile().toURI().toURL());
            } catch (MalformedURLException e) {
                throw new MojoExecutionException("URL construction error.", e);
            }
        }
        classLoader = new URLClassLoader(urls.toArray(new URL[0]), classLoader);
        LOGGER.log(Level.INFO, "Finished create classLoader.");
        LOGGER.log(Level.INFO, "Finished create " + CustomClassLoaderResourceAccessor.class.getSimpleName()
                + " (constructor).");
    }

    @Override
    public InputStream getResourceAsStream(final String file) throws IOException {
        LOGGER.log(Level.INFO, "Find file in classLoader.");
        InputStream resourceAsStream = classLoader.getResourceAsStream(file);
        if (resourceAsStream == null) {
            LOGGER.log(Level.INFO, "Unable find file in classLoader.");
            @SuppressWarnings("unchecked")
            List<Resource> resources = project.getResources();
            LOGGER.log(Level.INFO, "Find file in project resources.");
            for (Resource resource : resources) {
                File resourceDir = new File(resource.getDirectory());
                File liquibaseXML = new File(resourceDir, file);
                resourceAsStream = new FileInputStream(liquibaseXML);
                if (resourceAsStream != null) {
                    LOGGER.log(Level.INFO, "Found file in project resources.");
                    break;
                }
            }
        } else {
            LOGGER.log(Level.INFO, "Found file in classLoader.");
        }
        return resourceAsStream;
    }

    @Override
    public Enumeration<URL> getResources(final String packageName) throws IOException {
        // TODO is correct or want to see the project resources folders?
        LOGGER.log(Level.INFO, "Find packageName in classLoader.");
        Enumeration<URL> resources = classLoader.getResources(packageName);
        LOGGER.log(Level.INFO, "Found packageName in classLoader.");
        return resources;
    }

    @Override
    public ClassLoader toClassLoader() {
        return classLoader;
    }
}
