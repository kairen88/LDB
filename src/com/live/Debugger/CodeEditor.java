 package com.live.Debugger;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.net.URL;

import javafx.scene.layout.StackPane;
import javafx.scene.web.WebView;

/**
 * A syntax highlighting code editor for JavaFX created by wrapping a
 * CodeMirror code editor in a WebView.
 *
 * See http://codemirror.net for more information on using the codemirror editor.
 */
public class CodeEditor extends StackPane {
  /** a webview used to encapsulate the CodeMirror JavaScript. */
  private boolean isReduced=false; 
  final WebView webview = new WebView();
  private String editingTemplate = "";

  /** a snapshot of the code to be edited kept for easy initilization and reversion of editable code. */
  private String editingCode;
  
  private int selectedLineNumber=0;
  
  
  
  /** applies the editing template to the editing code to create the html+javascript source for a code editor. */
  private String applyEditingTemplate() {
    return editingTemplate.replace("${code}", editingCode);
//	  String temp = editingTemplate.replace("${code}", editingCode);
//	  
//	  String jquery = loadExternalResource();
//	  temp = temp.replace("${jquery}", jquery);
//	  System.out.println(temp);
//    return temp;
  }
  public WebView getWebview(){
	  return this.webview;
  }  
  public String getEditingCode(){
	  return this.editingCode;
  }
  /** loads external resources from file and returns a string */
  private String loadExternalResource(String resourcePath) {
	  
	  String extResource = "";
	  try{
		  // Open the file that is the first command line parameter
		  FileInputStream fstream = new FileInputStream(resourcePath);
//		  FileInputStream fstream = new FileInputStream("/home/penguin/Desktop/FYP/LDB/resource/js/jquery.js");
		  // Get the object of DataInputStream
		  DataInputStream in = new DataInputStream(fstream);
		  BufferedReader br = new BufferedReader(new InputStreamReader(in));
		  String strLine;
		  //Read File Line By Line
		  while ((strLine = br.readLine()) != null)   
		  {
			  if(!strLine.contains("////"))
    		  //append to string
			  extResource += strLine + '\n';
		  }
		  //Close the input stream
		  in.close();

	    }catch (Exception e)
	    {
	    	//Catch exception if any
	    	System.err.println("Error: " + e.getMessage());
	    }
	  
	  return extResource;
  }
  
  /** applies external resouces by replacing the string*/
  private void addExternalResource(String resourcePath, String placeHolder) {
	  String resource = loadExternalResource(resourcePath);
	  //replace placeholder with resource
	  editingTemplate = editingTemplate.replace(placeHolder, resource);
//	  System.out.println(res);
//    return editingTemplate;
  }
  
  /** reads in template file and stores it in string var */
  private void loadTemplate() {
    //read in template file line by line
	  try{
		  // Open the file that is the first command line parameter
		  FileInputStream fstream = new FileInputStream("resource\\template.xml");
		  // Get the object of DataInputStream
		  DataInputStream in = new DataInputStream(fstream);
		  BufferedReader br = new BufferedReader(new InputStreamReader(in));
		  String strLine;
		  //Read File Line By Line
		  while ((strLine = br.readLine()) != null)   
		  {
    		  //append to string obj template
			  editingTemplate += strLine;
		  }
		  //Close the input stream
		  in.close();

	    }catch (Exception e)
	    {
	    	//Catch exception if any
	    	System.err.println("Error: " + e.getMessage());
	    }
  }

  /** sets the current code in the editor and creates an editing snapshot of the code which can be reverted to. */
  public void setCode(String newCode) {
    this.editingCode = newCode;
    webview.getEngine().loadContent(applyEditingTemplate());
  }

  /** returns the current code in the editor and updates an editing snapshot of the code which can be reverted to. */
  public String getCodeAndSnapshot() {
    this.editingCode = (String ) webview.getEngine().executeScript("editor.getValue();");
    return editingCode;
  }

  /** revert edits of the code to the last edit snapshot taken. */
  public void revertEdits() {
    setCode(editingCode);
  }

  /**
   * Create a new code editor.
   * @param editingCode the initial code to be edited in the code editor.
   */
  CodeEditor(String editingCode, int width, int height) {
    this.editingCode = editingCode;

    //loads the template from template.xml into string obj
//    loadTemplate();
    editingTemplate = loadExternalResource("resource/template.xml");
    
    webview.setPrefSize(width, height);
    webview.setMinSize(width, height);
    
    //adding external resources
    addExternalResource("resource/js/jquery.js", "${jquery}");
    addExternalResource("resource/js/modeJavaScript.js", "${mode}");
    addExternalResource("resource/js/codemirror.js", "${codemirror}");
    addExternalResource("resource/css/codemirror.css", "${codemirrorCss}");
    
    webview.getEngine().loadContent(applyEditingTemplate());
//    webview.getEngine().executeScript("editor.setLineClass(3, null,\"test\")");

    this.getChildren().add(webview);
    
//    System.out.println(webview.getEngine().);
  }
public int getSelectedLineNumber() {
	return selectedLineNumber;
}
public void setSelectedLineNumber(int selectedLineNumber) {
	this.selectedLineNumber = selectedLineNumber;
}
public boolean isReduced() {
	return isReduced;
}
public void setReduced(boolean isReduced) {
	this.isReduced = isReduced;
}
}