package com.hexmeet.chat.admin;

import pbx.Model.ServerMsg;

public class ChatLoginMsgResponseHandler implements ChatServerMsgHandler {

	@Override
	public void onSucess(ServerMsg msg) {
		
		//to notify that login is okay
		System.out.println("login is oky");
		ChatAdminClient.getDefaultInstance().attachChatGroup("grp4tVzpNn-KL8");
		ChatAdminClient.getDefaultInstance().delChatGroup("grp4tVzpNn-KL8");
	}
	
	@Override
	public void onFailure(String msgId,int code, String text) {
		//System.out.println("login is faied");
		System.out.println(text);
		
	}
}