import { Koodistokoodiviite } from './Koodistokoodiviite'
import { LocalizedString } from './LocalizedString'
import { OpiskeluoikeudenOrganisaatiohistoria } from './OpiskeluoikeudenOrganisaatiohistoria'
import { Koulutustoimija } from './Koulutustoimija'
import { EBTutkinnonSuoritus } from './EBTutkinnonSuoritus'
import { LähdejärjestelmäkytkennänPurkaminen } from './LahdejarjestelmakytkennanPurkaminen'
import { Oppilaitos } from './Oppilaitos'
import { EBOpiskeluoikeudenTila } from './EBOpiskeluoikeudenTila'
import { LähdejärjestelmäId } from './LahdejarjestelmaId'

/**
 * EBOpiskeluoikeus
 *
 * @see `fi.oph.koski.schema.EBOpiskeluoikeus`
 */
export type EBOpiskeluoikeus = {
  $class: 'fi.oph.koski.schema.EBOpiskeluoikeus'
  tyyppi: Koodistokoodiviite<'opiskeluoikeudentyyppi', 'ebtutkinto'>
  organisaatiohistoria?: Array<OpiskeluoikeudenOrganisaatiohistoria>
  oid?: string
  koulutustoimija?: Koulutustoimija
  versionumero?: number
  suoritukset: Array<EBTutkinnonSuoritus>
  aikaleima?: string
  päättymispäivä?: string
  lähdejärjestelmäkytkentäPurettu?: LähdejärjestelmäkytkennänPurkaminen
  oppilaitos?: Oppilaitos
  tila: EBOpiskeluoikeudenTila
  alkamispäivä?: string
  lähdejärjestelmänId?: LähdejärjestelmäId
  arvioituPäättymispäivä?: string
}

export const EBOpiskeluoikeus = (
  o: {
    tyyppi?: Koodistokoodiviite<'opiskeluoikeudentyyppi', 'ebtutkinto'>
    organisaatiohistoria?: Array<OpiskeluoikeudenOrganisaatiohistoria>
    oid?: string
    koulutustoimija?: Koulutustoimija
    versionumero?: number
    suoritukset?: Array<EBTutkinnonSuoritus>
    aikaleima?: string
    päättymispäivä?: string
    lähdejärjestelmäkytkentäPurettu?: LähdejärjestelmäkytkennänPurkaminen
    oppilaitos?: Oppilaitos
    tila?: EBOpiskeluoikeudenTila
    alkamispäivä?: string
    lähdejärjestelmänId?: LähdejärjestelmäId
    arvioituPäättymispäivä?: string
  } = {}
): EBOpiskeluoikeus => ({
  tyyppi: Koodistokoodiviite({
    koodiarvo: 'ebtutkinto',
    koodistoUri: 'opiskeluoikeudentyyppi'
  }),
  suoritukset: [],
  $class: 'fi.oph.koski.schema.EBOpiskeluoikeus',
  tila: EBOpiskeluoikeudenTila({ opiskeluoikeusjaksot: [] }),
  ...o
})

EBOpiskeluoikeus.className = 'fi.oph.koski.schema.EBOpiskeluoikeus' as const

export const isEBOpiskeluoikeus = (a: any): a is EBOpiskeluoikeus =>
  a?.$class === 'fi.oph.koski.schema.EBOpiskeluoikeus'
