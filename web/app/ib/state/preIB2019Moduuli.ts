import { useMemo } from 'react'
import {
  DialogField,
  useDialogField
} from '../../components-v2/createdialog/DialogField'
import {
  isKoodistokoodiviite,
  Koodistokoodiviite
} from '../../types/fi/oph/koski/schema/Koodistokoodiviite'
import { LaajuusOpintopisteissä } from '../../types/fi/oph/koski/schema/LaajuusOpintopisteissa'
import { LocalizedString } from '../../types/fi/oph/koski/schema/LocalizedString'
import { isPaikallinenKoodi } from '../../types/fi/oph/koski/schema/PaikallinenKoodi'
import { PreIBLukionModuulinTaiPaikallisenOpintojaksonSuoritus2019 } from '../../types/fi/oph/koski/schema/PreIBLukionModuulinTaiPaikallisenOpintojaksonSuoritus2019'
import {
  createPreIBLukionModuulinTaiPaikallisenOpintojaksonSuoritus2019,
  PreIB2019ModuuliOppiaineenTunniste,
  PreIB2019OsasuoritusTunniste
} from '../oppiaineet/preIBModuuli2019'
import { isLukionVieraanKielenModuuliMuissaOpinnoissa2019OppiaineenTunniste } from '../oppiaineet/tunnisteet'
import { uusiPaikallinenKey } from './options'

export const UusiPaikallinenLukionKurssiKey = uusiPaikallinenKey('lukio')

export type PreIB2019OsasuoritusState = {
  tunniste: DialogField<PreIB2019OsasuoritusTunniste>
  kuvaus: DialogField<LocalizedString>
  laajuus: DialogField<LaajuusOpintopisteissä>
  pakollinen: DialogField<boolean>
  vierasKieli: DialogField<Koodistokoodiviite<'kielivalikoima'>>
  isPaikallinen: boolean
  result: PreIBLukionModuulinTaiPaikallisenOpintojaksonSuoritus2019 | null
}

export const usePreIB2019OsasuoritusState = (
  oppiaineenTunniste?: PreIB2019ModuuliOppiaineenTunniste
): PreIB2019OsasuoritusState => {
  const tunniste = useDialogField<PreIB2019OsasuoritusTunniste>(true)

  const kuvaus = useDialogField<LocalizedString>(
    isPaikallinenKoodi(tunniste.value)
  )

  const laajuus = useDialogField<LaajuusOpintopisteissä>(true)

  const pakollinen = useDialogField<boolean>(true)

  const vierasKieli = useDialogField<Koodistokoodiviite<'kielivalikoima'>>(
    isLukionVieraanKielenModuuliMuissaOpinnoissa2019OppiaineenTunniste(
      oppiaineenTunniste
    ) && isKoodistokoodiviite(tunniste.value)
  )

  const result = useMemo(
    () =>
      oppiaineenTunniste
        ? createPreIBLukionModuulinTaiPaikallisenOpintojaksonSuoritus2019({
            oppiaineenTunniste: oppiaineenTunniste,
            tunniste: tunniste.value,
            kuvaus: kuvaus.value,
            laajuus: laajuus.value,
            pakollinen: pakollinen.value,
            vierasKieli: vierasKieli.value
          })
        : null,
    [
      oppiaineenTunniste,
      tunniste.value,
      kuvaus.value,
      laajuus.value,
      pakollinen.value,
      vierasKieli.value
    ]
  )

  return {
    tunniste,
    laajuus,
    vierasKieli,
    kuvaus,
    pakollinen,
    isPaikallinen: isPaikallinenKoodi(tunniste.value),
    result
  }
}
