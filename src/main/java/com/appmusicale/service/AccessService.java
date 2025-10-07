package com.appmusicale.service;

import com.appmusicale.dao.AuthorDao;
import com.appmusicale.dao.MemberDao;
import com.appmusicale.dao.MemberDaoImpl;
import com.appmusicale.model.LogSignResult;
import com.appmusicale.model.Member;
import com.appmusicale.model.Role;
import com.appmusicale.model.Status;
import com.appmusicale.util.PasswordUtils;

// SERVIZIO ACCEDI/REGISTRA MEMBRI

public class AccessService {

    MemberDao memberDao = new MemberDaoImpl();

    //REGISTRA NUOVO UTENTE
    public void registraUtente(String username, String email, String password){

        //verifica se l'email è già stata registrata
        if(memberDao.getMemberByEmail(email) != null){
            return;
        }

        //crea un nuovo utente
        Member newUser = new Member();
        newUser.setUsername(username);
        newUser.setEmail(email);
        newUser.setPassword(PasswordUtils.hashPassword(password));
        newUser.setRole(Role.USER);
        newUser.setStatus(Status.PENDING); //di default non attivo

        //inserisci utente nel database
        memberDao.insertMember(newUser);
    }


    //REGISTRA NUOVO AMMINISTRATORE
    public void registraAmministratore(String username, String email, String password){

        if(memberDao.getMemberByEmail(email) != null){
            return;
        }

        //crea un nuovo amministratore
        Member newAdmin = new Member();
        newAdmin.setUsername(username);
        newAdmin.setEmail(email);
        newAdmin.setPassword(PasswordUtils.hashPassword(password));
        newAdmin.setRole(Role.ADMIN);
        newAdmin.setStatus(Status.ACTIVE); //admin di default attivo

        //inserisci admin nel database
        memberDao.insertMember(newAdmin);
    }


    //VERIFICA LOGIN
    public LogSignResult attemptLogin(String username, String password) {

        Member member = verifyLogin(username, password); //verifica membro registrato

        if (member == null) {
            return new LogSignResult(false, "Username o password errati.", null);
        }
        if(member.getStatus()==Status.PENDING){
            return new LogSignResult(false, "La tua registrazione è in attesa di approvazione.", null);
        }
        if(member.getStatus()==Status.BANNED){
            return new LogSignResult(false, "Account disattivato da un amministratore", null);
        }
        if(member.getStatus()==Status.ACTIVE){
            return new LogSignResult(true, "", member);
        }
        return new LogSignResult(false, "Errore.", null);

    }

    //VERIFICA SIGNIN
    public LogSignResult attemptSignin(String username, String email, String password, String repPassword) {

        if(username.isEmpty() || email.isEmpty() || password.isEmpty() || repPassword.isEmpty()){
            return new LogSignResult(false, "Tutti i campi sono obbligatori.", null);
        }
        if (!emailValidator(email)){
            return new LogSignResult(false, "Inserisci un indirizzo email valido.", null);
        }
        if (password.length()<6){
            return new LogSignResult(false, "La passsword deve contenere almeno 6 caratteri.", null);
        }
        if(!repPassword.equals(password)) {
            return new LogSignResult(false, "Le password non coincidono.", null);
        }
        if(!verifyUniqueEmail(email)){
            return new LogSignResult(false, "Utente già registrato.", null);
        }
        if(!verifyUniqueUsername(username)){
            return new LogSignResult(false, "Username non disponibile.", null);
        }

        //altrimenti se tutto ok
        verifySignin(username, email, password);
        return new LogSignResult(true, "Registrazione completata!", null);

    }


    //effettua il login
    public Member login(String username, String password){

        Member member = memberDao.getMemberByUsername(username); //cerca username nel db

        if(member != null && PasswordUtils.verifyPassword(password, member.getPassword())){ //se è la sua password
            return member; //login riuscito
        }
        else{
            return null;
        }

    }

    //Cambia stato utente a ATTIVO
    public void changeStateToActive(String email){
        Member member = memberDao.getMemberByEmail(email);

        member.setStatus(Status.ACTIVE);
        memberDao.updateMember(member);
    }

    //Cambia stato utente a BANNATO
    public void changeStateToBanned(String email){
        Member member = memberDao.getMemberByEmail(email);

        member.setStatus(Status.BANNED);
        memberDao.updateMember(member);
    }


    //VERIFICA LOGIN
    public Member verifyLogin(String username, String password) { //controllo se user esiste
        Member member = login(username,password);
        if (member == null) {
            return null; //login fallito
        }
        return member;
    }

    //VERIFICA SIGNIN
    public void verifySignin(String username, String email, String password) { //controllo registrazione corretta
        registraUtente(username, email, password);
    }

    //VERIFICA MEMBRO UNICO
    public boolean verifyUniqueUsername(String username) {
        Member uname = memberDao.getMemberByUsername(username); //cerca username nel db

        if(uname!=null){ //se username già esiste
            return false;
        }
        return true;
    }
    //VERIFICA MEMBRO UNICO
    public boolean verifyUniqueEmail(String email) {
        Member umail = memberDao.getMemberByEmail(email); //cerca username nel db

        if(umail!=null){ //se username già esiste
            return false;
        }
        return true;
    }


    //FORMATO EMAIL
    public  boolean emailValidator(String email){
        return email.matches("^[\\w-.]+@([\\w-]+\\.)+[\\w-]{2,4}$");
    }


}