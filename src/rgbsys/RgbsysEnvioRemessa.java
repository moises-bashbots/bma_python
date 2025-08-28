package rgbsys;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.openqa.selenium.By;

import cedente.Cedente;
import conta_grafica.Critica;
import conta_grafica.TituloInstrucao;
import empresa.Empresa;
import utils.Utils;

public class RgbsysEnvioRemessa extends RgbsysSeleniumWebFactView {

    public RgbsysEnvioRemessa(String userName, String password)
    {
        super(userName, password);
    }

    public RgbsysEnvioRemessa()
    {
        super();
    }

    
    void conferirAbatimentoRealizado()
    {
        HashMap<Integer, String> mapTableCols = getTableCols(this.driver.getPageSource());
        List<String> listRows = getListRows(this.driver.getPageSource());

        for (String stringRow : listRows)
        {
            Document doc = Jsoup.parse(this.driver.getPageSource());
            // System.out.println(stringRow);
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

                // System.out.println("-------------------------");
                // System.out.println(row);

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
                    System.out.println("Trata-se de uma operação de abatimento e o flag \"Aprovado\" deveria estar marcado como \"Sim. Essa condição não deveria ser verdadeira.");
                    System.out.println("Abortando operação");
                    System.exit(1);
                }
            }
        }
    }

   public ArrayList<Critica> abatimento(Connection connMaria, Connection connMSS)
    {
        String stringReturn = "";
	    this.getRgbsysUser();
		this.driver.get(RgbsysUser.getRootURL()+"/operacoes/forms/BaixaSolicitacaoInternet.aspx?SessionId=");
//            Thread.sleep(10000);
	    Utils.waitv("Openning link to main window",10);

	    // Clica em pesquisar. Lista operacoes disponiveis
	    this.driver.findElement(By.id("ctl00_contentManager_btnBuscarImg")).click();
//            Thread.sleep(10000);
	    Utils.waitv("Click on Pesquisar",10);

	    // Filtro abatimento
	    this.driver.findElement(By.id("ctl00_contentManager_gridLiberacao_DXFREditorcol1_I")).sendKeys("Abater");
//            Thread.sleep(10000);
	    Utils.waitv("Filtro abatimento",10);
//	    makeTableHigher();
//      Thread.sleep(10000);
//	    Utils.waitv(10);
	    int currentPage=0;
        while (true)
		{
//    	    makeTableHigher();
//    	    Utils.waitv(10);
//                this.driver.get("https://gercloud2.rgbsys.com.br/BMA_HOMOLOGA/operacoes/forms/BaixaSolicitacaoInternet.aspx?SessionId=");
		    HashMap<Integer, String> mapTableCols = getTableCols(this.driver.getPageSource());
		    System.out.println("Cols list: ");
		    for(int i:mapTableCols.keySet())
		    {
		    	System.out.println(i+": "+mapTableCols.get(i));
		    }
		    List<String> listRows = getListRows(this.driver.getPageSource());
		    while(listRows.size()>0)
		    {
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
	
			                // Parsing status
			                if (mapTableCols.get(rowElementCounter).equals("Apr"))
			                {
			                    if (rowDescription.text().toLowerCase().contains("s"))
			                    {
			                        isApproved = true;
			                    }
			                    if (rowDescription.text().toLowerCase().contains("n"))
			                    {
			                        isApproved = false;
			                    }
			                }
	
			                rowElementCounter++;
	
			                if (mapTableCols.get(rowElementCounter) == null) 
			                { break; }
			            }
			            
			            System.out.println("Duplicata: " + idTitulo);
			            System.out.println("Empresa: "+ nomeEmpresa);
			            System.out.println("Cedente: "+nomeCedente);
			            System.out.println("Aprovacao: "+isApproved);
			            Empresa empresa = new Empresa(connMaria, connMSS, nomeEmpresa);
			            Cedente cedente = new Cedente(connMaria, connMSS, empresa, nomeCedente);
			            TituloInstrucao tituloInstrucao = new TituloInstrucao(empresa, cedente, idTitulo); 
			            boolean forbidden=tituloInstrucao.checkProdutoProibido(connMSS); 
			       
		                // Realizar prorrogacao
		                System.out.println("Realizando abatimento");
		                String clickId = row.select("input").first().id() + "_D";

		                assertStringContainsAnotherString(clickId, "ctl00_contentManager_gridLiberacao_DXSelBtn", "Id do elemento nao encontrado na página. Abortando o processo.");

		                if(!forbidden)
		                {
	//                            Thread.sleep(3000);
			                Utils.waitv(3);
			                System.out.println("Clicando no elemento" +  clickId);
			                this.driver.findElement(By.id(clickId)).click();
	//                            Thread.sleep(10000);
			                Utils.waitv(10);
	
			                if(!isApproved)
			                {
				                // Clica em aprovar
				                System.out.println("clicando em aprovar");
				                this.driver.findElement(By.id("ctl00_contentManager_btnAprova")).click();
		//                            Thread.sleep(10000);
				                Utils.waitv(10);
			                }
	
			                // Clica em efetua
			                 System.out.println("Clicando em efetua");
			                 this.driver.findElement(By.id("ctl00_contentManager_btnEfetua_CD")).click();
			                Utils.waitv("Clica em efetua",10);
	
			                // // Apos clicar em efetua, um novo pop up ira abrir. Tem que trocar para o novo frame.
			                 System.out.println("Clicando no novo popup");
			                 this.driver.switchTo().frame("ctl00_contentManager_popup_TODOS_CIF-1");
			                 Utils.waitv("Clicando no novo poput",10);
			                // Thread.sleep(10000);
	
			                // // Clica em OK
			                System.out.println("Apertando ok do sistema.");
			                 this.driver.findElement(By.id("btnOK_CD")).click();
			                 Utils.waitv("Apertando OK do sistema",10);
			                // Thread.sleep(10000);
	
			                // // CHECK FOR SYSTEM POPUP
			                 for (int i = 0; i < 5; i++)
			                 {
			                     pressSystemEnterKey();
				                 Utils.waitv("Enter!",4);
			                 }
	
			                // Salvando critica
			                Critica critica = new Critica(connMaria,connMSS,
			                    idTitulo,
			                    nomeCedente,
			                    nomeEmpresa,
			                    "ABATIMENTO"
			                );
			                this.getListCritica().add(critica);
			                System.out.println("Adding critica to the list");
			                System.out.println("Number of criticas: " +this.getListCritica().size());
	//		                makeTableHigher();
	////                            Thread.sleep(10000);
	//		                Utils.waitv(10);
		                }
		                else 
		                {
							System.out.println("Produto " + tituloInstrucao.getNomeProduto().toUpperCase()+ " proibido para esta instrução!");
							System.out.println("Rejeitando ABATIMENTO");
						    clickId = row.select("input").first().id() + "_D";
						    assert(clickId.contains("ctl00_contentManager_gridLiberacao_DXSelBtn")) : "Id inconsistente";
						    System.out.println("Clicando no elemento" +  clickId);
						    this.driver.findElement(By.id(clickId)).click();
						    Utils.waitv(10);
							this.driver.findElement(By.id("ctl00_contentManager_btnRejeita_CD")).click();
							Utils.waitv(4);
							this.driver.switchTo().frame("ctl00_contentManager_popup_TODOS_CIF-1");
							System.out.println("Produto proibido!");
							this.driver.findElement(By.id("ctl00_contentManager_txtMotivo_I")).sendKeys("Produto " + tituloInstrucao.getNomeProduto().toUpperCase()+ " proibido para esta instrução!");							
							Utils.waitv(2);
							this.driver.findElement(By.id("ctl00_contentManager_btnMotivo_CD")).click();
							Utils.waitv(4);
						}
			        }
			        break;
		        }
			    listRows = getListRows(this.driver.getPageSource());
