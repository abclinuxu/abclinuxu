AbcMonitor zaznamenal zmenu objektu

AbcMonitor je sluzba uzivatelum portalu www.abclinuxu.cz, ktera za ne
sleduje stav vybranych objektu. Pokud jej u zvoleneho objektu zapnete
a nekdo objekt nejak upravi, zasleme vam email s upozornenim.

Kdo:   ${ACTOR}
Akce:  ${ACTION}
Jmeno: ${NAME}
Datum: ${DATE.show(PERFORMED,"CZ_FULL")}
URL:   ${URL}

Sledovani tohoto objektu muzete zrusit na adrese:
${URL}
Vase prihlasovaci jmeno je ${USER.login}.

