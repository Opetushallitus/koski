import { useMemo } from 'react'
import {
  DialogField,
  useDialogField
} from '../../components-v2/createdialog/DialogField'
import { IBAineRyhmäOppiaine } from '../../types/fi/oph/koski/schema/IBAineRyhmaOppiaine'
import { isIBOppiaineLanguage } from '../../types/fi/oph/koski/schema/IBOppiaineLanguage'
import { IBTutkinnonOppiaineenSuoritus } from '../../types/fi/oph/koski/schema/IBTutkinnonOppiaineenSuoritus'
import { Koodistokoodiviite } from '../../types/fi/oph/koski/schema/Koodistokoodiviite'
import { LocalizedString } from '../../types/fi/oph/koski/schema/LocalizedString'
import {
  createIBTutkinnonOppiaine,
  DPCoreOppiaineet
} from '../oppiaineet/ibTutkintoOppiaine'
import { isIBOppiaineLanguageTunniste } from '../oppiaineet/tunnisteet'

export type UusiIBTutkintoOppiaineState = {
  tunniste: DialogField<Koodistokoodiviite<'oppiaineetib'>>
  kieli: DialogField<Koodistokoodiviite<'kielivalikoima'>>
  ryhmä: DialogField<Koodistokoodiviite<'aineryhmaib'>>
  taso: DialogField<Koodistokoodiviite<'oppiaineentasoib'>>
  pakollinen: DialogField<boolean>
  extendedEssay: {
    tunniste: DialogField<Koodistokoodiviite<'oppiaineetib'>>
    kieli: DialogField<Koodistokoodiviite<'kielivalikoima'>>
    ryhmä: DialogField<Koodistokoodiviite<'aineryhmaib'>>
    taso: DialogField<Koodistokoodiviite<'oppiaineentasoib'>>
    pakollinen: DialogField<boolean>
    aihe: DialogField<LocalizedString>
  }
  result: IBTutkinnonOppiaineenSuoritus | null
}

export const useIBTutkintoOppiaineState = (
  initial?: IBAineRyhmäOppiaine
): UusiIBTutkintoOppiaineState => {
  const tunniste = useDialogField<Koodistokoodiviite<'oppiaineetib'>>(
    true,
    () => initial?.tunniste
  )
  const ibOppiaineSelected =
    tunniste.value !== undefined &&
    !DPCoreOppiaineet.includes(tunniste.value.koodiarvo)
  const coreOppiaineSelected =
    tunniste.value !== undefined &&
    DPCoreOppiaineet.includes(tunniste.value.koodiarvo)
  const extendedEssaySelected = tunniste.value?.koodiarvo === 'EE'

  const kieli = useDialogField<Koodistokoodiviite<'kielivalikoima'>>(
    isIBOppiaineLanguageTunniste(tunniste.value),
    () => (isIBOppiaineLanguage(initial) ? initial.kieli : undefined)
  )

  const ryhmä = useDialogField<Koodistokoodiviite<'aineryhmaib'>>(
    ibOppiaineSelected,
    () => initial?.ryhmä
  )

  const taso = useDialogField<Koodistokoodiviite<'oppiaineentasoib'>>(
    ibOppiaineSelected,
    () => initial?.taso
  )

  const pakollinen = useDialogField<boolean>(
    ibOppiaineSelected || coreOppiaineSelected,
    () => initial?.pakollinen
  )

  const essayTunniste = useDialogField<Koodistokoodiviite<'oppiaineetib'>>(
    extendedEssaySelected
  )

  const essayKieli = useDialogField<Koodistokoodiviite<'kielivalikoima'>>(
    isIBOppiaineLanguageTunniste(essayTunniste.value)
  )
  const essayRyhmä = useDialogField<Koodistokoodiviite<'aineryhmaib'>>(true)
  const essayTaso = useDialogField<Koodistokoodiviite<'oppiaineentasoib'>>(true)
  const essayPakollinen = useDialogField<boolean>(true)
  const essayAihe = useDialogField<LocalizedString>(extendedEssaySelected)

  const result = useMemo(
    () =>
      createIBTutkinnonOppiaine({
        tunniste: tunniste.value,
        kieli: kieli.value,
        ryhmä: ryhmä.value,
        taso: taso.value,
        pakollinen: pakollinen.value,
        extendedEssay: {
          tunniste: essayTunniste.value,
          kieli: essayKieli.value,
          ryhmä: essayRyhmä.value,
          taso: essayTaso.value,
          pakollinen: essayPakollinen.value,
          aihe: essayAihe.value
        }
      }),
    [
      essayAihe.value,
      essayKieli.value,
      essayPakollinen.value,
      essayRyhmä.value,
      essayTaso.value,
      essayTunniste.value,
      kieli.value,
      pakollinen.value,
      ryhmä.value,
      taso.value,
      tunniste.value
    ]
  )

  return {
    tunniste,
    kieli,
    ryhmä,
    taso,
    pakollinen,
    extendedEssay: {
      tunniste: essayTunniste,
      kieli: essayKieli,
      ryhmä: essayRyhmä,
      taso: essayTaso,
      pakollinen: essayPakollinen,
      aihe: essayAihe
    },
    result
  }
}
