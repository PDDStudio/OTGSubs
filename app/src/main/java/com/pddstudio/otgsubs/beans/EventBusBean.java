package com.pddstudio.otgsubs.beans;

import org.androidannotations.annotations.AfterInject;
import org.androidannotations.annotations.EBean;
import org.greenrobot.eventbus.EventBus;

/**
 * Created by pddstudio on 21/04/2017.
 */

@EBean(scope = EBean.Scope.Singleton)
public class EventBusBean {

	private EventBus bus;

	@AfterInject
	protected void prepareBean() {
		if(bus == null) {
			bus = EventBus.getDefault();
		}
	}

	public EventBus getEventBus() {
		return bus;
	}

	public void post(Object o) {
		getEventBus().post(o);
	}

	public void register(Object o) {
		getEventBus().register(o);
	}

	public void unregister(Object o) {
		getEventBus().unregister(o);
	}

}
