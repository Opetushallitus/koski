import React from 'baret'
import Atom from 'bacon.atom'
import Text from '../i18n/Text'
import {ift} from '../util/util'
import {ISO2FinnishDate} from '../date/date'
import {modelData, modelTitle} from '../editor/EditorModel'
import {Popup} from '../components/Popup'
import {TiedotPalvelussa} from './TiedotPalvelussa'
import {Virheraportointi} from './Virheraportointi'

const togglePopup = stateA => () => stateA.modify(v => !v)

const HeaderInfo = ({henkilö, showPalvelussaNäkyvätTiedotA, showVirheraportointiA}) => {
  const nimi = <p>{`${modelData(henkilö, 'etunimet')} ${modelData(henkilö, 'sukunimi')}`}</p>
  const syntymäaika = modelTitle(henkilö, 'syntymäaika') &&
    <p className='syntynyt'>
      <Text name='syntynyt'/>
      <span> {ISO2FinnishDate(modelTitle(henkilö, 'syntymäaika'))}</span>
    </p>

  return (
    <header>
      <h2 className='header__heading'>
        <Text name='Opintoni'/>
      </h2>
      <div className='header__caption'>
        <p>
          <Text name='Opintoni ingressi'/>
        </p>
        <a onClick={togglePopup(showPalvelussaNäkyvätTiedotA)}>
          <Text name='Mitkä tiedot palvelussa näkyvät?'/>
        </a>
      </div>
      {ift(showPalvelussaNäkyvätTiedotA,
        <Popup showStateAtom={showPalvelussaNäkyvätTiedotA} inline={true}>
          <TiedotPalvelussa/>
        </Popup>
      )}
      <div className='header__bottom-row'>
        <div className='header__name'>
          {nimi}
          {syntymäaika}
        </div>

        <button onClick={togglePopup(showVirheraportointiA)}>
          <Text name='Suorituksissani on virhe'/>
        </button>
      </div>
    </header>
  )
}

export class Header extends React.Component {
  constructor(props) {
    super(props)

    this.showPalvelussaNäkyvätTiedot = Atom(false)
    this.showVirheraportointi = Atom(false)
  }

  render() {
    return (
      <div>
        <HeaderInfo
          henkilö={this.props.henkilö}
          showPalvelussaNäkyvätTiedotA={this.showPalvelussaNäkyvätTiedot}
          showVirheraportointiA={this.showVirheraportointi}
        />

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
