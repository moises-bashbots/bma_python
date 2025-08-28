package email;

import java.util.ArrayList;
import java.util.Properties;

import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.activation.FileDataSource;
import javax.mail.BodyPart;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;

import mysql.ConnectorMariaDB;
import utils.Utils;

public class SendEmailGmail {
	private static Properties props = new Properties();
	public String loginEmail = "";
	public String passwordEmail = "";
	public String appPasswordEmail="";
	public String messageBody= "";
	public String destinatariosPara="";
	public String destinatariosCC="";
	public String destinatariosCCO="";
	public String subject="";
	public String nameWriter="";
	public String phoneWriter="";
	public ArrayList<String> pathFiles= new ArrayList<String>();

	public SendEmailGmail() 
	{

	}
	
	public SendEmailGmail(String messageBody, String subject, String loginEmail, String passwordEmail, String nameWriter, String destinatariosPara, String destinatariosCC, String destinatariosCCO, ArrayList<String> pathFiles)
	{
		this.subject=subject;
		this.messageBody=messageBody;
		this.loginEmail=loginEmail;
		this.passwordEmail=passwordEmail;
		this.nameWriter=nameWriter;
		this.destinatariosPara=destinatariosPara;
		this.destinatariosCC=destinatariosCC;
		this.destinatariosCCO=destinatariosCCO;
		this.pathFiles=pathFiles;
		
	}
	
	public SendEmailGmail(String messageBody, String subject, String loginEmail, String passwordEmail, String nameWriter, String destinatariosPara, String destinatariosCC, String destinatariosCCO)
	{
		this.subject=subject;
		this.messageBody=messageBody;
		this.loginEmail=loginEmail;
		this.passwordEmail=passwordEmail;
		this.nameWriter=nameWriter;
		this.destinatariosPara=destinatariosPara;
		this.destinatariosCC=destinatariosCC;
		this.destinatariosCCO=destinatariosCCO;
	}
	
	public SendEmailGmail(String messageBody, String subject, String loginEmail, String passwordEmail, String nameWriter, String phoneWriter, String destinatariosPara, String destinatariosCC, String destinatariosCCO)
	{
		this.subject=subject;
		this.messageBody=messageBody;
		this.loginEmail=loginEmail;
		this.passwordEmail=passwordEmail;
		this.nameWriter=nameWriter;
		this.phoneWriter=phoneWriter;
		this.destinatariosPara=destinatariosPara;
		this.destinatariosCC=destinatariosCC;
		this.destinatariosCCO=destinatariosCCO;
	}
	
	public SendEmailGmail(String messageBody, String subject, Email senderEmail, String destinatariosPara, String destinatariosCC, String destinatariosCCO)
	{
		this.subject=subject;
		this.messageBody=messageBody;
		this.loginEmail=senderEmail.getAddress();
		this.passwordEmail=senderEmail.getPassword();
		this.appPasswordEmail=senderEmail.getAppPassword();
		this.nameWriter=senderEmail.getUser();
		this.phoneWriter="";
		this.destinatariosPara=destinatariosPara;
		this.destinatariosCC=destinatariosCC;
		this.destinatariosCCO=destinatariosCCO;
	}	
	

	public static void main(String[] args) 
	{
		ConnectorMariaDB.connect();
		Email senderEmail = Utils.setupEmail(ConnectorMariaDB.conn, "Elegibilidade");
		senderEmail.show();

		String subject="NomeOperacao" + " - Operação " + "NomeArquivo" + " com problema de Elegibilidade!";
		String para="moises@ai4finance.com.br, smoisesr@gmail.com";
		String bodyMessage="<br>Prezados,<br>"
						+ " a operação " + "nomeArquivo" + " se encontra com os seguintes problemas de elegibilidade:<br> "
						+ "<br>";
			  bodyMessage+="A lot of motives";
		SendEmailGmail sendEmailGmail = new SendEmailGmail(bodyMessage, subject, senderEmail,para,"","");
		sendEmailGmail.send(true);
	}
	
