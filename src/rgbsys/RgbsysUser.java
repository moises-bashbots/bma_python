package rgbsys;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class RgbsysUser {

    // Classe exemplo de como usar as demais classes

	public static  String userName="";
	public static String password="";
	public static String pathDownloads="";
	public static String rootURL="";

    public RgbsysUser(String userName, String password, String pathDownloads)
    {
        RgbsysUser.userName = userName;
        RgbsysUser.password = password;
        RgbsysUser.pathDownloads=pathDownloads;
    }
    
    public RgbsysUser()
    {
    	
    }
    

    public  static void readConf()
	{		
    	System.out.println("##############################################");
    	System.out.println("READING CONFIGURATION");
    	System.out.println("##############################################");
		List<Object> confLines = new ArrayList<>();
		String nameOS = System.getProperty("os.name");
		String fileName="../Conf/rgbsys.conf";
		if(!nameOS.equals("Linux"))
		{	
			fileName = "..\\Conf\\rgbsys.conf";		
		}
		
		File confFile = new File(fileName);
		
		if(!confFile.exists())
		{
			System.out.println("Config file not found in relative path! " + fileName);
			fileName=System.getProperty("user.home")+"/App/Conf/rgbsys.conf";
			confFile = new File(fileName);
			if(!confFile.exists())
			{
				System.out.println("Config file not found in absolute path! " + fileName);
			}
		}
		
		System.out.println("Reading "  + fileName);
		try (Stream<String> stream = Files.lines(Paths.get(fileName))){			
			confLines = stream.collect(Collectors.toList());
			for(Object confLine:confLines)
			{
				String line = (String) confLine;
				if(line.startsWith("#"))
				{
					
				}
				else if(!line.isEmpty())
				{
					String[] words = line.split(";");
					String key = words[0];
					String value = words[1];
					switch(key)
					{
						case "user": userName=value; break;
						case "password": password=value; break;
						case "pathDownload": pathDownloads=value; break;
						case "rootURL": rootURL=value; break;
						default: 	break;
					}
				}
				else
				{
					System.out.println("Linha em branco!");
				}
				System.out.println(line);
			}
 		} catch (IOException e) {
			e.printStackTrace();
		}
						
		System.out.println("UserName: "  + userName);
		System.out.println("Password: "  + password);
		System.out.println("PathDownloads:: "  + pathDownloads);
		System.out.println("RootURL: "  + rootURL);
		System.out.println("##############################################");
    	System.out.println("FINISHED READING CONFIGURATION");
    	System.out.println("##############################################");
	}

    void abatimento()
    {
        RgbsysAbatimento rgbSysSelenium = new RgbsysAbatimento(this.userName, this.password);
        try
        { 
            rgbSysSelenium.login();
//            rgbSysSelenium.abatimento();
        }
        catch (Exception e) {
            rgbSysSelenium.saveListCriticaOperacoesRealizadasToDatabase();
        }
        finally
        {
            rgbSysSelenium.driver.close();
        }
    }

    void protesto()
    {

    }

    void simulacao()
    {

    }

    void quitacao()
    {
        // Depois que o cedente efetuar o pix em quest��o e o bot localiz��-lo no banco, o processo de quita����o dever�� ser efetuado no sistema.

        RgbsysSeleniumQuitacaoBaixa rgbSysSelenium = new RgbsysSeleniumQuitacaoBaixa(this.userName, this.password);
        try
        { 

            List<String> listDuplicatasQuitar = new Vector<String>();
            listDuplicatasQuitar.add("116644NE2");
            listDuplicatasQuitar.add("116606NE2");
            String conta = "TESTE";

            // rgbSysSelenium.quitacaoBaixa("BMA FIDC", "MAXIBRASIL", listDuplicatasQuitar, conta);
            
        }
        catch (Exception e) 
        {
            rgbSysSelenium.saveListCriticaOperacoesRealizadasToDatabase();
        }
        finally
        {
            rgbSysSelenium.driver.close();

        }
    }

    void scrapeCadastroCedentes()
    {
        RgbsysScrapeCadastroCedentes rgbSysSelenium = new RgbsysScrapeCadastroCedentes("MOISES", "moises");
        try
        { 
            rgbSysSelenium.setSavingPath("/home/rafaelichow/Documents/bma_temp");
            rgbSysSelenium.scrapeCadastroCedente();
            
        }
        finally
        {
            rgbSysSelenium.driver.close();
        }
    }

    void lancamentoCritica()
    {
        RgbsisLancamentoCritica rgbSysSelenium = new RgbsisLancamentoCritica("MOISES", "moises");
        try
        { 
            // rgbSysSelenium.login();
            rgbSysSelenium.gravacaoCritica( "PRORROGACAO", "116748NE1", "BMA FIDC", "MAXIBRASIL");
            
        }
        finally
        {
            rgbSysSelenium.driver.close();
        }

    }

    void lancamentoEmContaCorrente()
    {}

    void lancamentoEmContaEnvio()
    {}

    void rejeicao()
    {
        // Rejeicao caso o PIX nao tenha sido feito at�� as 21h
    }

    public static void main(String[] args) {
        
    }


	public String getUserName() {
		return userName;
	}


	public void setUserName(String userName) {
		this.userName = userName;
	}


	public String getPassword() {
		return password;
	}


	public void setPassword(String password) {
		this.password = password;
	}


	public String getPathDownloads() {
		return pathDownloads;
	}


	public void setPathDownloads(String pathDownloads) {
		this.pathDownloads = pathDownloads;
	}

	public static String getRootURL() {
		return rootURL;
	}

	public static void setRootURL(String rootURL) {
		RgbsysUser.rootURL = rootURL;
	}
    
}
