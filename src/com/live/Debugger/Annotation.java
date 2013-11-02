package com.live.Debugger;
import java.awt.TextField;

import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.EventHandler;
import javafx.event.EventType;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;
import javafx.scene.shape.Polygon;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.RectangleBuilder;

import com.live.Debugger.ResizeIcon;
import com.live.Debugger.liveDebugging;


public class Annotation extends DraggableNode{
	
	TextArea textbox;
	String text;
	SimpleDoubleProperty width;
	SimpleDoubleProperty height;
	SimpleBooleanProperty isClicked;
	int idxInParent;
	
	Color annotatorColor = Color.web("#FFCC66");


	public Annotation (int _width, int _height, final ResizeIcon _parentResizeIcon, final DraggableNode parentContainer, final int _idxInParent)
	{
		textbox = new TextArea();
		textbox.wrapTextProperty().set(true);
		textbox.setStyle("-fx-background-color: #FFFFCC;");
		
		text = "";
		idxInParent = _idxInParent;
				
		//creating background
		Rectangle rect2 = RectangleBuilder.create()
	              .fill(annotatorColor)
	              .build();
	      rect2.setWidth(280);
	      rect2.setHeight(130);
	      
	      //creating tail
	      Polygon tail = new Polygon();
			tail.getPoints().addAll(new Double[]{
			        0.0, 0.0,
			        -20.0, 0.0,
			        0.0, 10.0 });
			tail.fillProperty().set(annotatorColor);
			
		//icon to resize annotation
		final ResizeIcon resizeIcon = new ResizeIcon(_width, _height);
		
		Label remove = new Label("X"); 
		
		
		//binding the width and height of background and textbox to the resize icon
		rect2.widthProperty().bind(resizeIcon.getX());
		rect2.heightProperty().bind(resizeIcon.getY());
		
		this.getLostFocuProperty().bind(resizeIcon.getIsClickedPropert());
		
		resizeIcon.layoutXProperty().bind(rect2.widthProperty());
		resizeIcon.layoutYProperty().bind(rect2.heightProperty());
		
		textbox.prefHeightProperty().bind(rect2.heightProperty().subtract(10));
		textbox.prefWidthProperty().bind(rect2.widthProperty().subtract(10));

		//add elements to container (draggable node)
	    this.getChildren().add(rect2);
	    this.getChildren().add(tail);
	    this.getChildren().add(resizeIcon);
	    this.getChildren().add(textbox);
	    this.getChildren().add(remove);
		
		textbox.relocate(5, 5);
		
		textbox.textProperty().addListener(new ChangeListener<Object>(){

			public void changed(ObservableValue<?> o, Object oldVal, Object newVal) {
				text = (String) newVal;
			}
	      });
		
		//mouse events to ensure parent container (draggble node as well) does note move while we move this annotator around
		this.addEventHandler(MouseEvent.MOUSE_PRESSED, new EventHandler<MouseEvent>() {
			   public void handle(MouseEvent arg0) {
				   
        	   _parentResizeIcon.getIsClickedPropert().set(true);

			   }
		});
		 
		this.addEventHandler(MouseEvent.MOUSE_RELEASED, new EventHandler<MouseEvent>() {
			   public void handle(MouseEvent arg0) {
				   
     	   _parentResizeIcon.getIsClickedPropert().set(false);

			   }
		});
		
		final DraggableNode parent = (DraggableNode) this.getParent();
		remove.addEventFilter(MouseEvent.MOUSE_CLICKED, new EventHandler<MouseEvent>() {
			   public void handle(MouseEvent arg0) {
				   
				   parentContainer.getChildren().remove(_idxInParent); //this crashes if we delete an earlier node first, must update idx when a child is removed
			   }
		});
	}
	
	public String getText()
	{
		return text;
	}
}
