package com.taurus.permanent.io;

import com.taurus.core.entity.ITObject;
import com.taurus.core.entity.TObject;
import com.taurus.permanent.TPServer;
import com.taurus.permanent.data.Packet;

/**
 * ProtocolHandler
 * @author daixiwei
 *
 */
public class ProtocolHandler {
	private static final String		ACTION_ID				= "a";
	private static final String		PARAM_ID				= "p";
	
	public void onPacketWrite(Packet packet) {
		ITObject params = TObject.newInstance();
		params.putByte(ACTION_ID, (byte) packet.getId());
		params.putTObject(PARAM_ID, (ITObject) packet.getData());
		packet.setData(params);
	}
	
	public void onPacketRead(Packet packet) {
		ITObject requestObject = (ITObject)packet.getData();
		if (requestObject.isNull(ACTION_ID)) {
			throw new IllegalStateException("Request rejected: No Action ID in request!");
		}
		if (requestObject.isNull(PARAM_ID)) {
			throw new IllegalStateException("Request rejected: Missing parameters field!");
		}

		packet.setId(requestObject.getByte(ACTION_ID));
		if(requestObject.containsKey(PARAM_ID)) {
			packet.setData(requestObject.getTObject(PARAM_ID));
		}else {
			packet.setData(null);
		}
		
		TPServer.me().getController().enqueueRequest(packet);
	}
}
