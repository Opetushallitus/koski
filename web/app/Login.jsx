import './polyfills.js'
import React from 'baret'
import ReactDOM from 'react-dom'
import Http from './http'
import Bacon from 'baconjs'
import Atom from 'bacon.atom'
import './style/main.less'
import {TopBar} from './TopBar.jsx'

const Input = ({ id, type, disabled, value }) => <input type={type} disabled={disabled} value={ value.or('') } onChange={ (e) => value.set(e.target.value)} id={id}></input>

const Login = () => {
  const state = Atom({username: '', password: ''})
  const valid = state.map(({username, password}) => username.length > 0 && password.length > 0)
  const inProgress = Atom(false)
  const error = Atom(null)
  const loginE = new Bacon.Bus()
  const loginResultE = loginE
    .map(state)
    .flatMap((credentials) => Http.post('/koski/user/login', credentials, { errorHandler: (e) => {
      inProgress.set(false)
      error.set(e)
    } }))
  loginResultE.onValue(() => document.location='/koski')
  const doLogin = (e) => {
    e.preventDefault()
    inProgress.set(true)
    loginE.push()
  }

  return (
    <form className={error.map(e => e ? 'login error': 'login')}>
      <label>Tunnus
        <Input id='username' type='text' disabled={inProgress} value={state.view('username')}/>
      </label>
      <label>Salasana
        <Input id='password' type='password' disabled={inProgress} value={state.view('password')}/>
      </label>
      <button className='button blue' onClick={doLogin} disabled={valid.not().or(inProgress)}>{inProgress.map(p => p ? 'Kirjaudutaan...' : 'Kirjaudu sisään')}</button>
    </form>
  )
}

ReactDOM.render(
  (<div>
    <TopBar user={null} saved={null} title={''} />
    <Login/>
  </div>),
  document.getElementById('content')
)

document.querySelector('title').innerHTML = 'Login - Koski - Opintopolku.fi'