package com.liang.example.shell;

import java.util.List;

/**
 * Results of running a command in a shell. Results contain stdout, stderr, and the exit status.
 *
 * @author liangyuyin
 * @since 2019/6/28
 */
public class CommandResult implements ShellExitCode {

    private static String toString(List<String> lines) {
        StringBuilder sb = new StringBuilder();
        if (lines != null) {
            String emptyOrNewLine = "";
            for (String line : lines) {
                sb.append(emptyOrNewLine).append(line);
                emptyOrNewLine = "\n";
            }
        }
        return sb.toString();
    }

    public final List<String> stdout;
    public final List<String> stderr;
    public final int exitCode;

    public CommandResult(List<String> stdout, List<String> stderr, int exitCode) {
        this.stdout = stdout;
        this.stderr = stderr;
        this.exitCode = exitCode;
    }

    public boolean isSuccessful() {
        return exitCode == SUCCESS;
    }

    public String getStdoutString() {
        return toString(stdout);
    }

    public List<String> getStdout() {
        return stdout;
    }

    public String getStderrString() {
        return toString(stderr);
    }

    public List<String> getStderr() {
        return stderr;
    }

    @Override public String toString() {
        return stdout.size() > 0 ? toString(stdout) : toString(stderr);
    }
}
