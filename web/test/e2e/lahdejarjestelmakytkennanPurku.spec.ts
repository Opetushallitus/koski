/* eslint-disable mocha/no-identical-title */
import { Oppija } from '../../app/types/fi/oph/koski/schema/Oppija'
import { Raw } from '../../app/util/schema'
import { test, expect } from './base'
import { virkailija } from './setup/auth'

test.describe('Lähdejärjestelmäkytkennän purku', () => {
    test.describe('Vanha käyttöliittymäteknologia (lukio)', () => {
        test.describe('Pääkäyttäjänä', () => {
            test.use({ storageState: virkailija('pää') })
            test.beforeEach(async ({ fixtures }) => {
                await fixtures.reset()
            })

            test("Oppijan lähdejärjestelmäkytkentä on purettavissa (happy path)", async ({ fixtures, oppijaPage }) => {
                const oppija = await fixtures.putOppija(lukioPäättötodistusLähdejärjestelmästä)
                await oppijaPage.goto(oppija.henkilö.oid)

                await expect(oppijaPage.muokkausNäkymäBtn).not.toBeVisible()
                await oppijaPage.puraLähdejärjestelmäkytkentä.click()
                await oppijaPage.vahvistaLähdejärjestelmäkytkentäButton.click()
                await expect(oppijaPage.muokkausNäkymäBtn).toBeVisible()
            })

            test("Keskeneräisessä opiskeluoikeudessa ei näytetä nappia", async ({ fixtures, oppijaPage }) => {
                const oppija = await fixtures.putOppija(lukioUusiLähdejärjestelmästä)
                await oppijaPage.goto(oppija.henkilö.oid)

                await expect(oppijaPage.muokkausNäkymäBtn).not.toBeVisible()
                await expect(oppijaPage.puraLähdejärjestelmäkytkentä).not.toBeVisible()
            })
        })

        test.describe('Tavallisena oppilaitoskäyttäjänä', () => {
            test.use({ storageState: virkailija('jyväs-palvelu') })
            test.beforeEach(async ({ fixtures }) => {
                await fixtures.reset()
            })

            test("Purkunappia ei näytetä", async ({ fixtures, oppijaPage }) => {
                const oppija = await fixtures.putOppija(lukioPäättötodistusLähdejärjestelmästä)
                await oppijaPage.goto(oppija.henkilö.oid)

                await expect(oppijaPage.muokkausNäkymäBtn).not.toBeVisible()
                await expect(oppijaPage.puraLähdejärjestelmäkytkentä).not.toBeVisible()
            })
        })
    })

    test.describe('Uusi käyttöliittymäteknologia (taiteen perusopetus)', () => {
        test.describe('Pääkäyttäjänä', () => {
            test.use({ storageState: virkailija('pää') })
            test.beforeEach(async ({ fixtures }) => {
                await fixtures.reset()
            })

            test("Oppijan lähdejärjestelmäkytkentä on purettavissa (happy path)", async ({ fixtures, oppijaPage }) => {
                const oppija = await fixtures.putOppija(suoritettuTaiteenPerusopetusLähdejärjestelmästä)
                await oppijaPage.goto(oppija.henkilö.oid)

                await expect(oppijaPage.muokkausNäkymäBtn).not.toBeVisible()
                await oppijaPage.puraLähdejärjestelmäkytkentä.click()
                await oppijaPage.vahvistaLähdejärjestelmäkytkentäButton.click()
                await expect(oppijaPage.muokkausNäkymäBtn).toBeVisible()
            })

            test("Keskeneräisessä opiskeluoikeudessa ei näytetä nappia", async ({ fixtures, oppijaPage }) => {
                const oppija = await fixtures.putOppija(keskeneräinenTaiteenperusopetusLähdejärjestelmästä)
                await oppijaPage.goto(oppija.henkilö.oid)

                await expect(oppijaPage.muokkausNäkymäBtn).not.toBeVisible()
                await expect(oppijaPage.puraLähdejärjestelmäkytkentä).not.toBeVisible()
            })
        })

        test.describe('Tavallisena oppilaitoskäyttäjänä', () => {
            test.use({ storageState: virkailija('varsinaissuomi-tallentaja') })
            test.beforeEach(async ({ fixtures }) => {
                await fixtures.reset()
            })

            test("Purkunappia ei näytetä", async ({ fixtures, oppijaPage }) => {
                const oppija = await fixtures.putOppija(suoritettuTaiteenPerusopetusLähdejärjestelmästä)
                await oppijaPage.goto(oppija.henkilö.oid)

                await expect(oppijaPage.muokkausNäkymäBtn).not.toBeVisible()
                await expect(oppijaPage.puraLähdejärjestelmäkytkentä).not.toBeVisible()
            })
        })
    })
})

