package sql;

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

public class CRUD_SQL {

	
	private static String url = "jdbc:mysql://10.2.10.222:3306/smarthome";
	private static String user = "mb";
	private static String password = "123456";
	
	private Connection _connection = null;
	private Statement _statement = null;
	private ResultSet _resultSet = null;
	private ResultSetMetaData _metaData  = null;
	private boolean _isConnected = false;
	
	public CRUD_SQL(){
		System.out.println("a new sql object");
	}
	static {
		InputStream inputStream = CRUD_SQL.class.getClassLoader().getResourceAsStream("database.properties");
		Properties p = new Properties();
		try {
			p.load(inputStream);
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		url = "jdbc:mysql://" + p.getProperty("serverIP") + ":3306/";
		user = p.getProperty("user");
		password = p.getProperty("password");
		System.out.println("MySQLUrl : " + url);
		System.out.println("user : " + user);
		System.out.println("password : " + password);
		try {
			Class.forName("com.mysql.jdbc.Driver");
			
		}catch(ClassNotFoundException e) {
			System.out.println("inialize exception");
			//throw new ExceptionInInitializerError(e);
		}
	}
	
	public  boolean connectMySQL(String database){
		System.out.println("connecting to mysql!");
		try{
			_connection = DriverManager.getConnection(url + database, user, password);
		}catch(SQLException e){
			System.out.println("connection MySQL exception !");
			return false;
		}
		try{
			_statement = _connection.createStatement();
		} catch(SQLException e){
			System.out.println("create statement for MySQL exception !");
			return false;
		}
		_isConnected = true;
		System.out.println("connected!");
		return true;
	}
	
	public  boolean add2MySQL(String sql){
		
		System.out.println("mysql insert");
		if(!_isConnected || _connection == null || _statement == null){
			return false;
		}
		try{
			_statement.executeUpdate(sql);
		} catch(SQLException e){
			System.out.println("executing insert exception !");
			return false;
		}
		
		return true;
	}
	public  boolean delete2MySQL(String sql){
		
		System.out.println("mysql delete");
		if(!_isConnected || _connection == null || _statement == null){
			return false;
		}
		try{
			_statement.executeUpdate(sql);
		} catch(SQLException e){
			System.out.println("executing delete exception !");
			return false;
		}
		
		return true;
		
	}
	public  boolean update2MySQL(String sql){
		
		System.out.println("sql update");
		if(!_isConnected || _connection == null || _statement == null){
			return false;
		}
		try{
			_statement.executeUpdate(sql);
		} catch(SQLException e){
			System.out.println("executing update exception !");
			return false;
		}
		
		return true;
	}
	
	public  int search2Count(String sql){
		int num;
		System.out.println("sql search count");
		if(!_isConnected || _connection == null || _statement == null){
			return -1;
		}
		
		try{
			_resultSet = _statement.executeQuery(sql);
			_resultSet.next();
			if(_resultSet.getObject(1) == null){
				num = 0;
			}
			else{
			num  = Integer.parseInt(_resultSet.getObject(1).toString());
			}
			//_metaData = _resultSet.getMetaData();
		} catch(SQLException e){
			System.out.println("executing search exception !");
			return -1;
		}
		return num;
	}
	public  boolean search2MySQL(String sql, List<Map<?,?>> lists){
		
		System.out.println("search sql");
		if(!_isConnected || _connection == null || _statement == null){
			return false;
		}
		
		try{
			_resultSet = _statement.executeQuery(sql);
			_metaData = _resultSet.getMetaData();
		} catch(SQLException e){
			System.out.println("executing search exception !");
			return false;
		}
		
		try {
			int columnNum = _metaData.getColumnCount();
			
			while(_resultSet.next()) {
				Map<String, String> map = new HashMap<String, String>();
				for(int i = 1; i <= columnNum; i++)
				{
				    String columnName = _metaData.getColumnName(i);
				    String columnAttribute = "";
					byte[] bytes = _resultSet.getBytes(i);				
					if(bytes != null)
						try {
							columnAttribute = new String(_resultSet.getBytes(i), "utf-8");
						} catch (UnsupportedEncodingException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					else
					 columnAttribute = "";
					
					map.put(columnName, columnAttribute);	
					
				}
				lists.add(map);
				}
			
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			//e.printStackTrace();
			System.out.println("store results exception!");
			return false;
		}
		System.out.println("show success!");
		return true;
		
	}
	public boolean customSearch2MySQL(String sql, List<Map<?,?>> lists){
		
		System.out.println("custom search sql");
		if(!_isConnected || _connection == null || _statement == null){
			return false;
		}
		
		try{
			_resultSet = _statement.executeQuery(sql);
			_metaData = _resultSet.getMetaData();
		} catch(SQLException e){
			System.out.println("executing search exception !");
			return false;
		}
		
		try {
			int columnNum = _metaData.getColumnCount();
			
			while(_resultSet.next()) {
				Map<String, String> map = new HashMap<String, String>();
				for(int i = 1; i <= columnNum; i++)
				{
				    String columnName = _metaData.getColumnName(i);
				    String columnAttribute = "";
					byte[] bytes = _resultSet.getBytes(i);				
					if(bytes != null)
						try {
							columnAttribute = new String(_resultSet.getBytes(i), "utf-8");
						} catch (UnsupportedEncodingException e) {
							// TODO Auto-generated catch block
							//e.printStackTrace();
							System.out.println("UnsupportedEncodingException");
						}
					else
					 columnAttribute = "";
					
					//if(!columnName.equals("password")){
						if(columnName.equals("privilege")){
							int nPrivilege = Integer.parseInt(columnAttribute);
							for(int j=1;j<10;j++){
								map.put(columnName + j, ((nPrivilege>>(9-j)) & 1) +"");
								//System.out.println(((nPrivilege>>(9-j)) & 1) + "xxx");
							}
						}else{
							map.put(columnName, columnAttribute);
						}	
						
				//	}
					//System.out.println("xxx" + columnName + ":" + columnAttribute);
					
				}
				lists.add(map);
				}
			
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			//e.printStackTrace();
			System.out.println("custom search exception!");
			return false;
		}
		System.out.println("show success!");
		return true;
		
	}
	public  boolean closeMySQL(){
		
		try {
			if(_resultSet != null)
				_resultSet.close();
		}catch(SQLException e) {
			System.out.println("close resultset exception");
		}finally {
			try {
				if(_statement != null)
					_statement.close();
				
			}catch(SQLException e) {
				System.out.println("close statement exception!");
			}finally {
				try {
					if(_connection != null) 
						_connection.close();
				}catch(SQLException e){
					System.out.println("close connection exception!");
				}
			}
			
		}
		System.out.println("close success");
		return true;
	}
	
	public static void main(String[] args) {
		CRUD_SQL mysql = new CRUD_SQL();
		mysql.connectMySQL("smarthome");
		System.out.println(mysql.search2Count("select count(*) from family_gates"));
		mysql.closeMySQL();
	}
}
