package com.hexmeet.chat.admin;

import java.util.logging.Logger;

import pbx.Model.ServerMsg;

public class ChatHiMsgResponseHandler extends ChatServerMsgHandlerImpl {

	private Logger logger = Logger.getLogger("HmChatAdminLog");
	@Override
	public void onSucess(ServerMsg msg) {
		
		logger.info("hi okay");
		if (!ChatAdminClient.getDefaultInstance().isAuthenticated()) 
			ChatAdminClient.getDefaultInstance().login();
		//notifyChatAdminMsgEvent(msg.getCtrl().getId(),"ok",ChatAdminEvent.EVENT_TYPE.LOGIN_SUCCESS);
	}
	
	@Override
	public void onFailure(String msgId,int code, String text) {
		
		logger.info("hi failure code msgid:" + msgId + " code:"+ code + " text:" + text);
		notifyChatAdminMsgEvent(msgId,text,ChatAdminEvent.EVENT_TYPE.HI_FAILURE);
	}
}
