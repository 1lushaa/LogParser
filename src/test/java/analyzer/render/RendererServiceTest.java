package analyzer.render;

import java.util.stream.Stream;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("RendererService test.")
class RendererServiceTest {

    private static Stream<String> getInvalidRenderersNames() {
        return Stream.of(
            "typo",
            "marKkdown",
            "aDdoc"
        );
    }

    private static Stream<String> getValidRenderersNames() {
        return Stream.of(
            "marKdOwn",
            "ADoC",
            "markdown",
            "adoc"
        );
    }

    private static Stream<Arguments> getValidRenderersNamesWithClasses() {
        return Stream.of(
            Arguments.of("markdown", MarkdownRenderer.class),
            Arguments.of("adoc", AdocRenderer.class)
        );
    }

    @ParameterizedTest
    @MethodSource("getInvalidRenderersNames")
    @DisplayName("Non-existent renderers test.")
    public void invalidRendersExistsTest_ExpectFalse(String format) {
        assertThat(RendererService.isRendererExists(format)).isFalse();
    }

    @ParameterizedTest
    @MethodSource("getInvalidRenderersNames")
    @DisplayName("Existing renderers test.")
    public void gettingInvalidRenderersTest_ExpectNull(String format) {
        assertThat(RendererService.getRenderer(format)).isNull();
    }

    @ParameterizedTest
    @MethodSource("getValidRenderersNames")
    @DisplayName("Getting non-existent renderers by format test.")
    public void validRenderersExistsTest_ExpectTrue(String format) {
        assertThat(RendererService.isRendererExists(format)).isTrue();
    }

    @ParameterizedTest
    @MethodSource("getValidRenderersNamesWithClasses")
    @DisplayName("Getting existing renderers by format test.")
    public void gettingValidRenderersTest_ExpectNull(String format, Class<? extends AbstractRenderer> rendererClass) {
        assertThat(RendererService.getRenderer(format)).isInstanceOf(rendererClass);
    }
}
