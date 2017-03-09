package com.github.bachelorpraktikum.dbvisualization.logparser;

import static org.junit.Assert.assertNotNull;

import com.github.bachelorpraktikum.dbvisualization.model.Context;
import java.io.IOException;
import org.junit.Test;

public class GraphParserTest {

    @Test
    public void testLog1() throws IOException {
        Context context = new GraphParser().parse("src/test/resources/test.zug.clean");
        assertNotNull(context);
    }

    @Test
    public void testLog2() throws IOException {
        Context context = new GraphParser().parse("src/test/resources/test2.zug.clean");
        assertNotNull(context);
    }

    @Test
    public void testLog3() throws IOException {
        Context context = new GraphParser().parse("src/test/resources/test3.zug.clean");
        assertNotNull(context);
    }

    @Test
    public void testLog4() throws IOException {
        Context context = new GraphParser().parse("src/test/resources/test4.zug.clean");
        assertNotNull(context);
    }

    @Test
    public void testLog5() throws IOException {
        Context context = new GraphParser().parse("src/test/resources/test5.zug.clean");
        assertNotNull(context);
    }

    @Test
    public void testLog6() throws IOException {
        Context context = new GraphParser().parse("src/test/resources/test6.zug");
        assertNotNull(context);
    }
}
