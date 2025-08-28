package mssql;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.microsoft.sqlserver.jdbc.SQLServerDataSource;
import com.microsoft.sqlserver.jdbc.SQLServerException;


public class ConnectorMSSQL {
	
	public static Connection conn=null;
	private static String server="localhost";
	private static int port=1433;
	private static String user="robot";
	private static String password="r0b0t";
	private static String scheme="dtvm";
	public static String JDBC_URL = "";
	
	
	public ConnectorMSSQL()
	{
		
	}
	
	
	
	public static void getDbConnection() 
	{
//		jdbc:sqlserver://dbHost\sqlexpress;user=sa;password=secret;
//		JDBC_URL = "jdbc:sqlserver://10.0.155.6:1433;databaseName=FidcCustodia;user=aiservico;password=@#!LiMINE1723";
		String url = "jdbc:sqlserver://"+server+":"+port+";databaseName="+scheme+";user="+user+";password="+password+";encrypt=true;trustServerCertificate=true";
//        String connectionUrl = "jdbc:sqlserver://177.85.36.105:1433;databaseName=FRT_CUST_PRD;user=indigo-rb001;password=Ind1g02020@";
        JDBC_URL = url;
		System.out.println(JDBC_URL);
//		System.out.println(connectionUrl);
        try 
        {
            Class.forName("com.microsoft.sqlserver.jdbc.SQLServerDriver");
            conn = DriverManager.getConnection(JDBC_URL);
            if(conn != null) {
                DatabaseMetaData metaObj = (DatabaseMetaData) conn.getMetaData();
                System.out.println("Driver Name?= " + metaObj.getDriverName() + ", Driver Version?= " + metaObj.getDriverVersion() + ", Product Name?= " + metaObj.getDatabaseProductName() + ", Product Version?= " + metaObj.getDatabaseProductVersion());
            }
        } catch(Exception sqlException) {
            sqlException.printStackTrace();
        }
    }
 
    public static void main(String[] args)
    {
    	server="10.0.155.6";
    	user="aiservico";
    	password="@#!LiMINE1723";
    	scheme="FidcCustodia";
//        getDbConnection();
    	ConnectorMSSQL.connect();
//        
//     // Create a variable for the connection string.
////        String connectionUrl = "jdbc:sqlserver://<server>:<port>;databaseName=AdventureWorks;user=<user>;password=<password>";
////        String connectionUrl = "jdbc:sqlserver://10.0.155.6:1433;databaseName=FidcCustodia;user=aiservico;password=@#!LiMINE1723";
//        String connectionUrl = "jdbc:sqlserver://10.0.155.6:1433;user=aiservico;password=@#!LiMINE1723";
//
//        try (Connection con = DriverManager.getConnection(connectionUrl); Statement stmt = con.createStatement();) {
//            String SQL = "SELECT TOP 10 * FROM Person.Contact";
//            ResultSet rs = stmt.executeQuery(SQL);
//
//            // Iterate through the data in the result set and display it.
//            while (rs.next()) {
//                System.out.println(rs.getString("FirstName") + " " + rs.getString("LastName"));
//            }
//        }
//        // Handle any errors that may have occurred.
//        catch (SQLException e) {
//            e.printStackTrace();
//        }
    }
	

