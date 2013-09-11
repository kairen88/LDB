package com.live.Debugger;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;

import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableIntegerValue;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Worker.State;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Group;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ComboBoxBuilder;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.Label;
import javafx.scene.control.SplitPane;
import javafx.scene.control.TitledPane;
import javafx.scene.effect.ColorAdjust;
import javafx.scene.effect.Effect;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.HBoxBuilder;
import javafx.scene.layout.Pane;
import javafx.scene.layout.RowConstraints;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.Text;

public class CodeWindow extends DraggableNode{

	private static String pinUnPressPath = CodeWindow.class.getResource("plus.png").toExternalForm();
    private static Image plus= new Image(pinUnPressPath);

	private static String pinPressPath = CodeWindow.class.getResource("minus.png").toExternalForm();
    private static Image minus= new Image(pinPressPath);
    private static String status="";
    private ComboBox iterationBox; 
    private ImageView imageView;
    private Label label;
    private Button pinBtn;
    private CodeEditor editor;
	private Rectangle cwBackground;
	private TitledPane edit;//may not be used
	private DraggableNode codeWindowContainer;
	
	private LinkedHashMap<String,ArrayList> localVariables= new LinkedHashMap<>();//store variables and value history
	private ArrayList<LinkedHashMap<String,ArrayList>> localVariablesList= new ArrayList<>();//list of localVariables hashmaps to correspond the current iteration of the window
	private String methodName;
	SimpleIntegerProperty selectedLineNumber = new SimpleIntegerProperty(0);//linenumber of the method call
	SimpleIntegerProperty lineoffset = new SimpleIntegerProperty(9); //amount to offset the position of the arrow for each line according to selectedLineNumber
	
	private int windowWidth;
	private int windowHeight;
	private int padding = 10;
	private int paddingTop = 15;
	
	private int currentExecutionLine = 0;
	private int instance=0;//This will determine how many instances of same method name are opened
	
	private int startLine=0;//line offset, to match actual line number to code editor line number
	private int endLine=0;
	
	//column values for highlighting sections of a line
	private int prevStartCol = -1;
	private int prevEndCol = -1;
	
	private GridPane gridPane=new GridPane();
	private ArrayList<GridPane> gridPaneList=new ArrayList<GridPane>();
	private liveDebugging ld;//to use reposition logic, etc.
	
	private final ObservableList strings = FXCollections.observableArrayList(
			"1");//may not be used
	
	private int indexOnScreen;
			
	double mousex=0;//may not be used
	 double mousey=0;//may not be used
	 
	 double x =0;
	 double y=0;
	 private Group hbox=new Group(); 
	
	public ImageView getPinBtn(){
			
			return this.imageView;
		}

	public void setPinBtn(String name){
			if(name.equals("plus")){
				ImageView img=new ImageView(plus);
        		//pinBtn.setGraphic(img);
        		imageView=img;
			}
			else{
				ImageView img=new ImageView(minus);
        		//pinBtn.setGraphic(img);
				imageView=img;
			}
		}
	
   public SimpleDoubleProperty getDraggableX(){
       return super.x;
   }
   
   public SimpleDoubleProperty getDraggableY(){
       return super.y;
   }	
	 
		public int getWindowWidth(){
			
			return this.windowWidth;
		}
		
		public int getWindowHeight(){
			
			return this.windowHeight;
		}
		
	public void setEditor(CodeEditor e){
		
		this.editor=e;
	}
	public CodeEditor getEditor(){
		
		return this.editor;
	}
	