const lukioPäättötodistusLähdejärjestelmästä: Raw<Oppija> = {
    "henkilö": {
        "hetu": "010106A480F",
        "etunimet": "Lilli",
        "kutsumanimi": "Lilli",
        "sukunimi": "Purkavainen"
    },
    "opiskeluoikeudet": [
        {
            "lähdejärjestelmänId": {
                "id": "123456",
                "lähdejärjestelmä": {
                    "koodiarvo": "primus",
                    "koodistoUri": "lahdejarjestelma"
                }
            },
            "oppilaitos": {
                "oid": "1.2.246.562.10.14613773812",
                "oppilaitosnumero": {
                    "koodiarvo": "00204",
                    "nimi": {
                        "fi": "Jyväskylän normaalikoulu",
                        "sv": "Jyväskylän normaalikoulu",
                        "en": "Jyväskylän normaalikoulu"
                    },
                    "lyhytNimi": {
                        "fi": "Jyväskylän normaalikoulu",
                        "sv": "Jyväskylän normaalikoulu",
                        "en": "Jyväskylän normaalikoulu"
                    },
                    "koodistoUri": "oppilaitosnumero"
                },
                "nimi": {
                    "fi": "Jyväskylän normaalikoulu",
                    "sv": "Jyväskylän normaalikoulu",
                    "en": "Jyväskylän normaalikoulu"
                },
                "kotipaikka": {
                    "koodiarvo": "179",
                    "nimi": {
                        "fi": "Jyväskylä",
                        "sv": "Jyväskylä"
                    },
                    "koodistoUri": "kunta"
                }
            },
            "tila": {
                "opiskeluoikeusjaksot": [
                    {
                        "alku": "2015-09-01",
                        "tila": {
                            "koodiarvo": "lasna",
                            "nimi": {
                                "fi": "Läsnä"
                            },
                            "koodistoUri": "koskiopiskeluoikeudentila",
                            "koodistoVersio": 1
                        },
                        "opintojenRahoitus": {
                            "koodiarvo": "1",
                            "koodistoUri": "opintojenrahoitus"
                        }
                    },
                    {
                        "alku": "2016-08-10",
                        "tila": {
                            "koodiarvo": "valmistunut",
                            "nimi": {
                                "fi": "Valmistunut"
                            },
                            "koodistoUri": "koskiopiskeluoikeudentila",
                            "koodistoVersio": 1
                        },
                        "opintojenRahoitus": {
                            "koodiarvo": "1",
                            "koodistoUri": "opintojenrahoitus"
                        }
                    }
                ]
            },
            "suoritukset": [
                {
                    "koulutusmoduuli": {
                        "tunniste": {
                            "koodiarvo": "HI",
                            "koodistoUri": "koskioppiaineetyleissivistava"
                        },
                        "pakollinen": true,
                        "perusteenDiaarinumero": "60/011/2015"
                    },
                    "toimipiste": {
                        "oid": "1.2.246.562.10.14613773812",
                        "oppilaitosnumero": {
                            "koodiarvo": "00204",
                            "nimi": {
                                "fi": "Jyväskylän normaalikoulu",
                                "sv": "Jyväskylän normaalikoulu",
                                "en": "Jyväskylän normaalikoulu"
                            },
                            "lyhytNimi": {
                                "fi": "Jyväskylän normaalikoulu",
                                "sv": "Jyväskylän normaalikoulu",
                                "en": "Jyväskylän normaalikoulu"
                            },
                            "koodistoUri": "oppilaitosnumero"
                        },
                        "nimi": {
                            "fi": "Jyväskylän normaalikoulu",
                            "sv": "Jyväskylän normaalikoulu",
                            "en": "Jyväskylän normaalikoulu"
                        },
                        "kotipaikka": {
                            "koodiarvo": "179",
                            "nimi": {
                                "fi": "Jyväskylä",
                                "sv": "Jyväskylä"
                            },
                            "koodistoUri": "kunta"
                        }
                    },
                    "arviointi": [
                        {
                            "arvosana": {
                                "koodiarvo": "9",
                                "koodistoUri": "arviointiasteikkoyleissivistava"
                            },
                            "hyväksytty": true
                        }
                    ],
                    "vahvistus": {
                        "päivä": "2016-01-10",
                        "paikkakunta": {
                            "koodiarvo": "179",
                            "nimi": {
                                "fi": "Jyväskylä"
                            },
                            "koodistoUri": "kunta"
                        },
                        "myöntäjäOrganisaatio": {
                            "oid": "1.2.246.562.10.14613773812",
                            "oppilaitosnumero": {
                                "koodiarvo": "00204",
                                "nimi": {
                                    "fi": "Jyväskylän normaalikoulu",
                                    "sv": "Jyväskylän normaalikoulu",
                                    "en": "Jyväskylän normaalikoulu"
                                },
                                "lyhytNimi": {
                                    "fi": "Jyväskylän normaalikoulu",
                                    "sv": "Jyväskylän normaalikoulu",
                                    "en": "Jyväskylän normaalikoulu"
                                },
                                "koodistoUri": "oppilaitosnumero"
                            },
                            "nimi": {
                                "fi": "Jyväskylän normaalikoulu",
                                "sv": "Jyväskylän normaalikoulu",
                                "en": "Jyväskylän normaalikoulu"
                            },
                            "kotipaikka": {
                                "koodiarvo": "179",
                                "nimi": {
                                    "fi": "Jyväskylä",
                                    "sv": "Jyväskylä"
                                },
                                "koodistoUri": "kunta"
                            }
                        },
                        "myöntäjäHenkilöt": [
                            {
                                "nimi": "Reijo Reksi",
                                "titteli": {
                                    "fi": "rehtori"
                                },
                                "organisaatio": {
                                    "oid": "1.2.246.562.10.14613773812",
                                    "oppilaitosnumero": {
                                        "koodiarvo": "00204",
                                        "nimi": {
                                            "fi": "Jyväskylän normaalikoulu",
                                            "sv": "Jyväskylän normaalikoulu",
                                            "en": "Jyväskylän normaalikoulu"
                                        },
                                        "lyhytNimi": {
                                            "fi": "Jyväskylän normaalikoulu",
                                            "sv": "Jyväskylän normaalikoulu",
                                            "en": "Jyväskylän normaalikoulu"
                                        },
                                        "koodistoUri": "oppilaitosnumero"
                                    },
                                    "nimi": {
                                        "fi": "Jyväskylän normaalikoulu",
                                        "sv": "Jyväskylän normaalikoulu",
                                        "en": "Jyväskylän normaalikoulu"
                                    },
                                    "kotipaikka": {
                                        "koodiarvo": "179",
                                        "nimi": {
                                            "fi": "Jyväskylä",
                                            "sv": "Jyväskylä"
                                        },
                                        "koodistoUri": "kunta"
                                    }
                                }
                            }
                        ]
                    },
                    "suorituskieli": {
                        "koodiarvo": "FI",
                        "nimi": {
                            "fi": "suomi"
                        },
                        "koodistoUri": "kieli"
                    },
                    "osasuoritukset": [
                        {
                            "koulutusmoduuli": {
                                "tunniste": {
                                    "koodiarvo": "HI1",
                                    "koodistoUri": "lukionkurssitops2003nuoret"
                                },
                                "laajuus": {
                                    "arvo": 1,
                                    "yksikkö": {
                                        "koodiarvo": "4",
                                        "koodistoUri": "opintojenlaajuusyksikko"
                                    }
                                },
                                "kurssinTyyppi": {
                                    "koodiarvo": "pakollinen",
                                    "koodistoUri": "lukionkurssintyyppi"
                                }
                            },
                            "arviointi": [
                                {
                                    "arvosana": {
                                        "koodiarvo": "7",
                                        "koodistoUri": "arviointiasteikkoyleissivistava"
                                    },
                                    "päivä": "2016-01-10",
                                    "hyväksytty": true
                                }
                            ],
                            "tyyppi": {
                                "koodiarvo": "lukionkurssi",
                                "koodistoUri": "suorituksentyyppi"
                            }
                        },
                        {
                            "koulutusmoduuli": {
                                "tunniste": {
                                    "koodiarvo": "HI2",
                                    "koodistoUri": "lukionkurssit"
                                },
                                "laajuus": {
                                    "arvo": 1,
                                    "yksikkö": {
                                        "koodiarvo": "4",
                                        "koodistoUri": "opintojenlaajuusyksikko"
                                    }
                                },
                                "kurssinTyyppi": {
                                    "koodiarvo": "pakollinen",
                                    "koodistoUri": "lukionkurssintyyppi"
                                }
                            },
                            "arviointi": [
                                {
                                    "arvosana": {
                                        "koodiarvo": "8",
                                        "koodistoUri": "arviointiasteikkoyleissivistava"
                                    },
                                    "päivä": "2016-01-10",
                                    "hyväksytty": true
                                }
                            ],
                            "tyyppi": {
                                "koodiarvo": "lukionkurssi",
                                "koodistoUri": "suorituksentyyppi"
                            }
                        },
                        {
                            "koulutusmoduuli": {
                                "tunniste": {
                                    "koodiarvo": "HI3",
                                    "koodistoUri": "lukionkurssit"
                                },
                                "laajuus": {
                                    "arvo": 1,
                                    "yksikkö": {
                                        "koodiarvo": "4",
                                        "koodistoUri": "opintojenlaajuusyksikko"
                                    }
                                },
                                "kurssinTyyppi": {
                                    "koodiarvo": "pakollinen",
                                    "koodistoUri": "lukionkurssintyyppi"
                                }
                            },
                            "arviointi": [
                                {
                                    "arvosana": {
                                        "koodiarvo": "7",
                                        "koodistoUri": "arviointiasteikkoyleissivistava"
                                    },
                                    "päivä": "2016-01-10",
                                    "hyväksytty": true
                                }
                            ],
                            "tyyppi": {
                                "koodiarvo": "lukionkurssi",
                                "koodistoUri": "suorituksentyyppi"
                            }
                        },
                        {
                            "koulutusmoduuli": {
                                "tunniste": {
                                    "koodiarvo": "HI4",
                                    "koodistoUri": "lukionkurssit"
                                },
                                "laajuus": {
                                    "arvo": 1,
                                    "yksikkö": {
                                        "koodiarvo": "4",
                                        "koodistoUri": "opintojenlaajuusyksikko"
                                    }
                                },
                                "kurssinTyyppi": {
                                    "koodiarvo": "pakollinen",
                                    "koodistoUri": "lukionkurssintyyppi"
                                }
                            },
                            "arviointi": [
                                {
                                    "arvosana": {
                                        "koodiarvo": "6",
                                        "koodistoUri": "arviointiasteikkoyleissivistava"
                                    },
                                    "päivä": "2016-01-10",
                                    "hyväksytty": true
                                }
                            ],
                            "tyyppi": {
                                "koodiarvo": "lukionkurssi",
                                "koodistoUri": "suorituksentyyppi"
                            }
                        }
                    ],
                    "tyyppi": {
                        "koodiarvo": "lukionoppiaineenoppimaara",
                        "koodistoUri": "suorituksentyyppi"
                    }
                }
            ],
            "tyyppi": {
                "koodiarvo": "lukiokoulutus",
                "koodistoUri": "opiskeluoikeudentyyppi"
            },
            "alkamispäivä": "2015-09-01",
            "päättymispäivä": "2016-08-10"
        }
    ]
}

