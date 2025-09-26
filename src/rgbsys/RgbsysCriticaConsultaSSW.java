package rgbsys;

import java.sql.Connection;
import java.text.SimpleDateFormat;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.support.ui.Select;

import consulta_ssw.Evento;
import consulta_ssw.SalvarCriticasConsultaSSW;
import consulta_ssw.TituloParaChecagem;
import utils.Utils;

public class RgbsysCriticaConsultaSSW {
	private String userName;
	private  String password;
	private String rootURL;
    public WebDriver driver;
    private JavascriptExecutor jsExecutor ;
	private static DateTimeFormatter dtfs = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

	private static SimpleDateFormat sdfr=new SimpleDateFormat("dd/MM/yyyy");
	private static SimpleDateFormat sdfm=new SimpleDateFormat("yyyy-MM-dd");
	private static SimpleDateFormat sdfmh=new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	
	public RgbsysCriticaConsultaSSW()
	{
//		super();
	}
	
	public RgbsysCriticaConsultaSSW(String userName, String password)
	{
//		super(userName, password);
		setUserName(userName);
        setPassword(password);
        // WebDriver driver = new FirefoxDriver();
//        WebDriverManager.chromedriver().setup();

        // Processo para habilitar popups. Necessario na simula����o de recompra e quitacao de baixa
        ChromeOptions options = new ChromeOptions();
        options.addArguments("test-type");
        options.addArguments("disable-popup-blocking");
        

        Map<String, Object> prefs = new HashMap<String, Object>(); 
        prefs.put("plugins.always_open_pdf_externally", true);
        options.setExperimentalOption("prefs", prefs);
        options.addArguments("--disable-extensions"); //to disable browser extension 
    	options.addArguments("disable-infobars");
		options.addArguments("ignore-certificate-errors");
		options.addArguments("--ignore-urlfetcher-cert-requests");
		options.addArguments("--remote-allow-origins=*");

//        DesiredCapabilities capabilities = DesiredCapabilities.chrome();
//        capabilities.setCapability(ChromeOptions.CAPABILITY, options);
        ///// 

//        WebDriver driver = new ChromeDriver(options);
		WebDriver driver = new FirefoxDriver();
        setDriver(driver);
        setJsExecutor((JavascriptExecutor) this.driver);
//        driver.manage().window().maximize();
        if(login())
        {
        	System.out.println("Successful login!");
        }
        else {
			System.out.println("Error to login!");
			this.driver.close();
			System.exit(1);
		}
	}
	
	  public void setJsExecutor(JavascriptExecutor jsExecutor) {
	        this.jsExecutor = jsExecutor;
	    }

	
	  public boolean  login()
	    {
	    	boolean success=true;
	    	try {
	        	System.out.println(RgbsysUser.rootURL+"/"+"Login.aspx");
	            this.driver.get(RgbsysUser.rootURL+"/"+"Login.aspx");
	            Utils.waitv(5);
	            this.driver.findElement(By.xpath("//*[@id='edusuario_I']")).sendKeys(getUserName());
	            Utils.waitv(3);
	            this.driver.findElement(By.xpath("//*[@id='edsenha_I']")).sendKeys(getPassword());
	            Utils.waitv(3);
	            this.driver.findElement(By.name("LoginButton")).click();
	            Utils.waitv(3);
	            List<WebElement> errorHorario=this.driver.findElements(By.xpath("//*[@id=\"lbmsg\"]"));
	            if(!errorHorario.isEmpty())
	            {
	            	if(errorHorario.get(0).isDisplayed())
	            	{
	            		success=false;
	            	}
	            }
			} catch (Exception e) {
				e.printStackTrace();
				success=false;
			}

	            
	            
	        return success;
	    }
	  
	  public void close()
	    {
//	        saveListCriticaOperacoesRealizadasToDatabase();
//	        cleanTempFolder();

	        System.out.println("Finalizando driver.");
	        this.driver.quit();
	    }
	
