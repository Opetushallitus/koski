import React from 'baret'
import Atom from 'bacon.atom'
import {modelData, modelItems, modelLookup} from '../../editor/EditorModel'
import {HeaderInfo} from './HeaderInfo'
import {withFeatureFlag} from '../../components/withFeatureFlag'
import {HeaderButtons} from './HeaderButtons'
import {HeaderName} from './HeaderName'
import {HeaderVirheraportointiSection} from './HeaderVirheraportointiSection'
import {HeaderSuoritusjakoSection} from './HeaderSuoritusjakoSection'
import {OppijaSelector} from './OppijaSelector'

export const FormState = {
  VIRHERAPORTOINTI: 'virheraportointi',
  SUORITUSJAKO: 'suoritusjako',
  NONE: 'none'
}

const VirheraportointiFeature = withFeatureFlag(FEATURE.OMAT_TIEDOT.VIRHERAPORTOINTI, HeaderVirheraportointiSection)
const SuoritusjakoFeature = withFeatureFlag(FEATURE.OMAT_TIEDOT.SUORITUSJAKO, HeaderSuoritusjakoSection)

export class Header extends React.Component {
  shouldComponentUpdate() {
    // The header for 'omat tiedot' is independent of other UI re-render triggers.
    return false
  }

  render() {
    const {oppija, onOppijaChanged} = this.props

    const uiMode = Atom(FormState.NONE)

    const henkilö = modelLookup(oppija, 'henkilö')
    const opiskeluoikeudet = modelItems(oppija, 'opiskeluoikeudet')
    let varoitukset = modelItems(oppija, 'varoitukset').map(modelData)

    return (
      <header className='header'>
        <OppijaSelector oppija={oppija} onOppijaChanged={onOppijaChanged} />
        <HeaderInfo varoitukset={varoitukset}/>

        <div className='header__bottom-row'>
          <HeaderName henkilö={henkilö}/>
          <HeaderButtons uiModeA={uiMode} stateType={FormState}/>
          <VirheraportointiFeature uiModeA={uiMode} henkilö={henkilö} opiskeluoikeudet={opiskeluoikeudet}/>
          <SuoritusjakoFeature uiModeA={uiMode} opiskeluoikeudet={opiskeluoikeudet}/>
        </div>
      </header>
    )
  }
}
