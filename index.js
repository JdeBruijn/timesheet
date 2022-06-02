var clients = {};

function getClients()
{
	fetch("backend",
	{
		method:"GET",
		headers:
		{
			"oper":"get_clients"
		}
	})
	.then(function(response)
	{
		if(response.redirected)
		{
			window.location=response.url;
			return;
		}//if.

		response.json()
		.then(function(data)
		{
			if(!data.success)
			{
				alert("Failed to retrieve clients:\n"+data.message);
				return;
			}//if.

			clients = data.clients;//global var.
			populateList("client_selector",clients);
		});//then.
	})//then.
	.catch(function(error)
	{
		alert("Error retrieving clients:\n"+error);
	});//catch.
}//getClients().

function populateList(selector_id, data, default_name)
{
	var input = document.getElementById(selector_id);
	if(input==null)
	{
		alert("Invalid input '"+selector_id+"'");
		return;
	}//if.

	var data_list = document.getElementById(selector_id+"_list");
	if(data_list!=null)
	{data_list.parentElement.removeChild(data_list);}

	data_list = document.createElement("DATALIST");
	data_list.id=selector_id+"_list";

	if(default_name!=null)
	{
		var default_option = document.createElement("OPTION");
		default_option.value=0;
		default_option.innerHTML=default_name;
		data_list.appendChild(default_option);
	}//if.

	var names = Object.keys(data);
	for(n=0; n<names.length; n++)
	{
		var option = document.createElement("OPTION");
		option.value = names[n];
		data_list.appendChild(option);
	}//for(n).
	document.body.appendChild(data_list);

	input.setAttribute("list",data_list.id);
}//populateList().

function getData()
{
	var date_range = getDateRange();
	console.log("date_range = "+date_range);//debug**
	if(date_range==null)
	{
		alert("Invalid date range!");
		return;
	}//if.

	var client_name = document.getElementById("client_selector").value;
	//console.log("getData(): client_name="+client_name);//debug**
	var client_id = clients[client_name];
	console.log("getData(): client_id="+client_id);//debug**


	fetch("backend", 
	{
		method:'GET',
		headers:{
			"oper":"get_logs",
			"date_range":date_range,
			"client_id":client_id
		}
	})
	.then(function(response)
	{
		if(response.redirected)
		{window.location=response.url;}

		console.log("getData(): response.headers:");//debug**
		for(var pair of response.headers.entries())
		{console.log(pair[0]+":"+pair[1]);}//debug**

		response.json()
		.then(function(data)
		{
			if(!data.success)
			{alert("Failed to retrieve data.");return;}

			headers = data.headers;
			data_headers = data.data_headers;
			column_sizes = data.column_sizes;
			rows_data = data.rows_data;
			populateTable(headers, data_headers, column_sizes, rows_data);
		});
	})//then.
	.catch(function(error)
	{
		alert("Error trying to get Data:\n"+Error);
	});//catch().

}//getData().

function getDateRange()
{
	var date = new Date();
	var start_date_str = document.getElementById("start_date").value;
	var end_date_str = document.getElementById("end_date").value;
	var start_epoch = Date.parse(start_date_str);
	var end_epoch = Number(Date.parse(end_date_str));//plus 1 day to include logs from day selected.
	//console.log("end_epoch="+end_epoch);//debug**
	end_epoch = end_epoch+(1000*3600*24*1);
	//console.log(start_date_str+" "+start_epoch+" "+end_date_str+" "+end_epoch);//debug**
	if(end_epoch<start_epoch)
	{return null;}
	return start_epoch+"-"+end_epoch;
}//getDateRange().

function populateTable(display_headers, data_headers, column_sizes, rows_data)
{
	var main_container = document.getElementById("main_container");
	var existing_rows = main_container.children;
	while(existing_rows[0]!=null)
	{main_container.removeChild(existing_rows[0]);}

	var header_row = document.createElement("DIV");
	header_row.classList.add("header_row", "row");
	for(hc=0; hc<headers.length; hc++)
	{
		var header_cell = document.createElement("DIV");
		header_cell.classList.add("header_cell","cell");
		header_cell.innerHTML=headers[hc];
		header_cell.style.width=column_sizes[hc]+"%";
		header_row.appendChild(header_cell);
	}//for(hc).
	main_container.appendChild(header_row);

	for(rd=0; rd<rows_data.length; rd++)
	{
		var columns = rows_data[rd];
		if(columns==null || columns.length<=0)
		{continue;}

		console.log("populateTable(): columns = "+JSON.stringify(columns));//debug**
		console.log("populateTable(): data_headers = "+data_headers);//debug**

		var row_element = document.createElement("DIV");
		row_element.id=columns["work_log_id"];
		row_element.classList.add("row");

		for(c=0; c<data_headers.length; c++)
		{
			var name = data_headers[c];
			console.log(" name = "+name);//debug**
			var cell=null;
			if(name=="work_log_description")
			{
				cell = document.createElement("INPUT");
				cell.value = columns[name];
				cell.onclick=markAsEdited;
			}//if.
			else if(name=="client_name")
			{
				cell = document.createElement("INPUT");
				cell.value = columns[name];
				cell.setAttribute("list","client_selector_list");
				cell.onclick=markAsEdited;
			}//else if.
			else
			{
				cell = document.createElement("DIV");
				if(name=="work_log_id")
				{
					if(row_element.id=="0")//indicated a sum row.
					{cell.innerHTML="";}
					else
					{
						var check_box = document.createElement("INPUT");
						check_box.type="checkbox";
						check_box.classList.add("delete_box");
						check_box.onclick=markAsDeleted;
						cell.appendChild(check_box);
					}//else.
				}//if.
				else
				{cell.innerHTML=columns[name];}
			}//else.
			cell.setAttribute("name",name);
			cell.classList.add("cell");
			cell.style.width=column_sizes[c]+"%";
			row_element.appendChild(cell);
		}//for(c).
		main_container.appendChild(row_element);
	}//for(rd).
}//populateTable().

