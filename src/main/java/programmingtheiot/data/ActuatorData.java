package programmingtheiot.data;

import java.io.Serializable;
import programmingtheiot.common.ConfigConst;

public class ActuatorData extends BaseIotData implements Serializable
{
	// private variables
	private int command = ConfigConst.DEFAULT_COMMAND;
	private float value = ConfigConst.DEFAULT_VAL;
	private boolean isResponse = false;
	private String stateData = "";

	// constructors
	public ActuatorData()
	{
		super();
	}
	
	// public methods
	public int getCommand()
	{
		return this.command;
	}
	
	public String getStateData()
	{
		return this.stateData;
	}
	
	public float getValue()
	{
		return this.value;
	}
	
	public boolean isResponseFlagEnabled()
	{
		return this.isResponse;
	}
	
	public void setAsResponse()
	{
		this.isResponse = true;
	}
	
	public void setCommand(int command)
	{
		this.command = command;
	}
	
	public void setStateData(String stateData)
	{
		if (stateData != null) {
			this.stateData = stateData;
		}
	}
	
	public void setValue(float val)
	{
		this.value = val;
	}
	
	// protected methods
	protected void handleUpdateData(BaseIotData data)
	{
		if (data instanceof ActuatorData) {
			ActuatorData aData = (ActuatorData) data;
			this.setCommand(aData.getCommand());
			this.setValue(aData.getValue());
			this.setStateData(aData.getStateData());
			this.isResponse = aData.isResponseFlagEnabled();
		}
	}
	
	// toString method
	public String toString()
	{
		StringBuilder sb = new StringBuilder(super.toString());
		sb.append(',');
		sb.append(ConfigConst.COMMAND_PROP).append('=').append(this.getCommand()).append(',');
		sb.append(ConfigConst.IS_RESPONSE_PROP).append('=').append(this.isResponseFlagEnabled()).append(',');
		sb.append(ConfigConst.VALUE_PROP).append('=').append(this.getValue());
		return sb.toString();
	}
}
