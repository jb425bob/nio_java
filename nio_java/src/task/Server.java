package task;

import io.GateWayWritable;
import io.MobileWritable;
import io.MonitorWritable;
import io.SocketChannelOutputStream;
import io.Writable;

import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;

public class Server {

	//private static byte []header = ;
	public static final ByteBuffer HEADER = ByteBuffer.wrap(new byte[]{(byte)0x9d,(byte)0x9d});
	private static final float MAX_CALL_QUEUE_TIME = 0.6f;
	private static final int MAX_QUEUE_SIZE_PER_HANDLER = 100;
	
	private String bindAddress;
	private int port;
	private int handlerCount;
	private int maxIdleTime;
	
	private int thresholdIdleConnections;
	
	int maxConnectionsToNuke;
	private int timeout;
	private long maxCallStartAge;
	private int maxQueueSize;
	volatile private boolean running = true;
	private LinkedList<Call> callQueue = new LinkedList<Call>();
	
	private List<Connection> connectionList =Collections.synchronizedList(new LinkedList<Connection>());
	
	private Listener listener = null;
	private int numConnections = 0;
	private Handler[] handlers = null;
	
	private static class Call{
		private long receivedTime;
		private Writable writable;
		private Connection connection;
		
		public Call(Connection c, Writable writable){
			this.receivedTime = System.currentTimeMillis();
			this.writable = writable;
			this.connection = c;
		}
		public void call(){
		
				try {
					writable.process(this.connection.out);
				} catch (IOException e) {
					// TODO Auto-generated catch block
					//e.printStackTrace();
					System.out.println("process exception " + e);
				}
				//DataOutputStream out = this.connection.out;
				//out.write(new byte[]{(byte)0x9d, (byte)0x9d, (byte)0x01});
				//SocketChannel socket = this.connection.channel;
			//	socket.write(ByteBuffer.wrap(new byte[]{(byte)0x9d, (byte)0x9d, (byte)0x01}));
		
		}
	}
	
	private class Listener extends Thread{
		
		private ServerSocketChannel acceptChannel = null;
		private Selector selector = null;
		private InetSocketAddress address;
		private Random rand = new Random();
		private long lastCleanupRunTime = 0;
		private long cleanupInterval = 10000;
		public Listener() throws IOException {
			address = new InetSocketAddress(bindAddress, port);
			acceptChannel = ServerSocketChannel.open();
			acceptChannel.configureBlocking(false);
			acceptChannel.socket().bind(address);
			selector = Selector.open();
			acceptChannel.register(selector, SelectionKey.OP_ACCEPT);
			this.setDaemon(true);
		}
		
		private void cleanupConnections(boolean force){
			long currentTime = System.currentTimeMillis();
			if(force || numConnections > thresholdIdleConnections){	
				if(!force && (currentTime - lastCleanupRunTime) < cleanupInterval){
					return;
				}
				int start = 0;
				int end = numConnections - 1;
				try{
					if(!force){
						start = rand.nextInt() % numConnections;
						end = rand.nextInt() % numConnections;
						int temp;
						if(end < start){
							temp = end;
							end = start;
							start = temp;
						}
					}
				}
					catch (ArithmeticException e){
						return ;
					}
				int i = start;
				int numNuked = 0;
				while(i <= end){
					Connection c;
					synchronized(connectionList){
						try{
							c = (Connection)connectionList.get(i);
						}catch(Exception e){return ;}
					}
					if(c.timeOut(currentTime)){
						synchronized(connectionList){
							if(connectionList.remove(c))
								numConnections --;
						}
						try{
							c.close();
						}catch(Exception e){}
						numNuked ++;
						end --;
						c = null;
						if(!force && numNuked == maxConnectionsToNuke) break;
						
					}else
						i++;
				}
				lastCleanupRunTime = System.currentTimeMillis();
	    	}
		}
		
