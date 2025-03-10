import { Koodistokoodiviite } from './Koodistokoodiviite'
import { LocalizedString } from './LocalizedString'
import { MuunKuinSäännellynKoulutuksenTila } from './MuunKuinSaannellynKoulutuksenTila'
import { OpiskeluoikeudenOrganisaatiohistoria } from './OpiskeluoikeudenOrganisaatiohistoria'
import { Koulutustoimija } from './Koulutustoimija'
import { MuunKuinSäännellynKoulutuksenLisätiedot } from './MuunKuinSaannellynKoulutuksenLisatiedot'
import { MuunKuinSäännellynKoulutuksenPäätasonSuoritus } from './MuunKuinSaannellynKoulutuksenPaatasonSuoritus'
import { LähdejärjestelmäId } from './LahdejarjestelmaId'
import { LähdejärjestelmäkytkennänPurkaminen } from './LahdejarjestelmakytkennanPurkaminen'
import { Oppilaitos } from './Oppilaitos'

/**
 * MuunKuinSäännellynKoulutuksenOpiskeluoikeus
 *
 * @see `fi.oph.koski.schema.MuunKuinSäännellynKoulutuksenOpiskeluoikeus`
 */
export type MuunKuinSäännellynKoulutuksenOpiskeluoikeus = {
  $class: 'fi.oph.koski.schema.MuunKuinSäännellynKoulutuksenOpiskeluoikeus'
  tyyppi: Koodistokoodiviite<
    'opiskeluoikeudentyyppi',
    'muukuinsaanneltykoulutus'
  >
  tila: MuunKuinSäännellynKoulutuksenTila
  alkamispäivä?: string
  organisaatiohistoria?: Array<OpiskeluoikeudenOrganisaatiohistoria>
  oid?: string
  koulutustoimija?: Koulutustoimija
  lisätiedot?: MuunKuinSäännellynKoulutuksenLisätiedot
  versionumero?: number
  suoritukset: Array<MuunKuinSäännellynKoulutuksenPäätasonSuoritus>
  aikaleima?: string
  päättymispäivä?: string
  lähdejärjestelmänId?: LähdejärjestelmäId
  arvioituPäättymispäivä?: string
  lähdejärjestelmäkytkentäPurettu?: LähdejärjestelmäkytkennänPurkaminen
  oppilaitos?: Oppilaitos
}

export const MuunKuinSäännellynKoulutuksenOpiskeluoikeus = (
  o: {
    tyyppi?: Koodistokoodiviite<
      'opiskeluoikeudentyyppi',
      'muukuinsaanneltykoulutus'
    >
    tila?: MuunKuinSäännellynKoulutuksenTila
    alkamispäivä?: string
    organisaatiohistoria?: Array<OpiskeluoikeudenOrganisaatiohistoria>
    oid?: string
    koulutustoimija?: Koulutustoimija
    lisätiedot?: MuunKuinSäännellynKoulutuksenLisätiedot
    versionumero?: number
    suoritukset?: Array<MuunKuinSäännellynKoulutuksenPäätasonSuoritus>
    aikaleima?: string
    päättymispäivä?: string
    lähdejärjestelmänId?: LähdejärjestelmäId
    arvioituPäättymispäivä?: string
    lähdejärjestelmäkytkentäPurettu?: LähdejärjestelmäkytkennänPurkaminen
    oppilaitos?: Oppilaitos
  } = {}
): MuunKuinSäännellynKoulutuksenOpiskeluoikeus => ({
  tyyppi: Koodistokoodiviite({
    koodiarvo: 'muukuinsaanneltykoulutus',
    koodistoUri: 'opiskeluoikeudentyyppi'
  }),
  tila: MuunKuinSäännellynKoulutuksenTila({ opiskeluoikeusjaksot: [] }),
  suoritukset: [],
  $class: 'fi.oph.koski.schema.MuunKuinSäännellynKoulutuksenOpiskeluoikeus',
  ...o
})

MuunKuinSäännellynKoulutuksenOpiskeluoikeus.className =
  'fi.oph.koski.schema.MuunKuinSäännellynKoulutuksenOpiskeluoikeus' as const

export const isMuunKuinSäännellynKoulutuksenOpiskeluoikeus = (
  a: any
): a is MuunKuinSäännellynKoulutuksenOpiskeluoikeus =>
  a?.$class ===
  'fi.oph.koski.schema.MuunKuinSäännellynKoulutuksenOpiskeluoikeus'
