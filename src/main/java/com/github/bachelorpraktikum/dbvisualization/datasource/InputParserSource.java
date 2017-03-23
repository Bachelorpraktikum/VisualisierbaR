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

class InputParserSource implements DataSource {

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
     * Listens to subprocess output and parses it until there is no new output for the specified
     * time.
     *
     * @param inputStream the input stream to listen to
     * @param timeout the timeout
     * @param unit the unit of the timeout
     */
    synchronized void listenToInput(InputStream inputStream, long timeout, TimeUnit unit) {
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

    private synchronized void launchPipingThread(
        BufferedReader reader,
        BufferedWriter writer,
        long timeout,
        TimeUnit unit) {
        Runnable readWhileReady = () -> {
            try {
                while (reader.ready()) {
                    String line = reader.readLine();
                    if (line != null) {
                        writer.write(line);
                        writer.write('\n');
                    }
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

        readWhileReady.run();
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
