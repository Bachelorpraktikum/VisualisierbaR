package com.github.bachelorpraktikum.dbvisualization.model;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;

import org.junit.Test;

public class ElementTypeTest {

    @Test
    public void testFromNameKnown() {
        // this string has been chosen based on very scientific criteria
        String someString = "shdgf.._fjgpvcbre-78234";
        for (Element.Type type : Element.Type.values()) {
            assertEquals(type, Element.Type.fromName(type.getLogName()));
            assertEquals(type, Element.Type.fromName(someString + type.getLogName()));
            assertEquals(type, Element.Type.fromName(type.getLogName() + someString));
            assertEquals(type, Element.Type.fromName(someString + type.getLogName() + someString));
        }
    }

    @Test
    public void testFromEmptyNameUnknown() {
        assertEquals(Element.Type.UnknownElement, Element.Type.fromName(""));
    }

    @Test
    public void testFromUnknownName() {
        assertEquals(Element.Type.UnknownElement, Element.Type.fromName("hjkf--459_.fjs"));
    }

    @Test
    public void testVisibleStateProperty() {
        for (Element.Type type : Element.Type.values()) {
            assertNotNull(type.visibleStateProperty());
        }
    }

    @Test
    public void testGetName() {
        for (Element.Type type : Element.Type.values()) {
            assertNotNull(type.getName());
            assertFalse(type.getName().isEmpty());
        }
    }
}
