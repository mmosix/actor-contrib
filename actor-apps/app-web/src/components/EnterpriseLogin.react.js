/*
 * Copyright (C) 2015 Actor LLC. <https://actor.im>
 */

import React, { Component, PropTypes } from 'react';
import { Container } from 'flux/utils';
import classnames from 'classnames';
import ReactMixin from 'react-mixin';
import { IntlMixin } from 'react-intl';
import { Styles, TextField } from 'material-ui';
import mixpanel from 'actor-sdk/build/utils/Mixpanel';

import { AuthSteps } from 'actor-sdk/build/constants/ActorAppConstants';

import LoginActionCreators from 'actor-sdk/build/actions/LoginActionCreators';
import EnterpriseLoginActionCreators from '../actions/EnterpriseLoginActionCreators';

import LoginStore from 'actor-sdk/build/stores/LoginStore';

import ActorTheme from 'actor-sdk/build/constants/ActorTheme';

const ThemeManager = new Styles.ThemeManager();

class EnterpriseLogin extends Component {
  constructor(props) {
    super(props);
  }

  static contextTypes = {
    router: PropTypes.func
  };

  static propTypes = {
    query: PropTypes.object
  };

  static childContextTypes = {
    muiTheme: PropTypes.object
  };

  getChildContext() {
    return {
      muiTheme: ThemeManager.getCurrentTheme()
    }
  };

  static getStores() {
    return [LoginStore];
  }

  static calculateState() {
    return {
      login: LoginStore.getLogin(),
      code: LoginStore.getCode(),
      name: LoginStore.getName(),
      step: LoginStore.getStep(),
      errors: LoginStore.getErrors(),
      isCodeRequested: LoginStore.isCodeRequested(),
      isCodeSended: LoginStore.isCodeSended(),
      isSignupStarted: LoginStore.isSignupStarted()
    }
  };

  componentWillMount() {
    const { query } = this.props;

    if (query.email) {
      this.setState({login: query.email});
      EnterpriseLoginActionCreators.requestEmail(query.email);
    }

    ThemeManager.setTheme(ActorTheme);
  }

  componentDidMount() {
    this.handleFocus();
  }

  componentDidUpdate() {
    this.handleFocus();
  }

  // From change handlers
  onLoginChange = event => {
    event.preventDefault();
    LoginActionCreators.changeLogin(event.target.value);
  };
  onCodeChange = event => {
    event.preventDefault();
    LoginActionCreators.changeCode(event.target.value);
  };
  onNameChange = event => {
    event.preventDefault();
    LoginActionCreators.changeName(event.target.value);
  };

  // Form submit handlers
  onRequestEmail = event => {
    event.preventDefault();
    EnterpriseLoginActionCreators.requestEmail(this.state.login);
  };
  onSendCode = event => {
    event.preventDefault();
    LoginActionCreators.sendCode(this.state.code);
  };
  onSignupRequested = event => {
    event.preventDefault();
    LoginActionCreators.sendSignup(this.state.name);
  };

  handleRestartAuthClick = event => {
    event.preventDefault();
    LoginActionCreators.restartAuth();
  };

  handleFocus = () => {
    const { step } = this.state;

    switch (step) {
      case AuthSteps.LOGIN_WAIT:
        this.refs.login.focus();
        break;
      case AuthSteps.CODE_WAIT:
        this.refs.code.focus();
        break;
      case AuthSteps.NAME_WAIT:
        this.refs.name.focus();
        break;
      default:
    }
  };

  handleNormalUserClick = (event) => {
    event.preventDefault();
    window.open('https://app.actor.im', '_self');
  };

