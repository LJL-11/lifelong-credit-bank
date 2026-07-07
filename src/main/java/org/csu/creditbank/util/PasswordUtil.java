package org.csu.creditbank.util;

import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.HexFormat;

public final class PasswordUtil {

    private static final SecureRandom RANDOM = new SecureRandom();

    private PasswordUtil() {}

    /** SHA-256(salt + rawPassword)，返回 "salt:hash" 格式 */
    public static String hash(String rawPassword) {
        try {
            byte[] salt = new byte[16];
            RANDOM.nextBytes(salt);
            String saltHex = HexFormat.of().formatHex(salt);
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            md.update(saltHex.getBytes());
            byte[] digest = md.digest(rawPassword.getBytes());
            return saltHex + ":" + HexFormat.of().formatHex(digest);
        } catch (Exception e) {
            throw new RuntimeException("密码加密失败", e);
        }
    }

    /** 验证原始密码与存储的 "salt:hash" 是否匹配 */
    public static boolean verify(String rawPassword, String stored) {
        if (stored == null || !stored.contains(":")) {
            return false;
        }
        try {
            String[] parts = stored.split(":", 2);
            String saltHex = parts[0];
            String expectedHash = parts[1];
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            md.update(saltHex.getBytes());
            String actualHash = HexFormat.of().formatHex(md.digest(rawPassword.getBytes()));
            return expectedHash.equals(actualHash);
        } catch (Exception e) {
            return false;
        }
    }
}
