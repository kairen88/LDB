package com.live.Debugger;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.TreeMap;

import com.sun.org.apache.bcel.internal.classfile.LocalVariable;

import tod.core.database.structure.IStructureDatabase.LocalVariableInfo;

import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.RowConstraints;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.util.Callback;
import javafx.util.Pair;

public class VariablePane extends VBox{
	
	private String methodName;
	private int iteration;
	private GridPane grid; 
	
	//store variables and value history
	private LinkedHashMap<String,ArrayList> localVariables= new LinkedHashMap<>();
	private TreeMap<String, Integer> variableMap;
	

	public VariablePane(String _methodName, int _iteration, ArrayList<EventInfo> _childEventsInfo){
		
		methodName = _methodName;
		iteration = _iteration;
		variableMap = new TreeMap<String, Integer>();
		grid = new GridPane();
		
		initializeGridProperties();		
		
		populateGrid(_childEventsInfo);
		
		Label metName = new Label("Method: " + methodName);
		
		this.getChildren().add(metName);
		this.getChildren().add(grid);
	}



	private void populateGrid(ArrayList<EventInfo> _childEventsInfo) {
		for(final EventInfo eventInfo : _childEventsInfo)
		{
			//is a write event with variable value
			//object[1] contains the variable / method name of the child events
			if(eventInfo.getWriteValue() != null)
			{
				//if we have not created the corresponding element for grid variable
				if(variableMap.get(eventInfo.getVarName()) == null)
				{
					//create it and add it to the variable Pane
					
					//create value pair, timestamp : var value
					valueTriple varValue = new valueTriple((long)eventInfo.getTimestamp(), (String) eventInfo.getWriteValue(), eventInfo.getLineNumber());
					ComboBox<valueTriple> valueBox = new ComboBox<valueTriple>();
					valueBox.getItems().add(varValue);
					
					Label varNameLabel = new Label(eventInfo.getVarName().toString());
					
					valueBox.setScaleX(1);
			        valueBox.setScaleY(1);
			        
			        grid.setMargin(varNameLabel, new Insets(10, 10, 10, 10));
			        grid.setMargin(valueBox, new Insets(10, 10, 10, 10));
					
					int row = grid.getChildren().size() / 2;
					grid.add(varNameLabel, 0, row);
					grid.add(valueBox, 1, row);
					
					//setting 
					valueBox.setCellFactory(
				            new Callback<ListView<valueTriple>, ListCell<valueTriple>>() {
				                @Override public ListCell<valueTriple> call(ListView<valueTriple> param) {
				                    final ListCell<valueTriple> cell = new ListCell<valueTriple>(){
				                    	 @Override
				                         protected void updateItem(valueTriple item, boolean empty) {
				                             super.updateItem(item, empty);

				                             if (!empty) {
				                               // Use a SimpleDateFormat or similar in the format method
				                               setText(item.value());
				                             } else {
				                               setText(null);
				                             }
				                         }

				                    };
				                    
				                    cell.addEventHandler(MouseEvent.MOUSE_ENTERED, new EventHandler<MouseEvent>() {
										   public void handle(MouseEvent arg0) {

											   if(cell.getItem() != null)
											   {
												   liveDebugging.selectLineInCodeWindow(methodName, cell.getItem().timestamp(), cell.getItem().lineNum());
												   liveDebugging.setTimelineTickHighlight(methodName, cell.getItem().timestamp());
											   }
										   }
				                    });
				                    
				                    cell.addEventHandler(MouseEvent.MOUSE_EXITED, new EventHandler<MouseEvent>() {
										   public void handle(MouseEvent arg0) {

											   if(cell.getItem() != null)
											   {
												   liveDebugging.selectLineInCodeWindow(methodName, cell.getItem().timestamp(), -1);
												   liveDebugging.removeTimelineTickHighlight(methodName, cell.getItem().timestamp());
											   }
										   }
				                    });
				                    
				                    cell.addEventHandler(MouseEvent.MOUSE_CLICKED, new EventHandler<MouseEvent>() {
										   public void handle(MouseEvent arg0) {

											   liveDebugging.naviTo(cell.getItem().timestamp());
										   }
				                    });
				                    
				                return cell;
				            }
				                
				                ListCell<valueTriple> cell = new ListCell<valueTriple>() {

				                    public void updateItem(valueTriple item, boolean empty) {
				                        super.updateItem(item, empty);
				                        setText((item == null || empty) ? null : item.toString());
				                        setGraphic(null);
				                    }
				                };
				        });
					
					//store the index of the variable label element, combobox element index will be idx + 1
					variableMap.put(eventInfo.getVarName(), grid.getChildren().size() - 2);
					
				}else //get the corresponding combo box and add the value
				{
					//get the index of the label element
					int index = variableMap.get(eventInfo.getVarName());
					ComboBox<valueTriple> valueBox = (ComboBox<valueTriple>) grid.getChildren().get(index + 1);
					
					valueTriple varValue = new valueTriple((long)eventInfo.getTimestamp(), (String) eventInfo.getWriteValue(), eventInfo.getLineNumber());
					valueBox.getItems().add(varValue);		
					
				}
			}
		}
	}



