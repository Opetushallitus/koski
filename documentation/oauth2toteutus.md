# OAuth 2.0:n toteutus Koskessa

Ks. myös [oauth2.md](oauth2.md)

# Sekvenssikaavio toteutuksen toiminnasta

![OAuth 2.0 servletit Koskessa](kuvat/png/oauth2sekvenssiservleteissa.png)
[oauth2sekvenssiservleteissa.puml](kuvat/oauth2sekvenssiservleteissa.puml)
[png/oauth2sekvenssiservleteissa.png](kuvat/png/oauth2sekvenssiservleteissa.png)
[svg/oauth2sekvenssiservleteissa.svg](kuvat/svg/oauth2sekvenssiservleteissa.svg)

    PARAMS:
    client_id             Esim. dvvdigilompakkopk
    redirect_uri          <DVV:n kertoma ja Koskeen rekisteröity callback-URI>
    state
    scope                 Esim. HENKILOTIEDOT_SYNTYMAAIKA HENKILOTIEDOT_NIMI OPISKELUOIKEUDET_SUORITETUT_TUTKINNOT
    response_type         =code
    response_mode         =form_post
    code_challenge_method =S256
    code_challenge
      Virhetilanteissa voi sisältää myös:
    error
    error_id

    RESPONSE_PARAMS:
    client_id
    redirect_uri
    state
    code
      Virhetilanteissa code:n sijasta voi sisältää:
    error
    error_description
    error_uri

    RESPONSE:
    state
    code
      Virhetilanteissa code:n sijasta voi sisältää:
    error
    error_description
    error_uri

    CODE_PARAMS:
    grant_type            =authorization_code
    code
    code_verifier
    redirect_uri
    client_id

    TOKEN_RESPONSE:
    access_token
    token_type        =Bearer
    expires_in

# Testi-osoitteita

localhost testi-uri http://localhost:7021/koski/omadata-oauth2/authorize?client_id=oauth2client&response_type=code&response_mode=form_post&redirect_uri=%2Fkoski%2Fomadata-oauth2%2Fdebug-post-response&code_challenge=NjIyMGQ4NDAxZGM0ZDI5NTdlMWRlNDI2YWNhNjA1NGRiMjQyZTE0NTg0YzRmOGMwMmU3MzFkYjlhNTRlZTlmZA&code_challenge_method=S256&state=internal+state&scope=HENKILOTIEDOT_SYNTYMAAIKA+HENKILOTIEDOT_NIMI+OPISKELUOIKEUDET_SUORITETUT_TUTKINNOT

untuva testi-uri https://untuvaopintopolku.fi/koski/omadata-oauth2/authorize?client_id=koskioauth2sampledevpk&response_type=code&response_mode=form_post&redirect_uri=%2Fkoski%2Fomadata-oauth2%2Fdebug-post-response&code_challenge=NjIyMGQ4NDAxZGM0ZDI5NTdlMWRlNDI2YWNhNjA1NGRiMjQyZTE0NTg0YzRmOGMwMmU3MzFkYjlhNTRlZTlmZA&code_challenge_method=S256&state=internal+state&scope=HENKILOTIEDOT_SYNTYMAAIKA+HENKILOTIEDOT_NIMI+OPISKELUOIKEUDET_SUORITETUT_TUTKINNOT
