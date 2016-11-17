// IApkinsonCallback.aidl
package com.applications.philipp.apkinson.Plugins;

/** Interface implemented by Apkinson so plugins can give feedback */
oneway interface IApkinsonCallback {
	/** To be called by plugin after registration to setup data for result viewing
		Usage of this data:
		Intent intent = new Intent(intentActionName);
		intent.putExtra(bundleResultKey, result);
		startActivity(intent);
	 */
	void onRegistered(String intentActionName, String bundleResultKey);
	/** To be called by plugin when result is ready after stopData() was called by Apkinson */
	void onResult(String result);
	/** Called if an error occured in the plugin and the call won't be successfully handled */
	void onError(String errorMsg);
}