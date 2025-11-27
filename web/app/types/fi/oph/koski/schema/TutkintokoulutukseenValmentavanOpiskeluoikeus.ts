import { Koodistokoodiviite } from './Koodistokoodiviite'
import { LocalizedString } from './LocalizedString'
import { OpiskeluoikeudenOrganisaatiohistoria } from './OpiskeluoikeudenOrganisaatiohistoria'
import { TutkintokoulutukseenValmentavanKoulutuksenPäätasonSuoritus } from './TutkintokoulutukseenValmentavanKoulutuksenPaatasonSuoritus'
import { LähdejärjestelmäkytkennänPurkaminen } from './LahdejarjestelmakytkennanPurkaminen'
import { Oppilaitos } from './Oppilaitos'
import { TutkintokoulutukseenValmentavanOpiskeluoikeudenTila } from './TutkintokoulutukseenValmentavanOpiskeluoikeudenTila'
import { Koulutustoimija } from './Koulutustoimija'
import { TutkintokoulutukseenValmentavanOpiskeluoikeudenLisätiedot } from './TutkintokoulutukseenValmentavanOpiskeluoikeudenLisatiedot'
import { LähdejärjestelmäId } from './LahdejarjestelmaId'

/**
 * Tutkintokoulutukseen valmentavan koulutuksen (TUVA) opiskeluoikeus
 *
 * @see `fi.oph.koski.schema.TutkintokoulutukseenValmentavanOpiskeluoikeus`
 */
export type TutkintokoulutukseenValmentavanOpiskeluoikeus = {
  $class: 'fi.oph.koski.schema.TutkintokoulutukseenValmentavanOpiskeluoikeus'
  tyyppi: Koodistokoodiviite<'opiskeluoikeudentyyppi', 'tuva'>
  organisaatiohistoria?: Array<OpiskeluoikeudenOrganisaatiohistoria>
  oid?: string
  versionumero?: number
  suoritukset: Array<TutkintokoulutukseenValmentavanKoulutuksenPäätasonSuoritus>
  aikaleima?: string
  päättymispäivä?: string
  järjestämislupa: Koodistokoodiviite<'tuvajarjestamislupa', string>
  lähdejärjestelmäkytkentäPurettu?: LähdejärjestelmäkytkennänPurkaminen
  oppilaitos?: Oppilaitos
  tila: TutkintokoulutukseenValmentavanOpiskeluoikeudenTila
  alkamispäivä?: string
  koulutustoimija?: Koulutustoimija
  lisätiedot?: TutkintokoulutukseenValmentavanOpiskeluoikeudenLisätiedot
  lähdejärjestelmänId?: LähdejärjestelmäId
  arvioituPäättymispäivä?: string
}

export const TutkintokoulutukseenValmentavanOpiskeluoikeus = (o: {
  tyyppi?: Koodistokoodiviite<'opiskeluoikeudentyyppi', 'tuva'>
  organisaatiohistoria?: Array<OpiskeluoikeudenOrganisaatiohistoria>
  oid?: string
  versionumero?: number
  suoritukset?: Array<TutkintokoulutukseenValmentavanKoulutuksenPäätasonSuoritus>
  aikaleima?: string
  päättymispäivä?: string
  järjestämislupa: Koodistokoodiviite<'tuvajarjestamislupa', string>
  lähdejärjestelmäkytkentäPurettu?: LähdejärjestelmäkytkennänPurkaminen
  oppilaitos?: Oppilaitos
  tila?: TutkintokoulutukseenValmentavanOpiskeluoikeudenTila
  alkamispäivä?: string
  koulutustoimija?: Koulutustoimija
  lisätiedot?: TutkintokoulutukseenValmentavanOpiskeluoikeudenLisätiedot
  lähdejärjestelmänId?: LähdejärjestelmäId
  arvioituPäättymispäivä?: string
}): TutkintokoulutukseenValmentavanOpiskeluoikeus => ({
  tyyppi: Koodistokoodiviite({
    koodiarvo: 'tuva',
    koodistoUri: 'opiskeluoikeudentyyppi'
  }),
  suoritukset: [],
  $class: 'fi.oph.koski.schema.TutkintokoulutukseenValmentavanOpiskeluoikeus',
  tila: TutkintokoulutukseenValmentavanOpiskeluoikeudenTila({
    opiskeluoikeusjaksot: []
  }),
  ...o
})

TutkintokoulutukseenValmentavanOpiskeluoikeus.className =
  'fi.oph.koski.schema.TutkintokoulutukseenValmentavanOpiskeluoikeus' as const

export const isTutkintokoulutukseenValmentavanOpiskeluoikeus = (
  a: any
): a is TutkintokoulutukseenValmentavanOpiskeluoikeus =>
  a?.$class ===
  'fi.oph.koski.schema.TutkintokoulutukseenValmentavanOpiskeluoikeus'
