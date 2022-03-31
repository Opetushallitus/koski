import * as R from 'ramda'
import React from 'baret'
import Bacon from 'baconjs'
import Atom from 'bacon.atom'
import Text from '../i18n/Text'
import {t} from '../i18n/i18n'
import Input from '../components/Input'
import Cookie from 'js-cookie'

const HetuLogin = ( { loginUrl = '/koski/cas/oppija', redirectUrl = null } ) => {
  const state = Atom({hetu: null, cn: null, FirstName: null, givenName: null, sn: null, lang: null})

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
    console.error('Fake cas login fail', e)
    inProgress.set(false)
    error.set(e)
  }

  const login = loginTrigger
    .map(state)
    .flatMap(credentials => {
      Cookie.set('_shibsession_', 'mock')
      const headers = R.reject(R.isNil, R.mergeRight(credentials, {security: 'mock'}))
      // console.log('Logging in with', headers)
      const lang = credentials.lang ? credentials.lang : 'fi'
      return Bacon.fromPromise(fetch(loginUrl + window.location.search, { credentials: 'include', headers})).map(resp => ({resp: resp, lang: lang}))
    })

  login.onValue((x) => {
    Cookie.set('lang', x.lang)
    if (x.resp.headers && x.resp.headers.map && x.resp.headers.map['x-virhesivu']) {
      // For PhantomJS - the fetch polyfill doesn't set "x.redirected"
      document.location = '/koski/virhesivu'
    } else if (redirectUrl) {
      document.location = redirectUrl
    } else if (x.resp.redirected) {
      document.location = x.resp.url
    } else {
      document.location = '/koski/omattiedot'
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
          type='text'
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
      <label><Text name="Kieli"/>
        <Input
          id='lang'
          type='text'
          disabled={inProgress}
          value={state.view('lang')}
        />
      </label>
      <button
        className='koski-button blue'
        disabled={valid.not().or(inProgress)}
        onClick={doLogin}>
        {t('Kirjaudu sisään')}
      </button>
      <div className="error-message">{errorMessage}</div>
    </form>
  )
}

HetuLogin.displayName = 'HetuLogin'

export default HetuLogin
