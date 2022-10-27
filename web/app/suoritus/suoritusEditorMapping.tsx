import React from 'react'
import { hasModelProperty, modelData, modelLookup } from '../editor/EditorModel'

import { PerusopetuksenOppiaineetEditor } from '../perusopetus/PerusopetuksenOppiaineetEditor'
import { PropertiesEditor } from '../editor/PropertiesEditor'
import { LukionOppiaineetEditor } from '../lukio/LukionOppiaineetEditor'
import { LuvaEditor } from '../lukio/LuvaEditor'
import { PropertyEditor } from '../editor/PropertyEditor'
import { Editor } from '../editor/Editor'
import { sortLanguages } from '../util/sorting'
import { ArvosanaEditor } from './ArvosanaEditor'
import {
  LukionOppiaineenOppimaaranSuoritus,
  OmatTiedotLukionOppiaineenOppimaaranSuoritus
} from '../lukio/LukionOppiaineenOppimaaranSuoritus'
import {
  CreativityActionService,
  ExtendedEssay,
  TheoryOfKnowledge
} from '../ib/IBYhteinenSuoritus'
import RyhmiteltyOppiaineetEditor from './RyhmiteltyOppiaineetEditor'
import OmatTiedotSuoritustaulukko from './OmatTiedotSuoritustaulukko'
import OmatTiedotLukionOppiaineet from '../lukio/OmatTiedotLukionOppiaineet'
import OmatTiedotPerusopetuksenOppiaineet from '../perusopetus/OmatTiedotPerusopetuksenOppiaineet'
import OmatTiedotRyhmiteltyOppiaineet from './OmatTiedotRyhmiteltyOppiaineet'
import TäydentääTutkintoaEditor from '../ammatillinen/TaydentaaTutkintoaEditor'
import InternationalSchoolOppiaineetEditor from '../internationalschool/InternationalSchoolOppiaineetEditor'
import { AikuistenPerusopetuksenKurssitEditor } from '../aikuistenperusopetus/AikuistenPerusopetuksenKurssitEditor'
import { Suoritustaulukko } from './Suoritustaulukko'
import { VapaanSivistystyonSuoritustaulukko } from '../vapaasivistystyo/VapaanSivistystyonSuoritustaulukko'
import { TutkintokoulutukseenValmentavanKoulutuksenSuoritustaulukko } from '../tuva/TutkintokoulutukseenValmentavanKoulutuksenSuoritustaulukko'
import { OsasuoritusEditorModel } from '../types/OsasuoritusEditorModel'
import { ObjectModel, ObjectModelProperty } from '../types/EditorModels'
import { VstVapaaTavoitteinenKoulutusmoduuliEditor } from './VstVapaaTavoitteinenKoulutusmoduuliEditor'
import { EuropeanSchoolOfHelsinkiOppiaineetEditor } from '../esh/EuropeanSchoolOfHelsinkiOppiaineetEditor'
import { eshSuoritus } from '../esh/europeanschoolofhelsinkiSuoritus'
import { SuoritusEditor } from './SuoritusEditor'

