// VIVA GUIDE: Standalone test or diagnostic program. Read fixture setup, assertions and cleanup; do not assume Maven package executes this file.
package com.queue.dao;

public class CurrentTokenTest {

    // Standalone entry point: runs this diagnostic/setup/check explicitly; Maven packaging does not automatically execute it.
    public static void main(String[] args) {

        QueueDAO queueDAO = new QueueDAO();

        // Test Examination Section
        int currentToken = queueDAO.getCurrentToken(1);

        if (currentToken > 0) {

            System.out.println("Current token found!");
            System.out.println("Service ID: 1");
            System.out.println("Current Token: " + currentToken);

        } else {

            System.out.println("No token is currently being served.");
        }
    }
}