package com.hexmeet.chat.admin;

public class ChatAdminGroupMsgEvent extends ChatAdminMsgEvent{

	private String groupId;
	public void setGroupId(String groupId) {
		this.groupId = groupId;
	}
	
	public String getGroupId() {
		return groupId;
	}
}
