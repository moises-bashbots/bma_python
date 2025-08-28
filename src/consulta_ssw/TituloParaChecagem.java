package consulta_ssw;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;

public class TituloParaChecagem {
	
	private static SimpleDateFormat sdfm=new SimpleDateFormat("YYYY-MM-dd");
	/*
	select t.sequencial, t.dcadastro, t.cheque, t.duplicata, t.total, t.nfechave, t.cedente, t.empresa, cmus.IdStatus, cs.Codigo, cs.Descritivo  
	from titulo t, ChecagemMovimentoUltimoStatus cmus, ChecagemStatus cs 
	where quitacao is null and tipo_operacao = 1
	and t.NFEChave is not null
	and t.NFEChave <> ''
	and cmus.Sequencial_Titulo =t.sequencial 
	and cs.Id = cmus.IdStatus 
	and cs.Id in (1,4,5,7,12,14,15,27,35,42,43,45,46,47,48,57)
	;
	*/
	private int sequencial=0;	
	private Date dataDeCadastro=null;
	private String cheque="";
	private String duplicata="";
	private double total=0;
	private String chaveNFE="";
	private String cnpjSacado="";
	private String cnpjCedente="";
	private String nomeCedente="";
	private String nomeEmpresa="";
	private int idStatus=0;
	private String codigoStatus="";
	private String descritivoStatus="";
	
	public TituloParaChecagem(int sequencial, Date dataDeCadastro, String cheque, String duplicata, double total, String chaveNFE, String cnpjSacado, String cnpjCedente, String nomeCedente, String nomeEmpresa, int idStatus, String codigoStatus, String descritivoStatus)
	{
		this.sequencial=sequencial;
		this.dataDeCadastro=dataDeCadastro;
		this.cheque=cheque;
		this.duplicata=duplicata;
		this.total=total;
		this.chaveNFE=chaveNFE;
		this.cnpjSacado=cnpjSacado;
		this.cnpjCedente=cnpjCedente;
		this.nomeCedente=nomeCedente;
		this.nomeEmpresa=nomeEmpresa;
		this.idStatus=idStatus;
		this.codigoStatus=codigoStatus;
		this.descritivoStatus=descritivoStatus;
	}
	
	public void show()
	{
		System.out.println("------------------------------------------------");
		System.out.println("Titulo para checagem");
		System.out.println("Empresa: "+this.nomeEmpresa);
		System.out.println("Cedente: "+this.nomeCedente);
		System.out.println("CNPJCedente: "+this.cnpjCedente);
		System.out.println("Duplicata: "+this.duplicata);
		System.out.println("DescritivoStatus: "+this.descritivoStatus);
		System.out.println("CNPJSacado: "+this.cnpjSacado);
		System.out.println("ChaveNFE: "+this.chaveNFE);
		
	}
	
