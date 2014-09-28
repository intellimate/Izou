package karlskrone.jarvis.communication;

import java.io.IOException;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;

/**
 * Created by julianbrendl on 9/28/14.
 */
public class Pipe {
    private final String id;
    private PipedInputStream inputPipe;
    private PipedOutputStream outputPipe;

    public Pipe(String id) throws IOException {
        this.id = id;
        outputPipe = new PipedOutputStream();
        inputPipe = new PipedInputStream(outputPipe);
    }

    public String getId() {
        return id;
    }

    public PipedInputStream getInputPipe() {

        return inputPipe;
    }

    public PipedOutputStream getOutputPipe() {
        return outputPipe;
    }
}
