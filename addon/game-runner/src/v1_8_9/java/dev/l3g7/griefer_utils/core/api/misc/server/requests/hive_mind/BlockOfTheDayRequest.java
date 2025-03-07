/*
 * This file is part of GrieferUtils (https://github.com/L3g7/GrieferUtils).
 * Copyright (c) L3g7.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 */

package dev.l3g7.griefer_utils.core.api.misc.server.requests.hive_mind;

import dev.l3g7.griefer_utils.core.api.misc.server.Request;

import static dev.l3g7.griefer_utils.core.api.misc.Constants.HIVEMIND_URL;

public class BlockOfTheDayRequest extends Request<Void> {

	private final String item;
	private final long timestamp;

	public BlockOfTheDayRequest(String item, long timestamp) {
		super(HIVEMIND_URL, "/hive_mind/block_of_the_day");

		this.item = item;
		this.timestamp = timestamp;
	}

	@Override
	protected Void parseResponse(Response response) {
		return null;
	}

}
