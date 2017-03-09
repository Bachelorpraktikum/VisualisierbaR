package com.github.bachelorpraktikum.dbvisualization.logparser;

import com.github.bachelorpraktikum.dbvisualization.logparser.LogParser.MsgContext;
import com.github.bachelorpraktikum.dbvisualization.logparser.LogParser.RatContext;
import com.github.bachelorpraktikum.dbvisualization.model.Context;
import com.github.bachelorpraktikum.dbvisualization.model.Coordinates;
import com.github.bachelorpraktikum.dbvisualization.model.Edge;
import com.github.bachelorpraktikum.dbvisualization.model.Element;
import com.github.bachelorpraktikum.dbvisualization.model.Messages;
import com.github.bachelorpraktikum.dbvisualization.model.Node;
import com.github.bachelorpraktikum.dbvisualization.model.train.Train;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigInteger;
import java.util.logging.Logger;
import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;
import org.antlr.v4.runtime.ANTLRFileStream;
import org.antlr.v4.runtime.ANTLRInputStream;
import org.antlr.v4.runtime.BailErrorStrategy;
import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.DefaultErrorStrategy;
import org.antlr.v4.runtime.atn.PredictionMode;
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.ParseTreeWalker;

@ParametersAreNonnullByDefault
public final class GraphParser {

    private static final Logger log = Logger.getLogger(GraphParser.class.getName());

    public GraphParser() {
    }

    @Nonnull
    private Context parse(@Nonnull CharStream input, @Nonnull Context context) {
        LogLexer lexer = new LogLexer(input);
        CommonTokenStream tokens = new CommonTokenStream(lexer);
        LogParser parser = new LogParser(tokens);
        parser.setErrorHandler(new DefaultErrorStrategy());
        parser.getInterpreter().setPredictionMode(PredictionMode.SLL);
        ParseTree parseTree;
        try {
            parseTree = parser.start();  // STAGE 1
        } catch (Exception ex) {
            tokens.reset(); // rewind input stream
            parser.reset();
            parser.getInterpreter().setPredictionMode(PredictionMode.LL);
            parseTree = parser.start();  // STAGE 2
            // if we parse ok, it's LL not SLL
        }

        ParseTreeWalker walker = new ParseTreeWalker();
        Listener listener = new Listener(context);
        walker.walk(listener, parseTree);
        return context;
    }

    @Nonnull
    public Context parse(String fileName) throws IOException {
        return parse(fileName, new Context());
    }

    @Nonnull
    public Context parse(String fileName, Context context) throws IOException {
        CharStream input = new ANTLRFileStream(fileName);
        return parse(input, context);
    }

    @Nonnull
    public Context parse(InputStream string) throws IOException {
        return parse(string, new Context());
    }

    @Nonnull
    public Context parse(InputStream input, Context context) throws IOException {
        ANTLRInputStream stream = new ANTLRInputStream(input);
        return parse(stream, context);
    }

    private static class Listener extends LogBaseListener {

        private final Context context;
        private final BigInteger thousandInt;

        Listener(Context context) {
            this.context = context;
            this.thousandInt = BigInteger.valueOf(1000);
        }

        private int createTime(LogParser.TimeContext ctx) {
            if (ctx.rat() != null) {
                RatContext ratContext = ctx.rat();
                BigInteger left = new BigInteger(ratContext.INT(0).getText());
                BigInteger right = new BigInteger(ratContext.INT(1).getText());
                return left.multiply(thousandInt).divide(right).intValue();
            }
            return Integer.parseInt(ctx.INT().getText()) * 1000;
        }

        private Coordinates createCoordinates(LogParser.CoordContext ctx) {
            int x = Integer.parseInt(ctx.INT(0).getText());
            int y = Integer.parseInt(ctx.INT(1).getText());
            return new Coordinates(x, y);
        }

        @Override
        public void enterNode(LogParser.NodeContext ctx) {
            String nodeName = ctx.node_name().getText();
            Coordinates coordinates = createCoordinates(ctx.coord());
            Node.in(context).create(nodeName, coordinates);
        }

