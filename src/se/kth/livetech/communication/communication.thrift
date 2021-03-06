#!/usr/local/bin/thrift --gen java:beans --gen java --gen py

/**
 * ICPC Live Communication thrift interface declarations.
 */

namespace java se.kth.livetech.communication.thrift

struct NodeId {
	1: string name, // our client name id
	2: string host, // machine's hostname
	3: string address, // reverse lookup name, use this to connect
	4: string ip, // ip address
	5: i32 port, // listen port
}

struct NodeStatus {
	1: string name,
	2: i64 lastContact,
	3: i64 lastPingIn,
	4: i64 lastPingReply,
	5: i32 ping,
	6: i64 clockSkew,
}

struct ContestId {
	1: string name,
	2: i64 starttime,
	/*
	3: optional bool replayFlag,
	4: optional string replayName,
	*/
}

struct ContestEvent {
	1: optional i32 id,
	2: i64 time,
	3: string type,
	4: map<string, string> attributes,
}

struct ContestDump {
	1: ContestId id,
	2: list<ContestEvent> setup,
	3: list<ContestEvent> events,
}

struct PropertyEvent {
	1: string key,
	2: optional string value,
	3: optional string link,
}

enum LogLevel {
	spam, debug, info, notice, warning, error, critical,
}

struct LogEvent {
	1: i64 time,
	2: LogLevel level,
	3: string message,
	4: optional string origin,
	5: optional string file,
	6: optional i32 line,
}

exception LiveException {
	1: string message,
}

service LiveService {
	// Node registration
	NodeId getNodeId(),
	void attach(1:NodeId node),
	void detach(),

	// Nodes
	void addNode(1:NodeId node),
	void removeNode(1:NodeId node),
	list<NodeId> getNodes(),
	list<NodeStatus> getNodeStatus(),

	// Server time
	i64 time(),

	// Code
	binary getClass(1:string className),
	oneway void classUpdate(1:string className),

	// Resources
	binary getResource(1:string resourceName),
	oneway void resourceUpdate(1:string resourceName),

	// Properties
	list<PropertyEvent> getProperties(),
	string getProperty(1:string key),
	void setProperty(1:string key, 2:string value),

	// Contest
	ContestDump getContest(1:ContestId contest),
	void addContest(1:ContestId contest),
	void removeContest(1:ContestId contest),

	// Updates
	oneway void propertyUpdate(1:PropertyEvent event),
	oneway void contestUpdate(2:ContestId contest, 1:ContestEvent event),

	// Logging
	oneway void logEvent(1:LogEvent event),
	oneway void logSubscribe(1:LogEvent template),
	oneway void logUnsubscribe(1:LogEvent template),
}


#void err() throws (1:Err err)
