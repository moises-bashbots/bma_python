package conta_grafica;

import java.sql.Connection;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;

import bradesco.Authentication;
import bradesco.CobrancaPIX;
import cedente.Cedente;
import email.Email;
import email.EmailCobrancaInstrucao;
import email.SendEmailGmail;
import empresa.Empresa;
import instrucao.TipoInstrucao;
import mssql.ConnectorMSSQL;
import mysql.ConnectorMariaDB;
import rgbsys.RgbsysOperacaoRecompra;
import rgbsys.RgbsysUser;
import utils.Utils;
  
public class BuilderRecompraRGB 
{
	private static SimpleDateFormat sdf=new SimpleDateFormat("yyyy-MM-dd");
	private static SimpleDateFormat sdfa = new SimpleDateFormat("yyyy-MM-dd");

	public static boolean test=false;
	public static boolean all=false;
	public static boolean exceptionsOnly=false;
	private static Connection connMaria=null;
	private static Connection connMSS=null;
	public static HashMap<String, Empresa> empresas = new HashMap<>();
	public static HashMap<String,  String> execoes=new HashMap<>();
	public static HashMap<String,  Integer> titulosEnviadosHoje=new HashMap<>();
	public static HashMap<String,  OperacaoRecompra> operacoesAdicionais=new HashMap<>();

	
    public static void main(String args[]) throws Exception
    {
    	readExecoes();
    	Date inquiryDate = null;
		if(args.length>0)
		{
			for(int i=0; i<args.length;i++)
			{
				if(args[i].length()==1)
				{
					switch (args[i]) {
					case "t":
						test=true;
						System.out.println("Running in test mode!");
						break;
					case "a":
						all=true;
						System.out.println("Running to test all!");
						break;
					default:
						break;
					}
				}
				if(args[i].length()==2)
				{
					switch (args[i]) {
					case "eo":
						exceptionsOnly=true;
						System.out.println("Running in Exceptions only mode!");
						break;
					default:
						break;
					}
				}
				else 	if(args[i].contains("-"))
				{
					System.out.println("Processing date: "+args[i]);
					try {
						inquiryDate=sdfa.parse(args[i]);
					} catch (ParseException e) {
						e.printStackTrace();
					}
				}
			}
		}
		if(inquiryDate==null)
		{
			inquiryDate=Calendar.getInstance().getTime();
		}
		    
    	ConnectorMariaDB.connect();
    	ConnectorMSSQL.connect();
    	connMaria=ConnectorMariaDB.getConn();
    	connMSS=ConnectorMSSQL.getConn();
    	empresas=Empresa.empresas(connMaria, connMSS);
		RgbsysUser userRGB = new RgbsysUser();
		RgbsysUser.readConf();


    	if(!test)
		{
//			for(String key:empresas.keySet())
//			{
//				System.out.println("  **** Empresa: "+empresas.get(key).getRazaoSocial());
//				empresas.get(key).getDadosBancariosEmpresa().show();
//				
//				Authentication authentication = new Authentication(empresas.get(key).getDadosBancariosEmpresa());
//				if(authentication.getClientId().length()>0)
//				{
//					authentication.obtainAccessTokenProducao();
//					System.out.println("-- CONSULTA DA LISTA DE COBRANÇA IMEDIATA -- para a empresa "+ empresas.get(key).getRazaoSocial());
//					CobrancaPIX.processResponseListaCobranca(connMaria, connMSS,  CobrancaPIX.consultaListaCobranca(authentication, inquiryDate),  userRGB);
//					
//					System.out.println("-- FIM DA CONSULTA -- ");
//				}
//			}
		}
    	
    	String fileName="/home/robot/Recompra/recompra.csv";
   	
    	RgbsysOperacaoRecompra rgbsysOperacaoRecompra = new RgbsysOperacaoRecompra();
    	boolean clean=true;
		ArrayList<OperacaoRecompra> operacoesRecompra = rgbsysOperacaoRecompra.leituraRecomprasValidas(connMaria, connMSS);
				
		rgbsysOperacaoRecompra.close();
		
		if(operacoesRecompra.size()>0)
		{
			System.out.println("##########################################");
			System.out.println("OPERACOES RECOMPRA");
			System.out.println("##########################################");
			ArrayList<OperacaoRecompra> operacoesParaProcessar = new ArrayList<>();
			for(OperacaoRecompra operacaoRecompra:operacoesRecompra)
			{
				if(operacaoRecompra.getTitulosRecompra().size()>0)
				{
					operacaoRecompra.show();
					operacoesParaProcessar.add(operacaoRecompra);
				}
				else
				{
					System.out.println("Esta recompra não tem títulos em aberto! " + operacaoRecompra.getEmpresa().getApelido() + " "+operacaoRecompra.getCedente().getApelido());
					System.out.println("Removendo recompra da lista!");
				}
			}

			operacoesRecompra=operacoesParaProcessar;
	    	for(OperacaoRecompra operacaoRecompra:operacoesRecompra)
	    	{
	    		operacaoRecompra.show();
	    		sendCobranca(operacaoRecompra);
	    	}
		}
    }
    
