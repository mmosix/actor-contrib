/*
 * Copyright (C) 2015 Actor LLC. <https://actor.im>
 */

import { ActorSDK, ActorSDKDelegate } from 'actor-sdk';

import Login from './components/EnterpriseLogin.react';
import RecentSection from './components/RecentGroupedSection.react';
import ToolbarSection from './components/ToolbarSection.react';
import SearchSection from './components/search/SearchSection.react';
import ActivitySection from 'actor-sdk/build/components/ActivitySection.react';

import l18n from './l18n'

import EnterpriseLoginActionCreators from './actions/EnterpriseLoginActionCreators';

const endpoints = [
  'wss://front1-ws-mtproto-api-rev2.actor.im',
  'wss://front2-ws-mtproto-api-rev2.actor.im'
];
const mixpanelAPIKey = '9591b090b987c2b701db5a8ef3e5055c';
const bugsnagApiKey = 'cd24ee53326e06669a36c637b29660c3';

const components = {
  login: Login,
  sidebar: {
    recent: RecentSection
  },
  dialog: {
    toolbar: ToolbarSection,
    activity: [SearchSection, ActivitySection]
  }
};

const actions = {
  setLoggedIn: EnterpriseLoginActionCreators.setLoggedIn,
  setLoggedOut: EnterpriseLoginActionCreators.setLoggedOut
};

const delegate = new ActorSDKDelegate(components, actions, l18n);

const app = new ActorSDK({endpoints, delegate, bugsnagApiKey, mixpanelAPIKey});
app.startApp();
