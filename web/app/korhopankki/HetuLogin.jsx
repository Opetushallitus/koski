import React from 'baret'
import Bacon from 'baconjs'
import Atom from 'bacon.atom'
import Http from '../util/http'
import Text from '../i18n/Text'
import Input from '../components/Input'

const LoginUrl = '/koski/user/shibbolethlogin'
const RedirectUrl = '/koski/omattiedot'

const HetuLogin = () => {
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
      const headers = {hetu: credentials.id, security: 'mock'}
      const errorHandler = e => {
        console.error('Fake shibboleth login fail')
        console.error(e)
        inProgress.set(false)
        error.set(e)
      }
      console.log('Logging in with', credentials.id)
      return Http.get(LoginUrl, {errorHandler}, headers)
    })
    .onValue(() => {
      console.log('Login ok')
      document.location = RedirectUrl
    })

  const errorMessage = error.map('.jsonMessage.0.message')

  return (
    <form className={error.map(e => e ? 'login error' : 'login')}>
      <img id="logo" src="/koski/images/korhopankki.png"/>
      <label><Text name="Henkilötunnus"/>
        <Input
          id='hetu'
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
      <div className="error-message">{errorMessage}</div>
    </form>
  )
}

export default HetuLogin
