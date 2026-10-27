pluginManagement { repositories { google { content { includeGroupByRegex("androidx.*");includeGroupByRegex("com.android.*");includeGroupByRegex("com.google.*") } }; mavenCentral(); gradlePluginPortal() } }
dependencyResolutionManagement { repositories { google { content { includeGroupByRegex("androidx.*");includeGroupByRegex("com.android.*");includeGroupByRegex("com.google.*") } }; mavenCentral() } }
rootProject.name = "AgroCampo"
include(":shared", ":androidApp")
