package conta_grafica;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import cedente.Cedente;
import empresa.Empresa;
import utils.Utils;

public class Extrato {
	private Date dataInicio=null;
	private Date dataFinal=null;
	private Saldo saldoInicial=new Saldo();
	private Saldo saldoFinal=new Saldo();
	private ArrayList<Lancamento> lancamentos = new ArrayList<Lancamento>();
	private static SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
	private static  DecimalFormat df = new DecimalFormat("#0.00#");
	
	public Extrato()
	{
		
	}
	
	public Extrato(Connection connMSS, Empresa empresa, Cedente cedente, Date dataInicial, Date dataFinal)
	{
		this.saldoInicial = new Saldo(connMSS, empresa, cedente, dataInicial);
		this.saldoFinal = new Saldo(connMSS, empresa, cedente, dataFinal);
		String query="select l.nseq as numero_sequencial, l.datal as data_lancamento, c.apelido as classificacao, l.complem as complemento, l.valor"
				+ " from BMA.dbo.lanca l, BMA.dbo.classif_cc c"
				+ " where l.empresa='"+empresa.getApelido()+"'"
				+ " and l.cedente='"+cedente.getApelido()+"'"
				+ " and c.nclassif=l.nclassif"
				+ " and l.datal > '"+sdf.format(dataInicial)+"'"
				+ " and l.datal <= '"+sdf.format(dataFinal)+"'"
						+ " order by data_lancamento"; 
		System.out.println(query);
		Statement st=null;
		try {
			st=connMSS.createStatement();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		double valorSaldoAtual=this.saldoInicial.getValor();
		ResultSet rs = null;
		try {
			rs=st.executeQuery(query);
			while(rs.next())
			{
				int numeroSequencial=rs.getInt("numero_sequencial");
				Date dataLancamento = rs.getDate("data_lancamento");
				String classificacao = rs.getString("classificacao");
				String complemento= rs.getString("complemento");
				double valor = rs.getDouble("valor");
				valorSaldoAtual=valorSaldoAtual+valor;
				Lancamento lancamento= new Lancamento(numeroSequencial, dataLancamento, classificacao, complemento, valor, valorSaldoAtual);
				lancamentos.add(lancamento);
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
	public void show()
	{
		System.out.println("**************************************************************************************");
		System.out.println("** EXTRATO do Cedente "+this.saldoInicial.getCedente().getParticipante().getRazaoSocial() + " na empresa "+this.saldoInicial.getEmpresa().getRazaoSocial());
		System.out.println("** DATA INICIAL: " + sdf.format(this.saldoInicial.getDataSaldo()) + " DATA FINAL: " +sdf.format(saldoFinal.getDataSaldo()));
		System.out.println("**************************************************************************************");
		System.out.println("Saldo anterior: "+ sdf.format(this.saldoInicial.getDataSaldo()) +" "+df.format(this.saldoInicial.getValor()));
		for(Lancamento lancamento: this.lancamentos)
		{
			lancamento.show();
		}
		System.out.println("Saldo atual: "+ sdf.format(this.saldoFinal.getDataSaldo()) +" "+df.format(this.saldoFinal.getValor()));		
	}

	public String toHTML(Empresa empresa)
	{
		String htmlCode="";		
		htmlCode+="<p>";
		htmlCode+="<h2>EXTRATO</h2>"
				+ "<b>Cedente: </b>"+this.saldoInicial.getCedente().getParticipante().getRazaoSocial() + " <b>Empresa: </b>"+this.saldoInicial.getEmpresa().getRazaoSocial();
		htmlCode+="<br>";
		htmlCode+="Data inicial: <b>" + sdf.format(this.saldoInicial.getDataSaldo()) + "</b> Data final: <b>" +sdf.format(saldoFinal.getDataSaldo())+"</b>";
		htmlCode+="<br>";
		htmlCode+="Saldo anterior: <b>"+ sdf.format(this.saldoInicial.getDataSaldo()) +" R$ "+df.format(this.saldoInicial.getValor())+"</b>";
		htmlCode+="</p>";
		htmlCode+="<table>";
		htmlCode+="<tr>"
								+ "<th>NroSequencial</th>"
								+ "<th>DataLan&ccedil;amento</th>"
								+ "<th>Classifica&ccedil;&atilde;o</th>"
								+ "<th>Complemento</th>"
								+ "<th>Cr&eacute;dito</th>"
								+ "<th>D&eacute;bito</th>"
								+ "<th>Saldo</th>"
							+ "</tr>";
		for(Lancamento lancamento: this.lancamentos)
		{
			htmlCode+=lancamento.toHTML();
		}	
		htmlCode+="</table>";
		htmlCode+="<p>"
							+ "Saldo final: <b>"+ sdf.format(this.saldoFinal.getDataSaldo()) +" R$ "+df.format(this.saldoFinal.getValor())+"</b>"
							+ "</p>";
		
		htmlCode+="<p><b>Empresa "+empresa.getApelido().toUpperCase()+"</b><br>"
				+ "<br>Dados banc&aacute;rios:<br>"
				+empresa.getRazaoSocial().toUpperCase()+"<br>"
				+ "CNPJ: "+Utils.formatarCNPJ(empresa.getCnpj())+"<br>"
				+ empresa.getDadosBancariosEmpresa().getBanco().getNomeBanco().toUpperCase()+"<br>"
				+ "C&Oacute;DIGO DE COMPENSA&Ccedil;&Atilde;O: "+empresa.getDadosBancariosEmpresa().getBanco().getCodigoCompe()+"<br>"
				+ "AG&Ecirc;NCIA: "+empresa.getDadosBancariosEmpresa().getAgencia()+"-"+empresa.getDadosBancariosEmpresa().getDigitoAgencia()+"<br>"
				+ "CONTA: "+empresa.getDadosBancariosEmpresa().getConta()+"-"+empresa.getDadosBancariosEmpresa().getDigitoConta()+"<br>"
				+ "PIX: "+Utils.formatarCNPJ(empresa.getCnpj())
				+"</p>";		
		return htmlCode;
	}
	
	public Date getDataInicio() {
		return dataInicio;
	}

	public void setDataInicio(Date dataInicio) {
		this.dataInicio = dataInicio;
	}

	public Date getDataFinal() {
		return dataFinal;
	}

	public void setDataFinal(Date dataFinal) {
		this.dataFinal = dataFinal;
	}

	public Saldo getSaldoInicial() {
		return saldoInicial;
	}

	public void setSaldoInicial(Saldo saldoInicial) {
		this.saldoInicial = saldoInicial;
	}

	public Saldo getSaldoFinal() {
		return saldoFinal;
	}

	public void setSaldoFinal(Saldo saldoFinal) {
		this.saldoFinal = saldoFinal;
	}

	public ArrayList<Lancamento> getLancamentos() {
		return lancamentos;
	}

	public void setLancamentos(ArrayList<Lancamento> lancamentos) {
		this.lancamentos = lancamentos;
	}

	public static SimpleDateFormat getSdf() {
		return sdf;
	}

	public static void setSdf(SimpleDateFormat sdf) {
		Extrato.sdf = sdf;
	}

	public static DecimalFormat getDf() {
		return df;
	}

	public static void setDf(DecimalFormat df) {
		Extrato.df = df;
	}
	
	

}
