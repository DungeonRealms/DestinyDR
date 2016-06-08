package net.dungeonrealms.tool;

import java.io.BufferedReader;
import java.io.InputStreamReader;

/**
 * Class written by APOLLOSOFTWARE.IO on 6/7/2016
 */

public class UpdateDevServerApplication {

    static String TOOL_PATH = "\"C:\\Users\\XenoJava\\Desktop\\DR\\DungeonRealms\\tools\\pushBuildToServer\"";

    public static void main(String[] args) {
        executeCommand("cd " + TOOL_PATH + " && push.bat");
    }


    private static void executeCommand(String command) {
        ProcessBuilder builder = new ProcessBuilder(
                "cmd.exe", "/c", command);
        builder.redirectErrorStream(true);
        Process p;

        try {
            p = builder.start();
            p.waitFor();
            BufferedReader reader = new BufferedReader(new InputStreamReader(p.getInputStream()));
            String line;
            while (true) {
                line = reader.readLine();
                if (line == null) {
                    break;
                }
                System.out.println(line);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

    }
}
