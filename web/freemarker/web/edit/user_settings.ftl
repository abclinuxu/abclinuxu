<#include "../header.ftl">

<@lib.showMessages/>

<h1>Nastavení vašeho účtu</h1>

<@lib.addForm URL.noPrefix("/EditUser")>
    <p>Pro vaši ochranu nejdříve zadejte vaše heslo:</p>
    <@lib.addPassword true, "PASSWORD", "Heslo" />

    <@lib.addGroup "Bezpečnost">
        <@lib.addSelect true, "cookieValid", "Doba platnosti přihlašovací cookie">
            <@lib.addOption "cookieValid", "nevytvářet", "0" />
            <@lib.addOption "cookieValid", "tato session", "-1" />
            <@lib.addOption "cookieValid", "hodina", "3600" />
            <@lib.addOption "cookieValid", "den", "86400" />
            <@lib.addOption "cookieValid", "týden", "604800" />
            <@lib.addOption "cookieValid", "měsíc", "2678400" />
            <@lib.addOption "cookieValid", "čtvrt roku", "8035200" />
            <@lib.addOption "cookieValid", "půl roku", "16070400", true />
            <@lib.addOption "cookieValid", "rok", "32140800" />
            <@lib.addOption "cookieValid", "sto let", "3214080000" />
        </@lib.addSelect>

        <@lib.addDescriptionLine>
            <p>Toto nastavení ovlivňuje vytváření cookie při přihlášení. Standardně se vytvoří cookie
                s platností půl roku, která vás dokáže automaticky přihlásit bez nutnosti zadávat vaše heslo.
                Pokud však počítač sdílíte s více lidmi, například ve škole či internetové kavárně, může být toto chování
                pro vás nepraktické.</p>

            <p>První volba je nevytvářet tuto cookie vůbec, takže příště se budete muset přihlásit ručně.
            Druhá omezí platnost této cookie jen do vypnutí prohlížeče (session), ostatní omezí její délku
            podle popisu.</p>
        </@lib.addDescriptionLine>
    </@lib.addGroup>

    <@lib.addGroup "Ovládání">
        <@lib.addFormField true, "Wysiwyg editor">
            <@lib.showOption "rte", "always", "zobrazit vždy", "radio", "tabindex='3'" />
            <@lib.showOption "rte", "request", "na žádost", "radio", "tabindex='3'" />
            <@lib.showOption "rte", "never", "nezobrazit nikdy", "radio", "tabindex='3'" />
        </@lib.addFormField>

        <@lib.addDescriptionLine>
            V případě přístupu z kompatibilního prohlížeče (Firefox, Internet Explorer či Opera) povoluje WYSIWYG editor.
            Jedná se o aplikaci napsanou v Javascriptu, která umožňuje pohodlně nastavovat formátování textu,
            aniž byste museli znát syntaxi HTML. Můžete si vybrat, zda se má editor zobrazit automaticky vždy,
            nebo jen když sami zmáčknete přepínací tlačítko (defaultní stav) nebo jej úplně vypnout.
        </@lib.addDescriptionLine>
    </@lib.addGroup>

    <@lib.addGroup "Vzhled">
        <@lib.addInput true, "css", "Vlastní CSS" />
        <@lib.addDescriptionLine>
            Zadejte URL souboru obsahující CSS definici vzhledu portálu. Bude použita místo
            standardního vzhledu. Pro bílé písmo na černém podkladu vložte
            <code>/styles-dark.css</code>.
            <a href="/napoveda/alternativni-design">Nápověda</a>
        </@lib.addDescriptionLine>

        <@lib.addTextArea false, "inline_css", "Deklarace CSS" />
        <@lib.addDescriptionLine>
            Zde máte možnost definovat CSS deklaraci, která bude vložena do hlavičky HTML kódu každé stránky.
        </@lib.addDescriptionLine>

        <@lib.addFormField true, "Grafické emotikony">
            <@lib.showOption "emoticons", "yes", "ano", "radio", "" />
            <@lib.showOption "emoticons", "no", "ne", "radio", "" />
        </@lib.addFormField>
        <@lib.addDescriptionLine>
            Určuje, zda má systém při zobrazování textu nahrazovat emotikony
            obrázky. Vypnutím získáte zanedbatelný nárůst rychlosti.
        </@lib.addDescriptionLine>

        <@lib.addFormField true, "Záložkové služby">
            <@lib.showOption "social_bookmarks", "yes", "ano", "radio", "" />
            <@lib.showOption "social_bookmarks", "no", "ne", "radio", "" />
        </@lib.addFormField>
        <@lib.addDescriptionLine>
            Umožňuje nezobrazovat ikonky online záložkových služeb typu Google bookmarks, Del.ici.ous,
                Linkuj či Facebook.
        </@lib.addDescriptionLine>

        <@lib.addFormField true, "Zobrazovat patičku">
            <@lib.showOption "signatures", "yes", "ano", "radio", "" />
            <@lib.showOption "signatures", "no", "ne", "radio", "" />
        </@lib.addFormField>
        <@lib.addDescriptionLine>
            Určuje, zda má systém při zobrazování diskusních příspěvků zobrazovat signatury autorů příspěvků.
        </@lib.addDescriptionLine>

        <@lib.addFormField true, "Zobrazovat avatary">
            <@lib.showOption "avatars", "yes", "ano", "radio", "" />
            <@lib.showOption "avatars", "no", "ne", "radio", "" />
        </@lib.addFormField>
        <@lib.addDescriptionLine>
            Určuje, zda má systém při zobrazování diskusních příspěvků zobrazovat avatary autorů příspěvků.
        </@lib.addDescriptionLine>


    </@lib.addGroup>

    <@lib.addGroup "Poradna">
        <@lib.addFormField true, "Počet dotazů na úvodní stránce">
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
        </@lib.addFormField>
        <@lib.addDescriptionLine>
            Zde máte možnost ovlivnit počet zobrazených dotazů na úvodní stránce.
            <#if !single_mode>Nula znamená, že se daná poradna na úvodní stránce zobrazovat nebude.</#if>
        </@lib.addDescriptionLine>

        <@lib.addInput true, "forum", "Velikost stránky poradny", 3 />
        <@lib.addDescriptionLine>
            Počet dotazů na stránce poradny (mimo úvodní stránku).
        </@lib.addDescriptionLine>
    </@lib.addGroup>

    <@lib.addGroup "Zprávičky">
        <@lib.addInput true, "news", "Počet zpráviček", 3 />
        <@lib.addDescriptionLine>
            Podobně můžete také určit počet zpráviček, které se zobrazují. Tento počet
            je standardně nastaven na ${DEFAULT_NEWS} a můžete jej zde předefinovat.
        </@lib.addDescriptionLine>

        <@lib.addFormField true, "Zobrazovat titulky zpráviček">
            <@lib.showOption "newsTitles", "yes", "ano", "radio", "" />
            <@lib.showOption "newsTitles", "no", "ne", "radio", "" />
        </@lib.addFormField>

        <@lib.addFormField true, "Titulky zpráviček na více řádků">
            <@lib.showOption "newsMultiline", "yes", "ano", "radio", "" />
            <@lib.showOption "newsMultiline", "no", "ne", "radio", "" />
        </@lib.addFormField>
    </@lib.addGroup>

    <@lib.addGroup "Titulní stránka">
        <@lib.addInput true, "articles", "Počet článků", 3 />
        <@lib.addDescriptionLine>
            Celkový počet článků, které se mají zobrazovat na titulní stránce. Nastavením na nulu
            zrušíte jejich zobrazování.
        </@lib.addDescriptionLine>

        <@lib.addInput true, "complete_articles", "Počet nezkrácených článků", 3 />
        <@lib.addDescriptionLine>
            Počet článků, které se mají zobrazovat celé, tedy včetně perexu a ikony. Ostatní články do celkového
            počtu budou zobrazeny zkráceně.
        </@lib.addDescriptionLine>

        <@lib.addInput true, "stories", "Počet zápisků", 3 />

        <@lib.addInput true, "digest_stories", "Počet výběrových zápisků", 3 />
        <@lib.addDescriptionLine>
            Počet článků, které se mají zobrazovat celé, tedy včetně perexu a ikony. Ostatní články do celkového
            počtu budou zobrazeny zkráceně.
        </@lib.addDescriptionLine>

        <@lib.addFormField true, "Zobrazovat všechny zápisky">
            <@lib.showOption "bannedStories", "yes", "ano", "radio", "tabindex='19'" />
            <@lib.showOption "bannedStories", "no", "ne", "radio", "tabindex='19'" />
        </@lib.addFormField>
        <@lib.addDescriptionLine>
            Administrátoři mohou zakázat zobrazování některého zápisku z blogu na titulní stránce. Typicky se
            to děje u provokací, urážek nebo tapetování. Toto nastavení způsobí, že na titulní stránce uvidíte
            i tyto zápisky.
        </@lib.addDescriptionLine>

        <@lib.addInput true, "screenshots", "Počet desktopů" />
        <@lib.addDescriptionLine>
            Počet zobrazovaných uživatelských desktopů na titulní stránce. Tento počet
            je standardně nastaven na ${DEFAULT_SCREENSHOTS}. Můžete jej přenastavit na hodnotu
            v rozmezí 0&nbsp;-&nbsp;3, přičemž 0 odstraní boxík se screenshoty desktopů z hlavní stránky úplně.
        </@lib.addDescriptionLine>
    </@lib.addGroup>

    <@lib.addGroup "Počet dokumentů jinde">
        <@lib.addInput "defaultPageSize", "Obecná velikost stránky", 3 />
        <@lib.addDescriptionLine>
            Počet zobrazených dokumentů na jedné stránce.
        </@lib.addDescriptionLine>

        <@lib.addInput "search", "Velikost stránky při hledání", 3 />
        <@lib.addDescriptionLine>
            Počet nalezených dokumentů zobrazených na jedné stránce.
        </@lib.addDescriptionLine>
    </@lib.addGroup>

    <@lib.addGroup "Rozcestník">
        <@lib.addFormField true, "Zobrazovat rozcestník">
            <@lib.showOption "guidepost", "yes", "ano", "radio", "tabindex='23'" />
            <@lib.showOption "guidepost", "no", "ne", "radio", "tabindex='23'" />
        </@lib.addFormField>
        <@lib.addDescriptionLine>
            Určuje, zda se má zobrazovat rozcestník.
        </@lib.addDescriptionLine>

        <@lib.addFormField true, "Zobrazovat servery">
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
        </@lib.addFormField>
        <@lib.addDescriptionLine>
            Pokud jste nevypnuli rozcestník úplně, tak zde si můžete vybrat servery,
            které se v něm mají zobrazovat.
        </@lib.addDescriptionLine>

        <@lib.addFormField true, "Počet odkazů">
            <@lib.addInputBare "indexFeedSize", 3>na hlavní stránce</@lib.addInputBare>
            <@lib.addInputBare "feedSize", 3>mimo hlavní stránku</@lib.addInputBare>
        </@lib.addFormField>
    </@lib.addGroup>

    <@lib.addSubmit "Dokončit" />
    <@lib.addHidden "uid", MANAGED.id />
    <@lib.addHidden "action", "editSettings2" />
</@lib.addForm>


<#include "../footer.ftl">
