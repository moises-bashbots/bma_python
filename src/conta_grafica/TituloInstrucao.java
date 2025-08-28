package conta_grafica;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.concurrent.TimeUnit;

import cedente.Cedente;
import empresa.Empresa;
import utils.Utils;

public class TituloInstrucao {
	private Empresa empresa = new Empresa();
	private Cedente cedente = new Cedente();
	private static SimpleDateFormat sdfr=new SimpleDateFormat("dd/MM/yyyy");
	private static SimpleDateFormat sdf=new SimpleDateFormat("yyyy-MM-dd");
	private static SimpleDateFormat sdfn=new SimpleDateFormat("yyyyMMdd");
	private static  DecimalFormat df = new DecimalFormat("#0.00#");
	private String identificacaoTitulo="";
	private String tipoCobranca="";

	private double valor=0;
	private String nomeProduto="";
	private Date vencimento=null;
	private Date dataSolicitacao=null;
	private boolean abatido=false;
	private boolean vencido=false;
	private boolean quitado=false;
	private boolean recebimentoCartorio=false;
	private boolean enviadoCartorio=false;
	private boolean prorrogado=false;
	private boolean baixado=false;
	private boolean baixadoBradesco=false;
	private boolean podeRecomprar=false;
	private boolean acatarBaixa=false;
	private String motivoNaoPodeRecomprar="";
	private boolean exception=false;
	
	public TituloInstrucao()
	{
		
	}
	
	public TituloInstrucao(Empresa empresa, Cedente cedente, String identificacaoTitulo)
	{
		this.empresa=empresa;
		this.cedente=cedente;
		this.identificacaoTitulo=identificacaoTitulo;
	}
	
