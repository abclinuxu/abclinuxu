${CONTENT}

Zobrazit: http://www.abclinuxu.cz/forum/show/${RELATION_ID}#${THREAD_ID}

<#if JOB?exists>Reklama: ${JOB.title}
${JOB.region}, ${JOB.category}<#if JOB.itJob>, IT</#if><#if JOB.linuxJob>, Linux</#if>
http://www.praceabc.cz/www/detail.php?id=${JOB.id}
</#if>
<#if USER.id!=4043>
Odhlaseni: http://www.abclinuxu.cz/EditUser/${USER.id}?action=subscribe
Vase prihlasovaci jmeno je ${USER.login}.
</#if>