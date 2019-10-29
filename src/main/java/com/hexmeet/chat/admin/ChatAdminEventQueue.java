package com.hexmeet.chat.admin;

import java.util.LinkedList;
import java.util.Queue;
import java.util.logging.Logger;

public class ChatAdminEventQueue implements Runnable{

	private Logger logger = Logger.getLogger("HmChatAdminLog");
	
	private int run;
	{
		run = 1;
	}
	
	public void run() {
	
		
		while(run == 1) {
			
			//handle queue event
			//log print time
			//notify the notifier
			//log print time
			ChatAdminEvent event = null;
			synchronized(this) {
				if (queue.size() > 0) {
					event = queue.poll();
				}
			}
			if (event != null) {
				
				logger.info("notify event start type " + event.getEventType());
				//notify 
				if (ChatAdminClient.getDefaultInstance().getEventNotifier() != null) {
					ChatAdminClient.getDefaultInstance().getEventNotifier().onEvent(event);
				}
				logger.info("notify event end type " + event.getEventType());
			}
		}
	}
	
	public void stop() {
		this.run = 0;
	}
	
	public void push(ChatAdminEvent event) {
		synchronized(this){
			queue.offer(event);
		}
		
	}
	
	private Queue<ChatAdminEvent> queue = new LinkedList<ChatAdminEvent>();
}
