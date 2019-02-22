
import mcgui.*;

/**
 * Message implementation for ExampleCaster.
 *
 * @author Andreas Larsson &lt;larandr@chalmers.se&gt;
 */
public class ExampleMessage extends Message implements Comparable<ExampleMessage>{

    String text;
    int seqNum;
    int phase;
    int proposed;
    int proposer;
    int globalSeq;

    public ExampleMessage(int sender, String text, int seqNum, int phase, int proposed, int proposer, int globalSeq) {
        super(sender);
        this.text = text;
        this.seqNum = seqNum;
        this.phase = phase;
        this.proposed = proposed;
        this.proposer = proposer;
        this.globalSeq = globalSeq;
    }

    /**
     * Returns the text of the message only. The toString method can
     * be implemented to show additional things useful for debugging
     * purposes.
     */
    public String getText() {
        return text;
    }

    public int getSeq() {
        return seqNum;
    }

    public static final long serialVersionUID = 0;


    @Override
    public int compareTo(ExampleMessage eMsg) {
        if (eMsg.globalSeq == this.globalSeq) {
            return this.sender - eMsg.getSender();
        } else {
            return this.globalSeq - eMsg.globalSeq;
        }
    }
}
