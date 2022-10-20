

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

	public boolean error=false;
	public String error_message="";

	public String invoice_date="";
	public String due_date="";

	public static double base_rate = 20.0;

	public double rate=base_rate;

	public int invoice_number=0;

	public LinkedList<String[]> billing_rows = new LinkedList<String[]>();
	public HashMap<String, String> client_details = new HashMap<String, String>();

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


}//class InvoiceData.