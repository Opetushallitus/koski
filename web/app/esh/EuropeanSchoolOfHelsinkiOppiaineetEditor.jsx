import React from 'baret'
import Bacon from 'baconjs'
import {
  addContext,
  contextualizeSubModel,
  ensureArrayKey,
  modelData,
  modelItems,
  modelLookup,
  modelSet,
  modelSetValue,
  oneOfPrototypes,
  pushModel,
  wrapOptional
} from '../editor/EditorModel'
import * as R from 'ramda'
import { arvioituTaiVahvistettu, osasuoritukset } from '../suoritus/Suoritus'
import { accumulateExpandedState } from '../editor/ExpandableItems'
import { t } from '../i18n/i18n'
import Text from '../i18n/Text'
import { isToimintaAlueittain, jääLuokalle, valmiitaSuorituksia } from './esh'
import {
  expandableProperties,
  EuropeanSchoolOfHelsinkiOppiaineRowEditor
} from './EuropeanSchoolOfHelsinkiOppiaineRowEditor'
import { UusiEuropeanSchoolOfHelsinkiOppiaineDropdown } from './EuropeanSchoolOfHelsinkiOppiaineDropdown'
import { parseISODate } from '../date/date'
import { EuropeanSchoolOfHelsinkiSuoritustaulukko } from '../suoritus/EuropeanSchoolOfHelsinkiSuoritustaulukko'

export const EuropeanSchoolOfHelsinkiOppiaineetEditor = ({ model }) => {
  model = addContext(model, { suoritus: model })
  const oppiaineSuoritukset = modelItems(model, 'osasuoritukset')
  const osasuorituksetModel = modelLookup(model, 'osasuoritukset')

  if (model.context.edit) {
    if (!valmiitaSuorituksia(oppiaineSuoritukset)) {
      prefillOsasuorituksetIfNeeded(model, oppiaineSuoritukset)
    } else if (!jääLuokalle(model)) {
      // emptyOsasuoritukset(model)
    }
  }

  return (
    <div className="oppiaineet">
      <div>
        <h5>
          <Text
            name={
              (isToimintaAlueittain(model)
                ? 'Toiminta-alueiden'
                : 'Oppiaineiden') + ' arvosanat'
            }
          />
        </h5>
        <p>
          <Text name="(ESH arvosteluteksti TODO)" />
        </p>
        <EuropeanSchoolOfHelsinkiSuoritustaulukko
          parentSuoritus={model}
          suorituksetModel={osasuorituksetModel}
          nestedLevel={2}
        />
      </div>
    </div>
  )
}

const prefillOsasuorituksetIfNeeded = (model, currentSuoritukset) => {
  const wrongOsasuorituksetTemplateP = fetchOsasuorituksetTemplate(model, false)
  const hasWrongPrefillP = wrongOsasuorituksetTemplateP.map(
    (wrongOsasuorituksetTemplate) =>
      // esitäyttödatan tyyppi ei sisällä nimi ja versiotietoja, poistetaan tyyppi koska se ei ole relevanttia vertailussa
      currentSuoritukset.length > 0 &&
      R.equals(
        wrongOsasuorituksetTemplate.value.map(modelDataIlmanTyyppiä),
        currentSuoritukset.map(modelDataIlmanTyyppiä)
      )
  )
  const changeTemplateP = hasWrongPrefillP.or(
    Bacon.constant(jääLuokalle(model))
  )
  fetchOsasuorituksetTemplate(model, false)
    .filter(changeTemplateP)
    .onValue((osasuorituksetTemplate) =>
      pushModel(
        modelSetValue(model, osasuorituksetTemplate.value, 'osasuoritukset')
      )
    )
}

// TODO: TOR-1685 Osasuoritusten template
const fetchOsasuorituksetTemplate = (_model, _toimintaAlueittain) =>
  Bacon.constant({ value: [] })

const modelDataIlmanTyyppiä = (suoritus) =>
  R.dissoc('tyyppi', modelData(suoritus))

/*
const hasPakollisuus = (model, uusiOppiaineenSuoritus) => {
  const oppiaineHasPakollisuus = (oppiaine) =>
    findModelProperty(oppiaine, (p) => p.key === 'pakollinen')
  const koulutusmoduuliProtos = oneOfPrototypes(
    modelLookup(uusiOppiaineenSuoritus, 'koulutusmoduuli')
  )
  return (
    !isToimintaAlueittain(model) &&
    (koulutusmoduuliProtos.some(oppiaineHasPakollisuus) ||
      modelItems(model, 'osasuoritukset')
        .map((m) => modelLookup(m, 'koulutusmoduuli'))
        .some(oppiaineHasPakollisuus))
  )
}


const GroupedOppiaineetEditor = ({ model, uusiOppiaineenSuoritus }) => {
  const groups = [pakollisetTitle, valinnaisetTitle]
  const groupedSuoritukset = R.groupBy(
    groupTitleForSuoritus,
    modelItems(model, 'osasuoritukset')
  )
  return (
    <span>
      {groups.map((pakollisuus) => {
        const pakollinen = pakollisuus === 'Pakolliset oppiaineet'
        const suoritukset = groupedSuoritukset[pakollisuus] || []

        return (
          <section
            className={pakollinen ? 'pakolliset' : 'valinnaiset'}
            key={pakollisuus}
          >
            <Oppiainetaulukko
              model={model}
              title={groups.length > 1 && pakollisuus}
              suoritukset={suoritukset}
              uusiOppiaineenSuoritus={uusiOppiaineenSuoritus}
              pakolliset={pakollinen}
            />
            {pakollinen ? null : <KäyttäytymisenArvioEditor model={model} />}
          </section>
        )
      })}
    </span>
  )
}
*/

