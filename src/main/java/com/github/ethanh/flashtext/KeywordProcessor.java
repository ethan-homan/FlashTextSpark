package com.github.ethanh.flashtext;

import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collector;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.io.Serializable;

/*
 * FlashTextJava - An idiomatic Java port of the Python library FlashText by Vikash Singh
 * Original Python source can be found at https://github.com/vi3k6i5/flashtext
 * Based on the Aho-Corasick algorithm (https://en.wikipedia.org/wiki/Aho%E2%80%93Corasick_algorithm)
 * Java Version written by Jason Sperske
 */
class KeywordProcessor implements Serializable {
	// immutable properties once KeywordProcessor is instantiated
	private final boolean CASE_SENSITIVE;

	// dynamic properties while KeywordProcessor is being built up
	private int _terms_in_trie = 0;
	private KeywordTrieNode rootNode;

	public KeywordProcessor() {
		this(false);
	}

	public KeywordProcessor(boolean case_sensitive) {
		this.CASE_SENSITIVE = case_sensitive;
		this.rootNode = new KeywordTrieNode();
	}

	public int length() {
		return this._terms_in_trie;
	}

	public boolean contains(String word) {
		KeywordTrieNode current_keyword_trie_node = this.rootNode;
		int chars_traveled = 0;

		if (!this.CASE_SENSITIVE) {
			word = word.toLowerCase();
		}
		for (Character c : word.toCharArray()) {
			if (current_keyword_trie_node.contains(c)) {
				current_keyword_trie_node = current_keyword_trie_node.children.get(c);
				chars_traveled += 1;
			} else {
				return false;
			}
		}

		return chars_traveled == word.length() && current_keyword_trie_node.contains(word);
	}

	public String get(String word) {
		KeywordTrieNode current_keyword_trie_node = this.rootNode;
		int chars_traveled = 0;

		if (!this.CASE_SENSITIVE) {
			word = word.toLowerCase();
		}
		for(Character c : word.toCharArray()) {
			if (current_keyword_trie_node.contains(c)) {
				current_keyword_trie_node = current_keyword_trie_node.children.get(c);
				chars_traveled += 1;
			} else {
				return null;
			}
		}

		if (chars_traveled == word.length()) {
			return current_keyword_trie_node.get();
		} else {
			return null;
		}
	}

	public void addKeyword(String word) {
		// Clean Name is set to word when not defined
		addKeyword(word, word);
	}

	public void addKeyword(String word, String clean_name) {
		if (!this.CASE_SENSITIVE) {
			word = word.toLowerCase();
		}
		LinkedList<Character> characters = word.chars().mapToObj(c -> (char)c).collect(Collectors.toCollection(LinkedList::new));

		this.rootNode.add(characters, word, clean_name);
		this._terms_in_trie += 1;
	}

	public HashMap<String, Integer> extractKeywords(String sentence) {
		return extractKeywords(sentence.chars().mapToObj(c -> (char) c));
	}
	public HashMap<String, Integer> extractKeywords(Stream<Character> chars) {
		return chars.collect(new Extractor(this.rootNode, this.CASE_SENSITIVE));
	}

	class Extractor implements Collector<Character, HashMap<String, Integer>, HashMap<String, Integer>> {
		private KeywordTrieNode currentNode;
		private final KeywordTrieNode rootNode;
		private final boolean CASE_SENSITIVE;
		private HashMap<String, Integer> keywords;

		public Extractor(KeywordTrieNode rootNode, boolean caseSensitive) {
			this.rootNode = rootNode;
			this.currentNode = rootNode;
			this.CASE_SENSITIVE = caseSensitive;
			this.keywords = new HashMap<>();
		}

		@Override
		public BiConsumer<HashMap<String, Integer>, Character> accumulator() {
			return (keywords, c) -> {
				if (!this.CASE_SENSITIVE) {
					c = Character.toLowerCase(c);
				}
				KeywordTrieNode node = currentNode.get(c);
				if (node == null) {
					currentNode = this.rootNode;
				} else {
					currentNode = node;
					String keyword = currentNode.get();
					if (keyword != null) {
						int count = keywords.containsKey(keyword) ? keywords.get(keyword) : 0;
						keywords.put(keyword, count + 1);
					}
				}
			};
		}

		@Override
		public Set<Characteristics> characteristics() {
			return Collections.emptySet();
		}

		@Override
		public BinaryOperator<HashMap<String, Integer>> combiner() {
			return (a, b) -> a;
		}

		@Override
		public Function<HashMap<String, Integer>, HashMap<String, Integer>> finisher() {
			return (keywords) -> keywords;
		}

		@Override
		public Supplier<HashMap<String, Integer>> supplier() {
			return () -> this.keywords;
		}

	}

	public String replace(String sentence) {
		return replace(sentence.chars().mapToObj(c -> (char) c));
	}
	private String replace(Stream<Character> chars) {
		return chars.collect(new Replacer(this.rootNode, this.CASE_SENSITIVE));
	}

	// Design adapted from https://codereview.stackexchange.com/a/199677/9162
	class Replacer implements Collector<Character, StringBuffer, String> {
		private StringBuffer out;
		private StringBuffer buffer;
		private KeywordTrieNode currentNode;
		private final KeywordTrieNode rootNode;
		private final boolean CASE_SENSITIVE;

		public Replacer(KeywordTrieNode rootNode, boolean caseSensitive) {
			this.rootNode = rootNode;
			this.currentNode = rootNode;
			this.out = new StringBuffer();
			this.buffer = new StringBuffer();
			this.CASE_SENSITIVE = caseSensitive;
		}

		@Override
		public BiConsumer<StringBuffer, Character> accumulator() {
			return (out, c) -> {
				char match_c = c;
				if (!this.CASE_SENSITIVE) {
					match_c = Character.toLowerCase(c);
				}
				KeywordTrieNode node = currentNode.get(match_c);
				if (node != null) {
					buffer.append(c);
					currentNode = node;
				} else {
					String keyword = currentNode.get();
					if (keyword != null) {
						out.append(keyword);
					} else {
						out.append(buffer);
					}
					out.append(c);
					buffer = new StringBuffer();
					currentNode = this.rootNode;
				}
			};
		}

		@Override
		public Set<Characteristics> characteristics() {
			return Collections.emptySet();
		}

		@Override
		public BinaryOperator<StringBuffer> combiner() {
			return (a, b) -> a;
		}

		@Override
		public Function<StringBuffer, String> finisher() {
			return (out) -> {
				String keyword = currentNode.get();
				if (keyword == null) {
					out.append(buffer);
				} else {
					out.append(keyword);
				}
				return out.toString();
			};
		}

		@Override
		public Supplier<StringBuffer> supplier() {
			return () -> this.out;
		}
	}

	public String toString() {
		return this.rootNode.toString();
	}
}
