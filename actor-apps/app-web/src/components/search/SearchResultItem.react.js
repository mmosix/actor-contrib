/*
 * Copyright (C) 2015 Actor LLC. <https://actor.im>
 */

import React, { Component, PropTypes } from 'react';

import { MessageContentTypes } from 'actor-sdk/build/constants/ActorAppConstants';

import AvatarItem from 'actor-sdk/build/components/common/AvatarItem.react';
import Text from 'actor-sdk/build/components/dialog/messages/Text.react';

class SearchResultItem extends Component {
  static propTypes = {
    content: PropTypes.object.isRequired,
    date: PropTypes.string.isRequired,
    rid: PropTypes.number.isRequired,
    sender: PropTypes.object.isRequired
  };

  render() {
    const { content, date, sender} = this.props;

    let resultContent;
    switch (content.content) {
      case MessageContentTypes.TEXT:
        resultContent = <Text content={content} className="text"/>;
        break;
      default:
    }

    return (
      <li className="search__results__item search__results__item--message row">
        <AvatarItem image={sender.avatar}
                    placeholder={sender.placeholder}
                    size="small"
                    title={sender.title}/>

        <div className="search__results__item__body col-xs">
          <header>
            <time className="time pull-right">{date}</time>
            <h4 className="title">{sender.title}</h4>
          </header>
          <div className="content">
            {resultContent}
          </div>
        </div>
      </li>
    );
  }
}

export default SearchResultItem;
