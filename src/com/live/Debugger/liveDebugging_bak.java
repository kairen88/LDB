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

import javax.swing.JComponent;
import javax.swing.JFrame;

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
import tod.core.database.structure.IStructureDatabase.LineNumberInfo;
import tod.core.database.structure.SourceRange;
import tod.core.database.structure.IStructureDatabase.ProbeInfo;
import tod.core.session.ISession;
import tod.core.session.SessionTypeManager;
import tod.gui.IExtensionPoints;
import tod.gui.MinerUI;
import tod.gui.StandaloneUI;
import tod.gui.SwingDialogUtils;
import tod.gui.IGUIManager.DialogType;
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
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Slider;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Line;
import javafx.scene.shape.Rectangle;
import javafx.stage.Stage;

public class liveDebugging_bak extends Application {
	
	private Parent root = null;
	private static String[] _args;

	int currentWindowIdx = 0;
	
	ArrayList<CodeWindow> codeWindowAry ;
	CodeWindow currentCodeWindow;
	
	NavigationBar currentNaviBar;
	ArrayList<NavigationBar> navibarAry;
	TickNavigator tickNavigator;
	
	ILogBrowser logBrowser;
	Stepper stepper;
	ArrayList<ICallerSideEvent> eventList;
	
	EventUtils eventUtils;
	
	 static final private String editingCode =
			    "import javafx.application.Application;\n" +
			    "import javafx.scene.Scene;\n" +
			    "import javafx.scene.web.WebView;\n" +
			    "import javafx.stage.Stage;\n" +
			    "\n" +
			    "/** Sample code editing application wrapping an editor in a WebView. */\n" +
			    "public class CodeEditorExample extends Application {\n" +
			    "  public static void main(String[] args) { launch(args); }\n" +
			    "  @Override public void start(Stage stage) throws Exception {\n" +
			    "    WebView webView = new WebView();\n" +
			    "    webView.getEngine().load(\"http://codemirror.net/mode/groovy/index.html\");\n" +
			    "    final Scene scene = new Scene(webView);\n" +
			    "    webView.prefWidthProperty().bind(scene.widthProperty());\n" +
			    "    webView.prefHeightProperty().bind(scene.heightProperty());\n" +
			    "    stage.setScene(scene);\n" +
			    "    stage.show();\n" +
			    "  }\n" +
			    "}";

	public static void main(String[] args) {
			launch(args);
			_args = args;
		}
	
