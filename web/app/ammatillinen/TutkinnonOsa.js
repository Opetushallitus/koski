import {
  contextualizeSubModel,
  modelItems,
  oneOfPrototypes,
  wrapOptional
} from '../editor/EditorModel'
import * as R from 'ramda'
import Http from '../util/http'
import { isKieliaine, isÄidinkieli } from '../suoritus/Koulutusmoduuli'
import { parseLocation } from '../util/location'

export const NON_GROUPED = '999999'
export const YHTEISET_TUTKINNON_OSAT = '2'

export const createTutkinnonOsanSuoritusPrototype = (osasuoritukset, groupId) =>
  selectTutkinnonOsanSuoritusPrototype(
    tutkinnonOsaPrototypes(osasuoritukset),
    groupId
  )

export const selectTutkinnonOsanSuoritusPrototype = (
  prototypes,
  groupId,
  preferedclass
) => {
  const preferredClass =
    preferedclass ||
    (groupId === YHTEISET_TUTKINNON_OSAT
      ? 'yhteisenammatillisentutkinnonosansuoritus'
      : 'muunammatillisentutkinnonosansuoritus')
  const sortValue = (oneOfProto) =>
    oneOfProto.value.classes.includes(preferredClass) ? 0 : 1
  return prototypes.sort((a, b) => sortValue(a) - sortValue(b))[0]
}

export const tutkinnonOsaPrototypes = (osasuorituksetModel) => {
  const osasuoritukset = wrapOptional(osasuorituksetModel)
  const newItemIndex = modelItems(osasuoritukset).length
  const suoritusProto = contextualizeSubModel(
    osasuoritukset.arrayPrototype,
    osasuoritukset,
    newItemIndex
  )
  // TODO: onlyWhen is wrongly copied from implementing case class to traits prototype. This should really be fixed in the backend.
  const alts = oneOfPrototypes(R.dissoc('onlyWhen', suoritusProto))
  return alts.map((alt) =>
    contextualizeSubModel(alt, osasuoritukset, newItemIndex)
  )
}

export const fetchLisättävätTutkinnonOsat = (
  diaarinumero,
  suoritustapa,
  groupId
) => {
  return Http.cachedGet(
    parseLocation(
      `/koski/api/tutkinnonperusteet/tutkinnonosat/${encodeURIComponent(
        diaarinumero
      )}`
    ).addQueryParams({
      suoritustapa,
      tutkinnonOsanRyhmä: groupId !== NON_GROUPED ? groupId : undefined
    })
  )
}

export const osanOsa = (m) =>
  m && m.value.classes.includes('ammatillisentutkinnonosanosaalue')

export const isYhteinenTutkinnonOsa = (suoritus) =>
  suoritus.value.classes.includes('yhteisenammatillisentutkinnonosansuoritus')

export const isYhteinenTutkinnonOsanOsa = (suoritus) =>
  suoritus.value.classes.includes('yhteisentutkinnonosanosaalueensuoritus')

export const isLukioOpintojenSuoritus = (suoritus) =>
  suoritus.value.classes.includes('lukioopintojensuoritus')

export const isMuidenOpintovalmiuksiaTukevienOpintojenSuoritus = (suoritus) =>
  suoritus.value.classes.includes(
    'muidenopintovalmiuksiatukevienopintojensuoritus'
  )

export const isOsittaisenAmmatillisenTutkinnonYhteisenTutkinnonOsanSuoritus = (
  s
) =>
  s.value.classes.includes(
    'yhteisenosittaisenammatillisentutkinnontutkinnonosansuoritus'
  )

export const isOsittaisenAmmatillisenTutkinnonMuunTutkinnonOsanSuoritus = (s) =>
  s.value.classes.includes(
    'muunosittaisenammatillisentutkinnontutkinnonosansuoritus'
  )

export const isValinnanMahdollisuus = (suoritus) =>
  suoritus.value.classes.includes('valinnanmahdollisuus')
export const isKorkeakouluOpintosuoritus = (suoritus) =>
  suoritus.value.classes.includes('korkeakouluopintosuoritus')
export const isJatkoOpintovalmiuksiaTukevienOpintojenSuoritus = (suoritus) =>
  suoritus.value.classes.includes(
    'jatkoopintovalmiuksiatukevienopintojensuoritus'
  )
export const isVälisuoritus = (suoritus) =>
  suoritus.value.classes.includes('valisuoritus')
export const isKorkeakouluOpintojenTutkinnonOsaaPienempiKokonaisuus = (km) =>
  km &&
  km.value.classes.includes(
    'korkeakouluopintojentutkinnonosaapienempikokonaisuus'
  )
export const isYlioppilastutkinnonKokeenSuoritus = (suoritus) =>
  suoritus.value.classes.includes('ylioppilastutkinnonkokeensuoritus')

const muutKieliaineet = ['TK1', 'VK', 'VVAI', 'VVAI22', 'VVTK', 'VVVK']
const äidinkieli = 'AI'
const kieliAineet = [äidinkieli, ...muutKieliaineet]

export const isAmmatillisenKieliaine = (koodiarvo) =>
  kieliAineet.includes(koodiarvo)

export const tutkinnonOsanOsaAlueenKoulutusmoduuli = (
  koulutusmoduulit,
  oppiaine
) => {
  if (!isAmmatillisenKieliaine(oppiaine.koodiarvo)) {
    return koulutusmoduulit.find(R.complement(isKieliaine))
  }
  if (oppiaine.koodiarvo === äidinkieli) {
    return koulutusmoduulit.find(isÄidinkieli)
  }
  if (muutKieliaineet.includes(oppiaine.koodiarvo)) {
    return koulutusmoduulit.find(isKieliaine)
  }
}
