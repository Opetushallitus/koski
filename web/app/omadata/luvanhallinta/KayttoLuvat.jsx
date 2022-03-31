import React from 'baret'
import {formatFinnishDate, ISO2FinnishDate, parseISODate} from '../../date/date'
import Text from '../../i18n/Text'

export class Kayttoluvat extends React.Component {
  render() {
    const {kayttoluvat, removeCallback} = this.props
    const hasKayttolupia = kayttoluvat.length > 0

    return (
      <div className='kayttoluvat-container'>
        <ul className='kayttolupa-list'>
          {
            (hasKayttolupia
              ? kayttoluvat.map(lupa =>
                (<Kayttolupa
                  key={lupa.asiakasId}
                  kayttolupa={lupa}
                  removeCallback={removeCallback}
                />))
              : <NoMyDataPermissions/>)
          }
        </ul>
      </div>
    )
  }
}

const Kayttolupa = ({kayttolupa, removeCallback}) => {
  const {asiakasName, asiakasId, expirationDate, timestamp, purpose} = kayttolupa
  const expDateInFinnish = formatFinnishDate(parseISODate(expirationDate))
  const timestampInFinnish = ISO2FinnishDate(timestamp)

  return (
    <li className='kayttolupa-container' tabIndex={0}>

      <h3 className='asiakas'>{asiakasName}</h3>

      <div className='bottom-items'>

        <div className='container'>

          <div className='voimassaolo'>
            <div className='teksti' ><Text name='Lupa voimassa'/></div>
            <div className='aikaleima'>
              <span className='mobile-whitespace'>{': '}</span><span> {`${timestampInFinnish} - ${expDateInFinnish}`}</span>
            </div>
          </div>

          <div className='oikeudet'>
            <span className='list-label'><Text name='Palveluntarjoaja näkee seuraavat opintoihisi liittyvät tiedot'/>{':'}</span>
            <ul>
              <li>
                <Text name='Tiedot opiskeluoikeuksistasi' />
              </li>
              <li>
                <Text name='Nimesi ja syntymäaikasi' />
              </li>
            </ul>
            {purpose && <Kayttotarkoitus purpose={purpose}/>}
          </div>
        </div>

        <div className='peru-lupa'>
          <button className='inline-link-button' onClick={() => removeCallback(asiakasId)}><Text name='Peru lupa'/></button>
        </div>

      </div>

    </li>
  )
}

Kayttolupa.displayName = 'Kayttolupa'

const Kayttotarkoitus = ({purpose}) => (
  <div className='kayttotarkoitus'>
    <div className='headline-container'>
      <span className='headline'><Text name='Tietojen käyttötarkoitus'/>{':'}</span>
    </div>
    <Text name={purpose}/>
  </div>
)

Kayttotarkoitus.displayName = 'Kayttotarkoitus'

const NoMyDataPermissions = () => (
  <li className='no-permission'>
    <Text name={'Et ole tällä hetkellä antanut millekään palveluntarjoajalle lupaa nähdä opintotietojasi Oma Opintopolusta. ' +
    'Luvan myöntäminen tapahtuu kyseisen palvelutarjoajan sivun kautta.'}/>
  </li>
)

NoMyDataPermissions.displayName = 'NoMyDataPermissions'
