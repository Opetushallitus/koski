import { string } from 'fp-ts'
import * as A from 'fp-ts/Array'
import { pipe } from 'fp-ts/lib/function'
import * as NonEmptyArray from 'fp-ts/NonEmptyArray'
import * as O from 'fp-ts/Option'
import React, { useCallback, useEffect, useRef, useState } from 'react'
import { ISO2FinnishDate } from '../../date/date'
import { t } from '../../i18n/i18n'
import { isArvioinniton } from '../../types/fi/oph/koski/schema/Arvioinniton'
import { Arviointi } from '../../types/fi/oph/koski/schema/Arviointi'
import { IBOpiskeluoikeus } from '../../types/fi/oph/koski/schema/IBOpiskeluoikeus'
import { isIBOppiaineenSuoritus } from '../../types/fi/oph/koski/schema/IBOppiaineenSuoritus'
import { IBTheoryOfKnowledgeSuoritus } from '../../types/fi/oph/koski/schema/IBTheoryOfKnowledgeSuoritus'
import { IBTutkinnonSuoritus } from '../../types/fi/oph/koski/schema/IBTutkinnonSuoritus'
import { LukionArviointi } from '../../types/fi/oph/koski/schema/LukionArviointi'
import { isLukionKurssinSuoritus2015 } from '../../types/fi/oph/koski/schema/LukionKurssinSuoritus2015'
import { MuidenLukioOpintojenPreIBSuoritus2019 } from '../../types/fi/oph/koski/schema/MuidenLukioOpintojenPreIBSuoritus2019'
import { Suoritus } from '../../types/fi/oph/koski/schema/Suoritus'
import { isValinnaisuus } from '../../types/fi/oph/koski/schema/Valinnaisuus'
import { isPaikallinenKoodi } from '../../types/fi/oph/koski/schema/PaikallinenKoodi'
import { PreIBSuoritus2019 } from '../../types/fi/oph/koski/schema/PreIBSuoritus2019'
import { isValinnanMahdollisuus } from '../../types/fi/oph/koski/schema/ValinnanMahdollisuus'
import { appendOptional, deleteAt } from '../../util/array'
import { parasArviointi, viimeisinArviointi } from '../../util/arvioinnit'
import { nonFalsy } from '../../util/fp/arrays'
import { PathToken } from '../../util/laxModify'
import { sum } from '../../util/numbers'
import { PäätasonSuoritusOf } from '../../util/opiskeluoikeus'
import { KoulutusmoduuliOf, OsasuoritusOf } from '../../util/schema'
import { suoritusValmis } from '../../util/suoritus'
import { match } from '../../util/patternmatch'
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
}

