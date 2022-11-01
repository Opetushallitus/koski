import React from 'baret'
import Bacon from 'baconjs'
import { ensureArrayKey, modelSet, pushModel } from '../editor/EditorModel'
import { koulutusModuuliprototypes } from '../suoritus/Koulutusmoduuli'
import {
  selectOsasuoritusPrototype,
  osasuorituksenKoulutusmoduuli
} from './Osasuoritus'
import { elementWithLoadingIndicator } from '../components/AjaxLoadingIndicator'
import { koodistoValues } from '../uusioppija/koodisto'
import { LisaaOsasuoritus } from './LisaaOsasuoritus'

export default ({
  suoritus,
  groupId,
  suoritusPrototypes,
  setExpanded,
  groupTitles
}) => {
  const suoritusPrototype = selectOsasuoritusPrototype(
    suoritusPrototypes,
    groupId
  )
  const osasuoritusKoulutusmoduulit =
    koulutusModuuliprototypes(suoritusPrototype)

  const koulutusmoduuliProto = (selectedItem) =>
    selectedItem !== undefined
      ? osasuorituksenKoulutusmoduuli(
          osasuoritusKoulutusmoduulit,
          selectedItem.data
        )
      : osasuoritusKoulutusmoduulit[0]

  const osasuorituksetP = Bacon.combineWith(
    (a, b) => [...a, ...b],
    koodistoValues('europeanschoolofhelsinkimuuoppiaine'),
    koodistoValues('europeanschoolofhelsinkikielioppiaine')
  ).map((oppiaineet) => {
    console.log('oppiaineet', oppiaineet)
    return { osat: oppiaineet, osanOsa: false }
  })

  const addOsasuoritus = (
    koulutusmoduuli,
    tutkinto,
    liittyyOsasuoritukseen
  ) => {
    const osasuoritus = createOsasuoritus(
      suoritusPrototype,
      koulutusmoduuli,
      tutkinto,
      groupId,
      groupTitles,
      liittyyOsasuoritukseen
    )
    pushSuoritus(setExpanded)(osasuoritus)
  }

  return (
    <span>
      {elementWithLoadingIndicator(
        osasuorituksetP.map((lisättävätOsasuoritukset) => {
          return (
            <div>
              <LisaaOsasuoritus
                {...{
                  addOsasuoritus,
                  lisättävätOsasuoritukset,
                  koulutusmoduuliProto
                }}
              />
            </div>
          )
        })
      )}
    </span>
  )
}

export const createOsasuoritus = (
  suoritusPrototype,
  koulutusmoduuli,
  _tutkinto,
  _groupId,
  _groupTitles,
  _liittyyTutkinnonOsaan
) => modelSet(suoritusPrototype, koulutusmoduuli, 'koulutusmoduuli')

export const pushSuoritus = (setExpanded) => (uusiSuoritus) => {
  pushModel(ensureArrayKey(uusiSuoritus))
  setExpanded(uusiSuoritus)(true)
}
