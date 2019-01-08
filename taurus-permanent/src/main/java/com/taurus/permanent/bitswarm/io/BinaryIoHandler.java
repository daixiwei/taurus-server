package com.taurus.permanent.bitswarm.io;

import java.nio.ByteBuffer;

import com.taurus.core.entity.ITObject;
import com.taurus.core.entity.TObject;
import com.taurus.core.util.Logger;
import com.taurus.core.util.Utils;
import com.taurus.core.util.executor.TaurusExecutor;
import com.taurus.permanent.TaurusPermanent;
import com.taurus.permanent.bitswarm.core.BitSwarmEngine;
import com.taurus.permanent.bitswarm.data.Packet;
import com.taurus.permanent.bitswarm.sessions.Session;

/**
 * 协议包字节流解析
 * @author daixiwei daixiwei15@126.com
 */
public class BinaryIoHandler {
	private static final String		ACTION_ID				= "a";
	private static final String		PARAM_ID				= "p";
	private static final int		INT_SIZE				= 4;
	private final Logger			log;
	private final BitSwarmEngine	engine;
	private volatile long			packetsRead				= 0L;
	private volatile long			droppedIncomingPackets	= 0L;
	private final int				maxIncomingPacketSize;
	private TaurusExecutor 			systemTreadPool;
	
	public BinaryIoHandler(IOHandler parentHandler) {
		this.log = Logger.getLogger(getClass());
		this.engine = BitSwarmEngine.getInstance();
		this.maxIncomingPacketSize = engine.getConfig().maxIncomingRequestSize;
		this.systemTreadPool = TaurusPermanent.getInstance().getSystemExecutor();
	}

	public long getReadPackets() {
		return this.packetsRead;
	}

	public long getIncomingDroppedPackets() {
		return this.droppedIncomingPackets;
	}

	public void handleWrite(Packet packet) throws Exception {
		ITObject params = TObject.newInstance();
		params.putByte(ACTION_ID, (byte) packet.getId());
		params.putTObject(PARAM_ID, (ITObject) packet.getData());

		byte[] binData = params.toBinary();

		ByteBuffer packetBuffer = ByteBuffer.allocate(INT_SIZE + binData.length);
		packetBuffer.putInt(binData.length);
		packetBuffer.put(binData);
		packet.setData(packetBuffer.array());
		engine.getSocketWriter().enqueuePacket(packet);
	}

	public void handleRead(Session session, byte[] data) {

		PacketReadState readState = (PacketReadState) session.getSystemProperty(Session.PACKET_READ_STATE);
		try {
			while (data.length > 0) {
				if (readState == PacketReadState.WAIT_NEW_PACKET) {
					ProcessedPacket process = handleNewPacket(session, data);
					readState = process.getState();
					data = process.getData();
				}

				if (readState == PacketReadState.WAIT_DATA_SIZE) {
					ProcessedPacket process = handleDataSize(session, data);
					readState = process.getState();
					data = process.getData();
				}

				if (readState == PacketReadState.WAIT_DATA_SIZE_FRAGMENT) {
					ProcessedPacket process = handleDataSizeFragment(session, data);
					readState = process.getState();
					data = process.getData();
				}

				if (readState != PacketReadState.WAIT_DATA)
					continue;
				ProcessedPacket process = handlePacketData(session, data);
				readState = process.getState();
				data = process.getData();
			}

		} catch (Exception err) {
			this.log.error(err);
			readState = PacketReadState.WAIT_NEW_PACKET;
		}

		session.setSystemProperty(Session.PACKET_READ_STATE, readState);
	}

	private ProcessedPacket handleNewPacket(Session session, byte[] data) {
		session.setSystemProperty(Session.DATA_BUFFER, new PendingPacket());
		return new ProcessedPacket(PacketReadState.WAIT_DATA_SIZE, data);
	}

	private ProcessedPacket handleDataSize(Session session, byte[] data) {
		PacketReadState state = PacketReadState.WAIT_DATA;
		PendingPacket pending = (PendingPacket) session.getSystemProperty(Session.DATA_BUFFER);
		int dataSize = -1;
		int sizeBytes = INT_SIZE;

		if (data.length >= INT_SIZE) {
			dataSize = 0;
			for (int i = 0; i < INT_SIZE; i++) {
				int pow256 = (int) Math.pow(256.0D, 3 - i);
				int intByte = data[i] & 0xFF;
				dataSize += pow256 * intByte;
			}
		}

		if (dataSize != -1) {
			validateIncomingDataSize(session, dataSize);
			pending.setExpectedLen(dataSize);
			pending.setBuffer(ByteBuffer.allocate(dataSize));
			data = Utils.resizeByteArray(data, sizeBytes, data.length - sizeBytes);
		} else {
			state = PacketReadState.WAIT_DATA_SIZE_FRAGMENT;
			ByteBuffer sizeBuffer = ByteBuffer.allocate(INT_SIZE);
			sizeBuffer.put(data);
			pending.setBuffer(sizeBuffer);
			data = new byte[0];
		}
		return new ProcessedPacket(state, data);
	}

