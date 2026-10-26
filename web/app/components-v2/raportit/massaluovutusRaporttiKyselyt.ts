import { MassaluovutusQueryParameters } from '../../types/fi/oph/koski/massaluovutus/MassaluovutusQueryParameters'
import {
  isMassaluovutusQueryAmmatillinenOpiskelijavuositiedot,
  MassaluovutusQueryAmmatillinenOpiskelijavuositiedot
} from '../../types/fi/oph/koski/massaluovutus/raportit/MassaluovutusQueryAmmatillinenOpiskelijavuositiedot'
import {
  isMassaluovutusQueryAmmatillinenOsittainenSuoritustiedot,
  MassaluovutusQueryAmmatillinenOsittainenSuoritustiedot
} from '../../types/fi/oph/koski/massaluovutus/raportit/MassaluovutusQueryAmmatillinenOsittainenSuoritustiedot'
import {
  isMassaluovutusQueryAmmatillinenTutkintoSuoritustiedot,
  MassaluovutusQueryAmmatillinenTutkintoSuoritustiedot
} from '../../types/fi/oph/koski/massaluovutus/raportit/MassaluovutusQueryAmmatillinenTutkintoSuoritustiedot'
import {
  isMassaluovutusQueryMuuAmmatillinen,
  MassaluovutusQueryMuuAmmatillinen
} from '../../types/fi/oph/koski/massaluovutus/raportit/MassaluovutusQueryMuuAmmatillinen'
import {
  isMassaluovutusQueryMuuKuinSaanneltyKoulutus,
  MassaluovutusQueryMuuKuinSaanneltyKoulutus
} from '../../types/fi/oph/koski/massaluovutus/raportit/MassaluovutusQueryMuuKuinSaanneltyKoulutus'
import {
  isMassaluovutusQueryTOPKSAmmatillinen,
  MassaluovutusQueryTOPKSAmmatillinen
} from '../../types/fi/oph/koski/massaluovutus/raportit/MassaluovutusQueryTOPKSAmmatillinen'
import {
  isMassaluovutusQueryVSTKOPS,
  MassaluovutusQueryVSTKOPS
} from '../../types/fi/oph/koski/massaluovutus/raportit/MassaluovutusQueryVSTKOPS'
import {
  isMassaluovutusQueryVSTOsaamismerkit,
  MassaluovutusQueryVSTOsaamismerkit
} from '../../types/fi/oph/koski/massaluovutus/raportit/MassaluovutusQueryVSTOsaamismerkit'
import {
  isMassaluovutusQueryVSTVapaatavoitteinen,
  MassaluovutusQueryVSTVapaatavoitteinen
} from '../../types/fi/oph/koski/massaluovutus/raportit/MassaluovutusQueryVSTVapaatavoitteinen'

export type RaporttiKyselynParametrit = {
  organisaatioOid: string
  alku: string
  loppu: string
  osasuoritustenAikarajaus?: boolean
  language: 'fi' | 'sv' | 'en'
}

export type RaporttiKyselynTiedot = {
  organisaatioOid?: string
  alku: string
  loppu: string
  osasuoritustenAikarajaus?: boolean
}

export type MassaluovutusKyselymalli = {
  luo: (parametrit: RaporttiKyselynParametrit) => MassaluovutusQueryParameters
  // Palauttaa null, jos kysely ei ole tämän raportin kysely
  lue: (kysely: MassaluovutusQueryParameters) => RaporttiKyselynTiedot | null
  näytäOsasuoritustenRajaus: boolean
}

export const ammatillinenTutkintoSuoritustiedotKyselymalli: MassaluovutusKyselymalli =
  {
    luo: (p) =>
      MassaluovutusQueryAmmatillinenTutkintoSuoritustiedot({
        organisaatioOid: p.organisaatioOid,
        language: p.language,
        alku: p.alku,
        loppu: p.loppu,
        osasuoritustenAikarajaus: p.osasuoritustenAikarajaus ?? false
      }),
    lue: (q) =>
      isMassaluovutusQueryAmmatillinenTutkintoSuoritustiedot(q)
        ? {
            organisaatioOid: q.organisaatioOid,
            alku: q.alku,
            loppu: q.loppu,
            osasuoritustenAikarajaus: q.osasuoritustenAikarajaus
          }
        : null,
    näytäOsasuoritustenRajaus: true
  }

