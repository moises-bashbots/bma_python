package bradesco;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.WindowType;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import conta_grafica.ExtratoBradesco;
import conta_grafica.LancamentoBradesco;
import empresa.Empresa;
import mssql.ConnectorMSSQL;
import mysql.ConnectorMariaDB;
import utils.Utils;

public class ExtratoDownloader 
{
	private static WebDriver driver = null;
	private static HashMap<String, Empresa> empresas = new HashMap<>();
	private static SimpleDateFormat sdf=new SimpleDateFormat("dd/MM/yyyy");
	private static SimpleDateFormat sdfd=new SimpleDateFormat("yyyy-MM-dd");
	
	public ExtratoDownloader()
	{
//		WebDriverManager.firefoxdriver().setup();
		
	}
	
	public static void main(String[] args)
	{
		ConnectorMariaDB.connect();
		ConnectorMSSQL.connect();
		empresas=Empresa.empresas(ConnectorMariaDB.conn, ConnectorMSSQL.conn);
		ExtratoDownloader.driver = new ChromeDriver();
		boolean logado=false;
		int attempt=0;
		int limitAttempts=4;
		while(!logado && attempt < limitAttempts)
		{
			System.out.println("Attempt: "+attempt);
			try {
					ExtratoDownloader.driver = new ChromeDriver();
					logado=ExtratoDownloader.login();
			} catch (Exception e) {
				attempt++;
			
				e.printStackTrace();
			}

			Utils.waitv(5);
			if(logado)
			{
				System.out.println("Login bem sucedido!!");				
			}
			else
			{
				try {
					ExtratoDownloader.exit();	
				} catch (Exception e) {
					e.printStackTrace();
				}				
			}
		}
		
		if(logado)
		{			
			for(String keyEmpresa:empresas.keySet())
			{
				try {
					ExtratoDownloader.selectCompany(empresas.get(keyEmpresa));	
					try {
						ExtratoDownloader.downloadExtrato(empresas.get(keyEmpresa));	
					} catch (Exception e) {
						e.printStackTrace();
					}				
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
		try {
			ExtratoDownloader.logout();			
		} catch (Exception e) {
			e.printStackTrace();
		}		
		ExtratoDownloader.exit();
	}
	
	public static void logout()
	{
//		Object[] windowHandles = ExtratoDownloader.driver.getWindowHandles().toArray();
//		ExtratoDownloader.driver.switchTo().window((String) windowHandles[0]); 
//		ExtratoDownloader.driver.switchTo().defaultContent();		
//		List<WebElement> sair = ExtratoDownloader.driver.findElements(By.xpath("//*[@id=\"botaoSair\"]"));
////		Utils.waitv(400);
//		if(!sair.isEmpty())
//		{
//
//			WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(15));
//			try {
//				WebElement sairLink = wait.until(
//					    ExpectedConditions.elementToBeClickable(
//					        By.id("botaoSair")));
//				sairLink.click();
//				Utils.waitv(3);
//				
//			} catch (Exception e) {
//				e.printStackTrace();
//			}
//		}
//		ExtratoDownloader.driver.close();
	}

	public static void exit()
	{		
		 try {
		   ExtratoDownloader.driver.close();
			System.out.println("Closing browser!");
		} catch (Exception e) {
			System.out.println("No need to close!");
		}			
	}
	
	public static boolean login()
	{	
		boolean success=false;
		ExtratoDownloader.driver.get("https://www.ne12.bradesconetempresa.b.br/ibpjlogin/login.jsf");
		Utils.waitv(10);
		WebElement user = ExtratoDownloader.driver.findElement(By.id("identificationForm:txtUsuario"));
		Utils.sendHumanKeys(user, "AAA01479");
//		user.sendKeys("AAA01479");https://www.youtube.com/watch?v=R1wwopVP7-A
		Utils.waitv(2);
		WebElement passwd = ExtratoDownloader.driver.findElement(By.id("identificationForm:txtSenha"));
//		passwd.sendKeys("Maninha1207");
		Utils.sendHumanKeys(passwd, "Maninha1207");
		Utils.waitv(2);
		WebElement goForward = ExtratoDownloader.driver.findElement(By.id("identificationForm:botaoAvancar"));
		goForward.click();
		Utils.waitv(6);
		
		List<WebElement> instalarMaisTarde = ExtratoDownloader.driver.findElements(By.id("formInstalarOFDB:btn-avancar-left"));
		if(!instalarMaisTarde.isEmpty())
		{
			if(instalarMaisTarde.get(0).isDisplayed())
			{
				instalarMaisTarde.get(0).click();
			}
		}
		Utils.waitv(15);
		List<WebElement> cancelarAcesso = ExtratoDownloader.driver.findElements(By.id("cancelAcesso"));
		
		if(!cancelarAcesso.isEmpty())
		{
			List<WebElement> closeButton = ExtratoDownloader.driver.findElements(By.id("btnFecharModal"));
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

	
		
		ExtratoDownloader.driver.switchTo().defaultContent();		
		List<WebElement> conteudo=ExtratoDownloader.driver.findElements(By.id("conteudo"));
		if(!conteudo.isEmpty())				
		{
			System.out.println("Conteudo found");
			List<WebElement> recaptcha=ExtratoDownloader.driver.findElements(By.id("recaptcha"));
			if(!recaptcha.isEmpty())
			{
				System.out.println("Within recaptcha");
				List<WebElement> frameAutenticator=recaptcha.get(0).findElements(By.tagName("iframe"));
				if(!frameAutenticator.isEmpty())
				{
					System.out.println("Within recaptcha frame");
					ExtratoDownloader.driver.switchTo().frame(frameAutenticator.get(0));
					WebElement recaptchaBox = ExtratoDownloader.driver.findElement(By.xpath("//*[@id=\"recaptcha-anchor\"]/div[1]"));
					recaptchaBox.click();
					Utils.waitv("RecaptchaBox clicked",6);
					Utils.waitv("RecaptchaBox clicked",47);
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
				
		List<WebElement> logoBradesco=ExtratoDownloader.driver.findElements(By.className("HtmlOutputLinkBradesco"));
		if(!logoBradesco.isEmpty())
		{
			List<WebElement> overlay = ExtratoDownloader.driver.findElements(By.className("jqmOverlay"));
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
				ExtratoDownloader.driver.navigate().refresh();
			}
		}
		success=true;
		return success;
	}
	
	public static void selectCompany(Empresa empresa)
	{		
		
		List<WebElement> logoBradesco=ExtratoDownloader.driver.findElements(By.className("HtmlOutputLinkBradesco"));
		if(!logoBradesco.isEmpty())
		{
			List<WebElement> overlay = ExtratoDownloader.driver.findElements(By.className("jqmOverlay"));
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
				ExtratoDownloader.driver.navigate().refresh();
			}
		}
		
		// xx.xxx.xxx/0001-xx
		String cnpjFormatado=empresa.getCnpj().substring(0, 2)+"."
											+empresa.getCnpj().substring(2,5)+"."
											+empresa.getCnpj().substring(5,8)+"/"
											+empresa.getCnpj().substring(8,12)+"-"
											+empresa.getCnpj().substring(12,14)
											;
		
		
		WebElement outrasEmpresas = ExtratoDownloader.driver.findElement(By.id("lnkGrupoEconomico"));
		if(outrasEmpresas.isDisplayed() && outrasEmpresas.isEnabled())
		{
			WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(15));
			wait.until(ExpectedConditions.invisibilityOfElementLocated(
				    By.cssSelector("div.jqmOverlay")));
			WebElement empresaLink = wait.until(
				    ExpectedConditions.elementToBeClickable(
				        By.id("lnkGrupoEconomico")));
			outrasEmpresas.click();
			Utils.waitv(15);

		
			WebElement frameModalnfraestrutura = ExtratoDownloader.driver.findElement(By.id("modal_infra_estrutura"));
			ExtratoDownloader.driver.switchTo().frame(frameModalnfraestrutura);
			
			List<WebElement> rowsCompany = ExtratoDownloader.driver.findElements(By.tagName("tr"));
			
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
			
			logoBradesco=ExtratoDownloader.driver.findElements(By.className("HtmlOutputLinkBradesco"));
			if(!logoBradesco.isEmpty())
			{
				List<WebElement> overlay = ExtratoDownloader.driver.findElements(By.className("jqmOverlay"));
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
					ExtratoDownloader.driver.navigate().refresh();
				}

			}
		}
	}
	
	public static ExtratoBradesco readExtrato(Empresa empresa)
	{
		ArrayList<LancamentoBradesco> lancamentosBradesco = new ArrayList<>();
		String dataLancamentoString = "";
		String descricaoString="";
		String documentoString="";
		String tipoLancamento="";
		String valorCreditoString="";
		String valorDebitoString="";
		String valorLancamentoString="";
		String saldoString="";
		ExtratoBradesco extratoBradesco=new ExtratoBradesco();
		List<WebElement> blocoLancamentos = ExtratoDownloader.driver.findElements(By.id("divUltimosLancamentos"));
		if(blocoLancamentos.isEmpty())
		{
			System.out.println("Not found table");
			return null;
		}
		else
		{
			List<WebElement> rowsLancamentos = blocoLancamentos.get(0).findElements(By.tagName("tr"));
			if(!rowsLancamentos.isEmpty())
			{
				for(WebElement rowLancamento:rowsLancamentos)
				{
					List<WebElement> colsLancamentos = rowLancamento.findElements(By.tagName("td"));
					int iCol=0;
					descricaoString="";
					tipoLancamento="";
					valorCreditoString="";
					valorDebitoString="";
					for(WebElement col:colsLancamentos)
					{
						String stringText=col.getText().trim();
//						System.out.println(iCol+": "+col.getText());
						switch (iCol) {
						case 0:
							if(stringText.length()==10)
							{
								dataLancamentoString=stringText;
							}
							break;
						case 1:
							descricaoString=stringText.replaceAll("\n", " ");
							break;
						case 3:
							documentoString=stringText;
							break;
						case 4:
							valorCreditoString=stringText;
							break;
						case 5:
							valorDebitoString=stringText;
							break;
						case 6:
							saldoString=stringText;
							break;
						default:
							break;
						}
						iCol++;
					}
					if(valorCreditoString.length()>0)
					{
						tipoLancamento="C";
						valorLancamentoString=valorCreditoString;						
					}
					else if (valorDebitoString.length()>0) {
						tipoLancamento="D";
						valorLancamentoString=valorDebitoString;
					}
					else {
						tipoLancamento="";
					}
					if(tipoLancamento.length()>0)
					{
						Date dataLancamento = null;
						try {
							dataLancamento = sdf.parse(dataLancamentoString);
						} catch (ParseException e) {
							e.printStackTrace();
						}
						double valorLancamento=0.0;
						double valorSaldo=0.0;
						valorLancamento=Utils.extractValueFromBrazilianNumber(valorLancamentoString);
						valorSaldo=Utils.extractValueFromBrazilianNumber(saldoString);
//						System.out.println(dataLancamentoString+"|"+descricaoString+"|"+documentoString+"|"+tipoLancamento+"|"+valorLancamentoString+"|"+saldoString);
						LancamentoBradesco lancamentoBradesco = new LancamentoBradesco(dataLancamento, descricaoString, documentoString, tipoLancamento, valorLancamento, valorSaldo);
						lancamentosBradesco.add(lancamentoBradesco);
					}
				}
			}
			extratoBradesco=new ExtratoBradesco(ConnectorMariaDB.getConn(), ConnectorMSSQL.getConn(), empresa.getDadosBancariosEmpresa().getAgencia(), empresa.getDadosBancariosEmpresa().getConta(), empresa.getDadosBancariosEmpresa().getDigitoConta(), lancamentosBradesco);
			extratoBradesco.setEmpresa(empresa);
//			System.out.println("#########################################################");
//			System.out.println("## LANCAMENTOS READ FROM WEB EXTRACTO         ##");
//			extratoBradesco.getEmpresa().show();
//			extratoBradesco.getEmpresa().getDadosBancariosEmpresa().show();			
//			System.out.println("#########################################################");
//			for(LancamentoBradesco lancamentoBradesco:lancamentosBradesco)
//			{
//				System.out.println(lancamentoBradesco.getDataLancamento()
//						+"|"+lancamentoBradesco.getDescricao()
//						+"|"+lancamentoBradesco.getDocumento()
//						+"|"+lancamentoBradesco.getTipoLancamento()
//						+"|"+lancamentoBradesco.getValor()
//						+"|"+lancamentoBradesco.getSaldo()
//						);
//			}
//			System.out.println("#########################################################");			
//			System.out.println("#########################################################");
		}		
		return extratoBradesco;
	}
	
	public static void processExtrato(ExtratoBradesco extratoBradesco)
	{
		for(LancamentoBradesco lancamentoBradesco:extratoBradesco.getLancamentosBradesco())
		{
//			System.out.println("Processing lancamento: " + lancamentoBradesco.getDescricao());
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
//		System.out.println(query);
		Statement st=null;
		try {
			st=ConnectorMariaDB.getConn().createStatement();
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
			System.out.println("**********************************************************");
			System.out.println("* PAYMENT IDENTIFIED!!! *");
			CobrancaPIX.updatePago(ConnectorMariaDB.getConn(), ConnectorMSSQL.getConn(), idCobrancaPix, true);
			System.out.println("**********************************************************");
		}
	}
	
	public static void downloadExtrato( Empresa empresa)
	{
		System.out.println("CurrentURL: "+ExtratoDownloader.driver.getCurrentUrl());
		String control=ExtratoDownloader.driver.getCurrentUrl().split("=")[1];
		String urlExtrato="https://www.ne12.bradesconetempresa.b.br/ibpjsaldosextratos/extratoUltimosLancamentosCC.jsf?CTRL="+control;
		ExtratoDownloader.driver.switchTo().newWindow(WindowType.TAB);
		ExtratoDownloader.driver.get(urlExtrato);
		System.out.println("CurrentURL: "+ExtratoDownloader.driver.getCurrentUrl());
//		Utils.waitv(15);
//		List<WebElement> fiveDays = ExtratoDownloader.driver.findElements(By.xpath("//*[@id=\"formFiltroUltimosLancamentos:filtro:_id76\"]"));
//		if(!fiveDays.isEmpty())
//		{
//			fiveDays.get(0).click();
//			Utils.waitv(15);
//		}
		ExtratoBradesco extratoBradesco=	readExtrato(empresa);
		
		if (extratoBradesco==null)
		{
			ExtratoDownloader.driver.close();
		}
		
		System.out.println("Processing extrato for: " +" Agencia:  "+extratoBradesco.getEmpresa().getDadosBancariosEmpresa().getAgencia()
				 																			+" Conta: "+extratoBradesco.getEmpresa().getDadosBancariosEmpresa().getConta()
				 																			 +" Empresa: "+extratoBradesco.getEmpresa().getApelido());
		processExtrato(extratoBradesco);
		
//		Utils.waitv(5000);
//		List<WebElement> salvarComoArquivo = this.driver.findElements(By.xpath("/html/body[1]/div[2]/div[2]/div[2]/div/ul/li[2]/a"));
//		if(salvarComoArquivo.isEmpty())
//		{
//			salvarComoArquivo=this.driver.findElements(By.xpath("//*[@id=\"conteudo\"]/div[2]/div[2]/div/ul/li[2]/a"));
//			if(salvarComoArquivo.isEmpty())
//			{
//				System.out.println("Panic!! not found!");
//			}
//			else
//			{
//				salvarComoArquivo.get(0).click();
//			}
//		}
//		else {
//			salvarComoArquivo.get(0).click();	
//		}
//		if(salvarComoArquivo.isEmpty())
//		{
//			System.out.println("Not able to find Salvar como arquivo!");
//		}
		
		Utils.waitv(10);
		System.out.println("CurrentURL: "+ExtratoDownloader.driver.getCurrentUrl());		
	    ArrayList<String> tabs = new ArrayList<String> (driver.getWindowHandles());
//	    System.out.println("Switch to tab 0");
//	    this.driver.switchTo().window(tabs.get(0));
//	    Utils.waitv(4);
//	    System.out.println("Switch to tab 1");
//	    this.driver.switchTo().window(tabs.get(1));
//	    Utils.waitv(4);
//	    System.out.println("Switch to tab 2");
//	    this.driver.switchTo().window(tabs.get(2));
//	    Utils.waitv(4);
//	    WebElement downloadCSV=this.driver.findElement(By.id("formSalvarComo:cvs"));
//	    downloadCSV.click();
//	    Utils.waitv(5);
//	    this.driver.close();
//	    Utils.waitv(3);
	    ExtratoDownloader.driver.switchTo().window(tabs.get(1));
	    ExtratoDownloader.driver.close();
	    Utils.waitv(3);
	    ExtratoDownloader.driver.switchTo().window(tabs.get(0));	   
	    tabs = new ArrayList<String> (driver.getWindowHandles());
	    System.out.println("Number of tabs "+tabs.size());
	}
	
	public WebDriver getDriver() {
		return ExtratoDownloader.driver;
	}
	public void setDriver(WebDriver driver) {
		ExtratoDownloader.driver = driver;
	}
}
