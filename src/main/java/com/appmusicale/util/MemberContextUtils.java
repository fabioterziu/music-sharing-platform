package com.appmusicale.util;

import com.appmusicale.model.Member;
import com.appmusicale.model.Role;

public class MemberContextUtils {
    private static Member loggedInMember;

    public static void setLoggedInMember(Member member) {
        loggedInMember = member;
    }

    public static Member getLoggedInMember() {
        return loggedInMember;
    }

    public static void clear() {
        loggedInMember = null;
    }

    //controllo se utente=utente login
    public static boolean isCurrentUserOwnerOrAdmin(Member recordOwner) {
        if (loggedInMember == null || recordOwner == null) {
            return false;
        }
        if (loggedInMember.getRole() == Role.ADMIN) {
            return true;
        }

        return loggedInMember.getId().equals(recordOwner.getId());
    }

    public static boolean isCreator(Member owner) {
        if (loggedInMember == null || owner == null) {
            return false;
        }
        return loggedInMember.getId().equals(owner.getId());
    }

    //admin
    public static boolean isCurrentAdmin() {
        return loggedInMember != null && loggedInMember.getRole() == Role.ADMIN;
    }
}
