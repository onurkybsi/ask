package org.kybprototyping.ask;

import java.util.List;
import java.util.Map;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

final class WordTranslation {

  private final String word;
  private final List<String> translations;
  private final Map<String, String> examples;

  @JsonCreator
  WordTranslation(@JsonProperty("word") String word,
      @JsonProperty("translations") List<String> translations,
      @JsonProperty("examples") Map<String, String> examples) {
    this.word = word;
    this.translations = translations;
    this.examples = examples;
  }

  String getWord() {
    return word;
  }

  List<String> getTranslations() {
    return translations;
  }

  Map<String, String> getExamples() {
    return examples;
  }

}