	//increment the current iteration in the iteration combo box
	public void incrementIteration()
	{
		int currentIteration = iterationBox.getItems().size() + 1;
		iterationBox.getItems().add(currentIteration);
		
		//set the last item in the iteration Box as it's displayed value
		iterationBox.setValue( iterationBox.getItems().get(iterationBox.getItems().size() - 1) );
	}
	
public void setIterationBox(ComboBox e){
		
		this.iterationBox=e;
	}
	public ComboBox getIterationBox(){
		
		return this.iterationBox;
	}
public void setCodeWindowContainer(DraggableNode e){
		
		this.codeWindowContainer=e;
	}
	public DraggableNode getCodeWindowContainer(){
		
		return this.codeWindowContainer;
	}
	public void initializeGrid(){
		gridPane.setPadding(new Insets(18, 18, 18, 18));
        gridPane.setGridLinesVisible(true);
        RowConstraints rowinfo = new RowConstraints();
        rowinfo.setPercentHeight(50);
        
        ColumnConstraints colInfo1 = new ColumnConstraints();
        colInfo1.setPercentWidth(40);
 
        ColumnConstraints colInfo2 = new ColumnConstraints();
        colInfo2.setPercentWidth(60);
 
        gridPane.getRowConstraints().add(rowinfo);//2*50 percent
        gridPane.getColumnConstraints().add(colInfo1); //25 percent
        gridPane.getColumnConstraints().add(colInfo2); //30 percent
        
 
        Label nameLabel = new Label("Name");
        GridPane.setMargin(nameLabel, new Insets(0, 5, 0, 10));
        GridPane.setConstraints(nameLabel, 0, 0);
        Label variableValue = new Label("Variable");
        GridPane.setMargin(variableValue, new Insets(0, 0, 0, 10));
        GridPane.setConstraints(variableValue, 1, 0);
        gridPane.getChildren().addAll(nameLabel, variableValue);
	}
	public CodeWindow(String editingCode, int _windowWidth, int _windowHeight,String methodName, liveDebugging ldInstance) {
		//initialize window dimensions
		//set code window size, min 300 by 300
		ld=ldInstance;
		if(_windowWidth < 600)
			this.windowWidth = 600;
		else
			this.windowWidth = _windowWidth;
		
		/*if(_windowHeight < 600)
			this.windowHeight= 600;
		else*/
			this.windowHeight= _windowHeight;
					
		//construct the code window
		constructCodeWindow(editingCode,methodName);	
		initializeGrid();
		
		//add change listener to run scripts when webform is loaded		
//		addWebviewLoadedChangeListener();
	}
	/*
	//overloaded constructor that takes path and loads code from file
	public CodeWindow(Path _editingCodePath, int _windowWidth, int _windowHeight) {
		//initialize window dimensions
		//set code window size, min 300 by 300
		if(_windowWidth < 600)
			this.windowWidth = 600;
		else
			this.windowWidth = _windowWidth;

			this.windowHeight= _windowHeight;
		
		//load the code from file
		String editingCode = getCodeFromFile(_editingCodePath);
		
		//construct the code window
		constructCodeWindow(editingCode, "old");	
		
		initializeGrid();
		//add change listener to run scripts when webform is loaded		
//		addWebviewLoadedChangeListener();
	}
	*/
	//public methods-----------------------------------------------------------
	
