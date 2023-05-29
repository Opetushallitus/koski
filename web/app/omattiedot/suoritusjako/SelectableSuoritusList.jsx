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
import SuoritetutKokonaisuudet from './SuoritetutKokonaisuudet'
import { useState, useEffect } from 'react'

export const SelectableSuoritusList = ({
  opiskeluoikeudet,
  selectedSuoritusIds
}) => {
  const [kokonaisuudetDisabled, setKokonaisuudetDisabled] = useState(false)
  const [tutkinnotDisabled, setTutkinnotDisabled] = useState(false)
  const toggleSelection = (id) => (event) => {
    selectedSuoritusIds.modify((ids) =>
      event.target.checked ? R.append(id, ids) : R.without([id], ids)
    )
    setKokonaisuudetDisabled(selectedSuoritusIds.value.length > 0)
  }

  const handleSelectedKokonaisuudet = (selectedItems, unselectedItems) => {
    const ourVersion = R.map(R.pick(['tyyppi']))
    selectedSuoritusIds.modify((ids) =>
      R.without(ourVersion(unselectedItems), ids)
    )
    selectedSuoritusIds.modify((ids) =>
      selectedItems.length > 0 ? R.concat(ourVersion(selectedItems), ids) : ids
    )
    setTutkinnotDisabled(selectedItems && selectedItems.length > 0)
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
                          disabled={tutkinnotDisabled}
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
      <h2>{'Jaettavat kokonaisuudet'}</h2>
      <SuoritetutKokonaisuudet
        handleSelectedItems={handleSelectedKokonaisuudet}
        allDisabled={kokonaisuudetDisabled}
      />
    </ul>
  )
}

const isKorkeakouluSuoritus = (suoritus) =>
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
                {opintojaksot.length} <Text name="opintojaksoa" />
              </span>
            )
          }
        ]

  return suoritusIdentifiers.concat(opintojaksoIdentifiers)
}

const withoutDuplicates = (suorituksetWithIdentifiers) =>
  R.uniqBy((sWithId) => sWithId.id, suorituksetWithIdentifiers)
