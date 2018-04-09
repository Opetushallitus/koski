import React from 'react'
import {ToggleButton} from '../../components/ToggleButton'
import {withFeatureFlag} from '../../components/withFeatureFlag'

const VirheraportointiButton = withFeatureFlag(FEATURE.OMAT_TIEDOT.VIRHERAPORTOINTI, ToggleButton)

export const HeaderButtons = ({showVirheraportointiA}) => (
  <div className='header__buttons'>
    <VirheraportointiButton
      toggleA={showVirheraportointiA}
      text='Onko suorituksissasi virhe?'
    />
  </div>
)
