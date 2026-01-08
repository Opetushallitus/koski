import { Koodistokoodiviite } from './Koodistokoodiviite'
import { LocalizedString } from './LocalizedString'
import { Koulutustoimija } from './Koulutustoimija'
import { KielitutkinnonPäätasonSuoritus } from './KielitutkinnonPaatasonSuoritus'
import { LähdejärjestelmäId } from './LahdejarjestelmaId'
import { LähdejärjestelmäkytkennänPurkaminen } from './LahdejarjestelmakytkennanPurkaminen'
import { Oppilaitos } from './Oppilaitos'
import { KielitutkinnonOpiskeluoikeudenTila } from './KielitutkinnonOpiskeluoikeudenTila'

/**
 * KielitutkinnonOpiskeluoikeus
 *
 * @see `fi.oph.koski.schema.KielitutkinnonOpiskeluoikeus`
 */
export type KielitutkinnonOpiskeluoikeus = {
  $class: 'fi.oph.koski.schema.KielitutkinnonOpiskeluoikeus'
  tyyppi: Koodistokoodiviite<'opiskeluoikeudentyyppi', 'kielitutkinto'>
  oid?: string
  koulutustoimija?: Koulutustoimija
  versionumero?: number
  suoritukset: Array<KielitutkinnonPäätasonSuoritus>
  aikaleima?: string
  päättymispäivä?: string
  lähdejärjestelmänId?: LähdejärjestelmäId
  lähdejärjestelmäkytkentäPurettu?: LähdejärjestelmäkytkennänPurkaminen
  oppilaitos?: Oppilaitos
  tila: KielitutkinnonOpiskeluoikeudenTila
  alkamispäivä?: string
}

export const KielitutkinnonOpiskeluoikeus = (
  o: {
    tyyppi?: Koodistokoodiviite<'opiskeluoikeudentyyppi', 'kielitutkinto'>
    oid?: string
    koulutustoimija?: Koulutustoimija
    versionumero?: number
    suoritukset?: Array<KielitutkinnonPäätasonSuoritus>
    aikaleima?: string
    päättymispäivä?: string
    lähdejärjestelmänId?: LähdejärjestelmäId
    lähdejärjestelmäkytkentäPurettu?: LähdejärjestelmäkytkennänPurkaminen
    oppilaitos?: Oppilaitos
    tila?: KielitutkinnonOpiskeluoikeudenTila
    alkamispäivä?: string
  } = {}
): KielitutkinnonOpiskeluoikeus => ({
  tyyppi: Koodistokoodiviite({
    koodiarvo: 'kielitutkinto',
    koodistoUri: 'opiskeluoikeudentyyppi'
  }),
  suoritukset: [],
  $class: 'fi.oph.koski.schema.KielitutkinnonOpiskeluoikeus',
  tila: KielitutkinnonOpiskeluoikeudenTila({ opiskeluoikeusjaksot: [] }),
  ...o
})

KielitutkinnonOpiskeluoikeus.className =
  'fi.oph.koski.schema.KielitutkinnonOpiskeluoikeus' as const

export const isKielitutkinnonOpiskeluoikeus = (
  a: any
): a is KielitutkinnonOpiskeluoikeus =>
  a?.$class === 'fi.oph.koski.schema.KielitutkinnonOpiskeluoikeus'