export const resolveOsasuorituksetEditor = (mdl: OsasuoritusEditorModel) => {
  const oneOf = (...classes: string[]) =>
    classes.some((c) => mdl.value.classes.includes(c))
  const firstClassOneOf = (...classes: string[]) =>
    classes.includes(mdl.value.classes[0])
  const { kansalainen } = mdl.context
  const LukionOppiaineetComponent = kansalainen
    ? OmatTiedotLukionOppiaineet
    : LukionOppiaineetEditor

  if (
    firstClassOneOf(
      'perusopetuksenvuosiluokansuoritus',
      'nuortenperusopetuksenoppimaaransuoritus',
      'aikuistenperusopetuksenoppimaaransuoritus',
      'aikuistenperusopetuksenalkuvaiheensuoritus',
      'perusopetuksenlisaopetuksensuoritus',
      'perusopetukseenvalmistavanopetuksensuoritus'
    )
  ) {
    const PerusopetuksenOppiaineetComponent = kansalainen
      ? OmatTiedotPerusopetuksenOppiaineet
      : PerusopetuksenOppiaineetEditor
    return <PerusopetuksenOppiaineetComponent model={mdl} />
  }
  if (firstClassOneOf('aikuistenperusopetuksenoppiaineenoppimaaransuoritus')) {
    return <AikuistenPerusopetuksenKurssitEditor model={mdl} />
  }
  if (firstClassOneOf('esiopetuksensuoritus')) {
    return (
      <PropertiesEditor
        model={modelLookup(mdl, 'koulutusmoduuli')}
        // @ts-expect-error PropertiesEditor
        propertyFilter={(p) => p.key === 'kuvaus'}
      />
    )
  }
  if (
    oneOf(
      'ammatillinenpaatasonsuoritus',
      'ylioppilastutkinnonsuoritus',
      'korkeakoulusuoritus'
    )
  ) {
    const SuoritustaulukkoComponent = kansalainen
      ? OmatTiedotSuoritustaulukko
      : Suoritustaulukko
    return (
      <SuoritustaulukkoComponent
        suorituksetModel={modelLookup(mdl, 'osasuoritukset')}
      />
    )
  }
  if (
    oneOf(
      "oppivelvollisillesuunnattuvapaansivistystyonkoulutuksensuoritus",
      "oppivelvollisillesuunnattumaahanmuuttajienkotoutumiskoulutuksensuoritus",
      "vapaansivistystyonlukutaitokoulutuksensuoritus",
      "vapaansivistystyonvapaatavoitteisenkoulutuksensuoritus",
      "oppivelvollisillesuunnattumaahanmuuttajienkotoutumiskoulutuksensuoritus2022",
      "vapaansivistystyonjotpakoulutuksensuoritus"
    )
  ) {
    return (
      <VapaanSivistystyonSuoritustaulukko
        parentSuoritus={mdl}
        suorituksetModel={modelLookup(mdl, 'osasuoritukset')}
      />
    )
  }
  if (oneOf('lukionoppimaaransuoritus2015')) {
    return (
      // @ts-expect-error LukionOppiaineetComponent
      <LukionOppiaineetComponent
        suorituksetModel={modelLookup(mdl, 'osasuoritukset')}
        classesForUusiOppiaineenSuoritus={[
          'lukionoppiaineensuoritus2015',
          'muidenlukioopintojensuoritus2015'
        ]}
      />
    )
  }
  if (
    oneOf(
      'lukionoppimaaransuoritus2019',
      'lukionoppiaineidenoppimaariensuoritus2019'
    )
  ) {
    return (
      // @ts-expect-error LukionOppiaineetComponent
      <LukionOppiaineetComponent
        suorituksetModel={modelLookup(mdl, 'osasuoritukset')}
        classesForUusiOppiaineenSuoritus={[
          'lukionoppiaineensuoritus2019',
          'muidenlukioopintojensuoritus2019'
        ]}
        useOppiaineLaajuus={true}
        showKeskiarvo={false}
        additionalOnlyEditableProperties={[
          'suorituskieli',
          'suoritettuErityisenäTutkintona'
        ]}
        additionalEditableKoulutusmoduuliProperties={[
          'pakollinen',
          'oppimäärä'
        ]}
        laajuusHeaderText={'Arvioitu'}
        showHyväksytystiArvioitujenLaajuus={true}
        useHylkäämättömätLaajuus={false}
      />
    )
  }
  if (oneOf('preibsuoritus2015')) {
    return (
      // @ts-expect-error LukionOppiaineetComponent
      <LukionOppiaineetComponent
        suorituksetModel={modelLookup(mdl, 'osasuoritukset')}
        classesForUusiOppiaineenSuoritus={[
          'preiboppiaineensuoritus2015',
          'muidenlukioopintojensuoritus'
        ]}
        additionalEditableKoulutusmoduuliProperties={['ryhmä']}
      />
    )
  }
  if (oneOf('preibsuoritus2019')) {
    return (
      // @ts-expect-error LukionOppiaineetComponent
      <LukionOppiaineetComponent
        suorituksetModel={modelLookup(mdl, 'osasuoritukset')}
        classesForUusiOppiaineenSuoritus={[
          'iboppiaineenpreibsuoritus2019',
          'lukionoppiaineenpreibsuoritus2019',
          'muidenlukioopintojenpreibsuoritus2019'
        ]}
        useOppiaineLaajuus={true}
        showKeskiarvo={false}
        additionalOnlyEditableProperties={[
          'suorituskieli',
          'suoritettuErityisenäTutkintona'
        ]}
        additionalEditableKoulutusmoduuliProperties={['pakollinen', 'ryhmä']}
        laajuusHeaderText={'Arvioitu'}
        showHyväksytystiArvioitujenLaajuus={true}
        useHylkäämättömätLaajuus={false}
      />
    )
  }
  if (oneOf('lukionoppiaineenoppimaaransuoritus2015')) {
    const LukionOppiaineenOppimaaranSuoritusComponent = kansalainen
      ? OmatTiedotLukionOppiaineenOppimaaranSuoritus
      : LukionOppiaineenOppimaaranSuoritus
    return <LukionOppiaineenOppimaaranSuoritusComponent model={mdl} />
  }
  if (oneOf('lukioonvalmistavankoulutuksensuoritus')) {
    return <LuvaEditor suorituksetModel={modelLookup(mdl, 'osasuoritukset')} />
  }
  if (oneOf('ibtutkinnonsuoritus')) {
    const TutkinnonOppiaineetComponent = kansalainen
      ? OmatTiedotRyhmiteltyOppiaineet
      : RyhmiteltyOppiaineetEditor
    return (
      <TutkinnonOppiaineetComponent
        suorituksetModel={modelLookup(mdl, 'osasuoritukset')}
        päätasonSuorituksenTyyppi={modelData(mdl, 'tyyppi').koodiarvo}
        additionalEditableKoulutusmoduuliProperties={['taso']}
      />
    )
  }
  if (
    oneOf(
      'diplomavuosiluokansuoritus',
      'mypvuosiluokansuoritus',
      'pypvuosiluokansuoritus'
    )
  ) {
    return (
      <InternationalSchoolOppiaineetEditor
        suorituksetModel={modelLookup(mdl, 'osasuoritukset')}
      />
    )
  }
  // Ei osasuorituksia: nursery
  // Osasuorituksellisia: primary, secondaryLower, secondaryUpper, secondaryUpperS6, secondaryUppserS7
  if (
    firstClassOneOf(
      eshSuoritus.nursery,
      eshSuoritus.primary,
      eshSuoritus.secondaryLower,
      eshSuoritus.secondaryUpper,
      eshSuoritus.secondaryUppers6,
      eshSuoritus.secondaryUppers7,
    )
  ) {
    return <EuropeanSchoolOfHelsinkiOppiaineetEditor model={mdl} />
  }
  if (oneOf('diavalmistavanvaiheensuoritus', 'diatutkinnonsuoritus')) {
    const TutkinnonOppiaineetComponent = kansalainen
      ? OmatTiedotRyhmiteltyOppiaineet
      : RyhmiteltyOppiaineetEditor
    return (
      <TutkinnonOppiaineetComponent
        suorituksetModel={modelLookup(mdl, 'osasuoritukset')}
        päätasonSuorituksenTyyppi={modelData(mdl, 'tyyppi').koodiarvo}
        additionalEditableKoulutusmoduuliProperties={['laajuus']}
      />
    )
  }
  if (
    oneOf(
      'tutkintokoulutukseenvalmentavankoulutuksensuoritus',
      'tutkintokoulutukseenvalmentavankoulutuksenosasuoritus',
      'tutkintokoulutukseenvalmentavankoulutuksenvalinnaisenosansuoritus'
    )
  ) {
    return (
      <TutkintokoulutukseenValmentavanKoulutuksenSuoritustaulukko
        parentSuoritus={mdl}
        suorituksetModel={modelLookup(mdl, 'osasuoritukset')}
      />
    )
  }
  return <PropertyEditor model={mdl} propertyName="osasuoritukset" />
}

export const resolvePropertyEditor = (
  property: ObjectModelProperty,
  model: ObjectModel
) => {
  const oneOf = (...classes: string[]) =>
    classes.some((c) => model.value.classes.includes(c))
  switch (property.key) {
    case 'suorituskieli':
      return (
        <Editor
          model={modelLookup(model, 'suorituskieli')}
          sortBy={sortLanguages}
        />
      )
    case 'arviointi':
      // @ts-expect-error ArvosanaEditor
      return <ArvosanaEditor model={model} />
    case 'koulutusmoduuli':
      if (
        oneOf('vapaansivistystyonpaatasonsuoritus') &&
        hasModelProperty(property.model, 'opintokokonaisuus')
      ) {
        return (
          <VstVapaaTavoitteinenKoulutusmoduuliEditor model={property.model} />
        )
      } else {
        return null
      }

    // IB
    case 'theoryOfKnowledge':
      return <TheoryOfKnowledge model={property.model} />
    case 'creativityActionService':
      return <CreativityActionService model={property.model} />
    case 'extendedEssay':
      return <ExtendedEssay model={property.model} />
    case 'täydentääTutkintoa':
      return <TäydentääTutkintoaEditor model={property.model} />

    default:
      return null
  }
}
