package wiki.resourcepack.modules.generator.render;

import java.io.IOException;
import java.io.Writer;
import java.util.HashMap;
import java.util.Map;

import wiki.resourcepack.modules.generator.Templates;
import wiki.resourcepack.modules.generator.model.ErrorPage;

public class ErrorRenderer implements Renderer<ErrorPage> {

	protected ErrorRenderer() {
	}

	@Override
	public void render(ErrorPage object, Map<String, Object> context, Writer writer) {

		Map<String, Object> errorContext = new HashMap<>();
		errorContext.putAll(context);
		errorContext.put("error", object);

		try {
			Templates.ERROR_TEMPLATE.evaluate(writer, errorContext);
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

}
