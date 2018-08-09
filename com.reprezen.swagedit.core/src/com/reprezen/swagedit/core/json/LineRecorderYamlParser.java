package com.reprezen.swagedit.core.json;

import java.io.IOException;
import java.io.Reader;
import java.util.Map;
import java.util.Set;

import org.eclipse.core.resources.IMarker;

import com.fasterxml.jackson.core.JsonLocation;
import com.fasterxml.jackson.core.JsonPointer;
import com.fasterxml.jackson.core.JsonStreamContext;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.core.ObjectCodec;
import com.fasterxml.jackson.core.io.IOContext;
import com.fasterxml.jackson.core.util.BufferRecycler;
import com.fasterxml.jackson.dataformat.yaml.YAMLParser;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.reprezen.swagedit.core.json.JsonRegion.Location;
import com.reprezen.swagedit.core.validation.Messages;
import com.reprezen.swagedit.core.validation.SwaggerError;

public class LineRecorderYamlParser extends YAMLParser {
    private final Map<JsonPointer, JsonRegion> ranges = Maps.newHashMap();
    private final Map<JsonPointer, Set<JsonPointer>> paths = Maps.newHashMap();
    private final Set<SwaggerError> errors = Sets.newHashSet();
    private final Set<JsonPointer> duplicateKeys = Sets.newHashSet();

    private final Map<JsonPointer, Integer> arrayContent = Maps.newHashMap();

    private JsonPointer ptr = JsonPointer.compile("");
    private boolean seenRoot = false;
    private boolean strictMode;

    public LineRecorderYamlParser(IOContext ctxt, BufferRecycler br, int parserFeatures, int formatFeatures,
            ObjectCodec codec, Reader reader) {
        super(ctxt, br, parserFeatures, formatFeatures, codec, reader);
    }

    public Map<JsonPointer, JsonRegion> getLines() {
        return ranges;
    }

    public Map<JsonPointer, Set<JsonPointer>> getPaths() {
        return paths;
    }

    public Set<SwaggerError> getErrors() {
        return errors;
    }

    @Override
    public JsonToken nextToken() throws IOException {
        JsonToken token = null;
        try {
            token = super.nextToken();
        } catch (Exception e) {
            e.printStackTrace();
            // if (strictMode) {
            // throw e;
            // }
        }

        if (token != null) {
            processLineEntry(token, getCurrentLocation(), getParsingContext());
        }

        return token;
    }