	@Override
	public void start(Stage primaryStage) throws IOException {
//		FXMLLoader fxmlLoader = new FXMLLoader();
		root = FXMLLoader.load(getClass().getClassLoader().getResource("LDB_fxml.fxml"));
		
		eventUtils = new EventUtils();

////---------------------------------
////        //testing navibation bar
//		
//		//adding loops to navi bar
//		CodeLoop lop1 = new CodeLoop (2, 25);
//    	CodeLoop  lop2 = new CodeLoop (4, 20);
//    	CodeLoop  lop3 = new CodeLoop (7, 14);
//    	CodeLoop  lop4 = new CodeLoop (16, 19);
//    	CodeLoop  lop5 = new CodeLoop (13, 14);
//    	CodeLoop  lop6 = new CodeLoop (28, 30);
//    	
//    	CodeLoop [] loopAry = {lop3, lop2, lop4, lop1, lop5, lop6};
//		
//		navibarAry = new ArrayList<NavigationBar>();
//
//		NavigationBar naviBarRoot = new NavigationBar(loopAry);
//		
//		navibarAry.add(naviBarRoot);
//		currentNaviBar = naviBarRoot;
//		
//		NavigationBar naviBarRoot2 = new NavigationBar(null);
//		Pane naviBar2 = naviBarRoot2.getNaviBarRoot();
//		
//		navibarAry.add(naviBarRoot2);
//		
//		Pane naviBarSection = (Pane) getRootAnchorPane().lookup("#naviBarSection");
//		navibarAry.get(0).getNaviBarRoot().relocate(10, 20); 
////		addElementToRoot(navibarAry.get(0).getNaviBarRoot());
//		naviBarSection.getChildren().add(navibarAry.get(0).getNaviBarRoot());
//		
//		navibarAry.get(1).getNaviBarRoot().relocate(10, 100); 
////		addElementToRoot(navibarAry.get(1).getNaviBarRoot());
//		naviBarSection.getChildren().add(navibarAry.get(1).getNaviBarRoot());
////-----------------------------------      
		
//		//testing out codemirror code editor
		codeWindowAry = new ArrayList<CodeWindow>();
		
		Path path = FileSystems.getDefault().getPath("resource", "sampleJs.txt");
		
		Pane codeWindowSection = (Pane) getRootAnchorPane().lookup("#codeWindowSection");
		Pane codeWindowArea = new Pane();
		
		//adding 1st code window setting it as current
//		CodeWindow editor = new CodeWindow(path, 600, 600);
//		addDraggableElementToRoot(editor.getRootNode());
//		codeWindowArea.getChildren().add(editor.getRootNode());
//		editor.getRootNode().relocate(20, 20);
//		codeWindowAry.add(editor);
		
//		currentCodeWindow = editor;
		
		//testing code fragments
//		CodeFragments codeFragment = new CodeFragments(path);
//		codeWindowArea.getChildren().add(codeFragment.getCodeFragment("_fact").getRootNode());
//		codeWindowArea.getChildren().add(codeFragment.getCodeFragment("_makeString").getRootNode());

////		adding 2nd code window to ary, setting code window below min width and height
//		editor = new CodeWindow(editingCode, 200, 150);
////		addDraggableElementToRoot(editor.getRootNode());
//		codeWindowArea.getChildren().add(editor.getRootNode());
//		editor.getRootNode().relocate(20, 330);
//		codeWindowAry.add(editor);
		
		Rectangle codeWindowSectionMask = new Rectangle(680, 485);
		codeWindowArea.setClip(codeWindowSectionMask);
		
		codeWindowSection.getChildren().add(codeWindowArea);
		
////--------------------------------------------------------------
//		//tick navigator
//
//		TickNavigator t = new TickNavigator();
//		t.setTickNavigatorToNaviBar(currentNaviBar);
//		
//		tickNavigator = t;
//		
////-------------------------------------------------------------		
		URI theUri = URI.create("tod-dbgrid-remote:localhost");
		
//		final StandaloneUI theUI = new StandaloneUI(theUri);
		
		ISession itsSession;
		MyTraceView itsTraceView;
		itsTraceView = new MyTraceView();
		
			TODConfig theConfig = new TODConfig();
			itsSession = SessionTypeManager.getInstance().createSession(itsTraceView, theUri, theConfig);
			
		//get log browser
		logBrowser = itsSession.getLogBrowser();
	
		//create event browser
		IEventBrowser eventBrwoser = logBrowser.createBrowser();
		
		//create stepper
		stepper = new Stepper(logBrowser);
		
		//set the stepper
		if(eventBrwoser.hasNext())
			stepper.setCurrentEvent(eventBrwoser.next());
		
		
//-------------------------------------------------------------			
		Pane naviBarSection = (Pane) getRootAnchorPane().lookup("#naviBarSection");
		
//		Slider timeline = createTimeLine();		
		
//		naviBarSection.getChildren().add(timeline);
//--------------------------------------------------------
        initializeElementControl();  	
        
		Scene s = new Scene(root);
		primaryStage.setScene(s);
		primaryStage.setWidth(920);
		primaryStage.setHeight(740);
		primaryStage.show();
	}
	
	private AnchorPane getRootAnchorPane()
    {
		AnchorPane pane = (AnchorPane) root.lookup("#AnchorPane");        
        return pane;
    }
	
	private void addDraggableElementToRoot(DraggableNode node)
    {
		AnchorPane root = getRootAnchorPane();
		root.getChildren().add(node);
    }
	
	private void addElementToRoot(Node node)
    {
		AnchorPane root = getRootAnchorPane();
		root.getChildren().add(node);
    }
	
	//need to find a way to get genric type for parent, for now use pane
    public void addElement(ScrollPane Parent, Node node)
    {
        if (Parent!=null) 
        {
            Parent.setContent(node); 
        }
    }
    
