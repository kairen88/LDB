package com.live.Debugger;
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

	private Rectangle tick;
	private Rectangle spaceR;
	private Rectangle spaceL;
	
	private Color tickColor = Color.WHITE;
	private Color spaceColor = Color.TRANSPARENT;
	private Color mouseOverColor = Color.LIGHTBLUE;
	private Color selectedColor = Color.ORANGE;
	
	private int tickHeightInitial = 10;
	private int tickWidthInitial = 5;
	private int spaceHeightInitial = 15;
	private int spaceWidthInitial = 5;
	
	private int tickHeightExpanded = 20;
	private int tickWidthExpanded = 5;
	private int spaceHeightExpanded = 25;
	private int spaceWidthExpanded = 20;
	
	private long timestamp;
	private boolean isSelected;
	private int positionIdx;
	
	public tick(long _timestamp, int _positionIdx)
	{
		timestamp = _timestamp;
		positionIdx = _positionIdx;

		//create the rectagles for the tick, the left and right spaces
		tick = new Rectangle(10,10,tickWidthInitial,tickHeightInitial);
		tick.setFill(tickColor);
		spaceR = new Rectangle(10,10,spaceWidthInitial,spaceHeightInitial);
		spaceR.setFill(spaceColor);
		spaceL = new Rectangle(10,10,spaceWidthInitial,spaceHeightInitial);
		spaceL.setFill(spaceColor);
		
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
				   }
			   }
		});
		
		
	}
	
	public void setTickSizeToInitial()
	{
		 //set height and width to initial values
		  tick.setHeight(tickHeightInitial);
	      spaceR.setHeight(spaceHeightInitial);
	      spaceR.setWidth(spaceWidthInitial);
	      spaceL.setHeight(spaceHeightInitial);
	      spaceL.setWidth(spaceWidthInitial);
	      
	      tick.setFill(tickColor);
	     
	}
	
	public void setTickSizeToExpanded()
	{
		 //increase height and width of rectangles
	      tick.setHeight(tickHeightExpanded);
	      spaceR.setHeight(spaceHeightExpanded);
	      spaceR.setWidth(spaceWidthExpanded);
	      spaceL.setHeight(spaceHeightExpanded);
	      spaceL.setWidth(spaceWidthExpanded);
	      
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
	      spaceR.setWidth(spaceWidthInitial);
	      spaceL.setHeight(spaceHeightInitial);
	      spaceL.setWidth(spaceWidthInitial);
	}

	public long getTimestamp() {
		// TODO Auto-generated method stub
		return 0;
	}
}
