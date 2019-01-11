package com.taurus.permanent.io;

import com.taurus.core.util.Logger;
import com.taurus.permanent.data.Packet;
import com.taurus.permanent.data.Session;

/**
 * 读写网络字节流
 * @author daixiwei daixiwei15@126.com
 */
public class IOHandler {
	private final BinaryIoHandler	binHandler;
	private final Logger			logger;
	
	public IOHandler() {
		logger = Logger.getLogger(getClass());
		binHandler = new BinaryIoHandler(this);
		
	}
	
	/**
	 * 读取网络包字节流
	 * @param session
	 * @param data
	 */
	public void onDataRead(Session session, byte[] data) {
		if ((data == null) || (data.length < 1)) {
			throw new IllegalArgumentException("Unexpected null or empty byte array!");
		}

		PacketReadState readState = (PacketReadState) session.getSystemProperty(Session.PACKET_READ_STATE);
		if(readState==null){
			if (data[0] == 60) {
				return;
			}
			session.setSystemProperty(Session.PACKET_READ_STATE, PacketReadState.WAIT_NEW_PACKET);
		}
		
		binHandler.handleRead(session, data);
	}
	
	/**
	 * 数据写入网络字节流，发送给指定客户端
	 * @param packet
	 */
	public void onDataWrite(Packet packet) {
		if (packet.getRecipients().size() > 0) {
			try {
				this.binHandler.handleWrite(packet);
			} catch (Exception e) {
				logger.error(e);
			}
		}
	}
	
	/**
	 * 统计丢包数量
	 * @return
	 */
	public long getReadPackets() {
		return binHandler.getReadPackets();
	}
	
	/**
	 * 统计丢包数量
	 * @return
	 */
	public long getIncomingDroppedPackets() {
		return binHandler.getIncomingDroppedPackets();
	}
	

}
