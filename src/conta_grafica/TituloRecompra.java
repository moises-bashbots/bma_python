package conta_grafica;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.concurrent.TimeUnit;

import cedente.Cedente;
import empresa.Empresa;
import utils.Utils;

public class TituloRecompra {
	private int idTituloRecompra=0;
	private Empresa empresa = new Empresa();
	private Cedente cedente = new Cedente();
	private static SimpleDateFormat sdfr=new SimpleDateFormat("dd/MM/yyyy");
	private static SimpleDateFormat sdf=new SimpleDateFormat("yyyy-MM-dd");
	private static SimpleDateFormat sdfn=new SimpleDateFormat("yyyyMMdd");
	private static  DecimalFormat df = new DecimalFormat("#0.00#");
	private String identificacaoTitulo="";
	private String nossoNumero="";
	private String tipoCobranca="";
	private double valor=0;
	private double valorCorrigido=0;
	private double deducao=0;
	private double mora=0;
	private double diferencaLiquidacao=0;
	private double valorRecompra=0;
	private double valorTotal=0;
	private double abatimento=0;
	private Date vencimento=null;
	private Date dataSolicitacao=null;
	private boolean vencido=false;
	private boolean quitado=false;
	private boolean recebimentoCartorio=false;
	private boolean enviadoCartorio=false;
	private int idOperacaoRecompra=0;
	private boolean vencimentoDistante=false;
	private boolean solicitacaoAntiga=false;
	private boolean exception=false;
	private boolean efetuado=false;
	private boolean baixado=false;
	private boolean baixadoBradesco=false;
	private boolean podeRecomprar=false;
	private boolean acatarBaixa=false;
	private boolean duplicata=false;
	private boolean cheque=false;
	private boolean exists=false;
	private String tipoTitulo="";
	
	private String motivoNaoPodeRecomprar="";
	
	public TituloRecompra()
	{
		
	}

	public TituloRecompra(Connection connMaria, Connection connMSS,String apelidoEmpresa, String apelidoCedente, String identificacaoTitulo, String tipoTitulo,  double valor, Date vencimento, Date dataSolicitacao)
	{		
		this.empresa=new Empresa(connMaria, connMSS, apelidoEmpresa);
		this.cedente=new Cedente(connMaria, connMSS, this.empresa, apelidoCedente);
		this.identificacaoTitulo=identificacaoTitulo;
		this.tipoTitulo=tipoTitulo;
		this.valor=valor;
		this.valorCorrigido=this.valor;
		this.vencimento=vencimento;
		this.dataSolicitacao=dataSolicitacao;		
		this.checkVencido();
		this.checkTitulo(connMSS, connMaria);
		if(this.isExists())
		{
			this.updateAfterCheck(connMaria);
		}
	}

	
	public TituloRecompra(Connection connMaria, Connection connMSS,String apelidoEmpresa, String apelidoCedente, String identificacaoTitulo, double valor, Date vencimento, Date dataSolicitacao)
	{		
		this.empresa=new Empresa(connMaria, connMSS, apelidoEmpresa);
		this.cedente=new Cedente(connMaria, connMSS, this.empresa, apelidoCedente);
		this.identificacaoTitulo=identificacaoTitulo;
		this.valor=valor;
		this.valorCorrigido=this.valor;
		this.vencimento=vencimento;
		this.dataSolicitacao=dataSolicitacao;		
		this.checkVencido();
		this.checkTitulo(connMSS, connMaria);
		if(this.isExists())
		{
			this.updateAfterCheck(connMaria);
		}
	}
	
	public TituloRecompra(Connection connMaria, Connection connMSS,OperacaoRecompra operacaoRecompra, String identificacaoTitulo, String tipotitulo, double valor, Date vencimento, Date dataSolicitacao, boolean register)
	{
		
		this.empresa=operacaoRecompra.getEmpresa();
		this.cedente=operacaoRecompra.getCedente();
		this.identificacaoTitulo=identificacaoTitulo;
		this.tipoTitulo = tipotitulo;
		this.valor=valor;
		this.valorCorrigido=this.valor;
		this.vencimento=vencimento;
		this.dataSolicitacao=dataSolicitacao;
		this.idOperacaoRecompra=operacaoRecompra.getIdOperacaoRecompra();
		this.checkVencido();
		this.checkTitulo(connMSS, connMaria);

		if(register)
		{
			String query="select * from BMA.titulo_recompra"
								+ " where operacao_recompra_id_operacao_recompra="+operacaoRecompra.getIdOperacaoRecompra()
								+ " and identificacao_titulo='"+this.identificacaoTitulo+"'"
								+ " and valor="+this.valor
								+ " and vencimento='"+sdf.format(this.vencimento)+"'";
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
					this.idTituloRecompra=rs.getInt("id_titulo_recompra");
					this.nossoNumero=rs.getString("nosso_numero");
					this.tipoCobranca=rs.getString("tcobran");
					int baixadoInt=0;
					int efetuadoInt=0;
					efetuadoInt=rs.getInt("efetuado");
					baixadoInt=rs.getInt("baixado");
					if(efetuadoInt>0)
					{
						this.efetuado=true;
					}
					if(baixadoInt>0)
					{
						this.baixado=true;
					}
				}
			} catch (SQLException e) {
				e.printStackTrace();
			}
			
