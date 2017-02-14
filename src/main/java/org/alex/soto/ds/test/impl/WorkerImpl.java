package org.alex.soto.ds.test.impl;

import java.util.Map;

import org.alex.soto.ds.test.WorkService;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

// @Component(configurationPid = "my.config", factory = "my.factory")
@Component(immediate = true, configurationPid = "my.config")
public class WorkerImpl implements WorkService {

	private String workToBeDone;
	final private static Logger log = LoggerFactory.getLogger(WorkerImpl.class);

	@Activate
	public void init(Map<String, String> config) {
		workToBeDone = config.getOrDefault("work.to.be.done", "rest");
		log.info("Activated WorkerImpl");
	}

	@Deactivate
	public void shutdown() {
		log.info("Shutting down WorkerImpl");
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.alex.soto.ds.test.WorkService#doWork()
	 */
	@Override
	public void doWork() {
		log.info("Doing work: {}.", workToBeDone);
	}
}
