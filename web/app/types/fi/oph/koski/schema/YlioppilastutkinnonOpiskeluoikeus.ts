import { Koodistokoodiviite } from './Koodistokoodiviite'
import { LocalizedString } from './LocalizedString'
import { YlioppilastutkinnonOpiskeluoikeudenTila } from './YlioppilastutkinnonOpiskeluoikeudenTila'
import { Koulutustoimija } from './Koulutustoimija'
import { YlioppilastutkinnonOpiskeluoikeudenLisätiedot } from './YlioppilastutkinnonOpiskeluoikeudenLisatiedot'
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
  oid?: string
  koulutustoimija?: Koulutustoimija
  lisätiedot?: YlioppilastutkinnonOpiskeluoikeudenLisätiedot
  versionumero?: number
  suoritukset: Array<YlioppilastutkinnonSuoritus>
  aikaleima?: string
  päättymispäivä?: string
  lähdejärjestelmänId?: LähdejärjestelmäId
  oppilaitosSuorituspäivänä?: Oppilaitos
  oppilaitos?: Oppilaitos
}

export const YlioppilastutkinnonOpiskeluoikeus = (
  o: {
    tyyppi?: Koodistokoodiviite<'opiskeluoikeudentyyppi', 'ylioppilastutkinto'>
    tila?: YlioppilastutkinnonOpiskeluoikeudenTila
    alkamispäivä?: string
    oid?: string
    koulutustoimija?: Koulutustoimija
    lisätiedot?: YlioppilastutkinnonOpiskeluoikeudenLisätiedot
    versionumero?: number
    suoritukset?: Array<YlioppilastutkinnonSuoritus>
    aikaleima?: string
    päättymispäivä?: string
    lähdejärjestelmänId?: LähdejärjestelmäId
    oppilaitosSuorituspäivänä?: Oppilaitos
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

YlioppilastutkinnonOpiskeluoikeus.className =
  'fi.oph.koski.schema.YlioppilastutkinnonOpiskeluoikeus' as const

export const isYlioppilastutkinnonOpiskeluoikeus = (
  a: any
): a is YlioppilastutkinnonOpiskeluoikeus =>
  a?.$class === 'fi.oph.koski.schema.YlioppilastutkinnonOpiskeluoikeus'
