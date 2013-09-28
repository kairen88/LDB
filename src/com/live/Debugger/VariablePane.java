package com.live.Debugger;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.TreeMap;

import com.sun.org.apache.bcel.internal.classfile.LocalVariable;

import tod.core.database.event.ILogEvent;
import tod.core.database.structure.IStructureDatabase.LocalVariableInfo;

import javafx.geometry.Insets;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.RowConstraints;
import javafx.scene.paint.Color;
import javafx.util.Pair;

public class VariablePane extends GridPane{
	
	private String methodName;
	private int iteration;
	
	//store variables and value history
	private LinkedHashMap<String,ArrayList> localVariables= new LinkedHashMap<>();
	private TreeMap<String, Integer> variableMap;
	

	public VariablePane(String _methodName, int _iteration, ILogEvent _event, ArrayList<Object[]> _childEventsInfo){
		
		methodName = _methodName;
		iteration = _iteration;
		variableMap = new TreeMap<String, Integer>();
		
		initializeGridProperties();		
		
		populateGrid(_childEventsInfo);
	}



	private void populateGrid(ArrayList<Object[]> _childEventsInfo) {
		for(Object[] eventInfo : _childEventsInfo)
		{
			//is a write event with variable value
			//object[1] contains the variable / method name of the child events
			if(eventInfo[2] != null)
			{
				//if we have not created the corresponding element for this variable
				if(variableMap.get(eventInfo[1]) == null)
				{
					//create it and add it to the variable Pane
					
					//create value pair, timestamp : var value
					valuePair<Long, String> varValue = new valuePair<Long, String>((long)eventInfo[0], (String) eventInfo[2]);
					ComboBox<valuePair<Long, String>> valueBox = new ComboBox<valuePair<Long, String>>();
					valueBox.getItems().add(varValue);
					
					Label varNameLabel = new Label(eventInfo[1].toString());
					
					valueBox.setScaleX(1);
			        valueBox.setScaleY(1);
			        
			        this.setMargin(varNameLabel, new Insets(10, 10, 10, 10));
			        this.setMargin(valueBox, new Insets(10, 10, 10, 10));
					
					int row = this.getChildren().size() / 2;
					this.add(varNameLabel, 0, row);
					this.add(valueBox, 1, row);
					
					//store the index of the variable label element, combobox element index will be idx + 1
					variableMap.put(eventInfo[1].toString(), this.getChildren().size() - 2);
					
				}else //get the corresponding combo box and add the value
				{
					//get the index of the label element
					int index = variableMap.get(eventInfo[1]);
					ComboBox<valuePair<Long, String>> valueBox = (ComboBox<valuePair<Long, String>>) this.getChildren().get(index + 1);
					
					valuePair<Long, String> varValue = new valuePair<Long, String>((long)eventInfo[0], (String) eventInfo[2]);
					valueBox.getItems().add(varValue);
				}
			}
		}
	}



	private void initializeGridProperties() {
		
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
	
	
	public VariablePane highlightVariableValue(String _varName, long _timestamp) {

		removeGridStyles(this);
		
		int index = variableMap.get(_varName);
		Label nameLabel = (Label) this.getChildren().get(index);
		ComboBox<valuePair<Long, String>> valueBox = (ComboBox<valuePair<Long, String>>) this.getChildren().get(index + 1);
		
		Iterator iter = valueBox.getItems().iterator();
		for (int i = 0; iter.hasNext(); i++)
		{
			valuePair<Long, String> pair = (valuePair) iter.next();
			if(pair.getKey() == _timestamp)
			{
				valueBox.setValue(valueBox.getItems().get(i));
				valueBox.setStyle("-fx-background-color: #ff9999");
				nameLabel.setTextFill(Color.web("#ff9999"));
			}
		}
		return this;
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
	
	private class valuePair<k, v> extends Pair<k, v>{

		public valuePair(k key, v value) {
			super(key, value);
			// TODO Auto-generated constructor stub
		}

		@Override
		public String toString(){
			return this.getValue().toString();
		}
		
	}

	
}
