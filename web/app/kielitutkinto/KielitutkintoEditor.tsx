import React from 'react'
import { useSchema } from '../appstate/constraints'
import { TestIdLayer, TestIdRoot } from '../appstate/useTestId'
import {
  EditorContainer,
  usePäätasonSuoritus
} from '../components-v2/containers/EditorContainer'
import { FormModel, useForm } from '../components-v2/forms/FormModel'
import { AdaptedOpiskeluoikeusEditorProps } from '../components-v2/interoperability/useUiAdapter'
import { OpiskeluoikeusTitle } from '../components-v2/opiskeluoikeus/OpiskeluoikeusTitle'
import { KielitutkinnonOpiskeluoikeudenOpiskeluoikeusjakso } from '../types/fi/oph/koski/schema/KielitutkinnonOpiskeluoikeudenOpiskeluoikeusjakso'
import { KielitutkinnonOpiskeluoikeus } from '../types/fi/oph/koski/schema/KielitutkinnonOpiskeluoikeus'
import { YleinenKielitutkintoEditor } from './YleinenKielitutkintoEditor'
import { YleinenKielitutkinto } from '../types/fi/oph/koski/schema/YleinenKielitutkinto'

export type KielitutkintoEditorProps =
  AdaptedOpiskeluoikeusEditorProps<KielitutkinnonOpiskeluoikeus>

export const KielitutkintoEditor: React.FC<KielitutkintoEditorProps> = (
  props
) => {
  const opiskeluoikeusSchema = useSchema('IBOpiskeluoikeus')
  const form = useForm(props.opiskeluoikeus, false, opiskeluoikeusSchema)

  return (
    <>
      <OpiskeluoikeusTitle
        opiskeluoikeus={form.state}
        opiskeluoikeudenNimi="kielitutkinto"
      />
      <KielitutkinnonPäätasonSuoritusEditor {...props} form={form} />
    </>
  )
}

const KielitutkinnonPäätasonSuoritusEditor: React.FC<
  KielitutkintoEditorProps & {
    form: FormModel<KielitutkinnonOpiskeluoikeus>
  }
> = ({ form, oppijaOid, invalidatable, opiskeluoikeus }) => {
  const [päätasonSuoritus, setPäätasonSuoritus] = usePäätasonSuoritus(form)
  const organisaatio =
    opiskeluoikeus.oppilaitos || opiskeluoikeus.koulutustoimija

  const overrides =
    !form.editMode &&
    opiskeluoikeus.suoritukset[0].koulutusmoduuli.$class ===
      YleinenKielitutkinto.className
      ? { opiskeluoikeudenTilaEditor: <></> } // Ei näytetä opiskeluoikeuden tilaa näyttötilassa
      : null

  return (
    <EditorContainer
      form={form}
      oppijaOid={oppijaOid}
      invalidatable={invalidatable}
      onChangeSuoritus={setPäätasonSuoritus}
      createOpiskeluoikeusjakso={
        KielitutkinnonOpiskeluoikeudenOpiskeluoikeusjakso
      }
      testId={päätasonSuoritus.testId}
      {...overrides}
    >
      <YleinenKielitutkintoEditor
        form={form}
        path={form.root.prop('suoritukset').at(0)}
        päätasonSuoritus={päätasonSuoritus}
        organisaatio={organisaatio}
      />
    </EditorContainer>
  )
}
