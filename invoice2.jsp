<%@ page import="timesheet.Backend"%>
<%@ page import="timesheet.InvoiceData"%>
<%@ page import="java.util.LinkedList"%>
<%@ page import="java.util.HashMap"%>

<%
	String class_name="invoice.jsp";

	String rate_str = request.getParameter("rate");
	String row_ids = request.getParameter("row_ids");
	String group_by = request.getParameter("group_by");
	String invoice_id = request.getParameter("invoice_id");
	InvoiceData invoice_data = Backend.generateInvoice(row_ids, group_by, invoice_id, rate_str);
	System.out.println(class_name+" invoice_data.error="+invoice_data.error);//debug**
%>

<html>

<head>
	<title><%=invoice_data.getInvoiceName()%></title>
	<link rel="stylesheet" type="text/css" href="invoice2.css">
	<script type="text/javascript" src="invoice.js"></script>
</head>

<body>
<%if(invoice_data.error)
{%>
	<%=invoice_data.error_message%>
<%}//if.
else
{%>
	<div id="main_container">
		<div class="invoice_row" style="padding-bottom:0px; text-align: center;">
			<div class="details_row" style="font-weight: 700; font-size: 30px;">INVOICE</div>
		</div>
		<div class="invoice_row" style="padding-top:0px; border-bottom: 1px solid black; margin-bottom:10px;">
			<div class="row_block" style="width:30%; float:right; text-align:right;">
				<div class="details_row" style="font-weight:500">Jonathan de Bruijn</div>
				<div class="details_row">8 Tedder road, Greendale</div>
				<div class="details_row">Harare, Zimbabwe</div>
				<div class="details_row">(+263) 777 697 631</div>
				<a class="details_row" href="mailto:jondebruijn42@gmail.com">jondebruijn42@gmail.com</a>
				<a class="details_row" href="https://jondeb.duckdns.org/jon/">website</a>
			</div>
		</div>
		<div class="invoice_row" style="padding-top:0px;">
			<div class="row_block" style="width:30%;float:left; text-align:left;">
				<div class="details_row">
					<div class="details_label">Date: </div>
					<div class="details_value"><%=invoice_data.invoice_date%></div>
				</div>
				<div class="details_row">
					<div class="details_label">Invoice: </div>
					<div class="details_value"><%=invoice_data.getInvoiceNumber()%></div>
				</div>
				<div class="details_row">
					<div class="details_label">Customer Id:</div>
					<div class="details_value"><%=invoice_data.client_details.get("client_id")%></div>
				</div>
				<div class="details_row">
					<div class="details_label">Due Date:</div>
					<div class="details_value"><%=invoice_data.due_date%></div>
				</div>
			</div>
		</div>
		<div class="invoice_row" style="border:1px solid black; padding-top:5px;">
			<div class="row_block" style="float:left; width:45%; text-align: left;">
				<div class="details_row" style="background:white; color:black; font-weight:bold;">BILL TO:</div>
				<div id="client_name" class="details_row"><%=invoice_data.client_details.get("client_name")%></div>
				<div id="client_street_address" class="details_row"><%=invoice_data.client_details.get("client_address")%></div>
				<div id="client_city" class="details_row"><%=invoice_data.client_details.get("client_city")%></div>
				<div id="client_phone_number" class="details_row"><%=invoice_data.client_details.get("client_phone_number")%></div>
			</div>
		</div>
		<div id="billing_details_table" class="invoice_row" style="padding-bottom:0;">
			<div class="table_row table_header">
				<div class="table_cell header_cell description_cell">Description</div>
				<div class="table_cell header_cell">Hours</div>
				<div class="table_cell header_cell">Rate</div>
				<div class="table_cell header_cell amount_cell">USD Amount</div>
			</div>
		<%
		String background_color = "";
		int rows_count = Math.max(8, invoice_data.billing_rows.size());
		for(int r=0; r<rows_count; r++)
		{
			String[] details = {" "," "," "," "};
			if(r<invoice_data.billing_rows.size())
			{details = invoice_data.billing_rows.get(r);}
			/*if(r%2==0)
			{background_color = "#e6e1e1";}
			else
			{background_color = "white";}*/
			background_color = "white";
		%>
			<div class="table_row" style="background:<%=background_color%>;">
				<div class="table_cell description_cell"><%=details[0]%></div>
				<div class="table_cell"><%=details[1]%></div>
				<div class="table_cell"><%=details[2]%></div>
				<div class="table_cell amount_cell" style="width:10%;"><%=details[3]%></div>
			</div>
		<%}//for(details).%>
			<div class="table_row" style="background:<%=background_color%>;">
				<div class="table_cell description_cell"></div>
				<div class="table_cell"></div>
				<div class="table_cell"></div>
				<div class="table_cell amount_cell" style="width:10%;"></div>
			</div>

			<div class="table_row" style="background:<%=background_color%>;">
				<div class="table_cell" style="width:60%; font-weight:bold;">Total Amount Due In USD: </div>
				<div class="table_cell"></div>
				<div class="table_cell"></div>
				<div class="table_cell amount_cell" style="width:10%; font-weight:bold; border-top:1px solid black;">$ <%=invoice_data.getTotal()%></div>
			</div>
		</div>

		<div class="invoice_row" style="float:left;font-weight:bold; margin-top: 20px; padding-bottom:0;">Payment instructions:</div>
		<div class="invoice_row" style="float:left; padding-bottom:0;">Please remit USD to:</div>

		<div class="invoice_row" style="float:left;">
			<div class="row_block" style="float:left; width:80%; padding:20px; padding-left:0; padding-top:0;">
				<div class="comments_box">
					<div class="comments_body">
						<div class="comments_line">Beneficiary Account Name: Jonathan de Bruijn</div>
						<div class="comments_line">Beneficiary Address: 8 Tedder road, Greendale, Harare, Zimbabwe</div>
						<div class="comments_line">Beneficiary Account #:9600006202039051</div>
						<div class="comments_line">Beneficiary Bank: WISE</div>
						<div class="comments_line">Bank's Address: 30 W. 26th Street, Sixth Floor, New York NY 10010, United States</div>
						<div class="comments_line">SWIFT/BIC: CMFGUS33</div>
						<div class="comments_line">Routing Number: 084009519</div>
					</div>
				</div>
			</div>
		</div>
	</div>

	<button onclick="printInvoiceToPDF()">Print</button>
<%}//else.%>
</body>

<script>document.onload=startup();</script>

</html>
