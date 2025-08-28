package conta_grafica;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
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
import rgbsys.RgbsysUser;

public class ConsultaSaldos {
	private static Connection connMaria=null;
	private static Connection connMSS=null;
	private ArrayList<Saldo> saldos = new ArrayList<>();
	private ArrayList<Saldo> saldosNegativos = new ArrayList<>();
	private static SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
	private static SimpleDateFormat sdfa = new SimpleDateFormat("yyyy-MM-dd");
	public static HashMap<String, Empresa> empresas = new HashMap<>();
	public static HashMap<String, HashMap<String,Cedente>> cedentesPorEmpresa = new HashMap<String, HashMap<String,Cedente>>(); 
	private static boolean test=false;
	private static boolean all=false;
	public static void main(String[] args)
	{
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
				if(args[i].contains("-"))
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
		connMaria=ConnectorMariaDB.conn;
		connMSS=ConnectorMSSQL.conn;
		empresas=Empresa.empresas(connMaria, connMSS);
		cedentesPorEmpresa=Cedente.cedentesPorEmpresa(connMaria, connMSS, empresas);
		System.out.println("After listing all saldos!");

		ConsultaSaldos consultaSaldos=new ConsultaSaldos();
		consultaSaldos.consulta();
		RgbsysUser userRGB = new RgbsysUser();
		RgbsysUser.readConf();
//		RgbsysEnvioContaCorrente rgbenvio = new RgbsysEnvioContaCorrente(userRGB);
		Calendar cal = Calendar.getInstance();
		int diasExtrato=7;
		cal.add(Calendar.DAY_OF_MONTH, -1);
		Date dataFinal = cal.getTime();
		System.out.println("DataFinal: "+sdf.format(dataFinal));
		cal.add(Calendar.DAY_OF_MONTH, -diasExtrato);		
		Date dataInicial = cal.getTime();
		System.out.println("DataInicial: "+sdf.format(dataInicial));
		
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
////					CobrancaPIX.processResponseListaCobranca(connMaria, connMSS,  CobrancaPIX.consultaListaCobranca(authentication, inquiryDate),  userRGB);					
//					CobrancaPIX.processResponseListaPIX(connMaria, connMSS, empresas.get(key),  CobrancaPIX.consultaListaPIX(authentication, inquiryDate),  userRGB);
//					System.out.println("-- FIM DA CONSULTA -- ");
//				}
//			}
		}
		
