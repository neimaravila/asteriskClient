package br.com.virtualsistemas.asteriskclient.model;

import java.util.Date;

import org.asteriskjava.manager.event.ManagerEvent;

public class AsteriskEvent {

	private String name;
	private Date dateReceived;
	private ManagerEvent event;
	
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	
	public Date getDateReceived() {
		return dateReceived;
	}
	public void setDateReceived(Date dateReceived) {
		this.dateReceived = dateReceived;
	}
	public ManagerEvent getEvent() {
		return event;
	}
	public void setEvent(ManagerEvent event) {
		this.event = event;
	}
	
}
