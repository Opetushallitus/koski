import { string } from 'fp-ts'
import * as A from 'fp-ts/Array'
import { constant, pipe } from 'fp-ts/lib/function'
import * as NEA from 'fp-ts/NonEmptyArray'
import * as NonEmptyArray from 'fp-ts/NonEmptyArray'
import * as O from 'fp-ts/Option'
import * as Ord from 'fp-ts/Ord'
import React, { useCallback, useEffect, useMemo, useRef, useState } from 'react'
import { TestIdLayer, TestIdText } from '../../appstate/useTestId'
import { ISO2FinnishDate } from '../../date/date'
import { t } from '../../i18n/i18n'
import { isArvioinniton } from '../../types/fi/oph/koski/schema/Arvioinniton'
import { Arviointi } from '../../types/fi/oph/koski/schema/Arviointi'
import { isIBAineRyhmäOppiaine } from '../../types/fi/oph/koski/schema/IBAineRyhmaOppiaine'
import { isIBKurssi } from '../../types/fi/oph/koski/schema/IBKurssi'
import { IBOpiskeluoikeus } from '../../types/fi/oph/koski/schema/IBOpiskeluoikeus'
import { isIBOppiaineCAS } from '../../types/fi/oph/koski/schema/IBOppiaineCAS'
import { isIBOppiaineenSuoritus } from '../../types/fi/oph/koski/schema/IBOppiaineenSuoritus'
import { isIBOppiaineExtendedEssay } from '../../types/fi/oph/koski/schema/IBOppiaineExtendedEssay'
import { isIBOppiaineLanguage } from '../../types/fi/oph/koski/schema/IBOppiaineLanguage'
import { isIBTaso } from '../../types/fi/oph/koski/schema/IBTaso'
import { IBTheoryOfKnowledgeSuoritus } from '../../types/fi/oph/koski/schema/IBTheoryOfKnowledgeSuoritus'
import { IBTutkinnonSuoritus } from '../../types/fi/oph/koski/schema/IBTutkinnonSuoritus'
import { LocalizedString } from '../../types/fi/oph/koski/schema/LocalizedString'
import { LukionArviointi } from '../../types/fi/oph/koski/schema/LukionArviointi'
import { isLukionKurssinSuoritus2015 } from '../../types/fi/oph/koski/schema/LukionKurssinSuoritus2015'
import { isLukionPaikallinenOpintojakso2019 } from '../../types/fi/oph/koski/schema/LukionPaikallinenOpintojakso2019'
import { MuidenLukioOpintojenPreIBSuoritus2019 } from '../../types/fi/oph/koski/schema/MuidenLukioOpintojenPreIBSuoritus2019'
import { isPaikallinenKoodi } from '../../types/fi/oph/koski/schema/PaikallinenKoodi'
import { isPaikallinenLukionKurssi2015 } from '../../types/fi/oph/koski/schema/PaikallinenLukionKurssi2015'
import { Suoritus } from '../../types/fi/oph/koski/schema/Suoritus'
import { isValinnaisuus } from '../../types/fi/oph/koski/schema/Valinnaisuus'
import { isValinnanMahdollisuus } from '../../types/fi/oph/koski/schema/ValinnanMahdollisuus'
import { appendOptional, deleteAt } from '../../util/array'
import { parasArviointi, viimeisinArviointi } from '../../util/arvioinnit'
import { nonFalsy, nonNull } from '../../util/fp/arrays'
import { PathToken } from '../../util/laxModify'
import { indexSequence, sum } from '../../util/numbers'
import { entries } from '../../util/objects'
import { PäätasonSuoritusOf } from '../../util/opiskeluoikeus'
import { KoulutusmoduuliOf, OsasuoritusOf } from '../../util/schema'
import { suoritusValmis } from '../../util/suoritus'
import { useBooleanState } from '../../util/useBooleanState'
import { notUndefined } from '../../util/util'
import { ActivePäätasonSuoritus } from '../containers/EditorContainer'
import { KeyValueRow, KeyValueTable } from '../containers/KeyValueTable'
import { FlatButton } from '../controls/FlatButton'
import { IconButton } from '../controls/IconButton'
import { FormModel, getValue } from '../forms/FormModel'
import { CHARCODE_REMOVE } from '../texts/Icon'
import { ArvosanaEdit, koodiarvoOnly } from './ArvosanaField'
import { OppiaineTableKurssiEditor } from './OppiaineTableKurssiEditor'
import { OppiaineTableOppiaineEditor } from './OppiaineTableOppiaineEditor'
import { formatLaajuus } from '../../util/laajuus'

