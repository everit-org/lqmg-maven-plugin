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

import java.io.*;
import java.net.URL;
import java.util.*;

import liquibase.resource.ResourceAccessor;

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
    public CustomClassLoaderResourceAccessor(final MavenProject project, final ClassLoader classLoader) {
        this.project = project;
        this.classLoader = classLoader;
    }

    @Override
    public InputStream getResourceAsStream(final String file) throws IOException {
        InputStream resourceAsStream = getClass().getClassLoader().getResourceAsStream(file);

        if (resourceAsStream == null) {
            @SuppressWarnings("unchecked")
            List<Resource> resources = project.getResources();
            for (Resource resource : resources) {
                File resourceDir = new File(resource.getDirectory());
                File liquibaseXML = new File(resourceDir, file);
                resourceAsStream = new FileInputStream(liquibaseXML);
                if (resourceAsStream != null) {
                    break;
                }
            }
        }
        return resourceAsStream;
    }

    @Override
    public Enumeration<URL> getResources(final String packageName) throws IOException {
        // TODO is correct or want to see the project resources folders?
        Enumeration<URL> resources = getClass().getClassLoader().getResources(packageName);
        return resources;
    }

    @Override
    public ClassLoader toClassLoader() {
        return classLoader;
    }
}
