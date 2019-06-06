import {contextualizeSubModel, modelItems, oneOfPrototypes, wrapOptional} from '../editor/EditorModel'
import * as R from 'ramda'
import Http from '../util/http'
import {isKieliaine, isÄidinkieli} from '../suoritus/Koulutusmoduuli'
import {parseLocation} from '../util/location'

export const placeholderForNonGrouped = '999999'

export const createTutkinnonOsanSuoritusPrototype = (osasuoritukset, groupId) =>
  selectTutkinnonOsanSuoritusPrototype(tutkinnonOsaPrototypes(osasuoritukset), groupId)

export const selectTutkinnonOsanSuoritusPrototype = (prototypes, groupId) => {
  const preferredClass = groupId === '2' ? 'yhteisenammatillisentutkinnonosansuoritus' : 'muunammatillisentutkinnonosansuoritus'
  const sortValue = (oneOfProto) => oneOfProto.value.classes.includes(preferredClass) ? 0 : 1
  return prototypes.sort((a, b) => sortValue(a) - sortValue(b))[0]
}

export const tutkinnonOsaPrototypes = osasuorituksetModel => {
  const osasuoritukset = wrapOptional(osasuorituksetModel)
  const newItemIndex = modelItems(osasuoritukset).length
  const suoritusProto = contextualizeSubModel(osasuoritukset.arrayPrototype, osasuoritukset, newItemIndex)
  // TODO: onlyWhen is wrongly copied from implementing case class to traits prototype. This should really be fixed in the backend.
  const alts = oneOfPrototypes(R.dissoc('onlyWhen', suoritusProto))
  return alts.map(alt => contextualizeSubModel(alt, osasuoritukset, newItemIndex))
}

export const fetchLisättävätTutkinnonOsat = (diaarinumero, suoritustapa, groupId) => {
  return Http.cachedGet(parseLocation(`/koski/api/tutkinnonperusteet/tutkinnonosat/${encodeURIComponent(diaarinumero)}`).addQueryParams({
    suoritustapa: suoritustapa,
    tutkinnonOsanRyhmä: groupId != placeholderForNonGrouped ? groupId : undefined
  }))
}

export const osanOsa = m => m && m.value.classes.includes('ammatillisentutkinnonosanosaalue')

export const isYhteinenTutkinnonOsa = suoritus => suoritus.value.classes.includes('yhteisenammatillisentutkinnonosansuoritus')

export const isOsittaisenAmmatillisenTutkinnonYhteisenTutkinnonOsanSuoritus = s => s.value.classes.includes('yhteisenosittaisenammatillisentutkinnontutkinnonosansuoritus')

export const isOsittaisenAmmatillisenTutkinnonMuunTutkinnonOsanSuoritus = s => s.value.classes.includes('muunosittaisenammatillisentutkinnontutkinnonosansuoritus')

const muutKieliaineet = ['TK1', 'VK', 'VVAI', 'VVTK', 'VVVK']
const äidinkieli = 'AI'
const kieliAineet = [äidinkieli, ...muutKieliaineet]

export const isAmmatillisenKieliaine = koodiarvo => kieliAineet.includes(koodiarvo)

export const tutkinnonOsanOsaAlueenKoulutusmoduuli = (koulutusmoduulit, oppiaine) => {
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
