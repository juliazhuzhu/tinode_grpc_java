package com.hexmeet.chat.admin;

import pbx.Model.ServerMsg;

public interface ChatServerMsgHandler {
	
	public void onSucess(ServerMsg msg);
	public void onFailure(String msgId,int code, String text);
}