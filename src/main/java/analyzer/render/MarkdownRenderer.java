package analyzer.render;

/**
 * A class that allows to get statistics in Markdown format.
 */
public class MarkdownRenderer extends AbstractRenderer {
    @Override
    protected String getFormatedHeader(String name) {
        return "#### " + name;
    }
}
