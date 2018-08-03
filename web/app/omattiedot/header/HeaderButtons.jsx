import React from 'react'
import {MultistateToggleButton} from '../../components/ToggleButton'
import {withFeatureFlag} from '../../components/withFeatureFlag'
import {FormState} from './Header'
import FloatingActionButton from '../../components/FloatingActionButton'

const VirheraportointiButton = withFeatureFlag(FEATURE.OMAT_TIEDOT.VIRHERAPORTOINTI, MultistateToggleButton)
const SuoritusjakoButton = withFeatureFlag(FEATURE.OMAT_TIEDOT.SUORITUSJAKO, MultistateToggleButton)
const FloatingSuoritusjakoButton = withFeatureFlag(FEATURE.OMAT_TIEDOT.SUORITUSJAKO, FloatingActionButton)

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

export const HeaderButtons = ({uiModeA}) => (
  <div className='header__buttons'>
    <VirheraportointiButton
      stateA={uiModeA}
      value={FormState.VIRHERAPORTOINTI}
      clearedStateValue={FormState.NONE}
      text='Onko suorituksissasi virhe?'
      style='secondary'
    />
    <SuoritusjakoButton
      id={ACTION_BUTTON_ID}
      stateA={uiModeA}
      value={FormState.SUORITUSJAKO}
      clearedStateValue={FormState.NONE}
      text='Jaa suoritustietoja'
    />

    <FloatingSuoritusjakoButton
      text={'Suoritustietojen jakaminen'}
      onClick={() => moveToSuoritusjako(() => uiModeA.set(FormState.SUORITUSJAKO))}
      visibilityOffset={ACTION_BUTTON_OFFSET}
    />
  </div>
)
