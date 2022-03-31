import React from 'baret'
import Atom from 'bacon.atom'
import {modelData, modelItems, modelLookup} from '../../editor/EditorModel'
import {HeaderInfo} from './HeaderInfo'
import {HeaderButtons} from './HeaderButtons'
import {HeaderName} from './HeaderName'
import {HeaderVirheraportointiSection} from './HeaderVirheraportointiSection'
import {HeaderSuoritusjakoSection} from './HeaderSuoritusjakoSection'
import {HeaderHuollettavanTiedotSection} from './HeaderHuollettavanTiedotSection'
import {HuollettavaDropdown} from './HuollettavaDropdown'

export const FormState = {
  VIRHERAPORTOINTI: 'virheraportointi',
  SUORITUSJAKO: 'suoritusjako',
  HUOLLETTAVANTIEDOT: 'huollettavantiedot',
  NONE: 'none'
}

export const Header = ({oppija, oppijaSelectionBus}) => {
  const uiMode = Atom(FormState.NONE)

  const henkilö = modelLookup(oppija, 'henkilö')
  const opiskeluoikeudet = modelItems(oppija, 'opiskeluoikeudet')
  let varoitukset = modelItems(oppija, 'varoitukset').map(modelData)

  return (
    <header className='header'>
      <HuollettavaDropdown oppija={oppija} oppijaSelectionBus={oppijaSelectionBus}/>
      <HeaderInfo oppija={oppija} varoitukset={varoitukset}/>

      <div className='header__bottom-row'>
        <HeaderName henkilö={henkilö}/>
        <HeaderButtons uiModeA={uiMode} stateType={FormState} oppija={oppija}/>
        <HeaderVirheraportointiSection uiModeA={uiMode} oppija={oppija}/>
        <HeaderSuoritusjakoSection uiModeA={uiMode} opiskeluoikeudet={opiskeluoikeudet}/>
        <HeaderHuollettavanTiedotSection uiModeA={uiMode}/>
      </div>
    </header>
  )
}

Header.displayName = 'Header'
