package com.hexmeet.chat.admin;

import pbx.NodeGrpc;
import pbx.Model.ClientAcc;
import pbx.Model.ClientDel;
import pbx.Model.ClientHi;
import pbx.Model.ClientLogin;
import pbx.Model.ClientMsg;
import pbx.Model.ClientSub;
import pbx.Model.ServerMsg;
import pbx.Model.ClientDel.What;
import pbx.Model.ServerMsg.MessageCase;

import com.alibaba.fastjson.JSONObject;
import com.google.protobuf.ByteString;
//import com.google.protobuf.Descriptors.FieldDescriptor;
//import com.google.protobuf.Message;
//import com.google.protobuf.MessageOrBuilder;
import java.util.logging.Level; 
import java.util.logging.Logger;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.logging.*; 


import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.stub.StreamObserver;
import java.util.*;

public class ChatAdminClient implements Runnable{
	
    private String 						host;
    private int							port;
    private String						user;
    private String						password;
    private ManagedChannel 				channel;
    private NodeGrpc.NodeStub 			stub;
    private StreamObserver<ClientMsg> 	cliObserver;
    private int 						msgId;
    private ChatFutures					futures;
    private static final ChatAdminClient DEFAULT_INSTANCE;
    private int							grpc_exit;
    private	static final Logger logger = Logger.getLogger("HmChatAdminLog");
    private String 						logFilePath;
    private ChatEventNotifier			eventNotifier;
    private ChatAdminEventQueue			eventQueue;
    private Thread 						event_thread;
    private FileHandler 				fh;
    private Map<String, List<String>> 	usermap;
    private final Object msgLock	=  new Object();
    private boolean					authenticated = false;
    static {
      DEFAULT_INSTANCE = new ChatAdminClient();
    }

    public static ChatAdminClient getDefaultInstance() {
      return DEFAULT_INSTANCE;
    }
    
    
    ChatAdminClient(){
    	host = "127.0.0.1";
    	port = 6061;
    	
    	try {
    		  
    		if (logFilePath == null) {
    			logFilePath = "adminchat.%g.log";
    		}
    		else {
    			logFilePath = logFilePath + "/adminchat.%g.log";
    			
    		}
    		
            fh = new FileHandler(logFilePath,5242880,5,true);
            logger.addHandler(fh);
            SimpleFormatter formatter = new SimpleFormatter();  
            fh.setFormatter(formatter);  
            
            usermap = new HashMap<String, List<String>>(); 
    	}
        catch (SecurityException e) {  
            e.printStackTrace();  
        } catch (IOException e) {  
            e.printStackTrace();  
        }  
    	eventQueue = new ChatAdminEventQueue();
    	event_thread = new Thread(eventQueue);
    }
    
    private String genMsgId() {
    	msgId = msgId + 1;
    	return String.valueOf(msgId);
    }
    
    private void messageLoop() {
    	
    	//ChatAdminClient admin = ChatAdminClient.getDefaultInstance();
    	
    	msgId = 0;
    	grpc_exit = 0;
    	
    	futures = new ChatFutures();
    	channel = ManagedChannelBuilder.forAddress(host, port)
                .usePlaintext(true)
                .build();
    	stub = NodeGrpc.newStub(channel);
    	
    	cliObserver = stub.messageLoop(new StreamObserver<ServerMsg>() {

            // Handler for messages from the server

            @Override
            public void onNext(ServerMsg msg) {
                // Display the message
            	//System.out.println(msg.getMessageCase());
            	logger.info("onNext: " + msg.getMessageCase() + " " + msg.getCtrl().getId() + " " + msg.getCtrl().getCode());
            	if (MessageCase.CTRL == msg.getMessageCase()) {
            		//hadle hi
            		//System.out.println(msg.getCtrl().getId());
            		//System.out.println(msg.getCtrl().getCode());
            		//if (msg.getCtrl().getCode() != 201) 
            		//	logger.info("onNext: retrieve " + msg.getCtrl().getId());
            		ChatPromisedReply reply = futures.retrieveReply(msg.getCtrl().getId());
            		if (reply != null) {
	            		int code = msg.getCtrl().getCode();
	            		logger.info("onNext: " + code);
	            		if (code >= 200 && code < 400) {
	            			reply.resolve(msg);
	            			
	            		}
	            		else {
	            			logger.info("onNext reject: " + msg.getCtrl().getText());
	            			String text = msg.getCtrl().getText();
	            			//ByteString content = msg.getCtrl().getParamsMap().get("what");
	            			//String reason = content.toString();
	            			reply.reject(msg.getCtrl().getId(),code,text,"");
	            			
	            		}
            		}else {
            			//if (msg.getCtrl().getCode() != 201)
            			//	logger.info("onNext: failure to locate " + msg.getMessageCase() + " " + msg.getCtrl().getId() + " " + msg.getCtrl().getCode());
            		}
            		//handle 
            	}
            	//value.getMessageCase()
                
            }

            @Override
            public void onError(Throwable t) {
            	logger.warning("OnError: " + t.getMessage());
                //System.out.println(t.getMessage());
                futures.rejectAndPurgeAll(503, t.getMessage());
                grpc_exit = 1;
                
                ChatAdminEvent event = new ChatAdminEvent();
                event.setEventType(ChatAdminEvent.EVENT_TYPE.NETWORK_FAILURE);
                event.setReason(t.getMessage());
                ChatAdminClient.getDefaultInstance().getEventQueue().push(event);
                
            }

            @Override
            public void onCompleted() {
            	logger.warning("onCompleted");
                //System.out.println("onCompleted");
            }
        });
    	
    	logger.info("messageLoop started " + host + " " + port);
    	sendHi();
    }
    
