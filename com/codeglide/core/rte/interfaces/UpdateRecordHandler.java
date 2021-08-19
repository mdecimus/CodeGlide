package com.codeglide.core.rte.interfaces;

import com.codeglide.core.rte.contexts.Context;
import com.codeglide.core.rte.exceptions.CodeGlideException;
import com.codeglide.core.rte.render.Record;

public interface UpdateRecordHandler {

	public boolean addRecord( Context context, Record record ) throws CodeGlideException;
	public boolean updateRecord( Context context, String id, Record record ) throws CodeGlideException;
	
}