    private ObservableList<HBox> getArray()
    {
    	ObservableList<HBox> vertArray  = FXCollections.observableArrayList();
    	ArrayList<String[]> list = getInputFromFile(); 
    	
    	for(String[] strAry : list)
    	{
	    	HBox hBox = new HBox();
	    	hBox.setStyle("-fx-background-color: #ECC3BF");
	    	for(String string : strAry)
	    	{
	    		Label label = new Label(string);    		
	    		hBox.getChildren().add(label);    		
	    	}
	    	vertArray.add(hBox);
    	}

//        hBox2.setStyle("-fx-background-color: #ECC3BF");
    	return vertArray;
    }
    
    private ArrayList<String[]> getInputFromFile()
    {
    	ArrayList<String[]> list = new ArrayList<String[]>();
    	try{
    		  // Open the file that is the first 
    		  // command line parameter
    		  FileInputStream fstream = new FileInputStream("textfile.txt");
    		  // Get the object of DataInputStream
    		  DataInputStream in = new DataInputStream(fstream);
    		  BufferedReader br = new BufferedReader(new InputStreamReader(in));
    		  String strLine;
    		  //Read File Line By Line
    		  while ((strLine = br.readLine()) != null)   
    		  {
	    		  // Print the content on the console
	    		  //System.out.println (strLine);
	    		  String[] strAry = strLine.split(" ");
	    		  
	    		  list.add(strAry); 
    		  }
    		  //Close the input stream
    		  in.close();

		    }catch (Exception e)
		    {
		    	//Catch exception if any
		    	System.err.println("Error: " + e.getMessage());
		    }
		return list;
    }
    

    
    private void initializeElementControl()
    {
    	Button nextBtn = (Button) getRootAnchorPane().lookup("#NextBtn");
        Button previousBtn = (Button) getRootAnchorPane().lookup("#PrevBtn");
        Button tagBtn = (Button) getRootAnchorPane().lookup("#varTagSwitch");
        
    	nextBtn.setOnAction(new EventHandler<ActionEvent>() {
            @Override public void handle(ActionEvent e) {
//        		if(currentCodeWindow.getCurrentExecutionLine() < currentCodeWindow.getLineCount())
//        		{            	
//        			//increment current exe line
//        			currentCodeWindow.incrementCurrentExecutionLine();
//	            	//set current line to yellow
//	            	currentCodeWindow.setLineColorToCurrent(currentCodeWindow.getCurrentExecutionLine() - 1);
//	            	
//	            	if(currentCodeWindow.getCurrentExecutionLine() > 1)
//	                	//set previous line to green
//	            		currentCodeWindow.setLineColorToCompleted(currentCodeWindow.getCurrentExecutionLine() - 2);
//	            	
//	            	//set tick navigator position
//	            	tickNavigator.moveTickNavigatorToCurrTick(currentNaviBar, currentCodeWindow);
//	            	
//	                System.out.println(currentCodeWindow.getCurrentExecutionLine());	                
//        		}
            	
//            	TODTest();
            	
            	//step forward and get next event
            	ILogEvent event = eventUtils.forwardStepInto();
            	if(event != null)
            	{
            		//is it a method call
            		eventUtils.isMethodCall(event);
            		//is it an exit event (return to parent method)
            		
            		//else highlight current line
            	}
            }
        });
        
        previousBtn.setOnAction(new EventHandler<ActionEvent>() {
            @Override public void handle(ActionEvent e) {
//            	if(currentCodeWindow.getCurrentExecutionLine() > 1)
//            	{
//            		//decrement current exe line
//            		currentCodeWindow.decrementCurrentExecutionLine();
//	            	
//            		//set previous line to yellow
//            		currentCodeWindow.setLineColorToCurrent(currentCodeWindow.getCurrentExecutionLine() - 1);
//                
//            		//set current line to red
//            		currentCodeWindow.setLineColorToNew(currentCodeWindow.getCurrentExecutionLine());
//            		
//            		//set tick navigator position
//            		tickNavigator.moveTickNavigatorToCurrTick(currentNaviBar, currentCodeWindow);
//            		
//                	System.out.println(currentCodeWindow.getCurrentExecutionLine() );
//                	
//            	}
            	
            	IEventBrowser eventBrwoser = logBrowser.createBrowser(logBrowser.createDepthFilter(3));
            	while(eventBrwoser.hasNext())
            	{
            		ILogEvent event = eventBrwoser.next();
            		
            		if( event instanceof MethodCallEvent)
            		{
            			ProbeInfo theProbeInfo = LocationUtils.getProbeInfo(event);
            			if(theProbeInfo != null)
            			{
	            			SourceRange srcRange = LocationUtils.getSourceRange(logBrowser.getStructureDatabase(),theProbeInfo);
	            			int lineNum = srcRange.startLine;
	            			
	            			IBehaviorInfo theBehavior = logBrowser.getStructureDatabase().getBehavior(theProbeInfo.behaviorId, true);
	            		    LineNumberInfo[] theLineNumber = theBehavior.getLineNumbers();
	            		    LineNumberInfo lineInfo = theLineNumber[0];
	            		    int a=1;
            			}
            		}
            	}
            }
        });
        
      //setting button to toggle current code window
        Button toggleBtn = (Button)getRootAnchorPane().lookup("#button");
        toggleBtn.setOnAction(new EventHandler<ActionEvent>() {
        	@Override public void handle(ActionEvent e) 
        		{
        			//cycle through code windows in ary
        			//if index is pointing to last element in ary, reset to 1st code window else set code window as current
        			if(codeWindowAry.lastIndexOf(currentCodeWindow) == codeWindowAry.size() - 1)
        				currentCodeWindow = codeWindowAry.get(0);
        			else
        				currentCodeWindow = codeWindowAry.get( codeWindowAry.lastIndexOf(currentCodeWindow) + 1 );
        			
        			if(navibarAry.lastIndexOf(currentNaviBar) == navibarAry.size() - 1)
        				currentNaviBar = navibarAry.get(0);
        			else
        				currentNaviBar = navibarAry.get( navibarAry.lastIndexOf(currentNaviBar) + 1 );
        			
        			//set current line to yellow
	            	//set line in previous code window to green
        			
        			tickNavigator.setTickNavigatorToNaviBar(currentNaviBar);
        			tickNavigator.moveTickNavigatorToCurrTick(currentNaviBar, currentCodeWindow);
     			
        		}
        	});
        
        tagBtn.setOnAction(new EventHandler<ActionEvent>() {
        	@Override public void handle(ActionEvent e) 
        		{
//                	String scriptStr = readFileReturnString("resource\\js\\create&InsertDivElement.txt");
//                	String scriptStr = readFileReturnString("resource\\js\\testingHoverFunction.txt");
//					String scriptStr = readFileReturnString("resource\\js\\attachTagToVariable.txt");
        			String scriptStr = readFileReturnString("resource\\js\\testScript.js");
//					currentCodeWindow.runScriptOnWebForm(scriptStr);
        		}
        	});
        
    }
    
