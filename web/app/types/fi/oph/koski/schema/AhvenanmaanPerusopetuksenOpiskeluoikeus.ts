import { Koodistokoodiviite } from './Koodistokoodiviite'
import { LocalizedString } from './LocalizedString'
import { OpiskeluoikeudenOrganisaatiohistoria } from './OpiskeluoikeudenOrganisaatiohistoria'
import { SisältäväOpiskeluoikeus } from './SisaltavaOpiskeluoikeus'
import { AhvenanmaanPerusopetuksenPäätasonSuoritus } from './AhvenanmaanPerusopetuksenPaatasonSuoritus'
import { LähdejärjestelmäId } from './LahdejarjestelmaId'
import { LähdejärjestelmäkytkennänPurkaminen } from './LahdejarjestelmakytkennanPurkaminen'
import { Oppilaitos } from './Oppilaitos'
import { AhvenanmaanPerusopetuksenOpiskeluoikeudenTila } from './AhvenanmaanPerusopetuksenOpiskeluoikeudenTila'
import { Koulutustoimija } from './Koulutustoimija'
import { AhvenanmaanPerusopetuksenOpiskeluoikeudenLisätiedot } from './AhvenanmaanPerusopetuksenOpiskeluoikeudenLisatiedot'

/**
 * Ahvenanmaan perusopetuksen opiskeluoikeus
 *
 * @see `fi.oph.koski.schema.AhvenanmaanPerusopetuksenOpiskeluoikeus`
 */
export type AhvenanmaanPerusopetuksenOpiskeluoikeus = {
  $class: 'fi.oph.koski.schema.AhvenanmaanPerusopetuksenOpiskeluoikeus'
  tyyppi: Koodistokoodiviite<'opiskeluoikeudentyyppi', 'ahvenanmaanperusopetus'>
  organisaatiohistoria?: Array<OpiskeluoikeudenOrganisaatiohistoria>
  sisältyyOpiskeluoikeuteen?: SisältäväOpiskeluoikeus
  oid?: string
  versionumero?: number
  suoritukset: Array<AhvenanmaanPerusopetuksenPäätasonSuoritus>
  aikaleima?: string
  päättymispäivä?: string
  lähdejärjestelmänId?: LähdejärjestelmäId
  lähdejärjestelmäkytkentäPurettu?: LähdejärjestelmäkytkennänPurkaminen
  oppilaitos?: Oppilaitos
  tila: AhvenanmaanPerusopetuksenOpiskeluoikeudenTila
  alkamispäivä?: string
  koulutustoimija?: Koulutustoimija
  lisätiedot?: AhvenanmaanPerusopetuksenOpiskeluoikeudenLisätiedot
}

export const AhvenanmaanPerusopetuksenOpiskeluoikeus = (
  o: {
    tyyppi?: Koodistokoodiviite<
      'opiskeluoikeudentyyppi',
      'ahvenanmaanperusopetus'
    >
    organisaatiohistoria?: Array<OpiskeluoikeudenOrganisaatiohistoria>
    sisältyyOpiskeluoikeuteen?: SisältäväOpiskeluoikeus
    oid?: string
    versionumero?: number
    suoritukset?: Array<AhvenanmaanPerusopetuksenPäätasonSuoritus>
    aikaleima?: string
    päättymispäivä?: string
    lähdejärjestelmänId?: LähdejärjestelmäId
    lähdejärjestelmäkytkentäPurettu?: LähdejärjestelmäkytkennänPurkaminen
    oppilaitos?: Oppilaitos
    tila?: AhvenanmaanPerusopetuksenOpiskeluoikeudenTila
    alkamispäivä?: string
    koulutustoimija?: Koulutustoimija
    lisätiedot?: AhvenanmaanPerusopetuksenOpiskeluoikeudenLisätiedot
  } = {}
): AhvenanmaanPerusopetuksenOpiskeluoikeus => ({
  tyyppi: Koodistokoodiviite({
    koodiarvo: 'ahvenanmaanperusopetus',
    koodistoUri: 'opiskeluoikeudentyyppi'
  }),
  suoritukset: [],
  $class: 'fi.oph.koski.schema.AhvenanmaanPerusopetuksenOpiskeluoikeus',
  tila: AhvenanmaanPerusopetuksenOpiskeluoikeudenTila({
    opiskeluoikeusjaksot: []
  }),
  ...o
})

AhvenanmaanPerusopetuksenOpiskeluoikeus.className =
  'fi.oph.koski.schema.AhvenanmaanPerusopetuksenOpiskeluoikeus' as const

export const isAhvenanmaanPerusopetuksenOpiskeluoikeus = (
  a: any
): a is AhvenanmaanPerusopetuksenOpiskeluoikeus =>
  a?.$class === 'fi.oph.koski.schema.AhvenanmaanPerusopetuksenOpiskeluoikeus'
