package com.github.bachelorpraktikum.dbvisualization.datasource;

import com.github.bachelorpraktikum.dbvisualization.model.Context;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

/**
 * Can be used to start a simulation subprocess and parse its output.
 */
public class SubprocessSource extends InputParserSource {

    private final Process process;

    /**
     * Creates a new {@link Context} and starts the process specified by the command with the
     * specified arguments.
     *
     * <p>Parses the process output until there is no output for {@link #DEFAULT_START_TIMEOUT}
     * milliseconds.</p>
     *
     * @param command the command to run. e.g. "echo"
     * @param args the arguments for the command. e.g. "Hello world!"
     * @throws IOException if the process can't be started
     */
    public SubprocessSource(String command, String... args) throws IOException {
        this.process = createProcess(Objects.requireNonNull(command), args);

        // TODO create constructor with the first timeout explicitly given
        // Listen until first output is processed
        // (so Node / Edge / Element / Train declarations are processed before MainWindow is shown)
        listenToInput(process.getInputStream(), DEFAULT_START_TIMEOUT, TimeUnit.MILLISECONDS);
    }

    /**
     * Gets the input stream of the subprocess (the output of the subprocess).
     *
     * @return an input stream
     */
    protected InputStream getInputStream() {
        return process.getInputStream();
    }

    private Process createProcess(String command, String... args) throws IOException {
        List<String> commands = new ArrayList<>(args.length + 1);
        commands.add(command);
        Collections.addAll(commands, args);

        return new ProcessBuilder(commands).start();
    }

    @Override
    public void close() throws IOException {
        super.close();
        try {
            // TODO this fails to shut down the model completely.
            // maybe a REST call to call exit could be implemented?
            process.destroyForcibly().waitFor();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
