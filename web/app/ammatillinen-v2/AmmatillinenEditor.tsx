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
import {
  PerusteEdit,
  PerusteView
} from '../components-v2/opiskeluoikeus/PerusteField'
import { AmmatillinenPäätasonSuoritus } from '../types/fi/oph/koski/schema/AmmatillinenPaatasonSuoritus'
import { AmmatillisenTutkinnonOsittainenSuoritus } from '../types/fi/oph/koski/schema/AmmatillisenTutkinnonOsittainenSuoritus'
import { FormListField } from '../components-v2/forms/FormListField'
import { ButtonGroup } from '../components-v2/containers/ButtonGroup'
import { FlatButton } from '../components-v2/controls/FlatButton'
import { append } from '../util/fp/arrays'
import { Koodistokoodiviite } from '../types/fi/oph/koski/schema/Koodistokoodiviite'
import {
  BooleanEdit,
  BooleanView
} from '../components-v2/opiskeluoikeus/BooleanField'

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
  päätasonSuoritus: ActivePäätasonSuoritus<
    AmmatillinenOpiskeluoikeus,
    AmmatillisenTutkinnonOsittainenSuoritus
  >
}> = ({ form, päätasonSuoritus }) => {
  const path = päätasonSuoritus.path

  return (
    <KeyValueTable>
      <KeyValueRow localizableLabel="Koulutus">
        <TestIdText id="koulutus">
          {/*TODO Koulutusmoduuli editor perustelinkillä*/}
          {t(päätasonSuoritus.suoritus.koulutusmoduuli.tunniste.nimi)}
        </TestIdText>{' '}
        {päätasonSuoritus.suoritus.koulutusmoduuli.tunniste.koodiarvo}{' '}
        <FormField
          form={form}
          path={path.prop('koulutusmoduuli').prop('perusteenDiaarinumero')}
          view={PerusteView}
        />
      </KeyValueRow>
      <KeyValueRow localizableLabel="Suoritustapa">
        {t(päätasonSuoritus.suoritus.suoritustapa.nimi)}
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
      <KeyValueRow localizableLabel="Tutkintonimike">
        <FormListField
          form={form}
          view={KoodistoView}
          edit={KoodistoEdit}
          editProps={{ koodistoUri: 'tutkintonimikkeet' }}
          path={path.prop('tutkintonimike')}
          removable
        />
        {form.editMode && (
          <ButtonGroup>
            <FlatButton
              onClick={() => {
                form.updateAt(
                  path.prop('tutkintonimike').valueOr([]),
                  append(
                    Koodistokoodiviite<'tutkintonimikkeet', string>({
                      koodiarvo: '00000',
                      koodistoUri: 'tutkintonimikkeet'
                    })
                  )
                )
              }}
            >
              {t('Lisää')}
            </FlatButton>
          </ButtonGroup>
        )}
      </KeyValueRow>
      <KeyValueRow localizableLabel="Toinen tutkintonimike">
        <FormField
          form={form}
          view={BooleanView}
          edit={BooleanEdit}
          path={path.prop('toinenTutkintonimike')}
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
  const osittainenPäätasonSuoritus = päätasonSuoritus as ActivePäätasonSuoritus<
    AmmatillinenOpiskeluoikeus,
    AmmatillisenTutkinnonOsittainenSuoritus
  >

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
        päätasonSuoritus={osittainenPäätasonSuoritus}
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
