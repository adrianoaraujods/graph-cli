package graph.cli;

import graph.cli.read.ReadConfig;

public record ReadCommand(ReadConfig config) implements CliCommand {
}
