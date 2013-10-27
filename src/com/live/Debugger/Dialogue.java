package com.live.Debugger;


import tod.core.database.event.ILogEvent;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.layout.VBoxBuilder;
import javafx.scene.text.Text;
import javafx.stage.Modality;
import javafx.stage.Stage;

public class Dialogue extends Stage{
	
	public Dialogue(String message, String Btn)
	{
		this.initModality(Modality.WINDOW_MODAL);
		
		Text txt = new Text(message);
		Button btn = new Button(Btn);
		
		this.setScene(new Scene(VBoxBuilder.create().children(txt, btn).
		    alignment(Pos.CENTER).padding(new Insets(5)).build()));
		
		this.setWidth(200);
		this.setHeight(150);
		
		final Dialogue dl = this;
		btn.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent e) { dl.close(); }
		});
	}

}
