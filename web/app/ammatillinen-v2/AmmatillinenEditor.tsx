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
import { localize, t } from '../i18n/i18n'
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
import { PerusteView } from '../components-v2/opiskeluoikeus/PerusteField'
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
import { ISO2FinnishDate, todayISODate } from '../date/date'
import { DateInput } from '../components-v2/controls/DateInput'
import { KoodistoSelect } from '../components-v2/opiskeluoikeus/KoodistoSelect'
import { DateEdit, DateView } from '../components-v2/controls/DateField'
import { Järjestämismuotojakso } from '../types/fi/oph/koski/schema/Jarjestamismuotojakso'
import { Oppisopimus } from '../types/fi/oph/koski/schema/Oppisopimus'
import { isOppisopimuksellinenJärjestämismuoto } from '../types/fi/oph/koski/schema/OppisopimuksellinenJarjestamismuoto'
import { JärjestämismuotoIlmanLisätietoja } from '../types/fi/oph/koski/schema/JarjestamismuotoIlmanLisatietoja'
import {
  LocalizedTextEdit,
  LocalizedTextView
} from '../components-v2/controls/LocalizedTestField'
import { Yritys } from '../types/fi/oph/koski/schema/Yritys'
import { TextEdit, TextView } from '../components-v2/controls/TextField'
import {
  isOppisopimuksenPurkaminen,
  OppisopimuksenPurkaminen
} from '../types/fi/oph/koski/schema/OppisopimuksenPurkaminen'
import { CHARCODE_REMOVE } from '../components-v2/texts/Icon'
import { IconButton } from '../components-v2/controls/IconButton'
import { OsaamisenHankkimistapajakso } from '../types/fi/oph/koski/schema/OsaamisenHankkimistapajakso'
import { isOppisopimuksellinenOsaamisenHankkimistapa } from '../types/fi/oph/koski/schema/OppisopimuksellinenOsaamisenHankkimistapa'
import { OsaamisenHankkimistapaIlmanLisätietoja } from '../types/fi/oph/koski/schema/OsaamisenHankkimistapaIlmanLisatietoja'
import { Työssäoppimisjakso } from '../types/fi/oph/koski/schema/Tyossaoppimisjakso'
import { LaajuusOsaamispisteissä } from '../types/fi/oph/koski/schema/LaajuusOsaamispisteissa'
import {
  LaajuusEdit,
  LaajuusView
} from '../components-v2/opiskeluoikeus/LaajuusField'
import { Koulutussopimusjakso } from '../types/fi/oph/koski/schema/Koulutussopimusjakso'
import { NumberField } from '../components-v2/controls/NumberField'
import {
  OsasuoritusTables,
  OsasuoritusTablesUseastaTutkinnosta
} from './OsasuoritusTables'
import { HenkilövahvistusValinnaisellaPaikkakunnalla } from '../types/fi/oph/koski/schema/HenkilovahvistusValinnaisellaPaikkakunnalla'
import { OpenAllButton, useTree } from '../appstate/tree'
import { AmisLaajuudetYhteensä } from './AmisLaajuudetYhteensä'
import { SisältyyOpiskeluoikeuteen } from './SisältyyOpiskeluoikeuteen'
import {
  AmmatillisenTutkinnonOsittainenUseastaTutkinnostaSuoritus,
  isAmmatillisenTutkinnonOsittainenUseastaTutkinnostaSuoritus
} from '../types/fi/oph/koski/schema/AmmatillisenTutkinnonOsittainenUseastaTutkinnostaSuoritus'

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
  if (
    oo.suoritukset[0].tyyppi.koodiarvo === 'ammatillinentutkintoosittainen' &&
    oo.suoritukset[0].koulutusmoduuli.tunniste.koodiarvo !==
      'ammatillinentutkintoosittainenuseastatutkinnosta'
  ) {
    return (
      t(oo.suoritukset[0].koulutusmoduuli.tunniste.nimi) + t(', osittainen')
    )
  } else return t(oo.suoritukset[0].koulutusmoduuli.tunniste.nimi)
}

const AmmatillinenPäätasonSuoritusEditor: React.FC<
  AmmatillinenEditorProps & {
    form: FormModel<AmmatillinenOpiskeluoikeus>
  }
> = (props) => {
  if (
    isAmmatillisenTutkinnonOsittainenUseastaTutkinnostaSuoritus(
      props.form.state.suoritukset[0]
    )
  ) {
    return <AmmatillinenTutkintoOsittainenUseastaTutkinnostaEditor {...props} />
  } else {
    return <AmmatillinenTutkintoOsittainenEditor {...props} />
  }
}