// Vain OppiaineTablen tukemat päätason suoritukset (tätä komponenttia tullaan myöhemmin käyttämään ainakin lukion näkymille)
export type OppiaineTableOpiskeluoikeus = IBOpiskeluoikeus
export type OppiaineTablePäätasonSuoritus =
  PäätasonSuoritusOf<OppiaineTableOpiskeluoikeus>

export type Oppiaine =
  | OsasuoritusOf<OppiaineTablePäätasonSuoritus>
  | IBTheoryOfKnowledgeSuoritus
export type OppiaineenOsasuoritus = OsasuoritusOf<Oppiaine>

export type OppiaineTableProps<T extends OppiaineTablePäätasonSuoritus> = {
  form: FormModel<OppiaineTableOpiskeluoikeus>
  selectedSuoritus: ActivePäätasonSuoritus<OppiaineTableOpiskeluoikeus>
  addOsasuoritusDialog: AddOppiaineenOsasuoritusDialog<
    OsasuoritusOf<OsasuoritusOf<T>>
  >
  groupBy?: (oppiaine: Oppiaine) => string
}

export const OppiaineTable = <T extends OppiaineTablePäätasonSuoritus>({
  form,
  selectedSuoritus,
  addOsasuoritusDialog,
  groupBy
}: OppiaineTableProps<T>) => {
  const suoritus = getValue(selectedSuoritus.path)(form.state)
  const path = selectedSuoritus.pathTokens
  const organisaatioOid = form.state.oppilaitos?.oid
  const alkamispäivä = form.state.alkamispäivä

  const showPredictedGrade =
    selectedSuoritus.suoritus.$class === IBTutkinnonSuoritus.className
  const hidePaikallinenIndicator =
    selectedSuoritus.suoritus.$class === IBTutkinnonSuoritus.className

  const oppiainePath = (oppiaine: Oppiaine) => [
    ...path,
    'osasuoritukset',
    suoritus?.osasuoritukset?.findIndex((os) => os === oppiaine) ?? -1
  ]

  const deleteOppiaine = (oppiaine: Oppiaine) => () =>
    form.modify(
      ...path,
      'osasuoritukset'
    )((os: Oppiaine[]) => deleteAt(os.indexOf(oppiaine) ?? -1)(os))

  const deleteKurssi = (oppiaine: Oppiaine) => (kurssiIndex: number) =>
    form.modify(
      ...oppiainePath(oppiaine),
      'osasuoritukset'
    )((os: OppiaineenOsasuoritus[]) => deleteAt(kurssiIndex)(os))

  const addOsasuoritus =
    (oppiaine: Oppiaine) => (osasuoritus: OppiaineenOsasuoritus) =>
      form.modify(
        ...oppiainePath(oppiaine),
        'osasuoritukset'
      )(appendOptional(osasuoritus))

  const addKurssiArviointi =
    (oppiaine: Oppiaine) => (osasuoritusIndex: number, arviointi: Arviointi) =>
      form.modify(
        ...oppiainePath(oppiaine),
        'osasuoritukset',
        osasuoritusIndex,
        'arviointi'
      )(appendOptional(arviointi))

  const addOppiaineArviointi =
    (oppiaine: Oppiaine) => (arviointi: Arviointi) => {
      form.modify(
        ...oppiainePath(oppiaine),
        'arviointi'
      )(appendOptional(arviointi))
    }

  const addPredictedGrade = (oppiaine: Oppiaine) => (arviointi: Arviointi) => {
    form.debug.modify(
      ...oppiainePath(oppiaine),
      'predictedArviointi'
    )(appendOptional(arviointi))
  }

  const groupedOppiaineet = useMemo(
    () =>
      pipe(
        suoritus?.osasuoritukset || [],
        NEA.fromArray<Oppiaine>,
        O.map(NEA.groupBy(groupBy || constant(''))),
        O.map(entries),
        O.getOrElse(constant<Array<[string, NEA.NonEmptyArray<Oppiaine>]>>([]))
      ),
    [groupBy, suoritus?.osasuoritukset]
  )

  const laajuusyksikkö = useMemo(() => {
    const koulutusmoduuli = (suoritus?.osasuoritukset || []).flatMap(
      (os) => (os.osasuoritukset || []) as Suoritus[]
    )[0]?.koulutusmoduuli
    return t((koulutusmoduuli as any)?.laajuus?.yksikkö.nimi)
  }, [suoritus?.osasuoritukset])

  const nextOppiaineIndex = indexSequence()

  return A.isEmpty(groupedOppiaineet) && organisaatioOid ? null : (
    <table className="OppiaineTable">
      <thead>
        <tr>
          <th></th>
          <th className="OppiaineTable__oppiaine">{t('Oppiaine')}</th>
          <th className="OppiaineTable__laajuus">
            {`${t('Laajuus')} ${laajuusyksikkö ? `(${laajuusyksikkö})` : ''}`}
          </th>
          {showPredictedGrade && (
            <th className="OppiaineTable__predictedGrade">
              {t('Predicted grade')}
            </th>
          )}
          <th className="OppiaineTable__arvosana">{t('Arvosana')}</th>
          {form.editMode && <th className="OppiaineTable__poisto" />}
        </tr>
      </thead>
      <TestIdLayer id="oppiaineryhmät">
        {groupedOppiaineet.map(([groupName, oppiaineet], groupIndex) => (
          <TestIdLayer id={groupIndex} key={groupIndex}>
            <tbody key={groupIndex}>
              {groupName ? (
                <tr>
                  <th colSpan={10}>
                    <TestIdText id="nimi">{groupName}</TestIdText>
                  </th>
                </tr>
              ) : null}
              <TestIdLayer id="oppiaineet">
                {oppiaineet.map((oppiaine, oppiaineArrayIndex) => {
                  const oppiaineModelIndex = nextOppiaineIndex()
                  return (
                    <TestIdLayer
                      id={oppiaineArrayIndex}
                      key={oppiaineModelIndex}
                    >
                      <OppiaineRow
                        organisaatioOid={organisaatioOid!}
                        alkamispäivä={alkamispäivä}
                        oppiaine={oppiaine}
                        form={form}
                        showPredictedGrade={showPredictedGrade}
                        oppiainePath={[
                          ...selectedSuoritus.pathTokens,
                          'osasuoritukset',
                          oppiaineModelIndex
                        ]}
                        hidePaikallinenIndicator={hidePaikallinenIndicator}
                        onDelete={deleteOppiaine(oppiaine)}
                        onDeleteKurssi={deleteKurssi(oppiaine)}
                        addOsasuoritusDialog={addOsasuoritusDialog}
                        onAddOsasuoritus={addOsasuoritus(oppiaine)}
                        onArviointi={addKurssiArviointi(oppiaine)}
                        onOppiaineArviointi={addOppiaineArviointi(oppiaine)}
                        onPredictedGrade={addPredictedGrade(oppiaine)}
                        key={oppiaineArrayIndex}
                      />
                    </TestIdLayer>
                  )
                })}
              </TestIdLayer>
            </tbody>
          </TestIdLayer>
        ))}
      </TestIdLayer>
    </table>
  )
}

