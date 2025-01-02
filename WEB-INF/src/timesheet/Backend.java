/*
Jon de Bruijn
2021-07-19
backend for work timesheet application.
database stored on raspberry pi that's also used for recording work time.
This application allows addition of description to time segments and deletion of time segments.
*/



package timesheet;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.ResultSetMetaData;

import java.io.IOException;
import java.io.InputStreamReader;

import java.util.logging.Logger;
import java.util.LinkedList;
import java.util.Set;
import java.util.HashMap;

import java.lang.Math;

import org.json.simple.JSONObject;
import org.json.simple.JSONArray;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import javax.servlet.annotation.MultipartConfig;
import javax.servlet.http.HttpSession;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.Part;
import javax.servlet.ServletException;

@MultipartConfig
public class Backend extends HttpServlet
{
	private static final String class_name="Backend";
	private static final Logger log = Logger.getLogger(class_name);

	private static final String[][] headers_list = {{"Delete","work_log_id","1", "0"},{"Start Time","start_time","3" ,""},{"End Time", "end_time","3", "Total:"},{"Elapsed Time(h)","elapsed_time","3", null},{"Description","work_log_description","8", ""},{"Client","client_name","3", ""},{"Invoice No","work_log_invoice_number","3", ""}};
							//headers_list format: {{"Display Name1", "index_name1", "column_width1", "default_column_value1"}, {"Display_name2",...}, ...}


	public void doGet(HttpServletRequest req, HttpServletResponse resp)
	{
		String oper = req.getHeader("oper");
		if(oper.equals("get_logs"))
		{getLogs(req, resp);}
		else if(oper.equals("get_clients"))
		{getClients(req, resp);}
		else
		{
			returnData(false, null, resp, "Invalid operation.");
			return;
		}//else.

	}//doGet().

	public void doPost(HttpServletRequest req, HttpServletResponse resp)
	{
		String deletions_str = req.getHeader("deleted");
		log.info(class_name+" deletions_str = "+deletions_str);//debug**

		JSONObject changes = null;
		try
		{
			Part changes_part = req.getPart("changes");
			InputStreamReader part_reader = new InputStreamReader(changes_part.getInputStream());
			JSONParser parser = new JSONParser();
			changes = (JSONObject) parser.parse(part_reader);
		}//try
		catch(IOException | ParseException | ServletException se)
		{
			log.severe(class_name+" Exception while trying to get changes part:\n"+se); 
			returnData(false, null, resp, "Failed to extract changes part");
			return;
		}//catch().

		log.info(class_name+" doPost(): changes = "+changes.toString());//debug**

		if( (changes==null || changes.size()<=0)  && (deletions_str.trim().isEmpty()))
		{
			returnData(false, null, resp, "No changes found.");
			return;
		}//if.

		//Changes.
		StringBuilder updates = new StringBuilder();
		LinkedList<String> changes_values = new LinkedList();
		if(changes!=null && changes.size()>0)
		{
			Set<String> work_log_ids = changes.keySet();
			for(String log_id: work_log_ids)
			{
				JSONObject row = (JSONObject) changes.get(log_id);
				log.info(class_name+" doPost(): changes = "+changes.toString());//debug**

				StringBuilder row_updates = new StringBuilder();
				Set<String> keys = row.keySet();
				for(String name: keys)
				{
					String value = (String) row.get(name);
					log.info(class_name+" doPost(): name="+name+" value="+value);//debug**
					if(name.equals("client_name"))
					{name="work_log_client_id";}
					if(row_updates.length()>0)
					{row_updates.append(", ");}
					row_updates.append(name+"=?");

					changes_values.add(value);
				}//for().
				row_updates.append(" WHERE work_log_id=?");
				changes_values.add(log_id);

				if(updates.length()>0)
				{updates.append("; ");}

				updates.append("UPDATE work_log SET "+row_updates);
			}//for(row).
			log.info(class_name+" doPost(): updates = "+updates.toString());//debug**
		}//if.
		//Deletions.
		StringBuilder deletions = new StringBuilder();
		LinkedList<Integer> delete_values = new LinkedList();
		if(!deletions_str.trim().isEmpty())
		{
			String[] deleted_rows = deletions_str.split("~!");
			for(String del_id: deleted_rows)
			{
				delete_values.add(Integer.parseInt(del_id));

				if(deletions.length()>0)
				{deletions.append("; ");}

				deletions.append("DELETE FROM work_log WHERE work_log_id=?");
			}//for(del_id).
		}//if.

		Connection conn = DatabaseHelper.getConnection();
		try
		{
		//Changes.	
			if(changes_values.size()>0)
			{
				PreparedStatement stmt = conn.prepareStatement(updates.toString());
				for(int cv=0; cv<changes_values.size(); cv++)
				{
					stmt.setString(cv+1, changes_values.get(cv));
				}//for(v).
				log.info(class_name+" updates = "+stmt);//debug**

				stmt.executeUpdate();
			}//if.

		//Deletions.
			if(!deletions_str.trim().isEmpty())
			{
				PreparedStatement stmt = conn.prepareStatement(deletions.toString());
				for(int dv=0; dv<delete_values.size(); dv++)
				{
					stmt.setInt(dv+1, delete_values.get(dv));
				}//for(dv).
				log.info(class_name+" deletions = "+stmt);//debug**

				stmt.executeUpdate();
			}//if.

			returnData(true, null, resp, "");
		}//try
		catch(SQLException se)
		{
			log.severe(class_name+" SQL Exception while trying to save changes:\n"+se);
			returnData(false, null, resp, "SQL Exception occurred.");
		}//catch().
		finally
		{
			try
			{conn.close();}
			catch(SQLException se)
			{log.severe(class_name+" SQL Exception while trying to close db connection in doPost():\n"+se);}
		}//finally.

	}//doPost().

