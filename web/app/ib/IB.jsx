import React from 'react'
import R from 'ramda'
import {LukionOppiaineEditor} from '../lukio/LukionOppiaineEditor'
import {LukionOppiaineetTableHead} from '../lukio/fragments/LukionOppiaineetTable'
import {modelData, modelLookup} from '../editor/EditorModel'
import {FootnoteDescriptions} from '../components/footnote'

const ArvosanaFootnote = {title: 'Ennustettu arvosana', hint: '*'}

export const IBTutkinnonOppiaineetEditor = ({oppiaineet}) => {
  const aineryhmittäin = R.groupBy(
    oppiaine => modelData(oppiaine, 'koulutusmoduuli.ryhmä').koodiarvo,
    oppiaineet
  )

  const footnotes = [
    oppiaineet.find(s => modelLookup(s, 'arviointi.-1.predicted')) && ArvosanaFootnote
  ] || []

  return (
    <div>
      <table className='suoritukset oppiaineet'>
        <LukionOppiaineetTableHead />
        <tbody>
        {
          Object.values(aineryhmittäin).map(aineet => [
            <tr className='aineryhmä'>
              <th colSpan='4'>{modelLookup(aineet[0], 'koulutusmoduuli.ryhmä').value.title}</th>
            </tr>,
            aineet.map((oppiaine, oppiaineIndex) => {
              const footnote = modelLookup(oppiaine, 'arviointi.-1.predicted') && ArvosanaFootnote
              return <LukionOppiaineEditor key={oppiaineIndex} oppiaine={oppiaine} footnote={footnote} />
            })
          ])
        }
        </tbody>
      </table>
      {!R.isEmpty(footnotes) && <FootnoteDescriptions data={footnotes}/>}
    </div>
  )
}
