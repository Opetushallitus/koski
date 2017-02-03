import React from 'react'
import { modelData, modelLookup, modelTitle, modelItems } from './EditorModel.js'
import * as GenericEditor from './GenericEditor.jsx'
import { LaajuusEditor } from './OppijaEditor.jsx'
import R from 'ramda'

export const PerusopetuksenOppiaineetEditor = React.createClass({
  render() {
    let {model, context} = this.props
    let käyttäytymisenArvio = modelData(model).käyttäytymisenArvio
    let grouped = R.toPairs(R.groupBy((o => modelData(o).koulutusmoduuli.pakollinen ? 'Pakolliset oppiaineet' : 'Valinnaiset oppiaineet'), modelItems(model, 'osasuoritukset') || []))
    return grouped.length > 0 && (<div className="oppiaineet">
        <h5>Oppiaineiden arvosanat</h5>
        <p>Arvostelu 4-10, S (suoritettu), H (hylätty) tai V (vapautettu)</p>
        {
          grouped.map(([name, suoritukset], i) => (<section key={i}>
              { grouped.length > 1 && <h5>{name}</h5> }
              <Oppiainetaulukko model={model} context={GenericEditor.childContext(this, context, 'osasuoritukset')} suoritukset={suoritukset} />
              {
                käyttäytymisenArvio && (i == grouped.length - 1) && (<div>
                  <h5 className="kayttaytyminen">Käyttäytymisen arviointi</h5>
                  {
                    <GenericEditor.PropertiesEditor properties={modelLookup(model, 'käyttäytymisenArvio').value.properties}
                                                    context={GenericEditor.childContext(this, context, 'käyttäytymisenArvio')}
                                                    getValueEditor={ (prop, ctx, getDefault) => prop.key == 'arvosana' ? prop.model.value.data.koodiarvo : getDefault() }
                    />
                  }
                </div>)
              }
            </section>
          ))
        }
      </div>)
  }
})

const Oppiainetaulukko = React.createClass({
  render() {
    let {suoritukset, context, showLaajuus = false} = this.props
    return (<table>
        <thead><tr><th className="oppiaine">Oppiaine</th><th className="arvosana">Arvosana</th>{showLaajuus && <th className="laajuus">Laajuus</th>}</tr></thead>
        <tbody>
        {
          suoritukset
            .map((oppiaine, i) => (<OppiaineEditor key={i} model={oppiaine} showLaajuus={showLaajuus} context={GenericEditor.childContext(this, context, i)}/>))
        }
        </tbody>
      </table>
    )
  }
})

const OppiaineEditor = React.createClass({
  render() {
    let {model, context, showLaajuus = false} = this.props
    var oppiaine = modelTitle(model, 'koulutusmoduuli')
    let arvosana = modelData(model, 'arviointi.-1.arvosana').koodiarvo
    let pakollinen = modelData(model, 'koulutusmoduuli.pakollinen')
    if (pakollinen === false) {
      oppiaine = 'Valinnainen ' + oppiaine.toLowerCase() // i18n
    }
    return (<tr>
      <td className="oppiaine">{oppiaine}</td>
      <td className="arvosana">
        {arvosana}
        {modelData(model, 'yksilöllistettyOppimäärä') ? <span className="yksilollistetty"> *</span> : null}
        {modelData(model, 'korotus') ? <span className="korotus">(korotus)</span> : null}
      </td>
      {
        showLaajuus && (<td className="laajuus">
          <LaajuusEditor model={modelLookup(model, 'koulutusmoduuli.laajuus')} context={GenericEditor.childContext(this, context, 'koulutusmoduuli', 'laajuus')} />
        </td>)
      }
    </tr>)
  }
})