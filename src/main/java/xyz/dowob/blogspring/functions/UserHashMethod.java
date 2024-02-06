package xyz.dowob.blogspring.functions;

import de.mkammerer.argon2.Argon2;
import de.mkammerer.argon2.Argon2Factory;

public class UserHashMethod {
    public static String hashPassword(String password) {
        Argon2 argon2 = Argon2Factory.create(Argon2Factory.Argon2Types.ARGON2id);
        char[] passwordChars = password.toCharArray();
        try {
            return argon2.hash(2, 1024 * 1024, 1, passwordChars);
        } finally {
            argon2.wipeArray(passwordChars);
        }
    }
}
