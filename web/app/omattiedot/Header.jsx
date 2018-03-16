import React from 'baret'
import Atom from 'bacon.atom'
import Text from '../i18n/Text'
import {ift} from '../util/util'
import {ISO2FinnishDate} from '../date/date'
import {modelData, modelTitle} from '../editor/EditorModel'
import {Popup} from '../components/Popup'
import {TiedotPalvelussa} from './TiedotPalvelussa'

export class Header extends React.Component {
  constructor(props) {
    super(props)
    this.showPopup = Atom(false)
  }

  render() {
    const {henkilö} = this.props

    const nimi = <p>{`${modelData(henkilö, 'etunimet')} ${modelData(henkilö, 'sukunimi')}`}</p>
    const syntymäaika = modelTitle(henkilö, 'syntymäaika') &&
      <p className='syntynyt'>
        <Text name='syntynyt'/>
        <span> {ISO2FinnishDate(modelTitle(henkilö, 'syntymäaika'))}</span>
      </p>

    const togglePopup = () => this.showPopup.modify(v => !v)

    return (
      <header>
        <h2 className='header__heading'>
          <Text name='Opintoni'/>
        </h2>
        <div className='header__caption'>
          <p>
            <Text name='Opintoni ingressi'/>
          </p>
          <div>
            <a onClick={togglePopup}>
              <Text name='Mitkä tiedot palvelussa näkyvät?'/>
            </a>
          </div>
        </div>
        {ift(this.showPopup,
          <Popup showStateAtom={this.showPopup} inline={true}>
            <TiedotPalvelussa/>
          </Popup>
        )}
        <div className='header__name'>
          {nimi}
          {syntymäaika}
        </div>
      </header>
    )
  }
}
