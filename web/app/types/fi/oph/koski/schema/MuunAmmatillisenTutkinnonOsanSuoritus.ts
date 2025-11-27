import { AmmatillinenArviointi } from './AmmatillinenArviointi'
import { Koodistokoodiviite } from './Koodistokoodiviite'
import { LocalizedString } from './LocalizedString'
import { AmmatillisenTutkinnonOsanLisätieto } from './AmmatillisenTutkinnonOsanLisatieto'
import { MuuKuinYhteinenTutkinnonOsa } from './MuuKuinYhteinenTutkinnonOsa'
import { AmmatillinenTutkintoKoulutus } from './AmmatillinenTutkintoKoulutus'
import { HenkilövahvistusValinnaisellaTittelillä } from './HenkilovahvistusValinnaisellaTittelilla'
import { Näyttö } from './Naytto'
import { OsaamisenTunnustaminen } from './OsaamisenTunnustaminen'
import { OrganisaatioWithOid } from './OrganisaatioWithOid'
import { AmmatillisenTutkinnonOsaaPienemmänKokonaisuudenSuoritus } from './AmmatillisenTutkinnonOsaaPienemmanKokonaisuudenSuoritus'

/**
 * Ammatilliseen tutkintoon liittyvän, muun kuin yhteisen tutkinnonosan suoritus
 *
 * @see `fi.oph.koski.schema.MuunAmmatillisenTutkinnonOsanSuoritus`
 */
export type MuunAmmatillisenTutkinnonOsanSuoritus = {
  $class: 'fi.oph.koski.schema.MuunAmmatillisenTutkinnonOsanSuoritus'
  arviointi?: Array<AmmatillinenArviointi>
  suorituskieli?: Koodistokoodiviite<'kieli', string>
  lisätiedot?: Array<AmmatillisenTutkinnonOsanLisätieto>
  koulutusmoduuli: MuuKuinYhteinenTutkinnonOsa
  tutkinto?: AmmatillinenTutkintoKoulutus
  vahvistus?: HenkilövahvistusValinnaisellaTittelillä
  näyttö?: Näyttö
  tyyppi: Koodistokoodiviite<'suorituksentyyppi', 'ammatillisentutkinnonosa'>
  tila?: Koodistokoodiviite<'suorituksentila', string>
  alkamispäivä?: string
  tunnustettu?: OsaamisenTunnustaminen
  toimipiste?: OrganisaatioWithOid
  tutkinnonOsanRyhmä?: Koodistokoodiviite<
    'ammatillisentutkinnonosanryhma',
    '1' | '3' | '4'
  >
  osasuoritukset?: Array<AmmatillisenTutkinnonOsaaPienemmänKokonaisuudenSuoritus>
}

export const MuunAmmatillisenTutkinnonOsanSuoritus = (o: {
  arviointi?: Array<AmmatillinenArviointi>
  suorituskieli?: Koodistokoodiviite<'kieli', string>
  lisätiedot?: Array<AmmatillisenTutkinnonOsanLisätieto>
  koulutusmoduuli: MuuKuinYhteinenTutkinnonOsa
  tutkinto?: AmmatillinenTutkintoKoulutus
  vahvistus?: HenkilövahvistusValinnaisellaTittelillä
  näyttö?: Näyttö
  tyyppi?: Koodistokoodiviite<'suorituksentyyppi', 'ammatillisentutkinnonosa'>
  tila?: Koodistokoodiviite<'suorituksentila', string>
  alkamispäivä?: string
  tunnustettu?: OsaamisenTunnustaminen
  toimipiste?: OrganisaatioWithOid
  tutkinnonOsanRyhmä?: Koodistokoodiviite<
    'ammatillisentutkinnonosanryhma',
    '1' | '3' | '4'
  >
  osasuoritukset?: Array<AmmatillisenTutkinnonOsaaPienemmänKokonaisuudenSuoritus>
}): MuunAmmatillisenTutkinnonOsanSuoritus => ({
  tyyppi: Koodistokoodiviite({
    koodiarvo: 'ammatillisentutkinnonosa',
    koodistoUri: 'suorituksentyyppi'
  }),
  $class: 'fi.oph.koski.schema.MuunAmmatillisenTutkinnonOsanSuoritus',
  ...o
})

MuunAmmatillisenTutkinnonOsanSuoritus.className =
  'fi.oph.koski.schema.MuunAmmatillisenTutkinnonOsanSuoritus' as const

export const isMuunAmmatillisenTutkinnonOsanSuoritus = (
  a: any
): a is MuunAmmatillisenTutkinnonOsanSuoritus =>
  a?.$class === 'fi.oph.koski.schema.MuunAmmatillisenTutkinnonOsanSuoritus'
