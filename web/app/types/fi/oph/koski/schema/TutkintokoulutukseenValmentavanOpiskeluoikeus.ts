import { Koodistokoodiviite } from './Koodistokoodiviite'
import { LocalizedString } from './LocalizedString'
import { TutkintokoulutukseenValmentavanOpiskeluoikeudenTila } from './TutkintokoulutukseenValmentavanOpiskeluoikeudenTila'
import { OpiskeluoikeudenOrganisaatiohistoria } from './OpiskeluoikeudenOrganisaatiohistoria'
import { Koulutustoimija } from './Koulutustoimija'
import { TutkintokoulutukseenValmentavanOpiskeluoikeudenLisätiedot } from './TutkintokoulutukseenValmentavanOpiskeluoikeudenLisatiedot'
import { TutkintokoulutukseenValmentavanKoulutuksenPäätasonSuoritus } from './TutkintokoulutukseenValmentavanKoulutuksenPaatasonSuoritus'
import { LähdejärjestelmäId } from './LahdejarjestelmaId'
import { Oppilaitos } from './Oppilaitos'

/**
 * Tutkintokoulutukseen valmentavan koulutuksen (TUVA) opiskeluoikeus
 *
 * @see `fi.oph.koski.schema.TutkintokoulutukseenValmentavanOpiskeluoikeus`
 */
export type TutkintokoulutukseenValmentavanOpiskeluoikeus = {
  $class: 'fi.oph.koski.schema.TutkintokoulutukseenValmentavanOpiskeluoikeus'
  tyyppi: Koodistokoodiviite<'opiskeluoikeudentyyppi', 'tuva'>
  tila: TutkintokoulutukseenValmentavanOpiskeluoikeudenTila
  alkamispäivä?: string
  organisaatiohistoria?: Array<OpiskeluoikeudenOrganisaatiohistoria>
  oid?: string
  koulutustoimija?: Koulutustoimija
  lisätiedot?: TutkintokoulutukseenValmentavanOpiskeluoikeudenLisätiedot
  versionumero?: number
  suoritukset: Array<TutkintokoulutukseenValmentavanKoulutuksenPäätasonSuoritus>
  aikaleima?: string
  päättymispäivä?: string
  järjestämislupa: Koodistokoodiviite<'tuvajarjestamislupa', string>
  lähdejärjestelmänId?: LähdejärjestelmäId
  arvioituPäättymispäivä?: string
  oppilaitos?: Oppilaitos
}

export const TutkintokoulutukseenValmentavanOpiskeluoikeus = (o: {
  tyyppi?: Koodistokoodiviite<'opiskeluoikeudentyyppi', 'tuva'>
  tila?: TutkintokoulutukseenValmentavanOpiskeluoikeudenTila
  alkamispäivä?: string
  organisaatiohistoria?: Array<OpiskeluoikeudenOrganisaatiohistoria>
  oid?: string
  koulutustoimija?: Koulutustoimija
  lisätiedot?: TutkintokoulutukseenValmentavanOpiskeluoikeudenLisätiedot
  versionumero?: number
  suoritukset?: Array<TutkintokoulutukseenValmentavanKoulutuksenPäätasonSuoritus>
  aikaleima?: string
  päättymispäivä?: string
  järjestämislupa: Koodistokoodiviite<'tuvajarjestamislupa', string>
  lähdejärjestelmänId?: LähdejärjestelmäId
  arvioituPäättymispäivä?: string
  oppilaitos?: Oppilaitos
}): TutkintokoulutukseenValmentavanOpiskeluoikeus => ({
  tyyppi: Koodistokoodiviite({
    koodiarvo: 'tuva',
    koodistoUri: 'opiskeluoikeudentyyppi'
  }),
  tila: TutkintokoulutukseenValmentavanOpiskeluoikeudenTila({
    opiskeluoikeusjaksot: []
  }),
  suoritukset: [],
  $class: 'fi.oph.koski.schema.TutkintokoulutukseenValmentavanOpiskeluoikeus',
  ...o
})

TutkintokoulutukseenValmentavanOpiskeluoikeus.className =
  'fi.oph.koski.schema.TutkintokoulutukseenValmentavanOpiskeluoikeus' as const

export const isTutkintokoulutukseenValmentavanOpiskeluoikeus = (
  a: any
): a is TutkintokoulutukseenValmentavanOpiskeluoikeus =>
  a?.$class ===
  'fi.oph.koski.schema.TutkintokoulutukseenValmentavanOpiskeluoikeus'
