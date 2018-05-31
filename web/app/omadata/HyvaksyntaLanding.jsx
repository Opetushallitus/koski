import React from 'react'
import ReactDOM from 'react-dom'
import AnnaHyvaksynta from './AnnaHyvaksynta'
import HyvaksyntaAnnettu from './HyvaksyntaAnnettu'
import Footer from './Footer'
import Header from './Header'
import {formatFinnishDate} from '../date/date.js'
import Text from '../i18n/Text'
import '../polyfills/polyfills.js'
import Http from '../util/http'
import { currentLocation, parseQuery } from '../util/location'

const memberCodeRegex = /\/koski\/mydata\/(.*)/


class HyvaksyntaLanding extends React.Component {
  constructor(props) {
    super(props)
    this.state = {
      firstName: 'Clara',
      lastName: 'Nieminen',
      dateOfBirth: new Date('December 17, 1995 03:24:00'),
      memberName: 'HSL Helsingin Seudun Liikenne',
      authorizationGiven: true,
      memberCode: this.getMemberCodeFromRequest(),
      callback: parseQuery(currentLocation().queryString).callback
    }

    this.postAuthorization = this.postAuthorization.bind(this)

    console.log(`Membercode: ${this.state.memberCode}, callback: ${this.state.callback}`)
  }

  getMemberCodeFromRequest() {
    const captureGroups = memberCodeRegex.exec(currentLocation().path)

    return (captureGroups && captureGroups.length > 1 && captureGroups[1]) ?
      captureGroups[1] :
      null
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
      .onValue((response) => {
        console.log(`Permissions added for ${this.state.memberCode}`)
        console.log(response)
      })
  }

  render() {

    const acceptanceBox = this.state.authorizationGiven ?
      <HyvaksyntaAnnettu/> :
      <AnnaHyvaksynta memberName={this.state.memberName} onAcceptClick={this.postAuthorization} />

    return (
      <div>
        <Header firstName={this.state.firstName} lastName={this.state.lastName}/>

        <div className="acceptance-container">
          <div className="heading"><h1><Text name="Henkilökohtaisten tietojen käyttö"/></h1></div>
          <div className="user">{this.state.firstName} {this.state.lastName}<span className="dateofbirth"> s. {formatFinnishDate(this.state.dateOfBirth)}</span></div>

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
