package fr.cgi.learninghub.swarm.utils;

import org.passay.CharacterRule;
import org.passay.EnglishCharacterData;
import org.passay.PasswordGenerator;

import java.util.Arrays;
import java.util.List;

public class UserUtils {

    public static String generateRandomValue(int length, List<CharacterRule> rules) {
        var generator = new PasswordGenerator();
        return generator.generatePassword(length, rules);
    }

    public static String generateUsername() {
        var length = 8;
        List<CharacterRule> rules = List.of(
                // username is full of lower case
                new CharacterRule(EnglishCharacterData.LowerCase, length)
        );

        return generateRandomValue(8, rules);
    }

    public static String generatePassword() {
        List<CharacterRule> rules = Arrays.asList(
                // at least one upper-case character
                new CharacterRule(EnglishCharacterData.UpperCase, 1),
                // at least one lower-case character
                new CharacterRule(EnglishCharacterData.LowerCase, 1),
                // at least one digit character
                new CharacterRule(EnglishCharacterData.Digit, 1)
        );

        return generateRandomValue(12, rules);
    }
}
