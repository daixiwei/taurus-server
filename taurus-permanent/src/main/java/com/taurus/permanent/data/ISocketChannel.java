package com.taurus.permanent.data;

import java.io.IOException;
import java.net.SocketAddress;
import java.nio.ByteBuffer;

public interface ISocketChannel{
	long write(ByteBuffer buffer) throws IOException;
	    
    void write(final String p0) throws IOException;
	
    boolean checkConnection();
    
    Object getChannel();
    
    SocketAddress getRemoteAddress();
    
    SocketAddress getLocalAddress();
    
    void close() throws IOException;
}
