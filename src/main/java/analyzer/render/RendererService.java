package analyzer.render;

import java.util.Map;
import lombok.experimental.UtilityClass;

/**
 * A class that allows to create instances of renders,
 * depending on the name of format.
 */
@UtilityClass
public class RendererService {

    private static final Map<String, AbstractRenderer> RENDERERS = Map.of(
        "markdown", new MarkdownRenderer(),
        "adoc", new AdocRenderer()
    );

    /**
     * Checks whether a renderer converting the data to the specified format exists.
     *
     * @param format name of the format.
     * @return {@code true} if renderer converting the data to the specified {@code format} exists,
     *     {@code false} otherwise.
     */
    public static boolean isRendererExists(String format) {
        return RENDERERS.containsKey(format.strip().toLowerCase());
    }

    /**
     * Returns a new instance of the renderer converting the data to the specified format,
     * if one exists, {@code null} otherwise.
     *
     * @param format name of the format.
     * @return a new instance of the renderer converting the data to the specified format,
     *     if one exists, {@code null} otherwise.
     */
    public static AbstractRenderer getRenderer(String format) {
        return RENDERERS.get(format.strip().toLowerCase());
    }
}
