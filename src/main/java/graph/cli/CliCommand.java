package graph.cli;

public sealed interface CliCommand permits CreateCommand, ReadCommand {
}
