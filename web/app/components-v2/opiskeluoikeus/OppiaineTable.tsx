import React, { useCallback, useEffect, useRef, useState } from 'react'
import { ISO2FinnishDate, todayISODate } from '../../date/date'
import { t } from '../../i18n/i18n'
import { Arviointi } from '../../types/fi/oph/koski/schema/Arviointi'
import { IBOpiskeluoikeus } from '../../types/fi/oph/koski/schema/IBOpiskeluoikeus'
import { IBPäätasonSuoritus } from '../../types/fi/oph/koski/schema/IBPaatasonSuoritus'
import { Koodistokoodiviite } from '../../types/fi/oph/koski/schema/Koodistokoodiviite'
import {
  isLukionArviointi,
  LukionArviointi
} from '../../types/fi/oph/koski/schema/LukionArviointi'
import { isMuidenLukioOpintojenPreIBSuoritus2019 } from '../../types/fi/oph/koski/schema/MuidenLukioOpintojenPreIBSuoritus2019'
import { NumeerinenLukionArviointi } from '../../types/fi/oph/koski/schema/NumeerinenLukionArviointi'
import { SanallinenLukionArviointi } from '../../types/fi/oph/koski/schema/SanallinenLukionArviointi'
import { Suoritus } from '../../types/fi/oph/koski/schema/Suoritus'
import { isValinnaisuus } from '../../types/fi/oph/koski/schema/Valinnaisuus'
import { isValinnanMahdollisuus } from '../../types/fi/oph/koski/schema/ValinnanMahdollisuus'
import { parasArviointi, viimeisinArviointi } from '../../util/arvioinnit'
import { sum } from '../../util/numbers'
import { KoulutusmoduuliOf, OsasuoritusOf } from '../../util/schema'
import { suoritusValmis } from '../../util/suoritus'
import { useBooleanState } from '../../util/useBooleanState'
import { notUndefined } from '../../util/util'
import { KeyValueRow, KeyValueTable } from '../containers/KeyValueTable'
import { FlatButton } from '../controls/FlatButton'
import { IconButton } from '../controls/IconButton'
import { FormModel } from '../forms/FormModel'
import { CHARCODE_REMOVE } from '../texts/Icon'
import { ArvosanaEdit } from './ArvosanaField'
import { KoodistokoodiviiteKoodistonKoodiarvollaOrd } from '../../appstate/koodisto'

// Vain OppiaineTablen tukemat päätason suoritukset (tätä komponenttia tullaan myöhemmin käyttämään ainakin lukion näkymille)
export type OppiainePäätasonSuoritus = IBPäätasonSuoritus

export type OppiaineOsasuoritus = OsasuoritusOf<OppiainePäätasonSuoritus>

export type OppiaineTableProps<T> = {
  form: FormModel<IBOpiskeluoikeus>
  suoritus: OppiainePäätasonSuoritus
  onDelete: (index: number) => void
  addOsasuoritusDialog: AddOppiaineenOsasuoritusDialog<T>
  onAddOsasuoritus: (oppiaineIndex: number, osasuoritus: T) => void
  onArviointi: (
    oppiaineIndex: number,
    osasuoritusIndex: number,
    arviointi: LukionArviointi
  ) => void
}

export const OppiaineTable = <T,>({
  suoritus,
  form,
  onDelete,
  addOsasuoritusDialog,
  onAddOsasuoritus,
  onArviointi
}: OppiaineTableProps<T>) => {
  const oppiaineet = suoritus.osasuoritukset || []

  return oppiaineet.length === 0 ? null : (
    <table className="OppiaineTable">
      <thead>
        <tr>
          <th></th>
          <th className="OppiaineTable__oppiaine">{t('Oppiaine')}</th>
          <th className="OppiaineTable__laajuus">{t('Laajuus (kurssia)')}</th>
          <th className="OppiaineTable__arvosana">{t('Arvosana')}</th>
          {form.editMode && <th className="OppiaineTable__poisto" />}
        </tr>
      </thead>
      <tbody>
        {oppiaineet.map((oppiaine, i) => (
          <OppiaineRow
            key={i}
            oppiaine={oppiaine}
            form={form}
            onDelete={() => onDelete(i)}
            addOsasuoritusDialog={addOsasuoritusDialog}
            onAddOsasuoritus={(osasuoritus) => onAddOsasuoritus(i, osasuoritus)}
            onArviointi={(osasuoritusIndex, arviointi) =>
              onArviointi(i, osasuoritusIndex, arviointi)
            }
          />
        ))}
      </tbody>
    </table>
  )
}

