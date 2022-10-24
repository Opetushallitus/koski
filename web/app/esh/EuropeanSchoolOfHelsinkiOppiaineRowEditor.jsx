import React from 'baret'
import cx from 'classnames'
import { Editor } from '../editor/Editor'
import {
  PropertiesEditor,
  shouldShowProperty
} from '../editor/PropertiesEditor'
import {
  addContext,
  modelData,
  modelEmpty,
  modelErrorMessages,
  modelLookup,
  modelProperties,
  pushRemoval
} from '../editor/EditorModel'
import { EuropeanSchoolOfHelsinkiOppiaineEditor } from './EuropeanSchoolOfHelsinkiOppiaineEditor'
import { isPaikallinen } from '../suoritus/Koulutusmoduuli'
import { KurssitEditor } from '../kurssi/KurssitEditor'
import { tilaKoodi } from '../suoritus/Suoritus'
import { FootnoteHint } from '../components/footnote'
import { EuropeanSchoolOfHelsinkiArvosanaEditor } from '../suoritus/EuropeanSchoolOfHelsinkiArvosanaEditor'

export class EuropeanSchoolOfHelsinkiOppiaineRowEditor extends React.Component {
  render() {
    const {
      model,
      showArvosana,
      showLaajuus,
      footnotes,
      uusiOppiaineenSuoritus,
      expanded,
      onExpand,
      className
    } = this.props

    const { edit } = model.context

    const oppiaine = modelLookup(model, 'koulutusmoduuli')

    const bodyClassName = cx(
      className,
      'oppiaine',
      'oppiaine-rivi',
      modelData(oppiaine, 'tunniste').koodiarvo,
      tilaKoodi(model),
      {
        pakollinen: isPakollinen(model),
        valinnainen: !isPakollinen(model),
        expanded: expanded,
        paikallinen: isPaikallinen(oppiaine)
      }
    )

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

    const modelWithToimipiste = addContext(model, {
      toimipiste: modelLookup(model, 'toimipiste')
    })

    return (
      <tbody className={bodyClassName}>
        <tr>
          <td className="oppiaine">
            {
              // expansion link
              showExpand && (
                <a
                  className="toggle-expand"
                  onClick={() => onExpand(!expanded)}
                >
                  {expanded ? '' : ''}
                </a>
              )
            }
            <EuropeanSchoolOfHelsinkiOppiaineEditor
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
                <EuropeanSchoolOfHelsinkiArvosanaEditor
                  model={modelWithToimipiste}
                />
              </span>
            </td>
          )}
          {showLaajuus && (
            <td className="laajuus">
              <Editor
                model={modelWithToimipiste}
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
              <a
                className="remove-value"
                onClick={() => pushRemoval(modelWithToimipiste)}
              />
            </td>
          )}
        </tr>
        {showSanallinenArvio && (
          <tr key="sanallinenArviointi" className="sanallinen-arviointi">
            <td colSpan="4" className="details">
              <PropertiesEditor
                properties={sanallinenArvioProperties}
                context={modelWithToimipiste.context}
              />
            </td>
          </tr>
        )}
        {expanded && (
          <tr key="details">
            <td colSpan="4" className="details">
              <PropertiesEditor
                context={modelWithToimipiste.context}
                properties={extraProperties}
              />
            </td>
          </tr>
        )}
        <tr className="kurssit">
          <td>
            <KurssitEditor model={modelWithToimipiste} />
          </td>
        </tr>
        {modelErrorMessages(modelWithToimipiste).map((error, i) => (
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
    ) {
      return false // these are never shown
    }
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
