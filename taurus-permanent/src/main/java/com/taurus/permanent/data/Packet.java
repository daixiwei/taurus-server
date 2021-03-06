package com.taurus.permanent.data;

import java.util.ArrayList;
import java.util.List;

/**
 * Packet data object
 * @author daixiwei daixiwei15@126.com
 */
public class Packet {
	protected int							id;
	protected Object						data;
	protected Session						sender;
	protected List<Session>					recipients;
	protected byte[]						fragmentBuffer;
	protected PackDataType					dataType = PackDataType.BINARY;
	
	

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
	
	public List<Session> getRecipients() {
		return this.recipients;
	}
	
	public void setRecipients(List<Session> recipients) {
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
	
	public byte[] getFragmentBuffer() {
		return this.fragmentBuffer;
	}
	
	public void setFragmentBuffer(byte[] bb) {
		this.fragmentBuffer = bb;
	}
	
	public PackDataType getDataType() {
		return dataType;
	}

	public void setDataType(PackDataType dataType) {
		this.dataType = dataType;
	}
	
	public String toString() {
		return String.format("{ data: %s }",  data.getClass().getName());
	}
	
	public Packet clone() {
		Packet newPacket = new Packet();
		newPacket.id = this.id;
		newPacket.data = data;
		List<Session> recipients = new ArrayList<Session>();
		recipients.addAll(this.recipients);
		newPacket.recipients = recipients;
		newPacket.sender = this.sender;
		newPacket.dataType = this.dataType;
		newPacket.fragmentBuffer = this.fragmentBuffer;
		return newPacket;
	}
}
