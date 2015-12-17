/*
 * Copyright (C) 2015 Actor LLC. <https://actor.im>
 */

import keymirror from 'keymirror';

export const ActionTypes = keymirror({
  SEARCH_SHOW: null,
  SEARCH_HIDE: null,
  SEARCH_EXPAND: null,
  SEARCH_COLLAPSE: null,
  SEARCH_TEXT: null,
  SEARCH_TEXT_SUCCESS: null,
  SEARCH_TEXT_ERROR: null,
  SEARCH_DOCS: null,
  SEARCH_DOCS_SUCCESS: null,
  SEARCH_DOCS_ERROR: null,
  SEARCH_LINKS: null,
  SEARCH_LINKS_SUCCESS: null,
  SEARCH_LINKS_ERROR: null,
  SEARCH_PHOTO: null,
  SEARCH_PHOTO_SUCCESS: null,
  SEARCH_PHOTO_ERROR: null
});
