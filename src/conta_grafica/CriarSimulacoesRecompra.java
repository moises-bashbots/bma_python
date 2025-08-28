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
import rgbsys.RgbsysUser;

public class CriarSimulacoesRecompra {
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
		RgbsysUser.readConf();
		RgbsysOperacaoRecompra rgbsysOperacaoRecompra = new RgbsysOperacaoRecompra();
		ArrayList<OperacaoRecompra> operacoesRecompra = rgbsysOperacaoRecompra.construcaoRecompra(connMaria, connMSS);
		rgbsysOperacaoRecompra.close();
		
		if(operacoesRecompra!=null)
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
			
//			for(OperacaoRecompra operacaoRecompra:operacoesRecompra)
//			{
//				if(operacaoRecompra.getTitulosRecompra().size()>0 && operacaoRecompra.getDemostrativoPDF()==null)
//				{
//					RgbsysSimulacaoRecompra rgbsysSimulacaoRecompra = new RgbsysSimulacaoRecompra();
//					rgbsysSimulacaoRecompra.simulacaoBaixa(operacaoRecompra, connMaria); //740.52
//					rgbsysSimulacaoRecompra.close();
//				}
//				if(operacaoRecompra.getTitulosRecompra().size()==0)
//				{
//					System.out.println("Esta recompra não tem títulos em aberto! " + operacaoRecompra.getEmpresa().getApelido() + " "+operacaoRecompra.getCedente().getApelido());
//				}
//				if(operacaoRecompra.getDemostrativoPDF()!=null)
//				{
//					System.out.println("Esta simulação de recompra já existe! ");
//				}
//			}
		}
//
//		ArrayList<OperacaoRecompra> operacoesRecompra = OperacaoRecompra.operacoesRecompraHoje(connMaria, connMSS);
//		for(OperacaoRecompra operacaoRecompra:operacoesRecompra)
//		{
//			if(operacaoRecompra.isPago())
//			{
//				Critica critica = new Critica(null, null, null, null);
//			}
//		}
//		RgbsisLancamentoCritica rgbsisLancamentoCritica = new RgbsisLancamentoCritica();
//		for(Critica critica:criticas)
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
		CriarSimulacoesRecompra.connMaria = connMaria;
	}

	public static Connection getConnMSS() {
		return connMSS;
	}

	public static void setConnMSS(Connection connMSS) {
		CriarSimulacoesRecompra.connMSS = connMSS;
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
		CriarSimulacoesRecompra.sdfa = sdfa;
	}

	public static HashMap<String, Empresa> getEmpresas() {
		return empresas;
	}

	public static void setEmpresas(HashMap<String, Empresa> empresas) {
		CriarSimulacoesRecompra.empresas = empresas;
	}

	public static HashMap<String, HashMap<String, Cedente>> getCedentesPorEmpresa() {
		return cedentesPorEmpresa;
	}

	public static void setCedentesPorEmpresa(HashMap<String, HashMap<String, Cedente>> cedentesPorEmpresa) {
		CriarSimulacoesRecompra.cedentesPorEmpresa = cedentesPorEmpresa;
	}

	public static boolean isTest() {
		return test;
	}

	public static void setTest(boolean test) {
		CriarSimulacoesRecompra.test = test;
	}

}