	private void getLogs(HttpServletRequest req, HttpServletResponse resp)
	{
		String date_range = req.getHeader("date_range");
		log.info(class_name+" date_range = "+date_range);//debug**
		long[] date_limits = getDateRange(date_range);

		int client_id = getInt(req.getHeader("client_id"));//-1 if no client_id specified.


		String work_log_query = "SELECT work_log_id, FROM_UNIXTIME(work_log_start_epoch) AS start_time, FROM_UNIXTIME(work_log_end_epoch) AS end_time,"
							+ " ROUND((work_log_end_epoch-work_log_start_epoch)/3600, 2) AS elapsed_time, DATE(FROM_UNIXTIME(work_log_end_epoch))AS date,"
							+ " work_log_description, work_log_invoice_number, client_id, client_name"
							+ " FROM work_log"
							+ " LEFT JOIN client ON client_id=work_log_client_id"
							+ " WHERE 1";
		if(date_limits!=null)
		{
			work_log_query+=" AND (work_log_start_epoch>="+String.valueOf(date_limits[0])+" AND work_log_end_epoch<="+String.valueOf(date_limits[1])+")";
		}//if.
		if(client_id>0)
		{
			work_log_query+=" AND work_log_client_id="+client_id;
		}//if.
		work_log_query+=" ORDER BY work_log_start_epoch ASC";

		String group_by_column=req.getHeader("group_by");
		if(group_by_column==null || group_by_column.trim().isEmpty())
		{group_by_column="date";}


		Connection conn = DatabaseHelper.getConnection();
		try
		{
			if(conn==null)
			{
				log.severe(class_name+" Failed to get database connection.");
				returnData(false, null, resp, "");
				return;
			}//if.

			double total_size = 0;
			for(String[]header: headers_list)
			{total_size+=Integer.parseInt(header[2]);}
			total_size=total_size/100;
			//log.info(class_name+" total_size="+total_size);//debug**

			JSONArray display_headers = new JSONArray();
			JSONArray column_sizes = new JSONArray();
			JSONArray data_headers = new JSONArray();
			for(String[]header: headers_list)
			{
				display_headers.add(header[0]);
				data_headers.add(header[1]);
				double column_size = Integer.parseInt(header[2])/total_size;
				column_sizes.add(column_size);
			}//for(header).

			log.info(class_name+" work_log_query = "+work_log_query);//debug**

			ResultSet res = conn.prepareStatement(work_log_query).executeQuery();
			ResultSetMetaData meta = res.getMetaData();
			int column_count = meta.getColumnCount();
			JSONObject results = new JSONObject();
			JSONArray rows_data = new JSONArray();

			String prev_group_id=null;
			double sum_hours = 0.0;
			double sum_sum_hours=0.0;

			while(res.next())
			{
			//Daily totals.
				String cur_group_id = res.getString(group_by_column);
				if(prev_group_id!=null && !cur_group_id.equals(prev_group_id))
				{
					JSONObject sum_row = new JSONObject();
					for(String[] column: headers_list)
					{
						String index_name = column[1];
						String default_value = column[3];
						if(index_name.equals("elapsed_time"))
						{sum_row.put(index_name,round(sum_hours,2));}
						else
						{sum_row.put(index_name,default_value);}
					}//for(column).
					rows_data.add(sum_row);
					sum_hours=0;
				}//if.
				double hours = res.getDouble("elapsed_time");
				sum_hours+=hours;
				sum_sum_hours+=hours;
				prev_group_id=cur_group_id;

			//Individual logs.	
				JSONObject row = new JSONObject();
				//row.put("work_log_id",res.getString("work_log_id"));
				for(String[] header: headers_list)
				{
					String index_name = header[1];
					if(!index_name.isEmpty())
					{row.put(index_name,res.getString(index_name));}
				}//for(header).
				rows_data.add(row);
				//log.info(class_name+" row = "+row.toString());//debug**
			}//while.

		//Last daily total.	
			if(sum_hours>0)
			{
				JSONObject sum_row = new JSONObject();
				for(String[] column: headers_list)
				{
					String index_name = column[1];
					String default_value = column[3];
					if(index_name.equals("elapsed_time"))
					{sum_row.put(index_name,round(sum_hours,2));}
					else
					{sum_row.put(index_name,default_value);}
				}//for(column).
				rows_data.add(sum_row);
			}//if.

		//Total total.
			if(sum_sum_hours>0)
			{
				JSONObject sum_sum_row = new JSONObject();
				for(String[] column: headers_list)
				{
					String index_name = column[1];
					String default_value = column[3];
					if(index_name.equals("elapsed_time"))
					{sum_sum_row.put(index_name,round(sum_sum_hours,2));}
					else
					{sum_sum_row.put(index_name, default_value);}
				}//for(column).
				rows_data.add(sum_sum_row);
			}//if.

			results.put("headers", display_headers);
			results.put("data_headers", data_headers);
			results.put("column_sizes",column_sizes);
			results.put("rows_data", rows_data);
			returnData(true, results, resp, "");
		}//try.
		catch(SQLException se)
		{
			log.severe(class_name+" SQL Exception while trying to retrieve logs:\n"+se);
			returnData(false, null, resp, "");
		}//catch().
		finally
		{
			try
			{conn.close();}
			catch(SQLException se)
			{log.severe(class_name+" SQL Exception while trying to close db connection:\n"+se);}
		}//finally.
	}//getLogs().

