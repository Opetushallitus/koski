import { MassaluovutusQueryParameters } from '../../types/fi/oph/koski/massaluovutus/MassaluovutusQueryParameters'
import {
  isMassaluovutusQueryAikuistenPerusopetuksenKurssikertyma,
  MassaluovutusQueryAikuistenPerusopetuksenKurssikertyma
} from '../../types/fi/oph/koski/massaluovutus/raportit/MassaluovutusQueryAikuistenPerusopetuksenKurssikertyma'
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
  isMassaluovutusQueryLukio2019Opintopistekertymat,
  MassaluovutusQueryLukio2019Opintopistekertymat
} from '../../types/fi/oph/koski/massaluovutus/raportit/MassaluovutusQueryLukio2019Opintopistekertymat'
import {
  isMassaluovutusQueryLukioKurssikertymat,
  MassaluovutusQueryLukioKurssikertymat
} from '../../types/fi/oph/koski/massaluovutus/raportit/MassaluovutusQueryLukioKurssikertymat'
import {
  isMassaluovutusQueryLukionSuoritustiedot,
  MassaluovutusQueryLukionSuoritustiedot
} from '../../types/fi/oph/koski/massaluovutus/raportit/MassaluovutusQueryLukionSuoritustiedot'
import {
  isMassaluovutusQueryMuuAmmatillinen,
  MassaluovutusQueryMuuAmmatillinen
} from '../../types/fi/oph/koski/massaluovutus/raportit/MassaluovutusQueryMuuAmmatillinen'
import {
  isMassaluovutusQueryMuuKuinSaanneltyKoulutus,
  MassaluovutusQueryMuuKuinSaanneltyKoulutus
} from '../../types/fi/oph/koski/massaluovutus/raportit/MassaluovutusQueryMuuKuinSaanneltyKoulutus'
import {
  isMassaluovutusQueryPerusopetukseenValmistava,
  MassaluovutusQueryPerusopetukseenValmistava
} from '../../types/fi/oph/koski/massaluovutus/raportit/MassaluovutusQueryPerusopetukseenValmistava'
import {
  isMassaluovutusQueryTOPKSAmmatillinen,
  MassaluovutusQueryTOPKSAmmatillinen
} from '../../types/fi/oph/koski/massaluovutus/raportit/MassaluovutusQueryTOPKSAmmatillinen'
import {
  isMassaluovutusQueryTuvaSuoritustiedot,
  MassaluovutusQueryTuvaSuoritustiedot
} from '../../types/fi/oph/koski/massaluovutus/raportit/MassaluovutusQueryTuvaSuoritustiedot'
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
  kotikuntaPvm?: string
  language: 'fi' | 'sv' | 'en'
}

export type RaporttiKyselynTiedot = {
  organisaatioOid?: string
  alku: string
  loppu: string
  osasuoritustenAikarajaus?: boolean
  kotikuntaPvm?: string
}

export type MassaluovutusKyselymalli = {
  luo: (parametrit: RaporttiKyselynParametrit) => MassaluovutusQueryParameters
  // Palauttaa null, jos kysely ei ole tämän raportin kysely
  lue: (kysely: MassaluovutusQueryParameters) => RaporttiKyselynTiedot | null
  // Annettuna omissa raporteissa näytetään osasuoritusten rajaus tällä otsikolla
  osasuoritustenRajausOtsikko?: string
  näytäKotikuntaPvm?: boolean
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
    osasuoritustenRajausOtsikko: 'Tutkinnon osat'
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
    osasuoritustenRajausOtsikko: 'Tutkinnon osat'
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
        : null
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
      : null
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
      : null
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
      : null
}

export const tuvaSuoritustiedotKyselymalli: MassaluovutusKyselymalli = {
  luo: (p) =>
    MassaluovutusQueryTuvaSuoritustiedot({
      organisaatioOid: p.organisaatioOid,
      language: p.language,
      alku: p.alku,
      loppu: p.loppu
    }),
  lue: (q) =>
    isMassaluovutusQueryTuvaSuoritustiedot(q)
      ? { organisaatioOid: q.organisaatioOid, alku: q.alku, loppu: q.loppu }
      : null
}

export const aikuistenPerusopetuksenKurssikertymaKyselymalli: MassaluovutusKyselymalli =
  {
    luo: (p) =>
      MassaluovutusQueryAikuistenPerusopetuksenKurssikertyma({
        organisaatioOid: p.organisaatioOid,
        language: p.language,
        alku: p.alku,
        loppu: p.loppu
      }),
    lue: (q) =>
      isMassaluovutusQueryAikuistenPerusopetuksenKurssikertyma(q)
        ? { organisaatioOid: q.organisaatioOid, alku: q.alku, loppu: q.loppu }
        : null
  }

export const lukioKurssikertymatKyselymalli: MassaluovutusKyselymalli = {
  luo: (p) =>
    MassaluovutusQueryLukioKurssikertymat({
      organisaatioOid: p.organisaatioOid,
      language: p.language,
      alku: p.alku,
      loppu: p.loppu
    }),
  lue: (q) =>
    isMassaluovutusQueryLukioKurssikertymat(q)
      ? { organisaatioOid: q.organisaatioOid, alku: q.alku, loppu: q.loppu }
      : null
}

export const lukio2019OpintopistekertymatKyselymalli: MassaluovutusKyselymalli =
  {
    luo: (p) =>
      MassaluovutusQueryLukio2019Opintopistekertymat({
        organisaatioOid: p.organisaatioOid,
        language: p.language,
        alku: p.alku,
        loppu: p.loppu
      }),
    lue: (q) =>
      isMassaluovutusQueryLukio2019Opintopistekertymat(q)
        ? { organisaatioOid: q.organisaatioOid, alku: q.alku, loppu: q.loppu }
        : null
  }

export const perusopetukseenValmistavaKyselymalli: MassaluovutusKyselymalli = {
  luo: (p) =>
    MassaluovutusQueryPerusopetukseenValmistava({
      organisaatioOid: p.organisaatioOid,
      language: p.language,
      alku: p.alku,
      loppu: p.loppu
    }),
  lue: (q) =>
    isMassaluovutusQueryPerusopetukseenValmistava(q)
      ? { organisaatioOid: q.organisaatioOid, alku: q.alku, loppu: q.loppu }
      : null
}

export const lukionSuoritustiedotKyselymalli: MassaluovutusKyselymalli = {
  luo: (p) =>
    MassaluovutusQueryLukionSuoritustiedot({
      organisaatioOid: p.organisaatioOid,
      language: p.language,
      alku: p.alku,
      loppu: p.loppu,
      osasuoritustenAikarajaus: p.osasuoritustenAikarajaus ?? false,
      kotikuntaPvm: p.kotikuntaPvm
    }),
  lue: (q) =>
    isMassaluovutusQueryLukionSuoritustiedot(q)
      ? {
          organisaatioOid: q.organisaatioOid,
          alku: q.alku,
          loppu: q.loppu,
          osasuoritustenAikarajaus: q.osasuoritustenAikarajaus,
          kotikuntaPvm: q.kotikuntaPvm
        }
      : null,
  osasuoritustenRajausOtsikko: 'Kurssit',
  näytäKotikuntaPvm: true
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
      : null
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
      : null
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
      : null
}
