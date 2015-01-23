package eu.crushedpixel.replaymod.api.client;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.List;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.FileEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import eu.crushedpixel.replaymod.api.client.holders.ApiError;
import eu.crushedpixel.replaymod.api.client.holders.AuthKey;
import eu.crushedpixel.replaymod.api.client.holders.Category;
import eu.crushedpixel.replaymod.api.client.holders.FileInfo;
import eu.crushedpixel.replaymod.api.client.holders.Success;
import eu.crushedpixel.replaymod.api.client.holders.UserFiles;

public class ApiClient {

	private static Gson gson = new Gson();
	private static JsonParser jsonParser = new JsonParser();

	public AuthKey getLogin(String username, String password) throws IOException, ApiException {
		QueryBuilder builder = new QueryBuilder(ApiMethods.login);
		builder.put("user", username);
		builder.put("pw", password);
		AuthKey auth = invokeAndReturn(builder, AuthKey.class);
		return auth;
	}

	public UserFiles getUserFiles(String auth, String user) throws IOException, ApiException {
		QueryBuilder builder = new QueryBuilder(ApiMethods.replay_files);
		builder.put("auth", auth);
		builder.put("user", user);
		UserFiles files = invokeAndReturn(builder, UserFiles.class);
		return files;
	}

	public FileInfo[] getFileInfo(String auth, List<Integer> ids) throws IOException, ApiException {
		QueryBuilder builder = new QueryBuilder(ApiMethods.replay_files);
		builder.put("auth", auth);
		builder.put("ids", buildListString(ids));
		FileInfo[] info = invokeAndReturn(builder, FileInfo[].class); //TODO: Test if that works
		return info;
	}

	public void uploadFile(String auth, File file, Category category) throws IOException, ApiException {
		QueryBuilder builder = new QueryBuilder(ApiMethods.upload_file);
		builder.put("auth", auth);
		builder.put("category", category.getId());
		String url = builder.toString();
		
		HttpClient client = new DefaultHttpClient();
		HttpPost post = new HttpPost(url);
		
		((InputStream)client).close();
		
		FileEntity entity = new FileEntity(file);
		post.setEntity(entity);
		HttpResponse response = client.execute(post);
		
		JsonElement element = jsonParser.parse(EntityUtils.toString(response.getEntity()));
		try {
			ApiError err = gson.fromJson(element, ApiError.class);
			if(err.getDesc() != null) {
				throw new ApiException(err);
			}
		} catch(Exception e) {}
	}

	public void downloadFile(String auth, int file, File target) throws IOException {
		QueryBuilder builder = new QueryBuilder(ApiMethods.download_file);
		builder.put("auth", auth);
		builder.put("id", file);
		String url = builder.toString();
		URL website = new URL(url);
		InputStream is = website.openStream();
		try { //If valid json, an error occured
			jsonParser.parse(StreamTools.readStreamtoString(is));
		} catch(Exception e) {
			Files.copy(is, target.toPath(), StandardCopyOption.REPLACE_EXISTING);
		}
	}

	public void rateFile(String auth, int file, boolean like) throws IOException, ApiException {
		QueryBuilder builder = new QueryBuilder(ApiMethods.rate_file);
		builder.put("auth", auth);
		builder.put("id", file);
		builder.put("like", like);
		invokeAndReturn(builder, Success.class);
	}

	public void removeFile(String auth, int file) throws IOException, ApiException {
		QueryBuilder builder = new QueryBuilder(ApiMethods.remove_file);
		builder.put("auth", auth);
		builder.put("id", file);
		invokeAndReturn(builder, Success.class);
	}

	private <T> T invokeAndReturn(QueryBuilder builder,Class<T> classOfT) throws IOException, ApiException {
		JsonObject arr = GsonApiClient.invoke(builder);
		return gson.fromJson(arr, classOfT);
	}

	@SuppressWarnings("rawtypes")
	private String buildListString(List idList) {
		if(idList == null) return null;

		String ids = "";
		Integer x=0;
		for(Object id : idList) {
			x++;
			ids += id.toString();
			if(x != idList.size()) {
				ids += ",";
			}
		}

		return ids;
	}
}