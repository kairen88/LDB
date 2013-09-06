package com.live.Debugger;
import javax.swing.text.StyleContext.SmallAttributeSet;

import javafx.beans.binding.NumberBinding;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.scene.shape.Line;


public class Arrow extends Line{

	String startMethodName;
	String endMethodName;
	
	public Arrow(String _startMethodName, String _endMethodName, int _lineNumber, double _windowWidth,
			SimpleDoubleProperty _startMethodX, SimpleDoubleProperty _startMethodY, 
			SimpleDoubleProperty _endMethodX, SimpleDoubleProperty _endMethodY)
	{
		startMethodName = _startMethodName;
		endMethodName = _endMethodName;
		
		SimpleDoubleProperty yOffset = new SimpleDoubleProperty(_lineNumber * 5);
		NumberBinding boundY = yOffset.add(_startMethodY);
		
		SimpleDoubleProperty xOffset = new SimpleDoubleProperty(_windowWidth);
		NumberBinding boundX = xOffset.add(_startMethodX);
		
		this.startXProperty().bind(boundX);
		this.startYProperty().bind(boundY);
		
		this.endXProperty().bind(_endMethodX);
		this.endYProperty().bind(_endMethodY);
	}
}
