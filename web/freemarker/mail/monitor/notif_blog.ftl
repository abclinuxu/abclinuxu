AbcMonitor zaznamenal zmenu v blogu:

Kdo:   ${ACTOR}
Akce:  ${ACTION}
Jmeno: ${NAME}
Datum: ${DATE.show(PERFORMED,"CZ_FULL",false)}
URL:   ${URL?default("neni dostupne")}

Sledovani tohoto objektu muzete zrusit na adrese:
${URL?if_exists}
Vase prihlasovaci jmeno je ${USER.login}.
