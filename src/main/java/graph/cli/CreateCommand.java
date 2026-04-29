package graph.cli;

import graph.cli.create.CreateConfig;

public record CreateCommand(CreateConfig config) implements CliCommand {
}