export const OppiaineTable = <T extends OppiaineTablePäätasonSuoritus>({
  form,
  selectedSuoritus,
  addOsasuoritusDialog
}: OppiaineTableProps<T>) => {
  const suoritus = getValue(selectedSuoritus.path)(form.state)
  const path = selectedSuoritus.pathTokens
  const oppiaineet = suoritus?.osasuoritukset || []
  const organisaatioOid = form.state.oppilaitos?.oid

  const showPredictedGrade =
    selectedSuoritus.suoritus.$class === IBTutkinnonSuoritus.className

  const oppiainePath = (index: number) => [...path, 'osasuoritukset', index]

  const deleteOppiaine = (index: number) => () =>
    form.modify(
      ...path,
      'osasuoritukset'
    )((os: Oppiaine[]) => deleteAt(index)(os))

  const deleteKurssi = (oppiaineIndex: number) => (kurssiIndex: number) =>
    form.modify(
      ...oppiainePath(oppiaineIndex),
      'osasuoritukset'
    )((os: OppiaineenOsasuoritus[]) => deleteAt(kurssiIndex)(os))

  const addOsasuoritus =
    (oppiaineIndex: number) => (osasuoritus: OppiaineenOsasuoritus) =>
      form.modify(
        ...oppiainePath(oppiaineIndex),
        'osasuoritukset'
      )(appendOptional(osasuoritus))

  const addKurssiArviointi =
    (oppiaineIndex: number) =>
    (osasuoritusIndex: number, arviointi: Arviointi) =>
      form.modify(
        ...oppiainePath(oppiaineIndex),
        'osasuoritukset',
        osasuoritusIndex,
        'arviointi'
      )(appendOptional(arviointi))

  const addOppiaineArviointi =
    (oppiaineIndex: number) => (arviointi: Arviointi) => {
      form.modify(
        ...oppiainePath(oppiaineIndex),
        'arviointi'
      )(appendOptional(arviointi))
    }

  const addPredictedGrade =
    (oppiaineIndex: number) => (arviointi: Arviointi) => {
      form.debug.modify(
        ...oppiainePath(oppiaineIndex),
        'predictedArviointi'
      )(appendOptional(arviointi))
    }

  return oppiaineet.length === 0 && organisaatioOid ? null : (
    <table className="OppiaineTable">
      <thead>
        <tr>
          <th></th>
          <th className="OppiaineTable__oppiaine">{t('Oppiaine')}</th>
          <th className="OppiaineTable__laajuus">{t('Laajuus (kurssia)')}</th>
          {showPredictedGrade && (
            <th className="OppiaineTable__predictedGrade">
              {t('Predicted grade')}
            </th>
          )}
          <th className="OppiaineTable__arvosana">{t('Arvosana')}</th>
          {form.editMode && <th className="OppiaineTable__poisto" />}
        </tr>
      </thead>
      <tbody>
        {oppiaineet.map((oppiaine, oppiaineIndex) => (
          <OppiaineRow
            key={oppiaineIndex}
            organisaatioOid={organisaatioOid!}
            oppiaine={oppiaine}
            form={form}
            showPredictedGrade={showPredictedGrade}
            oppiainePath={[
              ...selectedSuoritus.pathTokens,
              'osasuoritukset',
              oppiaineIndex
            ]}
            onDelete={deleteOppiaine(oppiaineIndex)}
            onDeleteKurssi={deleteKurssi(oppiaineIndex)}
            addOsasuoritusDialog={addOsasuoritusDialog}
            onAddOsasuoritus={addOsasuoritus(oppiaineIndex)}
            onArviointi={addKurssiArviointi(oppiaineIndex)}
            onOppiaineArviointi={addOppiaineArviointi(oppiaineIndex)}
            onPredictedGrade={addPredictedGrade(oppiaineIndex)}
          />
        ))}
      </tbody>
    </table>
  )
}

export type OppiaineRowProps<T> = {
  form: FormModel<OppiaineTableOpiskeluoikeus>
  oppiainePath: PathToken[]
  organisaatioOid: string
  oppiaine: Oppiaine
  showPredictedGrade: boolean
  addOsasuoritusDialog: AddOppiaineenOsasuoritusDialog<T>
  onAddOsasuoritus: (t: T) => void
  onArviointi: (osasuoritusIndex: number, arviointi: Arviointi) => void
  onOppiaineArviointi: (arviointi: Arviointi) => void
  onPredictedGrade: (arviointi: Arviointi) => void
  onDelete: () => void
  onDeleteKurssi: (index: number) => void
}

export type AddOppiaineenOsasuoritusDialog<T> = React.FC<{
  organisaatioOid: string
  oppiaine: Oppiaine
  onAdd: (t: T) => void
  onClose: () => void
}>

