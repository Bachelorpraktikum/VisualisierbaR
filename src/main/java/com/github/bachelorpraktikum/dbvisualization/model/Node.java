package com.github.bachelorpraktikum.dbvisualization.model;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.WeakHashMap;
import java.util.stream.Collectors;

import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;

/**
 * Represents a node on the track.<br>
 * There is only one instance of Node per name per {@link Context}.
 */
@ParametersAreNonnullByDefault
public final class Node {
    @Nonnull
    private final String name;
    @Nonnull
    private final Coordinates coordinates;
    @Nonnull
    private final Set<Edge> edges;
    @Nonnull
    private final Set<Element> elements;

    private Node(String name, Coordinates coordinates) {
        this.name = Objects.requireNonNull(name);
        this.coordinates = Objects.requireNonNull(coordinates);
        this.edges = new HashSet<>();
        this.elements = new HashSet<>();
    }

    /**
     * Manages all instances of {@link Node}.<br>
     * Ensures there is always only one instance of node per name per {@link Context}.
     */
    public static final class Factory {
        private static final int INITIAL_NODES_CAPACITY = 64;
        private static final Map<Context, Factory> instances = new WeakHashMap<>();

        @Nonnull
        private final Map<String, Node> nodes;

        @Nonnull
        private static Factory getInstance(Context context) {
            if (context == null) {
                throw new NullPointerException("context is null");
            }
            return instances.computeIfAbsent(context, g -> new Factory());
        }

        private Factory() {
            this.nodes = new HashMap<>(INITIAL_NODES_CAPACITY);
        }

        /**
         * Potentially creates a new instance of {@link Node}.<br>
         * If an node with the same name already exists, it is returned.
         *
         * @param name        the unique name of this node
         * @param coordinates the {@link Coordinates} of this node
         * @return an element
         * @throws NullPointerException     if either of the arguments is null
         * @throws IllegalArgumentException if an node with this name already exists, but with
         *                                  different coordinates
         */
        @Nonnull
        public Node create(String name, Coordinates coordinates) {
            Node result = nodes.computeIfAbsent(Objects.requireNonNull(name), nodeName ->
                    new Node(nodeName, coordinates
                    ));

            if (!result.getCoordinates().equals(coordinates)) {
                throw new IllegalArgumentException("node with that name already exists, but differently");
            }

            return result;
        }

        /**
         * Gets the {@link Node} with the given unique name.
         *
         * @param name the node's name
         * @return the node instance with this name
         * @throws NullPointerException     if the name is null
         * @throws IllegalArgumentException if there is no node associated with the name
         */
        @Nonnull
        public Node get(String name) {
            Node node = nodes.get(name);
            if (node == null) {
                throw new IllegalArgumentException("unknown node: " + name);
            }
            return node;
        }

        /**
         * Gets all {@link Node} instances in this {@link Context}.
         *
         * @return all nodes
         */
        @Nonnull
        public Collection<Node> getAll() {
            return Collections.unmodifiableCollection(nodes.values());
        }
    }

    /**
     * Gets the {@link Factory} instance for the given {@link Context}.
     *
     * @param context the context
     * @return the factory
     * @throws NullPointerException if context is null
     */
    public static Factory in(Context context) {
        return Factory.getInstance(context);
    }

    void addEdge(Edge edge) {
        edges.add(Objects.requireNonNull(edge));
    }

    /**
     * Gets the unique name of this node in its context.
     *
     * @return the name
     */
    @Nonnull
    public String getName() {
        return name;
    }

    /**
     * Gets the coordinates of this node.
     *
     * @return the coordinates
     */
    @Nonnull
    public Coordinates getCoordinates() {
        return coordinates;
    }

    /**
     * Gets a set of all {@link Edge edges} connected with this node, except the given one.
     *
     * @param edge the edge to exclude from the set.
     * @return the set of edges
     * @throws NullPointerException if edge is null
     */
    @Nonnull
    public Set<Edge> otherEdges(Edge edge) {
        if (edge == null) {
            throw new NullPointerException("edge is null");
        }
        return edges.stream()
                .filter(e -> !e.equals(edge))
                .collect(Collectors.toSet());
    }

    /**
     * Gets all edges connected to this node.
     *
     * @return the edges
     */
    @Nonnull
    public Set<Edge> getEdges() {
        return Collections.unmodifiableSet(edges);
    }

    /**
     * Gets all elements at this node.
     *
     * @return the elements
     */
    @Nonnull
    public Set<Element> getElements() {
        return Collections.unmodifiableSet(elements);
    }

    void addElement(Element element) {
        elements.add(Objects.requireNonNull(element));
    }

    @Override
    public String toString() {
        return "Node{"
                + "name='" + name + '\''
                + ", coordinates=" + coordinates
                + '}';
    }
}
