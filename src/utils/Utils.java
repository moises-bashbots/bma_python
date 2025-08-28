package utils;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;
import java.util.Random;
import java.util.concurrent.TimeUnit;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.FormulaEvaluator;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFFormulaEvaluator;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.openqa.selenium.WebElement;

import com.fasterxml.jackson.databind.ObjectMapper;

import email.Email;

public class Utils {
	private static SimpleDateFormat sdfrn=new SimpleDateFormat("ddMMyyyy");
	private static SimpleDateFormat sdfH=new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	private static SimpleDateFormat sdfp=new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
	private static SimpleDateFormat sdf=new SimpleDateFormat("yyyy-MM-dd");
	private static DecimalFormat df = new DecimalFormat("#0.00");
	public static void main(String[] args)
	{
		double test = 43.29;
		System.out.println(Double.toString(test));
		System.out.println(formatValor(test));
		String criacaoString="2022-11-22 15:59:01.000";
		try {
			System.out.println("Parsing: "+criacaoString + " "+sdfp.parse(criacaoString));
		} catch (ParseException e) {
						e.printStackTrace();
		}
//		{"txid":"8099bcfda2344a12bc2a805d9b6644b3","calendario":{"expiracao":"57600"},"devedor":{"cnpj":"05200644000147","nome":"ANGELI INDUSTRIA E COMERCIO DE CALCADOS LTDA"},"valor":{"original":"144.0"},"chave":"","solicitacaopagador":"Regularizacao de saldo negativo"}
		// "{\r\n    \"txid\": \"04353469000165000040640000000004742\",\r\n    \"calendario\": {\r\n        \"expiracao\": \"57600\"\r\n    },\r\n    \"devedor\": {\r\n        \"cnpj\": \"04353469000165\",\r\n        \"nome\": \"B. TRANSPORTES LTDA\"\r\n    },\r\n    \"valor\": {\r\n        \"original\": \"47.42\"\r\n    },\r\n    \"chave\": \"4581f4b7-957f-4aba-9ae8-c1c174e9452c\",\r\n    \"solicitacaopagador\": \"Regularizacao de saldo negativo\"\r\n}"
		String testString="{\"txid\":\"8099bcfda2344a12bc2a805d9b6644b3\",\"calendario\":{\"expiracao\":\"57600\"},\"devedor\":{\"cnpj\":\"05200644000147\",\"nome\":\"ANGELI INDUSTRIA E COMERCIO DE CALCADOS LTDA\"},\"valor\":{\"original\":\"144.0\"},\"chave\":\"\",\"solicitacaopagador\":\"Regularizacao de saldo negativo\"}";
		String procString="";
		try {
			procString = beautify(testString);
		} catch (IOException e1) {
			e1.printStackTrace();
		}
		System.out.println(procString);
		System.out.println(testString);
		procString=procString
								.replaceAll("\n", "\r\n")
//								.replaceAll("\"", "\\"")
								;
		System.out.println(new DecimalFormat("#0.00#").format(.10));
		Date limit = null;
		try {
			limit = sdfp.parse("2022-12-21 21:00:00.000");
		} catch (ParseException e) {
			e.printStackTrace();
		}
		System.out.println("Seconds left: "+secondsLeftToTime(limit));
		
		Date dataInicial = null;
		try {
			dataInicial = sdf.parse("2023-05-10");
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		Date dataFinal = null;
		try {
			dataFinal = sdf.parse("2022-10-10");
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		try {
			Date dataTeste=sdfrn.parse("13092023");
			System.out.println(dataTeste);
			System.out.println(sdfrn.format(dataTeste));
		} catch (ParseException e) {
			e.printStackTrace();
		}
		

		
		System.out.println(df.format(.1123231));
		System.out.println(df.format(Math.round(.125*100)/100.0));
		System.out.println(df.format(.12345));

		System.out.println(getDifferenceDays(dataInicial, dataFinal));
		
		String onlyNumberCNPJ="51410013000170";
		System.out.println("OnyNumbersCNPJ: "+onlyNumberCNPJ);
		System.out.println("FormattedCNPJ: "+formatarCNPJ(onlyNumberCNPJ));
		double valor=211976.78999999;
		String stringValor=new DecimalFormat("#0.00#").format((Math.abs(valor)));
	    System.out.println(Double.parseDouble(stringValor));
	    System.out.println(sdfH.format(Calendar.getInstance().getTime()));
	}
	
	
	public static void sendHumanKeys(WebElement element, String text) {
	    Random r = new Random();
	    for(int i = 0; i < text.length(); i++) {
	        try {
	            Thread.sleep((int)(Math.abs(r.nextGaussian() * 15 + 20)));
	        } catch(InterruptedException e) {}
	        String s = new StringBuilder().append(text.charAt(i)).toString();
	        System.out.println("Writing: " +s);
	        element.sendKeys(s);
	    }
	}
	
	public static void sendHumanKeysSilent(WebElement element, String text) {
	    Random r = new Random();
        System.out.println();
	    for(int i = 0; i < text.length(); i++) {
	        try {
	            Thread.sleep((int)(Math.abs(r.nextGaussian() * 5 + 2)));
	        } catch(InterruptedException e) {}
	        String s = new StringBuilder().append(text.charAt(i)).toString();

	        System.out.print(s);
	        
	        element.sendKeys(s);
	    }
	    System.out.println();
	}
	
	public static String formatarCNPJ(String onlyNumbers)
	{
		String formatado="";
		formatado=onlyNumbers.substring(0,2)+"."+onlyNumbers.substring(2,5)+"."+onlyNumbers.substring(5,8)+"/"+onlyNumbers.substring(8,12)+"-"+onlyNumbers.substring(12,14);
		return formatado;
	}
	

	public static boolean copyFileAndDelete(File origin, File destiny)
	{
		boolean successful=false;
		InputStream inStream = null;
		OutputStream outStream = null;
		//create output directory if not exists    				
    	System.out.println("---Destiny file: "+destiny.getAbsolutePath());
	    	try{
	    		if(destiny.exists())
	    		{
	    			System.out.println("File exist already!");
	    		}
	    		else
	    		{
	    			System.out.println("Destiny does not exist!");
		    	    inStream = new FileInputStream(origin);
		    	    outStream = new FileOutputStream(destiny);
		        	
		    	    byte[] buffer = new byte[1024];
		    		
		    	    int length;
		    	    //copy the file content in bytes 
		    	    while ((length = inStream.read(buffer)) > 0){
		    	  
		    	    	outStream.write(buffer, 0, length);
		    	 
		    	    }
		    	 
		    	    inStream.close();
		    	    outStream.close();
	    		}
	    		   		
	    	    
	    	    //delete the original file
	    	    
	    	    origin.delete();
	    	    
	    	    System.out.println("File is copied successful!");
	    	    successful=false;
	    	    
	    	}catch(IOException e){
	    	    e.printStackTrace();
	    	}
		return successful;
	}

	
	public static boolean copyFileAndDelete(File origin, String pathDestiny, String fileNameDestiny)
	{
		boolean successful=false;
		InputStream inStream = null;
		OutputStream outStream = null;
		//create output directory if not exists
    	File folder = new File(pathDestiny);
    	if(!folder.exists()){
    		folder.mkdirs();
    	}
    	File destiny = new File(pathDestiny+"\\\\"+fileNameDestiny);			
    	System.out.println("---Destiny file: "+destiny.getAbsolutePath());
	    	try{
	    		if(destiny.exists())
	    		{
	    			System.out.println("File exist already!");
	    			destiny.delete();
	    			destiny = new File(pathDestiny+"\\\\"+fileNameDestiny);
	    		}
	    		else
	    		{
	    			System.out.println("Destiny does not exist!");
		    	    inStream = new FileInputStream(origin);
		    	    outStream = new FileOutputStream(destiny);
		        	
		    	    byte[] buffer = new byte[1024];
		    		
		    	    int length;
		    	    //copy the file content in bytes 
		    	    while ((length = inStream.read(buffer)) > 0){
		    	  
		    	    	outStream.write(buffer, 0, length);
		    	 
		    	    }
		    	 
		    	    inStream.close();
		    	    outStream.close();
	    		}
	    		   		
	    	    
	    	    //delete the original file
	    	    
	    	    origin.delete();
	    	    
	    	    System.out.println("File is copied successful!");
	    	    successful=false;
	    	    
	    	}catch(IOException e){
	    	    e.printStackTrace();
	    	}
		return successful;
	}
	
	public static int secondsLeftToTime(Date limitTime)
	{
		int secondsLeftForTodayUntilLimit=0;
		int hour=Calendar.getInstance().get(Calendar.HOUR_OF_DAY);
		int minute=Calendar.getInstance().get(Calendar.MINUTE);
		int second=Calendar.getInstance().get(Calendar.SECOND);
		
		Calendar calLimit = Calendar.getInstance();
		calLimit.setTime(limitTime);
		int hourLimit=calLimit.get(Calendar.HOUR_OF_DAY);
		int minuteLimit=calLimit.get(Calendar.MINUTE);
		int secondLimit=calLimit.get(Calendar.SECOND);
		
		int secondsLimit=secondLimit+minuteLimit*60+hourLimit*60*60;
		int secondsNow=second+minute*60+hour*60*60;
		if(secondsLimit>secondsNow)
		{
			secondsLeftForTodayUntilLimit=secondsLimit-secondsNow;
		}
		if(secondsLeftForTodayUntilLimit==0)
		{
			secondsLeftForTodayUntilLimit=60*60;
		}
		return secondsLeftForTodayUntilLimit;
	}
	
	public static double extractValueFromBrazilianNumber(String rawNumber)
	{
		double value=0.0;
		if(rawNumber.trim().length()>0)
		{
			String cleanNumber=rawNumber.replaceAll("\\.", "");
			cleanNumber=cleanNumber.replace(",",".");
			value = Double.parseDouble(cleanNumber);			
		}
		return value;
	}
    public static String formatValor(Double valor)
    {
        return String.valueOf(valor).replace(".", ",");
    }
	public static String beautify(String json) throws IOException 
	{
	    ObjectMapper mapper = new ObjectMapper();
	    Object obj = mapper.readValue(json, Object.class);
	    return mapper.writerWithDefaultPrettyPrinter().writeValueAsString(obj);
	}
	
	public static String formatToSend(String rawString)
	{
		String procString="";
		try {
			procString = beautify(rawString);
		} catch (IOException e1) {
			e1.printStackTrace();
		}
		System.out.println(procString);
		procString=procString
								.replaceAll("\n", "\\\\r\\\\n")
								.replaceAll("\\\"", "\\\\\"")
								;
		System.out.println(procString);
		return procString;
	}
	
	public static String  uniqueStringPIX(int size, Date dataSaldo, String codigoMovimentacao, String cadastroCedente, String agenciaRecebedor, String contaRecebedor, String cadastroRecebedor, String valor)
	{
		valor=valor.replaceAll("\\.","");
		SimpleDateFormat sdf=new SimpleDateFormat("yyyyMMdd"); 
		
		String dataSaldoString=sdf.format(dataSaldo);
		if(cadastroCedente.length()==11)
		{
			cadastroCedente="CPF"+cadastroCedente;
		}
		String prefix = codigoMovimentacao+dataSaldoString+cadastroCedente;
	    String valueString = fillWithChar(13, valor, '0');
	    String uniqueString="";

	    agenciaRecebedor=fillWithChar(4, agenciaRecebedor, '0');
	    contaRecebedor=fillWithChar(13, contaRecebedor, '0');
	    uniqueString = prefix+contaRecebedor+valueString;

	    return uniqueString;
	}
	
	public static String signature(String nameWriter, String emailWriter)
	{
		String signature="<br>Atenciosamente, <br><br> \n"
			+ "	_________________________<br>"
			+ " " + nameWriter + " <a href=\"mailto:" + emailWriter +"\">"+ emailWriter +"</a>" +  " <br>\n";

		return signature;
	}
	
	public static String signature(String nameWriter, String emailWriter, String phoneWriter)
	{
		String signature="<br>Atenciosamente, <br><br> \n"
			+ "	_______________________________________<br>"
			+ " " + nameWriter + "<br><a href=\"mailto:" + emailWriter +"\">"+ emailWriter +"</a>" +  " <br>\n"
			+ " Tel: " + phoneWriter + " <br>\n";
		return signature;
	}
	
	public static String signature(String nameWriter)
	{
		String signature="<br>Atenciosamente, <br><br> \n"
			+ "	_______________________________________<br>"
			+ " " + nameWriter + "<br>\n";
		return signature;
	}

	
	public static String signature(String nameWriter, String emailWriter, String company, String contact)
	{
		String signature="<br>Atenciosamente, <br><br> \n"
			+ "	____________________________________<br>"
			+ " " + nameWriter + "<br> <a href=\"mailto:" + emailWriter +"\">"+ emailWriter +"</a>" +  " <br>\n"
			+ " " + company + "<br>\n"
			+ " " + contact + "<br>\n";
		return signature;
	}
	
	public static Email setupEmail(Connection conn, String chave)
	{
		Email email = new Email();
		String query="select * from configuracoes.email_conf where chave='"+chave+"';";
		System.out.println(query);
		Statement st = null;
		try {
			st = conn.createStatement();
		} catch (SQLException e) {
			e.printStackTrace();
		}

		try {
			ResultSet rs = st.executeQuery(query);
			while(rs.next())
			{
				email.setSmtpServer(rs.getString("smtp"));
				email.setSmtpPort(rs.getInt("porta_smtp"));
				email.setImapServer(rs.getString("imap"));
				email.setImapPort(rs.getInt("porta_imap"));
				email.setUser(rs.getString("nome"));
				email.setAddress(rs.getString("conta"));
				email.setPassword(rs.getString("senha"));
				email.setSecurity(rs.getString("seguranca"));
			} 
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return email;
	}

    public static long getDifferenceDays(Date d1, Date d2) 
    {
        long diff = d2.getTime() - d1.getTime();
        return TimeUnit.DAYS.convert(diff, TimeUnit.MILLISECONDS);
    }
    
    
	public static void waitv(double d) {
		System.out.print("Waiting ");
		for(int i=1;i<=d;i++)
		{
			System.out.print(i + "/" + d + " ");
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}			
		}		
		System.out.println("");
	}

	public static void waitv(String reason, double d) {
		System.out.print("Waiting for "+reason+" ");
		for(int i=1;i<=d;i++)
		{
			System.out.print(i + "/" + d + " ");
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}			
		}		
		System.out.println("");
	}

	public static String fillWithChar(int size, String original, char fillChar)
	{
		String result="";
		if(original.length()>size)
		{
			result = original.substring(0,size);
		}
		else
		{
			result=original;
			int zerosLeft=size - original.length();
			for(int i=0;i<zerosLeft;i++)
			{
				result=fillChar+result;
			}
			
		}
		return result;
	}
	
	public static ArrayList<String> readLinesInFile(String pathFile)
	{
		ArrayList<String> lines = new ArrayList<String>();
		File f = new File(pathFile);
		if(f.exists())
		{
			
		}
		else
		{
			 if(f.isDirectory())
			 {
				System.out.println(f.getAbsolutePath() + " is a folder!");
			 }
			 else
			 {
				 System.out.println(f.getAbsolutePath() + " does not exist!");
			 }
			return lines;		
		}
		
		
		FileInputStream fstream=null;
		try {
			fstream = new FileInputStream(pathFile);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		BufferedReader br = new BufferedReader(new InputStreamReader(fstream,StandardCharsets.ISO_8859_1));
		String strLine;

		//Read File Line By Line
		try {
			while ((strLine = br.readLine()) != null)   
			{
			  lines.add(strLine);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		try {
			br.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		try {
			fstream.close();
		} catch (IOException e1) {
			e1.printStackTrace();
		}
		
		System.out.println(lines.size() + " lines readed!");		
		return lines;
	}
	
	public static ArrayList<String> extractLinesFromXLSX(String pathFile, String nameSheet)
	{
		ArrayList<String> lines = new ArrayList<String>();
		FileInputStream fis=null;
//		OutputStream out=null;
		ByteArrayOutputStream out   = new ByteArrayOutputStream();
		try {
			fis = new FileInputStream(pathFile);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		XSSFWorkbook wb=null;
//		HSSFWorkbook wb=null;
		try {
//			wb = new HSSFWorkbook(fis);
			wb = new XSSFWorkbook(fis);
			writeWorkbookAsCSVToOutputStream(wb,out, nameSheet);
			String content=out.toString();
			String arraylines[] = content.split("\\r?\\n");
			for(int i=0;i<arraylines.length;i++)
			{
				lines.add(arraylines[i]);
			}
//			System.out.println(content);
		} catch (IOException e) {
			e.printStackTrace();
		}


		return lines;
	}
	
	   private static void writeWorkbookAsCSVToOutputStream(XSSFWorkbook workbook, OutputStream out, String nameSheet) 
	   {        
	       CSVPrinter csvPrinter = null;       
	       try {       
	           csvPrinter = new CSVPrinter(new OutputStreamWriter(out), CSVFormat.EXCEL.withDelimiter(';'));      
	           
	           FormulaEvaluator formulaEvaluator = new XSSFFormulaEvaluator((XSSFWorkbook)workbook);
	           DataFormatter dataFormatter=new DataFormatter();
	           if (workbook != null) 
	           {
//	               XSSFSheet sheet = workbook.getSheetAt(numberSheet); // Sheet #0 in this example
	               XSSFSheet sheet = workbook.getSheet(nameSheet);
	               Iterator<Row> rowIterator = sheet.rowIterator();
	               while (rowIterator.hasNext()) {               
	                   Row row = rowIterator.next();
	                   Iterator<Cell> cellIterator = row.cellIterator();
	                   while (cellIterator.hasNext()) 
	                   {
	                       Cell cell = cellIterator.next();
	                       formulaEvaluator.evaluate(cell);
	                       String stringValue=dataFormatter.formatCellValue(cell,formulaEvaluator);
//	                       switch (cell.getCellTypeEnum()) 
//	                       {
//								case STRING:
//			                    	   stringValue=cell.getStringCellValue();
//									break;
//								case NUMERIC:
//			                    	   stringValue=Double.toString(cell.getNumericCellValue());
//									break;		
//								case BOOLEAN:
//										if(cell.getBooleanCellValue())
//										{
//											
//										}
//			                    	   stringValue=Double.toString(cell.getNumericCellValue());
//									break;		
//								default:
//									break;
//							}
	                       
	                       csvPrinter.print(stringValue);
	                   }                   
	                   csvPrinter.println(); // Newline after each row
	               }               
	           }

	       }
	       catch (Exception e) {
	           System.out.println("Failed to write CSV file to output stream: " + e.getMessage());
	       }
	       finally {
	           try {
	               if (csvPrinter != null) {
	                   csvPrinter.flush(); // Flush and close CSVPrinter
	                   csvPrinter.close();
	               }
	           }
	           catch (IOException ioe) {
	               System.out.println("Error when closing CSV Printer: " + ioe.getMessage());
	           }           
	       }
	   }   
}
