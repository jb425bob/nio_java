package io;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;

public class SocketChannelOutputStream extends OutputStream{

	ByteBuffer buffer;
	ByteBuffer flush;
	SocketChannel socket;
	Selector selector;
	public SocketChannelOutputStream(SocketChannel sc){
		this.socket = sc;
		buffer = ByteBuffer.allocate(8);
		System.out.println("construction socketChannelOutputStream");
	}
	@Override
	public void write(int b) throws IOException {
		// TODO Auto-generated method stub
		buffer.clear();
		buffer.put((byte)b);
		buffer.flip();
		flush = buffer;
		flushBuffer();
	}
	
	private void flushBuffer() throws IOException{
		
		while(flush.hasRemaining()){
			int len = socket.write(flush);
			if(len < 0){
				throw new IOException("EOF");
			}
			if(len == 0){
				Thread.yield();
				len = socket.write(flush);
				if(len < 0)
					throw new IOException("EOF");
				if(len == 0){
					if(selector == null){
						selector = Selector.open();
						socket.register(selector, SelectionKey.OP_WRITE);
					}
					selector.select();
				}
			}
			
		}
		flush = null;
	}
	@Override
	public void close() throws IOException {
		// TODO Auto-generated method stub
		socket.close();
	}
	@Override
	public void flush() throws IOException {
		// TODO Auto-generated method stub
	
	}
	@Override
	public void write(byte[] b, int off, int len) throws IOException {
		// TODO Auto-generated method stub
		flush = ByteBuffer.wrap(b, off, len);
		flushBuffer();
	}
	@Override
	public void write(byte[] b) throws IOException {
		// TODO Auto-generated method stub
		flush = ByteBuffer.wrap(b);
		flushBuffer();
		System.out.println("write" + b);
	}

	public void destroy() {
		
		if(selector != null){
			try {
				selector.close();
			} catch (IOException e) {}
			selector = null;
			buffer = null;
			flush = null;
			socket = null;
		}
	}
	
	
}