		if(consultaSaldos.getSaldosNegativos().size()>0)
		{
			Email email = new Email();
			email.readConf();
			TipoInstrucao tipoInstrucao = new TipoInstrucao(connMaria, "SALDONEGATIVO");

			for(Saldo saldo:consultaSaldos.getSaldosNegativos())
			{
				Authentication authentication = new Authentication(saldo.getEmpresa().getDadosBancariosEmpresa());
//				authentication.obtainAccessTokenHomologacao();
				authentication.obtainAccessTokenProducao();

				if(Math.abs(saldo.getValor()) < 15.0)
				{
					continue;
				}
				email.setUser("Financeiro " +saldo.getEmpresa().getApelido());
				if(saldo.getCedente().getEmail().size()>0)
				{	
					Date dataUltimoSaldoZerado=Saldo.dataUltimoSaldoZerado(connMSS, saldo.getEmpresa(), saldo.getCedente());
					Calendar cal30 = Calendar.getInstance();
					cal30.add(Calendar.DAY_OF_MONTH, -30);
					Date data30DiasAtras=cal30.getTime();
//					System.out.println("Última data com saldo zerado: " + sdf.format(dataUltimoSaldoZerado));
					System.out.println("Saldo a partir de: " + sdf.format(data30DiasAtras));
//					Extrato extrato = new Extrato(connMSS, saldo.getEmpresa(),saldo.getCedente(), dataUltimoSaldoZerado, dataFinal);
					Extrato extrato = new Extrato(connMSS, saldo.getEmpresa(),saldo.getCedente(), data30DiasAtras, dataFinal);
					
	//				extrato.show();
//					System.out.println(extrato.toHTML());
					String subject=saldo.getEmpresa().getApelido()+ " - " + saldo.getCedente().getParticipante().getRazaoSocial() + ": Regularização de Saldo em Conta Corrente";
					String para="";
					int iEmail=0;
					for(String e:saldo.getCedente().getEmail())
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
					
					
//					para+=",thaiza@bmacapital.com.br";
//					para="moises@ai4finance.com.br";
					if(test)
					{
						para="moises@ai4finance.com.br";
					}
					CobrancaPIX cobrancaPIX = new CobrancaPIX(saldo, tipoInstrucao);
					if(!cobrancaPIX.checkExists(connMaria))
					{
						try {
							String respostaString= cobrancaPIX.enviarCobrancaImediata(authentication, saldo.getEmpresa(), connMaria, connMSS);
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
//								+ "<meta name=\"viewport\" content=\"width=device-width, initial-scale=1\">"
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
								+ "</style>"
								+ "</head>"
								+ "<body>"
								+ "<p>"
								+ "Ol&aacute;,<br>"
								+ " Segue abaixo o extrato da conta gr&aacute;fica com a "+cobrancaPIX.getEmpresa().getApelido()+". Caso prefira efetuar o pagamento por aqu&iacute;, basta copiar e colar o PIX abaixo:<br> "
								+"</p>"
								+"<p>"
								+ "PIX copia e cola: "
								+ "<input type=\"text\" value=\""+cobrancaPIX.getPixCopiaECola()+"\"  maxlength=\""+cobrancaPIX.getPixCopiaECola().length()*2+"\" size=\""+cobrancaPIX.getPixCopiaECola().length()*2+"\" readonly autofocus>"
								+"</p>"
								+ "<br>"
								+ "Gostar&iacute;amos de lembr&aacute;-los que este &eacute; um e-mail autom&aacute;tico de cunho informativo, qualquer d&uacute;vida, favor nos contatar atrav&eacute;s do telefone/Whastapp (31) 3448-3000.<br>"
								+ "Ahhh! Todas as informa&ccedil;&otilde;es do extrato abaixo, tamb&eacute;m est&atilde;o dispon&iacute;veis no portal MR!!</p>";
						bodyMessage+=extrato.toHTML(cobrancaPIX.getEmpresa());
						bodyMessage+= "<br>Atenciosamente<br>"
						+ "<span class=\"small\">"
						+ "Aten&ccedil;&atilde;o!<br>"
						+ "A BMA n&atilde;o se responsabiliza pela eventual quita&ccedil;&atilde;o de boleto fraudado ou adulterado, bem como pagamento em conta diferente da indicada, "
						+ "hip&oacute;tese em que o valor permanecer&aacute; devido em favor da BMA. &Eacute; de responsabilidade do pagador a confer&ecirc;ncia de boleto banc&aacute;"
						+ "rio antes da efetivação do pagamento, atentando-se a todos os dados, tais como: benefici&aacute;rio, ag&ecirc;ncia, conta e valor. <br>"
						+ "Se o benefici&aacute;rio final n&atilde;o for BMA FUNDO DE INVESTIMENTO EM DIREITOS CREDIT&Oacute;RIOS – CNPJ 10.434.089/0001-58; "
						+ "BMA INTER FUNDO DE INVESTIMENTO EM DIREITOS CREDIT&Oacute;RIOS – CNPJ 36.500.339/0001-02 OU BMA SECURITIZADORA S/A (Sigma Fomento Mercantil) - CNPJ\n"
						+ "04.567.353/0001-29, gentileza procurar imediatamente a BMA, para valida&ccedil;&atilde;o das informa&ccedil;&otilde;es - Whatsapp (31) 3448-3000.<br>"
						+ "</span>";
						bodyMessage+="</body>"
													+ "</html>";
						SendEmailGmail sendEmailGmail = new SendEmailGmail(bodyMessage, subject, email, para,"","");
						saldo.getCedente().checkSendingEmails(connMSS);
						if(saldo.getCedente().isSendEmailAditivo())
						{
							System.out.println("Not sending because sending flags are contrary!");
							System.out.println("Aditivo");
							emailCobrancaInstrucao=new EmailCobrancaInstrucao(connMaria, cobrancaPIX, true, Calendar.getInstance().getTime(), email.getAddress(), para, subject, bodyMessage);
						}
						else 
						{
							if(sendEmailGmail.send(false))
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
					
					if(test && !all)
					{
						break;
					}
				}
			}
		}
//        rgbenvio.close();
	}
	
	private void consulta()
	{
		Calendar cal = Calendar.getInstance();
		cal.add(Calendar.DAY_OF_MONTH, -1);
		Date dataOntem=cal.getTime();
		String queryMSS="select l.empresa, l.cedente, sum(valor) as saldo"
									+ " from cedente c, lanca l"
									+ " where c.ativo=1"
									+ " and c.apelido=l.cedente"
									+ " group by l.empresa, l.cedente";
		System.out.println(queryMSS);
		Statement stMSS=null;
		try {
			stMSS=connMSS.createStatement();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		ResultSet rsMSS=null;
		try {
			rsMSS=stMSS.executeQuery(queryMSS);
			int iSaldo=0;
			while(rsMSS.next())
			{
				iSaldo++;
				String apelidoEmpresa=rsMSS.getString("empresa");
				String apelidoCedente=rsMSS.getString("cedente");
				double valorSaldo=rsMSS.getDouble("saldo");
				System.out.println(iSaldo+" --- " +apelidoEmpresa + ": "+apelidoCedente);
				if(cedentesPorEmpresa.get(apelidoEmpresa)!=null)
				{
					if(cedentesPorEmpresa.get(apelidoEmpresa).get(apelidoCedente)!=null)
					{
						Saldo saldo = new Saldo(cedentesPorEmpresa.get(apelidoEmpresa).get(apelidoCedente).getEmpresa(), cedentesPorEmpresa.get(apelidoEmpresa).get(apelidoCedente), dataOntem, valorSaldo);
						saldos.add(saldo);
						System.out.println("Saldo empresa conhecida: "+saldo.getValor());
					}
					else {
						Saldo saldo = new Saldo(connMaria, connMSS, apelidoEmpresa, apelidoCedente, dataOntem, valorSaldo);
						saldos.add(saldo);					
						System.out.println("Saldo empresa nova: "+saldo.getValor());						
					}
				}
				else {
					System.out.println("Empresa não cadastrada!");
				}
			}
			
		} catch (SQLException e) {
			e.printStackTrace();
		}
		int iSaldoNegativo=0;
		for(Saldo saldo:saldos)
		{
			if(saldo.getValor()<0)
			{
				iSaldoNegativo++;
				System.out.println(iSaldoNegativo+"  ------");
				saldosNegativos.add(saldo);
				saldo.show();
			}
		}
	}

	public static Connection getConnMaria() {
		return connMaria;
	}

	public static void setConnMaria(Connection connMaria) {
		ConsultaSaldos.connMaria = connMaria;
	}

	public static Connection getConnMSS() {
		return connMSS;
	}

	public static void setConnMSS(Connection connMSS) {
		ConsultaSaldos.connMSS = connMSS;
	}

	public ArrayList<Saldo> getSaldos() {
		return saldos;
	}

	public void setSaldos(ArrayList<Saldo> saldos) {
		this.saldos = saldos;
	}

	public ArrayList<Saldo> getSaldosNegativos() {
		return saldosNegativos;
	}

	public void setSaldosNegativos(ArrayList<Saldo> saldosNegativos) {
		this.saldosNegativos = saldosNegativos;
	}

	public SimpleDateFormat getSdf() {
		return sdf;
	}

	public void setSdf(SimpleDateFormat sdf) {
		ConsultaSaldos.sdf = sdf;
	}

	public static SimpleDateFormat getSdfa() {
		return sdfa;
	}

	public static void setSdfa(SimpleDateFormat sdfa) {
		ConsultaSaldos.sdfa = sdfa;
	}

	public static HashMap<String, Empresa> getEmpresas() {
		return empresas;
	}

	public static void setEmpresas(HashMap<String, Empresa> empresas) {
		ConsultaSaldos.empresas = empresas;
	}

	public static HashMap<String, HashMap<String, Cedente>> getCedentesPorEmpresa() {
		return cedentesPorEmpresa;
	}

	public static void setCedentesPorEmpresa(HashMap<String, HashMap<String, Cedente>> cedentesPorEmpresa) {
		ConsultaSaldos.cedentesPorEmpresa = cedentesPorEmpresa;
	}

	public static boolean isTest() {
		return test;
	}

	public static void setTest(boolean test) {
		ConsultaSaldos.test = test;
	}

}
