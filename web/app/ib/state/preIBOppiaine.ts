import { useMemo } from 'react'
import { useKoodistot } from '../../appstate/koodisto'
import {
  DialogField,
  useDialogField
} from '../../components-v2/createdialog/DialogField'
import { Koodistokoodiviite } from '../../types/fi/oph/koski/schema/Koodistokoodiviite'
import { LocalizedString } from '../../types/fi/oph/koski/schema/LocalizedString'
import { PaikallinenKoodi } from '../../types/fi/oph/koski/schema/PaikallinenKoodi'
import { PreIBSuorituksenOsasuoritus2015 } from '../../types/fi/oph/koski/schema/PreIBSuorituksenOsasuoritus2015'
import { createPreIBSuorituksenOsasuoritus2015 } from '../oppiaineet/preIBOppiaine2015'
import {
  isIBOppiaineLanguageTunniste,
  isIBOppiaineMuuTunniste,
  isLukionMatematiikka2015Tunniste,
  isLukionÄidinkieliJaKirjallisuus2015Tunniste,
  isVierasTaiToinenKotimainenKieli2015Tunniste
} from '../oppiaineet/tunnisteet'

export type PreIBOppiaineProps = {
  tunniste?: PreIBOppiaineTunniste
  paikallinenTunniste?: PaikallinenKoodi
  kieli?: Koodistokoodiviite<'kielivalikoima'>
  ryhmä?: Koodistokoodiviite<'aineryhmaib'>
  matematiikanOppimäärä?: Koodistokoodiviite<'oppiainematematiikka'>
  äidinkielenKieli?: Koodistokoodiviite<'oppiaineaidinkielijakirjallisuus'>
  paikallinenKuvaus?: LocalizedString
}

