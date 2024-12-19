import { useMemo } from 'react'
import {
  DialogField,
  useDialogField
} from '../../components-v2/createdialog/DialogField'
import { LocalizedString } from '../../types/fi/oph/koski/schema/LocalizedString'
import {
  createPreIBKurssinSuoritus2019,
  PreIB2019OsasuoritusTunniste,
  PreIBKurssinSuoritus2019
} from '../oppiaineet/preIBModuuli2019'
import { uusiPaikallinenKey } from './options'
import { Koodistokoodiviite } from '../../types/fi/oph/koski/schema/Koodistokoodiviite'
import {
  isLukionMatematiikka2019Tunniste,
  isLukionÄidinkieliJaKirjallisuus2019Tunniste,
  isVierasTaiToinenKotimainenKieli2019Tunniste
} from '../oppiaineet/tunnisteet'
import { isPaikallinenKoodi } from '../../types/fi/oph/koski/schema/PaikallinenKoodi'

export const UusiPaikallinenLukionKurssiKey = uusiPaikallinenKey('lukio')

export type PreIB2019OsasuoritusState = {
  tunniste: DialogField<PreIB2019OsasuoritusTunniste>
  matematiikanOppimäärä: DialogField<Koodistokoodiviite<'oppiainematematiikka'>>
  äidinkielenKieli: DialogField<
    Koodistokoodiviite<'oppiaineaidinkielijakirjallisuus'>
  >
  vierasKieli: DialogField<Koodistokoodiviite<'kielivalikoima'>>
  kuvaus: DialogField<LocalizedString>
  pakollinen: DialogField<boolean>
  isPaikallinen: boolean
  result: PreIBKurssinSuoritus2019 | null
}

export const usePreIB2019OsasuoritusState = (): PreIB2019OsasuoritusState => {
  const tunniste = useDialogField<PreIB2019OsasuoritusTunniste>(true)

  const pakollinen = useDialogField<boolean>(false)

  const matematiikanOppimäärä = useDialogField<
    Koodistokoodiviite<'oppiainematematiikka'>
  >(isLukionMatematiikka2019Tunniste(tunniste.value))

  const äidinkielenKieli = useDialogField<
    Koodistokoodiviite<'oppiaineaidinkielijakirjallisuus'>
  >(isLukionÄidinkieliJaKirjallisuus2019Tunniste(tunniste.value))

  const vierasKieli = useDialogField<Koodistokoodiviite<'kielivalikoima'>>(
    isVierasTaiToinenKotimainenKieli2019Tunniste(tunniste.value)
  )

  const kuvaus = useDialogField<LocalizedString>(
    isPaikallinenKoodi(tunniste.value)
  )

  const result = useMemo(
    () =>
      createPreIBKurssinSuoritus2019({
        tunniste: tunniste.value,
        matematiikanOppimäärä: matematiikanOppimäärä.value,
        äidinkielenKieli: äidinkielenKieli.value,
        vierasKieli: vierasKieli.value,
        kuvaus: kuvaus.value,
        pakollinen: pakollinen.value
      }),
    [
      tunniste.value,
      matematiikanOppimäärä.value,
      äidinkielenKieli.value,
      vierasKieli.value,
      kuvaus.value,
      pakollinen.value
    ]
  )

  return {
    tunniste,
    matematiikanOppimäärä,
    äidinkielenKieli,
    vierasKieli,
    kuvaus,
    pakollinen,
    isPaikallinen: isPaikallinenKoodi(tunniste.value),
    result
  }
}
