/*
 * Copyright (C) 2011 Everit Kft. (http://www.everit.biz)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.everit.persistence.lqmg.maven;

import java.io.File;
import java.net.MalformedURLException;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.plugins.annotations.ResolutionScope;
import org.apache.maven.project.MavenProject;
import org.everit.persistence.lqmg.GenerationProperties;
import org.everit.persistence.lqmg.LQMG;

/**
 * Generates QueryDSL Metadata classes from Liquibase schema files.
 */
@Mojo(name = "generate", requiresProject = true,
    requiresDependencyResolution = ResolutionScope.COMPILE_PLUS_RUNTIME)
public class GenerateMojo extends AbstractMojo {

  /**
   * The expression of the schema that should be the starting point of the generation. Expressions
   * references to the name of a "liquibase.schema" capability of a bundle. E.g. If the bundle has
   * "Provide-Capability: liquibase.changelog;liquibase.changelog=mySchema", the value of this
   * property should be simply "mySchema". It is possible to append filter to the expression. E.g.:
   * mySchema;filter:=("someAttribute=someValue").
   */
  @Parameter(required = true, property = "lqmg.capability")
  private String capability;

  /**
   * The path of the main configuration XML. Optional. If defined, the naming rules in this XML have
   * higher priority than the ones specified in the bundles.
   */
  @Parameter(required = false, property = "lqmg.configFile")
  private String configFile;

  /**
   * Comma separated list of contexts Liquibase is running under.
   */
  @Parameter(required = false, property = "lqmg.contexts")
  private String contexts;

  /**
   * Default schema that appears in the generated Metadata classes. The schema of tables and views
   * might be overridden in the changelog files directly.
   */
  @Parameter(property = "lqmg.defaultSchema", defaultValue = "${project.artifactId}")
  private String defaultSchema;

  /**
   * If true, bundles that cannot be resolved are re-deployed in the way that their unsatisfied
   * requirements are changed to be optional.
   */
  @Parameter(property = "lqmg.hackWires", defaultValue = "true")
  private boolean hackWires;

  /**
   * Whether to generate inner classes in Metadata classes for primary and foreign keys or not.
   */
  @Parameter(property = "lqmg.innerClassesForKeys", defaultValue = "true")
  private boolean innerClassesForKeys;

  /**
   * The folder where source will be generated to.
   */
  @Parameter(required = true, property = "lqmg.outputFolder",
      defaultValue = "src/main/generated/java")
  private String outputFolder;

  /**
   * Comma separated list of packages that should be generated. If not defined, all packages will be
   * generated.
   */
  @Parameter(required = false, property = "lqmg.packages")
  private String packages;

  /**
   * Map of plugin artifacts.
   */
  @Parameter(defaultValue = "${plugin.artifactMap}", required = true, readonly = true)
  protected Map<String, Artifact> pluginArtifactMap;

  /**
   * The {@link MavenProject} which call the lqmg-maven-plugin.
   */
  @Parameter(property = "executedProject")
  private MavenProject project;

  @Override
  public void execute() throws MojoExecutionException {
    getLog().info("Start lqmg-maven-plugin.");
    GenerationProperties params = null;

    File targetFolderFile = new File(outputFolder);
    String generationFolder = new File(project.getBasedir()
        .getAbsolutePath(), outputFolder).getAbsolutePath();
    if (targetFolderFile.isAbsolute()) {
      generationFolder = new File(outputFolder).getAbsolutePath();
    }
    getLog().info("Generation target folder: " + generationFolder);
    String[] projectArtifactsPath = getProjectArtifactsPath();
    params = new GenerationProperties(capability,
        projectArtifactsPath, generationFolder);

    params.setConfigurationPath(configFile);
    params.setDefaultSchema(defaultSchema);
    params.setHackWires(hackWires);
    params.setInnerClassesForKeys(innerClassesForKeys);
    params.setContexts(contexts);

    if ((packages != null) && !"".equals(packages.trim())) {
      params.setPackages(packages.split(","));
    }

    LQMG.generate(params);
    getLog().info("Finished the metamodell generation at path " + generationFolder);
  }

  private String[] getProjectArtifactsPath() throws MojoExecutionException {
    Set<String> artifactsPath = new HashSet<String>();

    @SuppressWarnings("unchecked")
    Set<Artifact> dependencyArtifacts = project.getArtifacts();
    for (Artifact artifact : dependencyArtifacts) {
      String artifactFileURI = resolveArtifactFileURI(artifact);
      if (artifactFileURI != null) {
        getLog().info("Adding artifact: " + artifactFileURI);
        artifactsPath.add(artifactFileURI);
      }
    }

    Artifact projectArtifact = project.getArtifact();
    String projectArtifactURI = resolveArtifactFileURI(projectArtifact);
    if (projectArtifactURI != null) {
      getLog().info("Adding artifact (current project): " + projectArtifactURI);
      artifactsPath.add(projectArtifactURI);
    } else {
      String buildDirectoryString = project.getBuild().getOutputDirectory();
      File buildDirectory = new File(buildDirectoryString);
      if (buildDirectory.exists()) {
        try {
          String buildDirectoryURI = buildDirectory.toURI().toURL().toExternalForm();
          getLog().info("Adding build directory of current project: " + buildDirectoryURI);
          artifactsPath.add(buildDirectoryURI);
        } catch (MalformedURLException e) {
          getLog().error(e);
        }
      } else {
        getLog()
            .warn("Current project could not be added as a bundle. Please check if this is ok.");
      }
    }

    return artifactsPath.toArray(new String[artifactsPath.size()]);
  }

  private String resolveArtifactFileURI(final Artifact artifact) {
    File artifactFile = artifact.getFile();
    if (artifactFile != null) {
      try {
        return artifact.getFile().toURI().toURL().toExternalForm();
      } catch (MalformedURLException e) {
        getLog().error(e);
      }
    }
    return null;
  }

}
