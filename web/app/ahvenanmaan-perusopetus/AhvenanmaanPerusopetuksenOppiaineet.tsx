import React, { useCallback, useMemo } from 'react'
import { useKoodisto, useKoodistoFiller } from '../appstate/koodisto'
import { TestIdLayer, TestIdText } from '../appstate/useTestId'
import { Column, ColumnRow } from '../components-v2/containers/Columns'
import { ActivePäätasonSuoritus } from '../components-v2/containers/EditorContainer'
import {
  KeyValueRow,
  KeyValueTable
} from '../components-v2/containers/KeyValueTable'
import { FlatButton } from '../components-v2/controls/FlatButton'
import { IconButton } from '../components-v2/controls/IconButton'
import { NumberField } from '../components-v2/controls/NumberField'
import {
  LocalizedTextEdit,
  LocalizedTextView
} from '../components-v2/controls/LocalizedTestField'
import {
  OptionList,
  Select,
  SelectOption
} from '../components-v2/controls/Select'
import { TextEdit, TextView } from '../components-v2/controls/TextField'
import { FieldViewerProps, FormField } from '../components-v2/forms/FormField'
import { FormModel, FormOptic } from '../components-v2/forms/FormModel'
import {
  ParasArvosanaEdit,
  ParasArvosanaViewProps
} from '../components-v2/opiskeluoikeus/ArvosanaField'
import {
  BooleanEdit,
  BooleanView
} from '../components-v2/opiskeluoikeus/BooleanField'
import {
  KoodistoEdit,
  KoodistoView
} from '../components-v2/opiskeluoikeus/KoodistoField'
import { KoodistoSelect } from '../components-v2/opiskeluoikeus/KoodistoSelect'
import {
  OsasuoritusProperty,
  OsasuoritusPropertyValue
} from '../components-v2/opiskeluoikeus/OsasuoritusProperty'
import {
  OsasuoritusRowData,
  OsasuoritusTable,
  OsasuoritusTableColumn
} from '../components-v2/opiskeluoikeus/OsasuoritusTable'
import { CHARCODE_REMOVE } from '../components-v2/texts/Icon'
import { FootnoteDescriptions } from '../components/footnote'
import { finnish, t } from '../i18n/i18n'
import { shouldShowLaajuusColumn } from '../perusopetus-v2/oppiaineLaajuus'
import { AhvenanmaanPerusopetuksenMuuOppiaine } from '../types/fi/oph/koski/schema/AhvenanmaanPerusopetuksenMuuOppiaine'
import { AhvenanmaanOppiaineenTaiToimintaAlueenSuoritus } from '../types/fi/oph/koski/schema/AhvenanmaanOppiaineenTaiToimintaAlueenSuoritus'
import { AhvenanmaanPerusopetuksenOpiskeluoikeus } from '../types/fi/oph/koski/schema/AhvenanmaanPerusopetuksenOpiskeluoikeus'
import {
  AhvenanmaanPerusopetuksenOppiaineenSuoritus,
  isAhvenanmaanPerusopetuksenOppiaineenSuoritus
} from '../types/fi/oph/koski/schema/AhvenanmaanPerusopetuksenOppiaineenSuoritus'
import {
  AhvenanmaanPerusopetuksenOppimääränSuoritus,
  isAhvenanmaanPerusopetuksenOppimääränSuoritus
} from '../types/fi/oph/koski/schema/AhvenanmaanPerusopetuksenOppimaaranSuoritus'
import {
  AhvenanmaanPerusopetuksenPaikallinenOppiaine,
  isAhvenanmaanPerusopetuksenPaikallinenOppiaine
} from '../types/fi/oph/koski/schema/AhvenanmaanPerusopetuksenPaikallinenOppiaine'
import { AhvenanmaanPerusopetuksenToimintaAlue } from '../types/fi/oph/koski/schema/AhvenanmaanPerusopetuksenToimintaAlue'
import {
  AhvenanmaanPerusopetuksenToimintaAlueenSuoritus,
  isAhvenanmaanPerusopetuksenToimintaAlueenSuoritus
} from '../types/fi/oph/koski/schema/AhvenanmaanPerusopetuksenToimintaAlueenSuoritus'
import { AhvenanmaanPerusopetuksenVastuuJaYhteistyöArviointi } from '../types/fi/oph/koski/schema/AhvenanmaanPerusopetuksenVastuuJaYhteistyoArviointi'
import { AhvenanmaanPerusopetuksenVierasKieli } from '../types/fi/oph/koski/schema/AhvenanmaanPerusopetuksenVierasKieli'
import {
  AhvenanmaanPerusopetuksenVuosiluokanSuoritus,
  isAhvenanmaanPerusopetuksenVuosiluokanSuoritus
} from '../types/fi/oph/koski/schema/AhvenanmaanPerusopetuksenVuosiluokanSuoritus'
import { Arviointi } from '../types/fi/oph/koski/schema/Arviointi'
import { Koodistokoodiviite } from '../types/fi/oph/koski/schema/Koodistokoodiviite'
import { LaajuusVuosiviikkotunneissa } from '../types/fi/oph/koski/schema/LaajuusVuosiviikkotunneissa'
import { PaikallinenKoodi } from '../types/fi/oph/koski/schema/PaikallinenKoodi'
import { appendOptional } from '../util/array'
import { parasArviointi } from '../util/arvioinnit'
import { deleteAt } from '../util/fp/arrays'
import { EmptyObject } from '../util/objects'

