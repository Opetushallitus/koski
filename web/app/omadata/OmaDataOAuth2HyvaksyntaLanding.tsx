import '../polyfills/polyfills.js'
import Cookie from 'js-cookie'
import React, { useState } from 'react'
import ReactDOM from 'react-dom'
import {
  mapError,
  mapInitial,
  mapLoading,
  mapSuccess,
  useApiWithParams
} from '../api-fetch'
import { KoodistoProvider } from '../appstate/koodisto'
import { Trans } from '../components-v2/texts/Trans'
import { lang, Language, tTemplate } from '../i18n/i18n'
import { fetchOmaDataOAuth2ClientDetails } from '../util/koskiApi'
import { loadStyles } from '../util/loadStyles'
import ErrorPage from './ErrorPage'
import Footer from './Footer'
import OmaDataOAuth2UusiHyvaksynta from './OmaDataOAuth2UusiHyvaksynta'
import Spinner from './Spinner'

// @ts-ignore
__webpack_nonce__ = window.nonce
// @ts-ignore
loadStyles(() => import(/* webpackChunkName: "styles" */ '../style/main.less'))

const urlParams = new URLSearchParams(window.location.search)
const clientId = urlParams.get('client_id')
const scope = urlParams.get('scope') ?? ''
const error = urlParams.get('error')
const errorId = urlParams.get('error_id')

const OmaDataOAuth2HyvaksyntaLanding = () => {
  // Virheilmoitusta näytettäessä palveluntarjoajan tietoja ei tarvita
  const clientDetails = useApiWithParams(
    fetchOmaDataOAuth2ClientDetails,
    error || !clientId ? undefined : [clientId]
  )

  return (
    <KoodistoProvider>
      <div>
        <Header />
        {error ? (
          <>
            <ErrorBanner text={error} />
            <ErrorPage
              text={tTemplate('omadataoauth2_error', {
                error,
                error_id: errorId
              })}
            />
          </>
        ) : !clientId ? (
          <ErrorPage />
        ) : (
          <>
            {mapInitial(clientDetails, () => (
              <Spinner />
            ))}
            {mapLoading(clientDetails, () => (
              <Spinner />
            ))}
            {mapError(clientDetails, () => (
              <ErrorPage />
            ))}
            {mapSuccess(clientDetails, (client) => (
              <OmaDataOAuth2UusiHyvaksynta
                clientId={clientId}
                clientName={client.name}
                scope={scope}
                onAuthorization={authorizeClient}
                onDecline={declineClient}
                durationInMin={client.tokenDurationMinutes}
              />
            ))}
          </>
        )}

        <Footer />
      </div>
    </KoodistoProvider>
  )
}

const authorizeClient = () => {
  const params = new URL(document.location.toString()).searchParams

  window.location.href = `/koski/api/omadata-oauth2/resource-owner/authorize?${params.toString()}`
}

const declineClient = () => {
  const params = new URL(document.location.toString()).searchParams
  params.set('error', 'access_denied')

  window.location.href = `/koski/api/omadata-oauth2/resource-owner/authorize?${params.toString()}`
}

const ErrorBanner = ({ text }: { text: string }) => {
  const [visible, setVisible] = useState(true)

  return (
    <div id="error" className={visible ? 'error' : undefined}>
      {visible && (
        <span>
          <a onClick={() => setVisible(false)}>{'✕'}</a>
          <span className="error-text" data-testid="error">
            {text}
          </span>
        </span>
      )}
    </div>
  )
}

const Header = () => {
  return (
    <div className="header">
      <div className="title">
        <img src="/koski/images/opintopolku_logo.svg" alt="" />
        <h1>
          <Trans>{'Oma Opintopolku'}</Trans>
        </h1>
      </div>

      <div className="lang">
        <ChangeLang />
      </div>
    </div>
  )
}

const ChangeLang = () => (
  <div className="change-lang">
    {lang !== 'fi' ? (
      <button
        id={'change-lang-fi'}
        onClick={() => setLang('fi')}
        title={'Suomeksi'}
      >
        {'Suomi'}
      </button>
    ) : null}

    {lang !== 'sv' ? (
      <button
        id={'change-lang-sv'}
        onClick={() => setLang('sv')}
        title={'På svenska'}
      >
        {'Svenska'}
      </button>
    ) : null}

    {lang !== 'en' ? (
      <button
        id={'change-lang-en'}
        onClick={() => setLang('en')}
        title={'In English'}
      >
        {'English'}
      </button>
    ) : null}
  </div>
)

// Poistaa locale-parametrin, jottei backend ylikirjoita valittua kieltä sen perusteella
const setLang = (newLang: Language) => {
  Cookie.set('lang', newLang)

  const url = new URL(location.href)
  url.searchParams.delete('locale')

  window.location.href = url.href
}

ReactDOM.render(
  <div>
    <OmaDataOAuth2HyvaksyntaLanding />
  </div>,
  document.getElementById('content')
)
