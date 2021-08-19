package com.codeglide.interfaces.xmldb;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import com.codeglide.core.ServerSettings;
import com.codeglide.core.Logger;

public class FileDbInputStream extends DbInputStream {
	protected File binPath;
	
	public FileDbInputStream(String appName, long nodeId, long streamId) {
		super(appName, nodeId, streamId);
		setBinPath(appName, nodeId, 0);
	}

	public FileDbInputStream( File binPath ) {
		super();
		this.binPath = binPath;
	}

	private void setBinPath(String appName, long parentId, int seed) {
		StringBuffer name = new StringBuffer();
		name.append(((DbInterface)ServerSettings.getInterface(ServerSettings.XMLDB_IF)).getParameter("binarystore"));
		name.append("/");
		name.append(appName);
		name.append("/");
		name.append(String.valueOf((parentId+seed)%64));
		name.append("/");
		name.append(String.valueOf((streamId+seed)%64));
		name.append("/");
		name.append(Long.toString(streamId, 36).toUpperCase());
		if( seed > 0 )
			name.append(Integer.toString(seed, 36).toUpperCase());
		binPath = new File(name.toString());
	}
	
	public long getSize() {
		return binPath.length();
	}
	
	public void open() throws IOException {
		stream = new BufferedInputStream(new FileInputStream(binPath));
	}
	
	public void delete() {
		try {
			if( stream != null )
				stream.close();
			binPath.delete();
		} catch (IOException e) {
			Logger.debug(e);
		}
	}

	public void insert(InputStream stream) {
		try {
			FileOutputStream fos = new FileOutputStream(binPath);
			BufferedInputStream is = new BufferedInputStream(stream);
			BufferedOutputStream os = new BufferedOutputStream(fos);
			int c;
			while( (c = is.read()) != -1 )
				os.write(c);
			os.flush();
			os.close();
			fos.close();
			is.close();
		} catch (FileNotFoundException e) {
			Logger.debug(e);
		} catch (IOException e) {
			Logger.debug(e);
		}
	}

	public void setSize(long size) {
	}
	

}
