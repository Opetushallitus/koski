import { ActivePäätasonSuoritus } from '../components-v2/containers/EditorContainer'
import { UusiOpiskeluoikeusjakso } from '../components-v2/opiskeluoikeus/UusiOpiskeluoikeudenTilaModal'
import { todayISODate } from '../date/date'
import { t, localize } from '../i18n/i18n'
import { Finnish } from '../types/fi/oph/koski/schema/Finnish'
import { Koodistokoodiviite } from '../types/fi/oph/koski/schema/Koodistokoodiviite'
import { LaajuusOpintopisteissä } from '../types/fi/oph/koski/schema/LaajuusOpintopisteissa'
import { LukutaitokoulutuksenArviointi } from '../types/fi/oph/koski/schema/LukutaitokoulutuksenArviointi'
import { MuuallaSuoritettuOppivelvollisilleSuunnatunVapaanSivistystyönOpintojenSuoritus } from '../types/fi/oph/koski/schema/MuuallaSuoritettuOppivelvollisilleSuunnatunVapaanSivistystyonOpintojenSuoritus'
import { OppivelvollisilleSuunnattuVapaanSivistystyönKoulutus } from '../types/fi/oph/koski/schema/OppivelvollisilleSuunnattuVapaanSivistystyonKoulutus'
import { OppivelvollisilleSuunnattuVapaanSivistystyönOpiskeluoikeusjakso } from '../types/fi/oph/koski/schema/OppivelvollisilleSuunnattuVapaanSivistystyonOpiskeluoikeusjakso'
import { OppivelvollisilleSuunnatunVapaanSivistystyönOpintokokonaisuudenArviointi } from '../types/fi/oph/koski/schema/OppivelvollisilleSuunnatunVapaanSivistystyonOpintokokonaisuudenArviointi'
import { OppivelvollisilleSuunnatunVapaanSivistystyönOpintokokonaisuudenSuoritus } from '../types/fi/oph/koski/schema/OppivelvollisilleSuunnatunVapaanSivistystyonOpintokokonaisuudenSuoritus'
import { VapaanSivistystyöJotpaKoulutuksenArviointi } from '../types/fi/oph/koski/schema/VapaanSivistystyoJotpaKoulutuksenArviointi'
import { VapaanSivistystyönJotpaKoulutuksenOpiskeluoikeusjakso } from '../types/fi/oph/koski/schema/VapaanSivistystyonJotpaKoulutuksenOpiskeluoikeusjakso'
import { VapaanSivistystyönJotpaKoulutuksenOsasuorituksenSuoritus } from '../types/fi/oph/koski/schema/VapaanSivistystyonJotpaKoulutuksenOsasuorituksenSuoritus'
import { VapaanSivistystyönLukutaitokoulutuksenKokonaisuudenSuoritus } from '../types/fi/oph/koski/schema/VapaanSivistystyonLukutaitokoulutuksenKokonaisuudenSuoritus'
import { VapaanSivistystyönLukutaitokoulutus } from '../types/fi/oph/koski/schema/VapaanSivistystyonLukutaitokoulutus'
import { VapaanSivistystyönMaahanmuuttajienKotoutumiskoulutuksenKieliopintojenArviointi } from '../types/fi/oph/koski/schema/VapaanSivistystyonMaahanmuuttajienKotoutumiskoulutuksenKieliopintojenArviointi'
import { VapaanSivistystyönMaahanmuuttajienKotoutumiskoulutuksenKieliopintojenSuoritus } from '../types/fi/oph/koski/schema/VapaanSivistystyonMaahanmuuttajienKotoutumiskoulutuksenKieliopintojenSuoritus'
import { VapaanSivistystyönMaahanmuuttajienKotoutumiskoulutuksenOhjauksenSuoritus } from '../types/fi/oph/koski/schema/VapaanSivistystyonMaahanmuuttajienKotoutumiskoulutuksenOhjauksenSuoritus'
import { VapaanSivistystyönMaahanmuuttajienKotoutumiskoulutuksenTyöelämäJaYhteiskuntataidot } from '../types/fi/oph/koski/schema/VapaanSivistystyonMaahanmuuttajienKotoutumiskoulutuksenTyoelamaJaYhteiskuntataidot'
import { VapaanSivistystyönMaahanmuuttajienKotoutumiskoulutuksenTyöelämäJaYhteiskuntataitojenOpintojenSuoritus } from '../types/fi/oph/koski/schema/VapaanSivistystyonMaahanmuuttajienKotoutumiskoulutuksenTyoelamaJaYhteiskuntataitojenOpintojenSuoritus'
import { VapaanSivistystyönMaahanmuuttajienKotoutumiskoulutuksenTyöelämäJaYhteiskuntataitojenTyöelämäJakso } from '../types/fi/oph/koski/schema/VapaanSivistystyonMaahanmuuttajienKotoutumiskoulutuksenTyoelamaJaYhteiskuntataitojenTyoelamaJakso'
import { VapaanSivistystyönMaahanmuuttajienKotoutumiskoulutuksenValinnaistenOpintojenOsasuoritus } from '../types/fi/oph/koski/schema/VapaanSivistystyonMaahanmuuttajienKotoutumiskoulutuksenValinnaistenOpintojenOsasuoritus'
import { VapaanSivistystyönMaahanmuuttajienKotoutumiskoulutuksenValinnaistenOpintojenSuoritus } from '../types/fi/oph/koski/schema/VapaanSivistystyonMaahanmuuttajienKotoutumiskoulutuksenValinnaistenOpintojenSuoritus'
import { VapaanSivistystyönMaahanmuuttajienKotoutumiskoulutus } from '../types/fi/oph/koski/schema/VapaanSivistystyonMaahanmuuttajienKotoutumiskoulutus'
import { VapaanSivistystyönOpiskeluoikeus } from '../types/fi/oph/koski/schema/VapaanSivistystyonOpiskeluoikeus'
import { VapaanSivistystyönOpiskeluoikeusjakso } from '../types/fi/oph/koski/schema/VapaanSivistystyonOpiskeluoikeusjakso'
import { VapaanSivistystyönPäätasonSuoritus } from '../types/fi/oph/koski/schema/VapaanSivistystyonPaatasonSuoritus'
import { VapaanSivistystyönVapaatavoitteisenKoulutuksenOpiskeluoikeusjakso } from '../types/fi/oph/koski/schema/VapaanSivistystyonVapaatavoitteisenKoulutuksenOpiskeluoikeusjakso'
import { VapaanSivistystyönVapaatavoitteisenKoulutuksenOsasuorituksenSuoritus } from '../types/fi/oph/koski/schema/VapaanSivistystyonVapaatavoitteisenKoulutuksenOsasuorituksenSuoritus'
import { VapaanSivistystyöVapaatavoitteisenKoulutuksenArviointi } from '../types/fi/oph/koski/schema/VapaanSivistystyoVapaatavoitteisenKoulutuksenArviointi'
import { VSTKotoutumiskoulutuksenKieliJaViestintäosaamisenArviointi } from '../types/fi/oph/koski/schema/VSTKotoutumiskoulutuksenKieliJaViestintaosaamisenArviointi'
import { VSTKotoutumiskoulutuksenKieliJaViestintäosaamisenOsasuoritus } from '../types/fi/oph/koski/schema/VSTKotoutumiskoulutuksenKieliJaViestintaosaamisenOsasuoritus'
import { VSTKotoutumiskoulutuksenKieliJaViestintäosaamisenSuoritus2022 } from '../types/fi/oph/koski/schema/VSTKotoutumiskoulutuksenKieliJaViestintaosaamisenSuoritus2022'
import { VSTKotoutumiskoulutuksenOsasuorituksenArviointi2022 } from '../types/fi/oph/koski/schema/VSTKotoutumiskoulutuksenOsasuorituksenArviointi2022'
import { VSTKotoutumiskoulutuksenValinnaistenOpintojenOsasuoritus2022 } from '../types/fi/oph/koski/schema/VSTKotoutumiskoulutuksenValinnaistenOpintojenOsasuoritus2022'
import { VSTKotoutumiskoulutuksenYhteiskuntaJaTyöelämäosaaminenSuoritus2022 } from '../types/fi/oph/koski/schema/VSTKotoutumiskoulutuksenYhteiskuntaJaTyoelamaosaaminenSuoritus2022'
import { VSTKotoutumiskoulutus2022 } from '../types/fi/oph/koski/schema/VSTKotoutumiskoulutus2022'
import { KoodiarvotOf } from '../util/koodisto'
import { assertNever } from '../util/selfcare'
import { CreateVSTArviointi, VSTOsasuoritusArvioinnilla } from './typeguards'

