import { Koodistokoodiviite } from '../../types/fi/oph/koski/schema/Koodistokoodiviite'
import { LocalizedString } from '../../types/fi/oph/koski/schema/LocalizedString'
import { LukionModuuliOppiaineissa2019 } from '../../types/fi/oph/koski/schema/LukionModuuliOppiaineissa2019'
import { LukionOppiaineenSuoritus2019 } from '../../types/fi/oph/koski/schema/LukionOppiaineenSuoritus2019'
import { MuidenLukioOpintojenPreIBSuoritus2019 } from '../../types/fi/oph/koski/schema/MuidenLukioOpintojenPreIBSuoritus2019'
import { PaikallinenKoodi } from '../../types/fi/oph/koski/schema/PaikallinenKoodi'

export type PreIB2019ModuuliProps = {
  oppiaineenTunniste: Koodistokoodiviite<'koskioppiaineetyleissivistava'>
  tunniste?: PreIB2019OsasuoritusTunniste
  matematiikanOppimäärä?: Koodistokoodiviite<'oppiainematematiikka'>
  äidinkielenKieli?: Koodistokoodiviite<'oppiaineaidinkielijakirjallisuus'>
  vierasKieli?: Koodistokoodiviite<'kielivalikoima'>
  kuvaus?: LocalizedString
  pakollinen?: boolean
}

export type PreIB2019OsasuoritusTunniste =
  | Koodistokoodiviite<'moduulikoodistolops2021'>
  | PaikallinenKoodi

export type PreIBModuulinSuoritus2019 =
  | MuidenLukioOpintojenPreIBSuoritus2019
  | LukionOppiaineenSuoritus2019

export const createPreIBModuulinSuoritus2019 = (
  props: PreIB2019ModuuliProps
): LukionModuuliOppiaineissa2019 | null => null