function markAsEdited(event)
{
	var cell = event.target;
	var row = cell.parentElement;
	while(!row.classList.contains("row"))
	{row = row.parentElement;}

	if(Number(row.id)<=0)//Summary row, not a log row.
	{return;}

	cell.classList.add("changed");
	row.classList.add("edited");
}//markAsEdited().

function markAsDeleted(event)
{
	var cell = event.target;
	var row = cell.parentElement;
	while(!row.classList.contains("row"))
	{row = row.parentElement;}

	row.classList.toggle("deleted");
}//markAsDeleted().

function submitChanges()
{
	var altered_rows = document.getElementsByClassName("edited");
	console.log("submitChanges(): altered_rows.length = "+altered_rows.length);//debug**

	var changes = {};
	if(altered_rows.length>0)
	{
		for(cr=0; cr<altered_rows.length; cr++)
		{
			var row = altered_rows[cr];
			var changed_cells = row.getElementsByClassName("changed");
			console.log("submitChanges(): changed_cells.length="+changed_cells.length);//debug**

			if(changed_cells.length<=0)
			{continue;}
			var row_changes = {};

			var changed=false;
			for(cc=0; cc<changed_cells.length; cc++)
			{
				var changed_cell = changed_cells[cc];
				var name = changed_cell.getAttribute("name");
				var value = changed_cell.value;
				console.log("submitChanges(): name="+name+" value="+value);//debug**
				if(value==null|| value=="")
				{value=" ";}
				else if(name=="client_name")
				{value = clients[value];}
				row_changes[name]=value;
				changed=true;
			}//for(cc).
			console.log("submitChanges(): row_changes = "+JSON.stringify(row_changes));//debug**
			if(changed)
			{changes[row.id]=row_changes;}
		}//for(cr).
	}//if.

	console.log("changes = "+JSON.stringify(changes));//debug**

	var deleted_rows = document.getElementsByClassName("deleted");
	var deleted_ids = "";
	if(deleted_rows.length>0 && confirm("Are you sure you want to delete "+deleted_rows.length+" rows?"))
	{
		for(r=0; r<deleted_rows.length; r++)
		{
			if(r>0)
			{deleted_ids+="~!";}
			deleted_ids+=deleted_rows[r].id;
		}//for(r).
	}//if.
	console.log("deleted_rows = "+deleted_ids);//debug**

	if(altered_rows.length<=0 && deleted_rows.length<=0)
	{alert("No changes found"); return;}

	var form_data = new FormData();
	form_data.append("changes", JSON.stringify(changes));

	fetch("backend",{
		method:"POST",
		headers:{
			"deleted":deleted_ids
		},
		body: form_data
	})
	.then(function(response)
	{
		if(response.redirected)
		{window.location=response.url;}

		response.json()
		.then(function(data)
		{
			if(!data.success)
			{alert("Error:\n"+data.message);}
			else
			{
				alert("Changes saved");
				getData();
			}//else.
		});//then
	})//then.
	.catch(function(error)
	{alert("Error trying to save changes:\n"+error);});//catch().
}//submitChanges().

function generateInvoice()
{
	var rows = document.getElementsByClassName("row");
	var log_ids = "";
	for(r=0; r<rows.length; r++)
	{
		if(rows[r].id=="0")
		{continue;}
		if(log_ids.length>0)
		{log_ids+=",";}
		log_ids+=rows[r].id;
	}//for(r).
	if(log_ids.length<=0)
	{alert("No valid logs found for invoice.");return;}

	window.open("invoice.jsp?row_ids="+log_ids+"&group_by= ");
}//generateInvoice().


function setupDateFilters()
{
	var date = new Date();
	var month = (((date.getMonth()+1)<10) ? "0"+(date.getMonth()+1) : (date.getMonth()+1) );
	var day = ((date.getDate()<10) ? "0"+date.getDate() : date.getDate() );
	var current_date_str=date.getFullYear()+"-"+month+"-"+day;

	var current_epoch = date.getTime();
	var start_of_week_diff = date.getDay()-1;
	if(start_of_week_diff<0)
	{start_of_week_diff=6;}
	//console.log("start_of_week_diff = "+start_of_week_diff);//debug**
	var start_of_week_epoch = current_epoch-(1000*60*60*24*start_of_week_diff);
	date.setTime(start_of_week_epoch);
	month = (((date.getMonth()+1)<10) ? "0"+(date.getMonth()+1) : (date.getMonth()+1) );
	day = ((date.getDate()<10) ? "0"+date.getDate() : date.getDate() );
	var start_of_week_date_str = date.getFullYear()+"-"+month+"-"+day;

	document.getElementById("end_date").value=current_date_str;
	document.getElementById("start_date").value=start_of_week_date_str;

	//console.log("start_date = "+start_of_week_date_str);//debug**
	//console.log("end_date = "+current_date_str);//debug**
}//setDateFilters().



function setup()
{
	setupDateFilters();
	getClients();
	getData();
}