		public void run(){
			System.out.println("listener is listerning ");
			while(running){
				SelectionKey key = null;
				try{
					selector.select();
					Iterator iter = selector.selectedKeys().iterator();
					//System.out.println("listenging select");
					while(iter.hasNext()){
						key = (SelectionKey)iter.next();
						//System.out.println(key.channel().toString());
						iter.remove();
						try{
							if(key.isValid()){
								if(key.isAcceptable()){
									doAccept(key);
								} else if(key.isReadable()){
									doRead(key);
								}
							}
						} catch (IOException e){
							key.cancel();
						}
						key = null;
					}
				} catch (OutOfMemoryError e){
					System.out.println("Out of Memory in server select " + e);
					closeCurrentConnection(key, e);
					cleanupConnections(true);
					try{Thread.sleep(60000);}catch (Exception ie){}
				} catch(Exception e){
					closeCurrentConnection(key, e);
				}
				cleanupConnections(false);
			}
			synchronized(this){
				try{
					acceptChannel.close();
					selector.close();
				} catch(IOException e){}
				selector = null;
				acceptChannel = null;
				connectionList = null;
			}

			
		}
		private void closeCurrentConnection(SelectionKey key, Throwable e){
			
			if(key != null){
				Connection c = (Connection)key.attachment();
				if(c != null){
					synchronized(connectionList){
						if(connectionList.remove(c))
							numConnections --;
					}
					try{
						c.close();
					} catch (Exception ex){}
					c =null;
				}
				
			}
		}
		void doAccept(SelectionKey key)  throws IOException, OutOfMemoryError{
			Connection c = null;
			ServerSocketChannel server = (ServerSocketChannel)key.channel();
			SocketChannel channel = server.accept();
			channel.configureBlocking(false);
			SelectionKey readKey = channel.register(selector, SelectionKey.OP_READ);
			c = new Connection(readKey, channel, System.currentTimeMillis());
			
			readKey.attach(c);
			synchronized(connectionList){
				connectionList.add(numConnections, c);
				numConnections ++;
				System.out.println("Connections : " + numConnections);
				
			}
		}
		void doRead(SelectionKey key){
			int count = 0;
			Connection c = (Connection)key.attachment();
			if(c == null)
				return ;
			c.setLastContact(System.currentTimeMillis());
			try{
				count = c.readAndProcess();
			} catch (Exception e){
			//	e.printStackTrace();
				key.cancel();
				System.out.println(getName() + ": readAndProcess threw exception " + e + ". Count of bytes read :" + count );
				
				count = -1;
			}
			if(count < 0){
				synchronized(connectionList){
					if(connectionList.remove(c))
						numConnections --;
				}
				try{
					c.close();
				} catch (Exception e) {}
				c = null;
			}
			else{
				c.setLastContact(System.currentTimeMillis());
			}
		}
		synchronized void doStop(){
			
			if(selector != null){
				selector.wakeup();
				Thread.yield();
			}
			if(acceptChannel != null){
				try{
					acceptChannel.socket().close();
				} catch (IOException e){}
			}
		}
	}
	
	private class Connection{
		
		private boolean firstData = true;
		private SocketChannel channel;
		private SelectionKey key;
		private ByteBuffer data;
		private ByteBuffer headerBuffer;
		private ByteBuffer dataLengthBuffer;
		private ByteBuffer typeBuffer;
		public  DataOutputStream out;
		private SocketChannelOutputStream channelOut;
		private long lastContact;
		private int dataLength;
		private Socket socket;
		private String hostAddress;
		private int remotePort;
		
		public Connection (SelectionKey key, SocketChannel channel, long lastContact){
			this.key = key;
			this.channel = channel;
			this.lastContact = lastContact;
			this.dataLengthBuffer = ByteBuffer.allocate(4);
			this.headerBuffer = ByteBuffer.allocate(2);
			this.typeBuffer = ByteBuffer.allocate(1);
			this.socket = channel.socket();
			/*this.out = new DataOutputStream(
						new BufferedOutputStream(channelOut = new SocketChannelOutputStream(channel)));*/
			this.out = new DataOutputStream(channelOut = new SocketChannelOutputStream(channel));
			InetAddress addr = socket.getInetAddress();
			
		}
		
