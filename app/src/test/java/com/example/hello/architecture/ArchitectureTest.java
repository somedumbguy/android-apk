package com.example.hello.architecture;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noFields;

import com.example.hello.app.AppContainer;
import com.example.hello.auth.MockAuthProvider;
import com.example.hello.data.SharedPrefsSettingsRepository;
import com.example.hello.data.SharedPrefsWorkoutRepository;
import com.example.hello.domain.forecast.Forecaster;
import com.example.hello.domain.history.SettingsRepository;
import com.example.hello.domain.history.WorkoutRepository;
import com.example.hello.domain.plates.PlateMath;
import com.example.hello.domain.progression.ProgressionEngine;
import com.example.hello.domain.training.TrainingService;
import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.lang.ArchRule;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Calendar;
import java.util.Date;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * The project's design rules, checked against the compiled code. Breaking one fails the build.
 * Each rule says why it exists; change a rule only when the design deliberately changes.
 */
public class ArchitectureTest {
    private static final String ROOT = "com.example.hello";
    private static JavaClasses classes;

    @BeforeClass
    public static void importClasses() {
        classes = new ClassFileImporter()
                .withImportOption(ImportOption.Predefined.DO_NOT_INCLUDE_TESTS)
                // Android puts compiled unit tests in .../debugUnitTest/..., which the default misses.
                .withImportOption(location -> !location.contains("UnitTest/"))
                // Shared test fakes wire objects themselves, like tests do.
                .withImportOption(location -> !location.contains("/domain/fakes/"))
                .importPackages(ROOT);
    }

    private static void check(ArchRule rule) {
        rule.check(classes);
    }

    /** Domain logic runs anywhere and is tested on the JVM, so it can't know about Android or the app. */
    @Test
    public void domainDependsOnNothingButJava() {
        check(noClasses()
                .that()
                .resideInAPackage(ROOT + ".domain..")
                .should()
                .dependOnClassesThat()
                .resideInAnyPackage("android..", "androidx..")
                .orShould()
                .dependOnClassesThat()
                .resideInAnyPackage(ROOT + ".app..", ROOT + ".data..", ROOT + ".auth..", ROOT + ".ui..", ROOT)
                .because("the domain must stay pure Java"));
    }

    /** Presenters and formatters hold the screens' logic, so they must be testable without a phone. */
    @Test
    public void presentersAndFormattersAvoidAndroid() {
        check(noClasses()
                .that()
                .haveSimpleNameEndingWith("Presenter")
                .or()
                .haveSimpleNameEndingWith("Formatter")
                .or()
                .haveSimpleNameEndingWith("State")
                .or()
                .resideInAPackage(ROOT + ".ui.format..")
                .or()
                .haveSimpleName("RestTimer")
                .should()
                .dependOnClassesThat()
                .resideInAnyPackage("android..", "androidx..")
                .because("screen logic must be unit-testable on the JVM"));
    }

    /** Screens use storage only through the domain's interfaces, never the implementations. */
    @Test
    public void uiDoesNotUseStorageImplementations() {
        check(noClasses()
                .that()
                .resideInAPackage(ROOT + ".ui..")
                .should()
                .dependOnClassesThat()
                .resideInAPackage(ROOT + ".data..")
                .because("storage is swappable behind WorkoutRepository and SettingsRepository"));
    }

    /** Views only draw state; their content comes from presenters. */
    @Test
    public void viewsDoNotCallUseCasesDirectly() {
        check(noClasses()
                .that()
                .haveSimpleNameEndingWith("View")
                .or()
                .haveSimpleNameEndingWith("Sheet")
                .should()
                .dependOnClassesThat()
                .resideInAnyPackage(ROOT + ".domain.training..", ROOT + ".domain.history..")
                .because("views render presenter state and contain no logic"));
    }

    /** Dependency injection: only the composition root creates implementations. */
    @Test
    public void onlyTheContainerWiresImplementations() {
        check(noClasses()
                .that()
                .doNotHaveFullyQualifiedName(AppContainer.class.getName())
                .should()
                .callConstructor(
                        SharedPrefsWorkoutRepository.class,
                        android.content.SharedPreferences.class,
                        com.example.hello.data.SeedData.class,
                        Clock.class)
                .orShould()
                .callConstructor(SharedPrefsSettingsRepository.class, android.content.SharedPreferences.class)
                .orShould()
                .callConstructor(MockAuthProvider.class, com.example.hello.auth.UserStore.class)
                .orShould()
                .callConstructor(Forecaster.class, Clock.class)
                .orShould()
                .callConstructor(
                        TrainingService.class,
                        WorkoutRepository.class,
                        SettingsRepository.class,
                        ProgressionEngine.class,
                        Forecaster.class,
                        PlateMath.class,
                        Clock.class)
                .because("AppContainer is the one place objects are created and wired"));
    }

    /** Time is injected through java.time.Clock, so results are repeatable in tests. */
    @Test
    public void onlyTheContainerReadsTheSystemClock() {
        check(noClasses()
                .that()
                .doNotHaveFullyQualifiedName(AppContainer.class.getName())
                .should()
                .callMethod(Clock.class, "systemDefaultZone")
                .orShould()
                .callMethod(Clock.class, "systemUTC")
                .orShould()
                .callMethod(System.class, "currentTimeMillis")
                .orShould()
                .callMethod(Instant.class, "now")
                .orShould()
                .callMethod(LocalDate.class, "now")
                .orShould()
                .callMethod(LocalTime.class, "now")
                .orShould()
                .callMethod(LocalDateTime.class, "now")
                .orShould()
                .callMethod(Calendar.class, "getInstance")
                .orShould()
                .callConstructor(Date.class)
                .because("pass a Clock in instead, so tests control time"));
    }

    /** Admin lives in a separate client backed by server-side roles, never in this app. See docs/security.md. */
    @Test
    public void noAdminCodeInTheConsumerApp() {
        check(noClasses()
                .should()
                .haveSimpleNameContaining("Admin")
                .because("anything in the APK can be found and switched on"));
    }

    /** Keys belong on the backend: anything in the APK is public. Catches the obvious cases. */
    @Test
    public void noHardCodedSecrets() {
        check(noFields()
                .that()
                .areStatic()
                .and()
                .areFinal()
                .and()
                .haveRawType(String.class)
                .should()
                .haveNameMatching("(?i).*(api_?key|secret|password|passwd|token|credential).*")
                .because("secrets stay on the server; see docs/security.md"));
    }
}
