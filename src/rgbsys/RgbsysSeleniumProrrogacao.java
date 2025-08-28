package rgbsys;

import java.sql.Connection;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.openqa.selenium.By;

import cedente.Cedente;
import conta_grafica.AceitarProrrogacao;
import conta_grafica.Critica;
import conta_grafica.TituloInstrucao;
import empresa.Empresa;
import utils.Utils;

public class RgbsysSeleniumProrrogacao extends RgbsysSeleniumWebFactView {
	
	private static SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
	private static SimpleDateFormat sdfa = new SimpleDateFormat("yyyy-MM-dd");
	private static SimpleDateFormat sdfn = new SimpleDateFormat("yyyyMMdd");

   public  RgbsysSeleniumProrrogacao(String userName, String password)
    {
        super(userName, password);
    }

   public  RgbsysSeleniumProrrogacao()
   {
       super();
   }

   
   public  void conferirProrrogacao()
    {
        try 
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

                    Date vctoTitulo =  new Date(1900, 1, 1, 1, 1);
                    Date novoVcto =  new Date(2022, 1, 1, 1, 1);
                    Boolean isProrogar = false;
                    Boolean isApproved = true;
                    Integer rowElementCounter = 0;
                    Elements listRowDescriptions =  row.select("td");
                    listRowDescriptions.remove(0);

                    System.out.println("-------------------------");
                    System.out.println(row);

                    for (Element rowDescription : listRowDescriptions)
                    {

                        if (mapTableCols.get(rowElementCounter).equals("Prorrogar"))
                        {
                            isProrogar = true;
                        }

                        if (mapTableCols.get(rowElementCounter).equals("Vcto. do Título"))
                        {
                            vctoTitulo = this.formatter.parse(rowDescription.text());
                            System.out.println("vencimento titulo");
                            System.out.println(vctoTitulo);
                        }

                        if (mapTableCols.get(rowElementCounter).equals("Novo Venc."))
                        {
                            novoVcto = this.formatter.parse(rowDescription.text());
                            System.out.println("novo vencimento");
                            System.out.println(novoVcto);
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

                    // long durationDays = Duration.between(novoVcto, vctoTitulo).toDays();
                    long durationDays = getDifferenceDays(vctoTitulo, novoVcto);
                    System.out.println("Duration in days " + String.valueOf(durationDays));

                    if (durationDays <= 30 && durationDays > 0 && !isApproved) // Conforme no manual
                    {
                        // Isso nao deveria ser verdade
                        System.out.println("Trata-se de uma opera����o de prorrogacao de um titulo dentro do prazo de 30 dias e o flag \"Aprovado\" deveria estar marcado como \"Sim. Essa condi����o n��o deveria ser verdadeira.");
                        System.out.println("Abortando opera����o");
                        System.exit(1);
                    }
                }
            }
        } 
        catch (ParseException e) 
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

