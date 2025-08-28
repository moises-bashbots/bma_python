package conta_grafica;

import java.text.SimpleDateFormat;
import java.util.Date;

public class LancamentoBradesco {
	private Date dataLancamento=null;
	private String descricao="";
	private String documento="";
	private String tipoLancamento="";
	private double valor=0.0;
	private double saldo=0.0;
	private static SimpleDateFormat sdf=new SimpleDateFormat("dd/MM/yyyy");
	private static SimpleDateFormat sdfd=new SimpleDateFormat("yyyy-MM-dd");

	
	public LancamentoBradesco(Date dataLancamento, String descricao, String documento, String tipoLancamento, double valor, double saldo)
	{
		this.dataLancamento=dataLancamento;
		this.descricao=descricao;
		this.documento=documento;
		this.tipoLancamento=tipoLancamento;
		this.valor=valor;
		this.saldo=saldo;
	}
	
	public void show()
	{
		System.out.println(sdfd.format(this.dataLancamento) +" | "+this.descricao+" | "+this.documento + " | "+this.tipoLancamento+ " | "+this.valor+" | "+this.saldo);
	}

	public Date getDataLancamento() {
		return this.dataLancamento;
	}

	public void setDataLancamento(Date dataLancamento) {
		this.dataLancamento = dataLancamento;
	}

	public String getDescricao() {
		return this.descricao;
	}

	public void setDescricao(String descricao) {
		this.descricao = descricao;
	}

	public String getDocumento() {
		return this.documento;
	}

	public void setDocumento(String documento) {
		this.documento = documento;
	}

	public String getTipoLancamento() {
		return this.tipoLancamento;
	}

	public void setTipoLancamento(String tipoLancamento) {
		this.tipoLancamento = tipoLancamento;
	}

	public double getValor() {
		return this.valor;
	}

	public void setValor(double valor) {
		this.valor = valor;
	}

	public double getSaldo() {
		return this.saldo;
	}

	public void setSaldo(double saldo) {
		this.saldo = saldo;
	}
}
