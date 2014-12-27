package com.vrheadphones.toyprojects.buivuhoang.androidgyroscopereading;

import android.content.Context;
import android.content.SharedPreferences;
import android.view.KeyCharacterMap;

import java.util.LinkedList;

public class Settings {
	public static String ip;

	public static boolean tapToClick;
	public static int clickTime;

	// public static boolean trackValue;

	//
	private static SharedPreferences prefs;
	//
	private static final String PREFS_IPKEY = "remoteip";
	private static final String PREFS_TAPTIME = "taptime";
	private static final String PREFS_RECENT_IP_PREFIX = "recenthost";
	//
	private static final String PREFS_FILENAME = "VRHeadphones";
	// number of hosts to save in the history
	public static final int MAX_SAVED_HOSTS = 5;

	/**
	 * this is the working data set of the saved hosts in the history this
	 * mirrors the data in the settings
	 */
	public static LinkedList<String> savedHosts;

	public static void init(Context con) {
		if (prefs == null) {
			prefs = con.getSharedPreferences(PREFS_FILENAME, Context.MODE_PRIVATE);
			// set up the stack for the saved hosts
			savedHosts = new LinkedList<String>();
			populateRecentIPs();

			// get all preferences
			ip = prefs.getString(PREFS_IPKEY, "127.0.0.1");
			clickTime = prefs.getInt(Settings.PREFS_TAPTIME, 200);
		}
	}

	public static void setIp(String ip) throws Exception {
		SharedPreferences.Editor edit = prefs.edit();
		testIPValid(ip);
		edit.putString(Settings.PREFS_IPKEY, ip);
		edit.commit();
		Settings.ip = ip;

		// save ip into a list of most used hosts here:
		// push IP onto recent stack
		if (!savedHosts.contains(ip)) {
			if (savedHosts.size() < MAX_SAVED_HOSTS) {
				savedHosts.addFirst(ip);
			} else {
				savedHosts.removeLast();
				savedHosts.addFirst(ip);
			}
		} else {
			while (savedHosts.contains(ip)) {
				savedHosts.remove(ip);
			}
			savedHosts.addFirst(ip);
		}
		// save recent ips to settings
		writeRecentIPsToSettings();

	}

	private static void testIPValid(String ip) throws Exception {
		try {
			String[] octets = ip.split("\\.");
			for (String s : octets) {
				int i = Integer.parseInt(s);
				if (i > 255 || i < 0) {
					throw new NumberFormatException();
				}
			}
		} catch (NumberFormatException e) {
			throw new Exception("Illegal IP address!");
		}

	}

	private static void writeRecentIPsToSettings() {
		SharedPreferences.Editor edit = prefs.edit();
		String s;
		for (int i = 0; i < MAX_SAVED_HOSTS; ++i) {
			try {
				s = savedHosts.get(i);
			} catch (IndexOutOfBoundsException e) {
				s = null;
			}
			edit.putString(PREFS_RECENT_IP_PREFIX + ((Integer) i).toString(), s);
		}
		edit.commit();
	}

	private static void populateRecentIPs() {
		savedHosts.clear();
		for (int i = 0; i < MAX_SAVED_HOSTS; ++i) {
			String host = prefs.getString(PREFS_RECENT_IP_PREFIX + ((Integer) i).toString(), null);
			if (host != null) {
				savedHosts.add(host);
			}
		}
	}

	// deletes a saved host from the list of saved hosts, by string
	public static void removeSavedHost(CharSequence ip) throws Exception {

		// remove ip from list
		if (savedHosts.remove(ip.toString())) {
			// rewrite settings
			writeRecentIPsToSettings();

		} else {
			throw new Exception("did not find " + ip.toString() + " in saved host list");
		}
	}
}
