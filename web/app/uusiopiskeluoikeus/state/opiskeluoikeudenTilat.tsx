import { useMemo } from 'react'
import { useChildSchema } from '../../appstate/constraints'
import { useKoodistoOfConstraint } from '../../appstate/koodisto'
import {
  SelectOption,
  sortOptions,
  koodiviiteToOption
} from '../../components-v2/controls/Select'
import { Koodistokoodiviite } from '../../types/fi/oph/koski/schema/Koodistokoodiviite'
import { opiskeluoikeudenTilaClass } from './opiskeluoikeudenTilaClass'
import {
  UusiOpiskeluoikeusDialogState,
  opiskeluoikeustyyppiToClassNames
} from './state'

const excludedOpiskeluoikeudenTilat: Record<string, string[]> = {
  default: ['mitatoity'],
  internationalschool: ['mitatoity', 'katsotaaneronneeksi', 'peruutettu'],
  ammatillinenkoulutus: ['mitatoity', 'eronnut'],
  taiteenperusopetus: [
    'mitatoity',
    'hyvaksytystisuoritettu',
    'valiaikaisestikeskeytynyt'
  ]
}

export const useOpiskeluoikeudenTilat = (
  state: UusiOpiskeluoikeusDialogState
): {
  options: Array<SelectOption<Koodistokoodiviite<'koskiopiskeluoikeudentila'>>>
  initialValue?: string
} => {
  const className = opiskeluoikeustyyppiToClassNames(
    state.ooMapping,
    state.opiskeluoikeus?.value?.koodiarvo
  )

  const opiskelujaksonTila = useChildSchema(
    opiskeluoikeudenTilaClass(
      className,
      state.päätasonSuoritus.value?.koodiarvo
    ),
    'tila'
  )

  const koodistot =
    useKoodistoOfConstraint<'koskiopiskeluoikeudentila'>(opiskelujaksonTila)

  const options = useMemo(() => {
    const excludedOptions =
      excludedOpiskeluoikeudenTilat[
        state.opiskeluoikeus.value?.koodiarvo || 'default'
      ] || excludedOpiskeluoikeudenTilat.default

    return koodistot
      ? sortOptions(
          koodistot
            .flatMap((k) =>
              excludedOptions.includes(k.koodiviite.koodiarvo)
                ? []
                : [k.koodiviite]
            )
            .map(koodiviiteToOption)
        )
      : []
  }, [koodistot, state.opiskeluoikeus.value?.koodiarvo])

  const initialValue = useMemo(() => {
    const defaults = [
      'koskiopiskeluoikeudentila_lasna',
      'koskiopiskeluoikeudentila_valmistunut', // Opiskeluoikeus halutaan merkitä tavallisesti suoraan valmistuneeksi, jolla sillä ei ole läsnä-tilaa
      'koskiopiskeluoikeudentila_hyvaksytystisuoritettu'
    ]
    return defaults.find((tila) => options.find((tt) => tt.key === tila))
  }, [options])

  return { options, initialValue }
}