        @Override
        public void enterElem(LogParser.ElemContext ctx) {
            String elementName = ctx.elem_name().getText();
            String nodeName = ctx.node_name().getText();
            Node node = Node.in(context).get(nodeName);
            Element.State state = Element.State.fromName(ctx.STATE().getText());
            Element.Type type = Element.Type.fromName(elementName);
            Element.in(context).create(elementName, type, node, state);
        }

        @Override
        public void enterEdge(LogParser.EdgeContext ctx) {
            String edgeName = ctx.edge_name().getText();
            String node1Name = ctx.node_name(0).getText();
            String node2Name = ctx.node_name(1).getText();
            int length = Integer.parseInt(ctx.INT().getText());

            Node node1 = Node.in(context).get(node1Name);
            Node node2 = Node.in(context).get(node2Name);
            Edge.in(context).create(edgeName, length, node1, node2);
        }

        @Override
        public void enterTrain(LogParser.TrainContext ctx) {
            String trainName = ctx.train_name().getText();
            String humanName = ctx.train_readable_name().getText();
            int length = Integer.parseInt(ctx.INT().getText());

            Train.in(context).create(trainName, humanName, length);
        }

        @Override
        public void enterMv_init(LogParser.Mv_initContext ctx) {
            Train train = Train.in(context).get(ctx.train_name().getText());
            Edge edge = Edge.in(context).get(ctx.edge_name().getText());
            int time = createTime(ctx.time());
            train.eventFactory().init(time, edge);
        }

        @Override
        public void enterMv_speed(LogParser.Mv_speedContext ctx) {
            String trainName = ctx.train_name().getText();
            Train train = Train.in(context).get(trainName);
            int time = createTime(ctx.time());
            int distance = Integer.parseInt(ctx.distance().getText());
            if (ctx.speed() == null) {
                train.eventFactory().move(time, distance);
            } else {
                int speed = Integer.parseInt(ctx.speed().INT().getText());
                train.eventFactory().speed(time, distance, speed);
            }
        }

        @Override
        public void enterMv_start(LogParser.Mv_startContext ctx) {
            String trainName = ctx.train_name().getText();
            Train train = Train.in(context).get(trainName);
            int time = createTime(ctx.time());
            String edgeName = ctx.edge_name().getText();
            Edge edge = Edge.in(context).get(edgeName);
            int distance = Integer.parseInt(ctx.distance().getText());
            train.eventFactory().reach(time, edge, distance);
        }

        @Override
        public void enterMv_leaves(LogParser.Mv_leavesContext ctx) {
            String trainName = ctx.train_name().getText();
            Train train = Train.in(context).get(trainName);
            int time = createTime(ctx.time());
            String edgeName = ctx.edge_name().getText();
            Edge edge = Edge.in(context).get(edgeName);
            int distance = Integer.parseInt(ctx.distance().getText());
            train.eventFactory().leave(time, edge, distance);
        }

        @Override
        public void enterMv_term(LogParser.Mv_termContext ctx) {
            String trainName = ctx.train_name().getText();
            Train train = Train.in(context).get(trainName);
            int time = createTime(ctx.time());
            int distance = Integer.parseInt(ctx.distance().getText());
            train.eventFactory().terminate(time, distance);
        }

        @Override
        public void enterCh(LogParser.ChContext ctx) {
            String elementName = ctx.elem_name().getText();
            Element element = Element.in(context).get(elementName);
            Element.State state = Element.State.fromName(ctx.STATE().getText());
            int time = createTime(ctx.time());
            element.addEvent(state, time);
        }

        @Override
        public void enterMsg(MsgContext ctx) {
            int time = createTime(ctx.time());
            String text = ctx.message().getText();
            Node node = Node.in(context).get(ctx.node_name().getText());
            Messages.in(context).add(time, text, node);
        }
    }

}
