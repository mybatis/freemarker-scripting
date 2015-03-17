select * from names where firstName in (
    <#list ids as id>
        <@p value=id/>
        <#if id_has_next>,</#if>
    </#list>
)