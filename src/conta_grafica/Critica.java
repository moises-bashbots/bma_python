package conta_grafica;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Objects;

import cedente.Cedente;
import empresa.Empresa;
import utils.Utils;

public class Critica {
	private int idCritica=0;
	private Date dataCritica=null;
	private Empresa empresa=new Empresa();
	private Cedente cedente = new Cedente();
	private TipoCritica tipoCritica = new TipoCritica();
    private String identificacaoDuplicata="";
    private String nomeEmpresa="";
    private String nomeCedente="";
    private String apelidoCritica="";
    private boolean registrado=false;
    public static HashMap<String, String> mapNomeCritica = new HashMap<String, String>();
    public static HashMap<String, String> mapNomeCriticaDescricao = new HashMap<String,String>();
    private static SimpleDateFormat sdf=new SimpleDateFormat("yyyy-MM-dd");


    public Critica() {
    	if(mapNomeCritica.size()==0)
    	{
    		populateMapNomeCritica();
    	}
    }

    public Critica(String idDuplicata, String nomeEmpresa, String nomeCedente, String nomeCritica)
    {
    	if(mapNomeCritica.size()==0)
    	{
    		populateMapNomeCritica();
    	}
        this.identificacaoDuplicata = idDuplicata;
        this.nomeEmpresa = nomeEmpresa;
        this.nomeCedente = nomeCedente;
        this.apelidoCritica = nomeCritica;
        this.show();
    }
    
    public Critica(Connection connMaria, Connection connMSS, int idCritica)
    {
    	
//        // Salvando critica
//        Critica critica = new Critica(connMaria,connMSS,
//            idTitulo,
//            nomeCedente,
//            nomeEmpresa,
//            "ABATIMENTO"
//        );
    	if(mapNomeCritica.size()==0)
    	{
    		populateMapNomeCritica();
    	}
    	this.idCritica=idCritica;
        String query="select * from BMA.critica where "
        						+ " id_critica="+this.idCritica;;
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
				
				this.empresa=new Empresa(connMaria, connMSS, rs.getInt("empresa_id_empresa"));
				this.cedente=new Cedente(connMaria, connMSS, rs.getInt("cedente_id_cedente"), this.empresa);
				this.tipoCritica=new TipoCritica(connMaria, rs.getInt("tipo_critica_id_tipo_critica"));
				this.identificacaoDuplicata=rs.getString("identificacao_duplicata");
				this.dataCritica=rs.getDate("data_critica");				
				int registradoInt=rs.getInt("registrado");
				if(registradoInt==1)
				{
					this.registrado=true;
				}
				this.nomeEmpresa=this.empresa.getApelido();
				this.nomeCedente=this.cedente.getApelido();
				this.apelidoCritica=this.tipoCritica.getApelido();
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
        this.show();
    }
      
    public Critica(Connection connMaria, Connection connMSS, String idDuplicata, String nomeCedente, String nomeEmpresa, String apelidoCritica)
    {
    	
//        // Salvando critica
//        Critica critica = new Critica(connMaria,connMSS,
//            idTitulo,
//            nomeCedente,
//            nomeEmpresa,
//            "ABATIMENTO"
//        );
    	if(mapNomeCritica.size()==0)
    	{
    		populateMapNomeCritica();
    	}
        this.identificacaoDuplicata = idDuplicata;
        this.nomeEmpresa = nomeEmpresa;
        this.nomeCedente = nomeCedente;
        this.apelidoCritica = apelidoCritica;
        this.empresa=new Empresa(connMaria, connMSS, this.nomeEmpresa);
        this.cedente=new Cedente(connMaria, connMSS, this.empresa, this.nomeCedente);
        this.tipoCritica=new TipoCritica(connMaria, this.apelidoCritica, mapNomeCritica.get(this.apelidoCritica), mapNomeCriticaDescricao.get(this.apelidoCritica));
        this.dataCritica=Calendar.getInstance().getTime();
        
        String query="select * from BMA.critica where "
        						+ "data_critica='"+sdf.format(this.dataCritica)+"'"
   								+ " and empresa_id_empresa="+this.empresa.getIdEmpresa()
   								+ " and cedente_id_cedente="+this.cedente.getIdCedente()
   								+ " and tipo_critica_id_tipo_critica="+this.tipoCritica.getIdTipoCritica()
        						+ " and identificacao_duplicata='"+this.identificacaoDuplicata+"'";
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
				this.idCritica=rs.getInt("id_critica");
				int registradoInt=rs.getInt("registrado");
				if(registradoInt==1)
				{
					this.registrado=true;
				}
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
        
        if(this.idCritica==0)
        {
        	String insert="insert into BMA.critica (data_critica, empresa_id_empresa, cedente_id_cedente, tipo_critica_id_tipo_critica, identificacao_duplicata)"
        							+ " values("
        							+ "'"+sdf.format(this.dataCritica)+"'"
        							+ ","+this.empresa.getIdEmpresa()
        							+ ","+this.cedente.getIdCedente()
        							+ ","+this.tipoCritica.getIdTipoCritica()
        							+ ",'"+this.identificacaoDuplicata+"'"
        							+ ")";
        	System.out.println(insert);
        	try {
				st.executeUpdate(insert);
			} catch (SQLException e) {
				e.printStackTrace();
			}
        	Utils.waitv(0.25);
        	 try {
     			rs=st.executeQuery(query);
     			while(rs.next())
     			{
     				this.idCritica=rs.getInt("id_critica");
     				int registradoInt=rs.getInt("registrado");
     				if(registradoInt==1)
     				{
     					this.registrado=true;
     				}
     			}
     		} catch (SQLException e) {
     			e.printStackTrace();
     		}
        }
        this.show();
    }
    
