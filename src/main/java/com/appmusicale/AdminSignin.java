package com.appmusicale;

import java.util.Scanner;

import com.appmusicale.dao.MemberDaoImpl;
import com.appmusicale.service.AccessService;

//REGISTRA ADMIN DA TERMINALE

public class AdminSignin {


    public static void main(String[] args) {
        String username, email, password;

        Scanner scanner = new Scanner(System.in);

        System.out.print("Username admin: ");
        username = scanner.nextLine();

        System.out.print("Email admin: ");
        email = scanner.nextLine();

        System.out.print("Password admin: ");
        password = scanner.nextLine();

        AccessService accessService = new AccessService();
        accessService.registraAmministratore(username, email, password);


        System.out.println("Amministratore registrato.");
    }
}
