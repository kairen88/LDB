
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
import javafx.scene.control.ProgressIndicator;
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
	Stack<ProgressIndicator> progIndStack;
	CodeWindow currentCodeWindow;

	timeline currentTimeline;
	timeline prevTimeline;
	CodeWindow prevCodeWindow;

	NavigationBar currentNaviBar;
	ArrayList<NavigationBar> navibarAry;
	TickNavigator tickNavigator;

	ILogBrowser logBrowser;
	Stepper stepper;
	ArrayList<ICallerSideEvent> eventList;
	
	Pane codeWindowAreaNew;
	Pane codeWindowArea=new Pane ();

	EventUtils eventUtils;
	CodeFragments codeFragments;

	int lineNumberOffset;
	ArrayList<Long> childEventsTimestamps;
	private HashMap codeFragmentWindowsList; //list of code windows currently displayed on screen
	Pane timelineSectionNew;
	Pane timelineSection=new Pane ();
	ProgressIndicator curProgIndicator;
	ProgressIndicator prevProgIndicator;
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
	private Rectangle createCodeWindowBackground(int windowWidth,int windowHeight,String color)
	{
		Rectangle codeWindowBackground = new Rectangle(windowWidth, windowHeight);
		javafx.scene.paint.Paint codeMirrorBackgroundColor = javafx.scene.paint.Paint.valueOf("CCCCCC");//grey
		codeWindowBackground.setFill(codeMirrorBackgroundColor);
		codeWindowBackground.setArcHeight(15);
		codeWindowBackground.setArcWidth(15);

		return codeWindowBackground;
	}

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
		progIndStack=new Stack<>();
		codeFragmentWindowsList = new HashMap<>();
		//vb.setSpacing(5);

		codeWindowAry = new ArrayList<CodeWindow>();

		Path path = FileSystems.getDefault()
				.getPath("resource", "sampleJs.txt");



		//StackPane codeWindowSection = (StackPane) getRootAnchorPane().lookup(
				//"#codeWindowSection");
		codeWindowAreaNew = (Pane) getRootAnchorPane().lookup(
				"#codeWindowSection");//new StackPane();

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
		Object []obj=createTimeLine(null,"main",-1,0);

		currentTimeline=(timeline)obj[0];
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
		primaryStage.setHeight(1100);
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

	private ObservableList<HBox> getArray() {
		ObservableList<HBox> vertArray = FXCollections.observableArrayList();
		ArrayList<String[]> list = getInputFromFile();

		for (String[] strAry : list) {
			HBox hBox = new HBox();
			hBox.setStyle("-fx-background-color: #ECC3BF");
			for (String string : strAry) {
				Label label = new Label(string);
				hBox.getChildren().add(label);
			}
			vertArray.add(hBox);
		}

		// hBox2.setStyle("-fx-background-color: #ECC3BF");
		return vertArray;
	}

	private ArrayList<String[]> getInputFromFile() {
		ArrayList<String[]> list = new ArrayList<String[]>();
		try {
			// Open the file that is the first
			// command line parameter
			FileInputStream fstream = new FileInputStream("textfile.txt");
			// Get the object of DataInputStream
			DataInputStream in = new DataInputStream(fstream);
			BufferedReader br = new BufferedReader(new InputStreamReader(in));
			String strLine;
			// Read File Line By Line
			while ((strLine = br.readLine()) != null) {
				// Print the content on the console
				// System.out.println (strLine);
				String[] strAry = strLine.split(" ");

				list.add(strAry);
			}
			// Close the input stream
			in.close();

		} catch (Exception e) {
			// Catch exception if any
			System.err.println("Error: " + e.getMessage());
		}
		return list;
	}

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
			codeFragmentWindowsList.put(methodName, 0);
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
//		for(int i=0;i<lineNumbers.size();i++){
//			currentCodeWindow.highlightGutters((int)lineNumbers.get(i).intValue() - 20- 1);
//		}
		currentCodeWindow.highlightGutters(lineNumbers, codeFragments.getLineNumberOffset(currentCodeWindow.getMethodName()) );
		
	}
	//This method is used to increment the slider value
	private void setTick(timeline currentTimeline2,ProgressIndicator p){
		double tick=currentTimeline2.getValue();
		try{
			tick++;
			if(tick<=currentTimeline2.getMax()){
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
	//This method is used to decrement the slider value
	private void decrementTick(timeline currentTimeline2,ProgressIndicator p){
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
			prevProgIndicator=curProgIndicator;

			if (!codeWindowStack.empty()){
				currentCodeWindow = codeWindowStack.pop();
				currentTimeline=timelineStack.pop();
				curProgIndicator= progIndStack.pop();
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
			
			decrementTick(currentTimeline,curProgIndicator);//kai
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
			prevProgIndicator=curProgIndicator;
			
			if (!codeWindowStack.empty()){
				currentCodeWindow = codeWindowStack.pop();
				currentTimeline=timelineStack.pop();
				curProgIndicator= progIndStack.pop();
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
				
			decrementTick(currentTimeline,curProgIndicator);//kai
			reposition();
		} 
		
		else {
			// did not step out of a method
				
			// do something
			int linenum=prevlineNum - lineNumberOffset - 1;
			currentCodeWindow.setLineColorToCurrent(linenum);
			currentCodeWindow.setSelectedLineNumber(linenum);
			currentCodeWindow.getEditor().setSelectedLineNumber(linenum);
			decrementTick(currentTimeline,curProgIndicator);//kai
		}
	}
	//This method is used to calculate the position of arrow at x axis
	private int calculateArrowX(DraggableNode cd){
		CodeEditor ed=((CodeEditor)(cd.getChildren().get(2)));
		if(!ed.isReduced()){
			return (int)(cd.getLayoutX()+612);
		}else
			return (int)(cd.getLayoutX()+ cd.getDWidth()+22);//((Rectangle)(cd.getChildren().get(0))).getWidth()
	}
	//This method is used to calculate the position of arrow at y axis
	private int calculateArrowY(DraggableNode cd, int lineNum){
		//return (int)(prevCodeWindow.y+lineNum*9+20);
		CodeEditor ed=((CodeEditor)(cd.getChildren().get(2)));
		
		if(!ed.isReduced()){
		return (int)cd.getLayoutY()+(lineNum+1)*9+10;
		}
		else
			return (int)(cd.getLayoutY()+ cd.getDHeigth());//(int)(((int)ed.getLayoutY()+ed.getHeight()));//(lineNum+1)*9+10)*0.3);//(int)(((int)ed.getLayoutY()+(lineNum+1)*9+10)*0.3);
	}
	//This method is used to calculate the position of code fragment window at x axis
	private int calculateWindowX(DraggableNode cd) {
		CodeEditor ed=((CodeEditor)(cd.getChildren().get(2)));
		if(!ed.isReduced()){
			return	(int)cd.getLayoutX()+630;
		}else
			return (int)(cd.getLayoutX()+ cd.getDWidth()+40);
		
		


	}
	//This method is used to calculate the position of code fragment window at y axis
	private int calculateWindowY(DraggableNode cd,int linenum) {
		// TODO Auto-generated method stub
		CodeEditor ed=((CodeEditor)(cd.getChildren().get(2)));
		if(!ed.isReduced()){
		return (int)cd.getLayoutY()+(linenum+1)*9;
		}
		else
			return (int)(cd.getLayoutY()+cd.getDHeigth());//(int)(((int)ed.getLayoutY()+ed.getHeight()));//(linenum+1)*9)*0.3);

	}
	//This method is used to relocate all the existing added arrows and code fragment windows over the screen
	public void reposition(){
		
		DraggableNode cd=(DraggableNode)codeWindowArea.getChildren().get(0);
		codeWindowArea.getChildren().set(0,cd);
		cd.relocate(0, 0);
		
		for(int j=1;j<codeWindowArea.getChildren().size();j=j+2){
			Polygon cArrow=(Polygon)(codeWindowArea.getChildren().get(j+1));
			int ax=calculateArrowX(cd);
			int ay=calculateArrowY(cd,((CodeEditor)(cd.getChildren().get(2))).getSelectedLineNumber());
			cArrow.relocate(ax, ay);	
			codeWindowArea.getChildren().set(j+1,cArrow);
			
			DraggableNode newCd=(DraggableNode) codeWindowArea.getChildren().get(j);
			int x=calculateWindowX(cd);
			int y=calculateWindowY(cd,((CodeEditor)(cd.getChildren().get(2))).getSelectedLineNumber());
			
			codeWindowArea.getChildren().set(j,newCd);			
			newCd.relocate(x, y);
			//((Group)(newCd.getChildren().get(1))).relocate(newCd.getLayoutX()+5, newCd.getLayoutY()+5);
			//((Group)(newCd.getChildren().get(1))).toFront();
			
			//((HBox)(((HBox)(newCd.getChildren().get(1))).getChildren().get(1))).relocate(newCd.getDWidth()-25, newCd.getLayoutY()+5);
			//((HBox)(newCd.getChildren().get(1))).toFront();
			cd=newCd;
		}
		/*s1=((ScrollPane)(codeWindowAreaNew.getChildren().get(0)));
		s1.setContent(codeWindowArea);
		codeWindowAreaNew.getChildren().set(0,s1);*/
	}
	//Thi is a helping function which returns index value of some variable name
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
		currentCodeWindow.setLineColorToCompleted(clineNum - lineNumberOffset
				- 1);//is this working???
		//Case: Method return 
		//if depth of next event > current, we have returned to parent method
		if (curEvent.getDepth() > nextEvent.getDepth()) {
			// set parent code fragment to current
			if(prevCodeWindow!=null){
				//prevCodeWindow.getPinBtn().setVisible(false);
			}
			
			if(prevCodeWindow!=mainCWH){
			ColorAdjust c=new ColorAdjust(0,0,0,0);
			String color="CCCCCC";//grey
			highlighter(prevCodeWindow,prevTimeline,c,color);
			//prevCodeWindow.reduceWindowSize();
			}/*
			else{
				if(prevCodeWindow!=null)
					prevCodeWindow.reduceWindowSize();

			}*/
//please explain this logic----------------------------------------------
			CodeWindow oldPreviousWindow=prevCodeWindow;
			prevCodeWindow = currentCodeWindow;
			prevTimeline=currentTimeline;
			prevProgIndicator=curProgIndicator;
			
			if (!codeWindowStack.empty()){
				currentCodeWindow= codeWindowStack.pop();
				currentTimeline=timelineStack.pop();
				curProgIndicator= progIndStack.pop();
			}
			if(oldPreviousWindow!=currentCodeWindow){
				oldPreviousWindow.reduceWindowSize();//prevCodeWindow.normalWindowSize();
				oldPreviousWindow.setPinBtn("plus");
				//oldPreviousWindow.getPinBtn().setVisible(true);
				currentCodeWindow.normalWindowSize();
			}
//----------------------------------------------------------------------------
			// highlight next line to be executed
			int lineNum = eventUtils.getLineNum(nextEvent);
			lineNumberOffset = codeFragments
					.getLineNumberOffset(currentCodeWindow.getMethodName());
			int line=lineNum - lineNumberOffset- 1;
			currentCodeWindow.setLineColorToCurrent(line);// should be this but currently i haven't set main
			currentCodeWindow.setSelectedLineNumber(line);
			currentCodeWindow.getEditor().setSelectedLineNumber(line);
			setTick(currentTimeline, curProgIndicator);//kai
			
			gridPane=currentCodeWindow.getGridPane();
			variablePane.getChildren().set(0,gridPane);
			reposition();
		}
		// Case: Method call
		else if (eventUtils.isMethodCall(nextEvent)) {
			String methodName = eventUtils.getMethodName(nextEvent);
			
			setTick(currentTimeline,curProgIndicator);//kai
			
			if (codeFragments.codeFragmentExist(methodName)) {
				int size=-1;
				boolean isExist = false;
				int value=0;
//explain this logic------------------------------------------------------------------------keeping track of iternation num for window = vlaue
				if (codeFragmentWindowsList.containsKey(methodName)) {
					value = (int) codeFragmentWindowsList.get(methodName);
					value++;
					//codeFragmentWindowsList.remove(methodName);
					codeFragmentWindowsList.put(methodName, value);
					isExist = true;
				}
				else{
					value++;
					codeFragmentWindowsList.put(methodName, value);	
				}
				
				// get the code fragment and add it to the code area
				CodeWindow codeFragment;
				codeFragment = codeFragments.getCodeFragment(methodName);
// explain this logic, is this needed?--------------------------------------------------------------------------------------no longer needed
				int prevTick=0;
				if(prevTimeline!=null){
					prevTick=((int)prevTimeline.getValue());
				}
//---------------------------------------------------------------------------------------------------------------------------
				Object []obj=createTimeLine(nextEvent,methodName,currentTimeline.getValue(),prevTick);

			
				if(!isExist){
					codeFragment.setIndexOnScreen(codeWindowArea.getChildren().size());// explain what setIndexOnScreen is for
					codeFragments.setCodeFragment(methodName,codeWindowArea.getChildren().size());
					codeWindowArea.getChildren().add(codeFragment.getRootNode());
				}
				else{//update and display the code window iteration dropdown box value
					int length=codeFragment.getIterationBox().getItems().size();
					codeFragment.getIterationBox().getItems().add(length+1);
					codeFragment.getIterationBox().setValue(length+1);
					codeWindowArea.getChildren().set(codeFragment.getIndexOnScreen(), codeFragment.getRootNode());//(codeFragment.getRootNode()); we get the code window, set the value of the iteration combo box then set it in the display area again, so indexOnScreen is needed to replace the old codewindow
					//additional note, arrows need index as well to get them from the child array and reposition them
				}
				
				int lineNum=eventUtils.getLineNum(nextEvent) - lineNumberOffset - 1;
				currentCodeWindow.setLineColorToNew(lineNum);
				// set line number offset
				lineNumberOffset = codeFragments
						.getLineNumberOffset(methodName);
				
				//Push the current codewindow and timeline in stack before we create the new window for the method call
				codeWindowStack.push(currentCodeWindow);
				timelineStack.push(currentTimeline);
				progIndStack.push(curProgIndicator);
				currentCodeWindow.setGridPane(gridPane);
				
				CodeWindow oldPrevious=prevCodeWindow;
				
				if(oldPrevious!=codeFragment){
					if(prevCodeWindow!=mainCWH&&prevCodeWindow!=null){	
						ColorAdjust c=new ColorAdjust(0,0,0,0);
						String color="CCCCCC";//grey
						highlighter(prevCodeWindow,prevTimeline,c,color);
						prevCodeWindow.reduceWindowSize();
						prevCodeWindow.setPinBtn("plus");
						//prevCodeWindow.getPinBtn().setVisible(true);
					}
					else{
						if(prevCodeWindow!=null){
							prevCodeWindow.reduceWindowSize();
							prevCodeWindow.setPinBtn("plus");
							//prevCodeWindow.getPinBtn().setVisible(true);
						}
					}
				}
				
				if(codeFragment.getEditor().isReduced()){
					codeFragment.normalWindowSize();
				}
				prevCodeWindow = currentCodeWindow;

				currentCodeWindow = codeFragment;

				prevTimeline=currentTimeline;
				currentTimeline=(timeline)obj[0];
				prevProgIndicator=curProgIndicator;
//				curProgIndicator=(ProgressIndicator)obj[1];
				if(!isExist){
					Polygon arrow = createUMLArrow();
					codeWindowArea.getChildren().add(arrow);
				}
				prevCodeWindow.getEditor().setSelectedLineNumber(lineNum);
				
				// need to highlight line here
				highlightGutters(nextEvent);
				initializeGrid();
				reposition();
				
			} else{
				int line=eventUtils.getLineNum(nextEvent) - lineNumberOffset - 1;
				currentCodeWindow.setLineColorToCurrent(line);
				currentCodeWindow.setSelectedLineNumber(line);
				currentCodeWindow.getEditor().setSelectedLineNumber(line);
			}
			// need to find out why highlighted line is off by 1
		}
		// Case: Next Line
		else {
			int line=eventUtils.getLineNum(nextEvent) - lineNumberOffset - 1;
			currentCodeWindow.setLineColorToCurrent(line);
			currentCodeWindow.setSelectedLineNumber(line);
			currentCodeWindow.getEditor().setSelectedLineNumber(line);
			setTick(currentTimeline,curProgIndicator);//kai
			
			if(eventUtils.isWriteEvent(nextEvent)){
				//List<LocalVariableInfo> locVariablesList=eventUtils.getLocalVariables(nextEvent);
				String name=eventUtils.getWriteEventVarName(nextEvent);
				
				String value=eventUtils.getWriteEventValue(nextEvent);
				LinkedHashMap<String,ArrayList> localVariableInfo=currentCodeWindow.getLocalVariables();
				
				if(!localVariableInfo.isEmpty()&&localVariableInfo.containsKey(name)){
					ArrayList values=(ArrayList)localVariableInfo.get(name);
					if(value!=null){
						values.add(value);
					}
					else{
						values.add("null");
					}
					localVariableInfo.put(name, values);
					int index=returnIndexofValue(name,localVariableInfo);
					if(index!=-1){
				        currentCodeWindow.setLocalVariables(localVariableInfo);
				        index=2+2*index+2;
				        
				        removeGridStyles(gridPane);
				        
				        Label nameLabel = (Label)gridPane.getChildren().get(index-1);	
						nameLabel.setTextFill(Color.web("#ff9999"));
						
				        ComboBox valueBox= (ComboBox)gridPane.getChildren().get(index);
				        
				        valueBox.setScaleX(1);
				        valueBox.setScaleY(1);
				        valueBox.setStyle("-fx-font-size: 10px; -fx-background-color: #ff9999");    
				        valueBox.getItems().add(value);
				        valueBox.setValue(value);
				        
				        
				        gridPane.getChildren().set(index-1, nameLabel);
				        gridPane.getChildren().set(index, valueBox);
			        }
				}
				else{
					int rows=((gridPane.getChildren().size())/2);
					removeGridStyles(gridPane);
					Label nameLabel = new Label(name);
					
					nameLabel.setTextFill(Color.web("#ff9999"));
					GridPane.setMargin(nameLabel, new Insets(10, 10, 10, 10));
			        
					GridPane.setConstraints(nameLabel, 0,rows);
			        
			        ComboBox valueBox= new ComboBox();
			        valueBox.setScaleX(1);
			        valueBox.setScaleY(1);
			        valueBox.setStyle("-fx-font-size: 10px; -fx-background-color: #ff9999");
			        valueBox.getItems().add(value);
			        valueBox.setValue(value);
			        
			        
			        GridPane.setMargin(valueBox, new Insets(10, 10, 10, 10));
			        
			        GridPane.setConstraints(valueBox, 1,rows);
			       gridPane.getChildren().addAll(nameLabel,valueBox);
			       
			       
			       
			       ArrayList values=new ArrayList();
			       values.add(value);
			       localVariableInfo.put(name, values);
			       currentCodeWindow.setLocalVariables(localVariableInfo);
				}

			}
		};
	}
	//This method is used to create arrow head
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

				// step forward and get next event
				ILogEvent nextEvent = eventUtils.forwardStepInto();
				if (nextEvent != null) {
					do {//this do while loop will ignore events on the same line, highlight and ticks will proceed line by line
						//comment out to proceed event by event 
						int clineNum = eventUtils.getLineNum(curEvent);
						int nextlineNum = eventUtils.getLineNum(nextEvent);
						if (clineNum != nextlineNum) {
							processNextLine(clineNum, curEvent, nextEvent);
							break;
						} 
						else if (eventUtils.isMethodCall(nextEvent)) {
							String methodName = eventUtils
									.getMethodName(nextEvent);
							if (codeFragments.codeFragmentExist(methodName)) {
								processNextLine(clineNum, curEvent, nextEvent);
								break;
							}
							else{
								curEvent = eventUtils.getCurrentEvent();
								nextEvent = eventUtils.forwardStepInto();
								setTick(currentTimeline,curProgIndicator);//kai
							}
						}

						else {
							curEvent = eventUtils.getCurrentEvent();
							nextEvent = eventUtils.forwardStepInto();
							setTick(currentTimeline,curProgIndicator);//kai
						}

					} while (true);
					
					//currentCodeWindow.getPinBtn().setVisible(false);
					
					//handling the window minimizing image (pin) here
					if(prevCodeWindow!=null){
					//prevCodeWindow.getPinBtn().setVisible(false);
				}
					if(mainCWH!=currentCodeWindow ){
						ColorAdjust c=new ColorAdjust(0.426,0.63,0.075,0.015);
						String color="A3FF7F";//green

						highlighter(currentCodeWindow,currentTimeline,c,color);
					}
					if(mainCWH!=prevCodeWindow){
						//highlightPrevious();
						ColorAdjust c=new ColorAdjust(0.3,1,0.218,0.38);
						String color="FFFB78";//yellow
						highlighter(prevCodeWindow,prevTimeline,c,color);
					}

				}
			}
		});

		previousBtn.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent e) {

				// get current event
				ILogEvent curEvent = eventUtils.getCurrentEvent();
				// step backward and the the previous event
				ILogEvent prevEvent = eventUtils.backwardStepInto();
				
				if (prevEvent != null) {
				
					do{
					
					int clineNum = eventUtils.getLineNum(curEvent);
					int prevLineNum = eventUtils.getLineNum(prevEvent);	
					if(clineNum!=prevLineNum){
					

					// currentCodeWindow.setLineColorToCompleted(clineNum -
					// lineNumberOffset + 1);

					// check if the previous event is the parent of current
					// event
						if (!eventUtils.isMethodCall(curEvent)) {
							
							processPrevious(clineNum,prevLineNum, prevEvent,curEvent);
						}
						else{

							processPrevious(clineNum,prevLineNum, prevEvent,curEvent);
						}
					break;
					}
					else if (eventUtils.isMethodCall(prevEvent)) {
						String methodName = eventUtils
								.getMethodName(prevEvent);
						//setTick(currentTimeline,curProgIndicator);
						
						if (codeFragments.codeFragmentExist(methodName)) {
							processPrevious(clineNum,prevLineNum, prevEvent,curEvent);
							break;
						}
						else{
							curEvent = eventUtils.getCurrentEvent();
							prevEvent = eventUtils.backwardStepInto();
							decrementTick(currentTimeline,curProgIndicator);//kai
						}
					}
					else{

						curEvent = eventUtils.getCurrentEvent();
						prevEvent = eventUtils.backwardStepInto();
						decrementTick(currentTimeline,curProgIndicator);//kai
						
					}	
					
				}while(true);
			
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

		});

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

	private Object[] createTimeLine(ILogEvent event,String methodName, double tick, double prevTick) {
		IEventBrowser eventBrowser = eventUtils.getUnfilteredEventBrowser();

		// find main method call event
		if (event == null) {
			while (eventBrowser.hasNext()) {
				event = eventBrowser.next();

				String mName = eventUtils.getMethodName(event);

				// just a hack to get the main method for now
				if (mName != null)
					if (mName.equalsIgnoreCase(methodName))
						break;
			}			

		}

		// get a list of child events timestamps
		// for now i just use a global var
		//create the slider (timeline)
		childEventsTimestamps = eventUtils.getChildEventTimestamps(event);
		
		timeline timeline = new timeline(childEventsTimestamps, eventUtils.getMethodName(event));
		HBox hb = new HBox();
		hb.setSpacing(5);
		hb.getChildren().addAll(timeline);

		posX=timelineLocationX();
		posY=timelineLocationY();

		timelineSection.getChildren().add(hb);
		hb.relocate(posX,posY);

		Object[] object=new Object[1];
		object[0]=timeline;
		return object;
	}

}
