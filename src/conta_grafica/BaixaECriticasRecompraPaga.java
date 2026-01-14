package conta_grafica;

import java.sql.Connection;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;

import cedente.Cedente;
import empresa.Empresa;
import mssql.ConnectorMSSQL;
import mysql.ConnectorMariaDB;
import rgbsys.RgbsysOperacaoRecompra;
import rgbsys.RgbsysSeleniumQuitacaoBaixa;
import rgbsys.RgbsysUser;
import utils.Utils;

public class BaixaECriticasRecompraPaga {
	private static Connection connMaria=null;
	private static Connection connMSS=null;
	private ArrayList<Saldo> saldos = new ArrayList<>();
	private ArrayList<Saldo> saldosNegativos = new ArrayList<>();
	private static SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
	private static SimpleDateFormat sdfn = new SimpleDateFormat("yyyyMMdd");
	private static SimpleDateFormat sdfa = new SimpleDateFormat("yyyy-MM-dd");
	private static SimpleDateFormat sdfH=new SimpleDateFormat("yyyy-MM-dd HH:mm:SS");
	public static HashMap<String, String> cedentesSemTarifaBaixa = new HashMap<>();
	public static HashMap<String, String> cedentesManterCobrancaSimples = new HashMap<>();
	public static HashMap<String, Empresa> empresas = new HashMap<>();
	public static HashMap<String, HashMap<String,Cedente>> cedentesPorEmpresa = new HashMap<String, HashMap<String,Cedente>>(); 
	private static boolean test=false;
	
	public static void main(String[] args)
	{
		readCedentesSemTarifaBaixa();
		readCedentesManterTituloCobrancaSimples();
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
		RgbsysUser userRGB = new RgbsysUser();
		RgbsysUser.readConf();
		
//    	if(!test)
//		{
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
//		}
		
		RgbsysOperacaoRecompra rgbsysOperacaoRecompra = new RgbsysOperacaoRecompra();
		ArrayList<OperacaoRecompra> operacoesRecompraPagas = OperacaoRecompra.operacoesRecompraPagasNaoBaixadas(connMaria, connMSS);
		
		System.out.println("----------------------------------   Número de operacoes recompra pagas: " + operacoesRecompraPagas.size()+" ----------------------------------------------");
		ArrayList<Critica> criticasALancar = new ArrayList<>();
		for(OperacaoRecompra operacaoRecompra:operacoesRecompraPagas)
		{
			int numeroDataRecompra = Integer.parseInt(sdfn.format(operacaoRecompra.getDataRecompra()));
			int numeroDataAtual = Integer.parseInt(sdfn.format(Calendar.getInstance().getTime()));
			int diaDiferente= numeroDataAtual-numeroDataRecompra;
			if(operacaoRecompra.isPago() && diaDiferente==0)
			{
				System.out.println("*********************************");
				System.out.println(" OPERAÇÃO RECOMPRA PAGA - PRONTA PARA BAIXA");
				System.out.println("*********************************");
				operacaoRecompra.showShort();
				boolean allTitulosBaixados=true;
				for(TituloRecompra tituloRecompra: operacaoRecompra.getTitulosRecompra())
				{
					if(!tituloRecompra.isBaixado())
					{
						allTitulosBaixados=false;
					}
				}
				if(allTitulosBaixados)
				{
					operacaoRecompra.updateBaixado(connMaria);
				}
				else
				{
					rgbsysOperacaoRecompra.aceitarEfetuarBaixaRecompra(connMaria, connMSS, operacaoRecompra, cedentesSemTarifaBaixa);
				}
				operacaoRecompra.updatePago(connMaria);
				/*
				 * JUST A COMMENT
				 */
			}
		}
		rgbsysOperacaoRecompra.close();

		System.out.println("*********************************");
		System.out.println(" Após aceitar e efeturar na lista de solicitações");
		System.out.println("*********************************");
		Utils.waitv(13);

		for(OperacaoRecompra operacaoRecompra:operacoesRecompraPagas)
		{
			System.out.println("*********************************");
			System.out.println(" PROCESSO COMPLEMENTAR PARA BAIXA");
			System.out.println("*********************************");

			RgbsysSeleniumQuitacaoBaixa rgbSysQuitacaoBaixa = new RgbsysSeleniumQuitacaoBaixa(operacaoRecompra); //
			System.out.println("***********************************************");
			System.out.println("DATA DA OPERACAO RECOMPRA: "+operacaoRecompra.getDataRecompra());
			System.out.println("***********************************************");

			Calendar calHoje = Calendar.getInstance();
			calHoje.set(Calendar.HOUR, 0);
			calHoje.set(Calendar.MINUTE, 0);
			calHoje.set(Calendar.SECOND, 0);

			System.out.println("***********************************************");
			System.out.println("DATA DE HOJE: "+calHoje.getTime());
			System.out.println("***********************************************");
			
			int numberDataOperacao=Integer.parseInt(sdfn.format(operacaoRecompra.getDataRecompra()));
			int numberDataHoje=Integer.parseInt(sdfn.format(calHoje.getTime()));

			System.out.println("NumberDataOperacao: "+numberDataOperacao);
			System.out.println("NumberDataHOje: "+numberDataHoje);
			
			if(numberDataOperacao < numberDataHoje)
			{
				System.out.println("Recompra não será processada por ser do dia anterior");
				rgbSysQuitacaoBaixa.close();
				continue;
			}
			else {
				System.out.println("Recompra será processada por ser do dia de hoje");
			}
			
			operacaoRecompra.show();
			operacaoRecompra.getEmpresa().show();
			operacaoRecompra.getEmpresa().getDadosBancariosEmpresa().show();

		    try
		    {
				rgbSysQuitacaoBaixa.quitacaoBaixa(cedentesSemTarifaBaixa, cedentesManterCobrancaSimples);
				rgbSysQuitacaoBaixa.close();
		    }
		    catch (Exception e) 
		    {
		            e.printStackTrace();
		            rgbSysQuitacaoBaixa.saveListCriticaOperacoesRealizadasToDatabase();
		    }
		    
//			for(TituloRecompra tituloRecompra:operacaoRecompra.getTitulosRecompra())
//			{
//				Critica critica = new Critica(connMaria,
//															connMSS,
//															tituloRecompra.getIdentificacaoTitulo(), 
//															tituloRecompra.getCedente().getApelido(),
//															tituloRecompra.getEmpresa().getApelido(), 
//															"BAIXA - ROBO MANDOU PIX");
//				if(!critica.isRegistrado())
//				{
//					criticasALancar.add(critica);
//				}
//			}
		}
//		RgbsisLancamentoCritica rgbsisLancamentoCritica = new RgbsisLancamentoCritica();
//		for(Critica critica:criticasALancar)
//		{
//			// rgbSysSelenium.gravacaoCritica( "PRORROGACAO", "116748NE1", "BMA FIDC", "MAXIBRASIL");
//			rgbsisLancamentoCritica.gravacaoCritica(critica);
//		}
//		rgbsisLancamentoCritica.close();
	}
	
