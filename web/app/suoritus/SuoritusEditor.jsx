import {addContext, modelData, modelItems, modelLookup, removeCommonPath} from '../editor/EditorModel'
import React from 'baret'
import {PropertyEditor} from '../editor/PropertyEditor'
import {PropertiesEditor} from '../editor/PropertiesEditor'
import * as Lukio from '../lukio/Lukio'
import {Suoritustaulukko} from './Suoritustaulukko'
import {LuvaEditor} from '../lukio/LuvaEditor'
import {PerusopetuksenOppiaineetEditor} from '../perusopetus/PerusopetuksenOppiaineetEditor'
import PerusopetuksenOppiaineenOppimääränSuoritusEditor from '../perusopetus/PerusopetuksenOppiaineenOppimaaranSuoritusEditor'
import {sortLanguages} from '../util/sorting'
import {Editor} from '../editor/Editor'
import {ArvosanaEditor} from './ArvosanaEditor'
import {TilaJaVahvistusEditor} from './TilaJaVahvistusEditor'
import {arviointiPuuttuu, osasuoritukset, suoritusKesken, suoritusValmis} from './Suoritus'
import Text from '../i18n/Text'
import {IBTutkinnonOppiaineetEditor} from '../ib/IB'

const resolveEditor = (mdl) => {
  const oneOf = (...classes) => classes.some(c => mdl.value.classes.includes(c))
  const firstClassOneOf = (...classes) => classes.includes(mdl.value.classes[0])

  if (firstClassOneOf(
      'perusopetuksenvuosiluokansuoritus',
      'nuortenperusopetuksenoppimaaransuoritus',
      'aikuistenperusopetuksenoppimaaransuoritus',
      'aikuistenperusopetuksenalkuvaiheensuoritus',
      'perusopetuksenlisaopetuksensuoritus',
      'perusopetukseenvalmistavanopetuksensuoritus')) {
    return <PerusopetuksenOppiaineetEditor model={mdl}/>
  }
  if (firstClassOneOf('perusopetuksenoppiaineenoppimaaransuoritus')) {
    return <PerusopetuksenOppiaineenOppimääränSuoritusEditor model={mdl}/>
  }
  if (firstClassOneOf('esiopetuksensuoritus')) {
    return <PropertiesEditor model={modelLookup(mdl, 'koulutusmoduuli')} propertyFilter={p => p.key === 'kuvaus'} />
  }
  if (oneOf('ammatillinenpaatasonsuoritus', 'ylioppilastutkinnonsuoritus', 'korkeakoulusuoritus')) {
    return <Suoritustaulukko suorituksetModel={modelLookup(mdl, 'osasuoritukset')}/>
  }
  if (oneOf('lukionoppimaaransuoritus', 'preibsuoritus')) {
    return <Lukio.LukionOppiaineetEditor oppiaineet={modelItems(mdl, 'osasuoritukset') || []} />
  }
  if (oneOf('lukionoppiaineenoppimaaransuoritus')) {
    return <Lukio.LukionOppiaineetEditor oppiaineet={[mdl]} />
  }
  if (oneOf('lukioonvalmistavankoulutuksensuoritus')) {
    return <LuvaEditor suoritukset={modelItems(mdl, 'osasuoritukset') || []}/>
  }
  if (oneOf('ibtutkinnonsuoritus')) {
    return <IBTutkinnonOppiaineetEditor oppiaineet={modelItems(mdl, 'osasuoritukset') || []} />
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
