package com.codeglide.core.rte.interfaces;

import java.util.List;

import com.codeglide.core.rte.contexts.ContextUi;
import com.codeglide.core.rte.exceptions.CodeGlideException;
import com.codeglide.core.rte.render.Action;

public interface ClickHandler {
	public void handleClick(ContextUi context, List<Action> result, Object input) throws CodeGlideException;
	public void handleDoubleClick(ContextUi context, List<Action> result, Object input) throws CodeGlideException;
}
