import './polyfills.js'
import React from 'react'
import ReactDOM from 'react-dom'
import Http from './http'
import Bacon from 'baconjs'
import './style/main.less'
import {TopBar} from './TopBar.jsx'

const loginE = new Bacon.Bus()
const loginResultE = loginE
  .flatMap((credentials) => Http.post('/koski/user/login', credentials))

const Login = React.createClass({
  render() {
    const {username, password, inProgress} = this.state

    const usernameIsValid = username && username.length > 0
    const passwordIsValid = password && password.length > 0

    const buttonLabel = inProgress ? 'Kirjaudutaan...' : 'Kirjaudu sisään'
    const buttonDisabled = !usernameIsValid || !passwordIsValid || inProgress

    return (
      <form onInput={this.onInput} className={this.state.error ? 'login error': 'login'}>
        <label>Tunnus
          <input id='username' ref='username' disabled={inProgress}></input>
        </label>
        <label>Salasana
          <input id='password' ref='password' type='password' disabled={inProgress}></input>
        </label>
        <button className='button blue' onClick={this.doLogin} disabled={buttonDisabled}>{buttonLabel}</button>
      </form>
    )
  },

  formState() {
    return { username: this.refs.username.value, password: this.refs.password.value }
  },

  getInitialState() {
    return {username: '', password: ''}
  },

  doLogin(e) {
    e.preventDefault()
    this.setState({inProgress: true})
    loginE.push(this.formState())
  },

  onInput() {
    this.setState(this.formState())
  },

  componentDidMount() {
    loginResultE.onError((e) => {this.setState({error: e, inProgress: false}); this.refs.username.focus()})
    this.refs.username.focus()
  }
})

loginResultE.onValue(() => document.location='/koski')

ReactDOM.render(
  (<div>
    <TopBar user={null} saved={null} title={''} />
    <Login/>
  </div>),
  document.getElementById('content')
)

document.querySelector('title').innerHTML = 'Login - Koski - Opintopolku.fi'