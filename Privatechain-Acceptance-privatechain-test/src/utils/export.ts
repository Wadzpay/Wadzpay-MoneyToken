import * as XLSX from "xlsx";
import { saveAs } from "file-saver";
import autoTable from "jspdf-autotable";
import jsPDF from "jspdf";

export const exportToCSV = (fileName: string, data: any) => {
  // Create a new worksheet from the data
  const worksheet = XLSX.utils.json_to_sheet(data);

  // Convert the worksheet to CSV format
  const csv = XLSX.utils.sheet_to_csv(worksheet);

  // Convert the CSV string to a Blob and save it as a CSV file
  const blob = new Blob([csv], { type: "text/csv;charset=utf-8;" });
  saveAs(blob, `${fileName}.csv`);
};

export const exportToPDF = (
  fileName: string,
  head: any,
  body: any,
  mode: string = "portrait"
) => {
  const doc =
    mode === "portrait"
      ? new jsPDF()
      : new jsPDF({
          orientation: "landscape",
        });

  autoTable(doc, {
    head: [head],
    body,
    // columnStyles: {
    //   0: { cellWidth: 35 },
    //   1: { cellWidth: 58 },
    //   5: { cellWidth: 20 },
    // },
  });

  doc.save(`${fileName}.pdf`);
};