    @Override
    public void run() {
    	System.out.println("loop begins");
    	//futures.start();
    	messageLoop();
    	event_thread.start();
    	int count = 0;
    	while (true) {
	    	if(grpc_exit == 1) {
	    		//messageLoop()
	    		//eventQueue.stop();
	    		return;
	    	}
    		try {
    			futures.process();
    			Thread.sleep(500);
    			count++;
    			if (count == 10) {
    				ChatAdminClient admin = ChatAdminClient.getDefaultInstance();
    				admin.heatBeat();
    				count = 0;
    			}
    		}catch (InterruptedException e) {
 
            }
        }
    }
    
    public void stop() {
    	grpc_exit = 1;
    	eventQueue.stop();
    }
    
    public void heatBeat() {
    	
    	/*ClientHi.Builder hi_buider = ClientHi.newBuilder();
        hi_buider.setPlatform("Linux");
        hi_buider.setId(genMsgId());
        hi_buider.setLang("Chinses");
        //hi_buider.setDeviceId("3333");
        hi_buider.setVer("0.15.14");
        ClientHi hi = hi_buider.build();
        
        pbx.Model.ClientMsg.Builder chatMessage = ClientMsg.newBuilder().
        						setHi(hi);
        
        //logger.info("sending msg heartbeat " + hi_buider.getId());
        //cliObserver.onNext(chatMessage.build());
        sendMessage(chatMessage);*/
    	sendHi();
    }
    
    public void sendHi() {
    	
    	 //ClientDel.Builder del_builder = ClientDel.newBuilder();
    	 //del_builder.setWhatValue(1);//user
    	 //del_builder.setUserId(value);
    	 
    	 ClientHi.Builder hi_buider = ClientHi.newBuilder();
         hi_buider.setPlatform("Linux");
         hi_buider.setId(genMsgId());
         hi_buider.setLang("Chinses");
         //hi_buider.setDeviceId("3333");
         hi_buider.setVer("0.15.14");
         ClientHi hi = hi_buider.build();
         
         pbx.Model.ClientMsg.Builder chatMessage = ClientMsg.newBuilder().
         						setHi(hi);
         
         logger.info("sending msg hi " + hi_buider.getId());
         //put it into futures
         //cliObserver.onNext(chatMessage.build());
         sendMessage(chatMessage);
         ChatHiMsgResponseHandler hi_handler = new ChatHiMsgResponseHandler();
         ChatPromisedReply reply = new ChatPromisedReply(hi_handler);
         reply.setMsgId(hi_buider.getId());
         futures.push(hi_buider.getId(), reply);
         
    }
    
    public void addAnonymousUserToGroup(String userid, String groupid) {
    	
    	logger.info("addAnonymousUser " + userid + " grouid "+groupid);
    	//usermap.put(groupid, value)
    	List<String> userList = usermap.get(groupid);
    	if (userList != null) {
    		userList.add(userid);
    	}else {
    		List<String> new_userList = new ArrayList<String>();
        	usermap.put(groupid,new_userList);
        	new_userList.add(userid);
    	}
    	System.out.print(usermap.size());
    }
    
    public void delAnonymousUserFromGroup(String groupid) {
    	logger.info("delAnonymousUserFromGroup grouid "+groupid);
    	List<String> userList = usermap.get(groupid);
    	if (userList != null) {
    		for(String usr:userList){
               // System.out.println(usr);
                delAnonymousUser(usr);
            }
    		
    		usermap.remove(groupid);
    	}
    	
    	//System.out.print(usermap.size());
    	
    }
    
