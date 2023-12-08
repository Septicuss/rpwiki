package wiki.resourcepack.modules.update;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import dev.dejvokep.boostedyaml.YamlDocument;
import dev.dejvokep.boostedyaml.block.implementation.Section;
import wiki.resourcepack.mainframe.Mainframe;
import wiki.resourcepack.modules.WikiModule;
import wiki.resourcepack.modules.generator.GeneratorModule;
import wiki.resourcepack.utils.FileUtils;
import wiki.resourcepack.utils.Utils;

/**
 * Module responsible for checking a given repository for updates, downloading
 * them and notifying generator module to re-generate.
 */
public class UpdateModule implements WikiModule {

	private static final String DEFAULT_SOURCE_FOLDER = "source";
	private static final String LATEST_COMMIT_PATH = "latest_commit";
	private static final String TOKEN = System.getenv("github_token");

	private Mainframe mainframe;
	private Logger logger;

	private String repositoryOrganization;
	private String repositoryBranch;
	private String repositoryName;

	private String latestCommit;
	private File sourceFolder;

	public UpdateModule(Mainframe mainframe) {
		this.mainframe = mainframe;
		this.logger = mainframe.logger("Update");

	}

	@Override
	public void run() {

		ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(5);
		YamlDocument config = mainframe.config();

		setupVariables(config);
		setupFiles(config);

		Runnable checkUpdateTask = () -> {

			String latestSha = getLatestCommitSha();

			// No updates needed
			if (latestCommit != null && latestCommit.equals(latestSha) && !sourceFolderEmpty()) {
				logger.info("Already up to date");
				return;
			}

			logger.info("Update found. Initiating updater...");
			latestCommit = latestSha;

			saveVariables(config);

			FileUtils.createFolder(sourceFolder);
			FileUtils.clearDirectory(sourceFolder.toPath());

			File zip = new File(sourceFolder, "temp.zip");
			File temp = new File(sourceFolder, "temp");

			FileUtils.create(zip);
			FileUtils.createFolder(temp);

			// 1 Download zip
			Runnable downloadTask = () -> {
				downloadRepositoryAsZip(zip);
			};

			try {
				Future<?> future = scheduler.submit(downloadTask);
				future.get(10, TimeUnit.SECONDS);
			} catch (Exception e) {
				logger.error("Failed to download repository (timed out)");
				return;
			}

			// 2 Unzip zip to temp folder
			FileUtils.unzip(zip, temp);

			// 3 Relocate files out of temp folder into source folder
			for (File file : temp.listFiles()) {
				if (!file.isDirectory())
					continue;

				FileUtils.copyFilesFrom(file, sourceFolder);
			}

			// 4 Clean up
			FileUtils.clearDirectory(temp.toPath());
			zip.delete();
			temp.delete();

			logger.info("Update finished successfully");

			// 5 Done, trigger generator if present
			Runnable generatorTask = () -> {
				mainframe.module("generator").ifPresent(module -> {
					logger.info("Launching generator");
					GeneratorModule generator = (GeneratorModule) module;
					generator.run();
					generator.generate();
				});
			};
			
			try {
				Future<?> future = scheduler.submit(generatorTask);
				future.get(30, TimeUnit.SECONDS);
			} catch (Exception e) {
				e.printStackTrace();
				logger.error("Failed to generate (timed out)");
				return;
			}
		};

		scheduler.scheduleAtFixedRate(checkUpdateTask, 0, 20, TimeUnit.SECONDS);

		getContributors();

	}

	@Override
	public void stop() {
		YamlDocument config = mainframe.config();
		saveVariables(config);
	}

