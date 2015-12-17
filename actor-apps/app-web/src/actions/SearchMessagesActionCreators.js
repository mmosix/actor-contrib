/*
 * Copyright (C) 2015 Actor LLC. <https://actor.im>
 */

import { dispatch, dispatchAsync } from 'actor-sdk/build/dispatcher/ActorAppDispatcher';
import { ActionTypes } from '../constants/AppConstants';
import ActorClient from 'actor-sdk/build/utils/ActorClient';

import ActivityActionCreators from 'actor-sdk/build/actions/ActivityActionCreators';

import ActivityStore from 'actor-sdk/build/stores/ActivityStore';
import DialogStore from 'actor-sdk/build/stores/DialogStore';
import SearchMessagesStore from '../stores/SearchMessagesStore';

let _isActivityOpenBeforeSearch = false;

export default {
  open() {
    dispatch(ActionTypes.SEARCH_SHOW);
    _isActivityOpenBeforeSearch = ActivityStore.isOpen();
    if (_isActivityOpenBeforeSearch) {
      ActivityActionCreators.hide()
    }
  },

  close() {
    dispatch(ActionTypes.SEARCH_HIDE);
    if (_isActivityOpenBeforeSearch) {
      ActivityActionCreators.show();
    }
  },

  expand() {
    dispatch(ActionTypes.SEARCH_EXPAND);
  },

  collapse() {
    dispatch(ActionTypes.SEARCH_COLLAPSE);
  },

  findAllText(query) {
    const latestQuery = SearchMessagesStore.getQuery();
    const isSearchOpen = SearchMessagesStore.isOpen();
    const peer = DialogStore.getCurrentPeer();

    if (!isSearchOpen) {
      this.open();
    }

    if (query !== latestQuery) {
      dispatchAsync(ActorClient.findAllText(peer, query), {
        request: ActionTypes.SEARCH_TEXT,
        success: ActionTypes.SEARCH_TEXT_SUCCESS,
        failure: ActionTypes.SEARCH_TEXT_ERROR
      }, {peer, query});
    }
  },

  findAllDocs() {
    const peer = DialogStore.getCurrentPeer();
    dispatchAsync(ActorClient.findAllDocs(peer), {
      request: ActionTypes.SEARCH_DOCS,
      success: ActionTypes.SEARCH_DOCS_SUCCESS,
      failure: ActionTypes.SEARCH_DOCS_ERROR
    }, { peer });
  },

  findAllLinks() {
    const peer = DialogStore.getCurrentPeer();
    dispatchAsync(ActorClient.findAllLinks(peer), {
      request: ActionTypes.SEARCH_LINKS,
      success: ActionTypes.SEARCH_LINKS_SUCCESS,
      failure: ActionTypes.SEARCH_LINKS_ERROR
    }, { peer });
  },

  findAllPhotos() {
    const peer = DialogStore.getCurrentPeer();
    dispatchAsync(ActorClient.findAllPhotos(peer), {
      request: ActionTypes.SEARCH_PHOTO,
      success: ActionTypes.SEARCH_PHOTO_SUCCESS,
      failure: ActionTypes.SEARCH_PHOTO_ERROR
    }, { peer });
  }
};
