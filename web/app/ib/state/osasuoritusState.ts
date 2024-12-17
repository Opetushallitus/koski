import { useMemo } from 'react'
import {
  DialogField,
  useDialogField
} from '../../components-v2/createdialog/DialogField'
import { Oppiaine } from '../../components-v2/opiskeluoikeus/OppiaineTable'
import {
  isKoodistokoodiviite,
  Koodistokoodiviite
} from '../../types/fi/oph/koski/schema/Koodistokoodiviite'
import { LaajuusKursseissa } from '../../types/fi/oph/koski/schema/LaajuusKursseissa'
import { LocalizedString } from '../../types/fi/oph/koski/schema/LocalizedString'
import { isPaikallinenKoodi } from '../../types/fi/oph/koski/schema/PaikallinenKoodi'
import { KoulutusmoduuliOf } from '../../util/schema'
import {
  IBOsasuoritusTunniste,
  PreIBKurssiProps
} from '../oppiaineet/preIBKurssi2015'
import { uusiPaikallinenKey } from './options'
import { koodistokoodiviiteId, koodiviiteId } from '../../util/koodisto'

export const UusiPaikallinenLukionKurssiKey = uusiPaikallinenKey('lukio')
export const UusiIBKurssiKey = uusiPaikallinenKey('ib')

export type IBOsasuoritusState<T> = {
  tunniste: DialogField<IBOsasuoritusTunniste>
  uusiTyyppi: DialogField<UusiOsasuoritustyyppi>
  lukiokurssinTyyppi: DialogField<Koodistokoodiviite<'lukionkurssintyyppi'>>
  kuvaus: DialogField<LocalizedString>
  pakollinen: DialogField<boolean>
  laajuus: DialogField<LaajuusKursseissa>
  isPaikallinen: boolean
  result?: T
}

export type UusiOsasuoritustyyppi = 'lukio' | 'ib'

export const useIBOsasuoritusState = <T>(
  createOsasuoritus: (p: PreIBKurssiProps) => T
): IBOsasuoritusState<T> => {
  const tunniste = useDialogField<IBOsasuoritusTunniste>(true)
  const uusiTyyppi = useDialogField<UusiOsasuoritustyyppi>(false)

  const isLukioValtakunnallinen = !uusiTyyppi.value
  const isLukioPaikallinen = uusiTyyppi.value === 'lukio'
  const isIBKurssi = uusiTyyppi.value === 'ib'

  const lukiokurssinTyyppi = useDialogField<
    Koodistokoodiviite<'lukionkurssintyyppi'>
  >(isLukioValtakunnallinen || isLukioPaikallinen)

  const pakollinen = useDialogField<boolean>(isIBKurssi)

  const kuvaus = useDialogField<LocalizedString>(
    isLukioPaikallinen || isIBKurssi
  )

  const laajuus = useDialogField<LaajuusKursseissa>(
    isLukioPaikallinen || isIBKurssi,
    () => LaajuusKursseissa({ arvo: 1 })
  )

  const result = useMemo(
    () =>
      createOsasuoritus({
        tunniste: tunniste.value,
        lukiokurssinTyyppi: lukiokurssinTyyppi.value,
        kuvaus: kuvaus.value,
        pakollinen: pakollinen.value,
        laajuus: laajuus.value
      }),
    [
      createOsasuoritus,
      tunniste.value,
      lukiokurssinTyyppi.value,
      kuvaus.value,
      pakollinen.value,
      laajuus.value
    ]
  )

  return {
    tunniste,
    uusiTyyppi,
    lukiokurssinTyyppi,
    kuvaus,
    pakollinen,
    laajuus,
    isPaikallinen: isLukioPaikallinen || isIBKurssi,
    result
  }
}
