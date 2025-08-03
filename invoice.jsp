<%@ page import="timesheet.Backend"%>
<%@ page import="timesheet.InvoiceData"%>
<%@ page import="java.util.LinkedList"%>
<%@ page import="java.util.HashMap"%>
<%@ page import="java.util.logging.Logger"%>


<%
	String class_name="invoice.jsp";
	Logger log = Logger.getLogger(class_name);

	String create_mode = request.getParameter("create_mode");

	boolean foreign_account=false;

	InvoiceData invoice_data = new InvoiceData();
	if(create_mode==null || create_mode.equals("false"))
	{
		String invoice_id = request.getParameter("invoice_id");
		String currency = request.getParameter("currency");
		String rate_str = request.getParameter("rate");
		String row_ids = request.getParameter("row_ids");
		String group_by = request.getParameter("group_by");
		invoice_data = Backend.generateInvoice(row_ids, group_by, invoice_id, currency, rate_str, invoice_data);
	}//if.
	else 
	{
		try
		{
			String invoice_number = request.getParameter("invoice_number");
			String currency = request.getParameter("currency");
			String rate_str = request.getParameter("rate");
			String client_id = request.getParameter("client_id");
			HashMap<String, String> client_details = Backend.getClientDetails(Integer.parseInt(client_id));
			invoice_data.setClientDetails(client_details);
			invoice_data.invoice_number = Integer.parseInt(invoice_number);
			invoice_data.setCurrency(currency);
			invoice_data.rate = Double.parseDouble(rate_str);
		}//try.
		catch(NullPointerException | NumberFormatException nfe)
		{
			invoice_data.error=true;
			invoice_data.error_message="Missing/Invalid details for creating new invoice";
			log.warning(class_name+" Exception while trying to create invoice:\n"+nfe);
		}//catch().
	}//else.
	
	System.out.println(class_name+" invoice_data.error="+invoice_data.error);//debug**

	HashMap<String, String> client_details = invoice_data.getClientDetails();
	LinkedList<String[]> billing_rows = invoice_data.getBillingRows();
%>

<html>

<head>
	<title><%=invoice_data.getInvoiceName()%></title>
	<link rel="stylesheet" type="text/css" href="invoice.css">
	<script type="text/javascript" src="invoice.js"></script>
</head>

