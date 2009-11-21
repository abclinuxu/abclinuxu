<#include "../header.ftl">

<@lib.showMessages/>

<h1>Nastavení vašeho účtu</h1>

<form action="${URL.noPrefix("/EditUser")}" method="POST">
    <p>
        Pro vaši ochranu nejdříve zadejte vaše heslo:
        <input type="password" name="PASSWORD" size="16" tabindex="1">
        <@lib.showError key="PASSWORD"/>
    </p>

    <h2 style="margin-bottom: 1em">Bezpečnost</h2>

    <table class="siroka" cellspacing="10px">
        <tr>
            <td class="required" width="200px">Doba platnosti přihlašovací cookie</td>
            <td>
                <select name="cookieValid" tabindex="2">
                    <#assign cookieValid=PARAMS.cookieValid!"16070400">
                    <option value="0"<#if cookieValid=="0">SELECTED</#if>>nevytvářet</option>
                    <option value="-1" <#if cookieValid=="-1">SELECTED</#if>>tato session</option>
                    <option value="3600"<#if cookieValid=="3600">SELECTED</#if>>hodina</option>
                    <option value="86400"<#if cookieValid=="86400">SELECTED</#if>>den</option>
                    <option value="604800"<#if cookieValid=="604800">SELECTED</#if>>týden</option>
                    <option value="2678400"<#if cookieValid=="2678400">SELECTED</#if>>měsíc</option>
                    <option value="8035200"<#if cookieValid=="8035200">SELECTED</#if>>čtvrt roku</option>
                    <option value="16070400"<#if cookieValid=="16070400">SELECTED</#if>>půl roku</option>
                    <option value="32140800"<#if cookieValid=="32140800">SELECTED</#if>>rok</option>
                    <option value="3214080000"<#if cookieValid=="3214080000">SELECTED</#if>>sto let</option>
                </select>
            </td>
        </tr>

        <tr>
            <td colspan="2">
                <p>Toto nastavení ovlivňuje vytváření cookie při přihlášení. Standardně se vytvoří cookie
                s platností půl roku, která vás dokáže automaticky přihlásit bez nutnosti zadávat vaše heslo.
                Pokud však počítač sdílíte s více lidmi, například ve škole či internetové kavárně, může být toto chování
                pro vás nepraktické.</p>

                <p>První volba je nevytvářet tuto cookie vůbec, takže příště se budete muset přihlásit ručně.
                Druhá omezí platnost této cookie jen do vypnutí prohlížeče (session), ostatní omezí její délku
                podle popisu.</p>
            </td>
        </tr>
    </table>

    <h2 style="margin-bottom: 1em">Ovládání</h2>

    <table class="siroka" cellspacing="10px">
        <tr>
            <td class="required" width="200px">Wysiwyg editor</td>
            <td>
                <@lib.showOption "rte", "always", "zobrazit vždy", "radio", "tabindex='3'" />
                <@lib.showOption "rte", "request", "na žádost", "radio", "tabindex='3'" />
                <@lib.showOption "rte", "never", "nezobrazit nikdy", "radio", "tabindex='3'" />
            </td>
        </tr>

        <tr>
            <td colspan="2">
                V případě přístupu z kompatibilního prohlížeče (Firefox, Internet Explorer či Opera) povoluje WYSIWYG editor.
                Jedná se o aplikaci napsanou v Javascriptu, která umožňuje pohodlně nastavovat formátování textu,
                aniž byste museli znát syntaxi HTML. Můžete si vybrat, zda se má editor zobrazit automaticky vždy,
                nebo jen když sami zmáčknete přepínací tlačítko (defaultní stav) nebo jej úplně vypnout.
            </td>
        </tr>
    </table>

    <h2 style="margin-bottom: 1em">Vzhled</h2>

    <table class="siroka" cellspacing="10px">
        <tr>
            <td class="required" width="200px" rowspan="4" valign="top">Vlastní CSS</td>
            <td>
                <input type="text" name="css" size="40" value="${PARAMS.css!}" tabindex="4"><br>
                <@lib.showError key="css"/>
            </td>
        </tr>

        <tr>
            <td>
            Zadejte URL souboru obsahující CSS definici vzhledu portálu. Bude použita místo
            standardního vzhledu. Pro bílé písmo na černém podkladu vložte
            <code>/styles-dark.css</code>.
            <a href="/napoveda/alternativni-design">Nápověda</a>
            </td>
        </tr>

        <tr>
            <td>
                <textarea name="inline_css" class="siroka" rows="4">${PARAMS.inline_css!?html}</textarea>
                <@lib.showError key="inline_css"/>
            </td>
        </tr>

        <tr>
            <td>
                Zde máte možnost definovat CSS deklaraci, která bude vložena do hlavičky HTML kódu každé stránky.
            </td>
        </tr>

        <tr>
            <td class="required" width="200px">Grafické emotikony</td>
            <td>
                <@lib.showOption "emoticons", "yes", "ano", "radio", "tabindex='5'" />
                <@lib.showOption "emoticons", "no", "ne", "radio", "tabindex='5'" />
            </td>
        </tr>

        <tr>
            <td colspan="2">
            Určuje, zda má systém při zobrazování textu nahrazovat emotikony
            obrázky. Vypnutím získáte zanedbatelný nárůst rychlosti.
            </td>
        </tr>

        <tr>
            <td class="required" width="200px">Záložkové služby</td>
            <td>
                <@lib.showOption "social_bookmarks", "yes", "ano", "radio", "tabindex='6'" />
                <@lib.showOption "social_bookmarks", "no", "ne", "radio", "tabindex='6'" />
            </td>
        </tr>

        <tr>
            <td colspan="2">
                Umožňuje nezobrazovat ikonky online záložkových služeb typu Google bookmarks, Del.ici.ous,
                Linkuj či Facebook.
            </td>
        </tr>

        <tr>
            <td class="required" width="200px">Zobrazovat patičku</td>
            <td>
                <@lib.showOption "signatures", "yes", "ano", "radio", "tabindex='7'" />
                <@lib.showOption "signatures", "no", "ne", "radio", "tabindex='7'" />
            </td>
        </tr>

        <tr>
            <td colspan="2">
            Určuje, zda má systém při zobrazování diskusních příspěvků zobrazovat signatury autorů příspěvků.
            </td>
        </tr>

        <tr>
            <td class="required" width="200px">Zobrazovat avatary</td>
            <td>
                <@lib.showOption "avatars", "yes", "ano", "radio", "tabindex='8'" />
                <@lib.showOption "avatars", "no", "ne", "radio", "tabindex='8'" />
            </td>
        </tr>

        <tr>
            <td colspan="2">
            Určuje, zda má systém při zobrazování diskusních příspěvků zobrazovat avatary autorů příspěvků.
            </td>
        </tr>
    </table>

    <h2 style="margin-bottom: 1em">Poradna</h2>

    <table class="siroka" cellspacing="10px">
        <tr>
            <td class="required" width="200px">Počet dotazů na úvodní stránce</td>
            <td>
                <#assign single_mode=(TOOL.xpath(MANAGED, "/data/profile/forum_mode")!"")=="single">
                <#if !single_mode>
                    samostatné poradny
                    |
                    <a href="${URL.noPrefix("/EditUser/"+USER.id+"?action=changeForumMode&amp;forumMode=single"+TOOL.ticket(USER,false))}">všechny dotazy v jednom výpisu</a>
                    <br>
                    <#assign forums=TOOL.getUserForums(MANAGED)>
                    <table>
                        <#list forums.entrySet() as forum>
                            <#if forum.key gt 0>
                                <#assign name=TOOL.childName(TOOL.createRelation(forum.key))>
                            <#elseif forum.key==-1>
                                <#assign name="Poradny ze skupin">
                            </#if>
                            <tr>
                                <td>${name}</td>
                                <td>
                                    <input type="text" name="discussions_${forum.key}" value="${forum.value}" size="3" tabindex="9">
                                </td>
                            </tr>
                        </#list>
                    </table>
                <#else>
                    <a href="${URL.noPrefix("/EditUser/"+USER.id+"?action=changeForumMode&amp;forumMode=split"+TOOL.ticket(USER,false))}">samostatné poradny</a>
                    |
                    všechny dotazy v jednom výpisu
                    <br>
                    <select name="discussions" tabindex="10">
                        <#assign discussions=PARAMS.discussions!"20">
                        <option value="-2"<#if discussions=="-2">SELECTED</#if>>default</option>
                        <option value="0" <#if discussions=="0">SELECTED</#if>>žádné</option>
                        <option value="5"<#if discussions=="5">SELECTED</#if>>5</option>
                        <option value="10"<#if discussions=="10">SELECTED</#if>>10</option>
                        <option value="15"<#if discussions=="15">SELECTED</#if>>15</option>
                        <option value="20"<#if discussions=="20">SELECTED</#if>>20</option>
                        <option value="25"<#if discussions=="25">SELECTED</#if>>25</option>
                        <option value="30"<#if discussions=="30">SELECTED</#if>>30</option>
                        <option value="40"<#if discussions=="40">SELECTED</#if>>40</option>
                        <option value="50"<#if discussions=="50">SELECTED</#if>>50</option>
                    </select>
                </#if>
            </td>
        </tr>

        <tr>
            <td colspan="2">
            Zde máte možnost ovlivnit počet zobrazených dotazů na úvodní stránce.
            <#if !single_mode>Nula znamená, že se daná poradna na úvodní stránce zobrazovat nebude.</#if>
            </td>
        </tr>

        <tr>
            <td class="required" width="200px">Velikost stránky poradny</td>
            <td>
                <input type="text" name="forum" value="${PARAMS.forum!}" size="3" tabindex="11">
                <@lib.showError key="forum"/>
            </td>
        </tr>

        <tr>
            <td colspan="2">Počet dotazů na stránce poradny (mimo úvodní stránku).</td>
        </tr>
    </table>

    <h2 style="margin-bottom: 1em">Zprávičky</h2>

    <table class="siroka" cellspacing="10px">
        <tr>
            <td class="required" width="200px">Počet zpráviček</td>
            <td>
                <input type="text" name="news" value="${PARAMS.news!}" size="3" tabindex="12">
                <@lib.showError key="news"/>
            </td>
        </tr>

        <tr>
            <td colspan="2">
            Podobně můžete také určit počet zpráviček, které se zobrazují. Tento počet
            je standardně nastaven na ${DEFAULT_NEWS} a můžete jej zde předefinovat.
            </td>
        </tr>

        <tr>
            <td class="required" width="200px">Zobrazovat titulky zpráviček</td>
            <td>
                <@lib.showOption "newsTitles", "yes", "ano", "radio", "tabindex='13'" />
                <@lib.showOption "newsTitles", "no", "ne", "radio", "tabindex='13'" />
            </td>
        </tr>
        <tr>
            <td class="required" width="200px">Titulky zpráviček na více řádků</td>
            <td>
                <@lib.showOption "newsMultiline", "yes", "ano", "radio", "tabindex='14'" />
                <@lib.showOption "newsMultiline", "no", "ne", "radio", "tabindex='14'" />
            </td>
        </tr>
    </table>

    <h2 style="margin-bottom: 1em">Titulní stránka</h2>

    <table class="siroka" cellspacing="10px">
        <tr>
            <td class="required" width="200px">Počet článků</td>
            <td>
                <input type="text" name="articles" value="${PARAMS.articles!}" size="3" tabindex="15">
                <@lib.showError key="articles"/>
            </td>
        </tr>

        <tr>
            <td colspan="2">
                Celkový počet článků, které se mají zobrazovat na titulní stránce. Nastavením na nulu
                zrušíte jejich zobrazování.
            </td>
        </tr>

        <tr>
            <td class="required" width="200px">Počet nezkrácených článků</td>
            <td>
                <input type="text" name="complete_articles" value="${PARAMS.complete_articles!}" size="3" tabindex="16">
                <@lib.showError key="complete_articles"/>
            </td>
        </tr>

        <tr>
            <td colspan="2">
                Počet článků, které se mají zobrazovat celé, tedy včetně perexu a ikony. Ostatní články do celkového
                počtu budou zobrazeny zkráceně.
            </td>
        </tr>

        <tr>
            <td class="required" width="200px">Počet zápisků</td>
            <td>
                <input type="text" name="stories" value="${PARAMS.stories!}" size="3" tabindex="17">
                <@lib.showError key="stories"/>
            </td>
        </tr>

        <tr>
            <td class="required" width="200px">Počet výběrových zápisků</td>
            <td>
                <input type="text" name="digestStories" value="${PARAMS.digestStories!}" size="3" tabindex="18">
                <@lib.showError key="digestStories"/>
            </td>
        </tr>

        <tr>
            <td colspan="2">
                Zde můžete specifikovat počet zápisků z blogů, které se zobrazují na úvodní stránce. První číslo
                (defaultní hodnota je ${DEFAULT_STORIES}) určuje počet nejnovějších zápisků bez ohledu na to, zda
                jsou či nejsou zařazeny do výběru. Druhé číslo (default je ${DEFAULT_DIGEST_STORIES})
                určuje počet dodatečných zápisků z výběru, které se mají zobrazit na konci seznamu. Nastavením na nulu
                zrušíte zobrazování zápisků. Další informace jsou v <a href="/napoveda/blogy">nápovědě</a>.
            </td>
        </tr>

        <tr>
            <td class="required" width="200px">Zobrazovat všechny zápisky</td>
            <td>
                <@lib.showOption "bannedStories", "yes", "ano", "radio", "tabindex='19'" />
                <@lib.showOption "bannedStories", "no", "ne", "radio", "tabindex='19'" />
            </td>
        </tr>

        <tr>
            <td colspan="2">
                Administrátoři mohou zakázat zobrazování některého zápisku z blogu na titulní stránce. Typicky se
                to děje u provokací, urážek nebo tapetování. Toto nastavení způsobí, že na titulní stránce uvidíte
                i tyto zápisky.
            </td>
        </tr>

        <tr>
            <td class="required" width="200px">Počet desktopů</td>
            <td>
                <input type="text" name="screenshots" value="${PARAMS.screenshots!}" size="3" tabindex="20">
                <@lib.showError key="screenshots"/>
            </td>
        </tr>

        <tr>
            <td colspan="2">
                Počet zobrazovaných uživatelských desktopů na titulní stránce. Tento počet
                je standardně nastaven na ${DEFAULT_SCREENSHOTS}. Můžete jej přenastavit na hodnotu
                v rozmezí 0&nbsp;-&nbsp;3, přičemž 0 odstraní boxík se screenshoty desktopů z hlavní stránky úplně.
            </td>
        </tr>
    </table>

    <h2 style="margin-bottom: 1em">Počet dokumentů jinde</h2>

    <table class="siroka" cellspacing="10px">
        <tr>
            <td class="required" width="200px">Obecná velikost stránky</td>
            <td>
                <input type="text" name="defaultPageSize" value="${PARAMS.defaultPageSize!}" size="3" tabindex="21">
                <@lib.showError key="defaultPageSize"/>
            </td>
        </tr>

        <tr>
            <td colspan="2">Počet zobrazených dokumentů na jedné stránce.</td>
        </tr>

        <tr>
            <td class="required" width="200px">Velikost stránky při hledání</td>
            <td>
                <input type="text" name="search" value="${PARAMS.search!}" size="3" tabindex="22">
                <@lib.showError key="search"/>
            </td>
        </tr>

        <tr>
            <td colspan="2">Počet nalezených dokumentů zobrazených na jedné stránce.</td>
        </tr>
    </table>

    <h2 style="margin-bottom: 1em">Rozcestník</h2>

    <table class="siroka" cellspacing="10px">
        <tr>
            <td class="required" width="200px">Zobrazovat rozcestník</td>
            <td>
                <@lib.showOption "guidepost", "yes", "ano", "radio", "tabindex='23'" />
                <@lib.showOption "guidepost", "no", "ne", "radio", "tabindex='23'" />
            </td>
        </tr>

        <tr>
            <td colspan="2">Určuje, zda se má zobrazovat rozcestník.</td>
        </tr>

        <tr>
            <td class="required" width="200px">Zobrazovat servery</td>
            <td>
                <table class="siroka">
                    <#list SERVERS as server>
                        <#if server_index % 3 == 0><tr></#if>
                            <td>
                                <#assign feedParam = "feed"+server.id>
                                <@lib.showOption3 feedParam, "yes", server.name, "checkbox", PARAMS[feedParam]?? />
                            </td>
                        <#if server_index % 3 == 2 || ! server_has_next></tr></#if>
                    </#list>
                </table>
            </td>
        </tr>

        <tr>
            <td colspan="2">
            Pokud jste nevypnuli rozcestník úplně, tak zde si můžete vybrat servery,
            které se v něm mají zobrazovat.
            </td>
        </tr>

        <tr>
            <td class="required" width="200px">Počet odkazů</td>
            <td>
                <input type="text" name="indexFeedSize" value="${PARAMS.indexFeedSize!}" size="3">
                na hlavní stránce<br><@lib.showError key="indexFeedSize"/>
                <input type="text" name="feedSize" value="${PARAMS.feedSize!}" size="3">
                mimo hlavní stránku <@lib.showError key="feedSize"/>
            </td>
        </tr>

        <tr>
            <td colspan="2">
            Počet odkazů pro jeden server v rozcestníku na hlavní stránce a mimo ni.
            Standardní počet je nastaven na ${DEFAULT_TEMPLATE_LINKS} a ${DEFAULT_LINKS}.
            </td>
        </tr>

        <tr>
            <td>&nbsp;</td>
            <td><input type="submit" value="Dokonči"></td>
        </tr>
    </table>
    <input type="hidden" name="action" value="editSettings2">
    <input type="hidden" name="uid" value="${MANAGED.id}">
</form>


<#include "../footer.ftl">
