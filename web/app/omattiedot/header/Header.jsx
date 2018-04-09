import React from 'baret'
import Atom from 'bacon.atom'
import {modelItems, modelLookup} from '../../editor/EditorModel'
import {HeaderInfo} from './HeaderInfo'
import {withFeatureFlag} from '../../components/withFeatureFlag'
import {HeaderButtons} from './HeaderButtons'
import {HeaderName} from './HeaderName'
import {HeaderVirheraportointiSection} from './HeaderVirheraportointiSection'
import {ift} from '../../util/util'

const VirheraportointiFeature = withFeatureFlag(FEATURE.OMAT_TIEDOT.VIRHERAPORTOINTI, HeaderVirheraportointiSection)

export const Header = ({oppija}) => {
  const showPalvelussaNäkyvätTiedot = Atom(false)
  const showVirheraportointi = Atom(false)

  const henkilö = modelLookup(oppija, 'henkilö')
  const opiskeluoikeudet = modelItems(oppija, 'opiskeluoikeudet')

  return (
    <header className='header'>
      <HeaderInfo showPalvelussaNäkyvätTiedotA={showPalvelussaNäkyvätTiedot}/>

      <div className='header__bottom-row'>
        <HeaderName henkilö={henkilö}/>
        <HeaderButtons showVirheraportointiA={showVirheraportointi}/>

        {ift(showVirheraportointi,
          <VirheraportointiFeature
            showVirheraportointiA={showVirheraportointi}
            henkilö={henkilö}
            opiskeluoikeudet={opiskeluoikeudet}
          />
        )}
      </div>
    </header>
  )
}
