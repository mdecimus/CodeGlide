package com.codeglide.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

public class TemporaryInputStream extends InputStream {
	final private File file;
	private FileInputStream fstream;
	private long mark = 0;
	private long cursor = 0;
	
	//Stores the stream contrent into a temporary file and opens another stream to access it
	public TemporaryInputStream(InputStream istream) throws IOException {
		file = this.createTemporaryFile(istream);
		this.reset();
	}
	
	//Creates a temporary file from data in stream
	private File createTemporaryFile(InputStream istream) throws IOException {
		final int BufferSize = 100;
		//Create a temporary file, with pefix cgs = CodeGlide Stream, and default subfix and location 
		File file = File.createTempFile("cgs",null,null);
		file.deleteOnExit();
		try {
			//Fill the file with the stream
			FileOutputStream ostream = new FileOutputStream(file);
			byte[] buffer = new byte[BufferSize];
			while (true) {			
				int ammount = istream.read(buffer,0,BufferSize);
				if (ammount == -1)
					break;	//All data read from istream, exit loop
				else
					ostream.write(buffer,0,ammount);
			}
			ostream.close();	
		} catch (IOException e) {
			file.delete();
			throw e;
		}
		return file;
	}
	
	//When stream is freed, delete the temporary file
	protected void finalize() {
		try {
			fstream.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		file.delete();
	};

	//Just delegate to the file stream
	public int available() throws IOException {
		return fstream.available();
	}
	
	//Just delegate to the file stream
	public void close() throws IOException {
		fstream.close();
	}
	
	//Save the offset
	public void mark(int readlimit) {
		mark = cursor;
	}
	
	public boolean markSupported() {
		return true;		
	}

	//Delegate to the file stream and update cursor 
	public int read() throws IOException {
		int ammount = fstream.read();
		if (ammount > -1)
			cursor += ammount;
		return ammount;
	}
	
	//Delegate to the file stream and update cursor
	public int read(byte[] b) throws IOException {
		int ammount = fstream.read(b);
		if (ammount > -1)
			cursor += ammount;
		return ammount;
	}
	
	//Delegate to the file stream and update cursor
	public int read(byte[] b, int off, int len) throws IOException {
		int ammount = fstream.read(b,off,len);
		if (ammount > -1)
			cursor += ammount;
		return ammount;
	}
	
	//Reopen (or just opon) the stream on file. Discard bytes up to "mark" and set "cursor" to that point 
	public void reset() throws IOException {
		try {
			fstream = new FileInputStream(file);
			/*
				Doing simply
 					fstream.skip(mark);
 				does not work beacuse skip may discard a smaller number of bytes
			*/
			long pending = mark;
			while (pending > 0)
				pending -= fstream.skip(pending);
			cursor = mark;
		} catch (IOException e) {
			file.delete();
			throw e;
		}
	}
	
	//Delegate to the file stream and update cursor
	public long skip(long n) throws IOException {
		long ammount = fstream.skip(n);
		if (ammount > -1)
			cursor += ammount;
		return ammount;
	}
}
