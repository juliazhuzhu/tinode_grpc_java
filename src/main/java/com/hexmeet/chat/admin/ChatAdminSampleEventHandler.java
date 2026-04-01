package com.hexmeet.chat.admin;

import com.hexmeet.chat.admin.ChatEventNotifier;
import com.hexmeet.chat.admin.ChatAdminClient;
import com.hexmeet.chat.admin.ChatAdminEvent;
import com.hexmeet.chat.admin.ChatAdminMsgEvent;
import com.hexmeet.chat.admin.ChatAdminGroupMsgEvent;

public class ChatAdminSampleEventHandler implements ChatEventNotifier{

	public void onEvent(ChatAdminEvent event) {
		System.out.println(event.getEventType());
		
		if (ChatAdminEvent.EVENT_TYPE.LOGIN_SUCCESS == event.getEventType()) {
			
			ChatAdminClient.getDefaultInstance().createChatGroup("beluga","1234");
			//ChatAdminClient.getDefaultInstance().delChatGroup("grp304Ksej2gvs");
			//ChatAdminClient.getDefaultInstance().delAnonymousUser("usrsrr2ew");
		}else if (ChatAdminEvent.EVENT_TYPE.NETWORK_FAILURE == event.getEventType()) {
			
			ChatAdminClient.getDefaultInstance().stop();
			try {
				Thread.sleep(3*1000);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			ChatAdminClient.getDefaultInstance().start();
			
		}
	}
}
