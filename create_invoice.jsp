<%@ page import="timesheet.Backend"%>
<%@ page import="timesheet.InvoiceData"%>
<%@ page import="java.util.LinkedList"%>
<%@ page import="java.util.HashMap"%>
<%@ page import="java.util.logging.Logger"%>
<%@ page import="org.json.simple.JSONObject"%>

<%
	String class_name="create_invoice.jsp";
	Logger log = Logger.getLogger(class_name);

	JSONObject clients = Backend.getClients();

	log.info(class_name+" clients = "+clients.toString());//debug**

%>

<html>

<head>
	<title>Create Invoice</title>
	<link rel="stylesheet" type="text/css" href="/timesheet/create_invoice.css">
	<script type="text/javascript" src="invoice.js"></script>
</head>

<body>

	<script>
		var client_mapper = {};
	</script>

	<div class="main_box">
		<div class="input_row" style="margin-top:10px;">
			<div class="input_label">Invoice Number: </div>
			<input id="invoice_number_input" class="input_box" type="Number">
		</div>
		<div class="input_row">
			<div class="input_label">Rate: </div>
			<input id="rate_input" class="input_box" type="Number">
		</div>
		<div class="input_row">
			<div class="input_label">Client: </div>
			<select id="client_input">
		<%
		for(Object client_key: clients.keySet())
		{
			String client_name = (String) client_key;
			String client_id = (String)clients.get(client_key);
			%>
			<script>client_mapper["<%=client_name%>"]="<%=client_id%>";</script>
			<option><%=client_name%></option>
			<%
		}
		%>
			</select>
		</div>

		<div class="input_row">
			<button class="input_label" style="margin-left: 48%;" onclick="createInvoice()">Create</button>
		</div>

	</div>
</body>

<script>

	function createInvoice()
	{
		var invoice_number = document.getElementById("invoice_number_input").value;
		var rate = document.getElementById("rate_input").value;
		var client_name = document.getElementById("client_input").value;
		var client = client_mapper[client_name];

		window.location.href="/timesheet/invoice.jsp?create_mode=true&invoice_number="+invoice_number+"&rate="+rate+"&client_id="+client;
	}//createInvoice().
</script>

</html>
