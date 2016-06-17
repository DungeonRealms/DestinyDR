@echo off
set buildPath=C:\Users\XenoJava\Desktop\DR\DungeonRealms\target\DungeonRealms.jar
set privateKey=C:\Users\XenoJava\Desktop\DR\keys\private.ppk
set server=%1

winscp.com /command ^
	"open sftp://dungeonrealms:apollo@158.69.122.139 -privatekey=%privateKey% -passphrase=apollo" ^
	"cd ""/home/dungeonrealms/%server%/plugins""" ^
    "put ""%buildPath%""" ^
    "exit"