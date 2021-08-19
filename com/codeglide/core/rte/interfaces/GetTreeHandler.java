package com.codeglide.core.rte.interfaces;

import java.util.List;

import com.codeglide.core.rte.contexts.ContextUi;
import com.codeglide.core.rte.exceptions.CodeGlideException;
import com.codeglide.core.rte.render.Record;

public interface GetTreeHandler {
	public List<Record> getRootChildren(ContextUi context) throws CodeGlideException;
	public List<Record> getChildren(ContextUi context, String parentId) throws CodeGlideException;
}
