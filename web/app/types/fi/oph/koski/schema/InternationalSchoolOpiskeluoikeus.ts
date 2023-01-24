import { Koodistokoodiviite } from './Koodistokoodiviite'
import { LocalizedString } from './LocalizedString'
import { InternationalSchoolOpiskeluoikeudenTila } from './InternationalSchoolOpiskeluoikeudenTila'
import { OpiskeluoikeudenOrganisaatiohistoria } from './OpiskeluoikeudenOrganisaatiohistoria'
import { Koulutustoimija } from './Koulutustoimija'
import { InternationalSchoolOpiskeluoikeudenLisätiedot } from './InternationalSchoolOpiskeluoikeudenLisatiedot'
import { InternationalSchoolVuosiluokanSuoritus } from './InternationalSchoolVuosiluokanSuoritus'
import { LähdejärjestelmäId } from './LahdejarjestelmaId'
import { Oppilaitos } from './Oppilaitos'

/**
 * InternationalSchoolOpiskeluoikeus
 *
 * @see `fi.oph.koski.schema.InternationalSchoolOpiskeluoikeus`
 */
export type InternationalSchoolOpiskeluoikeus = {
  $class: 'fi.oph.koski.schema.InternationalSchoolOpiskeluoikeus'
  tyyppi: Koodistokoodiviite<'opiskeluoikeudentyyppi', 'internationalschool'>
  tila: InternationalSchoolOpiskeluoikeudenTila
  alkamispäivä?: string
  organisaatiohistoria?: Array<OpiskeluoikeudenOrganisaatiohistoria>
  oid?: string
  koulutustoimija?: Koulutustoimija
  lisätiedot?: InternationalSchoolOpiskeluoikeudenLisätiedot
  versionumero?: number
  suoritukset: Array<InternationalSchoolVuosiluokanSuoritus>
  aikaleima?: string
  päättymispäivä?: string
  lähdejärjestelmänId?: LähdejärjestelmäId
  arvioituPäättymispäivä?: string
  oppilaitos?: Oppilaitos
}

export const InternationalSchoolOpiskeluoikeus = (
  o: {
    tyyppi?: Koodistokoodiviite<'opiskeluoikeudentyyppi', 'internationalschool'>
    tila?: InternationalSchoolOpiskeluoikeudenTila
    alkamispäivä?: string
    organisaatiohistoria?: Array<OpiskeluoikeudenOrganisaatiohistoria>
    oid?: string
    koulutustoimija?: Koulutustoimija
    lisätiedot?: InternationalSchoolOpiskeluoikeudenLisätiedot
    versionumero?: number
    suoritukset?: Array<InternationalSchoolVuosiluokanSuoritus>
    aikaleima?: string
    päättymispäivä?: string
    lähdejärjestelmänId?: LähdejärjestelmäId
    arvioituPäättymispäivä?: string
    oppilaitos?: Oppilaitos
  } = {}
): InternationalSchoolOpiskeluoikeus => ({
  tyyppi: Koodistokoodiviite({
    koodiarvo: 'internationalschool',
    koodistoUri: 'opiskeluoikeudentyyppi'
  }),
  tila: InternationalSchoolOpiskeluoikeudenTila({ opiskeluoikeusjaksot: [] }),
  suoritukset: [],
  $class: 'fi.oph.koski.schema.InternationalSchoolOpiskeluoikeus',
  ...o
})

InternationalSchoolOpiskeluoikeus.className =
  'fi.oph.koski.schema.InternationalSchoolOpiskeluoikeus' as const

export const isInternationalSchoolOpiskeluoikeus = (
  a: any
): a is InternationalSchoolOpiskeluoikeus =>
  a?.$class === 'fi.oph.koski.schema.InternationalSchoolOpiskeluoikeus'