const AmmatillisenOsittaisenSuorituksenTiedot: React.FC<{
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
      <KeyValueRow localizableLabel="Toinen osaamisala">
        <FormField
          form={form}
          view={BooleanView}
          edit={BooleanEdit}
          path={path.prop('toinenOsaamisala')}
        />
      </KeyValueRow>
      <KeyValueRow localizableLabel="Oppilaitos / toimipiste">
        <FormField
          form={form}
          path={path.prop('toimipiste')}
          view={OrganisaatioView}
          edit={OrganisaatioEdit}
        />
      </KeyValueRow>
      <KeyValueRow localizableLabel="Alkamispäivä">
        <FormField
          form={form}
          view={DateView}
          edit={DateEdit}
          path={path.prop('alkamispäivä')}
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
      <KeyValueRow localizableLabel="Järjestämismuodot">
        <FormListField
          form={form}
          view={JärjestämismouotoView}
          edit={JärjestämismouotoEdit}
          path={path.prop('järjestämismuodot')}
          removable
        />
        {form.editMode && (
          <ButtonGroup>
            <FlatButton
              onClick={() => {
                form.updateAt(
                  path.prop('järjestämismuodot').valueOr([]),
                  append(emptyJärjestämismuoto)
                )
              }}
            >
              {t('Lisää')}
            </FlatButton>
          </ButtonGroup>
        )}
      </KeyValueRow>
      <KeyValueRow localizableLabel="Osaamisen hankkimistapa">
        <FormListField
          form={form}
          view={OsaamisenHankkimistapaView}
          edit={OsaamisenHankkimistapaEdit}
          path={path.prop('osaamisenHankkimistavat')}
          removable
        />
        {form.editMode && (
          <ButtonGroup>
            <FlatButton
              onClick={() => {
                form.updateAt(
                  path.prop('osaamisenHankkimistavat').valueOr([]),
                  append(emptyOsaamisenHankkimistapa)
                )
              }}
            >
              {t('Lisää')}
            </FlatButton>
          </ButtonGroup>
        )}
      </KeyValueRow>
      <KeyValueRow localizableLabel="Työssäoppimisjaksot">
        <FormListField
          form={form}
          view={TyössäoppimisjaksoView}
          edit={TyössäoppimisjaksoEdit}
          path={path.prop('työssäoppimisjaksot')}
          removable
        />
        {form.editMode && (
          <ButtonGroup>
            <FlatButton
              onClick={() => {
                form.updateAt(
                  path.prop('työssäoppimisjaksot').valueOr([]),
                  append(emptyTyössäoppimisjakso)
                )
              }}
            >
              {t('Lisää')}
            </FlatButton>
          </ButtonGroup>
        )}
      </KeyValueRow>
      <KeyValueRow localizableLabel="Koulutussopimukset">
        <FormListField
          form={form}
          view={KoulutusspomiusView}
          edit={KoulutusspomiusEdit}
          path={path.prop('koulutussopimukset')}
          removable
        />
        {form.editMode && (
          <ButtonGroup>
            <FlatButton
              onClick={() => {
                form.updateAt(
                  path.prop('koulutussopimukset').valueOr([]),
                  append(emptyKoulutusspomius)
                )
              }}
            >
              {t('Lisää')}
            </FlatButton>
          </ButtonGroup>
        )}
      </KeyValueRow>
      <KeyValueRow localizableLabel="Todistuksella näkyvät lisätiedot">
        <FormField
          form={form}
          view={LocalizedTextView}
          edit={LocalizedTextEdit}
          path={path.prop('todistuksellaNäkyvätLisätiedot')}
        />
      </KeyValueRow>
      <KeyValueRow localizableLabel="Ryhmä">
        <FormField
          form={form}
          view={TextView}
          edit={TextEdit}
          path={path.prop('ryhmä')}
        />
      </KeyValueRow>
      <KeyValueRow localizableLabel="Painotettu keskiarvo">
        <FormField
          testId={'painotettu-keskiarvo'}
          form={form}
          view={TextView}
          edit={NumberField}
          path={path.prop('keskiarvo')}
        />
      </KeyValueRow>
      <KeyValueRow localizableLabel="Sisältää mukautettuja arvosanoja">
        <FormField
          form={form}
          view={BooleanView}
          edit={BooleanEdit}
          path={path.prop('keskiarvoSisältääMukautettujaArvosanoja')}
        />
      </KeyValueRow>
      <KeyValueRow localizableLabel="Korotetun suorituksen alkuperäinen opiskeluoikeus">
        <FormField
          form={form}
          view={TextView}
          edit={TextEdit}
          path={path.prop('korotettuOpiskeluoikeusOid')}
        />
      </KeyValueRow>
      <KeyValueRow localizableLabel="Korotettu painotettu keskiarvo">
        <FormField
          form={form}
          view={TextView}
          edit={NumberField}
          path={path.prop('korotettuKeskiarvo')}
        />
      </KeyValueRow>
      <KeyValueRow localizableLabel="Korotus sisältää mukautettuja arvosanoja">
        <FormField
          form={form}
          view={BooleanView}
          edit={BooleanEdit}
          path={path.prop('korotettuKeskiarvoSisältääMukautettujaArvosanoja')}
        />
      </KeyValueRow>
    </KeyValueTable>
  )
}

