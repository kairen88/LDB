package com.live.Debugger;
import java.util.ArrayList;

import tod.core.database.event.ILogEvent;

import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.EventHandler;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;


public class timeline extends StackPane{

	private ArrayList<Long> timestamps;
	private int prevSelectedTickIdx;
	private String methodName;
	private SimpleIntegerProperty currentValue;
	private ArrayList<MethodState> callStack;
	
	private HBox tickBox;
	private ArrayList<tick> tickList;
	Label methodLabel;
	Label valueLabel;
	
	
	timeline(ArrayList<Long> _timestamps, String _methodName, ArrayList<CodeWindow> codeWinCallStack)
	{
		timestamps = _timestamps;
		methodName = _methodName;
		currentValue = new SimpleIntegerProperty(0);
		
		getCallStack(codeWinCallStack);
		
		//stores the idx of the last tick that was selected
		//prev tick idx has not been set, initialized to -1
		prevSelectedTickIdx = -1;
		tickList = new ArrayList<tick>();
				
		//space for 1 tick * number of ticks + size of 1 expanded tick
		//refer to tick class for values
		int timelineLength = (3 + 5 + 5) * timestamps.size() + (3 + 10 + 10) ;
		
		this.setMinHeight(15);
		this.setMinWidth(timelineLength);
		this.setStyle("-fx-background-color: #D8BFD8;");
		
		tickBox = new HBox();
		createTicks();
		
		Pane labelBox = new Pane();		
		Font fontSize = new Font(9);
		
		methodLabel = new Label(methodName);		
		methodLabel.setTextFill(Color.web("#1E90FF"));
		methodLabel.setFont(fontSize);
		
		valueLabel = new Label(currentValue.getValue().toString());
		valueLabel.setFont(fontSize);
		
		labelBox.getChildren().addAll(methodLabel, valueLabel);
		
		//relocate the labels in the timeline
		methodLabel.relocate(0, 10);
		valueLabel.relocate(timelineLength, 10);		
		
		this.getChildren().add(labelBox);
		this.getChildren().add(tickBox);
				
		//add listener to update valueLabel
		currentValue.addListener(new ChangeListener<Object>(){

			public void changed(ObservableValue<?> o, Object oldVal, Object newVal) {
				// TODO Auto-generated method stub
				valueLabel.setText( ((Integer)newVal).toString() );
			}
	      });		
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
		  currentValue.setValue(tk.getPositionIdx() + 1);//position index is 0 based so we add 1
		  prevSelectedTickIdx = tk.getPositionIdx();
	}
	
	private int getIndexFromTimestamp(long _timestamp)
	{
		int index = -1;
		for(int i = 0; i < timestamps.size(); i++)
		{
			if(_timestamp == timestamps.get(i))
			{
				index = i;
				break;
			}
		}
		return index;
	}
	
	public String getMethodName()
	{
		return methodName;
	}
	
	public void setValue(double val)
	{
		tick tk = tickList.get((int)val);
		setTick(tk);
	}
	
	public void setTick(long _timestamp) {
		int index = getIndexFromTimestamp(_timestamp);
		
		if(index != -1)
		{
			tick tk = tickList.get(index);
			setTick(tk);
		}			
	}
	
	public int getValue()
	{
		return currentValue.getValue();
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
