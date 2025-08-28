package rgbsys;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Connection;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.Vector;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.openqa.selenium.Alert;
import org.openqa.selenium.By;
import org.openqa.selenium.Dimension;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.Select;
import org.unix4j.Unix4j;
import org.unix4j.unix.Ls;

import conta_grafica.OperacaoRecompra;
import conta_grafica.TituloRecompra;
import kong.unirest.Unirest;
import utils.Utils;

public class RgbsysSimulacaoRecompra extends RgbsysRecompraView {
    private static SimpleDateFormat sdfnr=new SimpleDateFormat("ddMMyyyy");
    private static SimpleDateFormat sdfr=new SimpleDateFormat("dd/MM/yyyy");
	private Double valorMoraOriginalCedenteView;
	private Double valorTarifaMoraCedenteView;

    public RgbsysSimulacaoRecompra(String userName, String password, Path downloadDir, String rootURL)
    {
        super(userName, password, downloadDir,rootURL);
    }
    
    public RgbsysSimulacaoRecompra()
    {
    	super();
    }

    public String getDailyIdSimulacaoRecompra(String savingDateString)
    {
        String stringReturn = "";

        String grep1Filter = "simulacao_recompra_" + savingDateString;
        String grep2Filter = getNomeEmpresa() + "_" + getNomeCedente();

        stringReturn = Unix4j.ls(Ls.Options.l.a, getSavingPath()).grep(grep1Filter).grep(grep2Filter).wc().toString();

        System.out.println(stringReturn);

        return stringReturn;
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
    
    public void simulacaoBaixa(OperacaoRecompra operacaoRecompra, Connection connMaria)
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
            int titlesChanged=0;
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
                titlesChanged+=trocarValorMora(operacaoRecompra.getTitulosRecompra().get(i), connMaria);
            }            
//            System.exit(1);
            // Finalizado processo de acordo com cada classe filha
            if(titlesChanged>0)
            {
	            childFinishProcess(operacaoRecompra, connMaria);
	            System.out.println("Value from RGB: " + operacaoRecompra.getValor());
	            operacaoRecompra.updateValue(connMaria);
	            System.out.println("Value from processing: " + operacaoRecompra.getValor());
            }
//            for (TituloRecompra tituloRecompra :operacaoRecompra.getTitulosRecompra())
//            {
//                // Realizacao de gravacao de critica
//                addToListCritica(tituloRecompra.getIdentificacaoTitulo());
//            }
//            // Salvando critica no banco de dados
//            saveListCriticaOperacoesRealizadasToDatabase();
            System.out.println("---- Finishing process to generate simulation ----");
    }

   public void moveFromTempFolder(OperacaoRecompra operacaoRecompra, Connection connMaria)
    {
        // try 
        // {
            String oldFileName = getTempSavingFolder().toFile().list()[0];
            System.out.println("Nome do arquivo que acabou de ser baixado: " + oldFileName);
            // String fileName = "relatorio_envio_extrato_conta_corrente_" + nomeApelidoEmpresa + "_" + nomeApelidoCedente + "_" + dateString + ".pdf";
            System.out.println("SavingFolder: " + this.getSavingFolder());
            String fileName= this.getSavingFolder()+File.separator+oldFileName;
            System.out.println("FileName: "+fileName);
            File filePDF = new File(fileName);
//            if(filePDF.exists())
//            {
            	operacaoRecompra.setDemostrativoPDF(filePDF);
            	operacaoRecompra.updatePath(connMaria);
//            }
            Path downloadedFile  = Paths.get(getTempSavingFolder().toAbsolutePath().toString(), oldFileName);
            System.out.println("Downloaded file before renaming: " + downloadedFile.toString());
            downloadedFile.toFile().renameTo(new File(Paths.get(getSavingFolder().toAbsolutePath().toString(), oldFileName).toString()));
            System.out.println("Final path: " + downloadedFile.toString());
        // } 
        // catch (ParseException e) 
        // {
        //     e.printStackTrace();
        // }
    }

   public int trocarValorMora(TituloRecompra tituloRecompra, Connection connMaria)
   {
       Date vctoTitulo =  new Date(1900, 1, 1);
		Date novoVcto =  new Date(2022, 1, 1);
		Date currentDate = new Date();

		Boolean rowSelected = false;
		Boolean boolAplicarMora = false;
		Document doc = Jsoup.parse(this.driver.getPageSource());

		Element table  = doc.getElementById("gridtitulos");
		if(table==null)
		{
			System.out.println("No titles!");
			return 0;
		}
		Elements listRows  = table.select("tr");

		Elements tableHeader = table.select("tr").first().select("th");

		for (int rowCounter = 0; rowCounter < listRows.size(); rowCounter++)
		{
		    rowSelected = false;
		    Element row  = listRows.get(rowCounter);
		    Elements listDescriptions = row.select("td");
		    
		    String vencimientroString="";
		    String duplicataString="";
		    String chequeString="";
		    String valorString="";
		    String deducaoString="";
		    String moraString="";
		    String diferencaLiquidacaoString="";
		    String totalString="";
		    String valorRecompraString="";
		    double valor=0;
		    double deducao=0;
		    double mora=0;
		    double diferencaLiquidacao=0;
		    double total=0;
		    double valorRecompra=0;
		    		
		    
		    for (int colCounter = 0; colCounter < listDescriptions.size(); colCounter++)
		    {
		        Element description = listDescriptions.get(colCounter);
		        if (row.html().contains(tituloRecompra.getIdentificacaoTitulo()))
		        {
		            // System.out.println("ROW COUNTER " + rowCounter);
		            // System.out.println(tableHeader.get(colCounter).html());
		            // System.out.println(description.html());


		            if (description.html().contains("MarcaDesmarca"))
		            {
		                rowSelected = true;

		                // Realizando checkbox da operacao de recompra
//                           System.out.println("Selecionando checkbox de operacao de recompra.");
		                Utils.waitv("Selecionando checkbox de operacao de recompra.",4);
//                           Thread.sleep(4000);

		                // System.out.println(row.html());
		                // System.out.println(row.select("input").attr("name").toString()); 

		                String localSelectName = row.select("input").attr("name").toString();
		                WebElement localElement = this.driver.findElement(By.name(localSelectName));

		                // Scrolling into element
		                this.jsExecutor.executeScript("arguments[0].scrollIntoView(true);", localElement);
		                Utils.waitv("Scroll até o título",4);
//                           Thread.sleep(4000);

		                // Clicking 
		                localElement.click();
		                Utils.waitv("Click no título",6);
		                doc = Jsoup.parse(this.driver.getPageSource());
		                table  = doc.getElementById("gridtitulos");
		                listRows  = table.select("tr");
		                row  = listRows.get(rowCounter);
		                listDescriptions = row.select("td");
//                           Thread.sleep(4000);
//		                Utils.waitv("After Click no título",120);
		                
		                continue;
		            }
		            
		            if(description.hasText())
		            {		            	
			            switch (colCounter) {
						case 1:
							vencimientroString=description.ownText();
							break;
						case 2:
							duplicataString=description.ownText();
							break;
						case 3:
							chequeString=description.ownText();
							break;
						case 4:
							valorString=description.ownText();
							break;
						case 5:
							deducaoString=description.ownText();
							break;
						case 6:
							moraString=description.ownText();
							break;
						case 7:
							diferencaLiquidacaoString=description.ownText();
							break;
						case 8:
							totalString=description.ownText();
							break;
						case 9:
							valorRecompraString=description.ownText();
							break;
						default:
							break;
						}
		            }
		            if (description.html().contains("Alterar Mora"))
            		{
		            	System.out.println("++++++++++ VALORES APÓS ALTERAÇÃO");
		            	System.out.println("vencimientroString: "+vencimientroString);
		            	System.out.println("duplicataString: "+duplicataString);
		            	System.out.println("chequeString: "+chequeString);
		            	System.out.println("valorString: "+valorString);
		            	System.out.println("deducaoString: "+deducaoString);
		            	System.out.println("moraString: "+moraString);
		            	System.out.println("diferencaLiquidacaoString: "+diferencaLiquidacaoString);
		            	System.out.println("totalString: "+totalString);
		            	System.out.println("valorRecompraString: "+valorRecompraString);
		            	
		              	System.out.println("valor: "+parseValor(valorString));
		            	System.out.println("deducao: "+parseValor(deducaoString));
		            	System.out.println("mora: "+parseValor(moraString));
		            	System.out.println("diferencaLiquidacao: "+parseValor(diferencaLiquidacaoString));
		            	System.out.println("total: "+parseValor(totalString));
		            	System.out.println("valorRecompra: "+parseValor(valorRecompraString));
		            	tituloRecompra.setDeducao(parseValor(deducaoString));
		            	tituloRecompra.setMora(parseValor(moraString));
		            	tituloRecompra.setDiferencaLiquidacao(parseValor(diferencaLiquidacaoString));
		            	tituloRecompra.setValorRecompra(parseValor(valorRecompraString));
		            	tituloRecompra.updateValues(connMaria);

            		}
		            if (description.html().contains("Alterar Mora") && tituloRecompra.isVencido())
		            {

		                System.out.println("Aplicando mora para titulo: " + tituloRecompra.getIdentificacaoTitulo());
		                Utils.waitv("Applying mora",4);
//                           Thread.sleep(4000);

		                // System.out.println("***********");
		                // System.out.println(description.html());

		                String selectName = description.select("input").attr("name").toString();
		                // System.out.println("***********");
		                // System.out.println(selectName);
		                // System.out.println("***********");
		                WebElement element = this.driver.findElement(By.name(selectName));

		                // Scrolling into element
		                this.jsExecutor.executeScript("arguments[0].scrollIntoView(true);", element);
		                Utils.waitv("Scroll no título",4);
//                           Thread.sleep(4000);

		                // Clicking 
		                element.click();
		                Utils.waitv("Click no elemento",8);
//                           Thread.sleep(4000);

		                // Obtendo valor anterior
		                setValorMoraOriginalCedenteView(this.driver);

		                // Calculando novo valor mora
		                Double novoValor = getValorTarifaMoraCedenteView() + getValorMoraOriginalCedenteView();
		                setValorMoraNovoCedenteView(novoValor);

		                // Alterando o valor
		                this.driver.findElement(By.id("txtValorMora_I")).clear();
		                Utils.waitv("Clear valor mora",2);
//                           Thread.sleep(2000);
		                this.driver.findElement(By.id("txtValorMora_I")).sendKeys(Keys.DELETE);
		                for (int z = 0; z < 10; z++)
		                {
		                    this.driver.findElement(By.id("txtValorMora_I")).sendKeys(Keys.BACK_SPACE);
		                };
		                this.driver.findElement(By.id("txtValorMora_I")).sendKeys(Keys.HOME);
		                Utils.waitv("Key home",1);
//                           Thread.sleep(2000);
		                this.driver.findElement(By.id("txtValorMora_I")).sendKeys(formatValor(novoValor));
		                Utils.waitv("Escrevendo novo valor: "+formatValor(novoValor),3);
//                           Thread.sleep(3000);

		                // Confirmando alteracao
		                String command = "document.querySelector(\"#divModalMora > div > div > div.modal-footer > div > div > button.btn.btn-info\").click();";
		                this.jsExecutor.executeScript(command);
		                Utils.waitv("Confirmando alteracao",3);
//                           Thread.sleep(3000);

		         
		            }
		        }
		    }
		    if(rowSelected)
		    {
                doc = Jsoup.parse(this.driver.getPageSource());
                table  = doc.getElementById("gridtitulos");
                listRows  = table.select("tr");
                row  = listRows.get(rowCounter);
                listDescriptions = row.select("td");

			    for (int colCounter = 0; colCounter < listDescriptions.size(); colCounter++)
			    {
			    	 Element description = listDescriptions.get(colCounter);
		            if(description.hasText())
		            {		            	
			            switch (colCounter) {
						case 1:
							vencimientroString=description.ownText();
							break;
						case 2:
							duplicataString=description.ownText();
							break;
						case 3:
							chequeString=description.ownText();
							break;
						case 4:
							valorString=description.ownText();
							break;
						case 5:
							deducaoString=description.ownText();
							break;
						case 6:
							moraString=description.ownText();
							break;
						case 7:
							diferencaLiquidacaoString=description.ownText();
							break;
						case 8:
							totalString=description.ownText();
							break;
						case 9:
							valorRecompraString=description.ownText();
							break;
						default:
							break;
						}
		            }
		            if (description.html().contains("Alterar Mora"))
	        		{
		            	System.out.println("++++++++++ VALORES APÓS ALTERAÇÃO");
		            	System.out.println("vencimientroString: "+vencimientroString);
		            	System.out.println("duplicataString: "+duplicataString);
		            	System.out.println("chequeString: "+chequeString);
		            	System.out.println("valorString: "+valorString);
		            	System.out.println("deducaoString: "+deducaoString);
		            	System.out.println("moraString: "+moraString);
		            	System.out.println("diferencaLiquidacaoString: "+diferencaLiquidacaoString);
		            	System.out.println("totalString: "+totalString);
		            	System.out.println("valorRecompraString: "+valorRecompraString);
		            	System.out.println("valor: "+parseValor(valorString));
		            	System.out.println("deducao: "+parseValor(deducaoString));
		            	System.out.println("mora: "+parseValor(moraString));
		            	System.out.println("diferencaLiquidacao: "+parseValor(diferencaLiquidacaoString));
		            	System.out.println("total: "+parseValor(totalString));
		            	System.out.println("valorRecompra: "+parseValor(valorRecompraString));
		            	tituloRecompra.setDeducao(parseValor(deducaoString));
		            	tituloRecompra.setMora(parseValor(moraString));
		            	tituloRecompra.setDiferencaLiquidacao(parseValor(diferencaLiquidacaoString));
		            	tituloRecompra.setValorRecompra(parseValor(valorRecompraString));
		            	tituloRecompra.updateValues(connMaria);
		            	Utils.waitv("After Click no título",20);
		                break;
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
			listRows  = table.select("tr");
	   		
		}
		
	 return 1;
   }
   
   private String formatValor(Double valor)
   {
       return String.valueOf(valor).replace(".", ",");
   }
   private Double parseValor(String valor)
   {
	   valor=valor.replaceAll("\\.","").replace(",",".");
       return Double.parseDouble(valor);
   }

   public Double getValorMoraOriginalCedenteView() {
       return this.valorMoraOriginalCedenteView;
   }
   public void setValorMoraOriginalCedenteView(WebDriver driver) {

       Document doc = Jsoup.parse(this.driver.getPageSource());
       this.valorMoraOriginalCedenteView = parseValor(doc.getElementById("txtValorMora_Raw").attr("value").toString());
   }
   public Double getValorTarifaMoraCedenteView() {
       return this.valorTarifaMoraCedenteView;
   }
   void setValorTarifaMoraCedenteView(WebDriver driver)
   {
       Document doc = Jsoup.parse(this.driver.getPageSource());
       this.valorTarifaMoraCedenteView = parseValor(doc.getElementById("EdMoraTitulos_I").attr("value").toString());
       System.out.println("Valor tarifa mora: " + this.valorTarifaMoraCedenteView);
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
    public void savePdf(String url)
    {
        try 
        {
            // Faz o get do PDF
            kong.unirest.HttpResponse<String> response = Unirest.get(url).asString();

            // Salva conteudo no folder
            String pdfContent = response.getBody();
            DateTimeFormatter savingDateFormatter = DateTimeFormatter.ofPattern("yyyyMMdd");
            String savingDateString = LocalDate.now().format(savingDateFormatter);

            String fileName = "simulacao_recompra_" + savingDateString + "_id_" + getDailyIdSimulacaoRecompra(savingDateString) + "_" + getNomeEmpresa() + "_" + getNomeCedente();

            Path filePath = Paths.get(this.getSavingPath(), "simulacao_recompra", fileName + ".pdf");
            createNewFileIfNotExists(filePath);
            FileWriter writer;
            writer = new FileWriter(filePath.toString());
            writer.write(pdfContent);
            writer.close();

        } 
        catch (IOException e) 
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    @Override
    void addToListCritica(String idDuplicataQuitar)
    {
        System.out.println("Simulação não requer gravação de critica a menos que a regra de negocio mude");
    }

    @Override
    void checkIfVariablesArePopulated()
    {
        if (getNomeCedente() == null)
        {
            System.out.println("Nome cedente não está definido. Abortando processo.");
        }

        if (getNomeEmpresa() == null)
        {
            System.out.println("Nome empresa não está definido. Abortando processo.");
        }

    }

     void childFinishProcess(OperacaoRecompra operacaoRecompra, Connection connMaria)
    {
        this.driver.findElement(By.id("ctl00_contentManager_ASPxPageControlRenegociacao_BtProcessa")).click();
		Utils.waitv("Click on Renegociacao Processa",2);
//            Thread.sleep(2000);

		// Clica em Gravar
		this.driver.findElement(By.id("ctl00_contentManager_ASPxButtonNovo_CD")).click();
		Utils.waitv("Click on Button Novo",8);
//            Thread.sleep(8000);

		// Systembox com as confirma����es deve ter aparecido. Clicando em OK
		pressSystemEnterKey();
		Utils.waitv("Press enter",16);
//            Thread.sleep(8000);

		String parentWindowHandler = this.driver.getWindowHandle(); // Store your parent window
		String subWindowHandler = null;

		
		Set<String> handles = driver.getWindowHandles(); // get all window handles
		if(handles.size()>1)
		{
			Iterator<String> iterator = handles.iterator();
			while (iterator.hasNext()){
			    subWindowHandler = iterator.next();
			}
			this.driver.switchTo().window(subWindowHandler); // switch to popup window
			Alert alert = this.driver.switchTo().alert();
	        String alertText = alert.getText();
	        System.out.println("Alert data: " + alertText);
	        alert.accept();
//	        this.driver.switchTo().defaultContent();
//			
		}

		this.driver.switchTo().window(parentWindowHandler);  // switch back to parent window
		Utils.waitv("Wait before trying to print anything",20);
		
		String valorRecompraString=this.driver.findElement(By.id("ctl00_contentManager_ASPxPageControlRenegociacao_VlTot_B_I")).getAttribute("value");
		System.out.println("Valor Recompra final:  "+valorRecompraString);
		operacaoRecompra.setValor(parseValor(valorRecompraString));

		this.driver.findElement(By.id("ctl00_contentManager_ASPxPageControlRenegociacao_menuImpressao_DXI0_T")).click();
		Utils.waitv("Click Menu Impressao",8);

		System.out.println("After trying to deal with alert window");
		// Clica em demonstrativo
		this.driver.findElement(By.id("ctl00_contentManager_ASPxPageControlRenegociacao_menuImpressao_DXI0i0_T")).click();
		Utils.waitv("Second Click Menu Impressao",15);
//            Thread.sleep(10000); // TIME SLEEP PRECISA SER LONGO AQUI

		// Switch to Frame 
		this.driver.switchTo().frame("ctl00_contentManager_popup_generico_CIF-1");
		Utils.waitv("Switching to popup frame",8);
//            Thread.sleep(8000);

		// Clica em demonstrativo de recompra 
		this.driver.findElement(By.id("listBox_Rel_ImpressaoRecompraRecibo_LBI0T0")).click();
		Utils.waitv("Click on demonstrativo recompra",8);
//            Thread.sleep(8000);

		// Salva window atual em uma variavel 
		String winHandleBefore = driver.getWindowHandle();

		// Clica na impressora. Novo popup vai ser aberto
		this.driver.findElement(By.id("btImpressaoImg")).click();
		Utils.waitv("Click on printer",15);
//            Thread.sleep(10000); // TIME SLEEP PRECISA SER LONGO AQUI

		// Alterna para novo popup
		for(String winHandle : this.driver.getWindowHandles()){
		    this.driver.switchTo().window(winHandle);
		}

		waitForFileToBeDownloaded();

		// Salva PDF
		// savePdf(this.driver.getCurrentUrl());
		moveFromTempFolder(operacaoRecompra, connMaria);
    }
    
    protected void waitForFileToBeDownloaded()
    {
        int timeout = 120;
        int timeElapsed = 0;
        int originalCount = 0;
        while (true)
		{
		    int numberOfFiles = getTempSavingFolder().toFile().list().length;
		    System.out.println("Files to be downloaded: " + numberOfFiles + " at " + getTempSavingFolder().toString());
		    if (numberOfFiles > originalCount)
		    {
		    	String[] filesNames=getTempSavingFolder().toFile().list();
		    	for(int i=0;i<filesNames.length;i++)
		    	{
		    		System.out.println(" -File: " + filesNames[i]);
		    	}
		        break;
		    }
		    else
		    {
		    	Utils.waitv("Waiting download",2);
//                    Thread.sleep(1000);
		        timeElapsed++;
		    }
		    if (timeElapsed > timeout)
		    {
		        System.out.println("Chegou-se ao timeout de espera de download do arquivo. Abortando processo.");
		        System.exit(1);
		    }
		}
    }


    public static void main(String[] args) {

        String processUrl = "https://gercloud2.rgbsys.com.br/BMA_HOMOLOGA/operacoes/forms/RenegociacaoBorderoPesquisaNova.aspx?origem=0";
        Path downloadDir = Paths.get("/home/rafaelichow/Documents/bma_temp/simulacao_recompra");
        String rootURL="https://gercloud2.rgbsys.com.br/BMA_HOMOLOGA/";
        
        RgbsysSimulacaoRecompra rgbSysSelenium = new RgbsysSimulacaoRecompra("MOISES", "moises", downloadDir,rootURL);
        try
        { 

            rgbSysSelenium.setNomeEmpresa("BMA FIDC");
            rgbSysSelenium.setNomeCedente("MAXIBRASIL");

            List<String> listDuplicatasQuitar = new Vector<String>();
            listDuplicatasQuitar.add("116748NE1");
            listDuplicatasQuitar.add("000004-004");

            // rgbSysSelenium.login();
            // rgbSysSelenium.quitacaoBaixa("BMA FIDC", "MAXIBRASIL", listDuplicatasQuitar, conta);
            rgbSysSelenium.quitacaoBaixa(listDuplicatasQuitar,  processUrl);
            
        }
        catch (Exception e) 
        {
            e.printStackTrace();
            rgbSysSelenium.saveListCriticaOperacoesRealizadasToDatabase();
        }
        finally
        {
            rgbSysSelenium.close();
        }

    }
}
