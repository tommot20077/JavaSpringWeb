package xyz.dowob.blogspring.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import xyz.dowob.blogspring.Exceptions.Userdata_UpdateException;
import xyz.dowob.blogspring.functions.UserInspection;
import xyz.dowob.blogspring.model.User;
import xyz.dowob.blogspring.repository.PersistentLoginRepository;
import xyz.dowob.blogspring.repository.TokenRepository;
import xyz.dowob.blogspring.repository.UserRepository;

import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

class UserServiceTest {
    @InjectMocks
    private UserService userService;

    @Mock
    private UserRepository userRepository;

    @Mock
    private UserInspection userInspection;

    @Mock
    private TokenService tokenService;

    @Mock
    private TokenRepository tokenRepository;

    @Mock
    private PersistentLoginRepository persistentLoginRepository;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @ParameterizedTest
    @MethodSource("provideTestCases")
    void testRegisterUser(
            String username, String password, String confirmPassword, String email, boolean isValidUsername, boolean isValidPassword, String returnedEmail, Class<? extends Exception> expectedException) throws Userdata_UpdateException {

        User user = new User();
        user.setUsername(username);
        user.setPassword(password);
        user.setEmail(email);

        when(userInspection.isValidUsername(username)).thenReturn(isValidUsername);
        when(userInspection.isValidPassword(password, username)).thenReturn(isValidPassword);
        when(userInspection.hasEmail(email)).thenReturn(returnedEmail);

        if (expectedException == null) {
            assertDoesNotThrow(() -> userService.registerUser(user, confirmPassword));
            verify(userRepository, times(1)).save(user);
            if (returnedEmail != null) {
                verify(tokenService, times(1)).sendVerificationEmail(user);
            } else {
                verify(tokenService, never()).sendVerificationEmail(user);
            }
        } else {
            assertThrows(expectedException, () -> userService.registerUser(user, confirmPassword));
            verify(userRepository, never()).save(any(User.class));
            verify(tokenService, never()).sendVerificationEmail(any(User.class));
        }
    }
    private static Stream<Arguments> provideTestCases() {
        //username, password, confirmPassword, email, isValidUsername, isValidPassword, returnedEmail, expectedException
        return Stream.of(
                // 正常情況
                Arguments.of("validUser", "validPass", "validPass", "valid@email.com", true, true, "valid@email.com", null),
                // 用戶名為空
                Arguments.of("", "validPass", "validPass", "valid@email.com", true, true, "valid@email.com", Userdata_UpdateException.class),
                // 密碼為空
                Arguments.of("validUser", "", "validPass", "valid@email.com", true, true, "valid@email.com", Userdata_UpdateException.class),
                // 確認密碼為空
                Arguments.of("validUser", "validPass", "", "valid@email.com", true, true, "valid@email.com", Userdata_UpdateException.class),
                // 密碼不匹配
                Arguments.of("validUser", "validPass", "invalidPass", "valid@email.com", true, true, "valid@email.com", Userdata_UpdateException.class),
                // 無效的用戶名
                //Arguments.of("in", "validPass", "validPass", "valid@email.com", false, true, "valid@email.com", Userdata_UpdateException.class),
                // 無效的密碼
                Arguments.of("validUser", "abc", "invalidPass", "valid@email.com", true, false, "valid@email.com", Userdata_UpdateException.class),
                // 無效的郵箱
                Arguments.of("validUser", "validPass", "validPass", "invalid@email", true, true, null, null),
                // 沒有郵箱
                Arguments.of("validUser", "validPass", "validPass", null, true, true, null, null)
        );
    }
}
