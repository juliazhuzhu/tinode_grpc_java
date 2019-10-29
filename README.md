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
ChatAdminClient is the object that interact with the server directly, you need to use object to configure , login and create/del chat groups. Also please be aware that the communication pattern in the project is asynchronized. The interface ChatEventNotifier is a place where you can handle events sent from server.

 


