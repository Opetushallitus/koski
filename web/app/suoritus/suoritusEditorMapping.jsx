import React from 'react'
import {modelLookup} from '../editor/EditorModel'

import {PerusopetuksenOppiaineetEditor} from '../perusopetus/PerusopetuksenOppiaineetEditor'
import PerusopetuksenOppiaineenOppimääränSuoritusEditor from '../perusopetus/PerusopetuksenOppiaineenOppimaaranSuoritusEditor'
import {PropertiesEditor} from '../editor/PropertiesEditor'
import {Suoritustaulukko} from './Suoritustaulukko'
import {LukionOppiaineetEditor} from '../lukio/LukionOppiaineetEditor'
import {LuvaEditor} from '../lukio/LuvaEditor'
import {IBTutkinnonOppiaineetEditor} from '../ib/IB'
import {PropertyEditor} from '../editor/PropertyEditor'
import {Editor} from '../editor/Editor'
import {sortLanguages} from '../util/sorting'
import {ArvosanaEditor} from './ArvosanaEditor'
import {LukionOppiaineenOppimaaranSuoritusEditor} from '../lukio/LukionOppiaineenOppimaaranSuoritusEditor'
import {CreativityActionService, ExtendedEssay, TheoryOfKnowledge} from '../ib/IBYhteinenSuoritus'

export const resolveOsasuorituksetEditor = (mdl) => {
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
  if (oneOf('lukionoppimaaransuoritus')) {
    return (
      <LukionOppiaineetEditor
        suorituksetModel={modelLookup(mdl, 'osasuoritukset')}
        classesForUusiOppiaineenSuoritus={['lukionoppiaineensuoritus', 'muidenlukioopintojensuoritus']}
      />
    )
  }
  if (oneOf('preibsuoritus')) {
    return (
      <LukionOppiaineetEditor
        suorituksetModel={modelLookup(mdl, 'osasuoritukset')}
        additionalEditableKoulutusmoduuliProperties={['ryhmä']}
      />
    )
  }
  if (oneOf('lukionoppiaineenoppimaaransuoritus')) {
    return <LukionOppiaineenOppimaaranSuoritusEditor model={mdl} />
  }
  if (oneOf('lukioonvalmistavankoulutuksensuoritus')) {
    return <LuvaEditor suorituksetModel={modelLookup(mdl, 'osasuoritukset')}/>
  }
  if (oneOf('ibtutkinnonsuoritus')) {
    return (
      <IBTutkinnonOppiaineetEditor
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