const lukioUusiLähdejärjestelmästä: Raw<Oppija> = {
    "henkilö": {
        "hetu": "280618-402H",
        "etunimet": "Aarne",
        "kutsumanimi": "Aarne",
        "sukunimi": "Ammattilainen"
    },
    "opiskeluoikeudet": [
        {
            "lähdejärjestelmänId": {
                "id": "123456",
                "lähdejärjestelmä": {
                    "koodiarvo": "primus",
                    "koodistoUri": "lahdejarjestelma"
                }
            },
            "oppilaitos": {
                "oid": "1.2.246.562.10.52251087186"
            },
            "arvioituPäättymispäivä": "2020-05-01",
            "ostettu": false,
            "tila": {
                "opiskeluoikeusjaksot": [
                    {
                        "alku": "2016-09-01",
                        "tila": {
                            "koodiarvo": "lasna",
                            "nimi": {
                                "fi": "Läsnä"
                            },
                            "koodistoUri": "koskiopiskeluoikeudentila",
                            "koodistoVersio": 1
                        },
                        "opintojenRahoitus": {
                            "koodiarvo": "1",
                            "koodistoUri": "opintojenrahoitus"
                        }
                    }
                ]
            },
            "suoritukset": [
                {
                    "koulutusmoduuli": {
                        "tunniste": {
                            "koodiarvo": "351301",
                            "koodistoUri": "koulutus",
                            "koodistoVersio": 11
                        },
                        "perusteenDiaarinumero": "39/011/2014"
                    },
                    "suoritustapa": {
                        "koodiarvo": "ops",
                        "nimi": {
                            "fi": "Ammatillinen perustutkinto"
                        },
                        "koodistoUri": "ammatillisentutkinnonsuoritustapa",
                        "koodistoVersio": 1
                    },
                    "toimipiste": {
                        "oid": "1.2.246.562.10.42456023292",
                        "nimi": {
                            "fi": "Stadin ammattiopisto, Lehtikuusentien toimipaikka"
                        }
                    },
                    "alkamispäivä": "2016-09-01",
                    "suorituskieli": {
                        "koodiarvo": "FI",
                        "nimi": {
                            "fi": "suomi"
                        },
                        "koodistoUri": "kieli"
                    },
                    "tyyppi": {
                        "koodiarvo": "ammatillinentutkinto",
                        "koodistoUri": "suorituksentyyppi"
                    }
                }
            ],
            "tyyppi": {
                "koodiarvo": "ammatillinenkoulutus",
                "koodistoUri": "opiskeluoikeudentyyppi"
            },
            "alkamispäivä": "2016-09-01"
        }
    ]
}

