import { Koodistokoodiviite } from './Koodistokoodiviite'
import { LocalizedString } from './LocalizedString'
import { YlioppilastutkinnonSuoritus } from './YlioppilastutkinnonSuoritus'
import { LähdejärjestelmäId } from './LahdejarjestelmaId'
import { Oppilaitos } from './Oppilaitos'
import { LähdejärjestelmäkytkennänPurkaminen } from './LahdejarjestelmakytkennanPurkaminen'
import { YlioppilastutkinnonOpiskeluoikeudenTila } from './YlioppilastutkinnonOpiskeluoikeudenTila'
import { Koulutustoimija } from './Koulutustoimija'
import { YlioppilastutkinnonOpiskeluoikeudenLisätiedot } from './YlioppilastutkinnonOpiskeluoikeudenLisatiedot'

/**
 * YlioppilastutkinnonOpiskeluoikeus
 *
 * @see `fi.oph.koski.schema.YlioppilastutkinnonOpiskeluoikeus`
 */
export type YlioppilastutkinnonOpiskeluoikeus = {
  $class: 'fi.oph.koski.schema.YlioppilastutkinnonOpiskeluoikeus'
  tyyppi: Koodistokoodiviite<'opiskeluoikeudentyyppi', 'ylioppilastutkinto'>
  oid?: string
  versionumero?: number
  suoritukset: Array<YlioppilastutkinnonSuoritus>
  aikaleima?: string
  päättymispäivä?: string
  lähdejärjestelmänId?: LähdejärjestelmäId
  oppilaitosSuorituspäivänä?: Oppilaitos
  lähdejärjestelmäkytkentäPurettu?: LähdejärjestelmäkytkennänPurkaminen
  oppilaitos?: Oppilaitos
  tila: YlioppilastutkinnonOpiskeluoikeudenTila
  alkamispäivä?: string
  koulutustoimija?: Koulutustoimija
  lisätiedot?: YlioppilastutkinnonOpiskeluoikeudenLisätiedot
}

export const YlioppilastutkinnonOpiskeluoikeus = (
  o: {
    tyyppi?: Koodistokoodiviite<'opiskeluoikeudentyyppi', 'ylioppilastutkinto'>
    oid?: string
    versionumero?: number
    suoritukset?: Array<YlioppilastutkinnonSuoritus>
    aikaleima?: string
    päättymispäivä?: string
    lähdejärjestelmänId?: LähdejärjestelmäId
    oppilaitosSuorituspäivänä?: Oppilaitos
    lähdejärjestelmäkytkentäPurettu?: LähdejärjestelmäkytkennänPurkaminen
    oppilaitos?: Oppilaitos
    tila?: YlioppilastutkinnonOpiskeluoikeudenTila
    alkamispäivä?: string
    koulutustoimija?: Koulutustoimija
    lisätiedot?: YlioppilastutkinnonOpiskeluoikeudenLisätiedot
  } = {}
): YlioppilastutkinnonOpiskeluoikeus => ({
  tyyppi: Koodistokoodiviite({
    koodiarvo: 'ylioppilastutkinto',
    koodistoUri: 'opiskeluoikeudentyyppi'
  }),
  suoritukset: [],
  $class: 'fi.oph.koski.schema.YlioppilastutkinnonOpiskeluoikeus',
  tila: YlioppilastutkinnonOpiskeluoikeudenTila({ opiskeluoikeusjaksot: [] }),
  ...o
})

YlioppilastutkinnonOpiskeluoikeus.className =
  'fi.oph.koski.schema.YlioppilastutkinnonOpiskeluoikeus' as const

export const isYlioppilastutkinnonOpiskeluoikeus = (
  a: any
): a is YlioppilastutkinnonOpiskeluoikeus =>
  a?.$class === 'fi.oph.koski.schema.YlioppilastutkinnonOpiskeluoikeus'
