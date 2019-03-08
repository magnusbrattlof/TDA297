import mcgui.*;

/**
 * Message implementation for ExampleCaster.
 *
 * @author Andreas Larsson &lt;larandr@chalmers.se&gt;
 */
public class ExampleMessage extends Message implements Comparable<ExampleMessage>{
        
    private int proposerID;
    private String status;
    private String text;
    //private int sequence;
    private int localSequence;
    private int globalSequence;
    private Boolean deliverable;

    //messagetext = "req" + ":" + messagetext + ":" + id + ":" + vectorArray[id]+1;
    public ExampleMessage(String status, String text, int sender, int proposerID, int localSequence, int globalSequence, Boolean deliverable) {
        super(sender);
        this.status = status;
        this.text = text;
        this.proposerID = proposerID;
        this.localSequence = localSequence;
        this.globalSequence = globalSequence;
        this.deliverable = deliverable;
    }

    // ugly code... but what the hell
    public ExampleMessage(int sender) {
        super(sender);
    }

    /**
     * Returns the text of the message only. The toString method can
     * be implemented to show additional things useful for debugging
     * purposes.
     */
    public String getText() {
        return text;
    }
    
    public int getProposerID() {
        return proposerID;
    }

    /**
     * Returns vectorVlaue of message, this info together with id of
     * sender complets all info needed for vector clock 
     * 
    public int getSequence() {
        return sequence;
    } */

    public String getStatus() {
        return status;
    }

    public int getLocalSequence() {
        return localSequence;
    }

    public int getGlobalSequence() {
        return globalSequence;
    }

    public Boolean getDeliverable() {
        return deliverable;
    }

    @Override
    public int compareTo(ExampleMessage eMessage) {
        if (eMessage.getLocalSequence() == this.localSequence) {
            return this.sender - eMessage.getSender();
        } else {
            return this.localSequence - eMessage.getLocalSequence();
        }
    }

    public static final long serialVersionUID = 0;
}
