export default ({suoritusAtom, oppilaitosAtom}) => {
  const makeSuoritus = (oppilaitos) => {
    if (oppilaitos) {
      return {
        koulutusmoduuli: {
          tunniste: {
            koodiarvo: '020075',
            koodistoUri: 'koulutus'
          }
        },
        toimipiste: oppilaitos,
        tila: { koodistoUri: 'suorituksentila', koodiarvo: 'KESKEN'},
        tyyppi: { koodistoUri: 'suorituksentyyppi', koodiarvo: 'perusopetuksenlisaopetus'}
      }
    }
  }
  oppilaitosAtom.map(makeSuoritus).onValue(suoritus => suoritusAtom.set(suoritus))
  return null
}