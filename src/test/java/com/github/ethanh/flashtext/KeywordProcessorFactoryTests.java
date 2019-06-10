package com.github.ethanh.flashtext;

import java.io.IOException;
import java.util.HashMap;
import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;

class KeywordProcessorFactoryTests {
	@Test
	void shouldSupportFileFormatOne() throws IOException {
		KeywordProcessor processor = KeywordProcessorFactory.fromFlashTextFile("src/test/resources/keywords_format_one.txt");
		HashMap<String, Integer> keywords = processor.extractKeywords("I know java_2e and product management techniques");

		assertTrue(keywords.keySet().size() == 2);
		assertTrue(keywords.containsKey("java"));
		assertTrue(keywords.containsKey("product management"));
	}

	@Test
	void shouldSupportFileFormatTwo() throws IOException {
		KeywordProcessor processor = KeywordProcessorFactory.fromFlashTextFile("src/test/resources/keywords_format_two.txt");
		HashMap<String, Integer> keywords = processor.extractKeywords("I know java and product management");

		assertTrue(keywords.keySet().size() == 2);
		assertTrue(keywords.containsKey("java"));
		assertTrue(keywords.containsKey("product management"));
	}
}
