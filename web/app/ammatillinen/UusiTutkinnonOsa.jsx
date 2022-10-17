import React from 'baret'
import Bacon from 'baconjs'
import {
  modelData,
  ensureArrayKey,
  modelLookup,
  modelSet,
  modelSetData,
  modelSetValue,
  pushModel
} from '../editor/EditorModel'
import * as R from 'ramda'
import { toKoodistoEnumValue } from '../koodisto/koodistot'
import {
  isPaikallinen,
  koulutusModuuliprototypes
} from '../suoritus/Koulutusmoduuli'
import {
  fetchLisättävätTutkinnonOsat,
  isJatkoOpintovalmiuksiaTukevienOpintojenSuoritus,
  isYhteinenTutkinnonOsa,
  NON_GROUPED,
  selectTutkinnonOsanSuoritusPrototype,
  tutkinnonOsanOsaAlueenKoulutusmoduuli
} from './TutkinnonOsa'
import { elementWithLoadingIndicator } from '../components/AjaxLoadingIndicator'
import { koodistoValues } from '../uusioppija/koodisto'
import { LisääKorkeakouluopintoSuoritus } from './LisaaKorkeakouluopintoSuoritus'
import { LisääJatkoOpintovalmiuksiaTukevienOpintojenSuoritus } from './LisaaJatkoOpintovalmiuksiaTukevienOpintojenSuoritus'
import { LisääYhteistenTutkinnonOsienOsaAlueidenTaiLukioOpintojenTaiMuidenOpintovalmiuksiaTukevienOpintojenOsasuoritus } from './LisaaYhteistenTutkinnonOsienOsaAlueidenTaiLukioOpintojenTaiMuidenOpintovalmiuksiaTukevienOpintojenOsasuoritus'
import { LisääPaikallinenTutkinnonOsa } from './LisaaPaikallinenTutkinnonOsa'
import { LisääOsaToisestaTutkinnosta } from './LisaaOsaToisestaTutkinnosta'
import { LisääRakenteeseenKuuluvaTutkinnonOsa } from './LisaaRakenteeseenKuuluvaTutkinnonOsa'

