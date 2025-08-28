package consulta_ssw;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

import mssql.ConnectorMSSQL;
import mysql.ConnectorMariaDB;
import rgbsys.RgbsysCriticaConsultaSSW;
import rgbsys.RgbsysUser;
import utils.Utils;

public class SalvarCriticasConsultaSSW {
	private static Connection connMaria=null;
	private static Connection connMSS=null;
	private static SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
	private static SimpleDateFormat sdfa = new SimpleDateFormat("yyyy-MM-dd");
	
	private ArrayList<TituloParaChecagem> titulosParaChecagem = new ArrayList<>();

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

		RgbsysUser.readConf();
		
		RgbsysCriticaConsultaSSW rgbsysCriticaConsultaSSW = new RgbsysCriticaConsultaSSW(RgbsysUser.userName,RgbsysUser.password);
		rgbsysCriticaConsultaSSW.openChecagem();
		ArrayList<TituloParaChecagem> titulosParaChecagem=TituloParaChecagem.titulosParaChecagem(connMSS, 60);
		System.out.println("There are "+titulosParaChecagem.size() + " Titulos para checagem!");
		int iTitulo=0;
		for(TituloParaChecagem tituloParaChecagem:titulosParaChecagem)
		{
			tituloParaChecagem.show();
			ArrayList<Evento> eventosCadastrados = Evento.eventosNaoCadastradosPorChave(connMaria, tituloParaChecagem.getChaveNFE());
			
			if(eventosCadastrados.size()>0)
			{
				tituloParaChecagem.show();
				System.out.println("EventosCadastrados: "+eventosCadastrados.size());
				for(Evento evento:eventosCadastrados)
				{
					evento.show();
				}
				try {
					rgbsysCriticaConsultaSSW.filterTitulo(tituloParaChecagem);
				} catch (Exception e) {
					e.printStackTrace();
					break;
				}
				ArrayList<TituloParaChecagem> titulosParaChecagemMesmaChave = TituloParaChecagem.titulosParaChecagemPorChave(connMSS, tituloParaChecagem.getChaveNFE());
				try {
					rgbsysCriticaConsultaSSW.selectAndFillChecagem(connMaria,titulosParaChecagemMesmaChave,eventosCadastrados);
				} catch (Exception e) {
					e.printStackTrace();
					break;
				}
				iTitulo++;
				rgbsysCriticaConsultaSSW.refreshChecagem();
			}
			else
			{
				System.out.println("Sem eventos cadastrados para este título!");
			}
			
		}
		Utils.waitv(60);
		rgbsysCriticaConsultaSSW.close();
	}
	
	public static void showCurrentProgress()
	{
		String query="select count(*) as contagem,evento_cadastrado  from BMA.evento_transporte group by evento_cadastrado";
		Statement st=null;
		try {
			st=connMaria.createStatement();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		int total=0;
		int cadastrado=0;
		int faltaCadastrar=0;
		ResultSet rs=null;
		try {
			rs=st.executeQuery(query);
			while(rs.next())
			{
				if(rs.getInt("evento_cadastrado")==1)
				{
					cadastrado+=rs.getInt("contagem");
				}
				total+=rs.getInt("contagem");
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		faltaCadastrar=total-cadastrado;
		System.out.println("********************************");
		System.out.println("********************************");

		System.out.println("Total de eventos: "+total);
		System.out.println("Eventos cadastrados: "+cadastrado);
		System.out.println("Eventos sem cadastro: "+faltaCadastrar);
		System.out.println("********************************");
		System.out.println("********************************");


	}

	public static Connection getConnMaria() {
		return connMaria;
	}

	public static void setConnMaria(Connection connMaria) {
		SalvarCriticasConsultaSSW.connMaria = connMaria;
	}

	public static Connection getConnMSS() {
		return connMSS;
	}

	public static void setConnMSS(Connection connMSS) {
		SalvarCriticasConsultaSSW.connMSS = connMSS;
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
		SalvarCriticasConsultaSSW.sdfa = sdfa;
	}

	public static boolean isTest() {
		return test;
	}

	public static void setTest(boolean test) {
		SalvarCriticasConsultaSSW.test = test;
	}

}