function isHyväksytty(
  arvosana: NonNullable<
    VSTOsasuoritusArvioinnilla['arviointi']
  >[number]['arvosana']
) {
  const { koodistoUri, koodiarvo } = arvosana
  switch (koodistoUri) {
    case 'arviointiasteikkovstjotpa':
    case 'arviointiasteikkovstvapaatavoitteinen':
      // Oletuksena hyväksytty
      return true
    case 'arviointiasteikkovst':
      return (
        koodistoUri === 'arviointiasteikkovst' && koodiarvo === 'hyvaksytty'
      )
    case 'arviointiasteikkokehittyvankielitaidontasot':
      return true
    default:
      return assertNever(koodistoUri)
  }
}

/**
 * Selvittää osasuorituksen tyypin perusteella, minkälaisen arviointiprototypen käyttöliittymälle tarjotaan.
 */
export const createVstArviointi: CreateVSTArviointi = (o) => (arvosana) => {
  const c = o.$class
  switch (c) {
    case VSTKotoutumiskoulutuksenKieliJaViestintäosaamisenSuoritus2022.className:
    case VSTKotoutumiskoulutuksenValinnaistenOpintojenOsasuoritus2022.className:
    case VSTKotoutumiskoulutuksenYhteiskuntaJaTyöelämäosaaminenSuoritus2022.className:
      return VSTKotoutumiskoulutuksenOsasuorituksenArviointi2022({
        arvosana,
        päivä: todayISODate(),
        hyväksytty: isHyväksytty(arvosana)
      })
    case VapaanSivistystyönJotpaKoulutuksenOsasuorituksenSuoritus.className:
      return VapaanSivistystyöJotpaKoulutuksenArviointi({
        arvosana,
        päivä: todayISODate(),
        hyväksytty: isHyväksytty(arvosana)
      })
    case VapaanSivistystyönLukutaitokoulutuksenKokonaisuudenSuoritus.className:
      return LukutaitokoulutuksenArviointi({
        arvosana,
        päivä: todayISODate(),
        taitotaso: Koodistokoodiviite({
          koodistoUri: 'arviointiasteikkokehittyvankielitaidontasot',
          koodiarvo: 'A1.1'
        }),
        hyväksytty: isHyväksytty(arvosana)
      })
    case VapaanSivistystyönMaahanmuuttajienKotoutumiskoulutuksenKieliopintojenSuoritus.className:
      return VapaanSivistystyönMaahanmuuttajienKotoutumiskoulutuksenKieliopintojenArviointi(
        {
          arvosana,
          päivä: todayISODate(),
          hyväksytty: isHyväksytty(arvosana)
        }
      )
    case VapaanSivistystyönMaahanmuuttajienKotoutumiskoulutuksenOhjauksenSuoritus.className:
    case VapaanSivistystyönMaahanmuuttajienKotoutumiskoulutuksenTyöelämäJaYhteiskuntataitojenOpintojenSuoritus.className:
    case VapaanSivistystyönMaahanmuuttajienKotoutumiskoulutuksenValinnaistenOpintojenSuoritus.className:
      return OppivelvollisilleSuunnatunVapaanSivistystyönOpintokokonaisuudenArviointi(
        {
          arvosana,
          päivä: todayISODate(),
          hyväksytty: isHyväksytty(arvosana)
        }
      )
    case VapaanSivistystyönVapaatavoitteisenKoulutuksenOsasuorituksenSuoritus.className:
      return VapaanSivistystyöVapaatavoitteisenKoulutuksenArviointi({
        arvosana,
        päivä: todayISODate(),
        hyväksytty: isHyväksytty(arvosana)
      })
    case VSTKotoutumiskoulutuksenKieliJaViestintäosaamisenOsasuoritus.className:
      return VSTKotoutumiskoulutuksenKieliJaViestintäosaamisenArviointi({
        arvosana,
        arviointipäivä: todayISODate(),
        hyväksytty: isHyväksytty(arvosana)
      })

    case OppivelvollisilleSuunnatunVapaanSivistystyönOpintokokonaisuudenSuoritus.className:
    case MuuallaSuoritettuOppivelvollisilleSuunnatunVapaanSivistystyönOpintojenSuoritus.className:
    case VapaanSivistystyönMaahanmuuttajienKotoutumiskoulutuksenTyöelämäJaYhteiskuntataitojenTyöelämäJakso.className:
    case VapaanSivistystyönMaahanmuuttajienKotoutumiskoulutuksenValinnaistenOpintojenOsasuoritus.className:
    case VapaanSivistystyönMaahanmuuttajienKotoutumiskoulutuksenTyöelämäJaYhteiskuntataidot.className:
      return OppivelvollisilleSuunnatunVapaanSivistystyönOpintokokonaisuudenArviointi(
        {
          päivä: todayISODate(),
          arvosana,
          hyväksytty: isHyväksytty(arvosana)
        }
      )
    default:
      console.error(
        `No arviointi component available for the following class: ${c}`
      )
      return assertNever(c)
  }
}

