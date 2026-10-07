package com.queue.util;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import java.security.SecureRandom;
import java.security.MessageDigest;
import java.util.Base64;

public final class Passwords {
    private static final int ITERATIONS = 210000;
    private Passwords() {}
    public static String hash(String password) {
        byte[] salt = new byte[16]; new SecureRandom().nextBytes(salt);
        return "pbkdf2$" + ITERATIONS + "$" + Base64.getEncoder().encodeToString(salt) + "$" + Base64.getEncoder().encodeToString(derive(password, salt, ITERATIONS));
    }
    public static boolean matches(String password, String stored) {
        if (password == null || stored == null) return false;
        // Existing team seed accounts are migrated after their first successful login.
        if (!stored.startsWith("pbkdf2$")) return MessageDigest.isEqual(password.getBytes(java.nio.charset.StandardCharsets.UTF_8), stored.getBytes(java.nio.charset.StandardCharsets.UTF_8));
        try {
            String[] parts = stored.split("\\$");
            return MessageDigest.isEqual(derive(password, Base64.getDecoder().decode(parts[2]), Integer.parseInt(parts[1])), Base64.getDecoder().decode(parts[3]));
        } catch (Exception e) { return false; }
    }
    private static byte[] derive(String password, byte[] salt, int iterations) {
        PBEKeySpec spec = new PBEKeySpec(password.toCharArray(), salt, iterations, 256);
        try { return SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256").generateSecret(spec).getEncoded(); }
        catch (Exception e) { throw new IllegalStateException("Password hashing unavailable.", e); }
        finally { spec.clearPassword(); }
    }
}