// Ahvenanmaan arviointiasteikossa numeeriset arvosanat (4–10) näytetään
// numerona, mutta sanalliset (G/D/U) koko nimellään käyttöliittymän kielellä
// (esim. G → Godkänd / Hyväksytty). Käytetään sekä näkymässä että pudotuksessa.
const ahvenanmaanArvosananNimi = (
  k: Pick<Koodistokoodiviite, 'koodiarvo' | 'nimi'>
): string => {
  if (/^\d+$/.test(k.koodiarvo)) {
    return k.koodiarvo
  }
  const nimi = t(k.nimi)
  return nimi ? nimi.charAt(0).toUpperCase() + nimi.slice(1) : k.koodiarvo
}

const AhvenanmaanArvosanaView = <T extends Arviointi>(
  props: ParasArvosanaViewProps<T>
) => {
  const paras = props.value !== undefined && parasArviointi(props.value)
  return (
    <TestIdText id="arvosana.value">
      {paras ? ahvenanmaanArvosananNimi(paras.arvosana) : ''}
    </TestIdText>
  )
}

type SuoritusWithOsasuoritukset =
  | AhvenanmaanPerusopetuksenOppimääränSuoritus
  | AhvenanmaanPerusopetuksenVuosiluokanSuoritus

type AhvenanmaanPerusopetuksenOppiaineetProps = {
  form: FormModel<AhvenanmaanPerusopetuksenOpiskeluoikeus>
  päätasonSuoritus: ActivePäätasonSuoritus<AhvenanmaanPerusopetuksenOpiskeluoikeus>
}

export const AhvenanmaanPerusopetuksenOppiaineet: React.FC<
  AhvenanmaanPerusopetuksenOppiaineetProps
