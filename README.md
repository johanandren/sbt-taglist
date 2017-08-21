# About
[![Build Status](https://travis-ci.org/johanandren/sbt-taglist.svg?branch=master)](https://travis-ci.org/johanandren/sbt-taglist)

Small SBT plugin that will contain roughly the functionality of the maven-tag-list plugin when done.


The plugin contains the task "tag-list" which performs a case insensitive search in the sources 
of a project for a set of keywords. The default keywords are "todo" and "fixme"

# Installation

The plugin is available from the community sbt-plugins repository on bintray, add a dependency on it in your project
(a common place is PROJECT_DIR/project/plugins.sbt):

    addSbtPlugin("com.markatta" % "taglist-plugin" % "1.4.0")

# Configuration
The plugin uses the regular source settings for your sbt-project so it should work out of the box with non-standard directory structures etc. 

### 1.4+:

In 1.4 all settings where renamed to be prefixed with `tagList` to not cause conflicts in the key namespace. This means the setting `tagWords` now is `tagListWords` etc. 

The default settings only looks for tags in `src/main/scala`. To configure additional source directories, for example to include the test sources:

```Scala
sources in tagList := (sources in Compile).value ++ (sources in Test).value
```

### 1.3+:
The default tags looked for are "todo" and "fixme". To override those you can either just simply set a list of words,
all of them will yield warnings in the log when found:

```Scala
tagListWords := Seq("incorrect", "plainwrong", "dontcheckin")
```

Or if it better suits your needs you can specify each word along with a log level:

```Scala
tagListTags := Set(Tag("todo", TagListPlugin.Info), Tag("fixme", TagListPlugin.Info))
```
