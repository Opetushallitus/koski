import React, { useContext, useMemo, useCallback } from 'react'
import { OpiskeluoikeusContext } from '../appstate/opiskeluoikeus'
import { useKoodisto } from '../appstate/koodisto'
import { usePreferences } from '../appstate/preferences'
import { finnish } from '../i18n/i18n'
import { PaikallinenKoodi } from '../types/fi/oph/koski/schema/PaikallinenKoodi'
import { LocalizedString } from '../types/fi/oph/koski/schema/LocalizedString'
import {
  OptionList,
  Select,
  SelectOption
} from '../components-v2/controls/Select'
import { TextEdit, TextView } from '../components-v2/controls/TextField'
import { PerusopetuksenOpiskeluoikeus } from '../types/fi/oph/koski/schema/PerusopetuksenOpiskeluoikeus'
import { OppiaineenTaiToiminta_AlueenSuoritus } from '../types/fi/oph/koski/schema/OppiaineenTaiToimintaAlueenSuoritus'
import { isNuortenPerusopetuksenOppiaineenSuoritus } from '../types/fi/oph/koski/schema/NuortenPerusopetuksenOppiaineenSuoritus'
import {
  NuortenPerusopetuksenPaikallinenOppiaine,
  isNuortenPerusopetuksenPaikallinenOppiaine
} from '../types/fi/oph/koski/schema/NuortenPerusopetuksenPaikallinenOppiaine'
import {
  PerusopetuksenToiminta_AlueenSuoritus,
  isPerusopetuksenToiminta_AlueenSuoritus
} from '../types/fi/oph/koski/schema/PerusopetuksenToimintaAlueenSuoritus'
import {
  PerusopetuksenVuosiluokanSuoritus,
  isPerusopetuksenVuosiluokanSuoritus
} from '../types/fi/oph/koski/schema/PerusopetuksenVuosiluokanSuoritus'
import { isNuortenPerusopetuksenOppiaineenOppimääränSuoritus } from '../types/fi/oph/koski/schema/NuortenPerusopetuksenOppiaineenOppimaaranSuoritus'
import { FootnoteDescriptions } from '../components/footnote'
import {
  OsasuoritusRowData,
  OsasuoritusTable,
  OsasuoritusTableColumn
} from '../components-v2/opiskeluoikeus/OsasuoritusTable'
import {
  OsasuoritusProperty,
  OsasuoritusPropertyValue
} from '../components-v2/opiskeluoikeus/OsasuoritusProperty'
import { t } from '../i18n/i18n'
import {
  NuortenPerusopetuksenOppimääränSuoritus,
  isNuortenPerusopetuksenOppimääränSuoritus
} from '../types/fi/oph/koski/schema/NuortenPerusopetuksenOppimaaranSuoritus'
import { FormModel, FormOptic } from '../components-v2/forms/FormModel'
import { FieldViewerProps, FormField } from '../components-v2/forms/FormField'
import { EmptyObject } from '../util/objects'
import { Column, ColumnRow } from '../components-v2/containers/Columns'
import { ActivePäätasonSuoritus } from '../components-v2/containers/EditorContainer'
import {
  ParasArvosanaViewProps,
  ParasArvosanaEdit,
  koodiarvoOnly
} from '../components-v2/opiskeluoikeus/ArvosanaField'
import { Arviointi } from '../types/fi/oph/koski/schema/Arviointi'
import { parasArviointi } from '../util/arvioinnit'
import { NuortenPerusopetuksenOppiaineenSuoritus } from '../types/fi/oph/koski/schema/NuortenPerusopetuksenOppiaineenSuoritus'
import {
  BooleanView,
  BooleanEdit
} from '../components-v2/opiskeluoikeus/BooleanField'
import {
  KoodistoView,
  KoodistoEdit
} from '../components-v2/opiskeluoikeus/KoodistoField'
import {
  LocalizedTextView,
  LocalizedTextEdit
} from '../components-v2/controls/LocalizedTestField'
import { KoodistoSelect } from '../components-v2/opiskeluoikeus/KoodistoSelect'
import { PerusopetuksenKäyttäytymisenArviointi } from '../types/fi/oph/koski/schema/PerusopetuksenKayttaytymisenArviointi'
import { MuuNuortenPerusopetuksenOppiaine } from '../types/fi/oph/koski/schema/MuuNuortenPerusopetuksenOppiaine'
import { NuortenPerusopetuksenÄidinkieliJaKirjallisuus } from '../types/fi/oph/koski/schema/NuortenPerusopetuksenAidinkieliJaKirjallisuus'
import { NuortenPerusopetuksenUskonto } from '../types/fi/oph/koski/schema/NuortenPerusopetuksenUskonto'
import { NuortenPerusopetuksenVierasTaiToinenKotimainenKieli } from '../types/fi/oph/koski/schema/NuortenPerusopetuksenVierasTaiToinenKotimainenKieli'
import { Koodistokoodiviite } from '../types/fi/oph/koski/schema/Koodistokoodiviite'
import { deleteAt } from '../util/fp/arrays'
import { appendOptional } from '../util/array'
import {
  KeyValueRow,
  KeyValueTable
} from '../components-v2/containers/KeyValueTable'
import { IconButton } from '../components-v2/controls/IconButton'
import {
  CHARCODE_ADD,
  CHARCODE_REMOVE,
  Icon
} from '../components-v2/texts/Icon'
import { FlatButton } from '../components-v2/controls/FlatButton'
import { NumberField } from '../components-v2/controls/NumberField'
import { LaajuusVuosiviikkotunneissa } from '../types/fi/oph/koski/schema/LaajuusVuosiviikkotunneissa'
import {
  SanallinenPerusopetuksenOppiaineenArviointi,
  isSanallinenPerusopetuksenOppiaineenArviointi
} from '../types/fi/oph/koski/schema/SanallinenPerusopetuksenOppiaineenArviointi'
import { TestIdLayer, TestIdText } from '../appstate/useTestId'
import { useKoodistoFiller } from '../appstate/koodisto'
import { PerusopetuksenToiminta_Alue } from '../types/fi/oph/koski/schema/PerusopetuksenToimintaAlue'
import { isToimintaAlueittainOpiskelu } from './toimintaAlueittain'
import {
  isUskonnonOppiaine,
  isVieraanKielenOppiaine,
  isÄidinkielenOppiaine
} from '../uusiopiskeluoikeus/opiskeluoikeusCreator/yleissivistavat'
import { shouldShowLaajuusColumn } from './oppiaineLaajuus'

