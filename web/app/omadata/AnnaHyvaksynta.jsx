import React from 'baret'
import Text from '../i18n/Text'
import '../style/main.less'

export default ({memberName, onAcceptClick, logoutURL}) => (
  <div>
    <div className='acceptance-box'>
      <div className='acceptance-title'><Text name='Antamalla suostumuksesi sallit, että Opetushallitus luovuttaa sinua koskevia...'/></div>
      <div className='acceptance-member-name'><Text name={memberName}/></div>
      <div className='acceptance-share-info'>
        <Text name='Palveluntarjoajalle luovutetaan seuraavat henkilötiedot'/>{':'}
        <ul>
          <li><Text name='Oppijanumerosi'/></li>
          <li>
            <Text name='Tiedot opiskeluoikeuksistasi sisältäen'/>{':'}
            <ul>
              <li><Text name='Tiedot oppilaitoksesta'/></li>
              <li><Text name='Opiskeluoikeuden alkamis- ja päättymispäivämäärät sekä läsnäolotiedot'/></li>
              <li><Text name='Opiskeluoikeutesi koulutusaste sekä suorituksen tavoite (tutkinto vai muu)'/></li>
              <li><Text name='Ammatillisista opiskeluoikeuksista tiedot osa-aikaisuusjaksoista sekä oppisopimuksista ja koulutussopimuksista'/></li>
            </ul>
          </li>
          <li><Text name='Suostumuksesi päättymisajankohta'/></li>
        </ul>
      </div>
      <div className='acceptance-paragraphs'>
        <p><a href='https://confluence.csc.fi/pages/viewpage.action?pageId=76536741'><Text name='Tarkemmat tiedot luovutettavista henkilötiedoista'/></a></p>
        <p><Text name='Palveluntarjoaja käyttää tietojasi opiskelijahintaisen matkalipun myöntämisen yhteydessä tapahtuvaan...'/></p>
        <p><Text name='Palveluntarjoaja suorittaa opiskelija-aseman todentamisessa automaattista päätöksentekoa.'/></p>
        <p><Text name='Palveluntarjoaja ei tämän suostumuksen perusteella luovuta henkilötietojasi eteenpäin.'/></p>
        <p><Text name='Suostumuksesi on voimassa 12 kuukautta, minkä jälkeen voit uusia se. Voit perua suostumuksesi...'/></p>
        <p><Text name='Kun suostumus on päättynyt tai peruutettu, Opetushallitus ei enää luovuta henkilötietojasi palveluntarjoajalle...'/></p>
        <p><a href='https://www.hsl.fi/tietosuoja'><Text name='Lisätietoja palveluntarjoajan suorittamasta tietojen käsittelystä'/></a></p>
        <p><a href='https://opintopolku.fi/wp/tietosuojaseloste/koski-palvelun-tietosuojaseloste/'><Text name='Lisätietoja Opetushallituksen suorittamasta tietojen käsittelystä'/></a></p>
        <p><Text name='Suostumuksen käsittelyä koskeva informointi...'/></p>
      </div>
    </div>
    <div className='acceptance-button-container'>
      <button className='acceptance-button koski-button' onClick={onAcceptClick}><Text name='Hyväksy'/></button>
      <div className='decline-link'><a href={logoutURL}><Text name='Peruuta ja palaa'/></a></div>
    </div>
  </div>
)
