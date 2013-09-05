package com.live.Debugger;

import java.awt.Component;
import java.awt.Dimension;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JComponent;
import javax.swing.JOptionPane;

import tod.core.config.TODConfig;
import tod.core.database.browser.IEventBrowser;
import tod.core.database.browser.ILogBrowser;
import tod.core.database.browser.IObjectInspector;
import tod.core.database.browser.LocationUtils;
import tod.core.database.browser.Stepper;
import tod.core.database.event.IArrayWriteEvent;
import tod.core.database.event.IBehaviorCallEvent;
import tod.core.database.event.IBehaviorExitEvent;
import tod.core.database.event.IExceptionGeneratedEvent;
import tod.core.database.event.IFieldWriteEvent;
import tod.core.database.event.IInstantiationEvent;
import tod.core.database.event.ILocalVariableWriteEvent;
import tod.core.database.event.ILogEvent;
import tod.core.database.event.IOutputEvent;
import tod.core.database.structure.IBehaviorInfo;
import tod.core.database.structure.ILocationInfo;
import tod.core.database.structure.IStructureDatabase.LocalVariableInfo;
import tod.core.database.structure.ObjectId;
import tod.core.database.structure.SourceRange;
import tod.core.database.structure.IStructureDatabase.LineNumberInfo;
import tod.core.database.structure.IStructureDatabase.ProbeInfo;
import tod.core.session.ISession;
import tod.core.session.SessionTypeManager;
import tod.gui.IExtensionPoints;
import tod.gui.MinerUI;
import tod.gui.SwingDialogUtils;
import tod.gui.components.eventlist.ArrayWriteNode;
import tod.gui.components.eventlist.FieldWriteNode;
import tod.impl.common.event.BehaviorExitEvent;
import tod.impl.dbgrid.event.MethodCallEvent;

public class EventUtils {

	private ILogBrowser logBrowser;
	private Stepper stepper;
	
	public EventUtils()
		{
			URI theUri = URI.create("tod-dbgrid-remote:localhost");
						
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
			
			//set the current event in the stepper to point to the first event
			if(eventBrwoser.hasNext())
			stepper.setCurrentEvent(eventBrwoser.next());
		}
	
	public ILogBrowser getLogBrowser()
	{
		return logBrowser;
	}
	
	public IEventBrowser getUnfilteredEventBrowser()
	{
		IEventBrowser eventBrowser = logBrowser.createBrowser();
		
		return eventBrowser;
	}
	
	//Returns if an event is a method call
	public boolean isMethodCall(ILogEvent _event)
	{
		if(_event instanceof MethodCallEvent)
			return true;
		else 
			return false;
	}
	
	public boolean isExitEvent(ILogEvent _event)
	{
		if(_event instanceof BehaviorExitEvent)
			return true;
		else 
			return false;
	}
	
	//returns true if it is a write event
	//write events include
	//IFieldWriteEvent, IArrayWriteEvent, ILocalVariableWriteEvent
	public boolean isWriteEvent(ILogEvent _event)
	{
		if(_event instanceof IFieldWriteEvent ||
			_event instanceof IArrayWriteEvent ||
			_event instanceof ILocalVariableWriteEvent)
			return true;
		else 
			return false;		
	}
	
	//return name of parent event if it is a method call
	//return null otherwise
	public String getParentMethodName(ILogEvent _event)
	{
		ILogEvent parentEvent = _event.getParent();
		if(isMethodCall(parentEvent))
		{
			ProbeInfo theProbeInfo = LocationUtils.getProbeInfo(_event);
			IBehaviorInfo theBehavior = logBrowser.getStructureDatabase().getBehavior(theProbeInfo.behaviorId, true);
			
			return theBehavior.getName();
			
		}else
			return null;
	}
	
	public String getMethodName(ILogEvent _event)
	{
		if(isMethodCall(_event))
		{
			MethodCallEvent methodCallEvent = (MethodCallEvent) _event;
			if(methodCallEvent.getExecutedBehavior()!=null)
				return methodCallEvent.getExecutedBehavior().getName();
			else
				return methodCallEvent.getCalledBehavior().getName();
		}else
			return null;
	}
	
	//given an event, if it is a method call
	//we return an array containing all the line numbers executed in that method
	public ArrayList<Integer> getExecutedLineNumers(ILogEvent _event)
	{
		//check if event is a method call
		if(isMethodCall(_event))
		{
		
			//get child event browser
			MethodCallEvent callEvent = (MethodCallEvent)_event;
			//IEventBrowser browser = callEvent.getChildrenBrowser();
			/*if(browser.hasNext())
			{
				//get child event
				ILogEvent childEvent = browser.next();
				*/
				//get array of line numbers
				LineNumberInfo[] lineNumAry = /*getExecutionPath(_event);*/callEvent.getCalledBehavior().getLineNumbers();
				ArrayList<Integer> lineNumbers = new ArrayList<Integer>();
				
				//convert into an ary of integers
				for(int i = 0; i < lineNumAry.length; i++)
					lineNumbers.add((int) lineNumAry[i].getLineNumber());
				
				return lineNumbers;
			/*}
			else
				//there is not child event for this method call(error?), for now we return null
				return null;
			*/
		}else		
		//else return null
			return null;
	}
	
