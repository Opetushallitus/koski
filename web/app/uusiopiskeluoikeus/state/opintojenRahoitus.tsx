import { useMemo } from 'react'
import { useChildSchemaSafe } from '../../appstate/constraints'
import { useKoodistoOfConstraint } from '../../appstate/koodisto'
import { koodiviiteToOption } from '../../components-v2/controls/Select'
import { koodistokoodiviiteId } from '../../util/koodisto'
import { opiskeluoikeudenTilaClass } from './opiskeluoikeudenTilaClass'
import {
  UusiOpiskeluoikeusDialogState,
  opiskeluoikeustyyppiToClassNames
} from './state'

export const useOpintojenRahoitus = (state: UusiOpiskeluoikeusDialogState) => {
  const className = opiskeluoikeustyyppiToClassNames(
    state.ooMapping,
    state.opiskeluoikeus?.value?.koodiarvo
  )

  const opiskelujaksonTila = useChildSchemaSafe(
    opiskeluoikeudenTilaClass(
      className,
      state.päätasonSuoritus.value?.koodiarvo
    ),
    'opintojenRahoitus'
  )

  const koodistot = useKoodistoOfConstraint<'opintojenrahoitus'>(
    opiskelujaksonTila ? opiskelujaksonTila : null
  )

  const options = useMemo(
    () =>
      koodistot
        ? koodistot.flatMap((k) => k.koodiviite).map(koodiviiteToOption)
        : [],
    [koodistot]
  )

  const initialValue = useMemo(
    () => options[0]?.value && koodistokoodiviiteId(options[0]?.value),
    [options]
  )

  return { options, initialValue }
}
