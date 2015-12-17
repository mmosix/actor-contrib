/*
 * Copyright (C) 2015 Actor LLC. <https://actor.im>
 */

import { debounce } from 'lodash';

import React, { Component, PropTypes } from 'react';

import classnames from 'classnames';
import ReactMixin from 'react-mixin';
import { IntlMixin } from 'react-intl';

import { KeyCodes } from 'actor-sdk/build/constants/ActorAppConstants';

import SearchMessagesActionCreators from '../../actions/SearchMessagesActionCreators'

import SearchMessagesStore from '../../stores/SearchMessagesStore'

const getStateFromStores = () => {
  return {
    isOpen: SearchMessagesStore.isOpen()
  };
};

class SearchInput extends Component {
  static propTypes = {
    className: PropTypes.string
  };

  constructor(props) {
    super(props);

    this.state = {
      query: ''
    };

    SearchMessagesStore.addListener(this.handleStoreChange);
  }

  render() {
    const { className } = this.props;
    const { query, isFocused } = this.state;
    const searchClassName = classnames('toolbar__search', className, {
      'toolbar__search--focused': isFocused || query.length > 0
    });

    return (
      <div className={searchClassName}>
        <input className="input input--search"
               placeholder={this.getIntlMessage('search.placeholder')}
               type="search"
               tabIndex="1"
               ref="search"
               onFocus={this.handleSearchFocus}
               onBlur={this.handleSearchBlur}
               onChange={this.handleSearchInputChange}
               value={query}/>
        <i className="search-icon material-icons">search</i>
        {
          query.length === 0
            ? null
            : <i className="close-icon material-icons" onClick={this.handleClearSearch}>close</i>
        }
      </div>
    );
  }

  handleStoreChange = () => {
    const newState = getStateFromStores();
    if (newState.isOpen) {
      document.addEventListener('keydown', this.handleKeyDown, false);
    } else {
      document.removeEventListener('keydown', this.handleKeyDown, false);
    }
    this.setState(newState);
  };

  handleSearchInputChange = (event) => {
    const query = event.target.value;
    this.setState({query});
    this.startSearch(query);
  };

  startSearch = debounce((query) => {
    SearchMessagesActionCreators.findAllText(query);
  }, 300, {trailing: true});

  handleClearSearch = () => {
    this.setState({query: ''});
    SearchMessagesActionCreators.close();
  };

  handleSearchFocus = () => {
    const isFocused = true;

    this.setState({isFocused})
  };

  handleSearchBlur = () => {
    const { query } = this.state;
    const isFocused = false;

    if (query.length === 0) {
      this.handleClearSearch();
    }
    this.setState({isFocused})
  };

  handleKeyDown = (event) => {
    if (event.keyCode === KeyCodes.ESC) {
      event.preventDefault();
      React.findDOMNode(this.refs.search).blur();
      this.handleClearSearch();
    }
  };
}

ReactMixin.onClass(SearchInput, IntlMixin);

export default SearchInput;