		public void setLastContact(long lastContact){
			this.lastContact = lastContact;
		}
		public long getLastContact(){
			return lastContact;
			
		}
		private boolean timeOut(long currentTime){
			if(currentTime - lastContact > maxIdleTime)
				return true;
			return false;
		}
		public int readAndProcess() throws IOException, InterruptedException{
			int count = -1;
			//headerBuffer
			//typeBuffer
			//dataLengthbuffer;
			//data
			if(headerBuffer.remaining() > 0){
				count = channel.read(headerBuffer);
				if(count < 0 ){
					System.out.println("no data to read");
					return count;
				}
				if(headerBuffer.remaining() > 0)
					return count;
				headerBuffer.flip();
				if(!HEADER.equals(headerBuffer)){
					System.out.println("Message Header not match");
					//headerBuffer.flip();
					System.out.println("Message Headers 0x9d 0x9d");
					System.out.printf("%x\n", (byte)headerBuffer.get());
					System.out.printf("%x\n", (byte)headerBuffer.get());
					return -1;
				}
				count = channel.read(typeBuffer);
				if(count < 0)
					return count;
			}
			if(dataLengthBuffer.remaining() > 0){		
				count = channel.read(dataLengthBuffer);
				if(count < 0 || dataLengthBuffer.remaining() > 0)
					return count;
				dataLengthBuffer.flip();
				dataLength = dataLengthBuffer.getInt() - 7;//减去文件头长度
				System.out.println("length" + dataLength);
				data = ByteBuffer.allocate(dataLength);
			}
			count = channel.read(data);
			//System.out.println("data:" + data.toString());
			//System.out.println(data.remaining()==0);
			if(data.remaining() == 0){
				//System.out.println("before");
				data.flip();
				typeBuffer.flip();
				//typeBuffer.flip();
				processData();
				//System.out.println("after process");
				//headerBuffer.flip();
				
				dataLengthBuffer.flip();
				/*	headerBuffer.flip();
				System.out.println("headerBuffer: " + headerBuffer.toString());
				System.out.println("typeBuffer: " + typeBuffer.toString());
				System.out.println("dataLengthBuffer: " + dataLengthBuffer.toString());*/
				data = null;
			}
			return count;
		}
		private void processData()throws IOException, InterruptedException{
			DataInputStream dis =
					new DataInputStream(new ByteArrayInputStream(data.array()));
			
			//System.out.println("typeBuffer" + typeBuffer.toString());
			byte type = typeBuffer.get();
			System.out.printf("type %x \n", (byte)type);
			Writable writable = null;
			if(type == 0x01){//来自网关，请求版本信息
				writable = new GateWayWritable(dataLength);
			}else if(type == 0x02){//来自网关，发送版本信息
				writable = new MobileWritable(dataLength);
			}else if(type == 0x03){
				writable = new MonitorWritable();
			}else{
				System.out.println("type error");
			}
			writable.readFields(dis);
			//读取规定的字符串；
			//1：判断cmd
			//2：读取sn
			//3：读取length
			//4：读取json
			Call call = new Call(this, writable);
			synchronized(callQueue){
				if(callQueue.size() >= maxQueueSize){
					Call oldCall = (Call) callQueue.removeFirst();			
				}
				callQueue.addLast(call);
				callQueue.notify();
			}
		}
		private void close() throws IOException{
			System.out.println("closing connection: " + key.channel().toString());
			data = null;
			dataLengthBuffer = null;
			headerBuffer = null;
			if(!channel.isOpen())
				return ;
			try{socket.shutdownOutput();} catch(Exception e){}
			try{out.close();}catch(Exception e){}
			try{channelOut.destroy();}catch(Exception e){}
			if(channel.isOpen()){
				try{channel.close();}catch(Exception e){}
			}
			try{socket.close();}catch(Exception e){}
			try{key.cancel();}catch(Exception e){}
			key = null;
		}
	}
	private class Handler extends Thread{
		public Handler(int instanceNumber){
			this.setDaemon(true);
			this.setName("Server Handler " + instanceNumber + " on " + port);
		}
		
		public void run() {
			System.out.println(getName() + ": starting ");
			while(running){
				try{
					Call call;
					synchronized(callQueue){
						while(running && callQueue.size() == 0){
							callQueue.wait(timeout);
						}
						if(!running) break;
						call = (Call)callQueue.removeFirst();
					}
					if(System.currentTimeMillis() - call.receivedTime > maxCallStartAge)
						continue;
					//将内容写进数据库；
					System.out.println(getName() + " begins to process");
					call.call();
				} catch(InterruptedException e){
					if(running){
						System.out.println(getName() + "caught:" + e);
					}
				} catch(Exception e){
					//e.printStackTrace();
					System.out.println(getName() + "caught:" + e);
				}
			}
			System.out.println(getName() + " : exiting");
		}
		
	}
		
	protected Server(String bindAddress, int port , int handlerCount) throws IOException{
		this.bindAddress = bindAddress;
		this.port = port;
		this.handlerCount = handlerCount;
		this.timeout = 10000;
		maxCallStartAge = (long)(timeout * MAX_CALL_QUEUE_TIME);
		maxQueueSize = handlerCount * MAX_QUEUE_SIZE_PER_HANDLER;
		this.maxIdleTime = 120000;
		this.maxConnectionsToNuke = 10;
		this.thresholdIdleConnections = 4000;
		listener = new Listener();
		
	}
	public void setTimeout (int timeout){ this.timeout = timeout;}
	public synchronized void start() throws IOException{
		listener.start();
		handlers = new Handler[handlerCount];
		for(int i = 0; i < handlerCount; i++){
			handlers[i] = new Handler(i);
			handlers[i].start();
		}
	}
	public synchronized void stop(){
		running = false;
		if(handlers != null){
			for(int i = 0; i < handlerCount; i++){
				if(handlers[i] != null)
				handlers[i].interrupt();
			}
		}
		listener.interrupt();
		listener.doStop();
		notifyAll();
	}
	public synchronized void join() throws InterruptedException{
		while(running){
			wait();
		}
	}
	public static void main(String [] argv){
		Server tcpServer ;
		try {
			tcpServer = new Server("localhost", 20000, 5);
			tcpServer.start();
			tcpServer.join();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InterruptedException e){
			e.printStackTrace();
		}
	}
}
