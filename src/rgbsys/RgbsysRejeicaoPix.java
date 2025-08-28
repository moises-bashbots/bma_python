package rgbsys;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.openqa.selenium.By;

import conta_grafica.Critica;

public class RgbsysRejeicaoPix extends RgbsysSeleniumWebFactView {

    RgbsysRejeicaoPix(String userName, String password)
    {
        super(userName, password);
    }

    Boolean checkIfIsAfter21()
    {
        Boolean returnVal = false;
        Date date = new Date();
        SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm");
        dateFormat.format(date);

        System.out.println(dateFormat.format(date));

        try {
            // if (dateFormat.parse(dateFormat.format(date)).after(dateFormat.parse("21:00"))) 
            if (dateFormat.parse(dateFormat.format(date)).after(dateFormat.parse("8:00"))) 
            {
                System.out.println("Current time is greater than 21h");
                return true;
            } else {
                System.out.println("Current time is less than 21h");
                return false;
            }
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return returnVal;
    }

    void conferirRejeicaoRealizada()
    {

        HashMap<Integer, String> mapTableCols = getTableCols(this.driver.getPageSource());
        List<String> listRows = getListRows(this.driver.getPageSource());

        for (String stringRow : listRows)
        {
            Document doc = Jsoup.parse(this.driver.getPageSource());
            System.out.println(stringRow);
            Element table = doc.getElementById(stringRow);
            Elements listTableRows = table.select("tr");

            // HashMap<Integer, String> mapTableCols = getTableCols(this.driver.getPageSource());
            for (Element row : listTableRows)
            {

                Boolean isAbatimento = false;
                Boolean isApproved = true;
                Integer rowElementCounter = 0;
                Elements listRowDescriptions =  row.select("td");
                listRowDescriptions.remove(0);

                System.out.println("-------------------------");
                System.out.println(row);

                for (Element rowDescription : listRowDescriptions)
                {

                    if (mapTableCols.get(rowElementCounter).equals("Baixar"))
                    {
                        isAbatimento = true;
                    }

                    if (mapTableCols.get(rowElementCounter).equals("Apr"))
                    {

                        if (rowDescription.text().equals("S"))
                        {
                            isApproved = true;
                        }
                        if (rowDescription.text().equals("N"))
                        {
                            isApproved = false;
                        }
                    }

                    rowElementCounter++;

                    if (mapTableCols.get(rowElementCounter) == null) 
                    { break; }
                }

                if (isAbatimento && !isApproved)
                {
                    System.out.println("Trata-se de uma opera����o de abatimento e o flag \"Aprovado\" deveria estar marcado como \"Sim. Essa condi����o n��o deveria ser verdadeira.");
                    System.out.println("Abortando opera����o");
                    System.exit(1);
                }
            }
        }
    }

    // void rejeitaPixApos21h(List<String> listIdDuplicata)
    void rejeitaPixApos21h(String idDuplicata)
    {
        try 
        {
            String motivo = "Pagamento n��o efetuadao.";
            if (checkIfIsAfter21())
            {
            	int iPage=0;
                while (true)
                {
                    this.driver.get("https://gercloud2.rgbsys.com.br/BMA_HOMOLOGA/operacoes/forms/BaixaSolicitacaoInternet.aspx?SessionId=");
                    Thread.sleep(10000);

                    // Clica em pesquisar. Lista operacoes disponiveis
                    this.driver.findElement(By.id("ctl00_contentManager_btnBuscarImg")).click();
                    Thread.sleep(10000);

                    // Filtro abatimento
                    this.driver.findElement(By.id("ctl00_contentManager_gridLiberacao_DXFREditorcol1_I")).sendKeys("Baixar");
                    Thread.sleep(10000);

                    this.driver.findElement(By.id("ctl00_contentManager_gridLiberacao_DXFREditorcol5_I")).sendKeys(idDuplicata);
                    Thread.sleep(10000);

                    makeTableHigher();
                    Thread.sleep(10000);

                    HashMap<Integer, String> mapTableCols = getTableCols(this.driver.getPageSource());
                    List<String> listRows = getListRows(this.driver.getPageSource());

                    for (String stringRow : listRows)
                    {
                        Document doc = Jsoup.parse(this.driver.getPageSource());
                        System.out.println(stringRow);
                        Element table = doc.getElementById(stringRow);
                        Elements listTableRows = table.select("tr");

                        // HashMap<Integer, String> mapTableCols = getTableCols(this.driver.getPageSource());
                        for (Element row : listTableRows)
                        {

                            String idTitulo = "";
                            String nomeEmpresa = "";
                            String nomeCedente = "";
                            Boolean isApproved = true;
                            Integer rowElementCounter = 0;
                            Elements listRowDescriptions =  row.select("td");
                            listRowDescriptions.remove(0);

                            System.out.println("-------------------------");
                            System.out.println(row);

                            // if (!stringContainsItemFromList(row.html(), listIdDuplicata))
                            if (!row.html().contains(idDuplicata))
                            {
                                // Se nao contem a duplicata em questao, itera para a proxima
                                continue;
                            }

                            for (Element rowDescription : listRowDescriptions)
                            {

                                // Parsing idTitulo
                                if (mapTableCols.get(rowElementCounter).equals("Duplicata"))
                                {
                                    idTitulo = rowDescription.text();
                                    assertStringEqualsAnotherString(idDuplicata, idTitulo, "Variavel idDuplicata �� " + idDuplicata + " e vari��vel idTitulo �� " + idTitulo + " e portanto n��o batem. Abortando processo");
                                }

                                // Parsing nomeEmpresa
                                if (mapTableCols.get(rowElementCounter).equals("Empresa"))
                                {
                                    nomeEmpresa = rowDescription.text();
                                }

                                // Parsing nomeCedente
                                if (mapTableCols.get(rowElementCounter).equals("Cedente"))
                                {
                                    nomeCedente = rowDescription.text();
                                }

                                if (mapTableCols.get(rowElementCounter).equals("Apr"))
                                {
                                    if (rowDescription.text().equals("S"))
                                    {
                                        isApproved = true;
                                    }
                                    if (rowDescription.text().equals("N"))
                                    {
                                        isApproved = false;
                                    }
                                }

                                rowElementCounter++;

                                if (mapTableCols.get(rowElementCounter) == null) 
                                { break; }
                            }

                            if (!isApproved) // Conforme no manual
                            {
                                // Realizar prorrogacao
                                System.out.println("Raalizando rejeicao");
                                String clickId = row.select("input").first().id() + "_D";
                                assert(clickId.contains("ctl00_contentManager_gridLiberacao_DXSelBtn")) : "Id inconsistente";
                                System.out.println("Clicando no elemento" +  clickId);
                                this.driver.findElement(By.id(clickId)).click();
                                Thread.sleep(10000);

                                // Clica em aprovar
                                this.driver.findElement(By.id("ctl00_contentManager_btnAprova")).click();
                                Thread.sleep(10000);

                                // // CHECK FOR SYSTEM POPUP
                                // pressSystemEnterKey();
                                // Thread.sleep(10000);

                                
                                // makeTableHigher();
                                // Thread.sleep(10000);

                                Critica critica = new Critica(
                                    idTitulo,
                                    nomeCedente,
                                    nomeEmpresa,
                                    "ABATIMENTO"
                                );
                                this.getListCritica().add(critica);
                            }

                            // Clica em rejeitar
                            System.out.println("Clicando em rejeitar");
                            this.driver.findElement(By.id("ctl00_contentManager_btnRejeita_CD")).click();
                            Thread.sleep(10000);

                            // Escreve o motivo
                            System.out.println("Escrevendo o motivo");
                            this.driver.findElement(By.id("ctl00_contentManager_txtMotivo_I")).sendKeys(motivo);
                            Thread.sleep(10000);

                            // Clica em OK
                            System.out.println("Clicando em ok");
                            this.driver.findElement(By.id("ctl00_contentManager_btnMotivo_CD")).click();
                            Thread.sleep(10000);
                        }

                    }
                    iPage++;
                    if (checkIfIsLastPage(this.driver.getPageSource()))
                    {
                        System.out.println("Driver esta na pagina final. Terminando o processo.");
                        break;
                    }
                    else
                    {
                        gotoNextPage(iPage);
                    }

                }

                saveListCriticaOperacoesRealizadasToDatabase();
            }
        }
        catch (InterruptedException ex) 
        {
            Thread.currentThread().interrupt();
        }
    }

    public static void main(String[] args) {

        RgbsysRejeicaoPix rgbSysSelenium = new RgbsysRejeicaoPix("MOISES", "moises");
        try
        { 
            // rgbSysSelenium.login();
            rgbSysSelenium.rejeitaPixApos21h("118480NE4");
        }
        catch (Exception e) {
            rgbSysSelenium.saveListCriticaOperacoesRealizadasToDatabase();
        }
        finally
        {
            rgbSysSelenium.driver.close();
        }
    }
}
