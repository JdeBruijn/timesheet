<%@ page import="timesheet.InvoiceData"%>
<html>

<head>
	<title>TimeSheet</title>
	<link rel="stylesheet" type="text/css" href="index.css">
	<script type="text/javascript" src="index.js"></script>
	<script type="text/javascript" src="download_csv_from_js.js"></script>
	<script type="text/javascript" src="https://jondeb.duckdns.org/javascript/mobile_browser_support.js"></script>
</head>

<body>
	<div class="line">
		<button id="button" onclick="submitChanges()" style="margin-left:20px; float:right;">Submit Changes</button>
		<button id="download_button" onclick="downloadCSVByHTMLClass('main_container','row','cell',null, 'timesheet_download.csv',null)" style="margin-left:20px;float:right;">Download CSV</button>
		<input id="rate_input" type="number" placeholder="<%=InvoiceData.base_rate%>">
		<button id="invoice_button" onclick="generateInvoice()">Generate Invoice</button>
	</div>
	<div class="line">
		<div style="display:inline-block;">Start date: </div>
		<input id="start_date" style="display:inline-block;" type="date" min="2021-07-01">
		<div style="display:inline-block;">End date: </div>
		<input id="end_date" style="display:inline-block;" type="date" min="2021-07-01">
		<div style="display:inline-block;">Client: </div>
		<input id="client_selector" list="client_selector_list" style="display:inline-block;">
		<button onclick="getData()">Apply</button>
	</div>

	<datalist id="client_selector_list">
		
	</datalist>

	<div id="main_container" class="main_container">
	</div>

</body>

<script>
	document.onLoad=setup();
</script>

</html>
