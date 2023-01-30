import React, { useCallback, useMemo, useState } from 'react'
import { localize, t } from '../../i18n/i18n'
import { LocalizedString } from '../../types/fi/oph/koski/schema/LocalizedString'
import { Opiskeluoikeus } from '../../types/fi/oph/koski/schema/Opiskeluoikeus'
import { Suoritus } from '../../types/fi/oph/koski/schema/Suoritus'
import { saveOpiskeluoikeus } from '../../util/koskiApi'
import { mergeOpiskeluoikeusVersionumero } from '../../util/opiskeluoikeus'
import { usePäätasonSuoritus } from '../../util/optics'
import { OpiskeluoikeusjaksoOf } from '../../util/schema'
import { ItemOf } from '../../util/types'
import { common, CommonPropsWithChildren } from '../CommonProps'
import { Tabs } from '../controls/Tabs'
import { FormField } from '../forms/FormField'
import { FormModel } from '../forms/FormModel'
import { Spacer } from '../layout/Spacer'
import { Snackbar } from '../messages/Snackbar'
import { EditBar } from '../opiskeluoikeus/EditBar'
import {
  OpiskeluoikeudenTilaEdit,
  OpiskeluoikeudenTilaView
} from '../opiskeluoikeus/OpiskeluoikeudenTila'
import { OpiskeluoikeusEditToolbar } from '../opiskeluoikeus/OpiskeluoikeusEditToolbar'
import { UusiOpiskeluoikeusjakso } from '../opiskeluoikeus/UusiOpiskeluoikeudenTilaModal'
import { Trans } from '../texts/Trans'

export type EditorContainerProps<T extends Opiskeluoikeus> =
  CommonPropsWithChildren<{
    form: FormModel<T>
    oppijaOid: string
    onChangeSuoritus: (index: number) => void
    createOpiskeluoikeusjakso: (
      seed: UusiOpiskeluoikeusjakso<OpiskeluoikeusjaksoOf<T['tila']>>
    ) => void
    suorituksenNimi?: (suoritus: ItemOf<T['suoritukset']>) => LocalizedString
  }>

export const EditorContainer = <T extends Opiskeluoikeus>(
  props: EditorContainerProps<T>
) => {
  const [suoritusIndex, setSuoritusIndex] = useState(0)
  const päätasonSuoritusPath = usePäätasonSuoritus<T>(suoritusIndex)
  const opiskeluoikeudenTilaPath = useMemo(
    () => props.form.root.prop('tila'),
    [props.form.root]
  )

  const onSave = useCallback(() => {
    props.form.save(
      saveOpiskeluoikeus(props.oppijaOid),
      mergeOpiskeluoikeusVersionumero
    )
  }, [props.form, props.oppijaOid])

  const { onChangeSuoritus: onSuoritusIndex } = props
  const updateSuoritusIndex = useCallback(
    (index: number) => {
      setSuoritusIndex(index)
      onSuoritusIndex(index)
    },
    [onSuoritusIndex]
  )

  return (
    <article {...common(props, ['EditorContainer'])}>
      <OpiskeluoikeusEditToolbar
        opiskeluoikeus={props.form.state}
        editMode={props.form.editMode}
        onStartEdit={props.form.startEdit}
      />

      <Spacer />

      <FormField
        form={props.form}
        path={opiskeluoikeudenTilaPath}
        view={OpiskeluoikeudenTilaView}
        edit={OpiskeluoikeudenTilaEdit}
        editProps={{ createJakso: props.createOpiskeluoikeusjakso }}
      />
      <Spacer />

      <h2>
        <Trans>{'Suoritukset'}</Trans>
      </h2>

      <Tabs
        onSelect={updateSuoritusIndex}
        tabs={props.form.state.suoritukset.map((s, i) => ({
          key: i,
          label: props.suorituksenNimi?.(s) || defaultSuorituksenNimi(s)
        }))}
      />

      <Spacer />

      {props.children}

      <EditBar form={props.form} onSave={onSave} />
      {props.form.isSaved && <Snackbar>{'Tallennettu'}</Snackbar>}
    </article>
  )
}

const defaultSuorituksenNimi = (s: Suoritus): LocalizedString =>
  s.tyyppi.lyhytNimi || s.tyyppi.nimi || localize(s.tyyppi.koodiarvo)
