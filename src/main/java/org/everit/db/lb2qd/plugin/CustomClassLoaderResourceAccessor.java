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

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
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

import liquibase.resource.ClassLoaderResourceAccessor;
import liquibase.resource.ResourceAccessor;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.DependencyResolutionRequiredException;
import org.apache.maven.model.Dependency;
import org.apache.maven.model.Resource;
import org.apache.maven.project.MavenProject;

/**
 * An implementation of {@link ResourceAccessor}.
 */
public class CustomClassLoaderResourceAccessor implements ResourceAccessor {

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
     */
    public CustomClassLoaderResourceAccessor(final MavenProject project, final ClassLoader classLoader,
            final Map<String, Artifact> pluginArtifactMap) {
        this.project = project;
        try {
            Set<URL> urls = new HashSet<URL>();
            List<String> compileClasspathElements = project.getCompileClasspathElements();
            List<String> runtimeClasspathElements = project.getRuntimeClasspathElements();
            List<Dependency> dependencies = project.getDependencies();
            Map<String, Artifact> artifactMap = project.getArtifactMap();
            List<Artifact> availableArtifacts = new ArrayList<Artifact>(project.getArtifacts());
            log("avsiz: " + artifactMap.size());
            for (Artifact artifact : availableArtifacts) {
                urls.add(artifact.getFile().toURI().toURL());
            }
            for (Artifact artifact : pluginArtifactMap.values()) {
                urls.add(artifact.getFile().toURI().toURL());
            }
            URLClassLoader urlClassLoader = new URLClassLoader(urls.toArray(new URL[0]), classLoader);
            log("OWWWWWWWWWWWWWWWWWWWWWWWWW");
            log(new ClassLoaderResourceAccessor(urlClassLoader).toString());
            log("OWWWWWWWWWWWWWWWWWWWWWWWWW");
            this.classLoader = urlClassLoader;
        } catch (DependencyResolutionRequiredException e) {
            throw new RuntimeException(e.getMessage(), e);
        } catch (MalformedURLException e) {
            throw new RuntimeException(e.getMessage(), e);
        }
        // this.classLoader = classLoader;
    }

    @Override
    public InputStream getResourceAsStream(final String file) throws IOException {
        InputStream resourceAsStream = classLoader.getResourceAsStream(file);

        log("++++++++++++++++++++++++");
        log(new ClassLoaderResourceAccessor(classLoader).toString());
        log("++++++++++++++++++++++++");
        if (resourceAsStream == null) {
            log("RESOURCE is NULL: ");
            @SuppressWarnings("unchecked")
            List<Resource> resources = project.getResources();
            for (Resource resource : resources) {
                log("RESOURCE is search: ");
                File resourceDir = new File(resource.getDirectory());
                File liquibaseXML = new File(resourceDir, file);
                resourceAsStream = new FileInputStream(liquibaseXML);
                log(liquibaseXML.getAbsolutePath());
                if (resourceAsStream != null) {
                    log("RESOURCE is find: ");
                    break;
                }
            }
        }
        log("RESOURCE: " + resourceAsStream.toString());
        return resourceAsStream;
    }

    @Override
    public Enumeration<URL> getResources(final String packageName) throws IOException {
        // TODO is correct or want to see the project resources folders?
        Enumeration<URL> resources = classLoader.getResources(packageName);
        log("-------------------");
        log(new ClassLoaderResourceAccessor(classLoader).toString());
        log("--------------------");
        return resources;
    }

    private void log(final String msg) {
        try {
            File file = new File("C:/lqmg/log.txt");
            FileWriter fw = new FileWriter(file, true);
            BufferedWriter bw = new BufferedWriter(fw);
            bw.append(msg);
            bw.newLine();
            bw.flush();
            fw.flush();
            fw.close();
            bw.close();
        } catch (Exception e) {

        }
    }

    @Override
    public ClassLoader toClassLoader() {
        return classLoader;
    }
}
