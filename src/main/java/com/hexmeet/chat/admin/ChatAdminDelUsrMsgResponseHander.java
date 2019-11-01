package com.hexmeet.chat.admin;

import java.util.logging.Logger;

import pbx.Model.ServerMsg;

public class ChatAdminDelUsrMsgResponseHander extends ChatServerMsgHandlerImpl {

	private Logger logger = Logger.getLogger("HmChatAdminLog");
	
	@Override
	public void onSucess(ServerMsg msg) {
		String msgId = msg.getCtrl().getId();
		logger.info("del user successfully msgId " + msgId);
		notifyChatAdminMsgEvent(msgId,"ok",ChatAdminEvent.EVENT_TYPE.DELUSER_SUCCESS);
	}
	
	
	@Override
	public void onFailure(String msgId,int code, String text) {
		
		logger.info("del user failure msgId " + msgId);
		notifyChatAdminMsgEvent(msgId,text,ChatAdminEvent.EVENT_TYPE.DELUSER_FAILURE);
	}
}
