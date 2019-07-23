import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.UUID;

import org.jdom.Document;
import org.jdom.Element;
import org.jdom.input.SAXBuilder;
import org.jdom.output.XMLOutputter;

import com.mysql.cj.util.Util;
import com.taurus.core.entity.ITArray;
import com.taurus.core.entity.TArray;
import com.taurus.core.plugin.PluginService;
import com.taurus.core.plugin.database.DataBase;
import com.taurus.core.plugin.redis.Redis;
import com.taurus.core.util.Utils;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.Transaction;

public class Test {
	 private static final int[] PIDS= {
			48,49,50,51,52,53,54,55,56,57,
//			65,66,67,68,69,70,71,72,73,74,75,76,77,78,79,80,81,82,83,84,85,86,87,88,89,90,
			97,98,99,100,101,102,103,104,105,106,107,108,109,110,111,112,113,114,115,116,117,118,119,120,121,122
	 };
	 private static final Random rnd = new Random(System.currentTimeMillis());
	 
	 private static final String rndId() {
		 char[] chars = new char[8];
		 for(int i=0;i<chars.length;++i) {
			 char c = (char)PIDS[rnd.nextInt(PIDS.length)];
			 chars[i] = c;
		 }
		 return new String(chars);
	 }
	 
	 public static List<File> searchFiles(File folder, final String keyword) {
	        List<File> result = new ArrayList<File>();
	        if (folder.isFile())
	            result.add(folder);

	        File[] subFolders = folder.listFiles(new FileFilter() {
	            @Override
	            public boolean accept(File file) {
	                if (file.isDirectory()) {
	                    return true;
	                }
	                if (file.getName().toLowerCase().contains(keyword)) {
	                    return true;
	                }
	                return false;
	            }
	        });

	        if (subFolders != null) {
	            for (File file : subFolders) {
	                if (file.isFile()) {
	                    // 如果是文件则将文件添加到结果列表中
	                    result.add(file);
	                } else {
	                    // 如果是文件夹，则递归调用本方法，然后把所有的文件加到结果列表中
	                    result.addAll(searchFiles(file, keyword));
	                }
	            }
	        }

	        return result;
	    }
	 
	private static void read(Data data) throws Exception {
		InputStream is = new FileInputStream(data.file);
		SAXBuilder builder = new SAXBuilder();
		Document document = builder.build(is);
		Element root = document.getRootElement(); 
		is.close();
		data.document = document;
		data.root = root;
		data.id = root.getAttributeValue("id");
	}
	
	private static final class Data{
		public File file;
		public Document document;
		public Element root;
		public String id;
		
		public boolean equals(Object obj) {
			if(obj instanceof Data) {
				return this == obj;
			}
			if(obj instanceof String) {
				return id.equals(obj);
			}
			return false;
		}
	}
	
	private static final String exceId(List<Data> datas) {
		String id = rndId();
		if(datas.contains(id)) {
			return exceId(datas);
		}
		return id;
	}
	
	private static void emoji_test() {
		try {
			PluginService.me().loadConfig();
			String sql = "insert into test(nick) values('12🦌🦌🦌12')";
			long time = System.currentTimeMillis();
			for(int i=0;i<1;++i) {
				DataBase.use().executeUpdate(sql);
			}
			System.out.println("use time:"+(System.currentTimeMillis() - time));
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	private static void find_test() {
		try {
			PluginService.me().loadConfig();
			String sql = "select * from test";
			long time = System.currentTimeMillis();
			for(int i=0;i<2000;++i) {
				DataBase.use().executeQueryByTArray(sql);
			}
			System.out.println("use time:"+(System.currentTimeMillis() - time));
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	
	public static void main(String[] args) {
		try {
			PluginService.me().loadConfig();
		} catch (Exception e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		
//		find_test();
//		for(int i =0;i<100;++i)
//		System.out.println(rndId());
		
		/*
		List<File> files = searchFiles(new File("F:\\work\\pro\\qyq_client\\client\\new_pro\\qyq_new_ui\\"), "package.xml");
		List<File> files1 = searchFiles(new File("F:\\work\\pro\\qyq_client\\client\\new_pro\\qyq_exinfo_ui\\"), "package.xml");
		files.addAll(files1);
		List<Data> datas = new ArrayList<>();
		try {
			for(File f : files) {
				Data d = new Data();
				d.file = f;
				read(d);
				datas.add(d);
			}
		}catch (Exception e) {
			e.printStackTrace();
		}
		List<String> listTemp = new ArrayList<String>();  
        for(int i=0;i<datas.size();i++){  
        	Data data = datas.get(i);
            if(!listTemp.contains(data.id)){  
                listTemp.add(data.id);  
                
            }  else {
            	System.out.println(data.file.getPath() + ":" +data.id);
            	
//            	String id = exceId(datas);
//            	data.id = id;
//            	data.root.setAttribute("id", id);
//            	XMLOutputter XMLOut = new XMLOutputter();
//        		try {
//					XMLOut.output(data.document, new FileOutputStream(data.file));
//				} catch (Exception e) {
//					// TODO Auto-generated catch block
//					e.printStackTrace();
//				}
            }
        }  */
	}
	
}