export type OppiaineRowProps<T> = {
  form: FormModel<OppiaineTableOpiskeluoikeus>
  oppiainePath: PathToken[]
  organisaatioOid: string
  alkamispäivä?: string
  oppiaine: Oppiaine
  showPredictedGrade: boolean
  addOsasuoritusDialog: AddOppiaineenOsasuoritusDialog<T>
  hidePaikallinenIndicator?: boolean
  onAddOsasuoritus: (t: T) => void
  onArviointi: (osasuoritusIndex: number, arviointi: Arviointi) => void
  onOppiaineArviointi: (arviointi: Arviointi) => void
  onPredictedGrade: (arviointi: Arviointi) => void
  onDelete: () => void
  onDeleteKurssi: (index: number) => void
}

export type AddOppiaineenOsasuoritusDialog<T> = React.FC<{
  alkamispäivä?: string
  organisaatioOid: string
  oppiaine: Oppiaine
  onAdd: (t: T) => void
  onClose: () => void
}>

const OppiaineRow = <T,>({
  organisaatioOid,
  alkamispäivä,
  oppiaine,
  oppiainePath,
  form,
  showPredictedGrade,
  hidePaikallinenIndicator,
  onDelete,
  addOsasuoritusDialog,
  onAddOsasuoritus,
  onArviointi,
  onPredictedGrade,
  onOppiaineArviointi,
  onDeleteKurssi
}: OppiaineRowProps<T>) => {
  const laajuusYhteensä = useLaajuusYhteensä(oppiaine)
  const [
    addOsasuoritusDialogVisible,
    showAddOsasuoritusDialog,
    hideAddOsasuoritusDialog
  ] = useBooleanState(false)

  const [tooltipVisible, openTooltip, closeTooltip] = useBooleanState(false)
  const [editModalVisible, openEditModal, closeEditModal] =
    useBooleanState(false)
  const tooltipId = `oppiaine-${oppiaine.koulutusmoduuli.tunniste.koodiarvo}`

  const AddOsasuoritusDialog = addOsasuoritusDialog

  const addOsasuoritus = useCallback(
    (osasuoritus: T) => {
      onAddOsasuoritus(osasuoritus)
      hideAddOsasuoritusDialog()
    },
    [hideAddOsasuoritusDialog, onAddOsasuoritus]
  )

  return (
    <tr>
      <td className="OppiaineRow__icon">
        <SuorituksenTilaIcon suoritus={oppiaine} />
      </td>
      <td className="OppiaineRow__oppiaine">
        <div className="OppiaineRow__nimi">
          <button
            className="Oppiaine__tunniste"
            onClick={form.editMode ? openEditModal : openTooltip}
            onTouchStart={openTooltip}
            onMouseEnter={openTooltip}
            onMouseLeave={closeTooltip}
            onFocus={openTooltip}
            onBlur={closeTooltip}
            aria-describedby={tooltipId}
          >
            <TestIdText id="nimi">
              {oppiaineenNimi(oppiaine.koulutusmoduuli)}
            </TestIdText>
          </button>
          {tooltipVisible && !editModalVisible && (
            <OppiaineDetails id={tooltipId} oppiaine={oppiaine} />
          )}
        </div>
        <OppiaineenKurssit
          form={form}
          kurssit={oppiaine.osasuoritukset || []}
          oppiaine={oppiaine}
          oppiainePath={oppiainePath}
          hidePaikallinenIndicator={hidePaikallinenIndicator}
          onArviointi={onArviointi}
          onDeleteKurssi={onDeleteKurssi}
          onShowAddOsasuoritusDialog={showAddOsasuoritusDialog}
        />
      </td>
      <td className="OppiaineRow__laajuus">
        <TestIdText id="laajuus">{laajuusYhteensä}</TestIdText>
      </td>
      {showPredictedGrade && (
        <td className="OppiaineRow__predictedGrade">
          <PredictedGrade
            form={form}
            oppiaine={oppiaine}
            onChange={onPredictedGrade}
          />
        </td>
      )}
      <td className="OppiaineRow__arvosana">
        <OppiaineArvosana
          form={form}
          oppiaine={oppiaine}
          onChange={onOppiaineArviointi}
        />
      </td>
      {form.editMode && (
        <td className="OppiaineRow__poisto">
          <IconButton
            charCode={CHARCODE_REMOVE}
            label={t('Poista')}
            size="input"
            onClick={onDelete}
            testId="delete"
          />
          {addOsasuoritusDialogVisible && (
            <AddOsasuoritusDialog
              alkamispäivä={alkamispäivä}
              organisaatioOid={organisaatioOid}
              oppiaine={oppiaine}
              onAdd={addOsasuoritus}
              onClose={hideAddOsasuoritusDialog}
            />
          )}
          {editModalVisible && (
            <OppiaineTableOppiaineEditor
              form={form}
              path={oppiainePath}
              onClose={closeEditModal}
            />
          )}
        </td>
      )}
    </tr>
  )
}

