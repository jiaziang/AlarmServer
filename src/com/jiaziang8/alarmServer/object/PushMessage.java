package com.jiaziang8.alarmServer.object;

public class PushMessage {
	public String type;	
	public String message;
	
	public PushMessage(String type,String message){
		this.type = type;
		this.message = message;
	}
	
	public String getType(){
		return this.type;
	}
	
	public String getMessage(){
		return this.message;
	}
	
	public void setType(String type){
		this.type = type;
	}
	
	public void setMeaaage(String message){
		this.message = message;
	}
	
}
