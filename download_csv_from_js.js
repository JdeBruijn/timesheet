//Jon de Bruijn.
//2020-03-12.
//This is a generic class to download a .csv file from data in, or accessible from, javascript.
var class_name="download_csv_from_js.js";

function downloadCSVFromArray(data, headers, filename, message)//data=>2D array, header=>csv string
{
	var csv = "";
	var max_data_row_length=0;
	for(r=0; r<data.length; r++)
	{
		var row = data[r];
		if(row==null || row.length<=0)
		{continue;}
		else if(row.length>max_data_row_length)
		{max_data_row_length=row.length;}
		csv+="\n";	
		csv+=row.join(",");
	}//for(r).
	
	downloadCSV(csv, max_data_row_length, headers, filename, message);
}//downloadCSVFromArray().

function downloadCSVByHTMLClass(parent_element_id, row_class, column_class, headers_class, filename, message)//Specify the html class of the elements that define the rows and the elements who's html=csv values.
{
	console.log("downloadCSVByHTMLClass(): parent_element_id="+parent_element_id);//debug**
	var parent_element = document.getElementById(parent_element_id);
	
	var rows;
	if(row_class!=null)
	{rows = parent_element.getElementsByClassName(row_class);}
	else
	{rows=parent_element.children;}

	console.log("rows="+rows.length);//debug**
		
	if(rows==null || rows.length<=0)
	{downloadError(true, "No row elements found with class="+row_class);return;}
	
	var headers="";
	if(headers_class!=null)
	{
		var header_elements = parent_element.getElementsByClassName(headers_class);
		for(h=0; h<header_elements.length; h++)
		{
			if(h>0)
			{headers+=",";}
			headers+=header_elements[h].innerHTML;
		}//for(h).
	}//if.
	
	var csv="";
	var max_data_row_length=0;
	for(r=0; r<rows.length; r++)
	{
		var columns;
		if(column_class!=null)
		{columns = rows[r].getElementsByClassName(column_class);}
		else
		{columns = rows[r].children;}	

		console.log("columns = "+columns.length);//debug**
			
		if(columns==null || columns.length<=0)
		{downloadError(false, "No column elements found with class="+column_class+" in row "+r);continue;}
		else if(columns.length>max_data_row_length)
		{max_data_row_length=columns.length;}
		csv+="\n";
		for(c=1; c<columns.length; c++)
		{
			if(c>1)
			{csv+=",";}
			var cell=columns[c];
			var cell_value=cell.innerHTML;
			if(cell.tagName.toLowerCase()=="input")
			{cell_value=cell.value;}
			if(cell_value=="")
			{cell_value=" ";}
			csv+=cell_value;
			console.log("cell_value="+cell_value);//debug**
		}//for(c).
	}//for(r).
	
	downloadCSV(csv, max_data_row_length, headers, filename, message);
}//downloadCSVByHTMLClass().

function downloadCSV(csv, max_data_row_length, headers, filename, message)
{
//	var headers_list = headers.split(",");
//	if(headers==null || headers.length<=0)
//	{downloadError(false,"No headers specified, headers="+headers);}
//	if(headers_list.length!=max_data_row_length)
//	{downloadError(true,"Data columns length("+max_data_row_length+")!=headers length("+headers_list.length+")");}
	if(csv.length<=0)
	{downloadError(true,"No data for csv! data="+data);return;}
	
//	csv=headers+csv;
//	if(message!=null)
//	{csv+="\n\n"+message;}

	console.log("csv = "+csv);//debug**
	
    var csv_element = document.createElement('a');
    csv_element.href = 'data:text/csv;charset=utf-8,'+encodeURI(csv);
    csv_element.target = '_blank';
    csv_element.download = filename;
    csv_element.click();
}//downloadCSV().

function downloadError(critical, message)
{
	var severity = " WARNING! ";
	if(critical)
	{severity=" ERROR! ";}
	console.log(class_name+severity+message);
	if(critical)
	{alert("Sorry something went wrong trying to create/export the csv file.<br>Please contact support.");}
}//downloadError().

