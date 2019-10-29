package com.hexmeet.chat.admin;

import pbx.Model.ServerMsg;

public class ChatServerMsgHandlerImpl implements ChatServerMsgHandler {

	@Override
	public void onSucess(ServerMsg msg) {
		
	}
	
	@Override
	public void onFailure(String msgId,int code, String text) {
		
		
	}
	
	
	
	public void notifyChatAdminMsgEvent(String msgId, String reason,ChatAdminEvent.EVENT_TYPE type) {
		
		ChatAdminMsgEvent event = new ChatAdminMsgEvent();
		event.setEventType(type);
		event.setMsgId(msgId);
		event.setReason(reason);
		
		ChatAdminClient.getDefaultInstance().getEventQueue().push(event);
		
	}
	
}




