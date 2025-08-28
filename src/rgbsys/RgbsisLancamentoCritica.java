package rgbsys;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.openqa.selenium.By;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.Actions;

import conta_grafica.Critica;

public class RgbsisLancamentoCritica extends RgbsysSeleniumWebFactView {
    
    public RgbsisLancamentoCritica(String userName, String password)
    {
        super(userName, password);
        Critica.populateMapNomeCritica();
    }

    public RgbsisLancamentoCritica()
    {
        super();
        Critica.populateMapNomeCritica();
    }

    private void assertNomeCriticaInOptions(String nomeCritica)
    {
        if (!Critica.mapNomeCritica.containsKey(nomeCritica))
        {
            System.out.println("Nome critica nao esta contida no hasmap. Abortando processo");
            System.exit(1);
        }
    }

    private void detectNaoForamEncontradosTitulos(String baseUrl)
    {
        if (this.driver.getCurrentUrl() == baseUrl)
        {
            System.out.println("Pagina permance igual, portanto o mais provável é que não há títulos com esse id para esse cedente.");
            System.out.println("Abortando o processo.");
            System.exit(1);
        }
        System.out.println("Titulos encontrados. Prosseguindo");
    }

    private void assertApenasUmElemento()
    {

        Document doc = Jsoup.parse(this.driver.getPageSource());
        Element table = doc.select("#ctl00_contentManager_gridPrincipal_DXMainTable").first();
        Elements listRows = table.select("tr");

        Integer i = 0;
        for (Element row : listRows)
        {
            i++;
        }
        
        if (i != 2) // There should be only header and one row
        {
            System.out.println("Tabela não tem apenas dois elementos. Abortando processo");
        }
    }

    private void assertFormularioCorretamentePreenchido(String nomeCritica)
    {
    	System.out.println("Testing: '"+nomeCritica+"'");
    	// <td class="dxeListBoxItem_PlasticBlue" id="cbxCodigo_DDD_L_LBI32T0">114 - COB - CHEQUES POSTADOS</td>
        String codigoCritica = Integer.toString(Integer.parseInt(Critica.mapNomeCritica.get(nomeCritica).split(" ")[0]));
    	System.out.println("CodigoCritica: '"+codigoCritica+"'");
    	System.out.println("CodigoCritica: '"+Critica.mapNomeCriticaDescricao.get(nomeCritica)+"'");
        String stringCheck = "<input id=\"cbxCodigo_VI\" name=\"cbxCodigo_VI\" type=\"hidden\" value=\"" + codigoCritica +"\">";
        assertStringContainsAnotherString(this.driver.getPageSource(), stringCheck, "Código de crítica não foi apropriadamente selecionado. Abortando operação.");
    }

    private String getIdControleCriticaDropdown()
    {
        String stringReturn = "";
        String stringCheck = "Controle de Críticas";
        Document doc = Jsoup.parse(this.driver.getPageSource());
        Elements listElements = doc.getElementsByClass("dxm-content dxm-hasText");
        System.out.println("***********");
        for (Element element : listElements)
        {
            if (element.html().contains(stringCheck))
            {
                // System.out.println(element);
                stringReturn = element.attr("id").toString();
                break;
            }
        }
        // System.out.println(stringReturn);
        return stringReturn;
    }

    private String getIdGravaCritica()
    {
        String stringReturn = "";
        String stringCheck = "Grava Crítica";
        Document doc = Jsoup.parse(this.driver.getPageSource());
        Elements listElements = doc.getElementsByClass("dxm-content dxm-hasText");
        System.out.println("***********");
        for (Element element : listElements)
        {
            if (element.html().contains(stringCheck))
            {
                // System.out.println(element);
                stringReturn = element.attr("id").toString();
                break;
            }
        }
        // System.out.println(stringReturn);
        return stringReturn;
    }

