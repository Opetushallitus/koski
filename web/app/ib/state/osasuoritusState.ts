import { useMemo } from 'react'
import {
  DialogField,
  useDialogField
} from '../../components-v2/createdialog/DialogField'
import { OppiaineOsasuoritus } from '../../components-v2/opiskeluoikeus/OppiaineTable'
import { Koodistokoodiviite } from '../../types/fi/oph/koski/schema/Koodistokoodiviite'
import { LaajuusKursseissa } from '../../types/fi/oph/koski/schema/LaajuusKursseissa'
import { LocalizedString } from '../../types/fi/oph/koski/schema/LocalizedString'
import { PaikallinenKoodi } from '../../types/fi/oph/koski/schema/PaikallinenKoodi'
import { KoulutusmoduuliOf } from '../../util/schema'
import {
  LukiokurssiTunnisteUri,
  PreIBKurssiProps
} from '../oppiaineet/preIBKurssi2015'
import { Paikallinen } from './options'

export type IBOsasuoritusState<T> = {
  tunniste: DialogField<Koodistokoodiviite<LukiokurssiTunnisteUri>>
  lukiokurssinTyyppi: DialogField<Koodistokoodiviite<'lukionkurssintyyppi'>>
  paikallinenTunniste: DialogField<PaikallinenKoodi>
  kuvaus: DialogField<LocalizedString>
  pakollinen: DialogField<boolean>
  laajuus: DialogField<LaajuusKursseissa>
  result?: T
}

export const useIBOsasuoritusState = <T>(
  koulutus: KoulutusmoduuliOf<OppiaineOsasuoritus>,
  createOsasuoritus: (p: PreIBKurssiProps) => T
): IBOsasuoritusState<T> => {
  const tunniste =
    useDialogField<Koodistokoodiviite<LukiokurssiTunnisteUri>>(true)
  const paikallinen = tunniste.value === Paikallinen

  const lukiokurssinTyyppi = useDialogField<
    Koodistokoodiviite<'lukionkurssintyyppi'>
  >(tunniste.value !== undefined && !paikallinen)

  const paikallinenTunniste = useDialogField<PaikallinenKoodi>(paikallinen)

  const kuvaus = useDialogField<LocalizedString>(paikallinen)

  const pakollinen = useDialogField<boolean>(paikallinen)

  const laajuus = useDialogField<LaajuusKursseissa>(paikallinen, () =>
    LaajuusKursseissa({ arvo: 1 })
  )

  const result = useMemo(
    () =>
      createOsasuoritus({
        lukioTunniste: paikallinen ? undefined : tunniste.value,
        lukiokurssinTyyppi: lukiokurssinTyyppi.value,
        paikallinenTunniste: paikallinenTunniste.value,
        kuvaus: kuvaus.value,
        pakollinen: pakollinen.value,
        laajuus: laajuus.value
      }),
    [
      createOsasuoritus,
      paikallinen,
      tunniste.value,
      lukiokurssinTyyppi.value,
      paikallinenTunniste.value,
      kuvaus.value,
      pakollinen.value,
      laajuus.value
    ]
  )

  return {
    tunniste,
    lukiokurssinTyyppi,
    paikallinenTunniste,
    kuvaus,
    pakollinen,
    laajuus,
    result
  }
}
