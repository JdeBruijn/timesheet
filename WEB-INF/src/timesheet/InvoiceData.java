

package timesheet;

import java.util.LinkedList;
import java.util.logging.Logger;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;

import java.text.SimpleDateFormat;
import java.text.DecimalFormat;


public class InvoiceData
{
	private static final String class_name="InvoiceData";
	private static final Logger log = Logger.getLogger(class_name);


	public static double base_rate = 20.0;
	public static int default_rows_number = 10;


	public boolean error=false;
	public String error_message="";

	public String invoice_date="";
	public String due_date="";

	public double rate=base_rate;

	public int invoice_number=0;

	private LinkedList<String[]> billing_rows = new LinkedList<String[]>();
	private HashMap<String, String> client_details = new HashMap<String, String>();

	private double total=0;

	public InvoiceData()
	{
		Calendar calendar = Calendar.getInstance();
		Date date = calendar.getTime();
		SimpleDateFormat format = new SimpleDateFormat("dd/MM/yyyy");
		this.invoice_date = format.format(date);

		long current_epoch = date.getTime();
		long due_epoch = date.getTime()+(1000*3600*24*30);
		date.setTime(due_epoch);
		this.due_date = format.format(date);
	}//constructor().

	public void addBillingRow(long start_epoch, double duration, String description)
	{
		if(duration<=0)
		{return;}

		double hourly_rate=rate;

		double amount = round((duration*hourly_rate),2);
		this.total+=amount;

		duration = round(duration, 2);

		String[] details = {description, ensure2DecimalPlaces(duration), String.valueOf(hourly_rate), ensure2DecimalPlaces(amount)};

		this.billing_rows.add(details);
	}//addBillingRow().

	public void addClientDetail(String key, String value)
	{
		if(this.client_details==null)
		{this.client_details = new HashMap<String, String>();}

		this.client_details.put(key, value);
	}//addClientDetail().

	public void setClientDetails(HashMap<String, String> details)
	{
		this.client_details=details;
	}//setClientDetails().

	public String getInvoiceNumber()
	{
		return ensure6Places(this.invoice_number);
	}//getInvoiceNumber().

	public String getInvoiceName()
	{
		return "invoice"+this.invoice_number+"_"+(this.invoice_date.replaceAll("/","_"))+".pdf";
	}//getInvoiceName().

	public String getTotal()
	{
		return ensure2DecimalPlaces(round(this.total, 2));
	}//getTotal().


	public static double round(double value, int places)
	{
		if(value==0)
		{return value;}
		double multiplier = Math.pow(10,places);
		if(multiplier==0)
		{multiplier=1;}
		value = Math.round(value*multiplier)/multiplier;
		return value;
	}//round().

	public static String ensure2DecimalPlaces(double d)
	{
		DecimalFormat dc = new DecimalFormat("0.00");
		return dc.format(d);
	}//ensure2DecimalPlaces().

	public static String ensure6Places(int i)
	{
		DecimalFormat dc = new DecimalFormat("000000");
		return dc.format(i);
	}//ensure6Places().

	public static String ensure3Places(int i)
	{
		DecimalFormat dc = new DecimalFormat("000");
		return dc.format(i);
	}//ensure6Places().


	public LinkedList<String[]> getBillingRows()
	{
		if(this.billing_rows!=null && this.billing_rows.size()>0)
		{return this.billing_rows;}
		
		this.billing_rows = new LinkedList<String[]>();
		for(int br=0; br<default_rows_number; br++)
		{
			this.billing_rows.add(new String[] {"", "", "", ""});
		}//for(br).

		return this.billing_rows;
	}//getBillingRows().

	public HashMap<String, String> getClientDetails()
	{
		if(this.client_details!=null && this.client_details.size()>0)
		{return this.client_details;}

		this.client_details = new HashMap<String, String>();
		client_details.put("client_id", "");
		this.client_details.put("client_name", "");
		this.client_details.put("client_address", "");
		this.client_details.put("client_city", "");
		this.client_details.put("client_country", "");
		this.client_details.put("client_phone_number", "");
		this.client_details.put("client_email", "");

		return this.client_details;
	}//getClientDetails().

}//class InvoiceData.