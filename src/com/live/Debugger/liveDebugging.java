
package com.live.Debugger;

import java.awt.Dimension;
import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URI;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Set;
import java.util.Stack;

import javax.swing.JComponent;
import javax.swing.JFrame;

import org.python.parser.ast.Print;

import com.sun.javafx.scene.layout.region.Margins.Converter;

import tod.core.config.TODConfig;
import tod.core.database.browser.IEventBrowser;
import tod.core.database.browser.ILogBrowser;
import tod.core.database.browser.LocationUtils;
import tod.core.database.browser.Stepper;
import tod.core.database.event.IBehaviorCallEvent;
import tod.core.database.event.ICallerSideEvent;
import tod.core.database.event.ILogEvent;
import tod.core.database.structure.IBehaviorInfo;
import tod.core.database.structure.ILocationInfo;
import tod.core.database.structure.IBehaviorInfo.BytecodeRole;
import tod.core.database.structure.IStructureDatabase.LineNumberInfo;
import tod.core.database.structure.IStructureDatabase.LocalVariableInfo;
import tod.core.database.structure.SourceRange;
import tod.core.database.structure.IStructureDatabase.ProbeInfo;
import tod.core.session.ISession;
import tod.core.session.SessionTypeManager;
import tod.gui.IExtensionPoints;
import tod.gui.MinerUI;
import tod.gui.StandaloneUI;
import tod.gui.SwingDialogUtils;
import tod.gui.IGUIManager.DialogType;
import tod.gui.components.eventlist.IntimacyLevel;
import tod.impl.common.event.BehaviorExitEvent;
import tod.impl.dbgrid.event.MethodCallEvent;
import tod.impl.local.EventBrowser;
import zz.utils.notification.IEvent;

import netscape.javascript.JSObject;

import javafx.application.Application;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.geometry.Pos;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.ScrollBar;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Slider;
import javafx.scene.control.Tooltip;
import javafx.scene.effect.ColorAdjust;
import javafx.scene.effect.Lighting;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.RowConstraints;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Line;
import javafx.scene.shape.Polygon;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.stage.Stage;

public class liveDebugging extends Application {

	private Parent root = null;
	private static String[] _args;

	int currentWindowIdx = 0;

	ArrayList<CodeWindow> codeWindowAry;

	Stack<CodeWindow> codeWindowStack;
	Stack<timeline> timelineStack;
	
	CodeWindow currentCodeWindow;
	CodeWindow prevCodeWindow;
	
	timeline currentTimeline;
	timeline prevTimeline;	

	
	Pane codeWindowAreaNew;
	Pane codeWindowArea=new Pane ();

	EventUtils eventUtils;
	CodeFragments codeFragments;

	int lineNumberOffset;
//	ArrayList<Long> childEventsTimestamps;
	private HashMap displayedCodeWindowsList; //list of code windows currently displayed on screen
	private ArrayList<CodeWindow> CodeWindowCallStack; //maintains a list that represents the call stack
	Pane timelineSectionNew;
	Pane timelineSection=new Pane ();

	//timeline positions
	double posX=37;
	int posY=0;
	Pane variablePane =new Pane();

	//for Highlighting
	CodeWindow mainCWH;
	timeline mainTH;

	ScrollPane s1 = new ScrollPane();
	GridPane gridPane=new GridPane(); 
	RowConstraints rowinfo = new RowConstraints();	

	static final private String editingCode = "import javafx.application.Application;\n"
			+ "import javafx.scene.Scene;\n"
			+ "import javafx.scene.web.WebView;\n"
			+ "import javafx.stage.Stage;\n"
			+ "\n"
			+ "/** Sample code editing application wrapping an editor in a WebView. */\n"
			+ "public class CodeEditorExample extends Application {\n"
			+ "  public static void main(String[] args) { launch(args); }\n"
			+ "  @Override public void start(Stage stage) throws Exception {\n"
			+ "    WebView webView = new WebView();\n"
			+ "    webView.getEngine().load(\"http://codemirror.net/mode/groovy/index.html\");\n"
			+ "    final Scene scene = new Scene(webView);\n"
			+ "    webView.prefWidthProperty().bind(scene.widthProperty());\n"
			+ "    webView.prefHeightProperty().bind(scene.heightProperty());\n"
			+ "    stage.setScene(scene);\n"
			+ "    stage.show();\n"
			+ "  }\n"
			+ "}";

	public static void main(String[] args) {
		launch(args);
		_args = args;
	}
//	private Rectangle createCodeWindowBackground(int windowWidth,int windowHeight,String color)
//	{
//		Rectangle codeWindowBackground = new Rectangle(windowWidth, windowHeight);
//		javafx.scene.paint.Paint codeMirrorBackgroundColor = javafx.scene.paint.Paint.valueOf("CCCCCC");//grey
//		codeWindowBackground.setFill(codeMirrorBackgroundColor);
//		codeWindowBackground.setArcHeight(15);
//		codeWindowBackground.setArcWidth(15);
//
//		return codeWindowBackground;
//	}

