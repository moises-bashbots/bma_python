package bradesco;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.KeyStore;
import java.security.SecureRandom;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import empresa.DadosBancariosEmpresa;
import empresa.Empresa;

public class Authentication 
{
	private static String pathRootCertificates= System.getProperty("user.home")+File.separator+"App"+File.separator+"BradescoCertificados";
	private int idAuthentication=0;
	private String clientId="";
	private String clientSecret="";
	private File certificateP12 = null;
	private String passwdP12 = "";
	private AccessToken accessToken=new AccessToken();
	public static String urlAuth="https://sts.itau.com.br/api/oauth/token";

	
	public static void main(String[] args)
	{
//		ConnectorMariaDB.connect();
//		DadosBancarios dadosBancarios = new DadosBancarios(ConnectorMariaDB.conn, 4);
//		Authentication authenticationDb=new Authentication(ConnectorMariaDB.conn, dadosBancarios);
		Authentication authentication = new Authentication("/home/moises/Files/Clients/BMA/Certificado/1001156785.p12"
																									, "TpLink0509"
																									, "4f75e132-4e3d-4e5f-abe2-9d574e3dafa6"
																									, "f37a47ea-eb3d-4fca-ac8d-637b5990b0db");
		authentication.obtainAccessTokenHomologacao();
		authentication.getAccessToken().show();
//		
	}
	
	public Authentication()
	{
		
	}
	
	public Authentication(String p12FileName, String p12FilePassword, String clientId, String clientSecret)
	{
		this.certificateP12 = new File(p12FileName);
		this.passwdP12=p12FilePassword;
		this.clientId=clientId;
		this.clientSecret=clientSecret;
	}
	public Authentication(DadosBancariosEmpresa dadosBancariosEmpresa)
	{
		this.certificateP12 = dadosBancariosEmpresa.getCertificateP12();
		this.passwdP12=dadosBancariosEmpresa.getPasswdP12();
		this.clientId=dadosBancariosEmpresa.getClientId();
		this.clientSecret=dadosBancariosEmpresa.getClientSecret();
	}
	