export type OppiaineenKurssitProps = {
  form: FormModel<OppiaineTableOpiskeluoikeus>
  kurssit: OppiaineenOsasuoritus[]
  oppiaine: Oppiaine
  oppiainePath: PathToken[]
  hidePaikallinenIndicator?: boolean
  onArviointi: (osasuoritusIndex: number, arviointi: Arviointi) => void
  onDeleteKurssi: (index: number) => void
  onShowAddOsasuoritusDialog: () => void
}

const kurssiNaturalOrd = Ord.contramap((kurssi: OppiaineenOsasuoritus) => {
  const tunniste = kurssi.koulutusmoduuli.tunniste.koodiarvo
  const tunnisteMatch = tunniste.match(/([^\d]*)(\d*)/)
  return tunnisteMatch
    ? `${tunnisteMatch[1]}${tunnisteMatch[2].padStart(8, '0')}`
    : tunniste
})(string.Ord)

const booleanOrd = Ord.fromCompare((a?: boolean, b?: boolean) =>
  a ? (b ? 0 : -1) : b ? 1 : 0
)

const kurssiPakollinenOrd = Ord.contramap((kurssi: OppiaineenOsasuoritus) =>
  isValinnaisuus(kurssi.koulutusmoduuli)
    ? kurssi.koulutusmoduuli.pakollinen
    : undefined
)(booleanOrd)

