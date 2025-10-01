package rgbsys;

import java.sql.Connection;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

import cedente.Cedente;
import conta_grafica.BuilderRecompraRGB;
import conta_grafica.OperacaoRecompra;
import conta_grafica.TituloInstrucao;
import conta_grafica.TituloRecompra;
import empresa.Empresa;
import utils.Utils;

public class RgbsysOperacaoRecompra extends RgbsysSeleniumWebFactView {
	private static SimpleDateFormat sdfrn=new SimpleDateFormat("ddMMyyyy");
	private static SimpleDateFormat sdfr=new SimpleDateFormat("dd/MM/yyyy");
	private static SimpleDateFormat sdfh=new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
	private static SimpleDateFormat sdf=new SimpleDateFormat("yyyy-MM-dd");
	private static SimpleDateFormat sdfn=new SimpleDateFormat("yyyyMMdd");
	private static DecimalFormat df = new DecimalFormat("#0.00");
    public RgbsysOperacaoRecompra(String userName, String password)
    {
        super(userName, password);
    }
    
    public RgbsysOperacaoRecompra()
    {
    	super();
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
                    System.out.println("Trata-se de uma operação de Baixa e o flag \"Aprovado\" deveria estar marcado como \"Sim. Essa condição não deveria ser verdadeira.");
                    System.out.println("Abortando operação");
                    System.exit(1);
                }
            }
        }
    }

    public void  aceitarEfetuarBaixaRecompra(Connection connMaria, Connection connMSS, OperacaoRecompra operacaoRecompra, HashMap<String, String> cedentesSemTarifaBaixa)
    {
    	System.out.println("###############################");
    	System.out.println(" STARTING ACEITAR EFETUAR BAIXA RECOMPRA");
    	System.out.println("###############################");

    	boolean allEfetuado=true;
    	for(TituloRecompra tr:operacaoRecompra.getTitulosRecompra())
    	{
    		if(!tr.isEfetuado())
    		{
    			allEfetuado=false;
    		}
    	}
    	
    	if(allEfetuado)
    	{
    		System.out.println("Every title has already been effectived!");
    		return;
    	}
    	
    	WebElement dataAtualElement = this.driver.findElement(By.id("ctl00_lbdataatual"));
    	
    	Date dataAtual = null;
    	try {
			dataAtual = sdfr.parse(dataAtualElement.getText());
		} catch (ParseException e1) {
			e1.printStackTrace();
		}
    	
    	int numberDataAtual=Integer.parseInt(sdfn.format(dataAtual));
    	int numberActualDate = Integer.parseInt(sdfn.format(Calendar.getInstance().getTime()));
    	
    	System.out.println("NumberDateRGB: "+numberDataAtual);
    	System.out.println("NumberDateSystem: "+numberActualDate);
    	if(numberDataAtual!=numberActualDate)
    	{
    		System.out.println("Dates does not match!");
    		Utils.waitv(10);
//    		return null;
    	}
    	
    	    	
        // Baixa solicitacao WebFact
        String stringReturn = "";
        int numberOfTitles=operacaoRecompra.getTitulosRecompra().size()*5;
        int numberEndPages=0;

    	int iPage=0;
    	boolean filtersApplied=false;
    	boolean capturingTitles=false;
    	boolean firstReading=false;
    	boolean listingEmpresasCedentes=true;
//          this.driver.get("https://gercloud2.rgbsys.com.br/BMA_HOMOLOGA/operacoes/forms/BaixaSolicitacaoInternet.aspx?SessionId=");
      this.driver.get(this.rootURL+"/operacoes/forms/BaixaSolicitacaoInternet.aspx?SessionId=");
      Utils.waitv(7);

      // Clica em pesquisar. Lista operacoes disponiveis
      this.driver.findElement(By.id("ctl00_contentManager_btnBuscarImg")).click();
      Utils.waitv(10);
//          Thread.sleep(10000);

      // Filtro abatimento
      this.driver.findElement(By.id("ctl00_contentManager_gridLiberacao_DXFREditorcol1_I")).sendKeys("Baixar");
//          Thread.sleep(10000);
      Utils.waitv(12);
      HashMap<Integer, String> mapTableCols = getTableCols(this.driver.getPageSource());
//          for(int iCol:mapTableCols.keySet())
//          {
//        	  System.out.println(iCol+": "+mapTableCols.get(iCol));
//          }
//          System.out.println("Table cols acquired");
      Empresa empresa = operacaoRecompra.getEmpresa();
      Cedente cedente = operacaoRecompra.getCedente();
      boolean hasTarifaBaixa=true;
      if(cedentesSemTarifaBaixa.get(cedente.getApelido().toUpperCase())!=null)
      {
    	  hasTarifaBaixa=false;
      }
      
      
      this.driver.findElement(By.id("ctl00_contentManager_gridLiberacao_DXFREditorcol2_I")).clear();
  		Utils.waitv("Clear Filtro Empresa",2);
      this.driver.findElement(By.id("ctl00_contentManager_gridLiberacao_DXFREditorcol2_I")).sendKeys(empresa.getApelido());
      Utils.waitv("Filtro Empresa",5);
      this.driver.findElement(By.id("ctl00_contentManager_gridLiberacao_DXFREditorcol3_I")).clear();
      Utils.waitv("Clear Filtro Cedente",2);
      this.driver.findElement(By.id("ctl00_contentManager_gridLiberacao_DXFREditorcol3_I")).sendKeys(cedente.getApelido());
      Utils.waitv("Filtro Cedente",5);
      filtersApplied=true;
      boolean actionTaked=false;
      boolean allRead=false;
      boolean todosBaixados=false;
      boolean todosSelecionados=false;
      boolean finalPage=false;
      while (!todosBaixados)
      {
//                // Filtro Apr = Nao
//                this.driver.findElement(By.id("ctl00$contentManager$gridLiberacao$DXFREditorcol8")).sendKeys("N");
//                Thread.sleep(10000);
//
//                makeTableHigher();
//                Thread.sleep(10000);
    	  	if(actionTaked)
    	  	{
    	  		Utils.waitv("----------------------->>>>>>>>>>>>>>>>>>>>>>>>>>> Action Taked: ",5);
    	  		actionTaked=false;
    	  	}
            List<String> listRows = getListRows(this.driver.getPageSource());
            System.out.println("Lista de linhas: " + listRows.size());
            
            if(listRows.size()==0)
            {
            	todosBaixados=true;
            	System.out.println("Nada para baixar!");
            	Utils.waitv(10);
            	break;
            }
            
            for (int iRow=0;iRow<listRows.size(); iRow++)
            {            	
            	String stringRow=listRows.get(iRow);
            	System.out.println(iRow+"----- Start of the row");
//                    System.out.println("ListingEmpresasCedentes: "+listingEmpresasCedentes);
//                    System.out.println("Filter: "+filtersApplied);
//                    System.out.println("Capturing: "+capturingTitles);
//                    System.out.println("FirstReading: "+firstReading);

                Document doc = Jsoup.parse(this.driver.getPageSource());
//                    System.out.println(stringRow);
                Element table = doc.getElementById(stringRow);
                Elements listTableRows = table.select("tr");
                boolean tituloAdicionado=false;


                // HashMap<Integer, String> mapTableCols = getTableCols(this.driver.getPageSource());
                for (Element row : listTableRows)
                {

                    String idTitulo = "";
                    String nomeEmpresa = "";
                    String nomeCedente = "";
                    String vencimentoString="";
                    String valorString="";
                    Boolean isApproved = true;
                    Integer rowElementCounter = 0;
                    Elements listRowDescriptions =  row.select("td");
                    listRowDescriptions.remove(0);

//                        System.out.println("-------------------------");
//                        System.out.println(row);

                    for (Element rowDescription : listRowDescriptions)
                    {
                    	System.out.print(rowDescription.text()+";");
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
                        
                     // Parsing vencimento
                        if (mapTableCols.get(rowElementCounter).equals("Vcto. do Título"))
                        {
                            vencimentoString = rowDescription.text();
                        }
                        
                        // Parsing valor
                        if (mapTableCols.get(rowElementCounter).equals("Valor"))
                        {
                            valorString = rowDescription.text();
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

//                           
                        rowElementCounter++;
                        if (mapTableCols.get(rowElementCounter) == null) 
                        { 
                        	break; 
                        }
                    }
                    System.out.print("\n");
//                        System.out.println("ListingEmpresasCedentes: "+listingEmpresasCedentes);
//                        System.out.println("Filter: "+filtersApplied);
//                        System.out.println("FirstReading: "+firstReading);
                    actionTaked=false;
                    if(idTitulo.length()>0 && nomeEmpresa.length()>0 && nomeCedente.length() >0 && vencimentoString.length()>0 && valorString.length()>0)
                    {
                    	Date vencimento=null;
                    	try {
							vencimento=sdfr.parse(vencimentoString);
						} catch (ParseException e) {
							e.printStackTrace();
						}
                    	double valor=Double.parseDouble(valorString.replaceAll("\\.", "").replace(",", "."));
//                        	System.out.println("Empresa: "+empresa.getRazaoSocial());
//                        	System.out.println("Cedente: "+cedente.getParticipante().getRazaoSocial());
//                        	System.out.println("IdentificacaoTitulo: "+idTitulo);
//                        	System.out.println("Valor: "+valor);
//                        	System.out.println("Vencimento: "+sdf.format(vencimento));
                    	if(TituloRecompra.checkAberto(connMSS, operacaoRecompra, idTitulo, valor, vencimento))
                    	{
                    		System.out.println("Titulo em aberto disponível para baixa");
                    	}
                    	else
                    	{
                    		System.out.println("Titulo já baixado, não será adicionado à lista da operacao recompra");                        		
                    	}
                    }
                    boolean withinOperacaoRecompra=false;
                    if(idTitulo.trim().length()>0)
                    {
	                    for(TituloRecompra tituloRecompra:operacaoRecompra.getTitulosRecompra())
	                    {
	                    	if(tituloRecompra.getIdentificacaoTitulo().toLowerCase().contains(idTitulo.toLowerCase()))
	                    	{
	                    		withinOperacaoRecompra=true;
	                    		System.out.println("     ----> Titulo: " + tituloRecompra.getIdentificacaoTitulo() + " within OperacaoRecompra");
	                    		System.out.println(" ---- idTitulo: "+idTitulo +" ----  tituloRecompra.getIdentificacaoTitulo:  "+tituloRecompra.getIdentificacaoTitulo());
	                    		System.out.println("---- TarifaBaixa: "+hasTarifaBaixa);
	                    		tituloRecompra.show();
	                    		System.out.println("    ---------------------");
	                    		break;
	                    	}
	                    }
                    }
                    if(!withinOperacaoRecompra)
                    {
                    	System.out.println("Titulo "+idTitulo+" não está na lista desta recompra!");
                    }
                    
                    if(withinOperacaoRecompra)
                    {
                    	TituloRecompra tituloRecompra = operacaoRecompra.getIdentificacaoTitulos().get(idTitulo);
                    	System.out.println("Show tituloRecompra before checking ");
                    	tituloRecompra.show();
                    	System.out.println("ConnMSS: "+connMSS);
                    	tituloRecompra.checkTitulo(connMSS, connMaria);
                    	tituloRecompra.show();
                    	
                    	if(tituloRecompra.isEnviadoCartorio()
                    			|| tituloRecompra.isRecebimentoCartorio()
                    			|| tituloRecompra.isQuitado()
                    			)
                    	{
                    		System.out.println("Titulo não pode ser baixado como recompra");
                    		System.out.println("Rejeitando RECOMPRA");
						    String clickId = row.select("input").first().id() + "_D";
						    assert(clickId.contains("ctl00_contentManager_gridLiberacao_DXSelBtn")) : "Id inconsistente";
						    System.out.println("Clicando no elemento" +  clickId);
						    this.driver.findElement(By.id(clickId)).click();
						    Utils.waitv(10);
							this.driver.findElement(By.id("ctl00_contentManager_btnRejeita_CD")).click();
							Utils.waitv(4);
							this.driver.switchTo().frame("ctl00_contentManager_popup_TODOS_CIF-1");
							
							if(tituloRecompra.isQuitado())
							{
								System.out.println("Título já quitado!");
								this.driver.findElement(By.id("ctl00_contentManager_txtMotivo_I")).sendKeys("Titulo já baixado");
							}
							if(tituloRecompra.isEnviadoCartorio())									
							{
								System.out.println("Enviado ao cartório!");
								this.driver.findElement(By.id("ctl00_contentManager_txtMotivo_I")).sendKeys("Enviado ao cartório");
							}
							if(tituloRecompra.isRecebimentoCartorio())
							{
								System.out.println("Recebimento do cartório!");
								this.driver.findElement(By.id("ctl00_contentManager_txtMotivo_I")).sendKeys("Recebimento do cartório");
							}
							if(tituloRecompra.isVencimentoDistante())
							{
								System.out.println("Recebimento do cartório!");
								this.driver.findElement(By.id("ctl00_contentManager_txtMotivo_I")).sendKeys("Vencimento muito distante");
							}
							if(tituloRecompra.isSolicitacaoAntiga())
							{
								System.out.println("Recebimento do cartório!");
								this.driver.findElement(By.id("ctl00_contentManager_txtMotivo_I")).sendKeys("Solicitação muito antiga");
							}
							
							Utils.waitv(2);
							this.driver.findElement(By.id("ctl00_contentManager_btnMotivo_CD")).click();
							Utils.waitv(4);
							actionTaked=true;
                    	}
                    	else 
                    	{
                        	// Realizar baixa
                            System.out.println("Realizando baixa");
                            String clickId = row.select("input").first().id() + "_D";
                            assert(clickId.contains("ctl00_contentManager_gridLiberacao_DXSelBtn")) : "Id inconsistente";
                            System.out.println("Clicando no elemento" +  clickId);
                            this.driver.findElement(By.id(clickId)).click();
                            Utils.waitv(10);

                            if(!isApproved)
                            {
	                            // Clica em aprovar
	    		                System.out.println("clicando em aprovar");
	    		                this.driver.findElement(By.id("ctl00_contentManager_btnAprova")).click();
	//                                Thread.sleep(10000);
	    		                Utils.waitv(10);
                            }
    		                // Clica em efetua
    		                 System.out.println("Clicando em efetua");
    		                 this.driver.findElement(By.id("ctl00_contentManager_btnEfetua_CD")).click();
    		                 System.out.println("HasTarifaBaixa: "+hasTarifaBaixa);
    		                 //***********************************
    		                 //***********************************
    		                Utils.waitv("Clica em efetua",10);

    		                // // Apos clicar em efetua, um novo pop up ira abrir. Tem que trocar para o novo frame.
    		                 System.out.println("Clicando no novo popup");
    		                 this.driver.switchTo().frame("ctl00_contentManager_popup_TODOS_CIF-1");
    		                 Utils.waitv("Clicando no novo poput",10);
//        		                 this.driver.findElement(By.id("dtOcorrencia_I")).click();
//        		                 this.driver.findElement(By.id("dtOcorrencia_I")).clear();
//        		                 System.out.println("Pressing HOME");
//        		                 this.driver.findElement(By.id("dtOcorrencia_I")).sendKeys(Keys.HOME);
//        		                 Utils.waitv(2);
//        		                 for(int i=0;i<10;i++)
//        		                 {
//        		                	 System.out.println("Pressing DELETE");
//        		                	 this.driver.findElement(By.id("dtOcorrencia_I")).sendKeys(Keys.DELETE);
//        		                	 Utils.waitv(0.25);
//        		                 }
//        		                 System.out.println("Pressing HOME");
//        		                 this.driver.findElement(By.id("dtOcorrencia_I")).sendKeys(Keys.HOME);
//        		                 Utils.waitv(2);
//        		                 System.out.println("Writing: "+sdfrn.format(tituloRecompra.getDataSolicitacao()));
//        		                 Utils.sendHumanKeys(this.driver.findElement(By.id("dtOcorrencia_I")), sdfrn.format(tituloRecompra.getDataSolicitacao()));

    		                // Thread.sleep(10000);

    		                // // Clica em OK
    		                System.out.println("Apertando ok do sistema.");
    		                 this.driver.findElement(By.id("btnOK_CD")).click();
    		                 Utils.waitv("Apertando OK do sistema",10);
    		                // Thread.sleep(10000);
    		                // // CHECK FOR SYSTEM POPUP
    		                 System.out.println("SYSTEM POPUP - CONFIRMAR BAIXA");

//    		                 for (int i = 0; i < 5; i++)
//    		                 {
    		                 pressSystemEnterKey();
//    			                 Utils.waitv("Enter!",4);
//    		                 }								
    		                 if(cedentesSemTarifaBaixa.get(tituloRecompra.getCedente().getApelido())!=null)
	                		 {
    		                	 System.out.println("Cedente NÃO COBRA TARIFA");
	                		 }
    		                 else {
    		                	 System.out.println("CedenteCOBRA TARIFA");
							}
    		                 System.out.println("");
    		                 pressSystemEscKey();    		                 
    		                 Utils.waitv(4);
       		                 
       		                 
    		                 int iTitulo=0;
    		                 for(TituloRecompra tituloRecompraBaixado:operacaoRecompra.getTitulosRecompra())
    	                     {
    	                        	if(tituloRecompraBaixado.getIdentificacaoTitulo().toLowerCase().contains(idTitulo.toLowerCase()))
    	                        	{
    	                        		System.out.println("     ----> Titulo: " + tituloRecompraBaixado.getIdentificacaoTitulo() + " efetuado!");
    	                        		operacaoRecompra.getTitulosRecompra().get(iTitulo).setEfetuado(true);
    	                        		operacaoRecompra.getTitulosRecompra().get(iTitulo).updateEfetuado(connMaria);
    	                        		actionTaked=true;
    	                        		break;
    	                        	}
    	                        	iTitulo++;
    	                     }
						}
                    }
               	 if(actionTaked)
                 {
               		iRow=-1;
               		try {
               			listRows = getListRows(this.driver.getPageSource());	
					} catch (Exception e) {
						e.printStackTrace();
					}
               		
                 	System.out.println("Getting out from rows loop!");
                 	Utils.waitv(3);
                 	break;
                 }
                }
                if(actionTaked)
                {
                	iRow=-1;
               		listRows = getListRows(this.driver.getPageSource());
                	System.out.println("Getting out from rows loop!");
                	Utils.waitv(3);
                	break;
                }
//                    if(filtersApplied)
//                    {
////                        System.out.println("Enabling capturing!");
////                        System.out.println("ListingEmpresasCedentes: "+listingEmpresasCedentes);
////                        System.out.println("Filter: "+filtersApplied);
////                        System.out.println("Capturing: "+capturingTitles);
////                        System.out.println("FirstReading: "+firstReading);
//                    	break;
//                    }
                iRow++;
                System.out.println("----- End of row!");
            }
            if(finalPage)
            {
            	todosBaixados=true;
            	allRead=true;           
            	allEfetuado=true;
            	break;
            }

            if (checkIfIsLastPage(this.driver.getPageSource()))
            {
            	
                System.out.println("Driver esta na pagina final. ");
                allRead=true;
            	System.out.println("ENTERING HERE AT THE FINAL PAGE!!");
            	finalPage=true;
            	listingEmpresasCedentes=false;
               allEfetuado=true;
				System.out.println("AllRead!! Finishing the process!");
				operacaoRecompra.setPago(true);		
				numberEndPages++;
            }
            else
            {
                gotoNextPage(iPage);
                iPage++;
            }
            if(allEfetuado)
            {
            	System.out.println("Todos baixados: "+allEfetuado);	
            }
            else {
            	allEfetuado=true;
            	for(TituloRecompra tr:operacaoRecompra.getTitulosRecompra())
            	{
            		if(!tr.isEfetuado())
            		{
            			allEfetuado=false;
            		}
            	}
				System.out.println("Nao foram encontrados todos os títulos desta recompra!");
			}
                            
            if(allRead && numberEndPages > numberOfTitles)
            {
            	break;
            }
        }
    }
    
    public ArrayList<OperacaoRecompra> leituraRecomprasValidasCedentesSemTarifa(Connection connMaria, Connection connMSS, HashMap<String, String> cedentesSemTarifaBaixa)
    {
    	
    	WebElement dataAtualElement = this.driver.findElement(By.id("ctl00_lbdataatual"));
    	Date dataAtual = null;
    	try {
			dataAtual = sdfr.parse(dataAtualElement.getText());
		} catch (ParseException e1) {
			e1.printStackTrace();
		}
    	
    	int numberDataAtual=Integer.parseInt(sdfn.format(dataAtual));
    	int numberActualDate = Integer.parseInt(sdfn.format(Calendar.getInstance().getTime()));
    	
    	System.out.println("NumberDateRGB: "+numberDataAtual);
    	System.out.println("NumberDateSystem: "+numberActualDate);
    	if(numberDataAtual!=numberActualDate)
    	{
    		System.out.println("Dates does not match!");
    		Utils.waitv(10);
//    		return null;
    	}
    	
    	System.out.println("###############################");
    	System.out.println("STARTING LEITURA RECOMPRA");
    	System.out.println("###############################");
    	ArrayList<OperacaoRecompra> operacoesRecompraRegistradas = OperacaoRecompra.loadOperacoesRecompra(connMaria, connMSS);
    	HashMap<String, OperacaoRecompra> operacoesRecompraValidos = new HashMap<>();
    	HashMap<String,TituloRecompra> titulosRecompraValidos = new HashMap<>();
    	HashMap<String,TituloRecompra> titulosSomenteBaixaSemRecompra= new HashMap<>();
    	for(OperacaoRecompra or:operacoesRecompraRegistradas)
    	{
    		String keyOperacaoRecompra=or.getEmpresa().getApelido()+or.getCedente().getApelido();
    		for(TituloRecompra tituloRecompra:or.getTitulosRecompra())
    		{
    			BuilderRecompraRGB.titulosEnviadosHoje.put(tituloRecompra.getIdentificacaoTitulo(), or.getIdOperacaoRecompra());
    		}
    	}
    	System.out.println("###############################");
    	System.out.println("TITULOS ENVIADOS HOJE");
    	for(String keyTitulo:BuilderRecompraRGB.titulosEnviadosHoje.keySet())
    	{
    		System.out.println("- "+keyTitulo+": OR: " +BuilderRecompraRGB.titulosEnviadosHoje.get(keyTitulo));
    	}
    	System.out.println("###############################");
    	    	
        // Baixa solicitacao WebFact
        String stringReturn = "";

        try 
        {
        	int iPage=0;
        	boolean filtersApplied=false;
        	boolean capturingTitles=false;
        	boolean firstReading=false;
        	boolean listingEmpresasCedentes=true;
//          this.driver.get("https://gercloud2.rgbsys.com.br/BMA_HOMOLOGA/operacoes/forms/BaixaSolicitacaoInternet.aspx?SessionId=");
          this.driver.get(this.rootURL+"/operacoes/forms/BaixaSolicitacaoInternet.aspx?SessionId=");
          Thread.sleep(10000);

          // Clica em pesquisar. Lista operacoes disponiveis
          this.driver.findElement(By.id("ctl00_contentManager_btnBuscarImg")).click();
          Utils.waitv(10);
//          Thread.sleep(10000);

          // Filtro abatimento
          this.driver.findElement(By.id("ctl00_contentManager_gridLiberacao_DXFREditorcol1_I")).sendKeys("Baixar");
//          Thread.sleep(10000);
          Utils.waitv("Filtro Baixar",12);
          HashMap<Integer, String> mapTableCols = getTableCols(this.driver.getPageSource());
          for(int iCol:mapTableCols.keySet())
          {
        	  System.out.println(iCol+": "+mapTableCols.get(iCol));
          }
          OperacaoRecompra operacaoRecompra = new OperacaoRecompra();
          String keyOperacaoRecompra="";
          String keyOperacaoRecompraTitulo="";
          Empresa empresa=null;
          Cedente cedente=null;
          boolean allRead=false;
          boolean actionTaked=false;
          while (true)
          {
//                // Filtro Apr = Nao
//                this.driver.findElement(By.id("ctl00$contentManager$gridLiberacao$DXFREditorcol8")).sendKeys("N");
//                Thread.sleep(10000);
//
//                makeTableHigher();
//                Thread.sleep(10000);
        	  Utils.waitv("Before reading the rows",3);
                List<String> listRows = getListRows(this.driver.getPageSource());
                if(listRows.size()==0)
                {
                	break;
                }
                int iRow=0;
                for (String stringRow : listRows)
                {
                	Utils.waitv(1);
                	System.out.println(iRow+"----- Start of the row");
                    System.out.println("ListingEmpresasCedentes: "+listingEmpresasCedentes);
                    System.out.println("Filter: "+filtersApplied);
                    System.out.println("Capturing: "+capturingTitles);
                    System.out.println("FirstReading: "+firstReading);

                    Document doc = Jsoup.parse(this.driver.getPageSource());
                    System.out.println(stringRow);
                    Element table = doc.getElementById(stringRow);
                    Elements listTableRows = table.select("tr");
                    boolean tituloAdicionado=false;
                    actionTaked=false;

                    // HashMap<Integer, String> mapTableCols = getTableCols(this.driver.getPageSource());
                    for (Element row : listTableRows)
                    {

                        String idTitulo = "";
                        String nomeEmpresa = "";
                        String nomeCedente = "";
                        String vencimentoString="";
                        String dataSolicitacaoString="";
                        String valorString="";
                        Boolean isApproved = true;
                        Integer rowElementCounter = 0;
                        Elements listRowDescriptions =  row.select("td");
                        listRowDescriptions.remove(0);

                        System.out.println("-------------------------");
                        System.out.println(row);
                        boolean hasToBeProcessed=false;
                        String tipoTitulo="";
                    	String idTituloDuplicata="";
                    	String idTituloCheque="";

                    	boolean hasToPayTarifaBaixa=true;
                        for (Element rowDescription : listRowDescriptions)
                        {
                        	hasToPayTarifaBaixa=true;
                        	System.out.println("RowElementCounter: "+rowElementCounter);
                            // Parsing idTitulo
                            if (mapTableCols.get(rowElementCounter).equals("Duplicata"))
                            {
                                idTituloDuplicata = rowDescription.text().trim();
                                System.out.println("idTituloDuplicata: "+idTituloDuplicata+" Size: "+idTituloDuplicata.length());
                            }
                            if (mapTableCols.get(rowElementCounter).equals("Cheque"))
                            {
                            	idTituloCheque = rowDescription.text().trim();
                            	System.out.println("idTituloCheque: "+idTituloCheque+" Size: "+idTituloCheque.length());
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
                                if(cedentesSemTarifaBaixa.get(nomeCedente.toUpperCase())!=null)
                                {
                                	hasToPayTarifaBaixa=false;
                                }
                            }
                            
                         // Parsing vencimento
                            if (mapTableCols.get(rowElementCounter).equals("Vcto. do Título"))
                            {
                                vencimentoString = rowDescription.text();
                            }
                            
                            //Data solicitacao
                            if (mapTableCols.get(rowElementCounter).equals("Data/Hora"))
                            {
                                dataSolicitacaoString = rowDescription.text();
                            }
                            
                            // Parsing valor
                            if (mapTableCols.get(rowElementCounter).equals("Valor"))
                            {
                                valorString = rowDescription.text();
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
                            { 
                            	break; 
                            }
                        }
                        
                        System.out.println("idTituloDuplicata: "+idTituloDuplicata);
                        System.out.println("idTituloCheque: "+idTituloCheque);
                        
                        if(idTituloDuplicata.length()>0)
                        {
                        	tipoTitulo="Duplicata";
                        	idTitulo=idTituloDuplicata;
                        }
                        if(idTituloCheque.length()>0)
                        {
                        	tipoTitulo="Cheque";
                        	idTitulo=idTituloCheque;
                        }
                        
                        System.out.println("TipoTitulo: "+tipoTitulo);
                        
                        System.out.println("idTitulo: "+idTitulo);
                        if(BuilderRecompraRGB.titulosEnviadosHoje.get(idTitulo)==null)
                        {
							System.out.println(">>TITULO: "+tipoTitulo+" "+idTitulo+" DEVE SER PROCESSADO");
                        	hasToBeProcessed=true;
                        }
                        else {
                        	System.out.println(">>TITULO: "+tipoTitulo+" "+idTitulo+ " já foi processado hoje!");
						}
                     
                        if(tipoTitulo.toLowerCase().contains("cheque"))
                        {
                        	System.out.println("Tipo de título CHEQUE, recompra deve ser rejeitada!");;
                        }
 
                        System.out.println("Filter: "+filtersApplied);
                        System.out.println("Capturing: "+capturingTitles);
                        System.out.println("FirstReading: "+firstReading);
                        
                        
                        
                        if(idTitulo.length()>0 
                        		&& nomeEmpresa.length()>0 
                        		&& nomeCedente.length() >0 
                        		&& vencimentoString.length()>0 
                        		&& valorString.length()>0 
                        		&& hasToBeProcessed)
                        {
                      	
                        	Date vencimento=null;
                        	try {
								vencimento=sdfr.parse(vencimentoString);
							} catch (ParseException e) {
								e.printStackTrace();
							}
                        	
                        	Date dataSolicitacao=null;
                        	
                        	try {
								dataSolicitacao=sdfh.parse(dataSolicitacaoString);
							} catch (ParseException e) {
								e.printStackTrace();
							}
                        	
                        	if(dataSolicitacao==null)
                        	{
                        		dataSolicitacao=Calendar.getInstance().getTime();
                        	}
                        	double limiteValor=100000.0;
                        	double valor=Double.parseDouble(valorString.replaceAll("\\.", "").replace(",", "."));
                        	empresa=new Empresa(connMaria, connMSS, nomeEmpresa);
                        	cedente=new Cedente(connMaria, connMSS, empresa, nomeCedente);
                            TituloInstrucao tituloInstrucao = new TituloInstrucao(empresa, cedente, idTitulo); 
    			            boolean forbidden=tituloInstrucao.checkProdutoProibido(connMSS); 
                        	System.out.println("Empresa: "+empresa.getRazaoSocial());
                        	System.out.println("Cedente: "+cedente.getParticipante().getRazaoSocial());
                        	System.out.println("IdentificacaoTitulo: "+idTitulo);
                        	System.out.println("Valor: "+valor);
                        	System.out.println("Vencimento: "+sdf.format(vencimento));
                        	System.out.println("DataSolicitacao: "+sdf.format(dataSolicitacao));
                        	System.out.println("TipoCobranca: "+tituloInstrucao.getTipoCobranca());
                        	
//                        	TituloRecompra tituloRecompra = new TituloRecompra(connMaria, operacaoRecompra, idTitulo, valor, vencimento,dataSolicitacao,false);
//                        	TituloRecompra tituloRecompraTemp = TituloRecompra.checkTitulo(connMSS, empresa, cedente, idTitulo, valor, vencimento, dataSolicitacao);
                        	TituloRecompra tituloRecompraTemp = new TituloRecompra(connMaria, connMSS, nomeEmpresa, nomeCedente, idTitulo,tipoTitulo, valor, vencimento, dataSolicitacao);
                        	tituloRecompraTemp.checkTitulo(connMSS, connMaria);
                        	
                        	if(!tituloRecompraTemp.isExists())
                        	{
                        		System.out.println("Configurando tituloRecompraTemp como baixado!");
                        		
                        		tituloRecompraTemp.setBaixado(true);
                        	}
                        	
                        	tituloRecompraTemp.show();
//                        	Utils.waitv(200);
                        	if( !tituloRecompraTemp.isQuitado()
                        			&& !tituloRecompraTemp.isEnviadoCartorio()
                        			&& !tituloRecompraTemp.isRecebimentoCartorio()
                        			&& !tituloRecompraTemp.isVencimentoDistante()
                        			&& !tituloRecompraTemp.isSolicitacaoAntiga()
                        			&& !forbidden
                        			&& tituloRecompraTemp.isAcatarBaixa()
                        			&& tituloRecompraTemp.getValor() <= limiteValor
                        			&& !tituloRecompraTemp.getTipoTitulo().toLowerCase().contains("cheque")
                        		)
                        	{
                        		if(!tituloRecompraTemp.isPodeRecomprar())
                        		{
                        			System.out.println("Titulo em aberto, adicionando à lista de títulos válidos para baixa mas não recompra");
                            		if(titulosSomenteBaixaSemRecompra.get(tituloRecompraTemp.getIdentificacaoTitulo())==null)
                            		{
                            			titulosSomenteBaixaSemRecompra.put(tituloRecompraTemp.getIdentificacaoTitulo(), tituloRecompraTemp);                    				
                    				}
                        		}
                        		else
                        		{
	                        		if(!BuilderRecompraRGB.exceptionsOnly)
	                        		{
	                            		System.out.println("Titulo em aberto, adicionando à lista de títulos válidos para operacao recompra");
	                            		if(titulosRecompraValidos.get(tituloRecompraTemp.getIdentificacaoTitulo())==null)
	                    				{
	                            			titulosRecompraValidos.put(tituloRecompraTemp.getIdentificacaoTitulo(),tituloRecompraTemp);
	                    				}
	                            		
	    	                        	tituloAdicionado=true;
	                        		}
                        		}
                        	}
                        	else 	if(!tituloRecompraTemp.isQuitado()
                        			&& !tituloRecompraTemp.isEnviadoCartorio()
                        			&& !tituloRecompraTemp.isRecebimentoCartorio()
                        			&& tituloRecompraTemp.isException()
                        			&& !forbidden
                        			&& !tituloRecompraTemp.isPodeRecomprar()
                        			&& !tituloRecompraTemp.getTipoTitulo().toLowerCase().contains("cheque")
                        			)
                        	{
                        		System.out.println("Titulo na lista de exeções, adicionando à lista da operacao recompra");
                        		if(titulosRecompraValidos.get(tituloRecompraTemp.getIdentificacaoTitulo())==null)
                				{
                        			titulosRecompraValidos.put(tituloRecompraTemp.getIdentificacaoTitulo(),tituloRecompraTemp);
                				}
	                        	tituloAdicionado=true;
                        	}
                        	else
                        	{
                        		System.out.println("Titulo não pode ser recomprado");
                        		String motivoString="";
                        		
                        		boolean chequeQuitado=false;
                        		
                        		if(tituloRecompraTemp.isCheque()&&tituloRecompraTemp.isQuitado())
                        		{
                        			chequeQuitado=true;
                        		}
                        		
                        		if(tituloRecompraTemp.getTipoTitulo().toLowerCase().contains("cheque"))
                        		{
                        			System.out.println("Título é cheque será rejeitado!");
									motivoString+="Recompra de cheque negada!";
                        		}
                        		
                        		
                        		if(tituloRecompraTemp.getValor() > limiteValor)
								{
									System.out.println("Título com valor acima do limite!");
									motivoString+="Título com valor acima do limite!";									
								}
                        		
                        		
                        		if(tituloRecompraTemp.isQuitado())
								{
									System.out.println("Título já quitado!");
									motivoString+="Título já quitado!";
									
								}

                        		if(tituloRecompraTemp.isBaixado())
								{
									System.out.println("Título já baixado!");
									motivoString+="Título já baixado!";
								}

								if(tituloRecompraTemp.isEnviadoCartorio())									
								{
									System.out.println("Enviado ao cartório!");
									motivoString+="Enviado ao cartório!";
								}
								if(tituloRecompraTemp.isRecebimentoCartorio())
								{
									System.out.println("Recebimento do cartório!");
									motivoString+="Recebimento do cartório!";
								}
								if(tituloRecompraTemp.isVencimentoDistante())
								{
									System.out.println("Vencimento muito distante");
									motivoString+="Vencimento muito distante!";
								}
								if(tituloRecompraTemp.isSolicitacaoAntiga())
								{
									System.out.println("Solicitação antiga!");
									motivoString+="Solicitação antiga!";
								}
								if(forbidden)
								{
									System.out.println("Produto " + tituloInstrucao.getNomeProduto().toUpperCase()+ " proibido para esta instrução!");
									motivoString+="Produto " + tituloInstrucao.getNomeProduto().toUpperCase()+ " proibido para esta instrução!";
								}
								if(tituloRecompraTemp.isAcatarBaixa() && !tituloRecompraTemp.isRecebimentoCartorio() && !chequeQuitado
										&& tituloRecompraTemp.getValor() < limiteValor
										&& !tituloRecompraTemp.isVencimentoDistante()
										&& !tituloRecompraTemp.isPodeRecomprar()
										)
								{
									// Realizar baixa
		                            System.out.println("Realizando baixa");
		                            String clickId = row.select("input").first().id() + "_D";
		                            assert(clickId.contains("ctl00_contentManager_gridLiberacao_DXSelBtn")) : "Id inconsistente";
		                            System.out.println("Clicando no elemento" +  clickId);
		                            this.driver.findElement(By.id(clickId)).click();
		                            Utils.waitv(10);

		                            if(!isApproved)
		                            {
			                            // Clica em aprovar
			    		                System.out.println("clicando em aprovar");
			    		                this.driver.findElement(By.id("ctl00_contentManager_btnAprova")).click();
			//                                Thread.sleep(10000);
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
								}
								else 
								{
									if(!tituloRecompraTemp.isExists())
									{
										motivoString="Titulo não existe no banco de dados!";
									}
									
									
									
	                        		System.out.println("Rejeitando RECOMPRA");
								    String clickId = row.select("input").first().id() + "_D";
								    assert(clickId.contains("ctl00_contentManager_gridLiberacao_DXSelBtn")) : "Id inconsistente";
								    System.out.println("Clicando no elemento" +  clickId);
								    this.driver.findElement(By.id(clickId)).click();
								    Utils.waitv(10);
									this.driver.findElement(By.id("ctl00_contentManager_btnRejeita_CD")).click();
									Utils.waitv(4);
									this.driver.switchTo().frame("ctl00_contentManager_popup_TODOS_CIF-1");
									System.out.println("Motivo: "+motivoString);
									this.driver.findElement(By.id("ctl00_contentManager_txtMotivo_I")).sendKeys(motivoString);
	
									Utils.waitv(2);
									this.driver.findElement(By.id("ctl00_contentManager_btnMotivo_CD")).click();
									Utils.waitv(4);
								}
								actionTaked=true;
                        	}
                        }
                        if(actionTaked)
                        {
                        	System.out.println("Getting out from rows loop!");
                        	Utils.waitv(5);
                        	break;
                        }
                    }
                    
                    if(actionTaked)
                    {
                    	System.out.println("Getting out from list row loop!");
                    	Utils.waitv(5);
                    	break;
                    }
                    iRow++;
                    System.out.println("----- End of row!");
                }
                
                if(!actionTaked)
                {
	                if(firstReading)                
	                {
	                	System.out.println("Since we are capturing, we prevent from breaking while once");
	                	firstReading=false;
	                }
	                else
	                {
		                iPage++;
		                if (checkIfIsLastPage(this.driver.getPageSource()))
		                {
		                	
		                    System.out.println("Driver esta na pagina final. ");
		                    allRead=true;
	                    	System.out.println("ENTERING HERE AT THE FINAL PAGE!!");
	                    	listingEmpresasCedentes=false;
							System.out.println("AllRead!! Finishing the process!");
		                }
		                else
		                {
		                    gotoNextPage(iPage);
		                }
	                }
	                if(allRead)
	                {
	                	break;
	                }
                }
            }
        }
        catch (InterruptedException ex) 
        {
            Thread.currentThread().interrupt();
        }
                              
       for(String identificacaoTitulo:titulosRecompraValidos.keySet())
       {
    	   String keyOperacaoRecompra=titulosRecompraValidos.get(identificacaoTitulo).getEmpresa().getApelido()+titulosRecompraValidos.get(identificacaoTitulo).getCedente().getApelido();
    	   System.out.println(" --------- Empresa: " +titulosRecompraValidos.get(identificacaoTitulo).getEmpresa().getApelido()
    			   + " Cedente: "+titulosRecompraValidos.get(identificacaoTitulo).getCedente().getApelido()
    			   + " IdTitulo: "+titulosRecompraValidos.get(identificacaoTitulo).getIdentificacaoTitulo());
    	   if(operacoesRecompraValidos.containsKey(keyOperacaoRecompra))
    	   {
    		   TituloRecompra tituloValido=new TituloRecompra(connMaria, connMSS, 
    				   																			operacoesRecompraValidos.get(keyOperacaoRecompra), 
    				   																			identificacaoTitulo, 
    				   																			titulosRecompraValidos.get(identificacaoTitulo).getTipoTitulo(),
    				   																			titulosRecompraValidos.get(identificacaoTitulo).getValor(), 
    				   																			titulosRecompraValidos.get(identificacaoTitulo).getVencimento(), 
    				   																			titulosRecompraValidos.get(identificacaoTitulo).getDataSolicitacao(), 
    				   																			true);
    		   tituloValido.checkTitulo(connMSS,connMaria);
    		   tituloValido.updateAfterCheck(connMaria);
    		   operacoesRecompraValidos.get(keyOperacaoRecompra).getTitulosRecompra().add(tituloValido);
    	   }
    	   else {
    		   OperacaoRecompra operacaoRecompra = new OperacaoRecompra(connMaria, 
    				   																		titulosRecompraValidos.get(identificacaoTitulo).getEmpresa(), 
    				   																		titulosRecompraValidos.get(identificacaoTitulo).getCedente(), true);
    		   operacoesRecompraValidos.put(keyOperacaoRecompra, operacaoRecompra);
    		   TituloRecompra tituloValido=new TituloRecompra(connMaria, connMSS, 
							operacoesRecompraValidos.get(keyOperacaoRecompra), 
							identificacaoTitulo, 
							titulosRecompraValidos.get(identificacaoTitulo).getTipoTitulo(),
							titulosRecompraValidos.get(identificacaoTitulo).getValor(), 
							titulosRecompraValidos.get(identificacaoTitulo).getVencimento(), 
							titulosRecompraValidos.get(identificacaoTitulo).getDataSolicitacao(), 
							true);
    		   tituloValido.checkTitulo(connMSS,connMaria);
    		   tituloValido.updateAfterCheck(connMaria);
    		   operacoesRecompraValidos.get(keyOperacaoRecompra).getTitulosRecompra().add(tituloValido);
    	   }
       }
       System.out.println("############################################");
       System.out.println("# OPERACOES RECOMPRA VALIDAS PARA SEREM GERADAS AS COBRANCAS #");
       System.out.println("############################################");
       ArrayList<OperacaoRecompra> listaOperacoesRecompra = new ArrayList<>();
       for(String keyOperacaoRecompra:operacoesRecompraValidos.keySet())
       {
    	   operacoesRecompraValidos.get(keyOperacaoRecompra).show();
    	   listaOperacoesRecompra.add(operacoesRecompraValidos.get(keyOperacaoRecompra));
       }
       Utils.waitv(6);
        
        System.out.println("******************************************************************************");
        System.out.println("OPERACOES RECOMPRA APÓS LEITURA DE TÍTULOS");
        for(String keyOperacaoRecompra: operacoesRecompraValidos.keySet())
        {
        	System.out.println("Operacao recompra: " + operacoesRecompraValidos.get(keyOperacaoRecompra).getEmpresa().getApelido()+ " - " + operacoesRecompraValidos.get(keyOperacaoRecompra).getCedente().getApelido());        	
        	if(operacoesRecompraValidos.get(keyOperacaoRecompra).getTitulosRecompra().size()>0)
        	{
        		System.out.println(">>>>> Processing OperacaoRecompra " + operacoesRecompraValidos.get(keyOperacaoRecompra).getEmpresa().getApelido() + "  -> "+operacoesRecompraValidos.get(keyOperacaoRecompra).getCedente().getApelido());
        		for(TituloRecompra tituloRecompra:operacoesRecompraValidos.get(keyOperacaoRecompra).getTitulosRecompra())
        		{
        			tituloRecompra.register(connMaria);
        			tituloRecompra.checkTitulo(connMSS,connMaria);
        			tituloRecompra.updateAfterCheck(connMaria);
        			System.out.println("Show after registering tituloRecompra");
        			tituloRecompra.show();
        			double valorCorrigido=0;
    				double tarifaBaixa=operacoesRecompraValidos.get(keyOperacaoRecompra).getCedente().getTarifaBaixa();
    				System.out.println("ValorOriginal: "+tituloRecompra.getValor());
    				System.out.println("ValorTotal: "+tituloRecompra.getValorTotal());
    				System.out.println("TarifaBaixa: "+tarifaBaixa);

        			if(tituloRecompra.isVencido())
        			{   			
        				System.out.println("Titulo Vencido");
        				long diasVencidos= TimeUnit.DAYS.convert(Calendar.getInstance().getTime().getTime()-tituloRecompra.getVencimento().getTime(), TimeUnit.MILLISECONDS);
        				System.out.println("DiasVencidos: "+diasVencidos);
        				double taxaMoraRecompra = operacoesRecompraValidos.get(keyOperacaoRecompra).getCedente().getTaxaMoraRecompra()/100.0; 				
       					System.out.println("Abatimento: "+tituloRecompra.getAbatimento());
       					System.out.println("Total: "+tituloRecompra.getValorTotal());
        				System.out.println("TaxaMoraRecompra: " + taxaMoraRecompra);
    					valorCorrigido=tituloRecompra.getValorTotal()*(1+diasVencidos*taxaMoraRecompra/30.0);        				
        				tituloRecompra.setValorCorrigido(valorCorrigido);
        				tituloRecompra.setValorCorrigido(Double.parseDouble(df.format(Math.round(tituloRecompra.getValorCorrigido()*100)/100.0)));
        				tituloRecompra.setMora(tituloRecompra.getCedente().getTarifaBaixa()+tituloRecompra.getValorCorrigido()-tituloRecompra.getValorTotal());
        				tituloRecompra.setValorRecompra(tituloRecompra.getValorCorrigido()+tarifaBaixa);
        				System.out.println("ValorOriginal: "+tituloRecompra.getValor());
        				System.out.println("ValorTotal: "+tituloRecompra.getValorTotal());
        				System.out.println("ValorRecompra: "+tituloRecompra.getValorRecompra());
        			}
        			else
        			{
        				System.out.println("Titulo a vencer ");
//        				tituloRecompra.setMora(tituloRecompra.getCedente().getTarifaBaixa());
        				tituloRecompra.setMora(0);
        				if(cedentesSemTarifaBaixa.get(tituloRecompra.getCedente().getApelido().toUpperCase())!=null)
        				{
        					tarifaBaixa=0;
        					System.out.println("TarifaBaixa não cobrada para este cedente");
        				}
        				tituloRecompra.setValorCorrigido(tituloRecompra.getValorTotal());
        				tituloRecompra.setMora(tarifaBaixa);
        				tituloRecompra.setValorRecompra(tituloRecompra.getValorCorrigido()+tarifaBaixa);
        			}        			
        			tituloRecompra.updateValues(connMaria);
        			System.out.println("Show after calculationg custs");
        			tituloRecompra.show();
        		}
        		
        		operacoesRecompraValidos.get(keyOperacaoRecompra).updateValue(connMaria);
        		operacoesRecompraValidos.get(keyOperacaoRecompra).show();
        		listaOperacoesRecompra.add(operacoesRecompraValidos.get(keyOperacaoRecompra));
        	}
        }
        return listaOperacoesRecompra;
    }    

    public ArrayList<OperacaoRecompra> leituraRecomprasValidas(Connection connMaria, Connection connMSS)
    {
    	
    	WebElement dataAtualElement = this.driver.findElement(By.id("ctl00_lbdataatual"));
    	Date dataAtual = null;
    	try {
			dataAtual = sdfr.parse(dataAtualElement.getText());
		} catch (ParseException e1) {
			e1.printStackTrace();
		}
    	
    	int numberDataAtual=Integer.parseInt(sdfn.format(dataAtual));
    	int numberActualDate = Integer.parseInt(sdfn.format(Calendar.getInstance().getTime()));
    	
    	System.out.println("NumberDateRGB: "+numberDataAtual);
    	System.out.println("NumberDateSystem: "+numberActualDate);
    	if(numberDataAtual!=numberActualDate)
    	{
    		System.out.println("Dates does not match!");
    		Utils.waitv(10);
//    		return null;
    	}
    	
    	System.out.println("###############################");
    	System.out.println("STARTING LEITURA RECOMPRA");
    	System.out.println("###############################");
    	ArrayList<OperacaoRecompra> operacoesRecompraRegistradas = OperacaoRecompra.loadOperacoesRecompra(connMaria, connMSS);
    	HashMap<String, OperacaoRecompra> operacoesRecompraValidos = new HashMap<>();
    	HashMap<String,TituloRecompra> titulosRecompraValidos = new HashMap<>();
    	HashMap<String,TituloRecompra> titulosSomenteBaixaSemRecompra= new HashMap<>();
    	for(OperacaoRecompra or:operacoesRecompraRegistradas)
    	{
    		String keyOperacaoRecompra=or.getEmpresa().getApelido()+or.getCedente().getApelido();
    		for(TituloRecompra tituloRecompra:or.getTitulosRecompra())
    		{
    			BuilderRecompraRGB.titulosEnviadosHoje.put(tituloRecompra.getIdentificacaoTitulo(), or.getIdOperacaoRecompra());
    		}
    	}
    	System.out.println("###############################");
    	System.out.println("TITULOS ENVIADOS HOJE");
    	for(String keyTitulo:BuilderRecompraRGB.titulosEnviadosHoje.keySet())
    	{
    		System.out.println("- "+keyTitulo+": OR: " +BuilderRecompraRGB.titulosEnviadosHoje.get(keyTitulo));
    	}
    	System.out.println("###############################");
    	    	
        // Baixa solicitacao WebFact
        String stringReturn = "";

        try 
        {
        	int iPage=0;
        	boolean filtersApplied=false;
        	boolean capturingTitles=false;
        	boolean firstReading=false;
        	boolean listingEmpresasCedentes=true;
//          this.driver.get("https://gercloud2.rgbsys.com.br/BMA_HOMOLOGA/operacoes/forms/BaixaSolicitacaoInternet.aspx?SessionId=");
          this.driver.get(this.rootURL+"/operacoes/forms/BaixaSolicitacaoInternet.aspx?SessionId=");
          Thread.sleep(10000);

          // Clica em pesquisar. Lista operacoes disponiveis
          this.driver.findElement(By.id("ctl00_contentManager_btnBuscarImg")).click();
          Utils.waitv(10);
//          Thread.sleep(10000);

          // Filtro abatimento
          this.driver.findElement(By.id("ctl00_contentManager_gridLiberacao_DXFREditorcol1_I")).sendKeys("Baixar");
//          Thread.sleep(10000);
          Utils.waitv("Filtro Baixar",12);
          HashMap<Integer, String> mapTableCols = getTableCols(this.driver.getPageSource());
          for(int iCol:mapTableCols.keySet())
          {
        	  System.out.println(iCol+": "+mapTableCols.get(iCol));
          }
          OperacaoRecompra operacaoRecompra = new OperacaoRecompra();
          String keyOperacaoRecompra="";
          String keyOperacaoRecompraTitulo="";
          Empresa empresa=null;
          Cedente cedente=null;
          boolean allRead=false;
          boolean actionTaked=false;
          while (true)
          {
//                // Filtro Apr = Nao
//                this.driver.findElement(By.id("ctl00$contentManager$gridLiberacao$DXFREditorcol8")).sendKeys("N");
//                Thread.sleep(10000);
//
//                makeTableHigher();
//                Thread.sleep(10000);
        	  Utils.waitv("Before reading the rows",3);
                List<String> listRows = getListRows(this.driver.getPageSource());
                if(listRows.size()==0)
                {
                	break;
                }
                int iRow=0;
                for (String stringRow : listRows)
                {
                	Utils.waitv(1);
                	System.out.println(iRow+"----- Start of the row");
                    System.out.println("ListingEmpresasCedentes: "+listingEmpresasCedentes);
                    System.out.println("Filter: "+filtersApplied);
                    System.out.println("Capturing: "+capturingTitles);
                    System.out.println("FirstReading: "+firstReading);

                    Document doc = Jsoup.parse(this.driver.getPageSource());
                    System.out.println(stringRow);
                    Element table = doc.getElementById(stringRow);
                    Elements listTableRows = table.select("tr");
                    boolean tituloAdicionado=false;
                    actionTaked=false;

                    // HashMap<Integer, String> mapTableCols = getTableCols(this.driver.getPageSource());
                    for (Element row : listTableRows)
                    {

                        String idTitulo = "";
                        String nomeEmpresa = "";
                        String nomeCedente = "";
                        String vencimentoString="";
                        String dataSolicitacaoString="";
                        String valorString="";
                        Boolean isApproved = true;
                        Integer rowElementCounter = 0;
                        Elements listRowDescriptions =  row.select("td");
                        listRowDescriptions.remove(0);

                        System.out.println("-------------------------");
                        System.out.println(row);
                        boolean hasToBeProcessed=false;
                        String tipoTitulo="";
                    	String idTituloDuplicata="";
                    	String idTituloCheque="";

                        for (Element rowDescription : listRowDescriptions)
                        {
                        	System.out.println("RowElementCounter: "+rowElementCounter);
                            // Parsing idTitulo
                            if (mapTableCols.get(rowElementCounter).equals("Duplicata"))
                            {
                                idTituloDuplicata = rowDescription.text().trim();
                                System.out.println("idTituloDuplicata: "+idTituloDuplicata+" Size: "+idTituloDuplicata.length());
                            }
                            if (mapTableCols.get(rowElementCounter).equals("Cheque"))
                            {
                            	idTituloCheque = rowDescription.text().trim();
                            	System.out.println("idTituloCheque: "+idTituloCheque+" Size: "+idTituloCheque.length());
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
                            
                         // Parsing vencimento
                            if (mapTableCols.get(rowElementCounter).equals("Vcto. do Título"))
                            {
                                vencimentoString = rowDescription.text();
                            }
                            
                            //Data solicitacao
                            if (mapTableCols.get(rowElementCounter).equals("Data/Hora"))
                            {
                                dataSolicitacaoString = rowDescription.text();
                            }
                            
                            // Parsing valor
                            if (mapTableCols.get(rowElementCounter).equals("Valor"))
                            {
                                valorString = rowDescription.text();
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
                            { 
                            	break; 
                            }
                        }
                        
                        System.out.println("idTituloDuplicata: "+idTituloDuplicata);
                        System.out.println("idTituloCheque: "+idTituloCheque);
                        
                        if(idTituloDuplicata.length()>0)
                        {
                        	tipoTitulo="Duplicata";
                        	idTitulo=idTituloDuplicata;
                        }
                        if(idTituloCheque.length()>0)
                        {
                        	tipoTitulo="Cheque";
                        	idTitulo=idTituloCheque;
                        }
                        
                        System.out.println("TipoTitulo: "+tipoTitulo);
                        
                        System.out.println("idTitulo: "+idTitulo);
                        if(BuilderRecompraRGB.titulosEnviadosHoje.get(idTitulo)==null)
                        {
							System.out.println(">>TITULO: "+tipoTitulo+" "+idTitulo+" DEVE SER PROCESSADO");
                        	hasToBeProcessed=true;
                        }
                        else {
                        	System.out.println(">>TITULO: "+tipoTitulo+" "+idTitulo+ " já foi processado hoje!");
						}
                     
                        if(tipoTitulo.toLowerCase().contains("cheque"))
                        {
                        	System.out.println("Tipo de título CHEQUE, recompra deve ser rejeitada!");;
                        }
 
                        System.out.println("Filter: "+filtersApplied);
                        System.out.println("Capturing: "+capturingTitles);
                        System.out.println("FirstReading: "+firstReading);
                        
                        
                        
                        if(idTitulo.length()>0 
                        		&& nomeEmpresa.length()>0 
                        		&& nomeCedente.length() >0 
                        		&& vencimentoString.length()>0 
                        		&& valorString.length()>0 
                        		&& hasToBeProcessed)
                        {
                      	
                        	Date vencimento=null;
                        	try {
								vencimento=sdfr.parse(vencimentoString);
							} catch (ParseException e) {
								e.printStackTrace();
							}
                        	
                        	Date dataSolicitacao=null;
                        	
                        	try {
								dataSolicitacao=sdfh.parse(dataSolicitacaoString);
							} catch (ParseException e) {
								e.printStackTrace();
							}
                        	
                        	if(dataSolicitacao==null)
                        	{
                        		dataSolicitacao=Calendar.getInstance().getTime();
                        	}
                        	double limiteValor=100000.0;
                        	double valor=Double.parseDouble(valorString.replaceAll("\\.", "").replace(",", "."));
                        	empresa=new Empresa(connMaria, connMSS, nomeEmpresa);
                        	cedente=new Cedente(connMaria, connMSS, empresa, nomeCedente);
                            TituloInstrucao tituloInstrucao = new TituloInstrucao(empresa, cedente, idTitulo); 
    			            boolean forbidden=tituloInstrucao.checkProdutoProibido(connMSS); 
                        	System.out.println("Empresa: "+empresa.getRazaoSocial());
                        	System.out.println("Cedente: "+cedente.getParticipante().getRazaoSocial());
                        	System.out.println("IdentificacaoTitulo: "+idTitulo);
                        	System.out.println("Valor: "+valor);
                        	System.out.println("Vencimento: "+sdf.format(vencimento));
                        	System.out.println("DataSolicitacao: "+sdf.format(dataSolicitacao));
                        	System.out.println("TipoCobranca: "+tituloInstrucao.getTipoCobranca());
                        	
//                        	TituloRecompra tituloRecompra = new TituloRecompra(connMaria, operacaoRecompra, idTitulo, valor, vencimento,dataSolicitacao,false);
//                        	TituloRecompra tituloRecompraTemp = TituloRecompra.checkTitulo(connMSS, empresa, cedente, idTitulo, valor, vencimento, dataSolicitacao);
                        	TituloRecompra tituloRecompraTemp = new TituloRecompra(connMaria, connMSS, nomeEmpresa, nomeCedente, idTitulo,tipoTitulo, valor, vencimento, dataSolicitacao);
                        	tituloRecompraTemp.checkTitulo(connMSS, connMaria);
                        	
                        	if(!tituloRecompraTemp.isExists())
                        	{
                        		System.out.println("Configurando tituloRecompraTemp como baixado!");
                        		
                        		tituloRecompraTemp.setBaixado(true);
                        	}
                        	
                        	tituloRecompraTemp.show();
//                        	Utils.waitv(200);
                        	if( !tituloRecompraTemp.isQuitado()
                        			&& !tituloRecompraTemp.isEnviadoCartorio()
                        			&& !tituloRecompraTemp.isRecebimentoCartorio()
                        			&& !tituloRecompraTemp.isVencimentoDistante()
                        			&& !tituloRecompraTemp.isSolicitacaoAntiga()
                        			&& !forbidden
                        			&& tituloRecompraTemp.isAcatarBaixa()
                        			&& tituloRecompraTemp.getValor() <= limiteValor
                        			&& !tituloRecompraTemp.getTipoTitulo().toLowerCase().contains("cheque")
                        		)
                        	{
                        		if(!tituloRecompraTemp.isPodeRecomprar())
                        		{
                        			System.out.println("Titulo em aberto, adicionando à lista de títulos válidos para baixa mas não recompra");
                            		if(titulosSomenteBaixaSemRecompra.get(tituloRecompraTemp.getIdentificacaoTitulo())==null)
                            		{
                            			titulosSomenteBaixaSemRecompra.put(tituloRecompraTemp.getIdentificacaoTitulo(), tituloRecompraTemp);                    				
                    				}
                        		}
                        		else
                        		{
	                        		if(!BuilderRecompraRGB.exceptionsOnly)
	                        		{
	                            		System.out.println("Titulo em aberto, adicionando à lista de títulos válidos para operacao recompra");
	                            		if(titulosRecompraValidos.get(tituloRecompraTemp.getIdentificacaoTitulo())==null)
	                    				{
	                            			titulosRecompraValidos.put(tituloRecompraTemp.getIdentificacaoTitulo(),tituloRecompraTemp);
	                    				}
	                            		
	    	                        	tituloAdicionado=true;
	                        		}
                        		}
                        	}
                        	else 	if(!tituloRecompraTemp.isQuitado()
                        			&& !tituloRecompraTemp.isEnviadoCartorio()
                        			&& !tituloRecompraTemp.isRecebimentoCartorio()
                        			&& tituloRecompraTemp.isException()
                        			&& !forbidden
                        			&& !tituloRecompraTemp.isPodeRecomprar()
                        			&& !tituloRecompraTemp.getTipoTitulo().toLowerCase().contains("cheque")
                        			)
                        	{
                        		System.out.println("Titulo na lista de exeções, adicionando à lista da operacao recompra");
                        		if(titulosRecompraValidos.get(tituloRecompraTemp.getIdentificacaoTitulo())==null)
                				{
                        			titulosRecompraValidos.put(tituloRecompraTemp.getIdentificacaoTitulo(),tituloRecompraTemp);
                				}
	                        	tituloAdicionado=true;
                        	}
                        	else
                        	{
                        		System.out.println("Titulo não pode ser recomprado");
                        		String motivoString="";
                        		
                        		boolean chequeQuitado=false;
                        		
                        		if(tituloRecompraTemp.isCheque()&&tituloRecompraTemp.isQuitado())
                        		{
                        			chequeQuitado=true;
                        		}
                        		
                        		if(tituloRecompraTemp.getTipoTitulo().toLowerCase().contains("cheque"))
                        		{
                        			System.out.println("Título é cheque será rejeitado!");
									motivoString+="Recompra de cheque negada!";
                        		}
                        		
                        		
                        		if(tituloRecompraTemp.getValor() > limiteValor)
								{
									System.out.println("Título com valor acima do limite!");
									motivoString+="Título com valor acima do limite!";									
								}
                        		
                        		
                        		if(tituloRecompraTemp.isQuitado())
								{
									System.out.println("Título já quitado!");
									motivoString+="Título já quitado!";
									
								}

                        		if(tituloRecompraTemp.isBaixado())
								{
									System.out.println("Título já baixado!");
									motivoString+="Título já baixado!";
								}

								if(tituloRecompraTemp.isEnviadoCartorio())									
								{
									System.out.println("Enviado ao cartório!");
									motivoString+="Enviado ao cartório!";
								}
								if(tituloRecompraTemp.isRecebimentoCartorio())
								{
									System.out.println("Recebimento do cartório!");
									motivoString+="Recebimento do cartório!";
								}
								if(tituloRecompraTemp.isVencimentoDistante())
								{
									System.out.println("Vencimento muito distante");
									motivoString+="Vencimento muito distante!";
								}
								if(tituloRecompraTemp.isSolicitacaoAntiga())
								{
									System.out.println("Solicitação antiga!");
									motivoString+="Solicitação antiga!";
								}
								if(forbidden)
								{
									System.out.println("Produto " + tituloInstrucao.getNomeProduto().toUpperCase()+ " proibido para esta instrução!");
									motivoString+="Produto " + tituloInstrucao.getNomeProduto().toUpperCase()+ " proibido para esta instrução!";
								}
								if(tituloRecompraTemp.isAcatarBaixa() && !tituloRecompraTemp.isRecebimentoCartorio() && !chequeQuitado
										&& tituloRecompraTemp.getValor() < limiteValor
										&& !tituloRecompraTemp.isVencimentoDistante()
										&& !tituloRecompraTemp.isPodeRecomprar()
										)
								{
									// Realizar baixa
		                            System.out.println("Realizando baixa");
		                            String clickId = row.select("input").first().id() + "_D";
		                            assert(clickId.contains("ctl00_contentManager_gridLiberacao_DXSelBtn")) : "Id inconsistente";
		                            System.out.println("Clicando no elemento" +  clickId);
		                            this.driver.findElement(By.id(clickId)).click();
		                            Utils.waitv(10);

		                            if(!isApproved)
		                            {
			                            // Clica em aprovar
			    		                System.out.println("clicando em aprovar");
			    		                this.driver.findElement(By.id("ctl00_contentManager_btnAprova")).click();
			//                                Thread.sleep(10000);
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
								}
								else 
								{
									if(!tituloRecompraTemp.isExists())
									{
										motivoString="Titulo não existe no banco de dados!";
									}
									
									
									
	                        		System.out.println("Rejeitando RECOMPRA");
								    String clickId = row.select("input").first().id() + "_D";
								    assert(clickId.contains("ctl00_contentManager_gridLiberacao_DXSelBtn")) : "Id inconsistente";
								    System.out.println("Clicando no elemento" +  clickId);
								    this.driver.findElement(By.id(clickId)).click();
								    Utils.waitv(10);
									this.driver.findElement(By.id("ctl00_contentManager_btnRejeita_CD")).click();
									Utils.waitv(4);
									this.driver.switchTo().frame("ctl00_contentManager_popup_TODOS_CIF-1");
									System.out.println("Motivo: "+motivoString);
									this.driver.findElement(By.id("ctl00_contentManager_txtMotivo_I")).sendKeys(motivoString);
	
									Utils.waitv(2);
									this.driver.findElement(By.id("ctl00_contentManager_btnMotivo_CD")).click();
									Utils.waitv(4);
								}
								actionTaked=true;
                        	}
                        }
                        if(actionTaked)
                        {
                        	System.out.println("Getting out from rows loop!");
                        	Utils.waitv(5);
                        	break;
                        }
                    }
                    
                    if(actionTaked)
                    {
                    	System.out.println("Getting out from list row loop!");
                    	Utils.waitv(5);
                    	break;
                    }
                    iRow++;
                    System.out.println("----- End of row!");
                }
                
                if(!actionTaked)
                {
	                if(firstReading)                
	                {
	                	System.out.println("Since we are capturing, we prevent from breaking while once");
	                	firstReading=false;
	                }
	                else
	                {
		                iPage++;
		                if (checkIfIsLastPage(this.driver.getPageSource()))
		                {
		                	
		                    System.out.println("Driver esta na pagina final. ");
		                    allRead=true;
	                    	System.out.println("ENTERING HERE AT THE FINAL PAGE!!");
	                    	listingEmpresasCedentes=false;
							System.out.println("AllRead!! Finishing the process!");
		                }
		                else
		                {
		                    gotoNextPage(iPage);
		                }
	                }
	                if(allRead)
	                {
	                	break;
	                }
                }
            }
        }
        catch (InterruptedException ex) 
        {
            Thread.currentThread().interrupt();
        }
                              
       for(String identificacaoTitulo:titulosRecompraValidos.keySet())
       {
    	   String keyOperacaoRecompra=titulosRecompraValidos.get(identificacaoTitulo).getEmpresa().getApelido()+titulosRecompraValidos.get(identificacaoTitulo).getCedente().getApelido();
    	   System.out.println(" --------- Empresa: " +titulosRecompraValidos.get(identificacaoTitulo).getEmpresa().getApelido()
    			   + " Cedente: "+titulosRecompraValidos.get(identificacaoTitulo).getCedente().getApelido()
    			   + " IdTitulo: "+titulosRecompraValidos.get(identificacaoTitulo).getIdentificacaoTitulo());
    	   if(operacoesRecompraValidos.containsKey(keyOperacaoRecompra))
    	   {
    		   TituloRecompra tituloValido=new TituloRecompra(connMaria, connMSS, 
    				   																			operacoesRecompraValidos.get(keyOperacaoRecompra), 
    				   																			identificacaoTitulo, 
    				   																			titulosRecompraValidos.get(identificacaoTitulo).getTipoTitulo(),
    				   																			titulosRecompraValidos.get(identificacaoTitulo).getValor(), 
    				   																			titulosRecompraValidos.get(identificacaoTitulo).getVencimento(), 
    				   																			titulosRecompraValidos.get(identificacaoTitulo).getDataSolicitacao(), 
    				   																			true);
    		   tituloValido.checkTitulo(connMSS,connMaria);
    		   tituloValido.updateAfterCheck(connMaria);
    		   operacoesRecompraValidos.get(keyOperacaoRecompra).getTitulosRecompra().add(tituloValido);
    	   }
    	   else {
    		   OperacaoRecompra operacaoRecompra = new OperacaoRecompra(connMaria, 
    				   																		titulosRecompraValidos.get(identificacaoTitulo).getEmpresa(), 
    				   																		titulosRecompraValidos.get(identificacaoTitulo).getCedente(), true);
    		   operacoesRecompraValidos.put(keyOperacaoRecompra, operacaoRecompra);
    		   TituloRecompra tituloValido=new TituloRecompra(connMaria, connMSS, 
							operacoesRecompraValidos.get(keyOperacaoRecompra), 
							identificacaoTitulo, 
							titulosRecompraValidos.get(identificacaoTitulo).getTipoTitulo(),
							titulosRecompraValidos.get(identificacaoTitulo).getValor(), 
							titulosRecompraValidos.get(identificacaoTitulo).getVencimento(), 
							titulosRecompraValidos.get(identificacaoTitulo).getDataSolicitacao(), 
							true);
    		   tituloValido.checkTitulo(connMSS,connMaria);
    		   tituloValido.updateAfterCheck(connMaria);
    		   operacoesRecompraValidos.get(keyOperacaoRecompra).getTitulosRecompra().add(tituloValido);
    	   }
       }
       System.out.println("############################################");
       System.out.println("# OPERACOES RECOMPRA VALIDAS PARA SEREM GERADAS AS COBRANCAS #");
       System.out.println("############################################");
       ArrayList<OperacaoRecompra> listaOperacoesRecompra = new ArrayList<>();
       for(String keyOperacaoRecompra:operacoesRecompraValidos.keySet())
       {
    	   operacoesRecompraValidos.get(keyOperacaoRecompra).show();
    	   listaOperacoesRecompra.add(operacoesRecompraValidos.get(keyOperacaoRecompra));
       }
       Utils.waitv(6);
        
        System.out.println("******************************************************************************");
        System.out.println("OPERACOES RECOMPRA APÓS LEITURA DE TÍTULOS");
        for(String keyOperacaoRecompra: operacoesRecompraValidos.keySet())
        {
        	System.out.println("Operacao recompra: " + operacoesRecompraValidos.get(keyOperacaoRecompra).getEmpresa().getApelido()+ " - " + operacoesRecompraValidos.get(keyOperacaoRecompra).getCedente().getApelido());        	
        	if(operacoesRecompraValidos.get(keyOperacaoRecompra).getTitulosRecompra().size()>0)
        	{
        		System.out.println(">>>>> Processing OperacaoRecompra " + operacoesRecompraValidos.get(keyOperacaoRecompra).getEmpresa().getApelido() + "  -> "+operacoesRecompraValidos.get(keyOperacaoRecompra).getCedente().getApelido());
        		for(TituloRecompra tituloRecompra:operacoesRecompraValidos.get(keyOperacaoRecompra).getTitulosRecompra())
        		{
        			tituloRecompra.register(connMaria);
        			tituloRecompra.checkTitulo(connMSS,connMaria);
        			tituloRecompra.updateAfterCheck(connMaria);
        			System.out.println("Show after registering tituloRecompra");
        			tituloRecompra.show();
        			if(tituloRecompra.isVencido())
        			{
        				System.out.println("ValorOriginal: "+tituloRecompra.getValor());
        				System.out.println("ValorTotal: "+tituloRecompra.getValorTotal());
        				System.out.println("Titulo Vencido");
        				long diasVencidos= TimeUnit.DAYS.convert(Calendar.getInstance().getTime().getTime()-tituloRecompra.getVencimento().getTime(), TimeUnit.MILLISECONDS);
        				System.out.println("DiasVencidos: "+diasVencidos);
        				double taxaMoraRecompra = operacoesRecompraValidos.get(keyOperacaoRecompra).getCedente().getTaxaMoraRecompra()/100.0;
        				double tarifaBaixa=operacoesRecompraValidos.get(keyOperacaoRecompra).getCedente().getTarifaBaixa();
        				double valorCorrigido=0;
       					System.out.println("Abatimento: "+tituloRecompra.getAbatimento());
       					System.out.println("Total: "+tituloRecompra.getValorTotal());
        				System.out.println("TaxaMoraRecompra: " + taxaMoraRecompra);
        				System.out.println("TarifaBaixa: "+tarifaBaixa);
    					valorCorrigido=tituloRecompra.getValorTotal()*(1+diasVencidos*taxaMoraRecompra/30.0);        				
        				tituloRecompra.setValorCorrigido(valorCorrigido);
        				tituloRecompra.setValorCorrigido(Double.parseDouble(df.format(Math.round(tituloRecompra.getValorCorrigido()*100)/100.0)));
        				tituloRecompra.setMora(tituloRecompra.getCedente().getTarifaBaixa()+tituloRecompra.getValorCorrigido()-tituloRecompra.getValorTotal());
        				tituloRecompra.setValorRecompra(tituloRecompra.getValorCorrigido()+tarifaBaixa);
        				System.out.println("ValorOriginal: "+tituloRecompra.getValor());
        				System.out.println("ValorTotal: "+tituloRecompra.getValorTotal());
        				System.out.println("ValorRecompra: "+tituloRecompra.getValorRecompra());
        			}
        			else
        			{
        				System.out.println("Titulo a vencer ");
//        				tituloRecompra.setMora(tituloRecompra.getCedente().getTarifaBaixa());
        				tituloRecompra.setMora(0);
        				tituloRecompra.setValorCorrigido(tituloRecompra.getValorTotal());
        				tituloRecompra.setValorRecompra(tituloRecompra.getValorCorrigido());
        			}        			
        			tituloRecompra.updateValues(connMaria);
        			System.out.println("Show after calculationg custs");
        			tituloRecompra.show();
        		}
        		
        		operacoesRecompraValidos.get(keyOperacaoRecompra).updateValue(connMaria);
        		operacoesRecompraValidos.get(keyOperacaoRecompra).show();
        		listaOperacoesRecompra.add(operacoesRecompraValidos.get(keyOperacaoRecompra));
        	}
        }
        return listaOperacoesRecompra;
    }    
    
    public ArrayList<OperacaoRecompra> leituraRecompra(Connection connMaria, Connection connMSS, boolean clean)
    {
    	
    	WebElement dataAtualElement = this.driver.findElement(By.id("ctl00_lbdataatual"));
    	Date dataAtual = null;
    	try {
			dataAtual = sdfr.parse(dataAtualElement.getText());
		} catch (ParseException e1) {
			e1.printStackTrace();
		}
    	
    	int numberDataAtual=Integer.parseInt(sdfn.format(dataAtual));
    	int numberActualDate = Integer.parseInt(sdfn.format(Calendar.getInstance().getTime()));
    	
    	System.out.println("NumberDateRGB: "+numberDataAtual);
    	System.out.println("NumberDateSystem: "+numberActualDate);
    	if(numberDataAtual!=numberActualDate)
    	{
    		System.out.println("Dates does not match!");
    		Utils.waitv(10);
//    		return null;
    	}
    	
    	System.out.println("###############################");
    	System.out.println("STARTING LEITURA RECOMPRA");
    	System.out.println("###############################");
    	ArrayList<OperacaoRecompra> operacoesRecompraRegistradas = OperacaoRecompra.loadOperacoesRecompra(connMaria, connMSS);
    	HashMap<String, OperacaoRecompra> operacoesRecompraTemporarios = new HashMap<>();
    	HashMap<String, OperacaoRecompra> operacoesRecompraValidos = new HashMap<>();
    	HashMap<String, HashMap<String, Boolean>> empresasCedente = new HashMap<>();
    	for(OperacaoRecompra or:operacoesRecompraRegistradas)
    	{
    		String keyOperacaoRecompra=or.getEmpresa().getApelido()+or.getCedente().getApelido();
    		for(TituloRecompra tituloRecompra:or.getTitulosRecompra())
    		{
    			BuilderRecompraRGB.titulosEnviadosHoje.put(tituloRecompra.getIdentificacaoTitulo(), or.getIdOperacaoRecompra());
    		}
    	}
    	System.out.println("###############################");
    	System.out.println("TITULOS ENVIADOS HOJE");
    	for(String keyTitulo:BuilderRecompraRGB.titulosEnviadosHoje.keySet())
    	{
    		System.out.println("- "+keyTitulo+": OR: " +BuilderRecompraRGB.titulosEnviadosHoje.get(keyTitulo));
    	}
    	System.out.println("###############################");
    	    	
        // Baixa solicitacao WebFact
        String stringReturn = "";

        try 
        {
        	int iPage=0;
        	boolean filtersApplied=false;
        	boolean capturingTitles=false;
        	boolean firstReading=false;
        	boolean listingEmpresasCedentes=true;
//          this.driver.get("https://gercloud2.rgbsys.com.br/BMA_HOMOLOGA/operacoes/forms/BaixaSolicitacaoInternet.aspx?SessionId=");
          this.driver.get(this.rootURL+"/operacoes/forms/BaixaSolicitacaoInternet.aspx?SessionId=");
          Thread.sleep(10000);

          // Clica em pesquisar. Lista operacoes disponiveis
          this.driver.findElement(By.id("ctl00_contentManager_btnBuscarImg")).click();
          Utils.waitv(10);
//          Thread.sleep(10000);

          // Filtro abatimento
          this.driver.findElement(By.id("ctl00_contentManager_gridLiberacao_DXFREditorcol1_I")).sendKeys("Baixar");
//          Thread.sleep(10000);
          Utils.waitv("Filtro Baixar",12);
          HashMap<Integer, String> mapTableCols = getTableCols(this.driver.getPageSource());
          for(int iCol:mapTableCols.keySet())
          {
        	  System.out.println(iCol+": "+mapTableCols.get(iCol));
          }
          Empresa empresa = new Empresa();
          Cedente cedente = new Cedente();          
          OperacaoRecompra operacaoRecompra = new OperacaoRecompra();
          String keyOperacaoRecompra="";
          String keyOperacaoRecompraTitulo="";
          boolean allRead=false;
          boolean actionTaked=false;
          while (true)
          {
//                // Filtro Apr = Nao
//                this.driver.findElement(By.id("ctl00$contentManager$gridLiberacao$DXFREditorcol8")).sendKeys("N");
//                Thread.sleep(10000);
//
//                makeTableHigher();
//                Thread.sleep(10000);
        	  Utils.waitv("Before reading the rows",3);
                List<String> listRows = getListRows(this.driver.getPageSource());
                int iRow=0;
                for (String stringRow : listRows)
                {
                	System.out.println(iRow+"----- Start of the row");
                    System.out.println("ListingEmpresasCedentes: "+listingEmpresasCedentes);
                    System.out.println("Filter: "+filtersApplied);
                    System.out.println("Capturing: "+capturingTitles);
                    System.out.println("FirstReading: "+firstReading);

                    Document doc = Jsoup.parse(this.driver.getPageSource());
                    System.out.println(stringRow);
                    Element table = doc.getElementById(stringRow);
                    Elements listTableRows = table.select("tr");
                    boolean tituloAdicionado=false;
                    actionTaked=false;

                    // HashMap<Integer, String> mapTableCols = getTableCols(this.driver.getPageSource());
                    for (Element row : listTableRows)
                    {

                        String idTitulo = "";
                        String nomeEmpresa = "";
                        String nomeCedente = "";
                        String vencimentoString="";
                        String dataSolicitacaoString="";
                        String valorString="";
                        Boolean isApproved = true;
                        Integer rowElementCounter = 0;
                        Elements listRowDescriptions =  row.select("td");
                        listRowDescriptions.remove(0);

                        System.out.println("-------------------------");
                        System.out.println(row);
                        boolean hasToBeProcessed=false;

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
                            
                         // Parsing vencimento
                            if (mapTableCols.get(rowElementCounter).equals("Vcto. do Título"))
                            {
                                vencimentoString = rowDescription.text();
                            }
                            
                            //Data solicitacao
                            if (mapTableCols.get(rowElementCounter).equals("Data"))
                            {
                                dataSolicitacaoString = rowDescription.text();
                            }
                            
                            // Parsing valor
                            if (mapTableCols.get(rowElementCounter).equals("Valor"))
                            {
                                valorString = rowDescription.text();
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
                            { 
                            	break; 
                            }
                        }
                        
                        if(BuilderRecompraRGB.titulosEnviadosHoje.get(idTitulo)==null)
                        {
							System.out.println(">>TITULO: "+idTitulo+" DEVE SER PROCESSADO");
                        	hasToBeProcessed=true;
                        }
                        else {
							System.out.println(">>TITULO: "+idTitulo+" JÁ FOI PROCESSADO HOJE");
						}
                        
 
                        System.out.println("ListingEmpresasCedentes: "+listingEmpresasCedentes);
                        System.out.println("Filter: "+filtersApplied);
                        System.out.println("Capturing: "+capturingTitles);
                        System.out.println("FirstReading: "+firstReading);
                        
                        if(idTitulo.length()>0 
                        		&& nomeEmpresa.length()>0 
                        		&& nomeCedente.length() >0 
                        		&& vencimentoString.length()>0 
                        		&& valorString.length()>0 
                        		&& hasToBeProcessed)
                        {
                        	keyOperacaoRecompraTitulo=nomeEmpresa+nomeCedente+idTitulo;
                            if(nomeCedente.length()>0 && nomeEmpresa.length()>0&&listingEmpresasCedentes)
                            {
                            	if(empresasCedente.get(nomeEmpresa)==null)
                        		{
                            		System.out.println("First time empresa: "+nomeEmpresa);
                        			HashMap<String, Boolean> cedentes = new HashMap<>();
                        			cedentes.put(nomeCedente,false);
                        			System.out.println("Adding cedente: "+nomeCedente);
                        			empresasCedente.put(nomeEmpresa, cedentes);
                        		}
                        		else {
                    				if(empresasCedente.get(nomeEmpresa).get(nomeCedente)==null)
                    				{
                    					empresasCedente.get(nomeEmpresa).put(nomeCedente, false);
                    					System.out.println("Adding cedente: "+nomeCedente + " on empresa: " +nomeEmpresa);
                    				}
                    			}
                            }
                            
                            
                            if(nomeCedente.length() >0 && nomeEmpresa.length()>0)
                            {                            	
                                empresa = new Empresa(connMaria, connMSS, nomeEmpresa);
                                cedente = new Cedente(connMaria, connMSS, empresa, nomeCedente);

                            	keyOperacaoRecompra=empresa.getApelido()+cedente.getApelido();

    							if(hasToBeProcessed)
    							{
                                	if(operacoesRecompraTemporarios.get(keyOperacaoRecompra)==null)
                                	{
                                        operacaoRecompra = new OperacaoRecompra(connMaria, empresa, cedente,true, 0);
                                        operacoesRecompraTemporarios.put(keyOperacaoRecompra, operacaoRecompra);                                    
                                	}
    							}
                            }                        	
                        	
                        	Date vencimento=null;
                        	try {
								vencimento=sdfr.parse(vencimentoString);
							} catch (ParseException e) {
								e.printStackTrace();
							}
                        	
                        	Date dataSolicitacao=null;
                        	
                        	try {
								dataSolicitacao=sdfr.parse(dataSolicitacaoString);
							} catch (ParseException e) {
								e.printStackTrace();
							}
                        	
                        	double valor=Double.parseDouble(valorString.replaceAll("\\.", "").replace(",", "."));
                        	System.out.println("Empresa: "+empresa.getRazaoSocial());
                        	System.out.println("Cedente: "+cedente.getParticipante().getRazaoSocial());
                        	System.out.println("IdentificacaoTitulo: "+idTitulo);
                        	System.out.println("Valor: "+valor);
                        	System.out.println("Vencimento: "+sdf.format(vencimento));
                        	
//                        	TituloRecompra tituloRecompra = new TituloRecompra(connMaria, operacaoRecompra, idTitulo, valor, vencimento,dataSolicitacao,false);
                        	TituloRecompra tituloRecompraTemp = TituloRecompra.checkTitulo(connMSS, empresa, cedente, idTitulo, valor, vencimento, dataSolicitacao);
                        	tituloRecompraTemp.checkTitulo(connMSS,connMaria);
                        	tituloRecompraTemp.show();
//                        	Utils.waitv(200);
                        	if( !tituloRecompraTemp.isQuitado()
                        			&& !tituloRecompraTemp.isEnviadoCartorio()
                        			&& !tituloRecompraTemp.isRecebimentoCartorio()
                        			&& !tituloRecompraTemp.isVencimentoDistante()
                        			&& !tituloRecompraTemp.isSolicitacaoAntiga()
                        		)
                        	{
                        		if(!BuilderRecompraRGB.exceptionsOnly)
                        		{
                            		System.out.println("Titulo em aberto, adicionando à lista da operacao recompra");	
                            		if(keyOperacaoRecompraTitulo.startsWith(keyOperacaoRecompra))
                            		{
	                            		TituloRecompra tituloRecompra = new TituloRecompra(connMaria, connMSS, operacaoRecompra, idTitulo, valor, vencimento,dataSolicitacao,false);
	    	                        	operacoesRecompraTemporarios.get(keyOperacaoRecompra).getTitulosRecompra().add(tituloRecompra);
                            		}
    	                        	tituloAdicionado=true;
                        		}
                        	}
                        	else 	if(!tituloRecompraTemp.isQuitado()
                        			&& !tituloRecompraTemp.isEnviadoCartorio()
                        			&& !tituloRecompraTemp.isRecebimentoCartorio()
                        			&& tituloRecompraTemp.isException()
                        			)
                        	{
                        		System.out.println("Titulo na lista de exeções, adicionando à lista da operacao recompra");
                        		if(keyOperacaoRecompraTitulo.startsWith(keyOperacaoRecompra))
                        		{
                        			operacoesRecompraTemporarios.get(keyOperacaoRecompra).register(connMaria);
	                        		TituloRecompra tituloRecompra = new TituloRecompra(connMaria, connMSS, operacaoRecompra, idTitulo, valor, vencimento,dataSolicitacao,false);
		                        	operacoesRecompraTemporarios.get(keyOperacaoRecompra).getTitulosRecompra().add(tituloRecompra);
                        		}
	                        	tituloAdicionado=true;
                        	}
                        	else
                        	{
                        		System.out.println("Titulo não pode ser recomprado");
                        		if(tituloRecompraTemp.isVencido())
								{
									System.out.println("Título já baixado!");
								}
								if(tituloRecompraTemp.isEnviadoCartorio())									
								{
									System.out.println("Enviado ao cartório!");
								}
								if(tituloRecompraTemp.isRecebimentoCartorio())
								{
									System.out.println("Recebimento do cartório!");
								}
								if(tituloRecompraTemp.isVencimentoDistante())
								{
									System.out.println("Recebimento do cartório!");
								}
								if(tituloRecompraTemp.isSolicitacaoAntiga())
								{
									System.out.println("Recebimento do cartório!");
								}
                        		System.out.println("Rejeitando RECOMPRA");
							    String clickId = row.select("input").first().id() + "_D";
							    assert(clickId.contains("ctl00_contentManager_gridLiberacao_DXSelBtn")) : "Id inconsistente";
							    System.out.println("Clicando no elemento" +  clickId);
							    this.driver.findElement(By.id(clickId)).click();
							    Utils.waitv(10);
								this.driver.findElement(By.id("ctl00_contentManager_btnRejeita_CD")).click();
								Utils.waitv(4);
								this.driver.switchTo().frame("ctl00_contentManager_popup_TODOS_CIF-1");
								if(tituloRecompraTemp.isVencido())
								{
									System.out.println("Título já baixado!");
									this.driver.findElement(By.id("ctl00_contentManager_txtMotivo_I")).sendKeys("Titulo já baixado");
								}
								if(tituloRecompraTemp.isEnviadoCartorio())									
								{
									System.out.println("Enviado ao cartório!");
									this.driver.findElement(By.id("ctl00_contentManager_txtMotivo_I")).sendKeys("Enviado ao cartório");
								}
								if(tituloRecompraTemp.isRecebimentoCartorio())
								{
									System.out.println("Recebimento do cartório!");
									this.driver.findElement(By.id("ctl00_contentManager_txtMotivo_I")).sendKeys("Recebimento do cartório");
								}
								if(tituloRecompraTemp.isVencimentoDistante())
								{
									System.out.println("Recebimento do cartório!");
									this.driver.findElement(By.id("ctl00_contentManager_txtMotivo_I")).sendKeys("Vencimento muito distante");
								}
								if(tituloRecompraTemp.isSolicitacaoAntiga())
								{
									System.out.println("Recebimento do cartório!");
									this.driver.findElement(By.id("ctl00_contentManager_txtMotivo_I")).sendKeys("Solicitação muito antiga");
								}
								Utils.waitv(2);
								this.driver.findElement(By.id("ctl00_contentManager_btnMotivo_CD")).click();
								Utils.waitv(4);
								actionTaked=true;
                        	}
                        }
                        if(actionTaked)
                        {
                        	System.out.println("Getting out from rows loop!");
                        	Utils.waitv(5);
                        	break;
                        }
                    }
                    
                    if(actionTaked)
                    {
                    	System.out.println("Getting out from list row loop!");
                    	Utils.waitv(5);
                    	break;
                    }
                    iRow++;
                    System.out.println("----- End of row!");
                }
                
                if(!actionTaked)
                {
	                if(firstReading)                
	                {
	                	System.out.println("Since we are capturing, we prevent from breaking while once");
	                	firstReading=false;
	                }
	                else
	                {
		                iPage++;
		                if (checkIfIsLastPage(this.driver.getPageSource()))
		                {
		                	
		                    System.out.println("Driver esta na pagina final. ");
		                    allRead=true;
	                    	System.out.println("ENTERING HERE AT THE FINAL PAGE!!");
	                    	listingEmpresasCedentes=false;
							System.out.println("AllRead!! Finishing the process!");
		                }
		                else
		                {
		                    gotoNextPage(iPage);
		                }
	                }
	                if(allRead)
	                {
	                	break;
	                }
                }
            }
        }
        catch (InterruptedException ex) 
        {
            Thread.currentThread().interrupt();
        }
        
        
        for(String keyOperacaoRecompra: operacoesRecompraTemporarios.keySet())
        {
        	if( operacoesRecompraTemporarios.get(keyOperacaoRecompra).getTitulosRecompra().size()>0)
        	{
        		operacoesRecompraTemporarios.get(keyOperacaoRecompra).register(connMaria);
        		operacoesRecompraValidos.put(keyOperacaoRecompra, operacoesRecompraTemporarios.get(keyOperacaoRecompra));
        	}
        }
        
        ArrayList<OperacaoRecompra> listaOperacoesRecompra = new ArrayList<>();
        System.out.println("******************************************************************************");
        System.out.println("OPERACOES RECOMPRA APÓS LEITURA DE TÍTULOS");
        for(String keyOperacaoRecompra: operacoesRecompraValidos.keySet())
        {
        	System.out.println("Operacao recompra: " + operacoesRecompraValidos.get(keyOperacaoRecompra).getEmpresa().getApelido()+ " - " + operacoesRecompraValidos.get(keyOperacaoRecompra).getCedente().getApelido());        	
        	if(operacoesRecompraValidos.get(keyOperacaoRecompra).getTitulosRecompra().size()>0)
        	{
        		System.out.println(">>>>> Processing OperacaoRecompra " + operacoesRecompraValidos.get(keyOperacaoRecompra).getEmpresa().getApelido() + "  -> "+operacoesRecompraValidos.get(keyOperacaoRecompra).getCedente().getApelido());
        		for(TituloRecompra tituloRecompra:operacoesRecompraValidos.get(keyOperacaoRecompra).getTitulosRecompra())
        		{
        			tituloRecompra.register(connMaria);
        			System.out.println("Show after registering tituloRecompra");
        			tituloRecompra.show();
        			if(tituloRecompra.isVencido())
        			{
        				System.out.println("ValorOriginal: "+tituloRecompra.getValor());
        				System.out.println("Titulo Vencido");
        				long diasVencidos= TimeUnit.DAYS.convert(Calendar.getInstance().getTime().getTime()-tituloRecompra.getVencimento().getTime(), TimeUnit.MILLISECONDS);
        				System.out.println("DiasVencidos: "+diasVencidos);
        				double taxaMoraRecompra = operacoesRecompraValidos.get(keyOperacaoRecompra).getCedente().getTaxaMoraRecompra()/100.0;
        				double tarifaBaixa=operacoesRecompraValidos.get(keyOperacaoRecompra).getCedente().getTarifaBaixa();
        				double valorCorrigido=0;
        				if(tituloRecompra.getAbatimento()>0)
        				{
        					valorCorrigido=tituloRecompra.getValorTotal()*(1+diasVencidos*taxaMoraRecompra/30.0);
        					System.out.println("Abatimento: "+tituloRecompra.getAbatimento());
        					System.out.println("Total: "+tituloRecompra.getValorTotal());
        				}
        				else {
        					valorCorrigido=tituloRecompra.getValor()*(1+diasVencidos*taxaMoraRecompra/30.0);	
						}
        				
        				System.out.println("TaxaMoraRecompra: " + taxaMoraRecompra);
        				System.out.println("TarifaBaixa: "+tarifaBaixa);
        				
        				tituloRecompra.setValorCorrigido(valorCorrigido);
        				tituloRecompra.setValorCorrigido(Double.parseDouble(df.format(Math.round(tituloRecompra.getValorCorrigido()*100)/100.0)));
        				tituloRecompra.setMora(tituloRecompra.getCedente().getTarifaBaixa()+tituloRecompra.getValorCorrigido()-tituloRecompra.getValor());
        				tituloRecompra.setValorRecompra(tituloRecompra.getValorCorrigido()+tarifaBaixa);
        				System.out.println("ValorOriginal: "+tituloRecompra.getValor());
        				System.out.println("ValorRecompra: "+tituloRecompra.getValorRecompra());
        			}
        			else
        			{
        				System.out.println("Titulo a vencer ");
//        				tituloRecompra.setMora(tituloRecompra.getCedente().getTarifaBaixa());
        				tituloRecompra.setMora(0);
        				tituloRecompra.setValorRecompra(tituloRecompra.getValorCorrigido());
        			}        			
        			tituloRecompra.updateValues(connMaria);
        			System.out.println("Show after calculationg custs");
        			tituloRecompra.show();
        		}
        		
        		operacoesRecompraValidos.get(keyOperacaoRecompra).updateValue(connMaria);
        		operacoesRecompraValidos.get(keyOperacaoRecompra).show();
        		listaOperacoesRecompra.add(operacoesRecompraValidos.get(keyOperacaoRecompra));
        	}
        }
        return listaOperacoesRecompra;
    }

    

    
    public ArrayList<OperacaoRecompra> construcaoRecompra(Connection connMaria, Connection connMSS)
    {

    	WebElement dataAtualElement = this.driver.findElement(By.id("ctl00_lbdataatual"));
    	Date dataAtual = null;
    	try {
			dataAtual = sdfr.parse(dataAtualElement.getText());
		} catch (ParseException e1) {
			e1.printStackTrace();
		}
    	
    	int numberDataAtual=Integer.parseInt(sdfn.format(dataAtual));
    	int numberActualDate = Integer.parseInt(sdfn.format(Calendar.getInstance().getTime()));
    	
    	System.out.println("NumberDateRGB: "+numberDataAtual);
    	System.out.println("NumberDateSystem: "+numberActualDate);
    	if(numberDataAtual!=numberActualDate)
    	{
    		System.out.println("Dates does not match!");
    		Utils.waitv(10);
//    		return null;
    	}
    	
    	System.out.println("###############################");
    	System.out.println("STARTING CONSTRUCAO RECOMPRA");
    	System.out.println("###############################");
    	ArrayList<OperacaoRecompra> operacoesRecompra = OperacaoRecompra.loadOperacoesRecompra(connMaria, connMSS);
    	HashMap<String, HashMap<String, Boolean>> empresasCedente = new HashMap<>();
    	for(OperacaoRecompra or:operacoesRecompra)
    	{
    		if(empresasCedente.get(or.getEmpresa().getApelido())==null)
    		{    			
    			HashMap<String, Boolean> cedentes = new HashMap<>();
    			cedentes.put(or.getCedente().getApelido(),true);
    			empresasCedente.put(or.getEmpresa().getApelido(), cedentes);
    		}
    		else {
				if(empresasCedente.get(or.getEmpresa().getApelido()).get(or.getCedente().getApelido())==null)
				{
					empresasCedente.get(or.getEmpresa().getApelido()).put(or.getCedente().getApelido(), true);
				}
			}
    	}
    	    	
        // Baixa solicitacao WebFact
        String stringReturn = "";

        try 
        {
        	int iPage=0;
        	boolean filtersApplied=false;
        	boolean capturingTitles=false;
        	boolean firstReading=false;
        	boolean listingEmpresasCedentes=true;
//          this.driver.get("https://gercloud2.rgbsys.com.br/BMA_HOMOLOGA/operacoes/forms/BaixaSolicitacaoInternet.aspx?SessionId=");
          this.driver.get(this.rootURL+"/operacoes/forms/BaixaSolicitacaoInternet.aspx?SessionId=");
          Thread.sleep(10000);

          // Clica em pesquisar. Lista operacoes disponiveis
          this.driver.findElement(By.id("ctl00_contentManager_btnBuscarImg")).click();
          Utils.waitv(10);
//          Thread.sleep(10000);

          // Filtro abatimento
          this.driver.findElement(By.id("ctl00_contentManager_gridLiberacao_DXFREditorcol1_I")).sendKeys("Baixar");
//          Thread.sleep(10000);
          Utils.waitv("Filtro Baixar",12);
          HashMap<Integer, String> mapTableCols = getTableCols(this.driver.getPageSource());
          for(int iCol:mapTableCols.keySet())
          {
        	  System.out.println(iCol+": "+mapTableCols.get(iCol));
          }
          Empresa empresa = new Empresa();
          Cedente cedente = new Cedente();          
          OperacaoRecompra operacaoRecompra = new OperacaoRecompra();
          while (true)
          {
//                // Filtro Apr = Nao
//                this.driver.findElement(By.id("ctl00$contentManager$gridLiberacao$DXFREditorcol8")).sendKeys("N");
//                Thread.sleep(10000);
//
//                makeTableHigher();
//                Thread.sleep(10000);


                List<String> listRows = getListRows(this.driver.getPageSource());
                int iRow=0;
                for (String stringRow : listRows)
                {
                	System.out.println(iRow+"----- Start of the row");
                    System.out.println("ListingEmpresasCedentes: "+listingEmpresasCedentes);
                    System.out.println("Filter: "+filtersApplied);
                    System.out.println("Capturing: "+capturingTitles);
                    System.out.println("FirstReading: "+firstReading);

                    Document doc = Jsoup.parse(this.driver.getPageSource());
                    System.out.println(stringRow);
                    Element table = doc.getElementById(stringRow);
                    Elements listTableRows = table.select("tr");
                    boolean tituloAdicionado=false;

                    // HashMap<Integer, String> mapTableCols = getTableCols(this.driver.getPageSource());
                    for (Element row : listTableRows)
                    {

                        String idTitulo = "";
                        String nomeEmpresa = "";
                        String nomeCedente = "";
                        String vencimentoString="";
                        String dataSolicitacaoString="";
                        String valorString="";
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
                            
                         // Parsing vencimento
                            if (mapTableCols.get(rowElementCounter).equals("Vcto. do Título"))
                            {
                                vencimentoString = rowDescription.text();
                            }
                            
                            if (mapTableCols.get(rowElementCounter).equals("Data"))
                            {
                                dataSolicitacaoString = rowDescription.text();
                            }
                            
                            // Parsing valor
                            if (mapTableCols.get(rowElementCounter).equals("Valor"))
                            {
                                valorString = rowDescription.text();
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

                            if(nomeCedente.length()>0 && nomeEmpresa.length()>0&&listingEmpresasCedentes)
                            {
                            	if(empresasCedente.get(nomeEmpresa)==null)
                        		{
                            		System.out.println("First time empresa: "+nomeEmpresa);
                        			HashMap<String, Boolean> cedentes = new HashMap<>();
                        			cedentes.put(nomeCedente,false);
                        			System.out.println("Adding cedente: "+nomeCedente);
                        			empresasCedente.put(nomeEmpresa, cedentes);
                        		}
                        		else {
                    				if(empresasCedente.get(nomeEmpresa).get(nomeCedente)==null)
                    				{
                    					empresasCedente.get(nomeEmpresa).put(nomeCedente, false);
                    					System.out.println("Adding cedente: "+nomeCedente + " on empresa: " +nomeEmpresa);
                    				}
                    			}
                            }
                            
                            
                            if(nomeCedente.length() >0 && nomeEmpresa.length()>0 && !filtersApplied && !capturingTitles && !listingEmpresasCedentes)
                            {
                            	boolean created=false;
                            	for(OperacaoRecompra or:operacoesRecompra)
                            	{
                            		
                            		if(or.getEmpresa().getApelido().toLowerCase().contains(nomeEmpresa.toLowerCase())
                            			&& or.getCedente().getApelido().toLowerCase().contains(nomeCedente.toLowerCase())
                            			)
                            		{
                            			created=true;
                            			System.out.println("Recompra for empresa: "+nomeEmpresa + " and cedente: "+nomeCedente+" already exists for today!");
                            			break;
                            		}                            		
                            	}
                            	
                            	
                            	if(!created)
                            	{
	                            	this.driver.findElement(By.id("ctl00_contentManager_gridLiberacao_DXFREditorcol2_I")).clear();
	                            	Utils.waitv("Clear Filtro Empresa",2);
	                                this.driver.findElement(By.id("ctl00_contentManager_gridLiberacao_DXFREditorcol2_I")).sendKeys(nomeEmpresa);
	                                Utils.waitv("Filtro Empresa",10);
	                                this.driver.findElement(By.id("ctl00_contentManager_gridLiberacao_DXFREditorcol3_I")).clear();
	                                Utils.waitv("Clear Filtro Cedente",2);
	                                this.driver.findElement(By.id("ctl00_contentManager_gridLiberacao_DXFREditorcol3_I")).sendKeys(nomeCedente);
	                                Utils.waitv("Filtro Cedente",20);
	                                
	                                List<WebElement> firstPage = this.driver.findElements(By.xpath("//*[@id=\"ctl00_contentManager_gridLiberacao_DXPagerBottom\"]/a[2]"));
	                                if(!firstPage.isEmpty())
	                                {
		                                firstPage.get(0).click();
		                                Utils.waitv("After selecting empresa and cedente and first page",20);
	                                }
	                                empresa = new Empresa(connMaria, connMSS, nomeEmpresa);
	                                cedente = new Cedente(connMaria, connMSS, empresa, nomeCedente);
	                                operacaoRecompra = new OperacaoRecompra(connMaria, empresa, cedente);
	                                operacoesRecompra.add(operacaoRecompra);
	                                filtersApplied=true;
                            	}
                            }
                            rowElementCounter++;
                            if (mapTableCols.get(rowElementCounter) == null) 
                            { 
                            	break; 
                            }
                        }
                        System.out.println("ListingEmpresasCedentes: "+listingEmpresasCedentes);
                        System.out.println("Filter: "+filtersApplied);
                        System.out.println("Capturing: "+capturingTitles);
                        System.out.println("FirstReading: "+firstReading);
                        if(filtersApplied && !capturingTitles)
                        {
                        	break;                        	
                        }
                        
                        if(idTitulo.length()>0 && nomeEmpresa.length()>0 && nomeCedente.length() >0 && vencimentoString.length()>0 && valorString.length()>0 && capturingTitles)
                        {
                        	Date vencimento=null;
                        	try {
								vencimento=sdfr.parse(vencimentoString);
							} catch (ParseException e) {
								e.printStackTrace();
							}
                        	
                        	Date dataSolicitacao=null;
                        	try {
                        		dataSolicitacao=sdfr.parse(dataSolicitacaoString);
							} catch (ParseException e) {
								e.printStackTrace();
							}
                        	double valor=Double.parseDouble(valorString.replaceAll("\\.", "").replace(",", "."));
                        	System.out.println("Empresa: "+empresa.getRazaoSocial());
                        	System.out.println("Cedente: "+cedente.getParticipante().getRazaoSocial());
                        	System.out.println("IdentificacaoTitulo: "+idTitulo);
                        	System.out.println("Valor: "+valor);
                        	System.out.println("Vencimento: "+sdf.format(vencimento));
                        	if(TituloRecompra.checkAberto(connMSS, operacaoRecompra, idTitulo, valor, vencimento))
                        	{
                        		System.out.println("Titulo em aberto, adicionando à lista da operacao recompra");
	                        	TituloRecompra tituloRecompra = new TituloRecompra(connMaria, connMSS, operacaoRecompra, idTitulo, valor, vencimento,dataSolicitacao,true);
	                        	operacaoRecompra.getTitulosRecompra().add(tituloRecompra);
	                        	tituloAdicionado=true;
                        	}
                        	else
                        	{
                        		System.out.println("Titulo já baixado, não será adicionado à lista da operacao recompra");
                        		System.out.println("Rejeitando RECOMPRA");
							    String clickId = row.select("input").first().id() + "_D";
							    assert(clickId.contains("ctl00_contentManager_gridLiberacao_DXSelBtn")) : "Id inconsistente";
							    System.out.println("Clicando no elemento" +  clickId);
							    this.driver.findElement(By.id(clickId)).click();
							    Utils.waitv(10);
								System.out.println("Data de prorrogacao além de 35 dias!");
								this.driver.findElement(By.id("ctl00_contentManager_btnRejeita_CD")).click();
								Utils.waitv(4);
								this.driver.switchTo().frame("ctl00_contentManager_popup_TODOS_CIF-1");
								this.driver.findElement(By.id("ctl00_contentManager_txtMotivo_I")).sendKeys("Prorrogação maior que 35 dias");
								Utils.waitv(2);
								this.driver.findElement(By.id("ctl00_contentManager_btnMotivo_CD")).click();
								Utils.waitv(4);
//								efetuado=true;

							    
//								if(durationDays>35)
//								{
//									System.out.println("Data de prorrogacao além de 35 dias!");
//							this.driver.findElement(By.id("ctl00_contentManager_btnRejeita_CD")).click();
//							Utils.waitv(4);
//							//                                		this.driver.switchTo().frame("ctl00_contentManager_popup_TODOS_PWH-1");
//							this.driver.switchTo().frame("ctl00_contentManager_popup_TODOS_CIF-1");
//							this.driver.findElement(By.id("ctl00_contentManager_txtMotivo_I")).sendKeys("Prorrogação maior que 35 dias");
//							Utils.waitv(2);
//							this.driver.findElement(By.id("ctl00_contentManager_btnMotivo_CD")).click();
//								Utils.waitv(4);
//								efetuado=true;
//							}
//							else if(vencido)
//							{
//								System.out.println("Titulo vencido, não prorrogável!");
//							this.driver.findElement(By.id("ctl00_contentManager_btnRejeita_CD")).click();
//							Utils.waitv(4);
//							//                                		this.driver.switchTo().frame("ctl00_contentManager_popup_TODOS_PWH-1");
//							this.driver.switchTo().frame("ctl00_contentManager_popup_TODOS_CIF-1");
//							this.driver.findElement(By.id("ctl00_contentManager_txtMotivo_I")).sendKeys("Título vencido");
//							Utils.waitv(2);
//							this.driver.findElement(By.id("ctl00_contentManager_btnMotivo_CD")).click();
//								Utils.waitv(4);
//								efetuado=true;
//							}
//							else if(durationDays==0)
//							{
//								System.out.println("Data de prorrogacao igual ao vencimento!");
//							this.driver.findElement(By.id("ctl00_contentManager_btnRejeita_CD")).click();
//							Utils.waitv(4);
//							//                                		this.driver.switchTo().frame("ctl00_contentManager_popup_TODOS_PWH-1");
//							this.driver.switchTo().frame("ctl00_contentManager_popup_TODOS_CIF-1");
//							this.driver.findElement(By.id("ctl00_contentManager_txtMotivo_I")).sendKeys("Vencimento igual");
//							Utils.waitv(2);
//							this.driver.findElement(By.id("ctl00_contentManager_btnMotivo_CD")).click();
//								Utils.waitv(4);
//								efetuado=true;
//							}
                        		
                        	}
                        }
//                        if (!isApproved) // Conforme no manual
//                        {
//                            // Realizar prorrogacao
//                            System.out.println("Raalizando aprovacao");
//                            String clickId = row.select("input").first().id() + "_D";
//
//                            assertStringContainsAnotherString(clickId, "ctl00_contentManager_gridLiberacao_DXSelBtn", "Id do elemento nao encontrado na página. Abortando o processo.");
//
//                            // Clicando no elemento
//                            System.out.println("Clicando no elemento" +  clickId);
//                            this.driver.findElement(By.id(clickId)).click();
//                            Thread.sleep(10000);
//
//                            // Clica em aprovar
//                            this.driver.findElement(By.id("ctl00_contentManager_btnAprova")).click();
//                            Thread.sleep(10000);
//
//                            // Salva Critica
//                            Critica critica = new Critica(
//                                idTitulo,
//                                nomeCedente,
//                                nomeEmpresa,
//                                "APROVACAO"
//                            );
//                            this.getListCritica().add(critica);
//
//                            makeTableHigher();
//                            Thread.sleep(10000);
//                        }
                    }
                    if(filtersApplied && !capturingTitles)
                    {
                        capturingTitles=true;
                        firstReading=true;
                        System.out.println("Enabling capturing!");
                        System.out.println("ListingEmpresasCedentes: "+listingEmpresasCedentes);
                        System.out.println("Filter: "+filtersApplied);
                        System.out.println("Capturing: "+capturingTitles);
                        System.out.println("FirstReading: "+firstReading);
                    	break;
                    }
                    iRow++;
                    System.out.println("----- End of row!");
                }

                if(firstReading)                
                {
                	System.out.println("Since we are capturing, we prevent from breaking while once");
                	firstReading=false;
                }
                else
                {
	                iPage++;
	                if (checkIfIsLastPage(this.driver.getPageSource()))
	                {
	                	
	                    System.out.println("Driver esta na pagina final. ");
	                    if(!listingEmpresasCedentes)
	                    {	              
                        	System.out.println("FINAL PAGE NOT LISTING EMPRESAS");

	                    	if(operacaoRecompra.getIdOperacaoRecompra()!=0)
	                    	{
			                    operacaoRecompra.updateValue(connMaria);
			                    operacaoRecompra.show();
			                    operacoesRecompra.add(operacaoRecompra);
			                    System.out.println("Setting true for "+operacaoRecompra.getEmpresa().getApelido()+  "  "+operacaoRecompra.getCedente().getApelido());
			                    empresasCedente.get(operacaoRecompra.getEmpresa().getApelido()).put(operacaoRecompra.getCedente().getApelido(),true);
	                    	}
		                    this.driver.findElement(By.id("ctl00_contentManager_gridLiberacao_DXFREditorcol2_I")).clear();
	                    	Utils.waitv("Clear Filtro Empresa",2);
	                        this.driver.findElement(By.id("ctl00_contentManager_gridLiberacao_DXFREditorcol3_I")).clear();
	                        Utils.waitv("Clear Filtro Cedente",2);
	                        filtersApplied=false;
	                        capturingTitles=false;
	                    }
	                    
                        if(listingEmpresasCedentes)
                        {
                        	System.out.println("ENTERING HERE AT THE FINAL PAGE!!");
                        	listingEmpresasCedentes=false;
                        	System.out.println("Finishing listing empresas cedentes!");
                            System.out.println("ListingEmpresasCedentes: "+listingEmpresasCedentes);
                            System.out.println("Filter: "+filtersApplied);
                            System.out.println("Capturing: "+capturingTitles);
                            System.out.println("FirstReading: "+firstReading);
                            System.out.println("******************************** All pair of empresas and cedentes");
							for(String stEmpresa:empresasCedente.keySet())						
							{
								System.out.println("**** Empresa: " + stEmpresa);
								for(String stCedente:empresasCedente.get(stEmpresa).keySet())
								{
									System.out.println("Empresa: " + stEmpresa + " Cedente: " +stCedente + " "+empresasCedente.get(stEmpresa).get(stCedente));
								}
							}
                        	Utils.waitv(25);
                        }
                       	else 
                       	{
                       		System.out.println("ENTERING HERE AT THE FINAL PAGE WITH NO MORE LISTING EMPRESAS!!");
                        	
                       		boolean allRead=true;
                       		System.out.println("Verifying if all read!");
							for(String stEmpresa:empresasCedente.keySet())						
							{
								System.out.println("**** Empresa: " + stEmpresa);
								for(String stCedente:empresasCedente.get(stEmpresa).keySet())
								{
									System.out.println("Empresa: " + stEmpresa + " Cedente: " +stCedente + " "+empresasCedente.get(stEmpresa).get(stCedente));
									if(!empresasCedente.get(stEmpresa).get(stCedente))
									{
										allRead=false;
									}
								}
							}
							if(allRead)
							{
								System.out.println("AllRead!! Finishing the process!");
								break;
							}
							{
								System.out.println("There are reading missing!");								
							}
							Utils.waitv(15);
                        }
	                }
	                else
	                {
	                    gotoNextPage(iPage);
	                }
                }
            }

            saveListCriticaOperacoesRealizadasToDatabase();
        }
        catch (InterruptedException ex) 
        {
            Thread.currentThread().interrupt();
        }
        return operacoesRecompra;
    }


    public static void main(String[] args) {

        RgbsysOperacaoRecompra rgbSysSelenium = new RgbsysOperacaoRecompra("MOISES", "moises");
        try
        { 
            rgbSysSelenium.login();
//            rgbSysSelenium.construcaoRecompra();
            
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
