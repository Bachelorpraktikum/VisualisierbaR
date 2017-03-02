package com.github.bachelorpraktikum.dbvisualization.logparser;

import com.github.bachelorpraktikum.dbvisualization.logparser.LogParser.MsgContext;
import com.github.bachelorpraktikum.dbvisualization.model.Context;
import com.github.bachelorpraktikum.dbvisualization.model.Coordinates;
import com.github.bachelorpraktikum.dbvisualization.model.Edge;
import com.github.bachelorpraktikum.dbvisualization.model.Element;
import com.github.bachelorpraktikum.dbvisualization.model.Messages;
import com.github.bachelorpraktikum.dbvisualization.model.Node;
import com.github.bachelorpraktikum.dbvisualization.model.train.Train;
import java.io.IOException;
import java.math.BigInteger;
import java.util.Objects;
import java.util.logging.Logger;
import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;
import org.antlr.v4.runtime.ANTLRFileStream;
import org.antlr.v4.runtime.BailErrorStrategy;
import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.atn.PredictionMode;
import org.antlr.v4.runtime.tree.ParseTree;

@ParametersAreNonnullByDefault
public final class GraphParser {

    private static final Logger log = Logger.getLogger(GraphParser.class.getName());

    @Nonnull
    private final String fileName;

    public GraphParser(String fileName) {
        this.fileName = Objects.requireNonNull(fileName);
    }

    @Nonnull
    public Context parse() throws IOException {
        CharStream input = new ANTLRFileStream(fileName);
        LogLexer lexer = new LogLexer(input);
        CommonTokenStream tokens = new CommonTokenStream(lexer);
        LogParser parser = new LogParser(tokens);
        parser.setErrorHandler(new BailErrorStrategy());
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

        return new ContextVisitor().visit(parseTree);
    }

    private static class ContextVisitor extends LogBaseVisitor<Context> {

        @Nonnull
        private final Context context;
        @Nonnull
        private final NodeVisitor nodeVisitor;
        @Nonnull
        private final ElementVisitor elementVisitor;
        @Nonnull
        private final CoordinatesVisitor coordinatesVisitor;
        @Nonnull
        private final EdgeVisitor edgeVisitor;
        @Nonnull
        private final TrainVisitor trainVisitor;
        @Nonnull
        private final TimeVisitor timeVisitor;

        ContextVisitor() {
            this.context = new Context();

            this.nodeVisitor = new NodeVisitor();
            this.elementVisitor = new ElementVisitor();
            this.coordinatesVisitor = new CoordinatesVisitor();
            this.edgeVisitor = new EdgeVisitor();
            this.trainVisitor = new TrainVisitor();
            this.timeVisitor = new TimeVisitor();
        }

        @Override
        public Context visit(ParseTree tree) {
            super.visit(tree);
            return context;
        }

        @Override
        public Context visitNode(LogParser.NodeContext ctx) {
            try {
                nodeVisitor.visitNode(ctx);
            } catch (IllegalArgumentException e) {
                log.warning("Could not parse line: " + ctx.getText()
                    + "\nReason: " + e.getMessage()
                );
            }
            return context;
        }

        @Override
        public Context visitElem(LogParser.ElemContext ctx) {
            try {
                elementVisitor.visitElem(ctx);
            } catch (IllegalArgumentException e) {
                log.warning("Could not parse line: " + ctx.getText()
                    + "\nReason: " + e.getMessage()
                );
            }
            return context;
        }

        @Override
        public Context visitEdge(LogParser.EdgeContext ctx) {
            try {
                edgeVisitor.visitEdge(ctx);
            } catch (IllegalArgumentException e) {
                log.warning("Could not parse line: " + ctx.getText()
                    + "\nReason: " + e.getMessage()
                );
            }
            return context;
        }

        @Override
        public Context visitTrain(LogParser.TrainContext ctx) {
            try {
                trainVisitor.visitTrain(ctx);
            } catch (IllegalArgumentException e) {
                log.warning("Could not parse line: " + ctx.getText()
                    + "\nReason: " + e.getMessage()
                );
            }
            return context;
        }

        @Override
        public Context visitMv_init(LogParser.Mv_initContext ctx) {
            Train train = Train.in(context).get(ctx.train_name().getText());
            Edge edge = Edge.in(context).get(ctx.edge_name().getText());
            int time = timeVisitor.visitTime(ctx.time());
            train.eventFactory().init(time, edge);
            return context;
        }

        @Override
        public Context visitMv_speed(LogParser.Mv_speedContext ctx) {
            String trainName = ctx.train_name().getText();
            Train train = Train.in(context).get(trainName);
            int time = timeVisitor.visitTime(ctx.time());
            int distance = Integer.parseInt(ctx.distance().getText());
            if (ctx.speed() == null) {
                train.eventFactory().move(time, distance);
            } else {
                int speed = Integer.parseInt(ctx.speed().INT().getText());
                train.eventFactory().speed(time, distance, speed);
            }
            return context;
        }

