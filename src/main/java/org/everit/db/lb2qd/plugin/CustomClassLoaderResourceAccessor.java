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

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Enumeration;
import java.util.List;

import org.apache.maven.model.Resource;
import org.apache.maven.project.MavenProject;

import liquibase.resource.FileSystemResourceAccessor;
import liquibase.resource.ResourceAccessor;

public class CustomClassLoaderResourceAccessor implements ResourceAccessor {

    private MavenProject project;

    private FileSystemResourceAccessor fileSystemResourceAccessor;

    private ClassLoader classLoader;

    public CustomClassLoaderResourceAccessor(final MavenProject project, final ClassLoader classLoader) {
        this.project = project;
        fileSystemResourceAccessor = new FileSystemResourceAccessor();
        this.classLoader = classLoader;
    }

    @Override
    public InputStream getResourceAsStream(final String file) throws IOException {
        InputStream resourceAsStream = getClass().getClassLoader().getResourceAsStream(file);
        if (resourceAsStream == null) {
            List<Resource> resources = project.getResources();
            for (Resource resource : resources) {
                if (file.startsWith("/") || file.startsWith("\\")) {
                    resourceAsStream = fileSystemResourceAccessor.getResourceAsStream(resource.getDirectory() + file);
                } else {
                    resourceAsStream = fileSystemResourceAccessor.getResourceAsStream(resource.getDirectory() + "/"
                            + file);
                }
                if (resourceAsStream != null) {
                    break;
                }
            }
        }
        return resourceAsStream;
    }

    @Override
    public Enumeration<URL> getResources(final String packageName) throws IOException {
        Enumeration<URL> resources = getClass().getClassLoader().getResources(packageName);
        if (resources == null) {
            List<Resource> projectResources = project.getResources();
            for (Resource resource : projectResources) {
                if (packageName.startsWith("/") || packageName.startsWith("\\")) {
                    resources = fileSystemResourceAccessor.getResources(resource.getDirectory() + packageName);
                } else {
                    resources = fileSystemResourceAccessor.getResources(resource.getDirectory() + "/" + packageName);
                }
                if (resources != null) {
                    break;
                }
            }
        }
        return resources;
    }

    @Override
    public ClassLoader toClassLoader() {
        return classLoader;
    }
}