	public boolean send(boolean verbose)
	{
		this.setupEmailProperties();
		if(verbose)
		{
			System.out.println("Props: " + props);
			System.out.println(this.destinatariosPara);
			System.out.println(this.destinatariosCC);
			System.out.println(this.subject);
		}
		if(phoneWriter=="")
		{
			this.messageBody += Utils.signature(this.nameWriter);
//			this.messageBody += Utils.signature(this.nameWriter, this.loginEmail);
		}
		else
		{
			this.messageBody += Utils.signature(this.nameWriter, this.loginEmail, phoneWriter);
		}
		
		if(verbose)
		{
			System.out.println(this.messageBody);		
		}
		
		Session session = Session.getDefaultInstance(props,
			new javax.mail.Authenticator() {
				protected PasswordAuthentication getPasswordAuthentication() 
				{
					if(verbose)
					{
						System.out.println("loginEmail: " + loginEmail + " password: " + passwordEmail + " appPassword: "+appPasswordEmail);
					}
					return new PasswordAuthentication(loginEmail,appPasswordEmail);					
				}
			});

		try {

			Message message = new MimeMessage(session);
			String internetAddress = this.nameWriter + " <" + this.loginEmail + ">";
			message.setFrom(new InternetAddress(internetAddress));
			
			message.setRecipients(Message.RecipientType.TO,
					InternetAddress.parse(this.destinatariosPara));
			if(!this.destinatariosCC.isEmpty())
			{
				message.setRecipients(Message.RecipientType.CC, InternetAddress.parse(this.destinatariosCC));
			}
			if(!this.destinatariosCCO.isEmpty())
			{
				message.setRecipients(Message.RecipientType.BCC,
						InternetAddress.parse(this.destinatariosCCO));
			}
			message.setSubject(this.subject);
			String stringHtml="";
			stringHtml = this.messageBody; 
			message.setContent(stringHtml, "text/html; charset=utf-8");
			
			Transport.send(message);
			Utils.waitv(1.00);
			System.out.println("Done");			
			return true;

		} catch (MessagingException e) {
			System.out.println(e.getMessage());		
			new RuntimeException(e);
			System.out.println(e.getMessage());		
			return false;
			
		}
	}

	private void setupEmailProperties() {
		props.put("mail.smtp.host", "smtp.gmail.com");
		props.put("mail.smtp.socketFactory.port", "465");
		props.put("mail.smtp.socketFactory.class","javax.net.ssl.SSLSocketFactory");
		props.put("mail.smtp.auth", "true");
		props.put("mail.smtp.port", "465");		
	}

	public boolean Send(String company, String contact)
	{
		props.put("mail.smtp.host", "smtp.gmail.com");
		props.put("mail.smtp.socketFactory.port", "465");
		props.put("mail.smtp.socketFactory.class","javax.net.ssl.SSLSocketFactory");
		props.put("mail.smtp.auth", "true");
		props.put("mail.smtp.port", "465");
			
		
		System.out.println("Props: " + props);
		System.out.println(this.destinatariosPara);
		System.out.println(this.destinatariosCC);
		System.out.println(this.subject);
		
		this.messageBody += Utils.signature(this.nameWriter, this.loginEmail, company, contact);
		System.out.println(this.messageBody);		
		
		Session session = Session.getDefaultInstance(props,
			new javax.mail.Authenticator() {
				protected PasswordAuthentication getPasswordAuthentication() {
					return new PasswordAuthentication(loginEmail,passwordEmail);					
				}
			});
		
			Message message = new MimeMessage(session);
			String internetAddress = this.nameWriter + " <" + this.loginEmail + ">";
			try {
				message.setFrom(new InternetAddress(internetAddress));
			} catch (AddressException e) {
				e.printStackTrace();
			} catch (MessagingException e) {
				e.printStackTrace();
			}
			System.out.println("From: " + this.nameWriter + " <" + this.loginEmail + ">");
			
			try {
				message.setRecipients(Message.RecipientType.TO,
						InternetAddress.parse(this.destinatariosPara));
				System.out.println("To: " + this.destinatariosPara);
			} catch (AddressException e) {
				e.printStackTrace();
			} catch (MessagingException e) {
				e.printStackTrace();
			}
			
			if(!this.destinatariosCC.isEmpty())
			{
				try {
					message.setRecipients(Message.RecipientType.CC, InternetAddress.parse(this.destinatariosCC));
				} catch (AddressException e) {
					e.printStackTrace();
				} catch (MessagingException e) {			
					e.printStackTrace();
				}
				System.out.println("CC: " + this.destinatariosCC);
			}
			if(!this.destinatariosCCO.isEmpty())
			{
				try {
					message.setRecipients(Message.RecipientType.BCC, InternetAddress.parse(this.destinatariosCCO));
				} catch (AddressException e) {
					e.printStackTrace();
				} catch (MessagingException e) {
					e.printStackTrace();
				}
				System.out.println("BCC: " + this.destinatariosCCO);
			}
			
			try {
				message.setSubject(this.subject);
				System.out.println("Subject: " + this.subject);
			} catch (MessagingException e) {
				e.printStackTrace();
			}
			String stringHtml="";
			stringHtml = this.messageBody; 
			try {
				message.setContent(stringHtml, "text/html; charset=utf-8");
				System.out.println("Content: " + stringHtml);
			} catch (MessagingException e) {
				e.printStackTrace();
			}
			
			try {
				Transport.send(message);
				System.out.println("Message sent!");
			} catch (MessagingException e) {
				System.out.println("Error to send email!");
				e.printStackTrace();
			}
			Utils.waitv(1.00);
			System.out.println("Done");
			return true;
	}
	
