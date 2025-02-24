package com.example.demo.touchpoint;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Map;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.equinox.internal.provisional.frameworkadmin.Manipulator;
import org.eclipse.equinox.p2.engine.spi.ProvisioningAction;
import org.eclipse.equinox.internal.p2.touchpoint.eclipse.EclipseTouchpoint;

/**
 * Demonstrates a minimal provisioning action that follows a similar structure
 * to ProvisioningInstallLUMKit, but without IBM-specific code.
 */
@SuppressWarnings("restriction") // because EclipseTouchpoint & Manipulator are internal
public class MinimalProvisioningAction extends ProvisioningAction {

	private static final Logger LOGGER = Logger.getLogger(MinimalProvisioningAction.class.getName());

	// Key expected in the .p2.inf file, e.g.:
	// instructions.configure = \
	// com.example.demo.touchpoint.action(incomingFile:myTestFile.txt);
	private static final String PARAM_INCOMING_FILE = "incomingFile";

	@Override
	public IStatus execute(Map<String, Object> parameters) {
		LOGGER.info("=== [MinimalProvisioningAction.execute] START ===");

		// --------------------------------------------------------------------
		// 1) Check if we have our custom "incomingFile" parameter
		// --------------------------------------------------------------------
		if (!parameters.containsKey(PARAM_INCOMING_FILE)) {
			LOGGER.severe("ERROR: No 'incomingFile' parameter provided.");
			return new Status(IStatus.ERROR, "com.example.demo.touchpoint", "No 'incomingFile' parameter found.");
		}

		Object incomingFileObj = parameters.get(PARAM_INCOMING_FILE);
		if (incomingFileObj == null) {
			LOGGER.severe("ERROR: 'incomingFile' parameter is null.");
			return new Status(IStatus.ERROR, "com.example.demo.touchpoint", "'incomingFile' parameter is null.");
		}
		// We will later write this value to file.
		LOGGER.info("Found 'incomingFile' param: " + incomingFileObj);

		// --------------------------------------------------------------------
		// 2) Retrieve the artifact location (p2 typical approach)
		// --------------------------------------------------------------------
		Object artifactLocation = parameters.get(EclipseTouchpoint.PARM_ARTIFACT_LOCATION);
		if (artifactLocation == null) {
			LOGGER.severe("ERROR: artifact.location is missing or null.");
			return new Status(IStatus.ERROR, "com.example.demo.touchpoint",
					"No artifact.location found; cannot proceed.");
		}
		LOGGER.info("Artifact location: " + artifactLocation.toString());

		// --------------------------------------------------------------------
		// 3) Retrieve the manipulator, if present
		// --------------------------------------------------------------------
		Manipulator manipulator = (Manipulator) parameters.get(EclipseTouchpoint.PARM_MANIPULATOR);
		if (manipulator != null) {
			LOGGER.info("Manipulator found; reading config properties...");
			Properties configProperties = manipulator.getConfigData().getProperties();
			for (Object key : configProperties.keySet()) {
				LOGGER.info("   Config Property: " + key + " = " + configProperties.get(key));
			}
		} else {
			LOGGER.info("No manipulator was provided in parameters.");
		}

		// --------------------------------------------------------------------
		// 4) Determine location to place the output file
		// --------------------------------------------------------------------
		String existingP2HomeDir = null;
		if (manipulator != null) {
			Properties configProps = manipulator.getConfigData().getProperties();
			if (configProps.containsKey("com.example.p2home")) {
				existingP2HomeDir = configProps.getProperty("com.example.p2home");
				LOGGER.info("Found 'com.example.p2home': " + existingP2HomeDir);
			}
		}

		if (existingP2HomeDir == null) {
			// Fallback if no property is found
			existingP2HomeDir = System.getProperty("user.home");
			LOGGER.info("Using fallback user.home: " + existingP2HomeDir);
		}

		// Prepare a demonstration file
		File p2HomeDir = new File(existingP2HomeDir);
		File minimalFile = new File(p2HomeDir, "minimalTouchpoint.txt");

		LOGGER.info("Target file path: " + minimalFile.getAbsolutePath());

		// --------------------------------------------------------------------
		// 5) Ensure the file exists, then write/append our parameter text
		// --------------------------------------------------------------------
		try {
			// If parent dirs don't exist, create them
			if (!p2HomeDir.exists()) {
				LOGGER.info("Creating parent directories for " + p2HomeDir.getAbsolutePath());
				p2HomeDir.mkdirs();
			}

			if (!minimalFile.exists()) {
				LOGGER.info("Creating new file: " + minimalFile.getAbsolutePath());
				minimalFile.createNewFile();
			}

			// Check write permission
			if (!minimalFile.canWrite()) {
				LOGGER.warning("No permission to write: " + minimalFile.getAbsolutePath());
				return new Status(IStatus.WARNING, "com.example.demo.touchpoint",
						"No permission to write " + minimalFile.getAbsolutePath());
			}

			// Actually append the parameter value
			LOGGER.info("Appending '" + incomingFileObj + "' to " + minimalFile.getAbsolutePath());
			try (FileWriter writer = new FileWriter(minimalFile, true)) {
				writer.write(incomingFileObj + "\n");
			}

		} catch (IOException e) {
			LOGGER.log(Level.SEVERE, "Error while creating or writing file: " + e.getMessage(), e);
			return new Status(IStatus.ERROR, "com.example.demo.touchpoint",
					"IOException while writing: " + minimalFile.getAbsolutePath(), e);
		}

		// --------------------------------------------------------------------
		// 6) Return success
		// --------------------------------------------------------------------
		LOGGER.info("=== [MinimalProvisioningAction.execute] SUCCESS ===");
		return Status.OK_STATUS;
	}

	@Override
	public IStatus undo(Map<String, Object> parameters) {
		LOGGER.info("=== [MinimalProvisioningAction.undo] START ===");
		// In a real scenario, remove or revert the file changes here.
		LOGGER.info("No undo logic implemented. Returning OK_STATUS.");
		LOGGER.info("=== [MinimalProvisioningAction.undo] END ===");
		return Status.OK_STATUS;
	}
}
