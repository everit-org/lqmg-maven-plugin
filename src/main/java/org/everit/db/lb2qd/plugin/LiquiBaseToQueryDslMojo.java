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

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.Component;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.everit.db.lqmg.Main;
import org.sonatype.plexus.build.incremental.BuildContext;

/**
 * Generating QueryDSL java sources.
 * 
 */
@Mojo(name = "generate", defaultPhase = LifecyclePhase.GENERATE_SOURCES)
public class LiquiBaseToQueryDslMojo extends AbstractMojo {

    /**
     * Path to the liquibase changelog file.
     */
    @Parameter(required = true, property = "lb2qd.sourceXML")
    private String sourceXML;

    /**
     * he java package of the generated QueryDSL metamodel classes. Default-value: empty, that means that the package
     * will be either empty or derived from the schema.
     */
    @Parameter(required = false, property = "lb2qd.packageName", defaultValue = "")
    private String packageName;

    /**
     * The folder where source will be generated to.
     */
    @Parameter(required = true, property = "lb2qd.targetFolder")
    private String targetFolder;

    /**
     * Those are the location of the XML by commas, which are needed for external relations, but java class will be
     * generated from them.
     */
    @Parameter(required = false, property = "lb2qd.externalXMLs")
    private String externalXMLs;

    /**
     * The schema name pattern. Must match the schema name as it is stored in the database. Retrieves those without a
     * schema. <code>null</code> means that the schema name should not be used to narrow the search.
     */
    @Parameter(required = false, property = "lb2qd.schemaPattern", defaultValue = "")
    private String schemaPattern;

    // TODO write java DOC
    @Parameter(required = false, property = "lb2qd.schemaToPackage")
    private boolean schemaToPackage = true;

    @Component
    protected BuildContext buildContext;

    @Override
    public void execute() throws MojoExecutionException {
        // TODO removing previous generated source?

        // TODO actualizing the generate projekt.
        File xmlFile = new File(sourceXML);
        if (buildContext.hasDelta(xmlFile)) {
            Main.generate(sourceXML, packageName, targetFolder, externalXMLs);
            return;
        }
        // generate all event.
        Main.generate(sourceXML, packageName, targetFolder, externalXMLs);
    }
}
