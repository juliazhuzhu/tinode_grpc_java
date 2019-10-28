package com.hexmeet.chat.admin;

public class ChatAdminGrpMsgEvent extends ChatAdminMsgEvent{

	private String groupId;
	public void setGroupId(String groupId) {
		this.groupId = groupId;
	}
	
	public String getGroupId() {
		return groupId;
	}
}
