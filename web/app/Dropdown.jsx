import React from 'react'

// Shows <select> if more than 1 option. If 1 option, automatically selects it and shows it. If zero options, hides the whole thing.
export default React.createClass({
  render() {
    let { title, options, value, onChange, className} = this.props
    let withEmptyValue = (xs) => [{ koodiarvo: '', nimi: 'Valitse...'}].concat(xs)
    let optionElems = opts => withEmptyValue(opts).map(s => <option key={s.koodiarvo} value={s.koodiarvo}>{s.nimi ? s.nimi : s.koodiarvo}</option>)

    return options.length > 1
      ? <label>{title}
      <select
        className={className}
        value={value}
        onChange={(event) => onChange(event.target.value)}>
        {optionElems(options)}
      </select>
    </label>
      : <div></div>
  },
  componentDidMount() {
    let { options, onChange, value} = this.props
    if (options.length == 1 && value !== options[0].koodiarvo) {
      onChange(options[0].koodiarvo)
    }
  }
})