const AmmatillisenOsittaisenUseastaTutkinnostaSuorituksenTiedot: React.FC<{
  form: FormModel<AmmatillinenOpiskeluoikeus>
  päätasonSuoritus: ActivePäätasonSuoritus<
    AmmatillinenOpiskeluoikeus,
    AmmatillisenTutkinnonOsittainenUseastaTutkinnostaSuoritus
  >
}> = ({ form, päätasonSuoritus }) => {
  const path = päätasonSuoritus.path

  return (
    <KeyValueTable>
      <KeyValueRow localizableLabel="Koulutus">
        <TestIdText id="koulutus">
          {t(päätasonSuoritus.suoritus.koulutusmoduuli.tunniste.nimi)}
        </TestIdText>{' '}
      </KeyValueRow>
      <KeyValueRow localizableLabel="Suoritustapa">
        <TestIdText id="suoritustapa">
          {t(päätasonSuoritus.suoritus.suoritustapa.nimi)}
        </TestIdText>
      </KeyValueRow>
      <KeyValueRow localizableLabel="Tutkintonimike">
        <FormListField
          form={form}
          view={KoodistoView}
          edit={KoodistoEdit}
          editProps={{ koodistoUri: 'tutkintonimikkeet' }}
          path={path.prop('tutkintonimike')}
          removable
          testId="tutkintonimike"
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
      <KeyValueRow localizableLabel="Toinen osaamisala">
        <FormField
          form={form}
          view={BooleanView}
          edit={BooleanEdit}
          path={path.prop('toinenOsaamisala')}
        />
      </KeyValueRow>
      <KeyValueRow localizableLabel="Oppilaitos / toimipiste">
        <FormField
          form={form}
          path={path.prop('toimipiste')}
          view={OrganisaatioView}
          edit={OrganisaatioEdit}
        />
      </KeyValueRow>
      <KeyValueRow localizableLabel="Alkamispäivä">
        <FormField
          form={form}
          view={DateView}
          edit={DateEdit}
          path={path.prop('alkamispäivä')}
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
      <KeyValueRow localizableLabel="Järjestämismuodot">
        <FormListField
          form={form}
          view={JärjestämismouotoView}
          edit={JärjestämismouotoEdit}
          path={path.prop('järjestämismuodot')}
          removable
        />
        {form.editMode && (
          <ButtonGroup>
            <FlatButton
              onClick={() => {
                form.updateAt(
                  path.prop('järjestämismuodot').valueOr([]),
                  append(emptyJärjestämismuoto)
                )
              }}
            >
              {t('Lisää')}
            </FlatButton>
          </ButtonGroup>
        )}
      </KeyValueRow>
      <KeyValueRow localizableLabel="Osaamisen hankkimistapa">
        <FormListField
          form={form}
          view={OsaamisenHankkimistapaView}
          edit={OsaamisenHankkimistapaEdit}
          path={path.prop('osaamisenHankkimistavat')}
          removable
        />
        {form.editMode && (
          <ButtonGroup>
            <FlatButton
              onClick={() => {
                form.updateAt(
                  path.prop('osaamisenHankkimistavat').valueOr([]),
                  append(emptyOsaamisenHankkimistapa)
                )
              }}
            >
              {t('Lisää')}
            </FlatButton>
          </ButtonGroup>
        )}
      </KeyValueRow>
      <KeyValueRow localizableLabel="Työssäoppimisjaksot">
        <FormListField
          form={form}
          view={TyössäoppimisjaksoView}
          edit={TyössäoppimisjaksoEdit}
          path={path.prop('työssäoppimisjaksot')}
          removable
        />
        {form.editMode && (
          <ButtonGroup>
            <FlatButton
              onClick={() => {
                form.updateAt(
                  path.prop('työssäoppimisjaksot').valueOr([]),
                  append(emptyTyössäoppimisjakso)
                )
              }}
            >
              {t('Lisää')}
            </FlatButton>
          </ButtonGroup>
        )}
      </KeyValueRow>
      <KeyValueRow localizableLabel="Koulutussopimukset">
        <FormListField
          form={form}
          view={KoulutusspomiusView}
          edit={KoulutusspomiusEdit}
          path={path.prop('koulutussopimukset')}
          removable
        />
        {form.editMode && (
          <ButtonGroup>
            <FlatButton
              onClick={() => {
                form.updateAt(
                  path.prop('koulutussopimukset').valueOr([]),
                  append(emptyKoulutusspomius)
                )
              }}
            >
              {t('Lisää')}
            </FlatButton>
          </ButtonGroup>
        )}
      </KeyValueRow>
      <KeyValueRow localizableLabel="Todistuksella näkyvät lisätiedot">
        <FormField
          form={form}
          view={LocalizedTextView}
          edit={LocalizedTextEdit}
          path={path.prop('todistuksellaNäkyvätLisätiedot')}
          testId="todistuksellaNäkyvätLisätiedot"
        />
      </KeyValueRow>
      <KeyValueRow localizableLabel="Ryhmä">
        <FormField
          form={form}
          view={TextView}
          edit={TextEdit}
          path={path.prop('ryhmä')}
        />
      </KeyValueRow>
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

  const { TreeNode, ...tree } = useTree()

  return (
    <TreeNode>
      <EditorContainer
        form={props.form}
        oppijaOid={props.oppijaOid}
        invalidatable={props.invalidatable}
        onChangeSuoritus={setPäätasonSuoritus}
        testId={päätasonSuoritus.testId}
        createOpiskeluoikeusjakso={createAmmatillinenOpiskeluoikeusJakso}
        lisätiedotContainer={AmmatillinenLisatiedot}
        additionalOpiskeluoikeusFields={SisältyyOpiskeluoikeuteen}
      >
        <AmmatillisenOsittaisenSuorituksenTiedot
          form={props.form}
          päätasonSuoritus={osittainenPäätasonSuoritus}
        />

        <Spacer />

        <SuorituksenVahvistusField
          form={props.form}
          suoritusPath={päätasonSuoritus.path}
          organisaatio={organisaatio}
          disableAdd={false}
          vahvistusClass={HenkilövahvistusValinnaisellaPaikkakunnalla.className}
        />

        <Spacer />
        <OpenAllButton {...tree} />
        <Spacer />
        <OsasuoritusTables
          form={props.form}
          osittainenPäätasonSuoritus={osittainenPäätasonSuoritus}
        />
        <AmisLaajuudetYhteensä suoritus={osittainenPäätasonSuoritus.suoritus} />
      </EditorContainer>
    </TreeNode>
  )
}

const AmmatillinenTutkintoOsittainenUseastaTutkinnostaEditor: React.FC<
  AmmatillinenEditorProps & {
    form: FormModel<AmmatillinenOpiskeluoikeus>
  }
> = (props) => {
  const [päätasonSuoritus, setPäätasonSuoritus] = usePäätasonSuoritus(
    props.form
  )
  const osittainenUseastaTutkinnostaSuoritus =
    päätasonSuoritus as ActivePäätasonSuoritus<
      AmmatillinenOpiskeluoikeus,
      AmmatillisenTutkinnonOsittainenUseastaTutkinnostaSuoritus
    >

  const organisaatio =
    props.opiskeluoikeus.oppilaitos || props.opiskeluoikeus.koulutustoimija

  const createAmmatillinenOpiskeluoikeusJakso = (
    seed: UusiOpiskeluoikeusjakso<AmmatillinenOpiskeluoikeusjakso>
  ) => AmmatillinenOpiskeluoikeusjakso(seed)

  const { TreeNode, ...tree } = useTree()

  return (
    <TreeNode>
      <EditorContainer
        form={props.form}
        oppijaOid={props.oppijaOid}
        invalidatable={props.invalidatable}
        onChangeSuoritus={setPäätasonSuoritus}
        testId={päätasonSuoritus.testId}
        createOpiskeluoikeusjakso={createAmmatillinenOpiskeluoikeusJakso}
        lisätiedotContainer={AmmatillinenLisatiedot}
        additionalOpiskeluoikeusFields={SisältyyOpiskeluoikeuteen}
        suorituksenNimi={(suoritus) =>
          localize(t(suoritus.koulutusmoduuli.tunniste.nimi))
        }
      >
        <AmmatillisenOsittaisenUseastaTutkinnostaSuorituksenTiedot
          form={props.form}
          päätasonSuoritus={osittainenUseastaTutkinnostaSuoritus}
        />
        <Spacer />
        <SuorituksenVahvistusField
          form={props.form}
          suoritusPath={päätasonSuoritus.path}
          organisaatio={organisaatio}
          disableAdd={false}
          vahvistusClass={HenkilövahvistusValinnaisellaPaikkakunnalla.className}
        />
        <Spacer />
        <OpenAllButton {...tree} />
        <Spacer />
        <OsasuoritusTablesUseastaTutkinnosta
          form={props.form}
          osittainenPäätasonSuoritus={osittainenUseastaTutkinnostaSuoritus}
        />
        <AmisLaajuudetYhteensä
          suoritus={osittainenUseastaTutkinnostaSuoritus.suoritus}
        />
      </EditorContainer>
    </TreeNode>
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
        <TestIdText id="osaamisala">{t(value.osaamisala.nimi)}</TestIdText>
        {' : '}
        <TestIdText id="alku">
          {value?.alku && ISO2FinnishDate(value.alku)}
        </TestIdText>{' '}
        {' - '}
        <TestIdText id="loppu">
          {value?.loppu && ISO2FinnishDate(value.loppu)}
        </TestIdText>
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
      <div className="AikajaksoEdit" data-testid="osaamisala">
        <KoodistoSelect
          koodistoUri={'osaamisala'}
          onSelect={(koodiviite) => {
            koodiviite && onChange({ ...value, osaamisala: koodiviite })
          }}
          value={value.osaamisala.koodiarvo}
          testId="osaamisala"
        />
        <span className="AikajaksoEdit__separator"> {' : '}</span>
        <DateInput
          value={value?.alku}
          onChange={(alku?: string) => {
            alku && onChange({ ...value, alku })
          }}
          testId="alku"
        />
        <span className="AikajaksoEdit__separator"> {' - '}</span>
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

const JärjestämismouotoView = <T extends Järjestämismuotojakso>({
  value
}: CommonProps<FieldViewerProps<T | undefined, EmptyObject>>) => {
  return (
    <>
      <TestIdText id="alku">
        {value?.alku && ISO2FinnishDate(value.alku)}
      </TestIdText>
      {' - '}
      <TestIdText id="loppu">
        {value?.loppu && ISO2FinnishDate(value.loppu)}
      </TestIdText>
      {' : '}
      <TestIdText id="järjestämismuoto">
        {t(value?.järjestämismuoto.tunniste.nimi)}
      </TestIdText>
      {isOppisopimuksellinenJärjestämismuoto(value?.järjestämismuoto) && (
        <OppisopimusView value={value?.järjestämismuoto?.oppisopimus} />
      )}
    </>
  )
}

const emptyJärjestämismuoto: Järjestämismuotojakso = {
  alku: todayISODate(),
  järjestämismuoto: {
    $class: 'fi.oph.koski.schema.JärjestämismuotoIlmanLisätietoja',
    tunniste: Koodistokoodiviite({
      koodistoUri: 'jarjestamismuoto',
      koodiarvo: ''
    })
  },
  $class: 'fi.oph.koski.schema.Järjestämismuotojakso'
}

const JärjestämismouotoEdit = ({
  value,
  onChange
}: FieldEditorProps<Järjestämismuotojakso | undefined, EmptyObject>) => {
  return (
    <>
      <div className="AikajaksoEdit">
        <DateInput
          value={value?.alku}
          onChange={(alku?: string) => {
            alku && onChange({ ...emptyJärjestämismuoto, ...value, alku })
          }}
          testId="alku"
        />
        <span className="AikajaksoEdit__separator"> {' - '}</span>
        <DateInput
          value={value?.loppu}
          onChange={(loppu?: string) => {
            loppu && onChange({ ...emptyJärjestämismuoto, ...value, loppu })
          }}
          testId="loppu"
        />
        <span className="AikajaksoEdit__separator"> {' : '}</span>
        <KoodistoSelect
          koodistoUri={'jarjestamismuoto'}
          value={value?.järjestämismuoto.tunniste.koodiarvo}
          onSelect={(tunniste) =>
            tunniste &&
            onChange({
              ...emptyJärjestämismuoto,
              ...value,
              järjestämismuoto: JärjestämismuotoIlmanLisätietoja({ tunniste })
            })
          }
          testId="järjestämismuoto"
        />
      </div>
      {value?.järjestämismuoto.tunniste.koodiarvo === '20' && (
        <OppisopimusEdit
          value={
            isOppisopimuksellinenJärjestämismuoto(value?.järjestämismuoto)
              ? value?.järjestämismuoto.oppisopimus
              : undefined
          }
          onChange={(oppisopimus) =>
            oppisopimus &&
            onChange({
              ...value,
              järjestämismuoto: {
                ...value.järjestämismuoto,
                $class:
                  'fi.oph.koski.schema.OppisopimuksellinenJärjestämismuoto',
                tunniste: Koodistokoodiviite({
                  koodiarvo: '20',
                  koodistoUri: 'jarjestamismuoto'
                }),
                oppisopimus
              }
            })
          }
        />
      )}
    </>
  )
}

const OppisopimusView = <T extends Oppisopimus>({
  value
}: CommonProps<FieldViewerProps<T | undefined, EmptyObject>>) => {
  return (
    <KeyValueTable>
      <KeyValueRow localizableLabel="Yritys">
        {t(value?.työnantaja.nimi)}
      </KeyValueRow>
      <KeyValueRow localizableLabel="Y-tunnus">
        {value?.työnantaja.yTunnus}
      </KeyValueRow>
      <KeyValueRow localizableLabel="Oppisopimuksen purkaminen">
        <KeyValueTable>
          <KeyValueRow localizableLabel="Päivä">
            {ISO2FinnishDate(value?.oppisopimuksenPurkaminen?.päivä)}
          </KeyValueRow>
          <KeyValueRow localizableLabel="Purettu koeajalla">
            <BooleanView
              value={value?.oppisopimuksenPurkaminen?.purettuKoeajalla}
            />
          </KeyValueRow>
        </KeyValueTable>
      </KeyValueRow>
    </KeyValueTable>
  )
}

const emptyYritys = Yritys({
  nimi: localize(''),
  yTunnus: ''
})

const emptyOppisopimus: Oppisopimus = Oppisopimus({
  työnantaja: emptyYritys,
  oppisopimuksenPurkaminen: {
    $class: 'fi.oph.koski.schema.OppisopimuksenPurkaminen',
    päivä: todayISODate(),
    purettuKoeajalla: false
  }
})

const OppisopimusEdit = ({
  value,
  onChange
}: FieldEditorProps<Oppisopimus | undefined, EmptyObject>) => {
  return (
    <KeyValueTable>
      <KeyValueRow localizableLabel="Yritys">
        <LocalizedTextEdit
          value={value?.työnantaja.nimi}
          onChange={(nimi) =>
            nimi &&
            onChange(
              Oppisopimus({
                ...value,
                työnantaja: Yritys({
                  ...emptyYritys,
                  ...value?.työnantaja,
                  nimi
                })
              })
            )
          }
        />
      </KeyValueRow>
      <KeyValueRow localizableLabel="Y-tunnus">
        <TextEdit
          value={value?.työnantaja.yTunnus}
          onChange={(yTunnus) =>
            yTunnus &&
            onChange(
              Oppisopimus({
                ...value,
                työnantaja: Yritys({
                  ...emptyYritys,
                  ...value?.työnantaja,
                  yTunnus
                })
              })
            )
          }
        />
      </KeyValueRow>
      <OppisopimuksenPurkaminenEdit
        value={value?.oppisopimuksenPurkaminen}
        onChange={(oppisopimuksenPurkaminen) =>
          onChange(
            Oppisopimus({
              ...emptyOppisopimus,
              ...value,
              oppisopimuksenPurkaminen
            })
          )
        }
      />
    </KeyValueTable>
  )
}

const OppisopimuksenPurkaminenEdit = ({
  value,
  onChange
}: FieldEditorProps<OppisopimuksenPurkaminen | undefined, EmptyObject>) => {
  return (
    <KeyValueRow localizableLabel="Oppisopimuksen purkaminen">
      {isOppisopimuksenPurkaminen(value) ? (
        <KeyValueTable>
          <KeyValueRow localizableLabel="Päivä">
            <DateInput
              value={value?.päivä}
              onChange={(päivä) =>
                päivä &&
                onChange({
                  ...emptyOppisopimus.oppisopimuksenPurkaminen,
                  ...value,
                  päivä
                })
              }
            />
          </KeyValueRow>
          <KeyValueRow localizableLabel="Purettu koeajalla">
            <BooleanEdit
              value={value?.purettuKoeajalla}
              onChange={(purettuKoeajalla) => {
                if (purettuKoeajalla !== undefined) {
                  onChange({
                    ...emptyOppisopimus.oppisopimuksenPurkaminen,
                    ...value,
                    purettuKoeajalla
                  })
                }
              }}
            />
            <IconButton
              charCode={CHARCODE_REMOVE}
              label={t('Poista')}
              size="input"
              onClick={() => onChange(undefined)}
              testId="delete"
            />
          </KeyValueRow>
        </KeyValueTable>
      ) : (
        <ButtonGroup>
          <FlatButton
            onClick={() => onChange(emptyOppisopimus.oppisopimuksenPurkaminen)}
          >
            {t('Lisää')}
          </FlatButton>
        </ButtonGroup>
      )}
    </KeyValueRow>
  )
}

const OsaamisenHankkimistapaView = <T extends OsaamisenHankkimistapajakso>({
  value
}: CommonProps<FieldViewerProps<T | undefined, EmptyObject>>) => {
  return (
    <>
      <TestIdText id="alku">
        {value?.alku && ISO2FinnishDate(value.alku)}
      </TestIdText>
      {' - '}
      <TestIdText id="loppu">
        {value?.loppu && ISO2FinnishDate(value.loppu)}
      </TestIdText>
      {' : '}
      <TestIdText id="osaamisenHankkimistapa">
        {t(value?.osaamisenHankkimistapa.tunniste.nimi)}
      </TestIdText>
      {isOppisopimuksellinenOsaamisenHankkimistapa(
        value?.osaamisenHankkimistapa
      ) && (
        <OppisopimusView value={value?.osaamisenHankkimistapa?.oppisopimus} />
      )}
    </>
  )
}

const emptyOsaamisenHankkimistapa: OsaamisenHankkimistapajakso =
  OsaamisenHankkimistapajakso({
    alku: todayISODate(),
    osaamisenHankkimistapa: {
      $class: 'fi.oph.koski.schema.OsaamisenHankkimistapaIlmanLisätietoja',
      tunniste: Koodistokoodiviite({
        koodistoUri: 'osaamisenhankkimistapa',
        koodiarvo: ''
      })
    }
  })

const OsaamisenHankkimistapaEdit = ({
  value,
  onChange
}: FieldEditorProps<OsaamisenHankkimistapajakso | undefined, EmptyObject>) => {
  return (
    <>
      <div className="AikajaksoEdit">
        <DateInput
          value={value?.alku}
          onChange={(alku?: string) => {
            alku && onChange({ ...emptyOsaamisenHankkimistapa, ...value, alku })
          }}
          testId="alku"
        />
        <span className="AikajaksoEdit__separator"> {' - '}</span>
        <DateInput
          value={value?.loppu}
          onChange={(loppu?: string) => {
            loppu &&
              onChange({ ...emptyOsaamisenHankkimistapa, ...value, loppu })
          }}
          testId="loppu"
        />
        <span className="AikajaksoEdit__separator"> {' : '}</span>
        <KoodistoSelect
          koodistoUri={'osaamisenhankkimistapa'}
          value={value?.osaamisenHankkimistapa.tunniste.koodiarvo}
          onSelect={(tunniste) =>
            tunniste &&
            onChange({
              ...emptyOsaamisenHankkimistapa,
              ...value,
              osaamisenHankkimistapa: OsaamisenHankkimistapaIlmanLisätietoja({
                tunniste
              })
            })
          }
          testId="osaamisenHankkimistapa"
        />
      </div>
      {value?.osaamisenHankkimistapa.tunniste.koodiarvo === 'oppisopimus' && (
        <OppisopimusEdit
          value={
            isOppisopimuksellinenOsaamisenHankkimistapa(
              value?.osaamisenHankkimistapa
            )
              ? value?.osaamisenHankkimistapa.oppisopimus
              : undefined
          }
          onChange={(oppisopimus) =>
            oppisopimus &&
            onChange({
              ...value,
              osaamisenHankkimistapa: {
                ...value.osaamisenHankkimistapa,
                $class:
                  'fi.oph.koski.schema.OppisopimuksellinenOsaamisenHankkimistapa',
                tunniste: Koodistokoodiviite({
                  koodiarvo: 'oppisopimus',
                  koodistoUri: 'osaamisenhankkimistapa'
                }),
                oppisopimus
              }
            })
          }
        />
      )}
    </>
  )
}

const TyössäoppimisjaksoView = <T extends Työssäoppimisjakso>({
  value
}: CommonProps<FieldViewerProps<T | undefined, EmptyObject>>) => {
  return (
    <>
      <TestIdText id="alku">
        {value?.alku && ISO2FinnishDate(value.alku)}
      </TestIdText>
      {' - '}
      <TestIdText id="loppu">
        {value?.loppu && ISO2FinnishDate(value.loppu)}
      </TestIdText>
      <br />
      {t(value?.paikkakunta.nimi) + ', ' + t(value?.maa.nimi)}
      <KeyValueRow localizableLabel="Työssäoppimispaikka">
        {t(value?.työssäoppimispaikka)}
      </KeyValueRow>
      <KeyValueRow localizableLabel="Työtehtävät">
        {t(value?.työtehtävät)}
      </KeyValueRow>
      <KeyValueRow localizableLabel="Laajuus">
        <LaajuusView value={value?.laajuus} />
      </KeyValueRow>
    </>
  )
}

const emptyTyössäoppimisjakso: Työssäoppimisjakso = Työssäoppimisjakso({
  alku: todayISODate(),
  laajuus: LaajuusOsaamispisteissä({ arvo: 1 }),
  paikkakunta: Koodistokoodiviite({
    koodistoUri: 'kunta',
    koodiarvo: '199'
  }),
  maa: Koodistokoodiviite({
    koodistoUri: 'maatjavaltiot2',
    koodiarvo: '999'
  })
})

const TyössäoppimisjaksoEdit = ({
  value,
  onChange
}: FieldEditorProps<Työssäoppimisjakso | undefined, EmptyObject>) => {
  return (
    <>
      <div className="AikajaksoEdit">
        <DateInput
          value={value?.alku}
          onChange={(alku?: string) => {
            alku && onChange({ ...emptyTyössäoppimisjakso, ...value, alku })
          }}
          testId="alku"
        />
        <span className="AikajaksoEdit__separator"> {' - '}</span>
        <DateInput
          value={value?.loppu}
          onChange={(loppu?: string) => {
            loppu && onChange({ ...emptyTyössäoppimisjakso, ...value, loppu })
          }}
          testId="loppu"
        />
      </div>
      <KeyValueRow localizableLabel="Paikkakunta">
        <KoodistoSelect
          koodistoUri="kunta"
          value={value?.paikkakunta.koodiarvo}
          onSelect={(paikkakunta) =>
            paikkakunta &&
            onChange({ ...emptyTyössäoppimisjakso, ...value, paikkakunta })
          }
          testId={'paikkakunta'}
        />
      </KeyValueRow>

      <KeyValueRow localizableLabel="Maa">
        <KoodistoSelect
          koodistoUri="maatjavaltiot2"
          value={value?.maa.koodiarvo}
          onSelect={(maa) =>
            maa && onChange({ ...emptyTyössäoppimisjakso, ...value, maa })
          }
          testId={'maa'}
        />
      </KeyValueRow>
      <KeyValueRow localizableLabel="Työssäoppimispaikka">
        <LocalizedTextEdit
          value={value?.työssäoppimispaikka}
          onChange={(työssäoppimispaikka) =>
            onChange({
              ...emptyTyössäoppimisjakso,
              ...value,
              työssäoppimispaikka
            })
          }
        />
      </KeyValueRow>
      <KeyValueRow localizableLabel="Työtehtävät">
        <LocalizedTextEdit
          value={value?.työtehtävät}
          onChange={(työtehtävät) =>
            onChange({
              ...emptyTyössäoppimisjakso,
              ...value,
              työtehtävät
            })
          }
        />
      </KeyValueRow>
      <KeyValueRow localizableLabel="Laajuus">
        <LaajuusEdit
          value={value?.laajuus}
          createLaajuus={(arvo) => LaajuusOsaamispisteissä({ arvo })}
          onChange={(laajuus) =>
            laajuus &&
            onChange({
              ...emptyTyössäoppimisjakso,
              ...value,
              laajuus
            })
          }
        />
      </KeyValueRow>
    </>
  )
}

const KoulutusspomiusView = <T extends Koulutussopimusjakso>({
  value
}: CommonProps<FieldViewerProps<T | undefined, EmptyObject>>) => {
  return (
    <>
      <TestIdText id="alku">
        {value?.alku && ISO2FinnishDate(value.alku)}
      </TestIdText>
      {' - '}
      <TestIdText id="loppu">
        {value?.loppu && ISO2FinnishDate(value.loppu)}
      </TestIdText>
      <br />
      {t(value?.paikkakunta.nimi) + ', ' + t(value?.maa.nimi)}
      <KeyValueRow localizableLabel="Työssäoppimispaikka">
        {t(value?.työssäoppimispaikka)}
      </KeyValueRow>
      <KeyValueRow localizableLabel="Työssäoppimispaikan Y-tunnus">
        {t(value?.työssäoppimispaikanYTunnus)}
      </KeyValueRow>
      <KeyValueRow localizableLabel="Työtehtävät">
        {t(value?.työtehtävät)}
      </KeyValueRow>
    </>
  )
}

const emptyKoulutusspomius: Koulutussopimusjakso = Koulutussopimusjakso({
  alku: todayISODate(),
  paikkakunta: Koodistokoodiviite({
    koodistoUri: 'kunta',
    koodiarvo: '199'
  }),
  maa: Koodistokoodiviite({
    koodistoUri: 'maatjavaltiot2',
    koodiarvo: '999'
  })
})

const KoulutusspomiusEdit = ({
  value,
  onChange
}: FieldEditorProps<Koulutussopimusjakso | undefined, EmptyObject>) => {
  return (
    <>
      <div className="AikajaksoEdit">
        <DateInput
          value={value?.alku}
          onChange={(alku?: string) => {
            alku && onChange({ ...emptyKoulutusspomius, ...value, alku })
          }}
          testId="alku"
        />
        <span className="AikajaksoEdit__separator"> {' - '}</span>
        <DateInput
          value={value?.loppu}
          onChange={(loppu?: string) => {
            loppu && onChange({ ...emptyKoulutusspomius, ...value, loppu })
          }}
          testId="loppu"
        />
      </div>
      <KeyValueRow localizableLabel="Paikkakunta">
        <KoodistoSelect
          koodistoUri="kunta"
          value={value?.paikkakunta.koodiarvo}
          onSelect={(paikkakunta) =>
            paikkakunta &&
            onChange({ ...emptyKoulutusspomius, ...value, paikkakunta })
          }
          testId={'paikkakunta'}
        />
      </KeyValueRow>

      <KeyValueRow localizableLabel="Maa">
        <KoodistoSelect
          koodistoUri="maatjavaltiot2"
          value={value?.maa.koodiarvo}
          onSelect={(maa) =>
            maa && onChange({ ...emptyKoulutusspomius, ...value, maa })
          }
          testId={'maa'}
        />
      </KeyValueRow>
      <KeyValueRow localizableLabel="Työssäoppimispaikka">
        <LocalizedTextEdit
          value={value?.työssäoppimispaikka}
          onChange={(työssäoppimispaikka) =>
            onChange({
              ...emptyKoulutusspomius,
              ...value,
              työssäoppimispaikka
            })
          }
        />
      </KeyValueRow>
      <KeyValueRow localizableLabel="Työssäoppimispaikan Y-tunnus">
        <TextEdit
          value={value?.työssäoppimispaikanYTunnus}
          onChange={(työssäoppimispaikanYTunnus) =>
            onChange({
              ...emptyKoulutusspomius,
              ...value,
              työssäoppimispaikanYTunnus
            })
          }
        />
      </KeyValueRow>
      <KeyValueRow localizableLabel="Työtehtävät">
        <LocalizedTextEdit
          value={value?.työtehtävät}
          onChange={(työtehtävät) =>
            onChange({
              ...emptyKoulutusspomius,
              ...value,
              työtehtävät
            })
          }
        />
      </KeyValueRow>
    </>
  )
}

export default AmmatillinenEditor
