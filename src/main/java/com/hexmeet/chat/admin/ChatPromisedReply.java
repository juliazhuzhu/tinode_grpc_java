package com.hexmeet.chat.admin;

import pbx.Model.ServerMsg;
import java.util.Date;

public class ChatPromisedReply {
	
	private long genTime;//mill seconds
	private String msgId;
	
	public ChatPromisedReply(ChatServerMsgHandler handler) {
		msg_handler = handler;
		Date date = new Date(); 
		genTime = date.getTime();
	}

	private ChatServerMsgHandler msg_handler;
	
	public void resolve(ServerMsg serverMsg) {
		
		msg_handler.onSucess(serverMsg);
	}
	
	public void reject(String msgId, int code, String text, String reason) {
		
		msg_handler.onFailure(msgId, code, text);
	}
	
	public long getGenTime() {
		return genTime;
	}
	
	public void setMsgId(String id) {
		msgId = id;
	}
	
	public String getMsgId() {
		return msgId;
	}
	
	
	
}
