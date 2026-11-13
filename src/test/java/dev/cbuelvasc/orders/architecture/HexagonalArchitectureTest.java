package dev.cbuelvasc.orders.architecture;

import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

/** The hexagon is only real if a test enforces it. */
@AnalyzeClasses(packages = "dev.cbuelvasc.orders", importOptions = ImportOption.DoNotIncludeTests.class)
class HexagonalArchitectureTest {

    @ArchTest
    static final ArchRule domainIsPureJava = noClasses()
            .that().resideInAPackage("..domain..")
            .should().dependOnClassesThat().resideInAnyPackage(
                    "org.springframework..", "jakarta..", "io.micrometer..", "io.modelcontextprotocol..",
                    "java.sql..");

    @ArchTest
    static final ArchRule applicationDoesNotDependOnFrameworks = noClasses()
            .that().resideInAPackage("..application..")
            .should().dependOnClassesThat().resideInAnyPackage("org.springframework..", "jakarta..");

    @ArchTest
    static final ArchRule coreDoesNotKnowInfrastructure = noClasses()
            .that().resideInAnyPackage("..domain..", "..application..")
            .should().dependOnClassesThat().resideInAPackage("..infrastructure..");

    @ArchTest
    static final ArchRule onlyTheMcpAdapterKnowsMcp = noClasses()
            .that().resideOutsideOfPackage("..adapter.in.mcp..")
            .should().dependOnClassesThat().resideInAnyPackage(
                    "org.springframework.ai..", "io.modelcontextprotocol..");

    @ArchTest
    static final ArchRule onlyTheJdbcAdapterKnowsSql = noClasses()
            .that().resideOutsideOfPackage("..adapter.out.jdbc..")
            .should().dependOnClassesThat().resideInAnyPackage("org.springframework.jdbc..", "java.sql..");

    @ArchTest
    static final ArchRule adaptersTalkToPortsNotToTheService = noClasses()
            .that().resideInAPackage("..adapter.in..")
            .should().dependOnClassesThat().resideInAPackage("..application..");
}
