package cucumber.runtime.junit;

import cucumber.annotation.DummyWhen;
import cucumber.api.CucumberOptions;
import cucumber.api.junit.Cucumber;
import cucumber.runtime.CucumberException;
import cucumber.runtime.junit.categories.CategoryFilterFactory;
import cucumber.runtime.junit.categories.UsedCategory;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.FilterFactory;
import org.junit.runner.RunWith;
import org.junit.runner.manipulation.NoTestsRemainException;
import org.junit.runners.model.InitializationError;

import java.io.File;
import java.io.IOException;
import java.util.List;

import static java.util.Collections.emptyList;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.fail;

public class CucumberTest {

    private String dir;

    @Before
    public void ensureDirectory() {
        dir = System.getProperty("user.dir");
        if (dir.endsWith("cucumber-jvm")) {
            // Might be the case if we're running in an IDE - at least in IDEA.
            System.setProperty("user.dir", new File(dir, "junit").getAbsolutePath());
        }
    }

    @After
    public void ensureOriginalDirectory() {
        System.setProperty("user.dir", dir);
    }

    @Test
    public void finds_features_based_on_implicit_package() throws IOException, InitializationError {
        Cucumber cucumber = new Cucumber(ImplicitFeatureAndGluePath.class);
        assertEquals(3, cucumber.getChildren().size());
        assertEquals("Feature: FA", cucumber.getChildren().get(0).getName());
    }

    @Test
    public void finds_features_based_on_explicit_root_package() throws IOException, InitializationError {
        Cucumber cucumber = new Cucumber(ExplicitFeaturePath.class);
        assertEquals(3, cucumber.getChildren().size());
        assertEquals("Feature: FA", cucumber.getChildren().get(0).getName());
    }

    @Test
    public void testThatParsingErrorsIsNicelyReported() throws Exception {
        try {
            new Cucumber(LexerErrorFeature.class);
            fail("Expecting error");
        } catch (CucumberException e) {
            assertEquals("Error parsing feature file cucumber/runtime/error/lexer_error.feature", e.getMessage());
        }
    }

    @Test
    public void testThatFileIsNotCreatedOnParsingError() throws Exception {
        try {
            new Cucumber(FormatterWithLexerErrorFeature.class);
            fail("Expecting error");
        } catch (CucumberException e) {
            assertFalse("File is created despite Lexor Error", new File("lexor_error_feature.json").exists());
        }
    }

    @Test
    public void testThatFilteringBasedOnJunitCategoriesWorks() throws IOException, InitializationError, NoTestsRemainException, FilterFactory.FilterNotCreatedException {
        Cucumber cucumber = new Cucumber(CategoryFeature.class);
        cucumber.filter(CategoryFilterFactory.includeCategory(UsedCategory.class));
        assertEquals(3, cucumber.getChildren().size());
        assertEquals("Feature: FA", cucumber.getChildren().get(0).getName());
    }

    @Test
    public void testThatFilteringBasedOnJunitCategoriesExclude() throws IOException, InitializationError, NoTestsRemainException, FilterFactory.FilterNotCreatedException {
        Cucumber cucumber = new Cucumber(ImplicitFeatureAndGluePath.class);
        cucumber.filter(CategoryFilterFactory.includeCategory(UsedCategory.class));
        assertEquals(0, cucumber.getChildren().size());
    }

    @Test
    public void finds_no_features_when_explicit_feature_path_has_no_features() throws IOException, InitializationError {
        Cucumber cucumber = new Cucumber(ExplicitFeaturePathWithNoFeatures.class);
        List<FeatureRunner> children = cucumber.getChildren();
        assertEquals(emptyList(), children);
    }

    @RunWith(Cucumber.class)
    private class RunCukesTestValidEmpty {
    }

    @RunWith(Cucumber.class)
    private class RunCukesTestValidIgnored {
        public void ignoreMe() {
        }
    }

    @RunWith(Cucumber.class)
    private class RunCukesTestInvalid {
        @DummyWhen
        public void ignoreMe() {
        }
    }

    @Test
    public void no_stepdefs_in_cucumber_runner_valid() {
        Assertions.assertNoCucumberAnnotatedMethods(RunCukesTestValidEmpty.class);
        Assertions.assertNoCucumberAnnotatedMethods(RunCukesTestValidIgnored.class);
    }

    @Test(expected = CucumberException.class)
    public void no_stepdefs_in_cucumber_runner_invalid() {
        Assertions.assertNoCucumberAnnotatedMethods(RunCukesTestInvalid.class);
    }

    public class ImplicitFeatureAndGluePath {
    }

    @CucumberOptions(features = {"classpath:cucumber/runtime/junit"})
    public class ExplicitFeaturePath {
    }

    @Category(UsedCategory.class)
    public class CategoryFeature {
    }

    @CucumberOptions(features = {"classpath:gibber/ish"})
    public class ExplicitFeaturePathWithNoFeatures {
    }

    @CucumberOptions(features = {"classpath:cucumber/runtime/error/lexer_error.feature"})
    public class LexerErrorFeature {

    }

    @CucumberOptions(features = {"classpath:cucumber/runtime/error/lexer_error.feature"}, plugin = {"json:lexor_error_feature.json"})
    public class FormatterWithLexerErrorFeature {

    }

}