	private void setupVariables(YamlDocument config) {
		Section updateSection = config.getSection("update");

		repositoryOrganization = updateSection.getString("organization", "minepack");
		repositoryBranch = updateSection.getString("branch", "main");
		repositoryName = updateSection.getString("repository", "resourcepack-wiki");

		if (repositoryOrganization.isBlank())
			repositoryOrganization = "minepack";

		if (repositoryBranch.isBlank())
			repositoryBranch = "main";

		if (repositoryName.isBlank())
			repositoryName = "resourcepack-wiki";

		latestCommit = updateSection.getString(LATEST_COMMIT_PATH);
	}

	private void setupFiles(YamlDocument config) {
		Section generatorSection = config.getSection("generator");

		String sourcePath = generatorSection.getString("source", DEFAULT_SOURCE_FOLDER);
		sourceFolder = new File(sourcePath);

		FileUtils.createFolder(sourceFolder);
	}

	private void saveVariables(YamlDocument config) {
		Section updateSection = config.getSection("update");

		updateSection.set(LATEST_COMMIT_PATH, latestCommit);

		try {
			config.save();
		} catch (IOException e) {
			logger.error("Failed to save config");
		}
	}

	private boolean sourceFolderEmpty() {
		if (sourceFolder == null)
			return true;

		return sourceFolder.list().length == 0;
	}

	private String getLatestCommitSha() {
		String request = String.format("https://api.github.com/repos/%s/%s/commits?per_page=1", repositoryOrganization,
				repositoryName);
		String response = sendGetRequest(request);

		CommitInfo[] commitInfoArray = Utils.gson.fromJson(response, CommitInfo[].class);

		if (commitInfoArray.length > 0) {
			return commitInfoArray[0].getSha();
		}

		return null;
	}

	private String sendGetRequest(String apiUrl) {
		try {
			URL url = new URL(apiUrl);
			HttpURLConnection connection = (HttpURLConnection) url.openConnection();

			connection.setRequestMethod("GET");
			connection.setRequestProperty("Authorization", "Bearer " + TOKEN);
			connection.setRequestProperty("Accept", "application/vnd.github.v3+json");

			BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
			StringBuilder response = new StringBuilder();
			String line;
			while ((line = reader.readLine()) != null) {
				response.append(line);
			}
			reader.close();

			connection.disconnect();

			return response.toString();
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}
	}

	private List<String> getContributors() {
		List<String> contributors = new ArrayList<>();

		String request = String.format("https://api.github.com/repos/%s/%s/contributors", repositoryOrganization,
				repositoryName);
		String response = sendGetRequest(request);

		if (response == null)
			return contributors;

		JsonArray contributorsArray = Utils.gson.fromJson(response, JsonArray.class);

		for (JsonElement contributorElement : contributorsArray.asList()) {
			if (!contributorElement.isJsonObject())
				continue;

			JsonObject contributorObject = contributorElement.getAsJsonObject();

			if (!contributorObject.has("login"))
				continue;

			contributors.add(contributorObject.get("login").getAsString());
		}

		return contributors;
	}

	private void downloadRepositoryAsZip(File outputfile) {

		String surl = String.format("https://api.github.com/repos/%s/%s/zipball/%s", repositoryOrganization,
				repositoryName, latestCommit);

		try {
			URL url = new URL(surl);

			HttpURLConnection con = (HttpURLConnection) url.openConnection();
			con.setRequestMethod("GET");

			con.setRequestProperty("Authorization", "Bearer " + TOKEN);
			con.setRequestProperty("Content-Type", "application/zip");

			logger.info("Downloading " + repositoryOrganization + "/" + repositoryName + " to a " + outputfile);
			long timestamp = System.currentTimeMillis();

			InputStream stream = con.getInputStream();

			Files.copy(stream, outputfile.toPath(), StandardCopyOption.REPLACE_EXISTING);

			logger.info("Downloaded in " + (System.currentTimeMillis() - timestamp) + "ms");

		} catch (IOException e) {
			throw new RuntimeException(e);
		}

	}

	private class CommitInfo {
		private String sha;

		public String getSha() {
			return sha;
		}
	}

}
