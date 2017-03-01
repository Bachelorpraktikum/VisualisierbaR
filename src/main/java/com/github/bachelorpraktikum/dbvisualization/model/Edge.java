package com.github.bachelorpraktikum.dbvisualization.model;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.WeakHashMap;
import java.util.logging.Logger;
import javafx.beans.property.Property;
import javafx.beans.property.SimpleObjectProperty;
import javafx.scene.shape.Line;
import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;
import javax.annotation.concurrent.Immutable;

/**
 * Represents an edge between 2 {@link Node nodes}.<br>
 * Edges are immutable. There is only one instance of Edge per name per {@link Context}.
 */
@Immutable
@ParametersAreNonnullByDefault
public final class Edge implements GraphObject<Line> {

    private static final Logger log = Logger.getLogger(Edge.class.getName());

    @Nonnull
    private final String name;
    private final int length;
    @Nonnull
    private final Node node1;
    @Nonnull
    private final Node node2;
    private final Property<VisibleState> stateProperty;

    private Edge(String name, int length, Node node1, Node node2) {
        this.name = Objects.requireNonNull(name);
        this.length = length;

        this.node1 = Objects.requireNonNull(node1);
        this.node2 = Objects.requireNonNull(node2);
        node1.addEdge(this);
        node2.addEdge(this);

        this.stateProperty = new SimpleObjectProperty<>();
    }

    /**
     * Manages all instances of {@link Edge}.
     */
    @ParametersAreNonnullByDefault
    public static final class Factory {

        private static final int INITIAL_EDGES_CAPACITY = 512;
        private static final Map<Context, Factory> instances = new WeakHashMap<>();

        @Nonnull
        private final Map<String, Edge> edges;

        private static Factory getInstance(Context context) {
            if (context == null) {
                throw new NullPointerException("context is null");
            }
            return instances.computeIfAbsent(context, g -> new Factory());
        }

        private Factory() {
            this.edges = new LinkedHashMap<>(INITIAL_EDGES_CAPACITY);
        }

        /**
         * Potentially creates a new instance of {@link Edge}.
         *
         * @param name the unique name of the edge
         * @param length the length in meters
         * @param node1 the first declared node at the end of the edge
         * @param node2 the second declared node at the end of the edge
         * @return an instance of Edge
         * @throws NullPointerException if at least one of the arguments is null
         * @throws IllegalArgumentException if an edge with the same name but different parameters
         * already exists
         */
        @Nonnull
        public Edge create(String name, int length, Node node1, Node node2) {
            Edge result = edges.computeIfAbsent(Objects.requireNonNull(name), edgeName ->
                new Edge(edgeName, length, node1, node2)
            );

            if (result.getLength() != length
                || !result.getNode1().equals(node1)
                || !result.getNode2().equals(node2)) {
                String edgeFormat = "(length: %d, node1: %s, node2: %s)";
                String message = "Edge with name: %s already exists:\n"
                    + edgeFormat + ", tried to recreate with following arguments:\n"
                    + edgeFormat;
                message = String.format(message, name, length, node1, node2,
                    result.getLength(), result.getNode1(), result.getNode2());
                throw new IllegalArgumentException(message);
            }

            return result;
        }

        /**
         * Gets the {@link Edge} with the given unique name.
         *
         * @param name the edge's name
         * @return the edge
         * @throws NullPointerException if name is null
         * @throws IllegalArgumentException if no edge with this name exists
         */
        @Nonnull
        public Edge get(String name) {
            Edge edge = edges.get(Objects.requireNonNull(name));
            if (edge == null) {
                throw new IllegalArgumentException("unknown edge: " + name);
            }
            return edge;
        }

        /**
         * Gets all {@link Edge edges} that exist in this context.
         *
         * @return all edges
         */
        @Nonnull
        public Collection<Edge> getAll() {
            return Collections.unmodifiableCollection(edges.values());
        }
    }

    /**
     * Gets the {@link Factory} instance for the given {@link Context}.
     *
     * @param context the context
     * @return the factory
     * @throws NullPointerException if the context is null
     */
    @Nonnull
    public static Factory in(Context context) {
        return Factory.getInstance(context);
    }

    /**
     * Gets the length of this {@link Edge}.
     *
     * @return the length in meters
     */
    public int getLength() {
        return length;
    }

    /**
     * Gets the node declared first.
     *
     * @return the first node
     */
    @Nonnull
    public Node getNode1() {
        return node1;
    }

    /**
     * Gets the node declared second.
     *
     * @return the second node
     */
    @Nonnull
    public Node getNode2() {
        return node2;
    }

    @Nonnull
    public Node getOtherNode(Node node) {
        if (getNode1().equals(node)) {
            return getNode2();
        } else if (getNode2().equals(node)) {
            return getNode1();
        } else {
            throw new IllegalArgumentException();
        }
    }

    public Node getCommonNode(Edge other) {
        Node n1 = getNode1();
        Node n2 = getNode2();

        if (n1.equals(other.getNode1()) || n1.equals(other.getNode2())) {
            return n1;
        } else if (n2.equals(other.getNode1()) || n2.equals(other.getNode2())) {
            return n2;
        } else {
            throw new IllegalArgumentException();
        }
    }

    @Override
    @Nonnull
    public String getName() {
        return name;
    }

    @Override
    public Line createShape() {
        return new Line();
    }

    @Override
    public Property<VisibleState> visibleStateProperty() {
        return stateProperty;
    }

    @Override
    public String toString() {
        return "Edge{"
            + "name='" + name + '\''
            + ", length=" + length
            + ", node1=" + node1
            + ", node2=" + node2
            + '}';
    }
}
