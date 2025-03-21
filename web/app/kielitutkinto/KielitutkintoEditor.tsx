import React from 'react'
import { useSchema } from '../appstate/constraints'
import {
  EditorContainer,
  hasPäätasonsuoritusOf,
  usePäätasonSuoritus
} from '../components-v2/containers/EditorContainer'
import { FormModel, useForm } from '../components-v2/forms/FormModel'
import { AdaptedOpiskeluoikeusEditorProps } from '../components-v2/interoperability/useUiAdapter'
import { OpiskeluoikeusTitle } from '../components-v2/opiskeluoikeus/OpiskeluoikeusTitle'
import { KielitutkinnonOpiskeluoikeudenOpiskeluoikeusjakso } from '../types/fi/oph/koski/schema/KielitutkinnonOpiskeluoikeudenOpiskeluoikeusjakso'
import { KielitutkinnonOpiskeluoikeus } from '../types/fi/oph/koski/schema/KielitutkinnonOpiskeluoikeus'
import { isValtionhallinnonKielitutkinnonSuoritus } from '../types/fi/oph/koski/schema/ValtionhallinnonKielitutkinnonSuoritus'
import { isYleisenKielitutkinnonSuoritus } from '../types/fi/oph/koski/schema/YleisenKielitutkinnonSuoritus'
import { ValtionhallinnonKielitutkintoEditor } from './ValtiohallinnonKielitutkintoEditor'
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
      opiskeluoikeudenTilaEditor={form.editMode ? null : <></>} // Piilota tilaeditori näyttökäyttöliittymästä
    >
      {hasPäätasonsuoritusOf(
        isYleisenKielitutkinnonSuoritus,
        päätasonSuoritus
      ) && (
        <YleinenKielitutkintoEditor
          form={form}
          päätasonSuoritus={päätasonSuoritus}
          organisaatio={organisaatio}
        />
      )}
      {hasPäätasonsuoritusOf(
        isValtionhallinnonKielitutkinnonSuoritus,
        päätasonSuoritus
      ) && (
        <ValtionhallinnonKielitutkintoEditor
          form={form}
          päätasonSuoritus={päätasonSuoritus}
          organisaatio={organisaatio}
        />
      )}
    </EditorContainer>
  )
}
