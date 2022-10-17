export const VARHAISKASVATUKSEN_TOIMIPAIKKA = 'VARHAISKASVATUKSEN_TOIMIPAIKKA'

export const makeSuoritus = (
  oppilaitos,
  organisaatiotyypit,
  peruste,
  suorituskieli
) => {
  if (oppilaitos) {
    return {
      koulutusmoduuli: {
        tunniste: {
          koodiarvo: tunnisteenKoodiarvo(organisaatiotyypit),
          koodistoUri: 'koulutus'
        },
        perusteenDiaarinumero: peruste
      },
      toimipiste: oppilaitos,
      tyyppi: {
        koodistoUri: 'suorituksentyyppi',
        koodiarvo: 'esiopetuksensuoritus'
      },
      suorituskieli
    }
  }
}

const tunnisteenKoodiarvo = (organisaatioTyypit) =>
  organisaatioTyypit.includes(VARHAISKASVATUKSEN_TOIMIPAIKKA)
    ? '001102'
    : '001101'