export const ammatillinenOsittainenSuoritustiedotKyselymalli: MassaluovutusKyselymalli =
  {
    luo: (p) =>
      MassaluovutusQueryAmmatillinenOsittainenSuoritustiedot({
        organisaatioOid: p.organisaatioOid,
        language: p.language,
        alku: p.alku,
        loppu: p.loppu,
        osasuoritustenAikarajaus: p.osasuoritustenAikarajaus ?? false
      }),
    lue: (q) =>
      isMassaluovutusQueryAmmatillinenOsittainenSuoritustiedot(q)
        ? {
            organisaatioOid: q.organisaatioOid,
            alku: q.alku,
            loppu: q.loppu,
            osasuoritustenAikarajaus: q.osasuoritustenAikarajaus
          }
        : null,
    näytäOsasuoritustenRajaus: true
  }

export const ammatillinenOpiskelijavuositiedotKyselymalli: MassaluovutusKyselymalli =
  {
    luo: (p) =>
      MassaluovutusQueryAmmatillinenOpiskelijavuositiedot({
        organisaatioOid: p.organisaatioOid,
        language: p.language,
        alku: p.alku,
        loppu: p.loppu
      }),
    lue: (q) =>
      isMassaluovutusQueryAmmatillinenOpiskelijavuositiedot(q)
        ? { organisaatioOid: q.organisaatioOid, alku: q.alku, loppu: q.loppu }
        : null,
    näytäOsasuoritustenRajaus: false
  }

export const muuAmmatillinenKyselymalli: MassaluovutusKyselymalli = {
  luo: (p) =>
    MassaluovutusQueryMuuAmmatillinen({
      organisaatioOid: p.organisaatioOid,
      language: p.language,
      alku: p.alku,
      loppu: p.loppu
    }),
  lue: (q) =>
    isMassaluovutusQueryMuuAmmatillinen(q)
      ? { organisaatioOid: q.organisaatioOid, alku: q.alku, loppu: q.loppu }
      : null,
  näytäOsasuoritustenRajaus: false
}

export const topksAmmatillinenKyselymalli: MassaluovutusKyselymalli = {
  luo: (p) =>
    MassaluovutusQueryTOPKSAmmatillinen({
      organisaatioOid: p.organisaatioOid,
      language: p.language,
      alku: p.alku,
      loppu: p.loppu
    }),
  lue: (q) =>
    isMassaluovutusQueryTOPKSAmmatillinen(q)
      ? { organisaatioOid: q.organisaatioOid, alku: q.alku, loppu: q.loppu }
      : null,
  näytäOsasuoritustenRajaus: false
}

export const muuKuinSaanneltyKoulutusKyselymalli: MassaluovutusKyselymalli = {
  luo: (p) =>
    MassaluovutusQueryMuuKuinSaanneltyKoulutus({
      organisaatioOid: p.organisaatioOid,
      language: p.language,
      alku: p.alku,
      loppu: p.loppu
    }),
  lue: (q) =>
    isMassaluovutusQueryMuuKuinSaanneltyKoulutus(q)
      ? { organisaatioOid: q.organisaatioOid, alku: q.alku, loppu: q.loppu }
      : null,
  näytäOsasuoritustenRajaus: false
}

export const vstVapaatavoitteinenKyselymalli: MassaluovutusKyselymalli = {
  luo: (p) =>
    MassaluovutusQueryVSTVapaatavoitteinen({
      organisaatioOid: p.organisaatioOid,
      language: p.language,
      alku: p.alku,
      loppu: p.loppu
    }),
  lue: (q) =>
    isMassaluovutusQueryVSTVapaatavoitteinen(q)
      ? { organisaatioOid: q.organisaatioOid, alku: q.alku, loppu: q.loppu }
      : null,
  näytäOsasuoritustenRajaus: false
}

export const vstKopsKyselymalli: MassaluovutusKyselymalli = {
  luo: (p) =>
    MassaluovutusQueryVSTKOPS({
      organisaatioOid: p.organisaatioOid,
      language: p.language,
      alku: p.alku,
      loppu: p.loppu
    }),
  lue: (q) =>
    isMassaluovutusQueryVSTKOPS(q)
      ? { organisaatioOid: q.organisaatioOid, alku: q.alku, loppu: q.loppu }
      : null,
  näytäOsasuoritustenRajaus: false
}

export const vstOsaamismerkitKyselymalli: MassaluovutusKyselymalli = {
  luo: (p) =>
    MassaluovutusQueryVSTOsaamismerkit({
      organisaatioOid: p.organisaatioOid,
      language: p.language,
      alku: p.alku,
      loppu: p.loppu
    }),
  lue: (q) =>
    isMassaluovutusQueryVSTOsaamismerkit(q)
      ? { organisaatioOid: q.organisaatioOid, alku: q.alku, loppu: q.loppu }
      : null,
  näytäOsasuoritustenRajaus: false
}
