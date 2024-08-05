package com.infinitehorizons.auth;

import com.infinitehorizons.config.ConfigLoader;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtBuilder;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.JwtParser;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;

import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Date;

/**
 * Manages JSON Web Token (JWT) encoding and verification.
 * <p>
 * Uses the HS512 algorithm for secure JWT handling.
 */
public class Auth {

    private final JwtBuilder jwtEncoder;
    private final JwtParser jwtVerifier;
    private final long jwtExpiration;

    /**
     * Constructs a new {@code Auth} instance for handling JWT operations.
     *
     * @throws NoSuchAlgorithmException if the algorithm for key specification is invalid.
     * @throws InvalidKeyException      if the key is invalid.
     */
    public Auth() throws NoSuchAlgorithmException, InvalidKeyException {
        ConfigLoader configLoader = new ConfigLoader("application.properties");

        // Load or create properties if not present
        String key = configLoader.getProperty("security.jwt.secret-key");
        if (key == null) {
            key = "defaultSecretKey";
            configLoader.setProperty("security.jwt.secret-key", key);
        }

        this.jwtExpiration = Long.parseLong(configLoader.getProperty("security.jwt.expiration", "3600000"));

        configLoader.saveProperties();

        SecretKeySpec keySpec = new SecretKeySpec(key.getBytes(StandardCharsets.UTF_8), SignatureAlgorithm.HS512.getJcaName());
        jwtEncoder = Jwts.builder().signWith(keySpec);
        jwtVerifier = Jwts.parser().setSigningKey(keySpec).build();
    }

    /**
     * Encodes the given subject into a JWT token.
     *
     * @param subject the subject to encode.
     * @return a JWT token as a {@link String}.
     */
    public String encode(String subject) {
        return jwtEncoder
                .setExpiration(new Date(System.currentTimeMillis() + jwtExpiration))
                .setSubject(subject)
                .compact();
    }

    /**
     * Verifies and decodes the given JWT token.
     *
     * @param encoded the encoded JWT token.
     * @return the decoded {@link Claims}, or {@code null} if verification fails.
     */
    public Claims verify(String encoded) {
        try {
            return jwtVerifier.parseClaimsJws(encoded).getBody();
        } catch (JwtException e) {
            return null;
        }
    }

    /**
     * Converts a byte array into a hexadecimal string.
     *
     * @param data the byte array to convert.
     * @return a hexadecimal {@link String} representation of the byte array.
     */
    public static String toHex(byte[] data) {
        StringBuilder builder = new StringBuilder(data.length * 2);

        for (byte b : data) {
            builder.append(String.format("%02x", b));
        }

        return builder.toString();
    }
}
