package rgbsys;

import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.openqa.selenium.By;

public class RgbsysScrapeCadastroCedentes extends RgbsisSeleniumCadastroGerenciaView {

    String savingPath;

    public String getSavingPath() {
        return savingPath;
    }

    public void setSavingPath(String savingPath) {
        this.savingPath = savingPath;
    }

    RgbsysScrapeCadastroCedentes(String userName, String password)
    {
        super(userName, password);
    }

    public void saveToPath(String cedenteCpfCnpj)
    {
        try
        {
            String pageSource = this.driver.getPageSource();
            
            // Checando se a página está como esperado
            String errorMessage = "Página não está estruturada conforme esperado. Abortando processo";
            assertStringContainsAnotherString(pageSource.toUpperCase(), "APELIDO", errorMessage);
            assertStringContainsAnotherString(pageSource.toUpperCase(), "NOME", errorMessage);
            assertStringContainsAnotherString(pageSource.toUpperCase(), "CIDADE", errorMessage);
            assertStringContainsAnotherString(pageSource, cedenteCpfCnpj, "Nao salvando para cedente " + cedenteCpfCnpj + " pois a pagina nao contem o cpf/cnpj na assinatura. Abortando operacao");

            // Salvando página
            Path filePath = Paths.get(this.savingPath, "cadastro_cedentes", cedenteCpfCnpj + ".html");
            createNewFileIfNotExists(filePath);
            FileWriter writer = new FileWriter(filePath.toString());
            writer.write(pageSource);
            writer.close();
        } 
        catch (IOException e) 
        {
            System.out.println("An error occurred.");
            e.printStackTrace();
        }
    }

    String scrapeCadastroCedente()
    {
        String stringReturn = "";

        try 
        {
            this.driver.get("https://gercloud2.rgbsys.com.br/BMA_HOMOLOGA/cadastros/forms/CedenteCadastro.aspx?SessionId=");
            Thread.sleep(10000);

            while (true)
            {

                List<String> listRows = getListRows(this.driver.getPageSource());

                for (String stringRow : listRows)
                {
                    Document doc = Jsoup.parse(this.driver.getPageSource());
                    System.out.println(stringRow);
                    Element table = doc.getElementById(stringRow);
                    Elements listTableRows = table.select("tr");

                    for (Element row : listTableRows)
                    {
                        String cedenteCpfCnpj = getCpfCnpjFromRow(row.text());
                        String idLinkDetails = stringRow.replace("DXDataRow", "cell").replace("#", "") + "_2_btnGoto";

                        System.out.println(cedenteCpfCnpj);
                        System.out.println(idLinkDetails);
                        this.driver.findElement(By.id(idLinkDetails)).click();;
                        Thread.sleep(5000);

                        // save to file path
                        saveToPath(cedenteCpfCnpj);

                        this.driver.navigate().back();
                        Thread.sleep(5000);
                    }
                }

                if (checkIfIsLastPage(this.driver.getPageSource()))
                {
                    System.out.println("Driver esta na pagina final. Terminando o processo.");
                    break;
                }
                else
                {
                    gotoNextPage();
                }

            }
        }
        catch (InterruptedException ex) 
        {
            Thread.currentThread().interrupt();
        }

        return stringReturn;
    }

    public static void main(String[] args) {

        RgbsysScrapeCadastroCedentes rgbSysSelenium = new RgbsysScrapeCadastroCedentes("MOISES", "moises");
        try
        { 
            rgbSysSelenium.setSavingPath("/home/rafaelichow/Documents/bma_temp");
            // rgbSysSelenium.login();
            rgbSysSelenium.scrapeCadastroCedente();
            
        }
        finally
        {
            rgbSysSelenium.driver.close();
        }
    }
}
