/*******************************************************************************
 * Copyright (c) 2012 GamezGalaxy.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 ******************************************************************************/
package net.mcforge.API.player;

import net.mcforge.API.Cancelable;
import net.mcforge.API.EventList;
import net.mcforge.iomodel.Player;

public class PlayerChatEvent extends PlayerEvent implements Cancelable {

	private static EventList events = new EventList();
	
	private boolean _canceled; 
	
	private String message;
	
	private String orginalmessage;
	
	public PlayerChatEvent(Player who, String message) {
		super(who);
		this.message = message;
		this.orginalmessage = message;
	}
	
	@Override
	public EventList getEvents() {
		return events;
	}
	/**
	 * Get a list of registered listeners
	 * @return The list of listeners
	 */
	public static EventList getEventList() {
		return events;
	}
	
	public String getMessage() {
		return message;
	}
	
	public String getOrginalMessage() {
		return orginalmessage;
	}
	
	public void setMessage(String message) {
		this.message = message;
	}

	@Override
	public boolean isCancelled() {
		return _canceled;
	}

	@Override
	public void Cancel(boolean cancel) {
		_canceled = cancel;
	}
}