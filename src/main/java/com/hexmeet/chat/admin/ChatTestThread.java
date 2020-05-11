package com.hexmeet.chat.admin;

public class ChatTestThread implements Runnable {
	
	
	
	@Override
    public void run() {
		ChatAdminClient.getDefaultInstance().createChatGroup("beluga", "");
	}
	
	
		
}