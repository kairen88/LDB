package com.live.Debugger;

import java.util.ArrayList;

//import sun.org.mozilla.javascript.internal.ast.Loop;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.shape.Line;
import javafx.scene.shape.Rectangle;

public class NavigationBar {
	
	Pane naviBar;
	
	double tickNavigatorPosition;
	int numberOfTicks = 30;
    double naviBarHeight = 30.0;
    double naviBarWidth = 200.0;
    int naviBarDefaultPadding = 5;
    
    double segmentHeight;
	double segmentWidth;
	double segmentTickHeight;
    
    //highlighted loop area, should change to array of pair values
//    int loopStart = 5;
//	int loopEnd = 10;
	ArrayList<loopHighlighter> loopHighlightAry;
	
	//loop highlight color
	private enum LoopColor
	{
		//pink  cyan    orange  purple
		B023A4, AFEEEE, FFA500, D8BFD8
	}
	
	private class loop{
		int loopStart;
		int loopEnd;
		
		loop(int _start, int _end)
		{
			loopStart = _start;
			loopEnd = _end;
		}
		
		public int getStart() {return loopStart;}
		public int getEnd() {return loopEnd;}
	}
	
	private class loopHighlighter {
		int StartLine;
		int EndLine;
		LoopColor loopColor;
		
		loopHighlighter(int _startLine, int _endLine, LoopColor _color) 
		{
			StartLine = _startLine;
			EndLine = _endLine;
			loopColor = _color;
		}
		
		public int getStart() { return StartLine; }
		public int getEnd() { return EndLine; }
		public LoopColor getColor() { return loopColor; }
	}
	
	//takes in an ary of loop objects denoting loops in the code
	//set to null if no loop in code segement
    public NavigationBar(CodeLoop [] loopAry) 
    {
    	//initialize loop highlighter
    	loopHighlightAry = new ArrayList<loopHighlighter>(); 
    	
    	//check if loopAry is null, then set loop
    	if(loopAry != null)
    		setLoopAry(loopAry);
    	
    	//creating navi bar background
    	Rectangle barBackground = new Rectangle(naviBarWidth, naviBarHeight);
		javafx.scene.paint.Paint color = javafx.scene.paint.Paint.valueOf("36B541");
		barBackground.setFill(color);
		barBackground.setArcHeight(5);
		barBackground.setArcWidth(5);
		
		//create loop components
		
		//construct time segments (ticks)
		HBox timeSegments = constructTimeSegment();
		
		//create navibar title
		Label naviBarTitle = new Label("MethodName()");
		naviBarTitle.setStyle("-fx-text-fill: #FFFFFF;");

		//adding components to navibar base
		naviBar = new Pane();
		naviBar.getChildren().addAll(barBackground, timeSegments, naviBarTitle);
		
		//adjust title position
		double naviBarTiltlePadding = (naviBarWidth / 2)  - (naviBarTitle.getWidth() / 2);
		naviBarTitle.relocate( naviBarTiltlePadding, 0.0);
		
		//adjust loop component position
		
		//adjust time segment position
		double naviBarCalculatedPadding = (naviBarWidth - (segmentWidth * numberOfTicks)) /2;
		timeSegments.relocate(naviBarCalculatedPadding, 0);		
		
	}
    
    public Pane getNaviBarRoot()
    {
    	return naviBar;
    }
    
    public double getSegmentWidth()
    {
    	return segmentWidth;
    }
    
    public double getNaviBarHeight()
    {
    	return naviBarHeight;
    }
    
