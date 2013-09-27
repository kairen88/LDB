package com.live.Debugger;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;

import javafx.geometry.Insets;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.RowConstraints;
import javafx.scene.paint.Color;

public class VariablePane extends GridPane{
	
	private String methodName;
	private int iteration;
	
	//store variables and value history
	private LinkedHashMap<String,ArrayList> localVariables= new LinkedHashMap<>();
	
	//list of localVariables hashmaps to correspond the current iteration of the window
//	private ArrayList<LinkedHashMap<String,ArrayList>> localVariablesList= new ArrayList<>();

	public VariablePane(String _methodName, int _iteration){
		
		methodName = _methodName;
		iteration = _iteration;

		this.setMaxSize(230, 800);
		this.setPadding(new Insets(18, 18, 18, 18));
        this.setGridLinesVisible(true);
        RowConstraints rowinfo = new RowConstraints();
        this.setMinWidth(230);
        
        ColumnConstraints colInfo1 = new ColumnConstraints();
        colInfo1.setPercentWidth(40);
 
        ColumnConstraints colInfo2 = new ColumnConstraints();
        colInfo2.setPercentWidth(60);
 
        this.getColumnConstraints().add(colInfo1); //25 percent
        this.getColumnConstraints().add(colInfo2); //30 percent
        
 
        Label nameLabel = new Label("Name");
        GridPane.setMargin(nameLabel, new Insets(0, 5, 0, 10));
        
        GridPane.setConstraints(nameLabel, 0, 0);
        Label variableValue = new Label("Variable");
        GridPane.setMargin(variableValue, new Insets(0, 0, 0, 10));
        GridPane.setConstraints(variableValue, 1, 0);
 
		
        this.getChildren().addAll(nameLabel, variableValue);
	}
	
	public LinkedHashMap getLocalVariables() {

		return localVariables;
	}
	public void setLocalVariables(LinkedHashMap localVariables) {

		this.localVariables = localVariables;
	}
	
	public VariablePane setVariableValue(String _varName, String _varValue)
	{
		//if the variable to be written to exist in our local var hashmap, append the value to it
		if(!localVariables.isEmpty()&&localVariables.containsKey(_varName)){
			ArrayList values=(ArrayList)localVariables.get(_varName);
			if(_varValue!=null){
				values.add(_varValue);
			}
			else{
				values.add("null");
			}
			//update the entry in local variable hashmap
			localVariables.put(_varName, values);
			
			//variable may not exist in our list in which index will be -1
			int index=returnIndexofValue(_varName,localVariables);
			//variable exists
			if(index!=-1){

		        index=2+2*index+2;
		        
		        removeGridStyles(this);
		        
		        //highlight the variable name that is being written to
		        Label nameLabel = (Label)this.getChildren().get(index-1);	
				nameLabel.setTextFill(Color.web("#ff9999"));
				
				//highlight and set the new value in the combo box
		        ComboBox valueBox= (ComboBox)this.getChildren().get(index);				        
		        valueBox.setScaleX(1);
		        valueBox.setScaleY(1);
		        valueBox.setStyle("-fx-font-size: 10px; -fx-background-color: #ff9999");    
		        valueBox.getItems().add(_varValue);
		        valueBox.setValue(_varValue);
		        
		        //update the elements being displayed
		        this.getChildren().set(index-1, nameLabel);
		        this.getChildren().set(index, valueBox);
	        }
		}
		else{ //if it does not exist in our hashmap, create the elements and add it to the variable pane
			
			//each time we add a variable we add 2 child elements
			//number of rows = no. of child elements / 2
			int rows=((this.getChildren().size())/2);
			
			Label nameLabel = new Label(_varName);
			
			//remove previous highlights and highlight the new var
			removeGridStyles(this);
			nameLabel.setTextFill(Color.web("#ff9999"));
			
	        			        
	        ComboBox valueBox= new ComboBox();
	        valueBox.setScaleX(1);
	        valueBox.setScaleY(1);
	        valueBox.setStyle("-fx-font-size: 10px; -fx-background-color: #ff9999");
	        valueBox.getItems().add(_varValue);
	        valueBox.setValue(_varValue);
	        
	        this.setMargin(nameLabel, new Insets(10, 10, 10, 10));
	        this.setMargin(valueBox, new Insets(10, 10, 10, 10));
	        
	        this.add(nameLabel, 0, rows);
	        this.add(valueBox, 1, rows);
	       
	       
	       //add the new var and value in local var hashmap and update the hashmap in the current code window
	       ArrayList values=new ArrayList();
	       values.add(_varValue);
	       localVariables.put(_varName, values);	       
	       
		}
		
		return this;
	}
	
	public VariablePane highlightVariableValue(String _varName) {
		//variable may not exist in our list in which index will be -1
				int index=returnIndexofValue(_varName,localVariables);
				//variable exists
				if(index!=-1){
			        index=2+2*index+2;
			        
			        removeGridStyles(this);
			        
			        //highlight the variable name that is being written to
			        Label nameLabel = (Label)this.getChildren().get(index-1);	
					nameLabel.setTextFill(Color.web("#ff9999"));
					
					//highlight and set the new value in the combo box
			        ComboBox valueBox= (ComboBox)this.getChildren().get(index);				        
			        valueBox.setScaleX(1);
			        valueBox.setScaleY(1);
			        valueBox.setStyle("-fx-font-size: 10px; -fx-background-color: #ff9999");    
			        //display the previous written value
			        //**this is not correct, we need a new impl
			        if(valueBox.getItems().size() > 1)
			        	valueBox.setValue( valueBox.getItems().get(valueBox.getItems().size() - 1) );
			        
			        //update the elements being displayed
			        this.getChildren().set(index-1, nameLabel);
			        this.getChildren().set(index, valueBox);
		        }
				
				return this;
	}
	
	//helping function which returns index value of some variable name
	private int returnIndexofValue(String name,LinkedHashMap<String,ArrayList> localVariableInfo){
			int i=0;
			Iterator keys=localVariableInfo.keySet().iterator();
			while(keys.hasNext()){
				String variableValue=(String)keys.next();
				if(variableValue.equals(name)){
					return i; 
				}
				i++;
			}
		
	return -1;	
	}
	
	//This method is used to change the style of grid pane
	private void removeGridStyles(GridPane gp){
		for(int i=3;i<gp.getChildren().size();i=i+2){
			Label label=(Label)gp.getChildren().get(i);
			ComboBox cb=(ComboBox)gp.getChildren().get(i+1);
			label.setTextFill(Color.web("#000000"));
			cb.setStyle("-fx-font-size: 10px");
			gp.getChildren().set(i,label);
			gp.getChildren().set(i+1,cb);
		}
	}

	
}
