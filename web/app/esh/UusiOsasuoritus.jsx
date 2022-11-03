import React from 'baret'
import Bacon from 'baconjs'
import { useState, useEffect } from 'react'
import { ensureArrayKey, modelSet, pushModel } from '../editor/EditorModel'
import { koulutusModuuliprototypes } from '../suoritus/Koulutusmoduuli'
import {
  selectOsasuoritusPrototype,
  osasuorituksenKoulutusmoduuli
} from './Osasuoritus'
import { LisaaOsasuoritus } from './LisaaOsasuoritus'
import { alternativesP, editorPrototypeP } from '../util/properties'
import { useBaconProperty } from '../util/hooks'

const isLoading = (val) => val.type === 'loading'
const isError = (val) => val.type === 'error'
const isDone = (val) => val.type === 'done'

const loading = () => ({
  type: 'loading'
})
const error = (
  message = 'Virhe ladattaessa osasuorituksen koodistoarvoja'
) => ({
  type: 'error',
  message
})
const done = (data) => ({
  type: 'done',
  data
})

const getProto = (prototype, name) => prototype.prototypes[`${name}`]

const useKoulutusmoduuliKoodistovalues = (
  editorPrototypeName,
  modelValueClass
) => {
  const [data, setData] = useState(loading())
  const editorPrototype = useBaconProperty(
    editorPrototypeP(editorPrototypeName)
  )
  useEffect(() => {
    if (editorPrototype !== null) {
      const modelPrototype = getProto(editorPrototype, modelValueClass)
      if (modelPrototype === undefined) {
        setData(error(`Prototyyppiä "${modelValueClass}" ei löydy `))
      } else {
        // Koulutusmoduuli haetaan prototyypistä ilman EditorModelin kontekstia
        const koulutusmoduuli = modelPrototype.value.properties.find(
          (property) => property.key === 'koulutusmoduuli'
        )
        if (koulutusmoduuli === undefined) {
          setData(
            error(
              `Koulutusmoduulia ei löydy prototyypille "${modelValueClass}"`
            )
          )
        } else {
          const alternativesPaths = getProto(
            editorPrototype,
            koulutusmoduuli.model.key
          )
            ?.oneOfPrototypes.filter(
              (prototype) => prototype.type === 'prototype'
            )
            .map((prototype) => getProto(editorPrototype, prototype.key))
            .flatMap((prototype) =>
              prototype.value.properties.find(
                (property) => property.key === 'tunniste'
              )
            )
            .filter((property) => property !== null)
            .map((property) => property.model)
            .map((model) => getProto(editorPrototype, model.key))
            .filter((proto) => proto !== null)
            .map((proto) => proto.alternativesPath)
            .map((alternativesPath) => alternativesP(alternativesPath))

          const prop = Bacon.combineWith(
            (...alts) => alts.flatMap((alt) => alt),
            alternativesPaths
          ).map((oppiaineet) => {
            if (Array.isArray(oppiaineet)) {
              setData(done(oppiaineet))
            } else {
              setData(done([]))
            }
          })
          const _dispose = prop.onValue((val) => {
            if (val !== undefined && Array.isArray(val)) {
              setData(done(val))
            }
          })
          const _errorDispose = prop.onError(() => console.error('Error'))
        }
      }
    }
    return () => {
      console.log('Unmount')
    }
  }, [editorPrototype, modelValueClass])
  return data
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
  const oppiaineet = useKoulutusmoduuliKoodistovalues(
    'EuropeanSchoolOfHelsinkiOpiskeluoikeus',
    suoritusPrototype.value.classes[0]
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

  if (isError(oppiaineet)) {
    return <div>{oppiaineet.message}</div>
  }
  if (isLoading(oppiaineet)) {
    return <div className="ajax-loading-placeholder">{'Ladataan...'}</div>
  }

  const osat = oppiaineet.data.map((oa) => oa.data)

  const lisättävätOsasuoritukset = {
    osat,
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
