//
//  Copyright (c) 2014-2015 Actor LLC. <https://actor.im>
//

import UIKit
import ActorSDK
import Fabric
import Crashlytics
import Mixpanel

@UIApplicationMain
class AppDelegate: ActorApplicationDelegate, ActorSDKAnalytics {

    override init() {
        super.init()
        
        Fabric.with([Crashlytics.self])
        Mixpanel.sharedInstanceWithToken("4bc3fc1970fca41cfaabf7e58feb0221")
        
        ActorSDK.sharedActor().analyticsDelegate = self
        
        ActorSDK.sharedActor().inviteUrlHost = "quit.email"
        ActorSDK.sharedActor().inviteUrlScheme = "actor"
        
        ActorSDK.sharedActor().supportAccount = "enterprise@actor.im"
        ActorSDK.sharedActor().supportActivationEmail = "activation@actor.im"
        ActorSDK.sharedActor().supportEmail = "support@actor.im"
        
        ActorSDK.sharedActor().apiPushId = 637471
        
        ActorSDK.sharedActor().style.searchStatusBarStyle = .Default
        
        ActorSDK.sharedActor().style.placeholderBgColor = UIColor(rgb: 0x589b87)
        ActorSDK.sharedActor().style.navigationBgColor = UIColor(rgb: 0x3f8a72)
        ActorSDK.sharedActor().style.navigationHairlineHidden = true
        
        ActorSDK.sharedActor().style.vcStatusBarStyle = .LightContent
        ActorSDK.sharedActor().style.vcBackyardColor = UIColor(rgb: 0xebebeb)
        ActorSDK.sharedActor().style.vcTintColor = UIColor(rgb: 0x0774ac)
        
        ActorSDK.sharedActor().style.navigationTintColor = UIColor.whiteColor()
        ActorSDK.sharedActor().style.navigationTitleColor = UIColor.whiteColor()
        ActorSDK.sharedActor().style.navigationSubtitleColor = UIColor.whiteColor().alpha(0.64)
        
        ActorSDK.sharedActor().style.userOfflineNavigationColor = UIColor.whiteColor().alpha(0.64)
        ActorSDK.sharedActor().style.userOnlineNavigationColor = UIColor.whiteColor()
        
        ActorSDK.sharedActor().style.chatBgColor = UIColor(patternImage: UIImage(named: "xv")!)
        
        ActorSDK.sharedActor().autoPushMode = AAAutoPush.AfterLogin
        
        ActorSDK.sharedActor().createActor()
    }
    
    override func application(application: UIApplication, didFinishLaunchingWithOptions launchOptions: [NSObject : AnyObject]?) -> Bool {
        super.application(application, didFinishLaunchingWithOptions: launchOptions)
        ActorSDK.sharedActor().presentMessengerInNewWindow()
        return true
    }
    
    override func actorControllerForAuthStart() -> UIViewController? {
        return AAAuthNavigationController(rootViewController: EmailAuthViewController())
    }

    // Analytics
    
    func analyticsEvent(event: ACEvent) {
        // TODO: Parse parameters
        Answers.logCustomEventWithName(event.getActionId(), customAttributes: nil)
        Mixpanel.sharedInstance().track(event.getActionId())
    }
    
    func analyticsPageVisible(page: ACPage) {
        // TODO: Parse parameters
        Answers.logContentViewWithName(page.getContentTypeDisplay(), contentType: page.getContentType(), contentId: page.getContentId(), customAttributes: nil)
        Mixpanel.sharedInstance().track("$page_shown", properties: ["type_display": page.getContentTypeDisplay(),
            "type": page.getContentType(), "content_id": page.getContentId()])
    }
    
    func analyticsPageHidden(page: ACPage) {
        Mixpanel.sharedInstance().track("$page_hidden", properties: ["type_display": page.getContentTypeDisplay(),
            "type": page.getContentType(), "content_id": page.getContentId()])
    }
}

