import { Koodistokoodiviite } from './Koodistokoodiviite'
import { LocalizedString } from './LocalizedString'
import { VirtaVirhe } from './VirtaVirhe'
import { KorkeakouluSuoritus } from './KorkeakouluSuoritus'
import { Oppilaitos } from './Oppilaitos'
import { KorkeakoulunOpiskeluoikeudenTila } from './KorkeakoulunOpiskeluoikeudenTila'
import { Koulutustoimija } from './Koulutustoimija'
import { KorkeakoulunOpiskeluoikeudenLisätiedot } from './KorkeakoulunOpiskeluoikeudenLisatiedot'
import { LähdejärjestelmäId } from './LahdejarjestelmaId'

/**
 * KorkeakoulunOpiskeluoikeus
 *
 * @see `fi.oph.koski.schema.KorkeakoulunOpiskeluoikeus`
 */
export type KorkeakoulunOpiskeluoikeus = {
  $class: 'fi.oph.koski.schema.KorkeakoulunOpiskeluoikeus'
  tyyppi: Koodistokoodiviite<'opiskeluoikeudentyyppi', 'korkeakoulutus'>
  oid?: string
  virtaVirheet: Array<VirtaVirhe>
  suoritukset: Array<KorkeakouluSuoritus>
  päättymispäivä?: string
  oppilaitos?: Oppilaitos
  tila: KorkeakoulunOpiskeluoikeudenTila
  alkamispäivä?: string
  synteettinen: boolean
  koulutustoimija?: Koulutustoimija
  lisätiedot?: KorkeakoulunOpiskeluoikeudenLisätiedot
  lähdejärjestelmänId?: LähdejärjestelmäId
  luokittelu?: Array<
    Koodistokoodiviite<'virtaopiskeluoikeudenluokittelu', string>
  >
  arvioituPäättymispäivä?: string
}

export const KorkeakoulunOpiskeluoikeus = (o: {
  tyyppi?: Koodistokoodiviite<'opiskeluoikeudentyyppi', 'korkeakoulutus'>
  oid?: string
  virtaVirheet?: Array<VirtaVirhe>
  suoritukset?: Array<KorkeakouluSuoritus>
  päättymispäivä?: string
  oppilaitos?: Oppilaitos
  tila?: KorkeakoulunOpiskeluoikeudenTila
  alkamispäivä?: string
  synteettinen: boolean
  koulutustoimija?: Koulutustoimija
  lisätiedot?: KorkeakoulunOpiskeluoikeudenLisätiedot
  lähdejärjestelmänId?: LähdejärjestelmäId
  luokittelu?: Array<
    Koodistokoodiviite<'virtaopiskeluoikeudenluokittelu', string>
  >
  arvioituPäättymispäivä?: string
}): KorkeakoulunOpiskeluoikeus => ({
  tyyppi: Koodistokoodiviite({
    koodiarvo: 'korkeakoulutus',
    koodistoUri: 'opiskeluoikeudentyyppi'
  }),
  virtaVirheet: [],
  suoritukset: [],
  $class: 'fi.oph.koski.schema.KorkeakoulunOpiskeluoikeus',
  tila: KorkeakoulunOpiskeluoikeudenTila({ opiskeluoikeusjaksot: [] }),
  ...o
})

KorkeakoulunOpiskeluoikeus.className =
  'fi.oph.koski.schema.KorkeakoulunOpiskeluoikeus' as const

export const isKorkeakoulunOpiskeluoikeus = (
  a: any
): a is KorkeakoulunOpiskeluoikeus =>
  a?.$class === 'fi.oph.koski.schema.KorkeakoulunOpiskeluoikeus'
