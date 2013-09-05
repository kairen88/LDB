package com.live.Debugger;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Set;

public class CodeFragments {
	
	private HashMap codeFragmentArray;
	private HashMap lineNumToMethodMap;
	private ArrayList<String> originalClass;
	ArrayList<int[]> fragmentList;
	
	public CodeFragments(){
		
	}
	public CodeFragments(Path _editingCodePath, liveDebugging ld)
	{
		codeFragmentArray = new HashMap<String, CodeWindow>();
		lineNumToMethodMap = new HashMap<String, Integer>();
		
		originalClass = loadClassIntoStringArray(_editingCodePath);
		//for Big factorial
		int[] fragment = {162, 175};//_fact
		int[] fragment2 = {185, 193};//_makeString
		int[] fragment3 = {21, 70}; //main
		int[] fragment4 = {82, 94};//multiply
		int[] fragment5 = {103, 113};//_int2Array
		int[] fragment6 = {122, 126};////_intLen
		int[] fragment7 = {135, 149};//_trimLeadZero
		
		
/*
		int[] fragment = {6, 7};//BubbleSort
		int[] fragment2 = {6, 11};//initialization
		int[] fragment3 = {12, 17}; //bubbleSort
		int[] fragment4 = {18, 26};//bubbleSwitch
		int[] fragment5 = {27, 32};//output
		int[] fragment6 = {33, 39};////main
	*/	
		fragmentList = new ArrayList<int[]>();
		fragmentList.add(fragment);
		fragmentList.add(fragment2);
		fragmentList.add(fragment3);
		fragmentList.add(fragment4);
		fragmentList.add(fragment5);
		fragmentList.add(fragment6);
		fragmentList.add(fragment7);
		
		
		
		//we convert line number to array index here to avoid confusion
		convertLineNumToAryIdx();
		
		createCodeFragments(fragmentList,ld);
	}
	
	public CodeWindow getCodeFragment(String _methodName)
	{
		CodeWindow cdw=(CodeWindow)codeFragmentArray.get(_methodName);
		
		return cdw;
	}
	private int returnIndexofValue(String name){
		int i=0;
		Set keys=codeFragmentArray.keySet();
		while(keys.iterator().hasNext()){
			String variableValue=(String)keys.iterator().next();
			if(variableValue.equals(name)){
				return i; 
			}
			i++;
		}
	
		return -1;	
		}
	
	public void setCodeFragment(String _methodName,int indexOnScreen)
	{
		//int index=returnIndexofValue(_methodName);
		CodeWindow cdw=(CodeWindow)codeFragmentArray.get(_methodName);
		cdw.setIndexOnScreen(indexOnScreen);
		codeFragmentArray.remove(_methodName);
		codeFragmentArray.put(_methodName,cdw);
	}
	
	public boolean codeFragmentExist(String _methodName)
	{
		return codeFragmentArray.containsKey(_methodName);
	}
	
	public int getLineNumberOffset(String _methodName)
	{
		return (int) lineNumToMethodMap.get(_methodName);
	}
	
	public int calculateHeight(int _startLine, int _endLine)
	{
		return (_endLine-_startLine+1)*9+80;
	}
	public int calculateWidth(int length)
	{
		return (length)*7;
	}
	private void createCodeFragments(ArrayList<int[]> _fragmentList, liveDebugging ld)
	{
		for(int[] fragmentIdx : _fragmentList)
		{
			Object []obj=(Object[])getCodeFragmentString(fragmentIdx[0], fragmentIdx[1]);
			String fragment = (String)obj[0];
			int width = 600;//(int)obj[1];
			String methodName = getMethodName(fragmentIdx[0]);
			int height= calculateHeight(fragmentIdx[0], fragmentIdx[1]);
			CodeWindow codeWin = new CodeWindow(fragment, width, height,methodName, ld);
			codeWin.setMethodName(methodName);
			codeWin.setStartLine(fragmentIdx[0]);
			codeWin.setEndLine(fragmentIdx[1]);
			codeFragmentArray.put(methodName, codeWin);
			
			//map method name to line number so we can convert line num 
			//from event to actual line number in the codefragment window
			lineNumToMethodMap.put(methodName, fragmentIdx[0]);
		}
	}
	/*
	public CodeWindow createCodeFragments(CodeWindow codeFragment,int instance){
		
		Object []obj=(Object[])getCodeFragmentString(codeFragment.getStartLine(),codeFragment.getEndLine());
		String fragment = (String)obj[0];
		int width = 600;//(int)obj[1];
		 
		String methodName = getMethodName(codeFragment.getStartLine());
		int height= calculateHeight(codeFragment.getStartLine(),codeFragment.getEndLine());
		CodeWindow codeWin = new CodeWindow(fragment, width, height,methodName);
		codeWin.setMethodName(methodName);
		//codeWin.setInstance(instance);
		
		return codeWin;
	}*/
	//line number starts from 1, ary idx starts from 0
	//converting line number to array idx
	private void convertLineNumToAryIdx()
	{
		for(int i = 0; i < fragmentList.size(); i++)
		{
			int[] fragmentIdx = fragmentList.get(i);
			fragmentIdx[0] -= 1;
			fragmentIdx[1] -= 1;
		}	
	}
	/*
	private CodeWindow createCodeWindow(int _startLine, int _endLine)
	{
		Object []obj=(Object[])getCodeFragmentString(_startLine, _endLine);
		String fragment = (String)obj[0];
		int width = (int)obj[1];
		return null;
	}*/
	
	//assumes that the 1st line of the fragment is the method signiture
	//processes and return the method name
	//returns null if line number is invalid
	private String getMethodName(int _lineNumber)
	{
		//check that line number is valid
		if(_lineNumber > -1)
		{
			String methodSig = originalClass.get(_lineNumber);
			String[] strAry = methodSig.split(" ");
			String methodName = "";
			if(strAry[1].equalsIgnoreCase("static"))
				methodName = strAry[3];
			else
				methodName = strAry[2];
			String[] methodNameAry = methodName.split("\\(");
			
			return methodNameAry[0];
		}
		return null;
	}
	
	private Object getCodeFragmentString(int _startLine, int _endLine)
	{
		Object []obj=new Object[2];
		String fragment = "";
		int max=0;
		for(int i = _startLine; i <= _endLine; i++)
		{
			fragment += originalClass.get(i);
			int length=originalClass.get(i).length();
			if(length>max){
				max=length;
			}
		}
		obj[0]=fragment;
		obj[1]=calculateWidth(max);
		return obj;
	}

	private ArrayList<String> loadClassIntoStringArray(Path _pathToClass)
	{
		ArrayList<String> code = new ArrayList<String>();
		//read in code from file
		String filePath = _pathToClass.toString();
		  try{
			  // Open the file that is the first command line parameter
			  FileInputStream fstream = new FileInputStream(filePath);
			  // Get the object of DataInputStream
			  DataInputStream in = new DataInputStream(fstream);
			  BufferedReader br = new BufferedReader(new InputStreamReader(in));
			  String strLine;
			  //Read File Line By Line
			  while ((strLine = br.readLine()) != null)   
			  {
	    		  //append to string obj template
				  code.add(strLine + "\n");
			  }
			  //Close the input stream
			  in.close();

		    }catch (Exception e)
		    {
		    	//Catch exception if any
		    	System.err.println("Error: " + e.getMessage());
		    }
		  
		  return code;
	}
}
