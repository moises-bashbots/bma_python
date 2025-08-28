package email;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class Email {
	private String smtpServer="";
	private String imapServer="";
	private int smtpPort=0;
	private int imapPort=0;
	private String security="";
	private String user="";
	private String password="";
	private String appPassword="";
	private String address="";
	private String phone="";
	
	public Email()
	{
		
	}
	
	public Email(String smtpServer, int smtpPort, String imapServer, int imapPort, String security, String user, String address, String password)
	{
		this.smtpServer=smtpServer;
		this.smtpPort=smtpPort;
		this.imapServer=imapServer;
		this.imapPort=imapPort;
		this.security=security;
		this.user=user;
		this.address=address;
		this.password=password;
	}

	public Email(String smtpServer, int smtpPort, String imapServer, int imapPort, String security, String user, String phone, String address, String password)
	{
		this.smtpServer=smtpServer;
		this.smtpPort=smtpPort;
		this.imapServer=imapServer;
		this.imapPort=imapPort;
		this.security=security;
		this.user=user;
		this.phone=phone;
		this.address=address;
		this.password=password;
	}
	
	public  void readConf()
	{		
		List<Object> confLines = new ArrayList<>();
		String nameOS = System.getProperty("os.name");
		String fileName="../Conf/email.conf";
		if(!nameOS.equals("Linux"))
		{	
			fileName = "..\\Conf\\email.conf";		
		}
		
		File confFile = new File(fileName);
		
		if(!confFile.exists())
		{
			System.out.println("Config file not found in relative path! " + fileName);
			fileName=System.getProperty("user.home")+"/App/Conf/email.conf";
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
						case "user": this.user=value; break;
						case "password": this.password=value; break;
						case "apppassword": this.appPassword=value; break;
						case "smtpServer": this.smtpServer=value; break;
						case "smtpPort": this.smtpPort=Integer.parseInt(value); break;
						case "imapServer": this.imapServer=value; break;
						case "imapPort": this.imapPort=Integer.parseInt(value); break;
						case "address": this.address=value; break;
						default: 	break;						
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
		if(address.length()==0)
		{
			address=user;
		}
		System.out.println("User: "  + user);
		System.out.println("Address: " + address );
		System.out.println("Password: "  + password);
		System.out.println("AppPassword: "+ appPassword);
	}
	
	public void show()
	{
		System.out.println("User: " + user + " address: " + address + " password: " + password);
	}
	public String getSmtpServer() {
		return smtpServer;
	}
	public void setSmtpServer(String smtpServer) {
		this.smtpServer = smtpServer;
	}
	public String getImapServer() {
		return imapServer;
	}
	public void setImapServer(String imapServer) {
		this.imapServer = imapServer;
	}
	public int getSmtpPort() {
		return smtpPort;
	}
	public void setSmtpPort(int smtpPort) {
		this.smtpPort = smtpPort;
	}
	public int getImapPort() {
		return imapPort;
	}
	public void setImapPort(int imapPort) {
		this.imapPort = imapPort;
	}
	public String getSecurity() {
		return security;
	}
	public void setSecurity(String security) {
		this.security = security;
	}
	public String getUser() {
		return user;
	}
	public void setUser(String user) {
		this.user = user;
	}
	public String getPassword() {
		return password;
	}
	public void setPassword(String password) {
		this.password = password;
	}
	public String getAddress() {
		return address;
	}
	public void setAddress(String address) {
		this.address = address;
	}

	public String getPhone() {
		return phone;
	}

	public void setPhone(String phone) {
		this.phone = phone;
	}

	public String getAppPassword() {
		return this.appPassword;
	}

	public void setAppPassword(String appPassword) {
		this.appPassword = appPassword;
	}
	
	
	
}
