package com.github.ethanh.flashtext;

import static org.junit.jupiter.api.Assertions.*;

import java.util.HashMap;
import org.junit.jupiter.api.Test;

class KeywordReplacerTests {
	@Test
	void shouldFindKeywordAtTheEndOfTheSentence() {
		KeywordProcessor processor = new KeywordProcessor();
		processor.addKeyword("Python", "python");

		HashMap<String, Integer> keywords = processor.extractKeywords("I like python");
		assertTrue(keywords.size() == 1);
		assertTrue(keywords.containsKey("python"));
	}
	@Test
	void shouldSkipIncompleteKeywordAtTheEndOfTheSentence() {
		KeywordProcessor processor = new KeywordProcessor();
		processor.addKeyword("Pythonizer", "pythonizer");

		HashMap<String, Integer> keywords = processor.extractKeywords("I like python");
		assertTrue(keywords.size() == 0);
	}
	@Test
	void shouldFindKeywordAtTheBeginningOfTheSentence() {
		KeywordProcessor processor = new KeywordProcessor();
		processor.addKeyword("Python", "python");

		HashMap<String, Integer> keywords = processor.extractKeywords("python I like");
		assertTrue(keywords.size() == 1);
		assertTrue(keywords.containsKey("python"));
		assertTrue(keywords.get("python") == 1);
	}
	@Test
	void shouldFindKeywordBeforeTheEndOfTheSentence() {
		KeywordProcessor processor = new KeywordProcessor();
		processor.addKeyword("Python", "python");

		HashMap<String, Integer> keywords = processor.extractKeywords("I like python also");
		assertTrue(keywords.size() == 1);
		assertTrue(keywords.containsKey("python"));
	}
	@Test
	void shouldFindMultipleKeywordsInTheEndOfTheSentence() {
		KeywordProcessor processor = new KeywordProcessor();

		processor.addKeyword("Python", "python");
		processor.addKeyword("Java", "java");

		HashMap<String, Integer> keywords = processor.extractKeywords("I like python java");
		assertTrue(keywords.size() == 2);
		assertTrue(keywords.containsKey("python"));
		assertTrue(keywords.containsKey("java"));
	}

	@Test
	void shouldFindTheSameKeywordAsManyTimesAsItAppears() {
		KeywordProcessor processor = new KeywordProcessor();

		processor.addKeyword("Python", "python");
		processor.addKeyword("Java", "java");

		HashMap<String, Integer> keywords = processor.extractKeywords("I like python java python java python java");
		assertTrue(keywords.keySet().size() == 2);
		assertTrue(keywords.containsKey("python"));
		assertTrue(keywords.containsKey("java"));
		assertTrue(keywords.get("java") == 3);
		assertTrue(keywords.get("python") == 3);
	}
}
