package com.live.Debugger;

import java.util.ArrayList;

public class MethodInfo {

	private long firstEventTimestamp;
	private long lastEventTimestamp;
	private long parentMethodfirstTimestamp;
	private long parentMethodlastTimestamp;
	
	private String methodName;
	private String parentName;
	
	public String getParentName() {
		return parentName;
	}

	public void setParentName(String parentName) {
		this.parentName = parentName;
	}

	//index of method in timeline and codeWindow area
	int timelineIdx;
	int codeWindowIdx;
	
	//child methods
	ArrayList<MethodInfo> childList;
	
	public MethodInfo(long firstEventTimestamp, long lastEventTimestamp,
			long parentMethodfirstTimestamp, long parentMethodlastTimestamp,
			String methodName, String parentName, int timelineIdx, int codeWindowIdx) {
		super();
		
		childList = new ArrayList<MethodInfo>();
		this.firstEventTimestamp = firstEventTimestamp;
		this.lastEventTimestamp = lastEventTimestamp;
		this.parentMethodfirstTimestamp = parentMethodfirstTimestamp;
		this.parentMethodlastTimestamp = parentMethodlastTimestamp;
		this.methodName = methodName;
		this.parentName = parentName;
		this.timelineIdx = timelineIdx;
		this.codeWindowIdx = codeWindowIdx;
	}

	public String methodName()
	{
		return methodName;
	}

	public long FirstEventTimestamp() {
		return firstEventTimestamp;
	}

	public long LastEventTimestamp() {
		return lastEventTimestamp;
	}

	public long ParentMethodfirstTimestamp() {
		return parentMethodfirstTimestamp;
	}

	public long ParentMethodlastTimestamp() {
		return parentMethodlastTimestamp;
	}

	public int getTimelineIdx() {
		return timelineIdx;
	}

	public void setTimelineIdx(int timelineIdx) {
		this.timelineIdx = timelineIdx;
	}

	public int getCodeWindowIdx() {
		return codeWindowIdx;
	}

	public void setCodeWindowIdx(int codeWindowIdx) {
		this.codeWindowIdx = codeWindowIdx;
	}

	public ArrayList<MethodInfo> getChildList() {
		return childList;
	}

	public void addChild(MethodInfo _childInfo) {
		childList.add(_childInfo);
	}
	
	
}
