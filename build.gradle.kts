// Coding standards are enforced here and in each module's build file.
// `./gradlew check` runs everything; see CLAUDE.md for the full list.
plugins {
    base
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.spotless)
    alias(libs.plugins.cpd)
}

// Formatting: `./gradlew spotlessApply` fixes it, `spotlessCheck` (part of `check`) verifies it.
spotless {
    java {
        target("*/src/**/*.java")
        palantirJavaFormat(libs.versions.palantir.java.format.get())
        removeUnusedImports()
        forbidWildcardImports()
    }
    kotlinGradle {
        target("*.gradle.kts", "*/*.gradle.kts")
        trimTrailingWhitespace()
        endWithNewline()
    }
    format("misc") {
        target("*.md", "docs/**/*.md", ".github/**/*.yml", ".githooks/*", "gradle/*.toml", "*/src/**/*.xml")
        trimTrailingWhitespace()
        endWithNewline()
    }
}

// DRY: fail the build on copy-pasted blocks of code.
cpd {
    language = "java"
    toolVersion = libs.versions.pmd.get()
    minimumTokenCount = 70
}
tasks.named<de.aaschmid.gradle.plugins.cpd.Cpd>("cpdCheck") {
    source = fileTree(".") { include("*/src/main/java/**/*.java") }
    reports {
        text.required.set(true)
        xml.required.set(false)
    }
}

// Point git at the versioned hooks in .githooks so every clone runs the same checks.
val installGitHooks by tasks.registering(Exec::class) {
    description = "Uses the git hooks in .githooks for this clone."
    group = "verification"
    commandLine("git", "config", "core.hooksPath", ".githooks")
    isIgnoreExitValue = true
}

tasks.named("check") {
    dependsOn("spotlessCheck", "cpdCheck")
}
