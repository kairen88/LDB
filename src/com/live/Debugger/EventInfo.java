package com.live.Debugger;

public class EventInfo {
	long timestamp;
	String methodName;
	String varName;
	int lineNumber; //raw line number
	String writeValue;
	public EventInfo(long timestamp, int lineNumber) {
		super();
		this.timestamp = timestamp;
		this.methodName = null;
		this.varName = null;
		this.lineNumber = lineNumber;
		this.writeValue = null;
	}
	public long getTimestamp() {
		return timestamp;
	}
	public void setTimestamp(long timestamp) {
		this.timestamp = timestamp;
	}
	public String getMethodName() {
		return methodName;
	}
	public void setMethodName(String methodName) {
		this.methodName = methodName;
	}
	public String getVarName() {
		return varName;
	}
	public void setVarName(String varName) {
		this.varName = varName;
	}
	public int getLineNumber() {
		return lineNumber;
	}
	public void setLineNumber(int lineNumber) {
		this.lineNumber = lineNumber;
	}
	public String getWriteValue() {
		return writeValue;
	}
	public void setWriteValue(String writeValue) {
		this.writeValue = writeValue;
	}
	
	

}
