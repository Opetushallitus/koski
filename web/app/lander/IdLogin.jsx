import React from 'baret'
import Bacon from 'baconjs'
import Atom from 'bacon.atom'
import Http from '../http'
import Text from '../Text.jsx'

const LoginUrl = '/koski/user/shibbolethlogin'
const RedirectUrl = '/koski/omattiedot'

const Input = ({id, type, disabled, value, autofocus = false}) => (
  <input
    type={type}
    disabled={disabled}
    value={value.or('')}
    onChange={e => value.set(e.target.value)}
    id={id}
    autoFocus={autofocus}>
  </input>
)

const IdLogin = () => {
  const state = Atom({id: ''})

  const valid = state.map(({id}) => id.length === 11)
  const inProgress = Atom(false)
  const error = Atom(null)

  const loginTrigger = new Bacon.Bus()

  const doLogin = e => {
    e.preventDefault()
    inProgress.set(true)
    loginTrigger.push()
  }

  loginTrigger
    .map(state)
    .flatMap(credentials => {
      const headers = {nationalidentificationnumber: credentials.id}
      const errorHandler = e => {
        inProgress.set(false)
        error.set(e)
      }

      return Http.get(LoginUrl, {errorHandler}, headers)
    })
    .onValue(() => document.location = RedirectUrl)

  return (
    <form className={error.map(e => e ? 'login error' : 'login')}>
      <label><Text name="Henkilötunnus"/>
        <Input
          id='password'
          type='password'
          disabled={inProgress}
          value={state.view('id')}
          autofocus={true}
        />
      </label>
      <button
        className='button blue'
        disabled={valid.not().or(inProgress)}
        onClick={doLogin}>
        {'Kirjaudu sisään'}
      </button>
    </form>
  )
}

export {IdLogin}