const ParasArvosanaKoodiarvoView = <T extends Arviointi>(
  props: ParasArvosanaViewProps<T>
) => {
  const paras = props.value !== undefined && parasArviointi(props.value)
  return (
    <TestIdText id="arvosana.value">
      {paras ? paras.arvosana.koodiarvo : ''}
    </TestIdText>
  )
}

type SuoritusWithOsasuoritukset =
  | NuortenPerusopetuksenOppimääränSuoritus
  | PerusopetuksenVuosiluokanSuoritus

type PerusopetuksenOppiaineetProps = {
  form: FormModel<PerusopetuksenOpiskeluoikeus>
  päätasonSuoritus: ActivePäätasonSuoritus<PerusopetuksenOpiskeluoikeus>
}

export const PerusopetuksenOppiaineet: React.FC<
  PerusopetuksenOppiaineetProps
> = ({ form, päätasonSuoritus }) => {
  const opiskeluoikeus = form.state
  const suoritus = päätasonSuoritus.suoritus
  const suoritusIndex = päätasonSuoritus.index
  const isToimintaAlueittain = isToimintaAlueittainOpiskelu(opiskeluoikeus)

  if (isNuortenPerusopetuksenOppiaineenOppimääränSuoritus(suoritus)) {
    return null
  }

  if (
    isPerusopetuksenVuosiluokanSuoritus(suoritus) &&
    suoritus.koulutusmoduuli.tunniste.koodiarvo === '9' &&
    !suoritus.jääLuokalle
  ) {
    return null
  }

  // After early returns, suoritus is NuortenPerusopetuksenOppimääränSuoritus | PerusopetuksenVuosiluokanSuoritus
  const suoritusPath = päätasonSuoritus.path as unknown as FormOptic<
    PerusopetuksenOpiskeluoikeus,
    SuoritusWithOsasuoritukset
  >
  const osasuoritukset = suoritus.osasuoritukset || []

  // Oppimäärän arvosanoja ei näytetä ennen vahvistusta ellei olla
  // muokkaustilassa. Vuosiluokan suoritusten arvosanat näytetään aina.
  const showArvosana =
    form.editMode ||
    !isNuortenPerusopetuksenOppimääränSuoritus(suoritus) ||
    !!suoritus.vahvistus

  // Ryhmittely yhteisiin (pakolliset) ja valinnaisiin oppiaineisiin. Näytetään
  // myös tyhjälle vuosiluokalle muokkaustilassa, jotta molemmat lisäyspudotukset
  // (pakollinen + valinnainen) ovat käytettävissä – ei vain valinnaisen.
  const hasGrouping =
    !isToimintaAlueittain &&
    (form.editMode ||
      osasuoritukset.some(
        (s) =>
          isNuortenPerusopetuksenOppiaineenSuoritus(s) &&
          'pakollinen' in s.koulutusmoduuli
      ))

  const footnotes = computeFootnotes(osasuoritukset)

  const title = isToimintaAlueittain
    ? 'Toiminta-alueiden arvosanat'
    : 'Oppiaineiden arvosanat'

  return (
    <div className="oppiaineet">
      <h5>{t(title)}</h5>
      <p
        className="perusopetuksen-arvosteluasteikko"
        data-testid="perusopetuksen-arvosteluasteikko"
      >
        {t('Arvostelu 4-10, S (suoritettu) tai H (hylätty)')}
      </p>
      {hasGrouping ? (
        <GroupedOppiaineet
          osasuoritukset={osasuoritukset}
          suoritusIndex={suoritusIndex}
          isToimintaAlueittain={isToimintaAlueittain}
          form={form}
          suoritusPath={suoritusPath}
          showArvosana={showArvosana}
          käyttäytymisenArvio={
            isPerusopetuksenVuosiluokanSuoritus(suoritus) &&
            (form.editMode || suoritus.käyttäytymisenArvio) ? (
              <KäyttäytymisenArvioField
                form={form}
                suoritusPath={
                  suoritusPath as unknown as FormOptic<
                    PerusopetuksenOpiskeluoikeus,
                    PerusopetuksenVuosiluokanSuoritus
                  >
                }
                suoritus={suoritus}
              />
            ) : undefined
          }
        />
      ) : (
        <>
          <Oppiainetaulukko
            osasuoritukset={osasuoritukset}
            suoritusIndex={suoritusIndex}
            isToimintaAlueittain={isToimintaAlueittain}
            columnHeader={isToimintaAlueittain ? 'Toiminta-alue' : 'Oppiaine'}
            form={form}
            suoritusPath={suoritusPath}
            showArvosana={showArvosana}
          />
          {isPerusopetuksenVuosiluokanSuoritus(suoritus) &&
            (form.editMode || suoritus.käyttäytymisenArvio) && (
              <KäyttäytymisenArvioField
                form={form}
                suoritusPath={
                  suoritusPath as unknown as FormOptic<
                    PerusopetuksenOpiskeluoikeus,
                    PerusopetuksenVuosiluokanSuoritus
                  >
                }
                suoritus={suoritus}
              />
            )}
        </>
      )}
      {!form.editMode && footnotes.length > 0 && (
        <FootnoteDescriptions data={footnotes} />
      )}
    </div>
  )
}

type OppiainetaulukkoProps = {
  osasuoritukset: OppiaineenTaiToiminta_AlueenSuoritus[]
  suoritusIndex: number
  isToimintaAlueittain: boolean
  columnHeader: string
  title?: string
  pakollinen?: boolean
  form: FormModel<PerusopetuksenOpiskeluoikeus>
  suoritusPath: FormOptic<
    PerusopetuksenOpiskeluoikeus,
    SuoritusWithOsasuoritukset
  >
  showArvosana: boolean
}

