

function printInvoiceToPDF()
{
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


function startup()
{
	fixCellHeights();
}//startup().