<body>
<%if(invoice_data.error)
{%>
	<%=invoice_data.error_message%>
	<a href="/timesheet/create_invoice.jsp" >Create Invoice</a>
<%}//if.
else
{%>
	<div class="line">
		<button id="save_edits_button" onclick="onFinishEdit()">Save edits</button>
	</div>

	<div id="main_container">
		<div class="invoice_row" style="padding-bottom:0px; text-align: center;">
			<div class="details_row" style="font-weight: 700; font-size: 30px;">INVOICE</div>
		</div>
		<div class="invoice_row" style="padding-top:0px;">
			<div class="row_block" style="width:30%; float:right; text-align:left;">
				<div class="details_row" style="font-weight:500" onclick="editCell(event)">Jonathan de Bruijn</div>
				<div class="details_row" onclick="editCell(event)">8 Tedder road, Greendale</div>
				<div class="details_row" onclick="editCell(event)">Harare, Zimbabwe</div>
				<div class="details_row" onclick="editCell(event)">(+263) 777 697 631</div>
				<a class="details_row" href="mailto:jondebruijn42@gmail.com">jondebruijn42@gmail.com</a>
				<a class="details_row" href="https://jondeb.duckdns.org/jon/">website</a>
			</div>
		</div>
		<div class="invoice_row" style="padding-top:0px">
			<div class="row_block" style="width:30%;float:left;text-align:left;">
				<div class="details_row">
					<div class="details_label">Date: </div>
					<div class="details_value" onclick="editCell(event)"><%=invoice_data.invoice_date%></div>
				</div>
				<div class="details_row">
					<div class="details_label">Invoice#: </div>
					<div class="details_value" onclick="editCell(event)"><%=invoice_data.getInvoiceNumber()%></div>
				</div>
				<div class="details_row">
					<div class="details_label">Customer Id:</div>
					<div class="details_value" onclick="editCell(event)"><%=client_details.get("client_id")%></div>
				</div>
				<div class="details_row">
					<div class="details_label">Due Date:</div>
					<div class="details_value" onclick="editCell(event)"><%=invoice_data.due_date%></div>
				</div>
			</div>
		</div>
		<div class="invoice_row">
			<div class="row_block" style="float:left; width:45%; text-align: left;">
				<div class="details_row" style="background:black; color:white;">Bill To</div>
				<div id="client_name" class="details_row" onclick="editCell(event)"><%=client_details.get("client_name")%></div>
				<div id="client_street_address" class="details_row" onclick="editCell(event)"><%=client_details.get("client_address")%></div>
				<div id="client_city" class="details_row" onclick="editCell(event)"><%=client_details.get("client_city")%></div>
				<div id="client_city" class="details_row" onclick="editCell(event)"><%=client_details.get("client_country")%></div>
			</div>
		</div>
		<div id="billing_details_table" class="invoice_row" style="padding-bottom:0;">
			<div class="table_row table_header">
				<div class="table_cell header_cell description_cell">Description</div>
				<div class="table_cell header_cell">Hours</div>
				<div class="table_cell header_cell">Rate</div>
				<div class="table_cell header_cell amount_cell">Amount</div>
			</div>
		<%
		String background_color = "";
		int rows_count = Math.max(8, billing_rows.size());
		for(int r=0; r<rows_count; r++)
		{
			String[] details = {" "," "," "," "};
			if(r<billing_rows.size())
			{details = billing_rows.get(r);}
			if(r%2==0)
			{background_color = "#e6e1e1";}
			else
			{background_color = "white";}
		%>
			<div class="table_row" style="background:<%=background_color%>;" oncontextmenu="removeRow(event)">
				<div class="table_cell description_cell" onclick="editCell(event)"><%=details[0]%></div>
				<div class="table_cell hours_cell" onclick="editCell(event)"><%=details[1]%></div>
				<div class="table_cell rate_cell" onclick="editCell(event)"><%=details[2]%></div>
				<div class="table_cell amount_cell" style="width:10%;"><%=details[3]%></div>
			</div>
		<%}//for(details).%>
			<button class="add_table_row" onclick="addRow(event)">+</button>
			<script>var last_background_colour="<%=background_color%>";</script>
		</div>
		<div class="invoice_row" style="padding-top:0px;">
			<div class="row_block" style="float:right; width:calc( 30% + 1px ); border:1px solid black; border-top:none;">
				<div class="details_row" style="float:left; font-size:24px; font-weight:bold; padding-left:0;">
					<div class="details_label" style="width:33.3%;">Total: </div>
					<div id="invoice_total" class="details_value" style="width:66.6%;"><%=invoice_data.currency%> <%=invoice_data.getTotal()%></div>
				</div>
			</div>
		</div>
		<div class="invoice_row" style="float:left;">
			<div class="row_block" style="float:left; width:80%; padding:20px; padding-left:0;">
				<div class="comments_box">
					<div class="comments_header">Payment Details</div>
					<div class="comments_body">
					<%if(foreign_account)
					{%>	
						<div class="comments_line"><b>Account Name:</b> Jonathan de Bruijn</div>
						<div class="comments_line"><b>Address:</b> 8 Tedder road, Harare, Zimbabwe</div>
						<div class="comments_line"><b>Account Number:</b> 8312255558</div>
						<div class="comments_line"><b>Bank Name:</b> Wise</div>
						<div class="comments_line"><b>Bank Address:</b> 30 W. 26th Street, Sixth Floor, New York NY 10010, United States</div>
						<div class="comments_line"><b>Swift Code:</b> CMFGUS33</div>
					<%}
					else
					{
						if(invoice_data.currency.equals("ZWG"))
						{%>
							<div class="comments_line"><b>Account Name:</b> Jonathan Pieter de Bruijn</div>
							<div class="comments_line"><b>Account Number:</b> 11016020697</div>
							<div class="comments_line"><b>Bank Name:</b> Nedbank</div>
							<div class="comments_line"><b>Branch:</b> Msasa</div>
						<%}//if.
						else
						{%>
							<div class="comments_line"><b>Account Name:</b> Jonathan Pieter de Bruijn</div>
							<div class="comments_line"><b>Account Number:</b> 11992626838</div>
							<div class="comments_line"><b>Bank Name:</b> Southerton (503)</div>
							<div class="comments_line"><b>Branch:</b> Msasa</div>
						<%}//else%>
					<%}%>
					<!--	<div class="comments_line">2) Payments will be accepted in USD or ZWL at the current rate on the day of payment.</div>-->
					</div>
				</div>
			</div>
		</div>
	</div>

	<button onclick="printInvoiceToPDF()">Print</button>
<%}//else.%>
</body>

<script>
	var currency="<%=invoice_data.currency%>";
	var invoice_rate=<%=invoice_data.rate%>;
	document.onload=startup();
</script>

</html>