const Oppiainetaulukko: React.FC<OppiainetaulukkoProps> = ({
  osasuoritukset,
  suoritusIndex,
  isToimintaAlueittain,
  columnHeader,
  title,
  pakollinen,
  form,
  suoritusPath,
  showArvosana
}) => {
  const suoritus = form.state.suoritukset[suoritusIndex] as
    | SuoritusWithOsasuoritukset
    | undefined
  const allOsasuoritukset = suoritus?.osasuoritukset || []
  const showLaajuus = shouldShowLaajuusColumn({
    editMode: form.editMode,
    isToimintaAlueittain,
    pakollinen,
    suoritus,
    osasuoritukset
  })
  const rows = osasuoritukset.map((s, i) => {
    const dataIndex = allOsasuoritukset.indexOf(s)
    return oppiaineToRow(
      s,
      suoritusIndex,
      dataIndex >= 0 ? dataIndex : i,
      columnHeader,
      form,
      suoritusPath,
      showLaajuus,
      showArvosana
    )
  })
  const columns: Array<OsasuoritusTableColumn<string>> = [
    { key: columnHeader },
    ...(showArvosana ? [{ key: 'Arvosana', align: 'right' as const }] : []),
    ...(showLaajuus ? [{ key: 'Laajuus' }] : [])
  ]

  return (
    <>
      {title && <h5>{t(title)}</h5>}
      <OsasuoritusTable
        editMode={form.editMode}
        columns={columns}
        rows={rows}
        expandedContentIndent={3}
        onRemove={(rowIndex) => {
          const dataIndex = allOsasuoritukset.indexOf(osasuoritukset[rowIndex])
          if (dataIndex >= 0) {
            form.updateAt(suoritusPath, (pts) => ({
              ...pts,
              osasuoritukset: deleteAt(pts.osasuoritukset || [], dataIndex)
            }))
          }
        }}
        addNewOsasuoritusView={
          isToimintaAlueittain
            ? UusiPerusopetuksenToimintaAlue
            : UusiPerusopetuksenOppiaine
        }
        addNewOsasuoritusViewProps={{
          form,
          suoritusPath,
          pakollinen,
          existingOsasuoritukset: allOsasuoritukset
        }}
      />
    </>
  )
}

type GroupedOppiaineetProps = {
  osasuoritukset: OppiaineenTaiToiminta_AlueenSuoritus[]
  suoritusIndex: number
  isToimintaAlueittain: boolean
  form: FormModel<PerusopetuksenOpiskeluoikeus>
  suoritusPath: FormOptic<
    PerusopetuksenOpiskeluoikeus,
    SuoritusWithOsasuoritukset
  >
  käyttäytymisenArvio?: React.ReactNode
  showArvosana: boolean
}

const GroupedOppiaineet: React.FC<GroupedOppiaineetProps> = ({
  osasuoritukset,
  suoritusIndex,
  isToimintaAlueittain,
  form,
  suoritusPath,
  käyttäytymisenArvio,
  showArvosana
}) => {
  const pakolliset = osasuoritukset.filter(
    (s) =>
      isNuortenPerusopetuksenOppiaineenSuoritus(s) &&
      s.koulutusmoduuli.pakollinen
  )
  const valinnaiset = osasuoritukset.filter(
    (s) =>
      isNuortenPerusopetuksenOppiaineenSuoritus(s) &&
      !s.koulutusmoduuli.pakollinen
  )

  return (
    <ColumnRow valign="top">
      {(pakolliset.length > 0 || form.editMode) && (
        <Column
          span={{ default: 12, large: 24 }}
          data-testid="oppiaineet-pakolliset"
        >
          <Oppiainetaulukko
            osasuoritukset={pakolliset}
            suoritusIndex={suoritusIndex}
            isToimintaAlueittain={isToimintaAlueittain}
            columnHeader="Oppiaine"
            title="Yhteiset oppiaineet"
            pakollinen={true}
            form={form}
            suoritusPath={suoritusPath}
            showArvosana={showArvosana}
          />
        </Column>
      )}
      {(valinnaiset.length > 0 || form.editMode || käyttäytymisenArvio) && (
        <Column
          span={{ default: 12, large: 24 }}
          data-testid="oppiaineet-valinnaiset"
        >
          {(valinnaiset.length > 0 || form.editMode) && (
            <Oppiainetaulukko
              osasuoritukset={valinnaiset}
              suoritusIndex={suoritusIndex}
              isToimintaAlueittain={isToimintaAlueittain}
              columnHeader="Oppiaine"
              title="Valinnaiset aineet"
              pakollinen={false}
              form={form}
              suoritusPath={suoritusPath}
              showArvosana={showArvosana}
            />
          )}
          {käyttäytymisenArvio}
        </Column>
      )}
    </ColumnRow>
  )
}

