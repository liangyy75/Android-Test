package com.liang.example.shell;

import java.io.IOException;

/**
 * Exception thrown when a shell could not be opened.
 *
 * @author liangyuyin
 * @since 2019/6/28
 */
public class ShellNotFoundException extends IOException {

    public ShellNotFoundException(String detailMessage) {
        super(detailMessage);
    }

    public ShellNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }

}
