package net.dungeonrealms.tool;

import java.io.BufferedReader;
import java.io.InputStreamReader;

/**
 * Copyright © 2016 APOLLOSOFTWARE.IO
 * All rights reserved. No part of this publication may be reproduced, distributed, or
 * transmitted in any form or by any means, including photocopying, recording, or other
 * electronic or mechanical methods, without the prior written permission of the publisher,
 * except in the case of brief quotations embodied in critical reviews and certain other
 * noncommercial uses permitted by copyright law.
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
