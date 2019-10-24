package com.hexmeet.chat.admin;

import pbx.Model.ServerMsg;

public class HiMsgResponseHandler implements ChatServerMsgHandler {

	@Override
	public void onSucess(ServerMsg msg) {}
	
	@Override
	public void onFailure(String msgId,int code, String text) {}
}
