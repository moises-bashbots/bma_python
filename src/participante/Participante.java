package participante;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import utils.Utils;

public class Participante {
	private int idParticipante=0;
	private String razaoSocial="";
	private String cadastro="";
	private String raizCadastro="";
	private String complementoCadastro="";
	private int r1=0;
	private int r2=0;
	private int r3=0;
	private int r4=0;
	
	public Participante()
	{
		
	}
	
	public Participante(Connection connMaria,  int idParticipante)
	{
		this.idParticipante=idParticipante;
		String queryMaria="select * from BMA.participante where id_participante="+this.idParticipante;
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
				this.razaoSocial=rsMaria.getString("razao_social");
				this.cadastro=rsMaria.getString("cadastro");
				this.raizCadastro=rsMaria.getString("raiz_cadastro");
				this.complementoCadastro=rsMaria.getString("complemento_cadastro");
				this.r1=rsMaria.getInt("r1");
				this.r2=rsMaria.getInt("r2");
				this.r3=rsMaria.getInt("r3");
				this.r4=rsMaria.getInt("r4");				
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		
	}
	
	public Participante(Connection connMaria, String cadastro)
	{
		this.cadastro=cadastro;
		if(cadastro.length()==14)
		{
			this.raizCadastro=this.cadastro.substring(0,8);
			this.complementoCadastro=this.cadastro.substring(12,14);
			this.r1=Integer.parseInt(this.cadastro.substring(0,1));
			this.r2=Integer.parseInt(this.cadastro.substring(2,3));
			this.r3=Integer.parseInt(this.cadastro.substring(5,6));
			this.r4=Integer.parseInt(this.cadastro.substring(12,13));
		}
		else {
			this.raizCadastro=this.cadastro.substring(0,9);
			this.complementoCadastro=this.cadastro.substring(9,11);
			this.r1=Integer.parseInt(this.cadastro.substring(0,1));
			this.r2=Integer.parseInt(this.cadastro.substring(3,4));
			this.r3=Integer.parseInt(this.cadastro.substring(6,7));
			this.r4=Integer.parseInt(this.cadastro.substring(9,10));
		}
		String queryMaria="select * from BMA.participante"
							+ " where r1="+this.r1
							+" and r2="+this.r2
							+" and r3="+this.r3
							+" and r4="+this.r4
							+" and complemento_cadastro='"+this.complementoCadastro+"'"
							+" and raiz_cadastro='"+this.raizCadastro+"'"
							+ " and cadastro='"+this.cadastro+"'";
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
				this.idParticipante=rsMaria.getInt("id_participante");
				this.razaoSocial=rsMaria.getString("razao_social");
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}		
	}
	
	public Participante(Connection connMaria, Connection connMSS, String cadastro, String razaoSocial)
	{
		this.cadastro=cadastro;
		this.razaoSocial=razaoSocial;
		
		if(cadastro.length()==14)
		{
			this.raizCadastro=this.cadastro.substring(0,8);
			this.complementoCadastro=this.cadastro.substring(12,14);
			this.r1=Integer.parseInt(this.cadastro.substring(0,1));
			this.r2=Integer.parseInt(this.cadastro.substring(2,3));
			this.r3=Integer.parseInt(this.cadastro.substring(5,6));
			this.r4=Integer.parseInt(this.cadastro.substring(12,13));
		}
		else {
			this.raizCadastro=this.cadastro.substring(0,9);
			this.complementoCadastro=this.cadastro.substring(9,11);
			this.r1=Integer.parseInt(this.cadastro.substring(0,1));
			this.r2=Integer.parseInt(this.cadastro.substring(3,4));
			this.r3=Integer.parseInt(this.cadastro.substring(6,7));
			this.r4=Integer.parseInt(this.cadastro.substring(9,10));
		}
		String queryMaria="select * from BMA.participante"
							+ " where r1="+this.r1
							+" and r2="+this.r2
							+" and r3="+this.r3
							+" and r4="+this.r4
							+" and complemento_cadastro='"+this.complementoCadastro+"'"
							+" and raiz_cadastro='"+this.raizCadastro+"'"
							+ " and cadastro='"+this.cadastro+"'";
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
				this.idParticipante=rsMaria.getInt("id_participante");
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		if(this.idParticipante==0)
		{
			String insert="insert into BMA.participante (r1,r2,r3,r4,razao_social, cadastro, raiz_cadastro, complemento_cadastro)"
									+ " values("
									+ this.r1
									+ ","+this.r2
									+ ","+this.r3
									+ ","+this.r4
									+ ",'"+this.razaoSocial+"'"
									+ ",'"+this.cadastro+"'"
									+ ",'"+this.raizCadastro+"'"
									+ ",'"+this.complementoCadastro+"'"
									+ ")";
			try {
				stMaria.executeUpdate(insert);
				try {
					rsMaria=stMaria.executeQuery(queryMaria);
					Utils.waitv(0.25);
					while(rsMaria.next())
					{
						this.idParticipante=rsMaria.getInt("id_participante");
					}
				} catch (SQLException e) {
					e.printStackTrace();
				}
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
	}

	public static void main(String[] args)
	{
		Connection connection=null;
		Participante participante = new Participante(connection, "10750879000142");
		participante.show();
	}
	
	public void show()
	{
		System.out.println("RazaoSocial: "+this.razaoSocial);
		System.out.println("Cadastro: "+this.cadastro);
		System.out.println("RaizCadastro: "+this.raizCadastro);
		System.out.println("ComplementoCadastro: "+this.complementoCadastro);
		System.out.println("R1: "+this.r1);
		System.out.println("R2: "+this.r2);
		System.out.println("R3: "+this.r3);
		System.out.println("R4: "+this.r4);
	}
	
	public int getIdParticipante() {
		return idParticipante;
	}
	public void setIdParticipante(int idParticipante) {
		this.idParticipante = idParticipante;
	}
	public String getRazaoSocial() {
		return razaoSocial;
	}
	public void setRazaoSocial(String razaoSocial) {
		this.razaoSocial = razaoSocial;
	}
	public String getCadastro() {
		return cadastro;
	}
	public void setCadastro(String cadastro) {
		this.cadastro = cadastro;
	}
	public String getRaizCadastro() {
		return raizCadastro;
	}
	public void setRaizCadastro(String raizCadastro) {
		this.raizCadastro = raizCadastro;
	}
	public String getComplementoCadastro() {
		return complementoCadastro;
	}
	public void setComplementoCadastro(String complementoCadastro) {
		this.complementoCadastro = complementoCadastro;
	}
	public int getR1() {
		return r1;
	}
	public void setR1(int r1) {
		this.r1 = r1;
	}
	public int getR2() {
		return r2;
	}
	public void setR2(int r2) {
		this.r2 = r2;
	}
	public int getR3() {
		return r3;
	}
	public void setR3(int r3) {
		this.r3 = r3;
	}
	public int getR4() {
		return r4;
	}
	public void setR4(int r4) {
		this.r4 = r4;
	}
	
	

}
