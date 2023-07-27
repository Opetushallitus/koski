import { ActivePäätasonSuoritus } from '../components-v2/containers/EditorContainer'
import { todayISODate } from '../date/date'
import { Koodistokoodiviite } from '../types/fi/oph/koski/schema/Koodistokoodiviite'
import { LukutaitokoulutuksenArviointi } from '../types/fi/oph/koski/schema/LukutaitokoulutuksenArviointi'
import { OppivelvollisilleSuunnattuVapaanSivistystyönOpiskeluoikeusjakso } from '../types/fi/oph/koski/schema/OppivelvollisilleSuunnattuVapaanSivistystyonOpiskeluoikeusjakso'
import { OppivelvollisilleSuunnatunVapaanSivistystyönOpintokokonaisuudenArviointi } from '../types/fi/oph/koski/schema/OppivelvollisilleSuunnatunVapaanSivistystyonOpintokokonaisuudenArviointi'
import { VapaanSivistystyöJotpaKoulutuksenArviointi } from '../types/fi/oph/koski/schema/VapaanSivistystyoJotpaKoulutuksenArviointi'
import { VapaanSivistystyönJotpaKoulutuksenOpiskeluoikeusjakso } from '../types/fi/oph/koski/schema/VapaanSivistystyonJotpaKoulutuksenOpiskeluoikeusjakso'
import { VapaanSivistystyönJotpaKoulutuksenOsasuorituksenSuoritus } from '../types/fi/oph/koski/schema/VapaanSivistystyonJotpaKoulutuksenOsasuorituksenSuoritus'
import { VapaanSivistystyönLukutaitokoulutuksenKokonaisuudenSuoritus } from '../types/fi/oph/koski/schema/VapaanSivistystyonLukutaitokoulutuksenKokonaisuudenSuoritus'
import { VapaanSivistystyönMaahanmuuttajienKotoutumiskoulutuksenKieliopintojenArviointi } from '../types/fi/oph/koski/schema/VapaanSivistystyonMaahanmuuttajienKotoutumiskoulutuksenKieliopintojenArviointi'
import { VapaanSivistystyönMaahanmuuttajienKotoutumiskoulutuksenKieliopintojenSuoritus } from '../types/fi/oph/koski/schema/VapaanSivistystyonMaahanmuuttajienKotoutumiskoulutuksenKieliopintojenSuoritus'
import { VapaanSivistystyönMaahanmuuttajienKotoutumiskoulutuksenOhjauksenSuoritus } from '../types/fi/oph/koski/schema/VapaanSivistystyonMaahanmuuttajienKotoutumiskoulutuksenOhjauksenSuoritus'
import { VapaanSivistystyönMaahanmuuttajienKotoutumiskoulutuksenTyöelämäJaYhteiskuntataitojenOpintojenSuoritus } from '../types/fi/oph/koski/schema/VapaanSivistystyonMaahanmuuttajienKotoutumiskoulutuksenTyoelamaJaYhteiskuntataitojenOpintojenSuoritus'
import { VapaanSivistystyönMaahanmuuttajienKotoutumiskoulutuksenValinnaistenOpintojenSuoritus } from '../types/fi/oph/koski/schema/VapaanSivistystyonMaahanmuuttajienKotoutumiskoulutuksenValinnaistenOpintojenSuoritus'
import { VapaanSivistystyönOpiskeluoikeus } from '../types/fi/oph/koski/schema/VapaanSivistystyonOpiskeluoikeus'
import { VapaanSivistystyönVapaatavoitteisenKoulutuksenOsasuorituksenSuoritus } from '../types/fi/oph/koski/schema/VapaanSivistystyonVapaatavoitteisenKoulutuksenOsasuorituksenSuoritus'
import { VapaanSivistystyöVapaatavoitteisenKoulutuksenArviointi } from '../types/fi/oph/koski/schema/VapaanSivistystyoVapaatavoitteisenKoulutuksenArviointi'
import { VSTKotoutumiskoulutuksenKieliJaViestintäosaamisenArviointi } from '../types/fi/oph/koski/schema/VSTKotoutumiskoulutuksenKieliJaViestintaosaamisenArviointi'
import { VSTKotoutumiskoulutuksenKieliJaViestintäosaamisenOsasuoritus } from '../types/fi/oph/koski/schema/VSTKotoutumiskoulutuksenKieliJaViestintaosaamisenOsasuoritus'
import { VSTKotoutumiskoulutuksenKieliJaViestintäosaamisenSuoritus2022 } from '../types/fi/oph/koski/schema/VSTKotoutumiskoulutuksenKieliJaViestintaosaamisenSuoritus2022'
import { VSTKotoutumiskoulutuksenOsasuorituksenArviointi2022 } from '../types/fi/oph/koski/schema/VSTKotoutumiskoulutuksenOsasuorituksenArviointi2022'
import { VSTKotoutumiskoulutuksenValinnaistenOpintojenOsasuoritus2022 } from '../types/fi/oph/koski/schema/VSTKotoutumiskoulutuksenValinnaistenOpintojenOsasuoritus2022'
import { VSTKotoutumiskoulutuksenYhteiskuntaJaTyöelämäosaaminenSuoritus2022 } from '../types/fi/oph/koski/schema/VSTKotoutumiskoulutuksenYhteiskuntaJaTyoelamaosaaminenSuoritus2022'
import { assertNever } from '../util/selfcare'
import { CreateVSTArviointi, CreateVSTTaitotaso } from './typeguards'

