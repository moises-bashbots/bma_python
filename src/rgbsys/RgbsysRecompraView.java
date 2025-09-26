package rgbsys;

import java.nio.file.Path;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.openqa.selenium.By;
import org.openqa.selenium.Dimension;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.Select;

import conta_grafica.OperacaoRecompra;
import conta_grafica.TituloRecompra;
import mssql.ConnectorMSSQL;
import mysql.ConnectorMariaDB;
import utils.Utils;

public class RgbsysRecompraView extends RgbsysSelenium {
    
    private String savingPath;
    private String nomeEmpresa;
    private String nomeCedente;
    private String nomeContaBancaria;
    private Double valorMoraOriginalCedenteView;
    private Double valorTarifaMoraCedenteView;
    private Double valorMoraNovoCedenteView;
    public OperacaoRecompra operacaoRecompra=new OperacaoRecompra();

	private static SimpleDateFormat sdfH=new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	private static SimpleDateFormat sdfh=new SimpleDateFormat("HHmmss");
    private static SimpleDateFormat sdfnr=new SimpleDateFormat("ddMMyyyy");
    private static SimpleDateFormat sdfr=new SimpleDateFormat("dd/MM/yyyy");

    public RgbsysRecompraView(String userName, String password)
    {
        super(userName, password);
    }

    public RgbsysRecompraView(String userName, String password, Path downloadDir, String rootURL)
    {
        super(userName, password, downloadDir, rootURL);
    }

    public RgbsysRecompraView(String userName, String password, String savingPath, String nomeEmpresa, String nomeCedente, String nomeContaBancaria) {
        super(userName, password);
        this.savingPath = savingPath;
        this.nomeEmpresa = nomeEmpresa;
        this.nomeCedente = nomeCedente;
        this.nomeContaBancaria = nomeContaBancaria;
    }
    
    public RgbsysRecompraView()
    {
    	super();
    }
    
    public RgbsysRecompraView(OperacaoRecompra operacaoRecompra)
    {
    	super();
    	this.operacaoRecompra=operacaoRecompra;
    	this.nomeEmpresa=operacaoRecompra.getEmpresa().getApelido();
    	this.nomeCedente=operacaoRecompra.getCedente().getApelido();
    	this.nomeContaBancaria=operacaoRecompra.getEmpresa().getDadosBancariosEmpresa().getApelido();
    }

    public String getNomeContaBancaria() {
        return this.nomeContaBancaria;
    }

    public void setNomeContaBancaria(String nomeContaBancaria) {
        this.nomeContaBancaria = nomeContaBancaria;
    }

    public String getSavingPath() {
        return this.savingPath;
    }

    public void setSavingPath(String savingPath) {
        this.savingPath = savingPath;
    }

    public String getNomeEmpresa() {
        return this.nomeEmpresa;
    }

    public void setNomeEmpresa(String nomeEmpresa) {
        this.nomeEmpresa = nomeEmpresa;
    }

    public String getNomeCedente() {
        return this.nomeCedente;
    }

    public void setNomeCedente(String nomeCedente) {
        this.nomeCedente = nomeCedente;
    }

    public RgbsysRecompraView savingPath(String savingPath) {
        setSavingPath(savingPath);
        return this;
    }

    public RgbsysRecompraView nomeEmpresa(String nomeEmpresa) {
        setNomeEmpresa(nomeEmpresa);
        return this;
    }

    public RgbsysRecompraView nomeCedente(String nomeCedente) {
        setNomeCedente(nomeCedente);
        return this;
    }

    public Double getValorTarifaMoraCedenteView() {
        return this.valorTarifaMoraCedenteView;
    }

    public Double getValorMoraOriginalCedenteView() {
        return this.valorMoraOriginalCedenteView;
    }

    public void setValorMoraOriginalCedenteView(Double valorMoraOriginalCedenteView) {
        this.valorMoraOriginalCedenteView = valorMoraOriginalCedenteView;
    }

    public void setValorMoraOriginalCedenteView(WebDriver driver) {

        Document doc = Jsoup.parse(this.driver.getPageSource());
        this.valorMoraOriginalCedenteView = parseValor(doc.getElementById("txtValorMora_Raw").attr("value").toString());
    }

    public Double getValorMoraNovoCedenteView() {
        return this.valorMoraNovoCedenteView;
    }

    public void setValorMoraNovoCedenteView(Double valorMoraNovoCedenteView) {
        this.valorMoraNovoCedenteView = valorMoraNovoCedenteView;
    }

    
    
    private Boolean checkIfCheckBoxIsEnabled()
    {
        Boolean returnValue = false;
        
        Document doc = Jsoup.parse(this.driver.getPageSource());
        Element table = doc.getElementById("ctl00_contentManager_ASPxPageControlRenegociacao_ckmulta_S_D");

        // String buttonIsChecked = "dxICheckBox_PlasticBlue dxichSys   dxWeb_edtCheckBoxChecked_PlasticBlue";
        // String buttonIsChecked = "dxICheckBox_PlasticBlue dxichSys\" id=\"ctl00_contentManager_ASPxPageControlRenegociacao_ckmulta_S_D\">";
        String buttonIsChecked = "dxWeb_edtCheckBoxChecked_PlasticBlue";
        String buttonNotChecked = "dxICheckBox_PlasticBlue dxichSys    dxWeb_edtCheckBoxUnchecked_PlasticBlue";

        if (table.attr("class").toString().contains(buttonIsChecked))
        {
            returnValue = true;
        }

        return returnValue;
    }

    private String formatValor(Double valor)
    {
        return String.valueOf(valor).replace(".", ",");
    }

    private Double parseValor(String valor)
    {
        return Double.parseDouble(valor.replace(",", "."));
    }

    void setValorTarifaMoraCedenteView(WebDriver driver)
    {
        Document doc = Jsoup.parse(this.driver.getPageSource());
        this.valorTarifaMoraCedenteView = parseValor(doc.getElementById("EdMoraTitulos_I").attr("value").toString());
        System.out.println("Valor tarifa mora: " + this.valorTarifaMoraCedenteView);
    }

    void checkIfVariablesArePopulated()
    {
        // Must be overriden in each one of the child processes
    }


    void addToListCritica(String idDuplicataQuitar)
    {
        // Must be overriden in each one of the child processes
    }

    private Double getValorTarifaCedenteFromDB()
    {
        // TODO implement
        return 10.0;
    }

    private void assertCheckboxMarked(int rowCounter)
    {
        String rowNumber = String.valueOf(rowCounter + 1);
        if (rowNumber.length() == 1)
        {
            rowNumber = "0" + rowNumber;
        }

        String command = "return document.querySelector('[name=\"ctl00$contentManager$ASPxPageControlRenegociacao$gridtitulos$ctl" + rowNumber +  "$MarcaDesmarca\"]').checked;";
        System.out.println(command);
        Boolean returnValue = (Boolean) this.jsExecutor.executeScript(command);
        assertStringEqualsAnotherString(String.valueOf(returnValue), "true", "Checkbox n��o est�� apropriadamente clicada. Abortando processo.");
    }

    private void assertTarifaMoraCedenteMatchesDB()
    {
        Double valorTarifaCedente = 0.0;
        if (this.valorTarifaMoraCedenteView != valorTarifaCedente)
        {
            System.out.println("valor tarifa nao bate! Implmentar checagem");
        }
    }

