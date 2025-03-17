package com.paymybuddy;

import io.github.cdimascio.dotenv.Dotenv;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class PayMyBuddyApplication {

    public static void main(String[] args) {
        // Charger le fichier .env
        Dotenv dotenv = Dotenv.configure()
            .ignoreIfMissing()  // Pour ne pas planter si le fichier n'existe pas
            .load();

        // Optionnel : transférer les variables dans les propriétés système
        System.setProperty("DB_USERNAME", dotenv.get("DB_USERNAME"));
        System.setProperty("DB_PASSWORD", dotenv.get("DB_PASSWORD"));

        SpringApplication.run(PayMyBuddyApplication.class, args);
    }

}