export default ({
  suoritus,
  groupId,
  suoritusPrototypes,
  setExpanded,
  groupTitles
}) => {
  const suoritusPrototype = selectTutkinnonOsanSuoritusPrototype(
    suoritusPrototypes,
    groupId
  )
  const valtakunnallisetKoulutusmoduulit =
    valtakunnallisetKoulutusmoduuliPrototypes(suoritusPrototype)
  const paikallinenKoulutusmoduuli =
    koulutusModuuliprototypes(suoritusPrototype).find(isPaikallinen)

  const koulutusmoduuliProto = (selectedItem) =>
    selectedItem && isYhteinenTutkinnonOsa(suoritus)
      ? tutkinnonOsanOsaAlueenKoulutusmoduuli(
          valtakunnallisetKoulutusmoduulit,
          selectedItem.data
        )
      : valtakunnallisetKoulutusmoduulit[0]

  const diaarinumero =
    modelData(suoritus, 'koulutusmoduuli.perusteenDiaarinumero') ||
    modelData(suoritus, 'tutkinto.perusteenDiaarinumero')
  const suoritustapa = modelData(suoritus, 'suoritustapa.koodiarvo')

  const osatP = diaarinumero
    ? fetchLisättävätTutkinnonOsat(diaarinumero, suoritustapa, groupId)
    : isYhteinenTutkinnonOsa(suoritus)
    ? koodistoValues('ammatillisenoppiaineet').map((oppiaineet) => {
        return { osat: oppiaineet, paikallinenOsa: true, osanOsa: true }
      })
    : Bacon.constant({ osat: [], paikallinenOsa: canAddPaikallinen(suoritus) })

  const addTutkinnonOsa = (
    koulutusmoduuli,
    tutkinto,
    liittyyTutkinnonOsaan
  ) => {
    const group = groupId === NON_GROUPED ? undefined : groupId
    const tutkinnonOsa = createTutkinnonOsa(
      suoritusPrototype,
      koulutusmoduuli,
      tutkinto,
      group,
      groupTitles,
      liittyyTutkinnonOsaan
    )
    pushSuoritus(setExpanded)(tutkinnonOsa)
  }

  return (
    <span>
      {elementWithLoadingIndicator(
        osatP.map((lisättävätTutkinnonOsat) => {
          return (
            <div>
              <LisääRakenteeseenKuuluvaTutkinnonOsa
                {...{
                  addTutkinnonOsa,
                  lisättävätTutkinnonOsat,
                  koulutusmoduuliProto
                }}
              />
              <LisääOsaToisestaTutkinnosta
                {...{
                  addTutkinnonOsa,
                  lisättävätTutkinnonOsat,
                  suoritus,
                  koulutusmoduuliProto,
                  groupId,
                  diaarinumero
                }}
              />
              <LisääPaikallinenTutkinnonOsa
                {...{
                  lisättävätTutkinnonOsat,
                  addTutkinnonOsa,
                  paikallinenKoulutusmoduuli
                }}
              />
              <LisääKorkeakouluopintoSuoritus
                {...{
                  parentSuoritus: suoritus,
                  suoritusPrototypes,
                  addSuoritus: pushSuoritus(setExpanded),
                  groupTitles,
                  groupId
                }}
              />
              <LisääJatkoOpintovalmiuksiaTukevienOpintojenSuoritus
                {...{
                  parentSuoritus: suoritus,
                  suoritusPrototypes,
                  addSuoritus: pushSuoritus(setExpanded),
                  groupTitles,
                  groupId
                }}
              />
              <LisääYhteistenTutkinnonOsienOsaAlueidenTaiLukioOpintojenTaiMuidenOpintovalmiuksiaTukevienOpintojenOsasuoritus
                {...{
                  parentSuoritus: suoritus,
                  suoritusPrototypes,
                  setExpanded
                }}
              />
            </div>
          )
        })
      )}
    </span>
  )
}

export const createTutkinnonOsa = (
  suoritusPrototype,
  koulutusmoduuli,
  tutkinto,
  groupId,
  groupTitles,
  liittyyTutkinnonOsaan
) => {
  let tutkinnonOsa = modelSet(
    suoritusPrototype,
    koulutusmoduuli,
    'koulutusmoduuli'
  )
  if (groupId) {
    tutkinnonOsa = modelSetValue(
      tutkinnonOsa,
      toKoodistoEnumValue(
        'ammatillisentutkinnonosanryhma',
        groupId,
        groupTitles[groupId]
      ),
      'tutkinnonOsanRyhmä'
    )
  }
  if (tutkinto && modelLookup(tutkinnonOsa, 'tutkinto')) {
    tutkinnonOsa = modelSetData(
      tutkinnonOsa,
      {
        tunniste: {
          koodiarvo: tutkinto.tutkintoKoodi,
          nimi: tutkinto.nimi,
          koodistoUri: 'koulutus'
        },
        perusteenDiaarinumero: tutkinto.diaarinumero
      },
      'tutkinto'
    )
  }

  if (
    liittyyTutkinnonOsaan &&
    modelLookup(tutkinnonOsa, 'liittyyTutkinnonOsaan')
  ) {
    tutkinnonOsa = modelSetData(
      tutkinnonOsa,
      liittyyTutkinnonOsaan.data,
      'liittyyTutkinnonOsaan'
    )
  }
  return tutkinnonOsa
}

export const pushSuoritus = (setExpanded) => (uusiSuoritus) => {
  pushModel(ensureArrayKey(uusiSuoritus))
  setExpanded(uusiSuoritus)(true)
}

export const valtakunnallisetKoulutusmoduuliPrototypes = (suoritusPrototype) =>
  koulutusModuuliprototypes(suoritusPrototype).filter(
    R.complement(isPaikallinen)
  )

const canAddPaikallinen = (suoritus) =>
  !isJatkoOpintovalmiuksiaTukevienOpintojenSuoritus(suoritus)
