package com.live.Debugger;
import java.util.ArrayList;

import tod.core.database.event.ILogEvent;

import javafx.event.EventHandler;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;


public class timeline extends StackPane{

	private ArrayList<Long> timestamps;
	private int prevSelectedTickIdx;
	private String methodName;
	private int currentValue;
	private ArrayList<MethodState> callStack;
	
	private HBox tickBox;
	private ArrayList<tick> tickList;
	
	
	timeline(ArrayList<Long> _timestamps, String _methodName, ArrayList<CodeWindow> codeWinCallStack)
	{
		timestamps = _timestamps;
		methodName = _methodName;
		currentValue = 0;
		
		getCallStack(codeWinCallStack);
		
		//stores the idx of the last tick that was selected
		//prev tick idx has not been set, initialized to -1
		prevSelectedTickIdx = -1;
		tickList = new ArrayList<tick>();
				
		this.setMinHeight(30);
		this.setMinWidth(300);
		this.setStyle("-fx-background-color: #336699;");
		
		tickBox = new HBox();
		createTicks();
		
		Label methodLabel = new Label(methodName);
		methodLabel.setTextFill(Color.web("#FFFFFF"));
		
		this.getChildren().add(methodLabel);
		this.getChildren().add(tickBox);
		
		
		StackPane.setAlignment(methodLabel, Pos.BOTTOM_LEFT);
	}
	
	private void createTicks()
	{
		for(Long timestamp : timestamps)
		{
			final tick tk = new tick(timestamp, tickList.size());
			tickList.add(tk);
			tk.addEventHandler(MouseEvent.MOUSE_CLICKED, new EventHandler<MouseEvent>() {
			   public void handle(MouseEvent arg0) {
				   
				   setTick(tk);
			   }
			});
			
			tickBox.getChildren().add(tk);
		}
	}
	
	private void setTick(tick tk)
	{
		 //set the previously selected tick to false, enable mousout resizing
		  if(prevSelectedTickIdx != -1)
		  {
			  tickList.get(prevSelectedTickIdx).setIsSelected(false);
			  tickList.get(prevSelectedTickIdx).setTickSizeToInitial();
		  }
		  tk.setIsSelected(true);		  
		  tk.setTickSizeToExpanded();
		  tk.setTickColorSelected();
		  currentValue = tk.getPositionIdx() + 1;//position index is 0 based so we add 1
		  prevSelectedTickIdx = tk.getPositionIdx();
	}
	
	public void setValue(double val)
	{
		tick tk = tickList.get((int)val);
		setTick(tk);
	}
	
	public int getValue()
	{
		return currentValue;
	}
	
	//returns the index of the 1st tick in ticklist
	public int getMin()
	{
		return 0;
	}
	
	//return the index of the last tick in ticklist
	public int getMax()
	{
		return tickList.size() - 1;
	}
	
	public void setColor(String _color)
	{
		this.setStyle("-fx-background-color: #" + _color + ";");
	}
	
	//for debugging purposes
	public void printCallStack()
	{
		System.out.println("Call Stack for " + methodName + " timeline: \n");
		
		for(MethodState methodState : callStack)
		{
			System.out.println("method: " + methodState.methodName + " Line: " + methodState.selectedLine + "\n");
		}
	}
	
	//each timeline maintains a call stack of it's parent methods
	//for recreating the windows in the display area when we navigate to an event by selecting the ticks
	//**IMPORTANT currently, line numbers are WRONG
	private void getCallStack(ArrayList<CodeWindow> codeWinCallStack)
	{
		callStack = new ArrayList<MethodState>();
		
		for(CodeWindow codeWin : codeWinCallStack)
		{
			MethodState methodState = new MethodState(codeWin.getMethodName(), codeWin.getSelectedLineNumber());
			callStack.add(methodState);
		}
	}
	
	class  MethodState{
		String methodName;
		int selectedLine;
		
		MethodState(String _methodName, int _selectedLine)
		{
			methodName = _methodName;
			selectedLine = _selectedLine;
		}
	}
}
