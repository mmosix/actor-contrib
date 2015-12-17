/*
 * Copyright (C) 2015 Actor LLC. <https://actor.im>
 */

import React, { Component } from 'react';
import { Container } from 'flux/utils';

import classnames from 'classnames';

import SearchResults from './SearchResults.react.js';

import SearchMessagesStore from '../../stores/SearchMessagesStore';

export default class SearchSection extends Component {
  static getStores = () => [SearchMessagesStore];

  static calculateState() {
    return {
      isOpen: SearchMessagesStore.isOpen(),
      isExpanded: SearchMessagesStore.isExpanded()
    };
  }

  constructor(props) {
    super(props);
  }

  render() {
    const  { isOpen, isExpanded } = this.state;
    const searchClassName = classnames('search', {
      'search--opened': isOpen,
      'search--expanded': isExpanded
    });

    return (
      <section className={searchClassName}>
        {isOpen}
        <SearchResults/>
      </section>
    )
  }
}

export default Container.create(SearchSection, {pure: false});