	public TituloInstrucao(Empresa empresa, Cedente cedente, String identificacaoTitulo, double valor)
	{
		this.empresa=empresa;
		this.cedente=cedente;
		this.identificacaoTitulo=identificacaoTitulo;
		this.valor=valor;
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
	
	public void checkTipoCobranca()
	{
		int codigoTipoCobranca=Integer.parseInt(this.tipoCobranca);
		switch (codigoTipoCobranca) {
		case 19:
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
	
	public boolean checkProdutoProibido(Connection connMSS)
	{
		boolean forbidden=false;
		String query="select t.*, p.Id, p.Descritivo, pc.Empresa, pc.Cedente, pc.IdProdutoAtributo"
							+ " from BMA.dbo.Produto p  WITH (NOLOCK), BMA.dbo.ProdutoCedente pc  WITH (NOLOCK), BMA.dbo.titulo t  WITH (NOLOCK)"
							+ " where p.Descritivo in ('CAPITAL DE GIRO NP','COB SIMPLES GARANTIA','ESCROW/EX COB SIMPLES','RENEGOCIAÇÃO','NOTA COMERCIAL','ESCROW CONCENTRADO','BOLETO OCULTO','INTERCIA CHEQUE','ESCROW BOLETO','ESCROW DEP. BMA FIDC','ESCROW DEP. BMA INTER', 'CCB','COMISSÁRIA', 'CAPITAL DE GIRO','INTERCIA','18 - COBRANÇA SIMPLES ')"
							+ " and pc.IdProdutoAtributo =p.Id"
							+ " and t.id_produto =pc.Id"
							+ " and t.empresa="+"'"+this.empresa+"'"
							+ " and t.cedente="+"'"+this.cedente+"'"
							+ " and t.duplicata = '" +this.identificacaoTitulo + "'";
		System.out.println(query);
		
		Statement st=null;
		try {
			st=connMSS.createStatement();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		Date dataQuitacao=null;
		
		ResultSet rs=null;
		int iForbidden=0;
		try {
			rs=st.executeQuery(query);
			while(rs.next())
			{
				iForbidden++;
				this.nomeProduto=rs.getString("Descritivo");
				this.tipoCobranca=rs.getString("tcobran");
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		
		if(iForbidden>0 && this.nomeProduto.length()>0)
		{
			forbidden=true;
		}
//		checkTipoCobranca();
		return forbidden;
	}

	
	public static boolean checkAberto(Connection connMSS, OperacaoRecompra operacaoRecompra, String identificacaoTitulo, double valor, Date vencimento)
	{
		boolean aberto=false;
		String query="select * from BMA.dbo.titulo  WITH (NOLOCK)"
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
	
	public boolean checkProrrogado(Connection connMSS)
	{
		String query="select * from BMA.dbo.titulo  WITH (NOLOCK)"
							+ " where empresa='"+this.empresa.getApelido()+"'"
							+ " and cedente='"+this.cedente.getApelido()+"'"
							+ " and duplicata='"+this.identificacaoTitulo+"'";
//		System.out.println(query);
		
		Statement st=null;
		try {
			st=connMSS.createStatement();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		this.prorrogado=false;
		
		ResultSet rs=null;
		try {
			rs=st.executeQuery(query);
			while(rs.next())
			{
				Date dataVencimento=rs.getDate("venc");
				Date dataVencimentoNovo=rs.getDate("venc0");
				if(Utils.getDifferenceDays(dataVencimento, dataVencimentoNovo)>0)
				{
					this.prorrogado=true;
				}

			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		
		return this.prorrogado;
	}


	public static TituloInstrucao checkTitulo(Connection connMSS, Empresa empresa, Cedente cedente, String identificacaoTitulo, double valor, Date dataVencimento, Date dataSolicitacao )
	{
		TituloInstrucao tituloRecompra = new TituloInstrucao();
		tituloRecompra.setEmpresa(empresa);
		tituloRecompra.setCedente(cedente);
		tituloRecompra.setIdentificacaoTitulo(identificacaoTitulo);
		tituloRecompra.setVencimento(dataVencimento);
		tituloRecompra.setDataSolicitacao(dataSolicitacao);
		tituloRecompra.setValor(valor);
		
		String query="select * from BMA.dbo.titulo  WITH (NOLOCK)"
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
				if(rs.getDouble("abatimento")>0)
				{
					tituloRecompra.abatido=true;
				}
				else
				{
					tituloRecompra.abatido=false;
				}
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
		
		query="select top 1 * from BMA.dbo.CRITICAS  WITH (NOLOCK)"
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
		

		return tituloRecompra;
	}

	
	public void checkTitulo(Connection connMSS)
	{
		String query="select * from BMA.dbo.titulo  WITH (NOLOCK)"
							+ " where empresa='"+this.getEmpresa().getApelido()+"'"
							+ " and cedente='"+this.getCedente().getApelido()+"'"
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
				if(rs.getDouble("abatimento")>0)
				{
					this.abatido=true;
				}
				else
				{
					this.abatido=false;
				}
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
		
		query="select top 1 * from BMA.dbo.CRITICAS  WITH (NOLOCK)"
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
	
	}
	
	public void show()
	{
		System.out.println("    -- Titulo Recompra  --");
		System.out.println("Empresa: "+this.empresa.getApelido());
		System.out.println("Cedente: "+ this.cedente.getIdCedente());
		System.out.println("IdentificacaoTitulo: "+this.identificacaoTitulo);
		System.out.println("Valor titulo: "+ this.valor);
		System.out.println("Vencimento: "+ sdf.format(this.vencimento));
		System.out.println("Prorrogado:  "+ this.prorrogado);
		if(dataSolicitacao!=null)
		{
			System.out.println("DataSolicitacao: "+ sdf.format(this.dataSolicitacao));
		}
	}
	
	public void showShort()
	{
		System.out.print("IdentificacaoTitulo: "+this.identificacaoTitulo + " Valor titulo: "+ this.valor +" Vencimento: "+ sdf.format(this.vencimento));
		if(dataSolicitacao!=null)
		{
			System.out.print(" DataSolicitacao: "+ sdf.format(this.dataSolicitacao));
		}
	}


	public static SimpleDateFormat getSdfr() {
		return sdfr;
	}
	public static void setSdfr(SimpleDateFormat sdfr) {
		TituloInstrucao.sdfr = sdfr;
	}
	public static SimpleDateFormat getSdf() {
		return sdf;
	}
	public static void setSdf(SimpleDateFormat sdf) {
		TituloInstrucao.sdf = sdf;
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
		TituloInstrucao.sdfn = sdfn;
	}

	public static DecimalFormat getDf() {
		return df;
	}

	public static void setDf(DecimalFormat df) {
		TituloInstrucao.df = df;
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

	public String getNomeProduto() {
		return this.nomeProduto;
	}

	public void setNomeProduto(String nomeProduto) {
		this.nomeProduto = nomeProduto;
	}

	public boolean isProrrogado() {
		return this.prorrogado;
	}

	public void setProrrogado(boolean prorrogado) {
		this.prorrogado = prorrogado;
	}

	public boolean isAbatido() {
		return this.abatido;
	}

	public void setAbatido(boolean abatido) {
		this.abatido = abatido;
	}

	public String getTipoCobranca() {
		return this.tipoCobranca;
	}

	public void setTipoCobranca(String tipoCobranca) {
		this.tipoCobranca = tipoCobranca;
	}

	public boolean isBaixado() {
		return this.baixado;
	}

	public void setBaixado(boolean baixado) {
		this.baixado = baixado;
	}

	public boolean isBaixadoBradesco() {
		return this.baixadoBradesco;
	}

	public void setBaixadoBradesco(boolean baixadoBradesco) {
		this.baixadoBradesco = baixadoBradesco;
	}

	public boolean isPodeRecomprar() {
		return this.podeRecomprar;
	}

	public void setPodeRecomprar(boolean podeRecomprar) {
		this.podeRecomprar = podeRecomprar;
	}

	public boolean isAcatarBaixa() {
		return this.acatarBaixa;
	}

	public void setAcatarBaixa(boolean acatarBaixa) {
		this.acatarBaixa = acatarBaixa;
	}

	public String getMotivoNaoPodeRecomprar() {
		return this.motivoNaoPodeRecomprar;
	}

	public void setMotivoNaoPodeRecomprar(String motivoNaoPodeRecomprar) {
		this.motivoNaoPodeRecomprar = motivoNaoPodeRecomprar;
	}
}
