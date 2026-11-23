import * as Eq from 'fp-ts/Eq'
import React from 'react'
import { useSchema } from '../appstate/constraints'
import { OpenAllButton, useTree } from '../appstate/tree'
import { TestIdText } from '../appstate/useTestId'
import { ButtonGroup } from '../components-v2/containers/ButtonGroup'
import {
  ActivePäätasonSuoritus,
  EditorContainer,
  usePäätasonSuoritus
} from '../components-v2/containers/EditorContainer'
import {
  KeyValueRow,
  KeyValueTable
} from '../components-v2/containers/KeyValueTable'
import { VirkailijaKansalainenContainer } from '../components-v2/containers/VirkailijaKansalainenContainer'
import { DateEdit, DateView } from '../components-v2/controls/DateField'
import { FlatButton } from '../components-v2/controls/FlatButton'
import {
  LocalizedTextEdit,
  LocalizedTextView
} from '../components-v2/controls/LocalizedTestField'
import { NumberField } from '../components-v2/controls/NumberField'
import { TextEdit, TextView } from '../components-v2/controls/TextField'
import { FormField } from '../components-v2/forms/FormField'
import { FormListField } from '../components-v2/forms/FormListField'
import { FormModel, FormOptic, useForm } from '../components-v2/forms/FormModel'
import { useRemovePäätasonSuoritus } from '../components-v2/forms/useRemovePaatasonSuoritus'
import { AdaptedOpiskeluoikeusEditorProps } from '../components-v2/interoperability/useUiAdapter'
import { Spacer } from '../components-v2/layout/Spacer'
import {
  BooleanEdit,
  BooleanView
} from '../components-v2/opiskeluoikeus/BooleanField'
import {
  KoodistoEdit,
  KoodistoView
} from '../components-v2/opiskeluoikeus/KoodistoField'
import { KoulutuksenKoodiarvo } from '../components-v2/opiskeluoikeus/KoulutuksenKoodiarvo'
import {
  OrganisaatioEdit,
  OrganisaatioView
} from '../components-v2/opiskeluoikeus/OrganisaatioField'
import { AmmatillinenTyyliContext } from '../components-v2/opiskeluoikeus/OsasuoritusTable'
import { PerusteView } from '../components-v2/opiskeluoikeus/PerusteField'
import { RemovePaatasonSuoritus } from '../components-v2/opiskeluoikeus/RemovePaatasonSuoritus'
import { SuorituksenVahvistusField } from '../components-v2/opiskeluoikeus/SuorituksenVahvistus'
import { UusiOpiskeluoikeusjakso } from '../components-v2/opiskeluoikeus/UusiOpiskeluoikeudenTilaModal'
import { localize, t } from '../i18n/i18n'
import { AmmatillinenArviointiasteikko } from '../ammatillinen-v2/AmmatillinenArviointiasteikko'
import {
  emptyKoulutussopimus,
  emptyOsaamisenHankkimistapa,
  KeskiarvoView,
  KoulutussopimusEdit,
  KoulutussopimusView,
  OsaamisalaEdit,
  OsaamisalaView,
  OsaamisenHankkimistapaEdit,
  OsaamisenHankkimistapaView
} from '../ammatillinen-v2/AmmatillinenEditor'
import { KoulutuskoodiEdit } from '../ammatillinen-v2/KoulutuskoodiEdit'
import { OsasuoritusTables } from '../ammatillinen-v2/OsasuoritusTables'
import { SisältyyOpiskeluoikeuteen } from '../ammatillinen-v2/SisältyyOpiskeluoikeuteen'
import { AhvenanmaanAmmatillinenOpiskeluoikeus } from '../types/fi/oph/koski/schema/AhvenanmaanAmmatillinenOpiskeluoikeus'
import { AhvenanmaanAmmatillinenOpiskeluoikeusjakso } from '../types/fi/oph/koski/schema/AhvenanmaanAmmatillinenOpiskeluoikeusjakso'
import { AhvenanmaanAmmatillinenPäätasonSuoritus } from '../types/fi/oph/koski/schema/AhvenanmaanAmmatillinenPaatasonSuoritus'
import {
  AhvenanmaanAmmatillisenTutkinnonOsittainenSuoritus,
  isAhvenanmaanAmmatillisenTutkinnonOsittainenSuoritus
} from '../types/fi/oph/koski/schema/AhvenanmaanAmmatillisenTutkinnonOsittainenSuoritus'
import { HenkilövahvistusValinnaisellaPaikkakunnalla } from '../types/fi/oph/koski/schema/HenkilovahvistusValinnaisellaPaikkakunnalla'
import { Koodistokoodiviite } from '../types/fi/oph/koski/schema/Koodistokoodiviite'
import { LocalizedString } from '../types/fi/oph/koski/schema/LocalizedString'
import { Osaamisalajakso } from '../types/fi/oph/koski/schema/Osaamisalajakso'
import { append } from '../util/fp/arrays'
import { AhvenanmaanAmmatillinenLisatiedot } from './AhvenanmaanAmmatillinenLisatiedot'