	//returns the root/container node for the code window which is a draggable node
	public DraggableNode getRootNode()
	{
		return codeWindowContainer;	
	}
	
	 
	public void reduceWindowSize()
	{
		if(!this.editor.isReduced()){
		this.windowHeight=(int)((this.windowHeight)*0.3);
		this.windowWidth=(int)(this.windowWidth*0.3);
		
		this.cwBackground.setWidth(this.windowWidth+ 20);
		this.cwBackground.setHeight(this.windowHeight+ 40);
		
    
		this.editor.webview.setPrefSize(this.windowWidth, this.windowHeight);
		this.editor.webview.setMinSize(this.windowWidth, this.windowHeight);
		
		this.lineoffset.setValue(0);

		//resizing grid pane - grip pane contains method name label, iternation box and pin
		GridPane gp=((GridPane)(this.hbox.getChildren().get(0)));
		gp.setPrefSize(this.windowWidth, 30);

	        gp.setGridLinesVisible(false);

	        HBox labelHbox=(HBox)gp.getChildren().get(0);
	        HBox infoHBox=(HBox)gp.getChildren().get(1);
	        GridPane.setMargin(labelHbox, new Insets(0, 5, 0, 5));
	        GridPane.setConstraints(labelHbox, 0, 0);
	        GridPane.setMargin(infoHBox, new Insets(0, 0, 0, 0));
	        GridPane.setConstraints(infoHBox, 1, 0);
	 
			gp.getChildren().set(0,labelHbox);
			gp.getChildren().set(1,infoHBox);
			
			this.hbox.getChildren().set(0,gp);
	        
		editor.webview.getEngine().executeScript("editor.reduceWindowSize();");
		
		editor.webview.setPrefWidth(this.windowWidth);
		editor.webview.setPrefHeight(this.windowHeight);
		editor.webview.setMinSize(this.windowWidth, this.windowHeight);
		this.editor.setReduced(true);
		codeWindowContainer.setDSize(this.windowWidth, this.windowHeight);
		}
		
		
	}
	public void normalWindowSize()
	{
		if(this.editor.isReduced()){
		this.windowHeight=(int)((this.windowHeight)/0.3);
		this.windowWidth=600;
		
		this.cwBackground.setWidth(this.windowWidth+ 20);
		this.cwBackground.setHeight(this.windowHeight+ 40);
		
    
		this.editor.webview.setPrefSize(this.windowWidth, this.windowHeight);
		this.editor.webview.setMinSize(this.windowWidth, this.windowHeight);
		
		this.lineoffset.setValue(9);
		
		//this.hbox.relocate(codeWindowContainer.getLayoutX()+510, codeWindowContainer.getLayoutY());  
		//this.hbox.relocate(codeWindowContainer.getLayoutX()+5, codeWindowContainer.getLayoutY()+5);
	
		//((HBox)(this.hbox.getChildren().get(1))).relocate(codeWindowContainer.getDWidth()-25, codeWindowContainer.getLayoutY()+5);
		editor.webview.getEngine().executeScript("editor.normalWindowSize();");
		this.editor.setReduced(false);

		editor.webview.setPrefWidth(this.windowWidth);
		editor.webview.setPrefHeight(this.windowHeight);
		editor.webview.setMinSize(this.windowWidth, this.windowHeight);
		codeWindowContainer.setDSize(this.windowWidth, this.windowHeight);
		
		
		GridPane gp=((GridPane)(this.hbox.getChildren().get(0)));
		gp.setPrefSize(this.windowWidth, 30);
		//gp.setPadding(new Insets(18, 18, 18, 18));
	        gp.setGridLinesVisible(false);
	        /*RowConstraints rowinfo = new RowConstraints();
	        rowinfo.setPercentHeight(30);
	        
	        ColumnConstraints colInfo1 = (ColumnConstraints)gp.getColumnConstraints().get(0);
	        colInfo1.setPercentWidth(90);
	 
	        ColumnConstraints colInfo2 = (ColumnConstraints)gp.getColumnConstraints().get(0);
	        colInfo2.setPercentWidth(10);
	 
	        gp.getRowConstraints().add(rowinfo);//2*50 percent
	         //25 percent
	        gp.getColumnConstraints().add(colInfo2); //30 percent
	         */
	        
	        HBox labelHbox=(HBox)gp.getChildren().get(0);
	        HBox infoHBox=(HBox)gp.getChildren().get(1);
	        GridPane.setMargin(labelHbox, new Insets(0, 5, 0, 5));
	        GridPane.setConstraints(labelHbox, 0, 0);
	        GridPane.setMargin(infoHBox, new Insets(0, 0, 0, 39));
	        GridPane.setConstraints(infoHBox, 1, 0);
	 
			gp.getChildren().set(0,labelHbox);
			gp.getChildren().set(1,infoHBox);
		
			this.hbox.getChildren().set(0,gp);
		}
		
		//98FB98
	}
	//sets the class for the line number indicated to completedLine which styles it green
		public void highlightGutters(ArrayList<Integer> lineNumList, int offset)
		{
			for(int lineNum : lineNumList)
				editor.webview.getEngine().executeScript("editor.setMarker(" + String.valueOf(lineNum - offset - 1) + ",'<div height=10 width=10 style=\"background-color:#A3FF7F;\"> %N%');");
			//98FB98
		}
		
	//highlights a section of the current line
		//**currently this method works, but info from tod is returning 1, 1 for start and end, so no highlight appears
	public void highlightSection(int lineNum, int startChar, int endChar)
	{
		//if there was another section highlighted, remove the highlight
		if(prevStartCol != -1 && prevEndCol != -1)
		{
			editor.webview.getEngine().executeScript("var start = {line:" + String.valueOf(lineNum) + ",ch:" + String.valueOf(prevStartCol) + "};" +
					"var end = {line:" + String.valueOf(lineNum) + ", ch:" + String.valueOf(prevEndCol) + "};" +
					"editor.markText(start,end,\"CodeMirror-original-background\");");
			//set current section as previous
			prevStartCol = startChar;
			prevEndCol = endChar;
		}
		//highlihgt section on line
		editor.webview.getEngine().executeScript("var start = {line:" + String.valueOf(lineNum) + ",ch:" + String.valueOf(startChar) + "};" +
												"var end = {line:" + String.valueOf(lineNum) + ", ch:" + String.valueOf(endChar) + "};" +
												"editor.markText(start,end,\"CodeMirror-LineSection-highlight\");");		
	}
	
	//set CodeWindow background color to Red
	public void setBackgroundColorToMain()
	{		
		setBackgroundColor("FF8C73");
	}
		
