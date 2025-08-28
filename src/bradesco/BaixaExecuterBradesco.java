package bradesco;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.openqa.selenium.By;
import org.openqa.selenium.Dimension;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.WindowType;
import org.openqa.selenium.chrome.ChromeDriver;

import conta_grafica.OperacaoRecompra;
import conta_grafica.TituloRecompra;
import empresa.Empresa;
import mssql.ConnectorMSSQL;
import mysql.ConnectorMariaDB;
import utils.Utils;

public class BaixaExecuterBradesco 
{
	private WebDriver driver = null;
	private static HashMap<String, Empresa> empresas = new HashMap<>();
	private static HashMap<String, String> codigoTipoCobranca = new HashMap<>();
	private static Connection connMaria = null;
	private static Connection connMSQL = null;
	public BaixaExecuterBradesco()
	{
//		WebDriverManager.chromedriver().setup();
		this.driver = new ChromeDriver();
		Dimension dimension = new Dimension(1024, 1600);
		this.driver.manage().window().setSize(dimension);
	}
	
	public static void main(String[] args)
	{
		setupCodigoTipoCobranca();
		ConnectorMariaDB.connect();
		ConnectorMSSQL.connect();
		connMaria = ConnectorMariaDB.getConn();
		connMSQL = ConnectorMSSQL.getConn();
		empresas=Empresa.empresas(ConnectorMariaDB.conn, ConnectorMSSQL.conn);

		BaixaExecuterBradesco remesssaSender = null;
		boolean logado=false;
		while(!logado)
		{
			remesssaSender = new BaixaExecuterBradesco();
			logado=remesssaSender.login();
			Utils.waitv(5);
			if(logado)
			{
				System.out.println("Login bem sucedido!!");				
			}
			else
			{
				remesssaSender.exit();
			}
		}
		
		if(logado)
		{			
			for(String keyEmpresa:empresas.keySet())
			{
				try {
					remesssaSender.selectCompany(empresas.get(keyEmpresa));
					ArrayList<OperacaoRecompra> listaOperacaoRecompraPendentes = OperacaoRecompra.operacoesRecompraPendentes(ConnectorMariaDB.conn, ConnectorMSSQL.conn,empresas.get(keyEmpresa));
					for(OperacaoRecompra operacaoRecompra:listaOperacaoRecompraPendentes)
					{
						remesssaSender.baixarTitulos(operacaoRecompra);
						operacaoRecompra.setBaixadoBradesco(true);
						operacaoRecompra.updateBaixadoBradesco(ConnectorMariaDB.conn);
					}
					
				} catch (Exception e) {
					e.printStackTrace();
				}
				
//				Utils.waitv(555);
			}
		}
		try {
			remesssaSender.logout();			
		} catch (Exception e) {
			e.printStackTrace();
		}

		remesssaSender.exit();
	}
	public  void logout()
	{
		this.driver.switchTo().defaultContent();
		List<WebElement> sair = this.driver.findElements(By.xpath("//*[@id=\"botaoSair\"]"));
		if(!sair.isEmpty())
		{
			sair.get(0).click();
			Utils.waitv(5);
		}	
	}

	public void exit()
	{
		this.driver.close();
		System.out.println("Closing browser!");
	}
	
	public static void setupCodigoTipoCobranca()
	{
		codigoTipoCobranca.put("42", "codigo_pre_impresso");
		codigoTipoCobranca.put("48", "codigo_pre_impresso");
		codigoTipoCobranca.put("67", "codigo_pre_impresso");
		codigoTipoCobranca.put("50", "codigo_pre_impresso");
		codigoTipoCobranca.put("56", "codigo_pre_impresso");
		codigoTipoCobranca.put("69", "codigo_pre_impresso");
		codigoTipoCobranca.put("71", "codigo_pre_impresso");
		
		codigoTipoCobranca.put("32", "codigo_convencional");
		codigoTipoCobranca.put("66", "codigo_convencional");
		codigoTipoCobranca.put("07", "codigo_convencional");
		codigoTipoCobranca.put("68", "codigo_convencional");
		codigoTipoCobranca.put("70", "codigo_convencional");
	}
	
