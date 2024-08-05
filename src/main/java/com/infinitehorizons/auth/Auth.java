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

public class Auth {

    private final JwtBuilder jwtEncoder;
    private final JwtParser jwtVerifier;
    private final long jwtExpiration;

    public Auth() throws NoSuchAlgorithmException, InvalidKeyException {
        ConfigLoader configLoader = new ConfigLoader();
        String key = configLoader.getJwtSecretKey();
        this.jwtExpiration = configLoader.getJwtExpiration();

        SecretKeySpec keySpec = new SecretKeySpec(key.getBytes(StandardCharsets.UTF_8), SignatureAlgorithm.HS512.getJcaName());
        jwtEncoder = Jwts.builder().signWith(keySpec);
        jwtVerifier = (JwtParser) Jwts.parser().setSigningKey(keySpec);
    }

    public String encode(String subject) {
        return jwtEncoder.setExpiration(new Date(System.currentTimeMillis() + jwtExpiration)).setSubject(subject).compact();
    }

    public Claims verify(String encoded) {
        try {
            return jwtVerifier.parseClaimsJws(encoded).getBody();
        } catch (JwtException e) {
            return null;
        }
    }

    public static String toHex(byte[] data) {
        StringBuilder builder = new StringBuilder(data.length * 2);

        for (byte b : data) {
            builder.append(String.format("%02x", b));
        }

        return builder.toString();
    }
}
