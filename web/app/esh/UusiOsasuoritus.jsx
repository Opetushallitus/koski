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

const useKoodistoValues = (
  editorPrototypeName,
  modelValueClass,
  propertyName
) => {
  const [data, setData] = useState(loading())
  const editorPrototype = useBaconProperty(
    editorPrototypeP(editorPrototypeName)
  )
  useEffect(() => {
    if (editorPrototype !== null) {
      const modelPrototype = getProto(editorPrototype, modelValueClass)
      if (modelPrototype === undefined) {
        setData(error(`Prototype "${modelValueClass}" not found`))
      } else {
        // Koulutusmoduuli haetaan prototyypistä ilman EditorModelin kontekstia
        const modelProperty = modelPrototype.value.properties.find(
          (property) => property.key === propertyName
        )
        if (modelProperty === undefined) {
          setData(
            error(
              `Property "${propertyName}" not found for prototype "${modelValueClass}"`
            )
          )
        } else {
          const prototypePropertyKey = 'tunniste'
          const alternativesPaths = getProto(
            editorPrototype,
            modelProperty.model.key
          )
            ?.oneOfPrototypes.filter(
              (prototype) => prototype.type === 'prototype'
            )
            .map((prototype) => getProto(editorPrototype, prototype.key))
            .flatMap((prototype) =>
              prototype.value.properties.find(
                (property) => property.key === prototypePropertyKey
              )
            )
            .filter((property) => property !== null)
            .map((property) => getProto(editorPrototype, property.model.key))
            .filter((proto) => proto !== null)
            .map((proto) => alternativesP(proto.alternativesPath))

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
  }, [editorPrototype, modelValueClass, propertyName])
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
  const firstPrototype = suoritusPrototype.value.classes[0]
  // Haetaan EuropeanSchoolOfHelsinkiOpiskeluoikeus-prototypestä suoritusprototypeä vastaava osasuoritus, jonka koulutusmoduulit halutaan hakea.
  const oppiaineet = useKoodistoValues(
    'EuropeanSchoolOfHelsinkiOpiskeluoikeus', // fi.oph.koski.schema.EuropeanSchoolOfHelsinkiOpiskeluoikeus
    firstPrototype, // Suorituspyorotypeä vastaava osasuoritus löytyy ekana classes-listasta
    'koulutusmoduuli' // Osasuorituksesta halutaan koulutusmoduuli(t) ulos
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
