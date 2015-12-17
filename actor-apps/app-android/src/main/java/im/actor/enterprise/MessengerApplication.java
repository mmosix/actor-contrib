package im.actor.enterprise;

import android.graphics.Color;
import android.support.v4.app.Fragment;

import im.actor.sdk.ActorSDK;
import im.actor.sdk.ActorSDKApplication;
import im.actor.sdk.ActorStyle;
import im.actor.sdk.BaseActorSDKDelegate;
import im.actor.sdk.controllers.fragment.auth.BaseAuthFragment;
import im.actor.sdk.controllers.fragment.auth.SignPhoneFragment;
import im.actor.sdk.intents.ActorIntent;
import im.actor.sdk.intents.ActorIntentFragmentActivity;

public class MessengerApplication extends ActorSDKApplication {
    @Override
    public void onConfigureActorSDK() {
        ActorSDK.sharedActor().setDelegate(new ActorSdkDelegate());
        ActorSDK.sharedActor().setPushId(209133700967L);

        ActorStyle style = ActorSDK.sharedActor().style;
        style.setMainColor(Color.parseColor("#529a88"));
    }

    private class ActorSdkDelegate extends BaseActorSDKDelegate {

        @Override
        public ActorIntent getAuthStartIntent() {
            return new ActorIntentFragmentActivity() {
                @Override
                public Fragment getFragment() {
                    return new SignEmailFragment();
                }
            };
        }

        //Deprecated
        @Override
        public BaseAuthFragment getSignFragment() {
            return new SignEmailFragment();
        }
    }
}