	public boolean Send()
	{
		props.put("mail.smtp.host", "smtp.gmail.com");
		props.put("mail.smtp.socketFactory.port", "465");
		props.put("mail.smtp.socketFactory.class","javax.net.ssl.SSLSocketFactory");
		props.put("mail.smtp.auth", "true");
		props.put("mail.smtp.port", "465");
			
		
		System.out.println("Props: " + props);
		System.out.println(this.destinatariosPara);
		System.out.println(this.destinatariosCC);
		System.out.println(this.subject);
		
//		this.messageBody += Utils.signature(this.nameWriter, this.loginEmail, company, contact);
		System.out.println(this.messageBody);		
		
		Session session = Session.getDefaultInstance(props,
			new javax.mail.Authenticator() {
				protected PasswordAuthentication getPasswordAuthentication() {
					return new PasswordAuthentication(loginEmail,passwordEmail);					
				}
			});
		
			Message message = new MimeMessage(session);
			String internetAddress = this.nameWriter + " <" + this.loginEmail + ">";
			try {
				message.setFrom(new InternetAddress(internetAddress));
			} catch (AddressException e) {
				e.printStackTrace();
			} catch (MessagingException e) {
				e.printStackTrace();
			}
			System.out.println("From: " + this.nameWriter + " <" + this.loginEmail + ">");
			
			try {
				message.setRecipients(Message.RecipientType.TO,
						InternetAddress.parse(this.destinatariosPara));
				System.out.println("To: " + this.destinatariosPara);
			} catch (AddressException e) {
				e.printStackTrace();
			} catch (MessagingException e) {
				e.printStackTrace();
			}
			
			if(!this.destinatariosCC.isEmpty())
			{
				try {
					message.setRecipients(Message.RecipientType.CC, InternetAddress.parse(this.destinatariosCC));
				} catch (AddressException e) {
					e.printStackTrace();
				} catch (MessagingException e) {			
					e.printStackTrace();
				}
				System.out.println("CC: " + this.destinatariosCC);
			}
			if(!this.destinatariosCCO.isEmpty())
			{
				try {
					message.setRecipients(Message.RecipientType.BCC, InternetAddress.parse(this.destinatariosCCO));
				} catch (AddressException e) {
					e.printStackTrace();
				} catch (MessagingException e) {
					e.printStackTrace();
				}
				System.out.println("BCC: " + this.destinatariosCCO);
			}
			
			try {
				message.setSubject(this.subject);
				System.out.println("Subject: " + this.subject);
			} catch (MessagingException e) {
				e.printStackTrace();
			}
			String stringHtml="";
			stringHtml = this.messageBody; 
			try {
				message.setContent(stringHtml, "text/html; charset=utf-8");
				System.out.println("Content: " + stringHtml);
			} catch (MessagingException e) {
				e.printStackTrace();
			}
			
			try {
				Transport.send(message);
				System.out.println("Message sent!");
			} catch (MessagingException e) {
				System.out.println("Error to send email!");
				e.printStackTrace();
			}
			Utils.waitv(1.00);
			System.out.println("Done");
			return true;
	}
	
	
	public boolean SendWithAttachments(String company, String contact, ArrayList<String> pathFiles)
	{
		this.pathFiles=pathFiles;
		
		props.put("mail.smtp.host", "smtp.gmail.com");
		props.put("mail.smtp.socketFactory.port", "465");
		props.put("mail.smtp.socketFactory.class","javax.net.ssl.SSLSocketFactory");
		props.put("mail.smtp.auth", "true");
		props.put("mail.smtp.port", "465");
		
		this.setupEmailProperties();

			
		
		System.out.println("Props: " + props);
		System.out.println(this.destinatariosPara);
		System.out.println(this.destinatariosCC);
		System.out.println(this.subject);
		
		this.messageBody += Utils.signature(this.nameWriter, this.loginEmail, company, contact);
		System.out.println(this.messageBody);		
		
		Session session = Session.getDefaultInstance(props,
			new javax.mail.Authenticator() {
				protected PasswordAuthentication getPasswordAuthentication() {
					return new PasswordAuthentication(loginEmail,passwordEmail);					
				}
			});
		
			Message message = new MimeMessage(session);
			String internetAddress = this.nameWriter + " <" + this.loginEmail + ">";
			try {
				message.setFrom(new InternetAddress(internetAddress));
			} catch (AddressException e) {
				e.printStackTrace();
			} catch (MessagingException e) {
				e.printStackTrace();
			}
			System.out.println("From: " + this.nameWriter + " <" + this.loginEmail + ">");
			
			try {
				message.setRecipients(Message.RecipientType.TO,
						InternetAddress.parse(this.destinatariosPara));
				System.out.println("To: " + this.destinatariosPara);
			} catch (AddressException e) {
				e.printStackTrace();
			} catch (MessagingException e) {
				e.printStackTrace();
			}
			
			if(!this.destinatariosCC.isEmpty())
			{
				try {
					message.setRecipients(Message.RecipientType.CC, InternetAddress.parse(this.destinatariosCC));
				} catch (AddressException e) {
					e.printStackTrace();
				} catch (MessagingException e) {			
					e.printStackTrace();
				}
				System.out.println("CC: " + this.destinatariosCC);
			}
			if(!this.destinatariosCCO.isEmpty())
			{
				try {
					message.setRecipients(Message.RecipientType.BCC, InternetAddress.parse(this.destinatariosCCO));
				} catch (AddressException e) {
					e.printStackTrace();
				} catch (MessagingException e) {
					e.printStackTrace();
				}
				System.out.println("BCC: " + this.destinatariosCCO);
			}
			
			try {
				message.setSubject(this.subject);
				System.out.println("Subject: " + this.subject);
			} catch (MessagingException e) {
				e.printStackTrace();
			}
			
			MimeBodyPart mimeBodyPart = new MimeBodyPart();
			String stringHtml="";
			stringHtml = this.messageBody; 
			try {
				mimeBodyPart.setContent(stringHtml, "text/html; charset=utf-8");
				System.out.println("Content: " + stringHtml);
			} catch (MessagingException e) {
				e.printStackTrace();
			}
			
			
			Multipart multipart = new MimeMultipart();
			try {
				multipart.addBodyPart(mimeBodyPart);
			} catch (MessagingException e) {
				e.printStackTrace();
			}
			
			try {
				message.setContent(multipart);
				System.out.println("Content: " + stringHtml);

			} catch (MessagingException e) {
				e.printStackTrace();
			}
			
			if(!pathFiles.isEmpty())
			{
//				MimeBodyPart attachmentBodyPart = new MimeBodyPart();
				for(String pathFile:pathFiles)
				{
					addAttachment(multipart, pathFile);
				}

				try {
					Transport.send(message);
					System.out.println("Message sent!");
				} catch (MessagingException e) {
					System.out.println("Error to send email!");
					e.printStackTrace();
				}
				Utils.waitv(1.00);
				System.out.println("Done");
			}
		return true;
	}
	
	
	public boolean SendWithAttachments(String company, String contact)
	{
		props.put("mail.smtp.host", "smtp.gmail.com");
		props.put("mail.smtp.socketFactory.port", "465");
		props.put("mail.smtp.socketFactory.class","javax.net.ssl.SSLSocketFactory");
		props.put("mail.smtp.auth", "true");
		props.put("mail.smtp.port", "465");
			
		
		System.out.println("Props: " + props);
		System.out.println(this.destinatariosPara);
		System.out.println(this.destinatariosCC);
		System.out.println(this.subject);
		
		this.messageBody += Utils.signature(this.nameWriter, this.loginEmail, company, contact);
		System.out.println(this.messageBody);		
		
		Session session = Session.getDefaultInstance(props,
			new javax.mail.Authenticator() {
				protected PasswordAuthentication getPasswordAuthentication() {
					return new PasswordAuthentication(loginEmail,passwordEmail);					
				}
			});
		
			Message message = new MimeMessage(session);
			String internetAddress = this.nameWriter + " <" + this.loginEmail + ">";
			try {
				message.setFrom(new InternetAddress(internetAddress));
			} catch (AddressException e) {
				e.printStackTrace();
			} catch (MessagingException e) {
				e.printStackTrace();
			}
			System.out.println("From: " + this.nameWriter + " <" + this.loginEmail + ">");
			
			try {
				message.setRecipients(Message.RecipientType.TO,
						InternetAddress.parse(this.destinatariosPara));
				System.out.println("To: " + this.destinatariosPara);
			} catch (AddressException e) {
				e.printStackTrace();
			} catch (MessagingException e) {
				e.printStackTrace();
			}
			
			if(!this.destinatariosCC.isEmpty())
			{
				try {
					message.setRecipients(Message.RecipientType.CC, InternetAddress.parse(this.destinatariosCC));
				} catch (AddressException e) {
					e.printStackTrace();
				} catch (MessagingException e) {			
					e.printStackTrace();
				}
				System.out.println("CC: " + this.destinatariosCC);
			}
			if(!this.destinatariosCCO.isEmpty())
			{
				try {
					message.setRecipients(Message.RecipientType.BCC, InternetAddress.parse(this.destinatariosCCO));
				} catch (AddressException e) {
					e.printStackTrace();
				} catch (MessagingException e) {
					e.printStackTrace();
				}
				System.out.println("BCC: " + this.destinatariosCCO);
			}
			
			try {
				message.setSubject(this.subject);
				System.out.println("Subject: " + this.subject);
			} catch (MessagingException e) {
				e.printStackTrace();
			}
			
			MimeBodyPart mimeBodyPart = new MimeBodyPart();
			String stringHtml="";
			stringHtml = this.messageBody; 
			try {
				mimeBodyPart.setContent(stringHtml, "text/html; charset=utf-8");
				System.out.println("Content: " + stringHtml);
			} catch (MessagingException e) {
				e.printStackTrace();
			}
			
			
			Multipart multipart = new MimeMultipart();
			try {
				multipart.addBodyPart(mimeBodyPart);
			} catch (MessagingException e) {
				e.printStackTrace();
			}
			
			try {
				message.setContent(multipart);
				System.out.println("Content: " + stringHtml);

			} catch (MessagingException e) {
				e.printStackTrace();
			}
			
			if(!pathFiles.isEmpty())
			{
//				MimeBodyPart attachmentBodyPart = new MimeBodyPart();
				for(String pathFile:pathFiles)
				{
					addAttachment(multipart, pathFile);
				}

				try {
					Transport.send(message);
					System.out.println("Message sent!");
				} catch (MessagingException e) {
					System.out.println("Error to send email!");
					e.printStackTrace();
				}
				Utils.waitv(1.00);
				System.out.println("Done");
			}
		return true;
	}
	