export const OppiaineenKurssit = ({
  form,
  kurssit,
  oppiaine,
  oppiainePath,
  hidePaikallinenIndicator,
  onArviointi,
  onDeleteKurssi,
  onShowAddOsasuoritusDialog
}: OppiaineenKurssitProps) => {
  const sortedKurssit = useMemo(
    () => A.sortBy([kurssiPakollinenOrd, kurssiNaturalOrd])(kurssit),
    [kurssit]
  )

  const mapBack = useCallback(
    (index: number) => kurssit.indexOf(sortedKurssit[index]),
    [kurssit, sortedKurssit]
  )

  const addArviointi = useCallback(
    (osasuoritusIndex: number) => (arviointi?: Arviointi) => {
      if (arviointi) {
        onArviointi(mapBack(osasuoritusIndex), arviointi)
      }
    },
    [onArviointi, mapBack]
  )

  const deleteKurssi = useCallback(
    (sortedIndex: number) => {
      onDeleteKurssi(kurssit.indexOf(sortedKurssit[sortedIndex]))
    },
    [sortedKurssit, kurssit, onDeleteKurssi]
  )

  return (
    <div className="OppiaineRow__kurssit">
      <TestIdLayer id="kurssit">
        {sortedKurssit.map((kurssi, index) => (
          <TestIdLayer id={index} key={index}>
            <Kurssi
              key={index}
              form={form}
              kurssi={kurssi}
              kurssiPath={[...oppiainePath, 'osasuoritukset', index]}
              oppiaine={oppiaine}
              hidePaikallinenIndicator={hidePaikallinenIndicator}
              onArviointi={addArviointi(index)}
              onDelete={() => deleteKurssi(index)}
            />
          </TestIdLayer>
        ))}
      </TestIdLayer>
      {form.editMode && isOsasuorituksellinenOppiaine(oppiaine) && (
        <FlatButton onClick={onShowAddOsasuoritusDialog} testId="addKurssi">
          {t('Lisää osasuoritus')}
        </FlatButton>
      )}
    </div>
  )
}

const isOsasuorituksellinenOppiaine = (oppiaine?: Oppiaine): boolean => {
  const koulutus = oppiaine?.koulutusmoduuli
  return !isIBOppiaineCAS(koulutus) && !isIBOppiaineExtendedEssay(koulutus)
}

type OppiaineArvosanaProps = {
  form: FormModel<OppiaineTableOpiskeluoikeus>
  oppiaine: Oppiaine
  onChange: (a: Arviointi) => void
}

export const isArvioinnillinenOppiaine = (
  os: Oppiaine
): os is Exclude<Oppiaine, MuidenLukioOpintojenPreIBSuoritus2019> =>
  !isArvioinniton(os)

