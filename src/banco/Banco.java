package banco;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class Banco {
	private int idBanco=0;
	private String nomeBanco="";
	private String codigoCompe="";
	private String codigoISPB="";
	
	public Banco()
	{
		
	}

	public Banco(Connection connMaria, int idBanco)
	{
		this.idBanco=idBanco;
		Statement st=null;
		try {
			st=connMaria.createStatement();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		String query="select * from BMA.banco where id_banco="+this.idBanco;
		ResultSet rs=null;
		try {
			rs=st.executeQuery(query);
			while(rs.next())
			{
				this.nomeBanco=rs.getString("nome_banco");
				this.codigoCompe=rs.getString("codigo_compe");
				this.codigoISPB=rs.getString("codigo_ispb");
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
	public int getIdBanco() {
		return idBanco;
	}

	public void setIdBanco(int idBanco) {
		this.idBanco = idBanco;
	}

	public String getNomeBanco() {
		return nomeBanco;
	}

	public void setNomeBanco(String nomeBanco) {
		this.nomeBanco = nomeBanco;
	}

	public String getCodigoCompe() {
		return codigoCompe;
	}

	public void setCodigoCompe(String codigoCompe) {
		this.codigoCompe = codigoCompe;
	}

	public String getCodigoISPB() {
		return codigoISPB;
	}

	public void setCodigoISPB(String codigoISPB) {
		this.codigoISPB = codigoISPB;
	}

}
