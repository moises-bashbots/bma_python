package conta_grafica;

import java.io.File;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import bradesco.CobrancaPIX;
import empresa.Empresa;
import mssql.ConnectorMSSQL;
import mysql.ConnectorMariaDB;
import utils.Utils;

public class ReaderExtratosBradesco {
	private static SimpleDateFormat sdf=new SimpleDateFormat("dd/MM/yyyy");
	private static SimpleDateFormat sdfd=new SimpleDateFormat("yyyy-MM-dd");
	private static ArrayList<ExtratoBradesco> extratosBradesco = new ArrayList<>();
	private static Connection connMaria=null;
	private static File folderExtrato = new File("/home/robot/Extrato");
	private static Connection connMSS=null;
	public static void main(String[] args)
	{
		ConnectorMariaDB.connect();
		ConnectorMSSQL.connect();
		connMaria=ConnectorMariaDB.conn;
		connMSS=ConnectorMSSQL.conn;
		System.out.println("Folder extrato: "+folderExtrato.getAbsolutePath());
		String[] pathNames = folderExtrato.list();
		for(String pathName:pathNames)
		{
			pathName=folderExtrato.getAbsolutePath()+File.separator+pathName;
			File fileInside = new File(pathName);
			if(!fileInside.isDirectory())
			{
				System.out.println("File on list: " + fileInside.getAbsolutePath());
				ExtratoBradesco extratoBradesco=readExtratoBradesco(fileInside);
				System.out.println("Processing extrato for " +extratoBradesco.getEmpresa().getApelido());
				processExtrato(extratoBradesco);
				extratosBradesco.add(extratoBradesco);
				String nameFileDestiny = folderExtrato+File.separator+"Processados"+File.separator+fileInside.getName();
				File destiny = new File(nameFileDestiny);
				Utils.copyFileAndDelete(fileInside,destiny);
			}
		}
	}
	
	public static void processExtrato(ExtratoBradesco extratoBradesco)
	{
		for(LancamentoBradesco lancamentoBradesco:extratoBradesco.getLancamentosBradesco())
		{
			System.out.println("Processing lancamento: " + lancamentoBradesco.getDescricao());
			lancamentoBradesco.show();
			processLancamentoBradesco(extratoBradesco.getEmpresa(), lancamentoBradesco);
		}
	}
	
	public static void processLancamentoBradesco(Empresa empresa, LancamentoBradesco lancamentoBradesco)
	{
		String query="select * from BMA.cobranca_pix "
							+ " where empresa_id_empresa="+empresa.getIdEmpresa()
							+ " and data_pix <= "+"'"+sdfd.format(lancamentoBradesco.getDataLancamento())+"'"
							+ " and valor="+lancamentoBradesco.getValor()
							+ " and pago=0";
		System.out.println(query);
		Statement st=null;
		try {
			st=connMaria.createStatement();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		ResultSet rs=null;
		int idCobrancaPix=0;

		try {
			rs = st.executeQuery(query);
			while(rs.next())
			{
				idCobrancaPix=rs.getInt("id_cobranca_pix");				
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		if(idCobrancaPix>0)
		{			
			CobrancaPIX.updatePago(connMaria, idCobrancaPix, true);
		}
	}
	
	
	public static ExtratoBradesco readExtratoBradesco(File fileExtratoCSV)
	{
		System.out.println("Trying to read lines on "+fileExtratoCSV.getAbsolutePath());
		ArrayList<String> lines = Utils.readLinesInFile(fileExtratoCSV.getAbsolutePath());
		String agencia="";
		String conta="";
		String digitoConta="";
		ArrayList<LancamentoBradesco> lancamentosBradesco = new ArrayList<>();
		for(String line:lines)
		{
//			System.out.println(line);
			String[] fields = null;
			if(line.toLowerCase().contains("extrato") && line.toLowerCase().contains("conta"))
			{
				fields = line.split(":");
				for(int i=0;i<fields.length;i++)
				{
					System.out.println(i+" > "+fields[i]);
					if(i==2)
					{
						String[] subfields=fields[i].trim().split(" ");
						agencia=subfields[0];
					}
					if(i==3)
					{
						String[] subfields=fields[i].trim().split(";");
						conta=subfields[0].split("-")[0];
						digitoConta=subfields[0].split("-")[1];
					}
				}
				System.out.println("Agencia: "+agencia+ " Conta: "+conta+"-"+digitoConta);
			}
			else
			{
				if( (line.toLowerCase().contains("data") && line.toLowerCase().contains("dcto")) 
						|| line.toLowerCase().startsWith("total")
						|| line.toLowerCase().contains("saldos invest f") 
						|| (line.toLowerCase().contains("amentos/opera")&&line.toLowerCase().contains("selecionado"))
						)
				{
					
				}
				else 
				{
					
					if(line.toLowerCase().contains("amento")&&line.toLowerCase().contains("futuro"))
					{
						break;
					}
					else if(line.length()>6)
					{
						System.out.println("P: "+line);
						fields=line.split(";");
						Date dataLancamento = null;
						try {
							dataLancamento = sdf.parse(fields[0]);
						} catch (ParseException e) {
							e.printStackTrace();
						}
						String descricaoLancamento=fields[1];
						String documentoLancamento=fields[2];
						String tipoLancamento="";
						if(fields[3].length()>0)
						{
							tipoLancamento="C";
						}
						else {
							tipoLancamento="D";
						}
						double valorLancamento=0.0;
						if(tipoLancamento.contains("C"))
						{
							valorLancamento=Utils.extractValueFromBrazilianNumber(fields[3]);
						}
						else {
							valorLancamento=Utils.extractValueFromBrazilianNumber(fields[4]);
						}
						double saldo=0;
						saldo=Utils.extractValueFromBrazilianNumber(fields[5]);
						System.out.println(sdfd.format(dataLancamento) +" | "+descricaoLancamento+" | "+documentoLancamento + " | "+tipoLancamento+ " | "+valorLancamento+" | "+saldo);
						LancamentoBradesco lancamentoBradesco = new LancamentoBradesco(dataLancamento, descricaoLancamento, documentoLancamento, tipoLancamento, valorLancamento, saldo);
						lancamentosBradesco.add(lancamentoBradesco);
					}
				}
			}
		}
		ExtratoBradesco extratoBradesco=new ExtratoBradesco(connMaria, connMSS, agencia, conta, digitoConta, lancamentosBradesco);
		return extratoBradesco;
	}
}
