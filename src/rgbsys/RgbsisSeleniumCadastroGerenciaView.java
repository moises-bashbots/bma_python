package rgbsys;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class RgbsisSeleniumCadastroGerenciaView extends RgbsysSelenium {

    RgbsisSeleniumCadastroGerenciaView(String userName, String password)
    {
        super(userName, password);
    }

    // Pattern patternCpfCnpj = Pattern.compile("/[0-9]{2,3}.?[0-9]{3}.?[0-9]{3}/?[0-9]{4}-?[0-9]{2}|");

    protected String getCpfCnpjFromRow(String row)
    {
        return row.split(" ")[0].replaceAll("\\D+","");
    }


    protected List<String> getListRows(String htmlString)
    {
        List<String> listReturn = new ArrayList<String>();
        Pattern pattern = Pattern.compile("ctl00_contentManager_gridprincipal_DXDataRow[0-9]{1,100}");
        Matcher m = pattern.matcher(htmlString);
        while (m.find()) {
            listReturn.add(m.group());
        }
        return listReturn;
    }

    protected Boolean checkIfIsLastPage(String htmlSource)
    {
        String nextButtonIsDisabled = "<b class=\"dxp-button dxp-bt dxp-disabledButton\">Next &gt;</b>";
        if (htmlSource.contains(nextButtonIsDisabled))
        {
            System.out.println("Encontra-se na ultima pagina de iteracao");
            return true;
        }

        String nextButtonIsEnabled = "<a class=\"dxp-button dxp-bt\" onclick=\"aspxGVPagerOnClick('ctl00_contentManager_gridprincipal','PBN');\">Next &gt;</a>";
        if (htmlSource.contains(nextButtonIsEnabled))
        {
            System.out.println("Ha mais paginas a se iterar");
            return false;
        }

        System.out.println("Nao se encontrou botao de proxima pagina nem de que chegou-se a ao fim da iteracao. Portanto trata-se de uma view de pagina unica.");
        return true;

    }

    protected void gotoNextPage()
    {
        try 
        {
            String checkIfIsNext = (String) this.jsExecutor.executeScript("return document.querySelector(\"#ctl00_contentManager_gridprincipal_DXPagerBottom > a:nth-child(14)\").text;");

            assert (checkIfIsNext.contains("Next")) : "gptoNextPage from class RgbsisSeleniumCadastroGerenciaView is Broken.";

            String command = "document.querySelector(\"#ctl00_contentManager_gridprincipal_DXPagerBottom > a:nth-child(14)\").click();";
            this.jsExecutor.executeScript(command);
            Thread.sleep(10000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }


    
}