	//set CodeWindow background color to Green
	public void setBackgroundColorToCurrent()
	{		
		setBackgroundColor("A3FF7F");
	}
	
	//set CodeWindow background color to Yellow
	public void setBackgroundColorToPrevious()
	{		
		setBackgroundColor("FFFB78");
	}
	
	//set CodeWindow background color to Grey - oldPreviousWindow
	public void setBackgroundColorToInactive()
	{		
		setBackgroundColor("CCCCCC");
	}
	
	//sets the class for the line number indicated to completedLine which styles it white
	public void setLineColorToCompleted(int lineNum)
	{
		editor.webview.getEngine().executeScript("editor.setLineClass(" + String.valueOf(lineNum) + ", null, 'completedLine');");
		//98FB98
	}
	
	
	//sets the class for the line number indicated to currentLine which styles it yellow
	public void setLineColorToCurrent(int lineNum)
	{
		editor.webview.getEngine().executeScript("editor.setLineClass(" + String.valueOf(lineNum) + ", null, 'currentLine');");
	}
	
	//sets the class for the line number indicated to newLine which styles it red - for method calls
	public void setLineColorToMethodCall(int lineNum)
	{
		editor.webview.getEngine().executeScript("editor.setLineClass(" + String.valueOf(lineNum) + ", null, 'newLine');");
	}
	
	//execute script on editor webview
	public void runScriptOnWebForm(String _script)
	{
		editor.webview.getEngine().executeScript(_script);
	}
	
	//return current execution line
	public int getCurrentExecutionLine()
	{
		return currentExecutionLine;
	}
	
	//sets current execution line
	public void setCurrentExecutionLine(int newLineNum)
	{
		this.currentExecutionLine = newLineNum ;
	}
	
	//increment the current execution line by 1
	public void incrementCurrentExecutionLine()
	{
		this.currentExecutionLine += 1;
	}
	
	//decrement the current execution line by 1
	public void decrementCurrentExecutionLine()
	{
		this.currentExecutionLine -= 1;
	}
	
	public int getLineCount()
	{
		Object codeLineCount = editor.webview.getEngine().executeScript("editor.lineCount();");
		return (int) codeLineCount;
	}
	
	public void setMethodName(String _methodName)
	{
		methodName = _methodName;
	}
	
	public String getMethodName()
	{
		return methodName;
	}

	/*public int getInstance() {
		return instance;
	}

	public void setInstance(int instance) {
		this.instance = instance;
	}*/
	
	public int getStartLine() {
		return startLine;
	}
	public void setStartLine(int startLine) {
		this.startLine = startLine;
	}
	public int getEndLine() {
		return endLine;
	}
	public void setEndLine(int endLine) {
		this.endLine = endLine;
	}
	//private helper methods-----------------------------------------------------------

	
	private Rectangle createCodeWindowBackground()
	{
		Rectangle codeWindowBackground = new Rectangle(windowWidth, windowHeight);
		javafx.scene.paint.Paint codeMirrorBackgroundColor = javafx.scene.paint.Paint.valueOf("CCCCCC");//F9FCAC
		codeWindowBackground.setFill(codeMirrorBackgroundColor);
		codeWindowBackground.setArcHeight(15);
		codeWindowBackground.setArcWidth(15);
		return codeWindowBackground;
	}
	
	//helper method to set the background color (of the rectangle)
	private void setBackgroundColor(String _color)
	{
		javafx.scene.paint.Paint codeMirrorBackgroundColor = javafx.scene.paint.Paint.valueOf(_color);//F9FCAC

		cwBackground.setFill(codeMirrorBackgroundColor);
	}
	
	private void addWebviewLoadedChangeListener()
	{
		//add change listener to run scripts when webform is loaded		
				this.editor.webview.getEngine().getLoadWorker().stateProperty().addListener(
				        new ChangeListener<State>() {
				            public void changed(ObservableValue ov, State oldState, State newState) {
				                if (newState == State.SUCCEEDED) {
				                	//run scripts when webform loaded
				                	//set all lines to class newLine, red		                	
				                	
				                }
				            }
				        });
	}
	