const OppiaineRow = <T,>({
  organisaatioOid,
  oppiaine,
  oppiainePath,
  form,
  showPredictedGrade,
  onDelete,
  addOsasuoritusDialog,
  onAddOsasuoritus,
  onArviointi,
  onPredictedGrade,
  onOppiaineArviointi,
  onDeleteKurssi
}: OppiaineRowProps<T>) => {
  const kurssit = oppiaine.osasuoritukset || []
  const kurssejaYhteensä = sum(
    kurssit.map((k) => k.koulutusmoduuli.laajuus?.arvo || 0)
  )
  const [
    addOsasuoritusDialogVisible,
    showAddOsasuoritusDialog,
    hideAddOsasuoritusDialog
  ] = useBooleanState(false)

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
          {oppiaineenNimi(oppiaine.koulutusmoduuli)}
        </div>
        <OppiaineenKurssit
          form={form}
          kurssit={kurssit}
          oppiaine={oppiaine}
          oppiainePath={oppiainePath}
          onArviointi={onArviointi}
          onDeleteKurssi={onDeleteKurssi}
          onShowAddOsasuoritusDialog={showAddOsasuoritusDialog}
        />
      </td>
      <td className="OppiaineRow__laajuus">{kurssejaYhteensä}</td>
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
              organisaatioOid={organisaatioOid}
              oppiaine={oppiaine}
              onAdd={addOsasuoritus}
              onClose={hideAddOsasuoritusDialog}
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
  onArviointi: (osasuoritusIndex: number, arviointi: Arviointi) => void
  onDeleteKurssi: (index: number) => void
  onShowAddOsasuoritusDialog: () => void
}

export const OppiaineenKurssit = ({
  form,
  kurssit,
  oppiaine,
  oppiainePath,
  onArviointi,
  onDeleteKurssi,
  onShowAddOsasuoritusDialog
}: OppiaineenKurssitProps) => (
  <div className="OppiaineRow__kurssit">
    {kurssit.map((kurssi, index) => (
      <Kurssi
        key={index}
        form={form}
        kurssi={kurssi}
        kurssiPath={[...oppiainePath, 'osasuoritukset', index]}
        oppiaine={oppiaine}
        onArviointi={(a) => a && onArviointi(index, a)}
        onDelete={() => onDeleteKurssi(index)}
      />
    ))}
    {form.editMode && (
      <FlatButton onClick={onShowAddOsasuoritusDialog}>
        {t('Lisää osasuoritus')}
      </FlatButton>
    )}
  </div>
)

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
    <span>
      {arvioinnit ? parasArviointi(arvioinnit)?.arvosana.koodiarvo : '-'}
    </span>
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
    />
  ) : (
    <span>
      {arvioinnit ? parasArviointi(arvioinnit)?.arvosana.koodiarvo : '-'}
    </span>
  )
}

const oppiaineenNimi = (koulutusmoduuli: KoulutusmoduuliOf<Oppiaine>) =>
  [
    koulutusmoduuli.tunniste.nimi,
    (koulutusmoduuli as any)?.kieli?.nimi,
    (koulutusmoduuli as any)?.oppimäärä?.nimi
  ]
    .filter(notUndefined)
    .map((s) => t(s))
    .join(', ')

type KurssiProps = {
  form: FormModel<OppiaineTableOpiskeluoikeus>
  oppiaine: Oppiaine
  kurssi: OsasuoritusOf<Oppiaine>
  kurssiPath: PathToken[]
  onArviointi: (arviointi?: LukionArviointi) => void
  onDelete: () => void
}

export const Kurssi: React.FC<KurssiProps> = ({
  form,
  kurssi,
  kurssiPath,
  oppiaine,
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
        {kurssi.koulutusmoduuli.tunniste.koodiarvo}
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
        ) : kurssi.arviointi ? (
          parasArviointi(kurssi.arviointi as Arviointi[])?.arvosana.koodiarvo
        ) : (
          '-'
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

type KurssiTooltipProps = {
  id: string
  kurssi: OsasuoritusOf<Oppiaine>
}

const KurssiDetails: React.FC<KurssiTooltipProps> = ({ kurssi, id }) => {
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
          <KeyValueTable>
            <KeyValueRow localizableLabel="Nimi">
              {t(kurssi.koulutusmoduuli.tunniste.nimi)}
            </KeyValueRow>

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
                    <KeyValueRow
                      localizableLabel="Arviointipäivä"
                      innerKeyValueTable
                    >
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
        </aside>
      )}
    </div>
  )
}
