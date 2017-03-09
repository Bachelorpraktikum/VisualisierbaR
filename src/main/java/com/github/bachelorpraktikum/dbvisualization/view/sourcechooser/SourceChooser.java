package com.github.bachelorpraktikum.dbvisualization.view.sourcechooser;

import com.github.bachelorpraktikum.dbvisualization.datasource.DataSource;
import java.io.IOException;
import javafx.beans.value.ObservableBooleanValue;
import javax.annotation.Nonnull;

/**
 * Represents a chooser for a data source.<br>The user can input a URL by which the data source can
 * be accessed.<br>The chooser does not check whether that is the case.<br>The data which can be
 * retrieved from the source has to be compatible with the described {@link
 * com.github.bachelorpraktikum.dbvisualization.model model}
 */
interface SourceChooser<T extends DataSource> {

    /**
     * Returns the resource.
     *
     * @return the resource
     */
    @Nonnull
    T getResource() throws IOException;

    @Nonnull
    ObservableBooleanValue inputChosen();
}
