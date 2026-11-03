import { Koodistokoodiviite } from './Koodistokoodiviite'
import { LocalizedString } from './LocalizedString'
import { OpiskeluoikeudenOrganisaatiohistoria } from './OpiskeluoikeudenOrganisaatiohistoria'
import { Koulutustoimija } from './Koulutustoimija'
import { IBDiplomanSuoritus } from './IBDiplomanSuoritus'
import { LähdejärjestelmäkytkennänPurkaminen } from './LahdejarjestelmakytkennanPurkaminen'
import { Oppilaitos } from './Oppilaitos'
import { IBDiplomaOpiskeluoikeudenTila } from './IBDiplomaOpiskeluoikeudenTila'
import { LähdejärjestelmäId } from './LahdejarjestelmaId'

/**
 * IBO:n myöntämän IB-tutkinnon (IB Diploma) opiskeluoikeus. Tiedot tuodaan IBO:n IBIS-järjestelmän tulostiedostosta.
 *
 * @see `fi.oph.koski.schema.IBDiplomaOpiskeluoikeus`
 */
export type IBDiplomaOpiskeluoikeus = {
  $class: 'fi.oph.koski.schema.IBDiplomaOpiskeluoikeus'
  tyyppi: Koodistokoodiviite<'opiskeluoikeudentyyppi', 'ibdiploma'>
  organisaatiohistoria?: Array<OpiskeluoikeudenOrganisaatiohistoria>
  oid?: string
  koulutustoimija?: Koulutustoimija
  iboTunniste: string
  versionumero?: number
  suoritukset: Array<IBDiplomanSuoritus>
  aikaleima?: string
  päättymispäivä?: string
  lähdejärjestelmäkytkentäPurettu?: LähdejärjestelmäkytkennänPurkaminen
  oppilaitos?: Oppilaitos
  tila: IBDiplomaOpiskeluoikeudenTila
  alkamispäivä?: string
  lähdejärjestelmänId?: LähdejärjestelmäId
  arvioituPäättymispäivä?: string
}

export const IBDiplomaOpiskeluoikeus = (o: {
  tyyppi?: Koodistokoodiviite<'opiskeluoikeudentyyppi', 'ibdiploma'>
  organisaatiohistoria?: Array<OpiskeluoikeudenOrganisaatiohistoria>
  oid?: string
  koulutustoimija?: Koulutustoimija
  iboTunniste: string
  versionumero?: number
  suoritukset?: Array<IBDiplomanSuoritus>
  aikaleima?: string
  päättymispäivä?: string
  lähdejärjestelmäkytkentäPurettu?: LähdejärjestelmäkytkennänPurkaminen
  oppilaitos?: Oppilaitos
  tila?: IBDiplomaOpiskeluoikeudenTila
  alkamispäivä?: string
  lähdejärjestelmänId?: LähdejärjestelmäId
  arvioituPäättymispäivä?: string
}): IBDiplomaOpiskeluoikeus => ({
  tyyppi: Koodistokoodiviite({
    koodiarvo: 'ibdiploma',
    koodistoUri: 'opiskeluoikeudentyyppi'
  }),
  suoritukset: [],
  $class: 'fi.oph.koski.schema.IBDiplomaOpiskeluoikeus',
  tila: IBDiplomaOpiskeluoikeudenTila({ opiskeluoikeusjaksot: [] }),
  ...o
})

IBDiplomaOpiskeluoikeus.className =
  'fi.oph.koski.schema.IBDiplomaOpiskeluoikeus' as const

export const isIBDiplomaOpiskeluoikeus = (
  a: any
): a is IBDiplomaOpiskeluoikeus =>
  a?.$class === 'fi.oph.koski.schema.IBDiplomaOpiskeluoikeus'
