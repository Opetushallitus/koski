import {modelData} from './EditorModel'

export const isToimintaAlueittain = (model) => !!modelData(model.context.opiskeluoikeus, 'lisätiedot.erityisenTuenPäätös.opiskeleeToimintaAlueittain')
export const isYsiluokka = (suoritus) => {
  let tunniste = modelData(suoritus, 'koulutusmoduuli.tunniste')
  return tunniste.koodistoUri == 'perusopetuksenluokkaaste' && tunniste.koodiarvo == '9'
}
export const isPerusopetuksenOppimäärä = (suoritus) => {
  let tunniste = modelData(suoritus, 'koulutusmoduuli.tunniste')
  return tunniste.koodistoUri == 'koulutus' && tunniste.koodiarvo == '201101'
}
export const jääLuokalle = (suoritus) => modelData(suoritus, 'jääLuokalle')
