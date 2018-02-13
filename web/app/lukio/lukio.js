const perusteenDiaarinumeroToOppimäärä = diaarinumero => {
  switch (diaarinumero) {
    case '60/011/2015':
    case '33/011/2003':
      return 'nuortenops'
    case '70/011/2015':
    case '4/011/2004':
      return 'aikuistenops'
  }
}

export {
  perusteenDiaarinumeroToOppimäärä
}