const suoritettuTaiteenPerusopetusLähdejärjestelmästä: Raw<Oppija> = {
    "henkilö": {
        "hetu": "010106A4351",
        "etunimet": "Pentti",
        "kutsumanimi": "Pentti",
        "sukunimi": "Purkutaiteilija"
    },
    "opiskeluoikeudet": [
        {
            "lähdejärjestelmänId": {
                "id": "123456",
                "lähdejärjestelmä": {
                    "koodiarvo": "primus",
                    "koodistoUri": "lahdejarjestelma"
                }
            },
            "oppilaitos": {
                "oid": "1.2.246.562.10.31915273374",
                "oppilaitosnumero": {
                    "koodiarvo": "01694",
                    "koodistoUri": "oppilaitosnumero"
                },
                "nimi": {
                    "fi": "Varsinais-Suomen kansanopisto"
                }
            },
            "koulutustoimija": {
                "oid": "1.2.246.562.10.44330177021",
                "nimi": {
                    "fi": "Varsinais-Suomen Aikuiskoulutussäätiö sr"
                },
                "yTunnus": "0136193-2",
                "kotipaikka": {
                    "koodiarvo": "577",
                    "koodistoUri": "kunta"
                }
            },
            "tila": {
                "opiskeluoikeusjaksot": [
                    {
                        "alku": "2021-01-01",
                        "tila": {
                            "koodiarvo": "lasna",
                            "koodistoUri": "koskiopiskeluoikeudentila"
                        }
                    },
                    {
                        "alku": "2022-01-01",
                        "tila": {
                            "koodiarvo": "hyvaksytystisuoritettu",
                            "koodistoUri": "koskiopiskeluoikeudentila"
                        }
                    }
                ]
            },
            "oppimäärä": {
                "koodiarvo": "laajaoppimaara",
                "koodistoUri": "taiteenperusopetusoppimaara"
            },
            "koulutuksenToteutustapa": {
                "koodiarvo": "itsejarjestettykoulutus",
                "koodistoUri": "taiteenperusopetuskoulutuksentoteutustapa"
            },
            "suoritukset": [
                {
                    "koulutusmoduuli": {
                        "tunniste": {
                            "koodiarvo": "999907",
                            "koodistoUri": "koulutus"
                        },
                        "taiteenala": {
                            "koodiarvo": "musiikki",
                            "koodistoUri": "taiteenperusopetustaiteenala"
                        },
                        "laajuus": {
                            "arvo": 29.6,
                            "yksikkö": {
                                "koodiarvo": "2",
                                "nimi": {
                                    "fi": "opintopistettä",
                                    "sv": "studiepoäng",
                                    "en": "ECTS credits"
                                },
                                "lyhytNimi": {
                                    "fi": "op",
                                    "sv": "sp",
                                    "en": "ECTS cr"
                                },
                                "koodistoUri": "opintojenlaajuusyksikko"
                            }
                        },
                        "perusteenDiaarinumero": "OPH-2068-2017"
                    },
                    "toimipiste": {
                        "oid": "1.2.246.562.10.78513447389"
                    },
                    "vahvistus": {
                        "päivä": "2021-01-01",
                        "paikkakunta": {
                            "koodiarvo": "091",
                            "nimi": {
                                "fi": "Helsinki"
                            },
                            "koodistoUri": "kunta"
                        },
                        "myöntäjäOrganisaatio": {
                            "oid": "1.2.246.562.10.31915273374",
                            "oppilaitosnumero": {
                                "koodiarvo": "01694",
                                "koodistoUri": "oppilaitosnumero"
                            },
                            "nimi": {
                                "fi": "Varsinais-Suomen kansanopisto"
                            }
                        },
                        "myöntäjäHenkilöt": [
                            {
                                "nimi": "Musa Ope",
                                "titteli": {
                                    "fi": "Opettaja"
                                },
                                "organisaatio": {
                                    "oid": "1.2.246.562.10.31915273374",
                                    "oppilaitosnumero": {
                                        "koodiarvo": "01694",
                                        "koodistoUri": "oppilaitosnumero"
                                    },
                                    "nimi": {
                                        "fi": "Varsinais-Suomen kansanopisto"
                                    }
                                }
                            }
                        ]
                    },
                    "tyyppi": {
                        "koodiarvo": "taiteenperusopetuksenlaajanoppimaaranperusopinnot",
                        "koodistoUri": "suorituksentyyppi"
                    },
                    "osasuoritukset": [
                        {
                            "koulutusmoduuli": {
                                "tunniste": {
                                    "koodiarvo": "musa1",
                                    "nimi": {
                                        "fi": "Musiikin kurssi"
                                    }
                                },
                                "laajuus": {
                                    "arvo": 10,
                                    "yksikkö": {
                                        "koodiarvo": "2",
                                        "nimi": {
                                            "fi": "opintopistettä",
                                            "sv": "studiepoäng",
                                            "en": "ECTS credits"
                                        },
                                        "lyhytNimi": {
                                            "fi": "op",
                                            "sv": "sp",
                                            "en": "ECTS cr"
                                        },
                                        "koodistoUri": "opintojenlaajuusyksikko"
                                    }
                                }
                            },
                            "arviointi": [
                                {
                                    "arvosana": {
                                        "koodiarvo": "hyvaksytty",
                                        "koodistoUri": "arviointiasteikkotaiteenperusopetus"
                                    },
                                    "päivä": "2021-01-01",
                                    "hyväksytty": true
                                }
                            ],
                            "tyyppi": {
                                "koodiarvo": "taiteenperusopetuksenpaikallinenopintokokonaisuus",
                                "koodistoUri": "suorituksentyyppi"
                            },
                            "tunnustettu": {
                                "selite": {
                                    "fi": "Tunnustettu paikallinen opintokokonaisuus"
                                }
                            }
                        },
                        {
                            "koulutusmoduuli": {
                                "tunniste": {
                                    "koodiarvo": "musa2",
                                    "nimi": {
                                        "fi": "Musiikin kurssi"
                                    }
                                },
                                "laajuus": {
                                    "arvo": 10,
                                    "yksikkö": {
                                        "koodiarvo": "2",
                                        "nimi": {
                                            "fi": "opintopistettä",
                                            "sv": "studiepoäng",
                                            "en": "ECTS credits"
                                        },
                                        "lyhytNimi": {
                                            "fi": "op",
                                            "sv": "sp",
                                            "en": "ECTS cr"
                                        },
                                        "koodistoUri": "opintojenlaajuusyksikko"
                                    }
                                }
                            },
                            "arviointi": [
                                {
                                    "arvosana": {
                                        "koodiarvo": "hyvaksytty",
                                        "koodistoUri": "arviointiasteikkotaiteenperusopetus"
                                    },
                                    "päivä": "2021-01-01",
                                    "hyväksytty": true
                                }
                            ],
                            "tyyppi": {
                                "koodiarvo": "taiteenperusopetuksenpaikallinenopintokokonaisuus",
                                "koodistoUri": "suorituksentyyppi"
                            }
                        },
                        {
                            "koulutusmoduuli": {
                                "tunniste": {
                                    "koodiarvo": "musa3",
                                    "nimi": {
                                        "fi": "Musiikin kurssi"
                                    }
                                },
                                "laajuus": {
                                    "arvo": 9.6,
                                    "yksikkö": {
                                        "koodiarvo": "2",
                                        "nimi": {
                                            "fi": "opintopistettä",
                                            "sv": "studiepoäng",
                                            "en": "ECTS credits"
                                        },
                                        "lyhytNimi": {
                                            "fi": "op",
                                            "sv": "sp",
                                            "en": "ECTS cr"
                                        },
                                        "koodistoUri": "opintojenlaajuusyksikko"
                                    }
                                }
                            },
                            "arviointi": [
                                {
                                    "arvosana": {
                                        "koodiarvo": "hyvaksytty",
                                        "koodistoUri": "arviointiasteikkotaiteenperusopetus"
                                    },
                                    "päivä": "2021-01-01",
                                    "hyväksytty": true
                                }
                            ],
                            "tyyppi": {
                                "koodiarvo": "taiteenperusopetuksenpaikallinenopintokokonaisuus",
                                "koodistoUri": "suorituksentyyppi"
                            }
                        }
                    ]
                },
                {
                    "koulutusmoduuli": {
                        "tunniste": {
                            "koodiarvo": "999907",
                            "koodistoUri": "koulutus"
                        },
                        "taiteenala": {
                            "koodiarvo": "musiikki",
                            "koodistoUri": "taiteenperusopetustaiteenala"
                        },
                        "laajuus": {
                            "arvo": 18.5,
                            "yksikkö": {
                                "koodiarvo": "2",
                                "nimi": {
                                    "fi": "opintopistettä",
                                    "sv": "studiepoäng",
                                    "en": "ECTS credits"
                                },
                                "lyhytNimi": {
                                    "fi": "op",
                                    "sv": "sp",
                                    "en": "ECTS cr"
                                },
                                "koodistoUri": "opintojenlaajuusyksikko"
                            }
                        },
                        "perusteenDiaarinumero": "OPH-2068-2017"
                    },
                    "toimipiste": {
                        "oid": "1.2.246.562.10.78513447389"
                    },
                    "vahvistus": {
                        "päivä": "2021-01-01",
                        "paikkakunta": {
                            "koodiarvo": "091",
                            "nimi": {
                                "fi": "Helsinki"
                            },
                            "koodistoUri": "kunta"
                        },
                        "myöntäjäOrganisaatio": {
                            "oid": "1.2.246.562.10.31915273374",
                            "oppilaitosnumero": {
                                "koodiarvo": "01694",
                                "koodistoUri": "oppilaitosnumero"
                            },
                            "nimi": {
                                "fi": "Varsinais-Suomen kansanopisto"
                            }
                        },
                        "myöntäjäHenkilöt": [
                            {
                                "nimi": "Musa Ope",
                                "titteli": {
                                    "fi": "Opettaja"
                                },
                                "organisaatio": {
                                    "oid": "1.2.246.562.10.31915273374",
                                    "oppilaitosnumero": {
                                        "koodiarvo": "01694",
                                        "koodistoUri": "oppilaitosnumero"
                                    },
                                    "nimi": {
                                        "fi": "Varsinais-Suomen kansanopisto"
                                    }
                                }
                            }
                        ]
                    },
                    "tyyppi": {
                        "koodiarvo": "taiteenperusopetuksenlaajanoppimaaransyventavatopinnot",
                        "koodistoUri": "suorituksentyyppi"
                    },
                    "osasuoritukset": [
                        {
                            "koulutusmoduuli": {
                                "tunniste": {
                                    "koodiarvo": "musa1",
                                    "nimi": {
                                        "fi": "Musiikin kurssi"
                                    }
                                },
                                "laajuus": {
                                    "arvo": 10,
                                    "yksikkö": {
                                        "koodiarvo": "2",
                                        "nimi": {
                                            "fi": "opintopistettä",
                                            "sv": "studiepoäng",
                                            "en": "ECTS credits"
                                        },
                                        "lyhytNimi": {
                                            "fi": "op",
                                            "sv": "sp",
                                            "en": "ECTS cr"
                                        },
                                        "koodistoUri": "opintojenlaajuusyksikko"
                                    }
                                }
                            },
                            "arviointi": [
                                {
                                    "arvosana": {
                                        "koodiarvo": "hyvaksytty",
                                        "koodistoUri": "arviointiasteikkotaiteenperusopetus"
                                    },
                                    "päivä": "2021-01-01",
                                    "hyväksytty": true
                                }
                            ],
                            "tyyppi": {
                                "koodiarvo": "taiteenperusopetuksenpaikallinenopintokokonaisuus",
                                "koodistoUri": "suorituksentyyppi"
                            }
                        },
                        {
                            "koulutusmoduuli": {
                                "tunniste": {
                                    "koodiarvo": "musa2",
                                    "nimi": {
                                        "fi": "Musiikin kurssi"
                                    }
                                },
                                "laajuus": {
                                    "arvo": 8.5,
                                    "yksikkö": {
                                        "koodiarvo": "2",
                                        "nimi": {
                                            "fi": "opintopistettä",
                                            "sv": "studiepoäng",
                                            "en": "ECTS credits"
                                        },
                                        "lyhytNimi": {
                                            "fi": "op",
                                            "sv": "sp",
                                            "en": "ECTS cr"
                                        },
                                        "koodistoUri": "opintojenlaajuusyksikko"
                                    }
                                }
                            },
                            "arviointi": [
                                {
                                    "arvosana": {
                                        "koodiarvo": "hyvaksytty",
                                        "koodistoUri": "arviointiasteikkotaiteenperusopetus"
                                    },
                                    "päivä": "2021-01-01",
                                    "hyväksytty": true
                                }
                            ],
                            "tyyppi": {
                                "koodiarvo": "taiteenperusopetuksenpaikallinenopintokokonaisuus",
                                "koodistoUri": "suorituksentyyppi"
                            }
                        }
                    ]
                }
            ],
            "tyyppi": {
                "koodiarvo": "taiteenperusopetus",
                "koodistoUri": "opiskeluoikeudentyyppi"
            },
            "arvioituPäättymispäivä": "2022-01-01",
            "alkamispäivä": "2021-01-01",
            "päättymispäivä": "2022-01-01"
        }
    ]
}

