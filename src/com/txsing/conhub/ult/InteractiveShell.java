/**
 *
 * @author txsing
 */
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.LinkedList;
import java.util.List;

/**
 * Automates an interactive shell process
 */
public class InteractiveShell {

    /**
     * Kickstart the example
     */
    public static void main(String[] args) {
        //Full path that the interactive process will run from
        //String startAt = "/";
        //A collection of interactions that we expect
        InteractionPoint[] interactions = {
            new InteractionPoint("",
            "cat RUNING.txt")
        };
        //The command that will be automated and its parameters 
        String cmd = "docker";
        String arg0 = "run";
        String arg1 = "-i";
        String arg2 = "tomcat:9";
        String arg3 = "bash";

        new InteractiveShell(new LinkedList<String>() {
            private static final long serialVersionUID = 1L;

            {
                add(cmd);
                add(arg0);
                add(arg1);
                add(arg2);
                add(arg3);
            }
        }, interactions);
    }

    /**
     * Simulates an interaction
     */
    public static class InteractionPoint {

        String signalTextToMatchLastLine;
        String textToWriteWhenSignalFound;

        public InteractionPoint(String signalTextToMatchLastLine,
                String textToWriteWhenSignalFound) {
            this.signalTextToMatchLastLine = signalTextToMatchLastLine;
            this.textToWriteWhenSignalFound = textToWriteWhenSignalFound;
        }
    }

    public InteractiveShell(
            List<String> command,
            InteractiveShell.InteractionPoint[] interactions) {
        ProcessBuilder pb = new ProcessBuilder(command);
        //Set the running directory
        //pb.directory(new File(startAt));
        try {
            //Start the process
            Process p = pb.start();
            //System.out.println("hello1");
            List<String> linesRead = new LinkedList<String>();
            InputStream is = p.getInputStream();
            //Handle each interaction
            for (InteractionPoint interaction : interactions) {
                linesRead.addAll(handleInteraction(interaction, is,
                        p.getOutputStream()));
            }

            do {
                linesRead.addAll(readProcessOutputInterval(p.getInputStream()));
            } while (p.isAlive());

            // catch rest of output
            for (String line : linesRead) {
                System.out.println(line);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Waits for an interaction signal and prints the text for this interaction
     *
     * @param interaction
     * @param is - The process input stream
     * @param os - The process output stream
     * @return - line read
     * @throws IOException
     */
    private List<String> handleInteraction(
            InteractiveShell.InteractionPoint interaction,
            InputStream is, OutputStream os) throws IOException {
        boolean waitingForInputSignal = true;
        List<String> linesRead = null;
        PrintWriter pw = new PrintWriter(os);
        pw.println(interaction.textToWriteWhenSignalFound);
        pw.println("\n");
        pw.flush();
        //System.out.println("hello2");
        linesRead = readProcessOutputInterval(is);
//        while (waitingForInputSignal) {
//            linesRead = readProcessOutputInterval(is);
//            String lastLine = linesRead.get(linesRead.size() - 1);
//            if (lastLine.indexOf(interaction.signalTextToMatchLastLine) != -1) {
//                waitingForInputSignal = false;
//                PrintWriter pw = new PrintWriter(os);
//                pw.println(interaction.textToWriteWhenSignalFound);
//                pw.flush();
//            }
//            // Waiting for more output lines from the process
//            // until the input signal was received
//        }
        return linesRead;
    }

    /**
     * The process can output in several intervals. This method reads a single
     * output interval
     *
     * @return A list of lines read from the process output
     */
    private List<String> readProcessOutputInterval(InputStream is)
            throws IOException {
        int available = 0;
        // Waiting for process output
        while (available == 0) {
            available = is.available();
        }
        // process output available
        byte[] bytesRead = new byte[available];
        is.read(bytesRead);
        String stringRead = new String(bytesRead);
        // read line by line
        BufferedReader br = new BufferedReader(
                new InputStreamReader(
                        new ByteArrayInputStream(stringRead.getBytes())
                )
        );
        List<String> linesRead = new LinkedList<String>();
        while (br.ready()) {
            linesRead.add(br.readLine());
        }
        return linesRead;
    }
}
