package consulta_ssw;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;

import mssql.ConnectorMSSQL;
import mysql.ConnectorMariaDB;
import utils.Utils;

public class ConsultaSSW {
	
	private ArrayList<TituloParaChecagem> titulosParaChecagem = new ArrayList<>();
	private HashMap<String, HashMap<String, String>> chavesParaConsulta = new HashMap<>();
	private static DateTimeFormatter dtf = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");
	private static DateTimeFormatter dtfe = DateTimeFormatter.ofPattern("dd/MM/yy");

	private static SimpleDateFormat sdfe = new SimpleDateFormat("dd/MM/yyyy HH:mm:SS");
	private static SimpleDateFormat sdfm = new SimpleDateFormat("yyyy-MM-dd");
	private static SimpleDateFormat sdfs = new SimpleDateFormat("dd/MM/yy");

	private static WebDriver driver = null;
	
	public ConsultaSSW()
	{
		
	}

	public ConsultaSSW(ArrayList<TituloParaChecagem> titulosParaChecagem)
	{
		this.titulosParaChecagem=titulosParaChecagem;
	}

	
	public static void main(String[] args) 
	{
		File fileCheckedToday = new File("/home/robot/App/Log/consultaSSW.txt");
		FileWriter writer=null;
		HashMap<String, String> chavesConsultadasHoje = new HashMap<>();
		
		if(!fileCheckedToday.exists())
		{
			try {
				if (fileCheckedToday.createNewFile()){
				    System.out.println("File is created!");
				   }else{
				    System.out.println("File already exists.");
				   }
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		else {
		    System.out.println("File "+fileCheckedToday.getAbsolutePath()+ " already exists.");

			ArrayList<String> lines= Utils.readLinesInFile(fileCheckedToday.getAbsolutePath());
			for(String line:lines)
			{
				chavesConsultadasHoje.put(line.trim(), line.trim());
			}
		}

		if(fileCheckedToday.exists())
		{
			String stringToday=sdfm.format(Calendar.getInstance().getTime());
			String stringDateFile=sdfm.format(fileCheckedToday.lastModified());
			if(stringToday.contains(stringDateFile))
			{
				System.out.println("File with todays keys found!");
				try {
					writer=new FileWriter(fileCheckedToday);
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			else
			{
				System.out.println("File with todays keys not found anywhere!");
				try {
					writer=new FileWriter(fileCheckedToday,false);
					writer.flush();
					writer.close();
					writer=new FileWriter(fileCheckedToday);
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		ConnectorMSSQL.connect();
		ConnectorMariaDB.connect();
		HashMap<String,String> chavesConsultadas=TituloParaChecagem.chavesConsultadasFinalizadas(ConnectorMariaDB.conn);
		System.out.println("Chaves já consultadas e finalizadas: "+chavesConsultadas.size());
		System.out.println("Chaves já consultadas hoje sem registro: "+chavesConsultadasHoje.size());

		Utils.waitv(5);
		ConsultaSSW consultaSSW = new ConsultaSSW(TituloParaChecagem.titulosParaChecagem(ConnectorMSSQL.conn, 60));
		consultaSSW.chooseNFEKeys();
//		ConsultaSSW consultaSSW = new ConsultaSSW();	
//		consultaSSW.loadTestCase();		
//		consultaSSW.showNFEKeys();
		ConsultaSSW.driver=new ChromeDriver();
		consultaSSW.openSSW();
		int iChaveNFE=0;
		int iChaveEncontrada=0;
		int nChaves=0;
		
		for(String stringCnpjCedente:consultaSSW.getChavesParaConsulta().keySet())
		{
			for(String chaveNFE:consultaSSW.getChavesParaConsulta().get(stringCnpjCedente).keySet())
			{	
				nChaves++;
			}
		}

		System.out.println("- File exists "+fileCheckedToday.exists());
		
		for(String stringCnpjCedente:consultaSSW.getChavesParaConsulta().keySet())
		{
			for(String chaveNFE:consultaSSW.getChavesParaConsulta().get(stringCnpjCedente).keySet())
			{		
				System.out.println(iChaveNFE+"/"+nChaves);

				iChaveNFE++;
				if(chavesConsultadas.get(chaveNFE)==null && chavesConsultadasHoje.get(chaveNFE)==null) 
				{
					if(consultaSSW.consultar(chaveNFE).size()>0)
					{
						iChaveEncontrada++;
					}
				}
				else
				{
					System.out.println("Chave já consultada hoje "+chaveNFE);
				}
				if(iChaveNFE%500==0 && iChaveNFE>0)
				{
					try {
						writer.flush();
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
				try {
					writer.write(chaveNFE+"\n");
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		System.out.println("Chaves consultadas com sucessso: "+iChaveEncontrada);
		try {
			writer.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		ConsultaSSW.driver.close();
	}
	
	public void openSSW()
	{
		ConsultaSSW.driver.get("https://ssw.inf.br/2/rastreamento_danfe");
	}
	
	public ArrayList<Evento> consultar(String chaveNFE)
	{
		ArrayList<Evento> eventos = new ArrayList<>();
		boolean success=false;
		System.out.println("----------------------------------------------------------------------------");
		System.out.println("Consultando: "+chaveNFE);
		WebElement inputChaveNFE=ConsultaSSW.driver.findElement(By.id("danfe"));
		inputChaveNFE.sendKeys(chaveNFE);
		Utils.waitv(1.0);
		try {
			List<WebElement> errors = ConsultaSSW.driver.findElements(By.id("erro"));
			if(!errors.isEmpty())
			{
				if(errors.size()>0)
				{
					if(errors.get(0).getText().toLowerCase().contains("danfe não localizada"))
					{
						System.out.println("DANFE não localizada!");
						return eventos;
					}
				}
				else {
					success=true;				
				}
			}
			success=true;
			
		} catch (Exception e) {
			e.printStackTrace();
		}
		System.out.println("Success: "+success);
		ArrayList<String> tabs = new ArrayList<String> (ConsultaSSW.driver.getWindowHandles());
		int iTab=0;
		String tabRastreamentoDanfe="";
		String tabRastreamentoDetalhado="";
		for(String tab:tabs)
		{
			ConsultaSSW.driver.switchTo().window(tab);
//			System.out.println(iTab+ "th Tab: "+tab);
			String pageTitle=ConsultaSSW.driver.getTitle();
//			System.out.println("Page Title: " + pageTitle);
			if(pageTitle.toLowerCase().contains("rastreamento pelo danfe"))
			{
				tabRastreamentoDanfe=tab;
			}
			if(pageTitle.toLowerCase().contains("rastreamento detalhado"))
			{
				Utils.waitv(1.5);
				try {
					readEventos(chaveNFE);
				} catch (Exception e) {
					e.printStackTrace();
					ConsultaSSW.driver.close();
					try {
				        Object[] windowHandles=driver.getWindowHandles().toArray();
				        ConsultaSSW.driver.switchTo().window((String)windowHandles[0]);
						ConsultaSSW.driver.close();
					} catch (Exception e2) {
						e2.printStackTrace();
					}
					System.exit(1);
				}
				Utils.waitv(1);
				ConsultaSSW.driver.close();
				tabRastreamentoDetalhado=tab;				
				ConsultaSSW.driver.switchTo().window(tabRastreamentoDanfe);
			}
			iTab++;
		}
				
//		Utils.sendHumanKeys(inputChaveNFE, chaveNFE);
		
		return eventos;
	}
	
	public ArrayList<Evento> readEventos(String chaveNFE)
	{
		ArrayList<Evento> eventos = new ArrayList<>();
		Evento eventoPrevisao=new Evento();			

		List<WebElement> tablePrevisao = ConsultaSSW.driver.findElements(By.xpath("/html/body/div[3]/div[2]/div/table[1]/tbody"));
		if(tablePrevisao.isEmpty())
		{
			
		}
		else
		{
			List<WebElement> rowsPrevisao=tablePrevisao.get(0).findElements(By.tagName("tr"));
	
			for(WebElement row: rowsPrevisao)
			{
				if(row.getText().toLowerCase().startsWith("previ")) 
				{
					String[] fields=row.getText().split(":");
					if(fields[1].trim().length()==8)
					{
						LocalDateTime dataEvento=null;
						String dateToParse =fields[1].trim();
						System.out.println("DateToParse: "+dateToParse);
						if(dateToParse.length()==8)
						{
							LocalDate dateDataEvento = LocalDate.parse(dateToParse,dtfe);
							 dataEvento = dateDataEvento.atStartOfDay();
						}
						System.out.println(fields[1].trim());
						 eventoPrevisao=new Evento(chaveNFE, dataEvento, "Transportista", "PREVISAO DE ENTREGA", "PREVISAO DE ENTREGA");			
						 eventoPrevisao.show();
//						Utils.waitv("Correct here ", 200);
					}
					System.out.println(row.getText());
				}
			}
		}
		Utils.waitv(1);
		List<WebElement> tableEventos = ConsultaSSW.driver.findElements(By.xpath("/html/body/div[3]/div[2]/div/table[2]"));
		if(tableEventos.isEmpty())
		{
			
		}
		else 
		{
			List<WebElement> rows = tableEventos.get(0).findElements(By.tagName("tr"));
			int iEvento=0;
			for(WebElement row: rows)
			{
				if(row.getText().toLowerCase().contains("data/hora") 
						|| row.getText().toLowerCase().trim().length()==0
						)
				{
					continue;
				}
				if(row.getText().toLowerCase().trim().contains("fale conosco"))
				{
					break;
				}
				List<WebElement> cols = row.findElements(By.tagName("td"));
				int iCol=0;
				LocalDateTime dataEvento=null;			
				String unidade="";
				String tituloEvento="";
				String descricaoEvento="";
				for(WebElement col:cols)
				{
//					System.out.println(iCol+": "+col.getText());
					switch (iCol) {
					case 0:
						
							String toParse=col.getText().replaceAll("\n", " ").trim()+":00";
//							System.out.println("ToParseBefore: "+toParse);
							toParse=toParse.substring(0,6)+"20"+toParse.substring(6);
//							System.out.println("ToParseAfter: "+toParse);
							//dataEvento=sdfe.parse(toParse);
							
							System.out.println(toParse);
							dataEvento = LocalDateTime.parse(toParse,dtf);
							System.out.println("--");
							System.out.println("DataEvento: "+dataEvento);
					
						break;
					case 1:
						unidade=col.getText().replaceAll("\n", " ");
						System.out.println("Unidade: "+unidade);
						break;
					case 2:
						String content=col.getText();
						String[] fields=content.split("\n") ;
						if(fields.length>1)
						{
							tituloEvento=fields[0];
							descricaoEvento=fields[1];
						}
						System.out.println("TituloEvento: "+tituloEvento);
						System.out.println("DescricaoEvento: "+descricaoEvento);
						break;
					default:
						break;
					}
					iCol++;
				}
				if(tituloEvento.length()>0)
				{
					Evento evento=new Evento(chaveNFE, dataEvento, unidade, tituloEvento, descricaoEvento);			
		//			evento.register(ConnectorMariaDB.conn);
					eventos.add(evento);
					iEvento++;
				}
			}
		}
		
		if(eventos.size()>0)
		{
			int iEvento=0;
			boolean finalizado=false;
			for(Evento evento:eventos)
			{
				if(iEvento==0)
				{
					evento.register(ConnectorMariaDB.conn);
				}
				if(evento.getTituloEvento().toLowerCase().contains("desacordo")
						|| evento.getTituloEvento().toLowerCase().contains("avaria")
						|| evento.getTituloEvento().toLowerCase().contains("devolu")
						|| evento.getTituloEvento().toLowerCase().contains("devolvid")
						)
				{
					evento.register(ConnectorMariaDB.conn);
				}
				if(evento.getTituloEvento().toLowerCase().contains("mercadoria entregue"))
				{
					evento.register(ConnectorMariaDB.conn);
					finalizado=true;
					break;
				}
				iEvento++;
			}
			if(eventoPrevisao.getChaveNota().length()>0 && !finalizado)
			{
				eventoPrevisao.register(ConnectorMariaDB.conn);
			}

		}
		return eventos;
	}
	
	public void loadTestCase()
	{
		HashMap<String, String> chavesNFE=new HashMap<>();
		/*
		 CNPJ Cedente: 03939466000145
	   -- 35240603939466000145550000001158711076657468
	   -- 35240603939466000145550000001158781076532020
	   -- 35240603939466000145550000001158801076496188
	   -- 35240603939466000145550000001158761076567864
	   -- 35240603939466000145550020000080771790400269
	   -- 35240603939466000145550000001158621076818780
	   -- 35240603939466000145550000001158791076514100

		 */
//		chavesNFE.put("35240603939466000145550000001158711076657468", "35240603939466000145550000001158711076657468");
		chavesNFE.put("35240603939466000145550000001158781076532020", "35240603939466000145550000001158781076532020");
		chavesNFE.put("35240603939466000145550000001158801076496188", "35240603939466000145550000001158801076496188");
		chavesNFE.put("35240603939466000145550000001158761076567864", "35240603939466000145550000001158761076567864");
		this.chavesParaConsulta.put("03939466000145", chavesNFE);
	}
	
	public void chooseNFEKeys()
	{
		for(TituloParaChecagem tituloParaChecagem:titulosParaChecagem)
		{
			String cnpjCedente=tituloParaChecagem.getCnpjCedente();
			String chaveNFE=tituloParaChecagem.getChaveNFE();
			if(chavesParaConsulta.get(cnpjCedente)!=null)
			{
				if(chavesParaConsulta.get(cnpjCedente).get(chaveNFE)==null)
				{
					chavesParaConsulta.get(cnpjCedente).put(chaveNFE, chaveNFE);
				}
			}
			else {
				HashMap<String, String> mapChaves = new HashMap<>();
				mapChaves.put(chaveNFE, chaveNFE);
				chavesParaConsulta.put(cnpjCedente, mapChaves);
			}
		}
	}
	
	public void showNFEKeys()
	{
		int countNFE=0;
		for(String keyCnpjCedente:chavesParaConsulta.keySet())
		{
			System.out.println("CNPJ Cedente: "+keyCnpjCedente);
			for(String keyNFE:chavesParaConsulta.get(keyCnpjCedente).keySet())
			{
				System.out.println("   -- "+keyNFE);
				countNFE++;
			}			
		}
		System.out.println("Total of CNPJ Cedentes "+chavesParaConsulta.size());
		System.out.println("Total de chavesNFE: "+countNFE);
		
	}

	public ArrayList<TituloParaChecagem> getTitulosParaChecagem() {
		return this.titulosParaChecagem;
	}

	public void setTitulosParaChecagem(ArrayList<TituloParaChecagem> titulosParaChecagem) {
		this.titulosParaChecagem = titulosParaChecagem;
	}

	public HashMap<String, HashMap<String, String>> getChavesParaConsulta() {
		return this.chavesParaConsulta;
	}

	public void setChavesParaConsulta(HashMap<String, HashMap<String, String>> chavesParaConsulta) {
		this.chavesParaConsulta = chavesParaConsulta;
	}

	public static WebDriver getDriver() {
		return driver;
	}

	public static void setDriver(WebDriver driver) {
		ConsultaSSW.driver = driver;
	}

	public static SimpleDateFormat getSdfe() {
		return sdfe;
	}

	public static void setSdfe(SimpleDateFormat sdfe) {
		ConsultaSSW.sdfe = sdfe;
	}

	public static SimpleDateFormat getSdfm() {
		return sdfm;
	}

	public static void setSdfm(SimpleDateFormat sdfm) {
		ConsultaSSW.sdfm = sdfm;
	}

	public static SimpleDateFormat getSdfs() {
		return sdfs;
	}

	public static void setSdfs(SimpleDateFormat sdfs) {
		ConsultaSSW.sdfs = sdfs;
	}
	
}
