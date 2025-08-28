package email;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.SimpleDateFormat;
import java.util.Date;

import bradesco.CobrancaPIX;

public class EmailCobrancaInstrucao {
	private int idEmailCobrancaInstrucao=0;
	private CobrancaPIX cobrancaPIX=new CobrancaPIX();
	private boolean enviado=false;
	private Date horaEnvio=null;
	private String de="";
	private String para="";
	private String assunto="";
	private String corpo="";
	private static SimpleDateFormat sdfm = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	
	public EmailCobrancaInstrucao()
	{
		
	}
	
	public EmailCobrancaInstrucao(Connection connMaria, CobrancaPIX cobrancaPIX, boolean enviado, Date horaEnvio, String de, String para, String assunto, String corpo)	
	{
		this.cobrancaPIX=cobrancaPIX;
		this.enviado=enviado;
		this.horaEnvio=horaEnvio;
		this.de=de;
		this.para=para;
		this.assunto=assunto;
		this.corpo=corpo;
		String query="select * from BMA.email_cobranca_instrucao where cobranca_pix_id_cobranca_pix="+this.cobrancaPIX.getIdCobrancaPIX();
		Statement st=null;
		try {
			st=connMaria.createStatement();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		ResultSet rs = null;
		try {
			rs=st.executeQuery(query);
			while(rs.next())
			{
				this.idEmailCobrancaInstrucao=rs.getInt("id_email_cobranca_instrucao");
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		if(this.idEmailCobrancaInstrucao==0)
		{
			int enviadoInt=0;
			if(enviado)
			{
				enviadoInt=1;
			}
			String insert="insert into BMA.email_cobranca_instrucao "
									+ "(cobranca_pix_id_cobranca_pix, enviado, hora_envio, de, para, assunto, corpo)"
									+ " values("
									+ this.cobrancaPIX.getIdCobrancaPIX()
									+ ","+enviadoInt
									+ ",'"+sdfm.format(this.horaEnvio)+"'"
									+ ",'"+this.de+"'"
									+ ",'"+this.para+"'"
									+ ",'"+this.assunto+"'"
									+ ",'"+this.corpo+"'"
									+ ")";
			System.out.println(insert);
			try {
				st.executeUpdate(insert);
				try {
					rs=st.executeQuery(query);
					while(rs.next())
					{
						this.idEmailCobrancaInstrucao=rs.getInt("id_email_cobranca_instrucao");
					}
				} catch (SQLException e) {
					e.printStackTrace();
				}

			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
	}
	
	
	public EmailCobrancaInstrucao(Connection connMaria, CobrancaPIX cobrancaPIX)	
	{
		this.cobrancaPIX=cobrancaPIX;
		String query="select * from BMA.email_cobranca_instrucao where cobranca_pix_id_cobranca_pix="+this.cobrancaPIX.getIdCobrancaPIX();
		Statement st=null;
		try {
			st=connMaria.createStatement();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		ResultSet rs = null;
		try {
			rs=st.executeQuery(query);
			while(rs.next())
			{
				this.idEmailCobrancaInstrucao=rs.getInt("id_email_cobranca_instrucao");
				int enviadoInt=rs.getInt("enviado");
				if(enviadoInt==1)
				{
					this.enviado=true;
				}
				this.horaEnvio=rs.getTime("hora_envio");
				this.de=rs.getString("de");
				this.para=rs.getString("para");
				this.assunto=rs.getString("assunto");
				this.corpo=rs.getString("corpo");
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
	public int getIdEmailCobrancaInstrucao() {
		return idEmailCobrancaInstrucao;
	}
	public void setIdEmailCobrancaInstrucao(int idEmailCobrancaInstrucao) {
		this.idEmailCobrancaInstrucao = idEmailCobrancaInstrucao;
	}
	public CobrancaPIX getCobrancaPIX() {
		return cobrancaPIX;
	}
	public void setCobrancaPIX(CobrancaPIX cobrancaPIX) {
		this.cobrancaPIX = cobrancaPIX;
	}
	public boolean isEnviado() {
		return enviado;
	}
	public void setEnviado(boolean enviado) {
		this.enviado = enviado;
	}
	public Date getHoraEnvio() {
		return horaEnvio;
	}
	public void setHoraEnvio(Date horaEnvio) {
		this.horaEnvio = horaEnvio;
	}
	public String getDe() {
		return de;
	}
	public void setDe(String de) {
		this.de = de;
	}
	public String getPara() {
		return para;
	}
	public void setPara(String para) {
		this.para = para;
	}
	public String getAssunto() {
		return assunto;
	}
	public void setAssunto(String assunto) {
		this.assunto = assunto;
	}
	public String getCorpo() {
		return corpo;
	}
	public void setCorpo(String corpo) {
		this.corpo = corpo;
	}
	
	
}
