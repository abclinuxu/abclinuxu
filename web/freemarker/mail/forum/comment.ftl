${CONTENT}

Zobrazit diskusi: http://www.abclinuxu.cz/forum/show/${RELATION_ID}
Odpovedet: http://www.abclinuxu.cz/forum/EditDiscussion/${RELATION_ID}?action=add&dizId=${DISCUSSION_ID}&threadId=${THREAD_ID}

<#if USER.id!=4043>Odhlaseni
=========
Odhlaseni: http://www.abclinuxu.cz/EditUser/${USER.id}?action=subscribe
Vase prihlasovaci jmeno je ${USER.login}.</#if>
