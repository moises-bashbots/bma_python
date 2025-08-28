package conta_grafica;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import cedente.Cedente;
import empresa.Empresa;

public class Saldo {
	private double valor=0;
	private Cedente cedente=new Cedente();
	private Empresa empresa= new Empresa();
	private Date dataSaldo=null;
	private static SimpleDateFormat sdf= new SimpleDateFormat("yyyy-MM-dd");
	
	public Saldo()
	{
		
	}
	
	public Saldo(Connection connMaria, Connection connMSS, String apelidoEmpresa, String apelidoCedente, Date dataSaldo, double valorSaldo)
	{
		this.empresa=new Empresa(connMaria, connMSS, apelidoEmpresa);
		this.cedente=new Cedente(connMaria, connMSS, this.empresa, apelidoCedente);
		this.dataSaldo=dataSaldo;
		this.valor=valorSaldo;
	}
	
	public Saldo(Empresa empresa, Cedente cedente, Date dataSaldo, double valorSaldo)
	{
		this.empresa=empresa;
		this.cedente=cedente;
		this.dataSaldo=dataSaldo;
		this.valor=valorSaldo;
	}

	public double getValor() {
		return valor;
	}
	
	public Saldo(Connection connMSS, Empresa empresa, Cedente cedente, Date dataSaldo)
	{
		this.empresa=empresa;
		this.cedente=cedente;
		this.dataSaldo=dataSaldo;
		String queryMSS="select sum(valor) as saldo"
									+ " from lanca l"
									+ " where"
									+ " l.cedente = '"+this.cedente.getApelido()+"'"
									+ " and l.empresa='"+this.empresa.getApelido()+"'"
									+ " and datal<='"+sdf.format(dataSaldo)+"'"
									;
		System.out.println(queryMSS);
		Statement stMSS=null;
		try {
			stMSS=connMSS.createStatement();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		ResultSet rsMSS=null;
		try {
			rsMSS=stMSS.executeQuery(queryMSS);
			while(rsMSS.next())
			{
				this.valor=rsMSS.getDouble("saldo");
			}
			
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
	public static Date dataUltimoSaldoZerado(Connection connMSS, Empresa empresa, Cedente cedente)
	{
		Date dataUltimoSaldoZerado=null;

		String queryMSS="select datal, valor "
									+ " from lanca l"
									+ " where"
									+ " l.cedente = '"+ cedente.getApelido()+"'"
									+ " and l.empresa='"+empresa.getApelido()+"'"
									+ " and datal<='"+sdf.format(Calendar.getInstance().getTime())+"'"
									+ " order by datal";
		System.out.println(queryMSS);
		Statement stMSS=null;
		try {
			stMSS=connMSS.createStatement();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		ResultSet rsMSS=null;
		double valor=0;
		double saldoAcumulado=0;
		double saldoFinal=0;
		Date datal=null;
		Date dataInicial=null;
		
		try {
			rsMSS=stMSS.executeQuery(queryMSS);
			int iLancamento=0;
			while(rsMSS.next())
			{
				
				valor=rsMSS.getDouble("valor");
				datal=rsMSS.getDate("datal");
				saldoAcumulado=saldoAcumulado+valor;
				
				if(Math.abs(saldoAcumulado)<=0.02)
				{
					dataUltimoSaldoZerado=rsMSS.getDate("datal");
					System.out.println("Saldo zerado em: " + sdf.format(dataUltimoSaldoZerado));
				}
				
				System.out.println(sdf.format(datal)+"\t"+valor+"\t"+saldoAcumulado);
				if(iLancamento==0)
				{
					dataInicial=datal;
				}
				iLancamento++;
			}
			
		} catch (SQLException e) {
			e.printStackTrace();
		}
		if(dataUltimoSaldoZerado==null)
		{
			dataUltimoSaldoZerado=dataInicial;
		}
		return dataUltimoSaldoZerado;
	}
	
	public void show()
	{
		System.out.println(sdf.format(this.dataSaldo)
											+" " +this.empresa.getApelido() 
											+ " - "+this.empresa.getCnpj() 
											+ " Cedente: " + this.cedente.getApelido()
											+ " - " + this.cedente.getParticipante().getCadastro() + ": " + this.valor 
											+" email: "+this.cedente.getEmail());
	}

	public void setValor(double valor) {
		this.valor = valor;
	}

	public Cedente getCedente() {
		return cedente;
	}

	public void setCedente(Cedente cedente) {
		this.cedente = cedente;
	}

	public Empresa getEmpresa() {
		return empresa;
	}

	public void setEmpresa(Empresa empresa) {
		this.empresa = empresa;
	}

	public Date getDataSaldo() {
		return dataSaldo;
	}

	public void setDataSaldo(Date dataSaldo) {
		this.dataSaldo = dataSaldo;
	}


}