const oppiaineToRow = <T extends string>(
  suoritus: OppiaineenTaiToiminta_AlueenSuoritus,
  suoritusIndex: number,
  osasuoritusIndex: number,
  columnHeader: T,
  form: FormModel<PerusopetuksenOpiskeluoikeus>,
  suoritusPath: FormOptic<
    PerusopetuksenOpiskeluoikeus,
    SuoritusWithOsasuoritukset
  >,
  showLaajuus?: boolean,
  showArvosana: boolean = true
): OsasuoritusRowData<T | 'Arvosana' | 'Laajuus' | ' '> => {
  const osasuoritusPath = suoritusPath
    .prop('osasuoritukset')
    .optional()
    .at(osasuoritusIndex)
  const expandable = hasExpandableProperties(suoritus, form.editMode)

  const columns: Partial<
    Record<T | 'Arvosana' | 'Laajuus' | ' ', React.ReactNode>
  > = {}
  const isPaikallinen =
    isNuortenPerusopetuksenOppiaineenSuoritus(suoritus) &&
    isNuortenPerusopetuksenPaikallinenOppiaine(suoritus.koulutusmoduuli)
  const nimi = oppiaineNimi(suoritus)

  const paras = parasArviointi(suoritus.arviointi)
  const hasSanallinenArviointi =
    suoritus.arviointi &&
    suoritus.arviointi.length > 0 &&
    paras &&
    isSanallinenPerusopetuksenOppiaineenArviointi(paras)
  const sanallinenKuvaus = hasSanallinenArviointi ? paras.kuvaus : undefined
  const arviointiPath = hasSanallinenArviointi
    ? (osasuoritusPath
        .prop('arviointi')
        .optional()
        .at(suoritus.arviointi!.length - 1) as unknown as FormOptic<
        PerusopetuksenOpiskeluoikeus,
        SanallinenPerusopetuksenOppiaineenArviointi
      >)
    : undefined

  const showSanallinen =
    showArvosana && arviointiPath && (form.editMode || sanallinenKuvaus)

  const paikallinenTunnistePath = isPaikallinen
    ? (
        osasuoritusPath.prop('koulutusmoduuli') as unknown as FormOptic<
          PerusopetuksenOpiskeluoikeus,
          NuortenPerusopetuksenPaikallinenOppiaine
        >
      ).prop('tunniste')
    : undefined
  const hasKieli =
    isNuortenPerusopetuksenOppiaineenSuoritus(suoritus) &&
    'kieli' in suoritus.koulutusmoduuli &&
    !!suoritus.koulutusmoduuli.kieli
  const kieliPath = hasKieli
    ? ((
        osasuoritusPath.prop('koulutusmoduuli') as unknown as FormOptic<
          PerusopetuksenOpiskeluoikeus,
          { kieli: Koodistokoodiviite<string, string> }
        >
      ).prop('kieli') as FormOptic<
        PerusopetuksenOpiskeluoikeus,
        Koodistokoodiviite<string, string>
      >)
    : undefined
  const kieliKoodistoUri = hasKieli
    ? (
        suoritus as NuortenPerusopetuksenOppiaineenSuoritus & {
          koulutusmoduuli: { kieli: Koodistokoodiviite<string, string> }
        }
      ).koulutusmoduuli.kieli.koodistoUri
    : undefined
  columns[columnHeader] =
    isPaikallinen && form.editMode && paikallinenTunnistePath ? (
      <>
        <FormField
          form={form}
          path={paikallinenTunnistePath.prop('koodiarvo')}
          view={TextView}
          edit={TextEdit}
          editProps={{ placeholder: t('Koodi') }}
          testId="paikallinenKoodi"
        />
        <FormField
          form={form}
          path={paikallinenTunnistePath.prop('nimi')}
          view={LocalizedTextView}
          edit={LocalizedTextEdit}
          editProps={{ placeholder: t('Oppiaineen nimi') }}
          testId="paikallinenNimi"
        />
      </>
    ) : hasKieli && kieliPath && kieliKoodistoUri ? (
      <>
        <TestIdText id="nimi">{nimi + ', '}</TestIdText>
        <FormField
          form={form}
          path={kieliPath}
          view={KieliNimiLowerView}
          edit={KoodistoEdit}
          editProps={{ koodistoUri: kieliKoodistoUri }}
          testId="kieli"
        />
      </>
    ) : isPaikallinen ? (
      <span style={{ fontStyle: 'italic' }}>
        <TestIdText id="nimi">{nimi}</TestIdText>
      </span>
    ) : (
      <TestIdText id="nimi">{nimi}</TestIdText>
    )
  // Alaviitemerkit (* / **) näytetään arvosanan perässä (ks. CSS: sininen).
  // Muokkaustilassa niitä ei näytetä, koska ne sotkevat syöttökenttien
  // asettelua (ominaisuudet muokataan rivin laajennusosiosta).
  const rowFootnotes = footnotesForSuoritus(suoritus)
  const footnoteEl =
    !form.editMode && rowFootnotes.length > 0 ? (
      <TestIdText id="footnote">
        {rowFootnotes.map((note) => (
          <sup key={note.hint} className="footnote-hint" title={t(note.title)}>
            {` ${note.hint}`}
          </sup>
        ))}
      </TestIdText>
    ) : null
  if (showArvosana) {
    columns['Arvosana' as T | 'Arvosana' | ' '] = (
      <>
        <FormField
          form={form}
          path={osasuoritusPath.prop('arviointi')}
          optional
          view={ParasArvosanaKoodiarvoView}
          edit={ParasArvosanaEdit}
          editProps={{
            suoritusClassName: suoritus.$class,
            format: koodiarvoOnly
          }}
        />
        {footnoteEl}
      </>
    )
  }
  if (showLaajuus) {
    const laajuus =
      'koulutusmoduuli' in suoritus &&
      'laajuus' in suoritus.koulutusmoduuli &&
      suoritus.koulutusmoduuli.laajuus
        ? (suoritus.koulutusmoduuli.laajuus as LaajuusVuosiviikkotunneissa)
        : undefined
    columns['Laajuus' as T | 'Arvosana' | 'Laajuus' | ' '] = form.editMode ? (
      <NumberField
        value={laajuus?.arvo}
        onChange={(arvo) => {
          form.updateAt(osasuoritusPath.prop('koulutusmoduuli'), (km) => ({
            ...km,
            laajuus:
              arvo !== undefined
                ? LaajuusVuosiviikkotunneissa({ arvo })
                : undefined
          }))
        }}
        testId="laajuus"
      />
    ) : laajuus ? (
      <TestIdText id="laajuus.value">
        <span className="PerusopetuksenOppiaineet__laajuus">
          {laajuus.arvo}{' '}
          <span
            className={`PerusopetuksenOppiaineet__laajuusYksikko${
              laajuus.yksikkö.koodiarvo === '3'
                ? ' PerusopetuksenOppiaineet__laajuusYksikko--vuosiviikkotuntia'
                : ''
            }`}
          >
            {t(laajuus.yksikkö.nimi)}
          </span>
        </span>
      </TestIdText>
    ) : null
  }
  // Jos arvosanasaraketta ei näytetä, liitetään alaviite oppiaineen nimeen.
  if (footnoteEl && !showArvosana) {
    columns[columnHeader] = (
      <>
        {columns[columnHeader]}
        {footnoteEl}
      </>
    )
  }

  return {
    suoritusIndex,
    osasuoritusIndex,
    osasuoritusPath,
    expandable,
    columns,
    alwaysVisibleContent:
      showSanallinen && arviointiPath ? (
        <OsasuoritusProperty label="Sanallinen arviointi">
          <OsasuoritusPropertyValue>
            <FormField
              form={form}
              path={arviointiPath.prop('kuvaus')}
              optional
              view={LocalizedTextView}
              edit={LocalizedTextEdit}
              testId="sanallinenArviointi"
            />
          </OsasuoritusPropertyValue>
        </OsasuoritusProperty>
      ) : undefined,
    content: expandable ? (
      <OppiaineExpandedContent
        suoritus={suoritus}
        form={form}
        osasuoritusPath={osasuoritusPath}
      />
    ) : undefined
  }
}

