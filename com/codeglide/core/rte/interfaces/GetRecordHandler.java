package com.codeglide.core.rte.interfaces;

import java.util.List;

import com.codeglide.core.rte.contexts.Context;
import com.codeglide.core.rte.exceptions.CodeGlideException;
import com.codeglide.core.rte.render.Record;

public interface GetRecordHandler {

	public List<Record> getRecords(Context context, int pageStart, int pageLimit, String sortBy, String groupBy, String filterBy) throws CodeGlideException;
	public List<Record> getRecords(Context context) throws CodeGlideException;
	
}
