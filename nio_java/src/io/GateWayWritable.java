package io;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import sql.CRUD_SQL;

public class GateWayWritable implements Writable{

	private byte [] snNumber = new byte[28];
	private byte CMD ;
	private byte [] content = null;
	private int length;
	private int utf8_length = 0;
	//private CRUD_SQL _mysql = new CRUD_SQL();
	private final String init_version = "[{name:\"control\",version:\"00.00.00.00\"},{name:\"room\",version:\"00.00.00.00\"},{name:\"monitor\",version:\"00.00.00.00\"},{name:\"scene\",version:\"00.00.00.00\"}]";
	public GateWayWritable(int length){
		this.length =length;
		content = new byte[length-29];
	}
	@Override
	public void write(DataOutput out) throws IOException {
		// TODO Auto-generated method stub
		/*List<Map<?,?>> _lists = new ArrayList<Map<?,?>>();
		if(_mysql.connectMySQL("smarthome")!= true){
			
			show_jsResult.put("error", 1);
			_mysql.closeMySQL();
			return SUCCESS;
		}
		if( (nNum = _mysql.search2Count("select count(*) from family_gates " + limit_sql)) != -1)
			show_jsResult.put("total", nNum);
		if(_mysql.search2MySQL("select sncode,password,status,province,city,zone,location from family_gates " + limit_sql + sql_limit, _lists) != true)
			show_jsResult.put("error", 1);
		if(_mysql.closeMySQL() != true)
			show_jsResult.put("error", 1);*/
		byte []bytes = str2UTF8bytes(init_version);
		if(CMD == (byte)0x01){
			//获取版本信息
			out.write(new byte[]{(byte)0x9d, (byte)0x9d, (byte)0x01});
			out.writeInt(utf8_length + 28 + 7 + 1);
			out.write(new byte[]{(byte)0x10});
			out.write(snNumber, 0, 28);
			out.write(bytes, 0, utf8_length);
		}else if(CMD == (byte)0x02){
			//版本信息反馈version.json
		}else if(CMD == (byte)0x03){
			//控制信息更新control.json
		}else if(CMD == (byte)0x04){
			//监控信息更新monitor.json
		}
		
	}

	private byte[] str2UTF8bytes(String str){
		int tempLength = str.length();
		byte[] bytes = new byte[tempLength * 3];
		int l = 0; 
	   	 for(int i=0; i<tempLength;i++){
	   		 
	   		 int code = str.charAt(i);
	   		 if(code >= 0x01 && code <= 0x7f){
	   			 bytes[l++] = (byte)code;
	   		 }else if(code <= 0x07FF){
	   			 bytes[l++] = (byte)(0xC0 | ((code >> 6) & 0x1F));
	   			 bytes[l++] = (byte)(0x80 | code & 0x3F);
	   		 }else {
	   			 bytes[l++] = (byte)(0xE0 | ((code >> 12) & 0x0F) );
	   			 bytes[l++] = (byte)(0x80 | ((code >> 6) & 0x3F));
	   			 bytes[l++] = (byte)(0x80 | (code & 0x3F));
	   		 }
	   	 }
	   	 utf8_length = l;
	   	 return bytes;
	}
	@Override
	public void readFields(DataInput in) throws IOException {
		// TODO Auto-generated method stub
		CMD = in.readByte();
		//0x01 网关请求版本信息
		//0x02 网关发送新的版本信息version.json
		//0x03网关发送control.json
		//0x04网关发送monitor.json
		in.readFully(snNumber, 0, 28);
		if(CMD != (byte)0x01 ){
			in.readFully(content);
		}
	}

	@Override
	public void process(DataOutput out) throws IOException  {
		// TODO Auto-generated method stub
		byte []bytes = str2UTF8bytes(init_version);
		if(CMD == (byte)0x01){
			//获取版本信息
			System.out.println(out.toString());
			out.write(new byte[]{(byte)0x9d, (byte)0x9d, (byte)0x01});
			out.writeInt(utf8_length + 28 + 7 + 1);
			out.write(new byte[]{(byte)0x10});
			out.write(snNumber, 0, 28);
			out.write(bytes, 0, utf8_length);
			System.out.println("write success");
		}else if(CMD == (byte)0x02){
			//版本信息反馈version.json
		}else if(CMD == (byte)0x03){
			//控制信息更新control.json
		}else if(CMD == (byte)0x04){
			//监控信息更新monitor.json
		}
		try{
			String str = new String(content, 0, content.length, "utf-8");
			System.out.println(str);
		} catch(UnsupportedEncodingException e){
			e.printStackTrace();
		}
	}

}
