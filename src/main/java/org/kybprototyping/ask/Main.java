package org.kybprototyping.ask;

import java.io.File;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.Random;
import java.util.Scanner;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.TypeFactory;

@SuppressWarnings({"java:S3457", "java:S106"})
class Main {

  private static final Properties PROPERTIES = new Properties();
  static {
    try {
      try (InputStream propStream = Main.class.getResourceAsStream("/application.properties")) {
        PROPERTIES.load(propStream);
      }
    } catch (Exception e) {
      System.err.printf("Properties couldn't be loaded: %s\n", e.getMessage());
    }
  }
  private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
  private static final Random RANDOM = new SecureRandom();
  private static final Scanner SCANNER_STD_IN = new Scanner(System.in);

  public static void main(String[] args) {
    List<WordTranslation> translations = extractTranslations();

    while (true) {
      WordTranslation wordToAsk = translations.get(RANDOM.nextInt(translations.size()));

      System.out.printf("\nTell me what '%s' means ?\n", wordToAsk.getWord());
      String givenAnswer = SCANNER_STD_IN.nextLine();
      if ("exit".equals(givenAnswer)) {
        System.out.println("See you next time!");
        break;
      } else if (wordToAsk.getTranslations().stream()
          .anyMatch(t -> t.toLowerCase().equals(givenAnswer.toLowerCase().strip()))) {
        System.out.printf("\n%sYou're correct!%s\n", AnsiColourCodes.GREEN, AnsiColourCodes.RESET);
        System.out.printf("\nExamples:\n");
        wordToAsk.getExamples().entrySet()
            .forEach(e -> System.out.printf("\n- %s\n  %s\n", e.getKey(), e.getValue()));
        System.out.print("\n");
      } else {
        System.out.printf("\n%sTry again!%s\n", AnsiColourCodes.RED, AnsiColourCodes.RESET);
      }
    }
  }

  private static List<WordTranslation> extractTranslations() {
    String sourceJsonPath = PROPERTIES.getProperty("source_json_path");
    if (sourceJsonPath == null) {
      throw new AskException("source_json_path property couldn't extracted!");
    }

    File sourceJson = extractSourceJson(sourceJsonPath);

    return extractTranslationsFromSourceJson(sourceJson);
  }

  private static File extractSourceJson(String sourceJsonPathStr) {
    Path sourceJsonPath = toPath(sourceJsonPathStr);

    if (!Files.exists(sourceJsonPath)) {
      throw new AskException("source.json couldn't be found in: " + sourceJsonPath);
    }
    return sourceJsonPath.toFile();
  }

  private static Path toPath(String sourceJsonPathStr) {
    try {
      return Paths.get(sourceJsonPathStr);
    } catch (InvalidPathException e) {
      throw new AskException("source_json_path couldn't extracted!", e);
    }
  }

  private static List<WordTranslation> extractTranslationsFromSourceJson(File sourceJson) {
    try {
      return OBJECT_MAPPER.readValue(sourceJson, TypeFactory.defaultInstance()
          .constructCollectionType(ArrayList.class, WordTranslation.class));
    } catch (Exception e) {
      throw new AskException(
          "Translations couldn't extracted from source.json: " + sourceJson.getAbsolutePath(), e);
    }
  }

}
