package programmingtheiot.data;

import java.io.Serializable;
import programmingtheiot.common.ConfigConst;

public class SystemPerformanceData extends BaseIotData implements Serializable
{
	// private variables
	private float cpuUtil = ConfigConst.DEFAULT_VAL;
	private float diskUtil = ConfigConst.DEFAULT_VAL;
	private float memUtil = ConfigConst.DEFAULT_VAL;

	// constructors
	public SystemPerformanceData()
	{
		super();
	}

	// public methods
	public float getCpuUtilization()
	{
		return this.cpuUtil;
	}
	
	public float getDiskUtilization()
	{
		return this.diskUtil;
	}
	
	public float getMemoryUtilization()
	{
		return this.memUtil;
	}
	
	public void setCpuUtilization(float val)
	{
		this.cpuUtil = val;
	}
	
	public void setDiskUtilization(float val)
	{
		this.diskUtil = val;
	}
	
	public void setMemoryUtilization(float val)
	{
		this.memUtil = val;
	}
	
	// protected methods
	protected void handleUpdateData(BaseIotData data)
	{
		if (data instanceof SystemPerformanceData) {
			SystemPerformanceData sysData = (SystemPerformanceData) data;
			this.setCpuUtilization(sysData.getCpuUtilization());
			this.setDiskUtilization(sysData.getDiskUtilization());
			this.setMemoryUtilization(sysData.getMemoryUtilization());
		}
	}

	// toString method
	public String toString()
	{
		StringBuilder sb = new StringBuilder(super.toString());
		sb.append(',');
		sb.append(ConfigConst.CPU_UTIL_PROP).append('=').append(this.getCpuUtilization()).append(',');
		sb.append(ConfigConst.DISK_UTIL_PROP).append('=').append(this.getDiskUtilization()).append(',');
		sb.append(ConfigConst.MEM_UTIL_PROP).append('=').append(this.getMemoryUtilization());
		return sb.toString();
	}
}
