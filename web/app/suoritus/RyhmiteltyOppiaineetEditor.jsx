import React from 'baret'
import * as R from 'ramda'
import {modelData, modelItems} from '../editor/EditorModel'
import {t} from '../i18n/i18n'
import {isMobileAtom} from '../util/isMobileAtom'
import {
  LukionOppiaineetTableHead,
  OmatTiedotLukionOppiaineetTableHead
} from '../lukio/fragments/LukionOppiaineetTableHead'
import {LukionOppiaineEditor} from '../lukio/LukionOppiaineEditor'
import {arvosanaFootnote, ibRyhmät} from '../ib/IB'
import {UusiRyhmiteltyOppiaineDropdown} from '../oppiaine/UusiRyhmiteltyOppiaineDropdown'
import {FootnoteDescriptions} from '../components/footnote'
import {OmatTiedotLukionOppiaine} from '../lukio/OmatTiedotLukionOppiaineet'
import {diaRyhmät} from '../dia/DIA'

const diaCustomizations = {
  groupAineet: diaRyhmät,
  laajuusyksikkö: 'vuosiviikkotuntia',
  useOppiaineLaajuus: true,
  showArvosana: false,
  oppiaineOptionsFilter: m => m.value.classes.includes('diaosaalueoppiaine'),
  getFootnote: R.identity
}

const typeDependentCustomizations = {
  ibtutkinto: {
    groupAineet: ibRyhmät,
    laajuusyksikkö: 'kurssia',
    useOppiaineLaajuus: false,
    showArvosana: true,
    showRyhmättömät: false,
    oppiaineOptionsFilter: R.identity,
    getFootnote: oppiaine => modelData(oppiaine, 'arviointi.-1.predicted') && arvosanaFootnote
  },
  diavalmistavavaihe: diaCustomizations,
  diatutkintovaihe: Object.assign({}, diaCustomizations, {showRyhmättömät: true})
}

const resolvePropertiesByType = päätasonSuorituksenTyyppi => {
  const customizations = typeDependentCustomizations[päätasonSuorituksenTyyppi]
  if (!customizations) console.error(`Oppiaineiden ryhmittely ei onnistu päätason suoritukselle ${päätasonSuorituksenTyyppi}.`)
  return customizations
}

const RyhmättömätAineet = ({aineet, edit, useOppiaineLaajuus, showArvosana, päätasonSuoritusModel, oppiaineOptionsFilter}) => (
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
          additionalEditableKoulutusmoduuliProperties={'osaAlue'}
          useOppiaineLaajuus={useOppiaineLaajuus}
          showArvosana={showArvosana}
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

export const RyhmiteltyOppiaineetEditor = ({suorituksetModel, päätasonSuorituksenTyyppi, additionalEditableKoulutusmoduuliProperties}) => {
  const {edit, suoritus: päätasonSuoritusModel} = suorituksetModel.context
  const oppiaineet = modelItems(suorituksetModel)

  const {
    groupAineet,
    laajuusyksikkö,
    useOppiaineLaajuus,
    showArvosana,
    showRyhmättömät,
    oppiaineOptionsFilter,
    getFootnote
  } = resolvePropertiesByType(päätasonSuorituksenTyyppi)

  const {aineryhmät, muutAineet, footnotes} = groupAineet(oppiaineet, päätasonSuoritusModel, edit)

  return aineryhmät ? (
    <div>
      <table className='suoritukset oppiaineet'>
        <LukionOppiaineetTableHead laajuusyksikkö={laajuusyksikkö}/>
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
                additionalEditableKoulutusmoduuliProperties={additionalEditableKoulutusmoduuliProperties}
                useOppiaineLaajuus={useOppiaineLaajuus}
                showArvosana={showArvosana}
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
            useOppiaineLaajuus={useOppiaineLaajuus}
            showArvosana={showArvosana}
            päätasonSuoritusModel={päätasonSuoritusModel}
            oppiaineOptionsFilter={R.complement(oppiaineOptionsFilter)}
          />)}
        </tbody>
      </table>
      {!R.isEmpty(footnotes) && <FootnoteDescriptions data={footnotes}/>}
    </div>
  ) : null
}

export const OmatTiedotRyhmiteltyOppiaineet = ({suorituksetModel, päätasonSuorituksenTyyppi}) => {
  const {suoritus: päätasonSuoritusModel} = suorituksetModel.context
  const oppiaineet = modelItems(suorituksetModel)

  const {groupAineet} = resolvePropertiesByType(päätasonSuorituksenTyyppi)
  const {aineryhmät, footnotes} = groupAineet(päätasonSuorituksenTyyppi)(oppiaineet, päätasonSuoritusModel)

  return aineryhmät ? (
    <div className='aineryhmat'>
      {
        aineryhmät.map(ryhmät => ryhmät.map(r => [
          <h4 key={r.ryhmä.koodiarvo} className='aineryhma-title'>
            {t(r.ryhmä.nimi)}
          </h4>,
          <table key={`suoritustable-${r.ryhmä.koodiarvo}`} className='omattiedot-suoritukset'>
            <OmatTiedotLukionOppiaineetTableHead />
            <tbody>
            {r.aineet && r.aineet.map((oppiaine, oppiaineIndex) => {
              const footnote = modelData(oppiaine, 'arviointi.-1.predicted') && arvosanaFootnote
              return (
                <OmatTiedotLukionOppiaine
                  baret-lift
                  key={oppiaineIndex}
                  oppiaine={oppiaine}
                  isMobile={isMobileAtom}
                  footnote={footnote}
                />
              )
            })}
            </tbody>
          </table>
        ]))
      }
      {!R.isEmpty(footnotes) && <FootnoteDescriptions data={footnotes}/>}
    </div>
  ) : null
}