const createOppiaineenSuoritus = (
  suoritukset,
  preferredClassFn = (proto) => {
    return proto.value.classes[0]
  }
) => {
  const s = wrapOptional(suoritukset)
  const newItemIndex = modelItems(s).length
  let oppiaineenSuoritusProto = contextualizeSubModel(
    s.arrayPrototype,
    s,
    newItemIndex
  )
  const preferredClass = preferredClassFn(oppiaineenSuoritusProto)
  const sortValue = (suoritusProto) =>
    suoritusProto.value.classes.includes(preferredClass) ? 0 : 1
  const options = oneOfPrototypes(oppiaineenSuoritusProto).sort(
    (a, b) => sortValue(a) - sortValue(b)
  )
  oppiaineenSuoritusProto = options[0]
  return contextualizeSubModel(oppiaineenSuoritusProto, s, newItemIndex)
}

// eslint-disable-next-line no-unused-vars
class EshOppiainetaulukko extends React.Component {
  render() {
    const { model, suoritukset, title, pakolliset, uusiOppiaineenSuoritus } =
      this.props
    const { isExpandedP, setExpanded } = accumulateExpandedState({
      suoritukset,
      filter: (s) => expandableProperties(s).length > 0,
      component: this
    })

    const edit = model.context.edit
    const showArvosana = edit || arvioituTaiVahvistettu(model)
    const uudellaSuorituksellaLaajuus = () =>
      !!modelLookup(
        uusiOppiaineenSuoritus ||
          createOppiaineenSuoritus(modelLookup(model, 'osasuoritukset')),
        'koulutusmoduuli.laajuus'
      )
    const sisältääLajuudellisiaSuorituksia = !!suoritukset.find((s) =>
      modelData(s, 'koulutusmoduuli.laajuus')
    )
    const päätasonSuorituksenVahvistuspäivä = modelData(
      model,
      'vahvistus.päivä'
    )
    const vahvistusSalliiLaajuudenNäyttämisen =
      päätasonSuorituksenVahvistuspäivä &&
      parseISODate(päätasonSuorituksenVahvistuspäivä) >=
        parseISODate('2020-08-01')

    const showLaajuus = edit
      ? uudellaSuorituksellaLaajuus()
      : pakolliset || isToimintaAlueittain(model)
      ? vahvistusSalliiLaajuudenNäyttämisen && sisältääLajuudellisiaSuorituksia
      : sisältääLajuudellisiaSuorituksia

    const addOppiaine = (oppiaineenSuoritus) => (oppiaine) => {
      const suoritusUudellaOppiaineella = modelSet(
        oppiaineenSuoritus,
        oppiaine,
        'koulutusmoduuli'
      )
      pushModel(suoritusUudellaOppiaineella, model.context.changeBus)
      ensureArrayKey(suoritusUudellaOppiaineella)
      setExpanded(suoritusUudellaOppiaineella)(true)
    }

    if (suoritukset.length === 0 && !model.context.edit) {
      return null
    }

    /*
    const placeholder = t(
      isToimintaAlueittain(model)
        ? 'Lisää toiminta-alue'
        : pakolliset === undefined
        ? 'Lisää oppiaine'
        : pakolliset
        ? 'Lisää pakollinen oppiaine'
        : 'Lisää valinnainen oppiaine'
    )
    */

    const suoritusListaus = (listattavatSuoritukset, listausTitle) => (
      <React.Fragment>
        {listausTitle && (
          <b>
            <Text name={listausTitle} />
          </b>
        )}
        <thead>
          <tr>
            <th className="oppiaine">
              <Text
                name={
                  isToimintaAlueittain(model) ? 'Toiminta-alue' : 'Oppiaine'
                }
              />
            </th>
            {showArvosana && (
              <th className="arvosana" colSpan={'1'}>
                <Text name="Arvosana" />
              </th>
            )}
            {showLaajuus && (
              <th className="laajuus" colSpan={'1'}>
                <Text name="Laajuus" />
              </th>
            )}
          </tr>
        </thead>
        <hr />
        {listattavatSuoritukset
          .filter(
            (s) => edit || arvioituTaiVahvistettu(s) || osasuoritukset(s).length
          )
          .map((suoritus) => (
            <EuropeanSchoolOfHelsinkiOppiaineRowEditor
              baret-lift
              key={suoritus.arrayKey}
              model={suoritus}
              uusiOppiaineenSuoritus={uusiOppiaineenSuoritus}
              expanded={isExpandedP(suoritus)}
              onExpand={setExpanded(suoritus)}
              showArvosana={showArvosana}
              showLaajuus={showLaajuus}
            />
          ))}
      </React.Fragment>
    )

    return (
      <section className="oppiaine-taulukko" style={{ width: '100%' }}>
        {title && (
          <h5>
            <Text name={title} />
          </h5>
        )}
        {suoritukset.length > 0 && (
          <table>{suoritusListaus(suoritukset)}</table>
        )}
        {uusiOppiaineenSuoritus && (
          <span className="uusi-esh-oppiaine">
            <UusiEuropeanSchoolOfHelsinkiOppiaineDropdown
              suoritukset={suoritukset}
              oppiaineenSuoritus={uusiOppiaineenSuoritus}
              pakollinen={pakolliset}
              resultCallback={addOppiaine(uusiOppiaineenSuoritus)}
              organisaatioOid={modelData(model.context.toimipiste).oid}
              placeholder={t('Lisää European School of Helsinki -oppiaine')}
            />
          </span>
        )}
      </section>
    )
  }
}