/**
 * Käytetään oikean opiskeluoikeuden tilaluokan selvittämiseen, jos tiloja voi olla useampia.
 */
export const resolveOpiskeluoikeudenTilaClass = (
  päätasonSuoritus: ActivePäätasonSuoritus<VapaanSivistystyönOpiskeluoikeus>
) => {
  const { koodiarvo } = päätasonSuoritus.suoritus.tyyppi
  switch (koodiarvo) {
    case 'vstjotpakoulutus':
      return VapaanSivistystyönJotpaKoulutuksenOpiskeluoikeusjakso.className
    case 'vstvapaatavoitteinenkoulutus':
      return VapaanSivistystyönVapaatavoitteisenKoulutuksenOpiskeluoikeusjakso.className
    case 'vstmaahanmuuttajienkotoutumiskoulutus':
    case 'vstlukutaitokoulutus':
    case 'vstoppivelvollisillesuunnattukoulutus':
      return OppivelvollisilleSuunnattuVapaanSivistystyönOpiskeluoikeusjakso.className
    default:
      return assertNever(koodiarvo)
  }
}

const resolveOpiskeluoikeudenJaksoClass = (
  päätasonSuoritus: ActivePäätasonSuoritus<VapaanSivistystyönOpiskeluoikeus>
) => {
  const { koodiarvo } = päätasonSuoritus.suoritus.tyyppi
  switch (koodiarvo) {
    case 'vstjotpakoulutus':
      return VapaanSivistystyönJotpaKoulutuksenOpiskeluoikeusjakso.className
    case 'vstvapaatavoitteinenkoulutus':
      return VapaanSivistystyönVapaatavoitteisenKoulutuksenOpiskeluoikeusjakso.className
    case 'vstmaahanmuuttajienkotoutumiskoulutus':
    case 'vstlukutaitokoulutus':
    case 'vstoppivelvollisillesuunnattukoulutus':
      return OppivelvollisilleSuunnattuVapaanSivistystyönOpiskeluoikeusjakso.className
    default:
      return assertNever(koodiarvo)
  }
}

