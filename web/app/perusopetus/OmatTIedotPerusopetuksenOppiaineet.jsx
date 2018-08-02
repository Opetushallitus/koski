import React from 'react'
import * as R from 'ramda'
import {
  footnoteDescriptions, isToimintaAlueittain,
  isVuosiluokkaTaiPerusopetuksenOppimäärä,
  isYsiluokka,
  jääLuokalle,
  valmiitaSuorituksia
} from './Perusopetus'
import {modelItems} from '../editor/EditorModel'
import {FootnoteDescriptions} from '../components/footnote'
import Text from '../i18n/Text'

export default ({model}) => {
  // Tarviiko kontekstia?   model = addContext(model, { suoritus: model })

  const oppiaineSuoritukset = modelItems(model, 'osasuoritukset')
  const footnotes = footnoteDescriptions(oppiaineSuoritukset)
  const showOppiaineet = !(isYsiluokka(model) && !jääLuokalle(model)) && (valmiitaSuorituksia(oppiaineSuoritukset) || isVuosiluokkaTaiPerusopetuksenOppimäärä(model))

  return showOppiaineet && (
    <div className='omattiedot-perusopetuksen-suoritukset'>
      <ArvosteluInfo model={model}/>
      {!R.isEmpty(footnotes) && <FootnoteDescriptions data={footnotes}/>}
    </div>
  )
}

const ArvosteluInfo = ({model}) => (
  <div className='arvostelu'>
    <h4><Text name={(isToimintaAlueittain(model) ? 'Toiminta-alueiden' : 'Oppiaineiden') + ' arvosanat'} /></h4>
    <Text name='Arvostelu 4-10, S (suoritettu) tai H (hylätty)'/>
  </div>
)
