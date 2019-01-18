package com.taurus.core.util;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;

/**
 * byte array	
 * @author daixiwei	daixiwei15@126.com
 */
public class ByteArray {
	private byte[]	buffer;
	private int		position;
	private boolean compressed;
	
	public ByteArray() {
		position = 0;
		buffer = new byte[0];
	}

	public ByteArray(byte[] buf) {
		position = 0;
		buffer = buf;
	}

	public byte[] reverseOrder(byte[] dt) {
		return dt;
	}

	public void writeByte(byte b) {
		byte[] buf = new byte[1];
		buf[0] = b;
		writeBytes(buf);
	}

	public void writeBytes(byte[] data) {
		writeBytes(data,0, data.length);
	}

	public void writeBytes(byte[] data, int ofs,int count) {
		byte[] dst = new byte[count + buffer.length];
        System.arraycopy(buffer, 0, dst, 0, buffer.length);
        System.arraycopy(data, ofs, dst, buffer.length, count);
		this.buffer = dst;
	}

	public void writeBool(boolean b) throws IOException {
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		DataOutputStream dos = new DataOutputStream(bos);
		dos.writeByte(b ? 1 : 0);
		writeBytes(bos.toByteArray());
	}

	public void writeInt(int i) throws IOException {
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		DataOutputStream dos = new DataOutputStream(bos);
		dos.writeInt(i);
		writeBytes(reverseOrder(bos.toByteArray()));
	}

	public void writeShort(short s) throws IOException {
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		DataOutputStream dos = new DataOutputStream(bos);
		dos.writeShort(s);
		writeBytes(reverseOrder(bos.toByteArray()));
	}

	public void writeUShort(int s) throws IOException {
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		DataOutputStream dos = new DataOutputStream(bos);
		byte[] tem = new byte[2];
		int b1 = (s & 0xFF00) >> 8;
		int b2 = s & 0xFF;
		tem[0] = (byte)b1;
		tem[1] = (byte)b2;
		dos.writeByte((byte) b1);
		dos.writeByte((byte) b2);
		writeBytes(bos.toByteArray());
	}

	public void writeLong(long l) throws IOException {
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		DataOutputStream dos = new DataOutputStream(bos);
		dos.writeLong(l);
		writeBytes(reverseOrder(bos.toByteArray()));
	}

	public void writeFloat(float f) throws IOException {
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		DataOutputStream dos = new DataOutputStream(bos);
		dos.writeFloat(f);
		writeBytes(reverseOrder(bos.toByteArray()));
	}

	public void writeDouble(double d) throws IOException {
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		DataOutputStream dos = new DataOutputStream(bos);
		dos.writeDouble(d);
		writeBytes(reverseOrder(bos.toByteArray()));
	}

	public void writeUTF(String str) throws IOException {
		int utfLen = 0;
		for (int i = 0; i < str.length(); i++) {
			int c = str.charAt(i);
			if ((c >= 1) && (c <= 127)) {
				utfLen++;
			} else if (c > 2047) {
				utfLen += 3;
			} else {
				utfLen += 2;
			}
		}
		if (utfLen > 32768) {
			throw new IOException("String length cannot be greater then 32768 !");
		}
		try {
			writeUShort(utfLen);
			writeBytes(StringUtil.getBytes(str));
		} catch (UnsupportedEncodingException e) {
			throw new IOException("Error writing to data buffer");
		}
	}

	public byte readByte() throws IOException {
		return this.buffer[(this.position++)];
	}

	public byte[] readBytes(int count) {
		byte[] res = new byte[count];
		ByteBuffer buf = ByteBuffer.wrap(this.buffer);
		buf.position(this.position);
		buf.get(res);
		this.position += count;
		return res;
	}

	public boolean readBool() throws IOException {
		return this.buffer[(this.position++)] == 1;
	}

	public int readInt() throws IOException {
		byte[] data = reverseOrder(readBytes(4));
		ByteArrayInputStream bis = new ByteArrayInputStream(data);
		DataInputStream dis = new DataInputStream(bis);
		return dis.readInt();
	}

	public short readShort() throws IOException {
		byte[] data = reverseOrder(readBytes(2));
		ByteArrayInputStream bis = new ByteArrayInputStream(data);
		DataInputStream dis = new DataInputStream(bis);
		return dis.readShort();
	}

	public int readUShort() throws IOException {
		byte[] data = reverseOrder(readBytes(2));

		int ib1 = new Integer(data[0]).intValue();
		if (ib1 < 0) {
			ib1 = data[0] & 0x80;
			ib1 += (data[0] & 0x7F);
		}
		int ib2 = new Integer(data[1]).intValue();
		if (ib2 < 0) {
			ib2 = data[1] & 0x80;
			ib2 += (data[1] & 0x7F);
		}
		return ib1 * 256 + ib2;
	}

	public long readLong() throws IOException {
		byte[] data = reverseOrder(readBytes(8));
		ByteArrayInputStream bis = new ByteArrayInputStream(data);
		DataInputStream dis = new DataInputStream(bis);
		return dis.readLong();
	}

	public float readFloat() throws IOException {
		byte[] data = reverseOrder(readBytes(4));
		ByteArrayInputStream bis = new ByteArrayInputStream(data);
		DataInputStream dis = new DataInputStream(bis);
		return dis.readFloat();

	}

	public double readDouble() throws IOException {
		byte[] data = reverseOrder(readBytes(8));
		ByteArrayInputStream bis = new ByteArrayInputStream(data);
		DataInputStream dis = new DataInputStream(bis);
		return dis.readDouble();
	}

	public String readUTF() throws IOException {
		int size = readUShort();
		if (size == 0) {
			return null;
		}
		byte[] data = readBytes(size);
		return StringUtil.getString(data);
	}

	public byte[] getBytes() {
		return this.buffer;
	}

	public void setBuffer(byte[] buffer) {
		this.buffer = buffer;
	}

	public int getLength() {
		return this.buffer.length;
	}

	public int getPosition() {
		return this.position;
	}

	public void setPosition(int position) {
		this.position = position;
	}
	
	public boolean isCompressed() {
        return this.compressed;
    }
    
    public void setCompressed(final boolean compressed) {
        this.compressed = compressed;
    }
    
	public void compress() throws Exception {
        if (this.compressed) {
            throw new Exception("Buffer is already compressed");
        }
        try {
        	buffer = Utils.compress(this.buffer);
            this.position = 0;
            this.compressed = true;
        }catch (IOException e) {
            throw new Exception("Error compressing data");
        }
    }
    
    public void uncompress() throws Exception {
        try {
        	buffer = Utils.uncompress(this.buffer);
            this.position = 0;
            this.compressed = false;
        }catch (IOException e2) {
            throw new Exception("Error decompressing data");
        }
    }
    
	public int getBytesAvailable() {
		int val = this.buffer.length - this.position;
		if ((val > this.buffer.length) || (val < 0)) {
			val = 0;
		}
		return val;
	}
}