// Kopio manner-Suomen ammatillisen tutkinnon editorista (AmmatillinenEditor).
// Tutkinnon osat näytetään manner-Suomen komponenteilla, koska Ahvenanmaan
// suoritusten osasuoritukset ovat manner-Suomen luokkia. Päätason suorituksilta
// puuttuvat järjestämismuodot ja työssäoppimisjaksot, eikä suoritustapaa
// näytetä, koska se on aina reformi.

export type AhvenanmaanAmmatillinenEditorProps =
  AdaptedOpiskeluoikeusEditorProps<AhvenanmaanAmmatillinenOpiskeluoikeus>

type Opiskeluoikeus = AhvenanmaanAmmatillinenOpiskeluoikeus

const AhvenanmaanAmmatillinenEditor: React.FC<
  AhvenanmaanAmmatillinenEditorProps
> = (props) => {
  const opiskeluoikeusSchema = useSchema(
    AhvenanmaanAmmatillinenOpiskeluoikeus.className
  )
  const form = useForm(props.opiskeluoikeus, false, opiskeluoikeusSchema)

  return (
    <VirkailijaKansalainenContainer
      opiskeluoikeus={form.state}
      opiskeluoikeudenNimi={opiskeluoikeudenNimi(form.state)}
    >
      <AmmatillinenTyyliContext.Provider value={true}>
        <AhvenanmaanAmmatillinenTutkintoEditor {...props} form={form} />
      </AmmatillinenTyyliContext.Provider>
    </VirkailijaKansalainenContainer>
  )
}

// Koodiston nimi voi poiketa perusteen nimestä, joten perusteen nimi on
// ensisijainen kuten manner-Suomen editorissa.
const koulutuksenNimi = (
  suoritus: AhvenanmaanAmmatillinenPäätasonSuoritus
): LocalizedString | undefined =>
  suoritus.koulutusmoduuli.perusteenNimi ||
  suoritus.koulutusmoduuli.tunniste.nimi

const opiskeluoikeudenNimi = (oo: Opiskeluoikeus): string => {
  const nimi = t(koulutuksenNimi(oo.suoritukset[0]))
  return isAhvenanmaanAmmatillisenTutkinnonOsittainenSuoritus(oo.suoritukset[0])
    ? nimi + t(', osittainen')
    : nimi
}

// Tutkinnon osan/osien suorituksen välilehti nimetään suoritustyypin mukaan,
// tutkinnon suoritus koulutuksen mukaan kuten manner-Suomen editorissa.
const päätasonSuorituksenNimi = (
  suoritus: AhvenanmaanAmmatillinenPäätasonSuoritus
): LocalizedString =>
  isAhvenanmaanAmmatillisenTutkinnonOsittainenSuoritus(suoritus)
    ? suoritus.tyyppi.nimi || localize(suoritus.tyyppi.koodiarvo)
    : koulutuksenNimi(suoritus) ||
      localize(suoritus.koulutusmoduuli.tunniste.koodiarvo)

const päätasonSuoritusEq: Eq.Eq<AhvenanmaanAmmatillinenPäätasonSuoritus> = {
  equals: (a, b) => a.$class === b.$class
}

