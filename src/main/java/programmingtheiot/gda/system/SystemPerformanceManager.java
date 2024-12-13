package programmingtheiot.gda.system;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import programmingtheiot.common.ConfigConst;
import programmingtheiot.common.ConfigUtil;
import programmingtheiot.common.IDataMessageListener;
import programmingtheiot.common.ResourceNameEnum;
import programmingtheiot.data.SystemPerformanceData;
import java.util.logging.Logger;

/**
 * SystemPerformanceManager is responsible for monitoring system performance.
 * 
 */
public class SystemPerformanceManager
{
	// private variables
	private ScheduledExecutorService schedExecSvc = null;
	private SystemCpuUtilTask sysCpuUtilTask = null;
	private SystemMemUtilTask sysMemUtilTask = null;
	private SystemDiskUtilTask sysDiskUtilTask = null; // New task for disk utilization
	private Runnable taskRunner = null;
	private boolean isStarted = false;
	private String locationID = ConfigConst.NOT_SET; // Location ID variable
	private IDataMessageListener dataMsgListener = null; // Listener for telemetry data
	
	private static final Logger _Logger = Logger.getLogger(SystemPerformanceManager.class.getName());
	private int pollRate = ConfigConst.DEFAULT_POLL_CYCLES;

	// constructors
	
	/**
	 * Default constructor.
	 * Initializes the tasks and scheduler, retrieves location ID.
	 */
	public SystemPerformanceManager() {
	    this.pollRate = ConfigUtil.getInstance().getInteger(
	        ConfigConst.GATEWAY_DEVICE, ConfigConst.POLL_CYCLES_KEY, ConfigConst.DEFAULT_POLL_CYCLES);

	    if (this.pollRate <= 0) {
	        this.pollRate = ConfigConst.DEFAULT_POLL_CYCLES;
	    }

	    this.schedExecSvc = Executors.newScheduledThreadPool(1);
	    this.sysCpuUtilTask = new SystemCpuUtilTask();
	    this.sysMemUtilTask = new SystemMemUtilTask();
	    this.sysDiskUtilTask = new SystemDiskUtilTask(); // Initialize disk task

	    // Retrieve location ID from configuration
	    this.locationID = ConfigUtil.getInstance().getProperty(
	        ConfigConst.GATEWAY_DEVICE, ConfigConst.LOCATION_ID_PROP, ConfigConst.NOT_SET);

	    // Define the task runner for telemetry
	    this.taskRunner = () -> {
	        this.handleTelemetry();
	    };
	}
	
	// public methods
	
	/**
	 * Collect and handle system telemetry data (CPU, memory, disk).
	 */
	public void handleTelemetry() {
	    float cpuUtil = this.sysCpuUtilTask.getTelemetryValue();
	    float memUtil = this.sysMemUtilTask.getTelemetryValue();
	    float diskUtil = this.sysDiskUtilTask.getTelemetryValue(); // Disk utilization

	    // Log telemetry results at the info level
	    _Logger.info("Handle telemetry results: cpuUtil=" + cpuUtil + ", memUtil=" + memUtil + ", diskUtil=" + diskUtil);

	    // Create a new instance of SystemPerformanceData to store the telemetry
	    SystemPerformanceData spd = new SystemPerformanceData();
	    spd.setLocationID(this.locationID);
	    spd.setCpuUtilization(cpuUtil);
	    spd.setMemoryUtilization(memUtil);
	    spd.setDiskUtilization(diskUtil); // Store disk utilization data

	    // If listener is set, notify it of the new telemetry data
	    if (this.dataMsgListener != null) {
	        this.dataMsgListener.handleSystemPerformanceMessage(
	            ResourceNameEnum.GDA_SYSTEM_PERF_MSG_RESOURCE, spd);
	    }
	}
	
	/**
	 * Set the data message listener for callback notifications.
	 */
	public void setDataMessageListener(IDataMessageListener listener) {
	    if (listener != null) {
	        this.dataMsgListener = listener;
	    }
	}

	/**
	 * Start the system performance manager and telemetry collection.
	 */
	public boolean startManager() {
	    if (!this.isStarted) {
	        _Logger.info("SystemPerformanceManager is starting...");

	        // Schedule the task runner at a fixed rate
	        this.schedExecSvc.scheduleAtFixedRate(this.taskRunner, 1L, this.pollRate, TimeUnit.SECONDS);

	        this.isStarted = true;
	    } else {
	        _Logger.info("SystemPerformanceManager is already started.");
	    }

	    return this.isStarted;
	}
	
	/**
	 * Stop the system performance manager and telemetry collection.
	 */
	public boolean stopManager() {
	    this.schedExecSvc.shutdown();
	    this.isStarted = false;

	    _Logger.info("SystemPerformanceManager is stopped.");

	    return true;
	}
}
