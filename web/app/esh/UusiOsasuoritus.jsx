import React from 'baret'
import {
  ensureArrayKey,
  modelProperty,
  modelSet,
  pushModel
} from '../editor/EditorModel'
import { koulutusModuuliprototypes } from '../suoritus/Koulutusmoduuli'
import {
  selectOsasuoritusPrototype,
  osasuorituksenKoulutusmoduuli
} from './Osasuoritus'
import { useKoodistovalues } from '../uusioppija/koodisto'
import { LisaaOsasuoritus } from './LisaaOsasuoritus'

export default ({
  suoritus,
  groupId,
  suoritusPrototypes,
  osasuorituksenOppiaineKoodistot,
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

  console.log('model', modelProperty(suoritus, 'koulutusmoduuli'))

  const oppiaineet = useKoodistovalues(osasuorituksenOppiaineKoodistot)

  console.log('oppiaineet', oppiaineet)

  const lisättävätOsasuoritukset = { osat: oppiaineet, osanOsa: false }

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
      <div>
        <LisaaOsasuoritus
          {...{
            addOsasuoritus,
            lisättävätOsasuoritukset,
            koulutusmoduuliProto
          }}
        />
      </div>
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