const AhvenanmaanAmmatillinenTutkintoEditor: React.FC<
  AhvenanmaanAmmatillinenEditorProps & {
    form: FormModel<Opiskeluoikeus>
  }
> = (props) => {
  const { form } = props
  const [päätasonSuoritus, setPäätasonSuoritus] = usePäätasonSuoritus(form)

  const organisaatio =
    props.opiskeluoikeus.oppilaitos || props.opiskeluoikeus.koulutustoimija

  const createOpiskeluoikeusjakso = (
    seed: UusiOpiskeluoikeusjakso<AhvenanmaanAmmatillinenOpiskeluoikeusjakso>
  ) => AhvenanmaanAmmatillinenOpiskeluoikeusjakso(seed)

  const { TreeNode, ...tree } = useTree()

  const removePäätasonSuoritus = useRemovePäätasonSuoritus(
    form,
    päätasonSuoritus.suoritus,
    päätasonSuoritusEq,
    () => setPäätasonSuoritus(0)
  )

  return (
    <TreeNode>
      <EditorContainer
        form={form}
        oppijaOid={props.oppijaOid}
        invalidatable={props.invalidatable}
        suoritusIndex={päätasonSuoritus.index}
        onChangeSuoritus={setPäätasonSuoritus}
        testId={päätasonSuoritus.testId}
        createOpiskeluoikeusjakso={createOpiskeluoikeusjakso}
        lisätiedotContainer={AhvenanmaanAmmatillinenLisatiedot}
        additionalOpiskeluoikeusFields={SisältyyOpiskeluoikeuteen}
        suorituksenNimi={päätasonSuorituksenNimi}
      >
        {form.state.suoritukset.length > 1 && (
          <RemovePaatasonSuoritus
            form={form}
            päätasonSuoritus={päätasonSuoritus}
            removePäätasonSuoritus={removePäätasonSuoritus}
            invalidatable={props.invalidatable}
          />
        )}

        <AhvenanmaanAmmatillisenSuorituksenTiedot
          form={form}
          päätasonSuoritus={päätasonSuoritus}
        />

        <Spacer />

        <SuorituksenVahvistusField
          form={form}
          suoritusPath={päätasonSuoritus.path}
          organisaatio={organisaatio}
          disableAdd={false}
          vahvistusClass={HenkilövahvistusValinnaisellaPaikkakunnalla.className}
        />

        <AmmatillinenArviointiasteikko suoritus={päätasonSuoritus.suoritus} />
        <Spacer />
        {(päätasonSuoritus.suoritus.osasuoritukset || []).length > 0 && (
          <>
            <OpenAllButton {...tree} />
            <Spacer />
          </>
        )}
        <OsasuoritusTables
          form={form}
          oppilaitosOid={organisaatio?.oid}
          päätasonSuoritus={päätasonSuoritus}
        />
      </EditorContainer>
    </TreeNode>
  )
}