	private static void addAttachment(Multipart multipart, String filename)
	{
	    DataSource source = new FileDataSource(filename);
	    BodyPart messageBodyPart = new MimeBodyPart();        
	    try {
			messageBodyPart.setDataHandler(new DataHandler(source));
		} catch (MessagingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	    try {
			messageBodyPart.setFileName(filename);
		} catch (MessagingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	    try {
			multipart.addBodyPart(messageBodyPart);
		} catch (MessagingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public static Properties getProps() {
		return props;
	}

	public static void setProps(Properties props) {
		SendEmailGmail.props = props;
	}

	public String getLoginEmail() {
		return loginEmail;
	}

	public void setLoginEmail(String loginEmail) {
		this.loginEmail = loginEmail;
	}

	public String getPasswordEmail() {
		return passwordEmail;
	}

	public void setPasswordEmail(String passwordEmail) {
		this.passwordEmail = passwordEmail;
	}

	public String getMessageBody() {
		return messageBody;
	}

	public void setMessageBody(String messageBody) {
		this.messageBody = messageBody;
	}

	public String getDestinatariosPara() {
		return destinatariosPara;
	}

	public void setDestinatariosPara(String destinatariosPara) {
		this.destinatariosPara = destinatariosPara;
	}

	public String getDestinatariosCC() {
		return destinatariosCC;
	}

	public void setDestinatariosCC(String destinatariosCC) {
		this.destinatariosCC = destinatariosCC;
	}

	public String getDestinatariosCCO() {
		return destinatariosCCO;
	}

	public void setDestinatariosCCO(String destinatariosCCO) {
		this.destinatariosCCO = destinatariosCCO;
	}

	public String getSubject() {
		return subject;
	}

	public void setSubject(String subject) {
		this.subject = subject;
	}

	public String getNameWriter() {
		return nameWriter;
	}

	public void setNameWriter(String nameWriter) {
		this.nameWriter = nameWriter;
	}

	public String getPhoneWriter() {
		return phoneWriter;
	}

	public void setPhoneWriter(String phoneWriter) {
		this.phoneWriter = phoneWriter;
	}

	public ArrayList<String> getPathFiles() {
		return pathFiles;
	}

	public void setPathFiles(ArrayList<String> pathFiles) {
		this.pathFiles = pathFiles;
	}

	public String getAppPasswordEmail() {
		return this.appPasswordEmail;
	}

	public void setAppPasswordEmail(String appPasswordEmail) {
		this.appPasswordEmail = appPasswordEmail;
	}

}