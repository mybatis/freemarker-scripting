MyBatis FreeMarker Support
========================

[![Build Status](https://travis-ci.org/elw00d/mybatis-freemarker.svg?branch=master)](https://travis-ci.org/elw00d/mybatis-freemarker)

![mybatis-velocity](http://mybatis.github.io/images/mybatis-logo.png)

MyBatis FreeMarker Scripting Support.

Getting started
===============

## Introduction

mybatis-freemarker is a plugin that helps creating big dynamic SQL queries. You can use it selectively, to only queries that need if statmenets or foreach-loops, for example. But it is possible to use this syntax by default too.

If you are not familiar with FreeMarker syntax, you can view [Template Language Reference](http://freemarker.org/docs/ref.html)

## Install from sources

- Checkout the source code
- Run `mvn install` to build and to automatically install it to your local maven repo
- Add maven dependency to your project

```xml
<dependency>
  <groupId>org.mybatis.scripting</groupId>
  <artifactId>mybatis-freemarker</artifactId>
  <version>1.0-SNAPSHOT</version>
</dependency>
```

## Configuring

### Common

- (Optional) Create `mybatis-freemarker.properties` file in your classpath:

```
basePackage=sql
```

This will define base package to search FreeMarker templates. By default it is empty string, so you will need to provide full path to template every time.

### XML-driven mappers

If your are using annotations-driven mappers, you don't need to do anything more. If you are using XML-driven mappers too, you may need to do next steps:

- Register the language driver alias in your mybatis configuration file:

```xml
<configuration>
  ...
  <typeAliases>
    <typeAlias alias="freemarker" type="org.mybatis.scripting.freemarker.FreeMarkerLanguageDriver"/>
  </typeAliases>
  ...
</configuration>
```

- (Optional) Set the freemarker as your default scripting language:

```xml
<configuration>
  ...
  <settings>
      <setting name="defaultScriptingLanguage" value="freemarker"/>
  </settings>
  ...
</configuration>
```

## Usage in annotations-driven mappers

Just write your queries using FreeMarker syntax:

```java
@Lang(FreeMarkerLanguageDriver.class)
@Select("select * from names where id in (${ids?join(',')})")
List<Name> findNamesByIds(@Param("ids") List<Integer> ids);
```

If any whitespace found inside `@Select` text, it is interpreted as inline script, not template name. It is convenient to avoid creating templates when script is really small. If you have a large SQL script, you can place it in distinct template and write next code:

```java
@Lang(FreeMarkerLanguageDriver.class)
@Select("findName.ftl")
Name findName(@Param("n") String name);
```

Template will be searched in classpath using `basePackage` property that has already been described above.

`findName.ftl` content can be:

```
SELECT *
FROM names
where firstName = <@p name="n"/>
```

`<@p name="n"/>` is a custom directive to generate `#{n}` markup. This markup further will be passed into MyBatis engine, and it will replace this to `?`-parameter. You can't write `#{paramName}` directly, because FreeMarker supports this syntax natively (alghough it is deprecated). So, to get `?`-parameters to prepared statements works, you need to use `${r"#{paramName}"}` verbose syntax, or this directive. By the way, in XML files `${r"#{paramName}"}` is more preferrable because you don't need wrap it using `CDATA` statements. In annotations and in external templates `<@p/>` directive is more neat.

## Usage in XML-driven mappers

As in annotations, you can write inline scripts or template names.

```xml
<!-- This is handled by FreeMarker too, because it is included into select nodes AS IS -->
<sql id="cols">id, ${r"firstName"}, lastName</sql>

<select id="findName" resultType="org.mybatis.scripting.freemarker.Name" lang="freemarker">
    findName.ftl
</select>

<select id="findNamesByIds" resultType="org.mybatis.scripting.freemarker.Name" lang="freemarker">
    select <include refid="cols"/> from names where id in (${ids?join(',')})
</select>

<!-- It is not very convenient - to use CDATA blocks. Better is to create external template
    or use more verbose syntax: ${r"#{id}"}. -->
<select id="find" resultType="org.mybatis.scripting.freemarker.Name" lang="freemarker">
    select * from names where id = <![CDATA[ <@p name='id'/> ]]> and id = ${id}
</select>
```

## Examples

You can view full-featured example of configuring and of both XML-mapper and annotations-driven mapper usage in [test suite](https://github.com/elw00d/mybatis-freemarker/tree/master/src/test)