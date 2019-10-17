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

export const isHuoltaja = oppija => modelData(oppija, 'henkilö.oid') === modelData(oppija, 'userHenkilö.oid')

export class Header extends React.Component {
  shouldComponentUpdate() {
    // The header for 'omat tiedot' is independent of other UI re-render triggers.
    return false
  }

  render() {
    const {oppijaP, onOppijaChanged} = this.props
    const uiMode = Atom(FormState.NONE)
    const henkilöP = oppijaP.map(o => modelLookup(o, 'henkilö'))
    const opiskeluoikeudetP = oppijaP.map(o => modelItems(o, 'opiskeluoikeudet'))
    const varoituksetP = oppijaP.map(o => modelItems(o, 'varoitukset').map(modelData))

    return (
      <header className='header'>
        <OppijaSelector oppijaP={oppijaP} onOppijaChanged={onOppijaChanged} />
        <HeaderInfo varoituksetP={varoituksetP} oppijaP={oppijaP}/>

        <div className='header__bottom-row'>
          <HeaderName henkilöP={henkilöP}/>
          <HeaderButtons uiModeA={uiMode} stateType={FormState} oppijaP={oppijaP}/>
          <VirheraportointiFeature uiModeA={uiMode} henkilöP={henkilöP} opiskeluoikeudetP={opiskeluoikeudetP}/>
          <SuoritusjakoFeature uiModeA={uiMode} opiskeluoikeudetP={opiskeluoikeudetP}/>
        </div>
      </header>
    )
  }
}