	public void highlighter(CodeWindow cd , timeline tl,ColorAdjust c, String color){
		
		if(cd!=null&&tl!=null){
			
			Rectangle r=(Rectangle)(cd.getCodeWindowContainer().getChildren().get(0));
			javafx.scene.paint.Paint codeMirrorBackgroundColor = javafx.scene.paint.Paint.valueOf(color);//F9FCAC
			r.setFill(codeMirrorBackgroundColor);
			
			cd.getCodeWindowContainer().getChildren().set(0,r);
			Object group=(cd.getCodeWindowContainer().getChildren().get(1));
			Object codeEditor=(cd.getCodeWindowContainer().getChildren().get(2));
			
			tl.setColor(color);//comment out for now to implement new timeline
		}
	}
	public GridPane initGridPane(){
		gridPane=new GridPane();
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

	@Override
	public void start(Stage primaryStage) throws IOException {
		// FXMLLoader fxmlLoader = new FXMLLoader();
		root = FXMLLoader.load(getClass().getClassLoader().getResource(
				"LDB_fxml.fxml"));

		Path classPath = FileSystems.getDefault().getPath("resource",
				"sampleJs.txt");//sampleJs
		
		eventUtils = new EventUtils();
		codeFragments = new CodeFragments(classPath, this);

		currentCodeWindow = null;
		prevCodeWindow = null;
		lineNumberOffset = 0;
		codeWindowStack = new Stack<>();
		timelineStack=new Stack<>();
		displayedCodeWindowsList = new HashMap<>();
		CodeWindowCallStack = new ArrayList<CodeWindow>();
		//vb.setSpacing(5);

		codeWindowAry = new ArrayList<CodeWindow>();

		Path path = FileSystems.getDefault()
				.getPath("resource", "sampleJs.txt");



		//StackPane codeWindowSection = (StackPane) getRootAnchorPane().lookup(
				//"#codeWindowSection");
		codeWindowAreaNew = (Pane) getRootAnchorPane().lookup(
				"#codeWindowSection");//new StackPane();
		codeWindowArea = new Pane ();		
		
		/*
		 * //adding 1st code window setting it as current CodeWindow editor =
		 * new CodeWindow(path, 300, 300);
		 */

		createMainWindow("main");
		mainCWH=currentCodeWindow;

		/*      
        Label acctLabel = new Label("Member Number:");
        GridPane.setHalignment(acctLabel, HPos.RIGHT);
        GridPane.setConstraints(acctLabel, 0, 1);
        TextField textBox = new TextField("Your number");
        GridPane.setMargin(textBox, new Insets(10, 10, 10, 10));
        GridPane.setConstraints(textBox, 1, 1);
 
        Button button = new Button("Help");
        GridPane.setConstraints(button, 2, 1);
        GridPane.setMargin(button, new Insets(10, 10, 10, 10));
        GridPane.setHalignment(button, HPos.CENTER);
 
        GridPane.setConstraints(condValue, 1, 0);
   */



		//codeWindowSection.getChildren().add(codeWindowArea);

		// -------------------------------------------------------------
		timelineSectionNew = (Pane) getRootAnchorPane().lookup(
				"#naviBarSection");
		
		variablePane = (Pane) getRootAnchorPane().lookup(
				"#VariablePane");
		initGridPane();
        variablePane.getChildren().add(gridPane);

		ScrollPane sc = new ScrollPane();
		sc.setPrefSize(1830, 200);
		sc.setContent(timelineSection);
		sc.setVbarPolicy(ScrollPane.ScrollBarPolicy.ALWAYS);
		sc.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
		
		
		timelineSectionNew.getChildren().add(sc);
		ILogEvent mainEvent = getMainMethodEvent();
		timeline tLine = createTimeLine(mainEvent,"main");

		currentTimeline = tLine;
//		curProgIndicator=(ProgressIndicator)obj[1];

		mainTH=currentTimeline;


		ColorAdjust c=new ColorAdjust(-0.047,0.793,0.476,0);//0.476
		String color="FF8C73"; //red
		highlighter(mainCWH,mainTH,c,color);
		timelineStack.push(currentTimeline);
		// --------------------------------------------------------
		initializeElementControl();

		Scene s = new Scene(root);
		java.net.URL url=liveDebugging.class.getResource("SDB.css");
		s.getStylesheets().add(url.toExternalForm());
		primaryStage.setTitle("Swift Debugger");
		primaryStage.setScene(s);
		primaryStage.setWidth(1830);
		primaryStage.setHeight(1000);
		primaryStage.show();
/*		
		ILogEvent curEvent = eventUtils.getCurrentEvent();
		int clineNum = eventUtils.getLineNum(curEvent);
		currentCodeWindow.setLineColorToCurrent(clineNum- lineNumberOffset-1);// should be this but currently i haven't set main
		setTick(currentTimeline, curProgIndicator);
*/		


	}

	private AnchorPane getRootAnchorPane() {
		AnchorPane pane = (AnchorPane) root.lookup("#AnchorPane");
		return pane;
	}

	private void addDraggableElementToRoot(DraggableNode node) {
		AnchorPane root = getRootAnchorPane();
		root.getChildren().add(node);
	}

	private void addElementToRoot(Node node) {
		AnchorPane root = getRootAnchorPane();
		root.getChildren().add(node);
	}

	// need to find a way to get genric type for parent, for now use pane
	public void addElement(ScrollPane Parent, Node node) {
		if (Parent != null) {
			Parent.setContent(node);
		}
	}

//	private ObservableList<HBox> getArray() {
//		ObservableList<HBox> vertArray = FXCollections.observableArrayList();
//		ArrayList<String[]> list = getInputFromFile();
//
//		for (String[] strAry : list) {
//			HBox hBox = new HBox();
//			hBox.setStyle("-fx-background-color: #ECC3BF");
//			for (String string : strAry) {
//				Label label = new Label(string);
//				hBox.getChildren().add(label);
//			}
//			vertArray.add(hBox);
//		}
//
//		// hBox2.setStyle("-fx-background-color: #ECC3BF");
//		return vertArray;
//	}
//
//	private ArrayList<String[]> getInputFromFile() {
//		ArrayList<String[]> list = new ArrayList<String[]>();
//		try {
//			// Open the file that is the first
//			// command line parameter
//			FileInputStream fstream = new FileInputStream("textfile.txt");
//			// Get the object of DataInputStream
//			DataInputStream in = new DataInputStream(fstream);
//			BufferedReader br = new BufferedReader(new InputStreamReader(in));
//			String strLine;
//			// Read File Line By Line
//			while ((strLine = br.readLine()) != null) {
//				// Print the content on the console
//				// System.out.println (strLine);
//				String[] strAry = strLine.split(" ");
//
//				list.add(strAry);
//			}
//			// Close the input stream
//			in.close();
//
//		} catch (Exception e) {
//			// Catch exception if any
//			System.err.println("Error: " + e.getMessage());
//		}
//		return list;
//	}

	// displaying main code
	private void createMainWindow(String methodName) {

		if (codeFragments.codeFragmentExist(methodName)) {
			// get the code fragment and add it to the code area
			CodeWindow editor = codeFragments.getCodeFragment(methodName);
			codeWindowArea.getChildren().add(editor.getRootNode());

			// set line number offset
			lineNumberOffset = codeFragments.getLineNumberOffset(methodName);

			codeWindowAry.add(editor);

			currentCodeWindow = editor;
		    s1.setPrefSize(1600, 800);
			s1.setVbarPolicy(ScrollPane.ScrollBarPolicy.ALWAYS);
			s1.setHbarPolicy(ScrollPane.ScrollBarPolicy.ALWAYS);
			s1.setContent(codeWindowArea);
			codeWindowAreaNew.getChildren().add(s1);
			displayedCodeWindowsList.put(methodName, 0);
			CodeWindowCallStack.add(editor);
			codeWindowStack.push(currentCodeWindow);
			
						
//			ILogEvent curEvent = eventUtils.getCurrentEvent();
			//currentCodeWindow.getPinBtn().setVisible(false);
			//ILogEvent nextEvent= eventUtils.forwardStepInto();
//			highlightGutters(curEvent);
		}
	}
	
	//This method is used to highlight gutters in the editor
	private void highlightGutters(ILogEvent event){
		ArrayList<Integer> lineNumbers =eventUtils.getExecutedLineNumers(event);
		currentCodeWindow.highlightGutters(lineNumbers, codeFragments.getLineNumberOffset(currentCodeWindow.getMethodName()) );
		
	}
	//This method is used to increment the slider value by 1
	private void setTick(timeline currentTimeline2){
		double tick=currentTimeline2.getValue();
		try{
			tick++;
			if(tick<=currentTimeline2.getMax()){
				currentTimeline2.setValue(tick);
			}
		}
		catch (Exception ex){
			int m=0;
		}

//		Tooltip tip=new Tooltip(String.valueOf(t.getValue()));
//		t.setTooltip(tip);
	}
	
	//This method is used to decrement the slider value by 1
	private void decrementTick(timeline currentTimeline2){
		double tick=currentTimeline2.getValue();
		try{
			tick--;
			if(tick>=currentTimeline2.getMin()){
				currentTimeline2.setValue(tick);
//				p.setProgress(t.getValue()/t.getMax());
			}
		}
		catch (Exception ex){
			int m=0;
		}

//		Tooltip tip=new Tooltip(String.valueOf(t.getValue()));
//		t.setTooltip(tip);
	}
	
	//This method is used to step backwards
	private void processPrevious(int clineNum, int prevlineNum, ILogEvent prevEvent,ILogEvent curEvent) {
		//step backwards towards previous line 
		// TODO Auto-generated method stub
		if (!eventUtils.isMethodCall(curEvent)) {
			currentCodeWindow.setLineColorToCompleted(clineNum - lineNumberOffset
				- 1);
		}
		
		if (eventUtils.isParentOf(prevEvent, curEvent)) {
			// previous event is parent, we stepped backwards out of
			// a method

			// do something
			if(prevCodeWindow!=mainCWH){
			ColorAdjust c=new ColorAdjust(0,0,0,0);
			String color="CCCCCC";//grey
			highlighter(prevCodeWindow,prevTimeline,c,color);
			prevCodeWindow.reduceWindowSize();
			}else{
				if(prevCodeWindow!=null)
					prevCodeWindow.reduceWindowSize();

			}

			//setTick(currentTimeline, curProgIndicator);
			CodeWindow oldPreviousWindow=prevCodeWindow;
			prevCodeWindow = currentCodeWindow;
			prevTimeline=currentTimeline;

			if (!codeWindowStack.empty()){
				currentCodeWindow = codeWindowStack.pop();
				currentTimeline=timelineStack.pop();
			}
			// highlight parent method
			if(oldPreviousWindow==currentCodeWindow){
				prevCodeWindow.normalWindowSize();
			}
			lineNumberOffset = codeFragments
					.getLineNumberOffset(currentCodeWindow.getMethodName());
			int line=prevlineNum - lineNumberOffset- 1;
			currentCodeWindow.setLineColorToCurrent(line);
			currentCodeWindow.setSelectedLineNumber(line);
			currentCodeWindow.getEditor().setSelectedLineNumber(line);
			
			decrementTick(currentTimeline);//kai
			reposition();
		} else if (eventUtils.isMethodCall(curEvent)){// (curEvent.getDepth() < prevEvent.getDepth()) {
			// we have stepped backwards into a method
			
			String methodName = prevCodeWindow.getMethodName();
			lineNumberOffset=codeFragments.getLineNumberOffset(methodName);
			
			if(prevCodeWindow!=mainCWH){
				ColorAdjust c=new ColorAdjust(0,0,0,0);
				String color="CCCCCC";//grey
				highlighter(prevCodeWindow,prevTimeline,c,color);
				prevCodeWindow.reduceWindowSize();
				}
			else{
				if(prevCodeWindow!=null)
					prevCodeWindow.reduceWindowSize();
			}
			CodeWindow oldPreviousWindow=prevCodeWindow;
			prevCodeWindow = currentCodeWindow;
			prevTimeline=currentTimeline;
			
			if (!codeWindowStack.empty()){
				currentCodeWindow = codeWindowStack.pop();
				currentTimeline=timelineStack.pop();
			}
			// highlight parent method
			
			if(oldPreviousWindow==currentCodeWindow){
				prevCodeWindow.normalWindowSize();
			}
			
			currentCodeWindow.setLineColorToCompleted(clineNum - lineNumberOffset
					- 1);// should be this but currently i haven't set main
			int linenum=prevlineNum - lineNumberOffset
					- 1;
			currentCodeWindow.setLineColorToCurrent(linenum);// should be this but currently i haven't set main
			currentCodeWindow.setSelectedLineNumber(linenum);
			currentCodeWindow.getEditor().setSelectedLineNumber(linenum);
				
			decrementTick(currentTimeline);//kai
			reposition();
		} 
		
		else {
			// did not step out of a method
				
			// do something
			int linenum=prevlineNum - lineNumberOffset - 1;
			currentCodeWindow.setLineColorToCurrent(linenum);
			currentCodeWindow.setSelectedLineNumber(linenum);
			currentCodeWindow.getEditor().setSelectedLineNumber(linenum);
			decrementTick(currentTimeline);//kai
		}
	}
	
	//calculate the position of arrow at x axis
	private int calculateArrowX(DraggableNode cd){
		CodeEditor ed=((CodeEditor)(cd.getChildren().get(2)));
		if(!ed.isReduced()){
			return (int)(cd.getLayoutX()+612);
		}else
			return (int)(cd.getLayoutX()+ cd.getDWidth()+22);//((Rectangle)(cd.getChildren().get(0))).getWidth()
	}
	
	//calculate the position of arrow at y axis
	private int calculateArrowY(DraggableNode cd, int lineNum){
		//return (int)(prevCodeWindow.y+lineNum*9+20);
		CodeEditor ed=((CodeEditor)(cd.getChildren().get(2)));
		
		if(!ed.isReduced()){
		return (int)cd.getLayoutY()+(lineNum+1)*9+10;
		}
		else
			return (int)(cd.getLayoutY()+ cd.getDHeigth());//(int)(((int)ed.getLayoutY()+ed.getHeight()));//(lineNum+1)*9+10)*0.3);//(int)(((int)ed.getLayoutY()+(lineNum+1)*9+10)*0.3);
	}
	
	//calculate the position of code fragment window at x axis
	private int calculateWindowX(DraggableNode cd) {
		CodeEditor ed=((CodeEditor)(cd.getChildren().get(2)));
		if(!ed.isReduced()){
			return	(int)cd.getLayoutX()+630;
		}else
			return (int)(cd.getLayoutX()+ cd.getDWidth()+40);
	}
	
	//calculate the position of code fragment window at y axis
	private int calculateWindowY(DraggableNode cd,int linenum) {
		// TODO Auto-generated method stub
		CodeEditor ed=((CodeEditor)(cd.getChildren().get(2)));
		if(!ed.isReduced()){
		return (int)cd.getLayoutY()+(linenum+1)*9;
		}
		else
			return (int)(cd.getLayoutY()+cd.getDHeigth());//(int)(((int)ed.getLayoutY()+ed.getHeight()));//(linenum+1)*9)*0.3);

	}
	//elocate all the existing added arrows and code fragment windows over the screen
	public void reposition(){
		
		DraggableNode cd=(DraggableNode)codeWindowArea.getChildren().get(0);
		codeWindowArea.getChildren().set(0,cd);
		cd.relocate(0, 0);
		
		for(int j=1;j<codeWindowArea.getChildren().size();j=j+2){
			Arrow cArrow=(Arrow)(codeWindowArea.getChildren().get(j+1));
			int ax=calculateArrowX(cd);
			int ay=calculateArrowY(cd,((CodeEditor)(cd.getChildren().get(2))).getSelectedLineNumber());
//			cArrow.relocate(ax, ay);	
//			cArrow.updatePosition(ax, ay);
//			codeWindowArea.getChildren().set(j+1,cArrow);
			
			DraggableNode newCd=(DraggableNode) codeWindowArea.getChildren().get(j);
			int x=calculateWindowX(cd);
			int y=calculateWindowY(cd,((CodeEditor)(cd.getChildren().get(2))).getSelectedLineNumber());
			
			codeWindowArea.getChildren().set(j,newCd);			
			newCd.relocate(x, y);
			newCd.x.set(x);
			newCd.y.set(y);

			cd=newCd;
		}

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
	
	//This method is used to initialize grid on moving towards another program fragment window, because we are showing grid pane of local variables for every program fragment window. 
	private void initializeGrid(){
		 gridPane=currentCodeWindow.getGridPane();
		 variablePane.getChildren().set(0,gridPane);
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
	//This method is used to proceed towards a next line
	private void processNextLine(int clineNum, ILogEvent curEvent,
			ILogEvent nextEvent) {
		//handle special case - check if this is still valid
		if (clineNum - lineNumberOffset < 0) {

			clineNum = lineNumberOffset + 1;
		}
		currentCodeWindow.setLineColorToCompleted(clineNum - lineNumberOffset - 1);
		
		//Case: Method return - 
		//if depth of next event > current, we have returned to parent method
		if (curEvent.getDepth() > nextEvent.getDepth()) {
			
			// set previous code window color to grey
			if(prevCodeWindow!=mainCWH){
			ColorAdjust c=new ColorAdjust(0,0,0,0);
			String color="CCCCCC";//grey
			highlighter(prevCodeWindow,prevTimeline,c,color);
			//prevCodeWindow.reduceWindowSize();
			}
			
			//since we are stepping out of the method, remove it from the call stack
			//check if the current code window is the last method in the call stack, remove it
			if(CodeWindowCallStack.get(CodeWindowCallStack.size() - 1).getMethodName().equalsIgnoreCase(currentCodeWindow.getMethodName()))
				CodeWindowCallStack.remove(CodeWindowCallStack.size() - 1);
			
			//print out call stack for debugging 
//			printCallStack();
			
			//stash the previous window
			CodeWindow oldPreviousWindow=prevCodeWindow;
			//set current window as previous
			prevCodeWindow = currentCodeWindow;
			
			prevTimeline=currentTimeline;
			
			//current code window is now from the code window stack
			if (!codeWindowStack.empty()){
				currentCodeWindow= codeWindowStack.pop();
				currentTimeline=timelineStack.pop();
				
				currentTimeline.printCallStack(); //for debugging
			}
			if(oldPreviousWindow!=currentCodeWindow){
				oldPreviousWindow.reduceWindowSize();//prevCodeWindow.normalWindowSize();
				oldPreviousWindow.setPinBtn("plus");
				//oldPreviousWindow.getPinBtn().setVisible(true);
				currentCodeWindow.normalWindowSize();
			}
			
			// highlight next line to be executed
			int lineNum = eventUtils.getLineNum(nextEvent);
			
			lineNumberOffset = codeFragments
					.getLineNumberOffset(currentCodeWindow.getMethodName());
			int line=lineNum - lineNumberOffset- 1;
			
			currentCodeWindow.setLineColorToCurrent(line);
			
			currentCodeWindow.setSelectedLineNumber(line);//so we know the start point of the arrow
			currentCodeWindow.getEditor().setSelectedLineNumber(line);
			
			setTick(currentTimeline);
			
			gridPane=currentCodeWindow.getGridPane();
			variablePane.getChildren().set(0,gridPane);
			reposition();
		}
		
		// Case: Method call
		else if (eventUtils.isMethodCall(nextEvent)) {
			String methodName = eventUtils.getMethodName(nextEvent);
			
			setTick(currentTimeline);
			
			if (codeFragments.codeFragmentExist(methodName)) {
				//int size=-1;
				boolean isExist = false;
				int value=0;
				
//keeping track of iteration num for window = value------------------------------------------------------------------------
				if (displayedCodeWindowsList.containsKey(methodName)) {//check if we already have a code window on the display stage with that name
					value = (int) displayedCodeWindowsList.get(methodName);
					value++;
					//codeFragmentWindowsList.remove(methodName);
					displayedCodeWindowsList.put(methodName, value);
					isExist = true;
				}
				else{
					value++;
					displayedCodeWindowsList.put(methodName, value);	
				}
//codeWindow iter number End----------------------------------------------------------------------------------------------
				
				// get the code window 
				CodeWindow codeWin = codeFragments.getCodeFragment(methodName);
				timeline tLine = createTimeLine(nextEvent,methodName);

				//if it is a new code window
				//add it to the display area and set the indexOnScreen
				if(!isExist){
					codeWin.setIndexOnScreen(codeWindowArea.getChildren().size());// IndexOnScreen = index of element (eg. codeWindow) as a child of the code area pane
					codeFragments.setCodeFragment(methodName,codeWindowArea.getChildren().size());
					
					codeWindowArea.getChildren().add(codeWin.getRootNode());
				}
				else{//if it's not a new code window, update iteration combobox value and replace the old codeWindow
					int length=codeWin.getIterationBox().getItems().size();
					codeWin.getIterationBox().getItems().add(length+1);
					codeWin.getIterationBox().setValue(length+1);
					
					//indexOnScreen is needed to replace the old codewindow
					codeWindowArea.getChildren().set(codeWin.getIndexOnScreen(), codeWin.getRootNode());
				}
				
				//update the selected line of the last method call
				CodeWindowCallStack.get(CodeWindowCallStack.size() - 1).getEditor().setSelectedLineNumber(clineNum);
				//add the new method to the call stack
				CodeWindowCallStack.add(codeWin);
				//print out for debugging
//				printCallStack();
				
				
				//highlight the line of the method call in the current window
				int lineNum=eventUtils.getLineNum(nextEvent) - lineNumberOffset - 1;
				currentCodeWindow.setLineColorToNew(lineNum);
				
				lineNumberOffset = codeFragments.getLineNumberOffset(methodName);
				
				//Push the current codewindow and timeline onto stack 
				codeWindowStack.push(currentCodeWindow);
				timelineStack.push(currentTimeline);
				
				//store the current grid pane in the current code window
				currentCodeWindow.setGridPane(gridPane);
								
				//if we are not returning to the previous code window, set it to grey and minimize it
				if(prevCodeWindow!=codeWin){
					if(prevCodeWindow!=mainCWH && prevCodeWindow!=null){	
						ColorAdjust c=new ColorAdjust(0,0,0,0);
						String color="CCCCCC";//grey
						highlighter(prevCodeWindow,prevTimeline,c,color);
					}
					
					if(prevCodeWindow!=null){
						prevCodeWindow.reduceWindowSize();
						prevCodeWindow.setPinBtn("plus");
						//prevCodeWindow.getPinBtn().setVisible(true);
					}					
				}
				
				if(codeWin.getEditor().isReduced()){
					codeWin.normalWindowSize();
				}
				
				//set the new window as current window
				prevCodeWindow = currentCodeWindow;
				currentCodeWindow = codeWin;

				prevTimeline=currentTimeline;
				currentTimeline= tLine;
				
				currentTimeline.printCallStack(); //for debugging
				
				//set the line number which initiated the method call so we know where to palce the arrow
				prevCodeWindow.getEditor().setSelectedLineNumber(lineNum);

				//if we added a new code window, add an arrow
				if(!isExist){
					Polygon arrow = createUMLArrow();
					
					Arrow arrowNew = new Arrow(prevCodeWindow, codeWin);
					
//					codeWindowArea.getChildren().add(arrow);
					codeWindowArea.getChildren().add(arrowNew);
				}
				
				
				highlightGutters(nextEvent);
				initializeGrid();
				reposition();
				
			} else{// code window for this method is not in code fragments, may be a system method call, we just highlight the next line
				int line=eventUtils.getLineNum(nextEvent) - lineNumberOffset - 1;
				currentCodeWindow.setLineColorToCurrent(line);
				
				currentCodeWindow.setSelectedLineNumber(line);
				currentCodeWindow.getEditor().setSelectedLineNumber(line);
			}
		}
		
		// Case: Next Line
		else {
			int line=eventUtils.getLineNum(nextEvent) - lineNumberOffset - 1;
			
			currentCodeWindow.setLineColorToCurrent(line);
			currentCodeWindow.setSelectedLineNumber(line);
			currentCodeWindow.getEditor().setSelectedLineNumber(line);
			
			setTick(currentTimeline);
			
			currentTimeline.printCallStack(); //for debugging
			
			//event writes to a variable, we handle inputing/updates values on the variable pane (grid pane)
			if(eventUtils.isWriteEvent(nextEvent)){
				//List<LocalVariableInfo> locVariablesList=eventUtils.getLocalVariables(nextEvent);
				String varName= eventUtils.getWriteEventVarName(nextEvent);			
				String varValue=eventUtils.getWriteEventValue(nextEvent);
				
				LinkedHashMap<String,ArrayList> localVariableInfo=currentCodeWindow.getLocalVariables();
				
				//if the variable to be written to exist in our local var hashmap, append the value to it
				if(!localVariableInfo.isEmpty()&&localVariableInfo.containsKey(varName)){
					ArrayList values=(ArrayList)localVariableInfo.get(varName);
					if(varValue!=null){
						values.add(varValue);
					}
					else{
						values.add("null");
					}
					//update the entry in local variable hashmap
					localVariableInfo.put(varName, values);
					
					//variable may not exist in our list in which index will be -1
					int index=returnIndexofValue(varName,localVariableInfo);
					if(index!=-1){
				        currentCodeWindow.setLocalVariables(localVariableInfo);//set the updated variable hashmap
				        index=2+2*index+2;
				        
				        removeGridStyles(gridPane);
				        
				        //highlight the variable name that is being written to
				        Label nameLabel = (Label)gridPane.getChildren().get(index-1);	
						nameLabel.setTextFill(Color.web("#ff9999"));
						
						//highlight and set the new value in the combo box
				        ComboBox valueBox= (ComboBox)gridPane.getChildren().get(index);				        
				        valueBox.setScaleX(1);
				        valueBox.setScaleY(1);
				        valueBox.setStyle("-fx-font-size: 10px; -fx-background-color: #ff9999");    
				        valueBox.getItems().add(varValue);
				        valueBox.setValue(varValue);
				        
				        //update the elements being displayed
				        gridPane.getChildren().set(index-1, nameLabel);
				        gridPane.getChildren().set(index, valueBox);
			        }
				}
				else{ //if it does not exist in our hashmap, create the elements and add it to the variable pane
					int rows=((gridPane.getChildren().size())/2);
					removeGridStyles(gridPane);
					Label nameLabel = new Label(varName);
					
					nameLabel.setTextFill(Color.web("#ff9999"));
					gridPane.setMargin(nameLabel, new Insets(10, 10, 10, 10));
			        
					gridPane.setConstraints(nameLabel, 0,rows);
			        
			        ComboBox valueBox= new ComboBox();
			        valueBox.setScaleX(1);
			        valueBox.setScaleY(1);
			        valueBox.setStyle("-fx-font-size: 10px; -fx-background-color: #ff9999");
			        valueBox.getItems().add(varValue);
			        valueBox.setValue(varValue);
			        
			        
			        gridPane.setMargin(valueBox, new Insets(10, 10, 10, 10));
			        
			        gridPane.setConstraints(valueBox, 1,rows);
			       gridPane.getChildren().addAll(nameLabel,valueBox);
			       
			       
			       //add the new var and value in local var hashmap and update the hashmap in the current code window
			       ArrayList values=new ArrayList();
			       values.add(varValue);
			       localVariableInfo.put(varName, values);
			       currentCodeWindow.setLocalVariables(localVariableInfo);
				}

			}
		};
	}
	
	//This method is used to create arrow head
	//additional note, arrows need indexOnScreen as well to get them from the child array and reposition them
	public static Polygon createUMLArrow() {
		Polygon polygon = new Polygon(new double[]{
				7.5, 0,
				15, 15,
				7.51, 15,
				7.51, 40,
				7.49, 40,
				7.49, 15,
				0, 15
		});
		polygon.setFill(Color.WHITE);
		polygon.setStroke(Color.BLACK);
		polygon.setRotate(90);
		return polygon;
	}
	
	private void initializeElementControl() {
		Button nextBtn = (Button) getRootAnchorPane().lookup("#NextBtn");
		Button previousBtn = (Button) getRootAnchorPane().lookup("#PrevBtn");
		Button tagBtn = (Button) getRootAnchorPane().lookup("#varTagSwitch");

		nextBtn.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent e) {
				ILogEvent curEvent = eventUtils.getCurrentEvent();
				stepForward(curEvent);
			}

			
		});

		previousBtn.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent e) {
				ILogEvent curEvent = eventUtils.getCurrentEvent();
				stepBackward(curEvent);
				
				
			}

			

		});

	}
	
	void stepForward(ILogEvent curEvent) {
		// step forward and get next event
		ILogEvent nextEvent = eventUtils.forwardStepInto();
		
		if (nextEvent != null) {

				int clineNum = eventUtils.getLineNum(curEvent);
				int nextlineNum = eventUtils.getLineNum(nextEvent);
				
					processNextLine(clineNum, curEvent, nextEvent);
			
			//handling the window minimizing image (pin) here
//			if(prevCodeWindow!=null){
//			prevCodeWindow.getPinBtn().setVisible(false);
//			}
			
			//set current code window to green
			if(mainCWH!=currentCodeWindow ){
				ColorAdjust c=new ColorAdjust(0.426,0.63,0.075,0.015);
				String color="A3FF7F";//green

				highlighter(currentCodeWindow,currentTimeline,c,color);
			}
			//set previous code window to yellow
			if(mainCWH!=prevCodeWindow){
				//highlightPrevious();
				ColorAdjust c=new ColorAdjust(0.3,1,0.218,0.38);
				String color="FFFB78";//yellow
				highlighter(prevCodeWindow,prevTimeline,c,color);
			}

		}
	}
	
	void stepBackward(ILogEvent curEvent) {
		// step backward and the the previous event
		ILogEvent prevEvent = eventUtils.backwardStepInto();
		
		if (prevEvent != null) {
		
//			do{
			
			int clineNum = eventUtils.getLineNum(curEvent);
			int prevLineNum = eventUtils.getLineNum(prevEvent);	
//			if(clineNum!=prevLineNum){
			

			// currentCodeWindow.setLineColorToCompleted(clineNum -
			// lineNumberOffset + 1);

			// check if the previous event is the parent of current
			// event
//				if (!eventUtils.isMethodCall(curEvent)) {
					
					processPrevious(clineNum,prevLineNum, prevEvent,curEvent);
//				}
//				else{
//
//					processPrevious(clineNum,prevLineNum, prevEvent,curEvent);
//				}
//			break;
//			}
//			else if (eventUtils.isMethodCall(prevEvent)) {
//				String methodName = eventUtils
//						.getMethodName(prevEvent);
//				//setTick(currentTimeline,curProgIndicator);
//				
//				if (codeFragments.codeFragmentExist(methodName)) {
//					processPrevious(clineNum,prevLineNum, prevEvent,curEvent);
////					break;
//				}
//				else{
//					curEvent = eventUtils.getCurrentEvent();
//					prevEvent = eventUtils.backwardStepInto();
//					decrementTick(currentTimeline);
//				}
//			}
//			else{
//
//				curEvent = eventUtils.getCurrentEvent();
//				prevEvent = eventUtils.backwardStepInto();
//				decrementTick(currentTimeline);
//				
//			}	
			
//		}while(true);
	
			if(mainCWH!=currentCodeWindow ){
				ColorAdjust c=new ColorAdjust(0.426,0.63,0.075,0.015);
				String color="A3FF7F";//green

				highlighter(currentCodeWindow,currentTimeline,c,color);
			}
			if(mainCWH!=prevCodeWindow){
				ColorAdjust c=new ColorAdjust(0.3,1,0.218,0.38);
				String color="FFFB78";//yellow
				highlighter(prevCodeWindow,prevTimeline,c,color);
			}
			
		}
	}
	
	//This method relocates the timeline position along x-axis 
	private int timelineLocationX(){
		int x=0;
		if(!timelineStack.empty()){
			for(int i=0;i<timelineStack.size();i++){

				timeline s=timelineStack.get(i);
				x=x+(int)(s.getLayoutX())+(int)(s.getValue()*1.6);

			}
		}
		else{
			x=5;
		}
		return x;
	}
	
	//This method relocates the timeline position along y-axis 
	private int timelineLocationY(){
		int y=0;
		if(timelineStack.empty()){
			y=5;
		}
		else{

			y=timelineStack.size()*40;
		}
		return y;
	}
	
	// just a hack to get the main method for now
	private ILogEvent getMainMethodEvent()
	{
		// find main method call event
		ILogEvent event = null;
		IEventBrowser eventBrowser = eventUtils.getUnfilteredEventBrowser();
		String methodName = "main";

		while (eventBrowser.hasNext()) {
			event = eventBrowser.next();

			String mName = eventUtils.getMethodName(event);

			
			if (mName != null)
				if (mName.equalsIgnoreCase(methodName))
					break;
		}			
		return event;
	}

	// get a list of child events timestamps
	//creates a tick for each event and returns a timeline object
	private timeline createTimeLine(ILogEvent event,String methodName) {

		ArrayList<Long> childEventsTimestamps = eventUtils.getChildEventTimestamps(event);
		
		timeline timeline = new timeline(childEventsTimestamps, eventUtils.getMethodName(event), CodeWindowCallStack);

		posX=timelineLocationX();
		posY=timelineLocationY();

		timelineSection.getChildren().add(timeline);
		timeline.relocate(posX,posY);

		return timeline;
	}
	
	private void printCallStack()
	{
		for(CodeWindow codeWin : CodeWindowCallStack)
			System.out.println(codeWin.getMethodName() + "\n");
	}

}
