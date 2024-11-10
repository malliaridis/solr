# Solr CLI module

The CLI module is a standalone application that allows the user to manage Solr via CLI commands.

## Getting Started

[TODO]

## Development

When developing the CLI module the simplest way to test a command execution, without rebuilding the
the application with `gradlew :solr:cli installDist`, is to run the `Main.kt` with the program
arguments you want.

The only difference with the distribution is that you are not executing it from inside a CLI and you
may need to provide the correct path to the server installation via `--install-dir` (or the
according system / environment variable), since the fallback path is based on the current script
location.
