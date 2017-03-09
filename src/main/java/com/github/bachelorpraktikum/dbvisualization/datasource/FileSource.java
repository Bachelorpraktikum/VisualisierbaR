package com.github.bachelorpraktikum.dbvisualization.datasource;

import com.github.bachelorpraktikum.dbvisualization.logparser.GraphParser;
import com.github.bachelorpraktikum.dbvisualization.model.Context;
import java.io.File;
import java.io.IOException;
import javafx.stage.FileChooser;

public class FileSource implements DataSource {

    private final File file;
    private final Context context;

    public FileSource(File file) throws IOException {
        this.file = file;
        this.context = parseFile();
    }

    private Context parseFile() throws IOException {
        return new GraphParser().parse(file.getPath());
    }

    @Override
    public Context getContext() {
        return context;
    }

    @Override
    public void close() {
    }
}