	public void openChecagem()
	{
		this.driver.findElement(By.xpath("//*[@id=\"ctl00_zmenu\"]/nav/div[2]/ul[7]/li/a")).click();
		Utils.waitv(2);
		this.driver.findElement(By.xpath("//*[@id=\"ctl00_linkCHECAGEM\"]")).click();
		Utils.waitv(4);
		ArrayList<String> tabs = new ArrayList<>(this.driver.getWindowHandles());
		int iTab=0;
		for(String tab:tabs)
		{
			System.out.println("Tab: "+tab);
			if(iTab>0)
			{
				this.driver.switchTo().window(tab);
				break;
			}
			iTab++;
		}
		System.out.println(this.driver.getCurrentUrl());
		this.driver.get("https://gercloud2.rgbsys.com.br/GER3_BMA/Checagem-Filtro-Global");
		System.out.println(this.driver.getCurrentUrl());
		WebElement dataInicial = this.driver.findElement(By.xpath("//*[@id=\"DataInicial\"]"));
		dataInicial.click();
		dataInicial.clear();
		Utils.waitv(7);
//		for(int i=0;i<20;i++)
//		{
//			dataInicial.sendKeys(Keys.ARROW_RIGHT);
//		}
		for(int i=0;i<20;i++)
		{
			dataInicial.sendKeys(Keys.DELETE);
		}
		Calendar calInicial= Calendar.getInstance();
		calInicial.add(Calendar.DAY_OF_MONTH, -30);
		System.out.println("DataInicial: "+sdfr.format(calInicial.getTime()));
		dataInicial.sendKeys(sdfr.format(calInicial.getTime()));
//		Utils.sendHumanKeys(dataInicial, sdfr.format(calInicial.getTime()));
		dataInicial.sendKeys(Keys.ENTER);
	}
	
	public void refreshChecagem()
	{
		this.driver.get("https://gercloud2.rgbsys.com.br/GER3_BMA/Checagem-Filtro-Global");
//		System.out.println(this.driver.getCurrentUrl());
//		WebElement dataInicial = this.driver.findElement(By.xpath("//*[@id=\"DataInicial\"]"));
//		dataInicial.click();
//		dataInicial.clear();
//		Utils.waitv(7);
////		for(int i=0;i<20;i++)
////		{
////			dataInicial.sendKeys(Keys.ARROW_RIGHT);
////		}
//		for(int i=0;i<20;i++)
//		{
//			dataInicial.sendKeys(Keys.DELETE);
//		}
//		Calendar calInicial= Calendar.getInstance();
//		calInicial.add(Calendar.DAY_OF_MONTH, -30);
//		System.out.println("DataInicial: "+sdfr.format(calInicial.getTime()));
//		dataInicial.sendKeys(sdfr.format(calInicial.getTime()));
////		Utils.sendHumanKeys(dataInicial, sdfr.format(calInicial.getTime()));
//		dataInicial.sendKeys(Keys.ENTER);
	}
	
