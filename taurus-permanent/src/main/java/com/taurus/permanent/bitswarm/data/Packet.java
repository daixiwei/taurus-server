package com.taurus.permanent.bitswarm.data;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import com.taurus.permanent.bitswarm.sessions.Session;

/**
 * Packet data object
 * @author daixiwei daixiwei15@126.com
 */
public class Packet {
	protected int							id;
	protected Object						data;
	protected Session						sender;
	protected int							originalSize	= -1;
	protected Collection<Session>			recipients;
	protected byte[]						fragmentBuffer;
	
	
	public int getId() {
		return id;
	}
	
	public void setId(int id) {
		this.id = id;
	}
	
	public Object getData() {
		return this.data;
	}
	
	public void setData(Object data) {
		this.data = data;
	}
	
	public Session getSender() {
		return this.sender;
	}
	
	public void setSender(Session sender) {
		this.sender = sender;
	}
	
	public Collection<Session> getRecipients() {
		return this.recipients;
	}
	
	public void setRecipients(Collection<Session> recipients) {
		this.recipients = recipients;
	}
	
	public void setRecipient(Session session) {
		List<Session> recipients = new ArrayList<Session>();
		recipients.add(session);
		setRecipients(recipients);
	}
	
	public boolean isFragmented() {
		return this.fragmentBuffer != null;
	}
	
	public int getOriginalSize() {
		return this.originalSize;
	}
	
	public void setOriginalSize(int originalSize) {
		if (this.originalSize == -1)
			this.originalSize = originalSize;
	}
	
	public byte[] getFragmentBuffer() {
		return this.fragmentBuffer;
	}
	
	public void setFragmentBuffer(byte[] bb) {
		this.fragmentBuffer = bb;
	}
	
	public String toString() {
		return String.format("{ data: %s }",  data.getClass().getName());
	}
	
	public Packet clone() {
		Packet newPacket = new Packet();
		newPacket.setData(getData());
		newPacket.setOriginalSize(getOriginalSize());
		newPacket.setRecipients(null);
		newPacket.setSender(getSender());
		return newPacket;
	}
}