	private void constructCodeWindow(String editingCode, String methodName)
	{
		
		codeWindowContainer = new DraggableNode();
		
		//checking for code string
		if(editingCode == null)
			editingCode = "";
		
		//initialize codeMirror editor
		//padding for editor is 2*padding, 2*padding + paddingTop
		editor = new CodeEditor(editingCode, windowWidth - (padding * 2), windowHeight - (padding * 2 + paddingTop) );
		
		//creating code window background
		Rectangle codeWindowBackground = createCodeWindowBackground();
		cwBackground= codeWindowBackground;
		
		
		//positioning editor with padding
		editor.relocate(padding, padding + paddingTop);
		
		iterationBox = ComboBoxBuilder.create()
				.id("cwPin")
				.promptText("1").build();
				//.items(FXCollections.observableArrayList(strings.subList(0,0))).build();
		iterationBox.setScaleX(0.6);
		iterationBox.setScaleY(0.6);
		iterationBox.getItems().add(1);
		iterationBox.setValue(1);
		
		imageView = new ImageView(minus);
		//status="canReduce";
		Font f=new Font(11);
		label = new Label(methodName);//, imageView);
		label.setContentDisplay(ContentDisplay.LEFT);
		label.setFont(f);
		label.setTextFill(Color.web("#0076a3"));
		
		pinBtn = new Button();
		pinBtn.setGraphic(imageView);
		pinBtn.setScaleX(1);
		pinBtn.setScaleY(1);
		pinBtn.setFont(f);
		pinBtn.setTextFill(Color.web("#0076a3"));
		pinBtn.setMinSize(20, 15);
		pinBtn.setMaxSize(20, 15);
		
		//imageView.setOnAction(new EventHandler<ActionEvent>() {
		    imageView.setOnMouseClicked(new EventHandler<MouseEvent>() {
                @Override
                public void handle(MouseEvent actionEvent) {
                 //   Platform.exit();
                	ImageView img;
                	
                	if(!editor.isReduced()){
                		img=new ImageView(plus);
                		imageView=img;
                		//pinBtn.setGraphic(img);
                		//status="cannotReduce";
                		reduceWindowSize();
                			
                    	ld.reposition();
                	}
                	else{
                	//status="canReduce";
                	img=new ImageView(minus);
                	imageView=img;
                	//pinBtn.setGraphic(img);
                	normalWindowSize();
                	ld.reposition();
                	}
                }
            });

            //is.getChildren().add(closeBtn);
        HBox labelHbox=HBoxBuilder.create().alignment(Pos.TOP_LEFT).spacing(5).build();
        labelHbox.getChildren().addAll(label,iterationBox);
        
        HBox  infoHBox= HBoxBuilder.create().alignment(Pos.TOP_RIGHT).spacing(5).build();
        infoHBox.getChildren().add(imageView);
        //infoHBox.relocate(codeWindowContainer.getLayoutX()+5, codeWindowContainer.getLayoutY()+5);

        GridPane gp=new GridPane();
        gp.setPrefSize(this.windowWidth, 30);
		//gp.setPadding(new Insets(18, 18, 18, 18));
        gp.setGridLinesVisible(false);
        RowConstraints rowinfo = new RowConstraints();
        rowinfo.setPercentHeight(30);
        
        ColumnConstraints colInfo1 = new ColumnConstraints();
        colInfo1.setPercentWidth(85);
 
        ColumnConstraints colInfo2 = new ColumnConstraints();
        colInfo2.setPercentWidth(15);
 
        gp.getRowConstraints().add(rowinfo);//2*50 percent
        
        gp.getColumnConstraints().add(colInfo1); //25 percent
        gp.getColumnConstraints().add(colInfo2); //30 percent
        
        GridPane.setMargin(labelHbox, new Insets(0, 0, 0, 0));
        GridPane.setConstraints(labelHbox, 0, 0);
        GridPane.setMargin(infoHBox, new Insets(0, 0, 0, 0));
        GridPane.setConstraints(infoHBox, 1, 0);
 
		
        gp.getChildren().addAll(labelHbox, infoHBox);
        
        hbox.getChildren().addAll(gp);
        this.getChildren().add(codeWindowBackground);
        this.getChildren().add(hbox);
		hbox.relocate(this.getLayoutX()+5, this.getLayoutY()+5);
		//infoHBox.relocate(codeWindowContainer.getDWidth()-25, codeWindowContainer.getLayoutY()+5);
		//hbox.toFront();
		//add code window to root draggable node
		
		//codeWindowBackground.toBack();
		
		//Group groupCont=new Group();
		this.getChildren().add(editor);
		 
		this.setDSize(this.windowWidth, this.windowHeight);

		//infoHBox.relocate(codeWindowContainer.getLayoutX()+490, codeWindowContainer.getLayoutY());

	}
	