export const resolveDiaarinumero = (
  koulutusmoduuli:
    | VapaanSivistystyönMaahanmuuttajienKotoutumiskoulutus
    | VSTKotoutumiskoulutus2022
    | OppivelvollisilleSuunnattuVapaanSivistystyönKoulutus
    | VapaanSivistystyönLukutaitokoulutus
): string => {
  const { $class } = koulutusmoduuli
  switch ($class) {
    case VapaanSivistystyönMaahanmuuttajienKotoutumiskoulutus.className:
    case VSTKotoutumiskoulutus2022.className:
      return 'vstmaahanmuuttajienkotoutumiskoulutus'
    case VapaanSivistystyönLukutaitokoulutus.className:
      return 'vstlukutaitokoulutus'
    case OppivelvollisilleSuunnattuVapaanSivistystyönKoulutus.className:
      return 'vstoppivelvollisillesuunnattukoulutus'
    default:
      return assertNever($class)
  }
}

export const createVstOpiskeluoikeusjakso =
  (
    päätasonSuoritus: ActivePäätasonSuoritus<VapaanSivistystyönOpiskeluoikeus>
  ) =>
  (seed: UusiOpiskeluoikeusjakso<VapaanSivistystyönOpiskeluoikeusjakso>) => {
    const jaksoClass = resolveOpiskeluoikeudenJaksoClass(päätasonSuoritus)
    switch (jaksoClass) {
      case VapaanSivistystyönVapaatavoitteisenKoulutuksenOpiskeluoikeusjakso.className:
        return VapaanSivistystyönVapaatavoitteisenKoulutuksenOpiskeluoikeusjakso(
          seed as UusiOpiskeluoikeusjakso<VapaanSivistystyönVapaatavoitteisenKoulutuksenOpiskeluoikeusjakso>
        )
      case OppivelvollisilleSuunnattuVapaanSivistystyönOpiskeluoikeusjakso.className:
        return OppivelvollisilleSuunnattuVapaanSivistystyönOpiskeluoikeusjakso(
          seed as UusiOpiskeluoikeusjakso<OppivelvollisilleSuunnattuVapaanSivistystyönOpiskeluoikeusjakso>
        )
      case VapaanSivistystyönJotpaKoulutuksenOpiskeluoikeusjakso.className:
        return VapaanSivistystyönJotpaKoulutuksenOpiskeluoikeusjakso(
          seed as UusiOpiskeluoikeusjakso<VapaanSivistystyönJotpaKoulutuksenOpiskeluoikeusjakso>
        )
      default:
        return assertNever(jaksoClass)
    }
  }

