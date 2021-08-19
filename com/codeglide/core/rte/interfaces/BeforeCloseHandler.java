package com.codeglide.core.rte.interfaces;

import java.util.List;

import com.codeglide.core.rte.contexts.ContextUi;
import com.codeglide.core.rte.exceptions.CodeGlideException;
import com.codeglide.core.rte.render.Action;

public interface BeforeCloseHandler {
	public void handleBeforeClose(ContextUi context, List<Action> result) throws CodeGlideException;
}
