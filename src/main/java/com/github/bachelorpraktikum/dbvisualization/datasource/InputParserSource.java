package com.github.bachelorpraktikum.dbvisualization.datasource;

import com.github.bachelorpraktikum.dbvisualization.logparser.GraphParser;
import com.github.bachelorpraktikum.dbvisualization.model.Context;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import javax.annotation.Nonnull;

public class InputParserSource implements DataSource {

    static final long DEFAULT_START_TIMEOUT = 1000;

    private final Context context;
    private final ScheduledExecutorService scheduler;
    private final GraphParser graphParser;

    /**
     * Callers of this constructor should call {@link #listenToInput(InputStream, long, TimeUnit)}
     * once after finished initialization.
     */
    InputParserSource() {
        this.context = new Context();
        this.scheduler = Executors.newSingleThreadScheduledExecutor();
        this.graphParser = new GraphParser();
    }

    /**
     * Creates a new InputParserSource and listens to the input once until there is not input for
     * {@link #DEFAULT_START_TIMEOUT} milliseconds.
     *
     * @param inputStream the input stream to listen to
     */
    public InputParserSource(InputStream inputStream) {
        this();
        listenToInput(inputStream, DEFAULT_START_TIMEOUT, TimeUnit.MILLISECONDS);
    }

    /**
     * Listens to subprocess output and parses it until there is no new output for the specified
     * time.
     *
     * @param inputStream the input stream to listen to
     * @param timeout the timeout
     * @param unit the unit of the timeout
     */
    void listenToInput(InputStream inputStream, long timeout, TimeUnit unit) {
        BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));

        try (PipedOutputStream parserOutStream = new PipedOutputStream();
            PipedInputStream parserInStream = new PipedInputStream(parserOutStream)) {
            BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(parserOutStream));
            // launch the piping thread which pipes the process output into the parser input
            launchPipingThread(reader, writer, timeout, unit);
            // this method runs / blocks until the piping thread closes the parserInputStream
            graphParser.parse(parserInStream, context);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void readOnce(BufferedReader reader, BufferedWriter writer) throws IOException {
        String line = reader.readLine();
        if (line != null) {
            writer.write(line);
            writer.newLine();
        }
    }

    private void launchPipingThread(
        BufferedReader reader,
        BufferedWriter writer,
        long timeout,
        TimeUnit unit) throws IOException {

        Runnable readWhileReady = () -> {
            try {
                while (reader.ready()) {
                    readOnce(reader, writer);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        };

        Runnable readAndRescheduleUntilTimeout = new Runnable() {
            @Override
            public void run() {
                try {
                    if (!reader.ready()) {
                        writer.close();
                    } else {
                        readWhileReady.run();
                        scheduler.schedule(this, timeout, unit);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        };

        readOnce(reader, writer);
        scheduler.schedule(readAndRescheduleUntilTimeout, timeout, unit);
    }

    @Nonnull
    public Context getContext() {
        return context;
    }

    @Override
    public void close() throws IOException {
        scheduler.shutdownNow();
    }
}
