import React from 'react'
import {modelLookup} from '../editor/EditorModel'

import {PerusopetuksenOppiaineetEditor} from '../perusopetus/PerusopetuksenOppiaineetEditor'
import PerusopetuksenOppiaineenOppimääränSuoritusEditor from '../perusopetus/PerusopetuksenOppiaineenOppimaaranSuoritusEditor'
import {PropertiesEditor} from '../editor/PropertiesEditor'
import {Suoritustaulukko} from './Suoritustaulukko'
import {LukionOppiaineetEditor} from '../lukio/LukionOppiaineetEditor'
import {LuvaEditor} from '../lukio/LuvaEditor'
import {IBTutkinnonOppiaineetEditor, OmatTiedotIBTutkinnonOppiaineet} from '../ib/IB'
import {PropertyEditor} from '../editor/PropertyEditor'
import {Editor} from '../editor/Editor'
import {sortLanguages} from '../util/sorting'
import {ArvosanaEditor} from './ArvosanaEditor'
import {
  LukionOppiaineenOppimaaranSuoritus,
  OmatTiedotLukionOppiaineenOppimaaranSuoritus
} from '../lukio/LukionOppiaineenOppimaaranSuoritus'
import {CreativityActionService, ExtendedEssay, TheoryOfKnowledge} from '../ib/IBYhteinenSuoritus'
import OmatTiedotSuoritustaulukko from './OmatTiedotSuoritustaulukko'
import OmatTiedotLukionOppiaineet from '../lukio/OmatTiedotLukionOppiaineet'

export const resolveOsasuorituksetEditor = (mdl) => {
  const oneOf = (...classes) => classes.some(c => mdl.value.classes.includes(c))
  const firstClassOneOf = (...classes) => classes.includes(mdl.value.classes[0])
  const {kansalainen} = mdl.context
  const LukionOppiaineetComponent = kansalainen ? OmatTiedotLukionOppiaineet : LukionOppiaineetEditor

  if (firstClassOneOf(
      'perusopetuksenvuosiluokansuoritus',
      'nuortenperusopetuksenoppimaaransuoritus',
      'aikuistenperusopetuksenoppimaaransuoritus',
      'aikuistenperusopetuksenalkuvaiheensuoritus',
      'perusopetuksenlisaopetuksensuoritus',
      'perusopetukseenvalmistavanopetuksensuoritus')) {
    return <PerusopetuksenOppiaineetEditor model={mdl}/>
  }
  if (firstClassOneOf('aikuistenperusopetuksenoppiaineenoppimaaransuoritus')) {
    return <PerusopetuksenOppiaineenOppimääränSuoritusEditor model={mdl}/>
  }
  if (firstClassOneOf('esiopetuksensuoritus')) {
    return <PropertiesEditor model={modelLookup(mdl, 'koulutusmoduuli')} propertyFilter={p => p.key === 'kuvaus'} />
  }
  if (oneOf('ammatillinenpaatasonsuoritus', 'ylioppilastutkinnonsuoritus', 'korkeakoulusuoritus')) {
    const SuoritustaulukkoComponent = kansalainen ? OmatTiedotSuoritustaulukko : Suoritustaulukko
    return <SuoritustaulukkoComponent suorituksetModel={modelLookup(mdl, 'osasuoritukset')} />
  }
  if (oneOf('lukionoppimaaransuoritus')) {
    return (
      <LukionOppiaineetComponent
        suorituksetModel={modelLookup(mdl, 'osasuoritukset')}
        classesForUusiOppiaineenSuoritus={['lukionoppiaineensuoritus', 'muidenlukioopintojensuoritus']}
      />
    )
  }
  if (oneOf('preibsuoritus')) {
    return (
      <LukionOppiaineetComponent
        suorituksetModel={modelLookup(mdl, 'osasuoritukset')}
        classesForUusiOppiaineenSuoritus={['preiboppiaineensuoritus', 'muidenlukioopintojensuoritus']}
        additionalEditableKoulutusmoduuliProperties={['ryhmä']}
      />
    )
  }
  if (oneOf('lukionoppiaineenoppimaaransuoritus')) {
    const LukionOppiaineenOppimaaranSuoritusComponent = kansalainen
      ? OmatTiedotLukionOppiaineenOppimaaranSuoritus
      : LukionOppiaineenOppimaaranSuoritus
    return <LukionOppiaineenOppimaaranSuoritusComponent model={mdl} />
  }
  if (oneOf('lukioonvalmistavankoulutuksensuoritus')) {
    return <LuvaEditor suorituksetModel={modelLookup(mdl, 'osasuoritukset')}/>
  }
  if (oneOf('ibtutkinnonsuoritus')) {
    const IBTutkinnonOppiaineetComponent = kansalainen ? OmatTiedotIBTutkinnonOppiaineet : IBTutkinnonOppiaineetEditor
    return (
      <IBTutkinnonOppiaineetComponent
        suorituksetModel={modelLookup(mdl, 'osasuoritukset')}
        additionalEditableKoulutusmoduuliProperties={['taso']}
      />
    )
  }
  return <PropertyEditor model={mdl} propertyName="osasuoritukset"/>
}

export const resolvePropertyEditor = (property, model) => {
  switch (property.key) {
    case 'suorituskieli': return <Editor model={modelLookup(model, 'suorituskieli')} sortBy={sortLanguages}/>
    case 'arviointi': return <ArvosanaEditor model={model}/>

    // IB
    case 'theoryOfKnowledge': return <TheoryOfKnowledge model={property.model}/>
    case 'creativityActionService': return <CreativityActionService model={property.model}/>
    case 'extendedEssay': return <ExtendedEssay model={property.model}/>

    default: return null
  }
}
