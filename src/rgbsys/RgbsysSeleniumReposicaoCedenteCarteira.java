package rgbsys;


import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.openqa.selenium.By;
import org.openqa.selenium.Keys;

import utils.Utils; 


public class RgbsysSeleniumReposicaoCedenteCarteira extends RgbsysSelenium {

   public RgbsysSeleniumReposicaoCedenteCarteira(String userName, String password)
    {
        super(userName, password);
    }
    
//    public RgbsysSeleniumReposicaoCedenteCarteira(RgbsysUser userRgb)
//    {
//        super(userRgb);
//    }

    public RgbsysSeleniumReposicaoCedenteCarteira()
    {
        super();
    }

    private String formatValor(Double valor)
    {
        return String.valueOf(valor).replace(".", ",");
    }

    private void assertPix()
    {

        Document doc = Jsoup.parse(this.driver.getPageSource());
        Element table = doc.getElementById("ctl00_contentManager_rblFormaPagamento_RB4");
        assertStringContainsAnotherString(table.html(), ">PIX</label", "Campo pix nao encontrado. Abortando operacao.");
    }

    // void reposicaoCedenteCarteira(String nomeCedente, String nomeContaCorrente, String dataBrasilString, String carteira, String valor)
    public void reposicaoCedenteCarteira(String nomeCedente, String nomeContaCorrente, String dataBrasilString, Double valor)
    {
        try
        {
            // Vai para pagina
//            this.driver.get("https://gercloud2.rgbsys.com.br/BMA_HOMOLOGA/operacoes/forms/ReposicaoCedenteEmpresa.aspx?SessionId=");
            
            this.driver.get(this.rootURL+"/operacoes/forms/ReposicaoCedenteEmpresa.aspx?SessionId=");
            Thread.sleep(4000);

            // Clica em "Novo"
            this.driver.findElement(By.id("ctl00_contentManager_btnIncluir")).click();
            Thread.sleep(4000);
            assertPix();

            // Seleciona PIX
            this.driver.findElement(By.id("ctl00_contentManager_rblFormaPagamento_RB4_I")).click();;
            Thread.sleep(4000);

            this.driver.findElement(By.id("ctl00_contentManager_txtData_I")).sendKeys(Keys.DELETE);
            for (int i = 0; i < 10; i++)
            {
                this.driver.findElement(By.id("ctl00_contentManager_txtData_I")).sendKeys(Keys.BACK_SPACE);
            };
            this.driver.findElement(By.id("ctl00_contentManager_txtData_I")).sendKeys(Keys.HOME);
            robotTypeString(dataBrasilString.replace("/", ""));
            Thread.sleep(2000);
            this.driver.findElement(By.id("ctl00_contentManager_txtData_I")).sendKeys(Keys.ENTER);
            Thread.sleep(2000);
            assertStringContainsAnotherString(this.driver.getPageSource(), dataBrasilString,  "Data não encontrada na página. Abortando operação.");

            // Seleciona conta corrente
            this.driver.findElement(By.id("ctl00_contentManager_cbContaCorrente_I")).sendKeys(nomeContaCorrente);
            Thread.sleep(2000);
            this.driver.findElement(By.id("ctl00_contentManager_cbContaCorrente_I")).sendKeys(Keys.ENTER);
            Thread.sleep(1000);
            assertStringContainsAnotherString(this.driver.getPageSource(), nomeContaCorrente, "Nome do cedente não encontrado na página. Abortando operação.");

            // Coloca descricao - "historico"
            String descricaoHistorico = "Pix - Conta corrente - bot";
            this.driver.findElement(By.id("ctl00_contentManager_txtHistorico_I")).sendKeys(descricaoHistorico);
            Thread.sleep(2000);
            this.driver.findElement(By.id("ctl00_contentManager_txtHistorico_I")).sendKeys(Keys.ENTER);
            Thread.sleep(2000);
            assertStringContainsAnotherString(this.driver.getPageSource(), descricaoHistorico, "Descricao historico nao contido na página. Abortando operação.");

            // Seleciona nome do cedente
            this.driver.findElement(By.id("ctl00_contentManager_cbCedente_I")).sendKeys(nomeCedente);
            Thread.sleep(1000);
            this.driver.findElement(By.id("ctl00_contentManager_cbCedente_I")).sendKeys(Keys.ENTER);
            Thread.sleep(3000);
            assertStringContainsAnotherString(this.driver.getPageSource(), nomeCedente, "Nome do cedente não encontrado na página. Abortando operação.");
            System.out.println("Valor utilizado antes: "+ this.driver.findElement(By.id("ctl00_contentManager_txtvalor_I")).getText());
            Utils.waitv("Long before writing value",5);
            // Selencionando valor
            this.driver.findElement(By.id("ctl00_contentManager_txtvalor_I")).click();
            Utils.waitv("Clicked",3);
            for(int i=0;i<30; i++)
            {
            	this.driver.findElement(By.id("ctl00_contentManager_txtvalor_I")).sendKeys(Keys.DELETE);
            }
            this.driver.findElement(By.id("ctl00_contentManager_txtvalor_I")).sendKeys(Keys.LEFT);
        	this.driver.findElement(By.id("ctl00_contentManager_txtvalor_I")).sendKeys(Keys.LEFT);
        	this.driver.findElement(By.id("ctl00_contentManager_txtvalor_I")).sendKeys(Keys.LEFT);
//            this.driver.findElement(By.id("ctl00_contentManager_txtvalor_I")).clear();
            Utils.waitv("Clear",3);
            this.driver.findElement(By.id("ctl00_contentManager_txtvalor_I")).sendKeys(formatValor(valor));
            System.out.println("Valor inserido: " + formatValor(valor));
//            Thread.sleep(10000);
//            Utils.waitv(10);
//            System.out.println("Valor utilizado: "+ this.driver.findElement(By.id("ctl00_contentManager_txtvalor_I")).getText());
//            Thread.sleep(2000);
            Utils.waitv(10);
//            System.exit(1);
            assertStringContainsAnotherString(this.driver.getPageSource(), formatValor(valor), "Valor não encontrado na página. Abortando operação.");
//            Thread.sleep(1000);
            Utils.waitv(10);


            System.out.println("Repetindo checagens");
            assertPix();
            assertStringContainsAnotherString(this.driver.getPageSource(), dataBrasilString,  "Data não encontrada na página. Abortando operação.");
            assertStringContainsAnotherString(this.driver.getPageSource(), formatValor(valor), "Valor não encontrado na página. Abortando operação.");
            assertStringContainsAnotherString(this.driver.getPageSource(), nomeContaCorrente, "Nome do cedente não encontrado na página. Abortando operação.");
            assertStringContainsAnotherString(this.driver.getPageSource(), descricaoHistorico, "Descricao historico nao contido na página. Abortando operação.");
            assertStringContainsAnotherString(this.driver.getPageSource(), nomeCedente, "Nome do cedente não encontrado na página. Abortando operação.");
            System.out.println("Checagem ok. Prosseguindo");
            // performAssertionnBeforeContinuing(nomeCedente, nomeContaCorrente, dataBrasilString, valor);

            // Clica em gravar 
            this.driver.findElement(By.id("ctl00_contentManager_btnAlterar_CD")).click();
//          
            Utils.waitv(10);
//            Thread.sleep(10000);

            pressSystemEnterKey();
            Utils.waitv(10);
//            Thread.sleep(1000);
        }
        catch (InterruptedException ex) 
        {
            Thread.currentThread().interrupt();
        }
    }

    public static void main(String[] args) {

        RgbsysSeleniumReposicaoCedenteCarteira rgbSysSelenium = new RgbsysSeleniumReposicaoCedenteCarteira("MOISES", "moises");
        try
        { 
            rgbSysSelenium.login();
            rgbSysSelenium.reposicaoCedenteCarteira(
                "BASTON",
                "BRADESCO INTER",
                "20/06/2022",
                3743.19
            );
        }
        finally
        {
            // rgbSysSelenium.driver.close();
        }
    }

}
