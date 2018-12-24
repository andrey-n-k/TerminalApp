/*
 * Arello Mobile
 * Mobile Framework
 * Except where otherwise noted, this work is licensed under a Creative Commons Attribution 3.0 Unported License
 * http://creativecommons.org/licenses/by/3.0
 */

package  com.arellomobile.android.libs.network;

import java.util.Vector;

/**
 * <p>
 * Arello Mobile<br/>
 * Mobile Framework<br/>
 * Except where otherwise noted, this work is licensed under a Creative Commons Attribution 3.0 Unported License<br/>
 * <a href="http://creativecommons.org/licenses/by/3.0">http://creativecommons.org/licenses/by/3.0</a></br>
 * </p>
 *
 * Pool returns instance of interface {@link  com.arellomobile.android.libs.network.INetwork}<br/>
 * For returned instance developer should call {@link  com.arellomobile.android.libs.network.Network#getInputForRequest(String, java.util.Map, int, java.util.Date, String, String)} or {@link  com.arellomobile.android.libs.network.Network#openConnection(String, java.util.Map, int, java.util.Date, String, String)} with {@link Network#close()} in one synchronized block.<br/>
 * Sample: <br/>
 * <code>
 * 		INetwork network = Network.getInstance();
 * 		synchronized (network) {
 * 			InputStream serverInput = network.getInputForRequest(request.getUrl(), request.getParameters(), request.getMethod());
 * 			// some work with response
 * 			network.close();
 * 		}
 * </code>
 * @author Swift 28.01.2010
 */
public class NetworkPool {
	private int lastReturned;
	private static NetworkPool instance;
	private Vector<Network> networks;
	protected int count;

	public static NetworkPool getInstance() {
		if (instance == null) instance = new NetworkPool();
		return instance;
	}

	private NetworkPool() {
		count = Integer.parseInt(System.getProperty("com.arellomobile.networkpull.count", "4"));
		networks = new Vector<Network>();
		networks.setSize(count);
		for (int i = 0; i < networks.size(); i++) {
			networks.setElementAt(new Network(), i);
		}
	}

	public INetwork getNetwork() {
		for (int i = 0; i < networks.size(); i++) {
			Network network = networks.elementAt(i);
			if (network.isFree()) {
				lastReturned = i;
				return network;
			}
		}
		// if all is busy return next after previous 
		lastReturned = (lastReturned+1) % count;
		return networks.elementAt(lastReturned);
	}
}
