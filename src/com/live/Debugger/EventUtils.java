package com.live.Debugger;

import java.awt.Component;
import java.awt.Dimension;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JComponent;
import javax.swing.JOptionPane;

import tod.core.config.TODConfig;
import tod.core.database.browser.ICompoundInspector.EntryValue;
import tod.core.database.browser.IEventBrowser;
import tod.core.database.browser.ILogBrowser;
import tod.core.database.browser.IObjectInspector;
import tod.core.database.browser.IObjectInspector.IEntryInfo;
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
import tod.tools.interpreter.ToStringComputer;

public class EventUtils {

	private ILogBrowser logBrowser;
	private Stepper stepper;
	private long firstEventTimestamp;
	private long lastEventTimestamp;
	
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
			IEventBrowser eventBrowser = logBrowser.createBrowser();
			
			//create stepper
			stepper = new Stepper(logBrowser);
			
			//set the current event in the stepper to point to the first event
			if(eventBrowser.hasNext())
			stepper.setCurrentEvent(eventBrowser.next());
			
//			firstEventTimestamp = eventBrowser.getFirstTimestamp();
//			lastEventTimestamp = eventBrowser.getLastTimestamp(); 
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
	
	public void setFirstTimestamp(long _timestamp)
	{
		firstEventTimestamp = _timestamp;
	}
	
	public void setCurrentEvent(ILogEvent _event)
	{
		stepper.setCurrentEvent(_event);
	}
	
	public long getFirstTimestamp()
	{
		return firstEventTimestamp;
	}
	
	public void setLastTimestamp(long _timestamp)
	{
		lastEventTimestamp = _timestamp;
	}
	
	public long getLastTimestamp()
	{
		return lastEventTimestamp;
	}
	
	public boolean validTimestamp(long _timestamp)
	{
		if(firstEventTimestamp <= _timestamp && lastEventTimestamp >= _timestamp)
			return true;
		else
			return false;
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
			_event instanceof ILocalVariableWriteEvent ||
			_event instanceof IInstantiationEvent)
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
	
	//returns the method name of an event
	//if event is a write event, get the parent and return the method name of the parent event
	public String getMethodName(ILogEvent _event)
	{
		if(isMethodCall(_event))
		{
			MethodCallEvent methodCallEvent = (MethodCallEvent) _event;			
			return getNameFromMethodCallEvent(methodCallEvent);

		}else if(isWriteEvent(_event))
		{
			MethodCallEvent methodCallEvent = (MethodCallEvent) _event.getParent();			
			return getNameFromMethodCallEvent(methodCallEvent);
		}
			return null;
	}
	
