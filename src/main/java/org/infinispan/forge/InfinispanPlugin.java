/**
 * Copyright 2012 Red Hat, Inc. and/or its affiliates.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA
 * 02110-1301 USA
 */
package org.infinispan.forge;

import javax.enterprise.event.Event;
import javax.inject.Inject;

import org.infinispan.forge.facet.InfinispanConfiguration;
import org.infinispan.forge.facet.InfinispanExample;
import org.infinispan.forge.facet.InfinispanExamples;
import org.infinispan.forge.facet.shell.InfinispanConfigurationCompleter;
import org.infinispan.forge.facet.shell.InfinispanExampleCompleter;
import org.infinispan.forge.util.FacetUtils;
import org.jboss.forge.project.Project;
import org.jboss.forge.project.facets.events.InstallFacets;
import org.jboss.forge.shell.PromptType;
import org.jboss.forge.shell.Shell;
import org.jboss.forge.shell.plugins.Alias;
import org.jboss.forge.shell.plugins.Command;
import org.jboss.forge.shell.plugins.Option;
import org.jboss.forge.shell.plugins.PipeOut;
import org.jboss.forge.shell.plugins.Plugin;
import org.jboss.forge.shell.plugins.RequiresProject;

/**
 * @author Andre Dietisheim
 */
@Alias("infinispan")
@RequiresProject
public class InfinispanPlugin implements Plugin {

	@Inject
	private Project project;
	@Inject
	private Event<InstallFacets> installFacetEvent;
	@Inject
	private InfinispanExamples examples;
	@Inject
	private Shell shell;
	
	@Command("create-example")
	public void createExample(
			@Option(name = "targetPackage", description="the base package to use when scaffolding", type = PromptType.JAVA_PACKAGE) 
			final String targetPackage,
			@Option(name = "example", description="the name of the existing example (ex. lifespan)", required = true, completer = InfinispanExampleCompleter.class) 
			final String exampleName,
			@Option(name = "configuration", description="the name of an existing configuration (ex. local)", required = true,  completer = InfinispanConfigurationCompleter.class) 
			final String configurationName,
			@Option(flagOnly = true, name = "overwrite") 
			final boolean overwrite,
			PipeOut out) {

		InfinispanExample example = getExample(exampleName);
		FacetUtils.installFacet(example.getClass(), project, installFacetEvent, out);
		InfinispanExample facet = project.getFacet(example.getClass());

		InfinispanConfiguration configuration = getConfiguration(configurationName);
		facet.create(targetPackage, configuration);
	}

	private InfinispanExample getExample(final String exampleName) {
		InfinispanExample example = examples.getByAlias(exampleName);
		while (example == null) {
			String[] names = examples.getNamesArray();
			int choice = shell.promptChoice(
					"example " + exampleName + " not found, please choose among the known ones:",
					(Object[]) names);
			example = examples.getByAlias(names[choice]);
		}
		return example;
	}

	private InfinispanConfiguration getConfiguration(
			final String configurationName) {
		InfinispanConfiguration configuration = InfinispanConfiguration.getByName(configurationName);
		while(configuration == null) {
			String[] names = InfinispanConfiguration.getNamesArray();
			int choice = shell.promptChoice(
					"configuration " + configurationName + " not found, please choose among the knonw ones:", 
					(Object[]) names);
			configuration = InfinispanConfiguration.getByName(names[choice]);
		}

		return configuration;
	}

}
