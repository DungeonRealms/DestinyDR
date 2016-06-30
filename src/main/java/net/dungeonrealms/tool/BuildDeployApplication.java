package net.dungeonrealms.tool;

import java.io.BufferedReader;
import java.io.InputStreamReader;

/**
 * Class written by APOLLOSOFTWARE.IO on 6/7/2016
 */

public class BuildDeployApplication {

    static String TOOL_PATH = "\"C:\\Users\\XenoJava\\Desktop\\DR\\DungeonRealms\\tools\"";

    public static void main(String[] args) {

        if (args.length > 0) {
            switch (args[0]) {
                case "-updateBungee":
                    executeCommand("cd " + TOOL_PATH + " && pushBungee.bat");
                    break;
                case "-updateLiveServer1":
                    executeCommand("cd " + TOOL_PATH + " && pushLive1.bat " + args[1].substring(1));
                    break;
                case "-updateDevServer":
                    executeCommand("cd " + TOOL_PATH + " && pushDev.bat " + args[1].substring(1));
                    break;
//                case "-updateDevServer2":
//                    executeCommand("cd " + TOOL_PATH + " && pushDev2.bat " + args[1].substring(1));
//                    break;
//                case "-updateDevServer3":
//                    executeCommand("cd " + TOOL_PATH + " && pushDev3.bat " + args[1].substring(1));
//                    break;
//                case "-updateAllDev3":
//                    executeCommand("cd " + TOOL_PATH + " && pushDev3.bat yt1");
//                    executeCommand("cd " + TOOL_PATH + " && pushDev3.bat sub1");
//                    break;
//                case "-updateAllDev2":
//                    executeCommand("cd " + TOOL_PATH + " && pushDev2.bat us3");
//                    executeCommand("cd " + TOOL_PATH + " && pushDev2.bat us4");
//                    break;
//                case "-updateAllDev1":
//                    executeCommand("cd " + TOOL_PATH + " && pushDev.bat us1");
//                    executeCommand("cd " + TOOL_PATH + " && pushDev.bat us2");
//                    break;
//                case "-updateAllDev":
//                    executeCommand("cd " + TOOL_PATH + " && pushDev3.bat yt1");
//                    executeCommand("cd " + TOOL_PATH + " && pushDev3.bat sub1");
//                    executeCommand("cd " + TOOL_PATH + " && pushDev.bat us1");
//                    executeCommand("cd " + TOOL_PATH + " && pushDev.bat us2");
//                    executeCommand("cd " + TOOL_PATH + " && pushDev2.bat us3");
//                    executeCommand("cd " + TOOL_PATH + " && pushDev2.bat us4");
//                    break;
                case "-updateAll":
                    executeCommand("cd " + TOOL_PATH + " && pushBungee.bat");
                    executeCommand("cd " + TOOL_PATH + " && pushDev.bat us0");
                    executeCommand("cd " + TOOL_PATH + " && pushDev.bat sub1");
                    //executeCommand("cd " + TOOL_PATH + " && pushDev2.bat us3");
                    //executeCommand("cd " + TOOL_PATH + " && pushDev2.bat us4");
//                    executeCommand("cd " + TOOL_PATH + " && pushDev3.bat yt1");
//                    executeCommand("cd " + TOOL_PATH + " && pushDev3.bat sub1");
                    break;
            }
        } else {
            System.out.println("Program arguments are invalid!");
        }

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
