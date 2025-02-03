package analyzer.render;

/**
 * A class that allows to get statistics in Adoc format.
 */
public class AdocRenderer extends AbstractRenderer {
    @Override
    protected String getFormatedHeader(String name) {
        return "=== " + name;
    }
}
