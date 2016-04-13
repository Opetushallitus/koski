import React from 'react'

// Shows <select> if more than 1 option. If 1 option and autoselect == true, automatically selects it and hides dropdown. If zero options, hides the dropdown.
export default React.createClass({
  render() {
    let { title, options, value, onChange, className, autoselect } = this.props
    let withEmptyValue = (xs) => [{ koodiarvo: '', nimi: 'Valitse...'}].concat(xs)
    let optionElems = opts => withEmptyValue(opts).map(s => <option key={s.koodiarvo} value={s.koodiarvo}>{s.nimi && s.nimi.fi ? s.nimi.fi : s.koodiarvo}</option>)

    return (!autoselect && options.length >= 1) || options.length > 1
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
    let { options, onChange, value, autoselect } = this.props
    if (autoselect && options.length == 1 && value !== options[0].koodiarvo) {
      onChange(options[0].koodiarvo)
    }
  }
})