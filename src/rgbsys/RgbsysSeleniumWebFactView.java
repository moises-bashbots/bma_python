package rgbsys;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

import utils.Utils;

public class RgbsysSeleniumWebFactView extends RgbsysSelenium {
	private RgbsysUser rgbsysUser = new RgbsysUser();
    
	public RgbsysSeleniumWebFactView() {
		super();
	}
	
	public RgbsysSeleniumWebFactView(RgbsysUser rgbsysUser)
	{
		this.rgbsysUser=rgbsysUser;
	}
	
	public RgbsysSeleniumWebFactView(String userName, String password)
    {
        super(userName, password);
    }

    protected List<String> getListRows(String htmlString)
    {
        List<String> listReturn = new ArrayList<String>();
        Pattern pattern = Pattern.compile("ctl00_contentManager_gridLiberacao_DXDataRow[0-9]{1,100}");
        Matcher m = pattern.matcher(htmlString);
        while (m.find()) 
        {
//        	System.out.println("Adding "+m.group());
            listReturn.add(m.group());
        }
        return listReturn;
    }

    protected HashMap<Integer, String> getTableCols(String htmlString)
    {
        HashMap<Integer, String> mapReturn = new HashMap<Integer, String>();

        Document doc = Jsoup.parse(htmlString);
        Element table = doc.getElementById("ctl00_contentManager_gridLiberacao_DXHeadersRow0");
        Elements tableRows = table.select("td");

        int counter = 0;
        for (Element row : tableRows)
        {  
            if (!mapReturn.containsValue(row.text()) && row.text().length() > 0)
            {
                mapReturn.put(counter, row.text());
                counter++;
            }
        }
        // System.out.println("map return");
        // System.out.println(mapReturn);

        return mapReturn;
    }

    protected void makeTableHigher()
    {
            String command = "document.querySelector(\"#ctl00_contentManager_gridLiberacao > tbody > tr > td > div.dxgvCSD\").style = \"height: 900px; overflow: scroll; width: 1131px\";";
            command = "document.querySelector(\"#ctl00_contentManager_gridLiberacao\").style = \"font-size: 9px; width: 1131px; border-collapse: collapse; margin-right: 6px; height: 600px;\";";
            System.out.println(command);
            jsExecutor.executeScript(command);
//            Thread.sleep(10000);
            Utils.waitv(10);

            String secondCommand = "document.querySelector(\"#ctl00_contentManager_gridCell\").style = \"border: 1px solid #3E5395; top: 170x; width: 1150px; height: 1000px; text-align: center; position: absolute; left: 34px; z-index: 0; overflow: auto\";";
            System.out.println(secondCommand);
            jsExecutor.executeScript(secondCommand);
//            Thread.sleep(10000);
            Utils.waitv(10);
    }

    protected Boolean checkIfIsLastPage(String htmlSource)
    {
        String nextButtonIsDisabled = "<b class=\"dxp-button dxp-bt dxp-disabledButton\">Next &gt;</b>";
        if (htmlSource.contains(nextButtonIsDisabled))
        {
            System.out.println("Encontra-se na ultima pagina de iteracao");
            return true;
        }

        String nextButtonIsEnabled = "<a class=\"dxp-button dxp-bt\" onclick=\"aspxGVPagerOnClick('ctl00_contentManager_gridLiberacao','PBN');\">Next &gt;</a>";
        if (htmlSource.contains(nextButtonIsEnabled))
        {
            System.out.println("Ha mais paginas a se iterar");
            return false;
        }

        System.out.println("Nao se encontrou botao de proxima pagina nem de que chegou-se a ao fim da iteracao. Portanto trata-se de uma view de pagina unica.");
        return true;

    }

    protected void gotoNextPage(int currentPage)
    {
        // TODO DOUBLE CHECK
    	// #ctl00_contentManager_gridLiberacao_DXPagerBottom > a.dxp-button.dxp-bt
    	// #ctl00_contentManager_gridLiberacao_DXPagerBottom > a:nth-child(8)
    	
    	
    	// 1 #ctl00_contentManager_gridLiberacao_DXPagerBottom > a.dxp-button.dxp-bt
    	// 2 #ctl00_contentManager_gridLiberacao_DXPagerBottom > a:nth-child(8)
    	// 3 #ctl00_contentManager_gridLiberacao_DXPagerBottom > a:nth-child(8)
    	// 4 #ctl00_contentManager_gridLiberacao_DXPagerBottom > a:nth-child(8)
    	// #ctl00_contentManager_gridLiberacao_DXPagerBottom > a:nth-child(8)
    	
    	
    	// //*[@id="ctl00_contentManager_gridLiberacao_DXPagerBottom"]/a[5]
    	// //*[@id="ctl00_contentManager_gridLiberacao_DXPagerBottom"]/a[6]    	
    	// //*[@id="ctl00_contentManager_gridLiberacao_DXPagerBottom"]/a[6]
    	// //*[@id="ctl00_contentManager_gridLiberacao_DXPagerBottom"]/a[6]
    	
    	
//    	#ctl00_contentManager_gridLiberacao_DXPagerBottom > a.dxp-button.dxp-bt
//    	#ctl00_contentManager_gridLiberacao_DXPagerBottom > a:nth-child(7)
//    	#ctl00_contentManager_gridLiberacao_DXPagerBottom > a:nth-child(8)
//    	#ctl00_contentManager_gridLiberacao_DXPagerBottom > a:nth-child(8)
    	
    	WebElement pagerElement = this.driver.findElement(By.id("ctl00_contentManager_gridLiberacao_DXPagerBottom"));
    	List<WebElement> pages = pagerElement.findElements(By.tagName("a"));
    	Utils.waitv(2);
    	for(WebElement page:pages)
    	{
    		System.out.println(page.getText());
    		if(page.getText().toLowerCase().contains("next"))
    		{
    			page.click();
    		}
    	}
    	
//        String command = "document.querySelector(\"#ctl00_contentManager_gridLiberacao_DXPagerBottom > a.dxp-button.dxp-bt\").click();";
//        Utils.waitv("Before choosing next page",2);
//        this.jsExecutor.executeScript(command);
//        if(currentPage>1)
//        {
//	        command = "document.querySelector(\"#ctl00_contentManager_gridLiberacao_DXPagerBottom > a:nth-child(7)\").click();";
//	        this.jsExecutor.executeScript(command);
//        }
////        Thread.sleep(10000);
        Utils.waitv("After choosing next page",8);
    }

	public RgbsysUser getRgbsysUser() {
		return rgbsysUser;
	}

	public void setRgbsysUser(RgbsysUser rgbsysUser) {
		this.rgbsysUser = rgbsysUser;
	}

}
