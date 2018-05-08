import R from 'ramda'
import React from 'baret'
import Bacon from 'baconjs'
import Atom from 'bacon.atom'
import Text from '../i18n/Text'
import {t} from '../i18n/i18n'
import Input from '../components/Input'

const LoginUrl = '/koski/user/shibbolethlogin'
const RedirectUrl = '/koski/omattiedot'

const HetuLogin = () => {
  const state = Atom({hetu: null, cn: null, FirstName: null, givenName: null, sn: null})

  const valid = state.map(({hetu}) => {
    return hetu && hetu.length === 11
  })
  const inProgress = Atom(false)
  const error = Atom(null)

  const loginTrigger = new Bacon.Bus()

  const doLogin = e => {
    e.preventDefault()
    inProgress.set(true)
    loginTrigger.push()
  }

  const errorHandler = e => {
    console.error('Fake shibboleth login fail', e)
    inProgress.set(false)
    error.set(e)
  }

  const login = loginTrigger
    .map(state)
    .flatMap(credentials => {
      const headers = R.reject(R.isNil, R.merge(credentials, {security: 'mock'}))
      // console.log('Logging in with', headers)
      return Bacon.fromPromise(fetch(LoginUrl, { credentials: 'include', headers}))
    })

  login.onValue((x) => {
    if (x.headers && x.headers.map && x.headers.map['x-virhesivu']) {
      // For PhantomJS - the fetch polyfill doesn't set "x.redirected"
      document.location = '/koski/virhesivu'
    } else if (x.redirected) {
      document.location = x.url
    } else {
      document.location = RedirectUrl
    }
  })

  login.onError(errorHandler)

  const errorMessage = error.map('.jsonMessage.0.message')

  return (
    <form className={error.map(e => e ? 'login error' : 'login')}>
      <img id="logo" src="/koski/images/korhopankki.png"/>
      <label><Text name="Henkilötunnus"/>
        <Input
          id='hetu'
          type='password'
          disabled={inProgress}
          value={state.view('hetu')}
          autofocus={true}
        />
      </label>
      <label><Text name="Sukunimi"/>
        <Input
          id='sn'
          type='text'
          disabled={inProgress}
          value={state.view('sn')}
        />
      </label>
      <label><Text name="Etunimet"/>
        <Input
          id='FirstName'
          type='text'
          disabled={inProgress}
          value={state.view('FirstName')}
        />
      </label>
      <label><Text name="Kutsumanimi"/>
        <Input
          id='givenName'
          type='text'
          disabled={inProgress}
          value={state.view('givenName')}
        />
      </label>
      <button
        className='button blue'
        disabled={valid.not().or(inProgress)}
        onClick={doLogin}>
        {t('Kirjaudu sisään')}
      </button>
      <div className="error-message">{errorMessage}</div>
    </form>
  )
}

export default HetuLogin
