<#assign strValue='Wilma' />
<#assign intValue=5/>
<#assign doubleValue=10.5/>
<#assign objectValue=innerObject/>
<#assign innerStringProp=innerObject.strValue/>

select * from names where firstName = <@p value='Wilma'/>
and '${strValue}' = <@p value=strValue/>
and ${intValue} = <@p value=intValue/>
and ${doubleValue} = <@p value=doubleValue/>
and '${innerStringProp}' = <@p value=innerObject.strValue/>
