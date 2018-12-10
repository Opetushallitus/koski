import React from 'baret'
import * as R from 'ramda'
import {modelData, modelItems} from '../editor/EditorModel'
import {t} from '../i18n/i18n'
import {LukionOppiaineetTableHead} from '../lukio/fragments/LukionOppiaineetTableHead'
import {LukionOppiaineEditor} from '../lukio/LukionOppiaineEditor'
import {arvosanaFootnote, ibRyhmät} from '../ib/IB'
import {UusiRyhmiteltyOppiaineDropdown} from '../oppiaine/UusiRyhmiteltyOppiaineDropdown'
import {FootnoteDescriptions} from '../components/footnote'
import {diaLukukausiAlternativesCompletionFn, diaRyhmät} from '../dia/DIA'

const diaCustomizations = {
  groupAineet: diaRyhmät,
  laajuusyksikkö: 'vuosiviikkotuntia',
  useOppiaineLaajuus: true,
  showArviointi: false,
  showKieli: true,
  showRyhmättömät: true,
  customOsasuoritusTitle: 'osasuoritus',
  customOsasuoritusTitleOmatTiedot: 'Suoritus',
  customOsasuoritusAlternativesFn: diaLukukausiAlternativesCompletionFn,
  oppiaineOptionsFilter: m => m.value.classes.includes('diaosaalueoppiaine'),
  getFootnote: R.identity
}

const typeDependentCustomizations = {
  ibtutkinto: {
    groupAineet: ibRyhmät,
    laajuusyksikkö: 'kurssia',
    useOppiaineLaajuus: false,
    showArviointi: true,
    showKieli: false,
    showRyhmättömät: false,
    oppiaineOptionsFilter: R.identity,
    getFootnote: oppiaine => modelData(oppiaine, 'arviointi.-1.predicted') && arvosanaFootnote
  },
  diavalmistavavaihe: diaCustomizations,
  diatutkintovaihe: diaCustomizations
}

export const resolvePropertiesByType = päätasonSuorituksenTyyppi => {
  const customizations = typeDependentCustomizations[päätasonSuorituksenTyyppi]
  if (!customizations) console.error(`Oppiaineiden ryhmittely ei onnistu päätason suoritukselle ${päätasonSuorituksenTyyppi}.`)
  return customizations
}

const RyhmättömätAineet = (
  {
    aineet,
    edit,
    additionalEditableKoulutusmoduuliProperties,
    useOppiaineLaajuus,
    showArviointi,
    showKieli,
    päätasonSuoritusModel,
    oppiaineOptionsFilter,
    customOsasuoritusTitle,
    customOsasuoritusAlternativesCompletionFn
  }) => (
  <React.Fragment>
    {
      (aineet && (!R.isEmpty(aineet) || edit)) && (
        <tr className='aineryhmä' key='lisäaineet'>
          <th colSpan='4'>{t('Lisäaineet')}</th>
        </tr>
      )
    }
    {
      aineet && aineet.map(aine => (
        <LukionOppiaineEditor
          key={modelData(aine, 'koulutusmoduuli.tunniste.koodiarvo')}
          oppiaine={aine}
          additionalEditableKoulutusmoduuliProperties={additionalEditableKoulutusmoduuliProperties}
          useOppiaineLaajuus={useOppiaineLaajuus}
          showArviointi={showArviointi}
          showKieli={showKieli}
          customOsasuoritusTitle={customOsasuoritusTitle}
          customOsasuoritusAlternativesCompletionFn={customOsasuoritusAlternativesCompletionFn}
        />
      ))
    }
    <tr className='uusi-oppiaine' key='uusi-oppiaine-lisäaineet'>
      <td colSpan='4'>
        <UusiRyhmiteltyOppiaineDropdown
          model={päätasonSuoritusModel}
          optionsFilter={oppiaineOptionsFilter}
        />
      </td>
    </tr>
  </React.Fragment>
)

export default ({suorituksetModel, päätasonSuorituksenTyyppi, additionalEditableKoulutusmoduuliProperties}) => {
  const {edit, suoritus: päätasonSuoritusModel} = suorituksetModel.context
  const oppiaineet = modelItems(suorituksetModel)

  const {
    groupAineet,
    laajuusyksikkö,
    useOppiaineLaajuus,
    showArviointi,
    showKieli,
    showRyhmättömät,
    customOsasuoritusTitle,
    customOsasuoritusAlternativesFn,
    oppiaineOptionsFilter,
    getFootnote
  } = resolvePropertiesByType(päätasonSuorituksenTyyppi)

  const {aineryhmät, muutAineet, footnotes} = groupAineet(oppiaineet, päätasonSuoritusModel, edit)

  const commonOppiaineProps = {
    additionalEditableKoulutusmoduuliProperties,
    useOppiaineLaajuus,
    showArviointi,
    showKieli,
    customOsasuoritusTitle,
    customOsasuoritusAlternativesCompletionFn: customOsasuoritusAlternativesFn
  }

  return aineryhmät ? (
    <div>
      <table className='suoritukset oppiaineet'>
        <LukionOppiaineetTableHead
          laajuusyksikkö={laajuusyksikkö}
          showArviointi={showArviointi}
        />
        <tbody>
        {
          aineryhmät.map(ryhmät => ryhmät.map(r => [
            <tr className='aineryhmä' key={r.ryhmä.koodiarvo}>
              <th colSpan='4'>{t(r.ryhmä.nimi)}</th>
            </tr>,
            r.aineet && r.aineet.map((oppiaine, oppiaineIndex) => (
              <LukionOppiaineEditor
                key={oppiaineIndex}
                oppiaine={oppiaine}
                footnote={getFootnote(oppiaine)}
                {...commonOppiaineProps}
              />
            )),
            <tr className='uusi-oppiaine' key={`uusi-oppiaine-${r.ryhmä.koodiarvo}`}>
              <td colSpan='4'>
                <UusiRyhmiteltyOppiaineDropdown
                  model={päätasonSuoritusModel}
                  aineryhmä={r.ryhmä}
                  optionsFilter={oppiaineOptionsFilter}
                />
              </td>
            </tr>
          ]))
        }
        {showRyhmättömät && (
          <RyhmättömätAineet
            aineet={muutAineet}
            edit={edit}
            päätasonSuoritusModel={päätasonSuoritusModel}
            oppiaineOptionsFilter={R.complement(oppiaineOptionsFilter)}
            {...commonOppiaineProps}
          />
        )}
        </tbody>
      </table>
      {!R.isEmpty(footnotes) && <FootnoteDescriptions data={footnotes}/>}
    </div>
  ) : null
}
