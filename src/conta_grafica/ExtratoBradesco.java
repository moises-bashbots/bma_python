package conta_grafica;

import java.sql.Connection;
import java.util.ArrayList;

import empresa.Empresa;

public class ExtratoBradesco {
	private Empresa empresa = new Empresa();
	private ArrayList<LancamentoBradesco> lancamentosBradesco = new ArrayList<>();
	
	public ExtratoBradesco()
	{
		
	}
	
	public ExtratoBradesco(Connection connMaria, Connection connMSS, String agencia, String conta, String digitoConta, ArrayList<LancamentoBradesco> lancamentosBradesco)
	{
		this.empresa = Empresa.identifyDadosBancariosEmpresa(connMaria, connMSS, agencia, conta, digitoConta);
		this.lancamentosBradesco=lancamentosBradesco;
	}

	public Empresa getEmpresa() {
		return this.empresa;
	}

	public void setEmpresa(Empresa empresa) {
		this.empresa = empresa;
	}

	public ArrayList<LancamentoBradesco> getLancamentosBradesco() {
		return this.lancamentosBradesco;
	}

	public void setLancamentosBradesco(ArrayList<LancamentoBradesco> lancamentosBradesco) {
		this.lancamentosBradesco = lancamentosBradesco;
	}	
	
	
}
