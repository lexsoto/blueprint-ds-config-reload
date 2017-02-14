package org.alex.soto.ds.test.impl;

import java.util.Map;

import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Component(configurationPid = "my.config", factory = "my.factory")
public class FactoryComp {
	final private static Logger log = LoggerFactory.getLogger(FactoryComp.class);

	@Activate
	public void init(Map<String, String> config) {
		log.info("Activated FactoryComp");
	}

	@Deactivate
	public void shutdown() {
		log.info("Deactivated FactoryComp");
	}
}
