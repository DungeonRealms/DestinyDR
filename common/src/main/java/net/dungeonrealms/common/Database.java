package net.dungeonrealms.common;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

public class Database {
	
	private final String username;
	private final String password;
	private final String host;
	private final String dbName;
	
	public Database(String host, String username, String password, String db) {
		this.host = host;
		this.username = username;
		this.password = password;
		this.dbName = db;
	}
	
	public String getURI() throws UnsupportedEncodingException {
		return "mongodb://" + this.username + ":" + URLEncoder.encode(this.password, "UTF-8") + "@" + host + "/" + this.dbName + "?replicaSet=rs-ds157228";
	}
	
	public String getDatabaseName(){
		return this.dbName;
	}
}