    public void delAnonymousUser(String userid) {
    	ClientDel.Builder del_builder = ClientDel.newBuilder();
    	del_builder.setId(genMsgId());
    	del_builder.setWhat(What.forNumber(3));
    	del_builder.setUserId(userid);
    	del_builder.setHard(true);
    	ClientDel del = del_builder.build();
    	
    	pbx.Model.ClientMsg.Builder chatMessage = ClientMsg.newBuilder().
					setDel(del);
    	logger.info("sending msg id " + del_builder.getId() + " to del user "+userid);
        //put it into futures
        //cliObserver.onNext(chatMessage.build());
    	sendMessage(chatMessage);
        ChatAdminDelUsrMsgResponseHander del_handler = new ChatAdminDelUsrMsgResponseHander();
        ChatPromisedReply reply = new ChatPromisedReply(del_handler);
        reply.setMsgId(del_builder.getId());
        futures.push(del_builder.getId(), reply);
    	
    }
    
    public void login() {
    	ClientLogin.Builder login_builder = ClientLogin.newBuilder();
    	login_builder.setId(genMsgId());
    	login_builder.setScheme("basic");
    	String secret = this.user + ":" + this.password;
    	login_builder.setSecret(ByteString.copyFrom(secret.getBytes()));
    	ClientLogin login = login_builder.build();
    	pbx.Model.ClientMsg.Builder chatMessage = ClientMsg.newBuilder().
					setLogin(login);
    	
    	logger.info("sending msg login " + login_builder.getId());
    	//cliObserver.onNext(chatMessage.build());
    	sendMessage(chatMessage);
    	
        ChatLoginMsgResponseHandler login_handler = new ChatLoginMsgResponseHandler();
        ChatPromisedReply reply = new ChatPromisedReply(login_handler);
        futures.push(login_builder.getId(), reply);
						
    }
    
    private static String string2Unicode(String string) {
		StringBuffer unicode = new StringBuffer();
		for (int i = 0; i < string.length(); i++) {
			// 取出每一个字符
			char c = string.charAt(i);
			// 转换为unicode
			unicode.append("\\u" + Integer.toHexString(c));
		}
 
		return unicode.toString();
	}
    
    public String createChatGroup(String topic_name, String pri_comments) {
    	
    	ClientSub.Builder sub_buidler = ClientSub.newBuilder();
    	sub_buidler.setId(genMsgId());
    	sub_buidler.setTopic("new");
    	//toHex(topic_name.toCharArray());
    	//String fn = "testsss";
    	//String uTopic_name = ChatAdminClient.string2Unicode(topic_name);
    	JSONObject obj = new JSONObject();
    	obj.put("fn",topic_name);
    	String pub = obj.toString();
    	sub_buidler.getSetQueryBuilder().getDescBuilder().setPublic(ByteString.copyFrom(pub.getBytes()));
    	JSONObject pri_obj = new JSONObject();
    	pri_obj.put("comment",pri_comments);
    	String prv=pri_obj.toString();
    	sub_buidler.getSetQueryBuilder().getDescBuilder().setPrivate(ByteString.copyFrom(prv.getBytes()));
    	sub_buidler.getSetQueryBuilder().getDescBuilder().getDefaultAcsBuilder().setAnon("JRWS");
    	//sub_buidler.set
    	
    	//FieldDescriptor filed = sub_builder.
    	//Message msg = new Message();
    	//MessageOrBuilder
    	//sub_buidler.getFieldBuilder(field)
    	//sub_buidler.setField(field, value)
    	
    	//sub_buidler.setTopicBytes()
    	//List userList = new List<String>();
    	
    	ClientSub sub = sub_buidler.build();
    	pbx.Model.ClientMsg.Builder chatMessage = ClientMsg.newBuilder().
				setSub(sub);
    	logger.info("createChatGroup " + sub_buidler.getId());
    	//cliObserver.onNext(chatMessage.build());
    	sendMessage(chatMessage);
	
    	ChatAdminCreateGroupMsgResponseHandler sub_handler = new ChatAdminCreateGroupMsgResponseHandler();
    	
        ChatPromisedReply reply = new ChatPromisedReply(sub_handler);
        reply.setMsgId(sub_buidler.getId());
        futures.push(sub_buidler.getId(), reply);
        logger.info("createChatGroup futrues id " + sub_buidler.getId());
        
    	return sub_buidler.getId();
    }
    
