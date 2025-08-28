package conta_grafica;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import utils.Utils;

public class TipoCritica {
	private int idTipoCritica=0;
	private String apelido="";
	private String nomeCritica="";
	private String descricaoCritica="";
	
	public TipoCritica()
	{
		
	}
	
	public TipoCritica(Connection connMaria, String apelido, String nomeCritica, String descricaoCritica)
	{
		this.apelido=apelido;
		this.nomeCritica=nomeCritica;
		this.descricaoCritica=descricaoCritica;
		String query="select * from BMA.tipo_critica "
							+ " where"
							+ " apelido='"+this.apelido+"'"
							+ " and  nome_critica='"+this.nomeCritica+"'"
							+ " and descricao_critica='"+this.descricaoCritica+"'";
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
				this.idTipoCritica=rs.getInt("id_tipo_critica");
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		if(this.idTipoCritica==0)
		{
			String insert="insert into BMA.tipo_critica (apelido, nome_critica, descricao_critica)"
								+ " values("
								+ "'"+this.apelido+"'"
								+ ",'"+this.nomeCritica+"'"
								+ ",'"+this.descricaoCritica+"')";
			try {
				st.executeUpdate(insert);
			} catch (SQLException e) {
				e.printStackTrace();
			}
			Utils.waitv(0.25);
			
			try {
				rs=st.executeQuery(query);
				while(rs.next())
				{
					this.idTipoCritica=rs.getInt("id_tipo_critica");
				}
			} catch (SQLException e) {
				e.printStackTrace();
			}

		}
	}

	public TipoCritica(Connection connMaria, int idTipoCritica)
	{
		this.idTipoCritica=idTipoCritica;
		String query="select * from BMA.tipo_critica "
							+ " where"
							+ " id_tipo_critica="+this.idTipoCritica;
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
				this.apelido=rs.getString("apelido");
				this.nomeCritica=rs.getString("nome_critica");
				this.descricaoCritica=rs.getString("descricao_critica");
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
	
	public int getIdTipoCritica() {
		return idTipoCritica;
	}
	public void setIdTipoCritica(int idTipoCritica) {
		this.idTipoCritica = idTipoCritica;
	}
	public String getNomeCritica() {
		return nomeCritica;
	}
	public void setNomeCritica(String nomeCritica) {
		this.nomeCritica = nomeCritica;
	}
	public String getDescricaoCritica() {
		return descricaoCritica;
	}
	public void setDescricaoCritica(String descricaoCritica) {
		this.descricaoCritica = descricaoCritica;
	}

	public String getApelido() {
		return apelido;
	}

	public void setApelido(String apelido) {
		this.apelido = apelido;
	}
	
	
}
