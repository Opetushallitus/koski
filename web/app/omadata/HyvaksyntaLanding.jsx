import '../polyfills/polyfills.js'
import React from 'baret'
import ReactDOM from 'react-dom'
import UusiHyvaksynta from './UusiHyvaksynta'
import ErrorPage from './ErrorPage'
import Spinner from './Spinner'
import Footer from './Footer'
import Header from './Header'
import Http from '../util/http'
import { currentLocation, parseQuery } from '../util/location'
import { Error as ErrorDisplay, logError } from '../util/Error'
import { t } from '../i18n/i18n'

const memberCodeRegex = /\/koski\/omadata\/valtuutus\/(.*)/

class HyvaksyntaLanding extends React.Component {
  constructor(props) {
    super(props)

    this.state = {
      loading: true,
      authorizationGiven: false,
      callback: this.parseCallbackURL(),
      error: undefined
    }

    this.authorizeMember = this.authorizeMember.bind(this)
  }

  componentDidMount() {
    try {
      const pathParam = memberCodeRegex.exec(currentLocation().path)[1]
      Http.cachedGet(`/koski/api/omadata/kumppani/${pathParam}`, {
        errorHandler: (e) => {
          logError(e)
          this.setState({ loading: false })
        }
      }).onValue((member) =>
        this.setState({
          memberName: member.name,
          memberCode: member.id,
          loading: false
        })
      )
    } catch (error) {
      logError(error)
      this.setState({ loading: false })
    }
  }

  authorizeMember() {
    Http.post(
      `/koski/api/omadata/valtuutus/${this.state.memberCode}`,
      {},
      {
        errorHandler: (e) => {
          if (e && e.httpStatus === 401) {
            logError(
              Error(
                `Must be logged in before we can authorize ${this.state.memberCode}`
              )
            )
            this.setState({ error: t('Sinun tulee olla kirjautunut sisään') })
          } else {
            ;[
              e,
              Error(`Failed to add permissions for ${this.state.memberCode}`)
            ].map(logError)
            this.setState({ error: t('Tallennus epäonnistui') })
          }
        }
      }
    ).onValue((response) => {
      if (response.success === true) {
        this.setState({
          authorizationGiven: true
        })
      } else {
        this.setState({ error: t('Tallennus epäonnistui') })
      }
    })
  }

  getLogoutURL() {
    return `/koski/user/logout?target=${
      window.location.origin
    }/koski/user/redirect?target=${encodeURIComponent(this.state.callback)}`
  }

  parseCallbackURL() {
    const callbackURL = parseQuery(currentLocation().queryString).callback

    if (!callbackURL) return '/'
    return callbackURL.includes('://') ? callbackURL : `https://${callbackURL}`
  }

  render() {
    const error = this.state.error ? (
      <ErrorDisplay error={{ text: this.state.error }} />
    ) : null

    return (
      <div>
        <Header />
        {error}

        {this.state.memberName ? (
          <UusiHyvaksynta
            memberName={this.state.memberName}
            logoutURL={this.getLogoutURL()}
            onAuthorization={this.authorizeMember}
            authorizationGiven={this.state.authorizationGiven}
          />
        ) : this.state.loading ? (
          <Spinner />
        ) : (
          <ErrorPage />
        )}

        <Footer />
      </div>
    )
  }
}

ReactDOM.render(
  <div>
    <HyvaksyntaLanding />
  </div>,
  document.getElementById('content')
)
