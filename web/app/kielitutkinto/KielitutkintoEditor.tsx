import React from 'react'
import { useSchema } from '../appstate/constraints'
import { TestIdRoot } from '../appstate/useTestId'
import {
  EditorContainer,
  usePäätasonSuoritus
} from '../components-v2/containers/EditorContainer'
import { FormModel, useForm } from '../components-v2/forms/FormModel'
import { AdaptedOpiskeluoikeusEditorProps } from '../components-v2/interoperability/useUiAdapter'
import { Spacer } from '../components-v2/layout/Spacer'
import { OpiskeluoikeusTitle } from '../components-v2/opiskeluoikeus/OpiskeluoikeusTitle'
import { SuorituksenVahvistusField } from '../components-v2/opiskeluoikeus/SuorituksenVahvistus'
import { KielitutkinnonOpiskeluoikeudenOpiskeluoikeusjakso } from '../types/fi/oph/koski/schema/KielitutkinnonOpiskeluoikeudenOpiskeluoikeusjakso'
import { KielitutkinnonOpiskeluoikeus } from '../types/fi/oph/koski/schema/KielitutkinnonOpiskeluoikeus'
import { YleinenKielitutkintoEditor } from './YleinenKielitutkintoEditor'

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
        opiskeluoikeudenNimi={'ib-tutkinto'}
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

  return (
    <EditorContainer
      form={form}
      oppijaOid={oppijaOid}
      invalidatable={invalidatable}
      onChangeSuoritus={setPäätasonSuoritus}
      createOpiskeluoikeusjakso={
        KielitutkinnonOpiskeluoikeudenOpiskeluoikeusjakso
      }
    >
      <TestIdRoot id={päätasonSuoritus.testId}>
        <YleinenKielitutkintoEditor
          form={form}
          path={form.root.prop('suoritukset').at(0)}
          päätasonSuoritus={päätasonSuoritus}
          organisaatio={organisaatio}
        />
      </TestIdRoot>
    </EditorContainer>
  )
}
