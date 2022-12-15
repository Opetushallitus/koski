import { Koodistokoodiviite } from './Koodistokoodiviite'
import { LocalizedString } from './LocalizedString'
import { VapaanSivistystyönMaahanmuuttajienKotoutumiskoulutus } from './VapaanSivistystyonMaahanmuuttajienKotoutumiskoulutus'
import { OrganisaatioWithOid } from './OrganisaatioWithOid'
import { VapaanSivistystyönMaahanmuuttajienKotoutumiskoulutuksenKokonaisuudenSuoritus } from './VapaanSivistystyonMaahanmuuttajienKotoutumiskoulutuksenKokonaisuudenSuoritus'
import { HenkilövahvistusValinnaisellaPaikkakunnalla } from './HenkilovahvistusValinnaisellaPaikkakunnalla'

/**
 * Laajennetun oppivelvollisuuden suoritus
 *
 * @see `fi.oph.koski.schema.OppivelvollisilleSuunnattuMaahanmuuttajienKotoutumiskoulutuksenSuoritus`
 */
export type OppivelvollisilleSuunnattuMaahanmuuttajienKotoutumiskoulutuksenSuoritus =
  {
    $class: 'fi.oph.koski.schema.OppivelvollisilleSuunnattuMaahanmuuttajienKotoutumiskoulutuksenSuoritus'
    tyyppi: Koodistokoodiviite<
      'suorituksentyyppi',
      'vstmaahanmuuttajienkotoutumiskoulutus'
    >
    tila?: Koodistokoodiviite<'suorituksentila', string>
    suorituskieli: Koodistokoodiviite<'kieli', string>
    todistuksellaNäkyvätLisätiedot?: LocalizedString
    koulutusmoduuli: VapaanSivistystyönMaahanmuuttajienKotoutumiskoulutus
    toimipiste: OrganisaatioWithOid
    osasuoritukset?: Array<VapaanSivistystyönMaahanmuuttajienKotoutumiskoulutuksenKokonaisuudenSuoritus>
    vahvistus?: HenkilövahvistusValinnaisellaPaikkakunnalla
  }

export const OppivelvollisilleSuunnattuMaahanmuuttajienKotoutumiskoulutuksenSuoritus =
  (o: {
    tyyppi?: Koodistokoodiviite<
      'suorituksentyyppi',
      'vstmaahanmuuttajienkotoutumiskoulutus'
    >
    tila?: Koodistokoodiviite<'suorituksentila', string>
    suorituskieli: Koodistokoodiviite<'kieli', string>
    todistuksellaNäkyvätLisätiedot?: LocalizedString
    koulutusmoduuli?: VapaanSivistystyönMaahanmuuttajienKotoutumiskoulutus
    toimipiste: OrganisaatioWithOid
    osasuoritukset?: Array<VapaanSivistystyönMaahanmuuttajienKotoutumiskoulutuksenKokonaisuudenSuoritus>
    vahvistus?: HenkilövahvistusValinnaisellaPaikkakunnalla
  }): OppivelvollisilleSuunnattuMaahanmuuttajienKotoutumiskoulutuksenSuoritus => ({
    tyyppi: Koodistokoodiviite({
      koodiarvo: 'vstmaahanmuuttajienkotoutumiskoulutus',
      koodistoUri: 'suorituksentyyppi'
    }),
    koulutusmoduuli: VapaanSivistystyönMaahanmuuttajienKotoutumiskoulutus({
      tunniste: Koodistokoodiviite({
        koodiarvo: '999910',
        koodistoUri: 'koulutus'
      })
    }),
    $class:
      'fi.oph.koski.schema.OppivelvollisilleSuunnattuMaahanmuuttajienKotoutumiskoulutuksenSuoritus',
    ...o
  })

export const isOppivelvollisilleSuunnattuMaahanmuuttajienKotoutumiskoulutuksenSuoritus =
  (
    a: any
  ): a is OppivelvollisilleSuunnattuMaahanmuuttajienKotoutumiskoulutuksenSuoritus =>
    a?.$class ===
    'OppivelvollisilleSuunnattuMaahanmuuttajienKotoutumiskoulutuksenSuoritus'
