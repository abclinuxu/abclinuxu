package cz.abclinuxu.utils.format;

import java.util.Map;

/*
 * User: literakl
 * Date: 29.1.2004
 * Time: 19:33:27
 */

/**
 * Renders text.
 */
public interface Renderer {
    /** Params, which instructs renderer to replace emoticons with images */
    String RENDER_EMOTICONS = "render emoticons";

    /**
     * Renders input.
     * @param input Input text.
     * @param params Params, that may influence way, how the input is rendered.
     * @return Rendered text.
     */
    String render(String input, Map params);
}
