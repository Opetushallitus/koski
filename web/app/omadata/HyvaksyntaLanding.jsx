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

const memberCodeRegex = /\/koski\/mydata\/(.*)/

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

  render() {

    const acceptanceBox = this.state.authorizationGiven ?
      <HyvaksyntaAnnettu callback={this.state.callback}/> :
      (
        <AnnaHyvaksynta memberP={memberP(this.state.memberCode)}
        onAcceptClick={() => this.authorizeMember(this.state.memberCode)}
        onCancelClick={() => window.location.href = this.state.callback}
        />
      )

    const error = this.state.error ?  <Error error={{text: this.state.error}} /> : null

    return (
      <div>
        <Header userP={userP}/>
        {error}

        <div className="acceptance-container">
          <div className="heading"><h1><Text name="Henkilökohtaisten tietojen käyttö"/></h1></div>
          <div className="user">{userP.map(user => user && user.name)}
            <span className="dateofbirth"> {editorP.map(s => this.getBirthDate(s))}</span>
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
