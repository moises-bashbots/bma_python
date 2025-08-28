package empresa;

import java.io.File;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import banco.Banco;

public class DadosBancariosEmpresa {
	private int idDadosBancariosEmpresa=0;
	private String apelido="";
	private Banco banco = new Banco();	
	private String agencia="";
	private String digitoAgencia="";
	private String conta="";
	private String digitoConta="";
	private String chavePIX="";
	private String clientId="";
	private String clientSecret="";
	private File certificateP12 = null;
	private String passwdP12 = "";
	private String codigoPreImpresso="";
	private String codigoConvencional="";
	
	public DadosBancariosEmpresa()
	{
		
	}
	
	public DadosBancariosEmpresa(Connection connMaria, Connection connMSS, int idDadosBancariosEmpresa, String apelidoEmpresa, boolean self)
	{
		Statement st=null;
		try {
			st=connMaria.createStatement();
		} catch (SQLException e) {
			e.printStackTrace();
		}

		String queryMaria="select * from BMA.dados_bancarios_empresa where id_dados_bancarios_empresa="+this.idDadosBancariosEmpresa;
		System.out.println(queryMaria);
		ResultSet rs = null;
		try {
			rs=st.executeQuery(queryMaria);
			while(rs.next())
			{
				this.banco=new Banco(connMaria, rs.getInt("banco_id_banco"));
				this.agencia=rs.getString("agencia");
				this.digitoAgencia=rs.getString("digito_agencia");
				this.conta=rs.getString("conta");
				this.digitoConta=rs.getString("digito_conta");
				this.codigoPreImpresso=rs.getString("codigo_pre_impresso");
				this.codigoConvencional=rs.getString("codigo_convencional");
				this.chavePIX=rs.getString("chave_pix");					
				this.clientId=rs.getString("client_id");
				this.clientSecret=rs.getString("client_secret");
				String pathFileP12 = rs.getString("path_certificate");
				this.certificateP12 = new File(pathFileP12);
				this.passwdP12 = rs.getString("password_certificate");
				this.apelido=rs.getString("apelido");
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}		
		String stringAgencia=(this.agencia+"-"+this.digitoAgencia).trim();
		String stringConta=(this.conta+"-"+this.digitoConta).trim();
		String queryMSS="select * from BMA.dbo.bancos"
										+ " where empresa='"+apelidoEmpresa+"'"
										+ " and codigo='"+this.banco.getCodigoCompe()+"'"
										+ " and nagencia='"+stringAgencia+"'"
										+ " and conta='"+stringConta+"'";
		System.out.println(queryMSS);
		Statement stMSS=null;
		try {
			stMSS=connMSS.createStatement();
		} catch (SQLException e) {
			e.printStackTrace();
		}

		ResultSet rsMSS=null;
		String apelidoConta="";
		try {
			rsMSS=stMSS.executeQuery(queryMSS);
			while(rsMSS.next())
			{
				apelidoConta=rsMSS.getString("apelido");
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		
		if(this.apelido.contains(apelidoConta) && this.apelido.length()==apelidoConta.length())
		{
			System.out.println("Apelido correto: "+this.apelido);
		}
		else
		{
			System.out.println("Apelido atualizado: "+apelidoConta);
			this.updateApelido(connMaria, apelidoConta);
		}
	}
	
	public DadosBancariosEmpresa(Connection connMaria, Connection connMSS, int idEmpresa, String apelidoEmpresa)
	{
		
		Statement st=null;
		try {
			st=connMaria.createStatement();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		
		String query="select * from BMA.dados_bancarios_empresa where empresa_id_empresa="+idEmpresa;
		ResultSet rs = null;
		try {
			rs=st.executeQuery(query);
			while(rs.next())
			{
				this.idDadosBancariosEmpresa=rs.getInt("id_dados_bancarios_empresa");
				this.banco=new Banco(connMaria, rs.getInt("banco_id_banco"));
				this.agencia=rs.getString("agencia");
				this.apelido=rs.getString("apelido");
				this.digitoAgencia=rs.getString("digito_agencia");
				this.conta=rs.getString("conta");
				this.digitoConta=rs.getString("digito_conta");
				this.codigoPreImpresso=rs.getString("codigo_pre_impresso");
				this.codigoConvencional=rs.getString("codigo_convencional");
				this.chavePIX=rs.getString("chave_pix");			
				this.clientId=rs.getString("client_id");
				this.clientSecret=rs.getString("client_secret");
				String pathFileP12 = rs.getString("path_certificate");
				this.certificateP12 = new File(pathFileP12);
				this.passwdP12 = rs.getString("password_certificate");
				
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}		
		
//		String stringAgencia=(this.agencia+"-"+this.digitoAgencia).trim();
//		String stringConta=(this.conta+"-"+this.digitoConta).trim();
//		String queryMSS="select * from BMA.dbo.bancos"
//										+ " where empresa='"+apelidoEmpresa+"'"
//										+ " and codigo='"+this.banco.getCodigoCompe()+"'"
//										+ " and nagencia='"+stringAgencia+"'"
//										+ " and conta='"+stringConta+"'";
//		System.out.println(queryMSS);
//		Statement stMSS=null;
//		try {
//			stMSS=connMSS.createStatement();
//		} catch (SQLException e) {
//			e.printStackTrace();
//		}
//
//		ResultSet rsMSS=null;
//		String apelidoConta="";
//		try {
//			rsMSS=stMSS.executeQuery(queryMSS);
//			while(rsMSS.next())
//			{
//				apelidoConta=rsMSS.getString("apelido");
//			}
//		} catch (SQLException e) {
//			e.printStackTrace();
//		}
//		
//		if(this.apelido.contains(apelidoConta) && this.apelido.length()==apelidoConta.length())
//		{
//			System.out.println("Apelido correto: "+this.apelido);
//		}
//		else
//		{
//			System.out.println("Apelido atualizado: "+apelidoConta);
//			this.updateApelido(connMaria, apelidoConta);
//		}
	}
	
	
	public void updateApelido(Connection connMaria, String apelido)
	{
		this.apelido=apelido;
		Statement st=null;
		try {
			st=connMaria.createStatement();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		String update = "update BMA.dados_bancarios_empresa set apelido='"+this.apelido+"' where id_dados_bancarios_empresa="+this.idDadosBancariosEmpresa;
		try {
			st.executeUpdate(update);
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
	public void show()
	{
		System.out.println("++++++++++++++++++++++++++++++++++++++++++++++++++");
		System.out.println("Dados bancarios da empresa: ");
		System.out.println("idDadosBancariosEmpresa: "+this.idDadosBancariosEmpresa);
		System.out.println("Banco: "+this.banco.getCodigoCompe());
		System.out.println("Agencia: "+this.agencia);
		System.out.println("Conta: "+this.conta+"-"+this.digitoConta);
		System.out.println("ApelidoConta: "+this.apelido);
		System.out.println("CodigoPreImpresso: "+this.codigoPreImpresso+"   CodigoConvencional: " + this.codigoConvencional);
		if(this.certificateP12!=null)
		{
			System.out.println("Certificado: "+this.certificateP12.getAbsolutePath());
			System.out.println("ClientId: "+this.clientId);
			System.out.println("ClientSecret: "+this.clientSecret);
		}
		System.out.println("++++++++++++++++++++++++++++++++++++++++++++++++++");
	}
	
	public int getIdDadosBancariosEmpresa() {
		return idDadosBancariosEmpresa;
	}
	public void setIdDadosBancariosEmpresa(int idDadosBancariosEmpresa) {
		this.idDadosBancariosEmpresa = idDadosBancariosEmpresa;
	}
	public Banco getBanco() {
		return banco;
	}
	public void setBanco(Banco banco) {
		this.banco = banco;
	}
	public String getAgencia() {
		return agencia;
	}
	public void setAgencia(String agencia) {
		this.agencia = agencia;
	}
	public String getDigitoAgencia() {
		return digitoAgencia;
	}
	public void setDigitoAgencia(String digitoAgencia) {
		this.digitoAgencia = digitoAgencia;
	}
	public String getConta() {
		return conta;
	}
	public void setConta(String conta) {
		this.conta = conta;
	}
	public String getDigitoConta() {
		return digitoConta;
	}
	public void setDigitoConta(String digitoConta) {
		this.digitoConta = digitoConta;
	}

	public String getChavePIX() {
		return chavePIX;
	}

	public void setChavePIX(String chavePIX) {
		this.chavePIX = chavePIX;
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

	public String getApelido() {
		return apelido;
	}

	public void setApelido(String apelido) {
		this.apelido = apelido;
	}

	public String getCodigoPreImpresso() {
		return this.codigoPreImpresso;
	}

	public void setCodigoPreImpresso(String codigoPreImpresso) {
		this.codigoPreImpresso = codigoPreImpresso;
	}

	public String getCodigoConvencional() {
		return this.codigoConvencional;
	}

	public void setCodigoConvencional(String codigoConvencional) {
		this.codigoConvencional = codigoConvencional;
	}
	

}
