package io;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

public class MonitorWritable implements Writable{

	//收到监控信息，先放入缓存，在存到数据库里面。
	private String content = null;
	@Override
	public void write(DataOutput out) throws IOException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void readFields(DataInput in) throws IOException {
		// TODO Auto-generated method stub
		content = in.readUTF();
	}

	@Override
	public void process(DataOutput out) throws IOException {
		// TODO Auto-generated method stub
		System.out.println("MonitorWritable: " + content);
	}

}
