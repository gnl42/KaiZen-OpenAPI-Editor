package com.reprezen.swagedit.core.json;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Map;
import java.util.Set;

import org.apache.commons.io.IOUtils;

import com.fasterxml.jackson.core.JsonPointer;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Charsets;
import com.google.common.collect.Maps;
import com.reprezen.jsonoverlay.JsonLoader;
import com.reprezen.jsonoverlay.ResolutionBaseRegistry;
import com.reprezen.kaizen.oasparser.OpenApi3Parser;
import com.reprezen.kaizen.oasparser.model3.OpenApi3;

public class OpenApi3LocationParser extends OpenApi3Parser {

    private final static LineRecorderYamlFactory factory = new LineRecorderYamlFactory();
    private final static ObjectMapper mapper = new ObjectMapper(factory);

    private final Map<JsonPointer, JsonRegion> regions = Maps.newHashMap();
    private final Map<JsonPointer, Set<JsonPointer>> paths = Maps.newHashMap();
    private final Map<String, JsonNode> cache = Maps.newHashMap();

    private class JsonLocationLoader extends JsonLoader {

        @Override
        public JsonNode load(URL url) throws IOException {
            String urlString = url.toString();
            if (cache.containsKey(urlString)) {
                return cache.get(urlString);
            }
            try (InputStream in = url.openStream()) {
                String json = IOUtils.toString(in, Charsets.UTF_8);
                return loadString(url, json);
            }
        }

        @Override
        public JsonNode loadString(URL url, String json) throws IOException, JsonProcessingException {
            LineRecorderYamlParser parser = (LineRecorderYamlParser) factory.createParser(json);
            JsonNode tree = null;
            try {
                tree = mapper.reader().readTree(parser);
            } catch (Exception e) {
                throw new IOException(e);
            }

            regions.putAll(parser.getLines());
            paths.putAll(parser.getPaths());

            if (url != null) {
                cache.put(url.toString(), tree);
            }
            return tree;
        }
    }

    @Override
    public OpenApi3 parse(String spec, URL resolutionBase, boolean validate) {
        try {
            JsonLoader jsonLoader = new JsonLocationLoader();
            JsonNode tree = jsonLoader.loadString(resolutionBase, spec);
            ResolutionBaseRegistry resolutionBaseRegistry = new ResolutionBaseRegistry(jsonLoader);
            resolutionBaseRegistry.register(resolutionBase.toString(), tree);

            return (OpenApi3) parse(resolutionBase, validate, resolutionBaseRegistry);
        } catch (Exception e) {
            throw new SwaggerParserException("Failed to parse spec as JSON or YAML", e);
        }
    }

    public Map<JsonPointer, JsonRegion> geRegions() {
        return regions;
    }

    public Map<JsonPointer, Set<JsonPointer>> getPaths() {
        return paths;
    }

    public JsonNode getJSON(URL url) {
        if (url != null) {
            return cache.get(url.toString());
        } else {
            return null;
        }
    }
}
