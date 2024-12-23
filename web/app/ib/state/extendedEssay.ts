import { useCallback, useEffect, useMemo } from 'react'
import { ActivePäätasonSuoritus } from '../../components-v2/containers/EditorContainer'
import {
  DialogField,
  useDialogField
} from '../../components-v2/createdialog/DialogField'
import { FormModel } from '../../components-v2/forms/FormModel'
import { IBExtendedEssaySuoritus } from '../../types/fi/oph/koski/schema/IBExtendedEssaySuoritus'
import { IBOpiskeluoikeus } from '../../types/fi/oph/koski/schema/IBOpiskeluoikeus'
import { IBTutkinnonSuoritus } from '../../types/fi/oph/koski/schema/IBTutkinnonSuoritus'
import { Koodistokoodiviite } from '../../types/fi/oph/koski/schema/Koodistokoodiviite'
import { LocalizedString } from '../../types/fi/oph/koski/schema/LocalizedString'
import { createIBExtendedEssaySuoritus } from '../oppiaineet/ibTutkintoOppiaine'
import { useIBTutkintoOppiaineState } from './ibTutkintoOppiaine'
import { parasArviointi } from '../../util/arvioinnit'

export type ExtendedEssayState = {
  arvosana: DialogField<
    Koodistokoodiviite<'arviointiasteikkocorerequirementsib'>
  >
  tunniste: DialogField<Koodistokoodiviite<'oppiaineetib'>>
  kieli: DialogField<Koodistokoodiviite<'kielivalikoima'>>
  ryhmä: DialogField<Koodistokoodiviite<'aineryhmaib'>>
  taso: DialogField<Koodistokoodiviite<'oppiaineentasoib'>>
  pakollinen: DialogField<boolean>
  aihe: DialogField<LocalizedString>
}

export const useExtendedEssayState = (
  form: FormModel<IBOpiskeluoikeus>,
  päätasonSuoritus: ActivePäätasonSuoritus<
    IBOpiskeluoikeus,
    IBTutkinnonSuoritus
  >
): ExtendedEssayState => {
  const initial = päätasonSuoritus.suoritus.extendedEssay

  const arvosana = useDialogField<
    Koodistokoodiviite<'arviointiasteikkocorerequirementsib'>
  >(true, () => parasArviointi(initial?.arviointi)?.arvosana)
  const arvosanaSelected = !!arvosana.value

  const aine = useIBTutkintoOppiaineState(initial?.koulutusmoduuli.aine)
  const tunniste: DialogField<Koodistokoodiviite<'oppiaineetib'>> = useMemo(
    () => ({
      value: aine.tunniste.value || initial?.koulutusmoduuli.tunniste,
      set: aine.tunniste.set,
      visible: aine.tunniste.visible && arvosanaSelected,
      setVisible: aine.tunniste.setVisible
    }),
    [
      aine.tunniste.set,
      aine.tunniste.setVisible,
      aine.tunniste.value,
      aine.tunniste.visible,
      arvosanaSelected,
      initial?.koulutusmoduuli.tunniste
    ]
  )
  const aihe = useDialogField<LocalizedString>(
    arvosanaSelected,
    () => initial?.koulutusmoduuli.aihe
  )

  const setSuoritus = useCallback(
    (suoritus?: IBExtendedEssaySuoritus) => {
      form.updateAt(päätasonSuoritus.path.prop('extendedEssay'), () => suoritus)
    },
    // eslint-disable-next-line react-hooks/exhaustive-deps
    []
  )

  useEffect(() => {
    const suoritus =
      createIBExtendedEssaySuoritus({
        tunniste: aine.tunniste.value,
        kieli: aine.kieli.value,
        ryhmä: aine.ryhmä.value,
        taso: aine.taso.value,
        aihe: aihe.value,
        pakollinen: aine.pakollinen.value,
        arvosana: arvosana.value
      }) || undefined

    setSuoritus(suoritus)
  }, [
    aihe.value,
    aine.kieli.value,
    aine.pakollinen.value,
    aine.ryhmä.value,
    aine.taso.value,
    aine.tunniste.value,
    arvosana.value,
    setSuoritus
  ])

  return useMemo(
    () => ({
      arvosana,
      tunniste,
      kieli: aine.kieli,
      ryhmä: aine.ryhmä,
      taso: aine.taso,
      pakollinen: aine.pakollinen,
      aihe
    }),
    [
      aihe,
      aine.kieli,
      aine.pakollinen,
      aine.ryhmä,
      aine.taso,
      arvosana,
      tunniste
    ]
  )
}
