package rgbsys;

import java.util.List;
import java.util.Vector;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.openqa.selenium.By;
import org.openqa.selenium.Keys;
import org.unix4j.Unix4j;
import org.unix4j.unix.Ls;

import conta_grafica.Critica;
import conta_grafica.OperacaoRecompra;
import utils.Utils;

// Mesma coisa que Recompra avulsa
public class RgbsysSeleniumQuitacaoBaixa extends RgbsysRecompraView {

    private Double valorSomaTotalRecompraComMoraParaConciliacao;

    public RgbsysSeleniumQuitacaoBaixa()
    {
        super();
    }
    
    public RgbsysSeleniumQuitacaoBaixa(OperacaoRecompra operacaoRecompra)
    {
        super(operacaoRecompra);
    }

    
    public RgbsysSeleniumQuitacaoBaixa(String userName, String password)
    {
        super(userName, password);
    }

    public Double getValorSomaTotalRecompraComMoraParaConciliacao() {
        return valorSomaTotalRecompraComMoraParaConciliacao;
    }

    public void setValorSomaTotalRecompraComMoraParaConciliacao(Double valorSomaTotalRecompraComMoraParaConciliacao) {
        this.valorSomaTotalRecompraComMoraParaConciliacao = valorSomaTotalRecompraComMoraParaConciliacao;
    }

    String getDailyIdSimulacaoRecompra(String savingDateString)
    {
        String stringReturn = "";

        String grep1Filter = "simulacao_recompra_" + savingDateString;
        String grep2Filter = getNomeEmpresa() + "_" + getNomeCedente();

        stringReturn = Unix4j.ls(Ls.Options.l.a, getSavingPath()).grep(grep1Filter).grep(grep2Filter).wc().toString();

        System.out.println(stringReturn);

        return stringReturn;
    }

    void assertValoresCorretos()
    {
        Document doc = Jsoup.parse(this.driver.getPageSource());

        String valorTotalSelecionado = doc.select("ctl00_contentManager_ASPxPageControlRenegociacao_TotSelecionado_I").attr("value");
        String valorTotalRecompra = doc.select("ctl00_contentManager_ASPxPageControlRenegociacao_VlTot_B_I").attr("value");
        assertStringEqualsAnotherString(valorTotalSelecionado, valorTotalRecompra, "Valores de recompra e selecionado n��o batem. Abortando processo!");
    }

    void assertContaBancariaCorreta()
    {}

    void assertEmpresaCedente()
    {}

    void somaMultaMoraCasoExitaUnchecked()
    {
        if (this.valorSomaTotalRecompraComMoraParaConciliacao == null)
        {
            System.out.println("**** ATEN����O ****");
            System.out.println("A variavel valorSomaTotalRecompraComMoraParaConciliacao n��o tem valor. Por isso a etapa extra de concilia����o n��o ser�� feita.");
        }
        else
        {}
    }

    @Override
    void addToListCritica(String idDuplicataQuitar)
    {
        Critica critica = new Critica(
            idDuplicataQuitar, 
            getNomeEmpresa(), 
            getNomeCedente(), 
            "QUITACAO EFETUADA VIA PIX INFORMADA PELO ROBO"
        );

        this.getListCritica().add(critica);
    }

    @Override
    void checkIfVariablesArePopulated()
    {
        if (getNomeCedente() == null)
        {
            System.out.println("Nome cedente n��o est�� definido. Abortando processo.");
        }

        if (getNomeEmpresa() == null)
        {
            System.out.println("Nome empresa n��o est�� definido. Abortando processo.");
        }

        if (getNomeContaBancaria() == null)
        {
            System.out.println("Nome conta banc��ria n��o est�� definida. Abortando processo.");
        }
    }

