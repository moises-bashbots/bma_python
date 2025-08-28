package instrucao;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import utils.Utils;

public class TipoInstrucao {
	private int idTipoInstrucao=0;
	private String tipoInstrucao="";
	
	public TipoInstrucao()
	{
		
	}
	
	public TipoInstrucao(Connection connMaria, String tipoInstrucao)
	{
		this.tipoInstrucao=tipoInstrucao.toUpperCase();
		String query="select * from BMA.tipo_instrucao where tipo_instrucao='"+this.tipoInstrucao+"'";
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
				this.idTipoInstrucao=rs.getInt("id_tipo_instrucao");
			}			
		} catch (SQLException e) {
			e.printStackTrace();
		}
		if(this.idTipoInstrucao==0)
		{
			String insert="insert into BMA.tipo_instrucao (tipo_instrucao) values ('"+this.tipoInstrucao+"')";
			try {
				st.executeUpdate(insert);
				Utils.waitv(0.25);
				try {
					rs=st.executeQuery(query);
					while(rs.next())
					{
						this.idTipoInstrucao=rs.getInt("id_tipo_instrucao");
					}			
				} catch (SQLException e) {
					e.printStackTrace();
				}
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}		
	}
	
	public TipoInstrucao(Connection connMaria, int idTipoInstrucao)
	{
		this.idTipoInstrucao=idTipoInstrucao;
		String query="select * from BMA.tipo_instrucao where id_tipo_instrucao="+this.idTipoInstrucao;
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
				this.tipoInstrucao=rs.getString("tipo_instrucao");
			}			
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	

	public int getIdTipoInstrucao() {
		return idTipoInstrucao;
	}

	public void setIdTipoInstrucao(int idTipoInstrucao) {
		this.idTipoInstrucao = idTipoInstrucao;
	}

	public String getTipoInstrucao() {
		return tipoInstrucao;
	}

	public void setTipoInstrucao(String tipoInstrucao) {
		this.tipoInstrucao = tipoInstrucao;
	}
	
	
}
