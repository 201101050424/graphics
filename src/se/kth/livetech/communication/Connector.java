package se.kth.livetech.communication;

import java.net.InetAddress;
import java.net.UnknownHostException;

import org.apache.thrift.TException;
import org.apache.thrift.protocol.TBinaryProtocol;
import org.apache.thrift.protocol.TProtocol;
import org.apache.thrift.server.TServer;
import org.apache.thrift.server.TSimpleServer;
import org.apache.thrift.server.TThreadPoolServer;
import org.apache.thrift.transport.TServerSocket;
import org.apache.thrift.transport.TServerTransport;
import org.apache.thrift.transport.TSocket;
import org.apache.thrift.transport.TTransport;
import org.apache.thrift.transport.TTransportException;

import se.kth.livetech.communication.thrift.LiveService;
import se.kth.livetech.communication.thrift.NodeId;
import se.kth.livetech.util.DebugTrace;


/** Establish two-way connections between nodes. */
public class Connector {
	//public static int PORT = 9099;
	
	public static NodeId getLocalNode(String name, int port) {
		NodeId localNode = new NodeId();
		// TODO: localNode override
		localNode.name = name;
	    try {
			localNode.ip = InetAddress.getLocalHost().getHostAddress();
			localNode.address = InetAddress.getLocalHost().getCanonicalHostName();
			localNode.host = InetAddress.getLocalHost().getHostName();
			if (localNode.name == null) localNode.name = localNode.host;
		} catch (UnknownHostException e) {
		}
		localNode.port = port;
		return localNode;
	}
	
	public static LiveService.Client connect(NodeId localNode, String host, int port) throws TTransportException, TException {
		DebugTrace.trace("CONNECT");
		TTransport transport = new TSocket(host, port);
		TProtocol protocol = new TBinaryProtocol(transport);
		LiveService.Client client = new LiveService.Client(protocol);
		transport.open();
		client.attach(localNode);
		return client;
	}

	public static void listen(LiveService.Iface handler, int port, boolean multithreaded) throws TTransportException {
		final LiveService.Processor processor = new LiveService.Processor(handler);
		final TServerTransport serverTransport = new TServerSocket(port);
		final TServer server;
		if (!multithreaded)
			server = new TSimpleServer(processor, serverTransport);
		else
			server = new TThreadPoolServer(processor, serverTransport);
		if (!multithreaded)
			server.serve();
		else {
			new Thread(String.format("Listen on %d", port)) {
				@Override
				public void run() {
					server.serve();
				}
			}.start();
		}
	}

	// TODO: configurable backoff, handled by node registry
	final int MIN_BACKOFF = 100;
	final float F_BACKOFF = 2f;
	final int MAX_BACKOFF = 10000;
	public Thread listenThread(final LiveService.Iface handler, final int port, final boolean multithreaded) {
		return new Thread() {
			public void run() {
				int backoff = MIN_BACKOFF;
				while (true) {
					try {
						listen(handler, port, multithreaded);
						backoff = MIN_BACKOFF; // TODO: this never happens!
					} catch (TTransportException e) {
						e.printStackTrace();
						backoff = (int) (backoff * F_BACKOFF);
					}
					try {
						Thread.sleep(backoff);
					} catch (InterruptedException e) {
						e.printStackTrace();
						break;
					}
				}
			}
		};
	}
	// TODO: to close a non-responsive client:
	// client.getOutputProtocol().getTransport().close();

	public static void main(String[] args) {
		try {
			System.out.println("Starting the server...");
			SpiderHandler handler = new SpiderHandler();
			listen(handler, 9090, true);
			// TODO: Synchronize time
		} catch (Exception x) {
			x.printStackTrace();
		}
		System.out.println("done.");
	}
}
