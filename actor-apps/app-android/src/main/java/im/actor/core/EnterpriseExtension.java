package im.actor.core;

import java.util.ArrayList;

import im.actor.core.api.ApiEmailActivationType;
import im.actor.core.api.rpc.RequestStartEmailAuth;
import im.actor.core.api.rpc.ResponseStartEmailAuth;
import im.actor.core.modules.ModuleContext;
import im.actor.core.viewmodel.Command;
import im.actor.core.viewmodel.CommandCallback;
import im.actor.runtime.Crypto;

public class EnterpriseExtension {
    private static final String KEY_EMAIL = "auth_email";
    private static final String KEY_TRANSACTION_HASH = "auth_transaction_hash";
    private static final String KEY_DEVICE_HASH = "device_hash";

    private byte[] deviceHash;
    private ApiConfiguration apiConfiguration;

    private ModuleContext context;
    private AuthState state;

    public EnterpriseExtension(Messenger messenger) {

        this.context = messenger.getModuleContext();
        apiConfiguration = context.getConfiguration().getApiConfiguration();
        // Keep device hash always stable across launch
        deviceHash = context.getPreferences().getBytes(KEY_DEVICE_HASH);
        if (deviceHash == null) {
            deviceHash = Crypto.SHA256(context.getConfiguration().getApiConfiguration().getDeviceString().getBytes());
            context.getPreferences().putBytes(KEY_DEVICE_HASH, deviceHash);
        }
    }

    public Command<AuthState> requestStartEmailAuth(final String email) {
        return new Command<AuthState>() {
            @Override
            public void start(final CommandCallback<AuthState> callback) {
                ArrayList<String> langs = new ArrayList<String>();
                for (String s : context.getConfiguration().getPreferredLanguages()) {
                    langs.add(s);
                }
                context.getExternalModule().externalMethod(new RequestStartEmailAuth(email,
                        apiConfiguration.getAppId(),
                        apiConfiguration.getAppKey(),
                        deviceHash,
                        apiConfiguration.getDeviceTitle(),
                        context.getConfiguration().getTimeZone(),
                        langs
                )).start(new CommandCallback<ResponseStartEmailAuth>() {
                    @Override
                    public void onResult(ResponseStartEmailAuth response) {
                        context.getPreferences().putString(KEY_EMAIL, email);
                        context.getPreferences().putString(KEY_TRANSACTION_HASH, response.getTransactionHash());

                        ApiEmailActivationType emailActivationType = response.getActivationType();

                        if (emailActivationType.equals(ApiEmailActivationType.OAUTH2)) {
                            state = AuthState.GET_OAUTH_PARAMS;
                        } else if (emailActivationType.equals(ApiEmailActivationType.CODE)) {
                            state = AuthState.CODE_VALIDATION_EMAIL;
                        }

                        im.actor.runtime.Runtime.postToMainThread(new Runnable() {
                            @Override
                            public void run() {
                                callback.onResult(state);
                            }
                        });
                    }

                    @Override
                    public void onError(final Exception e) {
                        im.actor.runtime.Runtime.postToMainThread(new Runnable() {
                            @Override
                            public void run() {
                                callback.onError(e);
                                e.printStackTrace();
                            }
                        });
                    }
                });
            }

        };
    }
}
