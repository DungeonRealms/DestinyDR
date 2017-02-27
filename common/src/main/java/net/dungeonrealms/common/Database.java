package net.dungeonrealms.common;

public enum Database {
	DEV("dungeonrealms_event", "mongodb://dungeonrealms:sBFt%2BSk2r%5Ejw7%2BXZ%2B3%3Dm@ds157228-a0.mlab.com:57228,ds157228-a1.mlab.com:57228/dungeonrealms_event?replicaSet=rs-ds157228"),
	NORMAL("dungeonrealms", "mongodb://druser:H%3A82F9xW3X425NrHra.UW7-gXV@ds157228-a0.mlab.com:57228,ds157228-a1.mlab.com:57228/dungeonrealms?replicaSet=rs-ds157228");
	
	private final String uri;
	private final String dbName;
	
	Database(String db, String uri){
		this.uri = uri;
		this.dbName = db;
	}
	
	public String getURI(){
		return this.uri;
	}
	
	public String getDatabaseName(){
		return this.dbName;
	}
}
