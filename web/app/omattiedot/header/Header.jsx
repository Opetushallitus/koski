import React from 'baret'
import Atom from 'bacon.atom'
import {modelData, modelItems, modelLookup} from '../../editor/EditorModel'
import {HeaderInfo} from './HeaderInfo'
import {withFeatureFlag} from '../../components/withFeatureFlag'
import {HeaderButtons} from './HeaderButtons'
import {HeaderName} from './HeaderName'
import {HeaderVirheraportointiSection} from './HeaderVirheraportointiSection'
import {HeaderSuoritusjakoSection} from './HeaderSuoritusjakoSection'
import {HeaderHuollettavanTiedotSection} from './HeaderHuollettavanTiedotSection'

export const FormState = {
  VIRHERAPORTOINTI: 'virheraportointi',
  SUORITUSJAKO: 'suoritusjako',
  HUOLLETTAVANTIEDOT: 'huollettavantiedot',
  NONE: 'none'
}

const VirheraportointiFeature = withFeatureFlag(FEATURE.OMAT_TIEDOT.VIRHERAPORTOINTI, HeaderVirheraportointiSection)
const SuoritusjakoFeature = withFeatureFlag(FEATURE.OMAT_TIEDOT.SUORITUSJAKO, HeaderSuoritusjakoSection)

export const Header = ({oppija}) => {
  const uiMode = Atom(FormState.NONE)

  const henkilö = modelLookup(oppija, 'henkilö')
  const opiskeluoikeudet = modelItems(oppija, 'opiskeluoikeudet')
  let varoitukset = modelItems(oppija, 'varoitukset').map(modelData)

  return (
    <header className='header'>
      <HeaderInfo oppija={oppija} varoitukset={varoitukset}/>

      <div className='header__bottom-row'>
        <HeaderName henkilö={henkilö}/>
        <HeaderButtons uiModeA={uiMode} stateType={FormState} oppija={oppija}/>
        <VirheraportointiFeature uiModeA={uiMode} oppija={oppija}/>
        <SuoritusjakoFeature uiModeA={uiMode} opiskeluoikeudet={opiskeluoikeudet}/>
        <HeaderHuollettavanTiedotSection uiModeA={uiMode}/>
      </div>
    </header>
  )
}