export type OppiaineRowProps<T> = {
  form: FormModel<IBOpiskeluoikeus>
  oppiaine: OppiaineOsasuoritus
  addOsasuoritusDialog: AddOppiaineenOsasuoritusDialog<T>
  onAddOsasuoritus: (t: T) => void
  onArviointi: (osasuoritusIndex: number, arviointi: LukionArviointi) => void
  onDelete: () => void
}

export type AddOppiaineenOsasuoritusDialog<T> = React.FC<{
  oppiaine: OppiaineOsasuoritus
  onAdd: (t: T) => void
  onClose: () => void
}>

const OppiaineRow = <T,>({
  oppiaine,
  form,
  onDelete,
  addOsasuoritusDialog,
  onAddOsasuoritus,
  onArviointi
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
        <div className="OppiaineRow__kurssit">
          {kurssit.map((kurssi, index) => (
            <Kurssi
              key={index}
              kurssi={kurssi}
              oppiaine={oppiaine}
              editMode={form.editMode}
              onArviointi={(a) => a && onArviointi(index, a)}
            />
          ))}
          {form.editMode && (
            <FlatButton onClick={showAddOsasuoritusDialog}>
              {t('Lisää osasuoritus')}
            </FlatButton>
          )}
        </div>
      </td>
      <td className="OppiaineRow__laajuus">{kurssejaYhteensä}</td>
      <td className="OppiaineRow__arvosana">{oppiaineenArvosana(oppiaine)}</td>
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

const oppiaineenNimi = (
  koulutusmoduuli: KoulutusmoduuliOf<OppiaineOsasuoritus>
) =>
  [
    koulutusmoduuli.tunniste.nimi,
    (koulutusmoduuli as any)?.kieli?.nimi,
    (koulutusmoduuli as any)?.oppimäärä?.nimi
  ]
    .filter(notUndefined)
    .map((s) => t(s))
    .join(', ')

const oppiaineenArvosana = (oppiaine: OppiaineOsasuoritus) =>
  isMuidenLukioOpintojenPreIBSuoritus2019(oppiaine) || !oppiaine.arviointi
    ? null
    : parasArviointi(oppiaine.arviointi as Arviointi[])?.arvosana.koodiarvo

type KurssiProps = {
  editMode?: boolean
  oppiaine: OppiaineOsasuoritus
  kurssi: OsasuoritusOf<OppiaineOsasuoritus>
  onArviointi: (arviointi?: LukionArviointi) => void
}

const Kurssi: React.FC<KurssiProps> = ({
  kurssi,
  oppiaine,
  editMode,
  onArviointi
}) => {
  const [tooltipVisible, openTooltip, closeTooltip] = useBooleanState(false)
  const tooltipId = `kurssi-${oppiaine.koulutusmoduuli.tunniste.koodiarvo}-${kurssi.koulutusmoduuli.tunniste.koodiarvo}`
  const arviointi =
    kurssi.arviointi && viimeisinArviointi([...kurssi.arviointi])

  return (
    <div className="Kurssi">
      <div
        className="Kurssi__tunniste"
        onClick={openTooltip}
        onTouchStart={openTooltip}
        onMouseEnter={openTooltip}
        onMouseLeave={closeTooltip}
        aria-describedby={tooltipId}
      >
        {kurssi.koulutusmoduuli.tunniste.koodiarvo}
      </div>
      <div className="Kurssi__arvosana">
        {editMode && isLukionArviointi(arviointi) ? (
          <ArvosanaEdit
            value={arviointi}
            onChange={onArviointi}
            createArviointi={createLukioArviointi}
            arviointiClass="fi.oph.koski.schema.LukionArviointi" // Luokka annettava erikseen, jotta saadaan sekä numeraaliset että sanalliset arvioinnit
            format={(k) => k.koodiarvo}
          />
        ) : kurssi.arviointi ? (
          parasArviointi(kurssi.arviointi as Arviointi[])?.arvosana.koodiarvo
        ) : (
          '-'
        )}
      </div>
      {tooltipVisible && <KurssiDetails kurssi={kurssi} id={tooltipId} />}
    </div>
  )
}

const createLukioArviointi = (
  arvosana: Koodistokoodiviite<'arviointiasteikkoyleissivistava'>
): LukionArviointi => {
  if (['H', 'S', 'O'].includes(arvosana?.koodiarvo)) {
    return SanallinenLukionArviointi({
      arvosana: arvosana as any,
      päivä: todayISODate()
    })
  } else {
    return NumeerinenLukionArviointi({
      arvosana: arvosana as any,
      päivä: todayISODate()
    })
  }
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
  kurssi: OsasuoritusOf<OppiaineOsasuoritus>
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
          </KeyValueTable>
        </aside>
      )}
    </div>
  )
}