   public ArrayList<Critica> prorrogacao(Connection connMaria, Connection connMSS)
    {
        // Video 4
        String stringReturn = "";

        	int iPage=0;
            this.driver.get(RgbsysUser.rootURL+"/operacoes/forms/BaixaSolicitacaoInternet.aspx?SessionId=");
            Utils.waitv(10);
            // Clica em pesquisar. Lista operacoes disponiveis
            this.driver.findElement(By.id("ctl00_contentManager_btnBuscarImg")).click();
            Utils.waitv(10);
//            Thread.sleep(10000);

            // Filtro abatimento
            this.driver.findElement(By.id("ctl00_contentManager_gridLiberacao_DXFREditorcol1_I")).sendKeys("Prorrogar");
//            Thread.sleep(10000);
            Utils.waitv(10);
            int numeroHoje = Integer.parseInt(sdfn.format(Calendar.getInstance().getTime()));
            int numeroVencimento = 0;
            String keyEmpresaCedente="";

            while (true)
            {
                Utils.waitv(5);
                HashMap<Integer, String> mapTableCols = getTableCols(this.driver.getPageSource());
                List<String> listRows = getListRows(this.driver.getPageSource());
                int iRow=0;
                while (listRows.size()>0 && iRow < listRows.size())
                {       
                	System.out.println("-- listRowsSize: " +listRows.size());
                	System.out.println("-- iRow: " +iRow);
                	String stringRow=listRows.get(iRow);
                    Document doc = Jsoup.parse(this.driver.getPageSource());
                    System.out.println(stringRow);
                    Element table = doc.getElementById(stringRow);
                    Elements listTableRows = table.select("tr");

                    // HashMap<Integer, String> mapTableCols = getTableCols(this.driver.getPageSource());
                    boolean efetuado=false;
                    for (Element row : listTableRows)
                    {
                    	Utils.waitv(3);
                        String idTitulo = "";
                        String nomeEmpresa = "";
                        String nomeCedente = "";
                        Boolean isApproved = true;
                        Date vctoTitulo =  new Date(1900, 1, 1, 1, 1);
                        Date novoVcto =  new Date(2022, 1, 1, 1, 1);
                        Integer rowElementCounter = 0;
                        Elements listRowDescriptions =  row.select("td");
                        listRowDescriptions.remove(0);
                        keyEmpresaCedente=nomeEmpresa.toUpperCase()+nomeCedente.toUpperCase();
                        System.out.println(iRow+ " -------------------------");
//                        System.out.println(row);                        	
                        boolean show=true;
                        efetuado=false;
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

                            // System.out.println(row);
                            if(show && nomeCedente.length()>0 && idTitulo.length() > 0 && nomeEmpresa.length()>0)
                            {
                            	System.out.println("idTitulo: "+idTitulo+ " Empresa: "+nomeEmpresa + " Cedente: "+nomeCedente);
                            	show=false;
                            }
                            // System.out.println(mapTableCols.get(rowElementCounter) + " " + rowDescription.text());
                            if (mapTableCols.get(rowElementCounter).equals("Vcto. do Título"))
                            {
                                try {
									vctoTitulo = this.formatter.parse(rowDescription.text());
									numeroVencimento = Integer.parseInt(sdfn.format(vctoTitulo));
								} catch (ParseException e) {
									e.printStackTrace();
								}
                                System.out.println("Vencimento titulo: "+vctoTitulo);
                            }

                            if (mapTableCols.get(rowElementCounter).equals("Novo Venc."))
                            {
                                try {
									novoVcto = this.formatter.parse(rowDescription.text());
								} catch (ParseException e) {
									e.printStackTrace();
								}
                                System.out.println("Novo vencimento: "+novoVcto);
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

                        Empresa empresa = new Empresa(connMaria, connMSS, nomeEmpresa);
			            Cedente cedente = new Cedente(connMaria, connMSS, empresa, nomeCedente);
			            TituloInstrucao tituloInstrucao = new TituloInstrucao(empresa, cedente, idTitulo);
			            tituloInstrucao.checkProrrogado(connMSS);
			            boolean forbidden=tituloInstrucao.checkProdutoProibido(connMSS); 
			            
                        // long durationDays = Duration.between(novoVcto, vctoTitulo).toDays();
                        long durationDays = getDifferenceDays(vctoTitulo, novoVcto);
                        System.out.println("Duration in days " + String.valueOf(durationDays));
                        boolean vencido=false;
                        if(numeroVencimento<numeroHoje)
                        {
                        	vencido=true;
                        	System.out.println("Titulo vencido!!");
                        }
                        
                        if (durationDays <= 35 
                        		&& durationDays > 0 
                        		&& !vencido
                        		&& AceitarProrrogacao.getBarrados().get(keyEmpresaCedente)==null  
                        		&& !forbidden
                        		&& !tituloInstrucao.isProrrogado()
                        		) // Conforme no manual
                        {
                            // Realizar prorrogacao
                            System.out.println("Raalizando progorracao");
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

                            Critica critica = new Critica(connMaria, connMSS,
                                idTitulo,
                                nomeCedente,
                                nomeEmpresa,
                                "PRORROGACAO"
                            );
                            this.getListCritica().add(critica);
                            efetuado=true;
                        }
                        else
                        {
                    	   System.out.println("Rejeitando progorracao");
                           String clickId = row.select("input").first().id() + "_D";
                           assert(clickId.contains("ctl00_contentManager_gridLiberacao_DXSelBtn")) : "Id inconsistente";
                           System.out.println("Clicando no elemento" +  clickId);
                           this.driver.findElement(By.id(clickId)).click();
                           Utils.waitv(10);
                        	if(durationDays>35)
                        	{
                        		System.out.println("Data de prorrogacao além de 35 dias!");
                        		this.driver.findElement(By.id("ctl00_contentManager_btnRejeita_CD")).click();
                        		Utils.waitv(4);
//                        		this.driver.switchTo().frame("ctl00_contentManager_popup_TODOS_PWH-1");
                        		this.driver.switchTo().frame("ctl00_contentManager_popup_TODOS_CIF-1");
                        		this.driver.findElement(By.id("ctl00_contentManager_txtMotivo_I")).sendKeys("Prorrogação maior que 35 dias");
                        		Utils.waitv(2);
                        		this.driver.findElement(By.id("ctl00_contentManager_btnMotivo_CD")).click();
                        		Utils.waitv(4);
                        		efetuado=true;
                        	}
                        	else if(vencido)
                        	{
                        		System.out.println("Titulo vencido, não prorrogável!");
                        		this.driver.findElement(By.id("ctl00_contentManager_btnRejeita_CD")).click();
                        		Utils.waitv(4);
//                        		this.driver.switchTo().frame("ctl00_contentManager_popup_TODOS_PWH-1");
                        		this.driver.switchTo().frame("ctl00_contentManager_popup_TODOS_CIF-1");
                        		this.driver.findElement(By.id("ctl00_contentManager_txtMotivo_I")).sendKeys("Título vencido");
                        		Utils.waitv(2);
                        		this.driver.findElement(By.id("ctl00_contentManager_btnMotivo_CD")).click();
                        		Utils.waitv(4);
                        		efetuado=true;
                        	}
                        	else if(durationDays==0)
                        	{
                        		System.out.println("Data de prorrogacao igual ao vencimento!");
                        		this.driver.findElement(By.id("ctl00_contentManager_btnRejeita_CD")).click();
                        		Utils.waitv(4);
//                        		this.driver.switchTo().frame("ctl00_contentManager_popup_TODOS_PWH-1");
                        		this.driver.switchTo().frame("ctl00_contentManager_popup_TODOS_CIF-1");
                        		this.driver.findElement(By.id("ctl00_contentManager_txtMotivo_I")).sendKeys("Vencimento igual");
                        		Utils.waitv(2);
                        		this.driver.findElement(By.id("ctl00_contentManager_btnMotivo_CD")).click();
                        		Utils.waitv(4);
                        		efetuado=true;
                        	}
                        	else if(durationDays<0)
                        	{
                        		System.out.println("Data de prorrogacao menor que a data do vencimento!");
                        		this.driver.findElement(By.id("ctl00_contentManager_btnRejeita_CD")).click();
                        		Utils.waitv(4);
//                        		this.driver.switchTo().frame("ctl00_contentManager_popup_TODOS_PWH-1");
                        		this.driver.switchTo().frame("ctl00_contentManager_popup_TODOS_CIF-1");
                        		this.driver.findElement(By.id("ctl00_contentManager_txtMotivo_I")).sendKeys("Data de prorrogacao menor que a data do vencimento!");
                        		Utils.waitv(2);
                        		this.driver.findElement(By.id("ctl00_contentManager_btnMotivo_CD")).click();
                        		Utils.waitv(4);
                        		efetuado=true;
                        	}
                        	else if(AceitarProrrogacao.getBarrados().get(keyEmpresaCedente)!=null)
                        	{
                        		System.out.println("Cedente barrado para prorrogacao!");
                        		this.driver.findElement(By.id("ctl00_contentManager_btnRejeita_CD")).click();
                        		Utils.waitv(4);
//                        		this.driver.switchTo().frame("ctl00_contentManager_popup_TODOS_PWH-1");
                        		this.driver.switchTo().frame("ctl00_contentManager_popup_TODOS_CIF-1");
                        		this.driver.findElement(By.id("ctl00_contentManager_txtMotivo_I")).sendKeys("Cedente barrado");
                        		Utils.waitv(2);
                        		this.driver.findElement(By.id("ctl00_contentManager_btnMotivo_CD")).click();
                        		Utils.waitv(4);
                        		efetuado=true;
                        	}
                        	else if (forbidden)
                        	{
                        		System.out.println("Produto " + tituloInstrucao.getNomeProduto().toUpperCase()+ " proibido para esta instrução!");
                        		this.driver.findElement(By.id("ctl00_contentManager_btnRejeita_CD")).click();
                        		Utils.waitv(4);
//                        		this.driver.switchTo().frame("ctl00_contentManager_popup_TODOS_PWH-1");
                        		this.driver.switchTo().frame("ctl00_contentManager_popup_TODOS_CIF-1");
                        		this.driver.findElement(By.id("ctl00_contentManager_txtMotivo_I")).sendKeys("Produto " + tituloInstrucao.getNomeProduto().toUpperCase()+ " proibido para esta instrução!");
                        		Utils.waitv(2);
                        		this.driver.findElement(By.id("ctl00_contentManager_btnMotivo_CD")).click();
                        		Utils.waitv(4);
                        		efetuado=true;                        		
                        	}            
                        	if (tituloInstrucao.isProrrogado())
                        	{
                        		System.out.println("Titulo já prorrogado!");
                        		this.driver.findElement(By.id("ctl00_contentManager_btnRejeita_CD")).click();
                        		Utils.waitv(4);
//                        		this.driver.switchTo().frame("ctl00_contentManager_popup_TODOS_PWH-1");
                        		this.driver.switchTo().frame("ctl00_contentManager_popup_TODOS_CIF-1");
                        		this.driver.findElement(By.id("ctl00_contentManager_txtMotivo_I")).sendKeys("Titulo já prorrogado anteriormente!!");
                        		Utils.waitv(2);
                        		this.driver.findElement(By.id("ctl00_contentManager_btnMotivo_CD")).click();
                        		Utils.waitv(4);
                        		efetuado=true;                        		
                        	}            
                        }
                    }                    
                    if(efetuado)
                    {
                        listRows = getListRows(this.driver.getPageSource());       
                        if(listRows.size()>0)
                        {
                        	iRow=0; 
                        }
                    }
                    else {
						iRow++;
					}
                }
                
                listRows = getListRows(this.driver.getPageSource());
                conferirProrrogacao();

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
       System.out.println("Total de "+this.getListCritica().size()+" críticas!");
        return this.getListCritica();
    }

    public static void main(String[] args) {

        RgbsysSeleniumProrrogacao rgbSysSelenium = new RgbsysSeleniumProrrogacao("MOISES", "moises");
        try
        { 
            rgbSysSelenium.login();
//            rgbSysSelenium.prorrogacao();
            
        }
        catch (Exception e) 
        {
            rgbSysSelenium.saveListCriticaOperacoesRealizadasToDatabase();
        }
        finally
        {
            rgbSysSelenium.close();

        }

    }

	public static SimpleDateFormat getSdf() {
		return sdf;
	}

	public static void setSdf(SimpleDateFormat sdf) {
		RgbsysSeleniumProrrogacao.sdf = sdf;
	}

	public static SimpleDateFormat getSdfa() {
		return sdfa;
	}

	public static void setSdfa(SimpleDateFormat sdfa) {
		RgbsysSeleniumProrrogacao.sdfa = sdfa;
	}

	public static SimpleDateFormat getSdfn() {
		return sdfn;
	}

	public static void setSdfn(SimpleDateFormat sdfn) {
		RgbsysSeleniumProrrogacao.sdfn = sdfn;
	}
    
}
