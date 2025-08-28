package bradesco;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import utils.Utils;

public class StatusCobrancaPIX {
	private int idStatusCobrancaPIX=0;
	private String status="";

	public StatusCobrancaPIX()
	{
		
	}
	
	public StatusCobrancaPIX(Connection connMaria, String status)
	{
		this.status=status;
		String query="select * from BMA.status_cobranca_pix where status='"+this.status+"'";
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
				this.idStatusCobrancaPIX=rs.getInt("id_status_cobranca_pix");
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		if(this.idStatusCobrancaPIX==0)
		{
			String insert="insert into BMA.status_cobranca_pix (status) values ('"+this.status+"')";
			try {
				st.executeUpdate(insert);
				Utils.waitv(.15);
				try {
					rs=st.executeQuery(query);
					while(rs.next())
					{
						this.idStatusCobrancaPIX=rs.getInt("id_status_cobranca_pix");
					}
				} catch (SQLException e) {
					e.printStackTrace();
				}

			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
	}
	
	public StatusCobrancaPIX(Connection connMaria, int idStatusCobrancaPIX)
	{
		this.idStatusCobrancaPIX=idStatusCobrancaPIX;
		String query="select * from BMA.status_cobranca_pix where id_status_cobranca_pix="+this.idStatusCobrancaPIX;
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
				this.status=rs.getString("status");
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	public int getIdStatusCobrancaPIX() {
		return idStatusCobrancaPIX;
	}

	public void setIdStatusCobrancaPIX(int idStatusCobrancaPIX) {
		this.idStatusCobrancaPIX = idStatusCobrancaPIX;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}
}
