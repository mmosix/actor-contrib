/*
 * Copyright (C) 2015 Actor LLC. <https://actor.im>
 */

import { dispatch, dispatchAsync } from 'actor-sdk/build/dispatcher/ActorAppDispatcher';
import { ActionTypes } from 'actor-sdk/build/constants/ActorAppConstants';
import ActorClient from 'actor-sdk/build/utils/ActorClient';
import RouterContainer from 'actor-sdk/build/utils/RouterContainer';

import LoginActionCreators from 'actor-sdk/build/actions/LoginActionCreators';
import MyProfileActionCreators from 'actor-sdk/build/actions/MyProfileActionCreators';
import DialogActionCreators from 'actor-sdk/build/actions/DialogActionCreators';
import ContactActionCreators from 'actor-sdk/build/actions/ContactActionCreators';
import QuickSearchActionCreators from 'actor-sdk/build/actions/QuickSearchActionCreators';
import FaviconActionCreators from 'actor-sdk/build/actions/FaviconActionCreators';

const EnterpriseLoginActionCreators = {
  requestEmail(email) {
    LoginActionCreators.changeLogin(email);
    dispatchAsync(ActorClient.requestCodeEmail(email), {
      request: ActionTypes.AUTH_CODE_REQUEST,
      success: ActionTypes.AUTH_CODE_REQUEST_SUCCESS,
      failure: ActionTypes.AUTH_CODE_REQUEST_FAILURE
    }, { email });
  },

  setLoggedIn: (opts = {}) => {
    if (opts.redirect) {
      const router = RouterContainer.get();
      const nextPath = router.getCurrentQuery().nextPath;
      nextPath ? router.replaceWith(nextPath) : router.replaceWith('/');
    }

    dispatch(ActionTypes.AUTH_SET_LOGGED_IN);
    ActorClient.bindUser(ActorClient.getUid(), MyProfileActionCreators.onProfileChanged);
    ActorClient.bindGroupDialogs(DialogActionCreators.setDialogs);
    ActorClient.bindContacts(ContactActionCreators.setContacts);
    ActorClient.bindSearch(QuickSearchActionCreators.setQuickSearchList);
    ActorClient.bindTempGlobalCounter(FaviconActionCreators.setFavicon);
  },

  setLoggedOut: () => {
    dispatch(ActionTypes.AUTH_SET_LOGGED_OUT);
    ActorClient.unbindUser(ActorClient.getUid(), MyProfileActionCreators.onProfileChanged);
    ActorClient.unbindGroupDialogs(DialogActionCreators.setDialogs);
    ActorClient.unbindContacts(ContactActionCreators.setContacts);
    ActorClient.unbindSearch(QuickSearchActionCreators.setQuickSearchList);
    ActorClient.unbindTempGlobalCounter(FaviconActionCreators.setFavicon);
  }
};

export default EnterpriseLoginActionCreators;
