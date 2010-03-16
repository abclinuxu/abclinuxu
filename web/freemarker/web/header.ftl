<#include "header-shared.ftl">

    <div id="ls_prepinac" title="Skrýt sloupec" onclick="prepni_sloupec()">&#215;</div>

    <div class="obal_ls" id="ls">
        <div class="s">
            <@lib.advertisement id="vip" />
            <@lib.advertisement id="vip-text" />

            <!-- ANKETA -->
            <#if VARS.currentPoll??>
                <#assign relAnketa = VARS.currentPoll, anketa = relAnketa.child, total = anketa.totalVoters,
                         url=relAnketa.url!("/ankety/show/"+relAnketa.id)>
                <#if anketa.multiChoice><#assign type = "checkbox"><#else><#assign type = "radio"></#if>

                <div class="s_nadpis">
                   <a class="s_nadpis-pravy-odkaz" href="/pozadavky?categoryPosition=0">navrhněte&nbsp;&raquo;</a>
                   <a href="/ankety">Anketa</a>
                </div>
                <div class="s_sekce">
                    <form action="${URL.noPrefix("/EditPoll/"+relAnketa.id)}" method="POST">
                     <div class="ank-otazka">${anketa.text}</div>
                     <#list anketa.choices as choice>
                        <div class="ank-odpov">
                          <#assign procento = TOOL.percent(choice.count,total)>
                          <label><input type="${type}" name="voteId" value="${choice.id}">${choice.text}</label>&nbsp;(<span title="${choice.count}">${procento}%</span>)<br>
                          <div class="ank-sloup-okraj" style="width: ${procento}px">
                            <div class="ank-sloup"></div>
                          </div>
                        </div>
                     </#list>
                     <div>
                      <input name="submit" type="submit" class="button" value="Hlasuj" alt="Hlasuj">
                        Celkem ${total} hlasů
                      <input type="hidden" name="url" value="${url}">
                      <input type="hidden" name="action" value="vote">
                     </div>
                    </form>
                </div>
                <#assign diz=TOOL.findComments(anketa)>
                <div>&nbsp;<a href="${url}" title="${anketa.text}">Komentářů: ${diz.responseCount}</a><#rt>
                  <#lt><#if diz.responseCount gt 0><@lib.markNewComments diz/>, poslední ${DATE.show(diz.updated,"CZ_SHORT")}</#if>
                  <@lib.advertisement id="anketa" />
                </div>
            </#if>

            <!-- ZPRÁVIČKY -->
            <#assign news=VARS.getFreshNews(USER!)>
            <div class="s_nadpis">
                <a class="s_nadpis-pravy-odkaz" href="${URL.make("/zpravicky/edit?action=add")}">napište &raquo;</a>
                <#if USER?? && USER.hasRole("news admin")>
                    <a class="s_nadpis-pravy-odkaz" href="${URL.make("/zpravicky/dir/37672")}" title="Počet neschválených a čekajících zpráviček">(${VARS.counter.WAITING_NEWS},${VARS.counter.SLEEPING_NEWS})&nbsp;</a>
                </#if>
                <a href="/zpravicky" title="zprávičky">Zprávičky</a>
            </div>

            <@lib.advertisement id="hypertext1" />

            <div class="s_sekce">
                <div class="ls_zpr">
                <#list news as relation>
                    <#if relation_index==2>
                         <@lib.advertisement id="sl-box" />
                    </#if>
                    <#if relation_index==4>
                         <@lib.advertisement id="skyscraper" />
                         <@lib.advertisement id="double-sky" />
                         <@lib.advertisement id="sl-mini" />
                    </#if>
                    <@lib.showTemplateNews relation/>
                    <hr>
                </#list>
                </div>
                <div class="s_odkaz">
                    <a href="/zpravicky">Centrum</a> |
                    <a href="${URL.make("/zpravicky/edit?action=add")}" rel="nofollow">Napsat</a> |
                    <a href="/History?type=news&amp;from=${news?size}&amp;count=15">Starší</a>
                </div>
            </div>

            <@lib.advertisement id="sl-jobscz" />
            <@lib.advertisement id="sl-abcprace" />

            <#if ! IS_INDEX??>
                <#assign FEEDS = VARS.getFeeds(USER!,false)>
                <#if (FEEDS.size() > 0)>
                    <!-- ROZCESTNÍK -->
                    <div class="s_nadpis">Rozcestník</div>
                    <div class="s_sekce">
                        <div class="rozc">
                            <#list FEEDS.keySet() as server>
                                <a class="server" href="${"/presmeruj?class=S&amp;id="+server.id+"&amp;url="+server.url?url}">${server.name}</a><br>
                                <ul>
                                <#list FEEDS(server) as link>
                                    <li>
                                        <a href="${"/presmeruj?class=S&amp;id="+server.id+"&amp;url="+link.url?url}">${link.text}</a>
                                    </li>
                                </#list>
                                </ul>
                            </#list>
                        </div>
                    </div>
                </#if>
            </#if>

            <@lib.advertisement id="sl-doporucujeme" />

            <@lib.advertisement id="sl-placene-odkazy" />

        </div> <!-- s -->
    </div> <!-- obal_ls -->

    <#if plovouci_sloupec??>
        <#if URL.prefix=='/hardware'>
            <#assign sloupecId = "hw-sloupec">
        <#elseif URL.prefix=='/software'>
            <#assign sloupecId = "sw-sloupec">
        </#if>
        <#if sloupecId??>
            <div class="${sloupecId}">
        </#if>
        <div class="obal_ps">
            <div class="ps"><div class="s">
               ${plovouci_sloupec}
            </div></div> <!-- ps, s -->
        </div> <!-- obal_ps -->
        <#if sloupecId??>
            </div> <!-- hw-sloupec, sw-sloupec -->
        </#if>
    </#if>

    <div class="st" id="st">
        <a name="obsah"></a>

        <#if plovouci_sloupec??>
            <@lib.advertisement id="obsah-box-uzky" />
        <#else>
            <@lib.advertisement id="obsah-box" />
        </#if>

        <#if PARENTS??>
            <div class="pwd-box">
                <div class="do-zalozek">
                    <#if RSS??>
                        <a href="${RSS}"><img src="/images/site2/feed16.png" width="16" height="16" border="0"></a>
                    </#if>
                    <#if RELATION?? && USER??>
                        <form action="/EditBookmarks/${USER.id}" style="display: inline">
                            <input type="submit" class="button" value="do záložek">
                            <input type="hidden" name="action" value="add">
                            <input type="hidden" name="rid" value="${RELATION.id}">
                            <input type="hidden" name="prefix" value="${URL.prefix}">
                            <input type="hidden" name="ticket" value="${TOOL.ticketValue(USER)}">
                        </form>
                    </#if>
                </div>
                <div class="pwd">
                    <a href="/">AbcLinuxu</a>:/
                    <#list TOOL.getParents(PARENTS,USER!,URL) as link>
                        <a href="${link.url}">${link.title}</a>
                        <#if link_has_next> / </#if>
                    </#list>
                </div>
            </div>

            <#if ASSIGNED_TAGS??>
                <@lib.advertisement id="stitky" />

                <div class="tag-box">
                    <a href="/stitky">Štítky</a>:
                    <span id="prirazeneStitky">
                        <#if ASSIGNED_TAGS?size &gt; 0>
                            <#list ASSIGNED_TAGS as tag>
                                <a href="/stitky/${tag.id}" title="Zobrazit objekty, které mají přiřazen štítek „${tag.title}“.">${tag.title}</a><#if tag_has_next>, </#if>
                            </#list>
                        <#else>
                            <i>není přiřazen žádný štítek</i>
                        </#if>
                    </span>
                </div>
            </#if>
        </#if>

        <#if SYSTEM_CONFIG.isMaintainanceMode()>
            <div style="color: red; border: medium solid red; margin: 10px; padding: 3ex">
                <p style="font-size: xx-large; text-align: center">Režim údržby</p>
                <p>
                    Právě provádíme údržbu portálu. Prohlížení obsahu by mělo nadále fungovat,
                   úpravy obsahu bohužel nejsou prozatím k dispozici. Děkujeme za pochopení.
                </p>
            </div>
        </#if>
