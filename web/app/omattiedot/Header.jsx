import React from 'baret'
import Atom from 'bacon.atom'
import Text from '../i18n/Text'
import {ift} from '../util/util'
import {ISO2FinnishDate} from '../date/date'
import {modelData, modelTitle} from '../editor/EditorModel'
import {Popup} from '../components/Popup'
import {TiedotPalvelussa} from './TiedotPalvelussa'
import {Virheraportointi} from './Virheraportointi'

export class Header extends React.Component {
  constructor(props) {
    super(props)
    this.showPalvelussaNäkyvätTiedot = Atom(false)
    this.showVirheraportointi = Atom(false)
  }

  render() {
    const {henkilö} = this.props

    const nimi = <p>{`${modelData(henkilö, 'etunimet')} ${modelData(henkilö, 'sukunimi')}`}</p>
    const syntymäaika = modelTitle(henkilö, 'syntymäaika') &&
      <p className='syntynyt'>
        <Text name='syntynyt'/>
        <span> {ISO2FinnishDate(modelTitle(henkilö, 'syntymäaika'))}</span>
      </p>

    const togglePopup = stateA => () => stateA.modify(v => !v)

    return (
      <div>
        <header>
          <h2 className='header__heading'>
            <Text name='Opintoni'/>
          </h2>
          <div className='header__caption'>
            <p>
              <Text name='Opintoni ingressi'/>
            </p>
            <a onClick={togglePopup(this.showPalvelussaNäkyvätTiedot)}>
              <Text name='Mitkä tiedot palvelussa näkyvät?'/>
            </a>
          </div>
          {ift(this.showPalvelussaNäkyvätTiedot,
            <Popup showStateAtom={this.showPalvelussaNäkyvätTiedot} inline={true}>
              <TiedotPalvelussa/>
            </Popup>
          )}
          <div className='header__bottom-row'>
            <div className='header__name'>
              {nimi}
              {syntymäaika}
            </div>

            <button onClick={togglePopup(this.showVirheraportointi)}>
              <Text name='Suorituksissani on virhe'/>
            </button>
          </div>
        </header>

        {ift(this.showVirheraportointi,
          <div className='virheraportointi'>
            <Popup showStateAtom={this.showVirheraportointi} inline={true}>
              <Virheraportointi/>
            </Popup>
          </div>
        )}
      </div>
    )
  }
}
