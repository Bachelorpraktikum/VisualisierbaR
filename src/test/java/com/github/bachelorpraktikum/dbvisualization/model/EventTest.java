package com.github.bachelorpraktikum.dbvisualization.model;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javax.annotation.Nonnull;
import org.junit.Test;

public class EventTest {

    private Event createEvent(int time) {
        return new Event() {
            @Override
            public int getTime() {
                return time;
            }

            @Nonnull
            @Override
            public String getDescription() {
                return "time: " + time;
            }

            @Nonnull
            @Override
            public ObservableList<String> getWarnings() {
                return FXCollections.emptyObservableList();
            }
        };
    }

    @Test
    public void testCompareToReflexive() {
        Event event = createEvent(10);
        assertEquals(0, event.compareTo(event));
    }

    @Test
    public void testCompareToTransitive() {
        Event a = createEvent(0);
        Event b = createEvent(5);
        Event c = createEvent(10);

        assertTrue(a.compareTo(b) < 0);
        assertTrue(b.compareTo(c) < 0);
        assertTrue(a.compareTo(c) < 0);
    }

    @Test
    public void testCompareToAsymmetric() {
        Event a = createEvent(0);
        Event b = createEvent(5);

        assertTrue(a.compareTo(b) < 0);
        assertTrue(b.compareTo(a) > 0);
    }


}