			if(this.idTituloRecompra==0)
			{
				String insert="insert into BMA.titulo_recompra ("
						+ "operacao_recompra_id_operacao_recompra,"
						+ " identificacao_titulo,"
						+ "nosso_numero, "
						+"tipo_cobranca,"
						+ "valor, "
						+ "vencimento,"
						+ "data_solicitacao)"
									+ " values("
									+ operacaoRecompra.getIdOperacaoRecompra()
									+ ",'"+this.identificacaoTitulo+"'"
									+ ",'"+this.nossoNumero+"'"
									+ ",'"+this.tipoCobranca+"'"
									+ ","+this.valor
									+ ",'"+sdf.format(this.vencimento)+"'"
									+ ",'"+sdf.format(this.dataSolicitacao)+"'"
									+ ")";
				System.out.println(insert);
				try {
					st.executeUpdate(insert);				
					Utils.waitv(0.25);
					try {
						rs=st.executeQuery(query);
						while(rs.next())
						{
							this.idTituloRecompra=rs.getInt("id_titulo_recompra");				
						}
					} catch (SQLException e) {
						e.printStackTrace();
					}
				} catch (SQLException e) {
					e.printStackTrace();
				}				
			}
			this.updateAfterCheck(connMaria);
		}
		this.checkTipoCobranca();
	}
	
	public TituloRecompra(Connection connMaria, Connection connMSS,OperacaoRecompra operacaoRecompra, String identificacaoTitulo, double valor, Date vencimento, Date dataSolicitacao, boolean register)
	{
		
		this.empresa=operacaoRecompra.getEmpresa();
		this.cedente=operacaoRecompra.getCedente();
		this.identificacaoTitulo=identificacaoTitulo;
		this.valor=valor;
		this.valorCorrigido=this.valor;
		this.vencimento=vencimento;
		this.dataSolicitacao=dataSolicitacao;
		this.idOperacaoRecompra=operacaoRecompra.getIdOperacaoRecompra();
		this.checkVencido();
		this.checkTitulo(connMSS, connMaria);

		if(register)
		{
			String query="select * from BMA.titulo_recompra"
								+ " where operacao_recompra_id_operacao_recompra="+operacaoRecompra.getIdOperacaoRecompra()
								+ " and identificacao_titulo='"+this.identificacaoTitulo+"'"
								+ " and valor="+this.valor
								+ " and vencimento='"+sdf.format(this.vencimento)+"'";
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
					this.idTituloRecompra=rs.getInt("id_titulo_recompra");
					this.nossoNumero=rs.getString("nosso_numero");
					this.tipoCobranca=rs.getString("tcobran");
					int baixadoInt=0;
					int efetuadoInt=0;
					efetuadoInt=rs.getInt("efetuado");
					baixadoInt=rs.getInt("baixado");
					if(efetuadoInt>0)
					{
						this.efetuado=true;
					}
					if(baixadoInt>0)
					{
						this.baixado=true;
					}
				}
			} catch (SQLException e) {
				e.printStackTrace();
			}
			
			if(this.idTituloRecompra==0)
			{
				String insert="insert into BMA.titulo_recompra ("
						+ "operacao_recompra_id_operacao_recompra,"
						+ " identificacao_titulo,"
						+ "nosso_numero, "
						+"tipo_cobranca,"
						+ "valor, "
						+ "vencimento,"
						+ "data_solicitacao)"
									+ " values("
									+ operacaoRecompra.getIdOperacaoRecompra()
									+ ",'"+this.identificacaoTitulo+"'"
									+ ",'"+this.nossoNumero+"'"
									+ ",'"+this.tipoCobranca+"'"
									+ ","+this.valor
									+ ",'"+sdf.format(this.vencimento)+"'"
									+ ",'"+sdf.format(this.dataSolicitacao)+"'"
									+ ")";
				System.out.println(insert);
				try {
					st.executeUpdate(insert);				
					Utils.waitv(0.25);
					try {
						rs=st.executeQuery(query);
						while(rs.next())
						{
							this.idTituloRecompra=rs.getInt("id_titulo_recompra");				
						}
					} catch (SQLException e) {
						e.printStackTrace();
					}
				} catch (SQLException e) {
					e.printStackTrace();
				}				
			}
			this.updateAfterCheck(connMaria);
		}
		this.checkTipoCobranca();
	}
	
	public TituloRecompra(Connection connMaria, Connection connMSS, Empresa empresa, Cedente cedente, int idOperacaoRecompra, int idTituloRecompra)
	{
	
		this.idTituloRecompra=idTituloRecompra;
		this.empresa=empresa;
		this.cedente=cedente;
		String query="select * from BMA.titulo_recompra"
							+ " where operacao_recompra_id_operacao_recompra="+idOperacaoRecompra
							+ " and id_titulo_recompra="+this.idTituloRecompra;
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
				this.identificacaoTitulo=rs.getString("identificacao_titulo");
				this.valor=rs.getDouble("valor");
				this.valorCorrigido=rs.getDouble("valor_corrigido");
				this.valorRecompra=rs.getDouble("valor_recompra");
				this.vencimento=rs.getDate("vencimento");
				this.dataSolicitacao=rs.getDate("data_solicitacao");
				this.mora=rs.getDouble("mora");
				this.nossoNumero=rs.getString("nosso_numero");
				this.tipoCobranca=rs.getString("tipo_cobranca");
				this.checkVencido();
				int baixadoInt=0;
				int baixadoBradescoInt=0;
				int efetuadoInt=0;
				efetuadoInt=rs.getInt("efetuado");
				baixadoInt=rs.getInt("baixado");
				baixadoBradescoInt=rs.getInt("baixado_bradesco");
				if(efetuadoInt>0)
				{
					this.efetuado=true;
				}
				if(baixadoInt>0)
				{
					this.baixado=true;
				}
				if(baixadoBradescoInt>0)
				{
					this.baixadoBradesco=true;
				}

			}
		} catch (SQLException e) {
			e.printStackTrace();
		}		
		if(this.tipoCobranca==null)
		{
			System.out.println("TipoCobranca is null!");
			
			this.checkTitulo(connMSS,connMaria);
			this.updateAfterCheck(connMaria);
			this.updateTipoCobranca(connMaria);
		}
		else if(this.tipoCobranca.length()==0)
		{
			System.out.println("TipoCobranca is empty!");
			this.checkTitulo(connMSS,connMaria);
			this.updateAfterCheck(connMaria);
			this.updateTipoCobranca(connMaria);
		}
		this.checkTipoCobranca();
	}
	
	public void checkTipoCobranca()
	{
		int codigoTipoCobranca=Integer.parseInt(this.tipoCobranca);
		switch (codigoTipoCobranca) {
		case 19:
			this.acatarBaixa=true;
			this.podeRecomprar=false;
			this.motivoNaoPodeRecomprar="Recomprado em cobranca simples";
			break;
		case 128:
			this.acatarBaixa=true;
			this.podeRecomprar=false;
			this.motivoNaoPodeRecomprar="Recomprado em cobranca simples";
			break;			
		case 7:
			this.acatarBaixa=true;
			this.podeRecomprar=true;
			break;
		case 32:
			this.acatarBaixa=true;
			this.podeRecomprar=true;
			break;
		case 42:
			this.acatarBaixa=true;
			this.podeRecomprar=true;
			break;
		case 48:
			this.acatarBaixa=true;
			this.podeRecomprar=true;
			break;
		case 50:
			this.acatarBaixa=true;
			this.podeRecomprar=true;
			break;
		case 56:
			this.acatarBaixa=true;
			this.podeRecomprar=true;
			break;
		case 66:
		case 67:
		case 68:
		case 69:
		case 70:
		case 71:
			this.acatarBaixa=true;
			this.podeRecomprar=true;
			break;
		default:
			this.acatarBaixa=false;
			this.podeRecomprar=false;
			this.motivoNaoPodeRecomprar="Tipo cobranca "+this.tipoCobranca;
			break;
		}
	}
	
	public void register(Connection connMaria)
	{
		String query="select * from BMA.titulo_recompra"
				+ " where operacao_recompra_id_operacao_recompra="+this.idOperacaoRecompra
				+ " and identificacao_titulo='"+this.identificacaoTitulo+"'"
				+ " and valor="+this.valor
				+ " and vencimento='"+sdf.format(this.vencimento)+"'";
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
			this.idTituloRecompra=rs.getInt("id_titulo_recompra");				
		}
		} catch (SQLException e) {
		e.printStackTrace();
		}
		
		if(this.idTituloRecompra==0)
		{
			String insert="insert into BMA.titulo_recompra (operacao_recompra_id_operacao_recompra, identificacao_titulo,nosso_numero,tipo_cobranca, valor, valor_corrigido,vencimento,data_solicitacao)"
							+ " values("
							+ this.getIdOperacaoRecompra()
							+ ",'"+this.identificacaoTitulo+"'"
							+ ",'"+this.nossoNumero+"'"
							+ ",'"+this.tipoCobranca+"'"
							+ ","+this.valor
							+ ","+this.valorCorrigido
							+ ",'"+sdf.format(this.vencimento)+"'"
							+ ",'"+sdf.format(this.dataSolicitacao)+"'"
							+ ")";
			System.out.println(insert);
			try {
				st.executeUpdate(insert);				
				Utils.waitv(0.25);
				try {
					rs=st.executeQuery(query);
					while(rs.next())
					{
						this.idTituloRecompra=rs.getInt("id_titulo_recompra");				
					}
				} catch (SQLException e) {
					e.printStackTrace();
				}
			} catch (SQLException e) {
				e.printStackTrace();
				}
		}
	}
	
	private void checkVencido()
	{
		int numeroReferencia = Integer.parseInt(sdfn.format(Calendar.getInstance().getTime()));
		int numeroVencimento = Integer.parseInt(sdfn.format(this.vencimento));
		if(numeroVencimento<numeroReferencia)
		{
			this.vencido=true;
		}
	}

	public void updateValues(Connection connMaria)
	{
		String update="update BMA.titulo_recompra set"
								+ " deducao="+this.deducao
								+ ",mora="+this.mora
								+ ",valor_corrigido="+this.valorCorrigido
								+",diferenca_liquidacao="+this.diferencaLiquidacao
								+",valor_recompra="+this.valorRecompra
								+" where id_titulo_recompra="+this.idTituloRecompra;
		System.out.println(update);
		Statement st=null;
		try {
			st=connMaria.createStatement();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		try {
			st.executeUpdate(update);
		} catch (SQLException e) {
			e.printStackTrace();
		}
		
	}
	
	public void updateTipoCobranca(Connection connMaria)
	{
		String update="update BMA.titulo_recompra set"
								+" tipo_cobranca="+"'"+this.tipoCobranca+"'"
								+" where id_titulo_recompra="+this.idTituloRecompra;
		System.out.println(update);
		Statement st=null;
		try {
			st=connMaria.createStatement();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		try {
			st.executeUpdate(update);
		} catch (SQLException e) {
			e.printStackTrace();
		}
		
	}
	
	public void updateBaixadoBradesco(Connection connMaria)
	{
		int baixadoInt=0;
		if(this.baixadoBradesco)
		{
			baixadoInt=1;
		}
		String update="update BMA.titulo_recompra set"
								+" baixado_bradesco="+baixadoInt
								+" where id_titulo_recompra="+this.idTituloRecompra;
		System.out.println(update);
		Statement st=null;
		try {
			st=connMaria.createStatement();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		try {
			st.executeUpdate(update);
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
	public void updateBaixadoFromRGB(Connection connMaria)
	{
		int baixadoInt=0;
		if(this.baixado)
		{
			baixadoInt=1;
		}
		String update="update BMA.titulo_recompra set"
								+" baixado="+baixadoInt
								+" where id_titulo_recompra="+this.idTituloRecompra;
		System.out.println(update);
		Statement st=null;
		try {
			st=connMaria.createStatement();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		try {
			st.executeUpdate(update);
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
	public void updateBaixadoAfterCompletion(Connection connMaria)
	{
		int baixadoInt=0;
		if(this.baixado)
		{
			baixadoInt=1;
		}
		String update="update BMA.titulo_recompra set"
								+" baixado="+baixadoInt
								+" where id_titulo_recompra="+this.idTituloRecompra;
		System.out.println(update);
		Statement st=null;
		try {
			st=connMaria.createStatement();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		try {
			st.executeUpdate(update);
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
	public void updateEfetuado(Connection connMaria)
	{
		int efetuadoInt=0;
		if(this.efetuado)
		{
			efetuadoInt=1;
		}
		String update="update BMA.titulo_recompra set"
								+" efetuado="+efetuadoInt
								+" where id_titulo_recompra="+this.idTituloRecompra;
		System.out.println(update);
		Statement st=null;
		try {
			st=connMaria.createStatement();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		try {
			st.executeUpdate(update);
		} catch (SQLException e) {
			e.printStackTrace();
		}
		
	}

	public static boolean checkAberto(Connection connMSS, OperacaoRecompra operacaoRecompra, String identificacaoTitulo, double valor, Date vencimento)
	{
		boolean aberto=false;
		String query="select * from BMA.dbo.titulo"
							+ " where empresa='"+operacaoRecompra.getEmpresa().getApelido()+"'"
							+ " and cedente='"+operacaoRecompra.getCedente().getApelido()+"'"
							+ " and duplicata='"+identificacaoTitulo+"'"
							+ " and valor="+valor;
//		System.out.println(query);
		
		Statement st=null;
		try {
			st=connMSS.createStatement();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		Date dataQuitacao=null;
		
		ResultSet rs=null;
		try {
			rs=st.executeQuery(query);
			while(rs.next())
			{
				dataQuitacao=rs.getDate("quitacao");				
				if(dataQuitacao==null)
				{
//					System.out.println("Null: "+dataQuitacao);
				}
				else
				{
//					System.out.println("Text: "+dataQuitacao);					
				}

			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		
		if(dataQuitacao==null)
		{
			aberto=true;
		}
		return aberto;
	}

	public void checkTituloValido(Connection connMSS)
	{
		
		String query="select * from BMA.dbo.titulo"
							+ " where empresa='"+this.empresa.getApelido()+"'"
							+ " and cedente='"+this.cedente.getApelido()+"'"
							+ " and duplicata='"+this.identificacaoTitulo+"'"
							+ " and valor="+this.valor;
		System.out.println(query);
		
		Statement st=null;
		try {
			st=connMSS.createStatement();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		Date dataQuitacao=null;
		
		ResultSet rs=null;
		try {
			rs=st.executeQuery(query);
			while(rs.next())
			{
				dataQuitacao=rs.getDate("quitacao");
				this.setAbatimento(rs.getDouble("abatimento"));
				this.setValorTotal(rs.getDouble("total"));
				this.setNossoNumero(rs.getString("seuno"));
				this.setTipoCobranca(rs.getString("tcobran"));
				if(dataQuitacao==null)
				{
					System.out.println("Nao quitado!");
				}
				else
				{
					System.out.println("Titulo quitado em: "+dataQuitacao);					
				}

			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		
		if(dataQuitacao!=null)
		{
			this.setQuitado(true);
		}
		
		query="select top 1 * from BMA.dbo.CRITICAS"
				+ " where empresa='"+this.getEmpresa().getApelido()+"'"
				+ " and cedente='"+this.getCedente().getApelido()+"'"
				+ " and duplicata='"+this.identificacaoTitulo+"'"
				+ " order by data desc";

		System.out.println(query);
		
		rs=null;
		try 
		{
			rs=st.executeQuery(query);
			while(rs.next())
			{
				int codigo=rs.getInt("codigo");
				if(codigo==201 || codigo==205)
				{
					this.setEnviadoCartorio(true);
				}
				if(codigo==210)
				{
					this.setRecebimentoCartorio(true);
				}
			}
		} catch (SQLException e) {
		e.printStackTrace();
		}
		
		long vencimentoMenosSolicitacao=0;
		long antiguidadeSolicitacao=0;
		if(this.dataSolicitacao!=null)
		{
			vencimentoMenosSolicitacao=TimeUnit.DAYS.convert(this.getVencimento().getTime()-this.dataSolicitacao.getTime(), TimeUnit.MILLISECONDS);
			antiguidadeSolicitacao=TimeUnit.DAYS.convert(Calendar.getInstance().getTime().getTime()-this.dataSolicitacao.getTime(), TimeUnit.MILLISECONDS);
		}
		
		System.out.println("VencimentoMenosSolicitacao: " + vencimentoMenosSolicitacao);
		System.out.println("AntiguidadeSolicitacao: "+antiguidadeSolicitacao);
		if(vencimentoMenosSolicitacao>5)
		{
			this.vencimentoDistante=true;
		}
		if(antiguidadeSolicitacao>0)
		{
			this.solicitacaoAntiga=true;
		}
		String keyEmpresaCedente=this.empresa.getApelido().toUpperCase()+this.cedente.getApelido().toUpperCase();
		if(BuilderRecompraRGB.execoes.get(keyEmpresaCedente)!=null)
		{
			this.exception=true;
		}
	}
	

	public static TituloRecompra checkTitulo(Connection connMSS, Empresa empresa, Cedente cedente, String identificacaoTitulo, double valor, Date dataVencimento, Date dataSolicitacao )
	{
		TituloRecompra tituloRecompra = new TituloRecompra();
		tituloRecompra.setEmpresa(empresa);
		tituloRecompra.setCedente(cedente);
		tituloRecompra.setIdentificacaoTitulo(identificacaoTitulo);
		tituloRecompra.setVencimento(dataVencimento);
		tituloRecompra.setDataSolicitacao(dataSolicitacao);
		tituloRecompra.setValor(valor);
		
		String query="select * from BMA.dbo.titulo"
							+ " where empresa='"+empresa.getApelido()+"'"
							+ " and cedente='"+cedente.getApelido()+"'"
							+ " and duplicata='"+identificacaoTitulo+"'"
							+ " and valor="+valor;
		System.out.println(query);
		
		Statement st=null;
		try {
			st=connMSS.createStatement();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		Date dataQuitacao=null;
		
		ResultSet rs=null;
		try {
			rs=st.executeQuery(query);
			while(rs.next())
			{
				dataQuitacao=rs.getDate("quitacao");
				tituloRecompra.setAbatimento(rs.getDouble("abatimento"));
				tituloRecompra.setValorTotal(rs.getDouble("total"));
				tituloRecompra.setNossoNumero(rs.getString("seuno"));
				if(dataQuitacao==null)
				{
					System.out.println("Nao quitado!");
				}
				else
				{
					System.out.println("Titulo quitado em: "+dataQuitacao);					
				}

			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		
		if(dataQuitacao!=null)
		{
			tituloRecompra.setQuitado(true);
		}
		
		query="select top 1 * from BMA.dbo.CRITICAS"
				+ " where empresa='"+tituloRecompra.getEmpresa().getApelido()+"'"
				+ " and cedente='"+tituloRecompra.getCedente().getApelido()+"'"
				+ " and duplicata='"+tituloRecompra.identificacaoTitulo+"'"
				+ " order by data desc";

		System.out.println(query);
		
		rs=null;
		try 
		{
			rs=st.executeQuery(query);
			while(rs.next())
			{
				int codigo=rs.getInt("codigo");
				if(codigo==201 || codigo==205)
				{
					tituloRecompra.setEnviadoCartorio(true);
				}
				if(codigo==210)
				{
					tituloRecompra.setRecebimentoCartorio(true);
				}
			}
		} catch (SQLException e) {
		e.printStackTrace();
		}
		
		long vencimentoMenosSolicitacao=0;
		long antiguidadeSolicitacao=0;
		if(tituloRecompra.dataSolicitacao!=null)
		{
			vencimentoMenosSolicitacao=TimeUnit.DAYS.convert(tituloRecompra.getVencimento().getTime()-tituloRecompra.dataSolicitacao.getTime(), TimeUnit.MILLISECONDS);
			antiguidadeSolicitacao=TimeUnit.DAYS.convert(Calendar.getInstance().getTime().getTime()-tituloRecompra.dataSolicitacao.getTime(), TimeUnit.MILLISECONDS);
		}
		
		
		
		System.out.println("VencimentoMenosSolicitacao: " + vencimentoMenosSolicitacao);
		System.out.println("AntiguidadeSolicitacao: "+antiguidadeSolicitacao);
		if(vencimentoMenosSolicitacao>5)
		{
			tituloRecompra.vencimentoDistante=true;
		}
		if(antiguidadeSolicitacao>0)
		{
			tituloRecompra.solicitacaoAntiga=true;
		}
		String keyEmpresaCedente=tituloRecompra.empresa.getApelido().toUpperCase()+tituloRecompra.cedente.getApelido().toUpperCase();
		if(BuilderRecompraRGB.execoes.get(keyEmpresaCedente)!=null)
		{
			tituloRecompra.exception=true;
		}
		return tituloRecompra;
	}
	
	public void updateAfterCheck(Connection connMaria)
	{
		String update="update BMA.titulo_recompra"
								+ " set valor_total="+this.valorTotal
								+", nosso_numero="+"'"+this.nossoNumero+"'"
								+", abatimento="+this.abatimento
								+" where id_titulo_recompra="+this.idTituloRecompra;
		Statement st=null;
		try {
			st=connMaria.createStatement();
		} catch (SQLException e) {
			e.printStackTrace();
		}		
		try {
			st.executeUpdate(update);
		} catch (SQLException e) {
			e.printStackTrace();
		}		
	}


	public void checkTitulo(Connection connMSS, Connection connMariaDB)
	{
		String query="select * from BMA.dbo.titulo"
							+ " where empresa='"+this.getEmpresa().getApelido()+"'"
							+ " and cedente='"+this.getCedente().getApelido()+"'";
		if(this.tipoTitulo.toLowerCase().contains("duplicata"))
		{
							query+= " and duplicata='"+identificacaoTitulo+"'";
		}
		else 
		{
			query+= " and cheque='"+identificacaoTitulo+"'";
		}
		
		query+= " and valor="+valor;
		System.out.println(query);
		
		Statement st=null;
		try {
			st=connMSS.createStatement();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		Date dataQuitacao=null;
		
		ResultSet rs=null;
		try {
			rs=st.executeQuery(query);
			while(rs.next())
			{
				dataQuitacao=rs.getDate("quitacao");		
				this.abatimento=rs.getDouble("abatimento");
				this.valorTotal=rs.getDouble("total");
				this.nossoNumero=rs.getString("seuno");
				this.tipoCobranca=rs.getString("tcobran");
				String tipo=rs.getString("tipo");
				if(tipo.toLowerCase().contains("d"))
				{
					this.duplicata=true;
				}
				if(tipo.toLowerCase().contains("d"))
				{
					this.cheque=true;
				}
						
				if(dataQuitacao==null)
				{
					System.out.println("Nao quitado!");
				}
				else
				{
					System.out.println("Titulo quitado em: "+dataQuitacao);					
					this.quitado=true;
					this.baixado=true;
				}
				this.exists=true;
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		
		if(!this.exists)
		{
			System.out.println("*********************************************************************");
			System.out.println("*********************************************************************");
			System.out.println("Titulo não existe no banco de dados!");
			this.show();
			System.out.println(query);
			System.out.println("*********************************************************************");
			System.out.println("*********************************************************************");
//			this.setBaixado(true);
//			this.updateBaixadoAfterCompletion(connMariaDB);
		}
		else
		{	
		
			if(dataQuitacao!=null)
			{
				this.setQuitado(true);
			}
			
			query="select top 1 * from BMA.dbo.CRITICAS"
					+ " where empresa='"+this.getEmpresa().getApelido()+"'"
					+ " and cedente='"+this.getCedente().getApelido()+"'"
					+ " and duplicata='"+this.identificacaoTitulo+"'"
					+ " order by data desc";
			System.out.println(query);
			
			rs=null;
			try 
			{
				rs=st.executeQuery(query);
				while(rs.next())
				{
					int codigo=rs.getInt("codigo");
					if(codigo==201 || codigo==205)
					{
						this.setEnviadoCartorio(true);
					}
					if(codigo==210)
					{
						this.setRecebimentoCartorio(true);
					}
				}
			} catch (SQLException e) {
			e.printStackTrace();
			}
			
			long vencimentoMenosSolicitacao=0;
			long antiguidadeSolicitacao=0;
			if(this.dataSolicitacao!=null)
			{
				vencimentoMenosSolicitacao=TimeUnit.DAYS.convert(this.getVencimento().getTime()-this.dataSolicitacao.getTime(), TimeUnit.MILLISECONDS);
				antiguidadeSolicitacao=TimeUnit.DAYS.convert(Calendar.getInstance().getTime().getTime()-this.dataSolicitacao.getTime(), TimeUnit.MILLISECONDS);
			}
			
			int numeroDataSolicicitacao=Integer.parseInt(sdfn.format(this.dataSolicitacao));
			int numeroDataAtual=Integer.parseInt(sdfn.format(Calendar.getInstance().getTime()));
			
			antiguidadeSolicitacao=numeroDataAtual-numeroDataSolicicitacao;
			
			
			System.out.println("VencimentoMenosSolicitacao: " + vencimentoMenosSolicitacao);
			System.out.println("AntiguidadeSolicitacao: "+antiguidadeSolicitacao);
			
			if(this.isExists())
			{
				if(vencimentoMenosSolicitacao>5)
				{
					this.vencimentoDistante=true;
				}
				if(antiguidadeSolicitacao>0)
				{
					this.solicitacaoAntiga=true;
				}
				String keyEmpresaCedente=this.empresa.getApelido().toUpperCase()+this.cedente.getApelido().toUpperCase();
				if(BuilderRecompraRGB.execoes.get(keyEmpresaCedente)!=null)
				{
					this.exception=true;
				}
				this.checkTipoCobranca();
			}
		}
	}
	
	
	public static ArrayList<TituloRecompra> titulosRecompra(Connection connMaria, Connection connMSS, Empresa empresa, Cedente cedente , int idOperacaoRecompra)
	{
		ArrayList<TituloRecompra> titulosRecompra=new ArrayList<>();
		String query="select * from BMA.titulo_recompra"
							+ " where operacao_recompra_id_operacao_recompra="+idOperacaoRecompra;
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
				TituloRecompra tituloRecompra=new TituloRecompra(connMaria, connMSS, empresa, cedente, idOperacaoRecompra, rs.getInt("id_titulo_recompra"));
				tituloRecompra.checkTitulo(connMSS,connMaria);
				tituloRecompra.updateAfterCheck(connMaria);
				titulosRecompra.add(tituloRecompra);
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return titulosRecompra;
	}
	public void show()
	{
		System.out.println("    -- Titulo Recompra  --");
		System.out.println("idTituloRecompra: "+this.idTituloRecompra);
		System.out.println("Empresa: "+this.empresa.getApelido());
		System.out.println("Cedente: "+ this.cedente.getApelido());
		System.out.println("OperacaoRecompra: " + this.idOperacaoRecompra);
		System.out.println("IdentificacaoTitulo: "+this.identificacaoTitulo);
		System.out.println("TipoTitulo: "+this.tipoTitulo);
		System.out.println("NossoNumero: "+this.nossoNumero);
		System.out.println("Valor titulo: "+ this.valorTotal);
		System.out.println("Vencimento: "+ sdf.format(this.vencimento));
		if(dataSolicitacao!=null)
		{
			System.out.println("DataSolicitacao: "+ sdf.format(this.dataSolicitacao));
		}
		System.out.println("JurosMaisTaxa: "+this.mora);
		System.out.println("Valor recompra: "+ this.valorRecompra);
		System.out.println("Vencido: "+this.vencido);
		System.out.println("VencimentoDistante: "+this.vencimentoDistante);
		System.out.println("SolicitacaoAntiga: "+this.solicitacaoAntiga);
		System.out.println("Efetuado: "+this.efetuado);
		System.out.println("Baixado: "+this.baixado);
	}
	
	public void showShort()
	{
		System.out.print("IdentificacaoTitulo: "+this.identificacaoTitulo + " NossoNro: " +this.nossoNumero+" TipoCobranca: "+this.tipoCobranca+ " Valor titulo: "+ this.valorTotal +" Vencimento: "+ sdf.format(this.vencimento));
		if(dataSolicitacao!=null)
		{
			System.out.print(" DataSolicitacao: "+ sdf.format(this.dataSolicitacao));
		}
		System.out.print(" JurosMaisTaxa: "+this.mora);
		System.out.print(" Valor recompra: "+ this.valorRecompra 
										+ " Vencido: "+this.vencido 
										+ "VencimentoDistante: "+this.vencimentoDistante 
										+" SolicitacaoAntiga: "+this.solicitacaoAntiga
										+" Efetuado: "+this.efetuado
										+" Baixado: "+this.baixado
										+ "\n");
	}

	
	public int getIdTituloRecompra() {
		return idTituloRecompra;
	}
	public void setIdTituloRecompra(int idTituloRecompra) {
		this.idTituloRecompra = idTituloRecompra;
	}
	public static SimpleDateFormat getSdfr() {
		return sdfr;
	}
	public static void setSdfr(SimpleDateFormat sdfr) {
		TituloRecompra.sdfr = sdfr;
	}
	public static SimpleDateFormat getSdf() {
		return sdf;
	}
	public static void setSdf(SimpleDateFormat sdf) {
		TituloRecompra.sdf = sdf;
	}
	public String getIdentificacaoTitulo() {
		return identificacaoTitulo;
	}
	public void setIdentificacaoTitulo(String identificacaoTitulo) {
		this.identificacaoTitulo = identificacaoTitulo;
	}
	public double getValor() {
		return valor;
	}
	public void setValor(double valor) {
		this.valor = valor;
	}
	public Date getVencimento() {
		return vencimento;
	}
	public void setVencimento(Date vencimento) {
		this.vencimento = vencimento;
	}

	public Empresa getEmpresa() {
		return empresa;
	}

	public void setEmpresa(Empresa empresa) {
		this.empresa = empresa;
	}

	public Cedente getCedente() {
		return cedente;
	}

	public void setCedente(Cedente cedente) {
		this.cedente = cedente;
	}

	public double getValorRecompra() {
		return valorRecompra;
	}

	public void setValorRecompra(double valorRecompra) {
		this.valorRecompra = valorRecompra;
	}

	public boolean isVencido() {
		return vencido;
	}

	public void setVencido(boolean vencido) {
		this.vencido = vencido;
	}

	public static SimpleDateFormat getSdfn() {
		return sdfn;
	}

	public static void setSdfn(SimpleDateFormat sdfn) {
		TituloRecompra.sdfn = sdfn;
	}

	public double getDeducao() {
		return deducao;
	}

	public void setDeducao(double deducao) {
		this.deducao = deducao;
	}

	public double getMora() {
		return mora;
	}

	public void setMora(double mora) {
		this.mora = mora;
	}

	public double getDiferencaLiquidacao() {
		return diferencaLiquidacao;
	}

	public void setDiferencaLiquidacao(double diferencaLiquidacao) {
		this.diferencaLiquidacao = diferencaLiquidacao;
	}

	public String toHTML() {
		String htmlCode="";
		htmlCode+="<tr>"
				+ "<td>Recompra</td>"
				+ "<td>"+sdf.format(this.vencimento) +"</td>"
				+ "<td>"+this.identificacaoTitulo+"</td>";
		htmlCode+= "<td>"+df.format(this.valorTotal) +"</td>";
		htmlCode+= "<td>"+this.empresa.getApelido()+"</td>"
				+ "<td>"+this.cedente.getApelido()+"</td>";
		htmlCode+= "<td>"+df.format(this.valorCorrigido) +"</td>";
		htmlCode+= "<td>"+df.format(this.mora)+"</td>"
						+ "<td>"+df.format(this.valorRecompra) +"</td>"
				+ "</tr>";
		return htmlCode;
	}

	public double getValorCorrigido() {
		return this.valorCorrigido;
	}

	public void setValorCorrigido(double valorCorrigido) {
		this.valorCorrigido = valorCorrigido;
	}

	public static DecimalFormat getDf() {
		return df;
	}

	public static void setDf(DecimalFormat df) {
		TituloRecompra.df = df;
	}

	public int getIdOperacaoRecompra() {
		return this.idOperacaoRecompra;
	}

	public void setIdOperacaoRecompra(int idOperacaoRecompra) {
		this.idOperacaoRecompra = idOperacaoRecompra;
	}

	public boolean isRecebimentoCartorio() {
		return this.recebimentoCartorio;
	}

	public void setRecebimentoCartorio(boolean recebimentoCartorio) {
		this.recebimentoCartorio = recebimentoCartorio;
	}

	public boolean isEnviadoCartorio() {
		return this.enviadoCartorio;
	}

	public void setEnviadoCartorio(boolean enviadoCartorio) {
		this.enviadoCartorio = enviadoCartorio;
	}

	public Date getDataSolicitacao() {
		return this.dataSolicitacao;
	}

	public void setDataSolicitacao(Date dataSolicitacao) {
		this.dataSolicitacao = dataSolicitacao;
	}

	public boolean isVencimentoDistante() {
		return this.vencimentoDistante;
	}

	public void setVencimentoDistante(boolean vencimentoDistante) {
		this.vencimentoDistante = vencimentoDistante;
	}

	public boolean isSolicitacaoAntiga() {
		return this.solicitacaoAntiga;
	}

	public void setSolicitacaoAntiga(boolean solicitacaoAntiga) {
		this.solicitacaoAntiga = solicitacaoAntiga;
	}

	public boolean isException() {
		return this.exception;
	}

	public void setException(boolean exception) {
		this.exception = exception;
	}

	public boolean isQuitado() {
		return this.quitado;
	}

	public void setQuitado(boolean quitado) {
		this.quitado = quitado;
	}

	public double getValorTotal() {
		return this.valorTotal;
	}

	public void setValorTotal(double total) {
		this.valorTotal = total;
	}

	public double getAbatimento() {
		return this.abatimento;
	}

	public void setAbatimento(double abatimento) {
		this.abatimento = abatimento;
	}

	public boolean isBaixado() {
		return this.baixado;
	}

	public void setBaixado(boolean baixado) {
		this.baixado = baixado;
	}

	public boolean isEfetuado() {
		return this.efetuado;
	}

	public void setEfetuado(boolean efetuado) {
		this.efetuado = efetuado;
	}

	public String getNossoNumero() {
		return this.nossoNumero;
	}

	public void setNossoNumero(String nossoNumero) {
		this.nossoNumero = nossoNumero;
	}

	public boolean isBaixadoBradesco() {
		return this.baixadoBradesco;
	}

	public void setBaixadoBradesco(boolean baixadoBradesco) {
		this.baixadoBradesco = baixadoBradesco;
	}

	public String getTipoCobranca() {
		return this.tipoCobranca;
	}

	public void setTipoCobranca(String tipoCobranca) {
		this.tipoCobranca = tipoCobranca;
	}

	public boolean isPodeRecomprar() {
		return this.podeRecomprar;
	}

	public void setPodeRecomprar(boolean podeRecomprar) {
		this.podeRecomprar = podeRecomprar;
	}

	public String getMotivoNaoPodeRecomprar() {
		return this.motivoNaoPodeRecomprar;
	}

	public void setMotivoNaoPodeRecomprar(String motivoNaoPodeRecomprar) {
		this.motivoNaoPodeRecomprar = motivoNaoPodeRecomprar;
	}

	public boolean isAcatarBaixa() {
		return this.acatarBaixa;
	}

	public void setAcatarBaixa(boolean acatarBaixa) {
		this.acatarBaixa = acatarBaixa;
	}

	public boolean isDuplicata() {
		return this.duplicata;
	}

	public void setDuplicata(boolean duplicata) {
		this.duplicata = duplicata;
	}

	public boolean isCheque() {
		return this.cheque;
	}

	public void setCheque(boolean cheque) {
		this.cheque = cheque;
	}

	public boolean isExists() {
		return this.exists;
	}

	public void setExists(boolean exists) {
		this.exists = exists;
	}

	public String getTipoTitulo() {
		return this.tipoTitulo;
	}

	public void setTipoTitulo(String tipoTitulo) {
		this.tipoTitulo = tipoTitulo;
	}
}