	public static void readCedentesSemTarifaBaixa()
	{
		ArrayList<String> lines = Utils.readLinesInFile("/home/robot/App/Conf/cedentesSemTarifaBaixa.conf");
		for(String line:lines)
		{
			if(line.startsWith("#")&&line.trim().length()>0)
			{
				continue;
			}
			else
			{
				String[] fields=line.split(";");
				String nomeCedente=fields[0];
				if(cedentesSemTarifaBaixa.get(nomeCedente.toUpperCase())==null)
				{
					cedentesSemTarifaBaixa.put(nomeCedente.toUpperCase(), nomeCedente.toUpperCase());
				}
			}
		}
	}
	
	public static void readCedentesManterTituloCobrancaSimples()
	{
		ArrayList<String> lines = Utils.readLinesInFile("/home/robot/App/Conf/cedentesManterCobrancaSimples.conf");
		for(String line:lines)
		{
			if(line.startsWith("#")&&line.trim().length()>0)
			{
				continue;
			}
			else
			{
				String[] fields=line.split(";");
				String nomeCedente=fields[0];
				if(cedentesManterCobrancaSimples.get(nomeCedente.toUpperCase())==null)
				{
					cedentesManterCobrancaSimples.put(nomeCedente.toUpperCase(), nomeCedente.toUpperCase());
				}
			}
		}
	}

	public static Connection getConnMaria() {
		return connMaria;
	}

	public static void setConnMaria(Connection connMaria) {
		BaixaECriticasRecompraPaga.connMaria = connMaria;
	}

	public static Connection getConnMSS() {
		return connMSS;
	}

	public static void setConnMSS(Connection connMSS) {
		BaixaECriticasRecompraPaga.connMSS = connMSS;
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
		this.sdf = sdf;
	}

	public static SimpleDateFormat getSdfa() {
		return sdfa;
	}

	public static void setSdfa(SimpleDateFormat sdfa) {
		BaixaECriticasRecompraPaga.sdfa = sdfa;
	}

	public static HashMap<String, Empresa> getEmpresas() {
		return empresas;
	}

	public static void setEmpresas(HashMap<String, Empresa> empresas) {
		BaixaECriticasRecompraPaga.empresas = empresas;
	}

	public static HashMap<String, HashMap<String, Cedente>> getCedentesPorEmpresa() {
		return cedentesPorEmpresa;
	}

	public static void setCedentesPorEmpresa(HashMap<String, HashMap<String, Cedente>> cedentesPorEmpresa) {
		BaixaECriticasRecompraPaga.cedentesPorEmpresa = cedentesPorEmpresa;
	}

	public static boolean isTest() {
		return test;
	}

	public static void setTest(boolean test) {
		BaixaECriticasRecompraPaga.test = test;
	}

	public static SimpleDateFormat getSdfH() {
		return sdfH;
	}

	public static void setSdfH(SimpleDateFormat sdfH) {
		BaixaECriticasRecompraPaga.sdfH = sdfH;
	}

}
