package io;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.io.UnsupportedEncodingException;

public class MobileWritable implements Writable{

	private byte[] content = null;
	private int length = 0;
	public MobileWritable(int length){
		this.length =length;
		content = new byte[length];
	}
	@Override
	public void write(DataOutput out) throws IOException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void readFields(DataInput in) throws IOException {
		// TODO Auto-generated method stub
		System.out.println("MobileWritable readFields");
		in.readFully(content);
		try{
			String str = new String(content, 0, length, "utf-8");
			System.out.println(str);
		} catch(UnsupportedEncodingException e){
			e.printStackTrace();
		}
		
	
	}

	@Override
	public void process(DataOutput out) throws IOException {
		// TODO Auto-generated method stub
		
		System.out.println("MobileWritalbe : " + content);
		try{
			String str = new String(content, 0, length, "utf-8");
			System.out.println(str);
		} catch(UnsupportedEncodingException e){
			e.printStackTrace();
		}
	}

}
