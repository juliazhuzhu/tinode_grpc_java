package com.hexmeet.chat.admin;

import java.util.logging.Logger;

import pbx.Model.ServerMsg;

public class ChatAdminCreateGroupMsgResponseHandler extends ChatServerMsgHandlerImpl {

	private Logger logger = Logger.getLogger("HmChatAdminLog");
	
	@Override
	public void onSucess(ServerMsg msg) {
		String msgId = msg.getCtrl().getId();
		String topic = msg.getCtrl().getTopic();
		
		logger.info("create chat gropu success topic: " + topic + " msgid: " + msgId);
		
		
		ChatAdminGroupMsgEvent event = new ChatAdminGroupMsgEvent();
		event.setEventType(ChatAdminEvent.EVENT_TYPE.ADDGROUP_SUCCESS);
		event.setMsgId(msgId);
		event.setGroupId(topic);
		ChatAdminClient.getDefaultInstance().getEventQueue().push(event);
		
		//ChatAdminClient.getDefaultInstance().delChatGroup(topic);
	}
	
	@Override
	public void onFailure(String msgId,int code, String text) {
		
		logger.warning("create chat gropu failure msgid: " + msgId);
		
		ChatAdminGroupMsgEvent event = new ChatAdminGroupMsgEvent();
		event.setEventType(ChatAdminEvent.EVENT_TYPE.ADDGROUP_FAILURE);
		event.setMsgId(msgId);
		ChatAdminClient.getDefaultInstance().getEventQueue().push(event);
		//event.setGroupId(topic);
	}
}
