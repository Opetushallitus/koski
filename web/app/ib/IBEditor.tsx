import React from 'react'
import { useSchema } from '../appstate/constraints'
import { FormModel, useForm } from '../components-v2/forms/FormModel'
import { AdaptedOpiskeluoikeusEditorProps } from '../components-v2/interoperability/useUiAdapter'
import { OpiskeluoikeusTitle } from '../components-v2/opiskeluoikeus/OpiskeluoikeusTitle'
import { IBOpiskeluoikeus } from '../types/fi/oph/koski/schema/IBOpiskeluoikeus'
import { t } from '../i18n/i18n'
import {
  EditorContainer,
  usePäätasonSuoritus
} from '../components-v2/containers/EditorContainer'
import { LukionOpiskeluoikeusjakso } from '../types/fi/oph/koski/schema/LukionOpiskeluoikeusjakso'
import {
  ibKoulutusNimi,
  IBPäätasonSuoritusTiedot
} from './IBPaatasonSuoritusTiedot'
import { Spacer } from '../components-v2/layout/Spacer'
import { SuorituksenVahvistusField } from '../components-v2/opiskeluoikeus/SuorituksenVahvistus'

export type IBEditorProps = AdaptedOpiskeluoikeusEditorProps<IBOpiskeluoikeus>

export const IBEditor: React.FC<IBEditorProps> = (props) => {
  const opiskeluoikeusSchema = useSchema('IBOpiskeluoikeus')
  const form = useForm(props.opiskeluoikeus, false, opiskeluoikeusSchema)

  return (
    <>
      <OpiskeluoikeusTitle
        opiskeluoikeus={form.state}
        opiskeluoikeudenNimi={ibKoulutusNimi(form.state)}
      />
      <IBPäätasonSuoritusEditor {...props} form={form} />
    </>
  )
}

const IBPäätasonSuoritusEditor: React.FC<
  IBEditorProps & {
    form: FormModel<IBOpiskeluoikeus>
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
      onChangeSuoritus={() => console.log('todo: onChangeSuoritus')}
      createOpiskeluoikeusjakso={LukionOpiskeluoikeusjakso}
    >
      <IBPäätasonSuoritusTiedot
        form={form}
        päätasonSuoritus={päätasonSuoritus}
      />

      <Spacer />

      <SuorituksenVahvistusField
        form={form}
        suoritusPath={päätasonSuoritus.path}
        organisaatio={organisaatio}
        disableAdd={true} // TODO
      />
      <Spacer />
    </EditorContainer>
  )
}
