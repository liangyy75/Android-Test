package com.liang.example.shell;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.List;

/**
 * Thread utility class continuously reading from an InputStream
 *
 * @author liangyuyin
 * @since 2019/6/28
 */
public class StreamGobbler extends Thread {

    /**
     * Line callback interface
     */
    public interface OnLineListener {
        void onLine(String line);
    }

    private final BufferedReader reader;
    private List<String> writer;
    private OnLineListener listener;

    public StreamGobbler(InputStream inputStream, List<String> outputList) {
        reader = new BufferedReader(new InputStreamReader(inputStream));
        writer = outputList;
    }

    public StreamGobbler(InputStream inputStream, OnLineListener onLineListener) {
        reader = new BufferedReader(new InputStreamReader(inputStream));
        listener = onLineListener;
    }

    @Override public void run() {
        try {
            String line;
            while ((line = reader.readLine()) != null) {
                if (writer != null) {
                    writer.add(line);
                }
                if (listener != null) {
                    listener.onLine(line);
                }
            }
        } catch (IOException e) {
            // reader probably closed, expected exit condition
        }

        try {
            reader.close();
        } catch (IOException ignored) {
        }
    }

}