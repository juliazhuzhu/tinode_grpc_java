package com.hexmeet.chat.admin;

import java.util.logging.Logger;

import pbx.Model.ServerMsg;

public class ChatHiMsgResponseHandler implements ChatServerMsgHandler {

	private Logger logger = Logger.getLogger("HmChatAdminLog");
	@Override
	public void onSucess(ServerMsg msg) {
		logger.info("hi okay");
		ChatAdminClient.getDefaultInstance().login();
	}
	
	@Override
	public void onFailure(String msgId,int code, String text) {}
}
