import { Koodistokoodiviite } from './Koodistokoodiviite'
import { LocalizedString } from './LocalizedString'
import { KorkeakoulunOpiskeluoikeudenTila } from './KorkeakoulunOpiskeluoikeudenTila'
import { Koulutustoimija } from './Koulutustoimija'
import { KorkeakoulunOpiskeluoikeudenLisätiedot } from './KorkeakoulunOpiskeluoikeudenLisatiedot'
import { VirtaVirhe } from './VirtaVirhe'
import { KorkeakouluSuoritus } from './KorkeakouluSuoritus'
import { LähdejärjestelmäId } from './LahdejarjestelmaId'
import { Oppilaitos } from './Oppilaitos'

/**
 * KorkeakoulunOpiskeluoikeus
 *
 * @see `fi.oph.koski.schema.KorkeakoulunOpiskeluoikeus`
 */
export type KorkeakoulunOpiskeluoikeus = {
  $class: 'fi.oph.koski.schema.KorkeakoulunOpiskeluoikeus'
  tyyppi: Koodistokoodiviite<'opiskeluoikeudentyyppi', 'korkeakoulutus'>
  tila: KorkeakoulunOpiskeluoikeudenTila
  alkamispäivä?: string
  oid?: string
  synteettinen: boolean
  koulutustoimija?: Koulutustoimija
  lisätiedot?: KorkeakoulunOpiskeluoikeudenLisätiedot
  virtaVirheet: Array<VirtaVirhe>
  suoritukset: Array<KorkeakouluSuoritus>
  päättymispäivä?: string
  lähdejärjestelmänId?: LähdejärjestelmäId
  luokittelu?: Array<
    Koodistokoodiviite<'virtaopiskeluoikeudenluokittelu', string>
  >
  arvioituPäättymispäivä?: string
  oppilaitos?: Oppilaitos
}

export const KorkeakoulunOpiskeluoikeus = (o: {
  tyyppi?: Koodistokoodiviite<'opiskeluoikeudentyyppi', 'korkeakoulutus'>
  tila?: KorkeakoulunOpiskeluoikeudenTila
  alkamispäivä?: string
  oid?: string
  synteettinen: boolean
  koulutustoimija?: Koulutustoimija
  lisätiedot?: KorkeakoulunOpiskeluoikeudenLisätiedot
  virtaVirheet?: Array<VirtaVirhe>
  suoritukset?: Array<KorkeakouluSuoritus>
  päättymispäivä?: string
  lähdejärjestelmänId?: LähdejärjestelmäId
  luokittelu?: Array<
    Koodistokoodiviite<'virtaopiskeluoikeudenluokittelu', string>
  >
  arvioituPäättymispäivä?: string
  oppilaitos?: Oppilaitos
}): KorkeakoulunOpiskeluoikeus => ({
  tyyppi: Koodistokoodiviite({
    koodiarvo: 'korkeakoulutus',
    koodistoUri: 'opiskeluoikeudentyyppi'
  }),
  tila: KorkeakoulunOpiskeluoikeudenTila({ opiskeluoikeusjaksot: [] }),
  virtaVirheet: [],
  suoritukset: [],
  $class: 'fi.oph.koski.schema.KorkeakoulunOpiskeluoikeus',
  ...o
})

export const isKorkeakoulunOpiskeluoikeus = (
  a: any
): a is KorkeakoulunOpiskeluoikeus =>
  a?.$class === 'fi.oph.koski.schema.KorkeakoulunOpiskeluoikeus'
