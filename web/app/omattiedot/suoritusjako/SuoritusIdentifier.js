import {modelData} from '../../editor/EditorModel'

const suoritusIdProto = {
  oppilaitosOid: undefined,
  suorituksenTyyppi: undefined,
  koulutusmoduulinTunniste: undefined,
  toString: function() { return [this.oppilaitosOid, this.suorituksenTyyppi, this.koulutusmoduulinTunniste].join('__') }
}

export default (oppilaitos, suoritus) => {
  const id = Object.create(suoritusIdProto)

  id.oppilaitosOid = modelData(oppilaitos, 'oid')
  id.suorituksenTyyppi = modelData(suoritus, 'tyyppi.koodiarvo')
  id.koulutusmoduulinTunniste = modelData(suoritus, 'koulutusmoduuli.tunniste.koodiarvo')

  return id
}
