import { useMemo } from 'react'
import {
  DialogField,
  useDialogField
} from '../../components-v2/createdialog/DialogField'
import { OppiaineOsasuoritus } from '../../components-v2/opiskeluoikeus/OppiaineTable'
import { isIBOppiaineLanguage } from '../../types/fi/oph/koski/schema/IBOppiaineLanguage'
import { isIBOppiaineMuu } from '../../types/fi/oph/koski/schema/IBOppiaineMuu'
import { Koodistokoodiviite } from '../../types/fi/oph/koski/schema/Koodistokoodiviite'
import { LocalizedString } from '../../types/fi/oph/koski/schema/LocalizedString'
import { isLukionOppiaine2015 } from '../../types/fi/oph/koski/schema/LukionOppiaine2015'
import { PaikallinenKoodi } from '../../types/fi/oph/koski/schema/PaikallinenKoodi'
import { isPaikallinenLukionOppiaine2015 } from '../../types/fi/oph/koski/schema/PaikallinenLukionOppiaine2015'
import { KoulutusmoduuliOf } from '../../util/schema'
import {
  LukiokurssiTunnisteUri,
  PreIBKurssiProps
} from '../oppiaineet/preIBKurssi2015'
import { LaajuusKursseissa } from '../../types/fi/oph/koski/schema/LaajuusKursseissa'

export type IBOsasuoritusState<T> = {
  lukioTunniste: DialogField<Koodistokoodiviite<LukiokurssiTunnisteUri>>
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
  const lukio = isLukionOppiaine2015(koulutus)
  const paikallinen =
    isPaikallinenLukionOppiaine2015(koulutus) ||
    isIBOppiaineLanguage(koulutus) ||
    isIBOppiaineMuu(koulutus)

  const lukioTunniste = useDialogField<
    Koodistokoodiviite<LukiokurssiTunnisteUri>
  >(lukio && !paikallinen)

  const lukiokurssinTyyppi =
    useDialogField<Koodistokoodiviite<'lukionkurssintyyppi'>>(lukio)

  const paikallinenTunniste = useDialogField<PaikallinenKoodi>(paikallinen)

  const kuvaus = useDialogField<LocalizedString>(paikallinen)

  const pakollinen = useDialogField<boolean>(paikallinen && !lukio)

  const laajuus = useDialogField<LaajuusKursseissa>(lukio || paikallinen, () =>
    LaajuusKursseissa({ arvo: 1 })
  )

  const result = useMemo(
    () =>
      createOsasuoritus({
        lukioTunniste: lukioTunniste.value,
        lukiokurssinTyyppi: lukiokurssinTyyppi.value,
        paikallinenTunniste: paikallinenTunniste.value,
        kuvaus: kuvaus.value,
        pakollinen: pakollinen.value,
        laajuus: laajuus.value
      }),
    [
      createOsasuoritus,
      kuvaus.value,
      laajuus.value,
      lukioTunniste.value,
      lukiokurssinTyyppi.value,
      paikallinenTunniste.value,
      pakollinen.value
    ]
  )

  return {
    lukioTunniste,
    lukiokurssinTyyppi,
    paikallinenTunniste,
    kuvaus,
    pakollinen,
    laajuus,
    result
  }
}