  render() {
    const { step, errors, login, code, name, isCodeRequested, isCodeSended, isSignupStarted } = this.state;

    let requestFormClassName = classnames('login__form', 'login__form--request', {
      'login__form--active': step === AuthSteps.LOGIN_WAIT,
      'login__form--done': step !== AuthSteps.LOGIN_WAIT && isCodeRequested
    });
    let checkFormClassName = classnames('login__form', 'login__form--check', {
      'login__form--active': step === AuthSteps.CODE_WAIT && isCodeRequested,
      'login__form--done': step !== AuthSteps.CODE_WAIT && isCodeSended
    });
    let signupFormClassName = classnames('login__form', 'login__form--signup', {
      'login__form--active': step === AuthSteps.NAME_WAIT
    });

    const spinner = (
      <div className="spinner">
        <div/><div/><div/><div/><div/><div/><div/><div/><div/><div/><div/><div/>
      </div>
    );

    return (
      <section className="login-new row center-xs middle-xs">
        <div className="login-new__welcome col-xs row center-xs middle-xs">
          <img alt="Actor messenger"
               className="logo"
               src="assets/images/logo.png"
               srcSet="assets/images/logo@2x.png 2x"/>

          <article>
            <h1 className="login-new__heading">Welcome to <strong>Actor</strong></h1>
            <p>
              Actor Messenger brings all your business network connections into one place,
              makes it easily accessible wherever you go.
            </p>
            <p>
              Our aim is to make your work easier, reduce your email amount,
              make the business world closer by reducing time to find right contacts.
            </p>
          </article>

          <footer className="text-left">
            <p>Made by Actor LLC</p>
            <p>
              <a href="//twitter.com/actorapp">Our Twitter</a>&nbsp;&nbsp;•&nbsp;&nbsp;
              <a href="tel:+79217971234">+79217971234</a>
            </p>
            <p>Grafskiy pereulok 4&nbsp;&nbsp;•&nbsp;&nbsp;Saint-Petersburg, Russia&nbsp;&nbsp;•&nbsp;&nbsp;191002</p>
          </footer>
        </div>

        <div className="login-new__form col-xs-6 col-md-4 row center-xs middle-xs">
          <div>
            <h1 className="login-new__heading">{this.getIntlMessage('login.signIn')}</h1>

            <form className={requestFormClassName} onSubmit={this.onRequestEmail}>
              <a className="wrong" onClick={this.handleRestartAuthClick}>{this.getIntlMessage('login.wrong')}</a>
              <TextField className="login__form__input"
                         disabled={isCodeRequested || step !== AuthSteps.LOGIN_WAIT}
                         errorText={errors.login}
                         floatingLabelText={this.getIntlMessage('login.email')}
                         onChange={this.onLoginChange}
                         ref="login"
                         type="email"
                         value={login}/>

              <footer className="text-center">
                <button className="button button--rised button--wide"
                        type="submit"
                        disabled={isCodeRequested}>
                  {this.getIntlMessage('button.requestCode')}
                  {isCodeRequested ? spinner : null}
                </button>
              </footer>
            </form>

            <form className={checkFormClassName} onSubmit={this.onSendCode}>
              <TextField className="login__form__input"
                         disabled={isCodeSended || step !== AuthSteps.CODE_WAIT}
                         errorText={errors.code}
                         floatingLabelText={this.getIntlMessage('login.authCode')}
                         onChange={this.onCodeChange}
                         ref="code"
                         type="text"
                         value={code}/>

              <footer className="text-center">
                <button className="button button--rised button--wide"
                        type="submit"
                        disabled={isCodeSended}>
                  {this.getIntlMessage('button.checkCode')}
                  {isCodeSended ? spinner : null}
                </button>
              </footer>
            </form>

            <form className={signupFormClassName} onSubmit={this.onSignupRequested}>
              <TextField className="login__form__input"
                         disabled={isSignupStarted || step === AuthSteps.COMPLETED}
                         errorText={errors.signup}
                         floatingLabelText={this.getIntlMessage('login.yourName')}
                         onChange={this.onNameChange}
                         ref="name"
                         type="text"
                         value={name}/>

              <footer className="text-center">
                <button className="button button--rised button--wide"
                        type="submit"
                        disabled={isSignupStarted}>
                  {this.getIntlMessage('button.signUp')}
                  {isSignupStarted ? spinner : null}
                </button>
              </footer>
            </form>

            <a onClick={this.handleNormalUserClick}>{this.getIntlMessage('login.notEnterprise')}</a>

          </div>
        </div>
      </section>
    );
  }
}

ReactMixin.onClass(EnterpriseLogin, IntlMixin);

export default Container.create(EnterpriseLogin, {pure: false});
