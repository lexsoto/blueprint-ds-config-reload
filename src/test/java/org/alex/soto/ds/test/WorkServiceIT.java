package org.alex.soto.ds.test;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.ops4j.pax.exam.CoreOptions.maven;
import static org.ops4j.pax.exam.karaf.options.KarafDistributionOption.editConfigurationFileExtend;
import static org.ops4j.pax.exam.karaf.options.KarafDistributionOption.features;
import static org.ops4j.pax.exam.karaf.options.KarafDistributionOption.karafDistributionConfiguration;
import static org.ops4j.pax.exam.karaf.options.KarafDistributionOption.keepRuntimeFolder;
import static org.ops4j.pax.exam.karaf.options.KarafDistributionOption.logLevel;

import java.io.File;
import java.util.Collection;
import java.util.Collections;
import java.util.Dictionary;
import java.util.Hashtable;
import java.util.List;

import javax.inject.Inject;

import org.apache.karaf.bundle.core.BundleInfo;
import org.apache.karaf.bundle.core.BundleService;
import org.apache.karaf.bundle.core.BundleState;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.ops4j.pax.exam.Configuration;
import org.ops4j.pax.exam.MavenUtils;
import org.ops4j.pax.exam.Option;
import org.ops4j.pax.exam.ProbeBuilder;
import org.ops4j.pax.exam.TestProbeBuilder;
import org.ops4j.pax.exam.junit.PaxExam;
import org.ops4j.pax.exam.karaf.options.LogLevelOption;
import org.ops4j.pax.exam.options.MavenUrlReference;
import org.ops4j.pax.exam.spi.reactors.ExamReactorStrategy;
import org.osgi.framework.Bundle;
import org.osgi.framework.Constants;
import org.osgi.service.cm.ConfigurationAdmin;
import org.osgi.service.component.runtime.ServiceComponentRuntime;
import org.osgi.service.component.runtime.dto.ComponentConfigurationDTO;
import org.osgi.service.component.runtime.dto.ComponentDescriptionDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@RunWith(PaxExam.class)
@ExamReactorStrategy(org.ops4j.pax.exam.spi.reactors.PerClass.class)
public class WorkServiceIT {

	private static Logger log = LoggerFactory.getLogger(WorkServiceIT.class);

	@Inject
	private BundleService bundleService;
	@Inject
	private ServiceComponentRuntime scr;
	@Inject
	protected ConfigurationAdmin configurationAdmin;

	@ProbeBuilder
	public TestProbeBuilder probeConfiguration(TestProbeBuilder probe) {
		probe.setHeader(Constants.DYNAMICIMPORT_PACKAGE, "*,org.apache.felix.service.*;status=provisional");
		return probe;
	}

	@Configuration
	public Option[] config() {

		MavenUrlReference repo = maven()
				.groupId("org.alex.soto")
				.artifactId("blueprint-ds-config-reload")
				.versionAsInProject()
				.classifier("features")
				.type("xml");

		Option[] options = new Option[] {
				karafConfig(),
				keepRuntimeFolder(), logLevel(LogLevelOption.LogLevel.INFO),

				editConfigurationFileExtend("etc/org.ops4j.pax.logging.cfg", "log4j.logger.org.apache.sshd", "ERROR"),
				editConfigurationFileExtend("etc/org.ops4j.pax.logging.cfg", "log4j.logger.org.apache.camel", "INFO"),

				features(repo, "worker-feature")
		};
		return options;
	}

	private Option karafConfig() {
		return karafDistributionConfiguration().frameworkUrl(
				maven()
						.groupId("org.apache.karaf")
						.artifactId("apache-karaf")
						.versionAsInProject()
						.type("tar.gz"))
				.karafVersion(
						MavenUtils.getArtifactVersion("org.apache.karaf", "apache-karaf"))
				.name("Apache Karaf")
				.unpackDirectory(new File("target/exam/"));
	}

	@Test
	public void test() throws Exception {
		Thread.sleep(5_000);
		assertTrue(isHealthy());

		org.osgi.service.cm.Configuration configuration = configurationAdmin.getConfiguration("my.config", null);
		assertNotNull(configuration);
		Dictionary<String, String> properties = new Hashtable<String, String>();
		properties.put("work.to.be.done", "eat");
		configuration.update(properties);

		Thread.sleep(5_000);
		assertTrue(isHealthy());
	}

	public boolean isHealthy() {
		boolean isHealthy = true;

		List<Bundle> bundles = bundleService.selectBundles("0", Collections.emptyList(), true);
		for (Bundle bundle : bundles) {
			BundleInfo info = this.bundleService.getInfo(bundle);
			BundleState state = info.getState();
			if (BundleState.Active != state) {
				log.info("System not healthy due to bundle '{}' being in state {}.  The expected state is {}.", info.getName(), state, BundleState.Active);
				isHealthy = false;
			}
		}

		Collection<ComponentDescriptionDTO> descriptions = scr.getComponentDescriptionDTOs();
		for (final ComponentDescriptionDTO descDTO : descriptions) {
			// not enabled components ignored
			if (!scr.isComponentEnabled(descDTO)) continue;

			final Collection<ComponentConfigurationDTO> configs = scr.getComponentConfigurationDTOs(descDTO);
			ComponentConfigurationDTO configDTO = null;
			if (!configs.isEmpty()) {
				configDTO = configs.iterator().next();
			}

			final boolean active = isActive(configDTO);
			if (!active) {
				log.info("System not healthy due to component '{}' being not active. Current state: {}.", descDTO.name, (configDTO == null) ? "null" : configDTO.state);
				isHealthy = false;
			}
		}
		log.info("Healthy: {}.", isHealthy);
		return isHealthy;
	}

	private boolean isActive(ComponentConfigurationDTO configuration) {
		if (configuration == null) return false;

		log.info("Component '{}' state {}", configuration.description.name, configuration.state);
		if (configuration.unsatisfiedReferences != null && configuration.unsatisfiedReferences.length > 0) return false;

		return ComponentConfigurationDTO.ACTIVE == configuration.state ||
				ComponentConfigurationDTO.SATISFIED == configuration.state;
	}
}
