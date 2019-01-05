
#import "RNMoPubRewarded.h"

#if __has_include(<React/RCTUtils.h>)
#import <React/RCTUtils.h>
#else
#import "RCTUtils.h"
#endif

static NSString *const kEventAdLoaded = @"rewardedVideoAdLoaded";
static NSString *const kEventAdFailedToLoad = @"rewardedVideoAdFailedToLoad";
static NSString *const kEventAdOpened = @"rewardedVideoAdOpened";
static NSString *const kEventAdClosed = @"rewardedVideoAdClosed";
static NSString *const kEventRewarded = @"rewardedVideoAdRewarded";
static NSString *const kEventVideoStarted = @"rewardedVideoAdVideoStarted";
static NSString *const kEventVideoCompleted = @"rewardedVideoAdVideoCompleted";

@implementation RNMoPubRewarded
{
    NSString *_adUnitID;
    RCTPromiseResolveBlock _requestAdResolve;
    RCTPromiseRejectBlock _requestAdReject;
    BOOL hasListeners;
}

- (dispatch_queue_t)methodQueue
{
    return dispatch_get_main_queue();
}

+ (BOOL)requiresMainQueueSetup
{
    return NO;
}

RCT_EXPORT_MODULE()

- (NSArray<NSString *> *)supportedEvents
{
    return @[
             kEventRewarded,
             kEventAdLoaded,
             kEventAdFailedToLoad,
             kEventAdOpened,
             kEventVideoStarted,
             kEventAdClosed,
             kEventVideoCompleted ];
}

#pragma mark exported methods

RCT_EXPORT_METHOD(setAdUnitID:(NSString *)adUnitID)
{
    _adUnitID = adUnitID;
}

RCT_EXPORT_METHOD(requestAd:(RCTPromiseResolveBlock)resolve rejecter:(RCTPromiseRejectBlock)reject)
{
    _requestAdResolve = resolve;
    _requestAdReject = reject;
    
    if ([MPRewardedVideo hasAdAvailableForAdUnitID:_adUnitID]) {
        reject(@"E_AD_ALREADY_LOADED", @"Ad is already loaded.", nil);
    } else {
        [MPRewardedVideo setDelegate:self forAdUnitId:_adUnitID];
        [MPRewardedVideo loadRewardedVideoAdWithAdUnitID:_adUnitID withMediationSettings:@[]];
    }
}

RCT_EXPORT_METHOD(showAd:(RCTPromiseResolveBlock)resolve rejecter:(RCTPromiseRejectBlock)reject)
{
    if ([MPRewardedVideo hasAdAvailableForAdUnitID:_adUnitID]) {
        NSArray *rewards = [MPRewardedVideo availableRewardsForAdUnitID:_adUnitID];
        MPRewardedVideoReward *reward = rewards[0];
        
        UIWindow *keyWindow = [[UIApplication sharedApplication] keyWindow];
        UIViewController *rootViewController = [keyWindow rootViewController];
        
        [MPRewardedVideo presentRewardedVideoAdForAdUnitID:_adUnitID fromViewController:rootViewController withReward:reward customData:nil];
        resolve(nil);
    }
    else {
        reject(@"E_AD_NOT_READY", @"Ad is not ready.", nil);
    }
}

RCT_EXPORT_METHOD(isReady:(RCTResponseSenderBlock)callback)
{
    callback(@[[NSNumber numberWithBool:[MPRewardedVideo hasAdAvailableForAdUnitID:_adUnitID]]]);
}

- (void)startObserving
{
    hasListeners = YES;
}

- (void)stopObserving
{
    hasListeners = NO;
}

#pragma mark Delegate

- (void)rewardedVideoAdDidLoadForAdUnitID:(NSString *)adUnitID {
    if (hasListeners) {
        [self sendEventWithName:kEventAdLoaded body:nil];
    }
    _requestAdResolve(nil);
}

- (void)rewardedVideoAdDidFailToLoadForAdUnitID:(NSString *)adUnitID error:(NSError *)error {
    if (hasListeners) {
        NSDictionary *jsError = RCTJSErrorFromCodeMessageAndNSError(@"E_AD_FAILED_TO_LOAD", error.localizedDescription, error);
        [self sendEventWithName:kEventAdFailedToLoad body:jsError];
    }
    
    _requestAdReject(@"E_AD_FAILED_TO_LOAD", error.localizedDescription, error);
}

- (void)rewardedVideoAdWillAppearForAdUnitID:(NSString *)adUnitID {
}

- (void)rewardedVideoAdDidAppearForAdUnitID:(NSString *)adUnitID {
    if (hasListeners) {
        [self sendEventWithName:kEventAdOpened body:nil];
    }
}

- (void)rewardedVideoAdWillDisappearForAdUnitID:(NSString *)adUnitID {
    if (hasListeners) {
        [self sendEventWithName:kEventVideoCompleted body:nil];
    }
}

- (void)rewardedVideoAdDidDisappearForAdUnitID:(NSString *)adUnitID {
    if (hasListeners) {
        [self sendEventWithName:kEventAdClosed body:nil];
    }
}

- (void)rewardedVideoAdDidExpireForAdUnitID:(NSString *)adUnitID {
    
}

- (void)rewardedVideoAdDidReceiveTapEventForAdUnitID:(NSString *)adUnitID {
    
}

- (void)rewardedVideoAdShouldRewardForAdUnitID:(NSString *)adUnitID reward:(MPRewardedVideoReward *)reward {
    [self sendEventWithName:kEventRewarded body:nil];
}

- (void)rewardedVideoAdWillLeaveApplicationForAdUnitID:(NSString *)adUnitID {
    
}

@end

