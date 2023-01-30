import { AmmatillinenArviointi } from './AmmatillinenArviointi'
import { Näyttö } from './Naytto'
import { Koodistokoodiviite } from './Koodistokoodiviite'
import { LocalizedString } from './LocalizedString'
import { AmmatillisenTutkinnonOsanLisätieto } from './AmmatillisenTutkinnonOsanLisatieto'
import { MuuKuinYhteinenTutkinnonOsa } from './MuuKuinYhteinenTutkinnonOsa'
import { OsaamisenTunnustaminen } from './OsaamisenTunnustaminen'
import { OrganisaatioWithOid } from './OrganisaatioWithOid'
import { AmmatillisenTutkinnonOsaaPienemmänKokonaisuudenSuoritus } from './AmmatillisenTutkinnonOsaaPienemmanKokonaisuudenSuoritus'
import { AmmatillinenTutkintoKoulutus } from './AmmatillinenTutkintoKoulutus'
import { HenkilövahvistusValinnaisellaTittelillä } from './HenkilovahvistusValinnaisellaTittelilla'

/**
 * Ammatilliseen tutkintoon liittyvän, muun kuin yhteisen tutkinnonosan suoritus
 *
 * @see `fi.oph.koski.schema.MuunOsittaisenAmmatillisenTutkinnonTutkinnonosanSuoritus`
 */
export type MuunOsittaisenAmmatillisenTutkinnonTutkinnonosanSuoritus = {
  $class: 'fi.oph.koski.schema.MuunOsittaisenAmmatillisenTutkinnonTutkinnonosanSuoritus'
  arviointi?: Array<AmmatillinenArviointi>
  näyttö?: Näyttö
  tyyppi: Koodistokoodiviite<'suorituksentyyppi', 'ammatillisentutkinnonosa'>
  tila?: Koodistokoodiviite<'suorituksentila', string>
  alkamispäivä?: string
  suorituskieli?: Koodistokoodiviite<'kieli', string>
  lisätiedot?: Array<AmmatillisenTutkinnonOsanLisätieto>
  koulutusmoduuli: MuuKuinYhteinenTutkinnonOsa
  tunnustettu?: OsaamisenTunnustaminen
  toimipiste?: OrganisaatioWithOid
  tutkinnonOsanRyhmä?: Koodistokoodiviite<
    'ammatillisentutkinnonosanryhma',
    '1' | '3' | '4'
  >
  osasuoritukset?: Array<AmmatillisenTutkinnonOsaaPienemmänKokonaisuudenSuoritus>
  tutkinto?: AmmatillinenTutkintoKoulutus
  vahvistus?: HenkilövahvistusValinnaisellaTittelillä
}

export const MuunOsittaisenAmmatillisenTutkinnonTutkinnonosanSuoritus = (o: {
  arviointi?: Array<AmmatillinenArviointi>
  näyttö?: Näyttö
  tyyppi?: Koodistokoodiviite<'suorituksentyyppi', 'ammatillisentutkinnonosa'>
  tila?: Koodistokoodiviite<'suorituksentila', string>
  alkamispäivä?: string
  suorituskieli?: Koodistokoodiviite<'kieli', string>
  lisätiedot?: Array<AmmatillisenTutkinnonOsanLisätieto>
  koulutusmoduuli: MuuKuinYhteinenTutkinnonOsa
  tunnustettu?: OsaamisenTunnustaminen
  toimipiste?: OrganisaatioWithOid
  tutkinnonOsanRyhmä?: Koodistokoodiviite<
    'ammatillisentutkinnonosanryhma',
    '1' | '3' | '4'
  >
  osasuoritukset?: Array<AmmatillisenTutkinnonOsaaPienemmänKokonaisuudenSuoritus>
  tutkinto?: AmmatillinenTutkintoKoulutus
  vahvistus?: HenkilövahvistusValinnaisellaTittelillä
}): MuunOsittaisenAmmatillisenTutkinnonTutkinnonosanSuoritus => ({
  tyyppi: Koodistokoodiviite({
    koodiarvo: 'ammatillisentutkinnonosa',
    koodistoUri: 'suorituksentyyppi'
  }),
  $class:
    'fi.oph.koski.schema.MuunOsittaisenAmmatillisenTutkinnonTutkinnonosanSuoritus',
  ...o
})

MuunOsittaisenAmmatillisenTutkinnonTutkinnonosanSuoritus.className =
  'fi.oph.koski.schema.MuunOsittaisenAmmatillisenTutkinnonTutkinnonosanSuoritus' as const

export const isMuunOsittaisenAmmatillisenTutkinnonTutkinnonosanSuoritus = (
  a: any
): a is MuunOsittaisenAmmatillisenTutkinnonTutkinnonosanSuoritus =>
  a?.$class ===
  'fi.oph.koski.schema.MuunOsittaisenAmmatillisenTutkinnonTutkinnonosanSuoritus'
