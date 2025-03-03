import { Koodistokoodiviite } from './Koodistokoodiviite'
import { LocalizedString } from './LocalizedString'
import { KielitutkinnonOpiskeluoikeudenTila } from './KielitutkinnonOpiskeluoikeudenTila'
import { Koulutustoimija } from './Koulutustoimija'
import { KielitutkinnonPäätasonSuoritus } from './KielitutkinnonPaatasonSuoritus'
import { LähdejärjestelmäId } from './LahdejarjestelmaId'
import { LähdejärjestelmäkytkennänPurkaminen } from './LahdejarjestelmakytkennanPurkaminen'
import { Oppilaitos } from './Oppilaitos'

/**
 * KielitutkinnonOpiskeluoikeus
 *
 * @see `fi.oph.koski.schema.KielitutkinnonOpiskeluoikeus`
 */
export type KielitutkinnonOpiskeluoikeus = {
  $class: 'fi.oph.koski.schema.KielitutkinnonOpiskeluoikeus'
  tyyppi: Koodistokoodiviite<'opiskeluoikeudentyyppi', 'kielitutkinto'>
  tila: KielitutkinnonOpiskeluoikeudenTila
  alkamispäivä?: string
  oid?: string
  koulutustoimija?: Koulutustoimija
  versionumero?: number
  suoritukset: Array<KielitutkinnonPäätasonSuoritus>
  aikaleima?: string
  päättymispäivä?: string
  lähdejärjestelmänId?: LähdejärjestelmäId
  lähdejärjestelmäkytkentäPurettu?: LähdejärjestelmäkytkennänPurkaminen
  oppilaitos?: Oppilaitos
}

export const KielitutkinnonOpiskeluoikeus = (
  o: {
    tyyppi?: Koodistokoodiviite<'opiskeluoikeudentyyppi', 'kielitutkinto'>
    tila?: KielitutkinnonOpiskeluoikeudenTila
    alkamispäivä?: string
    oid?: string
    koulutustoimija?: Koulutustoimija
    versionumero?: number
    suoritukset?: Array<KielitutkinnonPäätasonSuoritus>
    aikaleima?: string
    päättymispäivä?: string
    lähdejärjestelmänId?: LähdejärjestelmäId
    lähdejärjestelmäkytkentäPurettu?: LähdejärjestelmäkytkennänPurkaminen
    oppilaitos?: Oppilaitos
  } = {}
): KielitutkinnonOpiskeluoikeus => ({
  tyyppi: Koodistokoodiviite({
    koodiarvo: 'kielitutkinto',
    koodistoUri: 'opiskeluoikeudentyyppi'
  }),
  tila: KielitutkinnonOpiskeluoikeudenTila({ opiskeluoikeusjaksot: [] }),
  suoritukset: [],
  $class: 'fi.oph.koski.schema.KielitutkinnonOpiskeluoikeus',
  ...o
})

KielitutkinnonOpiskeluoikeus.className =
  'fi.oph.koski.schema.KielitutkinnonOpiskeluoikeus' as const

export const isKielitutkinnonOpiskeluoikeus = (
  a: any
): a is KielitutkinnonOpiskeluoikeus =>
  a?.$class === 'fi.oph.koski.schema.KielitutkinnonOpiskeluoikeus'
