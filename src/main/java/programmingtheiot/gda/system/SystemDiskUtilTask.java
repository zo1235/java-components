package programmingtheiot.gda.system;
import java.lang.management.ManagementFactory;
import programmingtheiot.common.ConfigConst;
import java.lang.management.OperatingSystemMXBean;
import java.lang.management.MemoryUsage;
import java.util.logging.Logger;
import java.io.File;
/**
 * Shell representation of class for student implementation.
 * 
 */
public class SystemDiskUtilTask extends BaseSystemUtilTask
{
// constructors
/**
* Default.
* 
*/
public SystemDiskUtilTask()
{
super(ConfigConst.NOT_SET, ConfigConst.DEFAULT_TYPE_ID);
}
// public methods
@Override
public float getTelemetryValue()
{
File root = new File("/");
double diskUtil = 1.0f - ((double)root.getFreeSpace()/ (double)root.getTotalSpace());
return (float) diskUtil;
}
}