export const vstNimi = (opiskeluoikeus: VapaanSivistystyönOpiskeluoikeus) =>
  `${t(
    opiskeluoikeus.suoritukset[0]?.koulutusmoduuli.tunniste.nimi
  )}`.toLowerCase()

export const vstSuorituksenNimi = (
  suoritus: VapaanSivistystyönPäätasonSuoritus
) => {
  const titles: Record<
    KoodiarvotOf<VapaanSivistystyönPäätasonSuoritus['tyyppi']>,
    string
  > = {
    vstjotpakoulutus: 'Vapaan sivistystyön koulutus',
    vstlukutaitokoulutus: 'Lukutaitokoulutus oppivelvollisille',
    vstmaahanmuuttajienkotoutumiskoulutus:
      'Kotoutumiskoulutus oppivelvollisille',
    vstoppivelvollisillesuunnattukoulutus:
      'Kansanopistojen vapaan sivistystyön koulutus oppivelvollisille',
    vstvapaatavoitteinenkoulutus: 'Vapaan sivistystyön koulutus'
  }

  return localize(`${t(titles[suoritus.tyyppi.koodiarvo])}`)
}

export const laajuusOpintopisteissa = (arvo: number) =>
  LaajuusOpintopisteissä({
    arvo,
    yksikkö: Koodistokoodiviite({
      koodiarvo: '2',
      nimi: Finnish({
        fi: 'opintopistettä',
        sv: 'studiepoäng',
        en: 'ECTS credits'
      }),
      lyhytNimi: Finnish({
        fi: 'op',
        sv: 'sp',
        en: 'ECTS cr'
      }),
      koodistoUri: 'opintojenlaajuusyksikko'
    })
  })

export const defaultLaajuusOpintopisteissa = laajuusOpintopisteissa(0)

export const defaultFinnishKuvaus = Finnish({ fi: '' })
