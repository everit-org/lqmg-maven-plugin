package org.everit.osgi.dev.lqmg.maven;

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
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.Component;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.plugins.annotations.ResolutionScope;
import org.apache.maven.project.MavenProject;
import org.everit.osgi.dev.lqmg.GenerationProperties;
import org.everit.osgi.dev.lqmg.LQMG;
import org.sonatype.plexus.build.incremental.BuildContext;

/**
 * This class responsible to call the LiguiBase to QueryDSL metamodel generators
 * when using maven to generation.
 * 
 */
@Mojo(name = "generate", defaultPhase = LifecyclePhase.GENERATE_SOURCES, requiresProject = true, requiresDependencyResolution = ResolutionScope.COMPILE_PLUS_RUNTIME)
public class LiquiBaseToQueryDslMojo extends AbstractMojo {

	// /**
	// * The default empty value to schemaPattern.
	// */
	// private static final String EMPTY_VALUE =
	// "DEFAULT_EMPTY_VALUE_LB2QD_MAVEN_PLUGIN_TO_SCHEMA_PATTERN";

	/**
	 * Path to the liquibase changelog file.
	 */
	@Parameter(required = true, property = "lqmg.schemaExpression")
	private String schemaExpression;

	/**
	 * The java package of the generated QueryDSL metamodel classes.
	 * Default-value: empty, that means that the package will be either empty or
	 * derived from the schema.
	 */
	@Parameter(required = false, property = "lqmg.packageName", defaultValue = "")
	private String packageName;

	/**
	 * The folder where source will be generated to.
	 */
	@Parameter(required = true, property = "lqmg.targetFolder")
	private String targetFolder;

	/**
	 * The schema to package.
	 */
	@Parameter(required = false, property = "lqmg.schemaToPackage", defaultValue = "true")
	private boolean schemaToPackage;

	/**
	 * A schema name pattern; must match the schema name as it is stored in the
	 * database.
	 */
	@Parameter(required = false, property = "lqmg.schemaPattern", defaultValue = "")
	private String schemaPattern;

	/**
	 * The {@link MavenProject} which call the lb2qb-maven-plugin.
	 */
	@Parameter(readonly = true, required = true, defaultValue = "${project}")
	private MavenProject project;

	/**
	 * Map of plugin artifacts.
	 */
	@Parameter(defaultValue = "${plugin.artifactMap}", required = true, readonly = true)
	protected Map<String, Artifact> pluginArtifactMap;

	/**
	 * The {@link BuildContext} component instance.
	 */
	@Component
	protected BuildContext buildContext;

	private String[] getProjectArtifactsPath() throws MojoExecutionException {
		Set<String> artifactsPath = new HashSet<String>();

		@SuppressWarnings("unchecked")
		List<Artifact> artifacts = new ArrayList<Artifact>(
				project.getArtifacts());
		artifacts.add(project.getArtifact());

		for (Artifact artifact : artifacts) {
			File artifactFile = artifact.getFile();
			if (artifactFile != null) {
				getLog().info("Adding artifact: " + artifact);
				try {
					artifactsPath.add(artifact.getFile().toURI().toURL()
							.toExternalForm());
				} catch (MalformedURLException e) {
					getLog().error(e);
				}
			} else {
				getLog().warn(
						"Artifact "
								+ artifact
								+ " could not have been added as the location is unspecified. This happens normally "
								+ "if the plugin runs on the project without package phase defined before.");
			}

		}

		return artifactsPath.toArray(new String[artifactsPath.size()]);
	}

	@Override
	public void execute() throws MojoExecutionException {
		getLog().info("Start lb2qd-maven-plugin.");
		GenerationProperties params = null;

		File targetFolderFile = new File(targetFolder);
		String generationFolder = new File(project.getBasedir()
				.getAbsolutePath(), targetFolder).getAbsolutePath();
		if (targetFolderFile.isAbsolute()) {
			generationFolder = new File(targetFolder).getAbsolutePath();
		}
		getLog().info("Generation target folder: " + generationFolder);
		String[] projectArtifactsPath = getProjectArtifactsPath();
		params = new GenerationProperties(schemaExpression,
				projectArtifactsPath, generationFolder);

		getLog().info("Set schamaToPackage value. Value: " + schemaToPackage);
		// set the schemaToPackage
		params.setSchemaToPackage(schemaToPackage);

		getLog().info("Set schemaPattern value. Value: " + schemaPattern);
		// set schemaPattern if not a default value.
		if (schemaPattern != null) {
			params.setSchemaPattern(schemaPattern);
		}

		getLog().info("Set packageName value. Value: " + packageName);
		// set packagename if not a default value.
		if (packageName != null) {
			params.setPackageName(packageName);
		}

		// only generate if the sourceXML is changed. When building mvn clean
		// install this condition is true.
		File xmlFile = new File(schemaExpression);
		if (buildContext.hasDelta(xmlFile)) {
			getLog().info("Changed the sourceXML. Generating metamodel");
			LQMG.generate(params);
			getLog().info("Finished the metamodell generation.");
		} else {
			getLog().info(
					"Changelog XML was not changed. Not generating metamodel");
		}
	}

}
