package no.eidsa.importer;

public class Application {

    public static void main(String[] args) {

        if (args.length == 3) {
            Method method;
            try {
                method = Method.valueOf(args[0]);
            } catch (Exception e) {
                printHelp();
                throw e;
            }
            String source = args[1];
            String target = args[2];

            FilesService filesService = new FilesService(new Reporter(), method);
            filesService.startWork(source, target);
            filesService.printReport();
        } else {
            printHelp();
        }
    }

    public static void printHelp() {
        System.out.println("----  The app takes 3 params ----");
        System.out.println("1: method: copy/move/dry");
        System.out.println("2: Source location");
        System.out.println("3: Target location");
        System.out.println("---- Example usage ----");
        System.out.println("media-importer copy c:/source c:/target");
    }
}
