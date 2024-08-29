package xyz.dowob.blogspring.junit;

import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.TestFactory;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import xyz.dowob.blogspring.Exceptions.Userdata_UpdateException;
import xyz.dowob.blogspring.functions.UserInspection;
import xyz.dowob.blogspring.repository.UserRepository;

import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class ComponentTests {
    private final UserRepository userRepository = mock(UserRepository.class);

    private final UserInspection userInspection = new UserInspection(userRepository);

    @ParameterizedTest
    @CsvSource({"validUsername1, true", "Invalid@Username, false", "anotherValidUser2, true", "user_with_illegal_chars!, false", "user123, true", "Invalid space, false"})
    public void testIsValidUsername1(String username, boolean isValid) {
        if (isValid) {
            when(userRepository.findByUsername(username)).thenReturn(java.util.Optional.empty());
            try {
                userInspection.isValidUsername(username);
            } catch (Userdata_UpdateException e) {
                throw new RuntimeException(e);
            }
        } else {
            assertThrows(Userdata_UpdateException.class, () -> {
                userInspection.isValidUsername(username);
            });
        }
    }

    @TestFactory
    public List<DynamicTest> dynamicUserValidationTests() {
        List<String> usernames = generateUsernames();

        return usernames.stream().map(username -> DynamicTest.dynamicTest("Test for username: " + username, () -> {
            if (!userInspection.isValidUsername(username)) {
                assertThrows(Userdata_UpdateException.class, () -> {
                    userInspection.isValidUsername(username);
                });
            } else {
                assertDoesNotThrow(() -> userInspection.isValidUsername(username));
            }
        })).collect(Collectors.toList());
    }

    private List<String> generateUsernames() {
        return Stream.generate(this::generateRandomUsername).limit(100).collect(Collectors.toList());
    }
    private String generateRandomUsername() {
        //String characters = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789!@#$%^&*()_+{}[]|\\:;\"'<>,.?/`~";
        String characters = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
        StringBuilder username = new StringBuilder();
        Random random = new Random();
        int length = random.nextInt(4,20) + 1;
        for (int i = 0; i < length; i++) {
            username.append(characters.charAt(random.nextInt(characters.length())));
        }
        return username.toString();
    }
}