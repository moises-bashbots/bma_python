package rgbsys;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Iterator;
import java.util.Set;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.openqa.selenium.By;
import org.openqa.selenium.Keys;

import utils.Utils;

public class RgbsysEnvioContaCorrente extends RgbsysSelenium {
	
	public RgbsysEnvioContaCorrente (RgbsysUser rgbsysUser)
	{
		super(rgbsysUser.getUserName(),rgbsysUser.getPassword(), Paths.get(rgbsysUser.getPathDownloads()), rgbsysUser.getRootURL());
	}

    public RgbsysEnvioContaCorrente(String userName, String password, Path downloadDir, String rootURL)
    {
        super(userName, password, downloadDir,rootURL);
    }

    
    
    void conferenciaEnvioContaCorrente(String apelidoEmpresa, String apelidoCedente, String dataFormatoBrasil)
    {
        Document doc = Jsoup.parse(this.driver.getPageSource());

        // getting apelido empresa com checkbox
        Element tableApelidoEmpresa = doc.getElementById("ctl00_contentManager_lookupempresa_DXDataRow0");
        String selectedApelidoEmpresa = tableApelidoEmpresa.select("td").last().text();
        assertStringContainsAnotherString(tableApelidoEmpresa.html(), "dxWeb_edtCheckBoxChecked_PlasticBlue dxICheckBox_PlasticBlue dxichSys", "Campo não está apropriadamente selecionado. Abortando processo.");
        assertStringEqualsAnotherString(selectedApelidoEmpresa, apelidoEmpresa, "Apelido empresa não bate. Abortando processo.");

        // getting apelido cedente com checkbox
        Element tableApelidocedente = doc.getElementById("ctl00_contentManager_lookupcedente_DXDataRow0");
        String selectedApelidoCedente = tableApelidocedente.select("td").last().text();
        assertStringContainsAnotherString(tableApelidocedente.html(), "dxWeb_edtCheckBoxChecked_PlasticBlue dxICheckBox_PlasticBlue dxichSys", "Campo não está apropriadamente selecionado. Abortando processo.");
        assertStringEqualsAnotherString(selectedApelidoCedente, apelidoCedente, "Apelido cedente não bate. Abortando processo.");

        // getting data
        String selectedData = doc.getElementById("ctl00_contentManager_dtcadastro1_I").attr("value").toString();
        assertStringEqualsAnotherString(selectedData, dataFormatoBrasil, "Data não bate. Abortando processo.");

        System.out.println("Checagens ok. Continuando o processo.");
    }

    void moveFromTempFolder(String nomeApelidoEmpresa, String nomeApelidoCedente, String dataFormatoBrasil)
    {
        try 
        {
            Date data = this.formatter.parse(dataFormatoBrasil);
            String dateString  = new SimpleDateFormat("yyyyMMdd").format(data);
            String fileName = "relatorio_envio_extrato_conta_corrente_" + nomeApelidoEmpresa + "_" + nomeApelidoCedente + "_" + dateString + ".pdf";
            Path downloadedFile  = Paths.get(getTempSavingFolder().toAbsolutePath().toString(), "RepExtratoCC.pdf");
            downloadedFile.toFile().renameTo(new File(Paths.get(getSavingFolder().toAbsolutePath().toString(), fileName).toString()));
        } 
        catch (ParseException e) 
        {
            e.printStackTrace();
        }
    }