    private void assertValorMoraCorrectlyUpdated(Boolean isVencido, int rowCounter)
    {

        Document doc = Jsoup.parse(this.driver.getPageSource());

        int counterCol = 0;
        Element table  = doc.getElementById("gridtitulos");
        Elements tableHeader = table.select("tr").first().select("th");
        for (Element elem : tableHeader)
        {
            if (elem.html().contains("Mora"))
            {
                break;

            }
            counterCol++;
        }


        System.out.println(table.select("tr").get(rowCounter).select("td").get(counterCol).html());
        String tempValorPage = table.select("tr").get(rowCounter).select("td").get(counterCol).text();
        System.out.println("Novo valor mora contido na pagina: " + tempValorPage);
        Double valorMoraPage = parseValor(tempValorPage);

        if (isVencido)
        {
            System.out.println("getValorMoraNovoCedenteView " + getValorMoraNovoCedenteView());
            System.out.println("valorMoraPage " + valorMoraPage);
            Double novoValor = getValorTarifaMoraCedenteView() + getValorMoraOriginalCedenteView();
            System.out.println("novoValor " + novoValor);
            assertStringEqualsAnotherString(String.valueOf(getValorMoraNovoCedenteView()), String.valueOf(novoValor), "Teste 1: Valores de mora antigo e novo nao batem. Abortando o processo.");
            assertStringEqualsAnotherString(String.valueOf(valorMoraPage), String.valueOf(novoValor), "Teste 2: Valores de mora antigo e novo nao batem. Abortando o processo.");
            assertStringEqualsAnotherString(String.valueOf(getValorMoraNovoCedenteView()), String.valueOf(novoValor), "Teste 3: Valores de mora antigo e novo nao batem. Abortando o processo.");
        }
        // else
        // {
        //     assertStringEqualsAnotherString(String.valueOf(getValorMoraOriginalCedenteView()), String.valueOf(getValorMoraNovoCedenteView()), "Teste 4: Valores de mora antigo e novo nao batem. Abortando o processo.");
        //     assertStringEqualsAnotherString(String.valueOf(valorMoraPage), String.valueOf(getValorMoraNovoCedenteView()), "Teste 5: Valores de mora antigo e novo nao batem. Abortando o processo.");
        // }

    }

