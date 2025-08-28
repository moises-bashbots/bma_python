package consulta_ssw;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;

public class Evento {
	private int idEventoTransporte=0;
	private Date dataHora=null;
	private String chaveNota="";
	private String unidade="";
	private String tituloEvento="";
	private String descricaoEvento="";
	private boolean eventoCadastrado=false;
	private static SimpleDateFormat sdfmh = new SimpleDateFormat("yyyy-MM-dd HH:mm:SS");
	private static SimpleDateFormat sdfm = new SimpleDateFormat("yyyy-MM-dd");

	
	public Evento()
	{
		
	}
	
	public Evento(String chaveNFE, Date dataHora, String unidade, String tituloEvento, String descricaoEvento)
	{
		this.chaveNota=chaveNFE;
		this.dataHora=dataHora;
		this.unidade=unidade;
		this.tituloEvento=tituloEvento;
		this.descricaoEvento=descricaoEvento;
	}
	
	public Evento(Connection conn, int idEventoTransporte)
	{
		this.idEventoTransporte=idEventoTransporte;
		String query="select * from BMA.evento_transporte"
				+ " where id_evento_transporte="+this.idEventoTransporte;
//		System.out.println(query);
		Statement st=null;
		try {
		st=conn.createStatement();
		} catch (SQLException e) {
		e.printStackTrace();
		}
		
		ResultSet rs=null;
		try {
		rs=st.executeQuery(query);
		while(rs.next())
		{
			this.chaveNota=rs.getString("chave_nota");
			this.dataHora=rs.getTime("data_hora");
			this.unidade=rs.getString("unidade");
			this.tituloEvento=rs.getString("titulo_evento");
			this.descricaoEvento=rs.getString("descricao_evento");
			if(rs.getInt("evento_cadastrado")==1)
			{
				this.eventoCadastrado=true;
			}
		}
		} catch (SQLException e) {
		e.printStackTrace();
		}
	}
	
	public void show()
	{
		System.out.println("______________________________________________________");
		System.out.println("DataHora: "+sdfm.format(this.dataHora));
		System.out.println("Unidade: "+this.unidade);
		System.out.println("Titulo: "+this.tituloEvento);
		System.out.println("Descricao: "+this.descricaoEvento);
	}
	
	public static ArrayList<Evento> eventosSemCadastrar(Connection connMaria)
	{
		ArrayList<Evento> eventosSemCadastrar=new ArrayList<>();
		String query="select * from BMA.evento_transporte where evento_cadastrado=0";
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
				Evento evento = new Evento(connMaria, rs.getInt("id_evento_transporte"));
				eventosSemCadastrar.add(evento);
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return eventosSemCadastrar;
	}
	