type UusiPerusopetuksenOppiaineProps = {
  form: FormModel<PerusopetuksenOpiskeluoikeus>
  suoritusPath: FormOptic<
    PerusopetuksenOpiskeluoikeus,
    SuoritusWithOsasuoritukset
  >
  pakollinen?: boolean
  existingOsasuoritukset: OppiaineenTaiToiminta_AlueenSuoritus[]
}

const UUSI_PAIKALLINEN_OPPIAINE_KEY = 'uusi-paikallinen-oppiaine'
const PAIKALLINEN_OPPIAINE_PREF_TYPE =
  'nuortenperusopetuksenpaikallinenoppiaine'

const MUUT_VALTAKUNNALLISET_NUORTEN_PERUSOPETUKSEN_OPPIAINEET = [
  'HI',
  'MU',
  'BI',
  'PS',
  'ET',
  'KO',
  'FI',
  'KE',
  'YH',
  'TE',
  'KS',
  'FY',
  'GE',
  'LI',
  'KU',
  'MA',
  'YL',
  'OP'
]

const isSallittuValtakunnallinenNuortenPerusopetuksenOppiaine = (
  koodiarvo: string
) =>
  isUskonnonOppiaine(koodiarvo) ||
  isVieraanKielenOppiaine(koodiarvo) ||
  isÄidinkielenOppiaine(koodiarvo) ||
  MUUT_VALTAKUNNALLISET_NUORTEN_PERUSOPETUKSEN_OPPIAINEET.includes(koodiarvo)

const oletuskieliKieliaineelle = (
  tunniste: Koodistokoodiviite<'koskioppiaineetyleissivistava'>
) =>
  Koodistokoodiviite({
    koodiarvo: tunniste.koodiarvo === 'B1' ? 'SV' : 'EN',
    koodistoUri: 'kielivalikoima'
  })

const uusiValtakunnallinenKoulutusmoduuli = (
  tunniste: Koodistokoodiviite<'koskioppiaineetyleissivistava'>,
  pakollinen: boolean
) => {
  const koodiarvo = tunniste.koodiarvo

  if (isUskonnonOppiaine(koodiarvo)) {
    return NuortenPerusopetuksenUskonto({ pakollinen })
  }
  if (isVieraanKielenOppiaine(koodiarvo)) {
    return NuortenPerusopetuksenVierasTaiToinenKotimainenKieli({
      pakollinen,
      tunniste: tunniste as Koodistokoodiviite<
        'koskioppiaineetyleissivistava',
        'A1' | 'A2' | 'B1' | 'B2' | 'B3' | 'AOM'
      >,
      kieli: oletuskieliKieliaineelle(tunniste)
    })
  }
  if (isÄidinkielenOppiaine(koodiarvo)) {
    return NuortenPerusopetuksenÄidinkieliJaKirjallisuus({
      pakollinen,
      kieli: Koodistokoodiviite({
        koodiarvo: 'AI1',
        koodistoUri: 'oppiaineaidinkielijakirjallisuus'
      })
    })
  }
  return MuuNuortenPerusopetuksenOppiaine({
    pakollinen,
    tunniste: tunniste as Koodistokoodiviite<
      'koskioppiaineetyleissivistava',
      any
    >
  })
}

