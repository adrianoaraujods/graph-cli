package graph;

import graph.cli.CliCommand;
import graph.cli.CliParser;
import graph.cli.CreateCommand;
import graph.cli.ReadCommand;
import graph.cli.create.CreateHandler;
import graph.cli.read.ReadHandler;

import java.security.InvalidAlgorithmParameterException;

public class GraphCLI {

    public static void main(String[] args) {
        try {
            CliCommand command = CliParser.parse(args);

            if (command == null) {
                return; // help was printed
            }

            if (command instanceof CreateCommand cc) {
                CreateHandler.run(cc.config());
            } else if (command instanceof ReadCommand rc) {
                ReadHandler.run(rc.config());
            }

        } catch (InvalidAlgorithmParameterException e) {
            System.err.println(e.getMessage());
            if (e.getMessage().contains("Unknown subcommand")) {
                graph.util.Usage.printGeneral();
            }
        } catch (Exception e) {
            System.err.println(e.getMessage());
        }
    }
}
