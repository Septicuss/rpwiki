package wiki.resourcepack;

import java.util.Set;

import wiki.resourcepack.mainframe.Mainframe;
import wiki.resourcepack.modules.generator.GeneratorModule;
import wiki.resourcepack.modules.server.ServerModule;
import wiki.resourcepack.modules.update.UpdateModule;

public class Bootstrap {
	
	private static Mainframe mainframe;

	public static void main(String[] args) {
		mainframe();
		mainframe.register("update", new UpdateModule(mainframe));
		mainframe.register("generator", new GeneratorModule());
		mainframe.register("server", new ServerModule());
		
		run();
	}
	
	public static void run() {
		Set<String> ids = mainframe.moduleIds();
		ids.forEach(mainframe::runModule);
		
		Runtime.getRuntime().addShutdownHook(new Thread(() -> {
			ids.forEach(mainframe::stopModule);
		}));
	}
	
	public static Mainframe mainframe() {
		if (mainframe == null)
			mainframe = Mainframe.create();
		return mainframe;
	}
	
}
