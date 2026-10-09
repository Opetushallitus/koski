import { MassaluovutusQueryParameters } from '../../types/fi/oph/koski/massaluovutus/MassaluovutusQueryParameters'
import {
  isMassaluovutusQueryAmmatillinenTutkintoSuoritustiedot,
  MassaluovutusQueryAmmatillinenTutkintoSuoritustiedot
} from '../../types/fi/oph/koski/massaluovutus/raportit/MassaluovutusQueryAmmatillinenTutkintoSuoritustiedot'
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