> = ({ form, päätasonSuoritus }) => {
  const suoritus = päätasonSuoritus.suoritus
  const suoritusIndex = päätasonSuoritus.index

  // 9. vuosiluokan oppiaineet näytetään vain jos oppilas jää luokalle; muuten
  // päättövuoden arvosanat ovat päättötodistuksella (avgångsbetyg), eikä
  // oppiaineita näytetä vuosiluokalla – kuten manner-Suomessa.
  if (
    isAhvenanmaanPerusopetuksenVuosiluokanSuoritus(suoritus) &&
    suoritus.koulutusmoduuli.tunniste.koodiarvo === '9' &&
    !suoritus.jääLuokalle
  ) {
    return null
  }

  const suoritusPath = päätasonSuoritus.path as unknown as FormOptic<
    AhvenanmaanPerusopetuksenOpiskeluoikeus,
    SuoritusWithOsasuoritukset
  >
  const osasuoritukset = suoritus.osasuoritukset || []

  // Ahvenanmaalla ei ole lisätieto-lippua toiminta-alueittain opiskelusta,
  // joten se päätellään osasuoritusten tyypistä.
  const isToimintaAlueittain = osasuoritukset.some(
    isAhvenanmaanPerusopetuksenToimintaAlueenSuoritus
  )

  // Oppimäärän arvosanoja ei näytetä ennen vahvistusta ellei olla
  // muokkaustilassa. Vuosiluokan suoritusten arvosanat näytetään aina.
  const showArvosana =
    form.editMode ||
    !isAhvenanmaanPerusopetuksenOppimääränSuoritus(suoritus) ||
    !!suoritus.vahvistus

  // Ryhmittely yhteisiin (pakolliset) ja valinnaisiin oppiaineisiin. Näytetään
  // myös tyhjälle vuosiluokalle muokkaustilassa, jotta molemmat lisäyspudotukset
  // (pakollinen + valinnainen) ovat käytettävissä – ei vain valinnaisen.
  const hasGrouping =
    !isToimintaAlueittain &&
    (form.editMode ||
      osasuoritukset.some(
        (s) =>
          isAhvenanmaanPerusopetuksenOppiaineenSuoritus(s) &&
          'pakollinen' in s.koulutusmoduuli
      ))

  const footnotes = computeFootnotes(osasuoritukset)

  const title = isToimintaAlueittain
    ? 'Toiminta-alueiden arvosanat'
    : 'Oppiaineiden arvosanat'

  const vastuuJaYhteistyöArvio =
    isAhvenanmaanPerusopetuksenVuosiluokanSuoritus(suoritus) &&
    (form.editMode || suoritus.vastuuJaYhteistyöArvio) ? (
      <VastuuJaYhteistyöArvioField
        form={form}
        suoritusPath={
          suoritusPath as unknown as FormOptic<
            AhvenanmaanPerusopetuksenOpiskeluoikeus,
            AhvenanmaanPerusopetuksenVuosiluokanSuoritus
          >
        }
        suoritus={suoritus}
      />
    ) : undefined

  return (
    <div className="oppiaineet">
      <h5>{t(title)}</h5>
      <p
        className="perusopetuksen-arvosteluasteikko"
        data-testid="perusopetuksen-arvosteluasteikko"
      >
        {t('Arvostelu 4-10, G (godkänd), D (deltagit) tai U (underkänd)')}
      </p>
      {hasGrouping ? (
        <GroupedOppiaineet
          osasuoritukset={osasuoritukset}
          suoritusIndex={suoritusIndex}
          isToimintaAlueittain={isToimintaAlueittain}
          form={form}
          suoritusPath={suoritusPath}
          showArvosana={showArvosana}
          vastuuJaYhteistyöArvio={vastuuJaYhteistyöArvio}
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
          {vastuuJaYhteistyöArvio}
        </>
      )}
      {!form.editMode && footnotes.length > 0 && (
        <FootnoteDescriptions data={footnotes} />
      )}
    </div>
  )
}

