package com.taurus.permanent.io;

import java.nio.ByteBuffer;

import com.taurus.core.entity.ITObject;
import com.taurus.core.entity.TObject;
import com.taurus.permanent.TaurusPermanent;
import com.taurus.permanent.data.PackDataType;
import com.taurus.permanent.data.Packet;

public class ProtocolHandler {
	private static final String		ACTION_ID				= "a";
	private static final String		PARAM_ID				= "p";
	private static final int		INT_SIZE				= 4;
	
	public void onPacketWrite(Packet packet) {
		ITObject params = TObject.newInstance();
		params.putByte(ACTION_ID, (byte) packet.getId());
		params.putTObject(PARAM_ID, (ITObject) packet.getData());

		switch(packet.getDataType()) {
		case BINARY:
			byte[] binData = params.toBinary();
			ByteBuffer packetBuffer = ByteBuffer.allocate(INT_SIZE + binData.length);
			packetBuffer.putInt(binData.length);
			packetBuffer.put(binData);
			packet.setData(packetBuffer.array());
			break;
		case TEXT:
			String json = params.toJson();
			packet.setData(json);
			break;
		}
	}
	
	public void onPacketRead(Packet packet) {
		ITObject requestObject = null;
		if(packet.getDataType() == PackDataType.BINARY) {
			try {
				ByteBuffer dataBuffer = (ByteBuffer)packet.getData();
				requestObject = TObject.newFromBinaryData(dataBuffer.array());
			} catch (Exception e) {
				throw new RuntimeException("Error deserializing request: " + e);
			}
		}else {
			String json = (String)packet.getData();
			requestObject = TObject.newFromJsonData(json);
		}
		

		if (requestObject != null) {
			if (requestObject.isNull(ACTION_ID)) {
				throw new IllegalStateException("Request rejected: No Action ID in request!");
			}
			if (requestObject.isNull(PARAM_ID)) {
				throw new IllegalStateException("Request rejected: Missing parameters field!");
			}

			packet.setId(requestObject.getByte(ACTION_ID));
			packet.setData(requestObject.getTObject(PARAM_ID));
		}

		TaurusPermanent.getInstance().getController().enqueueRequest(packet);
	}
}
