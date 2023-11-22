import React from 'react'
import { hasModelProperty, modelData, modelLookup } from '../editor/EditorModel'

import { AikuistenPerusopetuksenKurssitEditor } from '../aikuistenperusopetus/AikuistenPerusopetuksenKurssitEditor'
import TäydentääTutkintoaEditor from '../ammatillinen/TaydentaaTutkintoaEditor'
import { Editor } from '../editor/Editor'
import { PropertiesEditor } from '../editor/PropertiesEditor'
import { PropertyEditor } from '../editor/PropertyEditor'
import { EuropeanSchoolOfHelsinkiOsasuorituksetEditor } from '../esh/EuropeanSchoolOfHelsinkiOsasuorituksetEditor'
import {
  ebSuorituksenClass,
  eshSuorituksenClass
} from '../esh/europeanschoolofhelsinkiSuoritus'
import {
  CreativityActionService,
  ExtendedEssay,
  TheoryOfKnowledge
} from '../ib/IBYhteinenSuoritus'
import InternationalSchoolOppiaineetEditor from '../internationalschool/InternationalSchoolOppiaineetEditor'
import { MuuKuinSäänneltySuoritustaulukko } from '../jotpa/MuuKuinSäänneltySuoritustaulukko'
import {
  LukionOppiaineenOppimaaranSuoritus,
  OmatTiedotLukionOppiaineenOppimaaranSuoritus
} from '../lukio/LukionOppiaineenOppimaaranSuoritus'
import { LukionOppiaineetEditor } from '../lukio/LukionOppiaineetEditor'
import { LuvaEditor } from '../lukio/LuvaEditor'
import OmatTiedotLukionOppiaineet from '../lukio/OmatTiedotLukionOppiaineet'
import OmatTiedotPerusopetuksenOppiaineet from '../perusopetus/OmatTiedotPerusopetuksenOppiaineet'
import { PerusopetuksenOppiaineetEditor } from '../perusopetus/PerusopetuksenOppiaineetEditor'
import { TutkintokoulutukseenValmentavanKoulutuksenSuoritustaulukko } from '../tuva/TutkintokoulutukseenValmentavanKoulutuksenSuoritustaulukko'
import { ObjectModel, ObjectModelProperty } from '../types/EditorModels'
import { OsasuoritusEditorModel } from '../types/OsasuoritusEditorModel'
import { opintokokonaisuuteenKuuluvatPäätasonSuoritukset } from '../util/opintokokonaisuus'
import { sortLanguages } from '../util/sorting'
import { ArvosanaEditor } from './ArvosanaEditor'
import OmatTiedotRyhmiteltyOppiaineet from './OmatTiedotRyhmiteltyOppiaineet'
import OmatTiedotSuoritustaulukko from './OmatTiedotSuoritustaulukko'
import { OpintokokonaisuudellinenKoulutusmoduuliEditor } from './OpintokokonaisuudellinenKoulutusmoduuliEditor'
import RyhmiteltyOppiaineetEditor from './RyhmiteltyOppiaineetEditor'
import { Suoritustaulukko } from './Suoritustaulukko'

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
  // Nursery-suoritustyypillä ei ole osasuorituksia, joten editoria ei tarvitse näyttää
  if (firstClassOneOf(eshSuorituksenClass.nursery)) {
    return null
  }
  if (
    firstClassOneOf(
      eshSuorituksenClass.primary,
      eshSuorituksenClass.secondaryLowerVuosiluokka,
      eshSuorituksenClass.secondaryUpperVuosiluokka,
      ebSuorituksenClass.ebtutkinto
    )
  ) {
    return <EuropeanSchoolOfHelsinkiOsasuorituksetEditor model={mdl} />
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
  if (oneOf('muunkuinsaannellynkoulutuksenpaatasonsuoritus')) {
    return (
      <MuuKuinSäänneltySuoritustaulukko
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
        oneOf(...opintokokonaisuuteenKuuluvatPäätasonSuoritukset) &&
        hasModelProperty(property.model, 'opintokokonaisuus')
      ) {
        return (
          <OpintokokonaisuudellinenKoulutusmoduuliEditor
            model={property.model}
          />
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