type OppiainetaulukkoProps = {
  osasuoritukset: AhvenanmaanOppiaineenTaiToimintaAlueenSuoritus[]
  suoritusIndex: number
  isToimintaAlueittain: boolean
  columnHeader: string
  title?: string
  pakollinen?: boolean
  form: FormModel<AhvenanmaanPerusopetuksenOpiskeluoikeus>
  suoritusPath: FormOptic<
    AhvenanmaanPerusopetuksenOpiskeluoikeus,
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
    // Leveämpi arvosanasarake mahtuu sanallisille arvosanoille (esim. Godkänd);
    // laajuussarake on kapeampi, koska siinä on vain pieni luku. Näkymässä
    // arvosana tasataan oikealle (lyhyet arvot), mutta muokkaustilassa sarake
    // venytetään täyteen leveyteen, jotta otsikko kohdistuu pudotusvalikon kanssa.
    ...(showArvosana
      ? [
          {
            key: 'Arvosana',
            align: form.editMode ? undefined : ('right' as const),
            span: 6
          }
        ]
      : []),
    // Laajuussarake on kapea vain muokkaustilassa (siinä on pelkkä numerokenttä
    // ja arvosanapudotus tarvitsee tilan). Näkymässä käytetään oletusleveyttä,
    // jotta yksikön nimi (esim. vuosiviikkotuntia) ei ylivuoda sarakkeen yli.
    ...(showLaajuus
      ? [{ key: 'Laajuus', span: form.editMode ? 2 : undefined }]
      : [])
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
            ? UusiAhvenanmaanToimintaAlue
            : UusiAhvenanmaanOppiaine
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
  osasuoritukset: AhvenanmaanOppiaineenTaiToimintaAlueenSuoritus[]
  suoritusIndex: number
  isToimintaAlueittain: boolean
  form: FormModel<AhvenanmaanPerusopetuksenOpiskeluoikeus>
  suoritusPath: FormOptic<
    AhvenanmaanPerusopetuksenOpiskeluoikeus,
    SuoritusWithOsasuoritukset
  >
  vastuuJaYhteistyöArvio?: React.ReactNode
  showArvosana: boolean
}

const GroupedOppiaineet: React.FC<GroupedOppiaineetProps> = ({
  osasuoritukset,
  suoritusIndex,
  isToimintaAlueittain,
  form,
  suoritusPath,
  vastuuJaYhteistyöArvio,
  showArvosana
}) => {
  const pakolliset = osasuoritukset.filter(
    (s) =>
      isAhvenanmaanPerusopetuksenOppiaineenSuoritus(s) &&
      s.koulutusmoduuli.pakollinen
  )
  const valinnaiset = osasuoritukset.filter(
    (s) =>
      isAhvenanmaanPerusopetuksenOppiaineenSuoritus(s) &&
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
      {(valinnaiset.length > 0 || form.editMode || vastuuJaYhteistyöArvio) && (
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
          {vastuuJaYhteistyöArvio}
        </Column>
      )}
    </ColumnRow>
  )
}

