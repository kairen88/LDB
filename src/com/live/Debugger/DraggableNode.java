package com.live.Debugger;

import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.collections.ObservableList;
import javafx.event.EventHandler;
import javafx.scene.Group;
import javafx.scene.Parent;
import javafx.scene.input.MouseEvent;


public class DraggableNode extends Group{

   //ATTRIBUTES
   //X AND Y postion of Node
   SimpleDoubleProperty x = new SimpleDoubleProperty(0);
   SimpleDoubleProperty y = new SimpleDoubleProperty(0);
   //X AND Y position of mouse
   double mousex=0;
   double mousey=0;
   
   int dwidth;
   int dheight;
   
   public int getDWidth() {
		return dwidth;
	}
   public int getDHeigth() {
		return dheight;
	}
	public void setDSize(int w,int h) {
		this.dwidth= w;
		this.dheight= h;
	}
	
   //To make this function accessible for other Class
   @Override
   public ObservableList getChildren(){
       return super.getChildren();
   }

   public DraggableNode(){
       super();

       //EventListener for MousePressed
       onMousePressedProperty().set(new EventHandler<MouseEvent>(){

           public void handle(MouseEvent event) {
              //record the current mouse X and Y position on Node
              mousex = event.getSceneX();
              mousey= event.getSceneY();
              //get the x and y position measure from Left-Top
              x.setValue(getLayoutX());
              y.setValue(getLayoutY());
           }

       });

       //Event Listener for MouseDragged
       onMouseDraggedProperty().set(new EventHandler<MouseEvent>(){

           public void handle(MouseEvent event) {
               //Get the exact moved X and Y
               x.setValue(x.get() + event.getSceneX()-mousex) ;
               y.setValue(y.get() + event.getSceneY()-mousey) ;
               
//               System.out.println("Value of X = "+x.get());
//               System.out.println("Value of Y = "+y.get());
               
              /* if(x>=0&&x<=640&&y<700&&y>=0){
            	*/
               
               //set the positon of Node after calculation
               setLayoutX(x.get());
               setLayoutY(y.get());

               //again set current Mouse x AND y position
               mousex = event.getSceneX();
               mousey= event.getSceneY();
               //}
           }
       });
   }
}

