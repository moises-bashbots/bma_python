package bradesco;

public class DadosContaTransferencia {
	
	private String nomeFavorecido="";
	private String cadastroFavorecido="";
	private String banco="";	
	private String agencia="";
	private String contaNumero="";
	private String contaDigito="";
	private String tipoConta="";
	private String valor="";
	private String finalidade="";
	
	
	public DadosContaTransferencia()
	{
		
	}
	
	public DadosContaTransferencia(String nomeFavorecido, String cadastroFavorecido, String banco, String agencia, String contaNumero, String contaDigito, String tipoConta, String valor, String finalidade)
	{
		this.nomeFavorecido=nomeFavorecido;
		this.cadastroFavorecido=cadastroFavorecido;
		this.banco=banco;
		this.agencia=agencia;
		this.contaNumero=contaNumero;
		this.contaDigito=contaDigito;
		this.tipoConta=tipoConta;
		this.valor=valor;
	}

	public String getAgencia() {
		return agencia;
	}

	public void setAgencia(String agencia) {
		this.agencia = agencia;
	}

	public String getContaNumero() {
		return contaNumero;
	}

	public void setContaNumero(String contaNumero) {
		this.contaNumero = contaNumero;
	}

	public String getContaDigito() {
		return contaDigito;
	}

	public void setContaDigito(String contaDigito) {
		this.contaDigito = contaDigito;
	}

	public String getValor() {
		return valor;
	}

	public void setValor(String valor) {
		this.valor = valor;
	}

	public String getNomeFavorecido() {
		return nomeFavorecido;
	}

	public void setNomeFavorecido(String nomeFavorecido) {
		this.nomeFavorecido = nomeFavorecido;
	}

	public String getCadastroFavorecido() {
		return cadastroFavorecido;
	}

	public void setCadastroFavorecido(String cadastroFavorecido) {
		this.cadastroFavorecido = cadastroFavorecido;
	}

	public String getBanco() {
		return banco;
	}

	public void setBanco(String banco) {
		this.banco = banco;
	}

	public String getTipoConta() {
		return tipoConta;
	}

	public void setTipoConta(String tipoConta) {
		this.tipoConta = tipoConta;
	}

	public String getFinalidade() {
		return finalidade;
	}

	public void setFinalidade(String finalidade) {
		this.finalidade = finalidade;
	}
}