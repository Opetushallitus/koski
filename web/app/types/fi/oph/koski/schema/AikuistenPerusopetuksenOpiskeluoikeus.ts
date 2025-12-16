import { Koodistokoodiviite } from './Koodistokoodiviite'
import { LocalizedString } from './LocalizedString'
import { OpiskeluoikeudenOrganisaatiohistoria } from './OpiskeluoikeudenOrganisaatiohistoria'
import { SisältäväOpiskeluoikeus } from './SisaltavaOpiskeluoikeus'
import { AikuistenPerusopetuksenPäätasonSuoritus } from './AikuistenPerusopetuksenPaatasonSuoritus'
import { LähdejärjestelmäId } from './LahdejarjestelmaId'
import { LähdejärjestelmäkytkennänPurkaminen } from './LahdejarjestelmakytkennanPurkaminen'
import { Oppilaitos } from './Oppilaitos'
import { AikuistenPerusopetuksenOpiskeluoikeudenTila } from './AikuistenPerusopetuksenOpiskeluoikeudenTila'
import { Koulutustoimija } from './Koulutustoimija'
import { AikuistenPerusopetuksenOpiskeluoikeudenLisätiedot } from './AikuistenPerusopetuksenOpiskeluoikeudenLisatiedot'

/**
 * Aikuisten perusopetuksen opiskeluoikeus
 *
 * @see `fi.oph.koski.schema.AikuistenPerusopetuksenOpiskeluoikeus`
 */
export type AikuistenPerusopetuksenOpiskeluoikeus = {
  $class: 'fi.oph.koski.schema.AikuistenPerusopetuksenOpiskeluoikeus'
  tyyppi: Koodistokoodiviite<'opiskeluoikeudentyyppi', 'aikuistenperusopetus'>
  organisaatiohistoria?: Array<OpiskeluoikeudenOrganisaatiohistoria>
  sisältyyOpiskeluoikeuteen?: SisältäväOpiskeluoikeus
  oid?: string
  versionumero?: number
  suoritukset: Array<AikuistenPerusopetuksenPäätasonSuoritus>
  aikaleima?: string
  päättymispäivä?: string
  lähdejärjestelmänId?: LähdejärjestelmäId
  lähdejärjestelmäkytkentäPurettu?: LähdejärjestelmäkytkennänPurkaminen
  oppilaitos?: Oppilaitos
  tila: AikuistenPerusopetuksenOpiskeluoikeudenTila
  alkamispäivä?: string
  koulutustoimija?: Koulutustoimija
  lisätiedot?: AikuistenPerusopetuksenOpiskeluoikeudenLisätiedot
}

export const AikuistenPerusopetuksenOpiskeluoikeus = (
  o: {
    tyyppi?: Koodistokoodiviite<
      'opiskeluoikeudentyyppi',
      'aikuistenperusopetus'
    >
    organisaatiohistoria?: Array<OpiskeluoikeudenOrganisaatiohistoria>
    sisältyyOpiskeluoikeuteen?: SisältäväOpiskeluoikeus
    oid?: string
    versionumero?: number
    suoritukset?: Array<AikuistenPerusopetuksenPäätasonSuoritus>
    aikaleima?: string
    päättymispäivä?: string
    lähdejärjestelmänId?: LähdejärjestelmäId
    lähdejärjestelmäkytkentäPurettu?: LähdejärjestelmäkytkennänPurkaminen
    oppilaitos?: Oppilaitos
    tila?: AikuistenPerusopetuksenOpiskeluoikeudenTila
    alkamispäivä?: string
    koulutustoimija?: Koulutustoimija
    lisätiedot?: AikuistenPerusopetuksenOpiskeluoikeudenLisätiedot
  } = {}
): AikuistenPerusopetuksenOpiskeluoikeus => ({
  tyyppi: Koodistokoodiviite({
    koodiarvo: 'aikuistenperusopetus',
    koodistoUri: 'opiskeluoikeudentyyppi'
  }),
  suoritukset: [],
  $class: 'fi.oph.koski.schema.AikuistenPerusopetuksenOpiskeluoikeus',
  tila: AikuistenPerusopetuksenOpiskeluoikeudenTila({
    opiskeluoikeusjaksot: []
  }),
  ...o
})

AikuistenPerusopetuksenOpiskeluoikeus.className =
  'fi.oph.koski.schema.AikuistenPerusopetuksenOpiskeluoikeus' as const

export const isAikuistenPerusopetuksenOpiskeluoikeus = (
  a: any
): a is AikuistenPerusopetuksenOpiskeluoikeus =>
  a?.$class === 'fi.oph.koski.schema.AikuistenPerusopetuksenOpiskeluoikeus'
