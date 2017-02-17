package com.github.bachelorpraktikum.dbvisualization;

import java.net.URI;
import java.util.Objects;
import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;

/**
 * Represents a data source for a graph. The way the {@link #getUri() uri} is loaded should be
 * determined by another class based on the {@link #getType() type} of this data source.
 */
@ParametersAreNonnullByDefault
public final class DataSource {

    @Nonnull
    private final Type type;
    @Nonnull
    private final URI uri;

    /**
     * The type of a data source.
     */
    public enum Type {
        /**
         * An output log-file from an ABS-simulation.
         */
        LOG_FILE,
        /**
         * A database which describes a Graph without events
         */
        DATABASE
    }

    /**
     * Creates a new instance.
     *
     * @param type the type of the data source
     * @param uri the URL to the resource
     * @throws NullPointerException if type or uri is null
     */
    public DataSource(Type type, URI uri) {
        this.type = Objects.requireNonNull(type);
        this.uri = Objects.requireNonNull(uri);
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
    public URI getUri() {
        return uri;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }

        DataSource that = (DataSource) obj;

        return type == that.type && uri.equals(that.uri);
    }

    @Override
    public int hashCode() {
        int result = type.hashCode();
        result = 31 * result + uri.hashCode();
        return result;
    }

    @Override
    public String toString() {
        return "DataSource{"
            + "type=" + type
            + ", uri=" + uri
            + '}';
    }
}
