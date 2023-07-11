package uni.fmi.mjt.project.spotify.account;

import org.junit.jupiter.api.Test;
import uni.fmi.mjt.project.spotify.exception.account.InvalidEmailFormatException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class AccountTest {
    @Test
    void testAccountInvalidEmailNoLeadingLetter() {
        assertThrows(InvalidEmailFormatException.class, () -> new Account("123@abv.bg", "123"),
                "Throws InvalidEmailFormatException when trying to use an email without leading letter");
    }

    @Test
    void testAccountInvalidEmailNoDomain() {
        assertThrows(InvalidEmailFormatException.class, () -> new Account("asd@abv.", "123"),
                "Throws InvalidEmailFormatException when trying to use an email without domain");
    }

    @Test
    void testAccountInvalidEmailWithoutAtSign() {
        assertThrows(InvalidEmailFormatException.class, () -> new Account("asdabv.bg", "123"),
                "Throws InvalidEmailFormatException when trying to use an email without at sign");
    }

    @Test
    void testAccountInvalidEmailShortUsername() {
        assertThrows(InvalidEmailFormatException.class, () -> new Account("as@bv.bg", "123"),
                "Throws InvalidEmailFormatException when trying to use an email with short username");
    }

    @Test
    void testAccountInvalidEmail() {
        assertThrows(InvalidEmailFormatException.class, () -> new Account("asd@.bg", "123"),
                "Throws InvalidEmailFormatException when trying to use an invalid email");
    }

    @Test
    void testAccountInvalidEmailWithIllegalSymbols() {
        assertThrows(InvalidEmailFormatException.class, () -> new Account("asd@@abv.bg", "123"),
                "Throws InvalidEmailFormatException when trying to use an email with illegal symbols");

        assertThrows(InvalidEmailFormatException.class, () -> new Account("a!d@abv.bg", "123"),
                "Throws InvalidEmailFormatException when trying to use an email with illegal symbols");
    }

    @Test
    void testCreate() {
        Account account = Account.create("asd_as@abv.bg  123");
        Account expected = new Account("asd_as@abv.bg", "123");

        assertEquals(expected, account, "Correctly creates an account from a string");
    }
}
