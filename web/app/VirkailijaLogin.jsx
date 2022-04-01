import './polyfills/polyfills.js'
import React from 'baret'
import ReactDOM from 'react-dom'
import Http from './util/http'
import Bacon from 'baconjs'
import Atom from 'bacon.atom'
import './style/main.less'
import { TopBar } from './topbar/TopBar'
import { t } from './i18n/i18n.js'
import Text from './i18n/Text'
import Input from './components/Input'

const Login = () => {
  const state = Atom({ username: '', password: '' })
  const valid = state.map(
    ({ username, password }) => username.length > 0 && password.length > 0
  )
  const inProgress = Atom(false)
  const error = Atom(null)
  const loginE = new Bacon.Bus()
  const loginResultE = loginE.map(state).flatMap((credentials) =>
    Http.post('/koski/user/login', credentials, {
      errorHandler: (e) => {
        inProgress.set(false)
        error.set(e)
      }
    })
  )
  loginResultE.onValue(() => (document.location = '/koski/virkailija'))
  const doLogin = (e) => {
    e.preventDefault()
    inProgress.set(true)
    loginE.push()
  }

  return (
    <form className={error.map((e) => (e ? 'login error' : 'login'))}>
      <label>
        <Text name="Tunnus" />
        <Input
          id="username"
          type="text"
          disabled={inProgress}
          value={state.view('username')}
          autofocus={true}
        />
      </label>
      <label>
        <Text name="Salasana" />
        <Input
          id="password"
          type="password"
          disabled={inProgress}
          value={state.view('password')}
        />
      </label>
      <button
        className="koski-button blue"
        onClick={doLogin}
        disabled={valid.not().or(inProgress)}
      >
        {inProgress.map((p) => (
          <Text name={p ? 'Kirjaudutaan...' : 'Kirjaudu sisään'} />
        ))}
      </button>
    </form>
  )
}

Login.displayName = 'Login'

function VirkailijaLogin() {
  return (
    <div>
      <TopBar user={null} />
      <Login />
    </div>
  )
}

ReactDOM.render(<VirkailijaLogin />, document.getElementById('content'))

document.querySelector('title').innerHTML = t('Login - Koski - Opintopolku.fi')
