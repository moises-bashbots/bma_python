package rgbsys;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.ParseException;
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
import conta_grafica.Critica;
import conta_grafica.TituloInstrucao;
import empresa.Empresa;
import utils.Utils;

public class RgbsysSeleniumProtesto extends RgbsysSeleniumWebFactView {
    
   public RgbsysSeleniumProtesto(String userName, String password)
    {
        super(userName, password);
    }
   
   public RgbsysSeleniumProtesto()
   {
       super();
   }

    void conferirProtesto()
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

                        if (mapTableCols.get(rowElementCounter).equals("Protestar"))
                        {
                            isProrogar = true;
                        }

                        if (mapTableCols.get(rowElementCounter).equals("Vcto. do Título"))
                        {
                            vctoTitulo = this.formatter.parse(rowDescription.text());
                            System.out.println("vencimento titulo");
                            System.out.println(vctoTitulo);
                        }

//                        if (mapTableCols.get(rowElementCounter).equals("Novo Venc."))
//                        {
//                            novoVcto = this.formatter.parse(rowDescription.text());
//                            System.out.println("novo vencimento");
//                            System.out.println(novoVcto);
//                        }

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
                        // Isso nao deveria ser verdade
                        System.out.println("Trata-se de uma operação de protesto de um tituloe o flag \"Aprovado\" deveria estar marcado como \"Sim. Essa condição não deveria ser verdadeira.");
                        System.out.println("Abortando operarão");
                        close();
                    }
                }
            }
        } 
        catch (ParseException e) 
        {
            e.printStackTrace();
        }
    }

    public ArrayList<Critica> protestar(Connection connMaria, Connection connMSS)
    {
        // Video 4
        String stringReturn = "";

        	int iPage=0;
//        	               this.driver.get("https://gercloud2.rgbsys.com.br/BMA_HOMOLOGA/operacoes/forms/BaixaSolicitacaoInternet.aspx?SessionId=");
            this.driver.get(RgbsysUser.rootURL+"/operacoes/forms/BaixaSolicitacaoInternet.aspx?SessionId=");
//            Thread.sleep(10000);
            Utils.waitv(10);


            // Clica em pesquisar. Lista operacoes disponiveis
            this.driver.findElement(By.id("ctl00_contentManager_btnBuscarImg")).click();
//            Thread.sleep(10000);
            Utils.waitv(10);

            // Filtro abatimento
            this.driver.findElement(By.id("ctl00_contentManager_gridLiberacao_DXFREditorcol1_I")).sendKeys("Protestar");
//            Thread.sleep(10000);
            Utils.waitv(10);
            while (true)
            {
//                makeTableHigher();
//                Thread.sleep(10000);

                HashMap<Integer, String> mapTableCols = getTableCols(this.driver.getPageSource());
                for(int col:mapTableCols.keySet())
                {
                	System.out.println(col + " -:- "+mapTableCols.get(col));
                }
                List<String> listRows = getListRows(this.driver.getPageSource());
                int iRow=0;
                while ( listRows.size()>0 && iRow <listRows.size())
                {
                	
                	String stringRow =  listRows.get(iRow);
                	iRow++;
                    Document doc = Jsoup.parse(this.driver.getPageSource());
                    System.out.println(stringRow);
                    Element table = doc.getElementById(stringRow);
                    Elements listTableRows = table.select("tr");

                    // HashMap<Integer, String> mapTableCols = getTableCols(this.driver.getPageSource());
                    boolean tituloRecomprado=false;
                    String  tipoOperacao="";
                    for (Element row : listTableRows)
                    {

                        String idTitulo = "";
                        String nomeEmpresa = "";
                        String nomeCedente = "";
                        String tipoCobranca="";
                        Boolean isApproved = true;
                        Date vctoTitulo =  new Date(1900, 1, 1, 1, 1);
                        Date novoVcto =  new Date(2022, 1, 1, 1, 1);
                        Integer rowElementCounter = 0;
                        Elements listRowDescriptions =  row.select("td");
                        listRowDescriptions.remove(0);

                        System.out.println("-------------------------");
                        System.out.println(row);
                        String novoVencimentoString="";
                        for (Element rowDescription : listRowDescriptions)
                        {

                            // Parsing idTitulo
                            if (mapTableCols.get(rowElementCounter).equals("Duplicata"))
                            {
                                idTitulo = rowDescription.text();
                            }
                            
                            // Parsing tipoCobranca
                            if (mapTableCols.get(rowElementCounter).equals("TC"))
                            {
                                tipoCobranca = rowDescription.text();
                                if (tipoCobranca.equals("4"))
                                {
                                	System.out.println("Tipo de operacao 4 - Ex-factoring!");
                                	Utils.waitv(5);
								}
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
                            // System.out.println(mapTableCols.get(rowElementCounter) + " " + rowDescription.text());
                            if (mapTableCols.get(rowElementCounter).equals("Vcto. do Título"))
                            {
                                try {
									vctoTitulo = this.formatter.parse(rowDescription.text());
								} catch (ParseException e) {
									// TODO Auto-generated catch block
									e.printStackTrace();
								}
                                System.out.println("vencimento titulo");
                                System.out.println(vctoTitulo);
                                // Date vctoTitualo = formatter.parse(row.text());
                            }

//                            if (mapTableCols.get(rowElementCounter).equals("Novo Venc."))
//                            {
//                            	novoVencimentoString=rowDescription.text();
//                            	if(novoVencimentoString.length()>0)
//                            	{
//	                            	try {
//	                                	System.out.println(rowDescription.text());
//										novoVcto = this.formatter.parse(rowDescription.text());
//									} catch (ParseException e) {
//										e.printStackTrace();
//									}
//	                                System.out.println("novo vencimento");
//	                                System.out.println(novoVcto);
//                            	}
//                            }

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
                            
                            if(idTitulo.length()>0 && nomeEmpresa.length()>0 && nomeCedente.length()>0)
                            {
                            	
                            }

                            rowElementCounter++;

                            if (mapTableCols.get(rowElementCounter) == null) 
                            { break; }
                        }

//                        if(novoVencimentoString.length()==0)
//                        {
//                        	continue;
//                        }
                        
                        // long durationDays = Duration.between(novoVcto, vctoTitulo).toDays();
                        Empresa empresa = new Empresa(connMaria, connMSS, nomeEmpresa);
			            Cedente cedente = new Cedente(connMaria, connMSS, empresa, nomeCedente);
			            TituloInstrucao tituloInstrucao = new TituloInstrucao(empresa, cedente, idTitulo); 
			            boolean forbidden=tituloInstrucao.checkProdutoProibido(connMSS); 
			            long expiredDays = getDifferenceDays(vctoTitulo, Calendar.getInstance().getTime());
                        long durationDays = getDifferenceDays(vctoTitulo, novoVcto);
                        System.out.println("Duration in days " + String.valueOf(durationDays));
                        boolean exFactoring=false;
                        boolean vencidoAntigo=false;
                        if(expiredDays<=-60)
                        {
                        	vencidoAntigo=true;
                        }
                        String answer=checkRecompraExFactoring(connMSS, idTitulo, nomeEmpresa, nomeCedente);
                        if(answer.toLowerCase().contains("recompra"))
                        {
                        	tituloRecomprado=true;
                        }
                        if(answer.toLowerCase().contains("factoring"))
                        {
                        	exFactoring=true;
                        }
		                 boolean alertaHistorico=false;                        

                        if (!isApproved && !tituloRecomprado && !exFactoring && !tipoCobranca.equals("4")&&!forbidden&&!vencidoAntigo) // Conforme no manual
                        {
                            // Realizar prorrogacao
                            System.out.println("Realizando protesto");
                            String clickId = row.select("input").first().id() + "_D";
                            assert(clickId.contains("ctl00_contentManager_gridLiberacao_DXSelBtn")) : "Id inconsistente";
                            System.out.println("Clicando no elemento" +  clickId);
                            this.driver.findElement(By.id(clickId)).click();
//                            Thread.sleep(10000);
                            Utils.waitv(10);

                            // Clica em aprovar
    		                System.out.println("clicando em aprovar");
    		                this.driver.findElement(By.id("ctl00_contentManager_btnAprova")).click();
//                                Thread.sleep(10000);
    		                Utils.waitv(10);
    		                isApproved=true;
                        }

                        if(isApproved && !tituloRecomprado && !exFactoring && !tipoCobranca.equals("4")&&!forbidden&&!vencidoAntigo)
                        {
                            // Realizar prorrogacao
                            System.out.println("Realizando protesto");
                            String clickId = row.select("input").first().id() + "_D";
                            assert(clickId.contains("ctl00_contentManager_gridLiberacao_DXSelBtn")) : "Id inconsistente";
                            System.out.println("Clicando no elemento" +  clickId);
                            this.driver.findElement(By.id(clickId)).click();
//                            Thread.sleep(10000);
                            Utils.waitv(10);

//    		                conferirProtesto();
    		                
    		                // Clica em efetua
    		                 System.out.println("Clicando em efetua");
    		                 this.driver.findElement(By.id("ctl00_contentManager_btnEfetua_CD")).click();

    		                Utils.waitv("Clica em efetua",10);
    		                try {
        		                this.driver.switchTo().alert().accept();								
        		                alertaHistorico=true;
							} catch (Exception e) {
								e.printStackTrace();
							}

    		                if(!alertaHistorico)
    		                {
	    		                // // Apos clicar em efetua, um novo pop up ira abrir. Tem que trocar para o novo frame.
	    		                 System.out.println("Clicando no novo popup");
	    		                 this.driver.switchTo().frame("ctl00_contentManager_popup_TODOS_CIF-1");
	    		                 Utils.waitv("Clicando no novo poput",10);
	    		                // Thread.sleep(10000);
	
	    		                // // Clica em OK
	    		                System.out.println("Apertando ok do sistema.");
	    		                 this.driver.findElement(By.id("btnOK_CD")).click();
	    		                 Utils.waitv("Apertando OK do sistema",10);
	    		                 
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
	                                "INSTRUCAO PROTESTO EFETUADA"
	                            );
	                            this.getListCritica().add(critica);
    		                }
                        } 
                        if(tituloRecomprado)
                        {
                        	 // Realizar prorrogacao
                            System.out.println("Rejeitando protesto");
                            String clickId = row.select("input").first().id() + "_D";
                            assert(clickId.contains("ctl00_contentManager_gridLiberacao_DXSelBtn")) : "Id inconsistente";
                            System.out.println("Clicando no elemento" +  clickId);
                            this.driver.findElement(By.id(clickId)).click();
//                            Thread.sleep(10000);
                            Utils.waitv(10);
                            this.driver.findElement(By.id("ctl00_contentManager_btnRejeita_CD")).click();
                    		Utils.waitv(4);
//                    		this.driver.switchTo().frame("ctl00_contentManager_popup_TODOS_PWH-1");
                    		this.driver.switchTo().frame("ctl00_contentManager_popup_TODOS_CIF-1");
                    		this.driver.findElement(By.id("ctl00_contentManager_txtMotivo_I")).sendKeys("Título recomprado");
                    		Utils.waitv(2);
                    		this.driver.findElement(By.id("ctl00_contentManager_btnMotivo_CD")).click();
                    		Utils.waitv(4);
                        }
                        else if(exFactoring)
                        {
                        	 // Realizar prorrogacao
                            System.out.println("Rejeitando protesto");
                            String clickId = row.select("input").first().id() + "_D";
                            assert(clickId.contains("ctl00_contentManager_gridLiberacao_DXSelBtn")) : "Id inconsistente";
                            System.out.println("Clicando no elemento" +  clickId);
                            this.driver.findElement(By.id(clickId)).click();
//                            Thread.sleep(10000);
                            Utils.waitv(10);
                            this.driver.findElement(By.id("ctl00_contentManager_btnRejeita_CD")).click();
                    		Utils.waitv(4);
//                    		this.driver.switchTo().frame("ctl00_contentManager_popup_TODOS_PWH-1");
                    		this.driver.switchTo().frame("ctl00_contentManager_popup_TODOS_CIF-1");
                    		this.driver.findElement(By.id("ctl00_contentManager_txtMotivo_I")).sendKeys("Título ex factoring");
                    		Utils.waitv(2);
                    		this.driver.findElement(By.id("ctl00_contentManager_btnMotivo_CD")).click();
                    		Utils.waitv(4);
                        }
                        else if(alertaHistorico)
                        {
                        	 // Realizar prorrogacao
                            System.out.println("Rejeitando protesto");
                            String clickId = row.select("input").first().id() + "_D";
                            assert(clickId.contains("ctl00_contentManager_gridLiberacao_DXSelBtn")) : "Id inconsistente";
                            System.out.println("Clicando no elemento" +  clickId);
                            this.driver.findElement(By.id(clickId)).click();
//                            Thread.sleep(10000);
                            Utils.waitv(10);

//                            String clickId = row.select("input").first().id() + "_D";
//                            assert(clickId.contains("ctl00_contentManager_gridLiberacao_DXSelBtn")) : "Id inconsistente";
//                            System.out.println("Clicando no elemento" +  clickId);
//                            this.driver.findElement(By.id(clickId)).click();
////                            Thread.sleep(10000);
//                            Utils.waitv(10);
                            this.driver.findElement(By.id("ctl00_contentManager_btnRejeita_CD")).click();
                    		Utils.waitv(4);
//                    		this.driver.switchTo().frame("ctl00_contentManager_popup_TODOS_PWH-1");
                    		this.driver.switchTo().frame("ctl00_contentManager_popup_TODOS_CIF-1");
                    		this.driver.findElement(By.id("ctl00_contentManager_txtMotivo_I")).sendKeys("Alerta sequencia de históricos incorreta");
                    		Utils.waitv(2);
                    		this.driver.findElement(By.id("ctl00_contentManager_btnMotivo_CD")).click();
                    		Utils.waitv(4);
                        }
                        else if(forbidden)
                        {
                        	 // Realizar prorrogacao
                            System.out.println("Rejeitando protesto");
                            System.out.println("Produto " + tituloInstrucao.getNomeProduto().toUpperCase()+ " proibido para esta instrução!");
                            String clickId = row.select("input").first().id() + "_D";
                            assert(clickId.contains("ctl00_contentManager_gridLiberacao_DXSelBtn")) : "Id inconsistente";
                            System.out.println("Clicando no elemento" +  clickId);
                            this.driver.findElement(By.id(clickId)).click();
//                            Thread.sleep(10000);
                            Utils.waitv(10);

//                            String clickId = row.select("input").first().id() + "_D";
//                            assert(clickId.contains("ctl00_contentManager_gridLiberacao_DXSelBtn")) : "Id inconsistente";
//                            System.out.println("Clicando no elemento" +  clickId);
//                            this.driver.findElement(By.id(clickId)).click();
////                            Thread.sleep(10000);
//                            Utils.waitv(10);
                            this.driver.findElement(By.id("ctl00_contentManager_btnRejeita_CD")).click();
                    		Utils.waitv(4);
//                    		this.driver.switchTo().frame("ctl00_contentManager_popup_TODOS_PWH-1");
                    		this.driver.switchTo().frame("ctl00_contentManager_popup_TODOS_CIF-1");
                    		this.driver.findElement(By.id("ctl00_contentManager_txtMotivo_I")).sendKeys("Produto " + tituloInstrucao.getNomeProduto().toUpperCase()+ " proibido para esta instrução!");
                    		Utils.waitv(2);
                    		this.driver.findElement(By.id("ctl00_contentManager_btnMotivo_CD")).click();
                    		Utils.waitv(4);
                        }
                        else if(vencidoAntigo)
                        {
                        	 // Realizar prorrogacao
                            System.out.println("Rejeitando protesto");
                            System.out.println("Este título está vencido há " + Math.abs(expiredDays)+ " o limite sendo 59 dias");
                            String clickId = row.select("input").first().id() + "_D";
                            assert(clickId.contains("ctl00_contentManager_gridLiberacao_DXSelBtn")) : "Id inconsistente";
                            System.out.println("Clicando no elemento" +  clickId);
                            this.driver.findElement(By.id(clickId)).click();
//                            Thread.sleep(10000);
                            Utils.waitv(10);

//                            String clickId = row.select("input").first().id() + "_D";
//                            assert(clickId.contains("ctl00_contentManager_gridLiberacao_DXSelBtn")) : "Id inconsistente";
//                            System.out.println("Clicando no elemento" +  clickId);
//                            this.driver.findElement(By.id(clickId)).click();
////                            Thread.sleep(10000);
//                            Utils.waitv(10);
                            this.driver.findElement(By.id("ctl00_contentManager_btnRejeita_CD")).click();
                    		Utils.waitv(4);
//                    		this.driver.switchTo().frame("ctl00_contentManager_popup_TODOS_PWH-1");
                    		this.driver.switchTo().frame("ctl00_contentManager_popup_TODOS_CIF-1");
                    		this.driver.findElement(By.id("ctl00_contentManager_txtMotivo_I")).sendKeys("Este título está vencido há " + Math.abs(expiredDays)+ " o limite sendo 59 dias");
                    		Utils.waitv(2);
                    		this.driver.findElement(By.id("ctl00_contentManager_btnMotivo_CD")).click();
                    		Utils.waitv(4);
                        }
                    }
                    listRows = getListRows(this.driver.getPageSource());
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
        return this.getListCritica();
    }
    
    public String checkRecompraExFactoring(Connection connMSS, String identificacaoTitulo, String apelidoEmpresa, String apelidoCedente)
    {
    	boolean recomprado=false;
    	boolean exFactoring=false;
    	Statement st=null;
    	try {
			st=connMSS.createStatement();
		} catch (SQLException e) {
			e.printStackTrace();
		}
    	String query="select * from BMA.dbo.titulo"
    						+ " where duplicata='"+identificacaoTitulo+"'"
    						+ " and empresa='"+apelidoEmpresa+"'"
    						+ " and cedente='"+apelidoCedente+"'";
    	
    	System.out.println(query);
    	ResultSet rs=null;
    	
    	try {
			rs=st.executeQuery(query);
			while(rs.next())
			{				
				if(rs.getDate("data_recompra")!=null)
				{
					System.out.println("Titulo recomprado!");
					System.out.println("Empresa: "+apelidoEmpresa);
					System.out.println("Cedente: "+apelidoCedente);
					recomprado=true;
				}
				if(rs.getInt("tipo_operacao")==4)
				{
					exFactoring=true;
					System.out.println("Titulo é ex-factoring!");
				}				
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
    	Utils.waitv(5);
    	String answerString="";
    	if(recomprado)
    	{
    		answerString="recomprado";
    	}
    	if(exFactoring)
    	{
    		answerString+="exfactoring";
    	}
//    	return (recomprado&&exFactoring);
    	return answerString;
    }


    public static void main(String[] args) {

        RgbsysSeleniumProtesto rgbSysSelenium = new RgbsysSeleniumProtesto("MOISES", "moises");
        try
        { 
            rgbSysSelenium.login();
//            rgbSysSelenium.protestar();
            
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
}
