import React from 'baret'
import Atom from 'bacon.atom'
import Text from '../i18n/Text'
import {ift} from '../util/util'
import {ISO2FinnishDate} from '../date/date'
import {modelData, modelTitle} from '../editor/EditorModel'
import {Popup} from '../components/Popup'
import {TiedotPalvelussa} from './TiedotPalvelussa'
import {RaportoiVirheestäForm} from './virheraportointi/RaportoiVirheestaForm'
import {withFeatureFlag} from '../components/withFeatureFlag'

const togglePopup = stateA => () => stateA.modify(v => !v)

const ToggleButton = ({toggleA, text, style}) => style === 'text'
  ? <a onClick={togglePopup(toggleA)}><Text name={text}/></a>
  : <button onClick={togglePopup(toggleA)}><Text name={text}/></button>

const HeaderInfo = ({henkilö, showPalvelussaNäkyvätTiedotA, showVirheraportointiA}) => {
  const VirheraportointiButton = withFeatureFlag(FEATURE.OMAT_TIEDOT.VIRHERAPORTOINTI, ToggleButton)

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
        <div>
          <ToggleButton
            toggleA={showPalvelussaNäkyvätTiedotA}
            text={'Mitkä tiedot palvelussa näkyvät?'}
            style='text'
          />
        </div>
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

        <VirheraportointiButton
          toggleA={showVirheraportointiA}
          text={'Suorituksissani on virhe'}
        />
      </div>
    </header>
  )
}

const VirheraportointiDialog = ({showVirheraportointiA}) => (
  <div>
    {ift(showVirheraportointiA,
      <div className='virheraportointi'>
        <Popup showStateAtom={showVirheraportointiA} inline={true}>
          <RaportoiVirheestäForm/>
        </Popup>
      </div>
    )}
  </div>
)

export const Header = ({henkilö}) => {
  const showPalvelussaNäkyvätTiedot = Atom(false)
  const showVirheraportointi = Atom(false)

  const VirheraportointiFeature = withFeatureFlag(FEATURE.OMAT_TIEDOT.VIRHERAPORTOINTI, VirheraportointiDialog)

  return (
    <div>
      <HeaderInfo
        henkilö={henkilö}
        showPalvelussaNäkyvätTiedotA={showPalvelussaNäkyvätTiedot}
        showVirheraportointiA={showVirheraportointi}
      />

      <VirheraportointiFeature showVirheraportointiA={showVirheraportointi}/>
    </div>
  )
}
