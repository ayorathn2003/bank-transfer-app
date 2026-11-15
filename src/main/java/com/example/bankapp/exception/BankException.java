// BankException.java
package com.example.bankapp.exception;

public class BankException extends RuntimeException {
    public BankException(String message) {
        super(message);
    }
}
/*
// AccountNotFoundException.java
package com.example.bankapp.exception;

public class AccountNotFoundException extends BankException {
    public AccountNotFoundException(String message) {
        super(message);
    }
}

// InsufficientBalanceException.java
package com.example.bankapp.exception;

public class InsufficientBalanceException extends BankException {
    public InsufficientBalanceException(String message) {
        super(message);
    }
}*/