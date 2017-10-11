package net.dungeonrealms.game.quests;

import org.bukkit.Location;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;

public class CompassData {
    /*private float lastYaw = 0;

    public CompassData(float val) {
        this.lastYaw = val;
    }

    public String generateString(final GeneratorInfo gi) {
        double yaw = gi.yaw;
        try {
            yaw += 360.0;
            if (yaw > 360.0) {
                yaw -= 360.0;
            }

            //So rip me?
            //North 180, -180
            //West 90
            //South 0
            //Easy -90
//            List<BlockFace> face = Lists.newArrayList();
//            Map<BlockFace, Float> faces = new HashMap<BlockFace, Float>() {{
//                put(BlockFace.NORTH, 180F);
//                put(BlockFace.SOUTH, 0F);
//                put(BlockFace.WEST, 90F);
//                put(BlockFace.EAST, 270F);
//            }};
//
//            BlockFace closest = null;
//            for (BlockFace fac : faces.keySet()) {
//                float val = faces.get(fac);
//                if (val < yaw) {
//
//                }
//            }




            final double ratio = yaw / 360.0;
            final StringBuilder strBuild = new StringBuilder();
            if (this.theme.getStringMapFull() != null) {
                final String[] arr = this.theme.getStringMapFull().split(";");
                final int length = arr.length;
                final double shift = length / 4;
                int appendRead = (int) Math.round(length * ratio - shift);
                final double step = 360.0 / length;
                for (int i = 0; i < length / 2 + 3; ++i) {
                    try {
                        int num;
                        if (appendRead < 0) {
                            num = length + appendRead;
                        } else if (appendRead >= length) {
                            num = appendRead - length;
                        } else {
                            num = appendRead;
                        }
                        if (--num >= length) {
                            num -= length;
                        } else if (num < 0) {
                            num += length;
                        }
                        if (arr[num] != null || !arr[num].isEmpty()) {
                            String appending = arr[num];
                            if (gi.targets != null && gi.p != null) {
                                for (final int trackNum : gi.targets.keySet()) {
                                    final TrackedTarget target = gi.targets.get(trackNum);
                                    final Location l1 = gi.p.getLocation();
                                    Location l2 = null;
                                    if (target != null) {
                                        l2 = target.getLocation();
                                    }
                                    if (l1 != null && l2 != null) {
                                        double angle = Math.atan2(l1.getX() - l2.getX(), l1.getZ() - l2.getZ());
                                        angle = -(angle / Math.PI) * 360.0 / 2.0 + 180.0;
                                        int num2 = num + 1;
                                        if (num2 >= length) {
                                            num2 -= length;
                                        } else if (num2 < 0) {
                                            num2 += length;
                                        }
                                        if (angle < step * num || angle > step * num2) {
                                            continue;
                                        }
                                        String targetNode = "&9\\u2588";
                                        if (targetNode == null) {
                                            continue;
                                        }
                                        targetNode = (appending = targetNode.replaceAll("%str%", appending).replaceAll(";", ""));
                                    }
                                }
                            }
                            if (gi.cursor && i == (length / 2 + 2) / 2) {
                                String cursorNode = "&f[;%str%;&f]";
                                if (cursorNode != null) {
                                    cursorNode = (appending = cursorNode.replaceAll("%str%", appending).replaceAll(";", ""));
                                }
                            }
                            strBuild.append(appending);
                        }
                        ++appendRead;
                    } catch (Exception ex) {
                    }
                }
            }
            String processsedString = strBuild.toString();
            final HashMap<String, String> directReplacers = this.theme.data.replacers;
            if (directReplacers != null && directReplacers.size() != 0) {
                for (final String s : directReplacers.keySet()) {
                    try {
                        processsedString = processsedString.replaceAll(s, directReplacers.get(s));
                    } catch (Exception ex2) {
                    }
                }
            }
            final HashMap<String, HashMap<String, String>> subPatternReplacers = this.theme.data.sub.replacers;
            if (subPatternReplacers != null && subPatternReplacers.size() != 0) {
                for (final String s2 : subPatternReplacers.keySet()) {
                    for (final String s3 : subPatternReplacers.get(s2).keySet()) {
                        try {
                            processsedString = processsedString.replaceAll(s3, subPatternReplacers.get(s2).get(s3));
                        } catch (Exception ex3) {
                        }
                    }
                }
            }
            String finalString = this.theme.post.pattern;
            final HashMap<String, String> finalPatternReplacers = this.theme.post.replacers;
            if (finalString.length() != 0) {
                finalString = finalString.replaceAll("%str%", processsedString);
                finalString = finalString.replaceAll(";", "");
                for (final String s4 : finalPatternReplacers.keySet()) {
                    try {
                        finalString = finalString.replaceAll(s4, finalPatternReplacers.get(s4));
                    } catch (Exception ex4) {
                    }
                }
            } else {
                finalString = processsedString;
            }
//            if (Main.placeholderAPIExist && gi.p != null) {
//                finalString = PlaceholderAPI.setPlaceholders(gi.p, finalString);
//            }
//            return Utils.fmtClr(finalString);
            return "";
        } catch (Exception e) {
            return "Compassance failed to parse this theme.";
        }
    }


    public static class GeneratorInfo {
        public final Player p;
        public final double yaw;
        public final boolean cursor;
        public final HashMap<Integer, TrackedTarget> targets;

        public GeneratorInfo(final Player p, final HashMap<Integer, TrackedTarget> targets) {
            this.p = p;
            this.yaw = p.getLocation().getYaw();
            this.targets = targets;
            this.cursor = true;
        }

        public GeneratorInfo(final double yaw, final boolean cursor) {
            this.p = null;
            this.targets = null;
            this.cursor = cursor;
            this.yaw = yaw;
        }
    }*/
}