	//given an event, if it is a method call
	//we return an array containing all the timestamps of the child events
	public ArrayList<Long> getChildEventTimestamps(ILogEvent _event)
	{
		//check if event is a method call
		if(isMethodCall(_event))
		{
			//get child event browser
			MethodCallEvent callEvent = (MethodCallEvent)_event;
			IEventBrowser childBrowser = callEvent.getChildrenBrowser();
			IEventBrowser browser = childBrowser.createIntersection(
										logBrowser.createDepthFilter(callEvent.getDepth() + 1));
			ArrayList<Long> timeStamps = new ArrayList<Long>();
			
			browser.setPreviousEvent(_event);
			while(browser.hasNext())
			{
				//get child event
				ILogEvent childEvent = browser.next();
				timeStamps.add(childEvent.getTimestamp());
				
			}
			return timeStamps;
			
		}		
		//else return null
			return null;
	}
	
	//take an event timestamp and returns the corresponding event
	public ILogEvent getEventFromTimestamp(long _timestamp)
	{
		IEventBrowser eventBrowser = logBrowser.createBrowser();
		eventBrowser.setNextTimestamp(_timestamp);
		ILogEvent event = eventBrowser.next();
		
		return event;
	}
	
	//if event is a behaviorExitEvent, returns parent method line number, else return-1
	public int getParentLineNum(ILogEvent _event)
	{
		if(_event instanceof BehaviorExitEvent)
		{
			int lineNum = getLineNum(_event);
			
			return lineNum;
		}
		return -1;
	}
	
	//returns the line number of an event, returns -1 if not available
	public int getLineNum(ILogEvent _event)
	{
		int lineNum = -1;
		ProbeInfo theProbeInfo = LocationUtils.getProbeInfo(_event);
		if(theProbeInfo !=null)
		{
			SourceRange srcRange = LocationUtils.getSourceRange(logBrowser.getStructureDatabase(),theProbeInfo);
			lineNum = srcRange.startLine;
		}
		
		return lineNum;
	}
	
	//returns the position of the start character of an event, returns -1 if not available
	public int getStartPos(ILogEvent _event)
	{
		int startPos = -1;
		ProbeInfo theProbeInfo = LocationUtils.getProbeInfo(_event);
		if(theProbeInfo !=null)
		{
			SourceRange srcRange = LocationUtils.getSourceRange(logBrowser.getStructureDatabase(),theProbeInfo);
			startPos = srcRange.startColumn;
		}
		
		return startPos;
	}
	
	//returns the position of the start character of an event, returns -1 if not available
	public int getEndPos(ILogEvent _event)
	{
		int endPos = -1;
		ProbeInfo theProbeInfo = LocationUtils.getProbeInfo(_event);
		if(theProbeInfo !=null)
		{
			SourceRange srcRange = LocationUtils.getSourceRange(logBrowser.getStructureDatabase(),theProbeInfo);
			endPos = srcRange.endColumn;
		}
		
		return endPos;
	}
	
	//takes an event and returns the line number of the next event
	public int getNextLineNum()
	{
		stepper.forwardStepInto();
		ILogEvent nextEvent = stepper.getCurrentEvent();
		return getLineNum(nextEvent);		
	}
	
	public ILogEvent forwardStepInto()
	{
		stepper.forwardStepInto();
		return stepper.getCurrentEvent();
	}
	
	public ILogEvent backwardStepInto()
	{
		stepper.backwardStepInto();
		return stepper.getCurrentEvent();
	}
	
	public ILogEvent getCurrentEvent()
	{
		return stepper.getCurrentEvent();
	}
	
	//get the next event without stepping forward
	public ILogEvent getNextEvent(ILogEvent _event)
	{
		IEventBrowser eventBrowser = logBrowser.createBrowser();
		eventBrowser.setPreviousEvent(_event);
		return eventBrowser.next();
	}
	
	public boolean isParentOf(ILogEvent parentEvent, ILogEvent currentEvent)
	{
		if(parentEvent.equals(currentEvent.getParent()))
			return true;
		else return false;
	}
	
	//takes a event and returns an array of LineNumberInfo 
	//(all the lines covered in the method the event belongs to)
	//each LineNumberInfo contains the line number and the col number
	//returns null if event has no probeInfo
	public LineNumberInfo[] getExecutionPath(ILogEvent _event)
	{
		ProbeInfo theProbeInfo = LocationUtils.getProbeInfo(_event);
		if(theProbeInfo != null)
		{
			IBehaviorInfo theBehavior = logBrowser.getStructureDatabase().getBehavior(theProbeInfo.behaviorId, true);
		    LineNumberInfo[] lineNumberInfoAry = theBehavior.getLineNumbers();
		    
		    return lineNumberInfoAry;
		}
		return null;
	}

