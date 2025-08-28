package conta_grafica;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

public class Lancamento {
	private int numeroSequencial=0;
	private Date dataLancamento = null;
	private String classificacao="";
	private String complemento="";
	private double valor=0;
	private double saldo=0;
	private static SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
	private static  DecimalFormat df = new DecimalFormat("#0.00#");

	public Lancamento()
	{
		
	}
	
	public Lancamento(int numeroSequencial, Date dataLancamento, String classificacao, String complemento, double valor, double saldo)
	{
		this.numeroSequencial=numeroSequencial;
		this.dataLancamento=dataLancamento;
		this.classificacao=classificacao;
		this.complemento=complemento;
		this.valor=valor;
		this.saldo=saldo;
	}
	
	public void show()
	{
		System.out.println(this.numeroSequencial+"\t"+sdf.format(this.dataLancamento) +"\t"+this.classificacao+"\t"+this.complemento+"\t"+df.format(this.valor) +"\t"+df.format(this.saldo));
	}	
	
	public String toHTML()
	{
		String htmlCode="";		
		htmlCode+="<tr>"
				+ "<td>"+this.numeroSequencial+"</td>"
				+ "<td>"+sdf.format(this.dataLancamento) +"</td>"
				+ "<td>"+this.classificacao+"</td>"
				+ "<td>"+this.complemento+"</td>";
		if(this.valor>=0)
		{
				htmlCode+= "<td>"+df.format(this.valor) +"</td><td></td>";
		}
		else
		{
			htmlCode+= "<td></td><td>"+df.format(this.valor) +"</td>";
		}
		htmlCode+="<td>"+df.format(this.saldo)+"</td>"
				+ "</tr>";
		return htmlCode;
	}	
	
	public int getNumeroSequencial() {
		return numeroSequencial;
	}
	public void setNumeroSequencial(int numeroSequencial) {
		this.numeroSequencial = numeroSequencial;
	}
	public Date getDataLancamento() {
		return dataLancamento;
	}
	public void setDataLancamento(Date dataLancamento) {
		this.dataLancamento = dataLancamento;
	}
	public String getClassificacao() {
		return classificacao;
	}
	public void setClassificacao(String classificacao) {
		this.classificacao = classificacao;
	}
	public String getComplemento() {
		return complemento;
	}
	public void setComplemento(String complemento) {
		this.complemento = complemento;
	}
	public double getSaldo() {
		return saldo;
	}
	public void setSaldo(double saldo) {
		this.saldo = saldo;
	}

	public double getValor() {
		return valor;
	}

	public void setValor(double valor) {
		this.valor = valor;
	}

	public static SimpleDateFormat getSdf() {
		return sdf;
	}

	public static void setSdf(SimpleDateFormat sdf) {
		Lancamento.sdf = sdf;
	}

	public static DecimalFormat getDf() {
		return df;
	}

	public static void setDf(DecimalFormat df) {
		Lancamento.df = df;
	}
	
	
}
