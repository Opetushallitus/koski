import '../polyfills/polyfills.js'
import React from 'baret'
import ReactDOM from 'react-dom'
import ErrorPage from './ErrorPage'
import Spinner from './Spinner'
import Footer from './Footer'
import Header from './Header'
import Http from '../util/http'
import { currentLocation } from '../util/location'
import { Error as ErrorDisplay, logError } from '../util/Error'
import { tTemplate } from '../i18n/i18n'

import OmaDataOAuth2UusiHyvaksynta from './OmaDataOAuth2UusiHyvaksynta'
import { KoodistoProvider } from '../appstate/koodisto'
__webpack_nonce__ = window.nonce

class OmaDataOAuth2HyvaksyntaLanding extends React.Component {
  constructor(props) {
    super(props)

    this.state = {
      loading: true,
      client_id: this.parseClientId(),
      scope: this.parseScope(),
      error: this.parseError(),
      error_id: this.parseErrorId(),
      clientName: undefined,
      durationInMin: 10 // TODO: TOR-2210: muuta tämä tulemaan bäkkäristä
    }

    this.authorizeClient = this.authorizeClient.bind(this)
    this.declineClient = this.declineClient.bind(this)
  }

  parseClientId() {
    const urlParams = new URLSearchParams(currentLocation().queryString)
    const clientId = urlParams.get('client_id')

    // TODO TOR-2210: Tarkista, että client_id on jokin olemassaoleva? Tämän voinee tehdä tässä jo ennen valtuutusta, vai tehdäänkö bäkkärissä, ja tässä vaan luotetaan?
    return clientId
  }

  parseScope() {
    const urlParams= new URLSearchParams(currentLocation().queryString)
    return urlParams.get('scope')
  }

  parseError() {
    const urlParams= new URLSearchParams(currentLocation().queryString)
    return urlParams.get('error')
  }

  parseErrorId() {
    const urlParams= new URLSearchParams(currentLocation().queryString)
    return urlParams.get('error_id')
  }

  componentDidMount() {
    try {
      Http.cachedGet(
        `/koski/api/omadata-oauth2/resource-owner/client-details/${this.state.client_id}`,
        {
          errorHandler: (e) => {
            logError(e)
            this.setState({ loading: false })
          }
        }
      ).onValue((client) =>
        this.setState({
          clientName: client.name,
          loading: false
        })
      )
    } catch (error) {
      logError(error)
      this.setState({ loading: false })
    }

    // TODO: TOR-2210: tässä voisi hakea myös scope detailsit ja välittää eteenpäin selväkielisinä merkkijonoina rendattavaksi
  }

  authorizeClient() {
    // TODO: TOR-2210 Pitäisikö parametreista tässä filtteröidä pois muut kuin ne, mistä backend on kiinnostunut?
    let params = new URL(document.location.toString()).searchParams

    window.location.href = `/koski/api/omadata-oauth2/resource-owner/authorize?${params.toString()}`
  }

  declineClient() {
    // TODO: TOR-2210 Pitäisikö parametreista tässä filtteröidä pois muut kuin ne, mistä backend on kiinnostunut?
    let params = new URL(document.location.toString()).searchParams
    params.set('error', 'access_denied') // TODO: TOR-2210: tämä on standardinmukainen minimivirhe, pitäisikö lisätä detskuja? https://www.rfc-editor.org/rfc/rfc6749#section-4.1.2.1

    window.location.href = `/koski/api/omadata-oauth2/resource-owner/authorize?${params.toString()}`
  }

  render() {
    const error = this.state.error ? (
      <ErrorDisplay error={{ text: this.state.error }} />
    ) : null

    const errorPage =
      this.state.error ? (
        <ErrorPage
          text={tTemplate('omadataoauth2_error', {
            error: this.state.error,
            error_id: this.state.error_id
          })}
        />
      ) : null

    return (
      <KoodistoProvider>
        <div>
          <Header />
          {error}

          {errorPage ? (
            errorPage
          ) : this.state.clientName ? (
            <OmaDataOAuth2UusiHyvaksynta
              clientId={this.state.client_id}
              clientName={this.state.clientName}
              scope={this.state.scope}
              onAuthorization={this.authorizeClient}
              onDecline={this.declineClient}
              durationInMin={this.state.durationInMin}
            />
          ) : this.state.loading ? (
            <Spinner />
          ) : (
            <ErrorPage />
          )}

          <Footer />
        </div>
      </KoodistoProvider>
    )
  }
}

ReactDOM.render(
  <div>
    <OmaDataOAuth2HyvaksyntaLanding />
  </div>,
  document.getElementById('content')
)
