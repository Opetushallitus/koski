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

export type PreIBKurssi2015Props = {
  tunniste?: PreIB2015OsasuoritusTunniste
  lukiokurssinTyyppi?: Koodistokoodiviite<'lukionkurssintyyppi'>
  kuvaus?: LocalizedString
  pakollinen?: boolean
  laajuus?: LaajuusKursseissa
}

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
  tunniste,
  lukiokurssinTyyppi,
  kuvaus,
  pakollinen,
  laajuus
}: PreIBKurssi2015Props): PreIBKurssi2015 | null => {
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
