package com.hexmeet.chat.admin;

import pbx.Model.ServerMsg;

public class ChatHiMsgResponseHandler implements ChatServerMsgHandler {

	@Override
	public void onSucess(ServerMsg msg) {
		
		ChatAdminClient.getDefaultInstance().login();
	}
	
	@Override
	public void onFailure(String msgId,int code, String text) {}
}
