package dfs.util;

import dfs.client.Client;
import dfs.transport.Node;
import java.util.Scanner;

/**
 *
 * @author Naman
 */
public class ConsoleCommands implements Runnable {

    private final Client client;
    private Scanner sc;

    public ConsoleCommands(Client node) {
        this.client = node;
        sc = new Scanner(System.in);
    }

    @Override
    public void run() {
        while (true) {
            String command = sc.nextLine();
            this.processCommand(command);
        }
    }

    public void processCommand(String command) {
        System.out.println("-command-" + command);
        String[] instruction = command.split(" ");
        if (instruction != null && instruction.length > 0) {
            int len = instruction.length;
            String argument = "";
            //second parameter can be file location, so merge all other indexes
            for (int i = 1; i < len; i++) {
                if (i < (len - 1)) {
                    argument += instruction[i] + " ";
                } else {
                    argument += instruction[i];
                }
            }
            if (!argument.equals("")) {
                if (instruction[0].equalsIgnoreCase(Constants.CLIENT_COMMANDS.STORE)) {
                    //store file
                    System.out.println("-store file called-");
                    System.out.println("-arg-" + argument);
                    client.storefile(argument);
                } else if (instruction[0].equalsIgnoreCase(Constants.CLIENT_COMMANDS.READ)) {
                    //Read file
                    client.readFile(argument);
                } else if (instruction[0].equalsIgnoreCase(Constants.CLIENT_COMMANDS.UPDATE)) {
                    //Update file
                    client.updateFile(argument);
                } else {
                    System.err.println("ERROR : Invalid command : " + instruction[0]);
                }
            } else {
                System.err.println("ERROR : No arguments found.");
            }
        } else {
            System.err.println("ERROR : Invalid command");
        }
    }
}