	public static void connect()
	{
		if(server.equals("localhost"))
		{
			System.out.println("Default server, we will try to read the customized configuration!");
			readConf();

		}

		if(JDBC_URL.length()>0)
		{
			System.out.println(JDBC_URL);
			try {
				conn=DriverManager.getConnection(JDBC_URL);
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
        
        if(conn!=null)
		{
			System.out.println("Connected to MSSQL on " +  server + ":" + port);			
		}
		else
		{
			System.out.println("Failed to make connection!");
			System.out.println("Trying alternative method");
			getDbConnection();
			if(conn!=null)
			{
				System.out.println("Connected to MSSQL on " +  server + ":" + port);
				getDbConnection();
			}
			else
			{
				System.out.println("Failed to make connection again!");
			}
		}
	}

	public static void connect(String pathFileConf)
	{
		System.out.println("Reading the customized configuration!");
		readConf(pathFileConf);
		
//		SQLServerDataSource ds = new SQLServerDataSource();
//        ds.setUser(user);
//        ds.setPassword(password);
//        ds.setServerName(server);
//        ds.setPortNumber(port);
//        ds.setDatabaseName(scheme);
        
        
//        // Create a variable for the connection string.
//        String connectionUrl = "jdbc:sqlserver://177.85.36.105:1433;databaseName=FRT_CUST_PRD;user=indigo-rb001;password=Ind1g02020@";
//
//        try (Connection con = DriverManager.getConnection(connectionUrl); Statement stmt = con.createStatement();) {
//            String SQL = "SELECT * FROM vw_multipag";
//            ResultSet rs = stmt.executeQuery(SQL);
//
//            // Iterate through the data in the result set and display it.
//            while (rs.next()) {
//                System.out.println(rs.getString("nome_arquivo") + " " + rs.getString("nome_fundo")  + " " + rs.getString("cnpj_fundo"));
//            }
//        }
//        // Handle any errors that may have occurred.
//        catch (SQLException e) {
//            e.printStackTrace();
//        }
		
//		String url = "jdbc:sqlserver://"+server+":"+port+";databaseName="+scheme+";user="+user+";password="+password;
//		System.out.println(url);
//		System.out.println(connectionUrl);
//		System.out.println(JDBC_URL);
//        
//        
//		try {
//			conn=DriverManager.getConnection(JDBC_URL);
//		} catch (SQLException e) {
//			e.printStackTrace();
//		}
		
        
        if(conn!=null)
		{
			System.out.println("Connected to MSSQL on " +  server + ":" + port);			
		}
		else
		{
			System.out.println("Failed to make connection!");
			System.out.println("Trying alternative method");
			getDbConnection();
			if(conn!=null)
			{
				System.out.println("Connected to MSSQL on " +  server + ":" + port);
				getDbConnection();
			}
			else
			{
				System.out.println("Failed to make connection again!");
			}
		}
	}

	
	public static void connect(String server, int port, String scheme, String user, String password)
	{
		if(server.equals("localhost"))
		{
			System.out.println("Default server, we will try to read the customized configuration!");
			readConf();

		}
		
		SQLServerDataSource ds = new SQLServerDataSource();
        ds.setUser(user);
        ds.setPassword(password);
        ds.setServerName(server);
        ds.setPortNumber(port);
        ds.setDatabaseName(scheme);
        
        try {
			conn=ds.getConnection();
		} catch (SQLServerException e) {
			e.printStackTrace();
		}
        
        if(conn!=null)
		{
			System.out.println("Connected to MSSQL on " +  server + ":" + port);
		}
		else
		{
			System.out.println("Failed to make connection!");			
		}
	}	

	
	public static void readConf()
	{		
		List<Object> confLines = new ArrayList<>();
		String nameOS = System.getProperty("os.name");
		String fileName=System.getProperty("user.home")+File.separator+"App"+File.separator+"Conf" +File.separator+"mssql.conf";
		
		if(!nameOS.equals("Linux"))
		{	
			fileName = "..\\Conf\\mssql.conf";
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
					String allValues = line.replaceAll("url;", "");
					System.out.println("AV: " + allValues);
					switch(key)
					{
						case "server": server=value; break;
						case "port": port=Integer.parseInt(value); break;
						case "user": user=value; break;
						case "password": password=value; break;
						case "scheme": scheme=value; break;
						case "url":	JDBC_URL=allValues;break;
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
	
	public static void readConf(String pathFileConf)
	{		
		List<Object> confLines = new ArrayList<>();
		String nameOS = System.getProperty("os.name");
		String fileName=pathFileConf;
		if(!nameOS.equals("Linux"))
		{	
			fileName = "..\\Conf\\mssql.conf";		
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
					String allValues = line.replaceAll("url;", "");
					System.out.println("AV: " + allValues);
					switch(key)
					{
						case "server": server=value; break;
						case "port": port=Integer.parseInt(value); break;
						case "user": user=value; break;
						case "password": password=value; break;
						case "scheme": scheme=value; break;
						case "url":	JDBC_URL=allValues;break;
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
	
	public static void disconnect()
	{
		try {
			conn.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}



	public static Connection getConn() {
		return conn;
	}


	public static void setConn(Connection conn) {
		ConnectorMSSQL.conn = conn;
	}


	public static String getServer() {
		return server;
	}


	public static void setServer(String server) {
		ConnectorMSSQL.server = server;
	}


	public static int getPort() {
		return port;
	}


	public static void setPort(int port) {
		ConnectorMSSQL.port = port;
	}


	public static String getUser() {
		return user;
	}


	public static void setUser(String user) {
		ConnectorMSSQL.user = user;
	}


	public static String getPassword() {
		return password;
	}


	public static void setPassword(String password) {
		ConnectorMSSQL.password = password;
	}
	public static String getScheme() {
		return scheme;
	}


	public static void setScheme(String scheme) {
		ConnectorMSSQL.scheme = scheme;
	}
}
