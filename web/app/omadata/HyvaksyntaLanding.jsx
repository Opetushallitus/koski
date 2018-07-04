import React from 'baret'
import ReactDOM from 'react-dom'
import UusiHyvaksynta from './UusiHyvaksynta'
import ErrorPage from './ErrorPage'
import Spinner from './Spinner'
import Footer from './Footer'
import Header from './Header'
import '../polyfills/polyfills.js'
import Http from '../util/http'
import { currentLocation, parseQuery } from '../util/location'
import { userP } from '../util/user'
import { Error as ErrorDisplay, logError } from '../util/Error'
import {t} from '../i18n/i18n'

const memberCodeRegex = /\/koski\/omadata\/(.*)/

class HyvaksyntaLanding extends React.Component {
  constructor(props) {
    super(props)

    this.state = {
      loading: true,
      authorizationGiven: false,
      callback: parseQuery(currentLocation().queryString).callback,
      error: undefined
    }

    this.authorizeMember = this.authorizeMember.bind(this)
    this.onLogoutClicked = this.onLogoutClicked.bind(this)
  }

  componentDidMount() {
    const pathParam = memberCodeRegex.exec(currentLocation().path)[1]
    Http.cachedGet(`/koski/api/omadata/kumppani/${pathParam}`)
      .doError(
        this.setState({ loading: false })
      )
      .onValue(member => this.setState({
        memberName: member.name,
        memberCode: member.id,
        loading: false
      }))
  }

  authorizeMember() {
    Http.post(`/koski/api/omadata/valtuutus/${this.state.memberCode}`, {})
      .doError(e => {
        if (e && e.httpStatus === 401) {
          logError(Error(`Must be logged in before we can authorize ${this.state.memberCode}`))
          this.setState({error: t('Sinun tulee olla kirjautunut sisään')})
        } else {
          [e, Error(`Failed to add permissions for ${this.state.memberCode}`)].map(logError)
          this.setState({error: t('Tallennus epäonnistui')})
        }
      })
      .onValue(response => {
        if (response.success === true) {
          this.setState({
            authorizationGiven: true
          })
        } else {
          this.setState({error: t('Tallennus epäonnistui')})
        }
      })
  }

  onLogoutClicked() {
    window.location.href = this.getLogoutURL()
  }

  getLogoutURL() {
    return `/koski/user/logout?target=${this.state.callback}`
  }

  render() {
    const error = this.state.error ?  <ErrorDisplay error={{text: this.state.error}} /> : null

    return (
      <div>
        <Header userP={userP} onLogoutClicked={this.onLogoutClicked}/>
        {error}

        {
          this.state.memberName ?
            <UusiHyvaksynta
              memberName={this.state.memberName}
              logoutURL={this.getLogoutURL()}
              onAuthorization={this.authorizeMember}
              authorizationGiven={this.state.authorizationGiven}
            /> :
            this.state.loading ? <Spinner /> : <ErrorPage />
        }

        <Footer/>
      </div>
    )
  }
}

ReactDOM.render((
  <div>
    <HyvaksyntaLanding/>
  </div>
), document.getElementById('content'))
