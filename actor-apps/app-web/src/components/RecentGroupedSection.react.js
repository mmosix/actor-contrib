/*
 * Copyright (C) 2015 Actor LLC. <https://actor.im>
 */

import { forEach, map } from 'lodash';

import React, { Component } from 'react';
import ReactMixin from 'react-mixin';
import { IntlMixin } from 'react-intl';

import CreateGroupActionCreators from 'actor-sdk/build/actions/CreateGroupActionCreators';
import ContactActionCreators from 'actor-sdk/build/actions/ContactActionCreators';
import GroupListActionCreators from 'actor-sdk/build/actions/GroupListActionCreators';
import AddContactActionCreators from 'actor-sdk/build/actions/AddContactActionCreators';

import AllDialogsStore from 'actor-sdk/build/stores/AllDialogsStore';

import RecentSectionItem from './sidebar/RecentSectionItem.react';

const getStateFromStore = () => {
  return {
    dialogs: AllDialogsStore.getAllDialogs()
  };
};

class RecentGroupedSection extends Component {
  constructor(props) {
    super(props);

    this.state = getStateFromStore();

    AllDialogsStore.addListener(this.onChange);
  }

  onChange = () => this.setState(getStateFromStore());

  handleCreateGroup = () => CreateGroupActionCreators.open();

  handleCreatePrivate = () => AddContactActionCreators.open();

  handleGroupListClick = () => GroupListActionCreators.open();

  handlePrivateListClick = () => ContactActionCreators.open();

  render() {
    const { dialogs } = this.state;

    let groupsList = [],
        privateList = [];

    forEach(dialogs, (dialogs) => {
      switch (dialogs.key) {
        case 'groups':
          groupsList = map(dialogs.shorts, (dialog, index) => <RecentSectionItem dialog={dialog} key={index}/>);
          break;
        case 'privates':
          privateList = map(dialogs.shorts, (dialog, index) => <RecentSectionItem dialog={dialog} key={index}/>);
          break;
        default:
      }
    });

    return (
      <section className="sidebar__recent">
        <div className="sidebar__recent__scroll-container">
          <ul className="sidebar__list sidebar__list--groups">
            <li className="sidebar__list__title">
              <a onClick={this.handleGroupListClick}>{this.getIntlMessage('sidebar.groups')}</a>
              <i className="material-icons sidebar__list__title__icon pull-right"
                 onClick={this.handleCreateGroup}>add_circle_outline</i>
            </li>
            {groupsList}
          </ul>
          <ul className="sidebar__list sidebar__list--private">
            <li className="sidebar__list__title">
              <a onClick={this.handlePrivateListClick}>{this.getIntlMessage('sidebar.private')}</a>
              <i className="material-icons sidebar__list__title__icon pull-right"
                 onClick={this.handleCreatePrivate}>add_circle_outline</i>
            </li>
            {privateList}
          </ul>
        </div>
      </section>
    );
  }
}

ReactMixin.onClass(RecentGroupedSection, IntlMixin);

export default RecentGroupedSection;