	public boolean login()
	{
		boolean success=false;
		this.driver.get("https://www.ne12.bradesconetempresa.b.br/ibpjlogin/login.jsf");
		Utils.waitv(10);
		WebElement user = this.driver.findElement(By.id("identificationForm:txtUsuario"));
		Utils.sendHumanKeys(user, "AAA01479");
//		user.sendKeys("AAA01479");
		Utils.waitv(2);
		WebElement passwd = this.driver.findElement(By.id("identificationForm:txtSenha"));
//		passwd.sendKeys("Maninha1207");
		Utils.sendHumanKeys(passwd, "Maninha1207");
		Utils.waitv(2);
		WebElement goForward = this.driver.findElement(By.id("identificationForm:botaoAvancar"));
		goForward.click();
		Utils.waitv(6);
		
		List<WebElement> instalarMaisTarde = this.driver.findElements(By.id("formInstalarOFDB:btn-avancar-left"));
		if(!instalarMaisTarde.isEmpty())
		{
			if(instalarMaisTarde.get(0).isDisplayed())
			{
				instalarMaisTarde.get(0).click();
			}
		}
		Utils.waitv(15);
		
		List<WebElement> cancelarAcesso = this.driver.findElements(By.id("cancelAcesso"));
		
		if(!cancelarAcesso.isEmpty())
		{
			List<WebElement> closeButton = this.driver.findElements(By.id("btnFecharModal"));
			if(!closeButton.isEmpty())
			{
				if(closeButton.get(0).isDisplayed())
				{
					closeButton.get(0).click();
					System.out.println("Can not continue login process!");
					return success;
				}
			}			
		}
		else {			
			System.out.println("No Cancelar acesso popup! Continuing!");
		}

	
		
		this.driver.switchTo().defaultContent();		
		List<WebElement> conteudo=this.driver.findElements(By.id("conteudo"));
		if(!conteudo.isEmpty())				
		{
			System.out.println("Conteudo found");
			List<WebElement> recaptcha=this.driver.findElements(By.id("recaptcha"));
			if(!recaptcha.isEmpty())
			{
				System.out.println("Within recaptcha");
				List<WebElement> frameAutenticator=recaptcha.get(0).findElements(By.tagName("iframe"));
				if(!frameAutenticator.isEmpty())
				{
					System.out.println("Within recaptcha frame");
					this.driver.switchTo().frame(frameAutenticator.get(0));
					WebElement recaptchaBox = this.driver.findElement(By.xpath("//*[@id=\"recaptcha-anchor\"]/div[1]"));
					recaptchaBox.click();
					Utils.waitv("RecaptchaBox clicked",6);
				}			
			}
			else {
				System.out.println("Recaptcha not found");
				Utils.waitv(35);
			}
		}
		else {
			System.out.println("Conteudo not found");
		}

						
		List<WebElement> logoBradesco=this.driver.findElements(By.className("HtmlOutputLinkBradesco"));
		if(!logoBradesco.isEmpty())
		{
			List<WebElement> overlay = this.driver.findElements(By.className("jqmOverlay"));
			if(!overlay.isEmpty())
			{
				overlay.get(0).click();
				Utils.waitv(2);
			}
			if(logoBradesco.get(0).isEnabled())
			{
				logoBradesco.get(0).click();
				Utils.waitv(3);
			}
			else {
				this.driver.navigate().refresh();
			}
		}
		success=true;
		return success;
	}
	
