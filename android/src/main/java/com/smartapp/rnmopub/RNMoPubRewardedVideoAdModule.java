package com.smartapp.rnmopub;

import android.os.Handler;
import android.os.Looper;
import android.support.annotation.Nullable;

import com.facebook.react.bridge.Arguments;
import com.facebook.react.bridge.Callback;
import com.facebook.react.bridge.Promise;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;
import com.facebook.react.bridge.ReadableArray;
import com.facebook.react.bridge.ReadableNativeArray;
import com.facebook.react.bridge.WritableMap;
import com.facebook.react.modules.core.DeviceEventManagerModule;

import com.mopub.common.MoPub;
import com.mopub.common.MoPubReward;
import com.mopub.common.SdkConfiguration;
import com.mopub.mobileads.MoPubErrorCode;
import com.mopub.mobileads.MoPubRewardedVideoListener;
import com.mopub.mobileads.MoPubRewardedVideos;

import java.util.Map;

public class RNMoPubRewardedVideoAdModule extends ReactContextBaseJavaModule implements MoPubRewardedVideoListener {

    public static final String REACT_CLASS = "RNMoPubRewarded";

    public static final String EVENT_AD_LOADED = "rewardedVideoAdLoaded";
    public static final String EVENT_AD_FAILED_TO_LOAD = "rewardedVideoAdFailedToLoad";
    public static final String EVENT_AD_OPENED = "rewardedVideoAdOpened";
    public static final String EVENT_AD_CLOSED = "rewardedVideoAdClosed";
    public static final String EVENT_AD_LEFT_APPLICATION = "rewardedVideoAdLeftApplication";
    public static final String EVENT_REWARDED = "rewardedVideoAdRewarded";
    public static final String EVENT_VIDEO_STARTED = "rewardedVideoAdVideoStarted";
    public static final String EVENT_VIDEO_COMPLETED = "rewardedVideoAdVideoCompleted";

    private String adUnitID;
    private Promise mRequestAdPromise;

    @Override
    public String getName() {
        return REACT_CLASS;
    }

    public RNMoPubRewardedVideoAdModule(ReactApplicationContext reactContext) {
        super(reactContext);
    }
 
    private void sendEvent(String eventName, @Nullable WritableMap params) {
        getReactApplicationContext().getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter.class).emit(eventName, params);
    }

    @ReactMethod
    public void setAdUnitID(String adUnitID) {
      this.adUnitID = adUnitID;
    }

    @ReactMethod
    public void requestAd(final Promise promise) {
        new Handler(Looper.getMainLooper()).post(new Runnable() {
            @Override
            public void run() {
                if (MoPubRewardedVideos.hasRewardedVideo(adUnitID)) {
                  promise.reject("E_AD_ALREADY_LOADED", "Ad is already loaded.");
                } else {
                  mRequestAdPromise = promise;
                  MoPubRewardedVideos.setRewardedVideoListener(RNMoPubRewardedVideoAdModule.this);
                  MoPubRewardedVideos.loadRewardedVideo(adUnitID);
                }
            }
        });
    }

    @ReactMethod
    public void showAd(final Promise promise) {
        new Handler(Looper.getMainLooper()).post(new Runnable() {
            @Override
            public void run() {
                if (MoPubRewardedVideos.hasRewardedVideo(adUnitID)) {
                    MoPubRewardedVideos.showRewardedVideo(adUnitID);
                    promise.resolve(null);
                } else {
                    promise.reject("E_AD_NOT_READY", "Ad is not ready.");
                }
            }
        });
    }

    @ReactMethod
    public void isReady(final Callback callback) {
        new Handler(Looper.getMainLooper()).post(new Runnable() {
            @Override
            public void run() {
              callback.invoke(MoPubRewardedVideos.hasRewardedVideo(adUnitID));
            }
        });
    }

    // MoPubRewardedVideoListener implementation
    @Override
    public void onRewardedVideoLoadSuccess(@NonNull final String adUnitId) {
        if (adUnitId.equals(adUnitID)) {
          sendEvent(EVENT_AD_LOADED, null);
          mRequestAdPromise.resolve(null);
        }
    }

    @Override
    public void onRewardedVideoLoadFailure(@NonNull final String adUnitId, @NonNull final MoPubErrorCode errorCode) {
        if (adUnitId.equals(adUnitID)) {
          String errorString = errorCode.name();
          String errorMessage = errorCode.toString();

          WritableMap event = Arguments.createMap();
          WritableMap error = Arguments.createMap();
          event.putString("message", errorMessage);
          sendEvent(EVENT_AD_FAILED_TO_LOAD, event);
          mRequestAdPromise.reject(errorString, errorMessage);
        }
    }

    @Override
    public void onRewardedVideoStarted(@NonNull final String adUnitId) {
        if (adUnitId.equals(adUnitID)) {
          sendEvent(EVENT_VIDEO_STARTED, null);
        }
    }

    @Override
    public void onRewardedVideoPlaybackError(@NonNull final String adUnitId, @NonNull final MoPubErrorCode errorCode) {
        if (adUnitId.equals(adUnitID)) {

        }
    }

    @Override
    public void onRewardedVideoClicked(@NonNull final String adUnitId) {
    }

    @Override
    public void onRewardedVideoClosed(@NonNull final String adUnitId) {
        if (adUnitId.equals(adUnitID)) {
          sendEvent(EVENT_VIDEO_COMPLETED, null);
          sendEvent(EVENT_AD_CLOSED, null);
        }
    }

    @Override
    public void onRewardedVideoCompleted(@NonNull final Set<String> adUnitIds,
            @NonNull final MoPubReward reward) {
        if (adUnitIds.contains(adUnitID)) {
          WritableMap reward = Arguments.createMap();
          sendEvent(EVENT_REWARDED, reward);
        }
    }
}