	private String getCodeFromFile(Path _editingCodePath)
	{
		//read in code from file
		String editingCode  = "";
		String filePath = _editingCodePath.toString();
		  try{
			  // Open the file that is the first command line parameter
			  FileInputStream fstream = new FileInputStream(filePath);
			  // Get the object of DataInputStream
			  DataInputStream in = new DataInputStream(fstream);
			  BufferedReader br = new BufferedReader(new InputStreamReader(in));
			  String strLine;
			  //Read File Line By Line
			  while ((strLine = br.readLine()) != null)   
			  {
	    		  //append to string obj template
				  editingCode += strLine + "\n";
			  }
			  //Close the input stream
			  in.close();

		    }catch (Exception e)
		    {
		    	//Catch exception if any
		    	System.err.println("Error: " + e.getMessage());
		    }
		  
		  return editingCode;
	}
	public TitledPane getEdit() {
		return edit;
	}
	public void setEdit(TitledPane edit) {
		this.edit = edit;
	}
	public LinkedHashMap getLocalVariables() {
		int value=Integer.parseInt(iterationBox.getValue().toString());
		
		if(localVariablesList.size()>0 && value<=localVariablesList.size()){
				localVariables=localVariablesList.get(value-1);
		}
		else{
			localVariables= new LinkedHashMap<>();
		}
		return localVariables;
	}
	public void setLocalVariables(LinkedHashMap localVariables) {
		int value=Integer.parseInt(iterationBox.getValue().toString());
		if(localVariablesList.size()>0){
			if(localVariablesList.size()>=value){
				localVariablesList.set(value-1,localVariables);
			}
			else{
				localVariablesList.add(value-1,localVariables);
			}
		}
		else{
			localVariablesList.add(value-1,localVariables);
		}
		this.localVariables = localVariables;
	}
	public int getExecutedLine()
	{
		return currentExecutionLine;
	}
	public void setExecutedLine(int _line)
	{
		currentExecutionLine = _line;
	}
	public int getSelectedLineNumber() {
		return selectedLineNumber.get();
	}
	public void setSelectedLineNumber(int selectedLineNumber) {
		this.selectedLineNumber.setValue(selectedLineNumber);
	}/*
	public GridPane getGridPane() {
		return gridPane;
	}*/
	public void setGridPane(GridPane gridPane) {
		gridPaneList.add((int)iterationBox.getValue()-1,gridPane);
		this.gridPane = gridPane;
	}

	public GridPane getGridPane() {
		gridPane = initGridPane();
		int value=Integer.parseInt(iterationBox.getValue().toString());
		if(gridPaneList.size()>0 && value<=gridPaneList.size()){
				gridPane=gridPaneList.get(value-1);
		}
		return gridPane;
	}
	
	public GridPane initGridPane(){
		GridPane gridPane=new GridPane();
	    gridPane.setMaxSize(230, 800);
		gridPane.setPadding(new Insets(18, 18, 18, 18));
        gridPane.setGridLinesVisible(true);
        RowConstraints rowinfo = new RowConstraints();
        rowinfo.setPercentHeight(50);
        
        ColumnConstraints colInfo1 = new ColumnConstraints();
        colInfo1.setPercentWidth(40);
 
        ColumnConstraints colInfo2 = new ColumnConstraints();
        colInfo2.setPercentWidth(60);
 
        gridPane.getRowConstraints().add(rowinfo);//2*50 percent
        //gridPane.getRowConstraints().add(rowinfo);
 
        gridPane.getColumnConstraints().add(colInfo1); //25 percent
        gridPane.getColumnConstraints().add(colInfo2); //30 percent
        
 
        Label nameLabel = new Label("Name");
        GridPane.setMargin(nameLabel, new Insets(0, 5, 0, 10));
        //GridPane.setHalignment(nameLabel, HPos.LEFT);
        
        GridPane.setConstraints(nameLabel, 0, 0);
        Label variableValue = new Label("Variable");
        GridPane.setMargin(variableValue, new Insets(0, 0, 0, 10));
        GridPane.setConstraints(variableValue, 1, 0);
 
		
        gridPane.getChildren().addAll(nameLabel, variableValue);
        return gridPane;
	}

	public int getIndexOnScreen() {
		return indexOnScreen;
	}

	public void setIndexOnScreen(int indexOnScreen) {
		this.indexOnScreen = indexOnScreen;
	}



	public ArrayList<GridPane> getGridPaneList() {
		return gridPaneList;
	}



	public void setGridPaneList(ArrayList<GridPane> gridPaneList) {
		this.gridPaneList = gridPaneList;
	}




}
