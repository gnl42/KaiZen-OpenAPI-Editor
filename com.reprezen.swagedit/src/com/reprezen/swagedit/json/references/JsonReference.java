package com.reprezen.swagedit.json.references;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Paths;

import com.fasterxml.jackson.core.JsonPointer;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;

public class JsonReference {

	private final URI uri;
	private final JsonPointer pointer;
	private final Object source;
	private final boolean absolute;
	private final boolean local;

	private JsonNode resolved;

	JsonReference(URI uri, JsonPointer pointer, boolean absolute, boolean local, Object source) {
		this.uri = uri;
		this.pointer = pointer;
		this.absolute = absolute;
		this.local = local;
		this.source = source;
	}

	public boolean isValid(URI baseURI) {
		if (uri == null) {
			return false;
		}

		JsonNode resolved = resolve(baseURI);
		return resolved != null && !resolved.isMissingNode();
	}

	public JsonNode resolve(URI baseURI) {
		URI resolvedURI = baseURI != null ? baseURI.resolve(uri) : uri;
		String filePath;
		try {
			filePath = resolvedURI.toURL().getFile();
		} catch (MalformedURLException e) {
			return null;
		}

		boolean exists = Files.exists(Paths.get(filePath));
		if (exists && resolved == null) {
			ObjectMapper mapper = getMapper();
			JsonNode doc;
			try {
				doc = mapper.readTree(resolvedURI.toURL());
			} catch (IOException e) {
				e.printStackTrace();
				return null;
			}

			resolved = doc.at(pointer);
		}

		return resolved;
	}

	public JsonPointer getPointer() {
		return pointer;
	}

	public URI getUri() {
		return uri;
	}

	public boolean isLocal() {
		return local;
	}

	public boolean isAbsolute() {
		return absolute;
	}

	public Object getSource() {
		return source;
	}

	protected ObjectMapper getMapper() {
		return new ObjectMapper(new YAMLFactory());
	}

}
