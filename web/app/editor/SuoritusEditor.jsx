import {addContext, modelData, modelItems, modelLookup, removeCommonPath} from './EditorModel'
import React from 'baret'
import {PropertyEditor} from './PropertyEditor'
import {PropertiesEditor} from './PropertiesEditor'
import * as Lukio from './Lukio'
import {Suoritustaulukko} from './Suoritustaulukko'
import {LuvaEditor} from './LuvaEditor'
import {PerusopetuksenOppiaineetEditor} from './PerusopetuksenOppiaineetEditor'
import PerusopetuksenOppiaineenOppimääränSuoritusEditor from './PerusopetuksenOppiaineenOppimaaranSuoritusEditor'
import {sortLanguages} from '../sorting'
import {Editor} from './Editor'
import {ArvosanaEditor} from './ArvosanaEditor'
import {TilaJaVahvistusEditor} from './TilaJaVahvistusEditor'
import {arviointiPuuttuu, osasuoritukset, suoritusKesken, suoritusValmis} from './Suoritus'
import Text from '../Text'

const resolveEditor = (mdl) => {
  if (['perusopetuksenvuosiluokansuoritus', 'nuortenperusopetuksenoppimaaransuoritus', 'aikuistenperusopetuksenoppimaaransuoritus', 'aikuistenperusopetuksenalkuvaiheensuoritus', 'perusopetuksenlisaopetuksensuoritus', 'perusopetukseenvalmistavanopetuksensuoritus'].includes(mdl.value.classes[0])) {
    return <PerusopetuksenOppiaineetEditor model={mdl}/>
  }
  if (['perusopetuksenoppiaineenoppimaaransuoritus'].includes(mdl.value.classes[0])) {
    return <PerusopetuksenOppiaineenOppimääränSuoritusEditor model={mdl}/>
  }
  if (['esiopetuksensuoritus'].includes(mdl.value.classes[0])) {
    return <PropertiesEditor model={modelLookup(mdl, 'koulutusmoduuli')} propertyFilter={p => p.key === 'kuvaus'} />
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
    const excludedProperties = ['osasuoritukset', 'käyttäytymisenArvio', 'vahvistus', 'jääLuokalle', 'pakollinen']

    let {model} = this.props
    model = addContext(model, { suoritus: model, toimipiste: modelLookup(model, 'toimipiste')})
    const editor = resolveEditor(model)

    let className = 'suoritus ' + model.value.classes.join(' ')

    return (<div className={className}>
      <TodistusLink suoritus={model} />
      <PropertiesEditor
        model={model}
        propertyFilter={p => !excludedProperties.includes(p.key) && (model.context.edit || modelData(p.model) !== false)}
        getValueEditor={ (prop, getDefault) => {
          switch (prop.key) {
            case 'suorituskieli': return <Editor model={modelLookup(model, 'suorituskieli')} sortBy={sortLanguages}/>
            case 'arviointi': return <ArvosanaEditor model={model}/>
            default: return getDefault()
          }
        }}
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
  let validateSuoritus = (s) => {
    return osasuoritukset(s)
      .flatMap(osasuoritus => {
        if (suoritusValmis(s) && suoritusKesken(osasuoritus)) {
          let subPath = removeCommonPath(osasuoritus.path, m.path)
          return [{
            path: subPath.concat('arviointi'),
            key: 'osasuorituksenTila',
            message: <Text name='Arvosana vaaditaan, koska päätason suoritus on merkitty valmiiksi.'/>
          }]
        } else {
          return validateSuoritus(osasuoritus)
        }
      })
  }
  return validateSuoritus(m)
}

class TodistusLink extends React.Component {
  render() {
    let {suoritus} = this.props
    let oppijaOid = suoritus.context.oppijaOid
    let suoritustyyppi = modelData(suoritus, 'tyyppi').koodiarvo
    let koulutusmoduuliKoodistoUri = modelData(suoritus, 'koulutusmoduuli').tunniste.koodistoUri
    let koulutusmoduuliKoodiarvo = modelData(suoritus, 'koulutusmoduuli').tunniste.koodiarvo
    let href = '/koski/todistus/' + oppijaOid + '?suoritustyyppi=' + suoritustyyppi + '&koulutusmoduuli=' + koulutusmoduuliKoodistoUri + '/' + koulutusmoduuliKoodiarvo
    return suoritusValmis(suoritus)
           && suoritustyyppi !== 'korkeakoulututkinto'
           && suoritustyyppi !== 'preiboppimaara'
           && suoritustyyppi !== 'esiopetuksensuoritus'
           && !(koulutusmoduuliKoodistoUri === 'perusopetuksenluokkaaste' && koulutusmoduuliKoodiarvo === '9')
        ? <a className="todistus" href={href}><Text name="näytä todistus"/></a>
        : null
  }
}