    public void gravacaoCritica(String nomeCritica, String idDuplicata, String nomeEmpresa, String nomeCedente)
    {
        try
        {
            // Checando se o nome da critica está entre as opções
            assertNomeCriticaInOptions(nomeCritica);

            System.out.println("Iniciando processo de gravação de crítica.");
            System.out.println("Nome da crítica a ser gravada: " + nomeCritica);
            System.out.println("Nome correspondente da crítica a ser gravada no rgbsys: " + Critica.mapNomeCritica.get(nomeCritica));
            System.out.println("Id Duplicata: " +  idDuplicata);
            System.out.println("Nome empresa " + nomeEmpresa);
            System.out.println("Nome cedente " + nomeCedente);


//            String url = "https://gercloud2.rgbsys.com.br/BMA_HOMOLOGA/operacoes/forms/OcorrenciasManipulaFiltro.aspx?SessionId=";
            String url = RgbsysUser.rootURL+"/operacoes/forms/OcorrenciasManipulaFiltro.aspx?SessionId=";
            this.driver.get(url);
            Thread.sleep(12000);

            // ** Parte 1: pesquisando a duplicata em questao a ser gravada a critica.

            // Passa o nome da empresa
            this.driver.findElement(By.id("ctl00_contentManager_cbEmpresa_I")).click();
            Thread.sleep(1000);
            String command = "document.querySelector(\"#ctl00_contentManager_cbEmpresa_I\").value = \"" + nomeEmpresa + "\";";
            this.jsExecutor.executeScript(command);
            Thread.sleep(1000);
            this.driver.findElement(By.id("ctl00_contentManager_cbEmpresa_I")).sendKeys(Keys.TAB);
            Thread.sleep(1000);

            // Passa o nome do cedente
            // this.driver.findElement(By.id("ctl00_contentManager_cbCedente_I")).sendKeys(nomeCedente);
            this.driver.findElement(By.id("ctl00_contentManager_cbCedente_I")).click();
            Thread.sleep(1000);
            command = "document.querySelector(\"#ctl00_contentManager_cbCedente_I\").value = \"" + nomeCedente + "\";";
            this.jsExecutor.executeScript(command);
            Thread.sleep(1000);
            this.driver.findElement(By.id("ctl00_contentManager_cbCedente_I")).sendKeys(Keys.TAB);
            Thread.sleep(1000);

            // Manda a duplicata em questao
            this.driver.findElement(By.id("ctl00_contentManager_EdDuplicata_I")).clear();
            this.driver.findElement(By.id("ctl00_contentManager_EdDuplicata_I")).sendKeys(idDuplicata);
            Thread.sleep(1000);

            // Remove cheque do filtro
            this.driver.findElement(By.id("ctl00_contentManager_ckCheques_I")).click();
            Thread.sleep(1000);

            // filtra por situacao abertos
            this.driver.findElement(By.id("ctl00_contentManager_RgSituacao_0")).click();
            Thread.sleep(1000);

            // clica em procura similar
            this.driver.findElement(By.id("ctl00_contentManager_chkProcuraSimilar_I")).click();
            Thread.sleep(1000);

            // clica em "procura todos" - "Em qualquer parte"
            this.driver.findElement(By.id("ctl00_contentManager_chkProcuraTodos_I")).click();
            Thread.sleep(1000);

            // clica em pesquisar (lupa)
            this.driver.findElement(By.id("ctl00_contentManager_btnPesquisaImg")).click();
            Thread.sleep(10000);

            // Detectando se os titulos foram encontrados.
            detectNaoForamEncontradosTitulos(url);

            // ** Parte 2: gravando a critica.

            // Quando se entra na pagina, tem-se que ter certeza de que so h�� um elemento para tickar o checkbox
            assertApenasUmElemento();

            // Realizar checagem de se o elemento em questao �� o elemento desdejado.
            
            // Seciona primeiro elemento disponivel
            this.driver.findElement(By.id("ctl00_contentManager_gridPrincipal_DXSelBtn0_D")).click();
            Thread.sleep(10000);

            // Move mouse para drop down
            Actions action = new Actions(this.driver);
            String idControleCriticaDropDown = getIdControleCriticaDropdown();
            String idGravaCritica = getIdGravaCritica();
            WebElement elementDropDownControleCritica = this.driver.findElement(By.id(idControleCriticaDropDown));
            WebElement elementGravaCritica = this.driver.findElement(By.id(idGravaCritica));
            action.moveToElement(elementDropDownControleCritica).perform();
            Thread.sleep(1000);
            elementGravaCritica.click();
            Thread.sleep(1000);

            // Troca para frame
            String frameId = "ctl00_contentManager_popup_TODOS_CIF-1";
            this.driver.switchTo().frame(frameId);
            Thread.sleep(2000);

            // Manda codigo da critica
            this.driver.findElement(By.id("cbxCodigo_I")).click();
            System.out.println("Writing: " + Critica.mapNomeCritica.get(nomeCritica));
            this.driver.findElement(By.id("cbxCodigo_I")).sendKeys(Critica.mapNomeCritica.get(nomeCritica));
            Thread.sleep(2000);
            this.driver.findElement(By.id("cbxCodigo_I")).sendKeys(Keys.TAB);
            Thread.sleep(2000);

            // Manda observacao
            this.driver.findElement(By.name("txtObs")).sendKeys(Critica.mapNomeCriticaDescricao.get(nomeCritica));
            Thread.sleep(2000);

            // Checando se formulario foi bem preenchido
            assertFormularioCorretamentePreenchido(nomeCritica);

            // // Clica em OK
            this.driver.findElement(By.name("btnOK")).click();
            Thread.sleep(4000);
            pressSystemEnterKey();
            Thread.sleep(10000);

        }
        catch (InterruptedException ex) 
        {
            Thread.currentThread().interrupt();
        }
    }
    
