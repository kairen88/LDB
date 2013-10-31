package com.live.Debugger;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.event.EventHandler;
import javafx.scene.input.MouseEvent;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Polygon;

public class ResizeIcon extends Polygon{
	
	private BooleanProperty isClicked;
	
	private DoubleProperty width;
	private DoubleProperty height;
	
	private double mouseX = 0;
	private double mouseY = 0;
	
	public ResizeIcon(double _width, double _height)
	{
		width = new SimpleDoubleProperty();
		height = new SimpleDoubleProperty();
		
		width.set(_width);
		height.set(_height);
		
		isClicked = new SimpleBooleanProperty();
		isClicked.setValue(false);

//		this.radiusProperty().set(5);
//		this.setFill(javafx.scene.paint.Paint.valueOf("99CCFF"));
		
//		this = new Polygon();
		this.getPoints().addAll(new Double[]{
		        -7.0, 7.0,
		        7.0, -7.0,
		        7.0, 7.0 });
		javafx.scene.paint.Paint tailColor = javafx.scene.paint.Paint.valueOf("000000");
		this.setFill(tailColor);
		
		//EventListener for MousePressed
	       onMousePressedProperty().set(new EventHandler<MouseEvent>(){

	           public void handle(MouseEvent event) {
	              //record the current mouse X and Y position on Node
	              mouseX = event.getSceneX();
	              mouseY = event.getSceneY();

	              isClicked.setValue(true);
	           }

	       });
	       
	       //so the nodes does not move while we resize
	       onMouseReleasedProperty().set(new EventHandler<MouseEvent>(){

	           public void handle(MouseEvent event) {
	              isClicked.setValue(false);
	           }

	       });

	       //Event Listener for MouseDragged
	       onMouseDraggedProperty().set(new EventHandler<MouseEvent>(){

	           public void handle(MouseEvent event) {
	               //calculate the new width and height
	        	   double newWidth = width.getValue() + (event.getSceneX() - mouseX);
	        	   double newHeight = height.getValue() + (event.getSceneY() - mouseY);
	        	   
	               width.setValue(newWidth);
	               height.setValue(newHeight);


	               //again set current Mouse x AND y position
	               mouseX = event.getSceneX();
	               mouseY= event.getSceneY();
	               //}
	           }
	       });
	}
	
	public BooleanProperty getIsClickedPropert()
	{
		return isClicked;
	}
	
	public DoubleProperty getX()
	{
		return width;
	}
	
	public DoubleProperty getY()
	{
		return height;
	}
	
	public void setWidth(double _width)
	{
		width.set(_width);
	}
	
	public void setHeight(double _height)
	{
		height.set(_height);
	}

}