const UusiPerusopetuksenOppiaine: React.FC<UusiPerusopetuksenOppiaineProps> = ({
  form,
  suoritusPath,
  pakollinen,
  existingOsasuoritukset
}) => {
  const fillKoodistot = useKoodistoFiller()
  const koodisto = useKoodisto('koskioppiaineetyleissivistava')
  const { organisaatio } = useContext(OpiskeluoikeusContext)
  const organisaatioOid = organisaatio?.oid
  const {
    preferences: paikalliset,
    store: storePaikallinen,
    remove: removePaikallinen
  } = usePreferences<NuortenPerusopetuksenPaikallinenOppiaine>(
    organisaatioOid,
    PAIKALLINEN_OPPIAINE_PREF_TYPE
  )

  const osasuorituksetPath = suoritusPath.prop('osasuoritukset').valueOr([]) as
    | FormOptic<
        PerusopetuksenOpiskeluoikeus,
        OppiaineenTaiToiminta_AlueenSuoritus[]
      >
    | any

  const existingKoodiarvot = useMemo(
    () =>
      existingOsasuoritukset
        .filter(isNuortenPerusopetuksenOppiaineenSuoritus)
        .filter(
          (s) =>
            pakollinen === undefined ||
            ('pakollinen' in s.koulutusmoduuli &&
              s.koulutusmoduuli.pakollinen === pakollinen)
        )
        .filter(
          (s) => !isNuortenPerusopetuksenPaikallinenOppiaine(s.koulutusmoduuli)
        )
        .map((s) => s.koulutusmoduuli.tunniste.koodiarvo),
    [existingOsasuoritukset, pakollinen]
  )

  const placeholder =
    pakollinen === undefined
      ? 'Lisää oppiaine'
      : pakollinen
        ? 'Lisää pakollinen oppiaine'
        : 'Lisää valinnainen oppiaine'

  const options: OptionList<
    Koodistokoodiviite<'koskioppiaineetyleissivistava'> | PaikallinenKoodi
  > = useMemo(() => {
    // Uskonto (KT) ja Elämänkatsomustieto (ET) näkyvät koodistossa samalla
    // nimellä "Uskonto/Elämänkatsomustieto". Lisätään koodiarvo näytettävään
    // tekstiin, jotta ne voidaan erottaa toisistaan pudotusvalikossa (vrt.
    // TOR-1208).
    const USKONTO_ET_KT_KEYS = ['KT', 'ET']
    const koodistoOpts = (koodisto || [])
      .filter((k) =>
        isSallittuValtakunnallinenNuortenPerusopetuksenOppiaine(
          k.koodiviite.koodiarvo
        )
      )
      .filter((k) =>
        pakollinen ? !existingKoodiarvot.includes(k.koodiviite.koodiarvo) : true
      )
      .map((k) => {
        const nimi = t(k.koodiviite.nimi)
        const label = USKONTO_ET_KT_KEYS.includes(k.koodiviite.koodiarvo)
          ? `${nimi} ${k.koodiviite.koodiarvo}`
          : nimi
        return {
          key: `valtakunnallinen-${k.koodiviite.koodiarvo}`,
          label,
          value:
            k.koodiviite as Koodistokoodiviite<'koskioppiaineetyleissivistava'>
        }
      })

    const paikallisetOpts = (paikalliset || []).map((p) => ({
      key: `paikallinen-${p.tunniste.koodiarvo}`,
      label: t(p.tunniste.nimi),
      value: p.tunniste as PaikallinenKoodi,
      removable: true
    }))

    const uusiPaikallinenLabel = t('Lisää') + '...'
    const uusiPaikallinen = {
      key: UUSI_PAIKALLINEN_OPPIAINE_KEY,
      label: uusiPaikallinenLabel,
      display: (
        <>
          <Icon charCode={CHARCODE_ADD} />
          {uusiPaikallinenLabel}
        </>
      ),
      isAddNew: true,
      value: undefined as any
    }

    return [...koodistoOpts, ...paikallisetOpts, uusiPaikallinen]
  }, [koodisto, paikalliset, existingKoodiarvot, pakollinen])

  const onChange = useCallback(
    async (
      option?: SelectOption<
        Koodistokoodiviite<'koskioppiaineetyleissivistava'> | PaikallinenKoodi
      >
    ) => {
      if (!option) return
      if (option.key === UUSI_PAIKALLINEN_OPPIAINE_KEY) {
        const emptyPaikallinen = NuortenPerusopetuksenPaikallinenOppiaine({
          pakollinen: pakollinen ?? false,
          tunniste: PaikallinenKoodi({
            koodiarvo: '',
            nimi: finnish('')
          }),
          kuvaus: finnish('')
        })
        const newSuoritus = NuortenPerusopetuksenOppiaineenSuoritus({
          painotettuOpetus: false,
          koulutusmoduuli: emptyPaikallinen
        })
        form.updateAt(osasuorituksetPath, appendOptional(newSuoritus))
      } else if (option.value && 'koodiarvo' in option.value) {
        const isPaikallinenOption = option.key.startsWith('paikallinen-')
        if (isPaikallinenOption) {
          const stored = (paikalliset || []).find(
            (p) =>
              p.tunniste.koodiarvo ===
              (option.value as PaikallinenKoodi).koodiarvo
          )
          if (stored) {
            const newSuoritus = NuortenPerusopetuksenOppiaineenSuoritus({
              painotettuOpetus: false,
              koulutusmoduuli: {
                ...stored,
                pakollinen: pakollinen ?? stored.pakollinen
              }
            })
            form.updateAt(osasuorituksetPath, appendOptional(newSuoritus))
          }
        } else {
          const newSuoritus = await fillKoodistot(
            NuortenPerusopetuksenOppiaineenSuoritus({
              painotettuOpetus: false,
              koulutusmoduuli: uusiValtakunnallinenKoulutusmoduuli(
                option.value as Koodistokoodiviite<'koskioppiaineetyleissivistava'>,
                pakollinen ?? false
              )
            })
          )
          form.updateAt(osasuorituksetPath, appendOptional(newSuoritus))
        }
      }
    },
    [fillKoodistot, form, osasuorituksetPath, paikalliset, pakollinen]
  )

  const onRemove = useCallback(
    (
      option: SelectOption<
        Koodistokoodiviite<'koskioppiaineetyleissivistava'> | PaikallinenKoodi
      >
    ) => {
      if (
        option.key.startsWith('paikallinen-') &&
        option.value &&
        'koodiarvo' in option.value
      ) {
        removePaikallinen((option.value as PaikallinenKoodi).koodiarvo)
      }
    },
    [removePaikallinen]
  )

  if (!form.editMode) return null

  return (
    <Select
      placeholder={t(placeholder)}
      options={options}
      hideEmpty
      onChange={onChange}
      onRemove={onRemove}
      testId="uusi-oppiaine"
    />
  )
}

const UusiPerusopetuksenToimintaAlue: React.FC<
  UusiPerusopetuksenOppiaineProps