    //adds the loop to the loop array and sets the color
    //check if loop range is valid
    //if loop added is within another loop
    //use the next color in the loop color enum
    public void setLoopHighlight(int _loopStart, int _loopEnd)
    {
    	//check if loop range is Valid
    	if(_loopStart >= 0 && _loopEnd <= numberOfTicks)
    	{
    		if(_loopStart < _loopEnd)
    		{
    			//if loop array is empty
    			if(loopHighlightAry.size() == 0)
    			{
    				//assign 1st color in ary
    				LoopColor color = LoopColor.values()[0];
    				loopHighlighter loopHighlighter = new loopHighlighter(_loopStart, _loopEnd, color);    				
    		    	loopHighlightAry.add(loopHighlighter);
    			}
    			else
    			{
    				LoopColor color = LoopColor.values()[0];

    				for(loopHighlighter loop : loopHighlightAry)
    				{
    					//compare new loop range with those in ary
    					if(_loopStart >= loop.getStart() && _loopEnd <= loop.getEnd())
    					{
    						LoopColor prevLoopColor = loop.getColor();
    						color = prevLoopColor;
    						//loop through LoopColor ary
		    				for(int colorIdx = 0; colorIdx < LoopColor.values().length; colorIdx ++)
		    				{
		    					//check for a match in LoopColor enum
		    					if(prevLoopColor == LoopColor.values()[colorIdx])
		    						//check if color is last color in enum, if so use 1st color of enum
		    						if(colorIdx == LoopColor.values().length - 1)
		    						{
		    							color = LoopColor.values()[0];		 
		    							System.err.println("RAN OUT OF LOOP ENUM COLORS");
		    						}
		    						else
		    						{
		    							//use next color in enum
		    							color = LoopColor.values()[colorIdx + 1];		    		    				
		    						}		    					
		    				}
    					}   

    				}
    				
    				loopHighlighter loopHighlighter = new loopHighlighter(_loopStart, _loopEnd, color);    				
    		    	loopHighlightAry.add(loopHighlighter);
    			}

    		}
    	}
    }
	
    
    private HBox constructTimeSegment()
    {
    	//container to hold time segments
		HBox timeSegments = new HBox();
		//initialize segment dimensions
		segmentHeight = naviBarHeight;
		segmentWidth = Math.floor( (naviBarWidth - (2 * naviBarDefaultPadding)) / numberOfTicks );
		segmentTickHeight = naviBarHeight / 3.0;
		
		javafx.scene.paint.Paint color = javafx.scene.paint.Paint.valueOf("36B541");
		
		//create a rectangle for each time segment and a line
		//if the current time segment falls within the range which is in a loop, set color to the highlight color (red)
		//for each time segment (numberOfTicks) add a set to a segment container
		//stack the segments in a HBox container
    	for(int i = 0; i < numberOfTicks; i++)
		{
			Pane segmentContainer = new Pane();			
			
			Rectangle segmentRec = new Rectangle(segmentWidth, segmentHeight);
			javafx.scene.paint.Paint normalColor = color;
			javafx.scene.paint.Paint loopColor = javafx.scene.paint.Paint.valueOf(LoopColor.B023A4.toString());

			//check if loop ary is empty, if empty set to normal bar color
			//for each object in loop array check if current line number is in it's range
			//if in range of current loop object, set the color and store the range
			//if loop object range is smaller than stored range ie. loop is inside previous loop, set color to new loop's color
			int loopStart = -1;
			int loopEnd = -1;
			javafx.scene.paint.Paint barColor = javafx.scene.paint.Paint.valueOf("36B541");
			//initialize with normal bar color		
			javafx.scene.paint.Paint segmentColor = barColor;
			
			if(loopHighlightAry.size() != 0)
			{
				for(loopHighlighter loop : loopHighlightAry)
				{					
					if(i >= loop.getStart() && i <= loop.getEnd())
					{
						if(loopStart == -1 && loopEnd == -1)
						{
							//set loop start and end
							loopStart = loop.getStart();
							loopEnd = loop.getEnd();
							segmentColor = javafx.scene.paint.Paint.valueOf(loop.getColor().toString());
						}
						else
						{
							//if the new loop is within the current loop
							if(loop.getStart() >= loopStart && loop.getEnd() <= loopEnd)
							{
								//store new loop start and end
								loopStart = loop.getStart();
								loopEnd = loop.getEnd();
								segmentColor = javafx.scene.paint.Paint.valueOf(loop.getColor().toString());
							}
						}
					}						
				}
			}
			segmentRec.setFill(segmentColor);
			
			Line segmentTick = new Line(1.0, segmentHeight, 1.0, segmentHeight - segmentTickHeight);
			javafx.scene.paint.Paint naviBarTickColor = javafx.scene.paint.Paint.valueOf("FFFFFF");
			segmentTick.setStroke(naviBarTickColor);
			
			segmentContainer.getChildren().addAll(segmentRec, segmentTick);
			
			timeSegments.getChildren().add(segmentContainer);
		}
    	
    	return timeSegments;
    }
    
    //sorts the arry of loop objects then set the color for each
    private void setLoopAry(CodeLoop[] loopAry)
    {
    	sortLoopAry(loopAry);
    	
    	for(CodeLoop  lop : loopAry)
    		setLoopHighlight(lop.getStart(), lop.getEnd());
    }
    
    private void sortLoopAry(CodeLoop [] _loopAry)
    {
    	//key element being sorted
    	CodeLoop key;
    	
    	for(int i = 1; i < _loopAry.length; i++)
    	{
    		key = _loopAry[i];
    		
    		int j = i - 1;
    		
    		while(j >= 0 && _loopAry[j].getStart() > key.getStart() && _loopAry[j].getEnd() < key.getEnd())
    		{
    			_loopAry[j + 1] = _loopAry[j];
    			
    			j--;
    		}
    		_loopAry[j + 1] = key;
    	}
    }

}
