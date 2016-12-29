package com.github.bachelorpraktikum.dbvisualization;

import java.net.URL;
import java.util.Objects;

import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;

/**
 * Represents a data source for a graph. The way the {@link #getUrl() url} is loaded should be
 * determined by another class based on the {@link #getType() type} of this data source.
 */
@ParametersAreNonnullByDefault
public final class DataSource {
    @Nonnull
    private final Type type;
    @Nonnull
    private final URL url;

    /**
     * The type of a data source.
     */
    public enum Type {
        /**
         * An output log-file from an ABS-simulation.
         */
        LOG_FILE,
        DATABASE
    }

    /**
     * Creates a new instance.
     *
     * @param type the type of the data source
     * @param url  the URL to the resource
     * @throws NullPointerException if type or url is null
     */
    public DataSource(Type type, URL url) {
        this.type = Objects.requireNonNull(type);
        this.url = Objects.requireNonNull(url);
    }

    /**
     * Gets the type of this data source.
     *
     * @return the type
     */
    @Nonnull
    public Type getType() {
        return type;
    }

    /**
     * Gets the URL this source can be accessed from.
     *
     * @return the URL
     */
    @Nonnull
    public URL getUrl() {
        return url;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;

        DataSource that = (DataSource) obj;

        if (type != that.type) return false;
        return url.toExternalForm().equals(that.url.toExternalForm());
    }

    @Override
    public int hashCode() {
        int result = type.hashCode();
        result = 31 * result + url.toExternalForm().hashCode();
        return result;
    }

    @Override
    public String toString() {
        return "DataSource{"
                + "type=" + type
                + ", url=" + url
                + '}';
    }
}
