import React from 'baret'
import { Editor } from '../editor/Editor'
import {
  PropertiesEditor,
  shouldShowProperty
} from '../editor/PropertiesEditor'
import {
  modelData,
  modelEmpty,
  modelErrorMessages,
  modelLookup,
  modelProperties,
  pushRemoval
} from '../editor/EditorModel'
import { PerusopetuksenOppiaineEditor } from './PerusopetuksenOppiaineEditor'
import { isPaikallinen } from '../suoritus/Koulutusmoduuli'
import { KurssitEditor } from '../kurssi/KurssitEditor'
import { ArvosanaEditor } from '../suoritus/ArvosanaEditor'
import { tilaKoodi } from '../suoritus/Suoritus'
import { FootnoteHint } from '../components/footnote'
import { isToimintaAlueittain } from './Perusopetus'

export class PerusopetuksenOppiaineRowEditor extends React.Component {
  render() {
    const {
      model,
      showArvosana,
      showLaajuus,
      footnotes,
      uusiOppiaineenSuoritus,
      expanded,
      onExpand
    } = this.props
    const { edit } = model.context

    const oppiaine = modelLookup(model, 'koulutusmoduuli')
    const className =
      'oppiaine oppiaine-rivi' +
      ' ' +
      (isPakollinen(model) ? 'pakollinen' : 'valinnainen') +
      ' ' +
      modelData(oppiaine, 'tunniste').koodiarvo +
      ' ' +
      tilaKoodi(model) +
      (expanded ? ' expanded' : '') +
      (isPaikallinen(oppiaine) ? ' paikallinen' : '')

    const extraProperties = expandableProperties(model)

    const showExpand = extraProperties.length > 0

    const sanallinenArvioProperties = modelProperties(
      modelLookup(model, 'arviointi.-1'),
      (p) => p.key === 'kuvaus'
    )
    const showSanallinenArvio =
      (model.context.edit && sanallinenArvioProperties.length > 0) ||
      (showArvosana &&
        sanallinenArvioProperties.filter((p) => !modelEmpty(p.model)).length >
          0)

    return (
      <tbody className={className}>
        <tr>
          <td className="oppiaine">
            {
              // expansion link
              showExpand && (
                <a
                  className="toggle-expand"
                  onClick={() => onExpand(!expanded)}
                >
                  {/* eslint-disable-next-line react/jsx-no-literals */}
                  {expanded ? <>&#61766;</> : <>&#61694;</>}
                </a>
              )
            }
            <PerusopetuksenOppiaineEditor
              {...{
                oppiaine,
                showExpand,
                expanded,
                onExpand,
                uusiOppiaineenSuoritus
              }}
            />
          </td>
          {showArvosana && (
            <td className="arvosana">
              <span className="value">
                <ArvosanaEditor model={model} />
              </span>
            </td>
          )}
          {showLaajuus && (
            <td className="laajuus">
              <Editor
                model={model}
                path="koulutusmoduuli.laajuus"
                compact="true"
              />
            </td>
          )}
          {!edit && footnotes && footnotes.length > 0 && (
            <td className="footnotes">
              <div className="footnotes-container">
                {footnotes.map((note) => (
                  <FootnoteHint
                    key={note.hint}
                    title={note.title}
                    hint={note.hint}
                  />
                ))}
              </div>
            </td>
          )}
          {edit && (
            <td>
              <a className="remove-value" onClick={() => pushRemoval(model)} />
            </td>
          )}
        </tr>
        {showSanallinenArvio && (
          <tr key="sanallinenArviointi" className="sanallinen-arviointi">
            <td colSpan="4" className="details">
              <PropertiesEditor
                properties={sanallinenArvioProperties}
                context={model.context}
              />
            </td>
          </tr>
        )}
        {expanded && (
          <tr key="details">
            <td colSpan="4" className="details">
              <PropertiesEditor
                context={model.context}
                properties={extraProperties}
              />
            </td>
          </tr>
        )}
        <tr className="kurssit">
          <td>
            <KurssitEditor model={model} />
          </td>
        </tr>
        {modelErrorMessages(model).map((error, i) => (
          <tr key={'error-' + i} className="error">
            <td colSpan="42" className="error">
              {error}
            </td>
          </tr>
        ))}
      </tbody>
    )
  }
}

export const expandableProperties = (model) => {
  const edit = model.context.edit
  const oppiaine = modelLookup(model, 'koulutusmoduuli')

  const extraPropertiesFilter = (p) => {
    if (
      !edit &&
      ['yksilöllistettyOppimäärä', 'painotettuOpetus', 'korotus'].includes(
        p.key
      )
    )
      return false // these are only shown when editing
    if (
      [
        'koulutusmoduuli',
        'arviointi',
        'tunniste',
        'kieli',
        'laajuus',
        'pakollinen',
        'arvosana',
        'päivä',
        'perusteenDiaarinumero',
        'osasuoritukset'
      ].includes(p.key)
    )
      return false // these are never shown
    if (isToimintaAlueittain(model) && p.key === 'korotus') return false
    return shouldShowProperty(model.context)(p)
  }

  return modelProperties(
    oppiaine,
    (p) =>
      !(p.key === 'kuvaus' && isPakollinen(model) && !isPaikallinen(oppiaine))
  )
    .concat(modelProperties(model))
    .filter(extraPropertiesFilter)
}

const isPakollinen = (model) => modelData(model, 'koulutusmoduuli.pakollinen')
