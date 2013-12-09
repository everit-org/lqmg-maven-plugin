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

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.everit.db.lqmg.Main;

/**
 * Generating QueryDSL java sources.
 */
@Mojo(name = "generate", defaultPhase = LifecyclePhase.GENERATE_SOURCES)
public class LiquiBaseToQueryDslMojo extends AbstractMojo { // implements ILifecycleMapping {

    /**
     * The LiquiBase XML place on the classpath.
     */
    @Parameter(required = true, property = "lb2qd.sourceXML")
    private String sourceXML;

    /**
     * The package where the generated Java classes.
     */
    @Parameter(required = true, property = "lb2qd.packageName")
    private String packageName;

    /**
     * The place where generated the classes. Default value is ${project.build.directory}.
     */
    @Parameter(required = true, property = "lb2qd.targetFolder",
            defaultValue = "${project.build.directory}/generated-sources/lbqd")
    private String targetFolder;

    /**
     * Those are the location of the XML by commas, which are needed for external relations, but java class will be
     * generated from them.
     */
    @Parameter(required = false, property = "lb2qd.externalXMLs")
    private String externalXMLs;

    // @Override
    // public void configure(final ProjectConfigurationRequest request, final IProgressMonitor monitor)
    // throws CoreException {
    // for (AbstractProjectConfigurator configurator : getProjectConfigurators(request.getMavenProjectFacade(),
    // monitor)) {
    // if (monitor.isCanceled()) {
    // throw new OperationCanceledException();
    // }
    // configurator.configure(request, monitor);
    // }
    // }

    @Override
    public void execute() throws MojoExecutionException {
        Main.generate(sourceXML, packageName, targetFolder, externalXMLs);
    }

    // @Override
    // public Map<MojoExecutionKey, List<AbstractBuildParticipant>> getBuildParticipants(
    // final IMavenProjectFacade project,
    // final IProgressMonitor monitor) throws CoreException {
    // MojoExecutionKey key = new MojoExecutionKey("org.everit.db", "lb2qd-maven-plugin", "1.0.0-SNAPSHOT",
    // "generate", LifecyclePhase.GENERATE_SOURCES.id(), UUID.randomUUID().toString());
    //
    // return null;
    // };
    //
    // @Override
    // public String getId() {
    // // TODO Auto-generated method stub
    // return null;
    // }
    //
    // @Override
    // public String getName() {
    // // TODO Auto-generated method stub
    // return null;
    // }
    //
    // @Override
    // public void unconfigure(final ProjectConfigurationRequest request, final IProgressMonitor monitor)
    // throws CoreException {
    // for (AbstractProjectConfigurator configurator : getProjectConfigurators(request.getMavenProjectFacade(),
    // monitor)) {
    // if (monitor.isCanceled()) {
    // throw new OperationCanceledException();
    // }
    // configurator.unconfigure(request, monitor);
    // }
    //
    // }
}
