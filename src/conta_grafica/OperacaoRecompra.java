package conta_grafica;

import java.io.File;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;

import bradesco.CobrancaPIX;
import cedente.Cedente;
import empresa.Empresa;
import instrucao.TipoInstrucao;
import mysql.ConnectorMariaDB;
import utils.Utils;

public class OperacaoRecompra {
	private int idOperacaoRecompra=0;
	private Date dataRecompra = null;
	private Empresa empresa = new Empresa();
	private Cedente cedente = new Cedente();
	private double valor=0;
	private ArrayList<TituloRecompra> titulosRecompra = new ArrayList<TituloRecompra>();
	private HashMap<String, TituloRecompra> identificacaoTitulos = new HashMap<>();
	private Date inicioVencimento=null;
	private Date finalVencimento=null;
	private File demostrativoPDF=null;
	private String codigoUnico="";
	private boolean pago=false;
	private boolean baixado=false;
	private boolean baixadoBradesco=false;
	
	private static SimpleDateFormat sdf=new SimpleDateFormat("yyyy-MM-dd");
	private static SimpleDateFormat sdfn=new SimpleDateFormat("yyyyMMdd");
	private static SimpleDateFormat sdfH=new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	private static  DecimalFormat df = new DecimalFormat("#0.00#");
	
	public OperacaoRecompra()
	{
		
	}
	