const keskeneräinenTaiteenperusopetusLähdejärjestelmästä: Raw<Oppija> = {
    "henkilö": {
        "hetu": "010106A446C",
        "etunimet": "Petra",
        "kutsumanimi": "Petra",
        "sukunimi": "Taiteilija"
    },
    "opiskeluoikeudet": [
        {
            "lähdejärjestelmänId": {
                "id": "123456",
                "lähdejärjestelmä": {
                    "koodiarvo": "primus",
                    "koodistoUri": "lahdejarjestelma"
                }
            },
            "oppilaitos": {
                "oid": "1.2.246.562.10.31915273374",
                "oppilaitosnumero": {
                    "koodiarvo": "01694",
                    "koodistoUri": "oppilaitosnumero"
                },
                "nimi": {
                    "fi": "Varsinais-Suomen kansanopisto"
                }
            },
            "koulutustoimija": {
                "oid": "1.2.246.562.10.44330177021",
                "nimi": {
                    "fi": "Varsinais-Suomen Aikuiskoulutussäätiö sr"
                },
                "yTunnus": "0136193-2",
                "kotipaikka": {
                    "koodiarvo": "577",
                    "koodistoUri": "kunta"
                }
            },
            "tila": {
                "opiskeluoikeusjaksot": [
                    {
                        "alku": "2021-01-01",
                        "tila": {
                            "koodiarvo": "lasna",
                            "koodistoUri": "koskiopiskeluoikeudentila"
                        }
                    }
                ]
            },
            "oppimäärä": {
                "koodiarvo": "yleinenoppimaara",
                "koodistoUri": "taiteenperusopetusoppimaara"
            },
            "koulutuksenToteutustapa": {
                "koodiarvo": "itsejarjestettykoulutus",
                "koodistoUri": "taiteenperusopetuskoulutuksentoteutustapa"
            },
            "suoritukset": [
                {
                    "koulutusmoduuli": {
                        "tunniste": {
                            "koodiarvo": "999907",
                            "koodistoUri": "koulutus"
                        },
                        "taiteenala": {
                            "koodiarvo": "musiikki",
                            "koodistoUri": "taiteenperusopetustaiteenala"
                        },
                        "perusteenDiaarinumero": "OPH-2069-2017"
                    },
                    "toimipiste": {
                        "oid": "1.2.246.562.10.78513447389"
                    },
                    "tyyppi": {
                        "koodiarvo": "taiteenperusopetuksenyleisenoppimaaranyhteisetopinnot",
                        "koodistoUri": "suorituksentyyppi"
                    }
                },
                {
                    "koulutusmoduuli": {
                        "tunniste": {
                            "koodiarvo": "999907",
                            "koodistoUri": "koulutus"
                        },
                        "taiteenala": {
                            "koodiarvo": "musiikki",
                            "koodistoUri": "taiteenperusopetustaiteenala"
                        },
                        "laajuus": {
                            "arvo": 7.4,
                            "yksikkö": {
                                "koodiarvo": "2",
                                "nimi": {
                                    "fi": "opintopistettä",
                                    "sv": "studiepoäng",
                                    "en": "ECTS credits"
                                },
                                "lyhytNimi": {
                                    "fi": "op",
                                    "sv": "sp",
                                    "en": "ECTS cr"
                                },
                                "koodistoUri": "opintojenlaajuusyksikko"
                            }
                        },
                        "perusteenDiaarinumero": "OPH-2069-2017"
                    },
                    "toimipiste": {
                        "oid": "1.2.246.562.10.78513447389"
                    },
                    "tyyppi": {
                        "koodiarvo": "taiteenperusopetuksenyleisenoppimaaranteemaopinnot",
                        "koodistoUri": "suorituksentyyppi"
                    }
                }
            ],
            "tyyppi": {
                "koodiarvo": "taiteenperusopetus",
                "koodistoUri": "opiskeluoikeudentyyppi"
            },
            "alkamispäivä": "2021-01-01"
        }
    ]
}