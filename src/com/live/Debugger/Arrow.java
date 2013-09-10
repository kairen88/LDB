 package com.live.Debugger;

import javax.swing.text.StyleContext.SmallAttributeSet;
import javax.xml.stream.events.EndElement;

import javafx.beans.binding.IntegerBinding;
import javafx.beans.binding.NumberBinding;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.scene.shape.Line;


public class Arrow extends Line{

	String startMethodName;
	String endMethodName;
	
//	SimpleIntegerProperty lineNumber;
	
	public Arrow(CodeWindow startWindow, CodeWindow endWindow)
	{
		startMethodName = startWindow.getMethodName();
		endMethodName = endWindow.getMethodName();
		
		IntegerBinding lineNumber = startWindow.selectedLineNumber.add(1);
		
//		lineNumber.add(startWindow.getEditor().selectedLineNumber.add(1));
		
	
		//calculate the offset for the y position at the start of the arrow
		//using the current line selected
		NumberBinding yLineOffset = lineNumber.multiply(startWindow.lineoffset);
		NumberBinding startY = yLineOffset.add(startWindow.getDraggableY()).add(30);
		
		//calculate the offset for the x position at the start of the arrow
		//using the width of the editor
		SimpleDoubleProperty xOffset = new SimpleDoubleProperty();
		NumberBinding endX = xOffset.add(startWindow.getDraggableX()).add(startWindow.getEditor().widthProperty());
		
		//bind the start position of the arrow to the start method
		this.startXProperty().bind(endX);
		this.startYProperty().bind(startY);
				
		//bind the end position of the arrow to the end method
		this.endXProperty().bind(endWindow.getDraggableX());
		this.endYProperty().bind(endWindow.getDraggableY());
	}
			
			
			
//			prevCodeWindow.getMethodName(), codeWin.getMethodName(), 
//			prevCodeWindow.getSelectedLineNumber(), prevCodeWindow.getEditor().getWidth(), 
//			prevCodeWindow.getCodeWindowContainer().x, prevCodeWindow.getCodeWindowContainer().y, 
//			codeWin.getCodeWindowContainer().x, codeWin.getCodeWindowContainer().y
	
}
