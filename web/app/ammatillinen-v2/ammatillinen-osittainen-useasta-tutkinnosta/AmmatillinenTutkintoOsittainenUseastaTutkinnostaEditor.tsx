import React from 'react'
import { FormModel } from '../../components-v2/forms/FormModel'
import { AmmatillinenOpiskeluoikeus } from '../../types/fi/oph/koski/schema/AmmatillinenOpiskeluoikeus'
import {
  ActivePäätasonSuoritus,
  EditorContainer,
  usePäätasonSuoritus
} from '../../components-v2/containers/EditorContainer'
import { AmmatillisenTutkinnonOsittainenUseastaTutkinnostaSuoritus } from '../../types/fi/oph/koski/schema/AmmatillisenTutkinnonOsittainenUseastaTutkinnostaSuoritus'
import { UusiOpiskeluoikeusjakso } from '../../components-v2/opiskeluoikeus/UusiOpiskeluoikeudenTilaModal'
import { AmmatillinenOpiskeluoikeusjakso } from '../../types/fi/oph/koski/schema/AmmatillinenOpiskeluoikeusjakso'
import { OpenAllButton, useTree } from '../../appstate/tree'
import { AmmatillinenLisatiedot } from '../AmmatillinenLisatiedot'
import { SisältyyOpiskeluoikeuteen } from '../SisältyyOpiskeluoikeuteen'
import { localize, t } from '../../i18n/i18n'
import { Spacer } from '../../components-v2/layout/Spacer'
import { SuorituksenVahvistusField } from '../../components-v2/opiskeluoikeus/SuorituksenVahvistus'
import { HenkilövahvistusValinnaisellaPaikkakunnalla } from '../../types/fi/oph/koski/schema/HenkilovahvistusValinnaisellaPaikkakunnalla'
import { AmisLaajuudetYhteensä } from '../AmisLaajuudetYhteensä'
import {
  AmmatillinenEditorProps,
  emptyJärjestämismuoto,
  emptyKoulutussopimus,
  emptyOsaamisenHankkimistapa,
  emptyTyössäoppimisjakso,
  JärjestämismouotoEdit,
  JärjestämismouotoView,
  KoulutussopimusEdit,
  KoulutussopimusView,
  OsaamisalaEdit,
  OsaamisalaView,
  OsaamisenHankkimistapaEdit,
  OsaamisenHankkimistapaView,
  TyössäoppimisjaksoEdit,
  TyössäoppimisjaksoView
} from '../AmmatillinenEditor'
import {
  KeyValueRow,
  KeyValueTable
} from '../../components-v2/containers/KeyValueTable'
import { TestIdText } from '../../appstate/useTestId'
import { FormListField } from '../../components-v2/forms/FormListField'
import {
  KoodistoEdit,
  KoodistoView
} from '../../components-v2/opiskeluoikeus/KoodistoField'
import { ButtonGroup } from '../../components-v2/containers/ButtonGroup'
import { FlatButton } from '../../components-v2/controls/FlatButton'
import { append } from '../../util/fp/arrays'
import { Koodistokoodiviite } from '../../types/fi/oph/koski/schema/Koodistokoodiviite'
import { FormField } from '../../components-v2/forms/FormField'
import {
  BooleanEdit,
  BooleanView
} from '../../components-v2/opiskeluoikeus/BooleanField'
import { Osaamisalajakso } from '../../types/fi/oph/koski/schema/Osaamisalajakso'
import {
  OrganisaatioEdit,
  OrganisaatioView
} from '../../components-v2/opiskeluoikeus/OrganisaatioField'
import { DateEdit, DateView } from '../../components-v2/controls/DateField'
import {
  LocalizedTextEdit,
  LocalizedTextView
} from '../../components-v2/controls/LocalizedTestField'
import { TextEdit, TextView } from '../../components-v2/controls/TextField'
import { OsasuoritusTablesUseastaTutkinnosta } from './OsasuoritusTablesUseastaTutkinnosta'

export const AmmatillinenTutkintoOsittainenUseastaTutkinnostaEditor: React.FC<
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
          oppilaitosOid={organisaatio?.oid}
          osittainenPäätasonSuoritus={osittainenUseastaTutkinnostaSuoritus}
        />
        <AmisLaajuudetYhteensä
          suoritus={osittainenUseastaTutkinnostaSuoritus.suoritus}
        />
      </EditorContainer>
    </TreeNode>
  )
}

export const AmmatillisenOsittaisenUseastaTutkinnostaSuorituksenTiedot: React.FC<{
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
          viewProps={{ hideFalse: true }}
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
          viewProps={{ hideFalse: true }}
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
          view={KoulutussopimusView}
          edit={KoulutussopimusEdit}
          path={path.prop('koulutussopimukset')}
          removable
        />
        {form.editMode && (
          <ButtonGroup>
            <FlatButton
              onClick={() => {
                form.updateAt(
                  path.prop('koulutussopimukset').valueOr([]),
                  append(emptyKoulutussopimus)
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