	public void filterTitulo(TituloParaChecagem tituloParaChecagem)
	{
		WebElement sacadoField = this.driver.findElement(By.xpath("//*[@id=\"CNPJSacadoFiltro\"]"));
		sacadoField.click();
		sacadoField.clear();
		Utils.sendHumanKeysSilent(sacadoField, tituloParaChecagem.getCnpjSacado());
		Utils.waitv(2);
		WebElement empresaField = this.driver.findElement(By.xpath("//*[@id=\"div-coluna-03\"]/div/table/tbody/tr[2]/td[2]/div/p/span"));
		System.out.println("Empresa Field: "+empresaField.getText());
		if(empresaField.getText().toLowerCase().contains(tituloParaChecagem.getNomeEmpresa().toLowerCase()))
		{
			System.out.println("Empresa already selected!");
		}
		else
		{
			empresaField.click();
			Utils.waitv(3);
			empresaField = this.driver.findElement(By.xpath("//*[@id=\"div-coluna-03\"]/div/table/tbody/tr[2]/td[2]/div/p/input"));
			Utils.sendHumanKeysSilent(empresaField, tituloParaChecagem.getNomeEmpresa());
			Utils.waitv(2);
			WebElement optionsEmpresa = this.driver.findElement(By.xpath("//*[@id=\"div-coluna-03\"]/div/table/tbody/tr[2]/td[2]/div/div"));
			List<WebElement> listaEmpresas=optionsEmpresa.findElements(By.tagName("li"));
			for(WebElement empresaFromLista: listaEmpresas)
			{
				if(empresaFromLista.getText().toLowerCase().contains(tituloParaChecagem.getNomeEmpresa().toLowerCase()))
				{
					System.out.println("Found empresa "+tituloParaChecagem.getNomeEmpresa());
					WebElement boxSelect = empresaFromLista.findElement(By.tagName("span"));
					boxSelect.click();
					Utils.waitv(1);
					break;
				}
			}
			WebElement okEmpresa=optionsEmpresa.findElement(By.className("btnOk"));
			okEmpresa.click();
		}
		
		WebElement cedenteField=this.driver.findElement(By.xpath("//*[@id=\"div-coluna-03\"]/div/table/tbody/tr[3]/td[2]/div/p/span"));
		System.out.println("Cedente Field: "+cedenteField.getText());
		if(cedenteField.getText().toLowerCase().contains(tituloParaChecagem.getNomeCedente().toLowerCase()))
		{
			System.out.println("Cedente Already Selected!");
		}
		else
		{
			cedenteField.click();
			Utils.waitv(3);
			cedenteField=this.driver.findElement(By.xpath("//*[@id=\"div-coluna-03\"]/div/table/tbody/tr[3]/td[2]/div/p/input"));
			Utils.sendHumanKeysSilent(cedenteField, tituloParaChecagem.getNomeCedente());
			Utils.waitv(5);
			
			WebElement optionsCedente = this.driver.findElement(By.xpath("//*[@id=\"div-coluna-03\"]/div/table/tbody/tr[3]/td[2]/div/div"));
			List<WebElement> listaCedentes=optionsCedente.findElements(By.tagName("li"));
			for(WebElement cedenteFromLista: listaCedentes)
			{
				if(cedenteFromLista.getText().toLowerCase().contains(tituloParaChecagem.getNomeCedente().toLowerCase()))
				{
					System.out.println("Found cedente "+tituloParaChecagem.getNomeCedente());
					WebElement boxSelect = cedenteFromLista.findElement(By.tagName("span"));
					boxSelect.click();
					Utils.waitv(1);
					break;
				}				
			}
			optionsCedente = this.driver.findElement(By.xpath("//*[@id=\"div-coluna-03\"]/div/table/tbody/tr[3]/td[2]/div/div"));
			WebElement okCedente=optionsCedente.findElement(By.className("btnOk"));
			okCedente.click();
			Utils.waitv(2);
//			Utils.waitv(100);
		}
		WebElement buttonFiltrar=this.driver.findElement(By.xpath("//*[@id=\"btfiltrar\"]"));
		buttonFiltrar.click();
		Utils.waitv(10);
	}
	
