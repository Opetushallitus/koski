import { Lukiodiplomit2019 } from '../../types/fi/oph/koski/schema/Lukiodiplomit2019'
import { LukionÄidinkieliJaKirjallisuus2019 } from '../../types/fi/oph/koski/schema/LukionAidinkieliJaKirjallisuus2019'
import { LukionMatematiikka2019 } from '../../types/fi/oph/koski/schema/LukionMatematiikka2019'
import { LukionMuuValtakunnallinenOppiaine2019 } from '../../types/fi/oph/koski/schema/LukionMuuValtakunnallinenOppiaine2019'
import { LukionOppiaineenPreIBSuoritus2019 } from '../../types/fi/oph/koski/schema/LukionOppiaineenPreIBSuoritus2019'
import { LukionUskonto2019 } from '../../types/fi/oph/koski/schema/LukionUskonto2019'
import { MuidenLukioOpintojenPreIBSuoritus2019 } from '../../types/fi/oph/koski/schema/MuidenLukioOpintojenPreIBSuoritus2019'
import { MuutLukionSuoritukset2019 } from '../../types/fi/oph/koski/schema/MuutLukionSuoritukset2019'
import { PaikallinenLukionOppiaine2019 } from '../../types/fi/oph/koski/schema/PaikallinenLukionOppiaine2019'
import { PreIBLukionOppiaine2019 } from '../../types/fi/oph/koski/schema/PreIBLukionOppiaine2019'
import { PreIBMuutSuorituksetTaiVastaavat2019 } from '../../types/fi/oph/koski/schema/PreIBMuutSuorituksetTaiVastaavat2019'
import { PreIBSuorituksenOsasuoritus2019 } from '../../types/fi/oph/koski/schema/PreIBSuorituksenOsasuoritus2019'
import { TemaattisetOpinnot2019 } from '../../types/fi/oph/koski/schema/TemaattisetOpinnot2019'
import { VierasTaiToinenKotimainenKieli2019 } from '../../types/fi/oph/koski/schema/VierasTaiToinenKotimainenKieli2019'
import { PreIBOppiaineProps } from '../state/preIBOppiaine'
import {
  isLukiodiplomit2019Tunniste,
  isLukionMatematiikka2019Tunniste,
  isLukionMuuValtakunnallinenOppiaine2019Tunniste,
  isLukionUskonto2019Tunniste,
  isLukionÄidinkieliJaKirjallisuus2019Tunniste,
  isMuutLukionSuoritukset2019Tunniste,
  isTemaattisetOpinnot2019Tunniste,
  isVierasTaiToinenKotimainenKieli2019Tunniste
} from './tunnisteet'

export const createPreIBSuorituksenOsasuoritus2019 = (
  props: PreIBOppiaineProps
): PreIBSuorituksenOsasuoritus2019 | null =>
  createLukionOppiaineenPreIBSuoritus2019(props) ||
  createMuidenLukioOpintojenPreIBSuoritus2019(props)

const createLukionOppiaineenPreIBSuoritus2019 = (
  props: PreIBOppiaineProps
): LukionOppiaineenPreIBSuoritus2019 | null => {
  const koulutusmoduuli = createPreIBLukionOppiaine2019(props)
  return (
    koulutusmoduuli &&
    LukionOppiaineenPreIBSuoritus2019({
      koulutusmoduuli
    })
  )
}

const createPreIBLukionOppiaine2019 = ({
  tunniste,
  matematiikanOppimäärä,
  äidinkielenKieli,
  paikallinenTunniste,
  paikallinenKuvaus,
  kieli
}: PreIBOppiaineProps): PreIBLukionOppiaine2019 | null => {
  if (isLukionMatematiikka2019Tunniste(tunniste)) {
    return matematiikanOppimäärä
      ? LukionMatematiikka2019({
          pakollinen: true,
          oppimäärä: matematiikanOppimäärä
        })
      : null
  }

  if (isLukionMuuValtakunnallinenOppiaine2019Tunniste(tunniste)) {
    return LukionMuuValtakunnallinenOppiaine2019({
      tunniste,
      pakollinen: true
    })
  }

  if (isLukionUskonto2019Tunniste(tunniste)) {
    return LukionUskonto2019({
      pakollinen: true
    })
  }

  if (isLukionÄidinkieliJaKirjallisuus2019Tunniste(tunniste)) {
    return äidinkielenKieli
      ? LukionÄidinkieliJaKirjallisuus2019({
          kieli: äidinkielenKieli,
          pakollinen: true
        })
      : null
  }

  if (isVierasTaiToinenKotimainenKieli2019Tunniste(tunniste)) {
    return kieli
      ? VierasTaiToinenKotimainenKieli2019({
          tunniste,
          kieli,
          pakollinen: true
        })
      : null
  }

  if (paikallinenTunniste && paikallinenKuvaus) {
    return PaikallinenLukionOppiaine2019({
      tunniste: paikallinenTunniste,
      kuvaus: paikallinenKuvaus,
      pakollinen: true
    })
  }

  return null
}

const createMuidenLukioOpintojenPreIBSuoritus2019 = (
  props: PreIBOppiaineProps
): MuidenLukioOpintojenPreIBSuoritus2019 | null => {
  const koulutusmoduuli = createPreIBMuutSuorituksetTaiVastaavat2019(props)
  return (
    koulutusmoduuli &&
    MuidenLukioOpintojenPreIBSuoritus2019({
      koulutusmoduuli
    })
  )
}

const createPreIBMuutSuorituksetTaiVastaavat2019 = ({
  tunniste
}: PreIBOppiaineProps): PreIBMuutSuorituksetTaiVastaavat2019 | null => {
  if (isLukiodiplomit2019Tunniste(tunniste)) {
    return Lukiodiplomit2019({ tunniste })
  }

  if (isMuutLukionSuoritukset2019Tunniste(tunniste)) {
    return MuutLukionSuoritukset2019({ tunniste })
  }

  if (isTemaattisetOpinnot2019Tunniste(tunniste)) {
    return TemaattisetOpinnot2019({ tunniste })
  }
  return null
}
