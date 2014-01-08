package task;

import io.MonitorWritable;
import io.GateWayWritable;
import io.Writable;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class UDPServer implements Runnable {

	public final int Port = 8888;
	DatagramSocket server = null;
	ThreadPoolExecutor threadPool = null;
	
	public UDPServer(){
		threadPool = new ThreadPoolExecutor(2,4,3,TimeUnit.SECONDS,
						new ArrayBlockingQueue<Runnable>(3), 
						new ThreadPoolExecutor.DiscardOldestPolicy());
	}
	@Override
	public void run(){
		// TODO Auto-generated method stub
			try{
				startServer();
			}catch(IOException e){
				e.printStackTrace();
			}
	
	}
	public void startServer()throws IOException{
		
		server= new DatagramSocket(Port);
		DatagramPacket packet = new DatagramPacket(new byte[2096], 2096);
		System.out.println("UDP Server Started");
		try{
			while(true){
				server.receive(packet);
				
				/*try{
					threadPool.execute(new Task(socket));
				}catch(IOException e){
					e.printStackTrace();
				}*/
				System.out.println("///////////////////////////////////////");
				System.out.println("address: " + packet.getAddress());
				System.out.println("port: " + packet.getPort());
				System.out.println("length: " + packet.getLength());
				int bufferLength = packet.getLength();
				int offset = packet.getOffset();
				byte [] data  = new byte[bufferLength];
				byte [] tempData;
				tempData = packet.getData();
				System.out.println("offset:" + offset);
				int j = 0;
				while(j<bufferLength){
					data[j++] = tempData[offset++];
				}
			/*	try {
					System.out.println("file content:" + new String (data, "utf-8"));
				} catch (UnsupportedEncodingException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}*/
				ByteBuffer buffer= ByteBuffer.wrap(data);
				//System.out.println("type : " + (byte)buffer.get());
				System.out.printf("%c\n", (byte)buffer.get());
				int fileNameLength,fileLength;
				byte [] contents = new byte[2048];
				for(int k=0; k<3; k++){
					try {
						fileNameLength = buffer.getInt();
						buffer.get(contents, 0, fileNameLength);
						System.out.println("file name :" + new String (contents,0,fileNameLength, "utf-8"));
						fileLength = buffer.getInt();
						buffer.get(contents, 0, fileLength);
						System.out.println("file contents; " + new String(contents, 0, fileLength, "utf-8"));
					} catch (UnsupportedEncodingException e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
					}							
				}
				System.out.println("------------------------------------------");
				
			}
		}finally{
			server.close();
			threadPool.shutdown();
		}
	}
	
	

	public class Task implements Runnable{

		private Socket socket;
		private DataInputStream in;
		private DataOutputStream out;
		private Writable writable;
		
		public Task(Socket s)throws IOException{
			this.socket = s;
			in = new DataInputStream(socket.getInputStream());
			out = new DataOutputStream(socket.getOutputStream());
		}
		@Override
		public void run() {
			// TODO Auto-generated method stub
			try {
				 byte type = in.readByte();
				 if(type == 'M'){//������ն��豸
					 writable = new MonitorWritable();
					
				 }else if(type == 'G'){//���������豸
					 writable = new GateWayWritable(20);
				 }else{//��Ϣ��ʽ�����˳�
					 return;
					 
				 }
				 writable.readFields(in);
				 writable.process(out);
				 writable.write(out);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}finally{
				try {
					in.close();
					out.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			
		}
		
	}
	public static void main(String[] argv){
		
		new Thread(new UDPServer()).start();
	}
}
