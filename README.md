Small SBT plugin that will mimic the behaviour of the maven-tag-list plugin when done.


The plugin contains the task "tag-list" which performs a case insensitive search in the sources 
for a set of keywords. The default keywords are "todo" and "fixme"

# Installation

Add the following lines to ~/.sbt/plugins/build.sbt or PROJECT_DIR/project/plugins.sbt:

    resolvers += Resolver.url("sbt-taglist-releases", new URL("http://johanandren.github.com/releases/"))(Resolver.ivyStylePatterns)
    
    addSbtPlugin("com.markatta" % "taglist-plugin" % "1.1")


And then this line to PROJECT_DIR/build.sbt to include the task tag-list into your project:

    seq(TagListPlugin.tagListSettings: _*)


# Configuration
The plugin uses the regular source settings for your sbt-project so it should work out of the box with non-standard directory structures etc. 

If you want to change what keywords the plugin searches for you can do so by modifying or appending like this in PROJECT_DIR/build.sbt:

    TagListPlugin.TagListKeys.tagWords += "laters"

