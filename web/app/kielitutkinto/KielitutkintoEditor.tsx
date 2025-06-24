import React from 'react'
import { useSchema } from '../appstate/constraints'
import {
  EditorContainer,
  hasPäätasonsuoritusOf,
  usePäätasonSuoritus
} from '../components-v2/containers/EditorContainer'
import { VirkailijaKansalainenContainer } from '../components-v2/containers/VirkailijaKansalainenContainer'
import { FormModel, useForm } from '../components-v2/forms/FormModel'
import { AdaptedOpiskeluoikeusEditorProps } from '../components-v2/interoperability/useUiAdapter'
import { ISO2FinnishDate } from '../date/date'
import { t } from '../i18n/i18n'
import { KielitutkinnonOpiskeluoikeudenOpiskeluoikeusjakso } from '../types/fi/oph/koski/schema/KielitutkinnonOpiskeluoikeudenOpiskeluoikeusjakso'
import { KielitutkinnonOpiskeluoikeus } from '../types/fi/oph/koski/schema/KielitutkinnonOpiskeluoikeus'
import { isValtionhallinnonKielitutkinnonSuoritus } from '../types/fi/oph/koski/schema/ValtionhallinnonKielitutkinnonSuoritus'
import { isYleisenKielitutkinnonSuoritus } from '../types/fi/oph/koski/schema/YleisenKielitutkinnonSuoritus'
import { ValtionhallinnonKielitutkintoEditor } from './ValtiohallinnonKielitutkintoEditor'
import {
  getJaksonAlkupäivä,
  YleinenKielitutkintoEditor
} from './YleinenKielitutkintoEditor'

export type KielitutkintoEditorProps =
  AdaptedOpiskeluoikeusEditorProps<KielitutkinnonOpiskeluoikeus>

export const KielitutkintoEditor: React.FC<KielitutkintoEditorProps> = (
  props
) => {
  const opiskeluoikeusSchema = useSchema('KielitutkinnonOpiskeluoikeus')
  const form = useForm(props.opiskeluoikeus, false, opiskeluoikeusSchema)

  const tutkinnonTyyppi = t(form.state.suoritukset[0].tyyppi.nimi)
  const kieli = t(form.state.suoritukset[0].koulutusmoduuli.kieli.nimi)
  const tutkintopäivä = ISO2FinnishDate(
    getJaksonAlkupäivä(form.state.tila, 'lasna')
  )

  return (
    <VirkailijaKansalainenContainer
      opiskeluoikeus={form.state}
      opiskeluoikeudenNimi={`${tutkinnonTyyppi}, ${kieli}`}
      pvmJaTila={tutkintopäivä}
    >
      <KielitutkinnonPäätasonSuoritusEditor {...props} form={form} />
    </VirkailijaKansalainenContainer>
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
      hideOpiskeluoikeusVoimassaoloaika
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
