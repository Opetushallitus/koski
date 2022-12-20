import { Koodistokoodiviite } from './Koodistokoodiviite'
import { LocalizedString } from './LocalizedString'
import { VSTKotoutumiskoulutus2022 } from './VSTKotoutumiskoulutus2022'
import { OrganisaatioWithOid } from './OrganisaatioWithOid'
import { VSTKotoutumiskoulutuksenKokonaisuudenOsasuoritus2022 } from './VSTKotoutumiskoulutuksenKokonaisuudenOsasuoritus2022'
import { HenkilövahvistusValinnaisellaPaikkakunnalla } from './HenkilovahvistusValinnaisellaPaikkakunnalla'

/**
 * Laajennetun oppivelvollisuuden suoritus
 *
 * @see `fi.oph.koski.schema.OppivelvollisilleSuunnattuMaahanmuuttajienKotoutumiskoulutuksenSuoritus2022`
 */
export type OppivelvollisilleSuunnattuMaahanmuuttajienKotoutumiskoulutuksenSuoritus2022 =
  {
    $class: 'fi.oph.koski.schema.OppivelvollisilleSuunnattuMaahanmuuttajienKotoutumiskoulutuksenSuoritus2022'
    tyyppi: Koodistokoodiviite<
      'suorituksentyyppi',
      'vstmaahanmuuttajienkotoutumiskoulutus'
    >
    tila?: Koodistokoodiviite<'suorituksentila', string>
    suorituskieli: Koodistokoodiviite<'kieli', string>
    todistuksellaNäkyvätLisätiedot?: LocalizedString
    koulutusmoduuli: VSTKotoutumiskoulutus2022
    toimipiste: OrganisaatioWithOid
    osasuoritukset?: Array<VSTKotoutumiskoulutuksenKokonaisuudenOsasuoritus2022>
    vahvistus?: HenkilövahvistusValinnaisellaPaikkakunnalla
  }

export const OppivelvollisilleSuunnattuMaahanmuuttajienKotoutumiskoulutuksenSuoritus2022 =
  (o: {
    tyyppi?: Koodistokoodiviite<
      'suorituksentyyppi',
      'vstmaahanmuuttajienkotoutumiskoulutus'
    >
    tila?: Koodistokoodiviite<'suorituksentila', string>
    suorituskieli: Koodistokoodiviite<'kieli', string>
    todistuksellaNäkyvätLisätiedot?: LocalizedString
    koulutusmoduuli?: VSTKotoutumiskoulutus2022
    toimipiste: OrganisaatioWithOid
    osasuoritukset?: Array<VSTKotoutumiskoulutuksenKokonaisuudenOsasuoritus2022>
    vahvistus?: HenkilövahvistusValinnaisellaPaikkakunnalla
  }): OppivelvollisilleSuunnattuMaahanmuuttajienKotoutumiskoulutuksenSuoritus2022 => ({
    tyyppi: Koodistokoodiviite({
      koodiarvo: 'vstmaahanmuuttajienkotoutumiskoulutus',
      koodistoUri: 'suorituksentyyppi'
    }),
    koulutusmoduuli: VSTKotoutumiskoulutus2022({
      tunniste: Koodistokoodiviite({
        koodiarvo: '999910',
        koodistoUri: 'koulutus'
      })
    }),
    $class:
      'fi.oph.koski.schema.OppivelvollisilleSuunnattuMaahanmuuttajienKotoutumiskoulutuksenSuoritus2022',
    ...o
  })

export const isOppivelvollisilleSuunnattuMaahanmuuttajienKotoutumiskoulutuksenSuoritus2022 =
  (
    a: any
  ): a is OppivelvollisilleSuunnattuMaahanmuuttajienKotoutumiskoulutuksenSuoritus2022 =>
    a?.$class ===
    'fi.oph.koski.schema.OppivelvollisilleSuunnattuMaahanmuuttajienKotoutumiskoulutuksenSuoritus2022'
