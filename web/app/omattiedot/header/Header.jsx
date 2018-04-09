import React from 'baret'
import Atom from 'bacon.atom'
import {modelItems, modelLookup} from '../../editor/EditorModel'
import {HeaderInfo} from './HeaderInfo'
import {withFeatureFlag} from '../../components/withFeatureFlag'
import {HeaderButtons} from './HeaderButtons'
import {HeaderName} from './HeaderName'
import {HeaderVirheraportointiSection} from './HeaderVirheraportointiSection'
import {HeaderSuoritusjakoSection} from './HeaderSuoritusjakoSection'
import {ift} from '../../util/util'

const VirheraportointiFeature = withFeatureFlag(FEATURE.OMAT_TIEDOT.VIRHERAPORTOINTI, HeaderVirheraportointiSection)
const SuoritusjakoFeature = withFeatureFlag(FEATURE.OMAT_TIEDOT.SUORITUSJAKO, HeaderSuoritusjakoSection)

export const Header = ({oppija}) => {
  const showPalvelussaNäkyvätTiedot = Atom(false)
  const showVirheraportointi = Atom(false)
  const showSuoritusjako = Atom(false)

  const henkilö = modelLookup(oppija, 'henkilö')
  const opiskeluoikeudet = modelItems(oppija, 'opiskeluoikeudet')

  return (
    <header className='header'>
      <HeaderInfo showPalvelussaNäkyvätTiedotA={showPalvelussaNäkyvätTiedot}/>

      <div className='header__bottom-row'>
        <HeaderName henkilö={henkilö}/>
        <HeaderButtons
          showVirheraportointiA={showVirheraportointi}
          showSuoritusjakoA={showSuoritusjako}
        />

        <VirheraportointiFeature
          showVirheraportointiA={showVirheraportointi}
          henkilö={henkilö}
          opiskeluoikeudet={opiskeluoikeudet}
        />

        {ift(showSuoritusjako,
          <SuoritusjakoFeature
            showSuoritusjakoA={showSuoritusjako}
          />
        )}
      </div>
    </header>
  )
}
