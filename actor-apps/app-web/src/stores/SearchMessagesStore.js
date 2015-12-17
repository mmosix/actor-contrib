/*
 * Copyright (C) 2015 Actor LLC. <https://actor.im>
 */

import { isEqual } from 'lodash';
import { Store } from 'flux/utils';
import Dispatcher from 'actor-sdk/build/dispatcher/ActorAppDispatcher';
import { ActionTypes } from '../constants/AppConstants';

let isOpen = false,
    isExpanded = false,
    allResults = [],
    query = '',
    isSearching = false;

class SearchMessagesStore extends Store {
  constructor(dispatcher) {
    super(dispatcher);
  }

  isOpen() {
    return isOpen;
  }

  isSearching() {
    return isSearching;
  }

  isExpanded() {
    return isExpanded;
  }

  getQuery() {
    return query;
  }

  getAllResults() {
    return allResults;
  }

  setAllResults(results) {
    if (!isEqual(allResults, results)) {
      allResults = results;
    }
  }

  __onDispatch = (action) => {
    switch (action.type) {
      case ActionTypes.SEARCH_SHOW:
        isOpen = true;
        isExpanded = false;
        this.__emitChange();
        break;
      case ActionTypes.SEARCH_HIDE:
        query = '';
        isOpen = false;
        this.__emitChange();
        break;
      case ActionTypes.SEARCH_EXPAND:
        isExpanded = true;
        this.__emitChange();
        break;
      case ActionTypes.SEARCH_COLLAPSE:
        isExpanded = false;
        this.__emitChange();
        break;

      case ActionTypes.SEARCH_TEXT:
        query = action.query;
        isSearching = true;
        this.__emitChange();
        break;
      case ActionTypes.SEARCH_TEXT_SUCCESS:
        if (action.query === '') {
          this.setAllResults([]);
        } else {
          this.setAllResults(action.response);
        }
        isSearching = false;
        this.__emitChange();
        break;
      case ActionTypes.SEARCH_TEXT_ERROR:
        this.__emitChange();
        break;
      default:
    }
  }
}

export default new SearchMessagesStore(Dispatcher);
