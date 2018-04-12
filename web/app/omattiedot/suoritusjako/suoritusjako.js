import React from 'react'
import {suorituksenTyyppi, suoritusTitle} from '../../suoritus/Suoritus'
import Text from '../../i18n/Text'
import {modelItems} from '../../editor/EditorModel'
import {flatMapArray} from '../../util/util'

export const suoritusjakoSuoritusTitle = suoritus => suorituksenTyyppi(suoritus) === 'perusopetuksenoppimaara'
  ? <Text name="Päättötodistus"/>
  : suoritusTitle(suoritus)

export const oppilaitoksenPäätasonSuoritukset = oppilaitoksenOpiskeluoikeudet => flatMapArray(
  modelItems(oppilaitoksenOpiskeluoikeudet, 'opiskeluoikeudet'),
  oo => modelItems(oo, 'suoritukset')
)
