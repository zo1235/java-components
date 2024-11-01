package programmingtheiot.data;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import programmingtheiot.common.ConfigConst;

public class SystemStateData extends BaseIotData implements Serializable
{
	// private variables
	private int command = ConfigConst.DEFAULT_COMMAND;
	private List<SystemPerformanceData> sysPerfDataList = new ArrayList<>();
	private List<SensorData> sensorDataList = new ArrayList<>();
	
	// constructors
	public SystemStateData()
	{
		super();
	}

	// public methods
	public boolean addSensorData(SensorData data)
	{
		return this.sensorDataList.add(data);
	}
	
	public boolean addSystemPerformanceData(SystemPerformanceData data)
	{
		return this.sysPerfDataList.add(data);
	}
	
	public int getCommand()
	{
		return this.command;
	}
	
	public List<SensorData> getSensorDataList()
	{
		return this.sensorDataList;
	}
	
	public List<SystemPerformanceData> getSystemPerformanceDataList()
	{
		return this.sysPerfDataList;
	}
	
	public void setCommand(int actionCmd)
	{
		this.command = actionCmd;
	}
	
	// protected methods
	protected void handleUpdateData(BaseIotData data)
	{
		if (data instanceof SystemStateData) {
			SystemStateData sysData = (SystemStateData) data;
			this.setCommand(sysData.getCommand());
			this.sysPerfDataList.addAll(sysData.getSystemPerformanceDataList());
			this.sensorDataList.addAll(sysData.getSensorDataList());
		}
	}
	
	// toString method
	public String toString()
	{
		StringBuilder sb = new StringBuilder(super.toString());
		sb.append(',');
		sb.append(ConfigConst.COMMAND_PROP).append('=').append(this.getCommand()).append(',');
		sb.append(ConfigConst.SENSOR_DATA_LIST_PROP).append('=').append(this.getSensorDataList()).append(',');
		sb.append(ConfigConst.SYSTEM_PERF_DATA_LIST_PROP).append('=').append(this.getSystemPerformanceDataList());
		return sb.toString();
	}
}
