package org.everit.db.lqmg.maven.plugin;

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

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.everit.db.lqmg.Main;

/**
 * Generating QueryDSL java sources.
 */
@Mojo(name = "generate")
public class QueryDSLGenerateMojo extends AbstractMojo {

    /**
     * The LiquiBase XML place on the classpath.
     */
    @Parameter(required = true, property = "lqmg.sourceXML")
    private String sourceXML;

    /**
     * The package where the generated Java classes.
     */
    @Parameter(required = true, property = "lqmg.packageName")
    private String packageName;

    /**
     * The place where generated the classes. Default value is ${project.build.directory}.
     */
    @Parameter(required = true, property = "lqmg.targetFolder", defaultValue = "${project.build.directory}")
    private String targetFolder;

    /**
     * Those are the location of the XML by commas, which are needed for external relations, but java class will be
     * generated from them.
     */
    @Parameter(required = false, property = "lqmg.externalXMLs")
    private String externalXMLs;

    @Override
    public void execute() throws MojoExecutionException {
        Main.generate(sourceXML, packageName, targetFolder, externalXMLs);
    }

}
