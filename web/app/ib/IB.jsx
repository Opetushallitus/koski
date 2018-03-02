import React from 'baret'
import R from 'ramda'
import Bacon from 'baconjs'
import {LukionOppiaineEditor} from '../lukio/LukionOppiaineEditor'
import {LukionOppiaineetTableHead} from '../lukio/fragments/LukionOppiaineetTableHead'
import {modelData, modelItems} from '../editor/EditorModel'
import {FootnoteDescriptions} from '../components/footnote'
import {UusiIBOppiaineDropdown} from './UusiIBOppiaineDropdown'
import {koodistoValues} from '../uusioppija/koodisto'
import {t} from '../i18n/i18n'

const ArvosanaFootnote = {title: 'Ennustettu arvosana', hint: '*'}

export const IBTutkinnonOppiaineetEditor = ({suorituksetModel}) => {
  const {edit, suoritus: päätasonSuoritusModel} = suorituksetModel.context
  const oppiaineet = modelItems(suorituksetModel)

  const oppiaineetAineryhmittäin = Bacon.constant(R.compose(
    R.map(aineet => ({aineet})),
    R.groupBy(oppiaine => modelData(oppiaine, 'koulutusmoduuli.ryhmä').koodiarvo)
  )(oppiaineet))

  const aineryhmäKoodistoArvot = koodistoValues('aineryhmaib')
    .map(ryhmät => ryhmät.reduce((obj, r) => R.assoc(r.koodiarvo, {ryhmä: r}, obj), {}))

  const aineryhmät = Bacon.combineWith(oppiaineetAineryhmittäin, aineryhmäKoodistoArvot,
    (aineet, ryhmät) => Object.values(R.mergeDeepLeft(aineet, ryhmät))
      .filter(edit ? R.identity : r => r.aineet)
  )

  const footnotes = R.any(s => modelData(s, 'arviointi.-1.predicted'), oppiaineet)
    ? [ArvosanaFootnote]
    : []

  return (
    <div>
      <table className='suoritukset oppiaineet'>
        <LukionOppiaineetTableHead />
        <tbody>
        {
          aineryhmät.map(ryhmät => ryhmät.map(r => [
            <tr className='aineryhmä' key={r.ryhmä.koodiarvo}>
              <th colSpan='4'>{t(r.ryhmä.nimi)}</th>
            </tr>,
            r.aineet && r.aineet.map((oppiaine, oppiaineIndex) => {
              const footnote = modelData(oppiaine, 'arviointi.-1.predicted') && ArvosanaFootnote
              return <LukionOppiaineEditor key={oppiaineIndex} oppiaine={oppiaine} footnote={footnote} />
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
  )
}
