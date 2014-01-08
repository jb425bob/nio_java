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
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

public class UDPClient {

	public final int ServerPort = 8888;
	private ByteBuffer buffer = ByteBuffer.allocate(2048);
	private DatagramSocket socket = null;
	private DatagramPacket packet = null;
	public UDPClient(String dir){
		
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
				//HashMap<String,List<String>>hash = new HashMap<String, List<String>>();
				int count = 1;
				int id = 1;
				JSONArray jsonResult = new JSONArray();
				JSONObject jsonTemp;
				while(itr.hasNext()){
					JSONObject json = (JSONObject)itr.next();
					jsonTemp = new JSONObject();
					jsonTemp.put("category", "com" + json.get("com"));
					jsonTemp.put("id", id++);
					jsonResult.add(jsonTemp);
					JSONArray json_array = json.getJSONArray("device");
					queue.addLast(json_array);
					queueCount.addLast(count ++);
				}
				int size = queue.size();
				for(int m = 0; m < size; m ++){
					JSONArray ja = queue.removeFirst();
					
					int nCount = queueCount.remove();
					Iterator<Object> iterator = ja.iterator();
					while(iterator.hasNext()){
						JSONObject json = (JSONObject)iterator.next();
						jsonTemp = new JSONObject();
						jsonTemp.put("category", json.get("category"));
						jsonTemp.put("_parentId", nCount);
						jsonTemp.put("id", id ++);
						jsonResult.add(jsonTemp);
						JSONArray json_content = json.getJSONArray("content");
						if(json_content != null){
							queue.addLast(json_content);
							queueCount.add(count ++);
						//	System.out.println(json_content.toJSONString());
						}
						//queue.addLast(json.getJSONArray("content"));
						//System.out.println(json.getJSONArray("content").toJSONString());
						//queueCount.add(count ++);
						
					}
				}
				//System.out.println(queue.size());
				size = queue.size();
				for(int m = 0; m < queue.size(); m ++){
					JSONArray ja = queue.removeFirst();
				//	System.out.println(ja.toJSONString());
					int nCount = queueCount.remove();
					Iterator<Object> iterator = ja.iterator();
					while(iterator.hasNext()){
						JSONObject json = (JSONObject)iterator.next();
						jsonTemp = new JSONObject();
						jsonTemp.put("category", json.get("name"));
						jsonTemp.put("_parentId", nCount);
						jsonTemp.put("id", id ++);
						jsonTemp.put("type", json.get("type"));
						jsonResult.add(jsonTemp);
						//queue.addLast(json.getJSONArray("content"));
						//queueCount.add(count ++);
						
					}
				}
				System.out.println(jsonResult.toJSONString());
				/*while(itr.hasNext()){
					JSONObject json = (JSONObject)itr.next();
					System.out.println("com: " + json.get("com"));
					JSONArray json_array  = json.getJSONArray("device");
					int size = json_array.size();
					for(int j = 0; j < size; j ++){
						JSONObject json_1 = json_array.getJSONObject(j);
						System.out.println("  category: "+ json_1.get("category"));
						JSONArray json_array_2 = json_1.getJSONArray("content");
						Iterator <Object> iter = json_array_2.iterator();
						while(iter.hasNext()){
							JSONObject json_2 = (JSONObject)iter.next();
							System.out.println("    name:" + json_2.get("name"));
							System.out.println("    type:" + json_2.get("type"));
							System.out.println("    value" + json_2.get("value"));
						}
					}
				}
			}*/
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
		new UDPClient("F:\\mabo\\client");
	}
}
