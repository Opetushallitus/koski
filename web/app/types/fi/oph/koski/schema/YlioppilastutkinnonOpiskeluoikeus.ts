import { Koodistokoodiviite } from './Koodistokoodiviite'
import { LocalizedString } from './LocalizedString'
import { YlioppilastutkinnonOpiskeluoikeudenTila } from './YlioppilastutkinnonOpiskeluoikeudenTila'
import { Koulutustoimija } from './Koulutustoimija'
import { YlioppilastutkinnonSuoritus } from './YlioppilastutkinnonSuoritus'
import { LähdejärjestelmäId } from './LahdejarjestelmaId'
import { Oppilaitos } from './Oppilaitos'

/**
 * YlioppilastutkinnonOpiskeluoikeus
 *
 * @see `fi.oph.koski.schema.YlioppilastutkinnonOpiskeluoikeus`
 */
export type YlioppilastutkinnonOpiskeluoikeus = {
  $class: 'fi.oph.koski.schema.YlioppilastutkinnonOpiskeluoikeus'
  tyyppi: Koodistokoodiviite<'opiskeluoikeudentyyppi', 'ylioppilastutkinto'>
  tila: YlioppilastutkinnonOpiskeluoikeudenTila
  alkamispäivä?: string
  koulutustoimija?: Koulutustoimija
  suoritukset: Array<YlioppilastutkinnonSuoritus>
  päättymispäivä?: string
  lähdejärjestelmänId?: LähdejärjestelmäId
  oppilaitos?: Oppilaitos
}

export const YlioppilastutkinnonOpiskeluoikeus = (
  o: {
    tyyppi?: Koodistokoodiviite<'opiskeluoikeudentyyppi', 'ylioppilastutkinto'>
    tila?: YlioppilastutkinnonOpiskeluoikeudenTila
    alkamispäivä?: string
    koulutustoimija?: Koulutustoimija
    suoritukset?: Array<YlioppilastutkinnonSuoritus>
    päättymispäivä?: string
    lähdejärjestelmänId?: LähdejärjestelmäId
    oppilaitos?: Oppilaitos
  } = {}
): YlioppilastutkinnonOpiskeluoikeus => ({
  tyyppi: Koodistokoodiviite({
    koodiarvo: 'ylioppilastutkinto',
    koodistoUri: 'opiskeluoikeudentyyppi'
  }),
  tila: YlioppilastutkinnonOpiskeluoikeudenTila({ opiskeluoikeusjaksot: [] }),
  suoritukset: [],
  $class: 'fi.oph.koski.schema.YlioppilastutkinnonOpiskeluoikeus',
  ...o
})

export const isYlioppilastutkinnonOpiskeluoikeus = (
  a: any
): a is YlioppilastutkinnonOpiskeluoikeus =>
  a?.$class === 'YlioppilastutkinnonOpiskeluoikeus'
