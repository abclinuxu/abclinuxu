AbcMonitor zaznamenal zmenu objektu

AbcMonitor je sluzba uzivatelum portalu www.abclinuxu.cz, ktera za ne
sleduje stav vybranych objektu. Pokud jej u zvoleneho objektu zapnete
a nekdo objekt nejak upravi, zasle vam email s upozornenim.

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

