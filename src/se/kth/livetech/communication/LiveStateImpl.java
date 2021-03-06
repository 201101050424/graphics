package se.kth.livetech.communication;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import se.kth.livetech.communication.thrift.ContestId;
import se.kth.livetech.properties.IProperty;
import se.kth.livetech.properties.PropertyHierarchy;
import se.kth.livetech.util.DebugTrace;

/** One class to hold all state for a Live node. */
public class LiveStateImpl implements LiveState {
	/** To start with, we have a server in the middle called the "Spider".
	 *  Main differences between the spider and other nodes are that the spider's
	 *  clock is authoritative, and it may have a different role in forwarding
	 *  class and resource requests. */
	private boolean spiderFlag;
	private boolean contestSourceFlag;

	private long clockSkew;
	private PropertyHierarchy hierarchy;
	private Map<ContestId, ContestState> contests;
	private Map<String, byte[]> classes;
	private Map<String, byte[]> resources;
	private Set<NodeUpdateListener> listeners;
	
	public LiveStateImpl(boolean spiderFlag) {
		this.spiderFlag = spiderFlag;
		clockSkew = 0;
		hierarchy = new PropertyHierarchy();
		contests = new TreeMap<ContestId, ContestState>();
		classes = new TreeMap<String, byte[]>();
		resources = new TreeMap<String, byte[]>();
		listeners = new HashSet<NodeUpdateListener>();
		
		ContestId id = new ContestId("contest", 0);
		contests.put(id, new ContestState());
	}
	
	@Override
	public void setContestSourceFlag(boolean contestSourceFlag) {
		this.contestSourceFlag = contestSourceFlag;
	}

	@Override
	public void addListeners(NodeUpdateListener connection) {
		IProperty root = this.hierarchy.getProperty("live"); // TODO: root property
		root.addPropertyListener(connection);
		DebugTrace.trace("addListeners %s -> %s", root, connection);
		if (this.spiderFlag || this.contestSourceFlag) {
			for (ContestId id : this.contests.keySet()) {
				DebugTrace.trace("contest sync %s -> %s", id, connection.getId());
				this.contests.get(id).addAttrsUpdateListener(connection.getAttrsUpdateListener(id));
			}
		}
		this.listeners.add(connection);
	}
	@Override
	public void removeListeners(NodeUpdateListener connection) {
		IProperty root = this.hierarchy.getProperty("live"); // TODO: root property
		root.removePropertyListener(connection);
		for (ContestId id : this.contests.keySet()) {
			DebugTrace.trace("contest sync remove %s -> %s", id, connection.getId());
			this.contests.get(id).removeAttrsUpdateListener(connection.getAttrsUpdateListener(id));
		}
		this.listeners.remove(connection);
	}

	@Override
	public boolean isSpider() {
		return spiderFlag;
	}

	public void setSpiderFlag(boolean spiderFlag) {
		this.spiderFlag = spiderFlag;
	}

	public long getClockSkew() {
		return clockSkew;
	}

	public void setClockSkew(long clockSkew) {
		this.clockSkew = clockSkew;
	}
	
	@Override
	public PropertyHierarchy getHierarchy() {
		return hierarchy;
	}
	
	@Override
	public Set<ContestId> getContests() {
		return contests.keySet();
	}

	@Override
	public ContestState getContest(ContestId id) {
		ContestState state = contests.get(id);
		if (state == null) {
			state = new ContestState();
			contests.put(id, state);
			for (NodeUpdateListener listener : this.listeners) {
				state.addAttrsUpdateListener(listener.getAttrsUpdateListener(id));
			}
		}
		return state;
	}

	public void setContest(ContestId id, ContestState dump) {
		this.contests.put(id, dump);
	}

	public Set<String> getClasses() {
		return classes.keySet();
	}

	public byte[] getClass(String name) {
		return classes.get(name);
	}

	public void setClass(String name, byte[] bytes) {
		this.classes.put(name, bytes);
	}

	public Set<String> getResources() {
		return resources.keySet();
	}

	public byte[] getResource(String name) {
		return resources.get(name);
	}

	public void setResource(String name, byte[] bytes) {
		this.resources.put(name, bytes);
	}
}
