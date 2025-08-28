package bradesco;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.KeyStore;
import java.security.SecureRandom;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;
import java.util.Map;
import java.util.UUID;

import javax.json.JsonObject;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import cedente.Cedente;
import conta_grafica.OperacaoRecompra;
import conta_grafica.Saldo;
import empresa.Empresa;
import instrucao.TipoInstrucao;
import rgbsys.RgbsysSeleniumReposicaoCedenteCarteira;
import rgbsys.RgbsysUser;
import utils.Utils;

public class CobrancaPIX 
{
	private int idCobrancaPIX=0;
	private JsonObject jsonPIXCobranca=null;
	private Empresa empresa= new Empresa();
	private Cedente cedente= new Cedente();
	private TipoInstrucao tipoInstrucao = new TipoInstrucao();
	private String jsonStringCobranca="";
	private String txid="";
	private StatusCobrancaPIX status= new StatusCobrancaPIX();
	private String location="";
	private int revisao=0;
	private Date dataPix=null;
	private Date criacao=null;
	private double valor=0;
	private String chave="";
	private String pixCopiaECola="";
	private String codigoUnico="";
	private boolean pago=false;
	private static SimpleDateFormat sdf=new SimpleDateFormat("yyyy-MM-dd");
	private static SimpleDateFormat sdfr=new SimpleDateFormat("dd/MM/yyyy");
	private static SimpleDateFormat sdfp=new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
	private static SimpleDateFormat sdft=new SimpleDateFormat("HH:mm:ss.SSS");
	private static RgbsysUser userRgb=new RgbsysUser();
	private ArrayList<PIX> pixes = new ArrayList<PIX>();
	
	public CobrancaPIX()
	{
		
	}
	
	public CobrancaPIX(OperacaoRecompra operacaoRecompra, TipoInstrucao tipoInstrucao)
	{
		this.tipoInstrucao=tipoInstrucao;
		this.empresa=operacaoRecompra.getEmpresa();
		this.cedente=operacaoRecompra.getCedente();
		this.dataPix=Calendar.getInstance().getTime();
//		this.valor=Math.abs(this.saldo.getValor());
		String stringValor=new DecimalFormat("#0.00#").format((Math.abs(operacaoRecompra.getValor())));
	    this.valor=Double.parseDouble(stringValor);
		
		UUID uuid = UUID.randomUUID();
		this.txid= uuid.toString().replaceAll("-", "");

		this.codigoUnico = Utils.uniqueStringPIX(
				35
				,operacaoRecompra.getDataRecompra()
				,this.tipoInstrucao.getTipoInstrucao()
				, operacaoRecompra.getCedente().getParticipante().getCadastro()
				, operacaoRecompra.getEmpresa().getDadosBancariosEmpresa().getAgencia()
				, operacaoRecompra.getEmpresa().getDadosBancariosEmpresa().getConta()+operacaoRecompra.getEmpresa().getDadosBancariosEmpresa().getDigitoConta()
				, operacaoRecompra.getEmpresa().getCnpj()
				, Double.toString(Math.abs(this.getValor()))
				);
		
		
		String tipoDocumento="";
		if(operacaoRecompra.getCedente().getParticipante().getCadastro().length()==14)
		{
			tipoDocumento="cnpj";
		}
		else
		{
			tipoDocumento="cpf";
		}
		Date limit = null;
		Calendar calLimit=Calendar.getInstance();
		calLimit.set(Calendar.HOUR_OF_DAY,21);
		calLimit.set(Calendar.MINUTE,0);
		calLimit.set(Calendar.SECOND,0);
		limit=calLimit.getTime();
		
		int secondsLeft=Utils.secondsLeftToTime(limit);		

		
		this.jsonStringCobranca="{\r\n    \"txid\": \""
				+ this.txid
				+ "\",\r\n    \"calendario\": {\r\n        \"expiracao\": \""
				+ secondsLeft
				+ "\"\r\n    },\r\n    \"devedor\": {\r\n        \""
				+ tipoDocumento
				+ "\": \""
				+ operacaoRecompra.getCedente().getParticipante().getCadastro()
				+ "\",\r\n        \"nome\": \""
				+ operacaoRecompra.getCedente().getParticipante().getRazaoSocial()
				+ "\"\r\n    },\r\n    \"valor\": {\r\n        \"original\": \""
				+ new DecimalFormat("#0.00#").format(this.valor)
				+ "\"\r\n    },\r\n    \"chave\": \""
				+ operacaoRecompra.getEmpresa().getDadosBancariosEmpresa().getChavePIX()
				+ "\",\r\n    \"solicitacaopagador\": \""
				+ "Regularizacao de saldo negativo"
				+ "\"\r\n}";
	}
	
