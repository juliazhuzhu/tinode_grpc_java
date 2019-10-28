package com.hexmeet.chat.admin;

import java.util.logging.Logger;

import pbx.Model.ServerMsg;

public class ChatLoginMsgResponseHandler implements ChatServerMsgHandler {

	private Logger logger = Logger.getLogger("HmChatAdminLog");
	@Override
	public void onSucess(ServerMsg msg) {
		
		//to notify that login is okay
		logger.info("login is oky");
		//ChatAdminClient.getDefaultInstance().attachChatGroup("grp4tVzpNn-KL8");
		//ChatAdminClient.getDefaultInstance().delChatGroup("grp4tVzpNn-KL8");
	}
	
	@Override
	public void onFailure(String msgId,int code, String text) {
		//System.out.println("login is faied");
		//System.out.println(text);
		logger.warning("login failure "+ text);
		
	}
}