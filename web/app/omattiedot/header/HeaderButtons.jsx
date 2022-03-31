import React from 'react'
import {MultistateToggleButton} from '../../components/ToggleButton'
import {FormState} from './Header'
import FloatingActionButton from '../../components/FloatingActionButton'
import {virheRaportointiTitle} from './HeaderVirheraportointiSection'
import {hasOpintoja} from '../../OmatTiedot'

const ACTION_BUTTON_OFFSET = 270
const ACTION_BUTTON_ID = 'suoritusjako-button'

const moveToSuoritusjako = (completionHandler) => {
  let timer = null

  const onScroll = () => {
    if (timer !== null) clearTimeout(timer)

    timer = setTimeout(() => {
      completionHandler()
      window.removeEventListener('scroll', onScroll)
    }, 100)
  }

  window.addEventListener('scroll', onScroll)

  document.getElementById(ACTION_BUTTON_ID).scrollIntoView({behavior: 'smooth'})
}

export const HeaderButtons = ({uiModeA, oppija}) => (
  <div className='header__buttons'>
    {hasOpintoja(oppija) && <MultistateToggleButton
      stateA={uiModeA}
      value={FormState.VIRHERAPORTOINTI}
      clearedStateValue={FormState.NONE}
      text={virheRaportointiTitle(oppija)}
      style='secondary'
    />}
    {!oppija.context.huollettava && hasOpintoja(oppija) && <MultistateToggleButton
      id={ACTION_BUTTON_ID}
      stateA={uiModeA}
      value={FormState.SUORITUSJAKO}
      clearedStateValue={FormState.NONE}
      text='Jaa suoritustietoja'
    />}
    <FloatingActionButton
      text='Jaa suoritustietoja'
      onClick={() => moveToSuoritusjako(() => uiModeA.set(FormState.SUORITUSJAKO))}
      visibilityOffset={ACTION_BUTTON_OFFSET}
    />
  </div>
)

HeaderButtons.displayName = 'HeaderButtons'
