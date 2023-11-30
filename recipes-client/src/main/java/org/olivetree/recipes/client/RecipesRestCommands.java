package org.olivetree.recipes.client;

public enum RecipesRestCommands {
    LIST("list"), SEARCH("search"), ADD("add"), DELETE("delete"), UPDATE("update"), HELP("help"), QUIT("quit");

    private final String command;

    RecipesRestCommands(String command) {
        this.command = command;
    }

    public static RecipesRestCommands getCommand(String value) {
        for(RecipesRestCommands cliCommand : RecipesRestCommands.values()) {
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