    public void updateRegistrado(Connection connMaria)
    {
    	Statement st=null;
        try {
			st=connMaria.createStatement();
		} catch (SQLException e) {
			e.printStackTrace();
		}
        int registradoInt=0;
        if(this.isRegistrado())
        {
        	registradoInt=1;
        }
        String update="update BMA.critica set registrado="+registradoInt
        							+ " where id_critica="+this.idCritica;
        try {
			st.executeUpdate(update);
		} catch (SQLException e) {
			e.printStackTrace();
		}
    }
    
    public static ArrayList<Critica> criticasForRegister(Connection connMaria, Connection connMSS)
    {
    	ArrayList<Critica> criticas=new ArrayList<>();
    	Statement st=null;
        try {
			st=connMaria.createStatement();
		} catch (SQLException e) {
			e.printStackTrace();
		}
        
        String query="select * from BMA.critica where registrado=0"
        						+ " and data_critica='"+sdf.format(Calendar.getInstance().getTime())+"'";
        
        System.out.println(query);
        
        ResultSet rs=null;
        
        try {
			rs=st.executeQuery(query);
			while(rs.next())
			{
				Critica critica=new Critica(connMaria, connMSS, rs.getInt("id_critica"));
				criticas.add(critica);
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
    	return criticas;
    }
    
    public static void populateMapNomeCritica()
    {
        mapNomeCritica.put("PRORROGACAO", "101 - COB - PRORROGAÇÃO");
        mapNomeCritica.put("ABATIMENTO", "122 - CHEC - ABATIMENTO");
        mapNomeCritica.put("BAIXA - ROBO MANDOU PIX", "018 - COB - OUTROS");
        mapNomeCritica.put("QUITACAO EFETUADA VIA PIX INFORMADA PELO ROBO",  "018 - COB - OUTROS");
        mapNomeCritica.put("INSTRUCAO PROTESTO EFETUADA", "021 - COB - CARTÓRIO");
        mapNomeCritica.put("INSTRUCAO DE BAIXA EFETUADA POIS O CEDENTE NAO FEZ O PIX",  "018 - COB - OUTROS");

        mapNomeCriticaDescricao.put("PRORROGACAO", "Prorrogação efetuada - bot");
        mapNomeCriticaDescricao.put("ABATIMENTO", "Abatimento efetuado - bot");
        mapNomeCriticaDescricao.put("BAIXA - ROBO MANDOU PIX", "Informado valor para pagamento - bot");
        mapNomeCriticaDescricao.put("QUITACAO EFETUADA VIA PIX INFORMADA PELO ROBO",  "Quitação efetuada - bot");
        mapNomeCriticaDescricao.put("INSTRUCAO PROTESTO EFETUADA", "Enviado a cartório - bot");
        mapNomeCriticaDescricao.put("INSTRUCAO DE BAIXA EFETUADA POIS O CEDENTE NAO FEZ O PIX",  "Instruçãoo de baixa excluíd por ausência de pagamento - bot");

            // ,"35 - Aguardando Instrumento de Protesto");
            // ,"122 - CHEC - ABATIMENTO");
            // ,"23 - CHEC - CANCELOU PEDIDO");
            // ,"37 - CHEC - CCB");
            // ,"09 - CHEC - COMISS��RIA");
            // ,"22 - CHEC - CONFIRMADO");
            // ,"26 - CHEC - CONTATO SOMENTE POR EMAIL");
            // ,"31 - CHEC - DESCONHECIMENTO DA OPERA����O");
            // ,"24 - CHEC - DEVOLU����O DE MERCADORIA");
            // ,"13 - CHEC - ESCROW");
            // ,"116 - CHEC - FOMENTO");
            // ,"111 - CHEC - INTERCOMPANY");
            // ,"134 - CHEC - LOCKDOWN");
            // ,"129 - CHEC - N��O ATENDE");
            // ,"100 - CHEC - N��O CONFIRMA");
            // ,"016 - CHEC - N��O RECEBEU MERCADORIA");
            // ,"125 - CHEC - NF CANCELADA");
            // ,"106 - CHEC - OUTROS");
            // ,"112 - CHEC - PEDIDO CONFIRMADO");
            // ,"107 - CHEC - PRORROGA����O");
            // ,"110 - CHEC - SEFAZ");
            // ,"127 - CHEC - TELEFONE CORTADO");
            // ,"128 - CHEC - TELEFONE N��O ATENDE");
            // ,"104 - CHEC - TRANSPORTADORA");
            // ,"117 - COB - ABATIMENTO");
            // ,"013 - COB - A����O DE EXECU����O");
            // ,"008 - COB - ACORDO");
            // ,"001 - COB - AVISO DE CHEQUE");
            // ,"002 - COB - AVISO DE DUPLICATA");
            // ,"021 - COB - CART��RIO");
            // ,"108 - COB - CART��RIO SUSTADO");
            // ,"120 - COB - CART��RIO VIA IEPTB");
            // ,"023 - COB - CEDENTE VAI RECOMPRAR");
            // ,"114 - COB - CHEQUES POSTADOS");
            // ,"011 - COB - COBRAN��A FEITA AO SACADO");
            // ,"133 - COB - COMISS��RIA");
            // ,"017 - COB - CONTATO REALIZADO, AGUARDANDO RETORNO");
            // ,"105 - COB - CONTATO SOMENTE POR EMAIL");
            // ,"019 - COB - CR��DITO CONTA CORRENTE");
            // ,"099 - COB - CR��DITO DO CHEQUE NA C/C");
            // ,"025 - COB - DEP��SITO DO SACADO");
            // ,"103 - COB - DEVOLU����O DE MERCADORIA");
            // ,"015 - COB - EMITIDA ANU��NCIA");
            // ,"014 - COB - ENVIADO CARTA DE COBRANCA");
            // ,"012 - COB - ENVIADO PARA JURIDICO");
            // ,"245 - COB - INSTRUMENTO DE PROTESTO ENVIADO AO CEDE");
            // ,"006 - COB - N��O PAGA TERCEIROS");
            // ,"007 - COB - N��O PASSA INFORMA����O");
            // ,"028 - COB - N��O PROTESTA");
            // ,"010 - COB - NAO REAPRESENTAR");
            // ,"130 - COB - N��O RECEBEU MERCADORIA");
            // ,"102 - COB - NF CANCELADA");
            // ,"018 - COB - OUTROS");
            // ,"027 - COB - PEDIDO DE SUSTA����O");
            // ,"009 - COB - PREVIS��O DE PAGAMENTO");
            // ,"101 - COB - PRORROGA����O");
            // ,"024 - COB - PROTESTADO");
            // ,"026 - COB - REAPRESENTAR");
            // ,"004 - COB - RECADO");
            // ,"132 - COB - RECOMPRA CEDENTE");
            // ,"118 - COB - REEMBOLSADO FIDC");
            // ,"119 - COB - SACADO PAGOU PARA CEDENTE");
            // ,"135 - COB - SACADO PG BOLETO DE COB SIMPLES DO CEDE");
            // ,"029 - COB - SACADO VAI PAGAR");
            // ,"115 - COB - SEM RETORNO DO SACADO");
            // ,"020 - COB - SERASA");
            // ,"250 - COB - SUSTADO JUDICIALMENTE");
            // ,"005 - COB - TELEFONE CORTADO");
            // ,"003 - COB - TELEFONE N��O ATENDE");
            // ,"136 - COB PRORROGA����O LOCKDOWN");
            // ,"121 - CR��DITO QI TECH");
            // ,"255 - Custas de Cart��rio Cobradas no C/C do Cliente");
            // ,"098 - Hist��ricos de T��tulo Migrados");
            // ,"240 - Recebido Instrumento de Protesto");
            // ,"210 - Recebimento do Cart��rio");
            // ,"230 - T��tulo Retirado de Cart��rio");

    }
    
    public String getNomeEmpresa() {
        return this.nomeEmpresa;
    }

    public void setNomeEmpresa(String nomeEmpresa) {
        this.nomeEmpresa = nomeEmpresa;
    }

    public String getNomeCedente() {
        return this.nomeCedente;
    }

    public void setNomeCedente(String nomeCedente) {
        this.nomeCedente = nomeCedente;
    }

    public String getNomeCritica() {
        return this.apelidoCritica;
    }

    public void setNomeCritica(String nomeCritica) {
        this.apelidoCritica = nomeCritica;
    }

    public Critica nomeEmpresa(String nomeEmpresa) {
        setNomeEmpresa(nomeEmpresa);
        return this;
    }

    public Critica nomeCedente(String nomeCedente) {
        setNomeCedente(nomeCedente);
        return this;
    }

    public Critica nomeCritica(String nomeCritica) {
        setNomeCritica(nomeCritica);
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (o == this)
            return true;
        if (!(o instanceof Critica)) {
            return false;
        }
        Critica critica = (Critica) o;
        return Objects.equals(identificacaoDuplicata, critica.identificacaoDuplicata) && Objects.equals(nomeEmpresa, critica.nomeEmpresa) && Objects.equals(nomeCedente, critica.nomeCedente) && Objects.equals(apelidoCritica, critica.apelidoCritica);
    }

    @Override
    public int hashCode() {
        return Objects.hash(identificacaoDuplicata, nomeEmpresa, nomeCedente, apelidoCritica);
    }

    @Override
    public String toString() {
        return "{" +
            " idDuplicata='" + getIdentificacaoDuplicata() + "'" +
            ", nomeEmpresa='" + getNomeEmpresa() + "'" +
            ", nomeCedente='" + getNomeCedente() + "'" +
            ", nomeCritica='" + getNomeCritica() + "'" +
            "}";
    }




    void assertCriticaObjectectIsProperlyPopulated()
    {}
    
    public void show()
    {
    	
//    	 "PRORROGACAO", "187364/4", "BMA FIDC", "ENOVELAR"
    	System.out.println("Critica:");
    	System.out.println("idCritica: "+this.idCritica);
    	System.out.println("identificacaoDuplicata: "+this.identificacaoDuplicata);
    	System.out.println("nomeEmpresa: "+this.nomeEmpresa);
    	System.out.println("nomeCedente: "+this.nomeCedente);
    	System.out.println("nomeCritica: "+this.apelidoCritica);
    	System.out.println("(\""+this.apelidoCritica+"\",\""+this.identificacaoDuplicata+"\",\""+this.nomeEmpresa+"\",\""+this.nomeCedente+"\")");
    }


    public void addCriticaToBeExecuted()
    {
        assertCriticaObjectectIsProperlyPopulated();

        System.out.println("Adicionando crítica à fila de execuções de críticas");
        System.out.println("Nome da crítica a ser gravada: " + this.apelidoCritica);
        System.out.println("Id Duplicata: " +  this.identificacaoDuplicata);
        System.out.println("Nome empresa " + this.nomeEmpresa);
        System.out.println("Nome cedente " + this.nomeCedente);

        // TODO MYSQL Command
    }

    static void addCriticaToBeExecuted(String idDuplicata, String nomeEmpresa, String nomeCedente, String nomeCritica)
    {
        System.out.println("Adicionando crítica à fila de execuções de críticas");
        System.out.println("Nome da crítica a ser gravada: " + nomeCritica);
        System.out.println("Id Duplicata: " +  idDuplicata);
        System.out.println("Nome empresa " + nomeEmpresa);
        System.out.println("Nome cedente " + nomeCedente);

        // TODO MYSQL Command

    }

	public int getIdCritica() {
		return idCritica;
	}

	public void setIdCritica(int idCritica) {
		this.idCritica = idCritica;
	}

	public String getIdentificacaoDuplicata() {
		return identificacaoDuplicata;
	}

	public void setIdentificacaoDuplicata(String identificacaoDuplicata) {
		this.identificacaoDuplicata = identificacaoDuplicata;
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

	public TipoCritica getTipoCritica() {
		return tipoCritica;
	}

	public void setTipoCritica(TipoCritica tipoCritica) {
		this.tipoCritica = tipoCritica;
	}

	public Date getDataCritica() {
		return dataCritica;
	}

	public void setDataCritica(Date dataCritica) {
		this.dataCritica = dataCritica;
	}

	public boolean isRegistrado() {
		return registrado;
	}

	public void setRegistrado(boolean registrado) {
		this.registrado = registrado;
	}

	public static HashMap<String, String> getMapNomeCritica() {
		return mapNomeCritica;
	}

	public static void setMapNomeCritica(HashMap<String, String> mapNomeCritica) {
		Critica.mapNomeCritica = mapNomeCritica;
	}

	public static HashMap<String, String> getMapNomeCriticaDescricao() {
		return mapNomeCriticaDescricao;
	}

	public static void setMapNomeCriticaDescricao(HashMap<String, String> mapNomeCriticaDescricao) {
		Critica.mapNomeCriticaDescricao = mapNomeCriticaDescricao;
	}

	public static SimpleDateFormat getSdf() {
		return sdf;
	}

	public static void setSdf(SimpleDateFormat sdf) {
		Critica.sdf = sdf;
	}
    
}
