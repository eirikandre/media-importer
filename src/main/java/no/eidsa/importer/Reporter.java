package no.eidsa.importer;

import java.util.Set;
import java.util.TreeSet;

public class Reporter {

    private Set<String> filesCopied = new TreeSet<>();
    private Set<String> filesNotCopied = new TreeSet<>();

    public void copied(String path) {
        filesCopied.add(path);
    }

    public void skippted(String path) {
        filesNotCopied.add(path);
    }

    public void printReport() {
        StringBuilder sb = new StringBuilder();
        sb.append("\n");
        sb.append("------- Successful ----------\n");
        filesCopied.forEach(line -> sb.append(addLine(line)));
        sb.append("------- Skipped    ---------- \n");
        filesNotCopied.forEach(line -> sb.append(addLine(line)));
        System.out.println(sb);
    }

    private static String addLine(String test) {
        return test + "\n";
    }

}
