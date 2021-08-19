package com.codeglide.core.rte.interfaces;

import java.util.List;

import com.codeglide.core.rte.contexts.ContextUi;
import com.codeglide.core.rte.render.Action;
import com.codeglide.core.rte.sequencers.SequenceBucketizable;

public interface SetStringHandler {
	public void handleSet(ContextUi context, List<Action> result, SequenceBucketizable target, String value );
}
