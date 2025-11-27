import { Koodistokoodiviite } from './Koodistokoodiviite'
import { LocalizedString } from './LocalizedString'
import { OpiskeluoikeudenOrganisaatiohistoria } from './OpiskeluoikeudenOrganisaatiohistoria'
import { InternationalSchoolVuosiluokanSuoritus } from './InternationalSchoolVuosiluokanSuoritus'
import { LähdejärjestelmäkytkennänPurkaminen } from './LahdejarjestelmakytkennanPurkaminen'
import { Oppilaitos } from './Oppilaitos'
import { InternationalSchoolOpiskeluoikeudenTila } from './InternationalSchoolOpiskeluoikeudenTila'
import { Koulutustoimija } from './Koulutustoimija'
import { InternationalSchoolOpiskeluoikeudenLisätiedot } from './InternationalSchoolOpiskeluoikeudenLisatiedot'
import { LähdejärjestelmäId } from './LahdejarjestelmaId'

/**
 * InternationalSchoolOpiskeluoikeus
 *
 * @see `fi.oph.koski.schema.InternationalSchoolOpiskeluoikeus`
 */
export type InternationalSchoolOpiskeluoikeus = {
  $class: 'fi.oph.koski.schema.InternationalSchoolOpiskeluoikeus'
  tyyppi: Koodistokoodiviite<'opiskeluoikeudentyyppi', 'internationalschool'>
  organisaatiohistoria?: Array<OpiskeluoikeudenOrganisaatiohistoria>
  oid?: string
  versionumero?: number
  suoritukset: Array<InternationalSchoolVuosiluokanSuoritus>
  aikaleima?: string
  päättymispäivä?: string
  lähdejärjestelmäkytkentäPurettu?: LähdejärjestelmäkytkennänPurkaminen
  oppilaitos?: Oppilaitos
  tila: InternationalSchoolOpiskeluoikeudenTila
  alkamispäivä?: string
  koulutustoimija?: Koulutustoimija
  lisätiedot?: InternationalSchoolOpiskeluoikeudenLisätiedot
  lähdejärjestelmänId?: LähdejärjestelmäId
  arvioituPäättymispäivä?: string
}

export const InternationalSchoolOpiskeluoikeus = (
  o: {
    tyyppi?: Koodistokoodiviite<'opiskeluoikeudentyyppi', 'internationalschool'>
    organisaatiohistoria?: Array<OpiskeluoikeudenOrganisaatiohistoria>
    oid?: string
    versionumero?: number
    suoritukset?: Array<InternationalSchoolVuosiluokanSuoritus>
    aikaleima?: string
    päättymispäivä?: string
    lähdejärjestelmäkytkentäPurettu?: LähdejärjestelmäkytkennänPurkaminen
    oppilaitos?: Oppilaitos
    tila?: InternationalSchoolOpiskeluoikeudenTila
    alkamispäivä?: string
    koulutustoimija?: Koulutustoimija
    lisätiedot?: InternationalSchoolOpiskeluoikeudenLisätiedot
    lähdejärjestelmänId?: LähdejärjestelmäId
    arvioituPäättymispäivä?: string
  } = {}
): InternationalSchoolOpiskeluoikeus => ({
  tyyppi: Koodistokoodiviite({
    koodiarvo: 'internationalschool',
    koodistoUri: 'opiskeluoikeudentyyppi'
  }),
  suoritukset: [],
  $class: 'fi.oph.koski.schema.InternationalSchoolOpiskeluoikeus',
  tila: InternationalSchoolOpiskeluoikeudenTila({ opiskeluoikeusjaksot: [] }),
  ...o
})

InternationalSchoolOpiskeluoikeus.className =
  'fi.oph.koski.schema.InternationalSchoolOpiskeluoikeus' as const

export const isInternationalSchoolOpiskeluoikeus = (
  a: any
): a is InternationalSchoolOpiskeluoikeus =>
  a?.$class === 'fi.oph.koski.schema.InternationalSchoolOpiskeluoikeus'
