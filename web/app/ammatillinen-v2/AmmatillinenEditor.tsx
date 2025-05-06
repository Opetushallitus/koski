import React from 'react'
import { AdaptedOpiskeluoikeusEditorProps } from '../components-v2/interoperability/useUiAdapter'
import { AmmatillinenOpiskeluoikeus } from '../types/fi/oph/koski/schema/AmmatillinenOpiskeluoikeus'
import {
  ActivePäätasonSuoritus,
  EditorContainer,
  usePäätasonSuoritus
} from '../components-v2/containers/EditorContainer'
import { FormModel, FormOptic, useForm } from '../components-v2/forms/FormModel'
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
import {
  FieldEditorProps,
  FieldViewerProps,
  FormField
} from '../components-v2/forms/FormField'
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
import { Osaamisalajakso } from '../types/fi/oph/koski/schema/Osaamisalajakso'
import { CommonProps } from '../components-v2/CommonProps'
import { EmptyObject } from '../util/objects'
import { ISO2FinnishDate } from '../date/date'
import { DateInput } from '../components-v2/controls/DateInput'
import { NumberField } from '../components-v2/controls/NumberField'
import { KoodistoSelect } from '../components-v2/opiskeluoikeus/KoodistoSelect'

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
      <KeyValueRow localizableLabel="Osaamisala">
        <FormListField
          form={form}
          view={OsaamisalaView}
          edit={OsaamisalaEdit}
          path={path.prop('osaamisala')}
          removable
        />
        {form.editMode && (
          <ButtonGroup>
            <FlatButton
              onClick={() => {
                form.updateAt(
                  path.prop('osaamisala').valueOr([]),
                  append<Osaamisalajakso>({
                    $class: 'fi.oph.koski.schema.Osaamisalajakso',
                    osaamisala: Koodistokoodiviite<'osaamisala', ''>({
                      koodiarvo: '',
                      koodistoUri: 'osaamisala'
                    })
                  })
                )
              }}
            >
              {t('Lisää')}
            </FlatButton>
          </ButtonGroup>
        )}
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

type OsaamisalajaksoReal = {
  $class: 'fi.oph.koski.schema.Osaamisalajakso'
  osaamisala: Koodistokoodiviite<'osaamisala', string>
  alku?: string
  loppu?: string
}

const isOsaamisalajaksoReal = (val: any): val is OsaamisalajaksoReal =>
  val.$class === 'fi.oph.koski.schema.Osaamisalajakso'

const OsaamisalaView = <T extends Osaamisalajakso>({
  value
}: CommonProps<FieldViewerProps<T | undefined, EmptyObject>>) => {
  if (isOsaamisalajaksoReal(value)) {
    return (
      <>
        <TestIdText id="alku">
          {value?.alku && ISO2FinnishDate(value.alku)}
        </TestIdText>{' '}
        {' - '}
        <TestIdText id="loppu">
          {value?.loppu && ISO2FinnishDate(value.loppu)}
        </TestIdText>
        {' - '}
        <TestIdText id="osaamisala">{t(value.osaamisala.nimi)}</TestIdText>
      </>
    )
  } else return <TestIdText id="osaamisala">{t(value?.nimi)}</TestIdText>
}

const OsaamisalaEdit = ({
  value,
  onChange
}: FieldEditorProps<Osaamisalajakso | undefined, EmptyObject>) => {
  if (isOsaamisalajaksoReal(value)) {
    return (
      <div className="MaksuttomuusEdit" data-testid="osaamisala">
        <KoodistoSelect
          koodistoUri={'osaamisala'}
          onSelect={(koodiviite) => {
            koodiviite && onChange({ ...value, osaamisala: koodiviite })
          }}
          value={value.osaamisala.koodiarvo}
          testId="osaamisala"
        />
        <span className="MaksuttomuusEdit__separator"> {' - '}</span>
        <DateInput
          value={value?.alku}
          onChange={(alku?: string) => {
            alku && onChange({ ...value, alku })
          }}
          testId="alku"
        />
        <span className="MaksuttomuusEdit__separator"> {' - '}</span>
        <DateInput
          value={value?.loppu}
          onChange={(loppu?: string) => {
            loppu && onChange({ ...value, loppu })
          }}
          testId="loppu"
        />
      </div>
    )
  } else
    return (
      <KoodistoSelect
        koodistoUri={'osaamisala'}
        onSelect={(koodiviite) => {
          koodiviite && onChange(koodiviite)
        }}
        value={value?.koodiarvo}
        testId="osaamisala"
      />
    )
}

export default AmmatillinenEditor