    public static void sendCobranca(OperacaoRecompra operacaoRecompra)
    {
    	Authentication authentication = new Authentication(operacaoRecompra.getEmpresa().getDadosBancariosEmpresa());
		authentication.obtainAccessTokenProducao();
		TipoInstrucao tipoInstrucao = new TipoInstrucao(connMaria, "RECOMPRA");
    	CobrancaPIX cobrancaPIX = new CobrancaPIX(operacaoRecompra,tipoInstrucao);
    	
    	Email email = new Email();
		email.readConf();
		email.setUser("Cobranca " +operacaoRecompra.getEmpresa().getApelido());
		
		String subject=operacaoRecompra.getEmpresa().getApelido()+ " - " 
								+ operacaoRecompra.getCedente().getParticipante().getRazaoSocial() + ": Recompra";
		
		String para="";
		int iEmail=0;
		for(String e:operacaoRecompra.getCedente().getEmail())
		{
			if(iEmail==0)
			{
				para+=e;
			}
			else {
				para+=","+e;
			}
			iEmail++;
		}
		if(test)
		{
			para="moises@ai4finance.com.br";
//			para="amanaara@bmacapital.com.br,moises@ai4finance.com.br";
		}

    	if(!cobrancaPIX.checkExists(connMaria))
		{
			try {
				String respostaString= cobrancaPIX.enviarCobrancaImediata(authentication, operacaoRecompra.getEmpresa(), connMaria, connMSS);
				System.out.println(respostaString);
			} catch (Exception e) {
				e.printStackTrace();
			}
			cobrancaPIX.register(connMaria);
		}
		else {
			System.out.println("-- CONSULTA DA COBRANCA IMEDIATA -- " + cobrancaPIX.getTxid());
			System.out.println(cobrancaPIX.consulta(authentication, connMaria, connMSS));
		}
    	EmailCobrancaInstrucao emailCobrancaInstrucao=new EmailCobrancaInstrucao(connMaria, cobrancaPIX);					
		if( (cobrancaPIX.getPixCopiaECola().length()>0 && emailCobrancaInstrucao.getIdEmailCobrancaInstrucao()==0)
				|| test
				)
		{
    	
    	String bodyMessage=""
				+ "<!DOCTYPE HTML PUBLIC \"-//W3C//DTD HTML 4.01 Transitional//EN\"\n"
				+ "            \"http://www.w3.org/TR/html4/loose.dtd\">"
				+ "<html>"
				+ "<head>"
//				+ "<meta name=\"viewport\" content=\"width=device-width, initial-scale=1\">"
				+ "<style>"
				+ "input {font-weight:bold;}"
				+ "table {"
				+ "        border: 1px solid black;"
				+ "        border-collapse: collapse;"
				+ "        width: 65%;"
				+ "      }"
				+ "th,td {"
				+ "        border: 1px solid black;"
				+ "        border-collapse: collapse;"
				+ "      }"
				+ "tr:nth-child(even) {"
				+ "    background-color: rgba(150, 212, 212, 0.4);"
				+ "}"
				+ "td {"
				+ "    text-align: center;"
				+ "}"
				+ "* {"
				+ "  font-size: 18px;"
				+ "  font-family: arial, sans-serif;"
				+ "}"
				+ "span.small {"
				+ " font-size: smaller;"
				+ "}"
				+ "</style>"
				+ "</head>"
				+ "<body>"
				+ "<p>"
				+ "Ol&aacute;,<br>"
				+ "Seguem dados para recompra solicitada via portal MR.<br>"
				+ "O pagamento  dever&aacute; ser realizado por meio de PIX, atrav&eacute;s do c&oacute;digo a seguir:<br>"
				+"</p>"
				+"<p>"
				+ "PIX copia e cola: "
				+ "<input type=\"text\" value=\""+cobrancaPIX.getPixCopiaECola()
				+"\"  maxlength=\""+cobrancaPIX.getPixCopiaECola().length()*2
				+"\" size=\""+cobrancaPIX.getPixCopiaECola().length()*2+"\" readonly autofocus>"
				+"</p>"
				+ operacaoRecompra.toHTML()				
				+ "A baixa ser&aacute; realizada, ap&oacute;s identifica&ccedil;&atilde;o do cr&eacute;dito.<br>"
				+ "Em caso de d&uacute;vidas, gentileza entrar em contato atrav&eacute;s do Whatsapp (31) 3448-3000.<br>"
				+ "<span class=\"small\">"
				+ "Aten&ccedil;&atilde;o!<br>"
				+ "A BMA n&atilde;o se responsabiliza pela eventual quita&ccedil;&atilde;o de boleto fraudado ou adulterado, bem como pagamento em conta diferente da indicada, "
				+ "hip&oacute;tese em que o valor permanecer&aacute; devido em favor da BMA. &Eacute; de responsabilidade do pagador a confer&ecirc;ncia de boleto banc&aacute;"
				+ "rio antes da efetivação do pagamento, atentando-se a todos os dados, tais como: benefici&aacute;rio, ag&ecirc;ncia, conta e valor. <br>"
				+ "Se o benefici&aacute;rio final n&atilde;o for BMA FUNDO DE INVESTIMENTO EM DIREITOS CREDIT&Oacute;RIOS – CNPJ 10.434.089/0001-58; "
				+ "BMA INTER FUNDO DE INVESTIMENTO EM DIREITOS CREDIT&Oacute;RIOS – CNPJ 36.500.339/0001-02 OU BMA SECURITIZADORA S/A (Sigma Fomento Mercantil) - CNPJ\n"
				+ "04.567.353/0001-29, gentileza procurar imediatamente a BMA, para valida&ccedil;&atilde;o das informa&ccedil;&otilde;es.<br>"
				+ "</span>"
				+ "</body>"
				+ "</html>";
    		System.out.println(bodyMessage);
    		SendEmailGmail sendEmailGmail = new SendEmailGmail(bodyMessage, subject, email, para,"","");
			operacaoRecompra.getCedente().checkSendingEmails(connMSS);
    		if( operacaoRecompra.getCedente().isSendEmailBorderoNormalAnalitico())
			{
				System.out.println("Not sending because sending flags are contrary!");
				System.out.println("Borderô normal analítico");
				emailCobrancaInstrucao=new EmailCobrancaInstrucao(connMaria, cobrancaPIX, true, Calendar.getInstance().getTime(), email.getAddress(), para, subject, bodyMessage);
			}
			else 
			{
				if(sendEmailGmail.send(true))
				{
					emailCobrancaInstrucao=new EmailCobrancaInstrucao(connMaria, cobrancaPIX, true, Calendar.getInstance().getTime(), email.getAddress(), para, subject, bodyMessage);
				}							
			}
		}
		else
		{
			if(cobrancaPIX.getPixCopiaECola().length()==0)
			{
				System.out.println("No pixCopiaECola!!");
			}
			else if(emailCobrancaInstrucao.getIdEmailCobrancaInstrucao()>0)
			{
				System.out.println("Email sent already!");
			}
		}
    }
    
    
	public static void readExecoes()
	{
		ArrayList<String> lines = Utils.readLinesInFile("/home/robot/App/Conf/execoesRecompra.conf");
		for(String line:lines)
		{
			if(line.startsWith("#")&&line.trim().length()>0)
			{
				continue;
			}
			else
			{
				String[] fields=line.split(";");
				String nomeEmpresa=fields[0];
				String nomeCedente=fields[1];
				String keyEmpresaCedente = nomeEmpresa.toUpperCase()+nomeCedente.toUpperCase();
				if(execoes.get(keyEmpresaCedente)==null)
				{
					execoes.put(keyEmpresaCedente, keyEmpresaCedente);
				}
			}
		}
	}
	
	
    public static HashMap<Integer, OperacaoRecompra> readOperacoesRecompra(String fileName)
    {
    	ArrayList<String> lines = Utils.readLinesInFile(fileName);
//    	ArrayList<String> lines = Utils.readLinesInFile("/home/moises/Files/Clients/BMA/Recompra/recompra.csv");
    	HashMap<Integer,OperacaoRecompra> operacoesRecompra = new HashMap<>();
    	for(String line: lines)
    	{
    		if(line.contains("Recompra") && !line.contains("Juros") && !line.contains("Tabela"))
    		{
    			System.out.println("-------------------------------------------------------------------");
    			System.out.println(line);
    			String[] fields = line.split(";");
    			String observacao=fields[0];
    			String instrucao=fields[1];
    			String vencimentoString=fields[2];
    			String titulo=fields[3];
    			String valorString=fields[4];
    			String empresaString=fields[5];
    			String cedenteString=fields[6];
    			String valorCorrigidoString=fields[7];
    			String jurosMaisTarifaString=fields[8];
    			String totalAPagarString=fields[9];
    			String dataSolicitacaoString=sdf.format(Calendar.getInstance().getTime());
    			double valor=0;
    			double valorCorrigido=0;
    			double jurosMaisTarifa=0;
    			double totalAPagar=0;
    			Date dataVencimento=null;
    			try {
    				dataVencimento=sdf.parse(vencimentoString);	
				} catch (Exception e) {
					e.printStackTrace();
				}
    			Date dataSolicitacao=null;
            	
            	try {
					dataSolicitacao=sdf.parse(dataSolicitacaoString);
				} catch (ParseException e) {
					e.printStackTrace();
				}
    			
    			try {
    				valor=Double.parseDouble(valorString);
				} catch (Exception e) {
					e.printStackTrace();
				}
    			try {
    				valorCorrigido=Double.parseDouble(valorCorrigidoString);
				} catch (Exception e) {
					e.printStackTrace();
				}
    			try {
    				jurosMaisTarifa=Double.parseDouble(jurosMaisTarifaString);
				} catch (Exception e) {
					e.printStackTrace();
				}
    			try {
    				totalAPagar=Double.parseDouble(totalAPagarString);
				} catch (Exception e) {
					e.printStackTrace();
				}
    			
    			
    			Empresa empresa = new Empresa(ConnectorMariaDB.getConn(), ConnectorMSSQL.getConn(), empresaString);
    			Cedente cedente = new Cedente(ConnectorMariaDB.getConn(), ConnectorMSSQL.getConn(), empresa, cedenteString);
    			
    			System.out.println("----------------------------------------------");
    			System.out.println("Observacao: " + observacao);
    			System.out.println("Instrucao: "+instrucao);
    			System.out.println("Vencimento: "+sdf.format(dataVencimento));
    			System.out.println("Titulo: "+titulo);
    			System.out.println("Valor: "+valor);
    			System.out.println("Empresa: "+empresa.getApelido()+ " - "+empresa.getCnpj());
    			System.out.println("Cedente: "+ cedente.getApelido()+ " - "+cedente.getEmail());
    			System.out.println("ValorCorrigido: "+valorCorrigido);
    			System.out.println("JurosMaisTarifa: "+jurosMaisTarifa);
    			System.out.println("TotalAPagar: "+totalAPagar);
    			OperacaoRecompra operacaoRecompra = new OperacaoRecompra(ConnectorMariaDB.getConn(), empresa, cedente);
    			if(operacoesRecompra.get(operacaoRecompra.getIdOperacaoRecompra())==null)
    			{
        			TituloRecompra tituloRecompra = new TituloRecompra(ConnectorMariaDB.getConn(), ConnectorMSSQL.getConn(), operacaoRecompra, titulo, valor, dataVencimento,dataSolicitacao,true);
        			tituloRecompra.setValorCorrigido(valorCorrigido);
        			tituloRecompra.setMora(jurosMaisTarifa);        			
        			tituloRecompra.setValorRecompra(totalAPagar);
        			tituloRecompra.updateValues(ConnectorMariaDB.getConn());
        			operacaoRecompra.getTitulosRecompra().add(tituloRecompra);
    				operacoesRecompra.put(operacaoRecompra.getIdOperacaoRecompra(), operacaoRecompra);
    			}
    			else
    			{
    				TituloRecompra tituloRecompra = new TituloRecompra(ConnectorMariaDB.getConn(), ConnectorMSSQL.getConn(), operacoesRecompra.get(operacaoRecompra.getIdOperacaoRecompra()), titulo, valor, dataVencimento,dataSolicitacao,true);
    				tituloRecompra.setValorCorrigido(valorCorrigido);
        			tituloRecompra.setMora(jurosMaisTarifa);
        			tituloRecompra.setValorRecompra(totalAPagar);
        			tituloRecompra.updateValues(ConnectorMariaDB.getConn());
    				operacoesRecompra.get(operacaoRecompra.getIdOperacaoRecompra()).getTitulosRecompra().add(tituloRecompra);    				
    			}
    			operacoesRecompra.get(operacaoRecompra.getIdOperacaoRecompra()).updateValue(ConnectorMariaDB.getConn());
    		}
    	}
		return operacoesRecompra;
    }
}