> = ({ form, suoritusPath, existingOsasuoritukset }) => {
  const fillKoodistot = useKoodistoFiller()
  if (!form.editMode) return null
  const osasuorituksetPath = suoritusPath.prop('osasuoritukset').valueOr([]) as
    | FormOptic<
        PerusopetuksenOpiskeluoikeus,
        OppiaineenTaiToiminta_AlueenSuoritus[]
      >
    | any

  const existingKoodiarvot = existingOsasuoritukset
    .filter(isPerusopetuksenToiminta_AlueenSuoritus)
    .map((s) => s.koulutusmoduuli.tunniste.koodiarvo)

  return (
    <KoodistoSelect
      koodistoUri="perusopetuksentoimintaalue"
      addNewText={t('Lisää toiminta-alue')}
      filter={(tunniste) => !existingKoodiarvot.includes(tunniste.koodiarvo)}
      onSelect={async (tunniste) => {
        if (!tunniste) return
        const newSuoritus = await fillKoodistot(
          PerusopetuksenToiminta_AlueenSuoritus({
            koulutusmoduuli: PerusopetuksenToiminta_Alue({ tunniste })
          })
        )
        form.updateAt(osasuorituksetPath, appendOptional(newSuoritus))
      }}
      testId="uusi-oppiaine"
    />
  )
}

const hasExpandableProperties = (
  suoritus: OppiaineenTaiToiminta_AlueenSuoritus,
  editMode: boolean
): boolean => {
  if (editMode && isNuortenPerusopetuksenOppiaineenSuoritus(suoritus)) {
    return true
  }
  if (
    isNuortenPerusopetuksenOppiaineenSuoritus(suoritus) &&
    isNuortenPerusopetuksenPaikallinenOppiaine(suoritus.koulutusmoduuli) &&
    t(suoritus.koulutusmoduuli.kuvaus)
  ) {
    return true
  }
  if (
    isNuortenPerusopetuksenOppiaineenSuoritus(suoritus) &&
    suoritus.suoritustapa
  ) {
    return true
  }
  if (
    isNuortenPerusopetuksenOppiaineenSuoritus(suoritus) &&
    suoritus.luokkaAste
  ) {
    return true
  }
  if (suoritus.suorituskieli) {
    return true
  }
  return false
}

type OppiaineExpandedContentProps = {
  suoritus: OppiaineenTaiToiminta_AlueenSuoritus
  form: FormModel<PerusopetuksenOpiskeluoikeus>
  osasuoritusPath: FormOptic<
    PerusopetuksenOpiskeluoikeus,
    OppiaineenTaiToiminta_AlueenSuoritus
  >
}

const OppiaineExpandedContent: React.FC<OppiaineExpandedContentProps> = ({
  suoritus,
  form,
  osasuoritusPath
}) => {
  const isOppiaine = isNuortenPerusopetuksenOppiaineenSuoritus(suoritus)
  const isPaikallinen =
    isOppiaine &&
    isNuortenPerusopetuksenPaikallinenOppiaine(suoritus.koulutusmoduuli)
  const oppiainePath = osasuoritusPath as unknown as FormOptic<
    PerusopetuksenOpiskeluoikeus,
    NuortenPerusopetuksenOppiaineenSuoritus
  >

  return (
    <>
      {isPaikallinen && (form.editMode || suoritus.koulutusmoduuli.kuvaus) && (
        <OsasuoritusProperty label="Kuvaus">
          <OsasuoritusPropertyValue>
            <FormField
              form={form}
              path={(
                oppiainePath.prop('koulutusmoduuli') as unknown as FormOptic<
                  PerusopetuksenOpiskeluoikeus,
                  NuortenPerusopetuksenPaikallinenOppiaine
                >
              ).prop('kuvaus')}
              view={LocalizedTextView}
              viewProps={{ style: { whiteSpace: 'pre-line' } }}
              edit={LocalizedTextEdit}
              editProps={{ large: true }}
              testId="paikallinenKuvaus"
            />
          </OsasuoritusPropertyValue>
        </OsasuoritusProperty>
      )}
      {isOppiaine && (form.editMode || suoritus.yksilöllistettyOppimäärä) && (
        <OsasuoritusProperty label="Yksilöllistetty oppimäärä">
          <OsasuoritusPropertyValue>
            <FormField
              form={form}
              path={oppiainePath.prop('yksilöllistettyOppimäärä')}
              view={BooleanView}
              edit={BooleanEdit}
              testId="yksilöllistettyOppimäärä"
            />
          </OsasuoritusPropertyValue>
        </OsasuoritusProperty>
      )}
      {isOppiaine && (form.editMode || suoritus.rajattuOppimäärä) && (
        <OsasuoritusProperty label="Rajattu oppimäärä">
          <OsasuoritusPropertyValue>
            <FormField
              form={form}
              path={oppiainePath.prop('rajattuOppimäärä')}
              view={BooleanView}
              edit={BooleanEdit}
              testId="rajattuOppimäärä"
            />
          </OsasuoritusPropertyValue>
        </OsasuoritusProperty>
      )}
      {isOppiaine && (form.editMode || suoritus.painotettuOpetus) && (
        <OsasuoritusProperty label="Painotettu opetus">
          <OsasuoritusPropertyValue>
            <FormField
              form={form}
              path={oppiainePath.prop('painotettuOpetus')}
              view={BooleanView}
              edit={BooleanEdit}
              testId="painotettuOpetus"
            />
          </OsasuoritusPropertyValue>
        </OsasuoritusProperty>
      )}
      {(form.editMode || suoritus.suorituskieli) && (
        <OsasuoritusProperty label="Suorituskieli">
          <OsasuoritusPropertyValue>
            <FormField
              form={form}
              path={osasuoritusPath.prop('suorituskieli')}
              optional
              view={KoodistoView}
              edit={KoodistoEdit}
              editProps={{ koodistoUri: 'kieli' }}
            />
          </OsasuoritusPropertyValue>
        </OsasuoritusProperty>
      )}
      {isOppiaine && (form.editMode || suoritus.suoritustapa) && (
        <OsasuoritusProperty label="Suoritustapa">
          <OsasuoritusPropertyValue>
            <FormField
              form={form}
              path={oppiainePath.prop('suoritustapa')}
              optional
              view={KoodistoView}
              edit={KoodistoEdit}
              editProps={{ koodistoUri: 'perusopetuksensuoritustapa' }}
            />
          </OsasuoritusPropertyValue>
        </OsasuoritusProperty>
      )}
      {isOppiaine && (form.editMode || suoritus.luokkaAste) && (
        <OsasuoritusProperty label="Luokka-aste">
          <OsasuoritusPropertyValue>
            <FormField
              form={form}
              path={oppiainePath.prop('luokkaAste')}
              optional
              view={KoodistoView}
              edit={KoodistoEdit}
              editProps={{
                koodistoUri: 'perusopetuksenluokkaaste',
                zeroValueOption: true
              }}
              testId="luokkaAste"
            />
          </OsasuoritusPropertyValue>
        </OsasuoritusProperty>
      )}
    </>
  )
}

