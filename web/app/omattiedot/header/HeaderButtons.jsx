import React from 'react'
import {MultistateToggleButton} from '../../components/ToggleButton'
import {FormState} from './Header'
import {virheRaportointiTitle} from './HeaderVirheraportointiSection'
import {hasOpintoja} from '../../oppija/oppija'


export const HeaderButtons = ({uiModeA, oppija}) => (
  <div className='header__buttons'>
    {hasOpintoja(oppija) && <MultistateToggleButton
      stateA={uiModeA}
      value={FormState.VIRHERAPORTOINTI}
      clearedStateValue={FormState.NONE}
      text={virheRaportointiTitle(oppija)}
      style='secondary'
    />}
  </div>
)
