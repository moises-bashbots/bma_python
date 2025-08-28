package rgbsys;

import java.util.HashMap;
import java.util.List;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.openqa.selenium.By;

import conta_grafica.Critica;

public class RgbsysBaixaAprovacao extends RgbsysSeleniumWebFactView {
    
    RgbsysBaixaAprovacao(String userName, String password)
    {
        super(userName, password);
    }

    void conferirBaixaRealizada()
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

                Boolean isBaixar = false;
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
                        isBaixar = true;
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

                if (isBaixar && !isApproved)
                {
                    System.out.println("Trata-se de uma opera����o de Baixa e o flag \"Aprovado\" deveria estar marcado como \"Sim. Essa condi����o n��o deveria ser verdadeira.");
                    System.out.println("Abortando opera����o");
                    System.exit(1);
                }
            }
        }
    }

    String baixaAprovacao()
    {
        // Baixa solicitacao WebFact
        String stringReturn = "";

        try 
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

                // Filtro Apr = Nao
                this.driver.findElement(By.id("ctl00$contentManager$gridLiberacao$DXFREditorcol8")).sendKeys("N");
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

                        for (Element rowDescription : listRowDescriptions)
                        {

                            // Parsing idTitulo
                            if (mapTableCols.get(rowElementCounter).equals("Duplicata"))
                            {
                                idTitulo = rowDescription.text();
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
                            System.out.println("Raalizando aprovacao");
                            String clickId = row.select("input").first().id() + "_D";

                            assertStringContainsAnotherString(clickId, "ctl00_contentManager_gridLiberacao_DXSelBtn", "Id do elemento nao encontrado na página. Abortando o processo.");

                            // Clicando no elemento
                            System.out.println("Clicando no elemento" +  clickId);
                            this.driver.findElement(By.id(clickId)).click();
                            Thread.sleep(10000);

                            // Clica em aprovar
                            this.driver.findElement(By.id("ctl00_contentManager_btnAprova")).click();
                            Thread.sleep(10000);

                            // Salva Critica
                            Critica critica = new Critica(
                                idTitulo,
                                nomeCedente,
                                nomeEmpresa,
                                "APROVACAO"
                            );
                            this.getListCritica().add(critica);

                            makeTableHigher();
                            Thread.sleep(10000);
                        }
                    }
                }

                conferirBaixaRealizada();
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
        catch (InterruptedException ex) 
        {
            Thread.currentThread().interrupt();
        }


        return stringReturn;

    }


    public static void main(String[] args) {

        RgbsysBaixaAprovacao rgbSysSelenium = new RgbsysBaixaAprovacao("MOISES", "moises");
        try
        { 
            rgbSysSelenium.login();
            rgbSysSelenium.baixaAprovacao();
            
        }
        catch (Exception e) 
        {
            System.out.println(e.getMessage());
            rgbSysSelenium.saveListCriticaOperacoesRealizadasToDatabase();
        }
        finally
        {
            rgbSysSelenium.driver.close();

        }

    }

}
