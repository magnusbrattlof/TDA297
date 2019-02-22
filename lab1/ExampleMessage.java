
import mcgui.*;

/**
 * Message implementation for ExampleCaster.
 *
 * @author Andreas Larsson &lt;larandr@chalmers.se&gt;
 */
public class ExampleMessage extends Message implements Comparable<ExampleMessage>{
        
    private String status;
    private String text;
    private int sequence;
    private int localSequence;

    //messagetext = "req" + ":" + messagetext + ":" + id + ":" + vectorArray[id]+1;
    public ExampleMessage(String status, String text, int sender, int sequence, int localSequence) {
        super(sender);
        this.status = status;
        this.text = text;
        this.sequence = sequence;
        this.localSequence = localSequence;
    }

    /**
     * Returns the text of the message only. The toString method can
     * be implemented to show additional things useful for debugging
     * purposes.
     */
    public String getText() {
        return text;
    }
    
    /**
     * Returns vectorVlaue of message, this info together with id of
     * sender complets all info needed for vector clock 
     */
    public int getSequence() {
        return sequence;
    }

    public String getStatus() {
        return status;
    }

    public int getLocalSequence() {
        return localSequence;
    }

    @Override
    public int compareTo(ExampleMessage eMessage) {
        if (eMessage.getSequence() == this.sequence) {
            return this.sender - eMessage.getSender();
        } else {
            return this.sequence - eMessage.getSequence();
        }
    }

    public static final long serialVersionUID = 0;
}