	private void getClients(HttpServletRequest req, HttpServletResponse resp)
	{
		JSONObject data = new JSONObject();
		JSONObject clients = getClients();
		if(clients==null)
		{
			returnData(false, null, resp, "Failed to retrieve clients");
			return;
		}//if.
		
		data.put("clients",clients);
		returnData(true, data, resp, "");
	}//getClients().

	public static JSONObject getClients()
	{
		String get_clients = "SELECT client_id, client_name"
						+ " FROM client";

		try(Connection conn = DatabaseHelper.getConnection())
		{
			ResultSet res = conn.prepareStatement(get_clients).executeQuery();
			JSONObject clients = new JSONObject();
			while(res.next())
			{
				clients.put(res.getString("client_name"), res.getString("client_id"));
			}//while.
			return clients;
		}//try
		catch(SQLException se)
		{
			log.severe(class_name+" SQL Exception while trying to get clients:\n"+se);
			return null;
		}//catch().
	}//getClients().


	private long[] getDateRange(String range)
	{
		if(range==null || range.isEmpty())
		{return null;}

		String[] split_range = range.split("-");
		long start_epoch = Long.parseLong(split_range[0])/1000;
		long end_epoch = Long.parseLong(split_range[1])/1000;

		log.info(class_name+" start_epoch = "+start_epoch+"  end_epoch = "+end_epoch);//debug**

		return new long[]{start_epoch, end_epoch};
	}//getDateRange().

