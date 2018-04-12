import {modelData} from '../../editor/EditorModel'

const suoritusIdProto = {
  lähdejärjestelmänId: undefined,
  oppilaitosOid: undefined,
  suorituksenTyyppi: undefined,
  koulutusmoduulinTunniste: undefined,
  toString: function() { return [this.oppilaitosOid, this.suorituksenTyyppi, this.koulutusmoduulinTunniste].join('__') }
}

export default (opiskeluoikeus, suoritus) => {
  const id = Object.create(suoritusIdProto)

  id.lähdejärjestelmänId = modelData(opiskeluoikeus, 'lähdejärjestelmänId.id')
  id.oppilaitosOid = modelData(opiskeluoikeus, 'oppilaitos.oid')
  id.suorituksenTyyppi = modelData(suoritus, 'tyyppi.koodiarvo')
  id.koulutusmoduulinTunniste = modelData(suoritus, 'koulutusmoduuli.tunniste.koodiarvo')

  return id
}
