AbcMonitor zaznamenal zmenu:

Kdo:   ${ACTOR}
Akce:  ${ACTION}
Titulek: ${NAME}
Datum: ${DATE.show(PERFORMED,"CZ_FULL")}
URL:   ${URL?default("neni dostupne")}

<#if CONTENT?exists>Obsah:

${CONTENT}
</#if>

Sledovani tohoto objektu muzete zrusit na adrese:
${URL?if_exists}
Vase prihlasovaci jmeno je ${USER.login}.