    public String attachChatGroup(String groupId) {
    	
    	ClientSub.Builder sub_buidler = ClientSub.newBuilder();
    	sub_buidler.setId(genMsgId());
    	sub_buidler.setTopic(groupId);
    	//FieldDescriptor filed = sub_builder.
    	//Message msg = new Message();
    	//MessageOrBuilder
    	//sub_buidler.getFieldBuilder(field)
    	//sub_buidler.setField(field, value)
    	
    	//sub_buidler.setTopicBytes()
    	ClientSub sub = sub_buidler.build();
    	pbx.Model.ClientMsg.Builder chatMessage = ClientMsg.newBuilder().
				setSub(sub);
    	
    	logger.info("sending msg attachChatGroup " + sub_buidler.getId() + " " + groupId);
    	
    	//cliObserver.onNext(chatMessage.build());
    	sendMessage(chatMessage);
    	
    	ChatAdminCreateGroupMsgResponseHandler sub_handler = new ChatAdminCreateGroupMsgResponseHandler();
        ChatPromisedReply reply = new ChatPromisedReply(sub_handler);
        reply.setMsgId(sub_buidler.getId());
        futures.push(sub_buidler.getId(), reply);
        
        return sub_buidler.getId();
    }
    
    public String delChatGroup(String groupId) {
    	
    	delAnonymousUserFromGroup(groupId);
    	ClientDel.Builder del_builder = ClientDel.newBuilder();
    	del_builder.setId(genMsgId());
    	del_builder.setTopic(groupId);
    	del_builder.setWhat(What.forNumber(1));
    	del_builder.setHard(true);
    	
    	ClientDel del = del_builder.build();
    	pbx.Model.ClientMsg.Builder chatMessage = ClientMsg.newBuilder().
				setDel(del);
    	
    	logger.info("sending msg delChatGroup " + del_builder.getId() + " " + groupId);
    	
    	//cliObserver.onNext(chatMessage.build());
    	sendMessage(chatMessage);
    	
    	ChatAdminDelGroupMsgResponseHandler sub_handler = new ChatAdminDelGroupMsgResponseHandler();
        ChatPromisedReply reply = new ChatPromisedReply(sub_handler);
        reply.setMsgId(del_builder.getId());
        futures.push(del_builder.getId(), reply);
        
    	return del_builder.getId();
    }
    
    public void sendMessage(pbx.Model.ClientMsg.Builder chatMessage) {
    	synchronized (msgLock) {
    		cliObserver.onNext(chatMessage.build()); 
    		
    	}
    }
    
    public void setHost(String host) {
    	this.host = host;
    }
    
    public void setPort(int port) {
    	this.port = port;
    }
    
    public void setUser(String user) {
    	this.user = user;
    }
    
    public void setPassword(String password) {
    	this.password = password;
    }
    
    public void setLogPath(String path) throws SecurityException, IOException {
    	logFilePath = path;
    	logFilePath = logFilePath + "/adminchat.%g.log";
    	if (fh != null) {
    		logger.removeHandler(fh);
    		fh.close();
    	}
	    fh = new FileHandler(logFilePath,5242880,5,true);
	    logger.addHandler(fh);
	    SimpleFormatter formatter = new SimpleFormatter();  
	    fh.setFormatter(formatter);  
    }
    
    public ChatEventNotifier getEventNotifier() {
    	return eventNotifier;
    }
    
    public void setEventNotifier(ChatEventNotifier eventNoitifier) {
    	
    	this.eventNotifier = eventNoitifier;
    }
    
    public ChatAdminEventQueue getEventQueue() {
    	return eventQueue;
    }
    
    public void setAuthenticated(boolean isSucess) {
    	authenticated = isSucess;
    }
    
    public boolean isAuthenticated() {
    	return authenticated;
    }
   
    public static void main(String[] args) throws InterruptedException {
    	
    	ChatAdminClient admin = ChatAdminClient.getDefaultInstance();
    	admin.setHost("127.0.0.1");
    	admin.setPort(6061);
    	admin.setUser("xena");
    	admin.setPassword("xena123");
    	
    	try {
    		admin.setLogPath("/Users/zhuyiye/Downloads");
    	}
		 catch (SecurityException e) {  
	            e.printStackTrace();  
	     } catch (IOException e) {  
	            e.printStackTrace();  
	     } 
    	
    	Thread admin_thread = new Thread(admin);
    	admin_thread.start();
    	//admin.login();
    	
    	Thread.sleep(1000*20);
    	ChatAdminClient.getDefaultInstance().createChatGroup("beluga", "");
    	
    	/*for (int i = 0 ;  i < 20; i++) {
    		ChatTestThread test = new ChatTestThread();
    		Thread test_thread = new Thread(test);
    		test_thread.start();
		}*/
    	admin_thread.join();
        
    	System.out.println("app exited...");
    	
    	
    }
    
}