//			    System.exit(1);
		    }
		    
		    currentPage++;
		    System.out.println("Pagina atual "+ currentPage);
		    if (checkIfIsLastPage(this.driver.getPageSource()))
		    {
		        System.out.println("Driver esta na pagina final. Terminando o processo.");
		        break;
		    }
		    else
		    {
		        gotoNextPage(currentPage);
		    }
		}

		// Conferindo se as alteracoes foram ok
//		conferirAbatimentoRealizado();

		// Realizando gravacao de criticas
		saveListCriticaOperacoesRealizadasToDatabase();
		this.driver.close();

//		RgbsisLancamentoCritica rgbsisLancamentoCritica = new RgbsisLancamentoCritica();
//		rgbsisLancamentoCritica.login();
//		for(Critica critica:this.listCritica)
//		{
//			// rgbSysSelenium.gravacaoCritica( "PRORROGACAO", "116748NE1", "BMA FIDC", "MAXIBRASIL");
//			rgbsisLancamentoCritica.gravacaoCritica(critica.getNomeCritica(), critica.getIdDuplicata(), critica.getNomeEmpresa(), critica.getNomeCedente());
//		}
        return this.getListCritica();
    }

    public static void main(String[] args) {

        RgbsysEnvioRemessa rgbSysSelenium = new RgbsysEnvioRemessa("MOISES", "moises");
        try
        { 
//            rgbSysSelenium.abatimento();
        }
        catch (Exception e) {
            System.out.println(e.getMessage());
            rgbSysSelenium.saveListCriticaOperacoesRealizadasToDatabase();
        }
        finally
        {
            rgbSysSelenium.close();
        }
    }
}
