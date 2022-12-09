const numberToString = (s, scale) => {
  if (s === undefined) return s
  if (scale === undefined || typeof scale !== 'number') {
    return (Math.round(s * 100) / 100).toString().replace('.', ',')
  } else {
    return (Math.round(s * 100) / 100).toFixed(scale).replace('.', ',')
  }
}

export { numberToString }
