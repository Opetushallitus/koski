import React from 'react'
import { AdaptedOpiskeluoikeusEditorProps } from '../components-v2/interoperability/useUiAdapter'
import { AmmatillinenOpiskeluoikeus } from '../types/fi/oph/koski/schema/AmmatillinenOpiskeluoikeus'
import {
  ActivePäätasonSuoritus,
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
import {
  KeyValueRow,
  KeyValueTable
} from '../components-v2/containers/KeyValueTable'
import { TestIdText } from '../appstate/useTestId'
import { FormField } from '../components-v2/forms/FormField'
import {
  OrganisaatioEdit,
  OrganisaatioView
} from '../components-v2/opiskeluoikeus/OrganisaatioField'
import {
  KoodistoEdit,
  KoodistoView
} from '../components-v2/opiskeluoikeus/KoodistoField'
import { Spacer } from '../components-v2/layout/Spacer'
import { SuorituksenVahvistusField } from '../components-v2/opiskeluoikeus/SuorituksenVahvistus'

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

const AmmatillisPääsuorituksenTiedot: React.FC<{
  form: FormModel<AmmatillinenOpiskeluoikeus>
  päätasonSuoritus: ActivePäätasonSuoritus<AmmatillinenOpiskeluoikeus>
}> = ({ form, päätasonSuoritus }) => {
  const path = päätasonSuoritus.path

  return (
    <KeyValueTable>
      <KeyValueRow localizableLabel="Koulutus">
        <TestIdText id="koulutus">
          {/*TODO Koulutusmoduuli editor perustelinkillä*/}
          {t(päätasonSuoritus.suoritus.koulutusmoduuli.tunniste.nimi)}
        </TestIdText>
      </KeyValueRow>
      <KeyValueRow localizableLabel="Oppilaitos / toimipiste">
        <FormField
          form={form}
          path={path.prop('toimipiste')}
          view={OrganisaatioView}
          edit={OrganisaatioEdit}
        />
      </KeyValueRow>
      <KeyValueRow localizableLabel="Suorituskieli">
        <FormField
          form={form}
          path={path.prop('suorituskieli')}
          view={KoodistoView}
          edit={KoodistoEdit}
          editProps={{ koodistoUri: 'kieli' }}
          testId="suorituskieli"
        />
      </KeyValueRow>
      {/*TODO lisää rivejä tietomallissa?*/}
    </KeyValueTable>
  )
}

const AmmatillinenTutkintoOsittainenEditor: React.FC<
  AmmatillinenEditorProps & {
    form: FormModel<AmmatillinenOpiskeluoikeus>
  }
> = (props) => {
  const [päätasonSuoritus, setPäätasonSuoritus] = usePäätasonSuoritus(
    props.form
  )

  const organisaatio =
    props.opiskeluoikeus.oppilaitos || props.opiskeluoikeus.koulutustoimija

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
    >
      <AmmatillisPääsuorituksenTiedot
        form={props.form}
        päätasonSuoritus={päätasonSuoritus}
      />

      <Spacer />

      <SuorituksenVahvistusField
        form={props.form}
        suoritusPath={päätasonSuoritus.path}
        organisaatio={organisaatio}
        disableAdd={false /*TODO?*/}
      />
    </EditorContainer>
  )
}

export default AmmatillinenEditor
