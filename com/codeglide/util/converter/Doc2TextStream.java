package com.codeglide.util.converter;

import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;

import org.apache.poi.hwpf.extractor.WordExtractor;

import com.codeglide.core.Logger;

public class Doc2TextStream extends Reader{

	private InputStream inputStream;
	private String wordText;
	private int position;
		
	public Doc2TextStream( InputStream in ) {
		
		this.inputStream = in;
		
		try {
			
			WordExtractor extractor = new WordExtractor(inputStream);
			wordText = extractor.getText();
			position = 0;
			
		} catch (IOException e) {
			Logger.debug(e);
		}		
		
	}
	
	public int read(){
		
		if(position < wordText.length())
			return wordText.charAt(position++);
		else
			return -1;
	}
	
	
	@Override
	public void close() throws IOException {}

	@Override
	public int read(char[] cbuf, int off, int len) throws IOException {
		throw new IOException("Not implemented");
	}

}
