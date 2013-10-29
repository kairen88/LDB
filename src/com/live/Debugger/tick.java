package com.live.Debugger;
import javafx.beans.binding.NumberBinding;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.value.ObservableIntegerValue;
import javafx.beans.value.ObservableValue;
import javafx.event.EventHandler;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;


public class tick extends HBox{
	
	private long timestamp;
	private boolean isSelected;
	private int positionIdx;
	private int lineNumber; //raw line number NOT corrected to relative line number in codeWindow
	private String methodName;
	
	private Rectangle tick;
	private Rectangle spaceR;
	private Rectangle spaceL;
	
	private SimpleIntegerProperty tickWidth;
	private SimpleIntegerProperty spaceWidth;
	
	//constants
	private final Color tickColor = Color.WHITE;
	private final Color spaceColor = Color.TRANSPARENT;
	private final Color mouseOverColor = Color.LIGHTBLUE;
	private final Color selectedColor = Color.ORANGE;
	
	private final int tickHeightInitial = 8;
	private final int tickWidthInitial = 3;
	private final int spaceHeightInitial = 20;
	private final int spaceWidthInitial = 5;
	
	private final int tickHeightExpanded = 15;
	private final int tickWidthExpanded = 3;
	private final int spaceHeightExpanded = 20;
	private final int spaceWidthExpanded = 10;
	
	
	
	public tick(String _methodName, long _timestamp, int _lineNum, int _positionIdx, SimpleIntegerProperty _isReduced)
	{
		timestamp = _timestamp;
		positionIdx = _positionIdx;
		lineNumber = _lineNum;
		methodName = _methodName;

		//create the rectagles for the tick, the left and right spaces
		tick = new Rectangle(10,10,tickWidthInitial,tickHeightInitial);
		tick.setFill(tickColor);
		spaceR = new Rectangle(10,10,spaceWidthInitial,spaceHeightInitial);
		spaceR.setFill(spaceColor);
		spaceL = new Rectangle(10,10,spaceWidthInitial,spaceHeightInitial);
		spaceL.setFill(spaceColor);
		
		tickWidth = new SimpleIntegerProperty(tickWidthInitial);
		spaceWidth = new SimpleIntegerProperty(spaceWidthInitial);
		
		NumberBinding tickWidthBind = tickWidth.multiply(_isReduced);
		NumberBinding spaceWidthBind = spaceWidth.multiply(_isReduced);
		
		tick.widthProperty().bind(tickWidthBind);
		spaceL.widthProperty().bind(spaceWidthBind);
		spaceR.widthProperty().bind(spaceWidthBind);
		
		this.getChildren().add(spaceR);
		this.getChildren().add(tick);
		this.getChildren().add(spaceL);
		
		//set the event when the mouse enters the tick region
		this.addEventHandler(MouseEvent.MOUSE_ENTERED, new EventHandler<MouseEvent>() {
			   public void handle(MouseEvent arg0) {

				   if(!isSelected)
				   {
					   //increase height and width of rectangles
					   setTickSizeToExpanded();
					   System.out.println("mouse in, Timestamp: " + timestamp);
					   System.out.println("position index: " + positionIdx);
					   
					   liveDebugging.selectLineInCodeWindow(methodName, timestamp, lineNumber);
				   }
			   }
		});
		
		//set event when mouse exist the tick region
		this.addEventHandler(MouseEvent.MOUSE_EXITED, new EventHandler<MouseEvent>() {
			   public void handle(MouseEvent arg0) {

				   if(!isSelected)
				   {
					   //set height and width to initial values
					   setTickSizeToInitial();
					   System.out.println("mouse out");
					   
					   liveDebugging.selectLineInCodeWindow(methodName, timestamp, -1);
				   }
			   }
		});
		
		
	}
	
	public void setTickSizeToInitial()
	{
		 //set height and width to initial values
		  tick.setHeight(tickHeightInitial);
	      spaceR.setHeight(spaceHeightInitial);
	      spaceL.setHeight(spaceHeightInitial);
	      
  	      spaceWidth.setValue(spaceWidthInitial);
  	      tickWidth.setValue(tickWidthInitial);

	      tick.setFill(tickColor);
	     
	}
	
	public void setTickSizeToExpanded()
	{
		 //increase height and width of rectangles
	      tick.setHeight(tickHeightExpanded);
	      spaceR.setHeight(spaceHeightExpanded);	      
	      spaceL.setHeight(spaceHeightExpanded);
	      
	      spaceWidth.setValue(spaceWidthExpanded);
  	      tickWidth.setValue(tickWidthExpanded);
	      
	      tick.setFill(mouseOverColor);
	      
	}
	
	public void setIsSelected(boolean _isSelected)
	{
		isSelected = _isSelected;
	}
	
	public int getPositionIdx()
	{
		return positionIdx;
	}
	
	public long getTimestamp1()
	{
		return timestamp;
	}
	
	public int getLineNumber()
	{
		return lineNumber;
	}
	
	public void setTickColorSelected()
	{
		tick.setFill(selectedColor);
	}
	
	//may need this function to minimize timeline when method moves to another timeline
	public void forceMinizeTick()
	{
		//set height and width to initial values
		  tick.setHeight(tickHeightInitial);
	      spaceR.setHeight(spaceHeightInitial);
	      spaceL.setHeight(spaceHeightInitial);
	      
	      spaceWidth.setValue(spaceWidthInitial);
	      tickWidth.setValue(tickWidthInitial);
	}

	public long getTimestamp() {
		// TODO Auto-generated method stub
		return timestamp;
	}
}
