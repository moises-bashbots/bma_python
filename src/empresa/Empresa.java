package empresa;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;

import endereco.Endereco;
import utils.Utils;

public class Empresa {
	private int idEmpresa=0;
	private String cnpj="";
	private String razaoSocial="";
	private String apelido="";
	private Endereco endereco= new Endereco();
	private DadosBancariosEmpresa dadosBancariosEmpresa = new DadosBancariosEmpresa();
	
	public Empresa()
	{
		
	}
	
	public Empresa(Connection connMaria, Connection connMSS, String apelido)
	{
		this.apelido=apelido;
		String queryMaria = "select * from BMA.empresa where apelido='"+this.apelido+"'";
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
				this.idEmpresa=rsMaria.getInt("id_empresa");
				this.cnpj=rsMaria.getString("cnpj");
				this.razaoSocial=rsMaria.getString("razao_social");
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		
		if(this.idEmpresa!=0)
		{		
			String queryMSS="select * from BMA.dbo.Empresa where cgc='"+this.cnpj+"'";
			System.out.println(queryMSS);
			Statement stMSS=null;
			try {
				stMSS=connMSS.createStatement();
			} catch (SQLException e) {
				e.printStackTrace();
			}
			
			ResultSet rsMSS=null;
			try {
				rsMSS=stMSS.executeQuery(queryMSS);
				while(rsMSS.next())
				{
					String logradouro=rsMSS.getString("Ender");
					String bairro=rsMSS.getString("Bairro");
					String cidade=rsMSS.getString("Cidade");
					String uf = rsMSS.getString("Uf");
					String cep = rsMSS.getString("Cep");
					this.endereco=new Endereco(logradouro, bairro, cidade, uf, cep);
				}
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}		
		else {
			Statement stMSS=null;
			try {
				stMSS=connMSS.createStatement();
			} catch (SQLException e) {
				e.printStackTrace();
			}
			String queryMSS="select * from BMA.dbo.Empresa where Apelido='"+this.apelido+"'";
			System.out.println(queryMSS);
			ResultSet rsMSS=null;
			try {
				rsMSS=stMSS.executeQuery(queryMSS);
				while(rsMSS.next())
				{
					this.cnpj=rsMSS.getString("Cgc");
					this.razaoSocial=rsMSS.getString("Nome");
					String logradouro=rsMSS.getString("Ender");
					String bairro=rsMSS.getString("Bairro");
					String cidade=rsMSS.getString("Cidade");
					String uf = rsMSS.getString("Uf");
					String cep = rsMSS.getString("Cep");
					this.endereco=new Endereco(logradouro, bairro, cidade, uf, cep);
				}
			} catch (SQLException e) {
				e.printStackTrace();
			}
			
			String insertMaria="insert into BMA.empresa (apelido, cnpj, razao_social)"
											+ " values("
											+ "'"+this.apelido+"'"
											+ ",'"+this.cnpj+"'"
											+ ",'"+this.razaoSocial+"'"
											+ ")";
			System.out.println(insertMaria);
			try {
				stMaria.executeUpdate(insertMaria);
				Utils.waitv(0.25);
				try {
					rsMaria=stMaria.executeQuery(queryMaria);
					while(rsMaria.next())
					{
						this.idEmpresa=rsMaria.getInt("id_empresa");
					}
				} catch (SQLException e) {
					e.printStackTrace();
				}
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
		this.dadosBancariosEmpresa=new DadosBancariosEmpresa(connMaria, connMSS, this.idEmpresa, this.apelido);
		this.dadosBancariosEmpresa.show();
		this.fillAddress(connMSS);
	}
	
	public Empresa(Connection connMaria, Connection connMSS, int idEmpresa)
	{
		this.idEmpresa=idEmpresa;
		String queryMaria = "select * from BMA.empresa where id_empresa="+this.idEmpresa;
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
				this.cnpj=rsMaria.getString("cnpj");
				this.razaoSocial=rsMaria.getString("razao_social");
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		
		if(this.cnpj.length()>0)
		{		
			String queryMSS="select * from BMA.dbo.Empresa where cgc='"+this.cnpj+"'";
			System.out.println(queryMSS);
			Statement stMSS=null;
			try {
				stMSS=connMSS.createStatement();
			} catch (SQLException e) {
				e.printStackTrace();
			}
			
			ResultSet rsMSS=null;
			try {
				rsMSS=stMSS.executeQuery(queryMSS);
				while(rsMSS.next())
				{
					this.apelido=rsMSS.getString("Apelido");
					String logradouro=rsMSS.getString("Ender");
					String bairro=rsMSS.getString("Bairro");
					String cidade=rsMSS.getString("Cidade");
					String uf = rsMSS.getString("Uf");
					String cep = rsMSS.getString("Cep");
					this.endereco=new Endereco(logradouro, bairro, cidade, uf, cep);
				}
			} catch (SQLException e) {
				e.printStackTrace();
			}
			this.dadosBancariosEmpresa=new DadosBancariosEmpresa(connMaria, connMSS,  this.idEmpresa, this.apelido);
			this.fillAddress(connMSS);
			this.dadosBancariosEmpresa.show();
		}	
	}
	
	public static Empresa identifyDadosBancariosEmpresa(Connection connMaria, Connection connMSS, String agencia, String conta, String digitoConta)
	{
		String query="select * from BMA.dados_bancarios_empresa"
							+ " where "
							+ " agencia = "+"'"+agencia+"'"
							+ " and conta="+"'"+conta+"'"
							+ " and digito_conta="+"'"+digitoConta+"'";
		Statement st=null;
		try {
			st=connMaria.createStatement();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		ResultSet rs=null;
		Empresa empresa = null;
		DadosBancariosEmpresa dadosBancariosEmpresa = null;
		try {
			rs=st.executeQuery(query);
			while(rs.next())
			{
				int idDadosBancariosEmpresa=rs.getInt("id_dados_bancarios_empresa");
				empresa = new Empresa(connMaria, connMSS, rs.getInt("empresa_id_empresa"));				
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return empresa;
	}

	
	public static HashMap<String, Empresa> empresas(Connection connMaria, Connection connMSS)
	{
		System.out.println("################################");
		System.out.println("LOADING LIST OF EMPRESAS");
		System.out.println("################################");
		HashMap<String, Empresa> empresas = new HashMap<String, Empresa>();
		String queryMaria = "select * from BMA.empresa";
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
				Empresa empresa = new Empresa(connMaria, connMSS, rsMaria.getInt("id_empresa"));
//				empresa.fillAddress(connMSS);
				if(empresa.getDadosBancariosEmpresa().getAgencia().length()>0)
				{
					empresas.put(empresa.getApelido(), empresa);
					empresa.show();
				}
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		System.out.println("################################");
		System.out.println("LOADED LIST OF EMPRESAS");
		System.out.println("################################");
		return empresas;
	}
//	public Empresa(Connection connMaria, Connection connMSS, int idEmpresa)
//	{
//		this.idEmpresa=idEmpresa;
//		String queryMaria = "select * from BMA.empresa where id_empresa="+this.idEmpresa;
//		System.out.println(queryMaria);
//		Statement stMaria=null;
//		try {
//			stMaria=connMaria.createStatement();
//		} catch (SQLException e) {
//			e.printStackTrace();
//		}
//		ResultSet rsMaria=null;
//		try {
//			rsMaria=stMaria.executeQuery(queryMaria);
//			while(rsMaria.next())
//			{
//				this.apelido=rsMaria.getString("apelido");
//				this.cnpj=rsMaria.getString("cnpj");
//				this.razaoSocial=rsMaria.getString("razao_social");
//			}
//		} catch (SQLException e) {
//			e.printStackTrace();
//		}
//		this.dadosBancariosEmpresa=new DadosBancariosEmpresa(connMaria, connMSS, this.idEmpresa, this.apelido);
//	}
	
	public void show()
	{
		System.out.println("idEmpresa: "+this.idEmpresa);
		System.out.println("RazaoSocial: "+this.razaoSocial);
		System.out.println("Apelido: "+this.apelido);
		this.getDadosBancariosEmpresa().show();
	}
	
	public void fillAddress(Connection connMSS)
	{
		if(this.cnpj.length()>0)
		{		
			String queryMSS="select * from BMA.dbo.Empresa where cgc='"+this.cnpj+"'";
			Statement stMSS=null;
			try {
				stMSS=connMSS.createStatement();
			} catch (SQLException e) {
				e.printStackTrace();
			}
			
			ResultSet rsMSS=null;
			try {
				rsMSS=stMSS.executeQuery(queryMSS);
				while(rsMSS.next())
				{
					this.apelido=rsMSS.getString("Apelido");
					String logradouro=rsMSS.getString("Ender");
					String bairro=rsMSS.getString("Bairro");
					String cidade=rsMSS.getString("Cidade");
					String uf = rsMSS.getString("Uf");
					String cep = rsMSS.getString("Cep");
					this.endereco=new Endereco(logradouro, bairro, cidade, uf, cep);
				}
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}	
	}

	public int getIdEmpresa() {
		return idEmpresa;
	}

	public void setIdEmpresa(int idEmpresa) {
		this.idEmpresa = idEmpresa;
	}

	public String getCnpj() {
		return cnpj;
	}

	public void setCnpj(String cnpj) {
		this.cnpj = cnpj;
	}

	public String getRazaoSocial() {
		return razaoSocial;
	}

	public void setRazaoSocial(String razaoSocial) {
		this.razaoSocial = razaoSocial;
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

	public DadosBancariosEmpresa getDadosBancariosEmpresa() {
		return dadosBancariosEmpresa;
	}

	public void setDadosBancariosEmpresa(DadosBancariosEmpresa dadosBancariosEmpresa) {
		this.dadosBancariosEmpresa = dadosBancariosEmpresa;
	}
}
