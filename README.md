# tinode_grpc_java

1.Dependencies

The communicate between the admin client and the server is grpc, we leverage some third party jars.
Please add following dependencies to your pom.
<dependencies>
        <dependency>
            <groupId>io.grpc</groupId>
            <artifactId>grpc-netty</artifactId>
            <version>1.0.0</version>
        </dependency>
        <dependency>
            <groupId>io.grpc</groupId>
            <artifactId>grpc-protobuf</artifactId>
            <version>1.0.0</version>
        </dependency>
        <dependency>
            <groupId>io.grpc</groupId>
            <artifactId>grpc-stub</artifactId>
            <version>1.0.0</version>
        </dependency>
        <dependency>
	    <groupId>com.alibaba</groupId>
	    	<artifactId>fastjson</artifactId>
	    	<version>1.2.47</version>
		</dependency>
</dependencies>


2.APIs
ChatAdminClient is the object that interact with the server directly, you need to use the object to configure logs, login server and create/del chat groups. Also please be aware that the communication pattern in the project is asynchronized. The interface ChatEventNotifier is the place where you can implements your event handler and process events sent from server.

3.Events
ChatAdminEvent
    ChatAdminMsgEvent
        ChatAdminGroupMsgEvent


The above hierarchy shows its relationship. We have 8 type of events, the relation between the event type and the event class is also listd.
HI_FAILURE,LOGIN_SUCCESS,LOGIN_FAILURE -- ChatAdminMsgEvent
ADDGROUP_SUCESS,ADDGROUP_FAILURE,DELGROUP_SUCESS,DELGROUP_FAILURE -- ChatAdminGroupMsgEvent
NETWORK_FAILURE -- ChatAdminEvent


