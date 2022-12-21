import React, { useEffect, useMemo } from 'react'
import {
  useApiWithParams,
  useOnApiSuccess,
  useSafeState
} from '../../api-fetch'
import { modelData } from '../../editor/EditorModel'
import { ObjectModel } from '../../types/EditorModels'
import { Opiskeluoikeus } from '../../types/fi/oph/koski/schema/Opiskeluoikeus'
import { isTäydellisetHenkilötiedot } from '../../types/fi/oph/koski/schema/TaydellisetHenkilotiedot'
import { fetchOppija } from '../../util/koskiApi'
import { ClassOf } from '../../util/types'
import { opiskeluoikeusEditors } from './uiAdapters'

export type AdaptedOpiskeluoikeusEditor<T extends Opiskeluoikeus> = (props: {
  oppijaOid: string
  opiskeluoikeus: T
}) => JSX.Element

export type AdaptedOpiskeluoikeusEditorCollection = Partial<{
  [OO in Opiskeluoikeus as ClassOf<OO>]: AdaptedOpiskeluoikeusEditor<OO>
}>

export type UiAdapter = {
  isLoading: boolean

  getOpiskeluoikeusEditor: (
    opiskeluoikeusModel: ObjectModel
  ) => AdapterComponent | undefined
}

const initialUiAdapter: UiAdapter = {
  isLoading: true,
  getOpiskeluoikeusEditor: () => undefined
}

export type AdapterComponent = () => JSX.Element

export type OpiskeluoikeusEditorProps<T extends Opiskeluoikeus> = {
  opiskeluoikeus: T
}

export const useUiAdapter = (oppijaModel: ObjectModel): UiAdapter => {
  const [adapter, setAdapter] = useSafeState<UiAdapter>(initialUiAdapter)

  const oppijaOid = useMemo(
    () => modelData(oppijaModel, 'henkilö.oid'),
    [oppijaModel]
  )
  const oppija = useApiWithParams(fetchOppija, [oppijaOid])

  useEffect(() => setAdapter(initialUiAdapter), [oppijaOid])
  useOnApiSuccess(oppija, (result) => {
    const opiskeluoikeudet = result.data.opiskeluoikeudet

    setAdapter({
      isLoading: false,
      getOpiskeluoikeusEditor(opiskeluoikeusModel) {
        const tyyppi = modelData(opiskeluoikeusModel, 'tyyppi.koodiarvo')
        const oid = modelData(opiskeluoikeusModel, 'oid')

        const oo = opiskeluoikeudet.find(
          // Tarkastetaan myös opiskeluoikeuden tyypillä, koska ylioppilastutkinnoilla ei ole oidia
          (o) => o.tyyppi.koodiarvo === tyyppi && (o as any).oid === oid
        )

        const Editor: AdaptedOpiskeluoikeusEditor<any> | undefined =
          oo && opiskeluoikeusEditors[oo.$class]

        return Editor
          ? () => <Editor oppijaOid={oppijaOid} opiskeluoikeus={oo} />
          : undefined
      }
    })
  })

  return adapter
}
