package mysql;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class ConnectorMariaDB {
	public static Connection conn = null;
	public static String scheme="asset";
	public static String server="localhost";
	public static String port="3306";
	public static String user="robot";
	public static String password="r0b0t";
	
	public ConnectorMariaDB()
	{

	}
	
	public static void connect()
	{
		if(server.equals("localhost"))			
		{		
			System.out.println("Default server, we will try to read the customized configuration!");
			readConf();
		}
//		String urlConnection="jdbc:mysql://"+server+":"+port+"/"+scheme+"?user="+user+"&password="+password;
//		String urlConnection="jdbc:mariadb://"+server+":"+port+"/"+scheme;
		String urlConnection="jdbc:mysql://"+server+":"+port+"/"+scheme;		
		try {
			 conn = DriverManager.getConnection(urlConnection, user, password);
			 
//			 conn = DriverManager.getConnection(urlConnection);

		} catch (SQLException e1) {
			e1.printStackTrace();
		}
//		SET GLOBAL max_allowed_packet=1073741824;

		if(conn!=null)
		{
			String sql = "SET GLOBAL max_allowed_packet=1073741824;";
			
			Statement st = null;
			
			try {
				st = conn.createStatement();
				st.execute(sql);
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
		
	}
	
	public static void disconnect()
	{
		if(conn!=null)
		{
			try {
				conn.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
			System.out.println("Closed connection to MySQL on " +  server);
		}
	}
	
	public static void main(String[] args)
	{
		readConf();
		connect();
		disconnect();
	}
	
	public static void readConf()
	{		
		List<Object> confLines = new ArrayList<>();
		String nameOS = System.getProperty("os.name");
		String fileName="../Conf/mysql.conf";
		if(!nameOS.equals("Linux"))
		{	
			fileName = "..\\Conf\\mysql.conf";		
		}
		
		File confFile = new File(fileName);
		
		if(!confFile.exists())
		{
			System.out.println("Config file not found in relative path! " + fileName);
			fileName=System.getProperty("user.home")+"/App/Conf/mysql.conf";
			confFile = new File(fileName);
			if(!confFile.exists())
			{
				System.out.println("Config file not found in absolute path! " + fileName);
			}
		}
		
		System.out.println("Reading "  + fileName);
		try (Stream<String> stream = Files.lines(Paths.get(fileName))){			
			confLines = stream.collect(Collectors.toList());
			for(Object confLine:confLines)
			{
				String line = (String) confLine;
				if(line.startsWith("#"))
				{
					
				}
				else if(!line.isEmpty())
				{
					String[] words = line.split(";");
					String key = words[0];
					String value = words[1];
					switch(key)
					{
						case "server": server=value; break;
						case "port": port=value; break;
						case "user": user=value; break;
						case "password": password=value; break;
						case "scheme": scheme=value; break;
						default: server="localhost";
						break;
					}
				}
				else
				{
					System.out.println("Linha em branco!");
				}
				System.out.println(line);
			}
 		} catch (IOException e) {
			e.printStackTrace();
		}
						
		System.out.println("Server: "  + server);
		System.out.println("Port: "  + port);
		System.out.println("User: "  + user);
		System.out.println("Password: "  + password);
		System.out.println("Scheme: "  + scheme);
		
	}

	public static Connection getConn() {
		return conn;
	}

	public static void setConn(Connection conn) {
		ConnectorMariaDB.conn = conn;
	}

	public String getServer() {
		return server;
	}

	public void setServer(String server) {
		ConnectorMariaDB.server = server;
	}

	public String getPort() {
		return port;
	}

	public void setPort(String port) {
		ConnectorMariaDB.port = port;
	}

	public String getUser() {
		return user;
	}

	public void setUser(String user) {
		ConnectorMariaDB.user = user;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		ConnectorMariaDB.password = password;
	}

	public static String getScheme() {
		return scheme;
	}

	public static void setScheme(String scheme) {
		ConnectorMariaDB.scheme = scheme;
	}
}
