package com.taurus.core.util;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.zip.Deflater;
import java.util.zip.DeflaterOutputStream;
import java.util.zip.Inflater;
import java.util.zip.InflaterInputStream;

import com.taurus.core.entity.ITArray;
import com.taurus.core.entity.ITObject;
import com.taurus.core.entity.TArray;
import com.taurus.core.entity.TDataType;
import com.taurus.core.entity.TDataWrapper;
import com.taurus.core.entity.TObject;

import java.util.Map.Entry;

/**
 * 工具类
 * @author daixiwei daixiwei15@126.com
 */
public final class Utils {
	private static final String	DELIMITER	= "__";
	private static final String	BUFFER_TYPE_DIRCT						= "DIRECT";
	private static final String	BUFFER_TYPE_HEAP						= "HEAP";
	public static final Random rand = new Random();
	static {
		rand.setSeed(System.currentTimeMillis());
	}
	
	/**
	 * 重置byte数组大小
	 * @param source
	 * @param pos
	 * @param size
	 * @return
	 */
	public static byte[] resizeByteArray(byte[] source, int pos, int size) {
		byte[] tmpArray = new byte[size];
		System.arraycopy(source, pos, tmpArray, 0, size);
		return tmpArray;
	}

	public static ITObject bytesToJson(byte[] bytes) throws IOException {
		if (bytes == null)
			return null;
		String json = uncompressString(bytes);
		ITObject p = TObject.newFromJsonData(json);
		return p;
	}

	public static byte[] jsonToBytes(ITObject json) throws IOException {
		if (json != null) {
			return compress(StringUtil.getBytes(json.toJson()));
		}
		return null;
	}

	/**
	 * 将密码字符串转换为MD5
	 * @param session
	 * @param clearPass
	 * @return
	 */
	public static String getClientPassword(String hashId, String clearPass) {
		return MD5.getInstance().getHash(hashId + clearPass);
	}

	/**
	 * 获取字符串转换为MD5
	 * @param str
	 * @return
	 */
	public static String getMD5Hash(String str) {
		return MD5.getInstance().getHash(str);
	}

	/**
	 * 生成session hash key
	 * @param fullIpAddress
	 * @return
	 */
	public static String getUniqueSessionToken(String fullIpAddress) {
		String key = fullIpAddress + DELIMITER + String.valueOf(rand.nextInt()) + System.currentTimeMillis();

		return MD5.getInstance().getHash(key);
	}

	/**
	 * 获取十六进制文件名
	 * @param name
	 * @return
	 */
	public static String getHexFileName(String name) {
		StringBuilder sb = new StringBuilder();
		char[] c = name.toCharArray();
		for (int i = 0; i < c.length; i++) {
			sb.append(Integer.toHexString(c[i]));
		}

		return sb.toString();
	}

	/**
	 * 根据类型创建ByteBuffer
	 * @param size
	 * @param type
	 * @return
	 */
	public static ByteBuffer allocateBuffer(int size, String type) {
		ByteBuffer bb = null;
		if (type.equalsIgnoreCase(BUFFER_TYPE_DIRCT)) {
			bb = ByteBuffer.allocateDirect(size);
		} else if (type.equalsIgnoreCase(BUFFER_TYPE_HEAP)) {
			bb = ByteBuffer.allocate(size);
		}
		return bb;
	}
	
	
	
	/**
	 * MPObject copy
	 * @param from
	 * @param to
	 */
	public static final void objectCopy(ITObject from,ITObject to) {
		for (Iterator<Entry<String, TDataWrapper>> it = from.iterator(); it.hasNext();) {
			Entry<String, TDataWrapper> entry = it.next();
			String key = (String) entry.getKey();
			TDataWrapper value = (TDataWrapper) entry.getValue();
			to.put(key, value);
		}
	}
	
	/**
	 * MPObject to  {@code Map<String,String>}
	 * @param from
	 * @param map
	 */
	public static final void objectToMap(ITObject from,Map<String,String> map) {
		for (Iterator<Entry<String, TDataWrapper>> it = from.iterator(); it.hasNext();) {
			Entry<String, TDataWrapper> entry = it.next();
			String key = (String) entry.getKey();
			TDataWrapper value = (TDataWrapper) entry.getValue();
			if (value.getTypeId() == TDataType.TOBJECT || value.getTypeId() == TDataType.TARRAY) {
				map.put(key, ((ITObject) value.getObject()).toJson());
			}else {
				map.put(key, value.getObject().toString());
			}
		}
	}
	
	/**
	 * list to MPArray
	 * @param list
	 * @return
	 */
	public static final ITArray toMpArray(List<Integer> list) {
		ITArray result = new TArray();
		for (Integer card : list) {
			result.addInt(card);
		}
		return result;
	}
	
	/**
	 * 压缩直接数组 
	 * @param data
	 * @return
	 * @throws IOException
	 */
    public static byte[] compress(byte[] data) throws IOException{  
        byte[] output = new byte[0];  

        Deflater compresser = new Deflater(Deflater.BEST_COMPRESSION,true);  
        compresser.reset();  
        compresser.setInput(data);  
        compresser.finish();  
        ByteArrayOutputStream bos = new ByteArrayOutputStream(data.length);  
        try {  
            byte[] buf = new byte[1024];  
            while (!compresser.finished()) {  
                int i = compresser.deflate(buf);  
                bos.write(buf, 0, i);  
            }  
            output = bos.toByteArray();  
        } catch (Exception e) {  
            output = data;  
            e.printStackTrace();  
        } finally {  
            bos.close();  
        }  
        compresser.end();  
        return output;  
    }  

    /**
     * 压缩 字节数组到输出流
     * @param data
     * @param os
     * @throws IOException
     */
    public static void compress(byte[] data, OutputStream os) throws IOException {  
    	Deflater deflater = new Deflater(Deflater.BEST_COMPRESSION, true);
        DeflaterOutputStream dos = new DeflaterOutputStream(os,deflater);  
        dos.write(data, 0, data.length);  
        dos.finish();  
        dos.flush();  
    }  

    /**
     * 解压缩 字节数组
     * @param data
     * @return
     * @throws IOException
     */
    public static String uncompressString(byte[] data) throws IOException {  
        byte[] output = uncompress(data);
        String str= StringUtil.getString(output);
        return str;  
    }  
    
    /**
     * 解压缩 字节数组
     * @param data
     * @return
     * @throws IOException
     */
    public static byte[] uncompress(byte[] data) throws IOException {  
        byte[] output = new byte[0];  

        Inflater decompresser = new Inflater(true);  
        decompresser.reset();  
        decompresser.setInput(data);  

        ByteArrayOutputStream o = new ByteArrayOutputStream(data.length);  
        try {  
            byte[] buf = new byte[1024];  
            while (!decompresser.finished()) {  
                int i = decompresser.inflate(buf);  
                o.write(buf, 0, i);  
            }  
            output = o.toByteArray();  
        } catch (Exception e) {  
            output = data;
        } finally {  
            o.close();  
        }

        decompresser.end();  
        return output;  
    }  

    /**
     * 解压缩 输入流 到字节数组
     * @param is
     * @return
     * @throws IOException
     */
    public static byte[] uncompress(InputStream is) throws IOException {  
    	Inflater decompresser = new Inflater(true); 
        InflaterInputStream iis = new InflaterInputStream(is,decompresser);  
        ByteArrayOutputStream o = new ByteArrayOutputStream(1024);  
        int i = 1024;  
        byte[] buf = new byte[i];  

        while ((i = iis.read(buf, 0, i)) > 0) {  
            o.write(buf, 0, i);  
        }   
        return o.toByteArray();  
    }
}
