package im.actor.enterprise;


import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;

import im.actor.core.AuthState;
import im.actor.core.network.RpcException;
import im.actor.core.network.RpcInternalException;
import im.actor.core.network.RpcTimeoutException;
import im.actor.core.viewmodel.Command;
import im.actor.core.viewmodel.CommandCallback;
import im.actor.sdk.controllers.fragment.auth.BaseAuthFragment;

public class AuthExecutor {

    private ProgressDialog progressDialog;
    private AlertDialog alertDialog;
    ResultCallback callback;
    BaseAuthFragment fragment;

    public AuthExecutor(BaseAuthFragment fragment) {
        this.fragment = fragment;
    }

    public void executeAuth(final Command<AuthState> command, final BaseAuthFragment fragment, final ResultCallback callback) {
        this.callback = callback;
        dismissProgress();
        progressDialog = new ProgressDialog(fragment.getActivity());
        progressDialog.setCanceledOnTouchOutside(false);
        progressDialog.setCancelable(false);
        progressDialog.setTitle("Loading...");
        progressDialog.show();
        command.start(new CommandCallback<AuthState>() {
            @Override
            public void onResult(final AuthState res) {
                dismissProgress();
                callback.onResult(res);
            }

            @Override
            public void onError(final Exception e) {
                dismissProgress();
                boolean canTryAgain = false;
                String message = fragment.getString(im.actor.sdk.R.string.error_unknown);
                String tag = "UNKNOWN";
                if (e instanceof RpcException) {
                    RpcException re = (RpcException) e;
                    tag = re.getTag();
                    if (re instanceof RpcInternalException) {
                        message = fragment.getString(im.actor.sdk.R.string.error_unknown);
                        canTryAgain = true;
                    } else if (re instanceof RpcTimeoutException) {
                        message = fragment.getString(im.actor.sdk.R.string.error_connection);
                        canTryAgain = true;
                    } else {
                        if ("PHONE_CODE_EXPIRED".equals(re.getTag())) {
                            message = fragment.getString(im.actor.sdk.R.string.auth_error_code_expired);
                            canTryAgain = false;
                        } else if ("PHONE_CODE_INVALID".equals(re.getTag())) {
                            message = fragment.getString(im.actor.sdk.R.string.auth_error_code_invalid);
                            canTryAgain = false;
                        } else if ("FAILED_GET_OAUTH2_TOKEN".equals(re.getTag())) {
                            message = fragment.getString(im.actor.sdk.R.string.auth_error_failed_get_oauth2_token);
                            canTryAgain = false;
                        } else {
                            message = re.getMessage();
                            canTryAgain = re.isCanTryAgain();
                        }
                    }
                }


                try {
                    if (canTryAgain) {
                        new AlertDialog.Builder(fragment.getActivity())
                                .setMessage(message)
                                .setPositiveButton(im.actor.sdk.R.string.dialog_try_again, new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        dismissAlert();
                                        executeAuth(command, fragment, callback);
                                    }
                                })
                                .setNegativeButton(im.actor.sdk.R.string.dialog_cancel, new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        dismissAlert();
                                    }
                                }).setCancelable(false)
                                .show()
                                .setCanceledOnTouchOutside(false);
                    } else {
                        new AlertDialog.Builder(fragment.getActivity())
                                .setMessage(message)
                                .setPositiveButton(im.actor.sdk.R.string.dialog_ok, new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        dismissAlert();
                                    }
                                })
                                .setCancelable(false)
                                .show()
                                .setCanceledOnTouchOutside(false);
                    }
                } catch (Exception ex) {
                    ex.printStackTrace();
                }

            }
        });
    }

    private void dismissProgress() {
        if (progressDialog != null) {
            progressDialog.dismiss();
            progressDialog = null;
        }
    }

    private void dismissAlert() {
        if (alertDialog != null) {
            alertDialog.dismiss();
            alertDialog = null;
        }
    }

    public interface ResultCallback {
        void onResult(AuthState result);
    }

}
