import React from 'react'
import {yearFromIsoDateString} from '../../date/date'
import {modelTitle} from '../../editor/EditorModel'

export const OpiskeluoikeudenTila = ({opiskeluoikeus}) => (
  <span>
    {' ('}
    <span className="alku pvm">{yearFromIsoDateString(modelTitle(opiskeluoikeus, 'alkamispäivä'))}</span>{'—'}
    <span className="loppu pvm">{yearFromIsoDateString(modelTitle(opiskeluoikeus, 'päättymispäivä'))}{', '}</span>
    <span className="tila">{modelTitle(opiskeluoikeus, 'tila.opiskeluoikeusjaksot.-1.tila').toLowerCase()}</span>
    {')'}
  </span>
)

OpiskeluoikeudenTila.displayName = 'OpiskeluoikeudenTila'