	public CobrancaPIX(int idCobrancaPIX, Connection connMaria, Connection connMSS)
	{
		this.idCobrancaPIX=idCobrancaPIX;
		String query="select * from BMA.cobranca_pix where"
				+ " id_cobranca_pix="+this.idCobrancaPIX;
;
		System.out.println(query);

		Statement st=null;
		try {
			st=connMaria.createStatement();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		
		ResultSet rs=null;
		try {
			rs=st.executeQuery(query);
			while(rs.next())
			{
				this.valor=rs.getDouble("valor");
				this.tipoInstrucao=new TipoInstrucao(connMaria, rs.getInt("tipo_instrucao_id_tipo_instrucao"));
				this.codigoUnico = rs.getString("codigo_unico");
				this.txid= rs.getString("txid");
				this.empresa=new Empresa(connMaria, connMSS, rs.getInt("empresa_id_empresa"));
				this.cedente=new Cedente(connMaria, connMSS, rs.getInt("cedente_id_cedente"), this.empresa);
				this.dataPix=rs.getDate("data_pix");
				this.status=new StatusCobrancaPIX(connMaria, rs.getInt("status_cobranca_pix_id_status_cobranca_pix"));
				this.location=rs.getString("location");
				this.revisao=rs.getInt("revisao");
				this.criacao=rs.getTime("criacao");
				this.valor=rs.getDouble("valor");
				this.chave=rs.getString("chave");
				this.pixCopiaECola=rs.getString("pix_copia_e_cola");
				int pagoInt=rs.getInt("pago");
				if(pagoInt==1)
				{
					this.setPago(true);
				}				
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
	public CobrancaPIX(OperacaoRecompra operacaoRecompra, TipoInstrucao tipoInstrucao, Connection connMaria)
	{
		this.tipoInstrucao=tipoInstrucao;
		this.empresa=operacaoRecompra.getEmpresa();
		this.cedente=operacaoRecompra.getCedente();
		this.dataPix=operacaoRecompra.getDataRecompra();
//		this.valor=Math.abs(this.saldo.getValor());
		String stringValor=new DecimalFormat("#0.00#").format((Math.abs(operacaoRecompra.getValor())));
	    this.valor=Double.parseDouble(stringValor);
		
		UUID uuid = UUID.randomUUID();
		this.txid= uuid.toString().replaceAll("-", "");

		
		this.codigoUnico = Utils.uniqueStringPIX(
				35
				,operacaoRecompra.getDataRecompra()
				,this.tipoInstrucao.getTipoInstrucao()
				, operacaoRecompra.getCedente().getParticipante().getCadastro()
				, operacaoRecompra.getEmpresa().getDadosBancariosEmpresa().getAgencia()
				, operacaoRecompra.getEmpresa().getDadosBancariosEmpresa().getConta()+operacaoRecompra.getEmpresa().getDadosBancariosEmpresa().getDigitoConta()
				, operacaoRecompra.getEmpresa().getCnpj()
				, Double.toString(Math.abs(this.getValor()))
				);
		
		String query="select * from BMA.cobranca_pix where"
				+ " codigo_unico='"+this.codigoUnico+"'"
				+ " and data_pix='"+sdf.format(this.dataPix)+"'";
;
		System.out.println(query);
		Statement st=null;
		try {
			st=connMaria.createStatement();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		
		ResultSet rs=null;
		try {
			rs=st.executeQuery(query);
			while(rs.next())
			{
				this.idCobrancaPIX=rs.getInt("id_cobranca_pix");
				this.codigoUnico=rs.getString("codigo_unico");
				this.txid=rs.getString("txid");
				this.status=new StatusCobrancaPIX(connMaria, rs.getInt("status_cobranca_pix_id_status_cobranca_pix"));
				this.location=rs.getString("location");
				this.revisao=rs.getInt("revisao");
				this.criacao=rs.getTime("criacao");
				this.valor=rs.getDouble("valor");
				this.chave=rs.getString("chave");
				this.pixCopiaECola=rs.getString("pix_copia_e_cola");
				int pagoInt=rs.getInt("pago");
				if(pagoInt==1)
				{
					this.setPago(true);
				}				
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	
	public CobrancaPIX(Saldo saldo, TipoInstrucao tipoInstrucao)
	{
		this.tipoInstrucao=tipoInstrucao;
		this.empresa=saldo.getEmpresa();
		this.cedente=saldo.getCedente();
		this.dataPix=Calendar.getInstance().getTime();
//		this.valor=Math.abs(this.saldo.getValor());
		String stringValor=new DecimalFormat("#0.00#").format((Math.abs(saldo.getValor())));
	    this.valor=Double.parseDouble(stringValor);
		
		UUID uuid = UUID.randomUUID();
		this.txid= uuid.toString().replaceAll("-", "");

		this.codigoUnico = Utils.uniqueStringPIX(
																	35
																	,saldo.getDataSaldo()
																	,this.tipoInstrucao.getTipoInstrucao()
																	, saldo.getCedente().getParticipante().getCadastro()
																	, saldo.getEmpresa().getDadosBancariosEmpresa().getAgencia()
																	, saldo.getEmpresa().getDadosBancariosEmpresa().getConta()+saldo.getEmpresa().getDadosBancariosEmpresa().getDigitoConta()
																	, saldo.getEmpresa().getCnpj()
																	, Double.toString(Math.abs(saldo.getValor()))
																	);
		
		
		String tipoDocumento="";
		if(saldo.getCedente().getParticipante().getCadastro().length()==14)
		{
			tipoDocumento="cnpj";
		}
		else
		{
			tipoDocumento="cpf";
		}
		Date limit = null;
		Calendar calLimit=Calendar.getInstance();
		calLimit.set(Calendar.HOUR_OF_DAY,21);
		calLimit.set(Calendar.MINUTE,0);
		calLimit.set(Calendar.SECOND,0);
		limit=calLimit.getTime();
		
		int secondsLeft=Utils.secondsLeftToTime(limit);		

		
		this.jsonStringCobranca="{\r\n    \"txid\": \""
				+ this.txid
				+ "\",\r\n    \"calendario\": {\r\n        \"expiracao\": \""
				+ secondsLeft
				+ "\"\r\n    },\r\n    \"devedor\": {\r\n        \""
				+ tipoDocumento
				+ "\": \""
				+ saldo.getCedente().getParticipante().getCadastro()
				+ "\",\r\n        \"nome\": \""
				+ saldo.getCedente().getParticipante().getRazaoSocial()
				+ "\"\r\n    },\r\n    \"valor\": {\r\n        \"original\": \""
				+ new DecimalFormat("#0.00#").format(this.valor)
				+ "\"\r\n    },\r\n    \"chave\": \""
				+ saldo.getEmpresa().getDadosBancariosEmpresa().getChavePIX()
				+ "\",\r\n    \"solicitacaopagador\": \""
				+ "Regularizacao de saldo negativo"
				+ "\"\r\n}";
	}
	
	public CobrancaPIX(Connection connMaria, Connection connMSS, String responseString)
	{
		this.processResponseCobranca(connMaria, connMSS, responseString);
		if(this.idCobrancaPIX==0)
		{
			String query="select * from BMA.cobranca_pix"
								+ " where txid=''"+this.txid+"'"
								+ " and location='"+this.location+"'"
								+ " and criacao='"+sdfp.format(this.criacao)+"'";
			Statement st=null;
			try {
				st=connMaria.createStatement();
			} catch (SQLException e) {
				e.printStackTrace();
			}
			ResultSet rs=null;
			try {
				rs=st.executeQuery(query);
				while(rs.next())
				{
					this.idCobrancaPIX=rs.getInt("id_cobranca_pix");
				}
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
	}
	
	
	public  void processResponseCobranca(Connection connMaria, Connection connMSS, String responseString)
	{
		JSONParser parser = new JSONParser();
		try 
		{
			JSONObject jsonResposta = (JSONObject) parser.parse(responseString);
			System.out.println("JSON resposta");
			System.out.println(responseString);
			
			this.txid= jsonResposta.get("txid").toString();
			this.location=jsonResposta.get("location").toString();
			this.revisao=Integer.parseInt(jsonResposta.get("revisao").toString());
			this.status=new StatusCobrancaPIX(connMaria, jsonResposta.get("status").toString());
			
			Map calendario = (Map) jsonResposta.get("calendario");
			Iterator<Map.Entry> iterator = calendario.entrySet().iterator();
			while(iterator.hasNext())
			{
				Map.Entry pairEntry = iterator.next();
				switch (pairEntry.getKey().toString()) {
				case "criacao":
					String criacaoString = pairEntry.getValue().toString();
					criacaoString=criacaoString.replaceAll("[a-zA-Z]", " ").trim();
					if(criacaoString.length()==19)
					{
						criacaoString+=".000";
					}
					try {
						System.out.println("Parsing: "+criacaoString);
						this.criacao=sdfp.parse(criacaoString);
					} catch (java.text.ParseException e) {
						e.printStackTrace();
					}
					break;
				default:
					break;
				}
//				System.out.println(pairEntry.getKey()+ ": " + pairEntry.getValue());
			}
			Map devedor = (Map) jsonResposta.get("devedor");
			iterator = devedor.entrySet().iterator();
			while(iterator.hasNext())
			{
				Map.Entry pairEntry = iterator.next();				
//				System.out.println(pairEntry.getKey()+ ": " + pairEntry.getValue());
			}
			Map valor = (Map) jsonResposta.get("valor");
			iterator = valor.entrySet().iterator();
			while(iterator.hasNext())
			{
				Map.Entry pairEntry = iterator.next();
				switch (pairEntry.getKey().toString()) {
				case "original":
					this.valor=Double.parseDouble(pairEntry.getValue().toString());
					break;
				default:
					break;
				}
//				System.out.println(pairEntry.getKey()+ ": " + pairEntry.getValue());
			}
//			System.out.println("chave: " +);
			this.chave= jsonResposta.get("chave").toString();
			Map loc = (Map) jsonResposta.get("loc");
			iterator = loc.entrySet().iterator();
			while(iterator.hasNext())
			{
				Map.Entry pairEntry = iterator.next();
//				System.out.println(pairEntry.getKey()+ ": " + pairEntry.getValue());
			}
			if(jsonResposta.get("pixCopiaECola")!=null)
			{
				this.pixCopiaECola=jsonResposta.get("pixCopiaECola").toString();
			}
			
			if(this.idCobrancaPIX==0)
			{
				String query="select * from BMA.cobranca_pix where"
						+ " txid='"+this.txid+"'"
						+ " and location='"+this.location+"'"
						+ " and valor="+this.valor
						+ " and chave='"+this.chave+"'";
				System.out.println(query);
				Statement st=null;
				try {
					st=connMaria.createStatement();
				} catch (SQLException e) {
					e.printStackTrace();
				}
				
				ResultSet rs=null;
				try {
					rs=st.executeQuery(query);
					while(rs.next())
					{
						this.idCobrancaPIX=rs.getInt("id_cobranca_pix");
						this.empresa=new Empresa(connMaria, connMSS, rs.getInt("empresa_id_empresa"));
						this.cedente=new Cedente(connMaria, connMSS, rs.getInt("cedente_id_cedente"), this.empresa);
						this.tipoInstrucao=new TipoInstrucao(connMaria, rs.getInt("tipo_instrucao_id_tipo_instrucao"));
						this.codigoUnico=rs.getString("codigo_unico");
						this.dataPix=rs.getDate("data_pix");
						int pagoInt=rs.getInt("pago");
						if(pagoInt==1)
						{
							this.setPago(true);
						}
						
					}
				} catch (SQLException e) {
					e.printStackTrace();
				}
			}
			
			
			if(jsonResposta.get("pix")!=null)
			{				
				JSONArray pixs = (JSONArray) jsonResposta.get("pix");
				for(int i=0; i < pixs.size();i++)
				{
					System.out.println(i+ ": -----------------");
					JSONObject element=(JSONObject) pixs.get(i);
					System.out.println(element.toString());
					String horarioString=element.get("horario").toString();
					horarioString=horarioString.replaceAll("[A-Z]", " ");
					String valorString=element.get("valor").toString();
					String txidString=element.get("txid").toString();
					String endToEndIdString=element.get("endToEndId").toString();
					Date horarioPagamento = null;
					try {
						horarioPagamento = sdfp.parse(horarioString);
					} catch (java.text.ParseException e) {
						e.printStackTrace();
					}
					double valorPagamento = Double.parseDouble(valorString);
					
//					PIX pix = new PIX(horarioPagamento, valorPagamento, txidString, endToEndIdString) ;
					PIX pix = new PIX(connMaria, this.idCobrancaPIX, horarioPagamento,valorPagamento, txidString, endToEndIdString);
					pix.show();
					if(!pix.isRegistrado())
					{
						long howOldPix=Utils.getDifferenceDays(pix.getHorario(), Calendar.getInstance().getTime());
						if(howOldPix<=1)
						{
							RgbsysSeleniumReposicaoCedenteCarteira rgbSysSelenium = new RgbsysSeleniumReposicaoCedenteCarteira();
							try
							{ 
	//							rgbSysSelenium.login();
								
							    rgbSysSelenium.reposicaoCedenteCarteira(this.cedente.getApelido(), 
							    																		this.empresa.getDadosBancariosEmpresa().getApelido(), 
							    																		sdfr.format(pix.getHorario()),
							    																		pix.getValor());
							    pix.updateRegistrado(connMaria, true);
							}
							finally
							{
							    rgbSysSelenium.getDriver().close();
							}
						}
						this.pixes.add(pix);							
						System.out.println(" -----------------");						
					}
				}
				double totalPago=0;
				for(PIX pix:this.pixes)
				{
					totalPago+=pix.getValor();
				}
				if(totalPago==this.getValor())
				{
					if(this.isPago()==false)
					{
						boolean pago=true;
						this.updatePago(connMaria, pago);
					}
				}
			}
			
			System.out.println("txid: " + this.txid);
			System.out.println("status: " + this.status.getStatus());
			System.out.println("location: " + this.location);
			System.out.println("revisao: " + this.revisao);
			System.out.println("criacao: " + sdfp.format(this.criacao));
			System.out.println("valor: " + this.valor);
			System.out.println("chave: " + this.chave);
			System.out.println("pixCopiaECola: " + this.pixCopiaECola);
			System.out.println("codigoUnico: " + this.codigoUnico);
			this.updateStatus(connMaria);
			if(this.pixes.size()>0)
			{
				for(PIX pix:this.pixes)
				{
					pix.show();
				}
			}
			
		} 
		catch (ParseException e) 
		{
			e.printStackTrace();
		}
	}
	
	public void show()
	{
		System.out.println("-------------------------------------------------------------------------");
		System.out.println("idCobrancaPIX: " + this.idCobrancaPIX);
		System.out.println("Empresa: "+this.empresa.getApelido());
		System.out.println("Cedente: "+this.cedente.getApelido());
		System.out.println("TipoInstrucao: "+ this.tipoInstrucao.getTipoInstrucao());
		System.out.println("CodigoUnico: "+ this.codigoUnico);
		System.out.println("DataPix: "+ this.dataPix);
		System.out.println("Pago: "+ this.isPago());
		System.out.println("Valor: " +this.valor);
	}

	public  void processResponsePIX(Connection connMaria, Connection connMSS, Empresa empresa, String responseString)
	{
		JSONParser parser = new JSONParser();
		try 
		{
			JSONObject jsonResposta = (JSONObject) parser.parse(responseString);
			System.out.println("***********************************");
			System.out.println("JSON resposta PIX");
			System.out.println(responseString);
			System.out.println("***********************************");
			String endToEndId= jsonResposta.get("endToEndId").toString();
			double valor=Double.parseDouble(jsonResposta.get("valor").toString());
			String chave= "";
			if(jsonResposta.get("chave")!=null)
			{
				chave= jsonResposta.get("chave").toString();
			}
			String horarioString = jsonResposta.get("horario").toString();
			horarioString=horarioString.replaceAll("[a-zA-Z]", " ").trim();
			Date horario = null;
			try {
				horario = sdfp.parse(horarioString);
			} catch (java.text.ParseException e) {
				e.printStackTrace();
			}		
			Map pagador = (Map) jsonResposta.get("pagador");
			Iterator<Map.Entry> iterator = pagador.entrySet().iterator();
			String nomePagador ="";
			String cnpjPagador ="";
			while(iterator.hasNext())
			{
				Map.Entry pairEntry = iterator.next();
				switch (pairEntry.getKey().toString()) {
				case "nome":
					nomePagador = pairEntry.getValue().toString();
					break;
				case "cnpj":
					cnpjPagador = pairEntry.getValue().toString();
					break;
				default:
					break;
				}
				System.out.println(pairEntry.getKey()+ ": " + pairEntry.getValue());
			}
			String infoPagador="";
			if(jsonResposta.get("infoPagador")!=null)
			{
				infoPagador=jsonResposta.get("infoPagador").toString();
			}
			System.out.println("--- Pagamento PIX");
			System.out.println("Empresa: "+ empresa.getApelido());
			System.out.println("endToEndId: "+endToEndId);
			System.out.println("valor: "+valor);
			System.out.println("chave: "+chave);
			System.out.println("horario: "+horario);
			System.out.println("infoPagador: "+infoPagador);
			System.out.println("pagador: "+nomePagador);
			System.out.println("CNPJpagador: "+cnpjPagador);
			System.out.println("--- End Pagamento PIX");
			
			if(this.idCobrancaPIX==0)
			{
				String query="select cp.* from "
						+ "BMA.cobranca_pix cp, BMA.cedente c, BMA.participante p"
						+ " where"
						+ " cp.empresa_id_empresa="+empresa.getIdEmpresa()
						+ " and cp.valor="+valor
						+" and c.id_cedente=cp.cedente_id_cedente"
						+ " and c.participante_id_participante=p.id_participante"
						+ " and p.cadastro="+"'"+cnpjPagador+"'";
						;
				
				System.out.println(query);
				Statement st=null;
				try {
					st=connMaria.createStatement();
				} catch (SQLException e) {
					e.printStackTrace();
				}
				
				ResultSet rs=null;
				try {
					rs=st.executeQuery(query);
					while(rs.next())
					{
						System.out.println("-- CobrancaPIX encontrada");
						this.idCobrancaPIX=rs.getInt("id_cobranca_pix");
						this.empresa=new Empresa(connMaria, connMSS, rs.getInt("empresa_id_empresa"));
						this.cedente=new Cedente(connMaria, connMSS, rs.getInt("cedente_id_cedente"), this.empresa);
						this.tipoInstrucao=new TipoInstrucao(connMaria, rs.getInt("tipo_instrucao_id_tipo_instrucao"));
						this.codigoUnico=rs.getString("codigo_unico");
						this.dataPix=rs.getDate("data_pix");
						int pagoInt=rs.getInt("pago");
						if(pagoInt==1)
						{
							this.setPago(true);
						}			
						this.updatePago(connMaria, pago);
						this.show();
					}
				} catch (SQLException e) {
					e.printStackTrace();
				}
			}
			
			if(this.idCobrancaPIX!=0)
			{				
				this.show();
				PIX pix = new PIX(connMaria, idCobrancaPIX, horario, valor, chave, infoPagador, endToEndId);
				pix.show();
				long diasDoPagamento = Utils.getDifferenceDays(pix.getHorario(), Calendar.getInstance().getTime());
				if(!pix.isRegistrado() && diasDoPagamento <= 1)
				{
					
					RgbsysSeleniumReposicaoCedenteCarteira rgbSysSelenium = new RgbsysSeleniumReposicaoCedenteCarteira();
					try
					{ 
//						rgbSysSelenium.login();
					    rgbSysSelenium.reposicaoCedenteCarteira(this.cedente.getApelido(), 
					    		this.empresa.getDadosBancariosEmpresa().getApelido(), 
					    		sdfr.format(pix.getHorario()), 
					    		pix.getValor());
					    pix.updateRegistrado(connMaria, true);
					}
					finally
					{
					    rgbSysSelenium.getDriver().close();
					}						
				}
				this.pixes.add(pix);
				double totalPago=0;
				for(PIX p:this.pixes)
				{
					totalPago+=p.getValor();
				}
				if(totalPago==this.getValor())
				{
					if(this.isPago()==false)
					{
						boolean pago=true;
						this.updatePago(connMaria, pago);
					}
				}
				System.out.println(" -----------------");						
			}
			else {
				System.out.println("-- CobrancaPIX NAO encontrada!");
			}
			
			
//			
//			
//			if(jsonResposta.get("pix")!=null)
//			{				
//				JSONArray pixs = (JSONArray) jsonResposta.get("pix");
//				for(int i=0; i < pixs.size();i++)
//				{
//					System.out.println(i+ ": -----------------");
//					JSONObject element=(JSONObject) pixs.get(i);
//					System.out.println(element.toString());
//					String horarioString=element.get("horario").toString();
//					horarioString=horarioString.replaceAll("[A-Z]", " ");
//					String valorString=element.get("valor").toString();
//					String txidString=element.get("txid").toString();
//					String endToEndIdString=element.get("endToEndId").toString();
//					Date horarioPagamento = null;
//					try {
//						horarioPagamento = sdfp.parse(horarioString);
//					} catch (java.text.ParseException e) {
//						e.printStackTrace();
//					}
//					double valorPagamento = Double.parseDouble(valorString);
//					
////					PIX pix = new PIX(horarioPagamento, valorPagamento, txidString, endToEndIdString) ;
//					PIX pix = new PIX(connMaria, this.idCobrancaPIX, horarioPagamento,valorPagamento, txidString, endToEndIdString);
//					pix.show();
//					if(!pix.isRegistrado())
//					{
//						RgbsysSeleniumReposicaoCedenteCarteira rgbSysSelenium = new RgbsysSeleniumReposicaoCedenteCarteira();
//						try
//						{ 
////							rgbSysSelenium.login();
//						    rgbSysSelenium.reposicaoCedenteCarteira(this.cedente.getApelido(), this.empresa.getDadosBancariosEmpresa().getApelido(), sdfr.format(pix.getHorario()), pix.getValor());
//						    pix.updateRegistrado(connMaria, true);
//						}
//						finally
//						{
//						    rgbSysSelenium.getDriver().close();
//						}
//						this.pixes.add(pix);							
//						System.out.println(" -----------------");						
//					}
//				}
//				double totalPago=0;
//				for(PIX pix:this.pixes)
//				{
//					totalPago+=pix.getValor();
//				}
//				if(totalPago==this.getValor())
//				{
//					if(this.isPago()==false)
//					{
//						boolean pago=true;
//						this.updataPago(connMaria, pago);
//					}
//				}
//			}
//			
//			System.out.println("txid: " + this.txid);
//			System.out.println("status: " + this.status.getStatus());
//			System.out.println("location: " + this.location);
//			System.out.println("revisao: " + this.revisao);
//			System.out.println("criacao: " + sdfp.format(this.criacao));
//			System.out.println("valor: " + this.valor);
//			System.out.println("chave: " + this.chave);
//			System.out.println("pixCopiaECola: " + this.pixCopiaECola);
//			System.out.println("codigoUnico: " + this.codigoUnico);
//			this.updateStatus(connMaria);
//			if(this.pixes.size()>0)
//			{
//				for(PIX pix:this.pixes)
//				{
//					pix.show();
//				}
//			}
			
		} 
		catch (ParseException e) 
		{
			e.printStackTrace();
		}
	}
	
	
	public   static void processResponseListaCobranca(Connection connMaria, Connection connMSS, String responseString, RgbsysUser userRGB)
	{
		if(CobrancaPIX.userRgb.getUserName().length()==0)
		{
			CobrancaPIX.userRgb=userRGB;
		}
		JSONParser parser = new JSONParser();
		try 
		{
			JSONObject jsonResposta = (JSONObject) parser.parse(responseString);
			System.out.println("JSON resposta de consulta lista");
			JSONArray cobrancas = (JSONArray) jsonResposta.get("cobs");
			Iterator iterator = cobrancas.iterator();
			int iCob=0;
			while(iterator.hasNext())
			{
				iCob++;
				JSONObject cobranca = (JSONObject) iterator.next();				
				CobrancaPIX cobrancaPIX = new CobrancaPIX();
				System.out.println(iCob+" **********  ");
				cobrancaPIX.processResponseCobranca(connMaria, connMSS, cobranca.toString());
				
			}
		} 
		catch (ParseException e) 
		{
			e.printStackTrace();
		}
	}
	
	public   static void processResponseListaPIX(Connection connMaria, Connection connMSS, Empresa empresa, String responseString, RgbsysUser userRGB)
	{
		if(CobrancaPIX.userRgb.getUserName().length()==0)
		{
			CobrancaPIX.userRgb=userRGB;
		}
		JSONParser parser = new JSONParser();
		try 
		{
			JSONObject jsonResposta = (JSONObject) parser.parse(responseString);
			System.out.println("JSON resposta de consulta lista de pix");
			JSONArray pixes = (JSONArray) jsonResposta.get("pix");
			Iterator iterator = pixes.iterator();
			int iCob=0;
			while(iterator.hasNext())
			{
				iCob++;
				JSONObject pix = (JSONObject) iterator.next();				
				CobrancaPIX cobrancaPIX = new CobrancaPIX();
				System.out.println(iCob+" **********  ");
				cobrancaPIX.processResponsePIX(connMaria, connMSS, empresa, pix.toString());				
			}
		} 
		catch (ParseException e) 
		{
			e.printStackTrace();
		}
	}
	
	public boolean checkExists(Connection connMaria)
	{
		boolean exists=false;
		System.out.println("-- Checking existence of CobrancaPIX "+this.codigoUnico);
		if(this.codigoUnico.length()>0)
		{
			String query="select * from BMA.cobranca_pix"
					+ " where "
					+ " empresa_id_empresa="+this.empresa.getIdEmpresa()
					+ " and cedente_id_cedente="+this.cedente.getIdCedente()
					+ " and tipo_instrucao_id_tipo_instrucao="+this.tipoInstrucao.getIdTipoInstrucao()
					+ " and codigo_unico= '"+this.codigoUnico+"'"
					+ " and data_pix='"+sdf.format(this.dataPix)+"'"
					+ " and valor="+this.valor;
			System.out.println(query);
			Statement st=null;
			try {
				st=connMaria.createStatement();
			} catch (SQLException e) {
				e.printStackTrace();
			}
			ResultSet rs=null;
			try {
				rs=st.executeQuery(query);
				while(rs.next())
				{
					this.idCobrancaPIX=rs.getInt("id_cobranca_pix");			
					this.txid=rs.getString("txid");
					this.location=rs.getString("location");
					this.pixCopiaECola=rs.getString("pix_copia_e_cola");
					this.status=new StatusCobrancaPIX(connMaria, rs.getInt("status_cobranca_pix_id_status_cobranca_pix"));
				}
			} catch (SQLException e) {
				e.printStackTrace();
			}
			if(this.idCobrancaPIX!=0)
			{
				System.out.println("Payment registered already location: "+this.location);
				System.out.println("PIXCopiaECola: "+this.pixCopiaECola);
				System.out.println("txid: "+this.txid);
				System.out.println("status: "+this.status.getStatus());
				exists= true;
			}
			else
			{
				System.out.println("CobrancaPix not registered yet!");
			}

		}
		System.out.println("-- Checking end!");
		return exists;
	}

	
	public boolean register(Connection connMaria)
	{
		if(this.codigoUnico.length()>0 && this.criacao!=null)
		{
			String query="select * from BMA.cobranca_pix"
					+ " where "
					+ " empresa_id_empresa="+this.empresa.getIdEmpresa()
					+ " and cedente_id_cedente="+this.cedente.getIdCedente()
					+ " and tipo_instrucao_id_tipo_instrucao="+this.tipoInstrucao.getIdTipoInstrucao()
					+ " and codigo_unico= '"+this.codigoUnico+"'"
					+ " and data_pix='"+sdf.format(this.dataPix)+"'"
					+ " and valor="+this.valor;
			Statement st=null;
			try {
				st=connMaria.createStatement();
			} catch (SQLException e) {
				e.printStackTrace();
			}
			ResultSet rs=null;
			try {
				rs=st.executeQuery(query);
				while(rs.next())
				{
					this.idCobrancaPIX=rs.getInt("id_cobranca_pix");					
				}
			} catch (SQLException e) {
				e.printStackTrace();
			}
			if(this.idCobrancaPIX!=0)
			{
				System.out.println("Payment registered already location: "+this.location);
			}
			else
			{
				String insert="insert into BMA.cobranca_pix"
						+ " (empresa_id_empresa"
						+ ",cedente_id_cedente"
						+ ",tipo_instrucao_id_tipo_instrucao"
						+ ",codigo_unico"
						+ ",data_pix"
						+ ",txid"
						+",status_cobranca_pix_id_status_cobranca_pix"
						+ ",location"
						+ ",revisao"
						+ ",criacao"
						+ ",valor"
						+ ",chave"
						+ ",pix_copia_e_cola"
						+ " )"
						+ " values("
						+ this.empresa.getIdEmpresa()
						+ ","+this.cedente.getIdCedente()
						+ ","+this.tipoInstrucao.getIdTipoInstrucao()
						+ ",'"+this.codigoUnico+"'"
						+ ",'"+sdf.format(this.dataPix)+"'"
						+ ",'"+this.txid+"'"
						+ ","+this.status.getIdStatusCobrancaPIX()
						+ ",'"+this.location+"'"
						+ ","+this.revisao
						+ ",'"+sdfp.format(this.criacao)+"'"
						+ ","+this.valor
						+ ",'"+this.chave+"'"
						+ ",'"+this.pixCopiaECola+"'"
						+ ")";
				try {
					st.executeUpdate(insert);
					Utils.waitv(0.25);
					try 
					{
						rs=st.executeQuery(query);
						while(rs.next())
						{
							this.idCobrancaPIX=rs.getInt("id_cobranca_pix");					
						}
					} catch (SQLException e) {
						e.printStackTrace();
					}

				} catch (SQLException e) {
					e.printStackTrace();
				}
				
			}
		}
		return true;
	}
	
	public void updateStatus(Connection connMaria)
	{
		String update="update BMA.cobranca_pix set status_cobranca_pix_id_status_cobranca_pix="+this.status.getIdStatusCobrancaPIX()+ " where id_cobranca_pix="+this.idCobrancaPIX;
		Statement st=null;
		try {
			st=connMaria.createStatement();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		try {
			st.executeUpdate(update);
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
	public void updatePago(Connection connMaria, boolean pago)
	{
		this.setPago(pago);
		int pagoInt=0;
		if(pago)
		{
			pagoInt=1;
		}
		String update="update BMA.cobranca_pix set pago="+pagoInt+ " where id_cobranca_pix="+this.idCobrancaPIX;
		System.out.println(update);
		Statement st=null;
		try {
			st=connMaria.createStatement();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		try {
			st.executeUpdate(update);
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
	public static void updatePago(Connection connMaria, int idCobrancaPIX, boolean pago)
	{
		int pagoInt=0;
		if(pago)
		{
			pagoInt=1;
		}
		
		CobrancaPIX cobrancaPIX  = new CobrancaPIX();
		String update="update BMA.cobranca_pix set pago="+pagoInt+ " where id_cobranca_pix="+idCobrancaPIX;
		System.out.println(update);
		Statement st=null;
		try {
			st=connMaria.createStatement();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		try {
			st.executeUpdate(update);
		} catch (SQLException e) {
			e.printStackTrace();
		}
		Utils.waitv(4);
	}
	
	public static void updatePago(Connection connMaria, Connection connMSS,  int idCobrancaPIX, boolean pago)
	{
		int pagoInt=0;
		if(pago)
		{
			pagoInt=1;
		}		
		CobrancaPIX cobrancaPIX  = new CobrancaPIX(idCobrancaPIX, connMaria, connMSS);
		System.out.println("*** Empresa: "+ cobrancaPIX.getEmpresa().getApelido()+" Cedente: "+cobrancaPIX.getCedente().getApelido()+ " Valor: "+cobrancaPIX.getValor() + " Codigo: "+cobrancaPIX.getCodigoUnico()); 
		String update="update BMA.cobranca_pix set pago="+pagoInt+ " where id_cobranca_pix="+idCobrancaPIX;
		System.out.println(update);
		Statement st=null;
		try {
			st=connMaria.createStatement();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		try {
			st.executeUpdate(update);
		} catch (SQLException e) {
			e.printStackTrace();
		}
		Utils.waitv(4);
	}

	
	public String enviarCobrancaImediata(Authentication authentication, Empresa empresa, Connection connMaria, Connection connMSS)
	{
		
		if(empresa.getDadosBancariosEmpresa().getChavePIX().length()==0)
		{
			return "";
		}
		String responseString="";
		System.out.println("Formatted JSON to send:\n'"+this.jsonStringCobranca+"'");
		HttpURLConnection connection = null;
		try {
			/*
			 * Uncomment line below for production
			 */
//			URL url = new URL(urlSendPayment);
//			String URLString="https://qrpix-h.bradesco.com.br/v2/cob/"+this.txid;
			String URLString="https://qrpix.bradesco.com.br/v2/cob/"+this.txid;
			URL url = new URL(URLString);
			connection = (HttpURLConnection) url.openConnection();
			connection.addRequestProperty("Content-Type", "application/json");
			connection.setRequestMethod("PUT");
//			connection.setRequestProperty("clientID", authentication.getClientId());
//			connection.setRequestProperty("x-itau-correlationid", "1");
//			connection.setRequestProperty("client_secret", authentication.getClientSecret());
//			connection.setRequestProperty("access_token", authentication.getAccessToken().getAccessToken());
			connection.setRequestProperty("Authorization", "Bearer "+authentication.getAccessToken().getAccessToken());
			connection.setDoOutput(true);

			// Add certificatehttp://marketplace.eclipse.org/marketplace-client-intro?mpc_install=1139
			File p12 = authentication.getCertificateP12();
			String p12password = authentication.getPasswdP12();

			InputStream keyInput = new FileInputStream(p12);

			KeyStore keyStore = KeyStore.getInstance("PKCS12");
			keyStore.load(keyInput, p12password.toCharArray());
			keyInput.close();

			KeyManagerFactory keyManagerFactory = KeyManagerFactory.getInstance("SunX509");
			keyManagerFactory.init(keyStore, p12password.toCharArray());

			SSLContext context = SSLContext.getInstance("TLS");
			context.init(keyManagerFactory.getKeyManagers(), null, new SecureRandom());

			SSLSocketFactory socketFactory = context.getSocketFactory();
			if (connection instanceof HttpsURLConnection)
				((HttpsURLConnection) connection).setSSLSocketFactory(socketFactory);
			//
			
			String body = this.jsonStringCobranca;

			OutputStream outputStream = connection.getOutputStream();
			outputStream.write(body.toString().getBytes());
			outputStream.close();
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (connection != null)
				connection.disconnect();
		}
		
		BufferedReader bufferedReader = null;
		try {
			bufferedReader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
		} catch (IOException e) {
			e.printStackTrace();
		}

		StringBuilder response = new StringBuilder();
		String line = null;
		try {
			while ((line = bufferedReader.readLine()) != null) {
				response.append(line);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}

		try {
			bufferedReader.close();
		} catch (IOException e) {
			e.printStackTrace();
		}

		System.out.println(response.toString());
		responseString=response.toString();
		processResponseCobranca(connMaria, connMSS, responseString);
		return responseString;
	}
	
	public static String consultaListaPIX(Authentication authentication, Date dataInicial)
	{
		Calendar cal = Calendar.getInstance();
		cal.setTime(dataInicial);
		cal.add(Calendar.DAY_OF_MONTH, -10);
		dataInicial=cal.getTime();
		Date dataFinal=Calendar.getInstance().getTime();
		String responseString="";
		HttpURLConnection connection = null;
		try {
			/*
			 * Uncomment line below for production
			 */
//			String requestString="https://qrpix-h.bradesco.com.br/v2/cob";
			String requestString="https://qrpix.bradesco.com.br/v2/pix";
			requestString+="?inicio="+sdf.format(dataInicial)+"T00:00:00.000Z";
			requestString+="&fim="+sdf.format(dataFinal)+"T"+sdft.format(dataFinal)+"Z";
//			requestString+="?"	+ "agencia_operacao="+agenciaOperacao
//									+"&"+ "conta_operacao="+contaOperacao
//									+"&"+ "cnpj_empresa="+cnpjEmpresa
//									+"&"+ "tipo_lista="+tipoLista
//									+"&"+ "data_inicial="+sdf.format(dataInicial)
//									+"&"+ "data_final="+sdf.format(dataFinal)
//									;
			
			System.out.println("RequestString: '"+requestString+"'");
			URL url = new URL(requestString);
			connection = (HttpURLConnection) url.openConnection();
			connection.addRequestProperty("Content-Type", "application/json");
			connection.setRequestMethod("GET");
//			connection.setRequestProperty("x-itau-apikey", authentication.getClientId());
//			connection.setRequestProperty("x-correlationID", "1");
//			connection.setRequestProperty("x-simulation", "false");
			connection.setRequestProperty("Authorization", "Bearer "+authentication.getAccessToken().getAccessToken());
			connection.setDoOutput(true);

			// Add certificate
			File p12 = authentication.getCertificateP12();
			String p12password = authentication.getPasswdP12();

			InputStream keyInput = new FileInputStream(p12);

			KeyStore keyStore = KeyStore.getInstance("PKCS12");
			keyStore.load(keyInput, p12password.toCharArray());
			keyInput.close();

			KeyManagerFactory keyManagerFactory = KeyManagerFactory.getInstance("SunX509");
			keyManagerFactory.init(keyStore, p12password.toCharArray());

			SSLContext context = SSLContext.getInstance("TLS");
			context.init(keyManagerFactory.getKeyManagers(), null, new SecureRandom());

			SSLSocketFactory socketFactory = context.getSocketFactory();
			if (connection instanceof HttpsURLConnection)
				((HttpsURLConnection) connection).setSSLSocketFactory(socketFactory);
			//
			
//			OutputStream outputStream = connection.getOutputStream();
//			outputStream.close();

			BufferedReader bufferedReader = 
		            new BufferedReader(new InputStreamReader(connection.getInputStream()));

			StringBuilder response = new StringBuilder();
			String line = null;
			while ((line = bufferedReader.readLine()) != null) {
				response.append(line);
			}

			bufferedReader.close();

			System.out.println(response.toString());
			responseString=response.toString();
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (connection != null)
				connection.disconnect();
		}		
		return responseString;
	}

	
	
	public static String consultaListaCobranca(Authentication authentication, Date dataInicial)
	{
		Calendar cal = Calendar.getInstance();
		cal.setTime(dataInicial);
		cal.add(Calendar.DAY_OF_MONTH, -10);
		dataInicial=cal.getTime();
		Date dataFinal=Calendar.getInstance().getTime();
		String responseString="";
		HttpURLConnection connection = null;
		try {
			/*
			 * Uncomment line below for production
			 */
//			String requestString="https://qrpix-h.bradesco.com.br/v2/cob";
			String requestString="https://qrpix.bradesco.com.br/v2/cob";
			requestString+="?inicio="+sdf.format(dataInicial)+"T00:00:00.000Z";
			requestString+="&fim="+sdf.format(dataFinal)+"T"+sdft.format(dataFinal)+"Z";
//			requestString+="?"	+ "agencia_operacao="+agenciaOperacao
//									+"&"+ "conta_operacao="+contaOperacao
//									+"&"+ "cnpj_empresa="+cnpjEmpresa
//									+"&"+ "tipo_lista="+tipoLista
//									+"&"+ "data_inicial="+sdf.format(dataInicial)
//									+"&"+ "data_final="+sdf.format(dataFinal)
//									;
			
			System.out.println("RequestString: '"+requestString+"'");
			URL url = new URL(requestString);
			connection = (HttpURLConnection) url.openConnection();
			connection.addRequestProperty("Content-Type", "application/json");
			connection.setRequestMethod("GET");
//			connection.setRequestProperty("x-itau-apikey", authentication.getClientId());
//			connection.setRequestProperty("x-correlationID", "1");
//			connection.setRequestProperty("x-simulation", "false");
			connection.setRequestProperty("Authorization", "Bearer "+authentication.getAccessToken().getAccessToken());
			connection.setDoOutput(true);

			// Add certificate
			File p12 = authentication.getCertificateP12();
			String p12password = authentication.getPasswdP12();

			InputStream keyInput = new FileInputStream(p12);

			KeyStore keyStore = KeyStore.getInstance("PKCS12");
			keyStore.load(keyInput, p12password.toCharArray());
			keyInput.close();

			KeyManagerFactory keyManagerFactory = KeyManagerFactory.getInstance("SunX509");
			keyManagerFactory.init(keyStore, p12password.toCharArray());

			SSLContext context = SSLContext.getInstance("TLS");
			context.init(keyManagerFactory.getKeyManagers(), null, new SecureRandom());

			SSLSocketFactory socketFactory = context.getSocketFactory();
			if (connection instanceof HttpsURLConnection)
				((HttpsURLConnection) connection).setSSLSocketFactory(socketFactory);
			//
			
//			OutputStream outputStream = connection.getOutputStream();
//			outputStream.close();

			BufferedReader bufferedReader = 
		            new BufferedReader(new InputStreamReader(connection.getInputStream()));

			StringBuilder response = new StringBuilder();
			String line = null;
			while ((line = bufferedReader.readLine()) != null) {
				response.append(line);
			}

			bufferedReader.close();

			System.out.println(response.toString());
			responseString=response.toString();
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (connection != null)
				connection.disconnect();
		}		
		return responseString;
	}
		
	
	public String consulta(Authentication authentication, Connection connMaria, Connection connMSS)
	{
		String responseString="";
		HttpURLConnection connection = null;
		try {
			/*
			 * Uncomment line below for production
			 */
//			String requestString="https://qrpix-h.bradesco.com.br/v2/cob/";
			String requestString="https://qrpix.bradesco.com.br/v2/cob/";
			requestString+=this.txid;
//			requestString+="?"	+ "agencia_operacao="+agenciaOperacao
//									+"&"+ "conta_operacao="+contaOperacao
//									+"&"+ "cnpj_empresa="+cnpjEmpresa
//									+"&"+ "tipo_lista="+tipoLista
//									+"&"+ "data_inicial="+sdf.format(dataInicial)
//									+"&"+ "data_final="+sdf.format(dataFinal)
//									;
			
			System.out.println("RequestString: '"+requestString+"'");
			URL url = new URL(requestString);
			connection = (HttpURLConnection) url.openConnection();
			connection.addRequestProperty("Content-Type", "application/json");
			connection.setRequestMethod("GET");
//			connection.setRequestProperty("x-itau-apikey", authentication.getClientId());
//			connection.setRequestProperty("x-correlationID", "1");
//			connection.setRequestProperty("x-simulation", "false");
			connection.setRequestProperty("Authorization", "Bearer "+authentication.getAccessToken().getAccessToken());
			connection.setDoOutput(true);

			// Add certificate
			File p12 = authentication.getCertificateP12();
			String p12password = authentication.getPasswdP12();

			InputStream keyInput = new FileInputStream(p12);

			KeyStore keyStore = KeyStore.getInstance("PKCS12");
			keyStore.load(keyInput, p12password.toCharArray());
			keyInput.close();

			KeyManagerFactory keyManagerFactory = KeyManagerFactory.getInstance("SunX509");
			keyManagerFactory.init(keyStore, p12password.toCharArray());

			SSLContext context = SSLContext.getInstance("TLS");
			context.init(keyManagerFactory.getKeyManagers(), null, new SecureRandom());

			SSLSocketFactory socketFactory = context.getSocketFactory();
			if (connection instanceof HttpsURLConnection)
				((HttpsURLConnection) connection).setSSLSocketFactory(socketFactory);
			//
			
//			OutputStream outputStream = connection.getOutputStream();
//			outputStream.close();

			BufferedReader bufferedReader = 
		            new BufferedReader(new InputStreamReader(connection.getInputStream()));

			StringBuilder response = new StringBuilder();
			String line = null;
			while ((line = bufferedReader.readLine()) != null) {
				response.append(line);
			}

			bufferedReader.close();

			System.out.println(response.toString());
			responseString=response.toString();
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (connection != null)
				connection.disconnect();
		}
		processResponseCobranca(connMaria, connMSS, responseString);
		return responseString;
	}
	
	public JsonObject getJsonPIXCobranca() {
		return jsonPIXCobranca;
	}

	public void setJsonPIXCobranca(JsonObject jsonPIXCobranca) {
		this.jsonPIXCobranca = jsonPIXCobranca;
	}

	public String getTxid() {
		return txid;
	}

	public void setTxid(String txid) {
		this.txid = txid;
	}

	public String getJsonStringCobranca() {
		return jsonStringCobranca;
	}

	public void setJsonStringCobranca(String jsonStringCobranca) {
		this.jsonStringCobranca = jsonStringCobranca;
	}

	public String getLocation() {
		return location;
	}

	public void setLocation(String location) {
		this.location = location;
	}

	public int getRevisao() {
		return revisao;
	}

	public void setRevisao(int revisao) {
		this.revisao = revisao;
	}

	public Date getDataPix() {
		return dataPix;
	}

	public void setDataPix(Date dataPix) {
		this.dataPix = dataPix;
	}

	public Date getCriacao() {
		return criacao;
	}

	public void setCriacao(Date criacao) {
		this.criacao = criacao;
	}

	public double getValor() {
		return valor;
	}

	public void setValor(double valor) {
		this.valor = valor;
	}

	public String getChave() {
		return chave;
	}

	public void setChave(String chave) {
		this.chave = chave;
	}

	public String getPixCopiaECola() {
		return pixCopiaECola;
	}

	public void setPixCopiaECola(String pixCopiaECola) {
		this.pixCopiaECola = pixCopiaECola;
	}

	public String getCodigoUnico() {
		return codigoUnico;
	}

	public void setCodigoUnico(String codigoUnico) {
		this.codigoUnico = codigoUnico;
	}

	public static SimpleDateFormat getSdfp() {
		return sdfp;
	}

	public static void setSdfp(SimpleDateFormat sdfp) {
		CobrancaPIX.sdfp = sdfp;
	}

	public Empresa getEmpresa() {
		return empresa;
	}

	public void setEmpresa(Empresa empresa) {
		this.empresa = empresa;
	}

	public Cedente getCedente() {
		return cedente;
	}

	public void setCedente(Cedente cedente) {
		this.cedente = cedente;
	}

	public int getIdCobrancaPIX() {
		return idCobrancaPIX;
	}

	public void setIdCobrancaPIX(int idCobrancaPIX) {
		this.idCobrancaPIX = idCobrancaPIX;
	}

	public TipoInstrucao getTipoInstrucao() {
		return tipoInstrucao;
	}

	public void setTipoInstrucao(TipoInstrucao tipoInstrucao) {
		this.tipoInstrucao = tipoInstrucao;
	}

	public static SimpleDateFormat getSdf() {
		return sdf;
	}

	public static void setSdf(SimpleDateFormat sdf) {
		CobrancaPIX.sdf = sdf;
	}

	public StatusCobrancaPIX getStatus() {
		return status;
	}

	public void setStatus(StatusCobrancaPIX status) {
		this.status = status;
	}

	public static SimpleDateFormat getSdft() {
		return sdft;
	}

	public static void setSdft(SimpleDateFormat sdft) {
		CobrancaPIX.sdft = sdft;
	}

	public ArrayList<PIX> getPixes() {
		return pixes;
	}

	public void setPixes(ArrayList<PIX> pixes) {
		this.pixes = pixes;
	}

	public boolean isPago() {
		return pago;
	}

	public void setPago(boolean pago) {
		this.pago = pago;
	}
}
