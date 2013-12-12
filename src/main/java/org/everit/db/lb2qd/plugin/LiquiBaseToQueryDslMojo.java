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
 * This class responsible to call the LiguiBase to QueryDSL metamodel generators when using maven to generation.
 */
@Mojo(name = "generate", defaultPhase = LifecyclePhase.GENERATE_SOURCES)
public class LiquiBaseToQueryDslMojo extends AbstractMojo {

    /**
     * The {@link Logger} instance for logging.
     */
    private static final Logger LOGGER = Logger.getLogger(LQMG.class.getName());

    /**
     * The default empty value to schemaPattern.
     */
    private static final String EMPTY_VALUE = "DEFAULT_EMPTY_VALUE_LB2QD_MAVEN_PLUGIN_TO_SCHEMA_PATTERN";

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
     * The schema to package.
     */
    @Parameter(required = false, property = "lb2qb.schemaToPackage", defaultValue = "true")
    private boolean schemaToPackage;

    /**
     * A schema name pattern; must match the schema name as it is stored in the database.
     */
    @Parameter(required = false, property = "lb2qb.schemaPattern", defaultValue = EMPTY_VALUE)
    private String schemaPattern;

    /**
     * The {@link MavenProject} which call the lb2qb-maven-plugin.
     */
    @Parameter(readonly = true, required = true, defaultValue = "${project}")
    private MavenProject project;

    /**
     * The {@link BuildContext} component instance.
     */
    @Component
    protected BuildContext buildContext;

    @Override
    public void execute() throws MojoExecutionException {
        // TODO removing previous generated source?
        LOGGER.log(Level.INFO, "Start lb2qd-maven-plugin.");
        GenerationProperties params = null;

        // check the target folder path is absolute path or not.
        LOGGER.log(Level.INFO, "Check the targetFolder path is absolue path or not.");
        if (new File(targetFolder).isAbsolute()) {
            LOGGER.log(Level.INFO, "The targetFolder path is absolue path."
                    + "\nCreate GenerationProperties. "
                    + "\nSoureXML: " + sourceXML
                    + "\ntargetFolder: " + targetFolder);
            params = new GenerationProperties(sourceXML, targetFolder);
        } else {
            // if not absolute we create absolute path.
            // ${project.basedir} + targetFolder
            // working all case
            // $project.basedir = C:\temp\
            // target folder: ..\temp1\temp
            // result C:\temp\..\temp1\temp so C:\temp1\temp
            LOGGER.log(Level.INFO, "The targetFolder path is not absolue path. Concatanating the ${project.basedir} "
                    + "and the targetFolder path."
                    + "\nCreate GenerationProperties. "
                    + "\nSoureXML: " + sourceXML
                    + "\ntargetFolder: "
                    + new File(project.getBasedir().getAbsolutePath(), targetFolder).getAbsolutePath());
            params = new GenerationProperties(sourceXML,
                    new File(project.getBasedir().getAbsolutePath(), targetFolder).getAbsolutePath());
        }

        LOGGER.log(Level.INFO, "Set schamaToPackage value. Value: " + schemaToPackage);
        // set the schemaToPackage
        params.setSchemaToPackage(schemaToPackage);

        LOGGER.log(Level.INFO, "Set schemaPattern value. Value: " + schemaPattern);
        // set schemaPattern if not a default value.
        if (!schemaPattern.equals(EMPTY_VALUE)) {
            params.setSchemaPattern(schemaPattern);
        }

        LOGGER.log(Level.INFO, "Set packageName value. Value: " + packageName);
        // set packagename if not a default value.
        if (packageName != null) {
            params.setPackageName(packageName);
        }

        // only generate if the sourceXML is changed. When building mvn clean install this condition is true.
        File xmlFile = new File(sourceXML);
        if (buildContext.hasDelta(xmlFile)) {
            LOGGER.log(Level.INFO, "Changed the sourceXML. Generating metamodel");
            LQMG.generate(params, new CustomClassLoaderResourceAccessor(project, getClass().getClassLoader()));
            LOGGER.log(Level.INFO, "Finished the metamodell generation.");
            return;
        }
        LOGGER.log(Level.INFO, "Changed the sourceXML. Not generating metamodel");
    }
}
