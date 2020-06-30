import React from 'baret'
import Atom from 'bacon.atom'
import {modelLookup} from '../../editor/EditorModel'
import {HeaderButtons} from './HeaderButtons'
import {HeaderName} from './HeaderName'
import {HeaderVirheraportointiSection} from './HeaderVirheraportointiSection'
import {HeaderHuollettavanTiedotSection} from './HeaderHuollettavanTiedotSection'
import {hasOpintoja} from '../../oppija/oppija'
import Text from '../../i18n/Text'

export const FormState = {
  VIRHERAPORTOINTI: 'virheraportointi',
  HUOLLETTAVANTIEDOT: 'huollettavantiedot',
  NONE: 'none'
}

export const Header = ({oppija}) => {
  const uiMode = Atom(FormState.NONE)

  const henkilö = modelLookup(oppija, 'henkilö')

  return (
    <header className='header'>
      <div className='header__bottom-row'>
        <HeaderName henkilö={henkilö}/>
        <HeaderButtons uiModeA={uiMode} stateType={FormState} oppija={oppija}/>
        <HeaderVirheraportointiSection uiModeA={uiMode} oppija={oppija}/>
        <HeaderHuollettavanTiedotSection uiModeA={uiMode}/>
      </div>
      {hasOpintoja(oppija) && (<div className='header__caption'>
        <p className='textstyle-lead'>
          <Text name='Opintoni ingressi'/>
        </p>
      </div>)}
    </header>
  )
}
