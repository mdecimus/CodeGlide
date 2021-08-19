package com.codeglide.core.rte.interfaces;

import com.codeglide.core.rte.contexts.ContextUi;
import com.codeglide.core.rte.exceptions.CodeGlideException;

public interface RenameItemHandler {
	
	public void handleRenameItem( ContextUi context, String itemId, String itemName ) throws CodeGlideException;
	
}
