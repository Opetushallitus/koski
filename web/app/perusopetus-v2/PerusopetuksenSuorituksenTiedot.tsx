import React, { useCallback, useState } from 'react'
import { useKoodisto } from '../appstate/koodisto'
import { FormModel } from '../components-v2/forms/FormModel'
import { ActivePäätasonSuoritus } from '../components-v2/containers/EditorContainer'
import { PerusopetuksenOpiskeluoikeus } from '../types/fi/oph/koski/schema/PerusopetuksenOpiskeluoikeus'
import {
  KeyColumnedValuesRow,
  KeyValueRow,
  KeyValueTable
} from '../components-v2/containers/KeyValueTable'
import {
  FieldEditorProps,
  FieldViewerProps,
  FormField
} from '../components-v2/forms/FormField'
import { EmptyObject } from '../util/objects'
import { FormListField } from '../components-v2/forms/FormListField'
import {
  OrganisaatioEdit,
  OrganisaatioView
} from '../components-v2/opiskeluoikeus/OrganisaatioField'
import {
  KoodistoEdit,
  KoodistoView
} from '../components-v2/opiskeluoikeus/KoodistoField'
import {
  PerusteEdit,
  PerusteView
} from '../components-v2/opiskeluoikeus/PerusteField'
import { SuorituksenVahvistusField } from '../components-v2/opiskeluoikeus/SuorituksenVahvistus'
import { FlatButton } from '../components-v2/controls/FlatButton'
import { Removable } from '../components-v2/controls/Removable'
import {
  LaajuusEdit,
  LaajuusView
} from '../components-v2/opiskeluoikeus/LaajuusField'
import { TestIdLayer, TestIdText } from '../appstate/useTestId'
import { finnish, t } from '../i18n/i18n'
import { PerusopetuksenVuosiluokanSuorituksenLiite } from '../types/fi/oph/koski/schema/PerusopetuksenVuosiluokanSuorituksenLiite'
import {
  isPerusopetuksenVuosiluokanSuoritus,
  PerusopetuksenVuosiluokanSuoritus
} from '../types/fi/oph/koski/schema/PerusopetuksenVuosiluokanSuoritus'
import {
  isNuortenPerusopetuksenOppimääränSuoritus,
  NuortenPerusopetuksenOppimääränSuoritus
} from '../types/fi/oph/koski/schema/NuortenPerusopetuksenOppimaaranSuoritus'
import { isNuortenPerusopetuksenOppiaineenOppimääränSuoritus } from '../types/fi/oph/koski/schema/NuortenPerusopetuksenOppiaineenOppimaaranSuoritus'
import { HenkilövahvistusPaikkakunnalla } from '../types/fi/oph/koski/schema/HenkilovahvistusPaikkakunnalla'
import { FormOptic } from '../components-v2/forms/FormModel'
import { TextView, TextEdit } from '../components-v2/controls/TextField'
import { DateView, DateEdit } from '../components-v2/controls/DateField'
import {
  BooleanView,
  BooleanEdit
} from '../components-v2/opiskeluoikeus/BooleanField'
import { Checkbox } from '../components-v2/controls/Checkbox'
import {
  LocalizedTextView,
  LocalizedTextEdit
} from '../components-v2/controls/LocalizedTestField'
import { Koodistokoodiviite } from '../types/fi/oph/koski/schema/Koodistokoodiviite'
import { KoodistoSelect } from '../components-v2/opiskeluoikeus/KoodistoSelect'
import { LaajuusVuosiviikkotunneissa } from '../types/fi/oph/koski/schema/LaajuusVuosiviikkotunneissa'
import { OmanÄidinkielenOpinnotLaajuusVuosiviikkotunteina } from '../types/fi/oph/koski/schema/OmanAidinkielenOpinnotLaajuusVuosiviikkotunteina'
import { UusiTäydentäväOmanÄidinkielenOpinnotModal } from './UusiTäydentäväOmanÄidinkielenOpinnotModal'
import { KoulusivistyskieliPropertyTitle } from '../suoritus/Koulusivistyskieli'

