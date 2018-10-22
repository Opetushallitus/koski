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

const resolveGroupingFn = päätasonSuoritusClass => {
  switch (päätasonSuoritusClass) {
    case 'ibtutkinnonsuoritus': return ibRyhmät
    case 'diatutkintovaiheensuoritus': return diaRyhmät
    default: {
      console.error(`Oppiaineiden ryhmittely ei onnistu päätason suoritukselle ${päätasonSuoritusClass}.`)
      return () => ({})
    }
  }
}

const resolveFootnotes = päätasonSuoritusClass => {
  switch (päätasonSuoritusClass) {
    case 'ibtutkinnonsuoritus': return arvosanaFootnote
    default: return null
  }
}

const useOppiaineLaajuus = päätasonSuoritusClass => päätasonSuoritusClass === 'diatutkintovaiheensuoritus'
const showArvosana = päätasonSuoritusClass => päätasonSuoritusClass === 'ibtutkinnonsuoritus'

export const RyhmiteltyOppiaineetEditor = ({suorituksetModel, päätasonSuoritusClass, additionalEditableKoulutusmoduuliProperties}) => {
  const {edit, suoritus: päätasonSuoritusModel} = suorituksetModel.context
  const oppiaineet = modelItems(suorituksetModel)

  const {aineryhmät, footnotes} = resolveGroupingFn(päätasonSuoritusClass)(oppiaineet, päätasonSuoritusModel, edit)

  return aineryhmät ? (
    <div>
      <table className='suoritukset oppiaineet'>
        <LukionOppiaineetTableHead
          laajuusyksikkö='vuosiviikkotuntia'
        />
        <tbody>
        {
          aineryhmät.map(ryhmät => ryhmät.map(r => [
            <tr className='aineryhmä' key={r.ryhmä.koodiarvo}>
              <th colSpan='4'>{t(r.ryhmä.nimi)}</th>
            </tr>,
            r.aineet && r.aineet.map((oppiaine, oppiaineIndex) => {
              const footnote = resolveFootnotes(päätasonSuoritusClass)
              return (
                <LukionOppiaineEditor
                  key={oppiaineIndex}
                  oppiaine={oppiaine}
                  footnote={footnote}
                  additionalEditableKoulutusmoduuliProperties={additionalEditableKoulutusmoduuliProperties}
                  useOppiaineLaajuus={useOppiaineLaajuus(päätasonSuoritusClass)}
                  showArvosana={showArvosana(päätasonSuoritusClass)}
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

export const OmatTiedotRyhmiteltyOppiaineet = ({suorituksetModel, päätasonSuoritusClass}) => {
  const {suoritus: päätasonSuoritusModel} = suorituksetModel.context
  const oppiaineet = modelItems(suorituksetModel)

  const {aineryhmät, footnotes} = resolveGroupingFn(päätasonSuoritusClass)(oppiaineet, päätasonSuoritusModel)

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
