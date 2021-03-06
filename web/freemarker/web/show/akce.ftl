<#assign html_header>
    <script src="http://maps.google.com/maps?file=api&amp;v=2&amp;key=${GOOGLE_MAPS_KEY}" type="text/javascript"></script>
</#assign>

<#import "../macros.ftl" as lib>

<#assign region=ITEM.string1!"UNDEF", subtype=ITEM.subType>

<#if subtype=="community"><#assign subtype="Komunitní">
<#elseif subtype=="educational"><#assign subtype="Školní">
<#elseif subtype=="company"><#assign subtype="Firemní">
</#if>

<#assign plovouci_sloupec>

    <@lib.advertisement id="hypertext2nahore" />

    <#if SUBPORTAL??>
        <@lib.showSubportal SUBPORTAL, true/>
        <#assign counter=VARS.getSubportalCounter(SUBPORTAL)>
    </#if>
    <#assign icon=TOOL.xpath(ITEM,"/data/icon")!"UNDEF">
    <#if icon!="UNDEF">
        <div class="s_nadpis">${TOOL.childName(ITEM)}</div>
        <div class="s_sekce">
            <div style="text-align:center">
                <img src="${icon}" alt="Logo akce ${TOOL.childName(ITEM)}">
            </div>
        </div>
    </#if>

    <#assign location=TOOL.xpath(ITEM,"//location")!"UNDEF">
    <div class="s_nadpis">Informace o akci</div>
    <div class="s_sekce">
        <table cellspacing="0" class="s_table">
            <tr>
                <td>Datum:</td>
                <td>${DATE.show(ITEM.created,"CZ_DMY")}, od: ${DATE.show(ITEM.created,"TIME")}</td>
            </tr>
            <#if ITEM.date1??>
              <tr>
                  <td>Konec:</td>
                  <td>${DATE.show(ITEM.date1,"CZ_DMY")}, do: ${DATE.show(ITEM.date1,"TIME")}</td>
              </tr>
            </#if>
            <tr>
                <td>Kraj:</td>
                <td><@lib.showRegion region/></td>
            </tr>
            <tr>
                <td>Typ akce:</td>
                <td>${subtype}</td>
            </tr>
        </table>
    </div>

    <#if USER?? && (USER.id == ITEM.owner || TOOL.permissionsFor(USER, RELATION).canModify())>
        <div class="s_nadpis">Správa akce</div>
        <div class="s_sekce">
            <ul>
                <li><a href="${URL.noPrefix("/akce/edit/${RELATION.id}?action=edit")}">Upravit</a></li>
                <#if TOOL.permissionsFor(USER, RELATION).canDelete()>
                    <li><a href="${URL.noPrefix("/EditRelation/"+RELATION.id+"?action=remove&amp;prefix=/akce")}">Smazat</a></li>
                </#if>
                <li><a href="${URL.noPrefix("/akce/inset/"+RELATION.id+"?action=addFile")}">Přidat přílohu</a></li>
                <li><a href="${URL.noPrefix("/akce/inset/"+RELATION.id+"?action=manage")}">Správa příloh</a></li>
            </ul>
        </div>
    </#if>

    <@lib.advertisement id="square" />
    <@lib.advertisement id="hypertext2dole" />

</#assign>

<#include "../header.ftl">


<h1>${TOOL.childName(ITEM)}</h1>

<p class="meta-vypis">
    Aktualizováno: ${DATE.show(ITEM.updated,"SMART")} | <@lib.showUserFromId ITEM.owner />
    |  Přečteno: ${TOOL.getCounterValue(ITEM,"read")}&times;
</p>

