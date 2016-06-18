@echo off
set buildPath=C:\Users\Alan\IdeaProjects\DR\target\DungeonRealms.jar
set privateKey=C:\Users\Alan\.ssh\github.ppk


winscp.com /command ^
	"open sftp://dungeonrealms@158.69.122.139 -privatekey=%privateKey%" ^
	"cd ""/home/dungeonrealms/d1/plugins""" ^
    "put ""%buildPath%""" ^
    "exit"