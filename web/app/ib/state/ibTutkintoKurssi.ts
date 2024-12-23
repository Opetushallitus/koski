import { useMemo } from 'react'
import {
  DialogField,
  useDialogField
} from '../../components-v2/createdialog/DialogField'
import { IBKurssinSuoritus } from '../../types/fi/oph/koski/schema/IBKurssinSuoritus'
import { Koodistokoodiviite } from '../../types/fi/oph/koski/schema/Koodistokoodiviite'
import { LaajuusKursseissa } from '../../types/fi/oph/koski/schema/LaajuusKursseissa'
import { LocalizedString } from '../../types/fi/oph/koski/schema/LocalizedString'
import { PaikallinenKoodi } from '../../types/fi/oph/koski/schema/PaikallinenKoodi'
import { createIBKurssinSuoritus } from '../oppiaineet/ibTutkintoKurssi'
import { uusiPaikallinenKey } from './options'

export const UusiIBKurssiKey = uusiPaikallinenKey('ib')

export type IBTutkintoKurssiState = {
  tunniste: DialogField<PaikallinenKoodi>
  kuvaus: DialogField<LocalizedString>
  laajuus: DialogField<LaajuusKursseissa>
  pakollinen: DialogField<boolean>
  suorituskieli: DialogField<Koodistokoodiviite<'kieli'>>
  result: IBKurssinSuoritus | null
}

export const useIBTutkintoKurssiState = (): IBTutkintoKurssiState => {
  const tunniste = useDialogField<PaikallinenKoodi>(true)
  const tunnisteSelected = !!tunniste.value

  const kuvaus = useDialogField<LocalizedString>(tunnisteSelected)

  const laajuus = useDialogField<LaajuusKursseissa>(tunnisteSelected, () =>
    LaajuusKursseissa({ arvo: 1 })
  )

  const pakollinen = useDialogField<boolean>(tunnisteSelected)

  const suorituskieli =
    useDialogField<Koodistokoodiviite<'kieli'>>(tunnisteSelected)

  const result = useMemo(
    () =>
      createIBKurssinSuoritus({
        tunniste: tunniste.value,
        kuvaus: kuvaus.value,
        laajuus: laajuus.value,
        pakollinen: pakollinen.value,
        suorituskieli: suorituskieli.value
      }),
    [
      tunniste.value,
      kuvaus.value,
      laajuus.value,
      pakollinen.value,
      suorituskieli.value
    ]
  )

  return {
    tunniste,
    kuvaus,
    laajuus,
    pakollinen,
    suorituskieli,
    result
  }
}
