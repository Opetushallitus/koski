import React from 'baret'
import Bacon from 'baconjs'
import R from 'ramda'
import {modelLookup, modelTitle} from '../../editor/EditorModel'
import SuoritusIdentifier from './SuoritusIdentifier'
import {oppilaitoksenPäätasonSuoritukset, suoritusjakoSuoritusTitle} from './suoritusjako'

export const SelectableSuoritusList = ({opiskeluoikeudet, selectedSuoritusIds}) => {
  const toggleSelection = id => event =>
    selectedSuoritusIds.modify(ids => event.target.checked ? R.append(id, ids) : R.without([id], ids))

  return (
    <ul className='create-suoritusjako__list'>
      {
        opiskeluoikeudet.map(oppilaitoksenOpiskeluoikeudet => {
          const oppilaitos = modelLookup(oppilaitoksenOpiskeluoikeudet, 'oppilaitos')
          const groupTitle = modelTitle(oppilaitos)
          const päätasonSuoritukset = Bacon.constant(oppilaitoksenPäätasonSuoritukset(oppilaitoksenOpiskeluoikeudet))

          return [
            <li className='oppilaitos-group-header' key={groupTitle}>
              {groupTitle}
            </li>,
            Bacon.combineWith(päätasonSuoritukset, selectedSuoritusIds, (suoritukset, selectedIds) =>
              suoritukset.map(s => {
                const id = SuoritusIdentifier(oppilaitos, s)
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
              })
            )
          ]
        })
      }
    </ul>
  )
}
