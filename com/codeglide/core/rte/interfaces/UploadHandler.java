package com.codeglide.core.rte.interfaces;

import java.util.List;

import com.codeglide.core.rte.ReceivedFile;
import com.codeglide.core.rte.contexts.ContextUi;
import com.codeglide.core.rte.exceptions.CodeGlideException;
import com.codeglide.core.rte.render.Action;

public interface UploadHandler {
	public String handleUpload(ContextUi context, List<Action> result, ReceivedFile file ) throws CodeGlideException;
}
