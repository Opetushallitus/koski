import React from 'baret'
import { ensureArrayKey, modelSet, pushModel } from '../editor/EditorModel'
import { koulutusModuuliprototypes } from '../suoritus/Koulutusmoduuli'
import {
  selectOsasuoritusPrototype,
  osasuorituksenKoulutusmoduuli
} from './Osasuoritus'
import { LisaaOsasuoritus } from './LisaaOsasuoritus'
import { useKoodistovalues } from '../uusioppija/koodisto'

function resolveOppiainekoodistot(suoritusTyyppi) {
  switch (suoritusTyyppi) {
    case 'primarylapsioppimisalueensuoritus':
      return [
        'europeanschoolofhelsinkilapsioppimisalue',
        'europeanschoolofhelsinkikielioppiaine',
        'europeanschoolofhelsinkimuuoppiaine'
      ]
    case 'primaryoppimisalueenalaosasuoritus':
      return ['europeanschoolofhelsinkiprimaryalaoppimisalue']
    case 'secondaryupperoppiaineensuorituss6':
    case 'secondaryupperoppiaineensuorituss7':
      return [
        'europeanschoolofhelsinkikielioppiaine',
        'europeanschoolofhelsinkimuuoppiaine'
      ]
    case 'secondaryloweroppiaineensuoritus':
      return []
    case 's7oppiaineenalaosasuoritus':
      return ['europeanschoolofhelsinkis7oppiaineenkomponentti']
    default:
      return []
  }
}

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
  const firstPrototype = suoritusPrototype.value.classes[0]

  const oppiaineKoodistot = resolveOppiainekoodistot(firstPrototype)
  const oppiaineet = useKoodistovalues(...oppiaineKoodistot)

  if (oppiaineet.length === 0) {
    return null
  }

  const osasuoritusKoulutusmoduulit =
    koulutusModuuliprototypes(suoritusPrototype)

  const koulutusmoduuliProto = (selectedItem) =>
    selectedItem !== undefined
      ? osasuorituksenKoulutusmoduuli(
          osasuoritusKoulutusmoduulit,
          selectedItem.data
        )
      : osasuoritusKoulutusmoduulit[0]

  const addOsasuoritus = (
    koulutusmoduuli,
    tutkinto,
    liittyyOsasuoritukseen
  ) => {
    console.log('koulutusmoduuli', koulutusmoduuli)
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

  const getFinnishName = (val) => val?.nimi?.fi || ''

  const lisättävätOsasuoritukset = {
    osat: oppiaineet.sort((a, b) =>
      getFinnishName(a).localeCompare(getFinnishName(b))
    ),
    osanOsa: false
  }

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