	public Authentication(Connection conn, Empresa empresa)
	{
		String query="select * from  BMA.dados_bancarios_empresa where empresa_id_empresa="+empresa.getIdEmpresa();
		Statement st = null;
		try {
			st = conn.createStatement();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		
		ResultSet rs = null;
		
		try {
			rs = st.executeQuery(query);
			while(rs.next())
			{
				this.clientId = rs.getString("client_id");
				this.clientSecret = rs.getString("client_secret");
				String pathFileP12 = rs.getString("path_certificate");
				this.certificateP12 = new File(pathFileP12);
				this.passwdP12 = rs.getString("password_certificate");
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	public void obtainAccessTokenHomologacao()
	{
		HttpURLConnection connection = null;
		try {
			/*
			 * Uncomment line below for production
			 */
//			URL url = new URL("https://sts.itau.com.br/api/oauth/token");
			URL url = new URL("https://qrpix-h.bradesco.com.br/oauth/token");
			connection = (HttpURLConnection) url.openConnection();
			connection.addRequestProperty("Content-Type", "application/x-www-form-urlencoded");
			connection.setRequestMethod("POST");
			connection.setDoOutput(true);

			// Add certificate
//			File p12 = new File("\\Dir\\file.p12");
//			String p12password = "YOUR_P12_PASSWORD";
			if(this.certificateP12==null)
			{
				System.out.println("Certificate null!");
				System.exit(1);
			}
			InputStream keyInput = new FileInputStream(this.certificateP12);

			KeyStore keyStore = KeyStore.getInstance("PKCS12");
			keyStore.load(keyInput, this.passwdP12.toCharArray());
			keyInput.close();

			KeyManagerFactory keyManagerFactory = KeyManagerFactory.getInstance("SunX509");
			keyManagerFactory.init(keyStore, this.passwdP12.toCharArray());

			SSLContext context = SSLContext.getInstance("TLS");
			context.init(keyManagerFactory.getKeyManagers(), null, new SecureRandom());

			SSLSocketFactory socketFactory = context.getSocketFactory();
			if (connection instanceof HttpsURLConnection)
				((HttpsURLConnection) connection).setSSLSocketFactory(socketFactory);
			//

			String body = "grant_type=client_credentials"
					+ "&client_id=" + this.clientId 
					+ "&client_secret=" + this.clientSecret;

			OutputStream outputStream = connection.getOutputStream();
			outputStream.write(body.toString().getBytes());
			outputStream.close();

			BufferedReader bufferedReader = 
		            new BufferedReader(new InputStreamReader(connection.getInputStream()));

			StringBuilder response = new StringBuilder();
			String line = null;
			while ((line = bufferedReader.readLine()) != null) {
				response.append(line);
			}

			bufferedReader.close();

//			System.out.println(response.toString());
			parseJsonResponse(response.toString());

		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (connection != null)
				connection.disconnect();
		}
	}

	public void obtainAccessTokenProducao()
	{
		HttpURLConnection connection = null;
		try {
			/*
			 * Uncomment line below for production
			 */
//			URL url = new URL("https://sts.itau.com.br/api/oauth/token");
//			URL url = new URL("https://qrpix-h.bradesco.com.br/oauth/token");
			URL url = new URL("https://qrpix.bradesco.com.br/oauth/token");
			connection = (HttpURLConnection) url.openConnection();
			connection.addRequestProperty("Content-Type", "application/x-www-form-urlencoded");
			connection.setRequestMethod("POST");
			connection.setDoOutput(true);

			// Add certificate
//			File p12 = new File("\\Dir\\file.p12");
//			String p12password = "YOUR_P12_PASSWORD";
			if(this.certificateP12==null)
			{
				System.out.println("Certificate null!");
				System.exit(1);
			}
			InputStream keyInput = new FileInputStream(this.certificateP12);

			KeyStore keyStore = KeyStore.getInstance("PKCS12");
			keyStore.load(keyInput, this.passwdP12.toCharArray());
			keyInput.close();

			KeyManagerFactory keyManagerFactory = KeyManagerFactory.getInstance("SunX509");
			keyManagerFactory.init(keyStore, this.passwdP12.toCharArray());

			SSLContext context = SSLContext.getInstance("TLS");
			context.init(keyManagerFactory.getKeyManagers(), null, new SecureRandom());

			SSLSocketFactory socketFactory = context.getSocketFactory();
			if (connection instanceof HttpsURLConnection)
				((HttpsURLConnection) connection).setSSLSocketFactory(socketFactory);
			//

			String body = "grant_type=client_credentials"
					+ "&client_id=" + this.clientId 
					+ "&client_secret=" + this.clientSecret;

			OutputStream outputStream = connection.getOutputStream();
			outputStream.write(body.toString().getBytes());
			outputStream.close();

			BufferedReader bufferedReader = 
		            new BufferedReader(new InputStreamReader(connection.getInputStream()));

			StringBuilder response = new StringBuilder();
			String line = null;
			while ((line = bufferedReader.readLine()) != null) {
				response.append(line);
			}

			bufferedReader.close();

//			System.out.println(response.toString());
			parseJsonResponse(response.toString());

		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (connection != null)
				connection.disconnect();
		}
	}

	
	public void obtainAccessToken()
	{
		HttpURLConnection connection = null;
		try {
			/*
			 * Uncomment line below for production
			 */
			URL url = new URL(urlAuth);

			connection = (HttpURLConnection) url.openConnection();
			connection.addRequestProperty("Content-Type", "application/x-www-form-urlencoded");
			connection.setRequestMethod("POST");
			connection.setDoOutput(true);

			// Add certificate
			File p12 = new File("/home/moises/Files/Clients/BMA/Certificado/1001156785.p12");
			String p12password = "TpLink0509";

//			File p12 = this.certificateP12;
//			String p12password = this.passwdP12;

			InputStream keyInput = new FileInputStream(p12);

			KeyStore keyStore = KeyStore.getInstance("PKCS12");
			keyStore.load(keyInput, p12password.toCharArray());
			keyInput.close();

			KeyManagerFactory keyManagerFactory = KeyManagerFactory.getInstance("SunX509");
			keyManagerFactory.init(keyStore, p12password.toCharArray());

			SSLContext context = SSLContext.getInstance("TLS");
			context.init(keyManagerFactory.getKeyManagers(), null, new SecureRandom());

			SSLSocketFactory socketFactory = context.getSocketFactory();
			if (connection instanceof HttpsURLConnection)
				((HttpsURLConnection) connection).setSSLSocketFactory(socketFactory);
			//

			String body = "grant_type=client_credentials"
					+ "&client_id=" + this.clientId 
					+ "&client_secret=" + this.clientSecret;

			OutputStream outputStream = connection.getOutputStream();
			outputStream.write(body.toString().getBytes());
			outputStream.close();

			BufferedReader bufferedReader = 
		            new BufferedReader(new InputStreamReader(connection.getInputStream()));

			StringBuilder response = new StringBuilder();
			String line = null;
			while ((line = bufferedReader.readLine()) != null) {
				response.append(line);
			}

			bufferedReader.close();

			System.out.println(response.toString());
			parseJsonResponse(response.toString());

		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (connection != null)
				connection.disconnect();
		}
	}
	
	private void parseJsonResponse(String response)
	{
		
			JSONParser parser = new JSONParser();
			Object obj;
			try {
				obj = parser.parse(response);
				JSONObject jsonObject = (JSONObject) obj;
				String accessToken = (String) jsonObject.get("access_token");				
				String tokenType = (String) jsonObject.get("token_type");
				long expiresIn = (long) jsonObject.get("expires_in");
				this.accessToken=new AccessToken(accessToken, tokenType, expiresIn, "", "", true);						
			} catch (ParseException e) {
				e.printStackTrace();
			}
	}
	
	

	public String getClientId() {
		return clientId;
	}

	public void setClientId(String clientId) {
		this.clientId = clientId;
	}

	public String getClientSecret() {
		return clientSecret;
	}

	public void setClientSecret(String clientSecret) {
		this.clientSecret = clientSecret;
	}

	public void setAccessToken(AccessToken accessToken) {
		this.accessToken = accessToken;
	}

	public AccessToken getAccessToken() {
		return accessToken;
	}

	public File getCertificateP12() {
		return certificateP12;
	}

	public void setCertificateP12(File certificateP12) {
		this.certificateP12 = certificateP12;
	}

	public String getPasswdP12() {
		return passwdP12;
	}

	public void setPasswdP12(String passwdP12) {
		this.passwdP12 = passwdP12;
	}

	public int getIdAuthentication() {
		return idAuthentication;
	}

	public void setIdAuthentication(int idAuthentication) {
		this.idAuthentication = idAuthentication;
	}
}
