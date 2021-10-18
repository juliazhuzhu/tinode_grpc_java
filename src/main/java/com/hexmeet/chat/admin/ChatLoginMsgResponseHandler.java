package com.hexmeet.chat.admin;


import java.util.logging.Logger;

import pbx.Model.ServerMsg;

public class ChatLoginMsgResponseHandler extends ChatServerMsgHandlerImpl {

	private Logger logger = Logger.getLogger("HmChatAdminLog");
	@Override
	public void onSucess(ServerMsg msg) {
		
		//to notify that login is okay
		logger.info("login is oky");
		notifyChatAdminMsgEvent(msg.getCtrl().getId(),"ok",ChatAdminEvent.EVENT_TYPE.LOGIN_SUCCESS);
		/*for (int i = 0 ;  i < 20; i++) {
			ChatTestThread test = new ChatTestThread();
			Thread test_thread = new Thread(test);
			test_thread.start();
		}*/
		
		//ChatAdminClient.getDefaultInstance().createChatGroup("beluga", "");
		
		//ChatAdminClient.getDefaultInstance().delAnonymousUser("usruLJZvKAWGLc");
		
		//ChatAdminClient.getDefaultInstance().addAnonymousUserToGroup("usr113445","grp01oXQ8x3y0s");
		//ChatAdminClient.getDefaultInstance().addAnonymousUserToGroup("usr113446","grp01oXQ8x3y0s");
		//ChatAdminClient.getDefaultInstance().addAnonymousUserToGroup("usr113447","grp01oXQ8x3y0s");
		
		//ChatAdminClient.getDefaultInstance().delChatGroup("grpnkSuv1OPiD8");
		//ChatAdminClient.getDefaultInstance().delChatGroup("grpnkSuv1OPiD8");
		//ChatAdminClient.getDefaultInstance().delChatGroup("grpnkSuv1OPiD8");
		
		//ChatAdminClient.getDefaultInstance().delAnonymousUser("usrMo7qmZfsE3k");
	}
	
	@Override
	public void onFailure(String msgId,int code, String text) {
		//System.out.println("login is faied");
		//System.out.println(text);
		logger.warning("login failure msgId:"+ msgId + " code:" + code + " reason " + text);
		
		notifyChatAdminMsgEvent(msgId,text,ChatAdminEvent.EVENT_TYPE.LOGIN_FAILURE);
		
	}
}