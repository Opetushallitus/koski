import * as string from 'fp-ts/string'
import React, { useEffect, useMemo } from 'react'
import { useApiMethod, useOnApiSuccess, useSafeState } from '../../api-fetch'
import { modelData } from '../../editor/EditorModel'
import { ObjectModel } from '../../types/EditorModels'
import { Opiskeluoikeus } from '../../types/fi/oph/koski/schema/Opiskeluoikeus'
import { intersects } from '../../util/fp/arrays'
import { fetchOppija } from '../../util/koskiApi'
import { getOpiskeluoikeusOid } from '../../util/opiskeluoikeus'
import { OpiskeluoikeudenTyyppiOf } from '../../util/types'
import { opiskeluoikeusEditors } from './uiAdapters'

export type AdaptedOpiskeluoikeusEditor<T extends Opiskeluoikeus> = (props: {
  oppijaOid: string
  opiskeluoikeus: T
}) => JSX.Element

export type AdaptedOpiskeluoikeusEditorCollection = Partial<{
  [OO in Opiskeluoikeus as OpiskeluoikeudenTyyppiOf<OO>]: AdaptedOpiskeluoikeusEditor<OO>
}>

export type UiAdapter = {
  isLoadingV2: boolean

  getOpiskeluoikeusEditor: (
    opiskeluoikeusModel: ObjectModel
  ) => AdapterComponent | undefined
}

const loadingUiAdapter: UiAdapter = {
  isLoadingV2: true,
  getOpiskeluoikeusEditor: () => undefined
}

const disabledUiAdapter: UiAdapter = {
  isLoadingV2: false,
  getOpiskeluoikeusEditor: () => undefined
}

export type AdapterComponent = () => JSX.Element

export type OpiskeluoikeusEditorProps<T extends Opiskeluoikeus> = {
  opiskeluoikeus: T
}

export const useUiAdapter = (oppijaModel: ObjectModel): UiAdapter => {
  const [adapter, setAdapter] = useSafeState<UiAdapter>(loadingUiAdapter)
  const oppija = useApiMethod(fetchOppija)

  const v2Mode = useMemo(() => {
    const ooTyypit: string[] =
      modelData(oppijaModel, 'opiskeluoikeudet')?.map(
        (o: any) => o.tyyppi.koodiarvo
      ) || []
    const v2OpiskeluoikeusTyypit = Object.keys(opiskeluoikeusEditors)
    return intersects(string.Eq)(ooTyypit)(v2OpiskeluoikeusTyypit)
  }, [oppijaModel])

  useEffect(() => {
    if (v2Mode) {
      const oppijaOid = modelData(oppijaModel, 'henkilö.oid')
      setAdapter(loadingUiAdapter)
      oppija.call(oppijaOid)
    }
  }, [oppijaModel, v2Mode])

  useOnApiSuccess(oppija, (result) => {
    const opiskeluoikeudet = result.data.opiskeluoikeudet
    const oppijaOid = modelData(oppijaModel, 'henkilö.oid')

    setAdapter({
      isLoadingV2: false,
      getOpiskeluoikeusEditor(opiskeluoikeusModel) {
        const tyyppi = modelData(opiskeluoikeusModel, 'tyyppi.koodiarvo')
        const oid = modelData(opiskeluoikeusModel, 'oid')

        const oo = opiskeluoikeudet.find(
          (o) =>
            o.tyyppi.koodiarvo === tyyppi && getOpiskeluoikeusOid(o) === oid
        )

        const Editor: AdaptedOpiskeluoikeusEditor<any> | undefined =
          oo && opiskeluoikeusEditors[oo.tyyppi.koodiarvo]

        return Editor
          ? () => <Editor oppijaOid={oppijaOid} opiskeluoikeus={oo} />
          : undefined
      }
    })
  })

  return v2Mode ? adapter : disabledUiAdapter
}