	public void selectCompany(Empresa empresa)
	{		
		List<WebElement> logoBradesco=this.driver.findElements(By.className("HtmlOutputLinkBradesco"));
		if(!logoBradesco.isEmpty())
		{
			List<WebElement> overlay = this.driver.findElements(By.className("jqmOverlay"));
			if(!overlay.isEmpty())
			{
				overlay.get(0).click();
				Utils.waitv(2);
			}
			if(logoBradesco.get(0).isEnabled())
			{
				logoBradesco.get(0).click();
				Utils.waitv(3);
			}
			else {
				this.driver.navigate().refresh();
			}
		}
		// xx.xxx.xxx/0001-xx
		String cnpjFormatado=empresa.getCnpj().substring(0, 2)+"."
											+empresa.getCnpj().substring(2,5)+"."
											+empresa.getCnpj().substring(5,8)+"/"
											+empresa.getCnpj().substring(8,12)+"-"
											+empresa.getCnpj().substring(12,14)
											;
		
		
		WebElement outrasEmpresas = this.driver.findElement(By.id("lnkGrupoEconomico"));
		outrasEmpresas.click();
		Utils.waitv(15);
		
		WebElement frameModalnfraestrutura = this.driver.findElement(By.id("modal_infra_estrutura"));
		this.driver.switchTo().frame(frameModalnfraestrutura);
		
		List<WebElement> rowsCompany = this.driver.findElements(By.tagName("tr"));
		
		for(WebElement row:rowsCompany)
		{
			System.out.println(row.getText());
			if(row.getText().contains(cnpjFormatado))
			{
				System.out.println("Found CNPJ "+cnpjFormatado);
				List<WebElement>cols = row.findElements(By.tagName("td"));
				cols.get(0).click();
				Utils.waitv(15);
				break;
			}
		}
		
		logoBradesco=this.driver.findElements(By.className("HtmlOutputLinkBradesco"));
		if(!logoBradesco.isEmpty())
		{
			List<WebElement> overlay = this.driver.findElements(By.className("jqmOverlay"));
			if(!overlay.isEmpty())
			{
				overlay.get(0).click();
				Utils.waitv(2);
			}
			if(logoBradesco.get(0).isEnabled())
			{
				logoBradesco.get(0).click();
				Utils.waitv(3);
			}
			else {
				this.driver.navigate().refresh();
			}
		}
		System.out.println("Selected: "+ empresa.getRazaoSocial() + "   Apelido: "+empresa.getApelido() + " CNPJ: "+empresa.getCnpj());
	}
	
	public void enviarRemessas()
	{
		System.out.println("CurrentURL: "+this.driver.getCurrentUrl());
		String control=this.driver.getCurrentUrl().split("=")[1];
		String urlTransmissaoArquivos="https://www.ne14.bradesconetempresa.b.br/ibpjwebta/remessa.jsf?CTRL="+control;
		this.driver.switchTo().newWindow(WindowType.TAB);
		this.driver.get(urlTransmissaoArquivos);
		WebElement servicos=this.driver.findElement(By.id("formTransmitirArquivos:servico"));
		servicos.click();
		Utils.waitv(2);
		List<WebElement> options = this.driver.findElements(By.tagName("option"));
		for(WebElement option:options)
		{
			System.out.println(option.getText() + " Value: "+ option.getAttribute("value") );
			if(option.getText().trim().equals("COBRANCA"))
			{
				System.out.println("Selecting: "+option.getText() + " Value: "+ option.getAttribute("value") );
				option.click();				
				break;
			}
		}
		Utils.waitv(2);
		List<WebElement> adicionarArquivos=this.driver.findElements(By.tagName("input"));
		for(WebElement adicionar:adicionarArquivos)
		{
			if(adicionar.getAttribute("value").trim().contains("Adicionar Arquivos"))
			{
				adicionar.click();
				break;
			}
		}
		Utils.waitv(666);
	}
	
