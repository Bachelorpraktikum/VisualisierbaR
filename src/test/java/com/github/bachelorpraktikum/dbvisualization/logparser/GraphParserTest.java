package com.github.bachelorpraktikum.dbvisualization.logparser;

import com.github.bachelorpraktikum.dbvisualization.model.Context;

import org.junit.Ignore;
import org.junit.Test;

import java.io.IOException;

import static org.junit.Assert.assertNotNull;

public class GraphParserTest {
    @Test
    public void testLog1() throws IOException {
        Context context = new GraphParser("src/test/resources/test.zug.clean").parse();
        assertNotNull(context);
    }

    @Test
    public void testLog2() throws IOException {
        Context context = new GraphParser("src/test/resources/test2.zug.clean").parse();
        assertNotNull(context);
    }

    @Ignore("Not parseable due to errors in log file")
    @Test
    public void testLog3() throws IOException {
        Context context = new GraphParser("src/test/resources/test3.zug.clean").parse();
        assertNotNull(context);
    }

    @Test
    public void testLog4() throws IOException {
        Context context = new GraphParser("src/test/resources/test4.zug.clean").parse();
        assertNotNull(context);
    }

    @Test
    public void testLog5() throws IOException {
        Context context = new GraphParser("src/test/resources/test5.zug.clean").parse();
        assertNotNull(context);
    }

    @Test
    public void testLog6() throws IOException {
        Context context = new GraphParser("src/test/resources/test6.zug").parse();
        assertNotNull(context);
    }
}
