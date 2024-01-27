package org.kybprototyping.ask;

import java.io.File;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Paths;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.Properties;
import java.util.Random;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.TypeFactory;

@SuppressWarnings({"java:S3457", "java:S2629", "java:S106"})
class Main {

  private static final Logger LOGGER = Logger.getLogger(Main.class.getCanonicalName());
  private static final Properties PROPERTIES = new Properties();
  static {
    try {
      try (InputStream propStream = Main.class.getResourceAsStream("/application.properties")) {
        PROPERTIES.load(propStream);
      }
    } catch (Exception e) {
      LOGGER.log(Level.SEVERE, "Properties couldn't be loaded!", e);
    }
  }
  private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
  private static final Random RANDOM = new SecureRandom();
  private static final Scanner SCANNER_STD_IN = new Scanner(System.in);

  public static void main(String[] args) {
    LOGGER.log(Level.FINE, "Starting...");

    String sourceJsonPathProp = PROPERTIES.getProperty("source_json_path");
    LOGGER.log(Level.FINE, "source.json path extracted: " + sourceJsonPathProp);
    if (sourceJsonPathProp == null) {
      throw new AskException("source_json_path property couldn't extracted!");
    }
    File sourceJson = extractSourceJson(sourceJsonPathProp);
    List<WordTranslation> translations = extractTranslations(sourceJson);
    LOGGER.log(Level.FINER, "Extracted translations: " + Arrays.toString(translations.toArray()));

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

  private static File extractSourceJson(String sourceJsonPathProp) {
    return Optional.of(sourceJsonPathProp).map(p -> {
      try {
        return Paths.get(p);
      } catch (InvalidPathException e) {
        throw new AskException("source_json_path couldn't extracted!", e);
      }
    }).map(p -> {
      if (!Files.exists(p)) {
        throw new AskException("source.json couldn't be found!");
      }
      return p.toFile();
    }).orElseThrow(() -> new AskException("source_json_path couldn't extracted!"));
  }

  private static List<WordTranslation> extractTranslations(File sourceJson) {
    try {
      return OBJECT_MAPPER.readValue(sourceJson, TypeFactory.defaultInstance()
          .constructCollectionType(ArrayList.class, WordTranslation.class));
    } catch (Exception e) {
      throw new AskException(
          "Translations couldn't extracted from source.json: " + sourceJson.getAbsolutePath(), e);
    }
  }

}
