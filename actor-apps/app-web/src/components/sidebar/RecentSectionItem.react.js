/*
 * Copyright (C) 2015 Actor LLC. <https://actor.im>
 */

import React, { Component, PropTypes } from 'react';
import classNames from 'classnames';

import PeerUtils from 'actor-sdk/build/utils/PeerUtils';
import { escapeWithEmoji } from 'actor-sdk/build/utils/EmojiUtils';

import DialogActionCreators from 'actor-sdk/build/actions/DialogActionCreators';

import DialogStore from 'actor-sdk/build/stores/DialogStore';

import AvatarItem from 'actor-sdk/build/components/common/AvatarItem.react';

class RecentSectionItem extends Component {
  constructor(props) {
    super(props);
  }

  static propTypes = {
    dialog: PropTypes.object.isRequired
  };

  onClick = () => DialogActionCreators.selectDialogPeer(this.props.dialog.peer.peer);

  handleHideChat = (event) => {
    event.stopPropagation();
    event.preventDefault();
    const { dialog } = this.props;
    DialogActionCreators.hideChat(dialog.peer.peer);
  };

  render() {
    const { dialog } = this.props;
    const selectedPeer = DialogStore.getCurrentPeer();

    const isActive = selectedPeer && PeerUtils.equals(dialog.peer.peer, selectedPeer);

    const recentClassName = classNames('sidebar__list__item', 'row', {
      'sidebar__list__item--active': isActive,
      'sidebar__list__item--unread': dialog.counter > 0
    });
    const counter = dialog.counter > 0 ? <span className="counter">{dialog.counter}</span> : null;

    return (
      <li className={recentClassName} onClick={this.onClick}>
        <AvatarItem image={dialog.peer.avatar}
                    placeholder={dialog.peer.placeholder}
                    size="tiny"
                    title={dialog.peer.title}/>
        <div className="title col-xs" dangerouslySetInnerHTML={{__html: escapeWithEmoji(dialog.peer.title)}}/>
        {counter}
        <i className="material-icons delete" onClick={this.handleHideChat}>clear</i>
      </li>
    );
  }
}

export default RecentSectionItem;
