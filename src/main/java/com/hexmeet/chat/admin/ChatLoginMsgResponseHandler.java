package com.hexmeet.chat.admin;

import java.io.UnsupportedEncodingException;
import java.util.logging.Logger;

import pbx.Model.ServerMsg;

public class ChatLoginMsgResponseHandler extends ChatServerMsgHandlerImpl {

	private Logger logger = Logger.getLogger("HmChatAdminLog");
	@Override
	public void onSucess(ServerMsg msg) {
		
		//to notify that login is okay
		logger.info("login is oky");
		notifyChatAdminMsgEvent(msg.getCtrl().getId(),"ok",ChatAdminEvent.EVENT_TYPE.LOGIN_SUCCESS);
		//ChatAdminClient.getDefaultInstance().createChatGroup("hleelo", "");
	}
	
	@Override
	public void onFailure(String msgId,int code, String text) {
		//System.out.println("login is faied");
		//System.out.println(text);
		logger.warning("login failure "+ text);
		
		notifyChatAdminMsgEvent(msgId,text,ChatAdminEvent.EVENT_TYPE.LOGIN_FAILURE);


		
	
		
	}
}