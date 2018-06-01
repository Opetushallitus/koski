import React from 'baret'
import ReactDOM from 'react-dom'
import AnnaHyvaksynta from './AnnaHyvaksynta'
import HyvaksyntaAnnettu from './HyvaksyntaAnnettu'
import Footer from './Footer'
import Header from './Header'
import {formatFinnishDate, parseISODate} from '../date/date.js'
import Text from '../i18n/Text'
import '../polyfills/polyfills.js'
import Http from '../util/http'
import { currentLocation, parseQuery } from '../util/location'
import {userP} from '../util/user'

const memberP = memberId => Http.cachedGet(`/koski/api/omadata/kumppani/${memberId}`, { errorMapper: () => undefined }).toProperty()

const memberCodeRegex = /\/koski\/mydata\/(.*)/


class HyvaksyntaLanding extends React.Component {
  constructor(props) {
    super(props)
    this.state = {
      authorizationGiven: false,
      memberCode: this.getMemberCodeFromRequest(),
      callback: parseQuery(currentLocation().queryString).callback
    }

    this.postAuthorization = this.postAuthorization.bind(this)

    console.log(`Membercode: ${this.state.memberCode}, callback: ${this.state.callback}`)

    this.initializeBirthDate()
  }

  getMemberCodeFromRequest() {
    const captureGroups = memberCodeRegex.exec(currentLocation().path)

    return (captureGroups && captureGroups.length > 1 && captureGroups[1]) ?
      captureGroups[1] :
      null
  }

  initializeBirthDate() {
    try {
      Http.cachedGet('/koski/api/omattiedot/editor', {})
        .onValue((response) => {
          const dateOfBirth = response.value.properties.find(p => p.key === 'henkilö')
            .model.value.properties.find(p => p.key === 'syntymäaika')
            .model.value.data

          this.setState({
            dateOfBirth: parseISODate(dateOfBirth)
          })
        })
    } catch (e) {
      console.log('Failed to get user birth date')
      console.log(e)
    }
  }

  postAuthorization() {
    Http.post(`/koski/api/omadata/valtuutus/${this.state.memberCode}`, {})
      .doError((e) => {
        if (e && e.httpStatus === 401) {
          console.log(`Must be logged in before we can authorize ${this.state.memberCode}`)
        }
        console.log(`Failed to add permissions for ${this.state.memberCode}`)
        console.log(e)
      })
      .onValue(() => {
        console.log(`Permissions added for ${this.state.memberCode}`)
        this.setState({
          authorizationGiven: true
        })
      })
  }

  render() {

    const acceptanceBox = this.state.authorizationGiven ?
      <HyvaksyntaAnnettu callback={this.state.callback}/> :
      <AnnaHyvaksynta memberP={memberP(this.state.memberCode)} onAcceptClick={this.postAuthorization} />

    const birthDate = this.state.dateOfBirth ?
      ` s. ${formatFinnishDate(this.state.dateOfBirth)}` :
      ''

    return (
      <div>
        <Header userP={userP}/>

        <div className="acceptance-container">
          <div className="heading"><h1><Text name="Henkilökohtaisten tietojen käyttö"/></h1></div>
          <div className="user">{userP.map(user => user.name)}<span className="dateofbirth">{birthDate}</span></div>

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