const KäyttäytymisenArvioField: React.FC<{
  form: FormModel<PerusopetuksenOpiskeluoikeus>
  suoritusPath: FormOptic<
    PerusopetuksenOpiskeluoikeus,
    PerusopetuksenVuosiluokanSuoritus
  >
  suoritus: PerusopetuksenVuosiluokanSuoritus
}> = ({ form, suoritusPath, suoritus }) => {
  const käyttäytymisenArvioPath = suoritusPath.prop('käyttäytymisenArvio')
  const hasArvio = !!suoritus.käyttäytymisenArvio

  if (!form.editMode && !hasArvio) return null

  return (
    <TestIdLayer id="kayttaytyminen">
      <div className="kayttaytyminen">
        <h5>
          {t('Käyttäytymisen arviointi')}
          {form.editMode && hasArvio && (
            <IconButton
              charCode={CHARCODE_REMOVE}
              label={t('Poista')}
              size="input"
              onClick={() =>
                form.updateAt(käyttäytymisenArvioPath, () => undefined)
              }
              testId="kayttaytyminen.remove"
            />
          )}
        </h5>
        {form.editMode && !hasArvio ? (
          <FlatButton
            onClick={() =>
              form.updateAt(käyttäytymisenArvioPath, () =>
                PerusopetuksenKäyttäytymisenArviointi({
                  arvosana: {
                    koodiarvo: 'S',
                    koodistoUri: 'arviointiasteikkoyleissivistava'
                  } as Koodistokoodiviite<
                    'arviointiasteikkoyleissivistava',
                    string
                  >
                })
              )
            }
            testId="kayttaytyminen.lisaa"
          >
            {t('lisää')}
          </FlatButton>
        ) : (
          <KeyValueTable>
            <KeyValueRow localizableLabel="Arvosana">
              {form.editMode ? (
                <KoodistoSelect
                  koodistoUri="arviointiasteikkoyleissivistava"
                  addNewText={t('Lisää')}
                  value={suoritus.käyttäytymisenArvio?.arvosana.koodiarvo}
                  onSelect={(tunniste) => {
                    if (tunniste) {
                      form.updateAt(käyttäytymisenArvioPath, (prev) =>
                        PerusopetuksenKäyttäytymisenArviointi({
                          arvosana: tunniste as Koodistokoodiviite<
                            'arviointiasteikkoyleissivistava',
                            string
                          >,
                          kuvaus: prev?.kuvaus
                        })
                      )
                    } else {
                      form.updateAt(käyttäytymisenArvioPath, () => undefined)
                    }
                  }}
                  testId="kayttaytyminen"
                />
              ) : (
                suoritus.käyttäytymisenArvio && (
                  <TestIdText id="arvosana">
                    {suoritus.käyttäytymisenArvio.arvosana.koodiarvo}
                  </TestIdText>
                )
              )}
            </KeyValueRow>
            {(form.editMode
              ? hasArvio
              : suoritus.käyttäytymisenArvio?.kuvaus) && (
              <KeyValueRow localizableLabel="Sanallinen arviointi">
                <FormField
                  form={form}
                  path={käyttäytymisenArvioPath.optional().prop('kuvaus')}
                  optional
                  view={LocalizedTextView}
                  edit={LocalizedTextEdit}
                  testId="kuvaus"
                />
              </KeyValueRow>
            )}
          </KeyValueTable>
        )}
      </div>
    </TestIdLayer>
  )
}

const oppiaineNimi = (
  suoritus: OppiaineenTaiToiminta_AlueenSuoritus
): string => {
  return t(suoritus.koulutusmoduuli.tunniste.nimi)
}

const KieliNimiLowerView: React.FC<
  FieldViewerProps<Koodistokoodiviite<string, string> | undefined, EmptyObject>
> = ({ value, testId }) => (
  <TestIdText id={testId ?? 'kieli.value'}>
    {value ? t(value.nimi).toLowerCase() : ''}
  </TestIdText>
)

type Footnote = { title: string; hint: string }

const YKSILOLLISTETTY_FOOTNOTE: Footnote = {
  title: 'Yksilöllistetty tai rajattu oppimäärä',
  hint: '*'
}
const PAINOTETTU_FOOTNOTE: Footnote = { title: 'Painotettu opetus', hint: '**' }
const footnotesForSuoritus = (
  suoritus: OppiaineenTaiToiminta_AlueenSuoritus
): Footnote[] =>
  isNuortenPerusopetuksenOppiaineenSuoritus(suoritus)
    ? ([
        (suoritus.yksilöllistettyOppimäärä || suoritus.rajattuOppimäärä) &&
          YKSILOLLISTETTY_FOOTNOTE,
        suoritus.painotettuOpetus && PAINOTETTU_FOOTNOTE
      ].filter(Boolean) as Footnote[])
    : []

const computeFootnotes = (
  osasuoritukset: OppiaineenTaiToiminta_AlueenSuoritus[]
): Footnote[] =>
  [
    osasuoritukset.some(
      (s) =>
        isNuortenPerusopetuksenOppiaineenSuoritus(s) &&
        (s.yksilöllistettyOppimäärä || s.rajattuOppimäärä)
    ) && YKSILOLLISTETTY_FOOTNOTE,
    osasuoritukset.some(
      (s) => isNuortenPerusopetuksenOppiaineenSuoritus(s) && s.painotettuOpetus
    ) && PAINOTETTU_FOOTNOTE
  ].filter(Boolean) as Footnote[]
