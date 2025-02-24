package com.example.demo.touchpoint;

import java.io.File;
import java.util.Map;
import java.util.Properties;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.equinox.internal.p2.touchpoint.eclipse.EclipseTouchpoint;
import org.eclipse.equinox.internal.provisional.frameworkadmin.Manipulator;
import org.eclipse.equinox.p2.engine.spi.ProvisioningAction;
import org.eclipse.osgi.util.NLS;

/**
 * Implements a eclipse touch point action Action Name : setp2home Complete name
 * : com.ibm.cic.licensing.common.p2.touchpoint.setp2home.
 * 
 * This class provides implementation for the same.
 * 
 * @author sara
 *
 */
public class SetConfigValue extends ProvisioningAction {

	public SetConfigValue() {
		// Called by eclipse at runtime.
	}

	/**
	 * Executes the action. We need to use eclipse non-API's for achieving this
	 * action. Code is similar to eclipse touchpoint action 'setProgramProperty'
	 */
	@Override
	public IStatus execute(Map<String, Object> parameters) {
		// Get the manipulator
		Manipulator manipulator = (Manipulator) parameters.get(EclipseTouchpoint.PARM_MANIPULATOR);

		if (manipulator.getConfigData().getProperties().containsKey("some.property")) {
			// com.ibm.cic.licensing.p2.home.dir proeprty is already set in config.ini
			// Do not overwrite.
			return Status.OK_STATUS;
		}

		// Set the new property in config data.
		String p2HomeDir = getP2HomeDir();
		manipulator.getConfigData().setProperty("some.property", p2HomeDir);
		try {
			// Save back into config.ini by taking a backup.
			manipulator.save(true);
			System.setProperty("some.property", p2HomeDir);
		} catch (Exception e) {
			// Error while saving the config.ini
			Status status = new Status(IStatus.ERROR, "com.example.demo.touchpoint",
					NLS.bind("Error saving property into configuration data.", "some.property"), e);

			return status;
		}

		return Status.OK_STATUS;
	}

	/**
	 * Determines the P2 home dir to be set as value to p2 home dir in config.ini
	 * based below rules in order.<br>
	 * <ol>
	 * <li>If user has set
	 * '<code>-Dinstall.lic.p2.home=<i>full path to existing dir</i></code>' this
	 * will be set. else<br>
	 * <li>Sets to all user directory if it is writable.
	 * <ul>
	 * <li>windows : C:\ProgramData which is %ALLUSERSPROFILE%
	 * <li>Linux : /var
	 * </ul>
	 * <li>Set's to user home directory.
	 * </p>
	 * <ul>
	 * <li>windows : Users app dir which is %APPDATA%
	 * <li>Linux : ${user.home}/var
	 * </ul>
	 * </ol>
	 * 
	 * @return computed director or a empty string.
	 */
	private String getP2HomeDir() {

		String retVal = null;

		/**
		 * A provision for user at install time to set the
		 * ProvisioningSetP2Home.LIC_P2HOME_PROP_NAME into config.ini.
		 */
		String fromSysProp = System.getProperty("some.other.property");
		if (null != fromSysProp) {
			File file = new File(fromSysProp);
			if (file.isDirectory()) {
			} else {
			}

			/**
			 * User has provided a custom location for p2 home dir. User should ensure this
			 * directory exist.
			 */
			retVal = fromSysProp;
		}

		boolean isWindows = Platform.getOS().toLowerCase().contains("win");
		Map<String, String> envMap = System.getenv();

		if (null == retVal) {
			/**
			 * User has not set property at install time. Try All User directory
			 */
			String allUserDir = null;
			if (isWindows) {
				for (String key : envMap.keySet()) {

					if (key.equalsIgnoreCase("ALLUSERSPROFILE")) {
						allUserDir = envMap.get(key);
						break;
					}

				}
			} else {
				// Else its a linux
				allUserDir = "/var";
			}

			// Check All user dir is accessible.
			if (null != allUserDir) {
				File allUserDirFile = new File(allUserDir);

				if (allUserDirFile.exists()) {
					if (allUserDirFile.canWrite()) {
						retVal = allUserDirFile.getAbsolutePath();
					} else {
					}
				} else {
					// hi
				}
			}
		}

		if (null == retVal && !isWindows) {
			/**
			 * If all user dir does not exist or not accessible. For linux alone rely on
			 * user.home from JAVA
			 */
			// Set the user home director
			String userHome = System.getProperty("user.home");
			if (null != userHome) {
				retVal = userHome + "/var";
			}
		}

		/**
		 * Still not found. Get from environment variable
		 */
		if (null == retVal) {
			// Default it to linux.
			String envKey = "HOME";
			if (isWindows) {
				envKey = "APPDATA";
			}

			String userHome = null;
			for (String key : envMap.keySet()) {

				if (key.equalsIgnoreCase(envKey)) {
					if (isWindows)
						userHome = envMap.get(key);
					else
						userHome = envMap.get(key) + "/var"; // For linux

					retVal = userHome;
				}

			}
		}

		if (null != retVal && !retVal.trim().isEmpty()) {
			File file = new File(retVal);
			return file.getAbsolutePath();
		}

		return "";
	}

	/*
	 * It is the undo action for setp2home. (non-Javadoc)
	 * 
	 * @see org.eclipse.equinox.p2.engine.spi.ProvisioningAction#undo(java.util.Map)
	 */
	@Override
	public IStatus undo(Map<String, Object> parameters) {

		Manipulator manipulator = (Manipulator) parameters.get(EclipseTouchpoint.PARM_MANIPULATOR);

		Properties configProps = manipulator.getConfigData().getProperties();

		if (!configProps.containsKey("some.property")) {
			// com.ibm.cic.licensing.p2.home.dir property is not present. Not required to
			// remove and save

			// Do not overwrite.
			return Status.OK_STATUS;
		}

		configProps.remove("some.property");

		manipulator.getConfigData().setProperties(configProps);
		try {
			manipulator.save(true);
		} catch (Exception e) {

			Status status = new Status(IStatus.ERROR, "com.example.demo.touchpoint",
					NLS.bind("Error removing property into configuration data.", "some.property"), e);

			return status;
		}

		return Status.OK_STATUS;
	}

}