<#assign descShort=TOOL.xpath(ITEM,"/data/descriptionShort"), desc=TOOL.xpath(ITEM,"/data/description")!"UNDEF">
<#if ITEM.type==27>
    <p>
        Stav: čeká na schválení
        <#if USER?? && TOOL.permissionsFor(USER, RELATION).canModify()>
            (<a href="${URL.noPrefix("/akce/edit/"+RELATION.id+"?action=approve"+TOOL.ticket(USER,false))}">Schválit</a>
            | <a href="${URL.noPrefix("/EditRelation/"+RELATION.id+"?action=remove&amp;prefix=/akce")}">Smazat</a>)
        </#if>
    </p>
</#if>

<#if desc!="UNDEF">
    <div class="cl_perex">
        ${TOOL.render(descShort,USER!)}
    </div>
    <div>
        ${TOOL.render(desc,USER!)}
    </div>
<#else>
    <hr />
    <div>
        ${TOOL.render(descShort,USER!)}
    </div>
</#if>

<#assign regs=TOOL.xpathValue(ITEM.data, "count(//registrations/registration)")>
<p>
    <b>Účast potvrdilo:</b>
        <#if regs?eval gt 0>
            <a href="?action=participants">${regs?eval} uživatelů</a>
        <#else>
            zatím bohužel nikdo
        </#if>
    <br />

    <#if USER??>
        <#assign myreg=TOOL.xpath(ITEM.data, "/data/registrations/registration[@uid="+USER.id+"]")!"UNDEF">
    <#else>
        <#assign myreg="UNDEF">
    </#if>

    <#if DATE.show("ISO").compareTo(DATE.show(ITEM.created,"ISO",false)) lt 0>
        <form action="/akce/edit" method="post">
            <input type="hidden" name="rid" value="${RELATION.id}">
            <#if myreg=="UNDEF">
                <input type="submit" value="Registrovat svou účast">
                <input type="hidden" name="action" value="register">
            <#else>
                <input type="submit" value="Odvolat svou účast">
                <input type="hidden" name="action" value="deregister2">
            </#if>
        </form>
    </#if>
</p>

<#if location!="UNDEF">
    <h2>Mapa</h2>
    <div id="gg_map" style="width: 500px; height: 300px"></div>
    <script type="text/javascript">
        var map = new GMap2(document.getElementById("gg_map"));
        geocoder = new GClientGeocoder();

        map.addControl(new GSmallMapControl());
        map.addControl(new GMapTypeControl());

        geocoder.getLatLng(
          "${location.replace("\"","\\\"")}",
          function(point) {
            if (!point) {
              alert(address + " nebylo na Google Maps nalezeno!");
            } else {
              map.setCenter(point, 13);
              var marker = new GMarker(point);
              map.addOverlay(marker);
              marker.bindInfoWindowHtml("<font color=\"black\"><b>${TOOL.childName(ITEM)}</b><br />${location?html}");
            }
          }
        );
    </script>
</#if>

<#assign attachments=TOOL.attachmentsFor(ITEM)>
<#if (attachments?size > 0)>
    <#assign wrote_div=false>
    <#list attachments as attachment>
        <#assign hidden=TOOL.xpath(attachment.child, "/data/object/@hidden")!"false">
        <#if hidden=="false" || TOOL.permissionsFor(USER, RELATION).canModify()>
            <#if !wrote_div>
                <div class="ds_attachments">
                    <h3>Přílohy:</h3>
                    <ul>
                <#assign wrote_div=true>
            </#if>
            <li>
                <a href="${TOOL.xpath(attachment.child, "/data/object/@path")}">${TOOL.xpath(attachment.child, "/data/object/originalFilename")}</a>
                (${TOOL.xpath(attachment.child, "/data/object/size")} bytů)
                <#if hidden=="true"><i>skrytá</i></#if>
            </li>
        </#if>
    </#list>
    <#if wrote_div>
            </ul>
        </div>
    </#if>
</#if>

<@lib.showPageTools RELATION />

<#if CHILDREN.discussion??>
    <h3>Komentáře</h3>
    <@lib.showDiscussion CHILDREN.discussion[0]/>
</#if>

<#include "../footer.ftl">
