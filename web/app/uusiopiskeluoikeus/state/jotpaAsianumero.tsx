import { useMemo } from 'react'
import { useChildSchemaSafe } from '../../appstate/constraints'
import { useKoodistoOfConstraint } from '../../appstate/koodisto'
import { koodiviiteToOption } from '../../components-v2/controls/Select'
import { isJotpaRahoituksenKoodistoviite } from '../../jotpa/jotpa'
import { opiskeluoikeudenLisätiedotClass } from './opiskeluoikeudenLisätiedotClass'
import {
  UusiOpiskeluoikeusDialogState,
  opiskeluoikeustyyppiToClassNames
} from './state'

export const useJotpaAsianumero = (state: UusiOpiskeluoikeusDialogState) => {
  const className = opiskeluoikeustyyppiToClassNames(
    state.ooMapping,
    state.opiskeluoikeus?.value?.koodiarvo
  )

  const asianumeroSchema = useChildSchemaSafe(
    opiskeluoikeudenLisätiedotClass(className),
    'jotpaAsianumero'
  )

  const koodistot = useKoodistoOfConstraint<'jotpaasianumero'>(
    asianumeroSchema &&
      state.opintojenRahoitus.value &&
      isJotpaRahoituksenKoodistoviite(state.opintojenRahoitus.value)
      ? asianumeroSchema
      : null
  )

  const options = useMemo(
    () =>
      koodistot
        ? koodistot.flatMap((k) => k.koodiviite).map(koodiviiteToOption)
        : [],
    [koodistot]
  )

  const initialValue = useMemo(() => options[0]?.value?.koodiarvo, [options])

  return { options, initialValue }
}