	public OperacaoRecompra(Connection connMaria, Empresa empresa, Cedente cedente, boolean temp, int zero)
	{
		this.empresa=empresa;
		this.cedente=cedente;
		this.dataRecompra=Calendar.getInstance().getTime();
		
		String query="select * from BMA.operacao_recompra"
				+ " where"
				+ " data_recompra='"+sdf.format(this.dataRecompra)+"'"
				+ " and empresa_id_empresa="+this.empresa.getIdEmpresa()
				+ " and cedente_id_cedente="+this.cedente.getIdCedente()
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
				this.idOperacaoRecompra=rs.getInt("id_operacao_recompra");
				int baixadoInt=rs.getInt("baixado");
				if(baixadoInt>0)
				{
					this.baixado=true;
				}
				String pathDemostrativo = rs.getString("path_file_demostrativo");
				if(pathDemostrativo!=null)
				{
					File filePDF=new File(pathDemostrativo);
					if(filePDF.exists())
					{
						this.demostrativoPDF=filePDF;
					}
				}
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		
		
		this.buildCodigoUnico(connMaria);
	}

	
	public OperacaoRecompra(Connection connMaria, Empresa empresa, Cedente cedente)
	{
		this.empresa=empresa;
		this.cedente=cedente;
		this.dataRecompra=Calendar.getInstance().getTime();
		
		String query="select * from BMA.operacao_recompra"
				+ " where"
				+ " data_recompra='"+sdf.format(this.dataRecompra)+"'"
				+ " and empresa_id_empresa="+this.empresa.getIdEmpresa()
				+ " and cedente_id_cedente="+this.cedente.getIdCedente()
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
				this.idOperacaoRecompra=rs.getInt("id_operacao_recompra");
				int baixadoInt=rs.getInt("baixado");
				if(baixadoInt>0)
				{
					this.baixado=true;
				}

				String pathDemostrativo = rs.getString("path_file_demostrativo");
				if(pathDemostrativo!=null)
				{
					File filePDF=new File(pathDemostrativo);
					if(filePDF.exists())
					{
						this.demostrativoPDF=filePDF;
					}
				}
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		
		if(this.idOperacaoRecompra==0)
		{
			String insert="insert into BMA.operacao_recompra"
									+ " (data_recompra, empresa_id_empresa, cedente_id_cedente)"
									+ " values("
									+ "'"+sdf.format(this.dataRecompra)+"'"
									+ ","+this.empresa.getIdEmpresa()
									+ ","+this.cedente.getIdCedente()
									+ ")";
			System.out.println(insert);
			try {
				st.executeUpdate(insert);
				Utils.waitv(0.25);
				try {
					rs=st.executeQuery(query);
					while(rs.next())
					{
						this.valor=rs.getDouble("valor");
						this.idOperacaoRecompra=rs.getInt("id_operacao_recompra");
					}
				} catch (SQLException e) {
					e.printStackTrace();
				}
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
		this.buildCodigoUnico(connMaria);
	}
	
	public OperacaoRecompra(Connection connMaria, Empresa empresa, Cedente cedente, boolean additional)
	{
		this.empresa=empresa;
		this.cedente=cedente;
		this.dataRecompra=Calendar.getInstance().getTime();
		
		String query="select * from BMA.operacao_recompra"
				+ " where"
				+ " data_recompra='"+sdf.format(this.dataRecompra)+"'"
				+ " and empresa_id_empresa="+this.empresa.getIdEmpresa()
				+ " and cedente_id_cedente="+this.cedente.getIdCedente();
		if(additional)
		{
				query += " order by id_operacao_recompra desc limit 1";
		}
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
				this.idOperacaoRecompra=rs.getInt("id_operacao_recompra");
				int baixadoInt=rs.getInt("baixado");
				if(baixadoInt>0)
				{
					this.baixado=true;
				}

				String pathDemostrativo = rs.getString("path_file_demostrativo");
				if(pathDemostrativo!=null)
				{
					File filePDF=new File(pathDemostrativo);
					if(filePDF.exists())
					{
						this.demostrativoPDF=filePDF;
					}
				}
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		
		if(this.idOperacaoRecompra==0 || additional)
		{
			String insert="insert into BMA.operacao_recompra"
									+ " (data_recompra, empresa_id_empresa, cedente_id_cedente)"
									+ " values("
									+ "'"+sdf.format(this.dataRecompra)+"'"
									+ ","+this.empresa.getIdEmpresa()
									+ ","+this.cedente.getIdCedente()
									+ ")";
			System.out.println(insert);
			try {
				st.executeUpdate(insert);
				Utils.waitv(0.25);
				try {
					rs=st.executeQuery(query);
					while(rs.next())
					{
						this.valor=rs.getDouble("valor");
						this.idOperacaoRecompra=rs.getInt("id_operacao_recompra");
					}
				} catch (SQLException e) {
					e.printStackTrace();
				}
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
		this.buildCodigoUnico(connMaria);
	}
	
	
	public OperacaoRecompra(Connection connMaria, Connection connMSS, int idOperacaoRecompra)
	{
		this.idOperacaoRecompra=idOperacaoRecompra;
		String query="select * from BMA.operacao_recompra"
				+ " where"
				+ " id_operacao_recompra="+this.idOperacaoRecompra;
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
				this.dataRecompra=rs.getDate("data_recompra");
				this.empresa=new Empresa(connMaria, connMSS, rs.getInt("empresa_id_empresa"));
				this.cedente=new Cedente(connMaria, connMSS, rs.getInt("cedente_id_cedente"), this.empresa);
				this.valor=rs.getDouble("valor");
				this.titulosRecompra=TituloRecompra.titulosRecompra(connMaria, connMSS, this.empresa, this.cedente, this.idOperacaoRecompra);
				int baixadoBradescoInt=rs.getInt("baixado_bradesco");
				int baixadoInt=rs.getInt("baixado");
				int pagoInt=rs.getInt("pago");
				if(baixadoInt>0)
				{
					this.baixado=true;
				}
				if(baixadoBradescoInt>0)
				{
					this.baixadoBradesco=true;
				}
				if(pagoInt>0)
				{
					this.pago=true;
				}

				String pathDemostrativo = rs.getString("path_file_demostrativo");
				if(pathDemostrativo!=null)
				{
					File filePDF=new File(pathDemostrativo);
					if(filePDF.exists())
					{
						this.demostrativoPDF=filePDF;
					}
				}
				this.setupVencimentos();
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		for(TituloRecompra tituloRecompra:this.titulosRecompra)
		{
			identificacaoTitulos.put(tituloRecompra.getIdentificacaoTitulo(), tituloRecompra);
		}
		this.buildCodigoUnico(connMaria);
	}
	
	public void register(Connection connMaria)
	{
		
		String query="select * from BMA.operacao_recompra"
				+ " where"
				+ " data_recompra='"+sdf.format(this.dataRecompra)+"'"
				+ " and empresa_id_empresa="+this.empresa.getIdEmpresa()
				+ " and cedente_id_cedente="+this.cedente.getIdCedente();
		
		Statement st=null;
		try {
			st=connMaria.createStatement();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		
		ResultSet rs=null;
		if(this.idOperacaoRecompra==0)
		{
			String insert="insert into BMA.operacao_recompra"
									+ " (data_recompra, empresa_id_empresa, cedente_id_cedente)"
									+ " values("
									+ "'"+sdf.format(this.dataRecompra)+"'"
									+ ","+this.empresa.getIdEmpresa()
									+ ","+this.cedente.getIdCedente()
									+ ")";
			System.out.println(insert);
			try {
				st.executeUpdate(insert);
				Utils.waitv(0.25);
				try {
					rs=st.executeQuery(query);
					while(rs.next())
					{
						this.valor=rs.getDouble("valor");
						this.idOperacaoRecompra=rs.getInt("id_operacao_recompra");
					}
				} catch (SQLException e) {
					e.printStackTrace();
				}
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
	}
	
	public void buildCodigoUnico(Connection connMaria)
	{
		TipoInstrucao tipoInstrucao = new TipoInstrucao(connMaria, "RECOMPRA");
		this.codigoUnico = Utils.uniqueStringPIX(
				35
				,this.getDataRecompra()
				, tipoInstrucao.getTipoInstrucao()
				, this.getCedente().getParticipante().getCadastro()
				, this.getEmpresa().getDadosBancariosEmpresa().getAgencia()
				, this.getEmpresa().getDadosBancariosEmpresa().getConta()+this.getEmpresa().getDadosBancariosEmpresa().getDigitoConta()
				, this.getEmpresa().getCnpj()
				, Double.toString(Math.abs(this.getValor()))
				);		
	}
	
	
	
	public static ArrayList<OperacaoRecompra> operacoesRecompraHoje(Connection connMaria, Connection connMSS)
	{
		ArrayList<OperacaoRecompra> operacoesRecompra = new ArrayList<>();
		String query="select * from BMA.operacao_recompra"
				+ " where"
				+ " data_recompra="+"'"+sdf.format(Calendar.getInstance().getTime())+"'";
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
				OperacaoRecompra operacaoRecompra = new OperacaoRecompra(connMaria, connMSS, rs.getInt("id_operacao_recompra"));
				TipoInstrucao tipoInstrucao = new TipoInstrucao(connMaria, "RECOMPRA");
		    	CobrancaPIX cobrancaPIX = new CobrancaPIX(operacaoRecompra,tipoInstrucao,connMaria);
		    	if(cobrancaPIX.isPago())
		    	{
		    		operacaoRecompra.setPago(true);
		    	}
		    	operacoesRecompra.add(operacaoRecompra);
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		
		return operacoesRecompra;
	}
	
	public static ArrayList<OperacaoRecompra> operacoesRecompraPendentes(Connection connMaria, Connection connMSS, Empresa empresa)
	{
		ArrayList<OperacaoRecompra> operacoesRecompra = new ArrayList<>();
		Calendar cal = Calendar.getInstance();
		cal.add(Calendar.DAY_OF_MONTH, -1);
		String query="select * from BMA.operacao_recompra"
				+ " where"
				+ " horario_baixa is not null "
				+ " and baixado_bradesco=0"
				+ " and empresa_id_empresa="+empresa.getIdEmpresa();
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
				OperacaoRecompra operacaoRecompra = new OperacaoRecompra(connMaria, connMSS, rs.getInt("id_operacao_recompra"));
				TipoInstrucao tipoInstrucao = new TipoInstrucao(connMaria, "RECOMPRA");
		    	CobrancaPIX cobrancaPIX = new CobrancaPIX(operacaoRecompra,tipoInstrucao,connMaria);
		    	if(cobrancaPIX.isPago())
		    	{
		    		operacaoRecompra.setPago(true);
		    	}
		    	operacoesRecompra.add(operacaoRecompra);
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		
		return operacoesRecompra;
	}
	
	public static ArrayList<OperacaoRecompra> operacoesRecompraPagas(Connection connMaria, Connection connMSS)
	{
		Calendar calendar=Calendar.getInstance();
		calendar.add(Calendar.DAY_OF_MONTH, -60);
		ArrayList<OperacaoRecompra> operacoesRecompra = new ArrayList<>();
		String query="select * from BMA.operacao_recompra"
				+ " where"
				+ " pago=0" 
				+ " and data_recompra >= " +"'" + sdf.format(calendar.getTime())+"'";
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
				System.out.println(" -------------------------------------------------------------------------------------------");
				OperacaoRecompra operacaoRecompra = new OperacaoRecompra(connMaria, connMSS, rs.getInt("id_operacao_recompra"));
				operacaoRecompra.show();
				TipoInstrucao tipoInstrucao = new TipoInstrucao(connMaria, "RECOMPRA");
		    	CobrancaPIX cobrancaPIX = new CobrancaPIX(operacaoRecompra,tipoInstrucao,connMaria);
		    	cobrancaPIX.show();
		    	if(cobrancaPIX.isPago())
		    	{
		    		operacaoRecompra.setPago(true);
		    		operacoesRecompra.add(operacaoRecompra);
		    	}		    	
		    	System.out.println(" -------------------------------------------------------------------------------------------");
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		
		return operacoesRecompra;
	}
	
	public static ArrayList<OperacaoRecompra> operacoesRecompraPagasNaoBaixadas(Connection connMaria, Connection connMSS)
	{
		Calendar calendar=Calendar.getInstance();
		calendar.add(Calendar.DAY_OF_MONTH, -3);
		ArrayList<OperacaoRecompra> operacoesRecompraPagasNaoBaixadas = new ArrayList<>();
		String query="select * from BMA.operacao_recompra"
				+ " where"
				+ " baixado=0"
				+ " and valor > 0"
				+ " and data_recompra >= " +"'" + sdf.format(calendar.getTime())+"'"
				+ " order by id_operacao_recompra desc";
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
				System.out.println("++++++++#################### -------------------------------------------------------------------------------------------");				
				OperacaoRecompra operacaoRecompra = new OperacaoRecompra(connMaria, connMSS, rs.getInt("id_operacao_recompra"));
				operacaoRecompra.showShort();
				if(!operacaoRecompra.isPago())
				{
					TipoInstrucao tipoInstrucao = new TipoInstrucao(connMaria, "RECOMPRA");
			    	CobrancaPIX cobrancaPIX = new CobrancaPIX(operacaoRecompra,tipoInstrucao,connMaria);
			    	cobrancaPIX.show();
			    	if(cobrancaPIX.isPago())
			    	{
			    		operacaoRecompra.setPago(true);
			    		if(!operacaoRecompra.isBaixado())
			    		{
			    			System.out.println("************************************************************");
			    			System.out.println("Adicionando para baixa");
			    			operacoesRecompraPagasNaoBaixadas.add(operacaoRecompra);
			    			operacaoRecompra.getEmpresa().getDadosBancariosEmpresa().show();
			    		}
			    		else
			    		{
			    			operacaoRecompra.updatePago(connMaria);
			    		}
			    			
			    	}		    						
				}
				else
				{
	    			System.out.println("************************************************************");
	    			System.out.println("Adicionando para baixa  -- Else");				
					operacoesRecompraPagasNaoBaixadas.add(operacaoRecompra);
					operacaoRecompra.getEmpresa().getDadosBancariosEmpresa().show();
				}
				System.out.println("++++++++#################### -------------------------------------------------------------------------------------------");
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		
		return operacoesRecompraPagasNaoBaixadas;
	}


	
	public void setupVencimentos()
	{
		ArrayList<Integer> numerosVencimentoMinimo = new ArrayList<>();
		ArrayList<Integer> numerosVencimentoMaximo = new ArrayList<>();
		int numeroMinimo=0;
		int numeroMaximo=0;
		int numeroVencimentoMinimo=0;
		int numeroVencimentoMaximo=0;
		int iTitulo=0;
		System.out.println("  ++ Setup dates for vencimento for "+ this.titulosRecompra.size() + " titles");
		for(TituloRecompra tituloRecompra:this.titulosRecompra)
		{
			Calendar calMin = Calendar.getInstance();
			calMin.setTime(tituloRecompra.getVencimento());
			Calendar calMax = Calendar.getInstance();
			calMax.setTime(tituloRecompra.getVencimento());
			
			calMin.add(Calendar.DAY_OF_MONTH, -5);
			calMax.add(Calendar.DAY_OF_MONTH, 5);
			numeroVencimentoMinimo=Integer.parseInt(sdfn.format(calMin.getTime()));
			numeroVencimentoMaximo=Integer.parseInt(sdfn.format(calMax.getTime()));
			numerosVencimentoMinimo.add(numeroVencimentoMinimo);
			numerosVencimentoMaximo.add(numeroVencimentoMaximo);
			if(iTitulo==0)
			{
				numeroMinimo=numeroVencimentoMinimo;
				numeroMaximo=numeroVencimentoMaximo;
			}
			else
			{
				if(numeroVencimentoMinimo< numeroMinimo)
				{
					numeroMinimo=numeroVencimentoMinimo;
				}
				if(numeroVencimentoMaximo>numeroMaximo)
				{
					numeroMaximo=numeroVencimentoMaximo;
				}
			}
			iTitulo++;
		}
		
		if(numeroMinimo !=0 && numeroMaximo !=0)
		{
			try {
				this.inicioVencimento=sdfn.parse(Integer.toString(numeroMinimo));
			} catch (ParseException e) {
				e.printStackTrace();
			}
			try {
				this.finalVencimento=sdfn.parse(Integer.toString(numeroMaximo));
			} catch (ParseException e) {
				e.printStackTrace();
			}
		}
	}
	
	public static ArrayList<OperacaoRecompra> loadOperacoesRecompra(Connection connMaria, Connection connMSS)
	{
		System.out.println("***********************************");
		System.out.println("LOADING EXISTING OPERACOES RECOMPRA");;
		System.out.println("***********************************");
		ArrayList<OperacaoRecompra> operacoesRecompra = new ArrayList<>();
		
		String query="select * from BMA.operacao_recompra"
				+ " where"
				+ " data_recompra='"+sdf.format(Calendar.getInstance().getTime())+"'";
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
				OperacaoRecompra operacaoRecompra = new OperacaoRecompra(connMaria, connMSS, rs.getInt("id_operacao_recompra"));
				if(operacaoRecompra.getTitulosRecompra().size()>0)
				{
					operacoesRecompra.add(operacaoRecompra);
				}
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return operacoesRecompra;
	}
	
	public void updateValue(Connection connMaria)
	{
		this.valor=0;
		for(TituloRecompra tituloRecompra:this.titulosRecompra)
		{
			this.valor+=tituloRecompra.getValorRecompra();
		}
		Statement st=null;
		try {
			st=connMaria.createStatement();
		} catch (SQLException e) {
			e.printStackTrace();
		}		
		String update="update BMA.operacao_recompra set valor="+this.valor+""
								+ " where id_operacao_recompra="+this.idOperacaoRecompra;
		System.out.println(update);
		try {
			st.executeUpdate(update);
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
	public void updatePago(Connection connMaria)
	{
		Statement st=null;
		try {
			st=connMaria.createStatement();
		} catch (SQLException e) {
			e.printStackTrace();
		}		
		int pago=0;
		if(this.isPago()) {
			pago=1;
		}
		for(TituloRecompra tituloRecompra:this.titulosRecompra)
		{
			tituloRecompra.setEfetuado(true);
			tituloRecompra.updateEfetuado(connMaria);
		}
		String update="update BMA.operacao_recompra set pago="+pago+""
								+ " where id_operacao_recompra="+this.idOperacaoRecompra;
		System.out.println(update);
		try {
			st.executeUpdate(update);
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
	public void updateBaixadoBradesco(Connection connMaria)
	{
		Statement st=null;
		try {
			st=connMaria.createStatement();
		} catch (SQLException e) {
			e.printStackTrace();
		}		
		int baixadoBradescoInt=0;
		if(this.isBaixadoBradesco()) {
			baixadoBradescoInt=1;
		}
		String update="update BMA.operacao_recompra"
				+ " set baixado_bradesco="+baixadoBradescoInt+""
						+ ",horario_baixa_bradesco="+"'"+sdfH.format(Calendar.getInstance().getTime())+"'"
								+ " where id_operacao_recompra="+this.idOperacaoRecompra;
		System.out.println(update);
		try {
			st.executeUpdate(update);
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
	public void updateBaixado(Connection connMaria)
	{
		Statement st=null;
		try {
			st=connMaria.createStatement();
		} catch (SQLException e) {
			e.printStackTrace();
		}		
		int baixadoInt=0;
		if(this.isBaixado()) {
			baixadoInt=1;
		}
		String update="update BMA.operacao_recompra set baixado="+baixadoInt+""
								+ " where id_operacao_recompra="+this.idOperacaoRecompra;
		System.out.println(update);
		try {
			st.executeUpdate(update);
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
	public void updateHorarioBaixadoFromRGB(Connection connMaria)
	{
		this.setBaixado(true);
		Statement st=null;
		try {
			st=connMaria.createStatement();
		} catch (SQLException e) {
			e.printStackTrace();
		}		
		int baixadoInt=0;
		if(this.isBaixado()) {
			baixadoInt=1;
		}
		
		String update="update BMA.operacao_recompra"
								+ " set baixado="+baixadoInt+""
								+ ", horario_baixa="+"'"+sdfH.format(Calendar.getInstance().getTime())+"'"
								+ " where id_operacao_recompra="+this.idOperacaoRecompra;
		System.out.println(update);
		try {
			st.executeUpdate(update);
		} catch (SQLException e) {
			e.printStackTrace();
		}
		
		for(TituloRecompra tituloRecompra: titulosRecompra)
		{
			tituloRecompra.updateBaixadoFromRGB(ConnectorMariaDB.conn);
		}
	}
	
	public void updateHorarioBaixadoAfterCompletion(Connection connMaria)
	{
		this.setBaixado(true);
		Statement st=null;
		try {
			st=connMaria.createStatement();
		} catch (SQLException e) {
			e.printStackTrace();
		}		
		int baixadoInt=0;
		if(this.isBaixado()) {
			baixadoInt=1;
		}
		
		String update="update BMA.operacao_recompra"
								+ " set baixado="+baixadoInt+""
								+ ", horario_baixa="+"'"+sdfH.format(Calendar.getInstance().getTime())+"'"
								+ " where id_operacao_recompra="+this.idOperacaoRecompra;
		System.out.println(update);
		try {
			st.executeUpdate(update);
		} catch (SQLException e) {
			e.printStackTrace();
		}
		
		for(TituloRecompra tituloRecompra: titulosRecompra)
		{
			tituloRecompra.updateBaixadoAfterCompletion(ConnectorMariaDB.conn);
		}
	}
	
	public void updatePath(Connection connMaria)
	{
		Statement st=null;
		try {
			st=connMaria.createStatement();
		} catch (SQLException e) {
			e.printStackTrace();
		}		
		String update="update BMA.operacao_recompra set path_file_demostrativo='"+this.demostrativoPDF.getAbsolutePath()+"'"
								+ " where id_operacao_recompra="+this.idOperacaoRecompra;
		System.out.println(update);
		try {
			st.executeUpdate(update);
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
	public void show()
	{
		this.setupVencimentos();
		System.out.println(" +++++++++++++++++++++++++++++++++++++++++++"); 
		System.out.println(" +++++ --- Operacao Recompra ---");
		System.out.println("Empresa: "+this.empresa.getRazaoSocial());
		System.out.println("Cedente: "+ this.cedente.getParticipante().getRazaoSocial());
		System.out.println("DataRecompra: "+ sdf.format(this.dataRecompra));
		System.out.println("ValorTotal: "+this.valor);
		System.out.println("Pago: "+this.pago);
		System.out.println("Baixado: "+this.baixado);
		System.out.println("Titulos: ");
		for(TituloRecompra tituloRecompra:this.titulosRecompra)
		{
			tituloRecompra.show();
		}
		if(this.inicioVencimento!=null && this.finalVencimento!=null)
		{
			System.out.println("Menor Vencimento: "+sdf.format(this.inicioVencimento));
			System.out.println("Maior Vencimento: "+sdf.format(this.finalVencimento));
		}
	}

	public void showShort()
	{
		this.setupVencimentos();
		System.out.println(" +++++ --- Operacao Recompra ---");
		System.out.println("Empresa: "+this.empresa.getRazaoSocial());
		System.out.println("Cedente: "+ this.cedente.getParticipante().getRazaoSocial());
		System.out.println("DataRecompra: "+ sdf.format(this.dataRecompra));
		System.out.println("ValorTotal: "+this.valor);
		System.out.println("Titulos: ");
		for(TituloRecompra tituloRecompra:this.titulosRecompra)
		{
			tituloRecompra.showShort();
		}
		if(this.inicioVencimento!=null && this.finalVencimento!=null)
		{
			System.out.println("Menor Vencimento: "+sdf.format(this.inicioVencimento));
			System.out.println("Maior Vencimento: "+sdf.format(this.finalVencimento));
		}
	}

	
	public String toHTML()
	{
		String htmlCode="";		
//		htmlCode+="<p>";
//		htmlCode+="<h2>RECOMPRA</h2>"
//				+ "<b>Cedente: </b>"+this.getCedente().getParticipante().getRazaoSocial() + " <b>Empresa: </b>"+this.getEmpresa().getRazaoSocial();
//		htmlCode+="</p>";
		htmlCode+="<table>";
		htmlCode+="<tr>"
								+ "<th>Instru&ccedil;&atilde;o</th>"
								+ "<th>Vencimento</th>"
								+ "<th>T&iacute;tulo</th>"
								+ "<th>Valor Original</th>"
								+ "<th>Empresa</th>"
								+ "<th>Cedente</th>"
								+ "<th>Valor Corrigido</th>"
								+ "<th>Juros + tarifa</th>"
								+ "<th>Total a pagar</th>"
							+ "</tr>";
		for(TituloRecompra tituloRecompra: this.titulosRecompra)
		{
			htmlCode+=tituloRecompra.toHTML();
		}	
		htmlCode+="</table>";
		htmlCode+="<p>"
							+ "Total Recompra: <b>"+ " R$ "+df.format(this.getValor())+"</b>"
							+ "</p>";
		htmlCode+="<p><b>Empresa "+this.empresa.getApelido().toUpperCase()+"</b><br>"
							+ "<br>Dados banc&aacute;rios:<br>"
							+this.empresa.getRazaoSocial().toUpperCase()+"<br>"
							+ "CNPJ: "+Utils.formatarCNPJ(this.empresa.getCnpj())+"<br>"
							+ this.empresa.getDadosBancariosEmpresa().getBanco().getNomeBanco().toUpperCase()+"<br>"
							+ "C&Oacute;DIGO DE COMPENSA&Ccedil;&Atilde;O: "+this.empresa.getDadosBancariosEmpresa().getBanco().getCodigoCompe()+"<br>"
							+ "AG&Ecirc;NCIA: "+this.empresa.getDadosBancariosEmpresa().getAgencia()+"-"+this.empresa.getDadosBancariosEmpresa().getDigitoAgencia()+"<br>"
							+ "CONTA: "+this.empresa.getDadosBancariosEmpresa().getConta()+"-"+this.empresa.getDadosBancariosEmpresa().getDigitoConta()+"<br>"
							+ "PIX: "+Utils.formatarCNPJ(this.empresa.getCnpj())
							+"</p>";		
		return htmlCode;
	}
	
	public int getIdOperacaoRecompra() {
		return idOperacaoRecompra;
	}
	public void setIdOperacaoRecompra(int idOperacaoRecompra) {
		this.idOperacaoRecompra = idOperacaoRecompra;
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
	public double getValor() {
		return valor;
	}
	public void setValor(double valor) {
		this.valor = valor;
	}

	public ArrayList<TituloRecompra> getTitulosRecompra() {
		return titulosRecompra;
	}

	public void setTitulosRecompra(ArrayList<TituloRecompra> titulosRecompra) {
		this.titulosRecompra = titulosRecompra;
		this.setupVencimentos();
	}

	public static SimpleDateFormat getSdf() {
		return sdf;
	}

	public static void setSdf(SimpleDateFormat sdf) {
		OperacaoRecompra.sdf = sdf;
	}

	public Date getDataRecompra() {
		return dataRecompra;
	}

	public void setDataRecompra(Date dataRecompra) {
		this.dataRecompra = dataRecompra;
	}

	public Date getInicioVencimento() {
		return inicioVencimento;
	}

	public void setInicioVencimento(Date inicioVencimento) {
		this.inicioVencimento = inicioVencimento;
	}

	public Date getFinalVencimento() {
		return finalVencimento;
	}

	public void setFinalVencimento(Date finalVencimento) {
		this.finalVencimento = finalVencimento;
	}

	public File getDemostrativoPDF() {
		return demostrativoPDF;
	}

	public void setDemostrativoPDF(File demostrativoPDF) {
		this.demostrativoPDF = demostrativoPDF;
	}

	public static SimpleDateFormat getSdfn() {
		return sdfn;
	}

	public static void setSdfn(SimpleDateFormat sdfn) {
		OperacaoRecompra.sdfn = sdfn;
	}

	public boolean isPago() {
		return this.pago;
	}

	public void setPago(boolean pago) {
		this.pago = pago;
	}

	public static DecimalFormat getDf() {
		return df;
	}

	public static void setDf(DecimalFormat df) {
		OperacaoRecompra.df = df;
	}


	public String getCodigoUnico() {
		return this.codigoUnico;
	}

	public void setCodigoUnico(String codigoUnico) {
		this.codigoUnico = codigoUnico;
	}

	public HashMap<String, TituloRecompra> getIdentificacaoTitulos() {
		return this.identificacaoTitulos;
	}

	public void setIdentificacaoTitulos(HashMap<String, TituloRecompra> identificacaoTitulos) {
		this.identificacaoTitulos = identificacaoTitulos;
	}


	public boolean isBaixado() {
		return this.baixado;
	}


	public void setBaixado(boolean baixado) {
		this.baixado = baixado;
	}


	public boolean isBaixadoBradesco() {
		return this.baixadoBradesco;
	}


	public void setBaixadoBradesco(boolean baixadoBradesco) {
		this.baixadoBradesco = baixadoBradesco;
	}


	public static SimpleDateFormat getSdfH() {
		return sdfH;
	}


	public static void setSdfH(SimpleDateFormat sdfH) {
		OperacaoRecompra.sdfH = sdfH;
	}
	
	
	

}
