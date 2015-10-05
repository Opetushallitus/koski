import React from "react"
import ReactDOM from "react-dom"
import ReactAutocomplete from 'react-autocomplete'
import Bacon from "baconjs"

export default React.createClass({
  render() {
    return <div><ReactAutocomplete
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
