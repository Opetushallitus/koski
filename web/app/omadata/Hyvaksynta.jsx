import React from 'react'
import '../polyfills/polyfills.js'
import Text from '../i18n/Text'
import '../style/main.less'
import {lang} from '../i18n/i18n'
import Http from '../util/http';
import {formatFinnishDate} from '../date/date.js'
import Footer from './Footer'
import Header from './Header'

export default ({ firstName, lastName, dateOfBirth, memberName, onAcceptClick }) => (
  <div>
    <Header firstName={firstName} lastName={lastName}/>

    <div className="acceptance-container">
      <div className="heading"><h1><Text name="Henkilökohtaisten tietojen käyttö"/></h1></div>
      <div className="user">{firstName} {lastName}<span className="dateofbirth"> s. {formatFinnishDate(dateOfBirth)}</span></div>
      <div className="acceptance-box">
        <div className="acceptance-title"><Text name="Omadata hyväksyntä otsikko"/></div>
        <div className="acceptance-member-name">{memberName}</div>
        <div className="acceptance-share-info">
          <Text name="Palveluntarjoaja näkee"/>
          <ul>
            <li><Text name="Näkee läsnäolotiedot"/></li>
            <li><Text name="Näkee oppilaitoksen tiedot"/></li>
          </ul>
        </div>
        <div className="acceptance-button-container">
          <div className="acceptance-button button" onClick={onAcceptClick}><Text name="Hyväksy"/></div>
          <span className="decline-link"><Text name="Peruuta ja palaa"/></span>
        </div>
      </div>
    </div>

    <Footer/>
  </div>
)