    @Override
    void childFinishProcess()
    {
        // Volta para pagina anterior
		this.driver.findElement(By.id("ctl00_contentManager_ASPxPageControlRenegociacao_BtProcessa_CD")).click();
		Utils.waitv("Volta para a pagina anterior",1);
//            Thread.sleep(1000);

		// Copia o valor do campo "Valor total selecionado" e cola no campo "Valor da recompra"
		Document doc = Jsoup.parse(this.driver.getPageSource());
		String valorTotalSelecionado = doc.getElementById("ctl00_contentManager_ASPxPageControlRenegociacao_TotSelecionado_I").attr("value").toString();

		// Fazendo colagem do valor da recompra
		System.out.println("Fazendo recompra no valor de " + valorTotalSelecionado);

		this.driver.findElement(By.id("ctl00_contentManager_ASPxPageControlRenegociacao_VlTot_B_I")).clear();
//            Thread.sleep(3000);
		Utils.waitv("Clear control renegociaçãor",3);
		this.driver.findElement(By.id("ctl00_contentManager_ASPxPageControlRenegociacao_VlTot_B_I")).sendKeys(Keys.DELETE);
		for (int i = 0; i < 10; i++)
		{
		    this.driver.findElement(By.id("ctl00_contentManager_ASPxPageControlRenegociacao_VlTot_B_I")).sendKeys(Keys.BACK_SPACE);
		};
		this.driver.findElement(By.id("ctl00_contentManager_ASPxPageControlRenegociacao_VlTot_B_I")).sendKeys(valorTotalSelecionado);
		Utils.waitv("Valor total selecionado",3);
//            Thread.sleep(3000);
		this.driver.findElement(By.id("ctl00_contentManager_ASPxPageControlRenegociacao_VlTot_B_I")).sendKeys(Keys.TAB);
		Utils.waitv("TAB",3);
//            Thread.sleep(3000);

		// Faz o select da conta bancaria
		this.driver.findElement(By.id("ctl00_contentManager_ASPxPageControlRenegociacao_edcontabancaria_I")).clear();
//            Thread.sleep(3000);
		Utils.waitv("Seleciona conta bancaria",3);
		this.driver.findElement(By.id("ctl00_contentManager_ASPxPageControlRenegociacao_edcontabancaria_I")).sendKeys(Keys.DELETE);
		for (int i = 0; i < 10; i++)
		{
		    this.driver.findElement(By.id("ctl00_contentManager_ASPxPageControlRenegociacao_edcontabancaria_I")).sendKeys(Keys.BACK_SPACE);
		};
		this.driver.findElement(By.id("ctl00_contentManager_ASPxPageControlRenegociacao_edcontabancaria_I")).sendKeys(getNomeContaBancaria());
//            Thread.sleep(3000);
		Utils.waitv("Escreve conta bancaria",3);
		this.driver.findElement(By.id("ctl00_contentManager_ASPxPageControlRenegociacao_edcontabancaria_I")).sendKeys(Keys.TAB);
		Utils.waitv("TAB",3);
//            Thread.sleep(3000);

		// Confirma valor da recompra 
		assertValoresCorretos();
		// Confirma conta bancaria
		assertContaBancariaCorreta();
		// Confirma empresa // Confirma Cedente
		assertEmpresaCedente();

		// Clica em gravar
		// this.driver.findElement(By.id("ctl00_contentManager_ASPxButtonNovo_CD")).click();
		// Thread.sleep(5000);

		// Press Enter para tirar o checkbox
		pressSystemEnterKey();
		Utils.waitv("Press enter",2);
    }

    public static void main(String[] args) {

        String processUrl = "https://gercloud2.rgbsys.com.br/BMA_HOMOLOGA/operacoes/forms/RenegociacaoBorderoPesquisaNova.aspx?origem=2";
        RgbsysSeleniumQuitacaoBaixa rgbSysSelenium = new RgbsysSeleniumQuitacaoBaixa("MOISES", "moises");
        try
        { 
            rgbSysSelenium.setNomeEmpresa("BMA FIDC");
            rgbSysSelenium.setNomeCedente("MAXIBRASIL");
            rgbSysSelenium.setNomeContaBancaria("BRADESCO FIDC");

            List<String> listDuplicatasQuitar = new Vector<String>();
            listDuplicatasQuitar.add("116644NE2");
            listDuplicatasQuitar.add("116606NE2");
            listDuplicatasQuitar.add("000004-004");

            // rgbSysSelenium.login();
            // rgbSysSelenium.quitacaoBaixa("BMA FIDC", "MAXIBRASIL", listDuplicatasQuitar, conta);
            rgbSysSelenium.quitacaoBaixa( listDuplicatasQuitar,  processUrl);
            
        }
        catch (Exception e) 
        {
            e.printStackTrace();
            rgbSysSelenium.saveListCriticaOperacoesRealizadasToDatabase();
        }
        finally
        {
            rgbSysSelenium.driver.close();
        }

    }
}
