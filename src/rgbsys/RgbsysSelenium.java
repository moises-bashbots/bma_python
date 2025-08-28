package rgbsys;

import java.awt.AWTException;
import java.awt.Robot;
import java.awt.event.KeyEvent;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.reflect.Field;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import org.apache.commons.io.FileUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;

import conta_grafica.Critica;
import io.github.bonigarcia.wdm.WebDriverManager;
import utils.Utils;

//comment the above line and uncomment below line to use Chrome
//import org.openqa.selenium.chrome.ChromeDriver;
public class RgbsysSelenium
{

    // System.setProperty("webdriver.gecko.driver","C:\\geckodriver.exe");
	protected String userName;
    protected  String password;
    protected String rootURL;
    public WebDriver driver;
    private ArrayList<Critica> listCritica = new ArrayList<Critica>(); 
    protected SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy", Locale.ENGLISH);
    private static SimpleDateFormat sdf=new SimpleDateFormat("yyyy-MM-dd");
    private Path savingFolder;
    private Path tempSavingFolder;
    
//    public RgbsysSelenium()
//    {
//    	this.userName=RgbsysUser.userName;
//    	this.password=RgbsysUser.password;
//    	this.rootURL=RgbsysUser.rootURL;
//    }

    public Path getSavingFolder() {
        return savingFolder;
    }

    public void setSavingFolder(Path savingFolder) {
        System.out.println("Definindo saving folder como: " + savingFolder.toAbsolutePath().toString());
        createNewFolderIfNotExists(savingFolder);
        this.savingFolder = savingFolder;
    }

    public Path getTempSavingFolder() {
        return this.tempSavingFolder;
    }

    public void setTempSavingFolder(Path tempSavingFolder) {
        System.out.println("Definindo temp saving folder como: " + tempSavingFolder.toAbsolutePath().toString());
        createNewFolderIfNotExists(tempSavingFolder);
        this.tempSavingFolder = tempSavingFolder;
    }

    JavascriptExecutor jsExecutor ;

    public JavascriptExecutor getJsExecutor() {
        return this.jsExecutor;
    }

    public void setJsExecutor(JavascriptExecutor jsExecutor) {
        this.jsExecutor = jsExecutor;
    }

    public RgbsysSelenium(WebDriver driver) {
        this.driver = driver;
    }

    public WebDriver getDriver() {
        return this.driver;
    }

    public void setDriver(WebDriver driver) {
        this.driver = driver;
    }

    public RgbsysSelenium driver(WebDriver driver) {
        setDriver(driver);
        return this;
    }