	private String getNameFromMethodCallEvent(MethodCallEvent methodCallEvent)
	{
		if(methodCallEvent.getExecutedBehavior()!=null)
			return methodCallEvent.getExecutedBehavior().getName();
		else
			return methodCallEvent.getCalledBehavior().getName();
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
				LineNumberInfo[] lineNumAry;
				if(callEvent.getExecutedBehavior() != null)
					lineNumAry = callEvent.getExecutedBehavior().getLineNumbers();
				else
					lineNumAry = callEvent.getCalledBehavior().getLineNumbers();
			
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

			ArrayList<Long> timeStamps = new ArrayList<Long>();
			
			childBrowser.setNextTimestamp(_event.getTimestamp());
			while(childBrowser.hasNext())
			{
				//get child event
				ILogEvent childEvent = childBrowser.next();
				timeStamps.add(childEvent.getTimestamp());
				
			}
			return timeStamps;
			
		}		
		//else return null
			return null;
	}
	
	public ArrayList<EventInfo> getChildEventsInfo(ILogEvent _event)
	{
		//check if event is a method call
		if(isMethodCall(_event))
		{
			//get child event browser
			MethodCallEvent callEvent = (MethodCallEvent)_event;
			IEventBrowser childBrowser = callEvent.getChildrenBrowser();

			ArrayList<EventInfo> eventInfo = new ArrayList<EventInfo>();
			
			childBrowser.setNextTimestamp(_event.getTimestamp());
			while(childBrowser.hasNext())
			{
				//get child event
				ILogEvent childEvent = childBrowser.next();
				
//				Object[] info = new Object[3];
//				
//				info[0] = childEvent.getTimestamp();
//				
//				if(isMethodCall(childEvent))
//					info[1] = getMethodName(childEvent);
//				else
//					info[1] = getWriteEventVarName(childEvent);
//				
//				info[2] = getWriteEventValue(childEvent);
				
				EventInfo info = new EventInfo(childEvent.getTimestamp(), getLineNum(childEvent));
				
				if(isMethodCall(childEvent))
					info.setMethodName(getMethodName(childEvent));
				else
				{
					info.setVarName(getWriteEventVarName(childEvent));
					info.setWriteValue(getWriteEventValue(childEvent));
				}
				
				eventInfo.add(info);
			}
			return eventInfo;			
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
		if(hasNextEvent(stepper.getCurrentEvent()))
		{
			stepper.forwardStepInto();
			return stepper.getCurrentEvent();
		}
		return null;
	}
	
	public ILogEvent forwardStepOver()
	{
		if(hasNextEvent(stepper.getCurrentEvent()))
		{
			stepper.forwardStepOver();
			return stepper.getCurrentEvent();
		}
		return null;
	}
	
	public ILogEvent StepOut()
	{
		if(hasNextEvent(stepper.getCurrentEvent()))
		{
			stepper.stepOut();
			return stepper.getCurrentEvent();
		}
		return null;
	}
	
	public ILogEvent backwardStepInto()
	{
		if(hasPrevEvent(stepper.getCurrentEvent()))
		{
			stepper.backwardStepInto();
			return stepper.getCurrentEvent();
		}
		return null;
	}
	
	public ILogEvent backwardStepOver()
	{
		if(hasPrevEvent(stepper.getCurrentEvent()))
		{
			stepper.backwardStepOver();
			return stepper.getCurrentEvent();
		}
		return null;
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
			
			String value = null;
			String name = null;
			
			if(_event instanceof IFieldWriteEvent)
			{
				IFieldWriteEvent fieldWriteEvent = (IFieldWriteEvent) _event;
				obj = fieldWriteEvent.getValue();
				value = fieldWriteEvent.getValue().toString();
				name = fieldWriteEvent.getField().getName();
			}
			else if(_event instanceof IArrayWriteEvent)
			{
				IArrayWriteEvent aryWriteEvent = (IArrayWriteEvent)_event;
				obj = aryWriteEvent.getValue();
				value = aryWriteEvent.getValue().toString();
				name = aryWriteEvent.getTarget().toString();
			}
			else if(_event instanceof ILocalVariableWriteEvent)
			{
				ILocalVariableWriteEvent localVarWriteEvent = (ILocalVariableWriteEvent)_event;
				obj =  localVarWriteEvent.getValue();
				value = localVarWriteEvent.getValue().toString();
				name = localVarWriteEvent.getVariable().getVariableName();
				if(localVarWriteEvent.getVariable().getVariableTypeName().compareToIgnoreCase("C") == 0)
					value = Character.toString((char)Integer.parseInt(value)); 
			}
			

			if(value != null && value.startsWith("UID"))
			{
//				if(name.compareTo("result") == 0)
				if (obj instanceof ObjectId)
				{
					ObjectId theObjectId = (ObjectId) obj;
					
//					Object theRegistered = logBrowser.getRegistered(theObjectId);
//					if (theRegistered != null) value = theRegistered.toString();
//					
//					System.out.println(value);

					
//					IObjectInspector oi = logBrowser.createObjectInspector(theObjectId);
//					oi.setReferenceEvent(_event); // Sets the point in time at which to reconstitute the object's state; could be your write event
//					List<IEntryInfo> entries = oi.getEntries(0, oi.getEntryCount()); // Entries are object fields or array slots
//					for(IEntryInfo entry : entries) {
////						System.out.println(entry.toString());
//					    EntryValue[] possibleValues = oi.getEntryValue(entry);
//					    System.out.println(possibleValues[0].getValue()); // Could have more than one possible value, or maybe no one, so you must check first
//					    if(possibleValues[0].getValue() != null)
//					    	value = possibleValues[0].getValue().toString();
//					}
					
					
					IObjectInspector oi = logBrowser.createObjectInspector(theObjectId);
					value = oi.getType().getName();
					
				}
			}
			
			return value;
//			if(name.compareTo("result") == 0)
//			if (obj instanceof ObjectId)
//			{
//				ObjectId theObjectId = (ObjectId) obj;
//				
//				TODConfig config = logBrowser.getSession().getConfig();
//				IObjectInspector oi = logBrowser.createObjectInspector(theObjectId);
//				oi.setReferenceEvent(_event);
//				ToStringComputer c = new ToStringComputer(config, oi);
//				System.out.println(c.compute());
//				System.out.println();
//			}
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
	
	private boolean hasNextEvent(ILogEvent _event)
	{
		IEventBrowser browser = logBrowser.createBrowser();
		browser.setNextEvent(_event);
		
		return browser.hasNext();
	}
	
	private boolean hasPrevEvent(ILogEvent _event)
	{
		IEventBrowser browser = logBrowser.createBrowser();
		browser.setPreviousEvent(_event);
		
		return browser.hasPrevious();
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
