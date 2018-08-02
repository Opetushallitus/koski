import React from 'react'
import * as R from 'ramda'
import {
  footnoteDescriptions, groupTitleForSuoritus, isToimintaAlueittain,
  isVuosiluokkaTaiPerusopetuksenOppimäärä,
  isYsiluokka,
  jääLuokalle, pakollisetTitle, valinnaisetTitle,
  valmiitaSuorituksia
} from './Perusopetus'
import {modelData, modelItems, modelLookup} from '../editor/EditorModel'
import {FootnoteDescriptions} from '../components/footnote'
import Text from '../i18n/Text'
import {PropertiesEditor} from '../editor/PropertiesEditor'

export default ({model}) => {
  // Tarviiko kontekstia?   model = addContext(model, { suoritus: model })

  const oppiaineSuoritukset = modelItems(model, 'osasuoritukset')
  const footnotes = footnoteDescriptions(oppiaineSuoritukset)
  const showOppiaineet = !(isYsiluokka(model) && !jääLuokalle(model)) && (valmiitaSuorituksia(oppiaineSuoritukset) || isVuosiluokkaTaiPerusopetuksenOppimäärä(model))

  return showOppiaineet && (
    <div className='omattiedot-perusopetuksen-suoritukset'>
      <ArvosteluInfo model={model}/>
      <GroupedOppiaineet model={model} />
      <KäyttäytymisenArvio model={model}/>
      {!R.isEmpty(footnotes) && <FootnoteDescriptions data={footnotes}/>}
    </div>
  )
}

const ArvosteluInfo = ({model}) => (
  <section className='arvostelu'>
    <h4><Text name={(isToimintaAlueittain(model) ? 'Toiminta-alueiden' : 'Oppiaineiden') + ' arvosanat'} /></h4>
    <Text name='Arvostelu 4-10, S (suoritettu) tai H (hylätty)'/>
  </section>
)

const KäyttäytymisenArvio = ({model}) => {
  const käyttäytymisenArvioModel = modelLookup(model, 'käyttäytymisenArvio')
  const shouldShow = käyttäytymisenArvioModel && modelData(käyttäytymisenArvioModel)
  return shouldShow ? (
      <section className='kayttaytymisen-arvio'>
        <h4><Text name="Käyttäytymisen arviointi"/></h4>
        <PropertiesEditor model={käyttäytymisenArvioModel} className='kansalainen'/>
      </section>
    ) : null
}

const GroupedOppiaineet = ({model}) => {
  const groups = [pakollisetTitle, valinnaisetTitle]
  const groupedSuoritukset = R.groupBy(groupTitleForSuoritus, modelItems(model, 'osasuoritukset'))

  return groups.map(groupTitle => {
    const suoritukset = groupedSuoritukset[groupTitle]
    const showLaajuus = groupTitle === valinnaisetTitle
    if (!suoritukset) return null

    return (
      <section className='suoritus-group' key={groupTitle}>
        <h4><Text name={groupTitle}/></h4>
        <Oppiainetaulukko model={model} suoritukset={suoritukset} showLaajuus={showLaajuus}/>
      </section>
    )
  })
}

const Oppiainetaulukko = ({model, suoritukset, showLaajuus}) => {
  return null
}