	public void baixarTitulos(OperacaoRecompra operacaoRecompra)
	{
		System.out.println("CurrentURL: "+this.driver.getCurrentUrl());
		String control=this.driver.getCurrentUrl().split("=")[1];

		for(TituloRecompra tituloRecompra:operacaoRecompra.getTitulosRecompra())
		{
			if(tituloRecompra.isBaixadoBradesco()
					|| codigoTipoCobranca.get(tituloRecompra.getTipoCobranca())==null )
			{
				if(tituloRecompra.isBaixadoBradesco())
				{
					System.out.println("Titulo já baixado no Bradesco!");
				}
				else
				{
					System.out.println("TipoCobranca: "+tituloRecompra.getTipoCobranca());
				}
				continue;
			}
			String urlCobranca="https://www.ne12.bradesconetempresa.b.br/ibpjtelainicial/menuCobranca.jsf?CTRL="+control;
			this.driver.switchTo().newWindow(WindowType.TAB);
			this.driver.get(urlCobranca);
			Utils.waitv(4);

			System.out.println("Alterar boleto - Click!");
			this.driver.findElement(By.linkText("Alterar boleto")).click();

			
			System.out.println("CodigoPagamento: "+codigoTipoCobranca.get(tituloRecompra.getTipoCobranca()));
			String sufix="";
			if(codigoTipoCobranca.get(tituloRecompra.getTipoCobranca()).contains("convencional"))
			{
				sufix="| "+operacaoRecompra.getEmpresa().getDadosBancariosEmpresa().getCodigoConvencional();
			}
			else if(codigoTipoCobranca.get(tituloRecompra.getTipoCobranca()).contains("impresso"))
			{
				sufix="| "+operacaoRecompra.getEmpresa().getDadosBancariosEmpresa().getCodigoPreImpresso();
			}
			System.out.println("Sufix: "+sufix);
			this.driver.findElement(By.id("cdConta__sexyCombo")).click();
			Utils.waitv(1);
			
			List<WebElement> codigosTipoPagamento = this.driver.findElements(By.tagName("li"));
			for(WebElement codigo:codigosTipoPagamento)
			{
				if(codigo.getText().trim().endsWith(sufix) && !codigo.getText().toLowerCase().contains("contrato"))
				{
					System.out.println("Found sufix on "+codigo.getText());
					codigo.click();
					break;
				}
			}
			
			System.out.println("Nosso numero: "+ tituloRecompra.getNossoNumero());
//			Utils.waitv(2);
//			this.driver.findElement(By.linkText("Baixar ou transferir boletos para desconto")).click();
//			Utils.waitv(5);
			//*[@id="divErroNum"]/ul/li/div/input
			//*[@id="divErroNum"]/ul/li/div/input
			this.driver.findElement(By.name("nossoNumeroPesquisa")).sendKeys(tituloRecompra.getNossoNumero());
			
//			this.driver.findElement(By.id("nossonum")).sendKeys(tituloRecompra.getNossoNumero());
			
//			this.driver.findElement(By.xpath("//*[@id=\"faixav\"]"));
			
			//*[@id="ListaTitulosPendentesForm"]/div[1]/div[7]
			
			WebElement camposForm = this.driver.findElement(By.xpath("//*[@id=\"ListaTitulosPendentesForm\"]/div[1]/div[7]"));
			List<WebElement> listOptions = camposForm.findElements(By.className("formulario"));
			System.out.println("Options: "+listOptions.size());
			for(WebElement option:listOptions)
			{
				System.out.println(option.getText());
				if(option.getText().toLowerCase().contains("a vencer"))
				{
					WebElement pointer = option.findElement(By.className("pointer"));
					pointer.click();
					Utils.waitv(2);
					break;
				}
			}
			
//			Utils.waitv(200);
			this.driver.findElement(By.name("btnBuscar")).click();
			Utils.waitv(10);
//			this.driver.findElement(By.name("btnAvancar")).click();
//			Utils.waitv(10);
			boolean foundNothing=false;			
			List<WebElement> textNonExist = this.driver.findElements(By.xpath("//*[@id=\"conteudo\"]/div[2]/div[2]/div/div/div"));
			if(!textNonExist.isEmpty())
			{
				if(textNonExist.get(0).getText().toLowerCase().contains("titulo inexistente para o contrato informado"))
				{
					foundNothing=true;
				}
			}
//			List<WebElement> texts = this.driver.findElements(By.tagName("p"));
//			boolean foundNothing=false;
//			for(WebElement text:texts)
//			{
//				if(text.getText().trim().toLowerCase().contains("solicitação não encontrada"))
//				{
//					System.out.println("Found nothing!");
//					Utils.waitv(3);
//					foundNothing=true;
//					break;
//				}
//			}
			if(foundNothing)
			{
				tituloRecompra.setBaixadoBradesco(true);
				tituloRecompra.updateBaixadoBradesco(connMaria);
				System.out.println("Clicando em voltar!");
				this.driver.findElement(By.linkText("Voltar")).click();
				Utils.waitv(11);
			}
			else 
			{
				Utils.waitv(5);
				//*[@id="divErroTabela"]/table
				WebElement tableBoletos = this.driver.findElement(By.xpath("//*[@id=\"divErroTabela\"]/table"));
				List<WebElement> rowsBoletos = tableBoletos.findElements(By.tagName("tr"));
				System.out.println("Buscando '"+tituloRecompra.getNossoNumero()+"'");
				for(WebElement row:rowsBoletos)
				{
					System.out.println(row.getText());
					List<WebElement> cols = row.findElements(By.tagName("td"));
					if(cols.size()>0)
					{
						int iCol=0;
						for(WebElement col:cols)
						{
							System.out.println(iCol+"|"+col.getText());
							iCol++;
						}
						if(cols.get(3).getText().toLowerCase().contains(tituloRecompra.getNossoNumero())
								|| tituloRecompra.getNossoNumero().contains(cols.get(3).getText())
								)
						{
							System.out.println("Nosso numero encontrado");
							WebElement checkbox=cols.get(0).findElement(By.tagName("input"));
							checkbox.click();
							Utils.waitv(3);
							//*[@id="ListaTitulosPendentesForm"]/div/div[7]/ul/li[2]/input
							WebElement cancelarBoleto = this.driver.findElement(By.xpath("//*[@id=\"ListaTitulosPendentesForm\"]/div/div[7]/ul/li[2]/input"));
							cancelarBoleto.click();
							Utils.waitv(5);
							WebElement avancar = this.driver.findElement(By.xpath("//*[@id=\"btnAvancar2\"]"));
							avancar.click();
							Utils.waitv(4);
//							WebElement passwd = this.driver.findElement(By.xpath("//*[@id=\"_id23:_id270\"]/ul/li/div/table/tbody/tr/td[2]/span/input"));
//							Utils.sendHumanKeys(passwd, "Maninha1207");
							
							WebElement frameAutenticator=this.driver.findElement(By.id("iframeAutenticador"));
							this.driver.switchTo().frame(frameAutenticator);
							List<WebElement> inputs = this.driver.findElements(By.tagName("input"));
							boolean foundPassword=false;
							for(WebElement input:inputs)
							{
								System.out.println(input.getTagName() + " type "+input.getAttribute("type") + " title "+input.getAttribute("title"));
								if(input.getAttribute("type").toLowerCase().contains("password"))
								{
									Utils.sendHumanKeys(input, "Maninha1207");
									foundPassword=true;
									break;
								}
							}
							if(foundPassword)
							{
								this.driver.switchTo().defaultContent();
								System.out.println("Confirmando!");
								this.driver.findElement(By.id("btnConfirmar")).click();
								Utils.waitv(5);
							}
							break;
						}
						else {
							System.out.println("Nosso numero não encontrado");
						}
					}
					
				}
				
				List<WebElement> texts2 = this.driver.findElements(By.tagName("li"));
				for(WebElement text:texts2)
				{
					if(text.getText().toLowerCase().contains("situação do título inválida para baixa/transferencia."))
					{
						System.out.println("Título já baixado!");
						tituloRecompra.setBaixadoBradesco(true);
						break;
					}
				}		
				try {
					WebElement frameComprovante = this.driver.findElement(By.id("iframeComprovante"));
					this.driver.switchTo().frame(frameComprovante);
					tituloRecompra.setBaixadoBradesco(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
				
			}
			tituloRecompra.updateBaixadoBradesco(ConnectorMariaDB.conn);
			System.out.println("After finishing with the title!");

			ArrayList<String> tabs = new ArrayList<String> (driver.getWindowHandles());
			if(tabs.size()>1)
			{
				System.out.println("Still more than one tab, number of tabs: "+tabs.size());
				this.driver.close();
				this.driver.switchTo().window(tabs.get(0));
			}
		}
	}
	
	public WebDriver getDriver() {
		return this.driver;
	}
	public void setDriver(WebDriver driver) {
		this.driver = driver;
	}

	public static HashMap<String, Empresa> getEmpresas() {
		return empresas;
	}

	public static void setEmpresas(HashMap<String, Empresa> empresas) {
		BaixaExecuterBradesco.empresas = empresas;
	}

	public static HashMap<String, String> getCodigoTipoCobranca() {
		return codigoTipoCobranca;
	}

	public static void setCodigoTipoCobranca(HashMap<String, String> codigoTipoCobranca) {
		BaixaExecuterBradesco.codigoTipoCobranca = codigoTipoCobranca;
	}

	public static Connection getConnMaria() {
		return connMaria;
	}

	public static void setConnMaria(Connection connMaria) {
		BaixaExecuterBradesco.connMaria = connMaria;
	}

	public static Connection getConnMSQL() {
		return connMSQL;
	}

	public static void setConnMSQL(Connection connMSQL) {
		BaixaExecuterBradesco.connMSQL = connMSQL;
	}
}
