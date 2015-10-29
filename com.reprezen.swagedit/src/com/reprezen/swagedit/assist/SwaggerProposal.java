package com.reprezen.swagedit.assist;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.eclipse.jface.text.contentassist.CompletionProposal;
import org.eclipse.jface.text.contentassist.ICompletionProposal;

import com.fasterxml.jackson.databind.JsonNode;

public interface SwaggerProposal {

	List<ICompletionProposal> asCompletionProposal(int offset);

	public static class Builder {

		private final JsonNode schema;

		public Builder(JsonNode schema) {
			this.schema = schema;
		}

		private SwaggerProposal getType(JsonNode node) {
			if (node == null) {
				return null;
			}

			if (node.has("enum")) {
				return SwaggerProposal.EnumProposal.create(node, this); 
			} else if (node.has("pattern")) {
				return SwaggerProposal.RegExProposal.create(node, this);
			} else if (node.has("items")) {
				return SwaggerProposal.ArrayProposal.create(node, this);
			} else if (node.has("$ref")) {
				return getType(find(node.get("$ref").asText()));
			} else if (node.has("type")) {
				String type = node.get("type").asText();
				if ("object".equals(type)) {
					return SwaggerProposal.ObjectProposal.create(node, this);
				} else if ("string".equals(type)) {
					return SwaggerProposal.StringProposal.create(node, this);
				}
			}
				
			return null;
		}

		private JsonNode find(String ref) {
			final String[] keys = ref.substring(2).split("/");
			JsonNode found = schema;
			for (String key: keys) {
				found = found.get(key);
			}

			return found;
		}

		public SwaggerProposal build() {
			return ObjectProposal.create(schema, this);
		}
	}

	public class StringProposal implements SwaggerProposal {

		StringProposal() {}

		@Override
		public List<ICompletionProposal> asCompletionProposal(int offset) {
			final String replacement = "''";
			final List<ICompletionProposal> result = new ArrayList<>();					
			result.add(new CompletionProposal(replacement, offset, 0, 1));
			return result;
		}

		public static SwaggerProposal create(JsonNode node, Builder builder) {
			return new StringProposal();
		}
		
	}
	
	public class EnumProposal implements SwaggerProposal {

		public final String[] literals;

		EnumProposal(String... literals) {
			this.literals = Arrays.copyOf(literals, literals.length);
		}

		public static EnumProposal create(JsonNode definition, Builder builder) {
			final JsonNode enums = definition.get("enum");
			final String[] literals = new String[enums.size()];
			for (int i=0; i < enums.size(); i++) {
				literals[i] = enums.get(i).asText();
			}

			return new EnumProposal(literals);
		}
		
		@Override
		public String toString() {
			String res = "[";
			for (String literal: literals) {
				res += " " + literal; 
			}
			res += " ]";
			return res;
		}

		@Override
		public List<ICompletionProposal> asCompletionProposal(int offset) {
			final List<ICompletionProposal> result = new LinkedList<>();
			for (String literal: literals) {
				final String replacement = "'" + literal + "'";
				result.add(new CompletionProposal(replacement, offset, 0, replacement.length()));
			}
			return result;
		}
	}

	public class RegExProposal implements SwaggerProposal {
		public final String pattern;

		RegExProposal(String pattern) {
			this.pattern = pattern;
		}

		public static RegExProposal create(JsonNode definition, Builder builder) {
			final String pattern = definition.get("pattern").asText();

			return new RegExProposal(pattern);
		}

		@Override
		public String toString() {
			return "{ pattern: " + pattern + " }";
		}

		@Override
		public List<ICompletionProposal> asCompletionProposal(int offset) {
			List<ICompletionProposal> result = new LinkedList<>();
			return result;
		}
	}

	public class ArrayProposal implements SwaggerProposal {

		private List<SwaggerProposal> items = new ArrayList<>();

		public static ArrayProposal create(JsonNode definition, Builder builder) {
			final JsonNode items = definition.get("items");
			final ArrayProposal proposal = new ArrayProposal();

			if (items.isObject()) {
				final SwaggerProposal type = builder.getType(items);
				if (type != null) {
					proposal.items.add(type);
				}
			} else if (items.isArray()) {
				for (JsonNode item: items) {
					final SwaggerProposal type = builder.getType(item);
					if (type != null) {
						proposal.items.add(type);
					}	
				}
			}

			return proposal;
		}
		
		public List<SwaggerProposal> getItems() {
			return items;
		}
		
		@Override
		public String toString() {
			return "[]";
		}

		@Override
		public List<ICompletionProposal> asCompletionProposal(int offset) {
			final List<ICompletionProposal> result = new LinkedList<>();
			for (SwaggerProposal proposal: getItems()) {				
				result.addAll(proposal.asCompletionProposal(offset));
			}
			return result;
		}

	}

	public class ObjectProposal implements SwaggerProposal {

		private final Map<String, SwaggerProposal> properties = new HashMap<>();

		void addProperty(String key, SwaggerProposal value) {
			properties.put(key, value);
		}

		public Map<String, SwaggerProposal> getProperties() {
			return properties;
		}

		static ObjectProposal create(JsonNode definition, Builder builder) {
			final ObjectProposal proposal = new ObjectProposal();

			if (definition.has("properties")) {
				JsonNode properties = definition.get("properties");

				for (Iterator<String> it = properties.fieldNames(); it.hasNext();) {
					String key = it.next();
					SwaggerProposal value = builder.getType(properties.get(key));
					if (value != null) {
						proposal.addProperty(key, value);
					}
				}
			}

			return proposal;
		}

		@Override
		public String toString() {
			String res = "{ ";
			for (String key: properties.keySet()) {
				res += key + ":" + properties.get(key); 
			}
			res += " }";
			return res;
		}

		@Override
		public List<ICompletionProposal> asCompletionProposal(int offset) {
			List<ICompletionProposal> result = new LinkedList<>();
			for (String property: properties.keySet()) {
				result.add(new CompletionProposal(property, offset, 0, property.length()));
			}
			return result;
		}
	}

}