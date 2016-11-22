package com.github.bachelorpraktikum.dbvisualization.view;

import com.github.bachelorpraktikum.dbvisualization.DataSource;

import java.net.URL;

import javafx.beans.property.ReadOnlyProperty;

interface SourceChooser {
    URL getResourceURL();

    ReadOnlyProperty<URL> resourceURLProperty();

    String getRootPaneId();

    DataSource.Type getResourceType();
}