    private String readFileReturnString(String _path)
    {
    	String fileStr = "";
    	
    	try{
  		  // Open the file that is the first command line parameter
  		  FileInputStream fstream = new FileInputStream(_path);
  		  // Get the object of DataInputStream
  		  DataInputStream in = new DataInputStream(fstream);
  		  BufferedReader br = new BufferedReader(new InputStreamReader(in));
  		  String strLine;
  		  //Read File Line By Line
  		  while ((strLine = br.readLine()) != null)   
  		  {
  			  //check to ignore comments
  			  if(!strLine.contains("//"))
      		  //append to string obj template
  				  fileStr += strLine;
  		  }
  		  //Close the input stream
  		  in.close();

  	    }catch (Exception e)
  	    {
  	    	//Catch exception if any
  	    	System.err.println("Error: " + e.getMessage());
  	    }
    	return fileStr;
    }
    
    private void TODTest()
    {
		//just going through the browser and looking at events with executed behavior
		while (stepper.getCurrentEvent() != null)
		{
			ILogEvent event = stepper.getCurrentEvent();
						
			if(event instanceof ICallerSideEvent)
			{
				//if we find a valid line num, stop. else continue until we find one
				if(highlightCurrentLine((ICallerSideEvent) event))
				{
					stepper.forwardStepInto();
					break;
				}
			}
			stepper.forwardStepInto();
		}
//    			ProbeInfo probInfo = LocationUtils.getProbeInfo((ICallerSideEvent)event);
//    			int index = probInfo.bytecodeIndex;
    }
    
