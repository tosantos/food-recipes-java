package org.olivetree.recipes.cli;

public enum RecipesCliCommands {
    LIST("list"), SEARCH("search"), ADD("add"), DELETE("delete"), UPDATE("update"), HELP("help"), QUIT("quit");

    private final String command;

    RecipesCliCommands(String command) {
        this.command = command;
    }

    public static RecipesCliCommands getCommand(String value) {
        for(RecipesCliCommands cliCommand : RecipesCliCommands.values()) {
            if(cliCommand.toString().equals(value)) {
                return cliCommand;
            }
        }

        return null;
    }

    @Override
    public String toString() {
        return this.command;
    }
}