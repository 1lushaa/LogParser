package backend.academy.analyzer.args;

import backend.academy.analyzer.render.AbstractRenderer;
import backend.academy.analyzer.render.RendererService;
import com.beust.jcommander.IStringConverter;
import com.beust.jcommander.ParameterException;

public class RendererConverter implements IStringConverter<AbstractRenderer> {

    @Override
    public AbstractRenderer convert(String renderer) {
        if (!RendererService.isRendererExists(renderer)) {
            throw new ParameterException("The format \"" + renderer + "\" is not allowed.");
        }
        return RendererService.getRenderer(renderer);
    }
}
