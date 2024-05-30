import React from 'baret'
import Bacon from 'baconjs'
import * as R from 'ramda'
import {
  modelData,
  modelItems,
  modelLookup,
  modelTitle
} from '../../editor/EditorModel'
import SuoritusIdentifier from './SuoritusIdentifier'
import { suoritusjakoSuoritusTitle } from './suoritusjako'
import { suorituksenTyyppi } from '../../suoritus/Suoritus'
import { OpiskeluoikeudenTila } from '../fragments/OpiskeluoikeudenTila'
import Text from '../../i18n/Text'
import Checkbox from '../../components/Checkbox'
import { isOpintojakso } from '../../opiskeluoikeus/OpiskeluoikeusEditor'

export const SelectableSuoritusList = ({
  opiskeluoikeudet,
  selectedSuoritusIds
}) => {
  const toggleSelection = (id) => (event) => {
    selectedSuoritusIds.modify((ids) =>
      event.target.checked ? R.append(id, ids) : R.without([id], ids)
    )
  }

  return (
    <ul className="create-suoritusjako__list">
      {opiskeluoikeudet.map((oppilaitoksittain) => {
        const oppilaitos = modelLookup(oppilaitoksittain, 'oppilaitos')
        const groupTitle = modelTitle(oppilaitos)
        const oppilaitoksenOpiskeluoikeudet = Bacon.constant(
          modelItems(oppilaitoksittain, 'opiskeluoikeudet')
        )

        return [
          <li className="oppilaitos-group" key={groupTitle}>
            <h3 className="oppilaitos-group__header">{groupTitle}</h3>

            <ul className="oppilaitos-group__list">
              {Bacon.combineWith(
                oppilaitoksenOpiskeluoikeudet,
                selectedSuoritusIds,
                (opiskeluoikeudetModels, selectedIds) =>
                  opiskeluoikeudetModels.map((oo) => {
                    const suoritukset = modelItems(oo, 'suoritukset')
                    const options = R.compose(
                      withoutDuplicates,
                      withIdentifiers(oo)
                    )(suoritukset)
                    return options.map(({ id, Title }) => (
                      <li key={id}>
                        <Checkbox
                          id={id}
                          checked={R.includes(id, selectedIds)}
                          onChange={toggleSelection(id)}
                          LabelComponent={Title}
                          listStylePosition="inside"
                        />
                      </li>
                    ))
                  })
              )}
            </ul>
          </li>
        ]
      })}
    </ul>
  )
}

export const isKorkeakouluSuoritus = (suoritus) =>
  [
    'korkeakoulututkinto',
    'korkeakoulunopintojakso',
    'muukorkeakoulunsuoritus'
  ].includes(suorituksenTyyppi(suoritus))

const withIdentifiers = (opiskeluoikeus) => (suoritukset) => {
  const [opintojaksot, muut] = R.partition(isOpintojakso, suoritukset)
  const suoritusIdentifiers = muut.map((suoritus) => {
    return {
      id: SuoritusIdentifier(opiskeluoikeus, suoritus),
      Title: () => (
        <span>
          {suoritusjakoSuoritusTitle(suoritus)}
          {isKorkeakouluSuoritus(suoritus) &&
            !!modelData(opiskeluoikeus, 'alkamispäivä') && (
              <OpiskeluoikeudenTila opiskeluoikeus={opiskeluoikeus} />
            )}
        </span>
      )
    }
  })

  const opintojaksoIdentifiers =
    opintojaksot.length === 0
      ? []
      : [
          {
            id: SuoritusIdentifier(opiskeluoikeus, opintojaksot[0]),
            Title: () => (
              <span>
                {opintojaksot.length}
                <Text name="opintojaksoa" />
              </span>
            )
          }
        ]

  return suoritusIdentifiers.concat(opintojaksoIdentifiers)
}

const withoutDuplicates = (suorituksetWithIdentifiers) =>
  R.uniqBy((sWithId) => sWithId.id, suorituksetWithIdentifiers)
