import React from 'react'
import { AdaptedOpiskeluoikeusEditorProps } from '../components-v2/interoperability/useUiAdapter'
import { AmmatillinenOpiskeluoikeus } from '../types/fi/oph/koski/schema/AmmatillinenOpiskeluoikeus'
import {
  EditorContainer,
  usePäätasonSuoritus
} from '../components-v2/containers/EditorContainer'
import { FormModel, useForm } from '../components-v2/forms/FormModel'
import { useSchema } from '../appstate/constraints'
import { UusiOpiskeluoikeusjakso } from '../components-v2/opiskeluoikeus/UusiOpiskeluoikeudenTilaModal'
import { AmmatillinenOpiskeluoikeusjakso } from '../types/fi/oph/koski/schema/AmmatillinenOpiskeluoikeusjakso'
import { AmmatillinenLisatiedot } from './AmmatillinenLisatiedot'
import { VirkailijaKansalainenContainer } from '../components-v2/containers/VirkailijaKansalainenContainer'
import { t } from '../i18n/i18n'

export type AmmatillinenEditorProps =
  AdaptedOpiskeluoikeusEditorProps<AmmatillinenOpiskeluoikeus>

const AmmatillinenEditor: React.FC<AmmatillinenEditorProps> = (props) => {
  const opiskeluoikeusSchema = useSchema(AmmatillinenOpiskeluoikeus.className)
  const form = useForm(props.opiskeluoikeus, false, opiskeluoikeusSchema)

  return (
    <VirkailijaKansalainenContainer
      opiskeluoikeus={form.state}
      opiskeluoikeudenNimi={suorituksenNimi(form.state)}
    >
      <AmmatillinenPäätasonSuoritusEditor {...props} form={form} />
    </VirkailijaKansalainenContainer>
  )
}

const suorituksenNimi = (oo: AmmatillinenOpiskeluoikeus): string => {
  if (oo.suoritukset[0].tyyppi.koodiarvo === 'ammatillinentutkintoosittainen')
    return (
      t(oo.suoritukset[0].koulutusmoduuli.tunniste.nimi) + t(', osittainen')
    )
  else return t(oo.suoritukset[0].koulutusmoduuli.tunniste.nimi)
}

const AmmatillinenPäätasonSuoritusEditor: React.FC<
  AmmatillinenEditorProps & {
    form: FormModel<AmmatillinenOpiskeluoikeus>
  }
> = (props) => {
  return <AmmatillinenTutkintoOsittainenEditor {...props} />
}

const AmmatillinenTutkintoOsittainenEditor: React.FC<
  AmmatillinenEditorProps & {
    form: FormModel<AmmatillinenOpiskeluoikeus>
  }
> = (props) => {
  const [päätasonSuoritus, setPäätasonSuoritus] = usePäätasonSuoritus(
    props.form
  )
  const createAmmatillinenOpiskeluoikeusJakso = (
    seed: UusiOpiskeluoikeusjakso<AmmatillinenOpiskeluoikeusjakso>
  ) => AmmatillinenOpiskeluoikeusjakso(seed)

  return (
    <EditorContainer
      form={props.form}
      oppijaOid={props.oppijaOid}
      invalidatable={props.invalidatable}
      onChangeSuoritus={setPäätasonSuoritus}
      testId={päätasonSuoritus.testId}
      createOpiskeluoikeusjakso={createAmmatillinenOpiskeluoikeusJakso}
      lisätiedotContainer={AmmatillinenLisatiedot}
    />
  )
}

export default AmmatillinenEditor
