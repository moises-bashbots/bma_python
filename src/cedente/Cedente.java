package cedente;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;

import empresa.Empresa;
import endereco.Endereco;
import mssql.ConnectorMSSQL;
import mysql.ConnectorMariaDB;
import participante.Participante;
import utils.Utils;

public class Cedente {
	private int idCedente=0;
	private Empresa empresa = new Empresa();
	private Participante participante = new Participante();
	private String apelido="";
	private ArrayList<String> email= new ArrayList<>();
	private Endereco endereco=new Endereco();
	private double tarifaProtesto=0;
	private double tarifaBaixa=0;
	private double tarifaAbatimento=0;
	private double taxaMoraRecompra=0;
	private boolean ativo=false;
	private boolean sendEmailBorderoNormalAnalitico=false;
	private boolean sendEmailAditivo=false;
	private boolean sendEmailCartaSacado=false;
	private boolean sendEmailConfirmacaoCheques=false;
	private boolean sendEmailImpressaoDuplicatas=false;
	private boolean sendEmailReciboDeRecompra=false;
	private boolean sendEmailConfirmacaoDeDuplicatas=false;
	private boolean sendEmailBorderoPorSacado=false;
	private boolean sendEmailNotaPromissoria=false;
	private boolean sendEmailDARF=false;
	private boolean sendEmailReciboDoTotal=false;
	private boolean sendEmailBorderoSimplificado=false;
	private boolean sendEmailCartaDeLiberacao=false;
	private boolean sendEmailConfirmacaoDeCheques=false;
	
	public static void main(String[] args) {
		ConnectorMariaDB.connect();
		ConnectorMSSQL.connect();
//		ArrayList<Cedente> cedentesRecadastroEmail = 
	}
	
	public Cedente()
	{
		
	}
	
