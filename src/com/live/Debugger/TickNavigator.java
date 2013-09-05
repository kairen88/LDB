package com.live.Debugger;

import javafx.scene.layout.Pane;
import javafx.scene.shape.Rectangle;

public class TickNavigator {
	
	private int bHeight;
	private int bWidth;
	private int lineThickness;
	private int lineLenght;
	private double tickNavigatorToNaviBarPadding;
	private double calculatedPadding;
	
	private Pane tickNavigatorContainer;
	
	public TickNavigator() {
		//initialize tick dimensions
		bHeight = 5;
		bWidth = 15;
		lineThickness = 2;
		lineLenght = 30;
		//default as 0, set padding when we attach this to a navi bar
		tickNavigatorToNaviBarPadding = 10.0;
		calculatedPadding = 0.0;
		
		//initialize tick navigator container
		tickNavigatorContainer = new Pane();
		
		//creating the tick navigator
		Rectangle tNTop = new Rectangle(bWidth, bHeight);
		tickNavigatorContainer.getChildren().add(tNTop);
		
		Rectangle tNMiddle = new Rectangle(lineThickness, lineLenght);
		tickNavigatorContainer.getChildren().add(tNMiddle);
		tNMiddle.relocate((bWidth - lineThickness) / 2, bHeight);
		
		Rectangle tNBottom = new Rectangle(bWidth, bHeight);
		tickNavigatorContainer.getChildren().add(tNBottom);
		tNBottom.relocate(0, bHeight + lineLenght);				

	}
	
	public void setTickNavigatorToNaviBar(NavigationBar currentNaviBar)
	{
		Pane naviBarContainer = currentNaviBar.getNaviBarRoot();
		
		currentNaviBar.getNaviBarRoot().getChildren().add(tickNavigatorContainer);
		
//		//calculate tick navigator height based on navi bar
//		//resize tick navigator
//		
//		//get 2nd element in container tNMiddle
//		//resize, relocate due to new length
//		tickNavigatorContainer.getChildren().get(1).setScaleY( (currentNaviBar.getNaviBarHeight() + tickNavigatorToNaviBarPadding) / lineLenght);
//		tickNavigatorContainer.getChildren().get(1).relocate((bWidth - lineThickness) / 2, bHeight + (tickNavigatorToNaviBarPadding / 2) );
//		
		//set new length
		lineLenght = (int) ( currentNaviBar.getNaviBarHeight() + tickNavigatorToNaviBarPadding);
		
		//create new tNMiddle
		Rectangle tNMiddle = new Rectangle(lineThickness, lineLenght);
		
		//replace current tNMiddle with new tNMiddle
		tickNavigatorContainer.getChildren().remove(1);
		tickNavigatorContainer.getChildren().add(1, tNMiddle);
		
		//relocate tNMiddle
		tNMiddle.relocate((bWidth - lineThickness) / 2, bHeight);
		
		//relocate tNBottom due to new line length
		tickNavigatorContainer.getChildren().get(2).relocate(0, lineLenght + bHeight);
		
		//relocation value from padding
		calculatedPadding = -( (tickNavigatorToNaviBarPadding / 2) + bHeight );
		
		//move navigator tick to first tick = segment width - tick width
		//add vertical padding to tick navigator
		tickNavigatorContainer.relocate(currentNaviBar.getSegmentWidth() - 2, calculatedPadding);

	}
	
	public int getTickNaviLineLength()
	{
		return lineLenght;
	}
	
	public Pane getTickNaviRoot()
	{
		return tickNavigatorContainer;
	}
	
	public void moveTickNavigatorToCurrTick(NavigationBar currentNaviBar, CodeWindow currentCodeWindow)
	{
		//set tick navigator position
		tickNavigatorContainer.setTranslateX( (currentNaviBar.getSegmentWidth()) + ( currentNaviBar.getSegmentWidth() * (currentCodeWindow.getCurrentExecutionLine() - 2) ) );

	}

}
