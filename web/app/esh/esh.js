import { modelData, modelItems } from '../editor/EditorModel'
import Bacon from 'baconjs'
import { arvioituTaiVahvistettu, suorituksenTyyppi } from '../suoritus/Suoritus'
import Http from '../util/http'

export const eshSallitutRahoituskoodiarvot = ['6']

// TODO: Siivoa
export const isToimintaAlueittain = (suoritus) =>
  suoritus
    ? !!modelData(
        suoritus.context.opiskeluoikeus,
        'lisätiedot.erityisenTuenPäätös.opiskeleeToimintaAlueittain'
      ) ||
      modelItems(
        suoritus.context.opiskeluoikeus,
        'lisätiedot.erityisenTuenPäätökset'
      ).some((etp) => modelData(etp, 'opiskeleeToimintaAlueittain'))
    : false

export const jääLuokalle = (suoritus) => modelData(suoritus, 'jääLuokalle')


export const isYksilöllistetty = (suoritus) =>
  modelData(suoritus, 'yksilöllistettyOppimäärä')
export const isPainotettu = (suoritus) =>
  modelData(suoritus, 'painotettuOpetus')
export const isKorotus = (suoritus) => modelData(suoritus, 'korotus')

export const luokkaAsteenOsasuoritukset = (luokkaAste_, toimintaAlueittain) =>
  Http.cachedGet(
    `/koski/api/editor/koodit/europeanschoolofhelsinkiluokkaaste/${luokkaAste_}/suoritukset/prefill`
  )

export const oppimääränOsasuoritukset = (
  suoritustyyppi,
  toimintaAlueittain = false
) =>
  suoritustyyppi
    ? Http.cachedGet(
        `/koski/api/editor/koodit/koulutus/201101/suoritukset/prefill?tyyppi=${suoritustyyppi.koodiarvo}&toimintaAlueittain=${toimintaAlueittain}`
      )
    : Bacon.constant([])

const YksilöllistettyFootnote = {
  title: 'Yksilöllistetty oppimäärä',
  hint: '*'
}
const PainotettuFootnote = { title: 'Painotettu opetus', hint: '**' }
const KorotusFootnote = {
  title: 'Perusopetuksen päättötodistuksen arvosanan korotus',
  hint: '†'
}

export const footnoteDescriptions = (oppiaineSuoritukset) =>
  [
    oppiaineSuoritukset.find(isYksilöllistetty) && YksilöllistettyFootnote,
    oppiaineSuoritukset.find(isPainotettu) && PainotettuFootnote,
    oppiaineSuoritukset.find(isKorotus) && KorotusFootnote
  ].filter((v) => !!v)

export const footnotesForSuoritus = (suoritus) =>
  [
    isYksilöllistetty(suoritus) && YksilöllistettyFootnote,
    isPainotettu(suoritus) && PainotettuFootnote,
    isKorotus(suoritus) && KorotusFootnote
  ].filter((v) => !!v)

export const valmiitaSuorituksia = (oppiaineSuoritukset) =>
  oppiaineSuoritukset.some(
    (oppiaine) =>
      arvioituTaiVahvistettu(oppiaine) ||
      modelItems(oppiaine, 'osasuoritukset').some(arvioituTaiVahvistettu)
  )

export const pakollisetTitle = 'Pakolliset oppiaineet'
export const valinnaisetTitle = 'Valinnaiset oppiaineet'
export const groupTitleForSuoritus = (suoritus) =>
  modelData(suoritus).koulutusmoduuli.pakollinen
    ? pakollisetTitle
    : valinnaisetTitle

export const isNuortenPerusopetuksenOppiaineenSuoritusValmistavassaOpetuksessa =
  (model) =>
    model.value.classes.includes(
      'nuortenperusopetuksenoppiaineensuoritusvalmistavassaopetuksessa'
    )

export const isPerusopetukseenValmistavanKoulutuksenSuoritus = (model) =>
  model.value.classes.includes('perusopetukseenvalmistavanopetuksensuoritus')
