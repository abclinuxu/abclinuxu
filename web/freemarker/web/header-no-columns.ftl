<#include "header-shared.ftl">

    <div id="st">
        <a name="obsah"></a>

        <#if SYSTEM_CONFIG.isMaintainanceMode()>
            <div style="color: red; border: medium solid red; margin: 10px; padding: 3ex">
                <p style="font-size: xx-large; text-align: center">Režim údržby</p>
                <p>
                    Právě provádíme údržbu portálu. Prohlížení obsahu by mělo nadále fungovat,
                   úpravy obsahu bohužel nejsou prozatím k dispozici. Děkujeme za pochopení.
                </p>
            </div>
        </#if>
