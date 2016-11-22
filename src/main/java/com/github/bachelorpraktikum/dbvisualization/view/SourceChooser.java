package com.github.bachelorpraktikum.dbvisualization.view;

import java.net.URL;

import javafx.beans.property.ReadOnlyProperty;

interface SourceChooser {
    URL getResourceURL();

    ReadOnlyProperty<URL> resourceURLProperty();

    String getRootPaneId();
}
