import { Koodistokoodiviite } from './Koodistokoodiviite'
import { LocalizedString } from './LocalizedString'
import { OpiskeluoikeudenOrganisaatiohistoria } from './OpiskeluoikeudenOrganisaatiohistoria'
import { SisältäväOpiskeluoikeus } from './SisaltavaOpiskeluoikeus'
import { AhvenanmaanAmmatillinenPäätasonSuoritus } from './AhvenanmaanAmmatillinenPaatasonSuoritus'
import { LähdejärjestelmäkytkennänPurkaminen } from './LahdejarjestelmakytkennanPurkaminen'
import { Oppilaitos } from './Oppilaitos'
import { AhvenanmaanAmmatillinenOpiskeluoikeudenTila } from './AhvenanmaanAmmatillinenOpiskeluoikeudenTila'
import { Koulutustoimija } from './Koulutustoimija'
import { AhvenanmaanAmmatillisenOpiskeluoikeudenLisätiedot } from './AhvenanmaanAmmatillisenOpiskeluoikeudenLisatiedot'
import { LähdejärjestelmäId } from './LahdejarjestelmaId'

/**
 * Ahvenanmaan ammatillisen koulutuksen opiskeluoikeus
 *
 * @see `fi.oph.koski.schema.AhvenanmaanAmmatillinenOpiskeluoikeus`
 */
export type AhvenanmaanAmmatillinenOpiskeluoikeus = {
  $class: 'fi.oph.koski.schema.AhvenanmaanAmmatillinenOpiskeluoikeus'
  tyyppi: Koodistokoodiviite<
    'opiskeluoikeudentyyppi',
    'ahvenanmaanammatillinenkoulutus'
  >
  organisaatiohistoria?: Array<OpiskeluoikeudenOrganisaatiohistoria>
  sisältyyOpiskeluoikeuteen?: SisältäväOpiskeluoikeus
  oid?: string
  versionumero?: number
  suoritukset: Array<AhvenanmaanAmmatillinenPäätasonSuoritus>
  aikaleima?: string
  päättymispäivä?: string
  ostettu: boolean
  lähdejärjestelmäkytkentäPurettu?: LähdejärjestelmäkytkennänPurkaminen
  oppilaitos?: Oppilaitos
  tila: AhvenanmaanAmmatillinenOpiskeluoikeudenTila
  alkamispäivä?: string
  koulutustoimija?: Koulutustoimija
  lisätiedot?: AhvenanmaanAmmatillisenOpiskeluoikeudenLisätiedot
  lähdejärjestelmänId?: LähdejärjestelmäId
  arvioituPäättymispäivä?: string
}

export const AhvenanmaanAmmatillinenOpiskeluoikeus = (
  o: {
    tyyppi?: Koodistokoodiviite<
      'opiskeluoikeudentyyppi',
      'ahvenanmaanammatillinenkoulutus'
    >
    organisaatiohistoria?: Array<OpiskeluoikeudenOrganisaatiohistoria>
    sisältyyOpiskeluoikeuteen?: SisältäväOpiskeluoikeus
    oid?: string
    versionumero?: number
    suoritukset?: Array<AhvenanmaanAmmatillinenPäätasonSuoritus>
    aikaleima?: string
    päättymispäivä?: string
    ostettu?: boolean
    lähdejärjestelmäkytkentäPurettu?: LähdejärjestelmäkytkennänPurkaminen
    oppilaitos?: Oppilaitos
    tila?: AhvenanmaanAmmatillinenOpiskeluoikeudenTila
    alkamispäivä?: string
    koulutustoimija?: Koulutustoimija
    lisätiedot?: AhvenanmaanAmmatillisenOpiskeluoikeudenLisätiedot
    lähdejärjestelmänId?: LähdejärjestelmäId
    arvioituPäättymispäivä?: string
  } = {}
): AhvenanmaanAmmatillinenOpiskeluoikeus => ({
  tyyppi: Koodistokoodiviite({
    koodiarvo: 'ahvenanmaanammatillinenkoulutus',
    koodistoUri: 'opiskeluoikeudentyyppi'
  }),
  suoritukset: [],
  tila: AhvenanmaanAmmatillinenOpiskeluoikeudenTila({
    opiskeluoikeusjaksot: []
  }),
  $class: 'fi.oph.koski.schema.AhvenanmaanAmmatillinenOpiskeluoikeus',
  ostettu: false,
  ...o
})

AhvenanmaanAmmatillinenOpiskeluoikeus.className =
  'fi.oph.koski.schema.AhvenanmaanAmmatillinenOpiskeluoikeus' as const

export const isAhvenanmaanAmmatillinenOpiskeluoikeus = (
  a: any
): a is AhvenanmaanAmmatillinenOpiskeluoikeus =>
  a?.$class === 'fi.oph.koski.schema.AhvenanmaanAmmatillinenOpiskeluoikeus'