	public void selectAndFillChecagem(Connection connMaria,ArrayList<TituloParaChecagem> titulosParaChecagemMesmaChave, ArrayList<Evento> eventosCadastrados)
	{
		System.out.println("*****************************************************************");
		System.out.println("Titles with the same Cedente, Sacado, Empresa and ChaveNFE");
		HashMap<String, String> duplicataNumbers = new HashMap<>();

		for(TituloParaChecagem tituloParaChecagem:titulosParaChecagemMesmaChave)
		{
			tituloParaChecagem.show();
			duplicataNumbers.put(tituloParaChecagem.getDuplicata(), tituloParaChecagem.getDuplicata());
		}
		System.out.println("*****************************************************************");
		
		WebElement tableTitulos=this.driver.findElement(By.xpath("//*[@id=\"tbtitulo\"]"));
		WebElement bodyTable=tableTitulos.findElement(By.tagName("tbody"));
		List<WebElement> rows = bodyTable.findElements(By.tagName("tr"));
		boolean hasTitlesToRegister=false;
		for(WebElement row:rows)
		{
			System.out.println(row.getText());
			List<WebElement> cols=row.findElements(By.tagName("td"));
			int iCol=0;
			for(WebElement col:cols)
			{
				System.out.println(iCol + " "+col.getText());
				if(iCol==12 || iCol==13)
				{
					if(duplicataNumbers.get(col.getText())!=null)
					{
						System.out.println("Found valid title "+col.getText()+"!");
						col.click();
						hasTitlesToRegister=true;
						Utils.waitv(1);
					}
				}
				iCol++;
			}
		}
		boolean mercadoriaEntregue=false;
		boolean emTransporte=false;
		boolean comDivergencia=false;
		if(hasTitlesToRegister)
		{
			String textCriticas="";
			for(Evento evento:eventosCadastrados)
			{
				evento.show();
//				textCriticas+=sdfm.format(evento.getDataHora())+" "+evento.getTituloEvento()+" ";
				textCriticas+=evento.getDataHora().format(dtfs)+" "+evento.getTituloEvento()+" ";
				if(evento.getTituloEvento().toLowerCase().contains("previsao de entrega"))
				{
					emTransporte=true;
				}
				if(evento.getTituloEvento().toLowerCase().contains("desacordo")
						|| evento.getTituloEvento().toLowerCase().contains("avaria")
						|| evento.getTituloEvento().toLowerCase().contains("devolu")
						|| evento.getTituloEvento().toLowerCase().contains("devolvid")
						)
				{
					comDivergencia=true;
				}
				if(evento.getTituloEvento().toLowerCase().contains("mercadoria entregue"))
				{
					mercadoriaEntregue=true;
					emTransporte=false;
					comDivergencia=false;
				}
			}
			
			if(eventosCadastrados.size()>0)
			{
				System.out.println("Hora de clicar em nova crítica!");
//				Utils.waitv(100);
				
				WebElement novaCritica=null;
//				  /html/body/div[1]/div[1]/div[23]/div[3]/div[2]/a[7]/span/button/i
//				novaCritica=this.driver.findElement(By.xpath("/html/body/div[1]/div[1]/div[22]/div[3]/div[2]/a[7]/span/button"));
				novaCritica=this.driver.findElement(By.xpath("/html/body/div[1]/div[1]/div[23]/div[3]/div[2]/a[7]/span/button/i"));
				novaCritica.click();
//				Utils.waitv(100);
				Utils.waitv(5);
				try {					
//					this.driver.findElement(By.xpath("//*[@id=\"ddlnovostatus\"]")).click();
					this.driver.findElement(By.xpath("//*[@id=\"divnovascriticas\"]/div/div/div[2]/table/tbody/tr[2]/td/div/p/span")).click();
					Utils.waitv(2);
//					Utils.waitv(100);
				} catch (Exception e) {
					e.printStackTrace();
					this.driver.close();					
					Utils.waitv(100);
				}
				
//				WebElement pesquisar=this.driver.findElement(By.xpath("//*[@id=\"divnovascriticas\"]/div/div/div[2]/table/tbody/tr[2]/td/div/p/input"));
//				pesquisar.click();
				WebElement optionsPack=this.driver.findElement(By.xpath("//*[@id=\"divnovascriticas\"]/div/div/div[2]/table/tbody/tr[2]/td/div/div/ul"));
//				WebElement optionsPack = this.driver.findElement(By.xpath("//*[@id=\"ddlnovostatus\"]"));
				List<WebElement> optionsList = optionsPack.findElements(By.tagName("li"));
				int iOption=0;
				for(WebElement option:optionsList)
				{
					System.out.println(iOption+":" +option.getText());
					if(option.getText().toLowerCase().contains("104-chec - transportadora"))
					{
						option.click();
						break;
					}
					iOption++;
				}
				
				System.out.println("Trying to send the text: "+textCriticas);
				Utils.waitv(5);
				WebElement textObservacao=this.driver.findElement(By.xpath("//*[@id=\"idobs1\"]"));
				textObservacao.click();
				textObservacao.sendKeys(textCriticas);
				
//				Utils.waitv(100);
				System.out.println("*****************");
				System.out.println(textCriticas);
				System.out.println("*****************");
//				Utils.sendHumanKeys(textObservacao, textCriticas);	
				
				Utils.waitv(1);
				this.driver.findElement(By.xpath("//*[@id=\"divnovascriticas\"]/div/div/div[3]/div[3]/button")).click();
				Utils.waitv(5);			
			}
		
			if(emTransporte || comDivergencia || mercadoriaEntregue)
			{
				tableTitulos=this.driver.findElement(By.xpath("//*[@id=\"tbtitulo\"]"));
				bodyTable=tableTitulos.findElement(By.tagName("tbody"));
				rows = bodyTable.findElements(By.tagName("tr"));
				for(WebElement row:rows)
				{
					System.out.println(row.getText());
					List<WebElement> cols=row.findElements(By.tagName("td"));
					int iCol=0;
					for(WebElement col:cols)
					{
	//					System.out.println(iCol + " "+col.getText());
						if(iCol==12 || iCol==13)
						{
							if(duplicataNumbers.get(col.getText())!=null)
							{
								System.out.println("Found valid title "+col.getText()+"!");
								col.click();
								Utils.waitv(1);
							}
						}
						iCol++;
					}
				}
				
				
				WebElement novoStatus=null;
				novoStatus = this.driver.findElement(By.xpath("/html/body/div[1]/div[1]/div[23]/div[3]/div[2]/a[6]/span/button"));
//				novoStatus = this.driver.findElement(By.xpath("/html/body/div[1]/div[1]/div[22]/div[3]/div[2]/a[6]/span/button"));
				novoStatus.click();
				Utils.waitv(8);
	
//				/html/body/div[1]/div[1]/div[22]/div[3]/div[2]/a[6]/span/button/i
				try {
					this.driver.findElement(By.xpath("//*[@id=\"divnovostatus\"]/div/div/div[2]/table/tbody/tr[2]/td")).click();	
				} catch (Exception e) {
					Utils.waitv(100);
				}
				
				WebElement selectNovoStatus=this.driver.findElement(By.xpath("//*[@id=\"ddlnovostatus\"]"));
				Select select=new Select(selectNovoStatus);
				if(mercadoriaEntregue)
				{
					select.selectByValue("63"); //CTMOI => CONFIRMADO TRANSPORTADORA SSW - F
				}
				else if(comDivergencia)
				{
					select.selectByValue("48"); //DTMOI => DIVERGENCIA TRANSPORTADORA SSW - P
				}
				else if(emTransporte)
				{
					select.selectByValue("42"); //TMOI => EM TRANSPORTE SSW - MOISES - F
				}
	//			select.selectByVisibleText("CT    => CONFIRMADO VIA TRANSPORTADORA  - F");
				Utils.waitv(5);
				WebElement salvar= this.driver.findElement(By.xpath("//*[@id=\"BotaoGravarTextoPos\"]"));
				salvar.click();
				SalvarCriticasConsultaSSW.showCurrentProgress();
				Utils.waitv(6);
			}
		}
		else
		{
			System.out.println("No titles for this selection!!!");
		}
		this.driver.findElement(By.xpath("//*[@id=\"tbtitulo_wrapper\"]/div[2]/a[7]/span/button")).click();
		
		for(Evento evento:eventosCadastrados)
		{
			evento.updateCadastrado(connMaria);
		}
		Utils.waitv(7);		
		System.out.println("Click on VOLTAR!");
		Utils.waitv(6);
	}

	public static SimpleDateFormat getSdfr() {
		return sdfr;
	}

	public static void setSdfr(SimpleDateFormat sdfr) {
		RgbsysCriticaConsultaSSW.sdfr = sdfr;
	}

	public static SimpleDateFormat getSdfm() {
		return sdfm;
	}

	public static void setSdfm(SimpleDateFormat sdfm) {
		RgbsysCriticaConsultaSSW.sdfm = sdfm;
	}

	public String getUserName() {
		return this.userName;
	}

	public void setUserName(String userName) {
		this.userName = userName;
	}

	public String getPassword() {
		return this.password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public String getRootURL() {
		return this.rootURL;
	}

	public void setRootURL(String rootURL) {
		this.rootURL = rootURL;
	}

	public WebDriver getDriver() {
		return this.driver;
	}

	public void setDriver(WebDriver driver) {
		this.driver = driver;
	}

	public static SimpleDateFormat getSdfmh() {
		return sdfmh;
	}

	public static void setSdfmh(SimpleDateFormat sdfmh) {
		RgbsysCriticaConsultaSSW.sdfmh = sdfmh;
	}

	public JavascriptExecutor getJsExecutor() {
		return this.jsExecutor;
	}
}
