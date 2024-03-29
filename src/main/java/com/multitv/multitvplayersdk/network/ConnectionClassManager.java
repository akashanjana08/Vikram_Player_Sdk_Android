/*
 *  Copyright (c) 2015, Facebook, Inc.
 *  All rights reserved.
 *
 *  This source code is licensed under the BSD-style license found in the
 *  LICENSE file in the root directory of this source tree. An additional grant
 *  of patent rights can be found in the PATENTS file in the same directory.
 *
 */

package com.multitv.multitvplayersdk.network;

import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicReference;

import android.support.annotation.NonNull;

/**
 * <p>
 * Class used to calculate the approximate bandwidth of a user's connection.
 * </p>
 * <p>
 * This class notifies all subscribed {@link ConnectionClassStateChangeListener}
 * with the new ConnectionClass when the network's ConnectionClass changes.
 * </p>
 */
public class ConnectionClassManager {

	static final double DEFAULT_SAMPLES_TO_QUALITY_CHANGE = 5;
	private static final int BYTES_TO_BITS = 8;

	/**
	 * Default values for determining quality of data connection. Bandwidth
	 * numbers are in Kilobits per second (kbps).
	 */
	private static final int DEFAULT_POOR_BANDWIDTH = 150;
	private static final int DEFAULT_MODERATE_BANDWIDTH = 550;
	private static final int DEFAULT_GOOD_BANDWIDTH = 2000;

	/**
	 * The factor used to calculate the current bandwidth depending upon the
	 * previous calculated value for bandwidth.
	 * 
	 * The smaller this value is, the less responsive to new samples the moving
	 * average becomes.
	 */
	private static final double DEFAULT_DECAY_CONSTANT = 0.05;

	/** Current bandwidth of the user's connection depending upon the response. */
	private ExponentialGeometricAverage mDownloadBandwidth = new ExponentialGeometricAverage(
			DEFAULT_DECAY_CONSTANT);
	private volatile boolean mInitiateStateChange = false;
	private AtomicReference<ConnectionQuality> mCurrentBandwidthConnectionQuality = new AtomicReference<ConnectionQuality>(
			ConnectionQuality.UNKNOWN);
	private AtomicReference<ConnectionQuality> mNextBandwidthConnectionQuality;
	private ArrayList<ConnectionClassStateChangeListener> mListenerList = new ArrayList<ConnectionClassStateChangeListener>();
	private int mSampleCounter;

	/**
	 * The lower bound for measured bandwidth in bits/ms. Readings lower than
	 * this are treated as effectively zero (therefore ignored).
	 */
	static final long BANDWIDTH_LOWER_BOUND = 10;

	// Singleton.
	private static class ConnectionClassManagerHolder {
		public static final ConnectionClassManager instance = new ConnectionClassManager();
	}

	/**
	 * Retrieval method for the DownloadBandwidthManager singleton.
	 * 
	 * @return The singleton instance of DownloadBandwidthManager.
	 */
	@NonNull
	public static ConnectionClassManager getInstance() {
		return ConnectionClassManagerHolder.instance;
	}

	// Force constructor to be private.
	private ConnectionClassManager() {
	}

	/**
	 * Adds bandwidth to the current filtered latency counter. Sends a broadcast
	 * to all {@link ConnectionClassStateChangeListener} if the counter moves
	 * from one bucket to another (i.e. poor bandwidth -> moderate bandwidth).
	 */
	public synchronized void addBandwidth(long bytes, long timeInMs) {

		// Ignore garbage values.
		if (timeInMs == 0
				|| (bytes) * 1.0 / (timeInMs) * BYTES_TO_BITS < BANDWIDTH_LOWER_BOUND) {
			return;
		}

		double bandwidth = (bytes) * 1.0 / (timeInMs) * BYTES_TO_BITS;
		mDownloadBandwidth.addMeasurement(bandwidth);

		if (mInitiateStateChange) {
			mSampleCounter += 1;
			if (getCurrentBandwidthQuality() != mNextBandwidthConnectionQuality
					.get()) {
				mInitiateStateChange = false;
				mSampleCounter = 1;
			}
			if (mSampleCounter >= DEFAULT_SAMPLES_TO_QUALITY_CHANGE) {
				mInitiateStateChange = false;
				mSampleCounter = 1;
				mCurrentBandwidthConnectionQuality
						.set(mNextBandwidthConnectionQuality.get());
				notifyListeners();
			}
			return;
		}

		if (mCurrentBandwidthConnectionQuality.get() != getCurrentBandwidthQuality()) {
			mInitiateStateChange = true;
			mNextBandwidthConnectionQuality = new AtomicReference<ConnectionQuality>(
					getCurrentBandwidthQuality());
		}
	}

