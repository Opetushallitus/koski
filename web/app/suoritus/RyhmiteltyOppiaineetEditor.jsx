import React from 'baret'
import * as R from 'ramda'
import {modelData, modelItems} from '../editor/EditorModel'
import {t} from '../i18n/i18n'
import {isMobileAtom} from '../util/isMobileAtom'
import {
  LukionOppiaineetTableHead,
  OmatTiedotLukionOppiaineetTableHead
} from '../lukio/fragments/LukionOppiaineetTableHead'
import {LukionOppiaineEditor} from '../lukio/LukionOppiaineEditor'
import {arvosanaFootnote, ibRyhmät} from '../ib/IB'
import {UusiIBOppiaineDropdown} from '../ib/UusiIBOppiaineDropdown'
import {FootnoteDescriptions} from '../components/footnote'
import {OmatTiedotLukionOppiaine} from '../lukio/OmatTiedotLukionOppiaineet'
import {diaRyhmät} from '../dia/DIA'

const resolveGroupingFn = päätasonSuorituksenTyyppi => {
  switch (päätasonSuorituksenTyyppi) {
    case 'ibtutkinto':
      return ibRyhmät
    case 'diavalmistavavaihe':
    case 'diatutkintovaihe':
      return diaRyhmät
    default: {
      console.error(`Oppiaineiden ryhmittely ei onnistu päätason suoritukselle ${päätasonSuorituksenTyyppi}.`)
      return () => ({})
    }
  }
}

const resolveLaajuusyksikkö = päätasonSuorituksenTyyppi => {
  switch (päätasonSuorituksenTyyppi) {
    case 'diavalmistavavaihe':
    case 'diatutkintovaihe':
      return 'vuosiviikkotuntia'
    default:
      return 'kurssia'
  }
}

const resolveFootnotes = päätasonSuorituksenTyyppi => {
  switch (päätasonSuorituksenTyyppi) {
    case 'ibtutkinto': return arvosanaFootnote
    default: return null
  }
}

const useOppiaineLaajuus = päätasonSuorituksenTyyppi => {
  switch (päätasonSuorituksenTyyppi) {
    case 'diavalmistavavaihe':
    case 'diatutkintovaihe':
      return true
    default:
      return false
  }
}
const showArvosana = päätasonSuorituksenTyyppi => päätasonSuorituksenTyyppi === 'ibtutkinto'

export const RyhmiteltyOppiaineetEditor = ({suorituksetModel, päätasonSuorituksenTyyppi, additionalEditableKoulutusmoduuliProperties}) => {
  const {edit, suoritus: päätasonSuoritusModel} = suorituksetModel.context
  const oppiaineet = modelItems(suorituksetModel)

  const {aineryhmät, footnotes} = resolveGroupingFn(päätasonSuorituksenTyyppi)(oppiaineet, päätasonSuoritusModel, edit)

  return aineryhmät ? (
    <div>
      <table className='suoritukset oppiaineet'>
        <LukionOppiaineetTableHead
          laajuusyksikkö={resolveLaajuusyksikkö(päätasonSuorituksenTyyppi)}
        />
        <tbody>
        {
          aineryhmät.map(ryhmät => ryhmät.map(r => [
            <tr className='aineryhmä' key={r.ryhmä.koodiarvo}>
              <th colSpan='4'>{t(r.ryhmä.nimi)}</th>
            </tr>,
            r.aineet && r.aineet.map((oppiaine, oppiaineIndex) => {
              const footnote = resolveFootnotes(päätasonSuorituksenTyyppi)
              return (
                <LukionOppiaineEditor
                  key={oppiaineIndex}
                  oppiaine={oppiaine}
                  footnote={footnote}
                  additionalEditableKoulutusmoduuliProperties={additionalEditableKoulutusmoduuliProperties}
                  useOppiaineLaajuus={useOppiaineLaajuus(päätasonSuorituksenTyyppi)}
                  showArvosana={showArvosana(päätasonSuorituksenTyyppi)}
                />
              )
            }),
            <tr className='uusi-oppiaine' key={`uusi-oppiaine-${r.ryhmä.koodiarvo}`}>
              <td colSpan='4'>
                <UusiIBOppiaineDropdown
                  model={päätasonSuoritusModel}
                  aineryhmä={r.ryhmä}
                />
              </td>
            </tr>
          ]))
        }
        </tbody>
      </table>
      {!R.isEmpty(footnotes) && <FootnoteDescriptions data={footnotes}/>}
    </div>
  ) : null
}

export const OmatTiedotRyhmiteltyOppiaineet = ({suorituksetModel, päätasonSuorituksenTyyppi}) => {
  const {suoritus: päätasonSuoritusModel} = suorituksetModel.context
  const oppiaineet = modelItems(suorituksetModel)

  const {aineryhmät, footnotes} = resolveGroupingFn(päätasonSuorituksenTyyppi)(oppiaineet, päätasonSuoritusModel)

  return aineryhmät ? (
    <div className='aineryhmat'>
      {
        aineryhmät.map(ryhmät => ryhmät.map(r => [
          <h4 key={r.ryhmä.koodiarvo} className='aineryhma-title'>
            {t(r.ryhmä.nimi)}
          </h4>,
          <table key={`suoritustable-${r.ryhmä.koodiarvo}`} className='omattiedot-suoritukset'>
            <OmatTiedotLukionOppiaineetTableHead />
            <tbody>
            {r.aineet && r.aineet.map((oppiaine, oppiaineIndex) => {
              const footnote = modelData(oppiaine, 'arviointi.-1.predicted') && arvosanaFootnote
              return (
                <OmatTiedotLukionOppiaine
                  baret-lift
                  key={oppiaineIndex}
                  oppiaine={oppiaine}
                  isMobile={isMobileAtom}
                  footnote={footnote}
                />
              )
            })}
            </tbody>
          </table>
        ]))
      }
      {!R.isEmpty(footnotes) && <FootnoteDescriptions data={footnotes}/>}
    </div>
  ) : null
}
