package programmingtheiot.data;

import java.util.logging.Logger;
import com.google.gson.Gson;

public class DataUtil
{
    // static
    private static final Logger _Logger = Logger.getLogger(DataUtil.class.getName());
    private static final DataUtil _Instance = new DataUtil();

    // Declare Gson object as a class member
    private static final Gson gson = new Gson();  // Declared as static final

    public static final DataUtil getInstance()
    {
        return _Instance;
    }

    // constructors
    private DataUtil()
    {
        super();
    }

    // public methods
    public String actuatorDataToJson(ActuatorData data)
    {
        String jsonData = null;
        if (data != null) {
            jsonData = gson.toJson(data);
        }
        return jsonData;
    }

    public ActuatorData jsonToActuatorData(String jsonData)
    {
        ActuatorData data = null;
        if (jsonData != null && jsonData.trim().length() > 0) {
            data = gson.fromJson(jsonData, ActuatorData.class);
        }
        return data;
    }

    public String sensorDataToJson(SensorData data)
    {
        String jsonData = null;
        if (data != null) {
            jsonData = gson.toJson(data);
            _Logger.info("SensorData converted to JSON: " + jsonData);
        } else {
            _Logger.warning("SensorData input is null, cannot convert to JSON.");
        }
        return jsonData;
    }

    public SensorData jsonToSensorData(String jsonData)
    {
        SensorData data = null;
        if (jsonData != null && !jsonData.trim().isEmpty()) {
            data = gson.fromJson(jsonData, SensorData.class);
            _Logger.info("JSON converted to SensorData: " + data);
        } else {
            _Logger.severe("Input JSON data is null or empty.");
        }
        return data;
    }

    public String systemPerformanceDataToJson(SystemPerformanceData data)
    {
        String jsonData = null;
        if (data != null) {
            jsonData = gson.toJson(data);
            _Logger.info("SystemPerformanceData converted to JSON: " + jsonData);
        } else {
            _Logger.warning("SystemPerformanceData input is null, cannot convert to JSON.");
        }
        return jsonData;
    }

    public SystemPerformanceData jsonToSystemPerformanceData(String jsonData)
    {
        SystemPerformanceData data = null;
        if (jsonData != null && !jsonData.trim().isEmpty()) {
            data = gson.fromJson(jsonData, SystemPerformanceData.class);
            _Logger.info("JSON converted to SystemPerformanceData: " + data);
        } else {
            _Logger.severe("Input JSON data is null or empty.");
        }
        return data;
    }

    public String systemStateDataToJson(SystemStateData data)
    {
        String jsonData = null;
        if (data != null) {
            jsonData = gson.toJson(data);
            _Logger.info("SystemStateData converted to JSON: " + jsonData);
        } else {
            _Logger.warning("SystemStateData input is null, cannot convert to JSON.");
        }
        return jsonData;
    }

    public SystemStateData jsonToSystemStateData(String jsonData)
    {
        SystemStateData data = null;
        if (jsonData != null && !jsonData.trim().isEmpty()) {
            data = gson.fromJson(jsonData, SystemStateData.class);
            _Logger.info("JSON converted to SystemStateData: " + data);
        } else {
            _Logger.severe("Input JSON data is null or empty.");
        }
        return data;
    }
}
