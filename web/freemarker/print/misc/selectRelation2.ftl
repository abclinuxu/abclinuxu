<#include "/include/macros.ftl">
<#include "../header.ftl">

<#call showMessages>

<form action="${URL.noPrefix("/SelectRelation")}" method="POST">
 <p>
  Pøejete si pokraèovat s relací ${TOOL.childName(CURRENT)}?
  <input type="submit" name="finish" value="Ano">
 </p>
  <input type="hidden" NAME="currentId" VALUE="${CURRENT.id}">
  ${TOOL.saveParams(PARAMS, ["confirm","currentId","enteredId"])}
</form>

<#include "../footer.ftl">