    public void gravacaoCritica(Critica critica)
    {
        try
        {
            // Checando se o nome da critica est�� entre as op����es
            assertNomeCriticaInOptions(critica.getTipoCritica().getApelido());

            System.out.println("Iniciando processo de gravação de crítica.");
            System.out.println("Apelido da crítica a ser gravada: " + critica.getTipoCritica().getApelido());
            System.out.println("Nome da crítica a ser gravada: " + critica.getTipoCritica().getNomeCritica());
            System.out.println("Nome correspondente da crítica a ser gravada no rgbsys: " + critica.getTipoCritica().getDescricaoCritica());
            System.out.println("Identificacao Duplicata: " +  critica.getIdentificacaoDuplicata());
            System.out.println("Nome empresa " + critica.getEmpresa().getApelido());
            System.out.println("Nome cedente " + critica.getCedente().getApelido());


//            String url = "https://gercloud2.rgbsys.com.br/BMA_HOMOLOGA/operacoes/forms/OcorrenciasManipulaFiltro.aspx?SessionId=";
            String url = RgbsysUser.rootURL+"/operacoes/forms/OcorrenciasManipulaFiltro.aspx?SessionId=";
            this.driver.get(url);
            Thread.sleep(12000);

            // ** Parte 1: pesquisando a duplicata em questao a ser gravada a critica.

            // Passa o nome da empresa
            this.driver.findElement(By.id("ctl00_contentManager_cbEmpresa_I")).click();
            Thread.sleep(1000);
            String command = "document.querySelector(\"#ctl00_contentManager_cbEmpresa_I\").value = \"" + critica.getEmpresa().getApelido() + "\";";
            this.jsExecutor.executeScript(command);
            Thread.sleep(1000);
            this.driver.findElement(By.id("ctl00_contentManager_cbEmpresa_I")).sendKeys(Keys.TAB);
            Thread.sleep(1000);

            // Passa o nome do cedente
            // this.driver.findElement(By.id("ctl00_contentManager_cbCedente_I")).sendKeys(nomeCedente);
            this.driver.findElement(By.id("ctl00_contentManager_cbCedente_I")).click();
            Thread.sleep(1000);
            command = "document.querySelector(\"#ctl00_contentManager_cbCedente_I\").value = \"" + critica.getCedente().getApelido() + "\";";
            this.jsExecutor.executeScript(command);
            Thread.sleep(1000);
            this.driver.findElement(By.id("ctl00_contentManager_cbCedente_I")).sendKeys(Keys.TAB);
            Thread.sleep(1000);

            // Manda a duplicata em questao
            this.driver.findElement(By.id("ctl00_contentManager_EdDuplicata_I")).clear();
            this.driver.findElement(By.id("ctl00_contentManager_EdDuplicata_I")).sendKeys(critica.getIdentificacaoDuplicata());
            Thread.sleep(1000);

            // Remove cheque do filtro
            this.driver.findElement(By.id("ctl00_contentManager_ckCheques_I")).click();
            Thread.sleep(1000);

            // filtra por situacao abertos
            this.driver.findElement(By.id("ctl00_contentManager_RgSituacao_0")).click();
            Thread.sleep(1000);

            // clica em procura similar
            this.driver.findElement(By.id("ctl00_contentManager_chkProcuraSimilar_I")).click();
            Thread.sleep(1000);

            // clica em "procura todos" - "Em qualquer parte"
            this.driver.findElement(By.id("ctl00_contentManager_chkProcuraTodos_I")).click();
            Thread.sleep(1000);

            // clica em pesquisar (lupa)
            this.driver.findElement(By.id("ctl00_contentManager_btnPesquisaImg")).click();
            Thread.sleep(10000);

            // Detectando se os titulos foram encontrados.
            boolean noTitles=false;
            try {
            	detectNaoForamEncontradosTitulos(url);
            	noTitles=true;
			} catch (Exception e) {
				e.printStackTrace();
			}
            

            // ** Parte 2: gravando a critica.

            // Quando se entra na pagina, tem-se que ter certeza de que so h�� um elemento para tickar o checkbox
            assertApenasUmElemento();

            // Realizar checagem de se o elemento em questao �� o elemento desdejado.
            
            // Seciona primeiro elemento disponivel
            this.driver.findElement(By.id("ctl00_contentManager_gridPrincipal_DXSelBtn0_D")).click();
            Thread.sleep(10000);

            // Move mouse para drop down
            Actions action = new Actions(this.driver);
            String idControleCriticaDropDown = getIdControleCriticaDropdown();
            String idGravaCritica = getIdGravaCritica();
            WebElement elementDropDownControleCritica = this.driver.findElement(By.id(idControleCriticaDropDown));
            WebElement elementGravaCritica = this.driver.findElement(By.id(idGravaCritica));
            action.moveToElement(elementDropDownControleCritica).perform();
            Thread.sleep(1000);
            elementGravaCritica.click();
            Thread.sleep(1000);

            // Troca para frame
            String frameId = "ctl00_contentManager_popup_TODOS_CIF-1";
            this.driver.switchTo().frame(frameId);
            Thread.sleep(2000);

            // Manda codigo da critica
            this.driver.findElement(By.id("cbxCodigo_I")).click();
//            System.out.println("Writing: " + Critica.mapNomeCritica.get(critica.getTipoCritica().getApelido()));
            System.out.println("Writing: " + critica.getTipoCritica().getNomeCritica());
//            this.driver.findElement(By.id("cbxCodigo_I")).sendKeys(Critica.mapNomeCritica.get(critica.getTipoCritica().getApelido()));
            this.driver.findElement(By.id("cbxCodigo_I")).sendKeys(critica.getTipoCritica().getNomeCritica());
            Thread.sleep(2000);
            this.driver.findElement(By.id("cbxCodigo_I")).sendKeys(Keys.TAB);
            Thread.sleep(2000);

            // Manda observacao
//            this.driver.findElement(By.name("txtObs")).sendKeys(Critica.mapNomeCriticaDescricao.get(critica.getTipoCritica().getApelido()));
            this.driver.findElement(By.name("txtObs")).sendKeys(critica.getTipoCritica().getDescricaoCritica());
            Thread.sleep(2000);

            // Checando se formulario foi bem preenchido
            assertFormularioCorretamentePreenchido(critica.getTipoCritica().getApelido());

            // // Clica em OK
            this.driver.findElement(By.name("btnOK")).click();
            Thread.sleep(4000);
            pressSystemEnterKey();
            Thread.sleep(10000);

        }
        catch (InterruptedException ex) 
        {
            Thread.currentThread().interrupt();
        }
    }
    