	private void initializeGridProperties() {
		
		grid.setMaxSize(230, 800);
		grid.setPadding(new Insets(18, 18, 18, 18));
        grid.setGridLinesVisible(true);
        RowConstraints rowinfo = new RowConstraints();
        grid.setMinWidth(230);
        
        ColumnConstraints colInfo1 = new ColumnConstraints();
        colInfo1.setPercentWidth(40);
 
        ColumnConstraints colInfo2 = new ColumnConstraints();
        colInfo2.setPercentWidth(60);
 
        grid.getColumnConstraints().add(colInfo1); //25 percent
        grid.getColumnConstraints().add(colInfo2); //30 percent
        
 
        Label nameLabel = new Label("Name");
        GridPane.setMargin(nameLabel, new Insets(0, 5, 0, 10));
        
        GridPane.setConstraints(nameLabel, 0, 0);
        Label variableValue = new Label("Variable");
        GridPane.setMargin(variableValue, new Insets(0, 0, 0, 10));
        GridPane.setConstraints(variableValue, 1, 0); 
		
        grid.getChildren().addAll(nameLabel, variableValue);
	}


	
	public LinkedHashMap getLocalVariables() {

		return localVariables;
	}
	public void setLocalVariables(LinkedHashMap localVariables) {

		this.localVariables = localVariables;
	}
	
	
	public VariablePane highlightVariableValue(String _varName, long _timestamp) {

		removeGridStyles();
		
		if(_varName != null && variableMap.containsKey(_varName))
		{
			int index = variableMap.get(_varName);
			Label nameLabel = (Label) grid.getChildren().get(index);
			ComboBox<valueTriple> valueBox = (ComboBox<valueTriple>) grid.getChildren().get(index + 1);
			
			Iterator iter = valueBox.getItems().iterator();
			for (int i = 0; iter.hasNext(); i++)
			{
				valueTriple pair = (valueTriple) iter.next();
				if(pair.timestamp() == _timestamp)
				{
					valueBox.setValue(valueBox.getItems().get(i));
					valueBox.setStyle("-fx-background-color: #ff9999");
					nameLabel.setTextFill(Color.web("#ff9999"));
				}
			}
		}
		return this;
	}
	
	//grid method is used to change the style of grid pane
	public void removeGridStyles(){
		for(int i=3;i<grid.getChildren().size();i=i+2){
			Label label=(Label)grid.getChildren().get(i);
			ComboBox cb=(ComboBox)grid.getChildren().get(i+1);
			label.setTextFill(Color.web("#000000"));
			cb.setStyle("-fx-font-size: 10px");
			grid.getChildren().set(i,label);
			grid.getChildren().set(i+1,cb);
		}
	}
	
	private class valueTriple implements Comparable<valueTriple>{
		private long timestamp;
		private String value;
		private int lineNum;

		public valueTriple(long _timestamp, String _value, int _lineNum) {
			super();
			timestamp = _timestamp;
			value = _value;
			lineNum = _lineNum;
		}
		
		long timestamp() { return timestamp; }
		String value() { return value; }
		int lineNum() { return lineNum; }

		@Override
		public String toString(){
			return value.toString();
		}

		@Override
		public int compareTo(valueTriple o) {
		      return (int) (this.timestamp() - o.timestamp());
		}
		
	}
	
	
}
