package fromtis;

import java.awt.Toolkit;
import java.util.List;

import org.openqa.selenium.By;
import org.openqa.selenium.Dimension;
import org.openqa.selenium.Keys;
import org.openqa.selenium.Point;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;

import utils.Utils;

public class AprovadorGestor {
//	private String usuario="marisa.moreira1";
//	private String senha="2jbgmHG#";
//	private String nomeMae="Marlisa";
//	private String sobrenome="Moreira";
//	private String dataNascimento="16011985";
	private String usuario="priscilla.campos1";
	private String senha="HieF7nI#";
	private String nomeMae="Maria";
	private String sobrenome="Campos";
	private String dataNascimento="09051988";
	private WebDriver driver=null;
	
	public AprovadorGestor()
	{
		driver = new ChromeDriver();
		java.awt.Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        int screenWidth = screenSize.width;
        int screenHeight = screenSize.height;
		Dimension dimension = new Dimension(1024,1200);
		driver.manage().window().setSize(dimension);
		
		 int x = screenWidth - dimension.getWidth();
	     int y = screenHeight - dimension.getHeight();
	     driver.manage().window().setPosition( new Point(x,y));
	}
	
	public static void main(String[] args) {
		AprovadorGestor aprovadorGestor = new AprovadorGestor();
		
		int attemps=0;
		int totalAttempts=60;
//		while(true)
//		{
			aprovadorGestor.login();
//			while(attemps< totalAttempts)
			while(true)
			{
//				System.out.println("Attempt "+attemps);
				List<WebElement> rows = aprovadorGestor.refresh();
				
				if(rows!=null)
				{					
					aprovadorGestor.aprovar(rows);
				}				
				else
				{
					Utils.waitv(4);
				}
				Utils.waitv(10);
				attemps++;
			}
//		}
	}
	
	public void login()
	{
		this.driver.get("https://portalfidc.singulare.com.br/portal/login");
		this.driver.findElement(By.id("j_username")).sendKeys(this.usuario);
		this.driver.findElement(By.id("j_password")).sendKeys(this.senha);
		Utils.waitv(3);
		this.driver.findElement(By.tagName("button")).submit();
		List<WebElement> sobrenome = this.driver.findElements(By.id("sobrenome"));
		if(!sobrenome.isEmpty())
		{
			if(sobrenome.get(0).isEnabled())
			{
				sobrenome.get(0).sendKeys(this.sobrenome);
			}
		}
		
		List<WebElement> nomeMae = this.driver.findElements(By.id("nomeMae"));
		if(!nomeMae.isEmpty())
		{
			if(nomeMae.get(0).isEnabled())
			{
				nomeMae.get(0).sendKeys(this.nomeMae);
			}
		}
		
		List<WebElement> dataNascimento = this.driver.findElements(By.id("dataNascimento"));
		if(!dataNascimento.isEmpty())
		{
			if(dataNascimento.get(0).isEnabled())
			{
				dataNascimento.get(0).sendKeys(this.dataNascimento);
				dataNascimento.get(0).sendKeys(Keys.RETURN);
			}
		}
		Utils.waitv(3);
		WebElement confirmar =  this.driver.findElement(By.id("btnValidarDupla"));
		confirmar.click();		
		Utils.waitv(7);
	}
	
	public List<WebElement> refresh()
	{
		List<WebElement> rows = null;
		
		this.driver.get("https://portalfidc.singulare.com.br/portal/financeiro/liquidacao");
		Utils.waitv(3);
		this.driver.findElement(By.id("pesquisa")).click();
		Utils.waitv(5);
		List<WebElement> naoExistemDados=this.driver.findElements(By.xpath("//*[@id=\"liquidacaoForm\"]/div[1]/div[1]/div[1]"));
		if(!naoExistemDados.isEmpty())
		{
			if(naoExistemDados.get(0).getText().toLowerCase().contains("existem"))
			{
				System.out.println("Nao existem dados!");
				return rows;
			}
		}

		WebElement tableBody = this.driver.findElement(By.tagName("tbody"));
		rows = tableBody.findElements(By.tagName("tr"));
		System.out.println(rows.size()+" operacoes para aprovar");		
		return rows;
	}
	
	public void aprovar(List<WebElement> rows)
	{				
		if(rows.size()>0)
		{
			int randomInt = (int)(Math.random()*rows.size());
			System.out.println("RandomInt: "+randomInt);
			if(rows.size()==1)
			{
				rows.get(0).click();
			}
			else {
				rows.get(randomInt).click();	
			}
			Utils.waitv(4);
			this.driver.findElement(By.id("aprovar")).click();
			Utils.waitv(4);
		}
	}

	public String getUsuario() {
		return this.usuario;
	}

	public void setUsuario(String usuario) {
		this.usuario = usuario;
	}

	public String getSenha() {
		return this.senha;
	}

	public void setSenha(String senha) {
		this.senha = senha;
	}

	public String getNomeMae() {
		return this.nomeMae;
	}

	public void setNomeMae(String nomeMae) {
		this.nomeMae = nomeMae;
	}

	public String getSobrenome() {
		return this.sobrenome;
	}

	public void setSobrenome(String sobrenome) {
		this.sobrenome = sobrenome;
	}

	public String getDataNascimento() {
		return this.dataNascimento;
	}

	public void setDataNascimento(String dataNascimento) {
		this.dataNascimento = dataNascimento;
	}

	public WebDriver getDriver() {
		return this.driver;
	}

	public void setDriver(WebDriver driver) {
		this.driver = driver;
	}
}
