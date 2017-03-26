package com.github.bachelorpraktikum.dbvisualization.model;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Objects;
import java.util.WeakHashMap;
import java.util.function.Function;
import java.util.logging.Logger;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
public final class Messages {

    private static final Logger log = Logger.getLogger(Messages.class.getName());
    private static final Map<Context, Messages> instances = new WeakHashMap<>();

    /**
     * Gets the Messages instance for the specified Context.
     *
     * @param context a Context
     * @return a Messages instance
     * @throws NullPointerException if context is null
     */
    @Nonnull
    public static Messages in(Context context) {
        return instances.computeIfAbsent(Objects.requireNonNull(context), c -> new Messages());
    }

    private final ObservableList<MessageEvent> messageEvents;
    /**
     * key -> endTime of the last call to {@link #fireEventsBetween(Function, int, int)}.
     * value -> index of the next event after the key time
     */
    private IntPair lastEvent;

    private Messages() {
        this.messageEvents = FXCollections.observableList(new ArrayList<>(64));
        this.lastEvent = null;
    }

    /**
     * Gets the list of message events.
     *
     * @return a list of events
     */
    public ObservableList<? extends Event> getEvents() {
        return messageEvents;
    }

    /**
     * <p>Registers a new message.</p>
     *
     * @param time the time at which the message will be shown
     * @param text the content of the message
     * @param node the node at which the message should appear
     * @throws IllegalArgumentException if time is negative
     * @throws NullPointerException if text or node are null
     */
    public void add(int time, String text, Node node) {
        if (time < 0) {
            throw new IllegalArgumentException("time is negative: " + time);
        }
        Objects.requireNonNull(text);
        Objects.requireNonNull(node);
        List<String> warnings = new LinkedList<>();
        if (!messageEvents.isEmpty()) {
            MessageEvent previousEvent = messageEvents.get(messageEvents.size() - 1);
            if (time < previousEvent.getTime()) {
                log.warning(String.format(
                    "Tried to add message event at time %d before previous event at time %d",
                    time,
                    previousEvent.getTime()
                ));
                warnings.add(String.format(
                    "Tried to add at time %d before last event",
                    time
                ));
                time = previousEvent.getTime();
            }
        }
        MessageEvent event = new MessageEvent(time, text, node, warnings);
        messageEvents.add(event);
        lastEvent = null;
    }

    /**
     * Fires all message events between the start time (exclusive) and the end time (inclusive).
     *
     * @param startTime the start time
     * @param endTime the maximum time of the last message event to be fired
     * @return whether any events have been fired
     * @throws IllegalArgumentException if startTime is after endTime
     */
    public boolean fireEventsBetween(Function<Node, javafx.scene.Node> nodeResolver,
        int startTime, int endTime) {
        if (startTime > endTime) {
            throw new IllegalArgumentException("startTime > endTime");
        }
        int startIndex = 0;
        if (lastEvent != null && startTime == lastEvent.getKey()) {
            startIndex = lastEvent.getValue();
        }

        if (startIndex >= messageEvents.size()) {
            lastEvent = null;
            return false;
        }

        boolean fired = false;
        ListIterator<MessageEvent> iterator = messageEvents.listIterator(startIndex);
        while (iterator.hasNext()) {
            MessageEvent event = iterator.next();
            if (event.getTime() <= startTime) {
                continue;
            }
            if (event.getTime() > endTime) {
                lastEvent = new IntPair(endTime, iterator.previousIndex());
                return fired;
            }

            event.fire(nodeResolver);
            fired = true;
        }
        lastEvent = new IntPair(endTime, iterator.nextIndex());
        return fired;
    }

    private static final class IntPair {

        private final int key;
        private final int value;

        IntPair(int key, int value) {
            this.key = key;
            this.value = value;
        }

        int getKey() {
            return key;
        }

        int getValue() {
            return value;
        }
    }
}