    public boolean trocarValorMora(TituloRecompra tituloRecompra)
    {
        Date vctoTitulo =  new Date(1900, 1, 1);
		Date novoVcto =  new Date(2022, 1, 1);
		Date currentDate = new Date();

		Boolean rowSelected = false;
		Boolean boolAplicarMora = false;
		Document doc = Jsoup.parse(this.driver.getPageSource());

		Element table  = doc.getElementById("gridtitulos");
		boolean titleFound=false;
		if(table!=null)
		{
			Elements listRows  = table.select("tr");
	
			Elements tableHeader = table.select("tr").first().select("th");
			titleFound=false;
			for (int rowCounter = 0; rowCounter < listRows.size(); rowCounter++)
			{
			    rowSelected = false;
			    Element row  = listRows.get(rowCounter);
			    Elements listDescriptions = row.select("td");
			    
			    for (int colCounter = 0; colCounter < listDescriptions.size(); colCounter++)
			    {
			        Element description = listDescriptions.get(colCounter);
			        if (row.html().contains(tituloRecompra.getIdentificacaoTitulo()))
			        {
			            // System.out.println("ROW COUNTER " + rowCounter);
			            // System.out.println(tableHeader.get(colCounter).html());
			            // System.out.println(description.html());
			        	titleFound=true;
	
			            if (description.html().contains("MarcaDesmarca"))
			            {
			                rowSelected = true;
	
			                // Realizando checkbox da operacao de recompra
	//                            System.out.println("Selecionando checkbox de operacao de recompra.");
			                Utils.waitv("Selecionando checkbox de operacao de recompra.",4);
	//                            Thread.sleep(4000);
	
			                // System.out.println(row.html());
			                // System.out.println(row.select("input").attr("name").toString()); 
	
			                String localSelectName = row.select("input").attr("name").toString();
			                WebElement localElement = this.driver.findElement(By.name(localSelectName));
	
			                // Scrolling into element
			                this.jsExecutor.executeScript("arguments[0].scrollIntoView(true);", localElement);
			                Utils.waitv("Scroll até o título",4);
	//                            Thread.sleep(4000);
	
			                // Clicking 
			                localElement.click();
			                Utils.waitv("Click no título",4);
	//                            Thread.sleep(4000);
			                continue;
			            }
	
	//                        if(tituloRecompra.isVencido())
	//                        {
	//                        	
	//                        }
	//                        if (tableHeader.get(colCounter).html().contains("Vencimento")) 
	//                        {
	////                            System.out.println("No vencimento");
	//                            System.out.println(description.html());
	//
	//                            vctoTitulo = (Date) this.formatter.parse(description.text());
	////                            System.out.println("vencimento titulo");
	//                            System.out.println(vctoTitulo);
	////                            System.out.println("data atual");
	//                            System.out.println(currentDate);
	//
	//                            long durationDays = getDifferenceDays(currentDate, vctoTitulo);
	////                            System.out.println("Difference in days " + String.valueOf(durationDays));
	//
	//                            if (durationDays <= 0)
	//                            {
	//                                System.out.println("Data de vencimento do t��tulo �� hoje ou t��tulo est�� vencido. Aplicando mora.");
	//                                boolAplicarMora = true;
	//                            }
	//
	//                            continue;
	//                        }
	
	//		            if (description.html().contains("Alterar Mora") && tituloRecompra.isVencido())
	//		            {
	//
	//		                System.out.println("Aplicando mora para titulo: " + tituloRecompra.getIdentificacaoTitulo());
	//		                Utils.waitv("Applying mora",4);
	////                            Thread.sleep(4000);
	//
	//		                // System.out.println("***********");
	//		                // System.out.println(description.html());
	//
	//		                String selectName = description.select("input").attr("name").toString();
	//		                // System.out.println("***********");
	//		                // System.out.println(selectName);
	//		                // System.out.println("***********");
	//		                WebElement element = this.driver.findElement(By.name(selectName));
	//
	//		                // Scrolling into element
	//		                this.jsExecutor.executeScript("arguments[0].scrollIntoView(true);", element);
	//		                Utils.waitv("Scroll no título",4);
	////                            Thread.sleep(4000);
	//
	//		                // Clicking 
	//		                element.click();
	//		                Utils.waitv("Click no elemento",4);
	////                            Thread.sleep(4000);
	//
	//		                // Obtendo valor anterior
	//		                setValorMoraOriginalCedenteView(this.driver);
	//
	//		                // Calculando novo valor mora
	//		                Double novoValor = getValorTarifaMoraCedenteView() + getValorMoraOriginalCedenteView();
	//		                setValorMoraNovoCedenteView(novoValor);
	//		                
	//		                novoValor=tituloRecompra.getMora();
	//		                System.out.println("Valor de mora para o título: "+novoValor);
	//
	//		                // Alterando o valor
	//		                this.driver.findElement(By.id("txtValorMora_I")).clear();
	//		                Utils.waitv("Clear valor mora",2);
	////                            Thread.sleep(2000);
	//		                this.driver.findElement(By.id("txtValorMora_I")).sendKeys(Keys.DELETE);
	//		                for (int z = 0; z < 10; z++)
	//		                {
	//		                    this.driver.findElement(By.id("txtValorMora_I")).sendKeys(Keys.BACK_SPACE);
	//		                };
	//		                this.driver.findElement(By.id("txtValorMora_I")).sendKeys(Keys.HOME);
	//		                Utils.waitv("Key home",1);
	////                            Thread.sleep(2000);
	//		                this.driver.findElement(By.id("txtValorMora_I")).sendKeys(formatValor(novoValor));
	//		                Utils.waitv("Escrevendo novo valor",3);
	////                            Thread.sleep(3000);
	//
	//		                // Confirmando alteracao
	//		                String command = "document.querySelector(\"#divModalMora > div > div > div.modal-footer > div > div > button.btn.btn-info\").click();";
	//		                this.jsExecutor.executeScript(command);
	//		                Utils.waitv("Confirmando alteracao",3);
	////                            Thread.sleep(3000);
	//
	//		                continue;
	//		            }
			        }
			    }
//			    if(!titleFound)
//			    {
			    	tituloRecompra.setBaixado(true);		    	
//			    	tituloRecompra.updateBaixado(ConnectorMariaDB.conn);
//			    }
			    
	//		    if (rowSelected) // A linha em si foi processada
	//		    {
	//		        // Assert checkbox checado
	//		        assertCheckboxMarked(rowCounter);
	//
	//		        // Assertion confirmando que os valores de mora foram atualizzados
	//		        assertValorMoraCorrectlyUpdated(boolAplicarMora, rowCounter);
	//
	//		    }
			}
		}
		return titleFound;
    }

    
    void trocarValorMora(String idDuplicataQuitar)
    {
        try
        {
            Date vctoTitulo =  new Date(1900, 1, 1);
            Date novoVcto =  new Date(2022, 1, 1);
            Date currentDate = new Date();

            Boolean rowSelected = false;
            Boolean boolAplicarMora = false;
            Document doc = Jsoup.parse(this.driver.getPageSource());

            Element table  = doc.getElementById("gridtitulos");
            Elements listRows  = table.select("tr");

            Elements tableHeader = table.select("tr").first().select("th");

            for (int rowCounter = 0; rowCounter < listRows.size(); rowCounter++)
            {
                rowSelected = false;
                Element row  = listRows.get(rowCounter);
                Elements listDescriptions = row.select("td");
                
                for (int colCounter = 0; colCounter < listDescriptions.size(); colCounter++)
                {
                    Element description = listDescriptions.get(colCounter);
                    if (row.html().contains(idDuplicataQuitar))
                    {
                        // System.out.println("ROW COUNTER " + rowCounter);
                        // System.out.println(tableHeader.get(colCounter).html());
                        // System.out.println(description.html());


                        if (description.html().contains("MarcaDesmarca"))
                        {
                            rowSelected = true;

                            // Realizando checkbox da operacao de recompra
                            System.out.println("Selecionando checkbox de operacao de recompra.");
                            Thread.sleep(4000);

                            // System.out.println(row.html());
                            // System.out.println(row.select("input").attr("name").toString()); 

                            String localSelectName = row.select("input").attr("name").toString();
                            WebElement localElement = this.driver.findElement(By.name(localSelectName));

                            // Scrolling into element
                            this.jsExecutor.executeScript("arguments[0].scrollIntoView(true);", localElement);
                            Thread.sleep(4000);

                            // Clicking 
                            localElement.click();;
                            Thread.sleep(4000);

                            continue;
                        }

                        if (tableHeader.get(colCounter).html().contains("Vencimento")) 
                        {
                            System.out.println("No vencimento");
                            System.out.println(description.html());

                            vctoTitulo = (Date) this.formatter.parse(description.text());
                            System.out.println("vencimento titulo");
                            System.out.println(vctoTitulo);
                            System.out.println("data atual");
                            System.out.println(currentDate);

                            long durationDays = getDifferenceDays(currentDate, vctoTitulo);
                            System.out.println("Difference in days " + String.valueOf(durationDays));

                            if (durationDays <= 0)
                            {
                                System.out.println("Data de vencimento do t��tulo �� hoje ou t��tulo est�� vencido. Aplicando mora.");
                                boolAplicarMora = true;
                            }

                            continue;
                        }

                        if (description.html().contains("Alterar Mora") && boolAplicarMora)
                        {

                            System.out.println("Aplicando mora para titulo: " + idDuplicataQuitar);
                            Thread.sleep(4000);



                            // System.out.println("***********");
                            // System.out.println(description.html());

                            String selectName = description.select("input").attr("name").toString();
                            // System.out.println("***********");
                            // System.out.println(selectName);
                            // System.out.println("***********");
                            WebElement element = this.driver.findElement(By.name(selectName));

                            // Scrolling into element
                            this.jsExecutor.executeScript("arguments[0].scrollIntoView(true);", element);
                            Thread.sleep(4000);

                            // Clicking 
                            element.click();;
                            Thread.sleep(4000);

                            // Obtendo valor anterior
                            setValorMoraOriginalCedenteView(this.driver);

                            // Calculando novo valor mora
                            Double novoValor = getValorTarifaMoraCedenteView() + getValorMoraOriginalCedenteView();
                            setValorMoraNovoCedenteView(novoValor);

                            // Alterando o valor
                            this.driver.findElement(By.id("txtValorMora_I")).clear();
                            Thread.sleep(2000);
                            this.driver.findElement(By.id("txtValorMora_I")).sendKeys(Keys.DELETE);
                            for (int z = 0; z < 10; z++)
                            {
                                this.driver.findElement(By.id("txtValorMora_I")).sendKeys(Keys.BACK_SPACE);
                            };
                            this.driver.findElement(By.id("txtValorMora_I")).sendKeys(Keys.HOME);
                            Thread.sleep(2000);
                            this.driver.findElement(By.id("txtValorMora_I")).sendKeys(formatValor(novoValor));
                            Thread.sleep(3000);

                            // Confirmando alteracao
                            String command = "document.querySelector(\"#divModalMora > div > div > div.modal-footer > div > div > button.btn.btn-info\").click();";
                            this.jsExecutor.executeScript(command);
                            Thread.sleep(3000);

                            continue;
                        }
                    }
                }
                
                if (rowSelected) // A linha em si foi processada
                {
                    // Assert checkbox checado
                    assertCheckboxMarked(rowCounter);

                    // Assertion confirmando que os valores de mora foram atualizzados
                    assertValorMoraCorrectlyUpdated(boolAplicarMora, rowCounter);

                }
            }
        }
        catch (InterruptedException ex) 
        {
            Thread.currentThread().interrupt();
        } catch (ParseException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    public void quitacaoBaixa(HashMap<String, String> cedentesSemTarifaBaixa)
    {
    	this.operacaoRecompra.setupVencimentos();
        String processUrl = "https://gercloud2.rgbsys.com.br/GER_BMA/operacoes/forms/RenegociacaoBorderoPesquisaNova.aspx?origem=2";
        System.out.println("****************************************************");
        System.out.println("* TENTANDO BAIXAR OPERACAO RECOMPRA  *");
        this.operacaoRecompra.showShort();
        this.operacaoRecompra.getEmpresa().show();
        this.operacaoRecompra.getEmpresa().getDadosBancariosEmpresa().show();
        System.out.println("****************************************************");
    	ArrayList<String> listIdDuplicatasQuitar = new ArrayList<>();
    	boolean allBaixado=true;
    	for(TituloRecompra tituloRecompra:this.operacaoRecompra.getTitulosRecompra())
    	{
    		tituloRecompra.checkTitulo(ConnectorMSSQL.conn, ConnectorMariaDB.conn);
    		if(tituloRecompra.isBaixado())
    		{
    			System.out.println("Check if tituloRecompra: "+tituloRecompra.getIdentificacaoTitulo()+" is baixado "+tituloRecompra.isBaixado());
    			tituloRecompra.updateBaixadoFromRGB(ConnectorMariaDB.conn);
    		}
    		else
    		{
    			allBaixado=false;
    		}
    		listIdDuplicatasQuitar.add(tituloRecompra.getIdentificacaoTitulo());
    		tituloRecompra.showShort();
    	}
    	if(allBaixado)
    	{
    		this.operacaoRecompra.setBaixado(allBaixado);
    		this.operacaoRecompra.updateHorarioBaixadoFromRGB(ConnectorMariaDB.conn);
    		return;
    	}
    	
        checkIfVariablesArePopulated();

        Dimension dimension = new Dimension(1600, 1700);
//        this.driver.manage().window().maximize();
        this.driver.manage().window().setSize(dimension);

        this.driver.get(processUrl);
        Utils.waitv(3);
        
        // Clica em pesquisar. Lista operacoes disponiveis
        this.driver.findElement(By.id("ctl00_contentManager_ASPxButtonNovaRecompra_CD")).click();
        Utils.waitv(5);
//        this.driver.findElement(By.id("ctl00_contentManager_ASPxPageControlRenegociacao_EdData_I")).clear();
//        this.driver.findElement(By.id("ctl00_contentManager_ASPxPageControlRenegociacao_EdData_I")).sendKeys(Keys.END);
//        for (int i = 0; i < 10; i++)
//        {
//        	this.driver.findElement(By.id("ctl00_contentManager_ASPxPageControlRenegociacao_EdData_I")).sendKeys(Keys.BACK_SPACE);
//        };
//        Utils.sendHumanKeys(this.driver.findElement(By.id("ctl00_contentManager_ASPxPageControlRenegociacao_EdData_I")), sdfnr.format(this.operacaoRecompra.getDataRecompra()));
//        Utils.waitv(500);
        // Seleciona nome da empresa
        this.driver.findElement(By.id("ctl00_contentManager_ASPxPageControlRenegociacao_cbempresa_I")).clear();
        Utils.waitv(1);
        this.driver.findElement(By.id("ctl00_contentManager_ASPxPageControlRenegociacao_cbempresa_I")).sendKeys(Keys.DELETE);
        for (int i = 0; i < 10; i++)
        {
            this.driver.findElement(By.id("ctl00_contentManager_ASPxPageControlRenegociacao_cbempresa_I")).sendKeys(Keys.BACK_SPACE);
        };
        this.driver.findElement(By.id("ctl00_contentManager_ASPxPageControlRenegociacao_cbempresa_I")).sendKeys(this.nomeEmpresa);
        Utils.waitv(1);
        this.driver.findElement(By.id("ctl00_contentManager_ASPxPageControlRenegociacao_cbempresa_I")).sendKeys(Keys.TAB);
        Utils.waitv(7);
        
        
       
//        Utils.waitv(200);
        
//        
//        
//        
//        while (tarifaBaixaCNABCobrado)
//        {
//	        System.out.println("Tarifa baixa CNAB: " +this.driver.findElement(By.id("ctl00_contentManager_ASPxPageControlRenegociacao_cktarbaixa_S")).getAttribute("value"));
//	        if(this.driver.findElement(By.id("ctl00_contentManager_ASPxPageControlRenegociacao_cktarbaixa_S")).getAttribute("value").toLowerCase().contains("u"))
//	        {
//	        	this.driver.findElement(By.id("ctl00_contentManager_ASPxPageControlRenegociacao_cktarbaixa_S_D")).click();
//	        }
//	        else {
//	        	tarifaBaixaCNABCobrado=false;
//			}
//        	Utils.waitv(2);
//        }
//        
//        
//        while(tarifaBaixaFIDCCobrado)
//        {
//	        System.out.println("Tarifa baixa FIDC: "+this.driver.findElement(By.id("ctl00_contentManager_ASPxPageControlRenegociacao_cktarbaixa_fidc_S")).getAttribute("value"));
//	        if(this.driver.findElement(By.id("ctl00_contentManager_ASPxPageControlRenegociacao_cktarbaixa_fidc_S")).getAttribute("value").toLowerCase().contains("u"))
//	        {
//		        this.driver.findElement(By.id("ctl00_contentManager_ASPxPageControlRenegociacao_cktarbaixa_fidc_S_D")).click();
//	        }
//	        else {
//				tarifaBaixaFIDCCobrado=false;
//			}
//	        Utils.waitv(2);
//        }
        // Pop up de recompra termorario pode aparecer
        pressSystemEscKey();
        Utils.waitv(8);

        // Seleciona nome do cedente
        this.driver.findElement(By.id("ctl00_contentManager_ASPxPageControlRenegociacao_cbcedente_I")).sendKeys(this.nomeCedente);
        Utils.waitv(2);
        this.driver.findElement(By.id("ctl00_contentManager_ASPxPageControlRenegociacao_cbcedente_I")).sendKeys(Keys.TAB);
        Utils.waitv(10);

        // Pop up de recompra termporario pode aparecer
        pressSystemEscKey();
        Utils.waitv(2);
        pressSystemEscKey();
        Utils.waitv(6);

        // Arruma data de vencimento
        this.driver.findElement(By.id("ctl00_contentManager_ASPxPageControlRenegociacao_EdVencimentoInicial_I")).clear();
        this.driver.findElement(By.id("ctl00_contentManager_ASPxPageControlRenegociacao_EdVencimentoInicial_I")).sendKeys(Keys.DELETE);
        for (int i = 0; i < 10; i++)
        {
            this.driver.findElement(By.id("ctl00_contentManager_ASPxPageControlRenegociacao_EdVencimentoInicial_I")).sendKeys(Keys.BACK_SPACE);
        };
        this.driver.findElement(By.id("ctl00_contentManager_ASPxPageControlRenegociacao_EdVencimentoInicial_I")).sendKeys(sdfnr.format(this.operacaoRecompra.getInicioVencimento()));   
        this.driver.findElement(By.id("ctl00_contentManager_ASPxPageControlRenegociacao_EdVencimento_I")).clear();
        
        Utils.waitv(1);
        this.driver.findElement(By.id("ctl00_contentManager_ASPxPageControlRenegociacao_EdVencimento_I")).sendKeys(Keys.DELETE);
        for (int i = 0; i < 10; i++)
        {
            this.driver.findElement(By.id("ctl00_contentManager_ASPxPageControlRenegociacao_EdVencimento_I")).sendKeys(Keys.BACK_SPACE);
        };
        this.driver.findElement(By.id("ctl00_contentManager_ASPxPageControlRenegociacao_EdVencimento_I")).sendKeys(sdfnr.format(this.operacaoRecompra.getFinalVencimento()));
        Utils.waitv(1);
        this.driver.findElement(By.id("ctl00_contentManager_ASPxPageControlRenegociacao_EdVencimento_I")).sendKeys(Keys.ENTER);
        Utils.waitv("After data de vencimento",7);
        
        
        boolean tarifaBaixaCNABCobrado=false;
        boolean tarifaBaixaFIDCCobrado=true;
        
        
        if(cedentesSemTarifaBaixa.get(this.operacaoRecompra.getCedente().getApelido().toUpperCase())!=null)
        {
        	tarifaBaixaCNABCobrado=false;
        	tarifaBaixaFIDCCobrado=false;
        }
        else
        {
        	tarifaBaixaCNABCobrado=false;
        	tarifaBaixaFIDCCobrado=true;
        }
       
        if(!tarifaBaixaCNABCobrado)
        {
	        if(this.driver.findElement(By.id("ctl00_contentManager_ASPxPageControlRenegociacao_cktarbaixa_S")).getDomAttribute("value").contains("c"))
	        {
	        	this.driver.findElement(By.id("ctl00_contentManager_ASPxPageControlRenegociacao_cktarbaixa_S")).click();	        	
	        }
	        System.out.println("TarifaBaixaCNAB: "+tarifaBaixaCNABCobrado);
        }
        else {
        	if(this.driver.findElement(By.id("ctl00_contentManager_ASPxPageControlRenegociacao_cktarbaixa_S")).getDomAttribute("value").contains("u"))
	        {
	        	this.driver.findElement(By.id("ctl00_contentManager_ASPxPageControlRenegociacao_cktarbaixa_S")).click();
	        }
        	Utils.waitv("TarifaBaixaCNAB: "+tarifaBaixaCNABCobrado,2);
		}
       
        if(!tarifaBaixaFIDCCobrado)
        {
	        if(this.driver.findElement(By.id("ctl00_contentManager_ASPxPageControlRenegociacao_cktarbaixa_fidc_S")).getDomAttribute("value").contains("c"))
	        {
	        	this.driver.findElement(By.id("ctl00_contentManager_ASPxPageControlRenegociacao_cktarbaixa_fidc_S")).click();
	        }
	        System.out.println("TarifaBaixaFIDC: "+tarifaBaixaCNABCobrado);
        }
        else {
        	if(this.driver.findElement(By.id("ctl00_contentManager_ASPxPageControlRenegociacao_cktarbaixa_fidc_S")).getDomAttribute("value").contains("u"))
	        {
	        	this.driver.findElement(By.id("ctl00_contentManager_ASPxPageControlRenegociacao_cktarbaixa_fidc_S")).click();	        
	        }
        	Utils.waitv("TarifaBaixaFIDC: "+tarifaBaixaFIDCCobrado,2);
		}

        // // Pop up de recompra termorario pode aparecer
        // pressSystemEscKey();
        // pressSystemEscKey();
        // Thread.sleep(8000);

        // Desmarca checkbox de soma de multa de mora caso esteja marcado;
        if (checkIfCheckBoxIsEnabled())
        {
            System.out.println("Desmarcando checkbox multa");
            this.driver.findElement(By.id("ctl00_contentManager_ASPxPageControlRenegociacao_ckmulta_S_D")).click();
            Utils.waitv(2);
        }

        // Obetendo valor da tarifa
        setValorTarifaMoraCedenteView(this.driver);

        // Realizando checagens
        Document doc = Jsoup.parse(this.driver.getPageSource());
        
        // ctl00_contentManager_ASPxPageControlRenegociacao_cktarbaixa_S
        String buttonNotChecked = "dxWeb_edtCheckBoxUnchecked_PlasticBlue";
        assertStringContainsAnotherString(doc.getElementById("ctl00_contentManager_ASPxPageControlRenegociacao_cbcedente_I").toString(), this.operacaoRecompra.getCedente().getApelido(), "Nome empresa nao contida na pagina. Abortando operacao."); 
        assertStringContainsAnotherString(doc.getElementById("ctl00_contentManager_ASPxPageControlRenegociacao_ckmulta_S_D").attr("class").toString(), buttonNotChecked, "Html inconsistente. Abortando processo.");
        assertStringContainsAnotherString(doc.getElementById("ctl00_contentManager_ASPxPageControlRenegociacao_cbempresa_I").toString(), this.operacaoRecompra.getEmpresa().getApelido(), "Nome empresa nao contida na pagina. Abortando operacao."); 
//        assertStringContainsAnotherString(doc.getElementById("ctl00_contentManager_ASPxPageControlRenegociacao_EdVencimento_I").attr("value").toString(), "31/12/2099", "Html inconsistente. Abortando processo.");

//       String valueNaoCobrarBaixa= this.driver.findElement(By.id("ctl00_contentManager_ASPxPageControlRenegociacao_cktarbaixa_S")).getDomAttribute("value");
//       System.out.println("ValueNaoCobrarBaixa: "+valueNaoCobrarBaixa);
//       if(valueNaoCobrarBaixa.contains("U"))
//       {
//    	   this.driver.findElement(By.id("ctl00_contentManager_ASPxPageControlRenegociacao_cktarbaixa_S")).click();
//    	   Utils.waitv("Não cobrar tarifa de baixa CNAB - Checking!",2);
//       }
//       else {
//    	   Utils.waitv("Não cobrar tarifa de baixa CNAB - Checked already!",2);
//       }
        
        // Clica em Selecao
        this.driver.findElement(By.id("ctl00_contentManager_ASPxPageControlRenegociacao_BtSelecao")).click();
        Utils.waitv(3);


        // **  Muda de pagina e vai para a tabela **

        // Scroll down till end of page
        this.jsExecutor.executeScript("window.scrollTo(0, document.body.scrollHeight)");
        Utils.waitv(2);


        // Seleciona "Todos" no rodape da pagina em vez de selecionar 30 a 30. 
        Select todosSelect = new Select(this.driver.findElement(By.id("ctl00_contentManager_ASPxPageControlRenegociacao_ddl_PageSize")));
        todosSelect.selectByValue("10000");
        Utils.waitv(2);
        int nTitulosEncontrados=0;
        for(TituloRecompra tituloRecompra:this.operacaoRecompra.getTitulosRecompra())
    	{
        	
            System.out.println("------------------------------");
            System.out.println("Processando para titulo " + tituloRecompra.getIdentificacaoTitulo());
            tituloRecompra.show();
            tituloRecompra.checkTitulo(ConnectorMSSQL.conn, ConnectorMariaDB.conn);
            if(tituloRecompra.isBaixado())
            {
            	// Taking out this line because it should be performed only at the final step
            	 //tituloRecompra.updateBaixado(ConnectorMariaDB.conn);
            	System.out.println("Titulo já baixado!");
            }
            else {
               	if(trocarValorMora(tituloRecompra))
               	{
               		nTitulosEncontrados++;
               	}
			}
        }
        Utils.waitv(7);
        this.driver.findElement(By.id("ctl00_contentManager_ASPxPageControlRenegociacao_BtProcessa_CD")).click();
        Utils.waitv(12);
//        this.driver.findElement(By.id("ctl00_contentManager_ASPxPageControlRenegociacao_VlTot_B_I")).click();
//        this.driver.findElement(By.id("ctl00_contentManager_ASPxPageControlRenegociacao_VlTot_B_I")).clear();
//        this.driver.findElement(By.id("ctl00_contentManager_ASPxPageControlRenegociacao_VlTot_B_I")).sendKeys(Keys.END);
//        for(int i=0;i<30;i++)
//        {
//        	this.driver.findElement(By.id("ctl00_contentManager_ASPxPageControlRenegociacao_VlTot_B_I")).sendKeys(Keys.BACK_SPACE);
//        	Utils.waitv(0.25);
//        }
        this.operacaoRecompra.getEmpresa().getDadosBancariosEmpresa().show();
        System.out.println("ApelidoConta: "+this.operacaoRecompra.getEmpresa().getDadosBancariosEmpresa().getApelido());
        if(nTitulosEncontrados==this.operacaoRecompra.getTitulosRecompra().size())
        {
	        System.out.println("ValorRecompra: "+this.operacaoRecompra.getValor());
	        Utils.sendHumanKeys(this.driver.findElement(By.id("ctl00_contentManager_ASPxPageControlRenegociacao_VlTot_B_I")), formatValor(operacaoRecompra.getValor()));
	        Utils.waitv(5);
	        this.driver.findElement(By.id("ctl00_contentManager_ASPxPageControlRenegociacao_edcontabancaria_I")).click();
	        // ctl00_contentManager_ASPxPageControlRenegociacao_edcontabancaria_B-1
	        Utils.waitv(1);
	        this.driver.findElement(By.id("ctl00_contentManager_ASPxPageControlRenegociacao_edcontabancaria_B-1")).click();
	        // ctl00_contentManager_ASPxPageControlRenegociacao_edcontabancaria
	        // ctl00_contentManager_ASPxPageControlRenegociacao_edcontabancaria_DDD_L_LBT
	        WebElement tableAccount=this.driver.findElement(By.id("ctl00_contentManager_ASPxPageControlRenegociacao_edcontabancaria_DDD_L_LBT"));
	       
	        List<WebElement> rows = tableAccount.findElements(By.tagName("tr"));
        	System.out.println("********************************************");
        	System.out.println("ApelidoConta: "+this.operacaoRecompra.getEmpresa().getDadosBancariosEmpresa().getApelido());
        	System.out.println(" Looking for: ");
        	System.out.println(this.operacaoRecompra.getEmpresa().getDadosBancariosEmpresa().getApelido());
        	boolean foundConta=false;
	        for(WebElement row:rows)
	        {
	        	System.out.println(row.getText());
	        	System.out.println(row.getText().toLowerCase());
	        	if(row.getText().startsWith(this.operacaoRecompra.getEmpresa().getDadosBancariosEmpresa().getApelido())
	        			&& row.getText().endsWith(this.operacaoRecompra.getEmpresa().getDadosBancariosEmpresa().getApelido())
	        			)
	        	{
	        		System.out.println("FOUND!");
	        		row.click();
	        		foundConta=true;
	        		break;
	        	}
	        }
	        System.out.println("Found: "+foundConta);
	        
	        Utils.waitv(4);
	        if(!foundConta)
	        {
	        	System.out.println("Conta not found! Exit!");
	        	this.driver.close();
	        	System.exit(1);
	        }
	        // ctl00_contentManager_ASPxPageControlRenegociacao_EdObs_I
	        // ctl00_contentManager_ASPxPageControlRenegociacao_EdObs_I
	        this.driver.findElement(By.id("ctl00_contentManager_ASPxPageControlRenegociacao_EdObs_I")).sendKeys("TARIFA BAIXA");
	//        Utils.sendHumanKeys(this.driver.findElement(By.id("ctl00_contentManager_ASPxPageControlRenegociacao_EdObs_I")), "TARIFA BAIXA");
	        Utils.waitv("Before pressing button Baixar",15);
//	        Utils.waitv(400);
	        // //*[@id="ctl00_contentManager_ASPxButtonNovo_CD"]/span
	        this.driver.findElement(By.id("ctl00_contentManager_ASPxButtonNovo_CD")).click();
	        Utils.waitv(8);
	        System.out.println("##################################################");
	        System.out.println("# BAIXA DESTA OPERAÇÃO RECOMPRA CONCLUÍDA  #");
	        System.out.println("##################################################");
			this.operacaoRecompra.updateHorarioBaixadoAfterCompletion(ConnectorMariaDB.conn);
			System.out.println("Operacao Recompra registrada como baixada!");
	
	        // Finalizado processo de acordo com cada classe filha
	//            childFinishProcess();
	
	            for (String idDuplicataQuitar : listIdDuplicatasQuitar)
	            {
	                // Realizacao de gravacao de critica
	                addToListCritica(idDuplicataQuitar);
	            }
	
	            // Salvando critica no banco de dados
	            saveListCriticaOperacoesRealizadasToDatabase();
        }
    }
    
    void quitacaoBaixa(List<String> listIdDuplicatasQuitar,  String url)
    {
        try
        {
            checkIfVariablesArePopulated();

            this.driver.manage().window().maximize();

            this.driver.get(url);
            Thread.sleep(3000);

            // Clica em pesquisar. Lista operacoes disponiveis
            this.driver.findElement(By.id("ctl00_contentManager_ASPxButtonNovaRecompra_CD")).click();
            Thread.sleep(5000);

            // Seleciona nome da empresa
            this.driver.findElement(By.id("ctl00_contentManager_ASPxPageControlRenegociacao_cbempresa_I")).clear();
            Thread.sleep(1000);
            this.driver.findElement(By.id("ctl00_contentManager_ASPxPageControlRenegociacao_cbempresa_I")).sendKeys(Keys.DELETE);
            for (int i = 0; i < 10; i++)
            {
                this.driver.findElement(By.id("ctl00_contentManager_ASPxPageControlRenegociacao_cbempresa_I")).sendKeys(Keys.BACK_SPACE);
            };
            this.driver.findElement(By.id("ctl00_contentManager_ASPxPageControlRenegociacao_cbempresa_I")).sendKeys(this.nomeEmpresa);
            Thread.sleep(1000);
            this.driver.findElement(By.id("ctl00_contentManager_ASPxPageControlRenegociacao_cbempresa_I")).sendKeys(Keys.TAB);
            Thread.sleep(7000);

            // Pop up de recompra termorario pode aparecer
            pressSystemEscKey();
            Thread.sleep(8000);

            // Seleciona nome do cedente
            this.driver.findElement(By.id("ctl00_contentManager_ASPxPageControlRenegociacao_cbcedente_I")).sendKeys(this.nomeCedente);
            Thread.sleep(2000);
            this.driver.findElement(By.id("ctl00_contentManager_ASPxPageControlRenegociacao_cbcedente_I")).sendKeys(Keys.TAB);
            Thread.sleep(10000);

            // Pop up de recompra termorario pode aparecer
            pressSystemEscKey();
            Thread.sleep(2000);
            pressSystemEscKey();
            Thread.sleep(8000);

            // Arruma data de vendimento
            this.driver.findElement(By.id("ctl00_contentManager_ASPxPageControlRenegociacao_EdVencimento_I")).clear();
            Thread.sleep(1000);
            this.driver.findElement(By.id("ctl00_contentManager_ASPxPageControlRenegociacao_EdVencimento_I")).sendKeys(Keys.DELETE);
            for (int i = 0; i < 10; i++)
            {
                this.driver.findElement(By.id("ctl00_contentManager_ASPxPageControlRenegociacao_EdVencimento_I")).sendKeys(Keys.BACK_SPACE);
            };
            this.driver.findElement(By.id("ctl00_contentManager_ASPxPageControlRenegociacao_EdVencimento_I")).sendKeys("31122099");
            Thread.sleep(1000);
            this.driver.findElement(By.id("ctl00_contentManager_ASPxPageControlRenegociacao_EdVencimento_I")).sendKeys(Keys.ENTER);
            Thread.sleep(7000);

            // // Pop up de recompra termorario pode aparecer
            // pressSystemEscKey();
            // pressSystemEscKey();
            // Thread.sleep(8000);

            // Desmarca checkbox de soma de multa de mora caso esteja marcado;
            if (checkIfCheckBoxIsEnabled())
            {
                System.out.println("Desmarcando checkbox multa");
                this.driver.findElement(By.id("ctl00_contentManager_ASPxPageControlRenegociacao_ckmulta_S_D")).click();
                Thread.sleep(2000);
            }

            // Obetendo valor da tarifa
            setValorTarifaMoraCedenteView(this.driver);

            // Realizando checagens
            Document doc = Jsoup.parse(this.driver.getPageSource());
            String buttonNotChecked = "dxWeb_edtCheckBoxUnchecked_PlasticBlue";
            assertStringContainsAnotherString(doc.getElementById("ctl00_contentManager_ASPxPageControlRenegociacao_cbcedente_I").toString(), this.nomeCedente, "Nome empresa n��o contida na pagina. Abortando operacao."); 
            assertStringContainsAnotherString(doc.getElementById("ctl00_contentManager_ASPxPageControlRenegociacao_ckmulta_S_D").attr("class").toString(), buttonNotChecked, "Html inconsistente. Abortando processo.");
            assertStringContainsAnotherString(doc.getElementById("ctl00_contentManager_ASPxPageControlRenegociacao_cbempresa_I").toString(), this.nomeEmpresa, "Nome empresa n��o contida na pagina. Abortando operacao."); 
            assertStringContainsAnotherString(doc.getElementById("ctl00_contentManager_ASPxPageControlRenegociacao_EdVencimento_I").attr("value").toString(), "31/12/2099", "Html inconsistente. Abortando processo.");


            // Clica em Selecao
            this.driver.findElement(By.id("ctl00_contentManager_ASPxPageControlRenegociacao_BtSelecao")).click();
            Thread.sleep(3000);


            // **  Muda de pagina e vai para a tabela **

            // Scroll down till end of page
            this.jsExecutor.executeScript("window.scrollTo(0, document.body.scrollHeight)");
            Thread.sleep(2000);


            // Seleciona "Todos" no rodape da pagina em vez de selecionar 30 a 30. 
            Select todosSelect = new Select(this.driver.findElement(By.id("ctl00_contentManager_ASPxPageControlRenegociacao_ddl_PageSize")));
            todosSelect.selectByValue("10000");
            Thread.sleep(2000);

            for (int i = 0 ; i < listIdDuplicatasQuitar.size(); i++)
            {
                System.out.println("------------------------------");
                System.out.println("Processando para titulo " + listIdDuplicatasQuitar.get(i));

                // Essa parte do processo so deve ser feita caso o titulo venca hoje ou esteja vencido.
                trocarValorMora(listIdDuplicatasQuitar.get(i));
            }

            // Finalizado processo de acordo com cada classe filha
//            childFinishProcess();

            for (String idDuplicataQuitar : listIdDuplicatasQuitar)
            {
                // Realizacao de gravacao de critica
                addToListCritica(idDuplicataQuitar);
            }

            // Salvando critica no banco de dados
            saveListCriticaOperacoesRealizadasToDatabase();
        }
        catch (InterruptedException ex) 
        {
            Thread.currentThread().interrupt();
        }
    }

    public void simulacaoBaixa(OperacaoRecompra operacaoRecompra)
    {
    	System.out.println("--------------- Starting simulation for Recompra -------------");
    	operacaoRecompra.show();
    	System.out.println("--------------- Filling forms -------------");
//            checkIfVariablesArePopulated();
            Dimension dimension = new Dimension(1920, 1800);
            this.driver.manage().window().setSize(dimension);
//            this.driver.manage().window().maximize();
// "https://gercloud2.rgbsys.com.br/BMA_HOMOLOGA/operacoes/forms/RenegociacaoBorderoPesquisaNova.aspx?origem=0"
//            this.driver.get("https://gercloud2.rgbsys.com.br/GER_BMA/operacoes/forms/RenegociacaoBorderoPesquisaNova.aspx?origem=0");
            this.driver.get(this.rootURL+"/operacoes/forms/RenegociacaoBorderoPesquisaNova.aspx?origem=0");
            Utils.waitv(3);
//            Thread.sleep(3000);

            this.driver.findElement(By.id("ctl00_contentManager_ASPxButtonNovaRecompra_CD")).click();
            Utils.waitv("Click nova recompra",5);
//            Thread.sleep(5000);

            // Seleciona nome da empresa
            this.driver.findElement(By.id("ctl00_contentManager_ASPxPageControlRenegociacao_cbempresa_I")).clear();
            Utils.waitv("Clear empresa",1);
//            Thread.sleep(1000);
            this.driver.findElement(By.id("ctl00_contentManager_ASPxPageControlRenegociacao_cbempresa_I")).sendKeys(Keys.DELETE);            
            for (int i = 0; i < 10; i++)
            {
                this.driver.findElement(By.id("ctl00_contentManager_ASPxPageControlRenegociacao_cbempresa_I")).sendKeys(Keys.BACK_SPACE);
            };
            this.driver.findElement(By.id("ctl00_contentManager_ASPxPageControlRenegociacao_cbempresa_I")).sendKeys(operacaoRecompra.getEmpresa().getApelido());
            Utils.waitv("Write nome empresa",1);
//            Thread.sleep(1000);
            this.driver.findElement(By.id("ctl00_contentManager_ASPxPageControlRenegociacao_cbempresa_I")).sendKeys(Keys.TAB);
            Utils.waitv("Sending TAB empresa",7);
//            Thread.sleep(7000);

            // Pop up de recompra termorario pode aparecer
            pressSystemEscKey();
            Utils.waitv("Escape for popup",8);
//            Thread.sleep(8000);

            // Seleciona nome do cedente
            this.driver.findElement(By.id("ctl00_contentManager_ASPxPageControlRenegociacao_cbcedente_I")).sendKeys(operacaoRecompra.getCedente().getApelido());
            Utils.waitv("Write nome cedente",2);
//            Thread.sleep(2000);
            this.driver.findElement(By.id("ctl00_contentManager_ASPxPageControlRenegociacao_cbcedente_I")).sendKeys(Keys.TAB);
            Utils.waitv("Sending TAB cedente",7);
//            Thread.sleep(10000);

            // Pop up de recompra termorario pode aparecer
            pressSystemEscKey();
            Utils.waitv("Escape for popup",2);
            pressSystemEscKey();
            Utils.waitv("Escape for popup",8);

            this.driver.findElement(By.id("ctl00_contentManager_ASPxPageControlRenegociacao_EdVencimentoInicial_I")).clear();
            this.driver.findElement(By.id("ctl00_contentManager_ASPxPageControlRenegociacao_EdVencimentoInicial_I")).sendKeys(Keys.DELETE);
            for (int i = 0; i < 10; i++)
            {
            	this.driver.findElement(By.id("ctl00_contentManager_ASPxPageControlRenegociacao_EdVencimentoInicial_I")).sendKeys(Keys.BACK_SPACE);
            };
            this.driver.findElement(By.id("ctl00_contentManager_ASPxPageControlRenegociacao_EdVencimentoInicial_I")).sendKeys(sdfnr.format(operacaoRecompra.getInicioVencimento()));
            Utils.waitv(1);
            // Arruma data de vencimento
            this.driver.findElement(By.id("ctl00_contentManager_ASPxPageControlRenegociacao_EdVencimento_I")).clear();
            Utils.waitv("Fixing data vencimento",1);
            this.driver.findElement(By.id("ctl00_contentManager_ASPxPageControlRenegociacao_EdVencimento_I")).sendKeys(Keys.DELETE);
            for (int i = 0; i < 10; i++)
            {
                this.driver.findElement(By.id("ctl00_contentManager_ASPxPageControlRenegociacao_EdVencimento_I")).sendKeys(Keys.BACK_SPACE);
            };
//            this.driver.findElement(By.id("ctl00_contentManager_ASPxPageControlRenegociacao_EdVencimento_I")).sendKeys("31122099");
            this.driver.findElement(By.id("ctl00_contentManager_ASPxPageControlRenegociacao_EdVencimento_I")).sendKeys(sdfnr.format(operacaoRecompra.getFinalVencimento()));
            Utils.waitv(1);
//            Thread.sleep(1000);
            this.driver.findElement(By.id("ctl00_contentManager_ASPxPageControlRenegociacao_EdVencimento_I")).sendKeys(Keys.ENTER);
            Utils.waitv(7);
            
//            Thread.sleep(7000);

            // // Pop up de recompra termorario pode aparecer
            // pressSystemEscKey();
            // pressSystemEscKey();
            // Thread.sleep(8000);

            // Desmarca checkbox de soma de multa de mora caso esteja marcado;
            if (checkIfCheckBoxIsEnabled())
            {
                System.out.println("Desmarcando checkbox multa");
                this.driver.findElement(By.id("ctl00_contentManager_ASPxPageControlRenegociacao_ckmulta_S_D")).click();
                Utils.waitv("Desmarcando checkbox multa",2);
//                Thread.sleep(2000);
            }

            // Obtendo valor da tarifa
            Utils.waitv("Obtaining valor taxa mora",10);
            System.out.println("Valor tarifa mora from database: " + operacaoRecompra.getCedente().getTarifaBaixa());
            operacaoRecompra.getCedente().showTarifas();
            setValorTarifaMoraCedenteView(this.driver);

            // Realizando checagens
            Document doc = Jsoup.parse(this.driver.getPageSource());
            String buttonNotChecked = "dxWeb_edtCheckBoxUnchecked_PlasticBlue";
            assertStringContainsAnotherString(doc.getElementById("ctl00_contentManager_ASPxPageControlRenegociacao_cbcedente_I").toString(), operacaoRecompra.getCedente().getApelido(), "Nome cedente não contida na pagina. Abortando operacao."); 
            assertStringContainsAnotherString(doc.getElementById("ctl00_contentManager_ASPxPageControlRenegociacao_ckmulta_S_D").attr("class").toString(), buttonNotChecked, "Html inconsistente multa. Abortando processo.");
            assertStringContainsAnotherString(doc.getElementById("ctl00_contentManager_ASPxPageControlRenegociacao_cbempresa_I").toString(), operacaoRecompra.getEmpresa().getApelido(), "Nome empresa não contida na pagina. Abortando operacao."); 
            assertStringContainsAnotherString(doc.getElementById("ctl00_contentManager_ASPxPageControlRenegociacao_EdVencimento_I").attr("value").toString(), sdfr.format(operacaoRecompra.getFinalVencimento()), "Html inconsistente data final. Abortando processo.");


            // Clica em Selecao
            this.driver.findElement(By.id("ctl00_contentManager_ASPxPageControlRenegociacao_BtSelecao")).click();
            Utils.waitv("Click on selecao",3);
//            Thread.sleep(3000);


            // **  Muda de pagina e vai para a tabela **

            // Scroll down till end of page
            this.jsExecutor.executeScript("window.scrollTo(0, document.body.scrollHeight)");
            Utils.waitv("Scroll down",3);
//            Thread.sleep(2000);


            // Seleciona "Todos" no rodape da pagina em vez de selecionar 30 a 30. 
            Select todosSelect = new Select(this.driver.findElement(By.id("ctl00_contentManager_ASPxPageControlRenegociacao_ddl_PageSize")));
            todosSelect.selectByValue("10000");
            Utils.waitv("After selecting Todos",80);
//            Thread.sleep(2000);

//            for (int i = 0 ; i < listIdDuplicatasQuitar.size(); i++)
            for (int i = 0 ; i < operacaoRecompra.getTitulosRecompra().size(); i++)            	
            {
                System.out.println("------------------------------");
                System.out.println("Processando para titulo "
                + operacaoRecompra.getTitulosRecompra().get(i).getIdentificacaoTitulo()
                + " Vencimento: " + sdfr.format(operacaoRecompra.getTitulosRecompra().get(i).getVencimento())
                + " Vencido: "+operacaoRecompra.getTitulosRecompra().get(i).isVencido());
                // Essa parte do processo so deve ser feita caso o titulo venca hoje ou esteja vencido.
//                if(operacaoRecompra.getTitulosRecompra().get(i).isVencido())
//                {
//                	trocarValorMora(operacaoRecompra.getTitulosRecompra().get(i));
//                }
                trocarValorMora(operacaoRecompra.getTitulosRecompra().get(i));
            }            
//            System.exit(1);
            // Finalizado processo de acordo com cada classe filha
            childFinishProcess();

//            for (TituloRecompra tituloRecompra :operacaoRecompra.getTitulosRecompra())
//            {
//                // Realizacao de gravacao de critica
//                addToListCritica(tituloRecompra.getIdentificacaoTitulo());
//            }
//            // Salvando critica no banco de dados
//            saveListCriticaOperacoesRealizadasToDatabase();
            System.out.println("---- Finishing process to generate simulation ----");
    }

     public static void main(String[] args) {

         RgbsysRecompraView rgbSysSelenium = new RgbsysRecompraView("MOISES", "moises");
         try
         { 
             rgbSysSelenium.setNomeEmpresa("BMA FIDC");
             rgbSysSelenium.setNomeCedente("MAXIBRASIL");
             rgbSysSelenium.setNomeContaBancaria("BRADESCO FIDC");

//             List<String> listDuplicatasQuitar = new Vector<String>();
//             listDuplicatasQuitar.add("116644NE2");
//             listDuplicatasQuitar.add("116606NE2");
//
//              rgbSysSelenium.login();
//              rgbSysSelenium.quitacaoBaixa(listDuplicatasQuitar, ur);
            
         }
         catch (Exception e) 
         {
             e.printStackTrace();
             rgbSysSelenium.saveListCriticaOperacoesRealizadasToDatabase();
         }
         finally
         {
             rgbSysSelenium.driver.close();

         }

     }

	public void setValorTarifaMoraCedenteView(Double valorTarifaMoraCedenteView) {
		this.valorTarifaMoraCedenteView = valorTarifaMoraCedenteView;
	}

	public static SimpleDateFormat getSdfnr() {
		return sdfnr;
	}

	public static void setSdfnr(SimpleDateFormat sdfnr) {
		RgbsysRecompraView.sdfnr = sdfnr;
	}

	void childFinishProcess() {
		// TODO Auto-generated method stub
		
	}

	public OperacaoRecompra getOperacaoRecompra() {
		return this.operacaoRecompra;
	}

	public void setOperacaoRecompra(OperacaoRecompra operacaoRecompra) {
		this.operacaoRecompra = operacaoRecompra;
	}

	public static SimpleDateFormat getSdfr() {
		return sdfr;
	}

	public static void setSdfr(SimpleDateFormat sdfr) {
		RgbsysRecompraView.sdfr = sdfr;
	}
}