	public static ArrayList<Evento> eventosNaoCadastradosPorChave(Connection connMaria, String chaveNota)
	{
		ArrayList<Evento>eventosCadastrados=new ArrayList<>();
		String query="select * from BMA.evento_transporte"
				              + " where chave_nota='"+chaveNota+"'"
				              		+ " and evento_cadastrado=0"
				              		+ " order by id_evento_transporte desc";
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
			int iEvento=0;
			while(rs.next())
			{							
//				System.out.println("id: "+rs.getInt("id_evento_transporte"));
				Evento evento = new Evento(connMaria, rs.getInt("id_evento_transporte"));
				System.out.println(iEvento+" "+evento.getIdEventoTransporte()+" "+evento.getTituloEvento()+ " "+evento.getChaveNota());
				eventosCadastrados.add(evento);
				iEvento++;
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return eventosCadastrados;
	}
	public static ArrayList<Evento> eventosPorChave(Connection connMaria, String chaveNota)
	{
		ArrayList<Evento>eventosCadastrados=new ArrayList<>();
		String query="select * from BMA.evento_transporte"
				              + " where chave_nota='"+chaveNota+"'"
				              		+ " order by id_evento_transporte";
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
			int iEvento=0;
			while(rs.next())
			{							
//				System.out.println("id: "+rs.getInt("id_evento_transporte"));
				Evento evento = new Evento(connMaria, rs.getInt("id_evento_transporte"));
				System.out.println(iEvento+" "+evento.getIdEventoTransporte()+" "+evento.getTituloEvento()+ " "+evento.getChaveNota());
				eventosCadastrados.add(evento);
				iEvento++;
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return eventosCadastrados;
	}
	
	public static HashMap<String,ArrayList<Evento>> eventosSemCadastrarPorChave(Connection connMaria)
	{
		HashMap<String,ArrayList<Evento>> eventosSemCadastrar=new HashMap<>();
		String query="select * from BMA.evento_transporte where evento_cadastrado=0";
//		System.out.println(query);
		Statement st=null;
		try {
			st=connMaria.createStatement();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		ResultSet rs=null;
		try {
			rs=st.executeQuery(query);
			int iEvento=0;
			while(rs.next())
			{							
//				System.out.println("id: "+rs.getInt("id_evento_transporte"));
				Evento evento = new Evento(connMaria, rs.getInt("id_evento_transporte"));
				System.out.println(iEvento+" "+evento.getIdEventoTransporte()+" "+evento.getTituloEvento()+ " "+evento.getChaveNota());
				if(eventosSemCadastrar.get(evento.getChaveNota())==null)
				{
					ArrayList<Evento> eventos = new ArrayList<>();
					eventos.add(evento);
					eventosSemCadastrar.put(evento.getChaveNota(), eventos);
				}
				else
				{
					eventosSemCadastrar.get(evento.getChaveNota()).add(evento);
				}
				iEvento++;
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return eventosSemCadastrar;
	}
	
	public void register(Connection conn)
	{
		String query="select * from BMA.evento_transporte"
								+ " where chave_nota="+"'"+this.chaveNota+"'"
								+ " and data_hora="+"'"+sdfmh.format(this.dataHora)+"'";
		System.out.println(query);
		Statement st=null;
		try {
			st=conn.createStatement();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		
		ResultSet rs=null;
		try {
			rs=st.executeQuery(query);
			while(rs.next())
			{
				this.idEventoTransporte=rs.getInt("id_evento_transporte");
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		if(this.idEventoTransporte==0)
		{
			String insert="insert into BMA.evento_transporte (chave_nota, data_hora, unidade, titulo_evento, descricao_evento,ultima_tentativa)"
										+ " values("
										+ "'"+this.chaveNota+"'"
										+ ","+"'"+sdfmh.format(this.dataHora)+"'"
										+ ","+"'"+this.unidade+"'"
										+ ","+"'"+this.tituloEvento+"'"
										+ ","+"'"+this.descricaoEvento+"'"
										+ ","+"'"+sdfm.format(Calendar.getInstance().getTime())+"'"
										+ ")";
			System.out.println(insert);
			try {
				st.executeUpdate(insert);
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
	}
	
	public void updateCadastrado(Connection conn)
	{
		String update="update BMA.evento_transporte set evento_cadastrado=1 where id_evento_transporte="+this.idEventoTransporte;
		System.out.println(update);
		Statement st=null;
		try {
			st=conn.createStatement();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		try {
			st.executeUpdate(update);
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
	public Date getDataHora() {
		return this.dataHora;
	}
	public void setDataHora(Date dataHora) {
		this.dataHora = dataHora;
	}
	public String getUnidade() {
		return this.unidade;
	}
	public void setUnidade(String unidade) {
		this.unidade = unidade;
	}
	public String getTituloEvento() {
		return this.tituloEvento;
	}
	public void setTituloEvento(String tituloEvento) {
		this.tituloEvento = tituloEvento;
	}
	public String getDescricaoEvento() {
		return this.descricaoEvento;
	}
	public void setDescricaoEvento(String descricaoEvento) {
		this.descricaoEvento = descricaoEvento;
	}

	public String getChaveNota() {
		return this.chaveNota;
	}

	public void setChaveNota(String chaveNota) {
		this.chaveNota = chaveNota;
	}

	public int getIdEventoTransporte() {
		return this.idEventoTransporte;
	}

	public void setIdEventoTransporte(int idEventoTransporte) {
		this.idEventoTransporte = idEventoTransporte;
	}

	public boolean isEventoCadastrado() {
		return this.eventoCadastrado;
	}

	public void setEventoCadastrado(boolean eventoCadastrado) {
		this.eventoCadastrado = eventoCadastrado;
	}

	public static SimpleDateFormat getSdfm() {
		return sdfm;
	}

	public static void setSdfm(SimpleDateFormat sdfm) {
		Evento.sdfm = sdfm;
	}

}
