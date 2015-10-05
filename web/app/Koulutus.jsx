import React from "react"
import ReactDOM from "react-dom"
import Autocomplete from 'react-autocomplete'
import Bacon from "baconjs"
import Http from "./http"

const oppilaitosE = new Bacon.Bus()
const tutkintoE = new Bacon.Bus()

const OegyComplete = React.createClass({
  render() {
    return <div><Autocomplete
      ref="autocomplete"
      items={(this.state || {}).items || []}
      getItemValue={(item) => item.nimi}
      onSelect={(value, item) => {
            this.setState({ items: [  ] })
            this.props.resultBus.push(item)
          }}
      onChange={(event, value) => {
            this.setState({loading: true})
            this.props.resultBus.push(undefined)
            let result = this.props.fetchItems(value).mapError([])
            result
              .onValue((items) => this.setState({ items: items, loading: false }))
          }}
      renderItem={(item, isHighlighted) => (
            <div
              className={isHighlighted ? "highlight" : ""}
              key={item.nimi}
            >{item.nimi}</div>
          )}
      renderMenu={(items, value) => {
        return items.length ? <div className="autocomplete-menu" children={items}/> : <div></div>
      }}
      /></div>
  }
})

const Oppilaitos = React.createClass({
  render() {
    return <label className="oppilaitos">Oppilaitos<OegyComplete
        resultBus={oppilaitosE}

        fetchItems={(value) => (value.length >= 3)
        ? Http.get("/tor/api/oppilaitos?query=" + value)
        : Bacon.once([])
      }/>
    </label>
  }
})

const Tutkinto = React.createClass({
  render() {
    return <label className="tutkinto">Tutkinto<OegyComplete
      resultBus={tutkintoE}

      fetchItems={(value) => (value.length >= 3)
        ? Http.get("/tor/api/koulutus/oppilaitos/" + this.props.oppilaitos.organisaatioId + "?query=" + value)
        : Bacon.once([])
      }/>
    </label>
  }
})

export const Koulutus = ({koulutus}) => <div>
  <Oppilaitos/>
  <Tutkinto oppilaitos={koulutus.oppilaitos}/>
</div>

export const koulutusP = Bacon.combineTemplate({
  oppilaitos: oppilaitosE.toProperty(undefined).skipDuplicates()
}).doLog("koulutus")