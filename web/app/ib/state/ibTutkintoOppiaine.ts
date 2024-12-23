import { useMemo } from 'react'
import {
  DialogField,
  useDialogField
} from '../../components-v2/createdialog/DialogField'
import { IBOppiaineenSuoritus } from '../../types/fi/oph/koski/schema/IBOppiaineenSuoritus'
import { Koodistokoodiviite } from '../../types/fi/oph/koski/schema/Koodistokoodiviite'
import { createIBOppiaineenSuoritus } from '../oppiaineet/ibTutkintoOppiaine'
import { isIBOppiaineLanguageTunniste } from '../oppiaineet/tunnisteet'
import { init } from 'ramda'
import { isIBOppiaineLanguage } from '../../types/fi/oph/koski/schema/IBOppiaineLanguage'
import { IBExtendedEssaySuoritus } from '../../types/fi/oph/koski/schema/IBExtendedEssaySuoritus'
import { IBAineRyhmäOppiaine } from '../../types/fi/oph/koski/schema/IBAineRyhmaOppiaine'

export type UusiIBTutkintoOppiaineState = {
  tunniste: DialogField<Koodistokoodiviite<'oppiaineetib'>>
  kieli: DialogField<Koodistokoodiviite<'kielivalikoima'>>
  ryhmä: DialogField<Koodistokoodiviite<'aineryhmaib'>>
  taso: DialogField<Koodistokoodiviite<'oppiaineentasoib'>>
  pakollinen: DialogField<boolean>
  result: IBOppiaineenSuoritus | null
}

export const useIBTutkintoOppiaineState = (
  initial?: IBAineRyhmäOppiaine
): UusiIBTutkintoOppiaineState => {
  const tunniste = useDialogField<Koodistokoodiviite<'oppiaineetib'>>(
    true,
    () => initial?.tunniste
  )
  const tunnisteSelected = !!tunniste.value

  const kieli = useDialogField<Koodistokoodiviite<'kielivalikoima'>>(
    isIBOppiaineLanguageTunniste(tunniste.value),
    () => (isIBOppiaineLanguage(initial) ? initial.kieli : undefined)
  )

  const ryhmä = useDialogField<Koodistokoodiviite<'aineryhmaib'>>(
    tunnisteSelected,
    () => initial?.ryhmä
  )

  const taso = useDialogField<Koodistokoodiviite<'oppiaineentasoib'>>(
    tunnisteSelected,
    () => initial?.taso
  )

  const pakollinen = useDialogField<boolean>(
    tunnisteSelected,
    () => initial?.pakollinen
  )

  const result = useMemo(
    () =>
      createIBOppiaineenSuoritus({
        tunniste: tunniste.value,
        kieli: kieli.value,
        ryhmä: ryhmä.value,
        taso: taso.value,
        pakollinen: pakollinen.value
      }),
    [kieli.value, pakollinen.value, ryhmä.value, taso.value, tunniste.value]
  )

  return {
    tunniste,
    kieli,
    ryhmä,
    taso,
    pakollinen,
    result
  }
}
