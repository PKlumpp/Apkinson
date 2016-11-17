// PluginInterface.aidl

package com.applications.philipp.apkinson.Plugins;

import com.applications.philipp.apkinson.Plugins.IApkinsonCallback;
import com.applications.philipp.apkinson.Plugins.ShortArray;
import com.applications.philipp.apkinson.Plugins.PluginOption;

/** Plugin service interface */
interface IApkinsonPlugin {
	/** Called by Apkinson when a new call starts */
	void startInput(int sampleRate);
	/** Called by Apkinson when a new call starts, with options */
	void startInputWithOptions(int sampleRate, in Map options);
	/** Called by Apkinson when call ends */
	void stopInput();
	/** Used to send a new package of PCM audio data to the plugin */
	void sendPCMData(in ShortArray buffer);
	/** Register a callback for notifications back to Apkinson */
	void registerCallback(IApkinsonCallback callback);
	/** Unregister a previously registered Callback object */
	void unregisterCallback(IApkinsonCallback callback);
	/** Can be used to get a list of supported options */
	List<PluginOption> getOptions();
}