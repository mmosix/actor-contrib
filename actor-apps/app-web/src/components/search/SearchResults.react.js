/*
 * Copyright (C) 2015 Actor LLC. <https://actor.im>
 */

import { map } from 'lodash';
import React, { Component } from 'react';
import ReactMixin from 'react-mixin';
import { IntlMixin, FormattedMessage, FormattedHTMLMessage } from 'react-intl';

import SearchMessagesActionCreators from '../../actions/SearchMessagesActionCreators';

import SearchMessagesStore from '../../stores/SearchMessagesStore';

import SearchResultItem from './SearchResultItem.react';

const getStateFromStores = () => {
  return {
    isExpanded: SearchMessagesStore.isExpanded(),
    query: SearchMessagesStore.getQuery(),
    allResults: SearchMessagesStore.getAllResults(),
    isSearching: SearchMessagesStore.isSearching()
  };
};

export default class SearchResults extends Component {
  constructor(props) {
    super(props);

    this.state = getStateFromStores();

    SearchMessagesStore.addListener(this.handleChange);
  }

  render() {
    const { isExpanded, allResults, query, isSearching } = this.state;

    const searchResults = map(allResults, (result) => {
      return <SearchResultItem key={result.rid} {...result}/>
    });

    if (searchResults.length === 0 && query !== '') {
      searchResults.push(
        <li className="search__results__item search__results__item--not-found">
          <FormattedHTMLMessage message={this.getIntlMessage('search.notFound')} query={query}/>
        </li>
      );
    }

    return (
      <div>
        <div className="search__expand" onClick={this.handleExpandClick}>
          <i className="material-icons">{isExpanded ? 'chevron_right' : 'chevron_left'}</i>
          <i className="material-icons">{isExpanded ? 'chevron_right' : 'chevron_left'}</i>
        </div>
        <header className="search__header">
          <ul className="search__filter">
            <li className="search__filter__item search__filter__item--active">Text</li>
          </ul>
        </header>
        <div className="search__body">
          <ul className="search__results">
            {
              query === ''
                ? <li className="search__results__item search__results__item--empty">
                    {this.getIntlMessage('search.emptyQuery')}
                  </li>
                : isSearching
                  ? <li className="search__results__item search__results__item--not-found">
                      <FormattedMessage message={this.getIntlMessage('search.searching')} query={query}/>
                    </li>
                  : searchResults
            }
          </ul>
        </div>
      </div>
    );
  }

  handleChange = () => this.setState(getStateFromStores());

  handleExpandClick = () => {
    const { isExpanded } = this.state;

    if (isExpanded) {
      SearchMessagesActionCreators.collapse()
    } else {
      SearchMessagesActionCreators.expand()
    }
  };
}

ReactMixin.onClass(SearchResults, IntlMixin);

export default SearchResults;
