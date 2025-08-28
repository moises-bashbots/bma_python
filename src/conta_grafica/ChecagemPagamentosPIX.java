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
import empresa.Empresa;
import mssql.ConnectorMSSQL;
import mysql.ConnectorMariaDB;
import rgbsys.RgbsysUser;

public class ChecagemPagamentosPIX {
	private static Connection connMaria=null;
	private static Connection connMSS=null;
	private ArrayList<Saldo> saldos = new ArrayList<>();
	private ArrayList<Saldo> saldosNegativos = new ArrayList<>();
	private static SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
	private static SimpleDateFormat sdfa = new SimpleDateFormat("yyyy-MM-dd");
	public static HashMap<String, Empresa> empresas = new HashMap<>();
	public static HashMap<String, HashMap<String,Cedente>> cedentesPorEmpresa = new HashMap<String, HashMap<String,Cedente>>(); 
	private static boolean test=false;
	
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
		
    	if(!test)
		{
			for(String key:empresas.keySet())
			{
				System.out.println("  **** Empresa: "+empresas.get(key).getRazaoSocial());
				empresas.get(key).getDadosBancariosEmpresa().show();
				
				Authentication authentication = new Authentication(empresas.get(key).getDadosBancariosEmpresa());
				if(authentication.getClientId().length()>0)
				{
					authentication.obtainAccessTokenProducao();
					System.out.println("*****************************************************************************************************************");
					System.out.println("*****************************************************************************************************************");
					System.out.println("-- CONSULTA DA LISTA DE COBRANÇA IMEDIATA -- para a empresa "+ empresas.get(key).getRazaoSocial());
					System.out.println("*****************************************************************************************************************");
					System.out.println("*****************************************************************************************************************");
					String responseConsultaListaPIX=CobrancaPIX.consultaListaPIX(authentication, inquiryDate);
					System.out.println("*****************************************************************************************************************");
					System.out.println("*****************************************************************************************************************");

					CobrancaPIX.processResponseListaPIX(connMaria, connMSS,  empresas.get(key),responseConsultaListaPIX,  userRGB);
					System.out.println("-- FIM DA CONSULTA -- ");
				}
			}
		}
		
//		RgbsysOperacaoRecompra rgbsysOperacaoRecompra = new RgbsysOperacaoRecompra();
//		ArrayList<OperacaoRecompra> operacoesRecompra = OperacaoRecompra.operacoesRecompraHoje(connMaria, connMSS);
//		ArrayList<Critica> criticasALancar = new ArrayList<>();
//		for(OperacaoRecompra operacaoRecompra:operacoesRecompra)
//		{
//			if(operacaoRecompra.isPago())
//			{
//				rgbsysOperacaoRecompra.baixaRecompra(connMaria, connMSS, operacaoRecompra);
//				for(TituloRecompra tituloRecompra:operacaoRecompra.getTitulosRecompra())
//				{
//					Critica critica = new Critica(connMaria,connMSS,tituloRecompra.getIdentificacaoTitulo(), tituloRecompra.getEmpresa().getApelido(), tituloRecompra.getCedente().getApelido(), "RECOMPRA");
//					if(!critica.isRegistrado())
//					{
//						criticasALancar.add(critica);
//					}
//				}
//			}
//		}
//
//		RgbsisLancamentoCritica rgbsisLancamentoCritica = new RgbsisLancamentoCritica();
//		for(Critica critica:criticasALancar)
//		{
//			// rgbSysSelenium.gravacaoCritica( "PRORROGACAO", "116748NE1", "BMA FIDC", "MAXIBRASIL");
//			rgbsisLancamentoCritica.gravacaoCritica(critica);
//		}
//		rgbsisLancamentoCritica.close();
	}

	public static Connection getConnMaria() {
		return connMaria;
	}

	public static void setConnMaria(Connection connMaria) {
		ChecagemPagamentosPIX.connMaria = connMaria;
	}

	public static Connection getConnMSS() {
		return connMSS;
	}

	public static void setConnMSS(Connection connMSS) {
		ChecagemPagamentosPIX.connMSS = connMSS;
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
		ChecagemPagamentosPIX.sdfa = sdfa;
	}

	public static HashMap<String, Empresa> getEmpresas() {
		return empresas;
	}

	public static void setEmpresas(HashMap<String, Empresa> empresas) {
		ChecagemPagamentosPIX.empresas = empresas;
	}

	public static HashMap<String, HashMap<String, Cedente>> getCedentesPorEmpresa() {
		return cedentesPorEmpresa;
	}

	public static void setCedentesPorEmpresa(HashMap<String, HashMap<String, Cedente>> cedentesPorEmpresa) {
		ChecagemPagamentosPIX.cedentesPorEmpresa = cedentesPorEmpresa;
	}

	public static boolean isTest() {
		return test;
	}

	public static void setTest(boolean test) {
		ChecagemPagamentosPIX.test = test;
	}

}
