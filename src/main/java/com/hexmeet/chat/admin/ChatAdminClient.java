package com.hexmeet.chat.admin;

import pbx.NodeGrpc;
import pbx.Model.ClientDel;
import pbx.Model.ClientHi;
import pbx.Model.ClientLogin;
import pbx.Model.ClientMsg;
import pbx.Model.ClientSub;
import pbx.Model.ServerMsg;
import pbx.Model.ClientDel.What;
import pbx.Model.ServerMsg.MessageCase;

import com.google.protobuf.ByteString;
//import com.google.protobuf.Descriptors.FieldDescriptor;
//import com.google.protobuf.Message;
//import com.google.protobuf.MessageOrBuilder;
import java.util.logging.Level; 
import java.util.logging.Logger;
import java.io.IOException;
import java.util.logging.*; 


import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.stub.StreamObserver;

class ChatAdminClient implements Runnable{
	
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
    		FileHandler fh;  
    		if (logFilePath == null) {
    			logFilePath = "adminchat.log";
    		}
            fh = new FileHandler(logFilePath);  
            logger.addHandler(fh);
            SimpleFormatter formatter = new SimpleFormatter();  
            fh.setFormatter(formatter);  
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
            		ChatPromisedReply reply = futures.retrieveReply(msg.getCtrl().getId());
            		if (reply != null) {
	            		int code = msg.getCtrl().getCode();
	            		if (code >= 200 && code < 400) {
	            			reply.resolve(msg);
	            			
	            		}
	            		else {
	            			logger.info("onNext: " + msg.getCtrl().getText());
	            			String text = msg.getCtrl().getText();
	            			//ByteString content = msg.getCtrl().getParamsMap().get("what");
	            			//String reason = content.toString();
	            			reply.reject(msg.getCtrl().getId(),code,text,"");
	            			
	            		}
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
    	while (true) {
	    	if(grpc_exit == 1) {
	    		//messageLoop()
	    		return;
	    	}
    		try {
    			futures.process();
    			Thread.sleep(500);
    		}catch (InterruptedException e) {
 
            }
        }
    }
    
    public void stop() {
    	grpc_exit = 1;
    	eventQueue.stop();
    }
    
    public void sendHi() {
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
         cliObserver.onNext(chatMessage.build());
         ChatHiMsgResponseHandler hi_handler = new ChatHiMsgResponseHandler();
         ChatPromisedReply reply = new ChatPromisedReply(hi_handler);
         futures.push(hi_buider.getId(), reply);
         
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
    	cliObserver.onNext(chatMessage.build());
    	
    	
        ChatLoginMsgResponseHandler login_handler = new ChatLoginMsgResponseHandler();
        ChatPromisedReply reply = new ChatPromisedReply(login_handler);
        futures.push(login_builder.getId(), reply);
						
    }
    
    public String createChatGroup() {
    	
    	ClientSub.Builder sub_buidler = ClientSub.newBuilder();
    	sub_buidler.setId(genMsgId());
    	sub_buidler.setTopic("new");
    	//FieldDescriptor filed = sub_builder.
    	//Message msg = new Message();
    	//MessageOrBuilder
    	//sub_buidler.getFieldBuilder(field)
    	//sub_buidler.setField(field, value)
    	
    	//sub_buidler.setTopicBytes()
    	ClientSub sub = sub_buidler.build();
    	pbx.Model.ClientMsg.Builder chatMessage = ClientMsg.newBuilder().
				setSub(sub);
    	logger.info("sending msg createChatGroup " + sub_buidler.getId());
    	cliObserver.onNext(chatMessage.build());
	
    	ChatSubMsgResponseHandler sub_handler = new ChatSubMsgResponseHandler();
        ChatPromisedReply reply = new ChatPromisedReply(sub_handler);
        futures.push(sub_buidler.getId(), reply);
        
        
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
    	
    	cliObserver.onNext(chatMessage.build());
	
    	ChatSubMsgResponseHandler sub_handler = new ChatSubMsgResponseHandler();
        ChatPromisedReply reply = new ChatPromisedReply(sub_handler);
        futures.push(sub_buidler.getId(), reply);
        
        return sub_buidler.getId();
    }
    
    public String delChatGroup(String groupId) {
    	
    	ClientDel.Builder del_builder = ClientDel.newBuilder();
    	del_builder.setId(genMsgId());
    	del_builder.setTopic(groupId);
    	del_builder.setWhat(What.forNumber(1));
    	
    	ClientDel del = del_builder.build();
    	pbx.Model.ClientMsg.Builder chatMessage = ClientMsg.newBuilder().
				setDel(del);
    	
    	logger.info("sending msg delChatGroup " + del_builder.getId() + " " + groupId);
    	
    	cliObserver.onNext(chatMessage.build());
    	
    	return del_builder.getId();
    }
    
    public void sendMessage(pbx.Model.ClientMsg.Builder chatMessage) {
    	cliObserver.onNext(chatMessage.build());
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
    
    public void setLogPath(String path) {
    	logFilePath = path;
    }
    
    public ChatEventNotifier getEventNotifier() {
    	return eventNotifier;
    }
    
    public void setEventNotifier(ChatEventNotifier eventNoitifier) {
    	
    	this.eventNotifier = eventNoitifier;
    }
    
   
    public static void main(String[] args) throws InterruptedException {
    	
    	ChatAdminClient admin = ChatAdminClient.getDefaultInstance();
    	//admin.setHost("172.24.0.63");
    	//admin.setPort(6061);
    	admin.setUser("xena");
    	admin.setPassword("xena123");
    	admin.setLogPath("/Users/zhuyiye/Downloads/MyLogFile.log");
    	Thread admin_thread = new Thread(admin);
    	admin_thread.start();
    	//admin.login();
    	admin_thread.join();
        
    	System.out.println("app exited...");
    	
    	
    }
    
}