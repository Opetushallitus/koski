import React from 'baret'
import ReactDOM from 'react-dom'
import AnnaHyvaksynta from './AnnaHyvaksynta'
import HyvaksyntaAnnettu from './HyvaksyntaAnnettu'
import Footer from './Footer'
import Header from './Header'
import { formatFinnishDate, parseISODate } from '../date/date.js'
import Text from '../i18n/Text'
import '../polyfills/polyfills.js'
import Http from '../util/http'
import { currentLocation, parseQuery } from '../util/location'
import { userP } from '../util/user'
import { Error } from '../util/Error'
import {t} from '../i18n/i18n'


const memberP = memberId => Http.cachedGet(`/koski/api/omadata/kumppani/${memberId}`, { errorMapper: () => undefined }).toProperty()
const editorP = Http.cachedGet('/koski/api/omattiedot/editor', { errorMapper: () => undefined }).toProperty()

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
    this.getLogoutURL = this.getLogoutURL.bind(this)
  }


  getBirthDate(editorResponse) {
    if (!editorResponse) return

    return formatFinnishDate(
      parseISODate(
        editorResponse.value.properties.find(p => p.key === 'henkilö')
          .model.value.properties.find(p => p.key === 'syntymäaika')
          .model.value.data
      )
    )
  }

  authorizeMember(memberCode) {
    Http.post(`/koski/api/omadata/valtuutus/${memberCode}`, {})
      .doError((e) => {
        if (e && e.httpStatus === 401) {
          console.log(`Must be logged in before we can authorize ${memberCode}`)
          this.setState({error: t('Sinun tulee olla kirjautunut sisään')})
        } else {
          console.log(`Failed to add permissions for ${memberCode}`, e)
          this.setState({error: t('Tallennus epäonnistui')})
        }
      })
      .onValue(() => {
        console.log(`Permissions added for ${memberCode}`)
        this.setState({
          authorizationGiven: true
        })
      })
  }

  onLogoutClicked() {
    window.location.href = this.getLogoutURL()
  }

  getLogoutURL() {
    return `/koski/user/logout?target=${this.state.callback}`
  }

  render() {

    const acceptanceBox = this.state.authorizationGiven ?
      <HyvaksyntaAnnettu logoutURL={this.getLogoutURL()}/> :
      (
        <AnnaHyvaksynta memberP={memberP(this.state.memberCode)}
        onAcceptClick={() => this.authorizeMember(this.state.memberCode)}
        onCancelClick={() => this.onLogoutClicked()}
        />
      )

    const error = this.state.error ?  <Error error={{text: this.state.error}} /> : null

    return (
      <div>
        <Header userP={userP} onLogoutClicked={this.onLogoutClicked}/>
        {error}

        <div className="acceptance-container">
          <div className="heading"><h1><Text name="Henkilökohtaisten tietojen käyttö"/></h1></div>
          <div className="user">
            <div className="username">{userP.map(user => user && user.name)}</div>
            <div className="dateofbirth"> {editorP.map(s => this.getBirthDate(s))}</div>
          </div>
          {acceptanceBox}
        </div>

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
