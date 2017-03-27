package com.github.bachelorpraktikum.dbvisualization.model;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;

import java.util.Arrays;
import java.util.Collection;
import java.util.stream.Collectors;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

@RunWith(Parameterized.class)
public class ElementTypeTest extends ShapeableImplementationTest {

    private Element.Type type;

    public ElementTypeTest(Element.Type type) {
        this.type = type;
    }

    @Test
    public void testFromNameKnown() {
        // this string has been chosen based on very scientific criteria
        String someString = "shdgf.._fjgpvcbre-78234";
        assertEquals(type, Element.Type.fromName(type.getLogName()));
        assertEquals(type, Element.Type.fromName(someString + type.getLogName()));
        assertEquals(type, Element.Type.fromName(type.getLogName() + someString));
        assertEquals(type, Element.Type.fromName(someString + type.getLogName() + someString));

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
        assertNotNull(type.visibleStateProperty());
    }

    @Override
    protected Shapeable<?> getShapeable() {
        return type;
    }

    @Test
    public void testGetName() {
        assertNotNull(type.getName());
        assertFalse(type.getName().isEmpty());
    }

    @Parameters
    public static Collection<Object[]> data() {
        return Arrays.stream(Element.Type.values())
            .map(type -> new Object[]{type})
            .collect(Collectors.toList());
    }
}