	//givent an event, return a list of local variable info used in that method
	public List<LocalVariableInfo> getLocalVariables(ILogEvent _event)
	{
		ProbeInfo theProbeInfo = LocationUtils.getProbeInfo(_event);
		if(theProbeInfo != null)
		{
			IBehaviorInfo theBehavior = logBrowser.getStructureDatabase().getBehavior(theProbeInfo.behaviorId, true);
		    List<LocalVariableInfo> varInfoList = theBehavior.getLocalVariables();
		    
		    return varInfoList;
		}
		return null;
	}
	
	public String getLocalVarName(LocalVariableInfo varInfo)
	{
		return varInfo.getVariableName();
	}
	
	public String getLocalVarType(LocalVariableInfo varInfo)
	{
		return varInfo.getVariableTypeName();
	}
	
	public String getWriteEventVarName(ILogEvent _event)
	{
		if(isWriteEvent(_event))
		{
			if(_event instanceof IFieldWriteEvent)
			{
				IFieldWriteEvent fieldWriteEvent = (IFieldWriteEvent) _event;
				return fieldWriteEvent.getField().getName();
			}
			else if(_event instanceof IArrayWriteEvent)
			{
				IArrayWriteEvent aryWriteEvent = (IArrayWriteEvent)_event;
				Object obj = aryWriteEvent.getTarget();
				if (obj instanceof ObjectId)
				{
					ObjectId theObjectId = (ObjectId) obj;
					IObjectInspector objInspect = logBrowser.createObjectInspector(theObjectId);
					objInspect.setReferenceEvent(_event);
					return objInspect.getType().getName();
				}
				return null;
			}
			else if(_event instanceof ILocalVariableWriteEvent)
			{
				ILocalVariableWriteEvent localVarWriteEvent = (ILocalVariableWriteEvent)_event;
				return localVarWriteEvent.getVariable().getVariableName();
			}
		}

		return null;
	}
	
	//need to confirm if this is the proper way of getting a var value
	//some write events may return UID: [value]
	//if so just process the string to remove "UID :" for now
	public String getWriteEventValue(ILogEvent _event)
	{
		if(isWriteEvent(_event))
		{
			Object obj = null;
			
			if(_event instanceof IFieldWriteEvent)
			{
				IFieldWriteEvent fieldWriteEvent = (IFieldWriteEvent) _event;
				return fieldWriteEvent.getValue().toString();
			}
			else if(_event instanceof IArrayWriteEvent)
			{
				IArrayWriteEvent aryWriteEvent = (IArrayWriteEvent)_event;
				return aryWriteEvent.getValue().toString();
							}
			else if(_event instanceof ILocalVariableWriteEvent)
			{
				ILocalVariableWriteEvent localVarWriteEvent = (ILocalVariableWriteEvent)_event;
				return localVarWriteEvent.getValue().toString();
			}
			
			return null;
		}

		return null;
	}
	
	//print out event info for debugging
	public String printOutEvent_Debug(ILogEvent _event)
	{
		String display = "";

		IBehaviorCallEvent callEvent;
		if(_event instanceof IBehaviorCallEvent)
		{
			callEvent = (IBehaviorCallEvent) _event;
		
			if(callEvent.getExecutedBehavior() != null)
				display += callEvent.getExecutedBehavior().getName() + "\n";
			else
				display += callEvent.getCalledBehavior().getName() + "\n";
		}
		else if (_event instanceof IInstantiationEvent)
		{
			display += ((IInstantiationEvent) _event);
		}
		else if (_event instanceof IBehaviorCallEvent)
		{
			display += ((IBehaviorCallEvent) _event);
		}
		else if (_event instanceof IBehaviorExitEvent)
		{
			display += ((IBehaviorExitEvent) _event);
		}
		else if (_event instanceof IFieldWriteEvent)
		{
			display += ((IFieldWriteEvent) _event).getOperationBehavior().getName();
		}
        else if (_event instanceof ILocalVariableWriteEvent)
		{
        	ILocalVariableWriteEvent lVWEvent = (ILocalVariableWriteEvent) _event; 
        	display += lVWEvent.getVariable().getVariableName() + " = " + lVWEvent.getValue() + "\n";
		}
		else if (_event instanceof IOutputEvent)
		{
			display += ((IOutputEvent) _event);
		}
		else if (_event instanceof IExceptionGeneratedEvent)
		{
			display += ((IExceptionGeneratedEvent) _event);
		}
		else if (_event instanceof IArrayWriteEvent)
		{
			IArrayWriteEvent aWEvent = (IArrayWriteEvent) _event;
			display += " write: " + aWEvent.getValue() + " to " + aWEvent.getTarget() + "\n";
		}
			
		return display;

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
//			JFrame theFrame = new JFrame("TOD Post-It");
//			theFrame.setContentPane(aComponent);
//			theFrame.pack();
//			theFrame.setVisible(true);
		}

		public IExtensionPoints getExtensionPoints()
		{
			return null;
		}
	}
}
