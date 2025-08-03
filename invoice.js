var current_input_cell = null;

//Defined in invoice.jsp
//var last_background_colour;
//var currency;
//var invoice_rate;

function printInvoiceToPDF()
{
	var edit_cells = document.getElementsByClassName("edit_cell");
	while(edit_cells[0]!=null)
	{
		current_input_cell=edit_cells[0];
		onFinishEdit();
	}//while.

	var original_contents = document.body.innerHTML;
	var print_contents  = document.getElementById("main_container").innerHTML;

	document.body.innerHTML=print_contents;

	window.print();

	document.body.innerHTML=original_contents;
}


function fixCellHeights()
{
	var rows = document.getElementsByClassName("table_row");
	for(r=0; r<rows.length; r++)
	{
		var row = rows[r];
		var height = row.offsetHeight;

		var cells = row.getElementsByClassName("table_cell");
		for(c=0; c<cells.length; c++)
		{
			cells[c].style.height = height+"px";
		}//for(c).
	}//for(r).
}//fixCellHeights().

function editCell(event)
{
	console.log("editCell()...");//debug**
	if(current_input_cell!=null)
	{onFinishEdit();}

	var cell = event.target;
	var parent = cell.parentElement;

	current_input_cell = createCell(cell,"INPUT");
	current_input_cell.onfocusout=onFinishEdit;
	current_input_cell.onblur=onFinishEdit;

	parent.insertBefore(current_input_cell, cell);
	parent.removeChild(cell);
	current_input_cell.focus();
}//editCell().

function onFinishEdit()
{
	console.log("onFinishEdit()...");//debug**

	if(current_input_cell==null)
	{
		console.log("ERROR! Failed to save edit! current_input_cell==null!");
		return;
	}//if.

	console.log("current_input_cell = "+JSON.stringify(current_input_cell));//debug**

	var parent = current_input_cell.parentElement;
	
	var value = current_input_cell.value;

	var cell = createCell(current_input_cell, "DIV");
	cell.onclick=editCell;
	parent.insertBefore(cell, current_input_cell);
	parent.removeChild(current_input_cell);

	current_input_cell=null;

	if(parent.classList.contains("table_row"))
	{adjustTotals(parent);}
}//onFinishEdit().

function createCell(original_cell, type)
{
	console.log("createCell()...");//debug**
	var cell = document.createElement(type);
	cell.classList=original_cell.classList;
	cell.style.width=original_cell.style.width;
	cell.style.height=original_cell.style.height;
	if(type=="DIV")
	{
		cell.innerHTML=original_cell.value;
		cell.classList.remove("edit_cell");
	}//if.
	else if(type=="INPUT")
	{
		cell.classList.add("edit_cell");
		cell.value=original_cell.innerHTML;
	}//else if.
	return cell;
}//createCell().

function addRow(event)
{
	console.log("addRow()...");
	if(last_background_colour=="white")
	{last_background_colour="#e6e1e1";}
	else
	{last_background_colour="white";}

	var table = document.getElementById("billing_details_table");
	var button = event.target;

	var row = document.createElement("DIV");
	row.classList.add("table_row");
	row.style.background=last_background_colour;
	row.oncontextmenu=removeRow;
	table.insertBefore(row, button);

	var description_cell = document.createElement("DIV");
	description_cell.classList.add("table_cell", "description_cell");
	description_cell.onclick=editCell;
	row.appendChild(description_cell);

	var hours_cell = document.createElement("DIV");
	hours_cell.classList.add("table_cell", "hours_cell");
	hours_cell.onclick=editCell;
	row.appendChild(hours_cell);

	var rate_cell = document.createElement("DIV");
	rate_cell.classList.add("table_cell", "rate_cell");
	rate_cell.onclick=editCell;
	row.appendChild(rate_cell);

	var amount_cell = document.createElement("DIV");
	amount_cell.classList.add("table_cell", "amount_cell");
	amount_cell.onclick=editCell;
	amount_cell.style.width="10%";
	row.appendChild(amount_cell);

}//addRow().

function removeRow(event)
{
	event.preventDefault();
	var row = event.target;
	while(!row.classList.contains("table_row"))
	{
		row=row.parentElement;
		if(row.parentElement==null)
		{
			console.log("Failed to correctly find 'table_row' element");
			return;
		}//if.
	}//while.

	if(!confirm("Are you sure you want to delete this row?"))
	{return false;}

	var table = row.parentElement;
	table.removeChild(row);

	 return false;
}//removeRow().

function adjustTotals(row)
{
	console.log("adjustTotals()...");//debug**
	var hours = Number(row.getElementsByClassName("hours_cell")[0].innerHTML);
	var rate = Number(row.getElementsByClassName("rate_cell")[0].innerHTML);
	console.log("hours="+hours+" rate="+rate);//debug**
	if(rate==0)
	{
		rate=invoice_rate;
		row.getElementsByClassName("rate_cell")[0].innerHTML=rate;
	}//if.

	var row_total = hours*rate;

	console.log("row_total = "+row_total);//debug**
	var amount_cell = row.getElementsByClassName("amount_cell")[0];
	amount_cell.innerHTML=Number(row_total).toFixed(2);
	console.log("amount_cell = "+JSON.stringify(amount_cell));//debug**

	var row_totals = document.getElementsByClassName("amount_cell");
	let total_total = 0;
	for(rt=0; rt<row_totals.length; rt++)
	{
		var amount = row_totals[rt].innerHTML;
		if(amount=="Amount")//Exclude header row.
		{continue;}

		var rotot = Number(row_totals[rt].innerHTML);
		total_total +=rotot;
	}//for(rt).
	total_total=total_total.toFixed(2);
	console.log("rounded total_total="+total_total);//debug**
	var invoice_total_cell = document.getElementById("invoice_total");
	invoice_total_cell.innerHTML=currency+" "+Number(total_total).toFixed(2);
}//adjustTotals().


function startup()
{
	fixCellHeights();
	current_input_cell=null;

}//startup().