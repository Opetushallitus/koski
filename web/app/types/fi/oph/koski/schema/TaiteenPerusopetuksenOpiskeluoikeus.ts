import { Koodistokoodiviite } from './Koodistokoodiviite'
import { LocalizedString } from './LocalizedString'
import { TaiteenPerusopetuksenOpiskeluoikeudenTila } from './TaiteenPerusopetuksenOpiskeluoikeudenTila'
import { OpiskeluoikeudenOrganisaatiohistoria } from './OpiskeluoikeudenOrganisaatiohistoria'
import { Koulutustoimija } from './Koulutustoimija'
import { TaiteenPerusopetuksenPäätasonSuoritus } from './TaiteenPerusopetuksenPaatasonSuoritus'
import { LähdejärjestelmäId } from './LahdejarjestelmaId'
import { Oppilaitos } from './Oppilaitos'

/**
 * TaiteenPerusopetuksenOpiskeluoikeus
 *
 * @see `fi.oph.koski.schema.TaiteenPerusopetuksenOpiskeluoikeus`
 */
export type TaiteenPerusopetuksenOpiskeluoikeus = {
  $class: 'fi.oph.koski.schema.TaiteenPerusopetuksenOpiskeluoikeus'
  tyyppi: Koodistokoodiviite<'opiskeluoikeudentyyppi', 'taiteenperusopetus'>
  tila: TaiteenPerusopetuksenOpiskeluoikeudenTila
  alkamispäivä?: string
  organisaatiohistoria?: Array<OpiskeluoikeudenOrganisaatiohistoria>
  oid?: string
  koulutustoimija?: Koulutustoimija
  versionumero?: number
  oppimäärä: Koodistokoodiviite<'taiteenperusopetusoppimaara', string>
  suoritukset: Array<TaiteenPerusopetuksenPäätasonSuoritus>
  aikaleima?: string
  päättymispäivä?: string
  lähdejärjestelmänId?: LähdejärjestelmäId
  arvioituPäättymispäivä?: string
  oppilaitos?: Oppilaitos
}

export const TaiteenPerusopetuksenOpiskeluoikeus = (o: {
  tyyppi?: Koodistokoodiviite<'opiskeluoikeudentyyppi', 'taiteenperusopetus'>
  tila?: TaiteenPerusopetuksenOpiskeluoikeudenTila
  alkamispäivä?: string
  organisaatiohistoria?: Array<OpiskeluoikeudenOrganisaatiohistoria>
  oid?: string
  koulutustoimija?: Koulutustoimija
  versionumero?: number
  oppimäärä: Koodistokoodiviite<'taiteenperusopetusoppimaara', string>
  suoritukset?: Array<TaiteenPerusopetuksenPäätasonSuoritus>
  aikaleima?: string
  päättymispäivä?: string
  lähdejärjestelmänId?: LähdejärjestelmäId
  arvioituPäättymispäivä?: string
  oppilaitos?: Oppilaitos
}): TaiteenPerusopetuksenOpiskeluoikeus => ({
  tyyppi: Koodistokoodiviite({
    koodiarvo: 'taiteenperusopetus',
    koodistoUri: 'opiskeluoikeudentyyppi'
  }),
  tila: TaiteenPerusopetuksenOpiskeluoikeudenTila({ opiskeluoikeusjaksot: [] }),
  suoritukset: [],
  $class: 'fi.oph.koski.schema.TaiteenPerusopetuksenOpiskeluoikeus',
  ...o
})

export const isTaiteenPerusopetuksenOpiskeluoikeus = (
  a: any
): a is TaiteenPerusopetuksenOpiskeluoikeus =>
  a?.$class === 'fi.oph.koski.schema.TaiteenPerusopetuksenOpiskeluoikeus'
