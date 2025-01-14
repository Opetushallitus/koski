import { IBKurssi } from '../../types/fi/oph/koski/schema/IBKurssi'
import { Koodistokoodiviite } from '../../types/fi/oph/koski/schema/Koodistokoodiviite'
import { isLaajuusKursseissa } from '../../types/fi/oph/koski/schema/LaajuusKursseissa'
import { LaajuusOpintopisteissäTaiKursseissa } from '../../types/fi/oph/koski/schema/LaajuusOpintopisteissaTaiKursseissa'
import { LocalizedString } from '../../types/fi/oph/koski/schema/LocalizedString'
import {
  isPaikallinenKoodi,
  PaikallinenKoodi
} from '../../types/fi/oph/koski/schema/PaikallinenKoodi'
import { PaikallinenLukionKurssi2015 } from '../../types/fi/oph/koski/schema/PaikallinenLukionKurssi2015'
import { PreIBKurssi2015 } from '../../types/fi/oph/koski/schema/PreIBKurssi2015'
import { PreIBKurssinSuoritus2015 } from '../../types/fi/oph/koski/schema/PreIBKurssinSuoritus2015'
import { PreIBSuorituksenOsasuoritus2015 } from '../../types/fi/oph/koski/schema/PreIBSuorituksenOsasuoritus2015'
import { ValtakunnallinenLukionKurssi2015 } from '../../types/fi/oph/koski/schema/ValtakunnallinenLukionKurssi2015'
import { PermissiveKoodiviite } from '../../util/koodisto'
import { KoulutusmoduuliOf, TunnisteOf } from '../../util/schema'
import { isValidPaikallinenKoodi } from './tunnisteet'

export type PreIBKurssi2015Props = {
  oppiaineenTunniste: PreIB2015KurssiOppiaineenTunniste
  tunniste?: PreIB2015OsasuoritusTunniste
  lukiokurssinTyyppi?: Koodistokoodiviite<'lukionkurssintyyppi'>
  kuvaus?: LocalizedString
  pakollinen?: boolean
  laajuus?: LaajuusOpintopisteissäTaiKursseissa
}

export type PreIB2015KurssiOppiaineenTunniste = PermissiveKoodiviite<
  TunnisteOf<KoulutusmoduuliOf<PreIBSuorituksenOsasuoritus2015>>
>

export type PreIB2015OsasuoritusTunniste =
  | Koodistokoodiviite<LukiokurssiTunnisteUri>
  | PaikallinenKoodi

export const lukiokurssiTunnisteUrit: LukiokurssiTunnisteUri[] = [
  'lukionkurssit',
  'lukionkurssitops2004aikuiset',
  'lukionkurssitops2003nuoret'
]

export type LukiokurssiTunnisteUri =
  | 'lukionkurssit'
  | 'lukionkurssitops2004aikuiset'
  | 'lukionkurssitops2003nuoret'

export const createPreIBKurssinSuoritus2015 = (
  props: PreIBKurssi2015Props
): PreIBKurssinSuoritus2015 | null => {
  const koulutusmoduuli = createPreIBKurssi2015(props)
  return (
    koulutusmoduuli &&
    PreIBKurssinSuoritus2015({
      koulutusmoduuli
    })
  )
}

const createPreIBKurssi2015 = ({
  oppiaineenTunniste,
  tunniste,
  lukiokurssinTyyppi,
  kuvaus,
  pakollinen,
  laajuus
}: PreIBKurssi2015Props): PreIBKurssi2015 | null => {
  if (
    lukiokurssinTyyppi &&
    tunniste &&
    !isPaikallinenKoodi(tunniste) &&
    isLaajuusKursseissa(laajuus)
  ) {
    return ValtakunnallinenLukionKurssi2015({
      tunniste,
      kurssinTyyppi: lukiokurssinTyyppi,
      laajuus
    })
  }

  if (isValidPaikallinenKoodi(tunniste) && kuvaus) {
    if (oppiaineenTunniste.koodistoUri === 'oppiaineetib') {
      return IBKurssi({
        tunniste,
        kuvaus,
        pakollinen: !!pakollinen,
        laajuus
      })
    } else {
      return lukiokurssinTyyppi && isLaajuusKursseissa(laajuus)
        ? PaikallinenLukionKurssi2015({
            tunniste,
            kurssinTyyppi: lukiokurssinTyyppi,
            kuvaus,
            laajuus
          })
        : null
    }
  }

  return null
}
