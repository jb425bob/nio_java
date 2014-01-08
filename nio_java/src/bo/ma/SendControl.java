package bo.ma;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.nio.charset.Charset;
import java.util.Iterator;

public class SendControl {
	public static final ByteBuffer HEADER = ByteBuffer.wrap(new byte[]{(byte)0x9d,(byte)0x9d});
	public static void main(String[] args)  throws IOException{
		int cPort = 1234;
		int sPort = 20000;
		
			SocketChannel sc = SocketChannel.open();
			Selector sel = Selector.open();
		try {
			sc.configureBlocking(false);
			//sc.socket().bind(new InetSocketAddress(0));
			sc.connect(new InetSocketAddress("localhost", sPort));
			sc.register(sel, SelectionKey.OP_CONNECT | SelectionKey.OP_READ| SelectionKey.OP_WRITE);
			
			int i = 0;
			boolean written = false, done = false;
			Charset cs = Charset.forName("utf8");
			ByteBuffer buf = ByteBuffer.allocate(16);
			ByteBuffer headerBuffer = ByteBuffer.allocate(2);
			ByteBuffer typeBuffer = ByteBuffer.allocate(1);
			ByteBuffer dataLengthBuffer = ByteBuffer.allocate(4);
			ByteBuffer data = null;
			int count = 0;
			while(!done){
			//	System.out.println("xxx");
				sel.select();
			//	System.out.println("xxx");
				Iterator it = sel.selectedKeys().iterator();
				//System.out.println("===========");
				while(it.hasNext()){
					SelectionKey key = (SelectionKey) it.next();
					it.remove();
					sc = (SocketChannel)key.channel();
					//System.out.println(key.isReadable());
					if(key.isConnectable() && !sc.isConnected()){
						if(sc.isConnectionPending())   
					        sc.finishConnect();
	/*					InetAddress addr = InetAddress.getByName(null);
						System.out.println(addr);
						boolean success = sc.connect(new InetSocketAddress("localhost", sPort));
						if(!success) sc.finishConnect();*/
					}
					if(key.isReadable() && written){
						System.out.println("read");
						if(headerBuffer.remaining() > 0){
							count = sc.read(headerBuffer);
							System.out.println("count : " + count);
							if(count < 0 ){
								System.out.println("no data to read");
								break;
							}
							if(headerBuffer.remaining() > 0)
								break;
							headerBuffer.flip();
							if(!HEADER.equals(headerBuffer)){
								System.out.println("Message Header not match");
								//headerBuffer.flip();
								System.out.println("Message Headers 0x9d 0x9d");
								System.out.printf("%x\n", (byte)headerBuffer.get());
								System.out.printf("%x\n", (byte)headerBuffer.get());
								break;
							}
							count = sc.read(typeBuffer);
							if(count < 0)
								break;
						}
						if(dataLengthBuffer.remaining() > 0){		
							count = sc.read(dataLengthBuffer);
							if(count < 0 || dataLengthBuffer.remaining() > 0)
								break;
							dataLengthBuffer.flip();
							int dataLength = dataLengthBuffer.getInt() - 7;//减去文件头长度
							System.out.println("length" + dataLength);
							data = ByteBuffer.allocate(dataLength);
						}
						count = sc.read(data);
						//System.out.println("data:" + data.toString());
						//System.out.println(data.remaining()==0);
						if(data.remaining() == 0){
							//System.out.println("before");
							data.flip();
							typeBuffer.flip();
							//typeBuffer.flip();
							try {

								System.out.printf("0x%x\n",(byte)data.get());
								byte [] contents = data.array();
								System.out.println("content :" + new String (contents, 1, contents.length-1, "utf-8"));

							} catch (UnsupportedEncodingException e1) {
								// TODO Auto-generated catch block
								e1.printStackTrace();
							}			
							
							dataLengthBuffer.flip();
							data = null;
							done = true;
						}
						
						System.out.println("read once");
					/*	if(sc.read((ByteBuffer)buf.clear()) > 0){
							written = false;
							String response = cs.decode((ByteBuffer)buf.flip()).toString();
							System.out.println(response);
							if(response.indexOf("END") != -1) done = true;
						}*/
					}
					if(key.isWritable() && !written){
						String str = "1234567891234567891234567891";
						FileInputStream fin  = null;
						byte [] datas = new byte[2048];
						int fileLength = 0;
						int fileNameLength = 0;
						i = 0;
						String fileName =null;

								fin = new FileInputStream(new File("F:\\mabo\\client\\control.json"));
								int n = fin.read();
								while(n != -1){
									datas[i++] = (byte)n;
									n = fin.read();
								}
					    String jsonStr = new String(datas, 0, i, "utf-8");
						System.out.println(jsonStr);
					    ByteBuffer contentBuffer = ByteBuffer.allocate(36 + i);
						contentBuffer.put(new byte[]{(byte)0x9d,(byte)0x9d});
						contentBuffer.put((byte)0x01);
						contentBuffer.putInt(36 + i);
						contentBuffer.put((byte)0x03);
						contentBuffer.put(str.getBytes(), 0, str.length());
						contentBuffer.put(datas, 0, i);
						System.out.println(contentBuffer.toString());;
					//	System.out.println(contentBuffer.array());
						//System.out.println(contentLength);
					/*	header.flip();
						type.flip();
						length.flip();
						content.flip();
						sc.write(header);
						sc.write(type);
						sc.write(length);
						sc.write(content);*/
						contentBuffer.flip();
						sc.write(contentBuffer);
						try {
							Thread.sleep(5000);
						} catch (InterruptedException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
						done = true;
						written = true;
						//sc.configureBlocking(false);
						//sc.register(sel, SelectionKey.OP_READ);
						//System.out.println("read");
						/*
						if(i < 10) sc.write(ByteBuffer.wrap(new String("hwody " + i + "\n").getBytes()));
						else if(i == 10) sc.write(ByteBuffer.wrap(new String ("END\n").getBytes()));
						written  = true;
						i ++;*/
					}
					
					
				}
			}
			/*sel.select();
			
			Iterator it = sel.selectedKeys().iterator();
			while(it.hasNext()){
				SelectionKey key = (SelectionKey) it.next();
				it.remove();
				if(key.isConnectable()){
					InetAddress ad = InetAddress.getLocalHost();
					System.out.println("connect will not block");
					if(!sc.connect(new InetSocketAddress(ad,sPort)))
						sc.finishConnect();
				}
				if(key.isReadable()){
					System.out.println("read will not block");
				}
				if(key.isWritable()){
					System.out.println("write will not block");
				}
			}*/
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}finally{
				sc.close();
			    sel.close();
		}
	}
}
