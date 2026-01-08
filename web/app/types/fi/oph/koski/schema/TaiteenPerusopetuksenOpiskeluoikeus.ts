import { Koodistokoodiviite } from './Koodistokoodiviite'
import { LocalizedString } from './LocalizedString'
import { OpiskeluoikeudenOrganisaatiohistoria } from './OpiskeluoikeudenOrganisaatiohistoria'
import { Koulutustoimija } from './Koulutustoimija'
import { TaiteenPerusopetuksenPäätasonSuoritus } from './TaiteenPerusopetuksenPaatasonSuoritus'
import { LähdejärjestelmäkytkennänPurkaminen } from './LahdejarjestelmakytkennanPurkaminen'
import { Oppilaitos } from './Oppilaitos'
import { TaiteenPerusopetuksenOpiskeluoikeudenTila } from './TaiteenPerusopetuksenOpiskeluoikeudenTila'
import { LähdejärjestelmäId } from './LahdejarjestelmaId'

/**
 * TaiteenPerusopetuksenOpiskeluoikeus
 *
 * @see `fi.oph.koski.schema.TaiteenPerusopetuksenOpiskeluoikeus`
 */
export type TaiteenPerusopetuksenOpiskeluoikeus = {
  $class: 'fi.oph.koski.schema.TaiteenPerusopetuksenOpiskeluoikeus'
  tyyppi: Koodistokoodiviite<'opiskeluoikeudentyyppi', 'taiteenperusopetus'>
  organisaatiohistoria?: Array<OpiskeluoikeudenOrganisaatiohistoria>
  oid?: string
  koulutustoimija?: Koulutustoimija
  suoritukset: Array<TaiteenPerusopetuksenPäätasonSuoritus>
  aikaleima?: string
  päättymispäivä?: string
  koulutuksenToteutustapa: Koodistokoodiviite<
    'taiteenperusopetuskoulutuksentoteutustapa',
    string
  >
  lähdejärjestelmäkytkentäPurettu?: LähdejärjestelmäkytkennänPurkaminen
  oppilaitos?: Oppilaitos
  tila: TaiteenPerusopetuksenOpiskeluoikeudenTila
  alkamispäivä?: string
  versionumero?: number
  oppimäärä: Koodistokoodiviite<'taiteenperusopetusoppimaara', string>
  lähdejärjestelmänId?: LähdejärjestelmäId
  arvioituPäättymispäivä?: string
}

export const TaiteenPerusopetuksenOpiskeluoikeus = (o: {
  tyyppi?: Koodistokoodiviite<'opiskeluoikeudentyyppi', 'taiteenperusopetus'>
  organisaatiohistoria?: Array<OpiskeluoikeudenOrganisaatiohistoria>
  oid?: string
  koulutustoimija?: Koulutustoimija
  suoritukset?: Array<TaiteenPerusopetuksenPäätasonSuoritus>
  aikaleima?: string
  päättymispäivä?: string
  koulutuksenToteutustapa: Koodistokoodiviite<
    'taiteenperusopetuskoulutuksentoteutustapa',
    string
  >
  lähdejärjestelmäkytkentäPurettu?: LähdejärjestelmäkytkennänPurkaminen
  oppilaitos?: Oppilaitos
  tila?: TaiteenPerusopetuksenOpiskeluoikeudenTila
  alkamispäivä?: string
  versionumero?: number
  oppimäärä: Koodistokoodiviite<'taiteenperusopetusoppimaara', string>
  lähdejärjestelmänId?: LähdejärjestelmäId
  arvioituPäättymispäivä?: string
}): TaiteenPerusopetuksenOpiskeluoikeus => ({
  tyyppi: Koodistokoodiviite({
    koodiarvo: 'taiteenperusopetus',
    koodistoUri: 'opiskeluoikeudentyyppi'
  }),
  suoritukset: [],
  $class: 'fi.oph.koski.schema.TaiteenPerusopetuksenOpiskeluoikeus',
  tila: TaiteenPerusopetuksenOpiskeluoikeudenTila({ opiskeluoikeusjaksot: [] }),
  ...o
})

TaiteenPerusopetuksenOpiskeluoikeus.className =
  'fi.oph.koski.schema.TaiteenPerusopetuksenOpiskeluoikeus' as const

export const isTaiteenPerusopetuksenOpiskeluoikeus = (
  a: any
): a is TaiteenPerusopetuksenOpiskeluoikeus =>
  a?.$class === 'fi.oph.koski.schema.TaiteenPerusopetuksenOpiskeluoikeus'
