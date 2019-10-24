package com.hexmeet.chat.admin;
import java.util.*;
public class ChatFutures {
	
	{
		reply_map = new HashMap<String,ChatPromisedReply>();
	}
	
	public void push(String msgId,ChatPromisedReply reply) {
		
		synchronized (this) {
			reply_map.put(msgId, reply);
		}
	}
	
	ChatPromisedReply retrieveReply(String msgId) {
		ChatPromisedReply reply = null;
			synchronized (this) {
			if (reply_map.containsKey(msgId)) {
				reply = reply_map.get(msgId);
				reply_map.remove(msgId);
			}
		}
		return reply;
	}
	
	public void rejectAndPurgeAll(int code, String reason) {//called when grpc disconnected
		
		synchronized (this) {
			for (ChatPromisedReply reply : reply_map.values()) {
				 
			    //System.out.println("Value = " + value);
				reply.reject(reply.getMsgId(), code, "reject", reason);
			 
			}
			reply_map.clear();
		}

	}
	
	public void process() {
		//to handle outdated msg
		
		synchronized (this) {
			Date date = new Date(); 
			long now = date.getTime();
			for (Map.Entry<String, ChatPromisedReply> entry : reply_map.entrySet()) {
			//for (ChatPromisedReply reply : reply_map.values()) {
				ChatPromisedReply reply = entry.getValue();
			    //System.out.println("Value = " + value);
				if (now - reply.getGenTime() > 10 * 1000) {
					reply.reject(reply.getMsgId(), 504, "reject", "timeout");
					reply_map.remove(entry.getKey());
					return;
					
				}
			 
			}
		}
	}
	
	
	private HashMap<String,ChatPromisedReply> reply_map;
	

}
