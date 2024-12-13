import { IBKurssi } from '../../types/fi/oph/koski/schema/IBKurssi'
import { Koodistokoodiviite } from '../../types/fi/oph/koski/schema/Koodistokoodiviite'
import { LaajuusKursseissa } from '../../types/fi/oph/koski/schema/LaajuusKursseissa'
import { LocalizedString } from '../../types/fi/oph/koski/schema/LocalizedString'
import {
  isPaikallinenKoodi,
  PaikallinenKoodi
} from '../../types/fi/oph/koski/schema/PaikallinenKoodi'
import { PaikallinenLukionKurssi2015 } from '../../types/fi/oph/koski/schema/PaikallinenLukionKurssi2015'
import { PreIBKurssi2015 } from '../../types/fi/oph/koski/schema/PreIBKurssi2015'
import { PreIBKurssinSuoritus2015 } from '../../types/fi/oph/koski/schema/PreIBKurssinSuoritus2015'
import { ValtakunnallinenLukionKurssi2015 } from '../../types/fi/oph/koski/schema/ValtakunnallinenLukionKurssi2015'

export type PreIBKurssiProps = {
  tunniste?: IBOsasuoritusTunniste
  lukiokurssinTyyppi?: Koodistokoodiviite<'lukionkurssintyyppi'>
  kuvaus?: LocalizedString
  pakollinen?: boolean
  laajuus?: LaajuusKursseissa
}

export type IBOsasuoritusTunniste =
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

export const createPreIBKurssinSuoritus2015 = (props: PreIBKurssiProps) => {
  const koulutusmoduuli = createPreIBKurssi2015(props)
  return (
    koulutusmoduuli &&
    PreIBKurssinSuoritus2015({
      koulutusmoduuli
    })
  )
}

const createPreIBKurssi2015 = ({
  tunniste,
  lukiokurssinTyyppi,
  kuvaus,
  pakollinen,
  laajuus
}: PreIBKurssiProps): PreIBKurssi2015 | null => {
  if (lukiokurssinTyyppi && tunniste && !isPaikallinenKoodi(tunniste)) {
    return ValtakunnallinenLukionKurssi2015({
      tunniste,
      kurssinTyyppi: lukiokurssinTyyppi,
      laajuus
    })
  }

  if (lukiokurssinTyyppi && isPaikallinenKoodi(tunniste) && kuvaus) {
    return PaikallinenLukionKurssi2015({
      tunniste,
      kurssinTyyppi: lukiokurssinTyyppi,
      kuvaus,
      laajuus
    })
  }

  if (isPaikallinenKoodi(tunniste) && kuvaus) {
    return IBKurssi({
      tunniste,
      kuvaus,
      pakollinen: !!pakollinen,
      laajuus
    })
  }

  return null
}
