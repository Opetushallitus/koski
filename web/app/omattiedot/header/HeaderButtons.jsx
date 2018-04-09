import React from 'react'
import {ToggleButton} from '../../components/ToggleButton'
import {withFeatureFlag} from '../../components/withFeatureFlag'

const VirheraportointiButton = withFeatureFlag(FEATURE.OMAT_TIEDOT.VIRHERAPORTOINTI, ToggleButton)
const SuoritusjakoButton = withFeatureFlag(FEATURE.OMAT_TIEDOT.SUORITUSJAKO, ToggleButton)

export const HeaderButtons = ({showVirheraportointiA, showSuoritusjakoA}) => (
  <div className='header__buttons'>
    <VirheraportointiButton
      toggleA={showVirheraportointiA}
      text='Onko suorituksissasi virhe?'
      style='text'
    />
    <SuoritusjakoButton
      toggleA={showSuoritusjakoA}
      text='Suoritustietojen jakaminen'
    />
  </div>
)
