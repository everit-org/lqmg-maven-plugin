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
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.Execute;
import org.apache.maven.plugins.annotations.LifecyclePhase;
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
@Execute(phase = LifecyclePhase.PACKAGE)
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

}