	public static double round(double value, int places)
	{
		return InvoiceData.round(value, places);
	}//round().

	private int getInt(String value)
	{
		int answer = -1;
		try
		{answer=Integer.parseInt(value);}
		catch(NullPointerException | NumberFormatException nfe)
		{log.info(class_name+" Exception trying to get int from '"+value+"':\n"+nfe);}
		return answer;
	}//getInt().


	private void returnData(boolean success, JSONObject json_data, HttpServletResponse resp, String message)
	{
		if(json_data==null)
		{json_data = new JSONObject();}

		if(message==null)
		{message="";}

		json_data.put("success", success);
		json_data.put("message",message);
		
		try
		{resp.getWriter().println(json_data.toString());}
		catch(IOException ioe)
		{log.severe(class_name+" IO Exception while trying to return data:\n"+ioe);}
	}//returnData().

	public static InvoiceData generateInvoice(String ids_str, String group_by, String invoice_id_str, String rate_str, InvoiceData invoice_data)
	{
		boolean new_invoice=false;
		try
		{invoice_data.invoice_number = Integer.parseInt(invoice_id_str);}
		catch(NullPointerException | NumberFormatException nfe)
		{
			log.info(class_name+" Exception trying to get invoice_number from '"+invoice_id_str+"':\n"+nfe);
			log.info(class_name+" Creating new Invoice...");
			new_invoice=true;
		}//catch().

		try
		{invoice_data.rate = Double.parseDouble(rate_str);}
		catch(NullPointerException | NumberFormatException nfe)
		{
			log.info(class_name+" Exception trying to get rate from '"+rate_str+"':\n"+nfe);
			log.info(class_name+" Using base_rate of '"+invoice_data.base_rate+"':\n"+nfe);
		}//catch().

		if(group_by==null || group_by.trim().isEmpty())
		{group_by="work_log_description";}

		String placeholders=null;
		String[] ids = null;
		if(new_invoice)
		{
			if(ids_str==null || ids_str.trim().isEmpty())
			{
				invoice_data.error=true;
				invoice_data.error_message="No logs specified.";
				return invoice_data;
			}//if.
			placeholders = ids_str.replaceAll("[^,]+","?");
			ids = ids_str.split(",");
		}//if.

		String get_last_invoice_number = "SELECT global_last_invoice_number FROM global";

		String get_invoice_data = "SELECT work_log_id, work_log_start_epoch, SUM(ROUND((work_log_end_epoch-work_log_start_epoch)/3600, 2)) AS elapsed_time,"
						+ " work_log_description, MIN(work_log_id) AS min_id, client.*, country_name AS client_country"
						+ " FROM work_log"
						+ " LEFT JOIN client ON client_id=work_log_client_id"
						+ " LEFT JOIN country ON country_id=client_country_id"
						+ " WHERE work_log_invoice_number="+invoice_data.invoice_number;
		if(new_invoice)
		{get_invoice_data+=" AND work_log_id IN ("+placeholders+")";}
		get_invoice_data+=" GROUP BY "+group_by
						+ " ORDER BY min_id";

		String update_last_invoice_number = "UPDATE global SET global_last_invoice_number=?";

		String update_work_logs = "UPDATE work_log SET work_log_invoice_number=?"
							+ " WHERE work_log_id IN ("+placeholders+")"
							+ " AND work_log_invoice_number=0";
		
		Connection conn = DatabaseHelper.getConnection();
		try
		{
		//Get Invoice Number.
			if(new_invoice)
			{
				PreparedStatement stmt = conn.prepareStatement(get_last_invoice_number);
				ResultSet res = stmt.executeQuery();
				if(res.next())
				{invoice_data.invoice_number=res.getInt("global_last_invoice_number")+1;}
			}//if.

		//Get invoice data.
			PreparedStatement stmt = conn.prepareStatement(get_invoice_data);
			if(new_invoice)
			{
				for(int i=0; i<ids.length; i++)
				{stmt.setInt(i+1, Integer.parseInt(ids[i]));}
			}//if.
			log.info(class_name+" get_invoice_data= "+get_invoice_data);//debug**
			log.info(class_name+" get_invoice_data= "+stmt);//debug**

			boolean results_found=false;
			ResultSet res = stmt.executeQuery();
			int prev_client_id=-1;
			while(res.next())
			{
				int cur_client_id = res.getInt("client_id");
				if(prev_client_id==-1)
				{
					String client_id_str = InvoiceData.ensure3Places(res.getInt("client_id"));
					invoice_data.addClientDetail("client_id", client_id_str);
					invoice_data.addClientDetail("client_name", res.getString("client_name"));
					invoice_data.addClientDetail("client_address", res.getString("client_address"));
					invoice_data.addClientDetail("client_city", res.getString("client_city"));
					invoice_data.addClientDetail("client_country", res.getString("client_country"));
					invoice_data.addClientDetail("client_phone_number", res.getString("client_phone_number"));
					invoice_data.addClientDetail("client_email", res.getString("client_email"));
				}//if.
				else if(cur_client_id!=prev_client_id)
				{
					log.info(class_name+" cur_client_id="+cur_client_id+" prev_client_id="+prev_client_id+" work_log_id="+res.getString("work_log_id"));//debug**
					invoice_data.error=true;
					invoice_data.error_message="Logs aren't all for the same client.";
					invoice_data.error_message+=" Log "+res.getInt("work_log_id")+" has client_id = "+cur_client_id+". Previous client_id="+prev_client_id;
				}//else if.

				invoice_data.addBillingRow(res.getLong("work_log_start_epoch"), res.getDouble("elapsed_time"),res.getString("work_log_description"));
				results_found=true;

				prev_client_id = cur_client_id;
			}//while.

			if(invoice_data.error)
			{return invoice_data;}

			if(results_found && new_invoice)
			{
			//Update Invoice Number in db.
				stmt = conn.prepareStatement(update_last_invoice_number);
				stmt.setInt(1, invoice_data.invoice_number);
				log.info(class_name+" update_last_invoice_number="+stmt);//debug**
				stmt.executeUpdate();

			//Update work_logs.
				stmt = conn.prepareStatement(update_work_logs);
				stmt.setInt(1, invoice_data.invoice_number);
				int insert_index=2;
				for(String id_str: ids)
				{
					stmt.setInt(insert_index, Integer.parseInt(id_str));
					insert_index++;
				}//for(id)
				log.info(class_name+" update_work_logs="+stmt);//debug**
				stmt.executeUpdate();
			}//if.
			else if(!results_found)
			{
				invoice_data.error=true;
				invoice_data.error_message="No valid work_logs found.";
			}//else.

			return invoice_data;
		}//try.
		catch(SQLException se)
		{
			log.severe(class_name+" SQL Exception while trying to generate invoice:\n"+se);
			invoice_data.error=true;
			invoice_data.error_message="SQL Exception occurred.";
			return invoice_data;
		}//catch().
		finally
		{
			try
			{conn.close();}
			catch(SQLException se)
			{log.severe(class_name+" SQL Exception while trying to close db connection in generateInvoice():\n"+se);}
		}//finally.

	}//generateInvoice().

	public static HashMap<String, String> getClientDetails(int client_id)
	{
		HashMap<String, String> client_details = new HashMap<String, String>();

		String get_client_details = "SELECT *"
								+ " FROM client"
								+ " JOIN country ON country_id=client_country_id"
								+ " WHERE client_id="+client_id;
		try(Connection conn = DatabaseHelper.getConnection())
		{
			log.info(class_name+" getClientDetails(): get_client_details="+get_client_details);//INFO.
			ResultSet res = conn.prepareStatement(get_client_details).executeQuery();

			if(res.next())
			{
				client_details.put("client_id", res.getString("client_id"));
				client_details.put("client_name", res.getString("client_name"));
				client_details.put("client_address", res.getString("client_address"));
				client_details.put("client_city", res.getString("client_city"));
				client_details.put("client_country", res.getString("country_name"));
				client_details.put("client_phone_number", res.getString("client_phone_number"));
				client_details.put("client_email", res.getString("client_email"));
			}//if.
		}//try.
		catch(SQLException se)
		{
			log.severe(class_name+" SQL Exception while trying to get client details:\n"+se);
		}//catch().
		return client_details;
	}//getClientDetails().

}//class Backend.
