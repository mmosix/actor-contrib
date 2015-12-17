/*
 * Copyright (C) 2015 Actor LLC. <https://actor.im>
 */

import assignDeep from 'assign-deep';

import russian from './ru-RU';
import english from './en-US';
import spanish from './es-ES';
import portuguese from './pt-BR';
import chinese from './zh-CN';

// Fallback to default language
const defaultLanguage = english;
russian.messages = assignDeep({}, defaultLanguage.messages, russian.messages);
spanish.messages = assignDeep({}, defaultLanguage.messages, spanish.messages);
portuguese.messages = assignDeep({}, defaultLanguage.messages, portuguese.messages);
chinese.messages = assignDeep({}, defaultLanguage.messages, chinese.messages);

export default {
  default: defaultLanguage,
  russian,
  english,
  spanish,
  portuguese,
  chinese
};
