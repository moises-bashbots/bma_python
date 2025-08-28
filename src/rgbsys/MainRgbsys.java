package rgbsys;

import java.util.List;
import java.util.Vector;

public class MainRgbsys {

    // Classe exemplo de como usar as demais classes

    String userName;
    String password;

    MainRgbsys(String userName, String password)
    {
        this.userName = userName;
        this.password = password;
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
    
}
