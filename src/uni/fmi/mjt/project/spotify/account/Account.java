package uni.fmi.mjt.project.spotify.account;

import uni.fmi.mjt.project.spotify.exception.account.InvalidEmailFormatException;

import java.util.Objects;

public record Account(String email, String password) {
    private static final String EMAIL_VALIDATION_REGEX = "[a-zA-z][a-zA-z0-9.\\-_]+[a-zA-z0-9]@[a-z]+\\.[a-z]+";

    public Account {
        this.checkEmailValidFormat(email);
    }

    public static Account create(String line) {
        final String separator = " ";
        String[] split = line.split(separator);

        return new Account(split[0], split[1]);
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }

        if (other == null || getClass() != other.getClass()) {
            return false;
        }

        Account account = (Account) other;
        return Objects.equals(this.email, account.email);
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.email);
    }

    private void checkEmailValidFormat(String email) {
        if (!email.matches(EMAIL_VALIDATION_REGEX)) {
            throw new InvalidEmailFormatException(
                    "Trying to login\\register with invalid email format! " + System.lineSeparator() +
                            "Valid email format is user@smt.domain, " +
                            "where user can only contain letters, numbers, '.', '-', '_' and " +
                            "always starts with a letter and ends with a letter or number!");
        }
    }
}
