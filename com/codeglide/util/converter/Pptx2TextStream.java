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

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import com.codeglide.core.Logger;

public class Pptx2TextStream extends Reader {

	private Xml2TextStream xml2TextStream;

	public Pptx2TextStream( InputStream in ) {
		
		File tempFile = null;
		
		try {
			
			tempFile = File.createTempFile("pptx", null);

			FileOutputStream fos = new FileOutputStream(tempFile);
			BufferedOutputStream bos = new BufferedOutputStream(fos);

			int c;

			while ( (c = in.read()) != -1 )
				bos.write( (char) c );

			bos.close();
			fos.close();

			ZipFile zipFile = new ZipFile(tempFile);
			
			Enumeration enumeration = zipFile.entries();
			ArrayList<InputStream> list = new ArrayList<InputStream>();
			
			while(enumeration.hasMoreElements()){
				
				ZipEntry entry = (ZipEntry) enumeration.nextElement();
				
				if(entry.getName().startsWith("ppt/slides/slide"))
					list.add(zipFile.getInputStream(entry));					
			}
			
			xml2TextStream = new Xml2TextStream(list, false);
			tempFile.delete();
			
		} catch (IOException e) {
			tempFile.delete();
			Logger.debug(e);
		}
	}

	public int read(){
		return xml2TextStream.read();
	}

	@Override
	public void close() throws IOException {}

	@Override
	public int read(char[] cbuf, int off, int len) throws IOException {
		throw new IOException("Not implemented");
	}

}
