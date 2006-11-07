<#include "../header.ftl">

<@lib.showMessages/>

<#if PARAMS.rid?exists>
    <h1>Úprava kvízu</h1>
<#else>
    <h1>Vlo¾ení kvízu</h1>
</#if>

<p>
    Kvíz je sada desíti otázek s výbìrem ze tøí odpovìdí, pøièem¾
    jedna je správná. Na konci je zobrazen pøehled v¹ech otázek
    vèetnì správných odpovìdí. Vyplòte prosím v¹echna políèka.
</p>


<form action="${URL.noPrefix("/EditTrivia")}" method="POST">
    <table border="0" cellpadding="10px">
        <tr>
            <td class="required">Jméno</td>
            <td>
                <input type="text" name="title" value="${PARAMS.title?if_exists}" size="40" tabindex="1">
                <div class="error">${ERRORS.title?if_exists}</div>
            </td>
        </tr>
        <tr>
            <td class="required">Popis</td>
            <td>
                <textarea name="desc" class="siroka" rows="4" tabindex="2">${PARAMS.desc?if_exists}</textarea>
                <div class="error">${ERRORS.desc?if_exists}</div>
            </td>
        </tr>
        <tr>
            <td class="required">Slo¾itost</td>
            <td>
                <@lib.showOption "difficulty", "simple", "jednoduchá", "radio"/>
                <@lib.showOption "difficulty", "normal", "normální", "radio"/>
                <@lib.showOption "difficulty", "hard", "tì¾ká", "radio"/>
                <@lib.showOption "difficulty", "guru", "guru", "radio"/>
            </td>
        </tr>
        <tr>
            <td class="required">Otázka 1</td>
            <td>
                <table border="0">
                    <tr>
                        <td align="right" class="required">Otázka</td>
                        <td>
                            <input type="text" name="q1question" value="${PARAMS.q1question?if_exists}" size="40">
                            <div class="error">${ERRORS.q1question?if_exists}</div>
                        </td>
                    </tr>
                    <tr>
                        <td align="right" class="required">Správná odpovìï</td>
                        <td>
                            <input type="text" name="q1answear" value="${PARAMS.q1answear?if_exists}" size="40">
                            <div class="error">${ERRORS.q1answear?if_exists}</div>
                        </td>
                    </tr>
                    <tr>
                        <td align="right" class="required">©patná odpovìï 1</td>
                        <td>
                            <input type="text" name="q1bad1" value="${PARAMS.q1bad1?if_exists}" size="40">
                            <div class="error">${ERRORS.q1bad1?if_exists}</div>
                        </td>
                    </tr>
                    <tr>
                        <td align="right" class="required">©patná odpovìï 2</td>
                        <td>
                            <input type="text" name="q1bad2" value="${PARAMS.q1bad2?if_exists}" size="40">
                            <div class="error">${ERRORS.q1bad2?if_exists}</div>
                        </td>
                    </tr>
                </table>
            </td>
        </tr>
        <tr>
            <td class="required">Otázka 2</td>
            <td>
                <table border="0">
                    <tr>
                        <td align="right" class="required">Otázka</td>
                        <td>
                            <input type="text" name="q2question" value="${PARAMS.q2question?if_exists}" size="40">
                            <div class="error">${ERRORS.q2question?if_exists}</div>
                        </td>
                    </tr>
                    <tr>
                        <td align="right" class="required">Správná odpovìï</td>
                        <td>
                            <input type="text" name="q2answear" value="${PARAMS.q2answear?if_exists}" size="40">
                            <div class="error">${ERRORS.q2answear?if_exists}</div>
                        </td>
                    </tr>
                    <tr>
                        <td align="right" class="required">©patná odpovìï 1</td>
                        <td>
                            <input type="text" name="q2bad1" value="${PARAMS.q2bad1?if_exists}" size="40">
                            <div class="error">${ERRORS.q2bad1?if_exists}</div>
                        </td>
                    </tr>
                    <tr>
                        <td align="right" class="required">©patná odpovìï 2</td>
                        <td>
                            <input type="text" name="q2bad2" value="${PARAMS.q2bad2?if_exists}" size="40">
                            <div class="error">${ERRORS.q2bad2?if_exists}</div>
                        </td>
                    </tr>
                </table>
            </td>
        </tr>
        <tr>
            <td class="required">Otázka 3</td>
            <td>
                <table border="0">
                    <tr>
                        <td align="right" class="required">Otázka</td>
                        <td>
                            <input type="text" name="q3question" value="${PARAMS.q3question?if_exists}" size="40">
                            <div class="error">${ERRORS.q3question?if_exists}</div>
                        </td>
                    </tr>
                    <tr>
                        <td align="right" class="required">Správná odpovìï</td>
                        <td>
                            <input type="text" name="q3answear" value="${PARAMS.q3answear?if_exists}" size="40">
                            <div class="error">${ERRORS.q3answear?if_exists}</div>
                        </td>
                    </tr>
                    <tr>
                        <td align="right" class="required">©patná odpovìï 1</td>
                        <td>
                            <input type="text" name="q3bad1" value="${PARAMS.q3bad1?if_exists}" size="40">
                            <div class="error">${ERRORS.q3bad1?if_exists}</div>
                        </td>
                    </tr>
                    <tr>
                        <td align="right" class="required">©patná odpovìï 2</td>
                        <td>
                            <input type="text" name="q3bad2" value="${PARAMS.q3bad2?if_exists}" size="40">
                            <div class="error">${ERRORS.q3bad2?if_exists}</div>
                        </td>
                    </tr>
                </table>
            </td>
        </tr>
        <tr>
            <td class="required">Otázka 4</td>
            <td>
                <table border="0">
                    <tr>
                        <td align="right" class="required">Otázka</td>
                        <td>
                            <input type="text" name="q4question" value="${PARAMS.q4question?if_exists}" size="40">
                            <div class="error">${ERRORS.q4question?if_exists}</div>
                        </td>
                    </tr>
                    <tr>
                        <td align="right" class="required">Správná odpovìï</td>
                        <td>
                            <input type="text" name="q4answear" value="${PARAMS.q4answear?if_exists}" size="40">
                            <div class="error">${ERRORS.q4answear?if_exists}</div>
                        </td>
                    </tr>
                    <tr>
                        <td align="right" class="required">©patná odpovìï 1</td>
                        <td>
                            <input type="text" name="q4bad1" value="${PARAMS.q4bad1?if_exists}" size="40">
                            <div class="error">${ERRORS.q4bad1?if_exists}</div>
                        </td>
                    </tr>
                    <tr>
                        <td align="right" class="required">©patná odpovìï 2</td>
                        <td>
                            <input type="text" name="q4bad2" value="${PARAMS.q4bad2?if_exists}" size="40">
                            <div class="error">${ERRORS.q4bad2?if_exists}</div>
                        </td>
                    </tr>
                </table>
            </td>
        </tr>
        <tr>
            <td class="required">Otázka 5</td>
            <td>
                <table border="0">
                    <tr>
                        <td align="right" class="required">Otázka</td>
                        <td>
                            <input type="text" name="q5question" value="${PARAMS.q5question?if_exists}" size="40">
                            <div class="error">${ERRORS.q5question?if_exists}</div>
                        </td>
                    </tr>
                    <tr>
                        <td align="right" class="required">Správná odpovìï</td>
                        <td>
                            <input type="text" name="q5answear" value="${PARAMS.q5answear?if_exists}" size="40">
                            <div class="error">${ERRORS.q5answear?if_exists}</div>
                        </td>
                    </tr>
                    <tr>
                        <td align="right" class="required">©patná odpovìï 1</td>
                        <td>
                            <input type="text" name="q5bad1" value="${PARAMS.q5bad1?if_exists}" size="40">
                            <div class="error">${ERRORS.q5bad1?if_exists}</div>
                        </td>
                    </tr>
                    <tr>
                        <td align="right" class="required">©patná odpovìï 2</td>
                        <td>
                            <input type="text" name="q5bad2" value="${PARAMS.q5bad2?if_exists}" size="40">
                            <div class="error">${ERRORS.q5bad2?if_exists}</div>
                        </td>
                    </tr>
                </table>
            </td>
        </tr>
        <tr>
            <td class="required">Otázka 6</td>
            <td>
                <table border="0">
                    <tr>
                        <td align="right" class="required">Otázka</td>
                        <td>
                            <input type="text" name="q6question" value="${PARAMS.q6question?if_exists}" size="40">
                            <div class="error">${ERRORS.q6question?if_exists}</div>
                        </td>
                    </tr>
                    <tr>
                        <td align="right" class="required">Správná odpovìï</td>
                        <td>
                            <input type="text" name="q6answear" value="${PARAMS.q6answear?if_exists}" size="40">
                            <div class="error">${ERRORS.q6answear?if_exists}</div>
                        </td>
                    </tr>
                    <tr>
                        <td align="right" class="required">©patná odpovìï 1</td>
                        <td>
                            <input type="text" name="q6bad1" value="${PARAMS.q6bad1?if_exists}" size="40">
                            <div class="error">${ERRORS.q6bad1?if_exists}</div>
                        </td>
                    </tr>
                    <tr>
                        <td align="right" class="required">©patná odpovìï 2</td>
                        <td>
                            <input type="text" name="q6bad2" value="${PARAMS.q6bad2?if_exists}" size="40">
                            <div class="error">${ERRORS.q6bad2?if_exists}</div>
                        </td>
                    </tr>
                </table>
            </td>
        </tr>
        <tr>
            <td class="required">Otázka 7</td>
            <td>
                <table border="0">
                    <tr>
                        <td align="right" class="required">Otázka</td>
                        <td>
                            <input type="text" name="q7question" value="${PARAMS.q7question?if_exists}" size="40">
                            <div class="error">${ERRORS.q7question?if_exists}</div>
                        </td>
                    </tr>
                    <tr>
                        <td align="right" class="required">Správná odpovìï</td>
                        <td>
                            <input type="text" name="q7answear" value="${PARAMS.q7answear?if_exists}" size="40">
                            <div class="error">${ERRORS.q7answear?if_exists}</div>
                        </td>
                    </tr>
                    <tr>
                        <td align="right" class="required">©patná odpovìï 1</td>
                        <td>
                            <input type="text" name="q7bad1" value="${PARAMS.q7bad1?if_exists}" size="40">
                            <div class="error">${ERRORS.q7bad1?if_exists}</div>
                        </td>
                    </tr>
                    <tr>
                        <td align="right" class="required">©patná odpovìï 2</td>
                        <td>
                            <input type="text" name="q7bad2" value="${PARAMS.q7bad2?if_exists}" size="40">
                            <div class="error">${ERRORS.q7bad2?if_exists}</div>
                        </td>
                    </tr>
                </table>
            </td>
        </tr>
        <tr>
            <td class="required">Otázka 8</td>
            <td>
                <table border="0">
                    <tr>
                        <td align="right" class="required">Otázka</td>
                        <td>
                            <input type="text" name="q8question" value="${PARAMS.q8question?if_exists}" size="40">
                            <div class="error">${ERRORS.q8question?if_exists}</div>
                        </td>
                    </tr>
                    <tr>
                        <td align="right" class="required">Správná odpovìï</td>
                        <td>
                            <input type="text" name="q8answear" value="${PARAMS.q8answear?if_exists}" size="40">
                            <div class="error">${ERRORS.q8answear?if_exists}</div>
                        </td>
                    </tr>
                    <tr>
                        <td align="right" class="required">©patná odpovìï 1</td>
                        <td>
                            <input type="text" name="q8bad1" value="${PARAMS.q8bad1?if_exists}" size="40">
                            <div class="error">${ERRORS.q8bad1?if_exists}</div>
                        </td>
                    </tr>
                    <tr>
                        <td align="right" class="required">©patná odpovìï 2</td>
                        <td>
                            <input type="text" name="q8bad2" value="${PARAMS.q8bad2?if_exists}" size="40">
                            <div class="error">${ERRORS.q8bad2?if_exists}</div>
                        </td>
                    </tr>
                </table>
            </td>
        </tr>
        <tr>
            <td class="required">Otázka 9</td>
            <td>
                <table border="0">
                    <tr>
                        <td align="right" class="required">Otázka</td>
                        <td>
                            <input type="text" name="q9question" value="${PARAMS.q9question?if_exists}" size="40">
                            <div class="error">${ERRORS.q9question?if_exists}</div>
                        </td>
                    </tr>
                    <tr>
                        <td align="right" class="required">Správná odpovìï</td>
                        <td>
                            <input type="text" name="q9answear" value="${PARAMS.q9answear?if_exists}" size="40">
                            <div class="error">${ERRORS.q9answear?if_exists}</div>
                        </td>
                    </tr>
                    <tr>
                        <td align="right" class="required">©patná odpovìï 1</td>
                        <td>
                            <input type="text" name="q9bad1" value="${PARAMS.q9bad1?if_exists}" size="40">
                            <div class="error">${ERRORS.q9bad1?if_exists}</div>
                        </td>
                    </tr>
                    <tr>
                        <td align="right" class="required">©patná odpovìï 2</td>
                        <td>
                            <input type="text" name="q9bad2" value="${PARAMS.q9bad2?if_exists}" size="40">
                            <div class="error">${ERRORS.q9bad2?if_exists}</div>
                        </td>
                    </tr>
                </table>
            </td>
        </tr>
        <tr>
            <td class="required">Otázka 10</td>
            <td>
                <table border="0">
                    <tr>
                        <td align="right" class="required">Otázka</td>
                        <td>
                            <input type="text" name="q10question" value="${PARAMS.q10question?if_exists}" size="40">
                            <div class="error">${ERRORS.q10question?if_exists}</div>
                        </td>
                    </tr>
                    <tr>
                        <td align="right" class="required">Správná odpovìï</td>
                        <td>
                            <input type="text" name="q10answear" value="${PARAMS.q10answear?if_exists}" size="40">
                            <div class="error">${ERRORS.q10answear?if_exists}</div>
                        </td>
                    </tr>
                    <tr>
                        <td align="right" class="required">©patná odpovìï 1</td>
                        <td>
                            <input type="text" name="q10bad1" value="${PARAMS.q10bad1?if_exists}" size="40">
                            <div class="error">${ERRORS.q10bad1?if_exists}</div>
                        </td>
                    </tr>
                    <tr>
                        <td align="right" class="required">©patná odpovìï 2</td>
                        <td>
                            <input type="text" name="q10bad2" value="${PARAMS.q10bad2?if_exists}" size="40">
                            <div class="error">${ERRORS.q10bad2?if_exists}</div>
                        </td>
                    </tr>
                </table>
            </td>
        </tr>
    </table>

    <input type="submit" value="Dokonèit">
    <#if PARAMS.rid?exists>
        <input type="hidden" name="action" value="edit2">
        <input type="hidden" name="rid" value="${PARAMS.rid}">
    <#else>
        <input type="hidden" name="action" value="add2">
    </#if>
</form>


<#include "../footer.ftl">