    public String getUserName() {
        return this.userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getPassword() {
        return this.password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public RgbsysSelenium userName(String userName) {
        setUserName(userName);
        return this;
    }

    public RgbsysSelenium password(String password) {
        setPassword(password);
        return this;
    }

    @Override
    public String toString() {
        return "{" +
            " userName='" + getUserName() + "'" +
            ", password='" + getPassword() + "'" +
            "}";
    }

    /**
     * @param _userName
     * @param _password
     */
    public RgbsysSelenium(String userName, String password)
    {
        setUserName(userName);
        setPassword(password);
        // WebDriver driver = new FirefoxDriver();
//        WebDriverManager.chromedriver().setup();

        // Processo para habilitar popups. Necessario na simula����o de recompra e quitacao de baixa
        ChromeOptions options = new ChromeOptions();
        options.addArguments("test-type");
        options.addArguments("disable-popup-blocking");
        

        Map<String, Object> prefs = new HashMap<String, Object>(); 
        prefs.put("plugins.always_open_pdf_externally", true);
        options.setExperimentalOption("prefs", prefs);
        options.addArguments("--disable-extensions"); //to disable browser extension 
    	options.addArguments("disable-infobars");
		options.addArguments("ignore-certificate-errors");
		options.addArguments("--ignore-urlfetcher-cert-requests");
		options.addArguments("--remote-allow-origins=*");

//        DesiredCapabilities capabilities = DesiredCapabilities.chrome();
//        capabilities.setCapability(ChromeOptions.CAPABILITY, options);
        ///// 

        WebDriver driver = new ChromeDriver(options);
        setDriver(driver);
        setJsExecutor((JavascriptExecutor) this.driver);
//        driver.manage().window().maximize();
        if(login())
        {
        	System.out.println("Successful login!");
        }
        else {
			System.out.println("Error to login!");
			this.driver.close();
			System.exit(1);
		}
    }

    public RgbsysSelenium(String userName, String password, Path downloadDir, String rootURL)
    {
        System.out.println("NO novo construtor");
        setUserName(userName);
        setPassword(password);
        setRootURL(rootURL);
        // WebDriver driver = new FirefoxDriver();
        WebDriverManager.chromedriver().setup();

        // Processo para habilitar popups. Necessario na simula����o de recompra e quitacao de baixa
        ChromeOptions options = new ChromeOptions();
        options.addArguments("test-type");
        options.addArguments("disable-popup-blocking");

        Map<String, Object> prefs = new HashMap<String, Object>(); 
        prefs.put("plugins.always_open_pdf_externally", true);
        setSavingFolder(downloadDir);
        setTempSavingFolder(generateRandomFolder(downloadDir));
        prefs.put("download.default_directory", getTempSavingFolder().toAbsolutePath().toString()); // Bypass default download directory in Chrome
        // prefs.put("download.default_directory", downloadDir.toString()); // Bypass default download directory in Chrome

        createNewFolderIfNotExists(getTempSavingFolder());

        options.setExperimentalOption("prefs", prefs);
        options.addArguments("--disable-extensions"); //to disable browser extension 

//        DesiredCapabilities capabilities = DesiredCapabilities.chrome();
//        capabilities.setCapability(ChromeOptions.CAPABILITY, options);
        ///// 

        WebDriver driver = new ChromeDriver(options);
        setDriver(driver);
        setJsExecutor((JavascriptExecutor) this.driver);
        login();
    }

    public RgbsysSelenium()
    {
        System.out.println("Using the new construtor");
    	this.userName=RgbsysUser.userName;
    	this.password=RgbsysUser.password;
    	this.rootURL=RgbsysUser.rootURL;

        setUserName(RgbsysUser.userName);
        setPassword(RgbsysUser.password);
        setRootURL(RgbsysUser.rootURL);
        Path downloadDir = Paths.get(RgbsysUser.pathDownloads);
        // WebDriver driver = new FirefoxDriver();
//        WebDriverManager.chromedriver().setup();

        // Processo para habilitar popups. Necessario na simula����o de recompra e quitacao de baixa
        ChromeOptions options = new ChromeOptions();
        options.addArguments("test-type");
        options.addArguments("--window-size=1920,1200");
        options.addArguments("disable-popup-blocking");
        options.addArguments("--no-sandbox");
        options.addArguments("--disable-dev-shm-usage");

        Map<String, Object> prefs = new HashMap<String, Object>(); 
        prefs.put("plugins.always_open_pdf_externally", true);
        setSavingFolder(downloadDir);
        setTempSavingFolder(Paths.get(downloadDir+File.separator+sdf.format(Calendar.getInstance().getTime())));
        prefs.put("download.default_directory", getTempSavingFolder().toAbsolutePath().toString()); // Bypass default download directory in Chrome
        // prefs.put("download.default_directory", downloadDir.toString()); // Bypass default download directory in Chrome

        createNewFolderIfNotExists(getTempSavingFolder());

        options.setExperimentalOption("prefs", prefs);
        options.addArguments("--disable-extensions");
        options.addArguments("--disable-gpu");
        options.addArguments("--no-sandbox");
        options.addArguments("--disable-dev-shm-usage");
        //to disable browser extension 

//        DesiredCapabilities capabilities = DesiredCapabilities.chrome();
//        capabilities.setCapability(ChromeOptions.CAPABILITY, options);
        ///// 

        WebDriver driver = new ChromeDriver(options);
        
        setDriver(driver);
        setJsExecutor((JavascriptExecutor) this.driver);
        login();
    }
    
    protected void waitForFileToBeDownloaded()
    {
        int timeout = 60;
        int timeElapsed = 0;
        int originalCount = 0;
        try 
        {
            while (true)
            {
                int numberOfFiles = getTempSavingFolder().toFile().list().length;
                System.out.println("numberOfFiles: " + numberOfFiles);
                if (numberOfFiles > originalCount)
                {
                    break;
                }
                else
                {
                    Thread.sleep(1000);
                    timeElapsed++;
                }
                if (timeElapsed > timeout)
                {
                    System.out.println("Chegou-se ao timeout de espera de download do arquivo. Abortando processo.");
                    System.exit(1);
                }
            }
        } 
        catch (InterruptedException e) 
        {
            e.printStackTrace();
        }
    }

    public void close()
    {
        saveListCriticaOperacoesRealizadasToDatabase();
//        cleanTempFolder();

        System.out.println("Finalizando driver.");
        this.driver.quit();
    }

    static protected Path generateRandomFolder(Path baseDir)
    {
        return Paths.get(baseDir.toString(), "random_" + UUID.randomUUID().toString());
    }

    void cleanTempFolder()
    {
        try 
        {
            if (this.tempSavingFolder != null)
            {
                System.out.println("Limpando folder temporario " + this.tempSavingFolder.toAbsolutePath().toString());
                FileUtils.deleteDirectory(this.tempSavingFolder.toFile());
            }
        } 
        catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void assertNoDuplicatesInListCritica()
    {}

    public void saveListCriticaOperacoesRealizadasToDatabase()
    {
        assertNoDuplicatesInListCritica();

        if (this.listCritica.size() == 0)
        {
            System.out.println("Nao há criticas a salvar");
        }

        // Caso o scraper venha a da crash durante a operação, esse método deve salvar as criticas das operações do que já foi feito.
        for (Critica critica : this.listCritica)
        {
            critica.addCriticaToBeExecuted();
        }
//        listCritica.clear();
    }

    protected static void pressSystemEnterKey() {
        Robot robot = null;//  w  ww  . j a  v  a2s .co  m
        try {
            robot = new Robot();
        } catch (AWTException e) {
            e.printStackTrace();
        }
        robot.keyPress(KeyEvent.VK_ENTER);
        robot.keyRelease(KeyEvent.VK_ENTER);
    }

    protected static void pressSystemEscKey() {
        Robot robot = null;//  w  ww  . j a  v  a2s .co  m
        try {
            robot = new Robot();
        } catch (AWTException e) {
            e.printStackTrace();
        }
        robot.keyPress(KeyEvent.VK_ESCAPE);
        robot.keyRelease(KeyEvent.VK_ESCAPE);
    }

    public static long getDifferenceDays(Date d1, Date d2) 
    {
        long diff = d2.getTime() - d1.getTime();
        return TimeUnit.DAYS.convert(diff, TimeUnit.MILLISECONDS);
    }

    protected int countNumberOfRowsInTable(String tablePathId)
    {
        Document doc = Jsoup.parse(this.driver.getPageSource());
        Element table = doc.getElementById(tablePathId);
        Elements tableRows = table.select("tr");
        return tableRows.size();
    }

    protected void createNewFolderIfNotExists(Path path)
    {
        File directory = new File(path.getParent().toAbsolutePath().toString());
        if (!directory.exists()) 
        {
            System.out.println("Creating folder: " + path.getParent().toAbsolutePath().toString());
            directory.mkdirs();
        }
    }

    protected void createNewFileIfNotExists(Path path)
    {
        try 
        {
            File directory = new File(path.getParent().toAbsolutePath().toString());
            if (!directory.exists()) 
            {
                directory.mkdirs();
            }

            File myObj = new File(path.toString());
            if (myObj.createNewFile()) 
            {
                System.out.println("File created: " + myObj.getName());
            } 
            else
            {
                System.out.println("File already exists.");
            }
        } 
        catch (IOException e)
        {
            System.out.println("An error occurred.");
            e.printStackTrace();
        }
    }

    protected void assertBool(Boolean evaluation, String errorMessage)
    {
        if (!evaluation)
        {
            System.out.println(errorMessage);
            close();
            System.exit(1);
        }
    }

    protected void assertStringEqualsAnotherString(String firstString, String secondString, String errorMessage)
    {
        if (!firstString.equals(secondString))
        {
            System.out.println(errorMessage);
            close();
            System.exit(1);
        }
    }

    protected void assertStringContainsAnotherString(String bigString, String smallString, String errorMessage)
    {
        if (!bigString.contains(smallString))
        {
            System.out.println(errorMessage);
//            close();
            System.exit(1);
        }
    }

    protected void assertDoubleEqualsAnotherDouble(Double firstVal, Double secondVal, String errorMessage)
    {
        if (firstVal != secondVal)
        {
            System.out.println(errorMessage);
            close();
            System.exit(1);
        }
    }

    protected static void printStackTrace(Exception e)
    {
        StringWriter sw = new StringWriter();
        e.printStackTrace(new PrintWriter(sw));
        System.out.println(e.getStackTrace().toString()); 
    }

    private static void typeCharacter(Robot robot, String letter)
    {
        try
        {
            boolean upperCase = Character.isUpperCase( letter.charAt(0) );
            String variableName = "VK_" + letter.toUpperCase();

            System.out.println(variableName);
            Class clazz = KeyEvent.class;
            Field field = clazz.getField( variableName );
            int keyCode = field.getInt(null);

            robot.delay(300);

            if (upperCase) robot.keyPress( KeyEvent.VK_SHIFT );

            robot.keyPress( keyCode );
            robot.keyRelease( keyCode );

            if (upperCase) robot.keyRelease( KeyEvent.VK_SHIFT );
        }
        catch(Exception e)
        {
            System.out.println(e);
        }
    }

    protected void robotTypeString(String string)
    {
        Robot robot;
        try {
            robot = new Robot();
            for (char ch: string.toCharArray()) {
                typeCharacter(robot, String.valueOf(ch));
            }
        } catch (AWTException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    public boolean  login()
    {
    	boolean success=true;
    	try {
        	System.out.println(RgbsysUser.rootURL+"/"+"Login.aspx");
            this.driver.get(RgbsysUser.rootURL+"/"+"Login.aspx");
            Utils.waitv(5);
            this.driver.findElement(By.xpath("//*[@id='edusuario_I']")).sendKeys(getUserName());
            Utils.waitv(3);
            this.driver.findElement(By.xpath("//*[@id='edsenha_I']")).sendKeys(getPassword());
            Utils.waitv(3);
            this.driver.findElement(By.name("LoginButton")).click();
            Utils.waitv(3);
            List<WebElement> errorHorario=this.driver.findElements(By.xpath("//*[@id=\"lbmsg\"]"));
            if(!errorHorario.isEmpty())
            {
            	if(errorHorario.get(0).isDisplayed())
            	{
            		success=false;
            	}
            }
		} catch (Exception e) {
			e.printStackTrace();
			success=false;
		}

            
            
        return success;
    }

    String goHomePage()
    {
        String stringReturn = "";
        try 
        {
            this.driver.get(this.rootURL+"Start.aspxl");
//            this.driver.get("https://gercloud2.rgbsys.com.br/BMA_HOMOLOGA/Start.aspxl");
            Thread.sleep(10000);
        }
        catch (InterruptedException ex) 
        {
            Thread.currentThread().interrupt();
        }
        return stringReturn;
    }

    protected Boolean checkSessionEnded()
    {
        if (this.driver.getPageSource().contains("Tempo de sess��o excedido"))
        {
            return true;
        }
        return false;
    }

    protected void restartSession()
    {
        if (checkSessionEnded())
        {
            login();
        }
    }



    public static void main(String[] args) {

        RgbsysSelenium rgbSysSelenium = new RgbsysSelenium("MOISES", "moises");
        try
        { 
            rgbSysSelenium.login();
            
        }
        finally
        {
            rgbSysSelenium.driver.close();

        }

    }

	public String getRootURL() {
		return rootURL;
	}

	public void setRootURL(String rootURL) {
		this.rootURL = rootURL;
	}

	public ArrayList<Critica> getListCritica() {
		return listCritica;
	}

	public void setListCritica(ArrayList<Critica> listCritica) {
		this.listCritica = listCritica;
	}

	public SimpleDateFormat getFormatter() {
		return formatter;
	}

	public void setFormatter(SimpleDateFormat formatter) {
		this.formatter = formatter;
	}


}