/**
 * Selvittää osasuorituksen tyypin perusteella, minkälaisen arviointiprototypen käyttöliittymälle tarjotaan.
 */
export const createVstArviointi: CreateVSTArviointi = (o) => (arvosana) => {
  const c = o.$class
  switch (c) {
    case VSTKotoutumiskoulutuksenKieliJaViestintäosaamisenSuoritus2022.className:
      return VSTKotoutumiskoulutuksenOsasuorituksenArviointi2022({
        arvosana,
        päivä: todayISODate()
      })
    case VSTKotoutumiskoulutuksenValinnaistenOpintojenOsasuoritus2022.className:
      return VSTKotoutumiskoulutuksenOsasuorituksenArviointi2022({
        arvosana,
        päivä: todayISODate()
      })
    case VSTKotoutumiskoulutuksenYhteiskuntaJaTyöelämäosaaminenSuoritus2022.className:
      return VSTKotoutumiskoulutuksenOsasuorituksenArviointi2022({
        arvosana,
        päivä: todayISODate()
      })
    case VapaanSivistystyönJotpaKoulutuksenOsasuorituksenSuoritus.className:
      return VapaanSivistystyöJotpaKoulutuksenArviointi({
        arvosana,
        päivä: todayISODate()
      })
    case VapaanSivistystyönLukutaitokoulutuksenKokonaisuudenSuoritus.className:
      return LukutaitokoulutuksenArviointi({
        arvosana,
        päivä: todayISODate(),
        // TODO: Tarkista, onko ok
        taitotaso: Koodistokoodiviite({
          koodistoUri: 'arviointiasteikkokehittyvankielitaidontasot',
          koodiarvo: 'A1.1'
        })
      })
    case VapaanSivistystyönMaahanmuuttajienKotoutumiskoulutuksenKieliopintojenSuoritus.className:
      return VapaanSivistystyönMaahanmuuttajienKotoutumiskoulutuksenKieliopintojenArviointi(
        {
          arvosana,
          päivä: todayISODate()
        }
      )
    case VapaanSivistystyönMaahanmuuttajienKotoutumiskoulutuksenOhjauksenSuoritus.className:
      return OppivelvollisilleSuunnatunVapaanSivistystyönOpintokokonaisuudenArviointi(
        {
          arvosana,
          päivä: todayISODate()
        }
      )
    case VapaanSivistystyönMaahanmuuttajienKotoutumiskoulutuksenTyöelämäJaYhteiskuntataitojenOpintojenSuoritus.className:
      return OppivelvollisilleSuunnatunVapaanSivistystyönOpintokokonaisuudenArviointi(
        {
          arvosana,
          päivä: todayISODate()
        }
      )
    case VapaanSivistystyönMaahanmuuttajienKotoutumiskoulutuksenValinnaistenOpintojenSuoritus.className:
      return OppivelvollisilleSuunnatunVapaanSivistystyönOpintokokonaisuudenArviointi(
        {
          arvosana,
          päivä: todayISODate()
        }
      )
    case VapaanSivistystyönVapaatavoitteisenKoulutuksenOsasuorituksenSuoritus.className:
      return VapaanSivistystyöVapaatavoitteisenKoulutuksenArviointi({
        arvosana,
        päivä: todayISODate()
      })
    case VSTKotoutumiskoulutuksenKieliJaViestintäosaamisenOsasuoritus.className:
      return VSTKotoutumiskoulutuksenKieliJaViestintäosaamisenArviointi({
        arvosana,
        arviointipäivä: todayISODate()
      })
    case 'fi.oph.koski.schema.VapaanSivistystyönMaahanmuuttajienKotoutumiskoulutuksenTyöelämäJaYhteiskuntataidot':
    case 'fi.oph.koski.schema.VapaanSivistystyönMaahanmuuttajienKotoutumiskoulutuksenTyöelämäJaYhteiskuntataitojenTyöelämäJakso':
    case 'fi.oph.koski.schema.VapaanSivistystyönMaahanmuuttajienKotoutumiskoulutuksenValinnaistenOpintojenOsasuoritus':
    case 'fi.oph.koski.schema.OppivelvollisilleSuunnatunVapaanSivistystyönOpintokokonaisuudenSuoritus':
    case 'fi.oph.koski.schema.MuuallaSuoritettuOppivelvollisilleSuunnatunVapaanSivistystyönOpintojenSuoritus':
      throw new Error('Not yet implemented')
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
      return VapaanSivistystyönJotpaKoulutuksenOpiskeluoikeusjakso.className
    case 'vstmaahanmuuttajienkotoutumiskoulutus':
    case 'vstlukutaitokoulutus':
    case 'vstoppivelvollisillesuunnattukoulutus':
      return OppivelvollisilleSuunnattuVapaanSivistystyönOpiskeluoikeusjakso.className
    default:
      return assertNever(koodiarvo)
  }
}
