package org.alex.soto.ds.test.impl;

import java.util.Map;

import org.osgi.service.component.ComponentFactory;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;
import org.osgi.service.component.annotations.ReferencePolicyOption;
import org.osgi.service.component.runtime.ServiceComponentRuntime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Component(immediate = true)
public class Booter {

	final private static Logger log = LoggerFactory.getLogger(Booter.class);

	@Reference
	private ServiceComponentRuntime scr;

	@Activate
	private void init() {
		log.info("Activated Booter");
	}

	@Reference(
			cardinality = ReferenceCardinality.MULTIPLE,
			policy = ReferencePolicy.DYNAMIC,
			policyOption = ReferencePolicyOption.GREEDY,
			target = "(component.factory=my.factory)",
			unbind = "unbindFactory")
	public void bindFactory(ComponentFactory factory, final Map<Object, Object> props) {
		log.info("Added factory");
		factory.newInstance(null);
	}

	public void unbindFactory(ComponentFactory factory) throws Exception {
		log.info("Removed factory");
	}
}