    private boolean highlightCurrentLine(ICallerSideEvent event)
    {
    	ProbeInfo probInfo = event.getProbeInfo();
		//get line number from probe info
		if(probInfo != null){
			SourceRange srcRange = LocationUtils.getSourceRange(logBrowser.getStructureDatabase(),probInfo);
			int lineNum = srcRange.startLine;
			
			if (lineNum != -1)
			{
				//-1 just to get it to show the correct line, NEED to find out why 
				//currently highlighting one line below
//				currentCodeWindow.setLineColorToCurrent(lineNum - 1);
				return true;
			}
			return false;
		}
		return false;
    }
    
    private boolean highlightCurrentLine_timeline(ICallerSideEvent event)
    {
    	ProbeInfo probInfo = event.getProbeInfo();
		//get line number from probe info
		if(probInfo != null){
			SourceRange srcRange = LocationUtils.getSourceRange(logBrowser.getStructureDatabase(),probInfo);
			int lineNum = srcRange.startLine;
			
			if (lineNum != -1)
			{
				//-1 just to get it to show the correct line, NEED to find out why 
				//currently highlighting one line below
//				currentCodeWindow.setLineColorToNew(lineNum - 1);
				return true;
			}
			return false;
		}
		return false;
    }
    
    private Slider createTimeLine()
    {
    	IEventBrowser mainEventsBrowser = logBrowser.createBrowser(logBrowser.createDepthFilter(2));
    	
    	eventList = new ArrayList<ICallerSideEvent>();
    	
    	while(mainEventsBrowser.hasNext())
    	{
    		ILogEvent event = mainEventsBrowser.next();
    		
    		if(event instanceof ICallerSideEvent)
			{
    			ICallerSideEvent callerEvent = (ICallerSideEvent)event;
    			ProbeInfo probeInfo = callerEvent.getProbeInfo();
    			if(probeInfo != null)
    			{
    				SourceRange srcRange = LocationUtils.getSourceRange(logBrowser.getStructureDatabase(),probeInfo);
    				if(srcRange.startLine != -1)
    					eventList.add(callerEvent);
    			}
    					
			}
    	}
    	
    	
    	
    	long eventCount = eventList.size();
    	
    	Slider timeline = new Slider(1, eventCount, 0);
    	timeline.setShowTickLabels(true);
		timeline.setShowTickMarks(true);
		timeline.setMajorTickUnit(1.0);
		timeline.setBlockIncrement(200.0);
		timeline.setMinorTickCount(0);
		timeline.prefWidth(300);
		timeline.setMajorTickUnit(10);
		
		timeline.valueProperty().addListener(new ChangeListener<Number>() {
			public void changed(ObservableValue<? extends Number> ov, Number old_val, Number new_val)
			{
				System.out.println(new_val.intValue());
				
				highlightCurrentLine_timeline( eventList.get(new_val.intValue()-1) );
			}
		});
		
		return timeline;
    }
    
    
    private class MyTraceView extends MinerUI
	{
		public void gotoSource(ILocationInfo aLocation)
		{
		}

		public void gotoSource(ProbeInfo aProbe)
		{
		}

		@Override
		public <T> T showDialog(DialogType<T> aDialog)
		{
			return SwingDialogUtils.showDialog(this, aDialog);
		}

		public void showPostIt(JComponent aComponent, Dimension aSize)
		{
			JFrame theFrame = new JFrame("TOD Post-It");
			theFrame.setContentPane(aComponent);
			theFrame.pack();
			theFrame.setVisible(true);
		}

		public IExtensionPoints getExtensionPoints()
		{
			return null;
		}
	}
	
}
