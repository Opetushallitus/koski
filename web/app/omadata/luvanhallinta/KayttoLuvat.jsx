import React from 'baret'
import {formatFinnishDate, ISO2FinnishDate, parseISODate} from '../../date/date'
import ChevronUpIcon from '../../icons/ChevronUpIcon'
import ChevronDownIcon from '../../icons/ChevronDownIcon'
import Text from '../../i18n/Text'

export class Kayttoluvat extends React.Component {
  constructor(props) {
    super(props)

    this.state = {
      expanded: false
    }

    this.toggleExpand = this.toggleExpand.bind(this)
  }

  toggleExpand() {
    this.setState(prevState => ({expanded: !prevState.expanded}))
  }

  render() {
    const {expanded} = this.state
    const {kayttoluvat, removeCallback} = this.props
    const hasKayttolupia = kayttoluvat.length > 0

    return (
      <div className='kayttoluvat-container'>
        <div className='kayttoluvat-expander'>
          <button className='kayttolupa-button' onClick={() => this.toggleExpand()} aria-pressed={expanded}>
            <div className='button-container'>
              <div className='expander-text'><h2><Text name='Annetut käyttöluvat' /></h2></div>
              <div className='expand-icon'>
                {expanded
                  ? <ChevronUpIcon/>
                  : <ChevronDownIcon/>}
              </div>
            </div>
          </button>
        </div>
        <hr className='divider' />
        <ul className='kayttolupa-list'>
          {
            expanded && (hasKayttolupia
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
  const {asiakasName, asiakasId, expirationDate, timestamp} = kayttolupa
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
                <Text name='Oppilaitosten läsnäolotiedot' />
              </li>
            </ul>
          </div>

        </div>

        <div className='peru-lupa'>
          <button className='inline-link-button' onClick={() => removeCallback(asiakasId)}><Text name='Peru lupa'/></button>
        </div>

      </div>

    </li>
  )
}

const NoMyDataPermissions = () => (
  <li className='no-permission'>
    <Text name={'Et ole tällä hetkellä antanut millekään palveluntarjoajalle lupaa nähdä opintotietojasi Oma Opintopolusta. ' +
    'Luvan myöntäminen tapahtuu kyseisen palvelutarjoajan sivun kautta.'}/>
  </li>
)