        @Override
        public Context visitMv_start(LogParser.Mv_startContext ctx) {
            String trainName = ctx.train_name().getText();
            Train train = Train.in(context).get(trainName);
            int time = timeVisitor.visitTime(ctx.time());
            String edgeName = ctx.edge_name().getText();
            Edge edge = Edge.in(context).get(edgeName);
            int distance = Integer.parseInt(ctx.distance().getText());
            train.eventFactory().reach(time, edge, distance);
            return context;
        }

        @Override
        public Context visitMv_leaves(LogParser.Mv_leavesContext ctx) {
            String trainName = ctx.train_name().getText();
            Train train = Train.in(context).get(trainName);
            int time = timeVisitor.visitTime(ctx.time());
            String edgeName = ctx.edge_name().getText();
            Edge edge = Edge.in(context).get(edgeName);
            int distance = Integer.parseInt(ctx.distance().getText());
            train.eventFactory().leave(time, edge, distance);
            return context;
        }

        @Override
        public Context visitMv_term(LogParser.Mv_termContext ctx) {
            String trainName = ctx.train_name().getText();
            Train train = Train.in(context).get(trainName);
            int time = timeVisitor.visitTime(ctx.time());
            int distance = Integer.parseInt(ctx.distance().getText());
            train.eventFactory().terminate(time, distance);
            return context;
        }

        @Override
        public Context visitCh(LogParser.ChContext ctx) {
            String elementName = ctx.elem_name().getText();
            Element element = Element.in(context).get(elementName);
            Element.State state = Element.State.fromName(ctx.STATE().getText());
            int time = timeVisitor.visitTime(ctx.time());
            element.addEvent(state, time);
            return context;
        }

        @Override
        public Context visitMsg(MsgContext ctx) {
            int time = timeVisitor.visitTime(ctx.time());
            String text = ctx.message().getText();
            Node node = Node.in(context).get(ctx.node_name().getText());
            Messages.in(context).add(time, text, node);
            return context;
        }

        private class NodeVisitor extends LogBaseVisitor<Node> {

            @Override
            public Node visitNode(LogParser.NodeContext ctx) {
                String nodeName = ctx.node_name().getText();
                Coordinates coordinates = coordinatesVisitor.visitCoord(ctx.coord());
                return Node.in(context).create(nodeName, coordinates);
            }
        }

        private class ElementVisitor extends LogBaseVisitor<Element> {

            @Override
            public Element visitElem(LogParser.ElemContext ctx) {
                String elementName = ctx.elem_name().getText();
                String nodeName = ctx.node_name().getText();
                Node node = Node.in(context).get(nodeName);
                Element.State state = Element.State.fromName(ctx.STATE().getText());
                Element.Type type = Element.Type.fromName(elementName);
                return Element.in(context).create(elementName, type, node, state);
            }
        }

        private class EdgeVisitor extends LogBaseVisitor<Edge> {

            @Override
            public Edge visitEdge(LogParser.EdgeContext ctx) {
                String edgeName = ctx.edge_name().getText();
                String node1Name = ctx.node_name(0).getText();
                String node2Name = ctx.node_name(1).getText();
                int length = Integer.parseInt(ctx.INT().getText());

                Node node1 = Node.in(context).get(node1Name);
                Node node2 = Node.in(context).get(node2Name);
                return Edge.in(context).create(edgeName, length, node1, node2);
            }
        }

        private class TrainVisitor extends LogBaseVisitor<Train> {

            @Override
            public Train visitTrain(LogParser.TrainContext ctx) {
                String trainName = ctx.train_name().getText();
                String humanName = ctx.train_readable_name().getText();
                int length = Integer.parseInt(ctx.INT().getText());

                return Train.in(context).create(trainName, humanName, length);
            }
        }

        private static class CoordinatesVisitor extends LogBaseVisitor<Coordinates> {

            @Override
            public Coordinates visitCoord(LogParser.CoordContext ctx) {
                int x = Integer.parseInt(ctx.INT(0).getText());
                int y = Integer.parseInt(ctx.INT(1).getText());
                return new Coordinates(x, y);
            }
        }

        private static class TimeVisitor extends LogBaseVisitor<Integer> {

            private final BigInteger thousandInt;

            TimeVisitor() {
                this.thousandInt = BigInteger.valueOf(1000);
            }

            @Override
            public Integer visitTime(LogParser.TimeContext ctx) {
                if (ctx.rat() != null) {
                    return visitRat(ctx.rat());
                }
                return Integer.parseInt(ctx.INT().getText()) * 1000;
            }

            @Override
            public Integer visitRat(LogParser.RatContext ctx) {
                BigInteger left = new BigInteger(ctx.INT(0).getText());
                BigInteger right = new BigInteger(ctx.INT(1).getText());
                return left.multiply(thousandInt).divide(right).intValue();
            }
        }
    }
}