	/**
	 * Resets the bandwidth average for this instance of the bandwidth manager.
	 */
	public void reset() {
		if (mDownloadBandwidth != null) {
			mDownloadBandwidth.reset();
		}
		mCurrentBandwidthConnectionQuality.set(ConnectionQuality.UNKNOWN);
	}

	/**
	 * Get the ConnectionQuality that the moving bandwidth average currently
	 * represents.
	 * 
	 * @return A ConnectionQuality representing the device's bandwidth at this
	 *         exact moment.
	 */
	public synchronized ConnectionQuality getCurrentBandwidthQuality() {
		if (mDownloadBandwidth == null) {
			return ConnectionQuality.UNKNOWN;
		}
		return mapBandwidthQuality(mDownloadBandwidth.getAverage());
	}

	private ConnectionQuality mapBandwidthQuality(double average) {
		if (average < 0) {
			return ConnectionQuality.UNKNOWN;
		}
		if (average < DEFAULT_POOR_BANDWIDTH) {
			return ConnectionQuality.POOR;
		}
		if (average < DEFAULT_MODERATE_BANDWIDTH) {
			return ConnectionQuality.MODERATE;
		}
		if (average < DEFAULT_GOOD_BANDWIDTH) {
			return ConnectionQuality.GOOD;
		}
		return ConnectionQuality.EXCELLENT;
	}

	/**
	 * Accessor method for the current bandwidth average.
	 * 
	 * @return The current bandwidth average, or -1 if no average has been
	 *         recorded.
	 */
	public synchronized double getDownloadKBitsPerSecond() {
		return mDownloadBandwidth == null ? -1.0 : mDownloadBandwidth
				.getAverage();
	}

	/**
	 * Interface for listening to when
	 * {@link com.facebook.network.connectionclass.ConnectionClassManager}
	 * changes state.
	 */
	public interface ConnectionClassStateChangeListener {
		/**
		 * The method that will be called when
		 * {@link com.facebook.network.connectionclass.ConnectionClassManager}
		 * changes ConnectionClass.
		 * 
		 * @param bandwidthState
		 *            The new ConnectionClass.
		 */
		public void onBandwidthStateChange(ConnectionQuality bandwidthState);
	}

	/**
	 * Method for adding new listeners to this class.
	 * 
	 * @param listener
	 *            {@link ConnectionClassStateChangeListener} to add as a
	 *            listener.
	 */
	public ConnectionQuality register(
			ConnectionClassStateChangeListener listener) {
		if (listener != null) {
			mListenerList.add(listener);
		}
		return mCurrentBandwidthConnectionQuality.get();
	}

	/**
	 * Method for removing listeners from this class.
	 * 
	 * @param listener
	 *            Reference to the {@link ConnectionClassStateChangeListener} to
	 *            be removed.
	 */
	public void remove(ConnectionClassStateChangeListener listener) {
		if (listener != null) {
			mListenerList.remove(listener);
		}
	}

	private void notifyListeners() {
		int size = mListenerList.size();
		for (int i = 0; i < size; i++) {
			mListenerList.get(i).onBandwidthStateChange(
					mCurrentBandwidthConnectionQuality.get());
		}
	}
}