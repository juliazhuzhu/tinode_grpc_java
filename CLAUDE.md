# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

This is a **Tinode chat server admin client** library in Java that provides administrative capabilities for managing chat groups and users through a gRPC interface. It uses Protocol Buffers for message serialization and implements an event-driven architecture with asynchronous request/response handling.

## Build and Development Commands

### Build
```bash
mvn clean compile
```

### Package
```bash
mvn package
```

### Run
```bash
mvn exec:java -Dexec.mainClass="com.hexmeet.chat.admin.ChatAdminClient"
```

Or run the compiled JAR:
```bash
java -cp target/classes:$(mvn dependency:build-classpath -q -DincludeScope=runtime) com.hexmeet.chat.admin.ChatAdminClient
```

## Architecture

### Core Components

- **ChatAdminClient** (`src/main/java/com/hexmeet/chat/admin/ChatAdminClient.java`) - Main singleton client class that manages the gRPC connection, message sending/receiving, and coordinates all operations
- **ChatFutures** - Manages pending asynchronous requests with timeout handling (10 seconds)
- **ChatPromisedReply** - Promise pattern implementation for async response handling
- **ChatAdminEventQueue** - Event queue for notification processing
- **ChatEventNotifier** - Observer pattern interface for event callbacks
- **Response Handlers** - Strategy pattern with specific handlers for different message types (Hi, Login, CreateGroup, Del, etc.)

### Communication Flow

1. **Client → Server**: `ClientMsg` protobuf messages with different types (Hi, Login, Sub, Pub, Del, etc.)
2. **Server → Client**: `ServerMsg` protobuf responses (Ctrl, Data, Pres, Meta, Info)
3. **Async Processing**: Messages are tracked via `ChatFutures` using unique message IDs

### Threading Model

- **ExecutorService**: Fixed thread pool (4 threads) for concurrent operations
- **Main loop**: Processes futures and handles reconnection
- **Event queue**: Separate thread for event notification
- **Heartbeat**: Sent every 5 seconds (10 loop iterations × 500ms sleep)

### Message Types

Client message types (set via `ClientMsg.Builder`):
- `Hi` - Connection handshake/heartbeat
- `Login` - Authentication (basic auth scheme supported)
- `Sub` - Subscribe/create topics (chat groups)
- `Del` - Delete operations (users, groups, messages)
- `Pub` - Publish messages

Server response types:
- `Ctrl` - Control messages with status codes
- `Data` - Topic data
- `Pres` - Presence notifications
- `Meta` - Metadata responses
- `Info` - Informational messages

### Protocol Buffer Definitions

The protobuf schema is in `src/main/proto/model.proto` and defines:
- **Node service** - Main bidirectional streaming endpoint (`messageLoop`)
- **Plugin service** - Server plugin endpoints
- Message types for all client/server communications

## Key Design Patterns

- **Singleton**: `ChatAdminClient.getDefaultInstance()`
- **Observer**: Event notifier for async callbacks
- **Promise**: `ChatPromisedReply` for async operation tracking
- **Strategy**: Different `ResponseHandler` classes per message type
- **Builder**: Protobuf builders for message construction

## Configuration

Default connection settings:
- Host: `127.0.0.1`
- Port: `6061`

Configurable via setters:
- `setHost(String)`
- `setPort(int)`
- `setUser(String)`
- `setPassword(String)`
- `setLogPath(String)`

## Dependencies

- **gRPC 1.0.0** - RPC framework
- **Protobuf 3.5.1** - Protocol Buffers
- **FastJSON 1.2.47** - JSON serialization
- **Java 8** - Target runtime

## Administrative Operations

### Group Management
- `createChatGroup(topic_name, external_guid)` - Creates new chat group
- `attachChatGroup(groupId)` - Joins existing group
- `delChatGroup(groupId)` - Deletes group (removes all users first)

### User Management
- `addAnonymousUserToGroup(userid, groupid)` - Tracks user in group
- `delAnonymousUser(userid)` - Deletes user from server
- `delAnonymousUserFromGroup(groupid)` - Deletes all tracked users in a group

### Authentication
- `login()` - Authenticate with basic auth (user:password)
- `sendHi()` - Initial handshake, also used as heartbeat

## Error Handling

- Network failures trigger `NETWORK_FAILURE` events via `ChatAdminEventQueue`
- Failed requests reject their `ChatPromisedReply` with error code and text
- Automatic reconnection is attempted on network failure
- Server responses with HTTP status codes >= 400 are treated as errors

## Event Handling

Implement `ChatEventNotifier` interface to receive events:
- Connection events
- Authentication status
- Network failures
- Response handling callbacks

Register via `setEventNotifier(ChatEventNotifier)` before calling `start()`.

## Lifecycle

1. Configure client (host, port, credentials, event notifier)
2. Call `start()` to begin connection and message loop
3. Use admin methods (createChatGroup, etc.)
4. Call `stop()` to gracefully shutdown