	private ProcessedPacket handleDataSizeFragment(Session session, byte[] data) {
		PacketReadState state = PacketReadState.WAIT_DATA_SIZE_FRAGMENT;
		PendingPacket pending = (PendingPacket) session.getSystemProperty(Session.DATA_BUFFER);
		ByteBuffer sizeBuffer = (ByteBuffer) pending.getBuffer();

		int remaining = INT_SIZE - sizeBuffer.position();

		if (data.length >= remaining) {
			sizeBuffer.put(data, 0, remaining);
			sizeBuffer.flip();
			int dataSize = sizeBuffer.getInt();

			validateIncomingDataSize(session, dataSize);
			pending.setExpectedLen(dataSize);
			pending.setBuffer(ByteBuffer.allocate(dataSize));
			state = PacketReadState.WAIT_DATA;

			if (data.length > remaining)
				data = Utils.resizeByteArray(data, remaining, data.length - remaining);
			else {
				data = new byte[0];
			}

		} else {
			sizeBuffer.put(data);
			data = new byte[0];
		}

		return new ProcessedPacket(state, data);
	}

	private ProcessedPacket handlePacketData(Session session, byte[] data) throws Exception {
		PacketReadState state = PacketReadState.WAIT_DATA;
		PendingPacket pending = (PendingPacket) session.getSystemProperty(Session.DATA_BUFFER);
		ByteBuffer dataBuffer = (ByteBuffer) pending.getBuffer();
		int readLen = dataBuffer.remaining();
		boolean isThereMore = data.length > readLen;

		if (data.length >= readLen) {
			dataBuffer.put(data, 0, readLen);

			if (pending.getExpectedLen() != dataBuffer.capacity()) {
				throw new Exception("Expected: " + pending.getExpectedLen() + ", Buffer size: " + dataBuffer.capacity());
			}

			this.packetsRead += 1L;
			dispatchRequest(session, dataBuffer.array());

			state = PacketReadState.WAIT_NEW_PACKET;

		} else {
			dataBuffer.put(data);
		}

		if (isThereMore)
			data = Utils.resizeByteArray(data, readLen, data.length - readLen);
		else {
			data = new byte[0];
		}
		return new ProcessedPacket(state, data);
	}

	private void onPacketRead(Session session,ByteBuffer dataBuffer) {
		Packet newPacket = new Packet();
		newPacket.setSender(session);
		newPacket.setOriginalSize(dataBuffer.capacity());
		ITObject requestObject = null;
		try {
			requestObject = TObject.newFromBinaryData(dataBuffer.array());
		} catch (Exception e) {
			throw new RuntimeException("Error deserializing request: " + e);
		}

		if (requestObject != null) {
			if (requestObject.isNull(ACTION_ID)) {
				throw new IllegalStateException("Request rejected: No Action ID in request!");
			}
			if (requestObject.isNull(PARAM_ID)) {
				throw new IllegalStateException("Request rejected: Missing parameters field!");
			}

			newPacket.setId(requestObject.getByte(ACTION_ID));
			newPacket.setData(requestObject.getTObject(PARAM_ID));
		}

		TaurusPermanent.getInstance().getController().enqueueRequest(newPacket);
	}
	
	private ProcessedPacket dispatchRequest(Session session, byte[] data) {
		PacketReadState state = PacketReadState.WAIT_DATA;
		PendingPacket pending = (PendingPacket) session.getSystemProperty(Session.DATA_BUFFER);
		ByteBuffer dataBuffer = (ByteBuffer) pending.getBuffer();

		int readLen = dataBuffer.remaining();

		boolean isThereMore = data.length > readLen;

		if (data.length >= readLen) {
			dataBuffer.put(data, 0, readLen);

			if (pending.getExpectedLen() != dataBuffer.capacity()) {
				throw new IllegalStateException("Expected: " + pending.getExpectedLen() + ", Buffer size: " + dataBuffer.capacity());
			}
			this.packetsRead += 1L;
			state = PacketReadState.WAIT_NEW_PACKET;
			this.systemTreadPool.execute(new Runnable() {
				@Override
				public void run() {
					onPacketRead(session,dataBuffer);
				}
			});
		} else {
			dataBuffer.put(data);
		}

		if (isThereMore)
			data = Utils.resizeByteArray(data, readLen, data.length - readLen);
		else {
			data = new byte[0];
		}
		return new ProcessedPacket(state, data);
	}

	/**
	 * 验证字节流数据大小
	 * @param session
	 * @param dataSize
	 */
	private void validateIncomingDataSize(Session session, int dataSize) {
		String who = session.toString();

		if (dataSize < 1) {
			this.droppedIncomingPackets += 1L;
			throw new IllegalArgumentException("Illegal request size: " + dataSize + " bytes, from: " + who);
		}

		if (dataSize > this.maxIncomingPacketSize) {
			TaurusPermanent.getInstance().getController().disconnect(session);
			this.droppedIncomingPackets += 1L;

			throw new IllegalArgumentException(String.format("Incoming request size too large: %s, Current limit: %s, From: %s", dataSize, this.maxIncomingPacketSize, who));
		}
	}

}
