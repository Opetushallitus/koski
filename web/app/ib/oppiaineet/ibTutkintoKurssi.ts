import { IBKurssi } from '../../types/fi/oph/koski/schema/IBKurssi'
import { IBKurssinSuoritus } from '../../types/fi/oph/koski/schema/IBKurssinSuoritus'
import { Koodistokoodiviite } from '../../types/fi/oph/koski/schema/Koodistokoodiviite'
import { LaajuusOpintopisteissäTaiKursseissa } from '../../types/fi/oph/koski/schema/LaajuusOpintopisteissaTaiKursseissa'
import { LocalizedString } from '../../types/fi/oph/koski/schema/LocalizedString'
import { PaikallinenKoodi } from '../../types/fi/oph/koski/schema/PaikallinenKoodi'
import { isValidPaikallinenKoodi } from './tunnisteet'

export type IBKurssinSuoritusProps = {
  tunniste?: PaikallinenKoodi
  kuvaus?: LocalizedString
  laajuus?: LaajuusOpintopisteissäTaiKursseissa
  pakollinen?: boolean
  suorituskieli?: Koodistokoodiviite<'kieli'>
}

export const createIBKurssinSuoritus = ({
  tunniste,
  kuvaus,
  laajuus,
  pakollinen,
  suorituskieli
}: IBKurssinSuoritusProps): IBKurssinSuoritus | null =>
  isValidPaikallinenKoodi(tunniste) && kuvaus && (laajuus?.arvo || 0) > 0
    ? IBKurssinSuoritus({
        koulutusmoduuli: IBKurssi({
          kuvaus,
          tunniste,
          laajuus,
          pakollinen: !!pakollinen
        }),
        suorituskieli
      })
    : null
