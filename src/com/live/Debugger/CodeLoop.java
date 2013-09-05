package com.live.Debugger;

public class CodeLoop {
	int loopStart;
	int loopEnd;
	
	public CodeLoop(int _start, int _end){
		loopStart = _start;
		loopEnd = _end;
	}
	
	public int getStart() {return loopStart;}
	public int getEnd() {return loopEnd;}
}
