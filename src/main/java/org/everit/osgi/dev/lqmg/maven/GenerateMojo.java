/**
 * This file is part of Everit - LQMG Maven Plugin.
 *
 * Everit - LQMG Maven Plugin is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Everit - LQMG Maven Plugin is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Everit - LQMG Maven Plugin.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.everit.osgi.dev.lqmg.maven;

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
import org.everit.osgi.dev.lqmg.GenerationProperties;
import org.everit.osgi.dev.lqmg.LQMG;

/**
 * Generates QueryDSL Metadata classes from Liquibase schema files.
 */
@Mojo(name = "generate", requiresProject = true, requiresDependencyResolution = ResolutionScope.COMPILE_PLUS_RUNTIME)
public class GenerateMojo extends AbstractMojo {

    /**
     * The path of the main configuration XML. Optional. If defined, the naming rules in this XML have higher priority
     * than the ones specified in the bundles.
     */
    @Parameter(required = false, property = "lqmg.configFile")
    private String configFile;

    /**
     * The folder where source will be generated to.
     */
    @Parameter(required = true, property = "lqmg.outputFolder", defaultValue = "src/main/generated/java")
    private String outputFolder;

    /**
     * Comma separated list of packages that should be generated. If not defined, all packages will be generated.
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

    /**
     * The expression of the schema that should be the starting point of the generation. Expressions references to the
     * name of a "liquibase.schema" capability of a bundle. E.g. If the bundle has
     * "Provide-Capability: liquibase.schema;name=mySchema", the value of this property should be simply "mySchema". It
     * is possible to append filter to the expression. E.g.: mySchema;filter:=("someAttribute=someValue").
     */
    @Parameter(required = true, property = "lqmg.schema")
    private String schema;

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
        params = new GenerationProperties(schema,
                projectArtifactsPath, generationFolder);

        params.setConfigurationPath(configFile);
        if (packages != null && !packages.trim().equals("")) {
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
                getLog().warn("Current project could not be added as a bundle. Please check if this is ok.");
            }
        }

        return artifactsPath.toArray(new String[artifactsPath.size()]);
    }

    private String resolveArtifactFileURI(Artifact artifact) {
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
