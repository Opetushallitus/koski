import {addContext, modelData, modelItems, modelLookup} from './EditorModel'
import React from 'baret'
import {PropertyEditor} from './PropertyEditor.jsx'
import {PropertiesEditor} from './PropertiesEditor.jsx'
import * as Lukio from './Lukio.jsx'
import {Suoritustaulukko} from './Suoritustaulukko.jsx'
import {LuvaEditor} from './LuvaEditor.jsx'
import {PerusopetuksenOppiaineetEditor} from './PerusopetuksenOppiaineetEditor.jsx'
import {sortLanguages} from '../sorting'
import {Editor} from './Editor.jsx'
import {TilaJaVahvistusEditor} from './TilaJaVahvistusEditor.jsx'
import {arviointiPuuttuu, suoritusValmis} from './Suoritus'
import Text from '../Text.jsx'

const resolveEditor = (mdl) => {
  if (['perusopetuksenvuosiluokansuoritus', 'perusopetuksenoppimaaransuoritus', 'perusopetuksenlisaopetuksensuoritus', 'perusopetukseenvalmistavanopetuksensuoritus'].includes(mdl.value.classes[0])) {
    return <PerusopetuksenOppiaineetEditor model={mdl}/>
  }
  if (mdl.value.classes.includes('ammatillinenpaatasonsuoritus')) {
    return <Suoritustaulukko suorituksetModel={modelLookup(mdl, 'osasuoritukset')}/>
  }
  if (mdl.value.classes.includes('lukionoppimaaransuoritus')) {
    return <Lukio.LukionOppiaineetEditor oppiaineet={modelItems(mdl, 'osasuoritukset') || []} />
  }
  if (mdl.value.classes.includes('lukionoppiaineenoppimaaransuoritus')) {
    return <Lukio.LukionOppiaineetEditor oppiaineet={[mdl]} />
  }
  if (mdl.value.classes.includes('lukioonvalmistavankoulutuksensuoritus')) {
    return <LuvaEditor suoritukset={modelItems(mdl, 'osasuoritukset') || []}/>
  }
  return <PropertyEditor model={mdl} propertyName="osasuoritukset"/>
}

export class SuoritusEditor extends React.Component {
  render() {
    const excludedProperties = ['osasuoritukset', 'käyttäytymisenArvio', 'tila', 'vahvistus', 'jääLuokalle', 'pakollinen']

    let {model} = this.props
    model = addContext(model, { suoritus: model, toimipiste: modelLookup(model, 'toimipiste')})
    const editor = resolveEditor(model)

    let className = 'suoritus ' + model.value.classes.join(' ')

    return (<div className={className}>
      <TodistusLink suoritus={model} />
      <PropertiesEditor
        model={model}
        propertyFilter={p => !excludedProperties.includes(p.key)}
        getValueEditor={ (prop, getDefault) => prop.key === 'suorituskieli'
          ? <Editor model={modelLookup(model, 'suorituskieli')} sortBy={sortLanguages}/>
          : getDefault() }
      />
      <TilaJaVahvistusEditor model={model} />
      <div className="osasuoritukset">{editor}</div>
    </div>)
  }

  shouldComponentUpdate(nextProps) {
    return Editor.shouldComponentUpdate.call(this, nextProps)
  }
}

SuoritusEditor.validateModel = (m) => {
  if (suoritusValmis(m) && arviointiPuuttuu(m)) {
    return [{key: 'missing', message: <Text name='Suoritus valmis, mutta arvosana puuttuu'/>}]
  }
}

class TodistusLink extends React.Component {
  render() {
    let {suoritus} = this.props
    let oppijaOid = suoritus.context.oppijaOid
    let suoritustyyppi = modelData(suoritus, 'tyyppi').koodiarvo
    let koulutusmoduuliKoodistoUri = modelData(suoritus, 'koulutusmoduuli').tunniste.koodistoUri
    let koulutusmoduuliKoodiarvo = modelData(suoritus, 'koulutusmoduuli').tunniste.koodiarvo
    let suoritusTila = modelData(suoritus, 'tila').koodiarvo
    let href = '/koski/todistus/' + oppijaOid + '?suoritustyyppi=' + suoritustyyppi + '&koulutusmoduuli=' + koulutusmoduuliKoodistoUri + '/' + koulutusmoduuliKoodiarvo
    return suoritusTila == 'VALMIS' && suoritustyyppi != 'korkeakoulututkinto' && suoritustyyppi != 'preiboppimaara' && suoritustyyppi != 'esiopetuksensuoritus' && !(koulutusmoduuliKoodistoUri == 'perusopetuksenluokkaaste' && koulutusmoduuliKoodiarvo == '9')
      ? <a className="todistus" href={href}><Text name="näytä todistus"/></a>
      : null
  }
}