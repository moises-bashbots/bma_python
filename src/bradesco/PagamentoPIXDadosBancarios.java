package bradesco;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;


public class PagamentoPIXDadosBancarios {
	private static SimpleDateFormat sdf= new SimpleDateFormat("yyyy-MM-dd");
/*
 * Entrada

Parâmetro	Tipo Parâmetro	Tipo Dado	Obrigatório	Descrição	Tamanho
valor_pagamento	body	string	sim	Valor a ser pago.	(17,2)
data_pagamento	body	string	sim	Data do pagamento, formato yyyy-MM-dd	10
chave	body	string	não	Chave do recebedor. (CNPJ, Telefone, E-Mail ou Chave Aleatória) Exemplos: CNPJ: 12345678000112, Telefone: +5561912345678, E-mail: funcionario_itau@itau-unibanco.com.br, Chave Aleatória: 123e4567-e12b-12d1-a456-426655440000	76
ispb	body	string	não	Ispb do recebedor.	8
tipo_identificacao_conta	body	string	não	Tipo de conta do recebedor. "CC": Conta Corrente "CP": Conta Pagamento "PP": Conta Poupança	2
agencia_recebedor	body	string	não	Agência do recebedor.	4
conta_recebedor	body	string	não	Conta do recebedor + Digito Verificador.	20
tipo_de_identificacao_do_recebedor	body	string	não	Tipo de pessoa do recebedor ("F" - Fisica ou "J" - Jurídica)	1
identificacao_recebedor	body	string	não	Documento do recebedor (CPF ou CNPJ)	CNPJ - 14 CPF - 11
informacoes_entre_usuarios	body	string	não	Informações entre usuários a ser trafegada no pagamento.	140
referencia_empresa	body	string	não	Texto livre de referência da empresa sobre o pagamento.	20
identificacao_comprovante	body	string	não	Texto livre de para identificação do comprovante da empresa sobre o pagamento.	100
txid	body	string	não	Identificador do PIX.	35
pagador	body	object	sim	Objeto: Detalhe Pagador.	
 * 
 * {
            "valor_pagamento": "123.40",
            "data_pagamento": "2020-07-13",
            "ispb": "60701190",
            "tipo_identificacao_conta": "CC",
            "agencia_recebedor": "1500",
            "conta_recebedor": "00052012",
            "tipo_de_identificacao_do_recebedor": "F",
            "identificacao_recebedor": "61389299260",
            "informacoes_entre_usuarios": "transferencia para o João - referente a manutencao",
            "referencia_empresa": "154785966666666",
            "identificacao_comprovante": "transferencia para o João - referente a manutencao",
            "txid": "bqJJjaVAThmtnUeVLNnSXQ",
            "pagador": {
              "tipo_conta": "CC",
              "agencia": "1500",
              "conta": "00800550",
              "tipo_pessoa": "J",
              "documento": "06830622000123",
              "modulo_sispag": "Fornecedores"
            }
    }
 */
	private double valorPagamento=0;
	private Date dataPagamento=null;
	private String ispb="";
	private String tipoIdentificacaoConta="";
	private String agenciaRecebedor="";
	private String contaRecebedor="";
	private String tipoIdentificacaoRecebedor="";
	private String identificacaoRecebedor="";
	private String informacoesEntreUsuarios="";
	private String referenciaEmpresa="";
	private String identificacaoComprovante="";
	private String txid="";
	private String pagador="";
	private String tipoConta="";
	private String agencia="";
	private String conta="";
	private String tipoPessoa="";
	private String documento="";
	private String moduloSispag="";
	
	public PagamentoPIXDadosBancarios()
	{
		
	}
	
	public PagamentoPIXDadosBancarios(
			double valorPagamento,
			Date dataPagamento,
			String ispb,
			String tipoIdentificacaoConta,
			String agenciaRecebedor,
			String contaRecebedor,
			String tipoIdentificacaoRecebedor,
			String identificacaoRecebedor,
			String informacoesEntreUsuarios,
			String referenciaEmpresa,
			String identificacaoComprovante,
			String txid,
			String tipoConta,
			String agencia,
			String conta,
			String tipoPessoa,
			String documento,
			String moduloSispag)
	{
		this.valorPagamento=valorPagamento;
		this.dataPagamento=dataPagamento;
		this.ispb=ispb;
		this.tipoIdentificacaoConta=tipoIdentificacaoConta;
		this.agenciaRecebedor=agenciaRecebedor;
		this.contaRecebedor=contaRecebedor;
		this.tipoIdentificacaoRecebedor=tipoIdentificacaoRecebedor;
		this.identificacaoRecebedor=identificacaoRecebedor;
		this.informacoesEntreUsuarios=informacoesEntreUsuarios;
		this.referenciaEmpresa=referenciaEmpresa;
		this.identificacaoComprovante=identificacaoComprovante;
		this.txid=txid;
		this.tipoConta=tipoConta;
		this.agencia=agencia;
		this.conta=conta;
		this.tipoPessoa=tipoPessoa;
		this.documento=documento;
		this.moduloSispag=moduloSispag;
	}
	
