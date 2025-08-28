package bradesco;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import utils.Utils;

public class PIX {
	private int idPix=0;
	private Date horario=null;
	private double valor=0;
	private String txid="";
	private String chave="";
	private String endToEndId="";
	private String infoPagador="";
	private boolean registrado=false;
	private static SimpleDateFormat sdfp=new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
	private static SimpleDateFormat sdfh=new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

	
	public PIX()
	{
		
	}
	
	public PIX(Date horario, double valor, String txid, String endToEndId)
	{
		this.horario=horario;
		this.valor=valor;
		this.txid=txid;
		this.endToEndId=endToEndId;
	}

	public PIX(int idPix, Date horario, double valor, String txid, String endToEndId)
	{
		this.idPix=idPix;
		this.horario=horario;
		this.valor=valor;
		this.txid=txid;
		this.endToEndId=endToEndId;
	}

	public PIX(Connection connMaria, int idCobrancaPIX, Date horario, double valor, String chave, String infoPagador, String endToEndId)
	{		
		this.horario=horario;
		this.valor=valor;
		this.chave=chave;
		this.infoPagador=infoPagador;
		this.endToEndId=endToEndId;
		
		String query="select * from BMA.pix"
							+ " where cobranca_pix_id_cobranca_pix="+idCobrancaPIX
							+" and end_to_end_id='"+this.endToEndId+"'"
							+" and valor="+this.valor;
		
		System.out.println(query);
		Statement st=null;
		try {
			st=connMaria.createStatement();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		ResultSet rs=null;
		try {
			rs=st.executeQuery(query);
			while(rs.next())
			{
				this.idPix=rs.getInt("id_pix");
				int registradoInt=rs.getInt("registrado");
				if(registradoInt==1)
				{
					this.setRegistrado(true);
				}
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		if(this.idPix==0)
		{
			String insert="insert into BMA.pix (cobranca_pix_id_cobranca_pix, horario, valor, info_pagador, end_to_end_id)"
					+ " values("
					+ idCobrancaPIX
					+ ",'"+sdfp.format(this.horario)+"'"
					+ ","+this.valor
					+ ",'"+this.infoPagador+"'"
					+ ",'"+this.endToEndId+"')";
			System.out.println(insert);
			try {
				st.executeUpdate(insert);
			} catch (SQLException e) {
				e.printStackTrace();
			}
			Utils.waitv(0.25);
			
			try {
				rs=st.executeQuery(query);
				System.out.println(query);
				while(rs.next())
				{
					this.idPix=rs.getInt("id_pix");
				}
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
	}
	
	public PIX(Connection connMaria, int idCobrancaPIX, Date horario, double valor, String txid, String endToEndId)
	{
		this.horario=horario;
		this.valor=valor;
		this.txid=txid;
		this.endToEndId=endToEndId;
		
		String query="select * from BMA.pix"
							+ " where cobranca_pix_id_cobranca_pix="+idCobrancaPIX
							+" and txid='"+this.txid+"'"
							+" and end_to_end_id='"+this.endToEndId+"'"
							+" and valor="+this.valor;
							;
		System.out.println(query);
		Statement st=null;
		try {
			st=connMaria.createStatement();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		ResultSet rs=null;
		try {
			rs=st.executeQuery(query);
			while(rs.next())
			{
				this.idPix=rs.getInt("id_pix");
				int registradoInt=rs.getInt("registrado");
				if(registradoInt==1)
				{
					this.setRegistrado(true);
				}
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		if(this.idPix==0)
		{
			String insert="insert into BMA.pix (cobranca_pix_id_cobranca_pix, horario, valor, txid, end_to_end_id)"
					+ " values("
					+ idCobrancaPIX
					+ ",'"+sdfp.format(this.horario)+"'"
					+ ","+this.valor
					+ ",'"+this.txid+"'"
					+ ",'"+this.endToEndId+"')";
			System.out.println(insert);
			try {
				st.executeUpdate(insert);
			} catch (SQLException e) {
				e.printStackTrace();
			}
			Utils.waitv(0.25);
			
			try {
				rs=st.executeQuery(query);
				System.out.println(query);
				while(rs.next())
				{
					this.idPix=rs.getInt("id_pix");
				}
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
	}
	
	public void updateRegistrado(Connection connMaria, boolean registrado)
	{
		this.registrado=registrado;
		int registradoInt=0;
		if(this.registrado)
		{
			registradoInt=1;
		}
		Statement st=null;
		try {
			st=connMaria.createStatement();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		String update="update BMA.pix set registrado="+registradoInt+" where id_pix="+this.idPix;
		try {
			st.executeUpdate(update);
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
	public static ArrayList<PIX> getPixes(Connection connMaria, CobrancaPIX cobrancaPIX)
	{
		ArrayList<PIX> pixes = new ArrayList<>();
		String query="select * from BMA.pix where cobranca_pix_id_cobranca_pix="+cobrancaPIX.getIdCobrancaPIX();
		System.out.println(query);
		Statement st=null;
		try {
			st=connMaria.createStatement();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		ResultSet rs=null;
		try {
			rs=st.executeQuery(query);
			while(rs.next())
			{
				int idPix=rs.getInt("id_pix");
				Date horario = rs.getTime("horario");
				double valor=rs.getDouble("valor");
				String txid=rs.getString("txid");				
				String endToEndId=rs.getString("end_to_end_id");
				
				PIX pix = new PIX(idPix,horario, valor, txid, endToEndId);
				pixes.add(pix);
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}		
		return pixes;
	}
	
	public void show()
	{
		System.out.println("---- PIX ----");
		System.out.println("idPix:" +this.idPix);
		System.out.println("PIX Pagamento");
		System.out.println("Horario: "+this.horario);
		System.out.println("txid: "+this.txid);
		System.out.println("EndToEndId: "+this.endToEndId);
		System.out.println("Registrado: "+this.registrado);
		System.out.println("---- END PIX ----");
	}
	
	public Date getHorario() {
		return horario;
	}
	public void setHorario(Date horario) {
		this.horario = horario;
	}
	public double getValor() {
		return valor;
	}
	public void setValor(double valor) {
		this.valor = valor;
	}
	public String getTxid() {
		return txid;
	}
	public void setTxid(String txid) {
		this.txid = txid;
	}
	public String getEndToEndId() {
		return endToEndId;
	}
	public void setEndToEndId(String endToEndId) {
		this.endToEndId = endToEndId;
	}

	public int getIdPix() {
		return idPix;
	}

	public void setIdPix(int idPix) {
		this.idPix = idPix;
	}

	public boolean isRegistrado() {
		return registrado;
	}

	public void setRegistrado(boolean registrado) {
		this.registrado = registrado;
	}

	public static SimpleDateFormat getSdfp() {
		return sdfp;
	}

	public static void setSdfp(SimpleDateFormat sdfp) {
		PIX.sdfp = sdfp;
	}

	public String getInfoPagador() {
		return this.infoPagador;
	}

	public void setInfoPagador(String infoPagador) {
		this.infoPagador = infoPagador;
	}

	public String getChave() {
		return this.chave;
	}

	public void setChave(String chave) {
		this.chave = chave;
	}
}
