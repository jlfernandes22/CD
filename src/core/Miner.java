package core;

import java.security.MessageDigest;
import java.util.Base64;
import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;

public class Miner {

    public static final String hashAlgorithm = "SHA3-256";

    /**
     * Multithreaded Proof-of-Work
     */
    public static int getNonce(String msg, int difficulty) {
        AtomicInteger solution = new AtomicInteger(0);
        AtomicInteger ticket = new AtomicInteger(new Random().nextInt(100_000));

        int nThreads = Runtime.getRuntime().availableProcessors();
        MinerThread[] threads = new MinerThread[nThreads];

        for (int i = 0; i < nThreads; i++) {
            threads[i] = new MinerThread(solution, ticket, msg, difficulty);
            threads[i].start();
        }

        // wait until one thread finds the solution
        while (solution.get() == 0) {
            Thread.yield();
        }

        return solution.get();
    }

    //::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::

    private static class MinerThread extends Thread {

        private final AtomicInteger solution;
        private final AtomicInteger ticket;
        private final String message;
        private final int difficulty;
        private final String zeros;

        MinerThread(AtomicInteger solution, AtomicInteger ticket,
                    String msg, int difficulty) {
            this.solution = solution;
            this.ticket = ticket;
            this.message = msg;
            this.difficulty = difficulty;
            this.zeros = String.format("%0" + difficulty + "d", 0);
        }

        @Override
        public void run() {
            while (solution.get() == 0) {
                int nonce = ticket.getAndIncrement();
                String hash = getHash(message + nonce);

                if (hash.startsWith(zeros)) {
                    solution.compareAndSet(0, nonce);
                }
            }
        }
    }

    //::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::

    public static String getHash(String msg) {
        try {
            MessageDigest md = MessageDigest.getInstance(hashAlgorithm);
            md.update(msg.getBytes());
            return Base64.getEncoder().encodeToString(md.digest());
        } catch (Exception e) {
            return "";
        }
    }
}