const OppiaineArvosana: React.FC<OppiaineArvosanaProps> = ({
  form,
  oppiaine,
  onChange
}) => {
  const onChange_ = useCallback(
    (a?: Arviointi) => {
      a && onChange(a)
    },
    [onChange]
  )

  if (!isArvioinnillinenOppiaine(oppiaine)) {
    return null
  }

  const arvioinnit: Arviointi[] | undefined = oppiaine.arviointi

  return form.editMode ? (
    <ArvosanaEdit
      value={arvioinnit && viimeisinArviointi(arvioinnit)}
      onChange={onChange_}
      suoritusClassName={oppiaine.$class}
      format={koodiarvoOnly}
    />
  ) : (
    <TestIdText id="arvosana.value">
      {arvioinnit ? parasArviointi(arvioinnit)?.arvosana.koodiarvo : '-'}
    </TestIdText>
  )
}

const PredictedGrade: React.FC<OppiaineArvosanaProps> = ({
  form,
  oppiaine,
  onChange
}) => {
  const onChange_ = useCallback(
    (a?: Arviointi) => {
      a && onChange(a)
    },
    [onChange]
  )

  if (!isIBOppiaineenSuoritus(oppiaine)) {
    return null
  }

  const arvioinnit: Arviointi[] | undefined = oppiaine.predictedArviointi

  return form.editMode ? (
    <ArvosanaEdit
      value={arvioinnit && viimeisinArviointi(arvioinnit)}
      onChange={onChange_}
      suoritusClassName={oppiaine.$class}
      arviointiPropName="predictedArviointi"
      format={koodiarvoOnly}
      testId="predictedGrade"
    />
  ) : (
    <TestIdText id="predictedGrade.value">
      {arvioinnit ? parasArviointi(arvioinnit)?.arvosana.koodiarvo : '-'}
    </TestIdText>
  )
}

const oppiaineenNimi = (koulutusmoduuli: KoulutusmoduuliOf<Oppiaine>) =>
  pipe(
    [
      (koulutusmoduuli as any)?.oppimäärä?.nimi ||
        koulutusmoduuli.tunniste.nimi,
      (koulutusmoduuli as any)?.kieli?.nimi
    ],
    A.filter(notUndefined),
    A.map(t),
    (as) => as.join(', ')
  )

type KurssiProps = {
  form: FormModel<OppiaineTableOpiskeluoikeus>
  oppiaine: Oppiaine
  kurssi: OsasuoritusOf<Oppiaine>
  kurssiPath: PathToken[]
  hidePaikallinenIndicator?: boolean
  onArviointi: (arviointi?: LukionArviointi) => void
  onDelete: () => void
}

export const Kurssi: React.FC<KurssiProps> = ({
  form,
  kurssi,
  kurssiPath,
  oppiaine,
  hidePaikallinenIndicator,
  onArviointi,
  onDelete
}) => {
  const [tooltipVisible, openTooltip, closeTooltip] = useBooleanState(false)
  const [editModalVisible, openEditModal, closeEditModal] =
    useBooleanState(false)
  const tooltipId = `kurssi-${oppiaine.koulutusmoduuli.tunniste.koodiarvo}-${kurssi.koulutusmoduuli.tunniste.koodiarvo}`
  const arviointi =
    kurssi.arviointi && viimeisinArviointi([...kurssi.arviointi])

  return (
    <div className="Kurssi">
      <button
        className="Kurssi__tunniste"
        onClick={form.editMode ? openEditModal : openTooltip}
        onTouchStart={openTooltip}
        onMouseEnter={openTooltip}
        onMouseLeave={closeTooltip}
        onFocus={openTooltip}
        onBlur={closeTooltip}
        aria-describedby={tooltipId}
      >
        <TestIdText id="tunniste">
          {kurssi.koulutusmoduuli.tunniste.koodiarvo}
          {!hidePaikallinenIndicator &&
            isPaikallinenKoodi(kurssi.koulutusmoduuli.tunniste) &&
            ' *'}
        </TestIdText>
        {form.editMode && (
          <IconButton
            charCode={CHARCODE_REMOVE}
            label={t('Poista')}
            size="input"
            onClick={onDelete}
            testId="delete"
          />
        )}
      </button>
      <div className="Kurssi__arvosana">
        {form.editMode ? (
          <ArvosanaEdit
            value={arviointi as any}
            onChange={onArviointi}
            suoritusClassName={kurssi.$class}
            format={koodiarvoOnly}
          />
        ) : (
          <TestIdText id="arvosana.value">
            {kurssi.arviointi
              ? parasArviointi(kurssi.arviointi as Arviointi[])?.arvosana
                  .koodiarvo
              : '–'}
          </TestIdText>
        )}
      </div>
      {editModalVisible ? (
        <OppiaineTableKurssiEditor
          form={form}
          path={kurssiPath}
          onClose={closeEditModal}
        />
      ) : (
        tooltipVisible && <KurssiDetails kurssi={kurssi} id={tooltipId} />
      )}
    </div>
  )
}

