const laajuusNumberToString = (s) => {
  if (s == undefined) return s
  return (Math.round(s * 100) / 100).toString().replace('.', ',')
}

export {
  laajuusNumberToString
}
