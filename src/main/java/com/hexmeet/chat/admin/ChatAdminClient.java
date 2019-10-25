package com.hexmeet.chat.admin;

import pbx.NodeGrpc;
import pbx.Model.ClientHi;
import pbx.Model.ClientLogin;
import pbx.Model.ClientMsg;
import pbx.Model.ClientSub;
import pbx.Model.ServerMsg;
import pbx.Model.ServerMsg.MessageCase;

import com.google.protobuf.ByteString;
import com.google.protobuf.Descriptors.FieldDescriptor;
import com.google.protobuf.Message;
import com.google.protobuf.MessageOrBuilder;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.stub.StreamObserver;

class ChatAdminClient implements Runnable{
	
	private static final String HOST = "127.0.0.1";
    private static final int PORT = 6061;
    
    private ManagedChannel 		channel;
    private NodeGrpc.NodeStub 	stub;
    private StreamObserver<ClientMsg> 	cliObserver;
    private int 						msgId;
    private ChatFutures					futures;
    private static final ChatAdminClient DEFAULT_INSTANCE;
    private int			grpc_exit;
    static {
      DEFAULT_INSTANCE = new ChatAdminClient();
    }

    public static ChatAdminClient getDefaultInstance() {
      return DEFAULT_INSTANCE;
    }
    
    
    ChatAdminClient(){
    	

    }
    
    private void messageLoop() {
    	
    	//ChatAdminClient admin = ChatAdminClient.getDefaultInstance();
    	
    	msgId = 0;
    	grpc_exit = 0;
    	
    	futures = new ChatFutures();
    	channel = ManagedChannelBuilder.forAddress(HOST, PORT)
                .usePlaintext(true)
                .build();
    	stub = NodeGrpc.newStub(channel);
    	
    	cliObserver = stub.messageLoop(new StreamObserver<ServerMsg>() {

            // Handler for messages from the server

            @Override
            public void onNext(ServerMsg msg) {
                // Display the message
            	System.out.println(msg.getMessageCase());
            	if (MessageCase.CTRL == msg.getMessageCase()) {
            		//hadle hi
            		System.out.println(msg.getCtrl().getId());
            		System.out.println(msg.getCtrl().getCode());
            		ChatPromisedReply reply = futures.retrieveReply(msg.getCtrl().getId());
            		if (reply != null) {
	            		int code = msg.getCtrl().getCode();
	            		if (code >= 200 && code < 400) {
	            			reply.resolve(msg);
	            			
	            		}
	            		else {
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
                System.out.println(t.getMessage());
                futures.rejectAndPurgeAll(503, t.getMessage());
                grpc_exit = 1;
                
            }

            @Override
            public void onCompleted() {
            	
                System.out.println("onCompleted");
            }
        });
    	
    	sendHi();
    }
    @Override
    public void run() {
    	System.out.println("loop begins");
    	messageLoop();
    	while (true) {
	    	if(grpc_exit == 1) {
	    		//messageLoop();
	    		return;
	    	}
    		try {
    			Thread.sleep(500);
    		}catch (InterruptedException e) {
 
            }
        }
    }
    
    public void sendHi() {
    	 ClientHi.Builder hi_buider = ClientHi.newBuilder();
         hi_buider.setPlatform("mac");
         hi_buider.setId("1");
         hi_buider.setLang("Chinses");
         hi_buider.setDeviceId("3333");
         hi_buider.setVer("0.15.14");
         ClientHi hi = hi_buider.build();
         
         pbx.Model.ClientMsg.Builder chatMessage = ClientMsg.newBuilder().
         						setHi(hi);
         
         //put it into futures
         cliObserver.onNext(chatMessage.build());
         ChatHiMsgResponseHandler hi_handler = new ChatHiMsgResponseHandler();
         ChatPromisedReply reply = new ChatPromisedReply(hi_handler);
         futures.push(hi_buider.getId(), reply);
         
    }
    
    public void login() {
    	ClientLogin.Builder login_builder = ClientLogin.newBuilder();
    	login_builder.setId("2");
    	login_builder.setScheme("basic");
    	String secret = "xena:xena123";
    	login_builder.setSecret(ByteString.copyFrom(secret.getBytes()));
    	ClientLogin login = login_builder.build();
    	pbx.Model.ClientMsg.Builder chatMessage = ClientMsg.newBuilder().
					setLogin(login);
    	cliObserver.onNext(chatMessage.build());
    	
        ChatLoginMsgResponseHandler login_handler = new ChatLoginMsgResponseHandler();
        ChatPromisedReply reply = new ChatPromisedReply(login_handler);
        futures.push(login_builder.getId(), reply);
						
    }
    
    public String createChatGroup() {
    	
    	ClientSub.Builder sub_buidler = ClientSub.newBuilder();
    	sub_buidler.setId("3");
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
    	cliObserver.onNext(chatMessage.build());
	
    	ChatSubMsgResponseHandler sub_handler = new ChatSubMsgResponseHandler();
        ChatPromisedReply reply = new ChatPromisedReply(sub_handler);
        futures.push(sub_buidler.getId(), reply);
        
        
    	return "3";
    }
    
    public void deleteChatGroup(String groupId) {
    	
    }
    
    
    
    public void sendMessage(pbx.Model.ClientMsg.Builder chatMessage) {
    	cliObserver.onNext(chatMessage.build());
    }
    
    public static void main(String[] args) throws InterruptedException {
    	
    	 // Create a channel
        
        
        //ClientHi hi = new ClientHi();
       // Builder builder = new Buidler();
       // ClientHi hi = new ClientHi();
        
       // ClientMsg cliMsg = ClientMsg.newBuilder().setHi(hi.toBuilder()).build();
        
    	
        
     // Create a blocking stub with the channel
        
        
      
        //ClientHi.Builder builder = new ClientHi.Builder();
       
        		
        
        // Send the message
        //cliObserver.onNext(chatMessage.build());

        
        //ClientMsg clientMsg = new ClientMsg();
        //cliMsg.newBuilder().setHi
        //ServerMsg serMsg;
        //stub.bindService();
        //ServerResponse helloResponse = stub.(msg);
    	ChatAdminClient admin = ChatAdminClient.getDefaultInstance();
    	//admin.sendHi();
    	Thread admin_thread = new Thread(admin);

    	admin_thread.start();
    	//admin.login();
    	admin_thread.join();
        
    	System.out.println("app exited...");
    }
    
}