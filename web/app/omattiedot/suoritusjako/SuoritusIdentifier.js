import { modelData } from '../../editor/EditorModel'
import { isOpintojakso } from '../../opiskeluoikeus/OpiskeluoikeusEditor'

const suoritusIdProto = {
  lähdejärjestelmänId: undefined,
  oppilaitosOid: undefined,
  suorituksenTyyppi: undefined,
  koulutusmoduulinTunniste: undefined,
  toString: function () {
    return [
      this.lähdejärjestelmänId,
      this.oppilaitosOid,
      this.suorituksenTyyppi,
      this.koulutusmoduulinTunniste
    ].join('__')
  }
}

export default (opiskeluoikeus, suoritus) => {
  const id = Object.create(suoritusIdProto)

  id.lähdejärjestelmänId = modelData(opiskeluoikeus, 'lähdejärjestelmänId.id')
  id.oppilaitosOid = modelData(opiskeluoikeus, 'oppilaitos.oid')
  id.suorituksenTyyppi = modelData(suoritus, 'tyyppi.koodiarvo')
  // Korkeakoulun opintojakso on päätason suoritus, mutta jaettaessa jaetaan samalla kaikki "kelluvat"
  // opintojaksot (ei siis spesifillä koulutusmoduulin tunnisteella juuri tiettyä opintojaksoa).
  id.koulutusmoduulinTunniste = isOpintojakso(suoritus)
    ? ''
    : modelData(suoritus, 'koulutusmoduuli.tunniste.koodiarvo')

  return id
}
