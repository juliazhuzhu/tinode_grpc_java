package com.hexmeet.chat.admin;

import pbx.NodeGrpc;
import pbx.Model.ClientHi;
import pbx.Model.ClientMsg;
import pbx.Model.ServerMsg;
import pbx.Model.ServerMsg.MessageCase;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.stub.StreamObserver;

class ChatAdminClient implements Runnable{
	
	private static final String HOST = "127.0.0.1";
    private static final int PORT = 6061;
    
    private ManagedChannel 		channel;
    private NodeGrpc.NodeStub 	stub;
    StreamObserver<ClientMsg> 	cliObserver;
    int 						msgId;
    
    ChatAdminClient(){
    	
    	msgId = 0;
    	channel = ManagedChannelBuilder.forAddress(HOST, PORT)
                .usePlaintext(true)
                .build();
    	stub = NodeGrpc.newStub(channel);
    	
    	cliObserver = stub.messageLoop(new StreamObserver<ServerMsg>() {

            // Handler for messages from the server

            @Override
            public void onNext(ServerMsg value) {
                // Display the message
            	System.out.println(value.getMessageCase());
            	if (MessageCase.CTRL == value.getMessageCase()) {
            		//hadle hi
            		//handle 
            	}
            	//value.getMessageCase()
                
            }

            @Override
            public void onError(Throwable t) {
                System.out.println(t.getMessage());
            }

            @Override
            public void onCompleted() {
            	
                System.out.println("onCompleted");
            }
        });
    	
    }
    
    @Override
    public void run() {
    	System.out.println("loop begins");
    	while(true) {
    		try {
    			Thread.sleep(5000);
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
    	ChatAdminClient admin = new ChatAdminClient();
    	admin.sendHi();
    	Thread admin_thread = new Thread(admin);

    	admin_thread.start();
    	admin_thread.join();
        
    	System.out.println("app exited...");
    }
    
}