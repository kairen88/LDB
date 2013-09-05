package com.live.Debugger;
import java.util.ArrayList;

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
	
	private HBox tickBox;
	private ArrayList<tick> tickList;
	
	timeline(ArrayList<Long> _timestamps, String _methodName)
	{
		timestamps = _timestamps;
		methodName = _methodName;
		currentValue = 0;
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

//				   //set the previously selected tick to false, enable mousout resizing
//				  if(prevSelectedTickIdx != -1)
//				  {
//					  tickList.get(prevSelectedTickIdx).setIsSelected(false);
//					  tickList.get(prevSelectedTickIdx).setTickSizeToInitial();
//				  }
//				  tk.setIsSelected(true);
//				  tk.setTickColorSelected();
//				  currentValue = tk.getPositionIdx() + 1;//position index is 0 based so we add 1
//				  prevSelectedTickIdx = tk.getPositionIdx();
				   
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
//		switch(_color){
//			case "FF8C73": // red
//				this.setStyle("-fx-background-color: #" + _color + ";");
//				break;
//	
//			case	"CCCCCC": // grey
//				this.setStyle("-fx-background-color: #" + _color + ";");
//
//	
//			case	"A3FF7F": // green
//				this.setStyle("-fx-background-color: #" + _color + ";");
//
//	
//			case	"FFFB78": // yellow
//		}
	}
}
