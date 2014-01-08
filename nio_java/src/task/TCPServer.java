package task;

import io.MonitorWritable;
import io.GateWayWritable;
import io.Writable;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class TCPServer implements Runnable{

	
	public final int Port = 20000;
	ServerSocket server = null;
	ThreadPoolExecutor threadPool = null;
	
	public TCPServer(){
		threadPool = new ThreadPoolExecutor(2,4,3,TimeUnit.SECONDS,
						new ArrayBlockingQueue<Runnable>(3), 
						new ThreadPoolExecutor.DiscardOldestPolicy());
	}
	@Override
	public void run(){
		// TODO Auto-generated method stub
			
	
	}
	public void startServer()throws IOException{
		
		server= new ServerSocket(Port);
		System.out.println("Server Started");
		try{
			while(true){
				Socket socket = server.accept();
				try{
					threadPool.execute(new Task(socket));
				}catch(IOException e){
					e.printStackTrace();
				}
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
				 byte b ;
				 while((b = in.readByte()) != -1){
					 System.out.printf("%f", 0xFF&b);
				 }
				 
				 /*if(type == 'M'){//mobile
					 writable = new MonitorWritable();
					
				 }else if(type == 'G'){//gate
					 writable = new GateWayWritable(20);
				 }else{//wrong
					 return;
					 
				 }
				 writable.readFields(in);
				 writable.process(out);
				 writable.write(out);*/
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
	
}