	public static void main(String[] args) 
	{
		double valorPagamento=123.40;
		Date dataPagamento=null;
		try {
			dataPagamento = sdf.parse("2020-07-13");
		} catch (ParseException e) {
			e.printStackTrace();
		}
		String ispb="60701190";
		String tipoIdentificacaoConta="CC";
		String agenciaRecebedor="1500";
		String contaRecebedor="00052012";
		String tipoIdentificacaoRecebedor="F";
		String identificacaoRecebedor="61389299260";
		String informacoesEntreUsuarios="transferencia para o João - referente a manutencao";
		String referenciaEmpresa="154785966666666";
		String identificacaoComprovante="transferencia para o João - referente a manutencao";
		String txid="bqJJjaVAThmtnUeVLNnSXQ";
		String tipoConta="CC";
		String agencia="1500";
		String conta="00800550";
		String tipoPessoa="J";
		String documento="06830622000123";
		String moduloSispag="Fornecedores";
		
		PagamentoPIXDadosBancarios pagamentoPIXDadosBancarios = new PagamentoPIXDadosBancarios(valorPagamento, dataPagamento, ispb, tipoIdentificacaoConta, agenciaRecebedor, contaRecebedor, tipoIdentificacaoRecebedor, identificacaoRecebedor, informacoesEntreUsuarios, referenciaEmpresa, identificacaoComprovante, txid, tipoConta, agencia, conta, tipoPessoa, documento, moduloSispag);
			
		
		System.out.println(pagamentoPIXDadosBancarios.toJSON());
		
	}

	public String toJSON()
	{
		String jsonString= "{" + 
				"\"valor_pagamento\": \"" + this.valorPagamento+"\"," + 
				"\"data_pagamento\": \""+sdf.format(dataPagamento)+"\"," + 
				"\"ispb\": \""+this.ispb+"\"," + 
				"\"tipo_identificacao_conta\": \""+this.tipoIdentificacaoConta+"\"," + 
				"\"agencia_recebedor\": \""+this.agenciaRecebedor+"\"," + 
				"\"conta_recebedor\": \""+this.contaRecebedor+"\"," + 
				"\"tipo_de_identificacao_do_recebedor\": \""+this.tipoIdentificacaoRecebedor+"\"," + 
				"\"identificacao_recebedor\": \""+this.identificacaoRecebedor+"\"," + 
				"\"informacoes_entre_usuarios\": \""+this.informacoesEntreUsuarios+"\"," + 
				"\"referencia_empresa\": \""+this.referenciaEmpresa+"\"," + 
				"\"identificacao_comprovante\": \""+this.identificacaoComprovante+"\"," + 
				"\"txid\": \""+this.txid+"\"," + 
				"\"pagador\": {" + 
				"\"tipo_conta\": \""+this.tipoConta+"\"," + 
				"\"agencia\": \""+this.agencia+"\"," + 
				"\"conta\": \""+this.conta+"\"," + 
				"\"tipo_pessoa\": \""+this.tipoPessoa+"\"," + 
				"\"documento\": \""+this.documento+"\"," + 
				"\"modulo_sispag\": \""+this.moduloSispag+"\"" + 
				"}" + 
				"}";
	            
	    return jsonString;
	}

	
	public String toBeautyJSON()
	{
		String jsonString= "{\n" + 
				"\"valor_pagamento\": \"" + this.valorPagamento+"\",\n" + 
				"\"data_pagamento\": \""+sdf.format(dataPagamento)+"\",\n" + 
				"\"ispb\": \""+this.ispb+"\",\n" + 
				"\"tipo_identificacao_conta\": \""+this.tipoIdentificacaoConta+"\",\n" + 
				"\"agencia_recebedor\": \""+this.agenciaRecebedor+"\",\n" + 
				"\"conta_recebedor\": \""+this.contaRecebedor+"\",\n" + 
				"\"tipo_de_identificacao_do_recebedor\": \""+this.tipoIdentificacaoRecebedor+"\",\n" + 
				"\"identificacao_recebedor\": \""+this.identificacaoRecebedor+"\",\n" + 
				"\"informacoes_entre_usuarios\": \""+this.informacoesEntreUsuarios+"\",\n" + 
				"\"referencia_empresa\": \""+this.referenciaEmpresa+"\",\n" + 
				"\"identificacao_comprovante\": \""+this.identificacaoComprovante+"\",\n" + 
				"\"txid\": \""+this.txid+"\",\n" + 
				"\"pagador\": {\n" + 
				"\"tipo_conta\": \""+this.tipoConta+"\",\n" + 
				"\"agencia\": \""+this.agencia+"\",\n" + 
				"\"conta\": \""+this.conta+"\",\n" + 
				"\"tipo_pessoa\": \""+this.tipoPessoa+"\",\n" + 
				"\"documento\": \""+this.documento+"\",\n" + 
				"\"modulo_sispag\": \""+this.moduloSispag+"\"\n" + 
				"}\n" + 
				"}";
	            
	    return jsonString;
	}
	
}
