package com.hexmeet.chat.admin;


public class ChatAdminEvent {
	
	public enum EVENT_TYPE {
		HI_FAILURE,
		LOGIN_SUCCESS,
		ADDGROUP_SUCESS,
		DELGROUP_SUCESS,
		LOGIN_FAILURE,
		ADDGROUP_FAILURE,
		DELGROUP_FAILURE,
		NETWORK_FAILURE,
	};
	
	private EVENT_TYPE event;
	public EVENT_TYPE getEventType() {
		return event;
	}
	
	public void setEventType(EVENT_TYPE event) {
		this.event = event;
	}
	
	private String reason;
	public String getReason() {
		return reason;
	}
	
	public void setReason(String reason) {
		this.reason = reason;
	}
	
}
