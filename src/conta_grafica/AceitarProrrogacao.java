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
import rgbsys.RgbsisLancamentoCritica;
import rgbsys.RgbsysSeleniumProrrogacao;
import rgbsys.RgbsysUser;
import utils.Utils;

public class AceitarProrrogacao {
	private static Connection connMaria=null;
	private static Connection connMSS=null;
	private ArrayList<Saldo> saldos = new ArrayList<>();
	private ArrayList<Saldo> saldosNegativos = new ArrayList<>();
	private static SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
	private static SimpleDateFormat sdfa = new SimpleDateFormat("yyyy-MM-dd");
	private static HashMap<String,  String> barrados=new HashMap<>();
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
		readBarrados();
		
		RgbsysUser.readConf();
		RgbsysSeleniumProrrogacao rgbsysSeleniumProrrogacao = new RgbsysSeleniumProrrogacao();
		ArrayList<Critica> criticas = rgbsysSeleniumProrrogacao.prorrogacao(connMaria, connMSS);
		System.out.println("Total de críticas: "+criticas.size());
		rgbsysSeleniumProrrogacao.close();
		if(criticas.size()>0)
		{
			RgbsisLancamentoCritica rgbsisLancamentoCritica = new RgbsisLancamentoCritica();
			for(Critica critica:criticas)
			{
				// rgbSysSelenium.gravacaoCritica( "PRORROGACAO", "116748NE1", "BMA FIDC", "MAXIBRASIL");
				if(!critica.isRegistrado())
				{
					rgbsisLancamentoCritica.gravacaoCritica(critica);
					critica.setRegistrado(true);
					critica.updateRegistrado(connMaria);
				}
			}
			rgbsisLancamentoCritica.close();
		}
	}

	public static void readBarrados()
	{
		ArrayList<String> lines = Utils.readLinesInFile("/home/robot/App/Conf/barradosProrrogacao.conf");
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
				if(barrados.get(keyEmpresaCedente)==null)
				{
					barrados.put(keyEmpresaCedente, keyEmpresaCedente);
				}
			}
		}
	}
	
	public static Connection getConnMaria() {
		return connMaria;
	}

	public static void setConnMaria(Connection connMaria) {
		AceitarProrrogacao.connMaria = connMaria;
	}

	public static Connection getConnMSS() {
		return connMSS;
	}

	public static void setConnMSS(Connection connMSS) {
		AceitarProrrogacao.connMSS = connMSS;
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
		AceitarProrrogacao.sdfa = sdfa;
	}

	public static HashMap<String, Empresa> getEmpresas() {
		return empresas;
	}

	public static void setEmpresas(HashMap<String, Empresa> empresas) {
		AceitarProrrogacao.empresas = empresas;
	}

	public static HashMap<String, HashMap<String, Cedente>> getCedentesPorEmpresa() {
		return cedentesPorEmpresa;
	}

	public static void setCedentesPorEmpresa(HashMap<String, HashMap<String, Cedente>> cedentesPorEmpresa) {
		AceitarProrrogacao.cedentesPorEmpresa = cedentesPorEmpresa;
	}

	public static boolean isTest() {
		return test;
	}

	public static void setTest(boolean test) {
		AceitarProrrogacao.test = test;
	}

	public static HashMap<String, String> getBarrados() {
		return barrados;
	}

	public static void setBarrados(HashMap<String, String> barrados) {
		AceitarProrrogacao.barrados = barrados;
	}

}
