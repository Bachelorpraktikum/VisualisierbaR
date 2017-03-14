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
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class SubprocessSource implements DataSource {

    private final Context context;
    private final Process process;
    private final ScheduledExecutorService scheduler;

    private final GraphParser graphParser;

    public SubprocessSource(String command, String... args) throws IOException {
        this.context = new Context();
        this.process = createProcess(Objects.requireNonNull(command), args);
        this.scheduler = Executors.newSingleThreadScheduledExecutor();

        this.graphParser = new GraphParser();

        Runtime.getRuntime().addShutdownHook(new Thread(this::close));

        // TODO use constants, and create constructor with the first timeout explicitly given
        // Listen until first output is processed
        // (so Node / Edge / Element / Train declarations are processed before MainWindow is shown)
        listenToOutput(600, TimeUnit.MILLISECONDS);
    }

    /**
     * Listens to subprocess output and parses it until there is no new output for the specified
     * timeout.
     *
     * @param timeout the timeout
     * @param unit the unit of the timeout
     */
    synchronized void listenToOutput(long timeout, TimeUnit unit) {
        InputStream inputStream = process.getInputStream();
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
        readWhileReady.run();

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
        scheduler.schedule(readAndRescheduleUntilTimeout, timeout, unit);
    }

    private Process createProcess(String command, String... args) throws IOException {
        List<String> commands = new ArrayList<>(args.length + 1);
        commands.add(command);
        Collections.addAll(commands, args);

        return new ProcessBuilder(commands).start();
    }

    @Override
    public final Context getContext() {
        return context;
    }

    @Override
    public void close() {
        scheduler.shutdownNow();
        try {
            process.destroyForcibly().waitFor();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
