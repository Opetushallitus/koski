import React from 'react'
import { suorituksenTyyppi, suoritusTitle } from '../../suoritus/Suoritus'
import Text from '../../i18n/Text'

export const suoritusjakoSuoritusTitle = (suoritus) =>
  suorituksenTyyppi(suoritus) === 'perusopetuksenoppimaara' ? (
    <Text name="Päättötodistus" />
  ) : (
    suoritusTitle(suoritus)
  )
