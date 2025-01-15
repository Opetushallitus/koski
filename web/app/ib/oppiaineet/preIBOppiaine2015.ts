import { t } from '../../i18n/i18n'
import { IBOppiaineLanguage } from '../../types/fi/oph/koski/schema/IBOppiaineLanguage'
import { IBOppiaineMuu } from '../../types/fi/oph/koski/schema/IBOppiaineMuu'
import { LukionÄidinkieliJaKirjallisuus2015 } from '../../types/fi/oph/koski/schema/LukionAidinkieliJaKirjallisuus2015'
import { LukionMatematiikka2015 } from '../../types/fi/oph/koski/schema/LukionMatematiikka2015'
import { LukionMuuValtakunnallinenOppiaine2015 } from '../../types/fi/oph/koski/schema/LukionMuuValtakunnallinenOppiaine2015'
import { LukionUskonto2015 } from '../../types/fi/oph/koski/schema/LukionUskonto2015'
import { PaikallinenLukionOppiaine2015 } from '../../types/fi/oph/koski/schema/PaikallinenLukionOppiaine2015'
import { PreIBOppiaine2015 } from '../../types/fi/oph/koski/schema/PreIBOppiaine2015'
import { PreIBOppiaineenSuoritus2015 } from '../../types/fi/oph/koski/schema/PreIBOppiaineenSuoritus2015'
import { PreIBSuorituksenOsasuoritus2015 } from '../../types/fi/oph/koski/schema/PreIBSuorituksenOsasuoritus2015'
import { VierasTaiToinenKotimainenKieli2015 } from '../../types/fi/oph/koski/schema/VierasTaiToinenKotimainenKieli2015'
import { PreIBOppiaineProps } from '../state/preIBOppiaine'
import {
  isIBOppiaineLanguageTunniste,
  isIBOppiaineMuuTunniste,
  isLukionMatematiikka2015Tunniste,
  isLukionMuuValtakunnallinenOppiaine2015Tunniste,
  isLukionUskonto2015Tunniste,
  isLukionÄidinkieliJaKirjallisuus2015Tunniste,
  isValidPaikallinenKoodi,
  isVierasTaiToinenKotimainenKieli2015Tunniste
} from './tunnisteet'

export const createPreIBSuorituksenOsasuoritus2015 = (
  props: PreIBOppiaineProps
): PreIBSuorituksenOsasuoritus2015 | null => {
  const koulutusmoduuli = createPreIBOppiaine2015(props)
  return (
    koulutusmoduuli &&
    PreIBOppiaineenSuoritus2015({
      koulutusmoduuli
    })
  )
}

const createPreIBOppiaine2015 = ({
  tunniste,
  paikallinenTunniste,
  kieli,
  ryhmä,
  matematiikanOppimäärä,
  äidinkielenKieli,
  paikallinenKuvaus
}: PreIBOppiaineProps): PreIBOppiaine2015 | null => {
  if (isIBOppiaineLanguageTunniste(tunniste)) {
    return kieli && ryhmä
      ? IBOppiaineLanguage({
          pakollinen: true,
          kieli,
          ryhmä,
          tunniste
        })
      : null
  }

  if (isIBOppiaineMuuTunniste(tunniste)) {
    return ryhmä
      ? IBOppiaineMuu({
          pakollinen: false,
          ryhmä,
          tunniste
        })
      : null
  }

  if (isLukionMatematiikka2015Tunniste(tunniste)) {
    return matematiikanOppimäärä
      ? LukionMatematiikka2015({
          pakollinen: true,
          oppimäärä: matematiikanOppimäärä
        })
      : null
  }

  if (isLukionMuuValtakunnallinenOppiaine2015Tunniste(tunniste)) {
    return LukionMuuValtakunnallinenOppiaine2015({
      tunniste,
      pakollinen: true
    })
  }

  if (isLukionUskonto2015Tunniste(tunniste)) {
    return LukionUskonto2015({ pakollinen: true })
  }

  if (isLukionÄidinkieliJaKirjallisuus2015Tunniste(tunniste)) {
    return äidinkielenKieli
      ? LukionÄidinkieliJaKirjallisuus2015({
          pakollinen: true,
          kieli: äidinkielenKieli
        })
      : null
  }

  if (isVierasTaiToinenKotimainenKieli2015Tunniste(tunniste)) {
    return kieli
      ? VierasTaiToinenKotimainenKieli2015({
          pakollinen: true,
          kieli,
          tunniste
        })
      : null
  }

  if (isValidPaikallinenKoodi(paikallinenTunniste) && paikallinenKuvaus) {
    return PaikallinenLukionOppiaine2015({
      pakollinen: true,
      kuvaus: paikallinenKuvaus,
      tunniste: paikallinenTunniste
    })
  }

  return null
}