type PerusopetuksenSuorituksenTiedotProps = {
  form: FormModel<PerusopetuksenOpiskeluoikeus>
  päätasonSuoritus: ActivePäätasonSuoritus<PerusopetuksenOpiskeluoikeus>
}

export const PerusopetuksenSuorituksenTiedot: React.FC<
  PerusopetuksenSuorituksenTiedotProps
> = ({ form, päätasonSuoritus }) => {
  const path = päätasonSuoritus.path
  const suoritus = päätasonSuoritus.suoritus
  const luokalleSiirtymisenTeksti = getLuokalleSiirtymisenTeksti(suoritus)

  return (
    <>
      {/* labelWidth pitää pisimmätkin nimet yhdellä rivillä ja tasaa arvot
          samaan sarakkeeseen vanhan käyttöliittymän tapaan. */}
      <KeyValueTable labelWidth={6}>
        <KeyColumnedValuesRow
          localizableName={
            isPerusopetuksenVuosiluokanSuoritus(suoritus)
              ? 'Luokka-aste'
              : 'Koulutus'
          }
          columnSpans={{ default: [3, '*'], phone: [24, '*'] }}
        >
          {[
            <TestIdText key="tunniste" id="koulutus">
              {t(suoritus.koulutusmoduuli.tunniste.nimi)}{' '}
              {suoritus.koulutusmoduuli.tunniste.koodiarvo}
            </TestIdText>,
            isNuortenPerusopetuksenOppimääränSuoritus(suoritus) ? (
              <FormField
                key="peruste"
                form={form}
                path={(
                  path as unknown as FormOptic<
                    PerusopetuksenOpiskeluoikeus,
                    NuortenPerusopetuksenOppimääränSuoritus
                  >
                )
                  .prop('koulutusmoduuli')
                  .prop('perusteenDiaarinumero')}
                view={PerusteView}
                edit={PerusteEdit}
                editProps={{ suorituksenTyyppi: suoritus.tyyppi.koodiarvo }}
              />
            ) : isPerusopetuksenVuosiluokanSuoritus(suoritus) ? (
              <FormField
                key="peruste"
                form={form}
                path={(
                  path as unknown as FormOptic<
                    PerusopetuksenOpiskeluoikeus,
                    PerusopetuksenVuosiluokanSuoritus
                  >
                )
                  .prop('koulutusmoduuli')
                  .prop('perusteenDiaarinumero')}
                view={PerusteView}
                edit={PerusteEdit}
                editProps={{ suorituksenTyyppi: suoritus.tyyppi.koodiarvo }}
              />
            ) : (
              <React.Fragment key="peruste" />
            )
          ]}
        </KeyColumnedValuesRow>

        {isPerusopetuksenVuosiluokanSuoritus(suoritus) && (
          <VuosiluokanLuokkaRow form={form} path={path} />
        )}

        <KeyValueRow localizableLabel="Oppilaitos / toimipiste">
          <FormField
            form={form}
            path={path.prop('toimipiste')}
            view={OrganisaatioView}
            edit={OrganisaatioEdit}
          />
        </KeyValueRow>

        {isPerusopetuksenVuosiluokanSuoritus(suoritus) && (
          <VuosiluokanKoulutustiedot
            form={form}
            path={path}
            suoritus={suoritus}
          />
        )}

        {isNuortenPerusopetuksenOppimääränSuoritus(suoritus) && (
          <OppimääränTiedot form={form} path={path} suoritus={suoritus} />
        )}

        {isNuortenPerusopetuksenOppiaineenOppimääränSuoritus(suoritus) && (
          <KeyValueRow localizableLabel="Suoritustapa">
            {t(suoritus.suoritustapa.nimi)}
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

        <MuutSuorituskieletRow form={form} path={path} suoritus={suoritus} />

        {(isNuortenPerusopetuksenOppimääränSuoritus(suoritus) ||
          isPerusopetuksenVuosiluokanSuoritus(suoritus)) && (
          <TäydentävätOmanÄidinkielenOpinnotRow
            form={form}
            path={path}
            suoritus={suoritus}
          />
        )}

        {isPerusopetuksenVuosiluokanSuoritus(suoritus) && (
          <KielikylpykieliRow form={form} path={path} suoritus={suoritus} />
        )}

        {isPerusopetuksenVuosiluokanSuoritus(suoritus) && (
          <VuosiluokanLisätiedot form={form} path={path} suoritus={suoritus} />
        )}

        {(form.editMode || suoritus.todistuksellaNäkyvätLisätiedot) && (
          <KeyValueRow localizableLabel="Todistuksella näkyvät lisätiedot">
            <FormField
              form={form}
              path={path.prop('todistuksellaNäkyvätLisätiedot')}
              optional
              view={LocalizedTextView}
              viewProps={{ style: { whiteSpace: 'pre-line' } }}
              edit={LocalizedTextEdit}
              editProps={{ large: true }}
              testId="todistuksellaNäkyvätLisätiedot"
            />
          </KeyValueRow>
        )}

        {isPerusopetuksenVuosiluokanSuoritus(suoritus) && (
          <VuosiluokanLiitetiedot form={form} path={path} suoritus={suoritus} />
        )}

        <KoulusivistyskieliRow form={form} suoritus={suoritus} />
      </KeyValueTable>

      <SuorituksenVahvistusField
        form={form}
        suoritusPath={päätasonSuoritus.path}
        organisaatio={form.state.oppilaitos}
        disableAdd={false}
        vahvistusClass={HenkilövahvistusPaikkakunnalla.className}
        statusInfo={
          luokalleSiirtymisenTeksti && (
            <TestIdText id="luokalleSiirtyminen">
              {t(luokalleSiirtymisenTeksti)}
            </TestIdText>
          )
        }
        modalBodyExtra={
          isPerusopetuksenVuosiluokanSuoritus(suoritus) && (
            <LuokalleSiirtyminenModalCheckbox
              form={form}
              path={path}
              suoritus={suoritus}
            />
          )
        }
      />
    </>
  )
}

const getLuokalleSiirtymisenTeksti = (suoritus: object): string | undefined => {
  if (!isPerusopetuksenVuosiluokanSuoritus(suoritus)) return undefined

  const luokka = suoritus.koulutusmoduuli.tunniste.koodiarvo
  if (luokka === '9') {
    return suoritus.jääLuokalle ? 'Oppilas jää luokalle' : undefined
  }

  return suoritus.jääLuokalle
    ? 'Ei siirretä seuraavalle luokalle'
    : 'Siirretään seuraavalle luokalle'
}

const LuokalleSiirtyminenModalCheckbox: React.FC<{
  form: FormModel<PerusopetuksenOpiskeluoikeus>
  path: any
  suoritus: PerusopetuksenVuosiluokanSuoritus
}> = ({ form, path, suoritus }) => {
  const vuosiluokkaPath = path as unknown as FormOptic<
    PerusopetuksenOpiskeluoikeus,
    PerusopetuksenVuosiluokanSuoritus
  >
  const isYsiluokka = suoritus.koulutusmoduuli.tunniste.koodiarvo === '9'

  const onChange = useCallback(
    (siirretäänSeuraavalleLuokalle: boolean) => {
      form.updateAt(vuosiluokkaPath.prop('jääLuokalle'), () => {
        return !siirretäänSeuraavalleLuokalle
      })
    },
    [form, vuosiluokkaPath]
  )

  return isYsiluokka ? null : (
    <Checkbox
      label="Siirretään seuraavalle luokalle"
      checked={!suoritus.jääLuokalle}
      onChange={onChange}
      testId="luokalleSiirtyminen"
    />
  )
}

type KieliKoodiviite = Koodistokoodiviite<'kieli', string>

const MuutSuorituskieletEdit: React.FC<
  FieldEditorProps<
    KieliKoodiviite,
    {
      usedValues: KieliKoodiviite[]
    }
  >
> = ({ usedValues, index = 0, ...props }) => {
  const kielet = useKoodisto('kieli')
  const currentKoodiarvo = props.value?.koodiarvo

  const koodiarvot = React.useMemo(() => {
    const blocked = new Set(
      usedValues
        .filter((_, usedIndex) => usedIndex !== index)
        .map((kieli) => kieli.koodiarvo)
    )

    return (kielet || [])
      .map((kieli) => kieli.koodiviite.koodiarvo)
      .filter(
        (koodiarvo) => !blocked.has(koodiarvo) || koodiarvo === currentKoodiarvo
      )
  }, [currentKoodiarvo, index, kielet, usedValues])

  return (
    <KoodistoEdit
      {...props}
      koodistoUri="kieli"
      koodiarvot={koodiarvot}
      testId={props.testId || 'muutSuorituskielet'}
    />
  )
}

const MuutSuorituskieletRow: React.FC<{
  form: FormModel<PerusopetuksenOpiskeluoikeus>
  path: any
  suoritus: { muutSuorituskielet?: KieliKoodiviite[] }
}> = ({ form, path, suoritus }) => {
  const muutSuorituskielet = suoritus.muutSuorituskielet || []
  if (!form.editMode && muutSuorituskielet.length === 0) return null

  const muutSuorituskieletPath = path.prop('muutSuorituskielet') as FormOptic<
    PerusopetuksenOpiskeluoikeus,
    KieliKoodiviite[] | undefined
  >

  return (
    <KeyValueRow localizableLabel="Muut suorituskielet">
      {form.editMode ? (
        <>
          <FormListField
            form={form}
            path={muutSuorituskieletPath}
            view={KoodistoView}
            edit={MuutSuorituskieletEdit}
            editProps={{ usedValues: muutSuorituskielet }}
            testId="muutSuorituskielet"
            removable
          />
          <KoodistoSelect
            koodistoUri="kieli"
            addNewText={t('Lisää')}
            filter={(tunniste) =>
              !muutSuorituskielet.some(
                (kieli) => kieli.koodiarvo === tunniste.koodiarvo
              )
            }
            onSelect={(tunniste) => {
              if (tunniste) {
                form.updateAt(muutSuorituskieletPath.valueOr([]), (prev) => [
                  ...prev,
                  tunniste as KieliKoodiviite
                ])
              }
            }}
            testId="muutSuorituskielet"
          />
        </>
      ) : (
        <TestIdText id="muutSuorituskielet">
          {muutSuorituskielet.map((k) => t(k.nimi)).join(', ')}
        </TestIdText>
      )}
    </KeyValueRow>
  )
}

const KielikylpykieliRow: React.FC<{
  form: FormModel<PerusopetuksenOpiskeluoikeus>
  path: any
  suoritus: PerusopetuksenVuosiluokanSuoritus
}> = ({ form, path, suoritus }) => {
  if (!form.editMode && !suoritus.kielikylpykieli) return null

  const kielikylpykieliPath = path.prop('kielikylpykieli') as FormOptic<
    PerusopetuksenOpiskeluoikeus,
    Koodistokoodiviite<'kieli', string> | undefined
  >

  return (
    <KeyValueRow localizableLabel="Kielikylpykieli">
      <FormField
        form={form}
        path={kielikylpykieliPath}
        optional
        view={KoodistoView}
        edit={KoodistoEdit}
        editProps={{ koodistoUri: 'kieli', zeroValueOption: true }}
        testId="kielikylpykieli"
      />
    </KeyValueRow>
  )
}

const KoulusivistyskieliRow: React.FC<{
  form: FormModel<PerusopetuksenOpiskeluoikeus>
  suoritus: object
}> = ({ form, suoritus }) => {
  const koulusivistyskieli = (
    suoritus as {
      koulusivistyskieli?: Koodistokoodiviite<'kieli', 'FI' | 'SV'>[]
    }
  ).koulusivistyskieli

  const hasValue = Boolean(koulusivistyskieli && koulusivistyskieli.length > 0)
  if (!hasValue && !form.editMode) {
    return null
  }

  return (
    <KeyValueRow labelContent={<KoulusivistyskieliPropertyTitle />}>
      <TestIdText id="koulusivistyskieli">
        {hasValue ? koulusivistyskieli!.map((k) => t(k.nimi)).join(', ') : ' '}
      </TestIdText>
    </KeyValueRow>
  )
}

const VuosiluokanLuokkaRow: React.FC<{
  form: FormModel<PerusopetuksenOpiskeluoikeus>
  path: any
}> = ({ form, path }) => {
  const vuosiluokkaPath = path as unknown as FormOptic<
    PerusopetuksenOpiskeluoikeus,
    PerusopetuksenVuosiluokanSuoritus
  >
  return (
    <>
      <KeyValueRow localizableLabel="Luokka">
        <FormField
          form={form}
          path={vuosiluokkaPath.prop('luokka')}
          view={TextView}
          edit={TextEdit}
          testId="luokka"
        />
      </KeyValueRow>
    </>
  )
}

const VuosiluokanKoulutustiedot: React.FC<{
  form: FormModel<PerusopetuksenOpiskeluoikeus>
  path: any
  suoritus: PerusopetuksenVuosiluokanSuoritus
}> = ({ form, path, suoritus }) => {
  const vuosiluokkaPath = path as unknown as FormOptic<
    PerusopetuksenOpiskeluoikeus,
    PerusopetuksenVuosiluokanSuoritus
  >
  return (
    <>
      {(form.editMode || suoritus.alkamispäivä) && (
        <KeyValueRow localizableLabel="Alkamispäivä">
          <FormField
            form={form}
            path={vuosiluokkaPath.prop('alkamispäivä')}
            optional
            view={DateView}
            edit={DateEdit}
            testId="alkamispäivä"
          />
        </KeyValueRow>
      )}
      {(form.editMode || suoritus.suoritustapa) && (
        <KeyValueRow localizableLabel="Suoritustapa">
          <FormField
            form={form}
            path={vuosiluokkaPath.prop('suoritustapa')}
            optional
            view={KoodistoView}
            edit={KoodistoEdit}
            editProps={{
              koodistoUri: 'perusopetuksensuoritustapa',
              zeroValueOption: true
            }}
            testId="suoritustapa"
          />
        </KeyValueRow>
      )}
    </>
  )
}

const VuosiluokanLisätiedot: React.FC<{
  form: FormModel<PerusopetuksenOpiskeluoikeus>
  path: any
  suoritus: PerusopetuksenVuosiluokanSuoritus
}> = ({ form, path, suoritus }) => {
  const vuosiluokkaPath = path as unknown as FormOptic<
    PerusopetuksenOpiskeluoikeus,
    PerusopetuksenVuosiluokanSuoritus
  >
  const isYsiluokka = suoritus.koulutusmoduuli.tunniste.koodiarvo === '9'

  return (
    <>
      {(form.editMode || suoritus.jääLuokalle) && (
        <KeyValueRow localizableLabel="Jää luokalle">
          <FormField
            form={form}
            path={vuosiluokkaPath.prop('jääLuokalle')}
            view={BooleanView}
            edit={BooleanEdit}
            testId="jääLuokalle"
          />
          {form.editMode && isYsiluokka && (
            <em>
              <TestIdText id="jääLuokalle.ohje">
                {t(
                  'Oppiaineiden arvioinnit syötetään 9. vuosiluokalla vain, jos oppilas jää luokalle'
                )}
              </TestIdText>
            </em>
          )}
        </KeyValueRow>
      )}
    </>
  )
}

const VuosiluokanLiitetiedot: React.FC<{
  form: FormModel<PerusopetuksenOpiskeluoikeus>
  path: any
  suoritus: PerusopetuksenVuosiluokanSuoritus
}> = ({ form, path, suoritus }) => {
  const vuosiluokkaPath = path as unknown as FormOptic<
    PerusopetuksenOpiskeluoikeus,
    PerusopetuksenVuosiluokanSuoritus
  >
  return (
    <>
      {(form.editMode ||
        (suoritus.liitetiedot && suoritus.liitetiedot.length > 0)) && (
        <KeyValueRow localizableLabel="Liitetiedot">
          <LiitetiedotEdit form={form} path={vuosiluokkaPath} />
        </KeyValueRow>
      )}
    </>
  )
}

const emptyLiitetieto: PerusopetuksenVuosiluokanSuorituksenLiite =
  PerusopetuksenVuosiluokanSuorituksenLiite({
    tunniste: Koodistokoodiviite({
      koodistoUri: 'perusopetuksentodistuksenliitetieto',
      koodiarvo: 'kayttaytyminen'
    }) as Koodistokoodiviite<
      'perusopetuksentodistuksenliitetieto',
      'kayttaytyminen' | 'tyoskentely'
    >,
    kuvaus: finnish('')
  })

const LiitetietoView: React.FC<
  FieldViewerProps<PerusopetuksenVuosiluokanSuorituksenLiite, EmptyObject>
> = ({ value }) =>
  value ? (
    <div className="Liitetieto">
      <div>
        <TestIdText id="tunniste">{t(value.tunniste.nimi)}</TestIdText>
      </div>
      <div>
        <TestIdText id="kuvaus">{t(value.kuvaus)}</TestIdText>
      </div>
    </div>
  ) : null

const LiitetietoEdit: React.FC<
  FieldEditorProps<PerusopetuksenVuosiluokanSuorituksenLiite, EmptyObject>
> = ({ value, onChange }) => {
  const current = value || emptyLiitetieto
  return (
    <div className="Liitetieto">
      <KoodistoSelect
        koodistoUri="perusopetuksentodistuksenliitetieto"
        value={current.tunniste.koodiarvo}
        onSelect={(tunniste) =>
          tunniste &&
          onChange({
            ...current,
            tunniste: tunniste as Koodistokoodiviite<
              'perusopetuksentodistuksenliitetieto',
              'kayttaytyminen' | 'tyoskentely'
            >
          })
        }
        testId="tunniste"
      />
      <LocalizedTextEdit
        value={current.kuvaus}
        onChange={(kuvaus) =>
          kuvaus !== undefined && onChange({ ...current, kuvaus })
        }
        testId="kuvaus"
      />
    </div>
  )
}

const LiitetiedotEdit: React.FC<{
  form: FormModel<PerusopetuksenOpiskeluoikeus>
  path: FormOptic<
    PerusopetuksenOpiskeluoikeus,
    PerusopetuksenVuosiluokanSuoritus
  >
}> = ({ form, path }) => {
  const liitetiedotPath = path.prop('liitetiedot') as FormOptic<
    PerusopetuksenOpiskeluoikeus,
    PerusopetuksenVuosiluokanSuorituksenLiite[] | undefined
  >
  return (
    <TestIdLayer id="liitetiedot">
      <FormListField
        form={form}
        path={liitetiedotPath}
        view={LiitetietoView}
        edit={LiitetietoEdit}
        removable
      />
      {form.editMode && (
        <FlatButton
          onClick={() =>
            form.updateAt(liitetiedotPath, (prev) => [
              ...(prev || []),
              emptyLiitetieto
            ])
          }
          testId="lisaa"
        >
          {t('Lisää')}
        </FlatButton>
      )}
    </TestIdLayer>
  )
}

const OppimääränTiedot: React.FC<{
  form: FormModel<PerusopetuksenOpiskeluoikeus>
  path: any
  suoritus: NuortenPerusopetuksenOppimääränSuoritus
}> = ({ form, path, suoritus }) => {
  const oppimääräPath = path as unknown as FormOptic<
    PerusopetuksenOpiskeluoikeus,
    NuortenPerusopetuksenOppimääränSuoritus
  >
  return (
    <>
      <KeyValueRow localizableLabel="Suoritustapa">
        <FormField
          form={form}
          path={oppimääräPath.prop('suoritustapa')}
          view={KoodistoView}
          edit={KoodistoEdit}
          editProps={{ koodistoUri: 'perusopetuksensuoritustapa' }}
          testId="suoritustapa"
        />
      </KeyValueRow>
    </>
  )
}

const TäydentävätOmanÄidinkielenOpinnotRow: React.FC<{
  form: FormModel<PerusopetuksenOpiskeluoikeus>
  path: any
  suoritus: {
    omanÄidinkielenOpinnot?: OmanÄidinkielenOpinnotLaajuusVuosiviikkotunteina
  }
}> = ({ form, path, suoritus }) => {
  const [modalVisible, setModalVisible] = useState(false)
  const opinnot = suoritus.omanÄidinkielenOpinnot
  const opinnotPath = path.prop('omanÄidinkielenOpinnot') as FormOptic<
    PerusopetuksenOpiskeluoikeus,
    OmanÄidinkielenOpinnotLaajuusVuosiviikkotunteina | undefined
  >

  if (!form.editMode && !opinnot) return null

  if (form.editMode && !opinnot) {
    return (
      <KeyValueRow localizableLabel="Oman äidinkielen opinnot">
        <FlatButton
          testId="omanÄidinkielenOpinnot.lisää"
          onClick={() => setModalVisible(true)}
        >
          {t('Lisää täydentävät oman äidinkielen opinnot')}
        </FlatButton>
        {modalVisible && (
          <UusiTäydentäväOmanÄidinkielenOpinnotModal
            onClose={() => setModalVisible(false)}
            onSubmit={(uusi) => form.updateAt(opinnotPath, () => uusi)}
          />
        )}
      </KeyValueRow>
    )
  }

  const opinnotOptionalPath = opinnotPath.optional()
  const removeOpinnot = () => form.updateAt(opinnotPath, () => undefined)

  return (
    <KeyValueRow localizableLabel="Oman äidinkielen opinnot">
      <Removable
        isRemovable={form.editMode}
        onClick={removeOpinnot}
        testId="omanÄidinkielenOpinnot"
      >
        <KeyValueTable>
          <KeyValueRow localizableLabel="Kieli" innerKeyValueTable>
            {form.editMode ? (
              <KoodistoSelect
                koodistoUri="kielivalikoima"
                onSelect={(k) =>
                  k && form.updateAt(opinnotOptionalPath.prop('kieli'), () => k)
                }
                value={opinnot?.kieli.koodiarvo}
                testId="omanÄidinkielenOpinnot.kieli"
              />
            ) : (
              <TestIdText id="omanÄidinkielenOpinnot.kieli">
                {t(opinnot?.kieli.nimi)}
              </TestIdText>
            )}
          </KeyValueRow>
          <KeyValueRow localizableLabel="Arvosana" innerKeyValueTable>
            {form.editMode ? (
              <KoodistoSelect
                koodistoUri="arviointiasteikkoyleissivistava"
                format={(k) => k.koodiarvo + ' ' + t(k.nimi)}
                onSelect={(k) =>
                  k &&
                  form.updateAt(
                    opinnotOptionalPath.prop('arvosana'),
                    () =>
                      k as OmanÄidinkielenOpinnotLaajuusVuosiviikkotunteina['arvosana']
                  )
                }
                value={opinnot?.arvosana.koodiarvo}
                testId="omanÄidinkielenOpinnot.arvosana"
              />
            ) : (
              <TestIdText id="omanÄidinkielenOpinnot.arvosana">
                {opinnot?.arvosana.koodiarvo}
              </TestIdText>
            )}
          </KeyValueRow>
          <KeyValueRow localizableLabel="Laajuus" innerKeyValueTable>
            <FormField
              form={form}
              path={opinnotOptionalPath.prop('laajuus')}
              optional
              view={LaajuusView}
              edit={LaajuusEdit}
              editProps={{
                createLaajuus: (arvo: number) =>
                  LaajuusVuosiviikkotunneissa({ arvo })
              }}
              testId="omanÄidinkielenOpinnot.laajuus"
            />
          </KeyValueRow>
          {(form.editMode || opinnot?.arviointipäivä) && (
            <KeyValueRow localizableLabel="Arviointipäivä" innerKeyValueTable>
              <FormField
                form={form}
                path={opinnotOptionalPath.prop('arviointipäivä')}
                optional
                view={DateView}
                edit={DateEdit}
                editProps={{ align: 'right' }}
                testId="omanÄidinkielenOpinnot.arviointipäivä"
              />
            </KeyValueRow>
          )}
        </KeyValueTable>
      </Removable>
    </KeyValueRow>
  )
}
