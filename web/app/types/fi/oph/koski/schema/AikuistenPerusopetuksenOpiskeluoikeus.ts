import { Koodistokoodiviite } from './Koodistokoodiviite'
import { LocalizedString } from './LocalizedString'
import { AikuistenPerusopetuksenOpiskeluoikeudenTila } from './AikuistenPerusopetuksenOpiskeluoikeudenTila'
import { OpiskeluoikeudenOrganisaatiohistoria } from './OpiskeluoikeudenOrganisaatiohistoria'
import { SisältäväOpiskeluoikeus } from './SisaltavaOpiskeluoikeus'
import { Koulutustoimija } from './Koulutustoimija'
import { AikuistenPerusopetuksenOpiskeluoikeudenLisätiedot } from './AikuistenPerusopetuksenOpiskeluoikeudenLisatiedot'
import { AikuistenPerusopetuksenPäätasonSuoritus } from './AikuistenPerusopetuksenPaatasonSuoritus'
import { LähdejärjestelmäId } from './LahdejarjestelmaId'
import { LähdejärjestelmäkytkennänPurkaminen } from './LahdejarjestelmakytkennanPurkaminen'
import { Oppilaitos } from './Oppilaitos'

/**
 * Aikuisten perusopetuksen opiskeluoikeus
 *
 * @see `fi.oph.koski.schema.AikuistenPerusopetuksenOpiskeluoikeus`
 */
export type AikuistenPerusopetuksenOpiskeluoikeus = {
  $class: 'fi.oph.koski.schema.AikuistenPerusopetuksenOpiskeluoikeus'
  tyyppi: Koodistokoodiviite<'opiskeluoikeudentyyppi', 'aikuistenperusopetus'>
  tila: AikuistenPerusopetuksenOpiskeluoikeudenTila
  alkamispäivä?: string
  organisaatiohistoria?: Array<OpiskeluoikeudenOrganisaatiohistoria>
  sisältyyOpiskeluoikeuteen?: SisältäväOpiskeluoikeus
  oid?: string
  koulutustoimija?: Koulutustoimija
  lisätiedot?: AikuistenPerusopetuksenOpiskeluoikeudenLisätiedot
  versionumero?: number
  suoritukset: Array<AikuistenPerusopetuksenPäätasonSuoritus>
  aikaleima?: string
  päättymispäivä?: string
  lähdejärjestelmänId?: LähdejärjestelmäId
  lähdejärjestelmäkytkentäPurettu?: LähdejärjestelmäkytkennänPurkaminen
  oppilaitos?: Oppilaitos
}

export const AikuistenPerusopetuksenOpiskeluoikeus = (
  o: {
    tyyppi?: Koodistokoodiviite<
      'opiskeluoikeudentyyppi',
      'aikuistenperusopetus'
    >
    tila?: AikuistenPerusopetuksenOpiskeluoikeudenTila
    alkamispäivä?: string
    organisaatiohistoria?: Array<OpiskeluoikeudenOrganisaatiohistoria>
    sisältyyOpiskeluoikeuteen?: SisältäväOpiskeluoikeus
    oid?: string
    koulutustoimija?: Koulutustoimija
    lisätiedot?: AikuistenPerusopetuksenOpiskeluoikeudenLisätiedot
    versionumero?: number
    suoritukset?: Array<AikuistenPerusopetuksenPäätasonSuoritus>
    aikaleima?: string
    päättymispäivä?: string
    lähdejärjestelmänId?: LähdejärjestelmäId
    lähdejärjestelmäkytkentäPurettu?: LähdejärjestelmäkytkennänPurkaminen
    oppilaitos?: Oppilaitos
  } = {}
): AikuistenPerusopetuksenOpiskeluoikeus => ({
  tyyppi: Koodistokoodiviite({
    koodiarvo: 'aikuistenperusopetus',
    koodistoUri: 'opiskeluoikeudentyyppi'
  }),
  tila: AikuistenPerusopetuksenOpiskeluoikeudenTila({
    opiskeluoikeusjaksot: []
  }),
  suoritukset: [],
  $class: 'fi.oph.koski.schema.AikuistenPerusopetuksenOpiskeluoikeus',
  ...o
})

AikuistenPerusopetuksenOpiskeluoikeus.className =
  'fi.oph.koski.schema.AikuistenPerusopetuksenOpiskeluoikeus' as const

export const isAikuistenPerusopetuksenOpiskeluoikeus = (
  a: any
): a is AikuistenPerusopetuksenOpiskeluoikeus =>
  a?.$class === 'fi.oph.koski.schema.AikuistenPerusopetuksenOpiskeluoikeus'
