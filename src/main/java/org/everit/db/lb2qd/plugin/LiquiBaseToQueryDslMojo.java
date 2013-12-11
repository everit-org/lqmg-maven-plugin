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
import java.util.logging.Level;
import java.util.logging.Logger;

import liquibase.resource.ClassLoaderResourceAccessor;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.Component;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;
import org.everit.db.lqmg.GenerationProperties;
import org.everit.db.lqmg.LQMG;
import org.sonatype.plexus.build.incremental.BuildContext;

/**
 * Generating QueryDSL java sources.
 * 
 */
@Mojo(name = "generate", defaultPhase = LifecyclePhase.GENERATE_SOURCES)
public class LiquiBaseToQueryDslMojo extends AbstractMojo {

    private static final String EMPTY_VALUE = "DEFAULT_EMPTY_VALUE";
    /**
     * Path to the liquibase changelog file.
     */
    @Parameter(required = true, property = "lb2qd.sourceXML")
    private String sourceXML;

    /**
     * he java package of the generated QueryDSL metamodel classes. Default-value: empty, that means that the package
     * will be either empty or derived from the schema.
     */
    @Parameter(required = false, property = "lb2qd.packageName", defaultValue = EMPTY_VALUE)
    private String packageName;

    /**
     * The folder where source will be generated to.
     */
    @Parameter(required = true, property = "lb2qd.targetFolder")
    private String targetFolder;

    /**
     * The schema to package.
     */
    @Parameter(required = false, property = "lb2qb.schemaToPackage", defaultValue = "true")
    private boolean schemaToPackage;

    /**
     * A schema name pattern; must match the schema name as it is stored in the database.
     */
    @Parameter(required = false, property = "lb2qb.schemaPattern", defaultValue = EMPTY_VALUE)
    private String schemaPattern;

    @Parameter(readonly = true, required = true, defaultValue = "${project}")
    private MavenProject project;

    @Component
    protected BuildContext buildContext;

    @Override
    public void execute() throws MojoExecutionException {
        // TODO removing previous generated source?

        // TODO actualizing the generate projekt.
        GenerationProperties params = new GenerationProperties(sourceXML, targetFolder);

        params.setSchemaToPackage(schemaToPackage);

        if (!schemaPattern.equals(EMPTY_VALUE)) {
            params.setSchemaPattern(schemaPattern);
        }

        if (!packageName.equals(EMPTY_VALUE)) {
            params.setPackageName(packageName);
        }

        Logger.getLogger(LiquiBaseToQueryDslMojo.class.getName()).log(Level.INFO, sourceXML);
        Logger.getLogger(LiquiBaseToQueryDslMojo.class.getName()).log(Level.INFO,
                new ClassLoaderResourceAccessor().toString());

        File xmlFile = new File(sourceXML);
        if (buildContext.hasDelta(xmlFile)) {
            // LQMG.generate(params, new ClassLoaderResourceAccessor());
            LQMG.generate(params, new CustomClassLoaderResourceAccessor(project, getClass().getClassLoader()));
            return;
        }
        // generate all event.
        LQMG.generate(params, new CustomClassLoaderResourceAccessor(project, getClass().getClassLoader()));
        // LQMG.generate(params, new ClassLoaderResourceAccessor());

    }
}
