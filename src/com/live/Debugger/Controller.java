package com.live.Debugger;

import java.net.URL;
import java.util.ResourceBundle;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;

import com.live.Debugger.liveDebugging;

public class Controller implements Initializable {

	@Override
	public void initialize(URL location, ResourceBundle resources) {
		System.err.println("The controller is created!!!");
	}
	
	@FXML
	private Label label;

	/**
	 * This the click 1 method
	 * 
	 * @param event
	 *            the event
	 */
	@FXML
	public void clicked(ActionEvent event) {
		System.err.println("Hello World");
		label.setText("Button was clicked");
	}

	/**
	 * This is the click 2 method
	 * 
	 * @param event
	 *            the event
	 */
	@FXML
	public void nextLineBtn(ActionEvent event) 
	{
		
	}

//	@FXML
//	public void clicked(String event) {
//
//	}

//	@FXML
//	public void clicked() {
//
//	}
}