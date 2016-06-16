@echo off
set buildPath=C:\Users\XenoJava\Desktop\DR\DungeonRealms\target\DungeonRealms.jar


winscp.com /command ^
	"open sftp://dungeonrealms:]us-=:p]fS5jj!2W@131.153.27.42" ^
	"cd ""/home/dungeonrealms/bungeedr/plugins""" ^
    "put ""%buildPath%""" ^
    "exit"