   public  void envioContaCorrente(String apelidoEmpresa, String apelidoCedente, String dataFormatoBrasil)
    {
        this.driver.get(this.rootURL+"relfact/relatorios/FRelExtratoCC.aspx?SessionId=");
//            this.driver.get("https://gercloud2.rgbsys.com.br/BMA_HOMOLOGA/relfact/relatorios/FRelExtratoCC.aspx?SessionId=");
		Utils.waitv(10);
//            Thread.sleep(10000);

		System.out.println("Preenchimento do campo apelido empresa");
		this.driver.findElement(By.id("ctl00_contentManager_lookupempresa_DXFREditorcol1_I")).sendKeys(apelidoEmpresa);
//            Thread.sleep(10000);
		Utils.waitv(10);
		this.driver.findElement(By.id("ctl00_contentManager_lookupempresa_DXSelBtn0_D")).click();
//            Thread.sleep(5000);
		Utils.waitv(5);

		System.out.println("Preenchimento do campo apelido cedente");
		this.driver.findElement(By.id("ctl00_contentManager_lookupcedente_DXFREditorcol1_I")).sendKeys(apelidoCedente);
//            Thread.sleep(10000);
		Utils.waitv(10);            
		this.driver.findElement(By.id("ctl00_contentManager_lookupcedente_DXSelBtn0_D")).click();
//            Thread.sleep(5000);
		Utils.waitv(5);

		this.driver.findElement(By.id("ctl00_contentManager_dtcadastro1_I")).clear();
//            Thread.sleep(1000);
		Utils.waitv(2);
		this.driver.findElement(By.id("ctl00_contentManager_dtcadastro1_I")).sendKeys(Keys.DELETE);
		for (int i = 0; i < 8; i++)
		{
		    this.driver.findElement(By.id("ctl00_contentManager_dtcadastro1_I")).sendKeys(Keys.BACK_SPACE);
		};
		this.driver.findElement(By.id("ctl00_contentManager_dtcadastro1_I")).sendKeys(Keys.HOME);
//            Thread.sleep(2000);
		Utils.waitv(2);
		this.driver.findElement(By.id("ctl00_contentManager_dtcadastro1_I")).sendKeys(dataFormatoBrasil.replace("/", ""));
//            Thread.sleep(10000);
		Utils.waitv(8);
		this.driver.findElement(By.id("ctl00_contentManager_dtcadastro1_I")).sendKeys(Keys.TAB);
//            Thread.sleep(10000);
		Utils.waitv(10);

		// Realiza a conferencia antes de clicar em imprimir
		conferenciaEnvioContaCorrente(apelidoEmpresa, apelidoCedente, dataFormatoBrasil);

		System.out.println("Clica em imprimir");
		this.driver.findElement(By.id("ctl00_contentManager_btImpressaoImg")).click();
//            Thread.sleep(5000);
		Utils.waitv(5);

		System.out.println("Aternando para nova janela");
		String parentWindowHandler = driver.getWindowHandle(); // Store your parent window
		String subWindowHandler = null;
		Set<String> handles = driver.getWindowHandles(); // get all window handles
		Iterator<String> iterator = handles.iterator();
		while (iterator.hasNext())
		{
		    subWindowHandler = iterator.next();
		}
		driver.switchTo().window(subWindowHandler); // switch to popup window

		System.out.println("Esperando o download terminar");
		waitForFileToBeDownloaded();
		driver.close();
		System.out.println("Movendo para o folder final");
		moveFromTempFolder(apelidoEmpresa, apelidoCedente, dataFormatoBrasil);
		driver.switchTo().window(parentWindowHandler);
    }

    public static void main(String[] args) 
    {
        Path baseDir = Paths.get("/home/rafaelichow/Documents/bma_temp/envio_conta_corrente");
        String rootURL="https://gercloud2.rgbsys.com.br/BMA_HOMOLOGA/";
        
        RgbsysEnvioContaCorrente rgbSysSelenium = new RgbsysEnvioContaCorrente("MOISES", "moises", baseDir,rootURL);
        try
        { 
            String apelidoEmpresa = "BMA FIDC";
            String apelidoCedente = "MAXIBRASIL";
            String dataFormatoBRasil = "20/05/2022";
            // rgbSysSelenium.login();
            rgbSysSelenium.envioContaCorrente(apelidoEmpresa, apelidoCedente, dataFormatoBRasil);
        }
        catch (Exception e) {
            e.printStackTrace();
            rgbSysSelenium.saveListCriticaOperacoesRealizadasToDatabase();
        }
        finally
        {
            rgbSysSelenium.close();
        }
        
    }
}