const oppiaineToRow = <T extends string>(
  suoritus: AhvenanmaanOppiaineenTaiToimintaAlueenSuoritus,
  suoritusIndex: number,
  osasuoritusIndex: number,
  columnHeader: T,
  form: FormModel<AhvenanmaanPerusopetuksenOpiskeluoikeus>,
  suoritusPath: FormOptic<
    AhvenanmaanPerusopetuksenOpiskeluoikeus,
    SuoritusWithOsasuoritukset
  >,
  showLaajuus?: boolean,
  showArvosana = true
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
    isAhvenanmaanPerusopetuksenOppiaineenSuoritus(suoritus) &&
    isAhvenanmaanPerusopetuksenPaikallinenOppiaine(suoritus.koulutusmoduuli)
  const nimi = oppiaineNimi(suoritus)

  const paikallinenTunnistePath = isPaikallinen
    ? (
        osasuoritusPath.prop('koulutusmoduuli') as unknown as FormOptic<
          AhvenanmaanPerusopetuksenOpiskeluoikeus,
          AhvenanmaanPerusopetuksenPaikallinenOppiaine
        >
      ).prop('tunniste')
    : undefined
  const hasKieli =
    isAhvenanmaanPerusopetuksenOppiaineenSuoritus(suoritus) &&
    'kieli' in suoritus.koulutusmoduuli &&
    !!suoritus.koulutusmoduuli.kieli
  const kieliPath = hasKieli
    ? ((
        osasuoritusPath.prop('koulutusmoduuli') as unknown as FormOptic<
          AhvenanmaanPerusopetuksenOpiskeluoikeus,
          { kieli: Koodistokoodiviite<string, string> }
        >
      ).prop('kieli') as FormOptic<
        AhvenanmaanPerusopetuksenOpiskeluoikeus,
        Koodistokoodiviite<string, string>
      >)
    : undefined
  const kieliKoodistoUri = hasKieli
    ? (
        suoritus as AhvenanmaanPerusopetuksenOppiaineenSuoritus & {
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
  // Alaviitemerkki (*) näytetään arvosanan perässä mukautetulle oppimäärälle.
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
          view={AhvenanmaanArvosanaView}
          edit={ParasArvosanaEdit}
          editProps={{
            suoritusClassName: suoritus.$class,
            format: ahvenanmaanArvosananNimi
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
    content: expandable ? (
      <OppiaineExpandedContent
        suoritus={suoritus}
        form={form}
        osasuoritusPath={osasuoritusPath}
      />
    ) : undefined
  }
}

type UusiAhvenanmaanOppiaineProps = {
  form: FormModel<AhvenanmaanPerusopetuksenOpiskeluoikeus>
  suoritusPath: FormOptic<
    AhvenanmaanPerusopetuksenOpiskeluoikeus,
    SuoritusWithOsasuoritukset
  >
  pakollinen?: boolean
  existingOsasuoritukset: AhvenanmaanOppiaineenTaiToimintaAlueenSuoritus[]
}

const UUSI_PAIKALLINEN_OPPIAINE_KEY = 'uusi-paikallinen-oppiaine'

// Ahvenanmaan vieraan kielen koodit (loput koodistosta ovat muita oppiaineita).
const VIERAS_KIELI_KOODIT = ['A1', 'A2', 'B1', 'B2']
const isVierasKieliKoodi = (koodiarvo: string): boolean =>
  VIERAS_KIELI_KOODIT.includes(koodiarvo)
// Kieltä muistuttava koodi (esim. B3) jota skeema ei salli jätetään pois
// pudotusvalikosta.
const isKielimäinenKoodi = (koodiarvo: string): boolean =>
  /^[AB]\d+$/.test(koodiarvo)

const oletuskieliKieliaineelle = (
  tunniste: Koodistokoodiviite<'ahvenanmaankoskioppiaineetyleissivistava'>
) =>
  Koodistokoodiviite({
    koodiarvo: tunniste.koodiarvo.startsWith('A') ? 'EN' : 'FI',
    koodistoUri: 'kielivalikoima'
  })

const uusiAhvenanmaanKoulutusmoduuli = (
  tunniste: Koodistokoodiviite<'ahvenanmaankoskioppiaineetyleissivistava'>,
  pakollinen: boolean
) => {
  if (isVierasKieliKoodi(tunniste.koodiarvo)) {
    return AhvenanmaanPerusopetuksenVierasKieli({
      pakollinen,
      tunniste: tunniste as AhvenanmaanPerusopetuksenVierasKieli['tunniste'],
      kieli: oletuskieliKieliaineelle(tunniste)
    })
  }
  return AhvenanmaanPerusopetuksenMuuOppiaine({
    pakollinen,
    tunniste: tunniste as AhvenanmaanPerusopetuksenMuuOppiaine['tunniste']
  })
}

const UusiAhvenanmaanOppiaine: React.FC<UusiAhvenanmaanOppiaineProps> = ({
  form,
  suoritusPath,
  pakollinen,
  existingOsasuoritukset
}) => {
  const fillKoodistot = useKoodistoFiller()
  const koodisto = useKoodisto('ahvenanmaankoskioppiaineetyleissivistava')

  const osasuorituksetPath = suoritusPath.prop('osasuoritukset').valueOr([]) as
    | FormOptic<
        AhvenanmaanPerusopetuksenOpiskeluoikeus,
        AhvenanmaanOppiaineenTaiToimintaAlueenSuoritus[]
      >
    | any

  const existingKoodiarvot = useMemo(
    () =>
      existingOsasuoritukset
        .filter(isAhvenanmaanPerusopetuksenOppiaineenSuoritus)
        .filter(
          (s) =>
            pakollinen === undefined ||
            ('pakollinen' in s.koulutusmoduuli &&
              s.koulutusmoduuli.pakollinen === pakollinen)
        )
        .filter(
          (s) =>
            !isAhvenanmaanPerusopetuksenPaikallinenOppiaine(s.koulutusmoduuli)
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
    | Koodistokoodiviite<'ahvenanmaankoskioppiaineetyleissivistava'>
    | PaikallinenKoodi
  > = useMemo(() => {
    const koodistoOpts = (koodisto || [])
      .filter(
        (k) =>
          !isKielimäinenKoodi(k.koodiviite.koodiarvo) ||
          isVierasKieliKoodi(k.koodiviite.koodiarvo)
      )
      .filter((k) =>
        pakollinen ? !existingKoodiarvot.includes(k.koodiviite.koodiarvo) : true
      )
      .map((k) => ({
        key: `valtakunnallinen-${k.koodiviite.koodiarvo}`,
        label: t(k.koodiviite.nimi),
        value:
          k.koodiviite as Koodistokoodiviite<'ahvenanmaankoskioppiaineetyleissivistava'>
      }))

    const uusiPaikallinenLabel = t('Lisää')
    const uusiPaikallinen = {
      key: UUSI_PAIKALLINEN_OPPIAINE_KEY,
      label: uusiPaikallinenLabel,
      display: (
        <>
          <span className="plus">{''}</span>
          {uusiPaikallinenLabel}
        </>
      ),
      value: undefined as any
    }

    return [...koodistoOpts, uusiPaikallinen]
  }, [koodisto, existingKoodiarvot, pakollinen])

  const onChange = useCallback(
    async (
      option?: SelectOption<
        | Koodistokoodiviite<'ahvenanmaankoskioppiaineetyleissivistava'>
        | PaikallinenKoodi
      >
    ) => {
      if (!option) return
      if (option.key === UUSI_PAIKALLINEN_OPPIAINE_KEY) {
        const emptyPaikallinen = AhvenanmaanPerusopetuksenPaikallinenOppiaine({
          pakollinen: pakollinen ?? false,
          tunniste: PaikallinenKoodi({
            koodiarvo: '',
            nimi: finnish('')
          }),
          kuvaus: finnish('')
        })
        const newSuoritus = AhvenanmaanPerusopetuksenOppiaineenSuoritus({
          koulutusmoduuli: emptyPaikallinen
        })
        form.updateAt(osasuorituksetPath, appendOptional(newSuoritus))
      } else if (option.value && 'koodiarvo' in option.value) {
        const newSuoritus = await fillKoodistot(
          AhvenanmaanPerusopetuksenOppiaineenSuoritus({
            koulutusmoduuli: uusiAhvenanmaanKoulutusmoduuli(
              option.value as Koodistokoodiviite<'ahvenanmaankoskioppiaineetyleissivistava'>,
              pakollinen ?? false
            )
          })
        )
        form.updateAt(osasuorituksetPath, appendOptional(newSuoritus))
      }
    },
    [fillKoodistot, form, osasuorituksetPath, pakollinen]
  )

  if (!form.editMode) return null

  return (
    <Select
      placeholder={t(placeholder)}
      options={options}
      hideEmpty
      onChange={onChange}
      testId="uusi-oppiaine"
    />
  )
}

const UusiAhvenanmaanToimintaAlue: React.FC<UusiAhvenanmaanOppiaineProps> = ({
  form,
  suoritusPath,
  existingOsasuoritukset
}) => {
  const fillKoodistot = useKoodistoFiller()
  if (!form.editMode) return null
  const osasuorituksetPath = suoritusPath.prop('osasuoritukset').valueOr([]) as
    | FormOptic<
        AhvenanmaanPerusopetuksenOpiskeluoikeus,
        AhvenanmaanOppiaineenTaiToimintaAlueenSuoritus[]
      >
    | any

  const existingKoodiarvot = existingOsasuoritukset
    .filter(isAhvenanmaanPerusopetuksenToimintaAlueenSuoritus)
    .map((s) => s.koulutusmoduuli.tunniste.koodiarvo)

  return (
    <KoodistoSelect
      koodistoUri="ahvenanmaanperusopetuksentoimintaalue"
      addNewText={t('Lisää toiminta-alue')}
      filter={(tunniste) => !existingKoodiarvot.includes(tunniste.koodiarvo)}
      onSelect={async (tunniste) => {
        if (!tunniste) return
        const newSuoritus = await fillKoodistot(
          AhvenanmaanPerusopetuksenToimintaAlueenSuoritus({
            koulutusmoduuli: AhvenanmaanPerusopetuksenToimintaAlue({ tunniste })
          })
        )
        form.updateAt(osasuorituksetPath, appendOptional(newSuoritus))
      }}
      testId="uusi-oppiaine"
    />
  )
}

const hasExpandableProperties = (
  suoritus: AhvenanmaanOppiaineenTaiToimintaAlueenSuoritus,
  editMode: boolean
): boolean => {
  if (editMode && isAhvenanmaanPerusopetuksenOppiaineenSuoritus(suoritus)) {
    return true
  }
  if (
    isAhvenanmaanPerusopetuksenOppiaineenSuoritus(suoritus) &&
    isAhvenanmaanPerusopetuksenPaikallinenOppiaine(suoritus.koulutusmoduuli) &&
    t(suoritus.koulutusmoduuli.kuvaus)
  ) {
    return true
  }
  if (
    isAhvenanmaanPerusopetuksenOppiaineenSuoritus(suoritus) &&
    suoritus.suoritustapa
  ) {
    return true
  }
  if (suoritus.suorituskieli) {
    return true
  }
  return false
}

type OppiaineExpandedContentProps = {
  suoritus: AhvenanmaanOppiaineenTaiToimintaAlueenSuoritus
  form: FormModel<AhvenanmaanPerusopetuksenOpiskeluoikeus>
  osasuoritusPath: FormOptic<
    AhvenanmaanPerusopetuksenOpiskeluoikeus,
    AhvenanmaanOppiaineenTaiToimintaAlueenSuoritus
  >
}

const OppiaineExpandedContent: React.FC<OppiaineExpandedContentProps> = ({
  suoritus,
  form,
  osasuoritusPath
}) => {
  const isOppiaine = isAhvenanmaanPerusopetuksenOppiaineenSuoritus(suoritus)
  const isPaikallinen =
    isOppiaine &&
    isAhvenanmaanPerusopetuksenPaikallinenOppiaine(suoritus.koulutusmoduuli)
  const oppiainePath = osasuoritusPath as unknown as FormOptic<
    AhvenanmaanPerusopetuksenOpiskeluoikeus,
    AhvenanmaanPerusopetuksenOppiaineenSuoritus
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
                  AhvenanmaanPerusopetuksenOpiskeluoikeus,
                  AhvenanmaanPerusopetuksenPaikallinenOppiaine
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
      {isOppiaine && (form.editMode || suoritus.mukautettuOppimäärä) && (
        <OsasuoritusProperty label="Mukautettu oppimäärä">
          <OsasuoritusPropertyValue>
            <FormField
              form={form}
              path={oppiainePath.prop('mukautettuOppimäärä')}
              view={BooleanView}
              edit={BooleanEdit}
              testId="mukautettuOppimäärä"
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
              editProps={{
                koodistoUri: 'perusopetuksensuoritustapa',
                // Vain erityinentutkinto on sallittu; tyhjennettävissä (kenttä on
                // valinnainen).
                koodiarvot: ['erityinentutkinto'],
                zeroValueOption: true
              }}
            />
          </OsasuoritusPropertyValue>
        </OsasuoritusProperty>
      )}
    </>
  )
}

const VastuuJaYhteistyöArvioField: React.FC<{
  form: FormModel<AhvenanmaanPerusopetuksenOpiskeluoikeus>
  suoritusPath: FormOptic<
    AhvenanmaanPerusopetuksenOpiskeluoikeus,
    AhvenanmaanPerusopetuksenVuosiluokanSuoritus
  >
  suoritus: AhvenanmaanPerusopetuksenVuosiluokanSuoritus
}> = ({ form, suoritusPath, suoritus }) => {
  const fillKoodistot = useKoodistoFiller()
  const arvioPath = suoritusPath.prop('vastuuJaYhteistyöArvio')
  const arvio = suoritus.vastuuJaYhteistyöArvio
  const hasArvio = !!arvio

  if (!form.editMode && !hasArvio) return null

  return (
    <TestIdLayer id="vastuuJaYhteistyo">
      <div className="kayttaytyminen">
        <h5>
          {t('Ansvar och samarbete')}
          {form.editMode && hasArvio && (
            <IconButton
              charCode={CHARCODE_REMOVE}
              label={t('Poista')}
              size="input"
              onClick={() => form.updateAt(arvioPath, () => undefined)}
              testId="remove"
            />
          )}
        </h5>
        {form.editMode && !hasArvio ? (
          <FlatButton
            onClick={async () => {
              // Täytetään koodiston nimi, jotta arvosana näkyy heti koko
              // nimellään (esim. Godkänd) eikä koodiarvona (G).
              const uusiArvio = await fillKoodistot(
                AhvenanmaanPerusopetuksenVastuuJaYhteistyöArviointi({})
              )
              form.updateAt(arvioPath, () => uusiArvio)
            }}
            testId="lisaa"
          >
            {t('lisää')}
          </FlatButton>
        ) : (
          arvio && (
            <KeyValueTable>
              <KeyValueRow localizableLabel="Arvosana">
                <TestIdText id="arvosana">
                  {ahvenanmaanArvosananNimi(arvio.arvosana)}
                </TestIdText>
              </KeyValueRow>
            </KeyValueTable>
          )
        )}
      </div>
    </TestIdLayer>
  )
}

const oppiaineNimi = (
  suoritus: AhvenanmaanOppiaineenTaiToimintaAlueenSuoritus
): string => t(suoritus.koulutusmoduuli.tunniste.nimi)

const KieliNimiLowerView: React.FC<
  FieldViewerProps<Koodistokoodiviite<string, string> | undefined, EmptyObject>
> = ({ value, testId }) => (
  <TestIdText id={testId ?? 'kieli.value'}>
    {value ? t(value.nimi).toLowerCase() : ''}
  </TestIdText>
)

type Footnote = { title: string; hint: string }

const MUKAUTETTU_FOOTNOTE: Footnote = {
  title: 'Mukautettu oppimäärä',
  hint: '*'
}

const footnotesForSuoritus = (
  suoritus: AhvenanmaanOppiaineenTaiToimintaAlueenSuoritus
): Footnote[] =>
  isAhvenanmaanPerusopetuksenOppiaineenSuoritus(suoritus) &&
  suoritus.mukautettuOppimäärä
    ? [MUKAUTETTU_FOOTNOTE]
    : []

const computeFootnotes = (
  osasuoritukset: AhvenanmaanOppiaineenTaiToimintaAlueenSuoritus[]
): Footnote[] =>
  osasuoritukset.some(
    (s) =>
      isAhvenanmaanPerusopetuksenOppiaineenSuoritus(s) && s.mukautettuOppimäärä
  )
    ? [MUKAUTETTU_FOOTNOTE]
    : []