	public static HashMap<String, String> chavesConsultadasFinalizadas(Connection conn)
	{
		HashMap<String, String> chavesConsultadas=new HashMap<>();
		String query="select distinct chave_nota from BMA.evento_transporte where titulo_evento='MERCADORIA ENTREGUE'";
		Statement st=null;
		try {
			st=conn.createStatement();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		ResultSet rs=null;
		try {
			rs = st.executeQuery(query);
			while(rs.next())
			{
				chavesConsultadas.put(rs.getString("chave_nota"), rs.getString("chave_nota"));
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return chavesConsultadas;
	}
	
	public static ArrayList<TituloParaChecagem> titulosParaChecagem(Connection conn, int daysBefore)
	{
		Calendar calendar=Calendar.getInstance();
		calendar.add(Calendar.DAY_OF_MONTH, -daysBefore);
		ArrayList<TituloParaChecagem> titulosParaChecagem = new ArrayList<>();
		String query="select t.sequencial, t.dcadastro, t.cheque, t.duplicata, t.total, t.nfechave,t.cnpj as cnpj_sacado, c.cnpj as cnpj_cedente,t.cedente, t.empresa, cmus.IdStatus, cs.Codigo, cs.Descritivo  "
				+ " from titulo t  WITH (NOLOCK), ChecagemMovimentoUltimoStatus cmus  WITH (NOLOCK), ChecagemStatus cs  WITH (NOLOCK), cedente c  WITH (NOLOCK)"
				+ " where quitacao is null and tipo_operacao = 1"
				+ " and t.NFEChave is not null"
				+ " and t.NFEChave <> ''"
				+ " and cmus.Sequencial_Titulo =t.sequencial "
				+ " and cs.Id = cmus.IdStatus "
				+ " and cs.Id in (1,4,5,7,12,14,15,27,35,42,43,45,46,47,48,57)"
				+ " and c.apelido =t.cedente "
				+ " and t.dcadastro > "+"'"+sdfm.format(calendar.getTime())+"'"
				+ " order by t.dcadastro desc";
		Statement st=null;
		try {
			st=conn.createStatement();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		
		ResultSet rs=null;
		try {
			rs=st.executeQuery(query);
			while(rs.next())
			{
				TituloParaChecagem tituloParaChecagem = new TituloParaChecagem(rs.getInt("sequencial"), 
																																				rs.getDate("dcadastro"), 
																																				rs.getString("cheque"), 
																																				rs.getString("duplicata"), 
																																				rs.getDouble("total"), 
																																				rs.getString("nfechave"), 
																																				rs.getString("cnpj_sacado"),
																																				rs.getString("cnpj_cedente"), 
																																				rs.getString("cedente"), 
																																				rs.getString("empresa"), 
																																				rs.getInt("IdStatus"), 
																																				rs.getString("Codigo"), 
																																				rs.getString("Descritivo"));
				titulosParaChecagem.add(tituloParaChecagem);
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		
		return titulosParaChecagem;
	}
	
	public static ArrayList<TituloParaChecagem> titulosParaChecagem(Connection conn)
	{
		ArrayList<TituloParaChecagem> titulosParaChecagem = new ArrayList<>();
		String query="select t.sequencial, t.dcadastro, t.cheque, t.duplicata, t.total, t.nfechave,t.cnpj as cnpj_sacado, c.cnpj as cnpj_cedente,t.cedente, t.empresa, cmus.IdStatus, cs.Codigo, cs.Descritivo  "
				+ " from titulo t  WITH (NOLOCK), ChecagemMovimentoUltimoStatus cmus  WITH (NOLOCK), ChecagemStatus cs  WITH (NOLOCK), cedente c  WITH (NOLOCK)"
				+ " where quitacao is null and tipo_operacao = 1"
				+ " and t.NFEChave is not null"
				+ " and t.NFEChave <> ''"
				+ " and cmus.Sequencial_Titulo =t.sequencial "
				+ " and cs.Id = cmus.IdStatus "
				+ " and cs.Id in (1,4,5,7,12,14,15,27,35,42,43,45,46,47,48,57)"
				+ " and c.apelido =t.cedente "
				+ " order by t.dcadastro desc";
		Statement st=null;
		try {
			st=conn.createStatement();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		
		ResultSet rs=null;
		try {
			rs=st.executeQuery(query);
			while(rs.next())
			{
				TituloParaChecagem tituloParaChecagem = new TituloParaChecagem(rs.getInt("sequencial"), 
																																				rs.getDate("dcadastro"), 
																																				rs.getString("cheque"), 
																																				rs.getString("duplicata"), 
																																				rs.getDouble("total"), 
																																				rs.getString("nfechave"), 
																																				rs.getString("cnpj_sacado"),
																																				rs.getString("cnpj_cedente"), 
																																				rs.getString("cedente"), 
																																				rs.getString("empresa"), 
																																				rs.getInt("IdStatus"), 
																																				rs.getString("Codigo"), 
																																				rs.getString("Descritivo"));
				titulosParaChecagem.add(tituloParaChecagem);
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		
		return titulosParaChecagem;
	}
	
	public static ArrayList<TituloParaChecagem> titulosParaChecagemPorChave(Connection conn, String chaveNFE)
	{
		ArrayList<TituloParaChecagem> titulosParaChecagem = new ArrayList<>();
		String query="select t.sequencial, t.dcadastro, t.cheque, t.duplicata, t.total, t.nfechave,t.cnpj as cnpj_sacado, c.cnpj as cnpj_cedente,t.cedente, t.empresa, cmus.IdStatus, cs.Codigo, cs.Descritivo  "
				+ " from titulo t  WITH (NOLOCK), ChecagemMovimentoUltimoStatus cmus  WITH (NOLOCK), ChecagemStatus cs  WITH (NOLOCK), cedente c  WITH (NOLOCK)"
				+ " where quitacao is null and tipo_operacao = 1"
				+ " and t.NFEChave = "+"'"+chaveNFE+"'"
				+ " and cmus.Sequencial_Titulo =t.sequencial "
				+ " and cs.Id = cmus.IdStatus "
				+ " and cs.Id in (1,4,5,7,12,14,15,27,35,42,43,45,46,47,48,57)"
				+ " and c.apelido =t.cedente"
				+ " order by t.dcadastro desc ";
		System.out.println(query);
		Statement st=null;
		try {
			st=conn.createStatement();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		
		ResultSet rs=null;
		try {
			rs=st.executeQuery(query);
			while(rs.next())
			{
				TituloParaChecagem tituloParaChecagem = new TituloParaChecagem(rs.getInt("sequencial"), 
																																				rs.getDate("dcadastro"), 
																																				rs.getString("cheque"), 
																																				rs.getString("duplicata"), 
																																				rs.getDouble("total"), 
																																				rs.getString("nfechave"), 
																																				rs.getString("cnpj_sacado"),
																																				rs.getString("cnpj_cedente"), 
																																				rs.getString("cedente"), 
																																				rs.getString("empresa"), 
																																				rs.getInt("IdStatus"), 
																																				rs.getString("Codigo"), 
																																				rs.getString("Descritivo"));
				titulosParaChecagem.add(tituloParaChecagem);
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		
		return titulosParaChecagem;
	}
	
	public int getSequencial() {
		return this.sequencial;
	}
	public void setSequencial(int sequencial) {
		this.sequencial = sequencial;
	}
	public Date getDataDeCadastro() {
		return this.dataDeCadastro;
	}
	public void setDataDeCadastro(Date dataDeCadastro) {
		this.dataDeCadastro = dataDeCadastro;
	}
	public String getCheque() {
		return this.cheque;
	}
	public void setCheque(String cheque) {
		this.cheque = cheque;
	}
	public String getDuplicata() {
		return this.duplicata;
	}
	public void setDuplicata(String duplicata) {
		this.duplicata = duplicata;
	}
	public double getTotal() {
		return this.total;
	}
	public void setTotal(double total) {
		this.total = total;
	}
	public String getChaveNFE() {
		return this.chaveNFE;
	}
	public void setChaveNFE(String chaveNFE) {
		this.chaveNFE = chaveNFE;
	}
	public String getNomeCedente() {
		return this.nomeCedente;
	}
	public void setNomeCedente(String nomeCedente) {
		this.nomeCedente = nomeCedente;
	}
	public String getNomeEmpresa() {
		return this.nomeEmpresa;
	}
	public void setNomeEmpresa(String nomeEmpresa) {
		this.nomeEmpresa = nomeEmpresa;
	}
	public int getIdStatus() {
		return this.idStatus;
	}
	public void setIdStatus(int idStatus) {
		this.idStatus = idStatus;
	}
	public String getCodigoStatus() {
		return this.codigoStatus;
	}
	public void setCodigoStatus(String codigoStatus) {
		this.codigoStatus = codigoStatus;
	}
	public String getDescritivoStatus() {
		return this.descritivoStatus;
	}
	public void setDescritivoStatus(String descritivoStatus) {
		this.descritivoStatus = descritivoStatus;
	}

	public String getCnpjCedente() {
		return this.cnpjCedente;
	}

	public void setCnpjCedente(String cnpjCedente) {
		this.cnpjCedente = cnpjCedente;
	}

	public static SimpleDateFormat getSdfm() {
		return sdfm;
	}

	public static void setSdfm(SimpleDateFormat sdfm) {
		TituloParaChecagem.sdfm = sdfm;
	}

	public String getCnpjSacado() {
		return this.cnpjSacado;
	}

	public void setCnpjSacado(String cnpjSacado) {
		this.cnpjSacado = cnpjSacado;
	}
	
	
}
