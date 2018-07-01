import React from 'baret'
import ReactDOM from 'react-dom'
import UusiHyvaksynta from './UusiHyvaksynta'
import Footer from './Footer'
import Header from './Header'
import '../polyfills/polyfills.js'
import Http from '../util/http'
import { currentLocation, parseQuery } from '../util/location'
import { userP } from '../util/user'
import { Error } from '../util/Error'
import {t} from '../i18n/i18n'

const memberCodeRegex = /\/koski\/omadata\/(.*)/

class HyvaksyntaLanding extends React.Component {
  constructor(props) {
    super(props)
    this.state = {
      authorizationGiven: false,
      memberCode: memberCodeRegex.exec(currentLocation().path)[1],
      callback: parseQuery(currentLocation().queryString).callback,
      error: undefined
    }

    this.authorizeMember = this.authorizeMember.bind(this)
    this.onLogoutClicked = this.onLogoutClicked.bind(this)

    console.log(`Member code: ${this.state.memberCode}`)
  }

  authorizeMember() {
    Http.post(`/koski/api/omadata/valtuutus/${this.state.memberCode}`, {})
      .doError((e) => {
        if (e && e.httpStatus === 401) {
          console.log(`Must be logged in before we can authorize ${this.state.memberCode}`)
          this.setState({error: t('Sinun tulee olla kirjautunut sisään')})
        } else {
          console.log(`Failed to add permissions for ${this.state.memberCode}`, e)
          this.setState({error: t('Tallennus epäonnistui')})
        }
      })
      .onValue((response) => {
        if (response.success === true) {
          console.log(`Permissions added for ${this.state.memberCode}`)
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
    const error = this.state.error ?  <Error error={{text: this.state.error}} /> : null

    return (
      <div>
        <Header userP={userP} onLogoutClicked={this.onLogoutClicked}/>
        {error}

        <UusiHyvaksynta
          memberCode={this.state.memberCode}
          logoutURL={this.getLogoutURL()}
          onAuthorization={this.authorizeMember}
          authorizationGiven={this.state.authorizationGiven}
        />

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
