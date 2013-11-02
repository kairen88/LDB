package com.live.Debugger;
import java.util.ArrayList;
import java.util.Vector;

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
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Polygon;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;


/**
 * @author penguin
 *
 */
/**
 * @author penguin
 *
 */
public class timeline extends VBox{

	private ArrayList<EventInfo> eventList;
	private int prevSelectedTickIdx;
	private String methodName;
	private long createdTimestamp;
	private SimpleIntegerProperty currentValue;
	private ArrayList<MethodState> callStack;
	
	int timelineLength;
	private SimpleIntegerProperty isReduced;//set to 0 to minimize timeline, else set 1 (used to calculate tick size)
	
	Vector<Integer> childTimelineIdxList; //index of the child timeline in the display container
	
	private HBox tickBox;
	private ArrayList<tick> tickList;
	Label methodLabel;
	Label valueLabel;
	
	
	timeline(ArrayList<EventInfo> _timestamps, String _methodName)
	{
		eventList = _timestamps;
		methodName = _methodName;
		currentValue = new SimpleIntegerProperty(0);
//		createdTimestamp = _timeCreated;
		isReduced = new SimpleIntegerProperty(1);
		childTimelineIdxList = new Vector<Integer>();
		
//		getCallStack(codeWinCallStack);
		
		//stores the idx of the last tick that was selected
		//prev tick idx has not been set, initialized to -1
		prevSelectedTickIdx = -1;
		tickList = new ArrayList<tick>();
				
		//space for 1 tick * number of ticks + size of 1 expanded tick
		//refer to tick class for values
		timelineLength = (3 + 5 + 5) * eventList.size() + (3 + 10 + 10) ;
		
		StackPane timebar = new StackPane();
		timebar.setMinHeight(15);
		timebar.setMinWidth(timelineLength);
		timebar.setStyle("-fx-background-color: #D8BFD8;");
		
		tickBox = new HBox();
		createTicks();
		
		//adding labels
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
		
		//adding tail
		//we add a tail only if it is not the main method
		if(!_methodName.equals("main"))
		{
			Polygon tail = new Polygon();
			tail.getPoints().addAll(new Double[]{
			        0.0, 0.0,
			        0.0, -15.0,
			        5.0, 0.0 });
			this.getChildren().add(tail);
		}
		
		timebar.getChildren().add(labelBox);
		timebar.getChildren().add(tickBox);
		
		
		this.getChildren().add(timebar);
				
		//add listener to update valueLabel
		currentValue.addListener(new ChangeListener<Object>(){

			public void changed(ObservableValue<?> o, Object oldVal, Object newVal) {
				valueLabel.setText( ((Integer)newVal).toString() );
			}
	      });		
		
		final StackPane thisTimeLine = timebar;
		//change listener to reduce timeline when isReduced is set to 0
		isReduced.addListener(new ChangeListener<Object>(){

			public void changed(ObservableValue<?> o, Object oldVal, Object newVal) {
				if((int)(newVal) == 0)
				{
					thisTimeLine.setMinWidth(10);
					thisTimeLine.setMaxWidth(10);
					valueLabel.visibleProperty().setValue(false);
					methodLabel.setText(methodName.substring(0, 4));
				}
				else
				{
					thisTimeLine.setMaxWidth(timelineLength);
					thisTimeLine.setMinWidth(timelineLength);
					valueLabel.visibleProperty().setValue(true);
					methodLabel.setText(methodName);
				}
			}
	      });
	}
	
	private void createTicks()
	{
		// - 1 since we want to exclude the method exit event
		for(int i = 0; i < (eventList.size() - 1); i++)
//		for(Long timestamp : timestamps)
		{
//			final tick tk = new tick(timestamp, tickList.size(), isReduced);
			final tick tk = new tick(methodName, eventList.get(i).getTimestamp(), eventList.get(i).getLineNumber(), tickList.size(), isReduced);
			tickList.add(tk);
			tk.addEventHandler(MouseEvent.MOUSE_CLICKED, new EventHandler<MouseEvent>() {
			   public void handle(MouseEvent arg0) {
				   
				   setTick(tk);
				   liveDebugging.naviTo(tk.getTimestamp());
			   }
			});
			
			tickBox.getChildren().add(tk);
		}
	}
	
	/**
	 * 
	 * @param tk
	 */
	private void setTick(tick tk)
	{
		 //if a tick was previously set, clear it
		  if(prevSelectedTickIdx != -1)
		  {
			  clearTick();
		  }
		  tk.setIsSelected(true);		  
		  tk.setTickSizeToExpanded();
		  tk.setTickColorSelected();
		  currentValue.setValue(tk.getPositionIdx() + 1);//position index is 0 based so we add 1
		  prevSelectedTickIdx = tk.getPositionIdx();
	}
	
	public void clearTick()
	{
		if(prevSelectedTickIdx != -1)
		{
			//set the previously selected tick to false, enable mousout resizing
			tickList.get(prevSelectedTickIdx).setIsSelected(false);
			tickList.get(prevSelectedTickIdx).setTickSizeToInitial();
		}
	}
	
	private int getIndexFromTimestamp(long _timestamp)
	{
		int index = -1;
		for(int i = 0; i < eventList.size(); i++)
		{
			if(_timestamp == eventList.get(i).getTimestamp())
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
	
	public Vector<Integer> getChildTimelineIdxList()
	{
		return childTimelineIdxList;
	}
	
	public void setChildTimelineIdx(int _childIdx)
	{
		childTimelineIdxList.add(_childIdx);
	}
	
	public void reduceTimeline()
	{
		isReduced.set(0);
		
		//shift the timeline by 5 since the tick size is reduced when it loses focus
		this.relocate(this.getLayoutX() - 5, this.getLayoutY());
	}
	
	public void expandTimeline()
	{
		isReduced.set(1);
	}
	
	public void hideTimeline()
	{
		this.visibleProperty().set(false);
	}
	
	public void showTimeline()
	{
		this.visibleProperty().set(true);
	}
	
	/**
	 * sets the correct tick given a timestamp
	 * @param _timestamp
	 * @return raw line number of the represented event (needs to be corrected to relative line number for the codeWindow)
	 */
	public int setTick(long _timestamp) {
		int index = getIndexFromTimestamp(_timestamp);
		int lineNum = -1;
		
		if(index != -1)
		{
			tick tk = tickList.get(index);
			setTick(tk);
			lineNum = tk.getLineNumber();
		}		
		return lineNum;
	}
	
	/**
	 * set tick as highlighted (same as mouse-over event)
	 * @param _timestamp
	 */
	
	public void setTickHighlight(long _timestamp)
	{
		int idx = getIndexFromTimestamp(_timestamp);
		tickList.get(idx).setTickSizeToExpanded();
	}
	
	public void removeTickHighlight(long _timestamp)
	{
		int idx = getIndexFromTimestamp(_timestamp);
		tickList.get(idx).setTickSizeToInitial();
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
		//if the timeline has a tail, set color for tail and timebar
		if(this.getChildren().size() > 1)
		{
			//set color of the tail
			javafx.scene.paint.Paint tailColor = javafx.scene.paint.Paint.valueOf(_color);
			Polygon tail = (Polygon)(this.getChildren().get(0));
			tail.setFill(tailColor);
			
			//get color of the timebar
			this.getChildren().get(1).setStyle("-fx-background-color: #" + _color + ";");
		}else
		{
			//get color of the timebar
			this.getChildren().get(0).setStyle("-fx-background-color: #" + _color + ";");
		}
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
