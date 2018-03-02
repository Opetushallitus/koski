import React from 'react'

import {t} from '../../i18n/i18n'
import {modelData, modelLookup, modelTitle} from '../../editor/EditorModel.js'
import {FootnoteHint} from '../../components/footnote'
import {isKieliaine, isLukionMatematiikka, isPaikallinen} from '../../suoritus/Koulutusmoduuli'
import {Editor} from '../../editor/Editor'
import {ArvosanaEditor} from '../../suoritus/ArvosanaEditor'
import {PropertiesEditor} from '../../editor/PropertiesEditor'

const Nimi = ({oppiaine}) => {
  const {edit} = oppiaine.context
  const koulutusmoduuli = modelLookup(oppiaine, 'koulutusmoduuli')
  const nimi = t(modelData(oppiaine, 'koulutusmoduuli.tunniste.nimi'))
  const nimiJaKieli = modelTitle(oppiaine, 'koulutusmoduuli')
  const hasOptions = isKieliaine(koulutusmoduuli) || isLukionMatematiikka(koulutusmoduuli)

  return (
    edit && isPaikallinen(koulutusmoduuli)
      ? (
        <span className='koodi-ja-nimi'>
          <span className='koodi'><Editor model={koulutusmoduuli} path='tunniste.koodiarvo' placeholder={t('Koodi')} /></span>
          <span className='nimi'><Editor model={koulutusmoduuli} path='tunniste.nimi' placeholder={t('Oppiaineen nimi')} /></span>
        </span>
      )
      : (
        <span className='nimi'>
          {edit && hasOptions ? `${nimi}, ` : nimiJaKieli}
        </span>
      )
  )
}

const KoulutusmoduuliPropertiesEditor = ({oppiaine}) => {
  if (!oppiaine.context.edit) return null

  const koulutusmoduuli = modelLookup(oppiaine, 'koulutusmoduuli')

  return (
    <span className='properties'>
      {isKieliaine(koulutusmoduuli) && <Editor model={koulutusmoduuli} path='kieli' inline={true}/>}
      {isLukionMatematiikka(koulutusmoduuli) && <Editor model={koulutusmoduuli} path='oppimäärä' inline={true}/>}
      {isPaikallinen(koulutusmoduuli) && <PropertiesEditor model={koulutusmoduuli} propertyFilter={p => p.key === 'kuvaus'} />}
    </span>
  )
}

const Arviointi = ({oppiaine, suoritetutKurssit, footnote}) => {
  const {edit} = oppiaine.context

  const arviointi = modelData(oppiaine, 'arviointi')
  const numeerinenArvosana = kurssi => parseInt(kurssi.arviointi.last().arvosana.koodiarvo)
  const kurssitNumeerisellaArvosanalla = suoritetutKurssit.filter(kurssi => !isNaN(numeerinenArvosana(kurssi)))
  const keskiarvo = kurssitNumeerisellaArvosanalla.length > 0 && Math.round((kurssitNumeerisellaArvosanalla.map(numeerinenArvosana).reduce((a, b) => a + b) / kurssitNumeerisellaArvosanalla.length) * 10) / 10

  return (
    <div>
      <div className='annettuArvosana'>
        {
          edit || arviointi
            ? <ArvosanaEditor model={oppiaine}/>
            : '-'
        }
        {
          edit &&
          <PropertiesEditor
            model={modelLookup(oppiaine, 'arviointi.-1')}
            propertyFilter={p => p.key !== 'arvosana' && !p.model.optional}
            key={'properties'}
          />
        }
        {
          !edit && arviointi && footnote &&
          <FootnoteHint title={footnote.title} hint={footnote.hint} />
        }
      </div>
      <div className='keskiarvo'>{keskiarvo ? '(' + keskiarvo.toFixed(1).replace('.', ',') + ')' : ''}</div>
    </div>
  )
}

export {
  Nimi,
  KoulutusmoduuliPropertiesEditor,
  Arviointi
}
