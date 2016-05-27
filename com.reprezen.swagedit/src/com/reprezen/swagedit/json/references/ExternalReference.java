package com.reprezen.swagedit.json.references;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.eclipse.core.runtime.IPath;

import com.fasterxml.jackson.core.JsonPointer;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.MissingNode;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;

public class ExternalReference extends JsonReference {

	public final IPath path;

	ExternalReference(IPath path, JsonPointer pointer) {
		super(pointer);
		this.path = path;
	}

	@Override
	public boolean isValid() {
		JsonNode node = get();
		return node != null && !(node instanceof MissingNode);
	}

	public InputStream getContent() {
		Path pp = Paths.get(path.toFile().toURI());
		try {
			return pp.toFile().exists() ? Files.newInputStream(pp) : null;
		} catch (IOException e) {
			return null;
		}
	}

	@Override
	public JsonNode get() {
		ObjectMapper mapper = new ObjectMapper(new YAMLFactory());

		try (InputStream stream = getContent()) {
			return mapper.readTree(stream).at(pointer);
		} catch (Exception e) {
			return null;
		}
	}
}