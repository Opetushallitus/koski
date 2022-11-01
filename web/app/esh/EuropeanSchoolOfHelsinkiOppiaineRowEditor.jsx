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
      showLaajuus,
      footnotes,
      uusiOppiaineenSuoritus,
      expanded,
      onExpand,
      className
    } = this.props

    const { edit } = model.context

    const koulutusmoduuli = modelLookup(model, 'koulutusmoduuli')

    const bodyClassName = cx(
      className,
      'oppiaine',
      'oppiaine-rivi',
      modelData(koulutusmoduuli, 'tunniste').koodiarvo,
      tilaKoodi(model),
      {
        pakollinen: true,
        valinnainen: false,
        expanded: expanded,
        paikallinen: isPaikallinen(koulutusmoduuli)
      }
    )

    const extraProperties = expandableProperties(model)

    const showExpand = extraProperties.length > 0

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
                  {/* eslint-disable-next-line react/jsx-no-literals */}
                  {expanded ? <>&#61766;</> : <>&#61694;</>}
                </a>
              )
            }
            <EuropeanSchoolOfHelsinkiOppiaineEditor
              {...{
                oppiaine: koulutusmoduuli,
                showExpand,
                expanded,
                onExpand,
                uusiOppiaineenSuoritus
              }}
            />
          </td>
          <td className="arvosana">
            <span className="value">
              <EuropeanSchoolOfHelsinkiArvosanaEditor
                model={modelWithToimipiste}
              />
            </span>
          </td>
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

// TODO: TOR-1685: Arviointi-lisätiedoissa ei tarvitse näyttää arvosanaa
const hiddenExtraProperties = ['tunniste', 'laajuus', 'koulutusmoduuli']

export const expandableProperties = (model) => {
  const koulutusmoduuli = modelLookup(model, 'koulutusmoduuli')

  const extraPropertiesFilter = (p) => {
    if (hiddenExtraProperties.includes(p.key)) {
      return false
    }
    return shouldShowProperty(model.context)(p)
  }

  return modelProperties(koulutusmoduuli)
    .concat(modelProperties(model))
    .filter(extraPropertiesFilter)
}