type SuorituksenTilaIconProps = {
  suoritus: Suoritus
}

const SuorituksenTilaIcon: React.FC<SuorituksenTilaIconProps> = ({
  suoritus
}) =>
  isValinnanMahdollisuus(suoritus) ? null : suoritusValmis(suoritus) ? (
    // eslint-disable-next-line react/jsx-no-literals
    <div title={t('Suoritus valmis')}>&#61452;</div>
  ) : (
    // eslint-disable-next-line react/jsx-no-literals
    <div title={t('Suoritus kesken')}>&#62034;</div>
  )

type TooltipXPosition = 'left' | 'right' | 'middle'
type TooltipYPosition = 'top' | 'bottom'

type OppiaineTooltipProps = {
  id: string
  oppiaine: Oppiaine
}

const OppiaineDetails: React.FC<OppiaineTooltipProps> = ({ oppiaine, id }) => {
  const koulutus = oppiaine.koulutusmoduuli
  const fields: string[][] = [
    isIBOppiaineExtendedEssay(koulutus)
      ? ['Aine', t(koulutus.aine.tunniste.nimi)]
      : null,
    isIBOppiaineExtendedEssay(koulutus) ? ['Aihe', t(koulutus.aihe)] : null,
    isIBOppiaineLanguage(koulutus) ? ['Kieli', t(koulutus.kieli.nimi)] : null,
    isIBOppiaineCAS(koulutus)
      ? ['Laajuus', formatLaajuus(koulutus.laajuus)]
      : null,
    isValinnaisuus(koulutus)
      ? ['Pakollinen', t(koulutus.pakollinen ? 'Kyllä' : 'Ei')]
      : null,
    isIBTaso(koulutus) && koulutus.taso
      ? ['Taso', t(koulutus.taso.nimi)]
      : null,
    isIBAineRyhmäOppiaine(koulutus) ? ['Ryhmä', t(koulutus.ryhmä.nimi)] : null
  ].filter(nonNull)

  return A.isEmpty(fields) ? null : (
    <Details id={id}>
      <KeyValueTable>
        {fields.map(([label, value]) => (
          <KeyValueRow localizableLabel={label}>{value}</KeyValueRow>
        ))}
      </KeyValueTable>
    </Details>
  )
}

type KurssiTooltipProps = {
  id: string
  kurssi: OsasuoritusOf<Oppiaine>
}

