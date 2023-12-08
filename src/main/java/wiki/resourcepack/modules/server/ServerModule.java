package wiki.resourcepack.modules.server;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import io.javalin.Javalin;
import wiki.resourcepack.Bootstrap;
import wiki.resourcepack.modules.WikiModule;

public class ServerModule implements WikiModule {

	@Override
	public void run() {

		String generated = Bootstrap.mainframe().config().getString("generator.destination");
		
		Javalin app = Javalin.create();

		app.get("/*", ctx -> {
			String contextPath = ctx.path();
			if (contextPath.equals("/") || contextPath.equals("/index") || contextPath.equals("/index.html")) {
				contextPath = "/index.html";
			}

			String path = generated + "/" + contextPath
					+ (!contextPath.contains(".") || contextPath.substring(contextPath.lastIndexOf(".") + 1).isEmpty()
							? ".html"
							: "");

			Path filePath = Paths.get(path);

			
			if (!filePath.toFile().exists()) {
				filePath = Paths.get(generated + "/404.html");
				ctx.status(404);
			}
			
			String contentType;
			
			if (filePath.toString().toLowerCase().endsWith(".ico")) {
				contentType = "images/x-icon";
			} else {
				contentType = Files.probeContentType(filePath);
			}
			
			if (contentType == null) {
				contentType = "text/html";
			}

			ctx.contentType(contentType);
			ctx.result(Files.newInputStream(filePath));
		});

		app.start(5050);
	}

	@Override
	public void stop() {
		
	}

}
