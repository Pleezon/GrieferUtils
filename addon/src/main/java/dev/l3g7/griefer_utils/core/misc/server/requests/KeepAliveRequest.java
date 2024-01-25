/*
 * This file is part of GrieferUtils (https://github.com/L3g7/GrieferUtils).
 * Copyright (c) L3g7.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 */

package dev.l3g7.griefer_utils.core.misc.server.requests;

import dev.l3g7.griefer_utils.core.misc.server.types.GUSession;
import dev.l3g7.griefer_utils.core.misc.server.Request;
import dev.l3g7.griefer_utils.core.misc.server.Response;

public class KeepAliveRequest extends Request<Void> {

	public KeepAliveRequest() {
		super("/keep_alive");
	}

	@Override
	protected Void parseResponse(GUSession session, Response response) {
		return null;
	}

}