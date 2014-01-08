package bo.ma;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.charset.Charset;
import java.util.Iterator;

public class MultiJabberServer1 {

	public static final int PORT = 8080;
	public static void main(String[] args) throws IOException {
		Charset cs = Charset.forName("utf8");
		ByteBuffer buffer = ByteBuffer.allocate(16);
		SocketChannel ch = null;
		ServerSocketChannel ssc = ServerSocketChannel.open();
		Selector sel = Selector.open();
		
		try{
			ssc.configureBlocking(false);
			ssc.socket().bind(new InetSocketAddress(PORT));
			ssc.register(sel, SelectionKey.OP_ACCEPT);
			System.out.println("server on port: " + PORT);
			while(true){
				sel.select();
				Iterator it = sel.selectedKeys().iterator();
				while(it.hasNext()){
					SelectionKey key = (SelectionKey) it.next();
					it.remove();
					if(key.isAcceptable()){
						
						ch = ssc.accept();
						System.out.println("Accepted connection from: " +
								ch.socket());
						ch.configureBlocking(false);
						ch.register(sel, SelectionKey.OP_READ);
					}
					else{
						ch = (SocketChannel) key.channel();
						ch.read(buffer);
						CharBuffer cb = cs.decode((ByteBuffer)buffer.flip());
						String response = cb.toString();
						System.out.println("eCHOING : " + response);
						ch.write((ByteBuffer)buffer.rewind());
						if(response.indexOf("END") != -1) ch.close();
						buffer.clear();
					}
				}
			}
		}finally{
			
			if(ch != null) ch.close();
			ssc.close();
			sel.close();
		}
	}
}