    public static void main(String[] args) {

//        RgbsisLancamentoCritica rgbSysSelenium = new RgbsisLancamentoCritica("MOISES", "moises");
    	RgbsysUser.readConf();
        RgbsisLancamentoCritica rgbSysSelenium = new RgbsisLancamentoCritica();
        try
        { 
        	rgbSysSelenium.gravacaoCritica("ABATIMENTO","893213/1","BMA INTER","PIETROBON");
//        	 rgbSysSelenium.gravacaoCritica("ABATIMENTO","444154/003","HEANLU","BMA INTER");
//        	 rgbSysSelenium.gravacaoCritica("ABATIMENTO","0003921801","PARFUMS","BMA FIDC");
//        	 rgbSysSelenium.gravacaoCritica("ABATIMENTO","892597/1","PIETROBON","BMA INTER");
//        	 rgbSysSelenium.gravacaoCritica("ABATIMENTO","0008567705","UNIFORT","BMA INTER");
//        	 rgbSysSelenium.gravacaoCritica("ABATIMENTO","0008567706","UNIFORT","BMA INTER");
//        	 rgbSysSelenium.gravacaoCritica("ABATIMENTO","0008567704","UNIFORT","BMA INTER");
//        	 rgbSysSelenium.gravacaoCritica("ABATIMENTO","0009035603","UNIFORT","BMA INTER");
//        	 rgbSysSelenium.gravacaoCritica("ABATIMENTO","0114876603","UNIFORT","BMA INTER");
//        	 rgbSysSelenium.gravacaoCritica("ABATIMENTO","0114876602","UNIFORT","BMA INTER");
//        	 rgbSysSelenium.gravacaoCritica("ABATIMENTO","0000558201","UNIFORT","BMA INTER");
//        	 rgbSysSelenium.gravacaoCritica("ABATIMENTO","0005862001","UNIFORT","BMA INTER");
        	 
        	
            // rgbSysSelenium.login();
//        	rgbSysSelenium.gravacaoCritica("PRORROGACAO","883686/3","PIETROBON","BMA INTER");
//        	rgbSysSelenium.gravacaoCritica("PRORROGACAO","883686/2","PIETROBON","BMA INTER");
//        	rgbSysSelenium.gravacaoCritica("PRORROGACAO","883686/1","PIETROBON","BMA INTER");
//        	
//            rgbSysSelenium.gravacaoCritica( "PRORROGACAO", "188633/3", "BMA FIDC", "ENOVELAR");
//            rgbSysSelenium.gravacaoCritica( "PRORROGACAO", "188432/4", "BMA INTER", "ENOVELAR");
//            rgbSysSelenium.gravacaoCritica( "PRORROGACAO", "188432/3", "BMA INTER", "ENOVELAR");
//            rgbSysSelenium.gravacaoCritica( "PRORROGACAO", "188432/2", "BMA INTER", "ENOVELAR");
//            
//            rgbSysSelenium.gravacaoCritica( "PRORROGACAO", "187364/4", "BMA FIDC", "ENOVELAR");
//            rgbSysSelenium.gravacaoCritica( "PRORROGACAO", "187364/5", "BMA FIDC", "ENOVELAR");

//            rgbSysSelenium.gravacaoCritica("ABATIMENTO","890912/2","BMA INTER","PIETROBON");
//            rgbSysSelenium.gravacaoCritica("ABATIMENTO","0001118502","BMA INTER","UNIFORT");
//            rgbSysSelenium.gravacaoCritica("ABATIMENTO","0001508004","BMA INTER","UNIFORT");
//            rgbSysSelenium.gravacaoCritica("ABATIMENTO","0001508003","BMA INTER","UNIFORT");
//            rgbSysSelenium.gravacaoCritica("ABATIMENTO","0001508002","BMA INTER","UNIFORT");
//            rgbSysSelenium.gravacaoCritica("ABATIMENTO","0001508001","BMA INTER","UNIFORT");
//            rgbSysSelenium.gravacaoCritica("ABATIMENTO","875500/3","BMA INTER","PIETROBON");
//            rgbSysSelenium.gravacaoCritica("ABATIMENTO","0114858202","BMA INTER","UNIFORT");
//            rgbSysSelenium.gravacaoCritica("ABATIMENTO","0005862201","BMA INTER","UNIFORT");
//            rgbSysSelenium.gravacaoCritica("ABATIMENTO","0005862202","BMA INTER","UNIFORT");
//            rgbSysSelenium.gravacaoCritica("ABATIMENTO","0005862203","BMA INTER","UNIFORT");
//            rgbSysSelenium.gravacaoCritica("ABATIMENTO","0005862203","BMA INTER","UNIFORT");
//            rgbSysSelenium.gravacaoCritica("ABATIMENTO","0005862204","BMA INTER","UNIFORT");
//            rgbSysSelenium.gravacaoCritica("ABATIMENTO","0005862205","BMA INTER","UNIFORT");
//            rgbSysSelenium.gravacaoCritica("ABATIMENTO","884724/1","BMA INTER","PIETROBON");
//            rgbSysSelenium.gravacaoCritica("ABATIMENTO","0001101603","BMA INTER","UNIFORT");
//            rgbSysSelenium.gravacaoCritica("ABATIMENTO","0005865003","BMA INTER","UNIFORT");
//            rgbSysSelenium.gravacaoCritica("ABATIMENTO","0005865002","BMA INTER","UNIFORT");
//            rgbSysSelenium.gravacaoCritica("ABATIMENTO","0005865001","BMA INTER","UNIFORT");
//            rgbSysSelenium.gravacaoCritica("ABATIMENTO","0115039603","BMA INTER","UNIFORT");
//            rgbSysSelenium.gravacaoCritica("ABATIMENTO","0115039602","BMA INTER","UNIFORT");
//            rgbSysSelenium.gravacaoCritica("ABATIMENTO","0115039601","BMA INTER","UNIFORT");
//            rgbSysSelenium.gravacaoCritica("ABATIMENTO","0113612803","BMA INTER","UNIFORT");
//            rgbSysSelenium.gravacaoCritica("ABATIMENTO","0113805003","BMA INTER","UNIFORT");
//            rgbSysSelenium.gravacaoCritica("ABATIMENTO","0114307403","BMA INTER","UNIFORT");
//            rgbSysSelenium.gravacaoCritica("ABATIMENTO","0114307402","BMA INTER","UNIFORT");
//            rgbSysSelenium.gravacaoCritica("ABATIMENTO","0114375205","BMA INTER","UNIFORT");
//            rgbSysSelenium.gravacaoCritica("ABATIMENTO","0114375204","BMA INTER","UNIFORT");
//            rgbSysSelenium.gravacaoCritica("ABATIMENTO","0114375203","BMA INTER","UNIFORT");
//            rgbSysSelenium.gravacaoCritica("ABATIMENTO","0114283502","BMA INTER","UNIFORT");
//            rgbSysSelenium.gravacaoCritica("ABATIMENTO","0115023401","BMA INTER","UNIFORT");
//            rgbSysSelenium.gravacaoCritica("ABATIMENTO","0115023401","BMA INTER","UNIFORT");
//            rgbSysSelenium.gravacaoCritica("ABATIMENTO","0005720104","BMA INTER","UNIFORT");
//            rgbSysSelenium.gravacaoCritica("ABATIMENTO","0113823703","BMA INTER","UNIFORT");
//            rgbSysSelenium.gravacaoCritica("ABATIMENTO","0001118501","BMA INTER","UNIFORT");
//            rgbSysSelenium.gravacaoCritica("ABATIMENTO","0005739803","BMA INTER","UNIFORT");
//            rgbSysSelenium.gravacaoCritica("ABATIMENTO","0114858201","BMA INTER","UNIFORT");
//            rgbSysSelenium.gravacaoCritica("ABATIMENTO","0009053802","BMA INTER","UNIFORT");
//            rgbSysSelenium.gravacaoCritica("ABATIMENTO","0113779003","BMA INTER","UNIFORT");
//            rgbSysSelenium.gravacaoCritica("ABATIMENTO","0115043101","BMA INTER","UNIFORT");
//            rgbSysSelenium.gravacaoCritica("ABATIMENTO","0114288903","BMA INTER","UNIFORT");

            
        }
        finally
        {
//            rgbSysSelenium.close();
        }

    }
    
}
