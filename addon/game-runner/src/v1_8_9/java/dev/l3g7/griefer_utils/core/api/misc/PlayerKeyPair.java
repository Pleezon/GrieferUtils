/*
 * This file is part of GrieferUtils (https://github.com/L3g7/GrieferUtils).
 * Copyright (c) L3g7.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 */

package dev.l3g7.griefer_utils.core.api.misc;

import com.google.gson.annotations.SerializedName;
import dev.l3g7.griefer_utils.core.api.util.IOUtil;

import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URI;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.time.Instant;
import java.util.Base64;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

public class PlayerKeyPair {

	private KeyPair keyPair;

	@SerializedName("publicKeySignatureV2")
	private String publicKeySignature;

	@SerializedName("expiresAt")
	private String expirationTime;

	private static final ConcurrentHashMap<String, CompletableFuture<PlayerKeyPair>> cache = new ConcurrentHashMap<>();

	public static CompletableFuture<PlayerKeyPair> getPlayerKeyPair(String authToken) {
		return cache.computeIfAbsent(authToken, key -> CompletableFuture.supplyAsync(() -> {
			try {
				HttpURLConnection c = (HttpURLConnection) URI.create("https://api.minecraftservices.com/player/certificates").toURL().openConnection();
				c.setRequestMethod("POST");
				c.setRequestProperty("Content-Length", "0");
				c.setRequestProperty("Content-Type", "application/json; charset=utf-8");
				c.setRequestProperty("Authorization", "Bearer " + authToken);
				c.setDoOutput(true);
				c.getOutputStream().close();

				// Check if request failed (e.g. 401 Unauthorized, 429 Too Many Requests)
				if (c.getResponseCode() >= 400)
					return null;

				return IOUtil.gson.fromJson(new InputStreamReader(c.getInputStream()), PlayerKeyPair.class);
			} catch (IOException e) {
				return null;
			}
		}));
	}

	public PrivateKey getPrivateKey() throws NoSuchAlgorithmException, InvalidKeySpecException {
		return KeyFactory.getInstance("RSA").generatePrivate(new PKCS8EncodedKeySpec(getRawPrivateKey()));
	}

	public byte[] getRawPrivateKey() {
		String privateKey = keyPair.privateKey.replaceAll("^-----BEGIN RSA PRIVATE KEY-----|-----END RSA PRIVATE KEY-----$|\r|\n", "");
		return Base64.getMimeDecoder().decode(privateKey);
	}

	public String getPublicKey() {
		return keyPair.publicKey.replaceAll("^-----BEGIN RSA PUBLIC KEY-----|-----END RSA PUBLIC KEY-----$|\r|\n", "");
	}

	public String getPublicKeySignature() {
		return publicKeySignature;
	}

	public long getExpirationTime() {
		return Instant.parse(expirationTime).toEpochMilli();
	}

	private static class KeyPair {
		private String privateKey;
		private String publicKey;
	}

}