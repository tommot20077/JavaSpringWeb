package xyz.dowob.blogspring.functions;

import de.mkammerer.argon2.Argon2;
import de.mkammerer.argon2.Argon2Factory;

public class UserHashMethod {
    public static String argonMethod(String original) {
        Argon2 argon2 = Argon2Factory.create(Argon2Factory.Argon2Types.ARGON2id);
        char[] originalChars = original.toCharArray();
        try {
            return argon2.hash(2, 1024 * 1024, 1, originalChars);
        } finally {
            argon2.wipeArray(originalChars);
        }
    }

    public static boolean argonMethodVerifyInRepository(String inRepository, String needToVerify) {
        Argon2 argon2 = Argon2Factory.create(Argon2Factory.Argon2Types.ARGON2id);
        char[] passwordChars = needToVerify.toCharArray();
        try {
            return argon2.verify(inRepository, passwordChars);
        } finally {
            argon2.wipeArray(passwordChars);
        }
    }
}