const AhvenanmaanAmmatillisenSuorituksenTiedot: React.FC<{
  form: FormModel<Opiskeluoikeus>
  päätasonSuoritus: ActivePäätasonSuoritus<Opiskeluoikeus>
}> = ({ form, päätasonSuoritus }) => {
  const { path, suoritus } = päätasonSuoritus
  // Toinen tutkintonimike/osaamisala ja korotustiedot ovat vain tutkinnon
  // osan/osien suorituksella.
  const osittainen = isAhvenanmaanAmmatillisenTutkinnonOsittainenSuoritus(
    suoritus
  )
    ? {
        suoritus,
        path: path as FormOptic<
          Opiskeluoikeus,
          AhvenanmaanAmmatillisenTutkinnonOsittainenSuoritus
        >
      }
    : undefined

  return (
    <KeyValueTable editMode={form.editMode}>
      <KeyValueRow localizableLabel="Koulutus">
        <span className="AmmatillinenTutkinto">
          <TestIdText id="koulutus">{t(koulutuksenNimi(suoritus))}</TestIdText>
          {form.editMode ? (
            <KoulutuskoodiEdit
              form={form}
              path={path.prop('koulutusmoduuli').prop('tunniste')}
              tunniste={suoritus.koulutusmoduuli.tunniste}
              diaarinumero={suoritus.koulutusmoduuli.perusteenDiaarinumero}
            />
          ) : (
            <KoulutuksenKoodiarvo
              koodiarvo={suoritus.koulutusmoduuli.tunniste.koodiarvo}
            />
          )}
          <FormField
            form={form}
            path={path.prop('koulutusmoduuli').prop('perusteenDiaarinumero')}
            view={PerusteView}
            viewProps={{ päättymispäivä: form.state.päättymispäivä }}
          />
        </span>
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
      {osittainen && (
        <KeyValueRow localizableLabel="Toinen tutkintonimike">
          <FormField
            form={form}
            view={BooleanView}
            viewProps={{ hideFalse: true }}
            edit={BooleanEdit}
            path={osittainen.path.prop('toinenTutkintonimike')}
          />
        </KeyValueRow>
      )}
      <KeyValueRow localizableLabel="Osaamisala">
        <FormListField
          form={form}
          view={OsaamisalaView}
          edit={OsaamisalaEdit}
          editProps={{
            perusteenDiaarinumero:
              suoritus.koulutusmoduuli.perusteenDiaarinumero
          }}
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
      {osittainen && (
        <KeyValueRow localizableLabel="Toinen osaamisala">
          <FormField
            form={form}
            view={BooleanView}
            viewProps={{ hideFalse: true }}
            edit={BooleanEdit}
            path={osittainen.path.prop('toinenOsaamisala')}
          />
        </KeyValueRow>
      )}
      <KeyValueRow localizableLabel="Oppilaitos / toimipiste">
        <FormField
          form={form}
          path={path.prop('toimipiste')}
          view={OrganisaatioView}
          edit={OrganisaatioEdit}
        />
      </KeyValueRow>
      {(form.editMode || suoritus.alkamispäivä) && (
        <KeyValueRow localizableLabel="Alkamispäivä">
          <FormField
            form={form}
            view={DateView}
            edit={DateEdit}
            editProps={{ align: 'right' }}
            path={path.prop('alkamispäivä')}
            testId="alkamispäivä"
          />
        </KeyValueRow>
      )}
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
          editProps={{ large: true }}
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
          view={KeskiarvoView}
          edit={NumberField}
          path={path.prop('keskiarvo')}
        />
      </KeyValueRow>
      <KeyValueRow
        localizableLabel="Sisältää mukautettuja arvosanoja"
        hideIfEmpty={suoritus.keskiarvoSisältääMukautettujaArvosanoja}
      >
        <FormField
          testId={'keskiarvo-sisaltaa-mukautettuja'}
          form={form}
          view={BooleanView}
          edit={BooleanEdit}
          path={path.prop('keskiarvoSisältääMukautettujaArvosanoja')}
        />
      </KeyValueRow>
      {osittainen && (
        <KeyValueRow localizableLabel="Korotetun suorituksen alkuperäinen opiskeluoikeus">
          <FormField
            form={form}
            view={TextView}
            edit={TextEdit}
            path={osittainen.path.prop('korotettuOpiskeluoikeusOid')}
          />
        </KeyValueRow>
      )}
      {osittainen && (
        <KeyValueRow localizableLabel="Korotettu painotettu keskiarvo">
          <FormField
            form={form}
            view={KeskiarvoView}
            edit={NumberField}
            path={osittainen.path.prop('korotettuKeskiarvo')}
          />
        </KeyValueRow>
      )}
      {osittainen && (
        <KeyValueRow
          localizableLabel="Korotus sisältää mukautettuja arvosanoja"
          hideIfEmpty={
            osittainen.suoritus.korotettuKeskiarvoSisältääMukautettujaArvosanoja
          }
        >
          <FormField
            testId={'korotus-sisaltaa-mukautettuja'}
            form={form}
            view={BooleanView}
            edit={BooleanEdit}
            path={osittainen.path.prop(
              'korotettuKeskiarvoSisältääMukautettujaArvosanoja'
            )}
          />
        </KeyValueRow>
      )}
    </KeyValueTable>
  )
}

export default AhvenanmaanAmmatillinenEditor
