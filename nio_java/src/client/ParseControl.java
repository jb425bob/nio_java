package client;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map.Entry;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

public class ParseControl {

	public final int ServerPort = 8888;
	private ByteBuffer buffer = ByteBuffer.allocate(2048);
	private DatagramSocket socket = null;
	private DatagramPacket packet = null;
	public ParseControl(String dir){
		
		File directory = new File(dir);
		File [] lists = null;
		buffer.put((byte)'G');
		try {
			lists = listFiles(directory);
			fillBuffer(lists);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		for(File f : lists){
			System.out.println(f.getName());
		}
		buffer.flip();
		System.out.println(buffer.limit());
		try {
			System.out.println(new String (buffer.array(), "utf-8"));
		} catch (UnsupportedEncodingException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		
		try {
			socket = new DatagramSocket();
			packet = new DatagramPacket(buffer.array(),
					buffer.limit(), InetAddress.getByName("localhost"), ServerPort);
		} catch (SocketException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch(UnknownHostException e){
			e.printStackTrace();
		}
		try {
			socket.send(packet);
			socket.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	//list files
	File [] listFiles(File dir)throws IOException{
		
		if(dir.isDirectory()){
			File[] listFiles = dir.listFiles();
			return listFiles;
		}
		return null;
	}
	void fillBuffer(File [] lists)throws IOException{
		FileInputStream fin  = null;
		byte [] data = new byte[2048];
		int i = 0;
		int fileLength = 0;
		int fileNameLength = 0;
		String fileName =null;
		
		for(File file :lists){
			fileName = file.getName();
			if(fileName.equals("control.json")){
				fin = new FileInputStream(file);
				int n = fin.read();
				while(n != -1){
					data[i++] = (byte)n;
					n = fin.read();
				}
				String jsonStr = new String(data, 0, i, "utf-8");
				
				
				JSONArray jsonArray = JSONObject.parseArray(jsonStr);
				Iterator <Object> itr =  jsonArray.iterator();
				LinkedList<JSONArray> queue = new LinkedList();
				LinkedList<Integer> queueCount = new LinkedList();
				HashMap<String,List<String>>hash = new HashMap<String, List<String>>();
				int count = 1;
				int id = 1;
				JSONArray jsonResult = new JSONArray();
				JSONObject jsonTemp;
				while(itr.hasNext()){
					JSONObject json = (JSONObject)itr.next();
					JSONArray json_array = json.getJSONArray("device");
					Iterator<Object> iterator = json_array.iterator();
					while(iterator.hasNext()){
						JSONObject json_category = (JSONObject)iterator.next();
						String category =(String) json_category.get("category");
						List<String> listContents =  null;
						if((listContents = hash.get(category)) == null){
							listContents = new ArrayList<String>();
							hash.put(category, listContents);
						}
						JSONArray json_content = json_category.getJSONArray("content");
						if(json_content != null){
							Iterator<Object> iterator1 = json_content.iterator();
							while(iterator1.hasNext()){
								JSONObject json_content_temp = (JSONObject)iterator1.next();
								listContents.add((String)json_content_temp.get("name"));
							}
						}
						
					}
				}
	
				Iterator<String> keyIterator = hash.keySet().iterator();
				int keyLength = hash.size() + 1;
			
				JSONArray json_Result = new JSONArray();
				while(keyIterator.hasNext()){
					String key = keyIterator.next();
					jsonTemp = new JSONObject();
					jsonTemp.put("category", key);
					jsonTemp.put("id", id);
					jsonTemp.put("state", "closed");
					json_Result.add(jsonTemp);
					List<String> list_values = hash.get(key);
					for(int k=0; k<list_values.size(); k++){
						jsonTemp = new JSONObject();
						jsonTemp.put("name", list_values.get(k));
						jsonTemp.put("_parentId", id);
						jsonTemp.put("id", keyLength++);
						jsonTemp.put("category", "");
						json_Result.add(jsonTemp);
					}
					id ++;
				}
				JSONObject js = new JSONObject();
				System.out.println(json_Result.toJSONString());
				js.put("total", keyLength);
				js.put("rows", json_Result);
				System.out.println(js.toJSONString());
				
			/*fileNameLength = fileName.getBytes().length;
			fileLength = (int)file.length();
			
			buffer.putInt(fileNameLength);
			buffer.put(fileName.getBytes());
			buffer.putInt(fileLength);
			fin = new FileInputStream(file);
			int n = fin.read();
			while(n != -1){
				data[i++] = (byte)n;
				n = fin.read();
			}
			if(buffer.remaining() < i){
				System.out.println("data overflow");
				break;
			}
			buffer.put(data, 0, i);
			i = 0;*/
		//}
			}
		}
	}
	public static void main(String[] args) {
		new ParseControl("F:\\mabo\\client");
	}
}
