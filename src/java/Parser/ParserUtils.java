package Parser;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashSet;
import java.util.Scanner;
import java.util.regex.Pattern;

/**
 * Classe utilitaire pour les opérations de parsing.
 */
public class ParserUtils {

    /**
     * Fonction pour obtenir une liste de chemins de fichiers RSML de l'utilisateur.
     *
     * @return HashSet des chemins de fichiers RSML.
     */
    public static HashSet<String> getUserInputPaths() {
        Scanner scanner = new Scanner(System.in);
        HashSet<String> validPaths = new HashSet<>();
        String input;

        // Regex pour matcher les fichiers .rsml, .rsml01, .rsml02, etc.
        String pattern = ".*\\.(rsml|rsml\\d{2})$";
        Pattern regex = Pattern.compile(pattern);

        System.out.println("Entrez un ou plusieurs chemins (tapez 'exit' pour terminer) :");

        while (true) {
            System.out.print("Path: ");
            input = scanner.nextLine();

            // Supprimer les guillemets ou apostrophes de l'entrée
            input = input.replace("\"", "").replace("'", "");

            // Terminer si l'utilisateur tape "exit"
            if ("exit".equalsIgnoreCase(input.trim())) {
                break;
            }

            try {
                Path path = Paths.get(input);

                // Vérifier si l'entrée correspond au pattern et est un chemin valide
                if (regex.matcher(path.toString()).matches()) {
                    validPaths.add(path.toString());
                    System.out.println("Chemin valide enregistré.");
                } else {
                    System.out.println("Chemin invalide. Veuillez entrer un chemin se terminant par .rsml ou .rsmlXX (ex. .rsml01).");
                }
            } catch (Exception e) {
                System.out.println("Erreur lors du traitement du chemin. Veuillez essayer de nouveau.");
            }
        }

        // Afficher tous les chemins valides
        System.out.println("\nChemins valides enregistrés :");
        for (String validPath : validPaths) {
            System.out.println(validPath);
        }

        scanner.close();
        return validPaths;
    }
}
