import React from 'react'
import '../polyfills/polyfills.js'
import Text from '../i18n/Text'
import '../style/main.less'
import {lang} from '../i18n/i18n'
import Http from '../util/http';
import {formatFinnishDate} from '../date/date.js'
import Footer from './Footer'
import Header from './Header'


export default class Hyvaksynta extends React.Component {
  constructor(props) {
    super(props)
    this.state = {
      firstName: this.props.firstName,
      lastName: this.props.lastName,
      dateOfBirth: this.props.dateOfBirth
    }
  }

  render() {
    return (
      <div>
        <Header firstName={this.state.firstName} lastName={this.state.lastName}/>

        <div className="acceptance-container">
          <div className="heading"><h1><Text name="Henkilökohtaisten tietojen käyttö"/></h1></div>
          <div className="user">{this.state.firstName} {this.state.lastName}<span className="dateofbirth"> s. {formatFinnishDate(this.state.dateOfBirth)}</span></div>
          <div className="acceptance-box">
            <div className="acceptance-title"><Text name="Omadata hyväksyntä otsikko"/></div>
            <div className="acceptance-member-name">HSL Helsingin Seudun Liikenne</div>
            <div className="acceptance-share-info">
              <Text name="Palveluntarjoaja näkee"/>
              <ul>
                <li><Text name="Näkee läsnäolotiedot"/></li>
                <li><Text name="Näkee oppilaitoksen tiedot"/></li>
              </ul>
            </div>
            <div className="acceptance-button-container">
              <div className="acceptance-button button" onClick={acceptHandler}><Text name="Hyväksy"/></div>
              <span className="decline-link"><Text name="Peruuta ja palaa"/></span>
            </div>
          </div>
        </div>

        <Footer/>
      </div>
    )
  }
}

const acceptHandler = () => {
  console.log('Button clicked')
  console.log(postAuthorization("hsl"));
}


const postAuthorization = (memberCode) => {
  Http.post(`/koski/api/omadata/valtuutus/${memberCode}`, {})
    .doError((e) => {
      console.log(e)
    })
    .onValue((response) => {
      console.log(response)
    })
}
