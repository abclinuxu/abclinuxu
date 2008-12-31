AbcMonitor zaznamenal zmenu:

Kdo:   ${ACTOR}
Akce:  ${ACTION}
Titulek: ${NAME}
Datum: ${DATE.show(PERFORMED,"CZ_FULL",false)}
URL:   ${URL?default("neni dostupne")}

<#if CONTENT??>Obsah:

${CONTENT}

</#if>


--
Sledovani tohoto objektu muzete zrusit na adrese:
${URL!}
Vase prihlasovaci jmeno je ${USER.login}.
