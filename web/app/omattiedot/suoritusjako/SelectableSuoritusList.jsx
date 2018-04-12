import React from 'baret'
import Bacon from 'baconjs'
import R from 'ramda'
import {modelItems, modelLookup, modelTitle} from '../../editor/EditorModel'
import SuoritusIdentifier from './SuoritusIdentifier'
import {suoritusjakoSuoritusTitle} from './suoritusjako'

export const SelectableSuoritusList = ({opiskeluoikeudet, selectedSuoritusIds}) => {
  const toggleSelection = id => event =>
    selectedSuoritusIds.modify(ids => event.target.checked ? R.append(id, ids) : R.without([id], ids))

  return (
    <ul className='create-suoritusjako__list'>
      {
        opiskeluoikeudet.map(oppilaitoksittain => {
          const oppilaitos = modelLookup(oppilaitoksittain, 'oppilaitos')
          const groupTitle = modelTitle(oppilaitos)
          const oppilaitoksenOpiskeluoikeudet = Bacon.constant(modelItems(oppilaitoksittain, 'opiskeluoikeudet'))

          return [
            <li className='oppilaitos-group-header' key={groupTitle}>
              {groupTitle}
            </li>,
            Bacon.combineWith(oppilaitoksenOpiskeluoikeudet, selectedSuoritusIds, (opiskeluoikeudeModels, selectedIds) =>
              opiskeluoikeudeModels.map(oo => modelItems(oo, 'suoritukset').map(s => {
                const id = SuoritusIdentifier(oo, s)
                const title = suoritusjakoSuoritusTitle(s)

                return (
                  <li key={id}>
                    <input
                      type='checkbox'
                      id={id}
                      name='suoritusjako'
                      checked={R.contains(id, selectedIds)}
                      onChange={toggleSelection(id)}
                    />
                    <label htmlFor={id}>{title}</label>
                  </li>
                )
              }))
            )
          ]
        })
      }
    </ul>
  )
}
