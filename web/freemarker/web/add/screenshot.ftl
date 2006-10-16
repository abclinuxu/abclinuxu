<#include "../header.ftl">
<#import "../misc/hardware.ftl" as hwlib>

<@lib.showMessages/>

<p>
    Na t�to str�nce m��ete nahr�t obr�zek. Maxim�ln� velikost je omezena
    na p�l megabajtu, podporov�ny jsou form�ty JPG, PNG a GIF (pro obr�zky
    program� je nejvhodn�j�� form�t PNG). Pokud obr�zek
    p�esahuje ���ku �i d�lku 200 pixel�, bude z�rove� vytvo�ena a zobrazena
    jeho miniatura.
</p>

<form action="${URL.make("/inset/"+RELATION.id)}" method="POST" enctype="multipart/form-data">
    <p>
        <span class="required">Jm�no souboru</span><br/>
        <input type="file" name="screenshot" size="40" tabindex="1">
        <div class="error">${ERRORS.screenshot?if_exists}</div>
        <input tabindex="2" type="submit" name="finish" value="Nahr�t">
        <input type="hidden" name="action" value="addScreenshot2">
    </p>
</form>

<#include "../footer.ftl">
