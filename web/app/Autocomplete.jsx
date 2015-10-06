import React from "react"
import ReactDOM from "react-dom"
import ReactAutocomplete from 'react-autocomplete'
import Bacon from "baconjs"

export default React.createClass({

  render() {
    const {selected} = this.props
    const {items, query} = this.state

    const itemElems = items ? items.map((item, i) => {
        return (
          <li key={i} onClick={this.handleSelect.bind(this, item)}>{item.nimi}</li>
        )}
    ) : []

    return (
      <div ref="autocomplete" className="autocomplete">
        <input className="autocomplete-input" onInput={this.handleInput} value={query ? query : selected ? selected.nimi : ''}></input>
        <ul className="results">{itemElems}</ul>
      </div>
    )
  },

  handleInput(e) {
    let query = e.target.value
    this.props.resultBus.push(undefined)
    this.props.fetchItems(query).mapError([]).onValue((items) => this.setState({ items: items, query: query }))
  },

  handleSelect(selected) {
    this.setState({query: undefined, items: []})
    this.props.resultBus.push(selected)
  },

  getInitialState() {
    return {query: undefined, items: []}
  }

})
