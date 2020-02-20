/*
 * This file is part of MrFoosball (https://github.com/gesundkrank/mrfoosball).
 * Copyright (c) 2020 Jan Graßegger.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, version 3.
 *
 * This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */

package de.gesundkrank.mrfoosball.slack;

import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import org.apache.commons.codec.binary.Hex;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class RequestVerifier {

    public static final int DEFAULT_TIMESTAMP_EXPIRATION_TIME_IN_MILLIS = 60 * 5 * 1000;

    private final Logger logger;
    private final Mac sha256;
    private final long expirationInMillis;

    public RequestVerifier(final String signingSecret) throws GeneralSecurityException {
        this(signingSecret, DEFAULT_TIMESTAMP_EXPIRATION_TIME_IN_MILLIS);
    }

    public RequestVerifier(final String signingSecret, final long expirationInMillis)
            throws GeneralSecurityException {
        this.expirationInMillis = expirationInMillis;
        this.logger = LogManager.getLogger();

        if (signingSecret == null || signingSecret.length() == 0) {
            logger.error("SigningSecret must not be null or empty");
            throw new RuntimeException("Empty signing secret");
        }

        this.sha256 = Mac.getInstance("HmacSHA256");
        final var key = new SecretKeySpec(signingSecret.getBytes(StandardCharsets.UTF_8),
                                          sha256.getAlgorithm());
        sha256.init(key);
    }

    public boolean verify(final String expectedSignature, final String body,
                          final String timestamp) {
        final var timestampLong = Long.parseLong(timestamp) * 1000;

        if ((System.currentTimeMillis() - timestampLong) > expirationInMillis) {
            logger.warn("Could not verify request. Too old.");
            return false;
        }

        final var requestSignature = calcRequestSignature(body, timestamp);

        if (!requestSignature.equals(expectedSignature)) {
            logger.warn("Could not verify request signature. Expected {}. Calculated {}",
                        expectedSignature, requestSignature);
            return false;
        }

        return true;
    }

    private String calcRequestSignature(final String requestBody, final String timestamp) {

        String baseString = "v0:" + timestamp + ":" + requestBody.strip();

        final var macBytes = sha256.doFinal(baseString.getBytes(StandardCharsets.UTF_8));
        return "v0=" + Hex.encodeHexString(macBytes);
    }
}