	public Cedente(Connection connMaria, int idCedente)
	{
		String queryMaria="select * from BMA.cedente where id_cedente="+this.idCedente;
		System.out.println(queryMaria);
		Statement stMaria=null;
		try {
			stMaria=connMaria.createStatement();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		ResultSet rsMaria=null;
		try {
			rsMaria=stMaria.executeQuery(queryMaria);
			while(rsMaria.next())
			{
				this.apelido=rsMaria.getString("apelido");
				this.participante=new Participante(connMaria, rsMaria.getInt("participante_id_participante"));	
				String emailCedente=rsMaria.getString("email");
				this.email=this.emaiStringToArray(emailCedente);
				System.out.println("  -- "+this.apelido+": "+this.participante.getRazaoSocial()+" - "+this.empresa.getRazaoSocial());
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
	public Cedente(Connection connMaria, Connection connMSS, int idCedente, Empresa empresa)
	{
		this.idCedente=idCedente;
		this.empresa=empresa;
		String queryMaria="select * from BMA.cedente where id_cedente="+this.idCedente;
		System.out.println(queryMaria);
		Statement stMaria=null;
		try {
			stMaria=connMaria.createStatement();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		ResultSet rsMaria=null;
		try {
			rsMaria=stMaria.executeQuery(queryMaria);
			while(rsMaria.next())
			{
				this.apelido=rsMaria.getString("apelido");
				this.participante=new Participante(connMaria, rsMaria.getInt("participante_id_participante"));	
				String emailCedente=rsMaria.getString("email");
				this.email=this.emaiStringToArray(emailCedente);
				System.out.println("  -- "+this.apelido+": "+this.participante.getRazaoSocial()+" - "+this.empresa.getRazaoSocial());
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		this.checkTarifasAndEmail(connMSS);
	}
	
	public Cedente(Connection connMaria, Connection connMSS, Empresa empresa, String apelido)
	{
		this.apelido=apelido;
		this.empresa=empresa;
		String queryMaria="select * from BMA.cedente where apelido='"+apelido+"' and empresa_id_empresa="+this.empresa.getIdEmpresa();
		System.out.println(queryMaria);
		Statement stMaria=null;
		try {
			stMaria=connMaria.createStatement();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		ResultSet rsMaria=null;
		try {
			rsMaria=stMaria.executeQuery(queryMaria);
			while(rsMaria.next())
			{
				this.idCedente=rsMaria.getInt("id_cedente");
				this.participante=new Participante(connMaria, rsMaria.getInt("participante_id_participante"));	
				String emailCedente=rsMaria.getString("email");
				this.email=this.emaiStringToArray(emailCedente);
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		if(this.participante.getIdParticipante()==0)
		{
			Statement stMSS=null;
			try {
				stMSS=connMSS.createStatement();
			} catch (SQLException e) {
				e.printStackTrace();
			}
			String queryMSS="select * from BMA.dbo.cedente "
										+ " where apelido='"+this.apelido+"'";
			System.out.println(queryMSS);
			
			ResultSet rsMSS=null;
			try {
				rsMSS=stMSS.executeQuery(queryMSS);
				while(rsMSS.next())
				{
					String razaoSocial=rsMSS.getString("nome");
					String cadastro=rsMSS.getString("cnpj");					
					this.email.add(rsMSS.getString("EMAILCED"));
					this.email.add(rsMSS.getString("EMAIL5"));
					this.participante=new Participante(connMaria, connMSS, cadastro, razaoSocial);
					
				}
			} catch (SQLException e) {
				e.printStackTrace();
			}
			if(this.participante.getIdParticipante()!=0)
			{
				String insertMaria="insert into BMA.cedente (empresa_id_empresa, participante_id_participante, apelido, email)"
												+ " values("
												+ this.empresa.getIdEmpresa()
												+","+this.participante.getIdParticipante()
												+",'"+this.apelido+"'"
												+ ",'"+emailArrayToString()+"')";
				System.out.println(insertMaria);
				try {
					stMaria.executeUpdate(insertMaria);
					Utils.waitv(0.15);
					try {
						rsMaria=stMaria.executeQuery(queryMaria);
						while(rsMaria.next())
						{
							this.idCedente=rsMaria.getInt("id_cedente");
							this.participante=new Participante(connMaria, rsMaria.getInt("participante_id_participante"));
						}
					} catch (SQLException e) {
						e.printStackTrace();
					}

				} catch (SQLException e) {
					e.printStackTrace();
				}
			}			
		}
		this.checkTarifasAndEmail(connMSS);
	}
	
	private String emailArrayToString()
	{
		String emailString="";
		int iEmail=0;
		for(String emailInstance: this.email)
		{
			if(iEmail>0)
			{
				emailString+=","+emailInstance;
			}
			else {
				emailString+=emailInstance;
			}
		}
		return emailString;
	}
	
	private ArrayList<String> emaiStringToArray(String emailString)
	{
		ArrayList<String> emailArray = new ArrayList<>();
		
		String[] emails = emailString.split(",");
		for(int iEmail=0; iEmail<emails.length;iEmail++)
		{
			emailArray.add(emails[iEmail]);
		}
		return emailArray;
	}
	
	public void checkTarifasAndEmail(Connection connMSS)
	{
			Statement stMSS=null;
			try {
				stMSS=connMSS.createStatement();
			} catch (SQLException e) {
				e.printStackTrace();
			}
			String queryMSS="select * from BMA.dbo.cedente "
										+ " where apelido='"+this.apelido+"'";
			System.out.println(queryMSS);
			
			ResultSet rsMSS=null;
			try {
				rsMSS=stMSS.executeQuery(queryMSS);
				while(rsMSS.next())
				{
					this.tarifaProtesto=rsMSS.getDouble("TAR3");
					this.tarifaBaixa=rsMSS.getDouble("TAR4");
					this.tarifaAbatimento=rsMSS.getDouble("TAR6");
					this.taxaMoraRecompra=rsMSS.getDouble("txmorarec");
					String emailCedente=rsMSS.getString("EMAILCED");
					String email1=rsMSS.getString("email1");
					String email2=rsMSS.getString("email2");
					String email3=rsMSS.getString("email3");
					String email4=rsMSS.getString("email4");
					String email5=rsMSS.getString("email5");
					if(emailCedente!=null && emailCedente.length()>0)
					{
						this.email.add(emailCedente);
					}
					if(email1!=null && email1.length()>0)
					{
						this.email.add(email1);
					}
					if(email2!=null && email2.length()>0)
					{
						this.email.add(email2);
					}
					if(email3!=null && email3.length()>0)
					{
						this.email.add(email3);
					}
					if(email4!=null && email4.length()>0)
					{
						this.email.add(email4);
					}
					if(email5!=null && email5.length()>0)
					{
						this.email.add(email5);
					}
					System.out.println("TarifaProtesto: "+this.tarifaProtesto);
					System.out.println("TarifaBaixa: "+this.tarifaBaixa);
					System.out.println("TarifaAbatimento: "+this.tarifaAbatimento);
					System.out.println("TaxaMoraRecompra: "+this.taxaMoraRecompra);
					System.out.println("Emails:");
					for(String e:this.email)
					{
						System.out.println("-"+e);
					}
				}
			} catch (SQLException e) {
				e.printStackTrace();
			}
	}

	public void checkSendingEmails(Connection connMSS)
	{

			Statement stMSS=null;
			try {
				stMSS=connMSS.createStatement();
			} catch (SQLException e) {
				e.printStackTrace();
			}
			String queryMSS="select * from BMA.dbo.cedente "
										+ " where apelido='"+this.apelido+"'";
			System.out.println(queryMSS);
			
			ResultSet rsMSS=null;
			try {
				rsMSS=stMSS.executeQuery(queryMSS);
				while(rsMSS.next())
				{
					if(rsMSS.getInt("em0")==1)
					{
						this.setSendEmailBorderoNormalAnalitico(true);
					}

					if(rsMSS.getInt("em1")==1)
					{
						this.setSendEmailAditivo(true);
					}
					if(rsMSS.getInt("em2")==1)
					{
						this.setSendEmailCartaSacado(true);
					}
					if(rsMSS.getInt("em3")==1)
					{
						this.setSendEmailConfirmacaoDeDuplicatas(true);
					}
					if(rsMSS.getInt("em4")==1)
					{
						this.setSendEmailBorderoPorSacado(true);
					}
					if(rsMSS.getInt("em5")==1)
					{
						this.setSendEmailNotaPromissoria(true);
					}
					if(rsMSS.getInt("em6")==1)
					{
						this.setSendEmailDARF(true);
					}
					if(rsMSS.getInt("em7")==1)
					{
						this.setSendEmailReciboDoTotal(true);
					}
					if(rsMSS.getInt("em8")==1)
					{
						this.setSendEmailBorderoSimplificado(true);
					}
					if(rsMSS.getInt("em9")==1)
					{
						this.setSendEmailCartaDeLiberacao(true);
					}
					if(rsMSS.getInt("em10")==1)
					{
						this.setSendEmailConfirmacaoDeCheques(true);
					}
					if(rsMSS.getInt("em11")==1)
					{
						this.setSendEmailImpressaoDuplicatas(true);
					}
					if(rsMSS.getInt("em12")==1)
					{
						this.setSendEmailConfirmacaoDeDuplicatas(true);
					}

				}
			} catch (SQLException e) {
				e.printStackTrace();
			}
			System.out.println("sendEmailAditivo: "+this.sendEmailAditivo);
			System.out.println("sendEmailCartaSacado: "+this.sendEmailCartaSacado);
			System.out.println("sendEmailConfirmacaoCheques: "+this.sendEmailConfirmacaoCheques);
			System.out.println("sendEmailImpressaoDuplicatas: "+this.sendEmailImpressaoDuplicatas);
			System.out.println("sendEmailReciboDeRecompra: "+this.sendEmailReciboDeRecompra);
			System.out.println("sendEmailConfirmacaoDeDuplicatas: "+this.sendEmailConfirmacaoDeDuplicatas);
			System.out.println("sendEmailBorderoPorSacado: "+this.sendEmailBorderoPorSacado);
			System.out.println("sendEmailNotaPromissoria: "+this.sendEmailNotaPromissoria);
			System.out.println("sendEmailDARF: "+this.sendEmailDARF);
			System.out.println("sendEmailReciboDoTotal: "+this.sendEmailReciboDoTotal);
			System.out.println("sendEmailBorderoSimplificado: "+this.sendEmailBorderoSimplificado);
			System.out.println("sendEmailCartaDeLiberacao: "+this.sendEmailCartaDeLiberacao);
			System.out.println("sendEmailConfirmacaoDeCheques: "+this.sendEmailConfirmacaoDeCheques);
	}

	
	public void showTarifas()
	{
		System.out.println("TarifaProtesto: "+this.tarifaProtesto);
		System.out.println("TarifaBaixa: "+this.tarifaBaixa);
		System.out.println("TarifaAbatimento: "+this.tarifaAbatimento);
		System.out.println("TaxaMoraRecompra: "+this.taxaMoraRecompra);
	}
	
	
	public static HashMap<String, HashMap<String,Cedente>> cedentesPorEmpresa(Connection connMaria, Connection connMSS, HashMap<String, Empresa> empresas)
	{
		String queryMaria="select * from BMA.cedente";
		
		HashMap<String, HashMap<String,Cedente>> cedentesPorEmpresa = new HashMap<String, HashMap<String,Cedente>>();
		HashMap<Integer, Empresa> empresasById = new HashMap<>();
		for(String key:empresas.keySet())
		{
			empresasById.put(empresas.get(key).getIdEmpresa(), empresas.get(key));
		}
		
		Statement st = null;
		
		try {
			st=connMaria.createStatement();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		
		ResultSet rs=null;
		try {
			rs=st.executeQuery(queryMaria);
			while(rs.next())
			{		
				int idEmpresa=rs.getInt("empresa_id_empresa");
				if(empresasById.get(idEmpresa)==null)
				{
					continue;
				}
				else
				{						
					if(cedentesPorEmpresa.get(empresasById.get(idEmpresa).getApelido())==null)
					{					
						Cedente cedente = new Cedente(connMaria, connMSS, rs.getInt("id_cedente"), empresasById.get(idEmpresa));
						HashMap<String, Cedente> cedentes = new HashMap<>();
						cedentes.put(cedente.apelido, cedente);
						cedentesPorEmpresa.put(empresasById.get(idEmpresa).getApelido(), cedentes);
					}
					else
					{
						Cedente cedente = new Cedente(connMaria, connMSS, rs.getInt("id_cedente"), empresasById.get(idEmpresa));
						cedentesPorEmpresa.get(empresasById.get(idEmpresa).getApelido()).put(cedente.getApelido(), cedente);
					}
				}
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return cedentesPorEmpresa;
	}
	
	public int getIdCedente() {
		return idCedente;
	}
	public void setIdCedente(int idCedente) {
		this.idCedente = idCedente;
	}
	public Empresa getEmpresa() {
		return empresa;
	}
	public void setEmpresa(Empresa empresa) {
		this.empresa = empresa;
	}
	public Participante getParticipante() {
		return participante;
	}
	public void setParticipante(Participante participante) {
		this.participante = participante;
	}
	public String getApelido() {
		return apelido;
	}
	public void setApelido(String apelido) {
		this.apelido = apelido;
	}
	public Endereco getEndereco() {
		return endereco;
	}
	public void setEndereco(Endereco endereco) {
		this.endereco = endereco;
	}
	public boolean isAtivo() {
		return ativo;
	}
	public void setAtivo(boolean ativo) {
		this.ativo = ativo;
	}

	public double getTarifaBaixa() {
		return tarifaBaixa;
	}

	public void setTarifaBaixa(double tarifaBaixa) {
		this.tarifaBaixa = tarifaBaixa;
	}

	public double getTarifaProtesto() {
		return tarifaProtesto;
	}

	public void setTarifaProtesto(double tarifaProtesto) {
		this.tarifaProtesto = tarifaProtesto;
	}

	public double getTarifaAbatimento() {
		return tarifaAbatimento;
	}

	public void setTarifaAbatimento(double tarifaAbatimento) {
		this.tarifaAbatimento = tarifaAbatimento;
	}

	public double getTaxaMoraRecompra() {
		return taxaMoraRecompra;
	}

	public void setTaxaMoraRecompra(double taxaMoraRecompra) {
		this.taxaMoraRecompra = taxaMoraRecompra;
	}

	public boolean isSendEmailAditivo() {
		return sendEmailAditivo;
	}

	public void setSendEmailAditivo(boolean sendEmailAditivo) {
		this.sendEmailAditivo = sendEmailAditivo;
	}

	public boolean isSendEmailCartaSacado() {
		return sendEmailCartaSacado;
	}

	public void setSendEmailCartaSacado(boolean sendEmailCartaSacado) {
		this.sendEmailCartaSacado = sendEmailCartaSacado;
	}

	public boolean isSendEmailConfirmacaoCheques() {
		return sendEmailConfirmacaoCheques;
	}

	public void setSendEmailConfirmacaoCheques(boolean sendEmailConfirmacaoCheques) {
		this.sendEmailConfirmacaoCheques = sendEmailConfirmacaoCheques;
	}

	public boolean isSendEmailImpressaoDuplicatas() {
		return sendEmailImpressaoDuplicatas;
	}

	public void setSendEmailImpressaoDuplicatas(boolean sendEmailImpressaoDuplicatas) {
		this.sendEmailImpressaoDuplicatas = sendEmailImpressaoDuplicatas;
	}

	public boolean isSendEmailReciboDeRecompra() {
		return sendEmailReciboDeRecompra;
	}

	public void setSendEmailReciboDeRecompra(boolean sendEmailReciboDeRecompra) {
		this.sendEmailReciboDeRecompra = sendEmailReciboDeRecompra;
	}

	public boolean isSendEmailConfirmacaoDeDuplicatas() {
		return sendEmailConfirmacaoDeDuplicatas;
	}

	public void setSendEmailConfirmacaoDeDuplicatas(boolean sendEmailConfirmacaoDeDuplicatas) {
		this.sendEmailConfirmacaoDeDuplicatas = sendEmailConfirmacaoDeDuplicatas;
	}

	public boolean isSendEmailBorderoPorSacado() {
		return sendEmailBorderoPorSacado;
	}

	public void setSendEmailBorderoPorSacado(boolean sendEmailBorderoPorSacado) {
		this.sendEmailBorderoPorSacado = sendEmailBorderoPorSacado;
	}

	public boolean isSendEmailNotaPromissoria() {
		return sendEmailNotaPromissoria;
	}

	public void setSendEmailNotaPromissoria(boolean sendEmailNotaPromissoria) {
		this.sendEmailNotaPromissoria = sendEmailNotaPromissoria;
	}

	public boolean isSendEmailDARF() {
		return sendEmailDARF;
	}

	public void setSendEmailDARF(boolean sendEmailDARF) {
		this.sendEmailDARF = sendEmailDARF;
	}

	public boolean isSendEmailReciboDoTotal() {
		return sendEmailReciboDoTotal;
	}

	public void setSendEmailReciboDoTotal(boolean sendEmailReciboDoTotal) {
		this.sendEmailReciboDoTotal = sendEmailReciboDoTotal;
	}

	public boolean isSendEmailBorderoSimplificado() {
		return sendEmailBorderoSimplificado;
	}

	public void setSendEmailBorderoSimplificado(boolean sendEmailBorderoSimplificado) {
		this.sendEmailBorderoSimplificado = sendEmailBorderoSimplificado;
	}

	public boolean isSendEmailCartaDeLiberacao() {
		return sendEmailCartaDeLiberacao;
	}

	public void setSendEmailCartaDeLiberacao(boolean sendEmailCartaDeLiberacao) {
		this.sendEmailCartaDeLiberacao = sendEmailCartaDeLiberacao;
	}

	public boolean isSendEmailConfirmacaoDeCheques() {
		return sendEmailConfirmacaoDeCheques;
	}

	public void setSendEmailConfirmacaoDeCheques(boolean sendEmailConfirmacaoDeCheques) {
		this.sendEmailConfirmacaoDeCheques = sendEmailConfirmacaoDeCheques;
	}

	public ArrayList<String> getEmail() {
		return this.email;
	}

	public void setEmail(ArrayList<String> email) {
		this.email = email;
	}

	public boolean isSendEmailBorderoNormalAnalitico() {
		return this.sendEmailBorderoNormalAnalitico;
	}

	public void setSendEmailBorderoNormalAnalitico(boolean sendEmailBorderoNormalAnalitico) {
		this.sendEmailBorderoNormalAnalitico = sendEmailBorderoNormalAnalitico;
	}
	
	
	
}
