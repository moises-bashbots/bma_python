package bradesco;

import java.text.SimpleDateFormat;
import java.util.Date;

public class DetalhePagamento 
{
	private String idPagamento="";
	private String status="";
	private String motivo="";
	private String nomeFavorecido="";
	private String cpfCnpj="";
	private String codigoBanco="";
	private String numeroAgencia="";
	private String numeroConta="";
	private String tipoPagamento="";
	private String referenciaEmpresa="";
	private Date dataPagamento=null;
	private double valorPagamento=0.0;
	private String numeroLancamento="";
	
	private static SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
	
	public DetalhePagamento()
	{
		
	}
	
	public void show()
	{
		System.out.println("--------------------------------------------------------------");
		System.out.println("idPagamento: "+this.idPagamento);
		System.out.println("status: "+this.status);
		System.out.println("motivo: "+this.motivo);
		System.out.println("nomeFavorecido: "+this.nomeFavorecido);
		System.out.println("cpfCnpj: "+this.cpfCnpj);
		System.out.println("codigoBanco: "+this.codigoBanco);
		System.out.println("numeroAgencia: "+this.numeroAgencia);
		System.out.println("numeroConta: "+this.numeroConta);
		System.out.println("tipoPagamento: "+this.tipoPagamento);
		System.out.println("referenciaEmpresa: "+this.referenciaEmpresa);
		System.out.println("dataPagamento: "+sdf.format(this.dataPagamento));
		System.out.println("valorPagamento: "+this.valorPagamento);
		System.out.println("numeroLancamento: "+this.numeroLancamento);
	}
	
	public String getIdPagamento() {
		return idPagamento;
	}
	public void setIdPagamento(String idPagamento) {
		this.idPagamento = idPagamento;
	}
	public String getStatus() {
		return status;
	}
	public void setStatus(String status) {
		this.status = status;
	}
	public String getNomeFavorecido() {
		return nomeFavorecido;
	}
	public void setNomeFavorecido(String nomeFavorecido) {
		this.nomeFavorecido = nomeFavorecido;
	}
	public String getCpfCnpj() {
		return cpfCnpj;
	}
	public void setCpfCnpj(String cpfCnpj) {
		this.cpfCnpj = cpfCnpj;
	}
	public String getCodigoBanco() {
		return codigoBanco;
	}
	public void setCodigoBanco(String codigoBanco) {
		this.codigoBanco = codigoBanco;
	}
	public String getNumeroAgencia() {
		return numeroAgencia;
	}
	public void setNumeroAgencia(String numeroAgencia) {
		this.numeroAgencia = numeroAgencia;
	}
	public String getNumeroConta() {
		return numeroConta;
	}
	public void setNumeroConta(String numeroConta) {
		this.numeroConta = numeroConta;
	}
	public String getTipoPagamento() {
		return tipoPagamento;
	}
	public void setTipoPagamento(String tipoPagamento) {
		this.tipoPagamento = tipoPagamento;
	}
	public String getReferenciaEmpresa() {
		return referenciaEmpresa;
	}
	public void setReferenciaEmpresa(String referenciaEmpresa) {
		this.referenciaEmpresa = referenciaEmpresa;
	}
	public Date getDataPagamento() {
		return dataPagamento;
	}
	public void setDataPagamento(Date dataPagamento) {
		this.dataPagamento = dataPagamento;
	}
	public double getValorPagamento() {
		return valorPagamento;
	}
	public void setValorPagamento(double valorPagamento) {
		this.valorPagamento = valorPagamento;
	}
	public String getNumeroLancamento() {
		return numeroLancamento;
	}
	public void setNumeroLancamento(String numeroLancamento) {
		this.numeroLancamento = numeroLancamento;
	}

	public String getMotivo() {
		return motivo;
	}

	public void setMotivo(String motivo) {
		this.motivo = motivo;
	}

	public static SimpleDateFormat getSdf() {
		return sdf;
	}

	public static void setSdf(SimpleDateFormat sdf) {
		DetalhePagamento.sdf = sdf;
	}
	
}