const KurssiDetails: React.FC<KurssiTooltipProps> = ({ kurssi, id }) => (
  <Details id={id}>
    <KeyValueTable>
      <KeyValueRow localizableLabel="Nimi">
        {t(kurssi.koulutusmoduuli.tunniste.nimi)}
      </KeyValueRow>

      {isKuvauksellinen(kurssi.koulutusmoduuli) && (
        <KeyValueRow localizableLabel="Kuvaus">
          {t(kurssi.koulutusmoduuli.kuvaus)}
        </KeyValueRow>
      )}

      <KeyValueRow localizableLabel="Laajuus">
        {kurssi.koulutusmoduuli.laajuus?.arvo}{' '}
        {t(kurssi.koulutusmoduuli.laajuus?.yksikkö.nimi)}
      </KeyValueRow>

      <KeyValueRow localizableLabel="Kurssin tyyppi">
        {!isValinnaisuus(kurssi.koulutusmoduuli) ||
        kurssi.koulutusmoduuli.pakollinen
          ? 'Pakollinen'
          : 'Valinnainen'}
      </KeyValueRow>

      <KeyValueRow localizableLabel="Suorituskieli">
        {t(kurssi.suorituskieli?.nimi)}
      </KeyValueRow>

      {kurssi.arviointi && (
        <KeyValueRow localizableLabel="Arviointi">
          {kurssi.arviointi.map((arviointi, index) => (
            <KeyValueTable key={index}>
              <KeyValueRow localizableLabel="Arvosana" innerKeyValueTable>
                {`${arviointi.arvosana.koodiarvo} (${t(arviointi.arvosana.nimi)})`}
              </KeyValueRow>
              <KeyValueRow localizableLabel="Arviointipäivä" innerKeyValueTable>
                {ISO2FinnishDate(arviointi.päivä)}
              </KeyValueRow>
            </KeyValueTable>
          ))}
        </KeyValueRow>
      )}

      {isLukionKurssinSuoritus2015(kurssi) && (
        <>
          {kurssi.tunnustettu && (
            <KeyValueRow localizableLabel="Tunnustettu">
              {t(kurssi.tunnustettu.selite)}
            </KeyValueRow>
          )}

          <KeyValueRow localizableLabel="Lisätiedot">
            {pipe(
              [
                kurssi.suoritettuLukiodiplomina &&
                  t('Suoritettu lukiodiplomina'),
                kurssi.suoritettuSuullisenaKielikokeena &&
                  t('Suoritettu suullisena kielikokeena')
              ],
              A.filter(nonFalsy),
              NonEmptyArray.fromArray,
              O.map((texts) => (
                <ul>
                  {texts.map((text, i) => (
                    <li key={i}>{text}</li>
                  ))}
                </ul>
              )),
              O.toNullable
            )}
          </KeyValueRow>
        </>
      )}
    </KeyValueTable>
  </Details>
)

const Details: React.FC<React.PropsWithChildren<{ id: string }>> = ({
  id,
  children
}) => {
  const [xPos, setXPos] = useState<TooltipXPosition>()
  const [yPos, setYPos] = useState<TooltipYPosition>()
  const self = useRef<HTMLDivElement>(null)

  useEffect(() => {
    const updatePosition = () => {
      if (self.current) {
        const rect = self.current.getBoundingClientRect()
        setXPos(
          rect.x < window.innerWidth / 3
            ? 'left'
            : rect.x > (window.innerWidth * 2) / 3
              ? 'right'
              : 'middle'
        )
        setYPos(rect.y > window.innerHeight / 2 ? 'top' : 'bottom')
      }
    }

    updatePosition()
    document.addEventListener('scroll', updatePosition)
    document.body.addEventListener('resize', updatePosition)
    return () => {
      document.removeEventListener('scroll', updatePosition)
      document.body.removeEventListener('resize', updatePosition)
    }
  }, [])

  return (
    <div ref={self}>
      {xPos && yPos && (
        <aside
          className={`KurssiDetails KurssiDetails-${xPos}-${yPos}`}
          role="tooltip"
          id={id}
        >
          {children}
        </aside>
      )}
    </div>
  )
}

export const isKuvauksellinen = (
  koulutusmoduuli: KoulutusmoduuliOf<OsasuoritusOf<Oppiaine>>
): koulutusmoduuli is Extract<
  KoulutusmoduuliOf<OsasuoritusOf<Oppiaine>>,
  { kuvaus: LocalizedString }
> =>
  isIBKurssi(koulutusmoduuli) ||
  isPaikallinenLukionKurssi2015(koulutusmoduuli) ||
  isLukionPaikallinenOpintojakso2019(koulutusmoduuli)

const useLaajuusYhteensä = (oppiaine: Oppiaine) =>
  useMemo(() => {
    if (isIBOppiaineCAS(oppiaine.koulutusmoduuli)) {
      return oppiaine.koulutusmoduuli.laajuus?.arvo
    }

    const kurssit = oppiaine.osasuoritukset || []
    return sum(kurssit.map((k) => k.koulutusmoduuli.laajuus?.arvo || 1))
  }, [oppiaine.koulutusmoduuli, oppiaine.osasuoritukset])