    private void processLineEntry(JsonToken token, JsonLocation location, JsonStreamContext context) {
        /*
         * Root needs to be handled specially.
         */
        if (!seenRoot) {
            JsonRegion range = getOrCreateRange(ptr);
            range.setContentLocation(new Location( //
                    location.getLineNr(), //
                    location.getColumnNr(), //
                    location.getLineNr(), //
                    location.getColumnNr()));

            ranges.put(ptr, range);
            paths.put(ptr, Sets.<JsonPointer> newHashSet());

            seenRoot = true;
            return;
        }

        /*
         * We get that if JSON Pointer "" points to a container... We need to skip that
         */
        if (context.inRoot()) {
            JsonRegion range = ranges.get(ptr);
            Location previousLocation = range.getContentLocation();
            range.setContentLocation(new Location( //
                    previousLocation.startLine, //
                    previousLocation.startColumn, //
                    location.getLineNr(), //
                    location.getColumnNr()));

            return;
        }

        /*
         * If the end of a container, "pop" one level
         */
        if (token == JsonToken.END_OBJECT || token == JsonToken.END_ARRAY) {
            JsonRegion range = getOrCreateRange(ptr);
            Location previousLocation = range.getContentLocation();
            range.setContentLocation(new Location( //
                    previousLocation.startLine, //
                    previousLocation.startColumn, //
                    location.getLineNr(), //
                    location.getColumnNr()));

            ptr = ptr.head();

            return;
        }

        /*
         * This is not addressable...
         */
        if (token == JsonToken.FIELD_NAME) {
            Set<JsonPointer> list = paths.get(ptr.head());
            if (list == null) {
                list = Sets.newHashSet();
            }
            list.add(ptr);

            JsonPointer fieldPointer = append(ptr, context);

            // Add error if object already contains field with same name
            if (ranges.containsKey(fieldPointer)) {
                if (!isInDuplicateParent(fieldPointer)) {
                    duplicateKeys.add(fieldPointer);

                    errors.add(new SwaggerError(location.getLineNr(), IMarker.SEVERITY_WARNING,
                            String.format(Messages.error_duplicate_keys, context.getCurrentName())));
                }
            }

            JsonRegion range = getOrCreateRange(fieldPointer);
            range.setFieldLocation(new Location( //
                    location.getLineNr(), //
                    location.getColumnNr(), //
                    location.getLineNr(), //
                    location.getColumnNr()));

            paths.put(ptr.head(), list);
            return;
        }

        final JsonStreamContext parent = context.getParent();

        /*
         * But this is; however we need to know what the parent is to do things correctly, delegate to another method
         */
        if (token == JsonToken.START_ARRAY || token == JsonToken.START_OBJECT) {
            startContainer(parent, location, token == JsonToken.START_ARRAY);
            return;
        }

        /*
         * OK, "normal" entry, build the pointer
         */
        final JsonPointer entryPointer = append(ptr, context);

        JsonRegion range = getOrCreateRange(entryPointer);
        range.setContentLocation(new Location( //
                location.getLineNr(), //
                location.getColumnNr(), //
                location.getLineNr(), //
                location.getColumnNr()));

        JsonPointer top = entryPointer.head();
        Set<JsonPointer> list = paths.get(top);
        if (list == null) {
            list = Sets.newHashSet();
        }
        list.add(entryPointer);
        paths.put(top, list);
    }

    protected JsonRegion getOrCreateRange(JsonPointer pointer) {
        JsonRegion range = ranges.get(pointer);
        if (range == null) {
            ranges.put(pointer, range = new JsonRegion(pointer));
        }
        return range;
    }

    private void startContainer(final JsonStreamContext parent, JsonLocation location, boolean isArray) {
        ptr = append(ptr, parent);

        if (isArray) {
            Integer count = arrayContent.get(ptr);
            if (count == null) {
                arrayContent.put(ptr, 0);
            }
        }

        JsonRegion range = getOrCreateRange(ptr);
        range.setContentLocation(new Location( //
                location.getLineNr(), //
                location.getColumnNr(), //
                location.getLineNr(), //
                location.getColumnNr()));

        Set<JsonPointer> list = paths.get(ptr.head());
        if (list == null) {
            list = Sets.newHashSet();
        }
        list.add(ptr);
        paths.put(ptr.head(), list);
    }

    private JsonPointer append(JsonPointer ptr, JsonStreamContext context) {
        JsonPointer result = ptr;

        if (context.inArray()) {
            // context.getCurrentIndex() does not return anything other than 0
            // so we keep track of number of elements in an array with the arrayContent map by
            // incrementing it each time we append something to the array pointer.

            int count = arrayContent.get(ptr);
            result = ptr.append(JsonPointer.compile("/" + count));
            arrayContent.put(ptr, ++count);
        } else if (context.inObject()) {
            result = ptr.append(JsonPointer.compile("/" + context.getCurrentName().replaceAll("/", "~1")));
        }

        return result;
    }

    public void setStrict(boolean strict) {
        this.strictMode = strict;
    }

    private boolean isInDuplicateParent(JsonPointer ptr) {
        boolean result = false;
        JsonPointer parent = ptr;

        while ((parent = parent.head()) != null && result == false) {
            if (duplicateKeys.contains(parent)) {
                result = true;
            }
        }
        return result;
    }

}
