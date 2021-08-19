/*
 * 	Copyright (C) 2007, CodeGlide - Entwickler, S.A.
 *	All rights reserved.
 *
 *	You may not distribute this software, in whole or in part, without
 *	the express consent of the author.
 *
 *	There is no warranty or other guarantee of fitness of this software
 *	for any purpose.  It is provided solely "as is".
 *
 */
package com.codeglide.util.converter;

import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;

import jxl.Cell;
import jxl.CellType;
import jxl.Sheet;
import jxl.Workbook;
import jxl.WorkbookSettings;
import jxl.read.biff.BiffException;

import com.codeglide.core.Logger;

public class Xls2TextStream extends Reader {

	private InputStream inputStream;
	private String xlsText;
	private int position;

	public Xls2TextStream( InputStream in ) {

		
		
		this.inputStream = in;

		try {

			WorkbookSettings settings = new WorkbookSettings();
			settings.setSuppressWarnings(true);
			
			Workbook workbook = Workbook.getWorkbook(inputStream, settings);
					
			xlsText = "";

			for (int i = 0; i < workbook.getNumberOfSheets(); i++) {

				Sheet sheet = workbook.getSheet(i);

				xlsText += sheet.getName() + " ";

				Cell[] row = null;

				for (int j = 0; j < sheet.getRows(); j++) {

					row = sheet.getRow(j);
					
					if(row.length > 0){

						for (int cell = 0; cell < row.length; cell++) {
							if(row[cell].getType() != CellType.EMPTY)
								xlsText += row[cell].getContents() + " ";
						}
					}
				}
			}

			position = 0;

		} catch (IOException e) {
			Logger.debug(e);
		} catch (BiffException e) {
			Logger.debug(e);
		}
	}

	public int read(){

		if(position < xlsText.length())
			return xlsText.charAt(position++);			
		else
			return -1;
	}


	@Override
	public void close() throws IOException {}

	@Override
	public int read(char[] cbuf, int off, int len)  throws IOException {
		throw new IOException("Not implemented");
	}

}
